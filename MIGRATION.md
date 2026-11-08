# Estado de migración — Fase 2

- Proyecto público creado sin arrastrar ningún historial privado.
- Conexión genérica SSH, pinning SHA-256 con confirmación explícita, diagnóstico de solo lectura, terminal protegido y perfil local.
- No se ha importado código privado ni información interna de redes o dispositivos.
- `applicationId` público conservado: `io.github.josxtm.mangossh.community`.
- Pendiente: soporte de claves privadas y Android Keystore, perfiles múltiples, terminal xterm, pruebas de integración SSH instrumentadas y firma release propia.
- El proyecto privado original no se modifica. No se transfieren sus perfiles ni credenciales. Nunca afirmar compatibilidad de actualizaciones entre ambas apps.
