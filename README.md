# MangoSSH Público — Fase 2

Edición **independiente**, paquete `io.github.josxtm.mangossh.community`, versión `0.2.0-public` (`versionCode 2`). Instalable en paralelo con aplicaciones privadas: **no las actualiza**.

## Funciones implementadas

- Cliente SSH genérico con servidor, puerto, usuario y contraseña introducidos por el usuario.
- Identidad del servidor comprobada mediante huella SHA-256. La primera conexión exige aceptación explícita; una clave de servidor cambiada se **rechaza**.
- Diagnóstico de solo lectura mediante comandos permitidos de forma explícita: `whoami`, `pwd`, `date`, `uname -a`, `df -h`.
- Terminal SSH interactivo, habilitado únicamente tras confirmar los privilegios de la sesión.
- Historial de comandos de la sesión actual, salida con tamaño máximo y perfil de host/puerto/usuario guardado localmente.
- No se guarda la contraseña ni se usa ninguna predeterminada. Datos locales del perfil y huellas no se incluyen en backups del sistema.
- Sin telemetría, archivos corporativos, catálogo de instalaciones, configuración de redes ajenas o dependencias de otros repositorios.

## Seguridad y límites

La primera huella del servidor **debe cotejarse fuera de banda**; aceptar una huella desconocida sin verificarla implica riesgo. El terminal interactivo ejecuta comandos con los privilegios concedidos por el servidor y no garantiza solo lectura. Actívalo solo sobre máquinas bajo tu control.

En esta entrega la autenticación es por contraseña **introducida para cada sesión**; el soporte de claves privadas importadas, perfiles múltiples y almacenamiento de credenciales con Android Keystore siguen pendientes. El historial es volátil y solo conserva hasta 20 comandos en memoria.

La terminal es de texto sencillo; ignora algunas secuencias ANSI y **no sustituye a un emulador xterm completo**. No hay reenvío de puertos, transferencia SFTP, comandos automáticos ni acceso a topologías específicas.

## Pruebas y compilación

```sh
python3 tools/privacy_guard.py .
python3 -m unittest discover -s tests -p 'test_*.py'
gradle --no-daemon :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
```

GitHub Actions ejecuta estas comprobaciones, inspecciona firma e identidad y publica una APK preview. La firma de depuración es efímera: no constituye una actualización estable. Falta una verificación manual de conexión SSH sobre equipo autorizado y establecer una firma permanente exclusiva de la edición pública.
