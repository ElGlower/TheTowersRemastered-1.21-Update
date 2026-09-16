# Destiny Towers (v1.0.0 - Paper 26.3 / 26.2)

> **Versión Original:** Pau Machetti Vallverdú (PauMAVA)  
> **Actualización, Modernización y Mantenimiento:** @StartCes, @Ripkyng1, @ElGlower

Modernización completa del clásico minijuego competitivo **Destiny Towers** adaptado para servidores de Minecraft **26.3 / 26.2** bajo la API oficial de **PaperMC** y **Java 25**.

---

## Características Principales (v1.0.0)

### 1. Panel de Configuración Dual: GUI en Cofre y Pantalla de Libro (Screen Mode)
* **Panel GUI en Cofre (/dt config o /dt gui):** Menú interactivo de 54 casillas con cristales estéticos, ajuste de duración (+1m, -1m, +5m), puntos requeridos, conmutador de auto-inicio, regeneración inmediata y fijación de puntos de spawn con un solo clic.
* **Pantalla de Libro Interactiva (/dt screen o /dt config screen):** Interfaz cliente nativa en libro (BookViewScreen vía Adventure API) con botones de texto cliqueables (ClickEvent y HoverEvent), paginación y sonidos envolventes sin requerir mods de cliente.
* **Ítems Interactivos de Lobby:** Comparador en la casilla 0 para administradores (abre configuración con clic derecho) y Estrella del Nether en la casilla 4 para todos los jugadores (selector de equipo).

### 2. Regeneración Atómica de Mapa (Rollback Engine)
* **Restauración sin Reinicio ni Recarga:** El plugin registra en tiempo real cada bloque colocado, destruido, quemado o afectado por explosiones (TNT, bolas de fuego).
* Al finalizar cada partida o ejecutando `/dt resetmap`, el mapa se reconstruye instantáneamente en su estado original sin necesidad de reiniciar el servidor.
* Limpieza integral de entidades huérfanas (ítems tirados, orbes de experiencia, flechas, bolas de fuego y cargas de viento).

### 3. Tipografía Tiny Generator Font Integral (Letras, Acentos y Números)
* Tipografía estilizada Small Caps (`ᴅᴇsᴛɪɴʏ ᴛᴏᴡᴇʀs`, `ᴋɪʟʟs`, `ᴛɪᴇᴍᴘᴏ`, `ᴇǫᴜɪᴘᴏ`, `₀₁₂₃₄₅₆₇₈₉`, etc.) en todas las letras, acentos y numerales.
* Implementado en **Scoreboard**, **Tablist**, **BossBar**, **Tienda del Faro**, **Títulos**, **Pantallas** y **Prefijos de Chat**.

### 4. Scoreboard Moderno y Limpio (Sin Números Rojos a la Derecha)
* Números de score rojos `7, 6, 5, 4, 3, 2, 1` ocultados nativamente mediante `NumberFormat.blank()` de Paper.
* Renderizado diferencial sobre el objetivo en memoria sin parpadeos, con iconos unicode limpios (◆, ▪, », ●) y sin enlaces web ni "www".
* Identidad visual premium con el sello **DestinyOwners**.

### 5. Tablist Animado con Efectos Visuales
* Efecto de onda brillante animada en la cabecera (`◆ ᴅᴇsᴛɪɴʏ ᴛᴏᴡᴇʀs ◆`).
* Footer pulsante con la marca `● ᴅᴇsᴛɪɴʏᴏᴡɴᴇʀs ●`.
* Indicadores visuales de señal de ping (` ▂▃▅`).

### 6. Tienda Táctica del Faro (Beacon Shop)
* Interfaz por categorías interactuando con los faros de cada base.
* Economía en **Esmeraldas** y **Carbón**.
* Soporte nativo para Cargas de Viento (Wind Charges), bolas de fuego con clic derecho, herramientas y proyectiles.
* Mejoras globales de equipo (Protección I-IV, Velocidad y Prisa Minera) gestionadas mediante PersistentDataContainer (PDC nativo de Paper).

### 7. Sistema de Auto-Inicio Inteligente (AutoStarter)
* Conteo regresivo automático configurable al alcanzar el límite de jugadores en lobby, con efectos sonoros (`BLOCK_NOTE_BLOCK_PLING`) y títulos en pantalla.

### 8. Eventos Dinámicos de Caos
* Eventos aleatorios periódicos (Salto V, Velocidad IV, Oscuridad, Escala Giga 2.0x, Escala Mini 0.5x, Gravedad Lunar y Lluvia de Meteoros).

---

## Comandos Administrativos (/dt o /destinytowers)

| Comando | Permiso | Descripción |
| :--- | :--- | :--- |
| `/dt`, `/destinytowers` o `/ttr` | `destinytowers.admin` | Menú principal interactivo con autocompletado inteligente. |
| `/dt config` | `destinytowers.admin` | Abre el Panel de Configuración interactivo (Chest GUI). |
| `/dt screen` | `destinytowers.admin` | Abre la Pantalla de Configuración interactiva en Libro nativo. |
| `/dt start` | `destinytowers.admin` | Inicia la partida manualmente. |
| `/dt stop` | `destinytowers.admin` | Detiene la partida actual. |
| `/dt resetmap` | `destinytowers.admin` | Ejecuta la regeneración atómica del mapa al instante. |
| `/setlobby` | `destinytowers.admin` | Fija el Lobby central en tu posición actual. |
| `/dt set <opción>` | `destinytowers.admin` | Configura spawns de equipos, jaulas y generadores de recursos. |
| `/dt config <time/points/autostart>` | `destinytowers.admin` | Modifica en caliente valores de la partida por comando o consola. |
| `/dt event <nombre/stop>` | `destinytowers.admin` | Lanza o detiene eventos de caos. |
| `/dt forcejoin <player> <equipo>` | `destinytowers.admin` | Fuerza la asignación de equipo a un jugador. |
| `/dt revive <player>` | `destinytowers.admin` | Revive a un jugador teletransportándolo a su base. |
| `/dt reload` | `destinytowers.admin` | Recarga las configuraciones (`config.yml`). |

---

## Compilación

Requisitos: **Java 25** y **Apache Maven 3.9+** (o el wrapper `./mvnw.cmd`).

```bash
mvn clean package
```

El binario optimizado se compilará en `target/DestinyTowers-1.0.0.jar`.

---

## Licencia

Destiny Towers es software libre bajo los términos de la Licencia Pública General GNU v3.
