# Migración y compatibilidad de MangoSSH-PUBLIC

El repositorio público se creó sin importar el historial de Git privado. La única rama de desarrollo activa para recuperar la interfaz aprobada es la PR #5 (Compose). El repositorio privado original permanece de solo lectura.

## Identidad, datos y migración

- Se conserva el identificador de paquete Android de la edición pública, **io.github.josxtm.mangossh.community**, y se incrementa versionCode por ejecución.
- Se mantiene la base Room original (esquema 1), SharedPreferences antiguos, identidades de host, datos de sesión y almacenamiento cifrado con Keystore. Ninguna de las nuevas funciones elimina esos datos.
- El formulario público Java anterior se conserva sin launcher; un importador no destructivo puede recuperar host/puerto/usuario una única vez, pero NO recupera contraseñas ni confía automáticamente en claves de servidor.
- El catálogo CT se vuelve a indexar desde el documento de Android con permiso persistente. Los parámetros del técnico se cifran en el dispositivo.
- Una APK con firma distinta **no actualiza** una instalación previa. No desinstales una app con datos necesarios para intentar forzar una instalación. La firma release estable exige claves gestionadas fuera de GitHub.

## Pruebas pendientes que no pueden sustituirse por CI

- Contrastar la GUI con la versión privada aprobada usando datos ficticios en móvil y tablet.
- Verificar conexión SSH, confirmación de huella, pings y restauración WAN sobre un Mango real autorizado.
- Establecer la firma de distribución y efectuar prueba de actualización sobre una instalación de la **misma identidad y firma** sin pérdida de perfiles ni almacenamiento.

[Configuración local y límites](docs/CT_CONFIG_LOCAL.md)
