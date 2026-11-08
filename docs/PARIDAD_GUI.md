# PARIDAD_GUI — MangoSSH

**Resultado de auditoría: BLOQUEANTE — GUI pública no equivalente a la versión privada aprobada.**

## Referencias técnicas privadas de lectura exclusiva

- Repositorio `josxtm/MangoSSH`.
- Ramas/commits de referencia: `feat/persistent-signed-updates @ c979fb7e8da98e1bdce3ae0933f9f7410a7a0f06; fix/telecontrol-ct-v1-import @ 5d96bc6b8854eb72f6674727b0f97734453b3933`.
- No se transfieren historiales, datos, fotos, claves, recursos restringidos ni documentación operativa.

## Qué debe restaurarse

GUI Kotlin/Compose con navegación lateral y terminal original, selección de apariencia clara/oscura, icono de mango con gafas y disposición adaptada a móvil/tablet.

## Diferencias observadas

El repositorio público tiene una Activity Java con formulario simple; falta la GUI Compose aprobada, su iconografía y el diseño completo de consola. Hay ramas privadas divergentes; la selección visual debe contrastarse sin copiar funciones operativas restringidas.

## Aceptación obligatoria

- [ ] Diseño, ubicación, iconos, temas, geometría y navegación idénticos a los aprobados.
- [ ] Las funciones antiguas siguen funcionando sin pérdida de datos.
- [ ] Todas las dependencias y recursos tienen revisión de seguridad y licencias.
- [ ] Comparación visual real con datos sintéticos en pantalla móvil y tablet.
- [ ] Firma y actualizaciones seguras comprobadas; APK generada tras pruebas.
- [ ] Aprobación explícita del propietario antes de integrar `main`.

El control `tools/gui_parity_gate.py` bloquea un conjunto mínimo de fuentes que deben existir. **No es una prueba pixel-perfect** ni sustituye revisar las pantallas. Su ejecución se ubica después de compilar y antes de subir la APK para impedir nuevas previews con interfaz incorrecta.

[Trabajo pendiente](https://github.com/josxtm/MANGOSSH-PUBLIC/issues/4) · [PR de trabajo](https://github.com/josxtm/MANGOSSH-PUBLIC/pull/3).

## Recurso restaurado en esta revisión

Se incorporó **el icono original aprobado de MangoSSH** desde la referencia privada, con comprobación de bytes idénticos mediante Git blob SHA-1. La excepción de binarios de la auditoría pública solo admite esa ruta y ese contenido exacto. **La interfaz principal y las pantallas continúan pendientes de recuperación; el icono no demuestra paridad GUI completa.**
