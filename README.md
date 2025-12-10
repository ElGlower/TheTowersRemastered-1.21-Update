# The Towers Remastered [1.21 Edition]

> **Versión Original (1.15-1.16):** Pau Machetti Vallverdú (PauMAVA)  
> **Actualización y Mantenimiento (1.21):** @StartCes, @Ripkyng1, @ElGlower

Esta es una **modernización completa** del clásico minijuego "The Towers" adaptada para servidores de Minecraft **1.21**, optimizada específicamente para funcionar con la API de **PaperMC**.

## 📥 Descarga
Puedes obtener la última versión compilada (.jar) directamente aquí:
> [**Descargar The Towers Remastered v1.21**](https://www.mediafire.com/file/w7d58nv239yqole/TheTowersRemastered.21-1.4-1.21.jar/file)

---

## ✨ Novedades Principales (v1.21)


### Tienda y Economía (Beacon Shop)
* **GUI Interactiva:** Nueva interfaz visual organizada en categorías (Bloques, Utilidad, Mejoras).
* **Sistema de Moneda Física:** Usa **Esmeraldas** y **Carbón** recolectados en el mapa para comerciar.
* **Catálogo Expandido:**
    * **Armamento:** Espadas, Arcos y Escudos.
    * **Utilidad:** Pociones, TNT, Ender Pearls y las nuevas **Cargas de Viento (1.21)**.
    * **Team Upgrades:** Mejoras permanentes para todo el equipo (Protección, Haste, Speed).

### Gestión de Jugadores y Lobby
* **Entrada Segura (Join System):** Sistema de "Soft-Reset" automático. Al entrar, el jugador es limpiado (inventario/efectos/vida) y puesto en modo Aventura.
* **Lobby Blindado:** Protección total contra manipulación de inventario o destrucción de bloques en la zona de espera.
* **Late Join:** Soporte para reingreso de jugadores en partidas iniciadas sin perder el balance.

### Administración y Técnica
* **Configuración Dinámica:** Persistencia de datos (`config.yml`) para ubicaciones y ajustes.
* **Sistema de Eventos (Chaos System):** Eventos aleatorios (Gravedad Lunar, Ceguera) para dinamizar la partida.
* **Reinicio Automático:** El servidor limpia la partida y reinicia el ciclo al finalizar.

---

## 📜 Comandos

| Comando | Permiso | Descripción |
| :--- | :--- | :--- |
| `/setlobby` | `ttr.admin` | **Nuevo:** Establece el punto de aparición del Lobby en tu posición. |
| `/ttrstart` | `ttr.admin` | Inicia la partida manualmente. |
| `/ttrstop` | `ttr.admin` | Detiene la partida y fuerza el reinicio. |
| `/ttrconfig` | `ttr.admin` | Cambia duración o puntos en vivo. |
| `/ttrevent` | `ttr.admin` | Controla eventos aleatorios manualmente. |
| `/ttrforcejoin` | `ttr.admin` | Fuerza a un jugador a entrar en un equipo. |
| `/ttrset` | `ttr.admin` | Configura los spawns de equipo y jaulas. |

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