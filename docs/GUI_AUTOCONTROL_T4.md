# MangoSSH — GUI coherente con AutoControL (noviembre 2026)

## Referencias públicas verificadas

- **MangoSSH-PUBLIC**, rama de partida `feat/restore-original-compose-gui`, compilación Android #57 verde (09/11/2026).
- **AUTOCONTROL-PUBLIC**, rama visual `feat/tablet-industrial-gui-mangossh`, commit `a871f549da`, compilación Android #40 verde (09/11/2026).
- Referencia cromática exacta: `app/src/main/java/com/josxtm/autocontrol/community/AutoThemes.kt`.

## Diseño entregado

- T4 Gris Industrial **predeterminado**: acento rojo `#A92E35`, fondo `#E5E7E9` y panel `#F8F8F7`, exactamente como el tema de AutoControL.
- Diez temas T1–T10 con sus nombres y valores originales; selector dentro de Configuración. La preferencia se almacena en el mismo DataStore, con una clave adicional y valor inicial 3.
- Modo Claro/Oscuro/Sistema se conserva; T6 presenta su modo Noche Suave.
- **Tablet horizontal**: barra lateral de 132 dp con las opciones Conexión, Diagnóstico, Historial y Ajustes. El terminal se fusiona con Conexión y se muestra en un panel central; perfiles a su izquierda y consultas rápidas a su derecha.
- **Móvil**: navegación inferior previa sin perder la pantalla Terminal, diseño responsivo, sin columnas estrechas de tablet.
- Las consultas rápidas son únicamente las operaciones de lectura ya incluidas en `Diagnostics.commands`; no se crea una nueva consola sin control ni se alteran las funciones SSH.
- Se mantiene el terminal original, con texto blanco en el tema correspondiente, ANSI/PTY, selección de perfiles, importación local de JSON y diagnóstico de CT.

## Compatibilidad / datos

Sin cambios en el `applicationId`, Room (`profiles` y `history`), cifrado de credenciales por Android Keystore, contrato de red, formato JSON CT, ni lógica de SSH. No borrar datos al modificar la apariencia. El nuevo índice de tema usa una clave opcional `tabletTheme` con valor por defecto 3.

Las **APK CI debug** no tienen todavía una firma de producción compartida: **no garantizan actualizar instalaciones existentes**. Conservar la app anterior hasta verificar firma y compatibilidad. Ningún cambio se fusiona automáticamente a `main`.

## Diferencias conscientes respecto a AutoControL

AutoControL tiene seis módulos (CT, Diario, Documentos, Copias, SNIFFER, Config.); MangoSSH es el cliente especializado, por lo que conserva sus pantallas específicas (Conexión, Diagnóstico, Historial, Ajustes), no copia módulos ajenos. La iconografía V10 de AutoControL no está incluida como biblioteca exportable en su rama pública: se utilizan los iconos Material existentes de MangoSSH con el mismo color y densidad. **No se declara paridad píxel a píxel sin comparar capturas reales en tablet y móvil.**

## Aceptación

- [ ] GitHub Actions verde (tests, lint, APK, emulador, privacidad)
- [ ] Abrir en tablet horizontal de 13,4 pulgadas, verificar que terminal y entrada funcionan
- [ ] Verificar navegación móvil y temas T1–T10
- [ ] Verificar perfiles SSH, CT y resultados de diagnóstico después de la instalación
- [ ] Comprobar una actualización firmada compatible antes de sustituir una instalación con datos
- [ ] Aprobación visual explícita antes de fusionar en `main`
