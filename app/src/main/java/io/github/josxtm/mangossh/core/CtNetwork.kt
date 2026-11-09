package io.github.josxtm.mangossh.core

/** User-supplied site layout only: no operational constants ship in source. */
data class CtOffset(val name: String, val offset: Int)
data class CtRuntimeConfig(val wanOffset: Int, val targets: List<CtOffset>) {
    fun validate() {
        require(wanOffset in 1..65535) { "Offset WAN inválido" }
        require(targets.size in 1..16) { "Se necesitan de 1 a 16 destinos autorizados" }
        require(targets.all { it.offset in 1..65535 && it.name.matches(Regex("[\\p{L}\\p{N} ._-]{1,32}")) }) {
            "Nombre u offset de destino inválido"
        }
        require(targets.map { it.offset }.distinct().size == targets.size && targets.none { it.offset == wanOffset }) {
            "Direcciones WAN y destinos duplicadas"
        }
        require(targets.map { it.name.lowercase() }.distinct().size == targets.size) {
            "Nombres de destinos duplicados"
        }
    }
}
data class CtTarget(val name: String, val offset: Int, val ip: String)
data class CtPlan(
    val matricula: String, val lanBase: String, val netmask: String,
    val prefix: Int, val mangoIp: String, val targets: List<CtTarget>
)
object CtPlanning {
    private fun number(ip: String): Long {
        require(Validation.ipv4(ip)) { "Dirección IPv4 inválida" }
        return ip.split('.').fold(0L) { current, part -> (current shl 8) or part.toLong() }
    }
    private fun dotted(value: Long) = (3 downTo 0).joinToString(".") {
        ((value ushr (it * 8)) and 255).toString()
    }
    fun build(matricula: String, lanBase: String, netmask: String, local: CtRuntimeConfig? = null): CtPlan {
        require(matricula.isNotBlank() && matricula.length <= 80) { "Identificador inválido" }
        val config = requireNotNull(local) { "Importa primero los parámetros locales de red" }
        config.validate()
        val base = number(lanBase)
        val mask = number(netmask)
        val hostMask = mask.inv() and 0xffffffffL
        require(hostMask >= 3 && (hostMask and (hostMask + 1)) == 0L) {
            "Máscara no contigua o sin direcciones utilizables"
        }
        val network = base and mask
        val broadcast = network or hostMask
        fun at(offset: Int): String {
            val value = base + offset.toLong()
            require(value <= 0xffffffffL && (value and mask) == network &&
                    value > network && value < broadcast && value != base) {
                "El offset $offset sale de la subred o coincide con una dirección reservada"
            }
            return dotted(value)
        }
        val mango = at(config.wanOffset)
        val destinations = config.targets.map { CtTarget(it.name, it.offset, at(it.offset)) }
        return CtPlan(matricula, lanBase, netmask, java.lang.Long.bitCount(mask), mango, destinations)
    }
}

/** Mango WAN only, never the remote site router. Requires an explicit UI confirmation. */
object CtCommands {
    private const val rollback = "/etc/config/mangossh-ct-wan-rollback"
    private val deviceCheck = """
        device=$(uci -q get network.wan.device || uci -q get network.wan.ifname || true)
        test -n "${'$'}device" || { echo 'WAN sin interfaz física identificable' >&2; exit 8; }
        case "${'$'}device" in *[!a-zA-Z0-9_.:-]* ) exit 8 ;; esac
        ip link show dev "${'$'}device" >/dev/null 2>&1 || { echo 'Interfaz WAN no disponible' >&2; exit 8; }
    """.trimIndent()
    fun preflight() = DiagnosticCommand("ct-preflight", "CT", "Comprobar WAN Mango", """
        set -eu
        test "$(id -u)" = 0 || { echo 'Necesita root' >&2; exit 8; }
        model=$(cat /tmp/sysinfo/model 2>/dev/null || true)
        case "${'$'}model" in *GL-MT300N-V2*|*GL-MT300N* ) ;; *) echo 'Modelo Mango no verificado' >&2; exit 8 ;; esac
        command -v uci >/dev/null 2>&1
        command -v ip >/dev/null 2>&1
        test "$(uci -q get network.wan)" = interface
        test ! -e $rollback || { echo 'Restauración WAN anterior pendiente' >&2; exit 8; }
        ${'$'}deviceCheck
        printf 'CT_PREFLIGHT_OK\n'
    """.trimIndent())
    fun apply(plan: CtPlan): DiagnosticCommand {
        require(Validation.ipv4(plan.mangoIp) && Validation.ipv4(plan.netmask))
        require(plan.prefix in 1..30 && plan.targets.isNotEmpty())
        return DiagnosticCommand("ct-apply", "CT", "Configurar solo WAN Mango", """
            set -eu
            umask 077
            test "$(id -u)" = 0
            test ! -e $rollback || { echo 'Restauración WAN pendiente' >&2; exit 8; }
            test "$(uci -q get network.wan)" = interface
            ${'$'}deviceCheck
            cp -p /etc/config/network $rollback
            uci set network.wan.proto='static'
            uci set network.wan.ipaddr='${plan.mangoIp}'
            uci set network.wan.netmask='${plan.netmask}'
            uci set network.wan.defaultroute='0'
            uci set network.wan.peerdns='0'
            uci -q delete network.wan.gateway || true
            uci -q delete network.wan.dns || true
            uci commit network
            /etc/init.d/network reload || { cp -p $rollback /etc/config/network; /etc/init.d/network reload || true; exit 8; }
            printf 'CT_WAN_CONFIG_OK\n'
        """.trimIndent())
    }
    fun verify(plan: CtPlan): DiagnosticCommand {
        require(Validation.ipv4(plan.mangoIp) && Validation.ipv4(plan.netmask) && plan.prefix in 1..30)
        return DiagnosticCommand("ct-verify", "CT", "Verificar WAN Mango", """
            set -eu
            test -f $rollback
            test "$(uci -q get network.wan.proto)" = static
            test "$(uci -q get network.wan.ipaddr)" = '${plan.mangoIp}'
            test "$(uci -q get network.wan.netmask)" = '${plan.netmask}'
            ${'$'}deviceCheck
            ip -4 addr show dev "${'$'}device" | grep -F 'inet ${plan.mangoIp}/${plan.prefix}' >/dev/null
            printf 'CT_WAN_OK\n'
        """.trimIndent())
    }
    fun ping(plan: CtPlan, target: CtTarget): DiagnosticCommand {
        require(plan.targets.contains(target) && Validation.ipv4(target.ip)) { "Destino no autorizado" }
        return DiagnosticCommand("ct-ping", "CT", "Ping destino",
            "if command -v ping >/dev/null 2>&1; then ping -c 4 -W 2 ${target.ip}; else exit 127; fi")
    }
    fun restore() = DiagnosticCommand("ct-restore", "CT", "Restaurar WAN Mango", """
        set -eu
        test "$(id -u)" = 0
        test -f $rollback || { echo 'No existe copia WAN para restaurar' >&2; exit 8; }
        cp -p $rollback /etc/config/network
        /etc/init.d/network reload
        rm -f $rollback
        printf 'CT_WAN_RESTORE_OK\n'
    """.trimIndent())
}
