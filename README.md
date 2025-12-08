# The Towers Remastered [1.21 Edition]

> **Versión Original (1.15-1.16):** Pau Machetti Vallverdú (PauMAVA)  
> **Actualización y Mantenimiento (1.21):** @StartCes, @Ripkyng1, @ElGlower

Esta es una **modernización completa** del clásico minijuego "The Towers" adaptada para servidores de Minecraft **1.21**, optimizada específicamente para funcionar con la API de **PaperMC**.

---

## ✨ Novedades Principales (v1.21)

###  Combate y Balance
* **PvP 1.9+ Nativo:** Se ha habilitado el sistema de "Cooldown" para un combate táctico (adiós al spam-click).
* **Anti-Fuego Amigo:** Bloqueo total de daño entre compañeros de equipo (incluyendo flechas y pociones).
* **Regeneración en Base:** Los jugadores recuperan salud automáticamente al defender su zona de aparición.
* **Kits Balanceados:** Equipamiento inicial ajustado (Espada de piedra, bloques, comida).

###  Nuevas Mecánicas
* **Tienda de Faro (Beacon Shop):** Al hacer clic derecho en un Faro con esmeraldas, se pueden comprar efectos globales para el equipo.
* **Sistema de Eventos (Chaos System):** Eventos aleatorios (Gravedad Lunar, Ceguera, Velocidad) ocurren cada 3 minutos para dinamizar la partida.
* **Late Join (Entrada Tardía):** Los jugadores pueden unirse a partidas ya iniciadas, elegir equipo mediante una GUI y equiparse automáticamente.

###  Administración y Técnica
* **Configuración en Vivo:** Nuevos comandos para cambiar la duración de la partida y los puntos objetivo sin reiniciar.
* **Lobby Blindado:** Protección total contra manipulación de inventario y destrucción de bloques en la fase de espera.
* **Reinicio Automático:** El servidor limpia la partida y reinicia el ciclo automáticamente al finalizar.
---

## Comandos

| Comando | Permiso | Descripción |
| :--- | :--- | :--- |
| `/ttrstart` | `ttr.admin` | Inicia la partida manualmente. |
| `/ttrstop` | `ttr.admin` | Detiene la partida y fuerza el reinicio. |
| `/ttrconfig <time/points> <valor>` | `ttr.admin` | Cambia duración o puntos en vivo. |
| `/ttrevent <tipo/auto> [on/off]` | `ttr.admin` | Controla eventos aleatorios. |
| `/ttrforcejoin <jugador> <equipo>` | `ttr.admin` | Fuerza a un jugador a entrar en un equipo. |
| `/ttrset` | `ttr.admin` | Configura los spawns y jaulas. |

---

## ⚖️ License (GNU GPL v3)

Este proyecto es software libre. Puedes redistribuirlo y/o modificarlo bajo los términos de la Licencia Pública General de GNU.

```text
 The Towers Remastered (TTR)
 Copyright (c) 2019-2021  Pau Machetti Vallverdú (Original Author)
 Copyright (c) 2025       @StartCes, @Ripkyng1, @ElGlower (1.21 Maintainers)

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program.  If not, see [http://www.gnu.org/licenses/](http://www.gnu.org/licenses/).