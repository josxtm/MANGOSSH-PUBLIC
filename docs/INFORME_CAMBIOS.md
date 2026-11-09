# INFORME_CAMBIOS — MangoSSH

## Completado

1. Identificación de rama/commit del diseño privado aprobado como referencia de lectura.
2. Inventario inicial de diferencias entre las pantallas privadas y públicas.
3. `tools/gui_parity_gate.py` y pruebas sintéticas del bloqueo de componentes GUI ausentes o de reemplazos genéricos.
4. Inserción del bloqueo de paridad **después de compilar y comprobar** y **antes de publicar** un APK de prueba.
5. Documentación de estado, compilaciones y restricciones de seguridad.

## Sin realizar todavía

- Restauración completa de la interfaz y funciones aprobadas.
- Comprobación visual automatizada y manual de todas las pantallas.
- Prueba de actualización con certificado permanente.
- APK equivalente y aceptada por el propietario.

No se han copiado archivos privados ni datos de instalaciones a GitHub público. No se ha modificado el repositorio privado.

## Recuperación verificada de iconografía

- Restaurado el icono original de mango con visor y terminal como recurso de la aplicación.
- Actualizado manifiesto Android para utilizarlo como launcher.
- Excepción estricta para un único fichero y hash en la revisión pública de privacidad; pruebas de archivo aprobado y modificación rechazada.
- Sin alterar pantallas o lógica SSH existentes.

## Implementación de interfaz original

- Migradas doce fuentes Compose y SSH desde `TELECONTROL-PUBLIC` PR #7, sin acceder a datos privados.
- Configurado Gradle Kotlin 2.0.21, Material 3, Room, DataStore y JSch, y numeración pública de builds.
- Activado launcher Compose; Activity Java antigua permanece no exportada para preservar los datos existentes.
- Corregido bloqueo de estructura GUI para comprobar la ruta real de Kotlin.
- Las operaciones de red específicas de instalaciones no se reactivan con código o reglas privadas.
