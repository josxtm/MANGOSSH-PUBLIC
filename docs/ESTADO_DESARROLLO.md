# ESTADO_DESARROLLO — MangoSSH

**Estado: RESTAURACIÓN INCOMPLETA.** La versión pública anterior no cumple la paridad de GUI.

- Repositorio privado: congelado y solo lectura.
- Repositorio público: único destino de código nuevo.
- Auditoría de referencia realizada para las fuentes GUI principales.
- Esta revisión aporta informes y comprobaciones de aceptación, no una restauración funcional de las pantallas.
- Los proyectos públicos anteriores y sus datos instalados no han sido modificados por este cambio.
- Falta portar componentes visuales autorizados, adaptar dependencias y funciones, validar datos existentes y verificar capturas reales.
- Las APK públicas anteriores son previews; no se deben presentar como equivalentes a las privadas.

Consulte `docs/PARIDAD_GUI.md`.

## Restauración Compose iniciada

Rama `feat/restore-original-compose-gui`: doce fuentes Kotlin revisadas importadas desde código público de TELECONTROL PR #7, plugins Compose/Room habilitados, icono original y Activity Compose como launcher. Se preservan `applicationId` y pantalla Java anterior (sin exportar) como respaldo de perfiles; la migración de host/usuario/puerto es no destructiva. La GUI no está aceptada hasta pasar compilación y pruebas en hardware.
