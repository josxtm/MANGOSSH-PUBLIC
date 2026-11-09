# MangoSSH-PUBLIC — Android (Kotlin / Compose)

Edición pública e independiente de MangoSSH. Paquete Android: **io.github.josxtm.mangossh.community**. GUI Compose original restaurada en la rama de trabajo de la PR #5; la referencia privada se utiliza exclusivamente para comparación visual, nunca como destino de cambios.

## Funciones incluidas en la rama de restauración

- Perfiles múltiples SSH con host, puerto, usuario y contraseña o clave importada; credenciales protegidas con **Android Keystore / AES-GCM**, sin backup automático.
- Confianza explícita en la huella SHA-256 del servidor; se rechazan cambios inesperados de clave.
- Terminal interactivo con administrador activado expresamente, consola y temas claro/oscuro, navegación adaptada a móvil/tablet, icono original.
- Consultas de diagnóstico de solo lectura, comandos rápidos, historial limitado por edad y tamaño, exportación bajo control del usuario.
- Importación de catálogos CT grandes en streaming, búsqueda por matrícula, carga de dirección base, informe visible de filas válidas/rechazadas y recuperación del documento mediante permiso Android persistente.
- Importación opcional de parámetros de red definidos por el técnico **únicamente en Android**, normalizados y cifrados; validación de máscara, WAN local y destinos autorizados.
- Preparación y restauración explícitas de la WAN del **Mango propio** y pings de cuatro paquetes por destino; sin envío de cambios a los equipos remotos. No se ofrece ninguna topología ni dirección de instalación en el repositorio.

[Manual de configuración local, comprobaciones y restauración](docs/CT_CONFIG_LOCAL.md)

## Seguridad

La confianza en un servidor debe verificarse fuera de banda. Solo ejecuta comandos administrativos sobre equipos propios y redes autorizadas. Los diagnósticos y exportaciones pueden contener datos sensibles. No incluyas catálogos, contraseñas, reglas de red ni informes operativos en el repositorio.

La integración CT exige parámetros locales importados: **sin ellos está bloqueada**. La configuración de WAN solo actúa sobre el Mango y exige confirmación; el firmware y el cableado reales requieren pruebas físicas. Una respuesta ICMP negativa no implica avería.

## Compilación y aceptación

El flujo de GitHub Actions ejecuta pruebas de privacidad, Kotlin/JUnit, Android Lint, validación de APK, pruebas instrumentadas en emulador y control estructural de GUI. Las APK públicas son previews de firma de depuración, **no actualizan automáticamente la instalación privada ni garantizan actualización entre ejecuciones**.

Pendiente para una entrega release actualizable: aportar y custodiar una firma estable, probar en hardware la restauración de WAN y verificar visualmente todas las pantallas contra la GUI privada aprobada. No fusionar la PR #5 a main sin esa aceptación.

~~~sh
python3 tools/privacy_guard.py .
python3 -m unittest discover -s tests -p 'test_*.py'
gradle --no-daemon :app:testDebugUnitTest :app:lintDebug :app:assembleDebug :app:assembleDebugAndroidTest
~~~
