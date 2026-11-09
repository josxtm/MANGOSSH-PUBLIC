# MangoSSH-PUBLIC — configuración local de CT

La aplicación usa datos importados por el técnico en su propio dispositivo. Este repositorio no publica direcciones operativas, reglas de instalaciones ni claves.

## Importación y búsqueda

1. Abre **Diagnóstico → CT · Red local** y selecciona el catálogo JSON de centros desde Android.
2. Selecciona **Importar parámetros de red locales (cifrados)**. El segundo archivo contiene únicamente offsets y nombres definidos por el técnico. El contenido se valida, normaliza y cifra con Android Keystore.
3. Busca por matrícula; la aplicación carga la dirección base del catálogo. Introduce o verifica la máscara real y comprueba el plan de destinos antes de confirmar cualquier cambio.
4. La concesión de lectura del catálogo persiste durante actualizaciones compatibles. Si se revoca o el archivo desaparece, reimpórtalo. Un fallo de importación no destruye el catálogo anterior.

## Formato JSON local, ejemplo estrictamente ficticio

Este es un ejemplo de laboratorio; los offsets de una instalación real se introducen **solo en el dispositivo**, nunca en GitHub.

~~~json
{
  "version": 1,
  "wanOffset": 42,
  "targets": [
    {"name": "Equipo A", "offset": 51},
    {"name": "Equipo B", "offset": 52},
    {"name": "Equipo C", "offset": 53},
    {"name": "Equipo D", "offset": 54},
    {"name": "Equipo E", "offset": 55},
    {"name": "Equipo F", "offset": 56},
    {"name": "Equipo G", "offset": 57}
  ]
}
~~~

El motor admite entre 1 y 16 destinos; todos los offsets han de ser diferentes y estar dentro de la subred. Se rechazan red, broadcast y conflictos con la WAN del Mango. No hay máscara o topología codificada de fábrica.

## Secuencia segura de diagnóstico

1. Conecta Android al Wi-Fi del propio GL.iNet GL-MT300N-V2. Conecta el cable WAN solo a una red autorizada.
2. Conecta por SSH al Mango como root y confirma explícitamente la clave del servidor.
3. Revisa en pantalla los destinos calculados y acepta la confirmación de configuración. La preparación verifica el modelo, herramientas, dispositivo WAN y la ausencia de un cambio pendiente.
4. Se guarda en el Mango, con permisos restringidos, una copia de su archivo de red. Solo se cambia la sección network.wan del Mango. Nunca se modifica un router o equipo remoto.
5. Se ejecutan cuatro paquetes ICMP por destino y se muestra el resultado. Puede repetirse la comprobación.
6. Pulsa **Restaurar WAN original** antes de abandonar el CT. La copia permanece en el router hasta restaurar, incluso tras reinicio. Si queda copia pendiente, una nueva preparación se bloquea.

Una respuesta ICMP negativa no demuestra por sí sola una avería. El cambio WAN se ha preparado para pruebas físicas, pero CI y emulador no prueban el firmware ni la conexión física: requiere verificación real con un equipo autorizado.

## Actualizaciones y protección de datos

La misma identidad Android, la misma firma y un versionCode creciente son imprescindibles para actualizar una instalación sin desinstalar. Las APK públicas de prueba usan la firma de depuración del runner; NO se deben presentar como actualización garantizada de otras compilaciones o de la app privada. Hace falta una firma estable protegida por el propietario para distribuir actualizaciones reales; no se guardan claves de firma en este repositorio.

El JSON y la configuración se conservan únicamente dentro de Android; la configuración normalizada se cifra con Android Keystore y no se copia a GitHub. Room preserva perfiles e historial con esquema v1; no se borran ajustes ni identidades SSH. No publiques exportaciones de diagnósticos que contengan datos de instalaciones.

La aceptación final requiere comparación visual de la GUI real y una conexión SSH / restauración WAN física antes de fusionar el PR en main.
