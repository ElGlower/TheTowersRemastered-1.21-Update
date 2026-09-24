package me.PauMAVA.TTR.ui;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.match.MatchStatus;
import me.PauMAVA.TTR.match.TTRMatch;
import me.PauMAVA.TTR.util.TextUtil;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ConfigScreen {

    public static void openScreen(Player player) {
        TTRCore plugin = TTRCore.getInstance();
        TTRMatch match = plugin.getCurrentMatch();
        int duration = plugin.getConfig().getInt("match.duration", 1200);
        int maxPoints = plugin.getConfig().getInt("match.maxpoints", 10);
        boolean autostart = plugin.getConfig().getBoolean("autostart.enabled", true);
        int autoCount = plugin.getConfig().getInt("autostart.count", 4);

        List<Component> pages = new ArrayList<>();

        // Pagina 1: Estado y Acciones Rapidas
        Component page1 = Component.text()
                .append(Component.text("◆ " + TextUtil.toTiny("DESTINY TOWERS") + " ◆\n", NamedTextColor.GOLD, TextDecoration.BOLD))
                .append(Component.text("-------------------\n", NamedTextColor.DARK_GRAY))
                .append(Component.text(TextUtil.toTiny("Pantalla de Config 26.x") + "\n\n", NamedTextColor.DARK_AQUA))
                .append(Component.text(TextUtil.toTiny("Estado: "), NamedTextColor.BLACK))
                .append(Component.text(TextUtil.toTiny(match != null ? match.getStatus().name() : "DETENIDO") + "\n\n", NamedTextColor.BLUE, TextDecoration.BOLD))
                .append(Component.text(TextUtil.toTiny("[ ▶ INICIAR ]") + "\n", NamedTextColor.DARK_GREEN, TextDecoration.BOLD)
                        .clickEvent(ClickEvent.runCommand("/ttr start"))
                        .hoverEvent(HoverEvent.showText(Component.text(TextUtil.toTiny("Haz clic para iniciar la partida"), NamedTextColor.GREEN))))
                .append(Component.text(TextUtil.toTiny("[ ⏹ DETENER ]") + "\n", NamedTextColor.RED, TextDecoration.BOLD)
                        .clickEvent(ClickEvent.runCommand("/ttr stop"))
                        .hoverEvent(HoverEvent.showText(Component.text(TextUtil.toTiny("Haz clic para detener la partida"), NamedTextColor.RED))))
                .append(Component.text(TextUtil.toTiny("[ ⚡ REGENERAR ]") + "\n\n", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD)
                        .clickEvent(ClickEvent.runCommand("/ttr resetmap"))
                        .hoverEvent(HoverEvent.showText(Component.text(TextUtil.toTiny("Regenerar mapa atómicamente"), NamedTextColor.LIGHT_PURPLE))))
                .append(Component.text(TextUtil.toTiny("[ ⚙ ABRIR CHEST GUI ]") + "\n", NamedTextColor.BLUE, TextDecoration.UNDERLINED)
                        .clickEvent(ClickEvent.runCommand("/ttr config gui"))
                        .hoverEvent(HoverEvent.showText(Component.text(TextUtil.toTiny("Abrir panel en interfaz de cofre"), NamedTextColor.AQUA))))
                .build();

        // Pagina 2: Ajustes de Partida
        Component page2 = Component.text()
                .append(Component.text("◆ " + TextUtil.toTiny("PARTIDA") + " ◆\n", NamedTextColor.GOLD, TextDecoration.BOLD))
                .append(Component.text("-------------------\n", NamedTextColor.DARK_GRAY))
                .append(Component.text(TextUtil.toTiny("Duración: "), NamedTextColor.BLACK))
                .append(Component.text(TextUtil.toTiny((duration / 60) + "m (" + duration + "s)") + "\n", NamedTextColor.DARK_BLUE, TextDecoration.BOLD))
                .append(Component.text(TextUtil.toTiny("[-1m] "), NamedTextColor.RED, TextDecoration.BOLD)
                        .clickEvent(ClickEvent.runCommand("/ttr config time " + Math.max(60, duration - 60)))
                        .hoverEvent(HoverEvent.showText(Component.text(TextUtil.toTiny("Restar 1 minuto (-60s)")))))
                .append(Component.text(TextUtil.toTiny("[+1m] "), NamedTextColor.DARK_GREEN, TextDecoration.BOLD)
                        .clickEvent(ClickEvent.runCommand("/ttr config time " + (duration + 60)))
                        .hoverEvent(HoverEvent.showText(Component.text(TextUtil.toTiny("Sumar 1 minuto (+60s)")))))
                .append(Component.text(TextUtil.toTiny("[+5m]") + "\n\n", NamedTextColor.GOLD, TextDecoration.BOLD)
                        .clickEvent(ClickEvent.runCommand("/ttr config time " + (duration + 300)))
                        .hoverEvent(HoverEvent.showText(Component.text(TextUtil.toTiny("Sumar 5 minutos (+300s)")))))
                .append(Component.text(TextUtil.toTiny("Puntos Victoria: "), NamedTextColor.BLACK))
                .append(Component.text(TextUtil.toTiny(String.valueOf(maxPoints)) + "\n", NamedTextColor.DARK_BLUE, TextDecoration.BOLD))
                .append(Component.text(TextUtil.toTiny("[-1] "), NamedTextColor.RED, TextDecoration.BOLD)
                        .clickEvent(ClickEvent.runCommand("/ttr config points " + Math.max(1, maxPoints - 1)))
                        .hoverEvent(HoverEvent.showText(Component.text(TextUtil.toTiny("Restar 1 punto")))))
                .append(Component.text(TextUtil.toTiny("[+1] "), NamedTextColor.DARK_GREEN, TextDecoration.BOLD)
                        .clickEvent(ClickEvent.runCommand("/ttr config points " + (maxPoints + 1)))
                        .hoverEvent(HoverEvent.showText(Component.text(TextUtil.toTiny("Sumar 1 punto")))))
                .append(Component.text(TextUtil.toTiny("[+5]") + "\n", NamedTextColor.GOLD, TextDecoration.BOLD)
                        .clickEvent(ClickEvent.runCommand("/ttr config points " + (maxPoints + 5)))
                        .hoverEvent(HoverEvent.showText(Component.text(TextUtil.toTiny("Sumar 5 puntos")))))
                .build();

        // Pagina 3: Auto-Inicio
        Component page3 = Component.text()
                .append(Component.text("◆ " + TextUtil.toTiny("AUTO-INICIO") + " ◆\n", NamedTextColor.GOLD, TextDecoration.BOLD))
                .append(Component.text("-------------------\n", NamedTextColor.DARK_GRAY))
                .append(Component.text(TextUtil.toTiny("Estado: "), NamedTextColor.BLACK))
                .append(Component.text(TextUtil.toTiny(autostart ? "ACTIVADO\n" : "DESACTIVADO\n"), (autostart ? NamedTextColor.DARK_GREEN : NamedTextColor.RED), TextDecoration.BOLD))
                .append(Component.text(TextUtil.toTiny("[ TOGGLE ESTADO ]") + "\n\n", NamedTextColor.DARK_AQUA, TextDecoration.BOLD)
                        .clickEvent(ClickEvent.runCommand("/ttr config autostart toggle"))
                        .hoverEvent(HoverEvent.showText(Component.text(TextUtil.toTiny("Alternar auto-inicio")))))
                .append(Component.text(TextUtil.toTiny("Mínimo Jugadores: "), NamedTextColor.BLACK))
                .append(Component.text(TextUtil.toTiny(String.valueOf(autoCount)) + "\n", NamedTextColor.DARK_BLUE, TextDecoration.BOLD))
                .append(Component.text(TextUtil.toTiny("[-1] "), NamedTextColor.RED, TextDecoration.BOLD)
                        .clickEvent(ClickEvent.runCommand("/ttr config autostart count " + Math.max(1, autoCount - 1)))
                        .hoverEvent(HoverEvent.showText(Component.text(TextUtil.toTiny("Reducir requerimiento")))))
                .append(Component.text(TextUtil.toTiny("[+1]") + "\n", NamedTextColor.DARK_GREEN, TextDecoration.BOLD)
                        .clickEvent(ClickEvent.runCommand("/ttr config autostart count " + (autoCount + 1)))
                        .hoverEvent(HoverEvent.showText(Component.text(TextUtil.toTiny("Aumentar requerimiento")))))
                .build();

        // Pagina 4: Spawns y Mapa
        Component page4 = Component.text()
                .append(Component.text("◆ " + TextUtil.toTiny("SPAWNS Y MAPA") + " ◆\n", NamedTextColor.GOLD, TextDecoration.BOLD))
                .append(Component.text("-------------------\n", NamedTextColor.DARK_GRAY))
                .append(Component.text(TextUtil.toTiny("Fijar en tu posición:") + "\n\n", NamedTextColor.BLACK))
                .append(Component.text(TextUtil.toTiny("[ 📍 FIJAR LOBBY ]") + "\n", NamedTextColor.GOLD, TextDecoration.BOLD)
                        .clickEvent(ClickEvent.runCommand("/ttrsetlobby"))
                        .hoverEvent(HoverEvent.showText(Component.text(TextUtil.toTiny("Establecer lobby en tu posición")))))
                .append(Component.text(TextUtil.toTiny("[ 🚩 BASE ROJA ]") + "\n", NamedTextColor.RED, TextDecoration.BOLD)
                        .clickEvent(ClickEvent.runCommand("/ttr set redspawn"))
                        .hoverEvent(HoverEvent.showText(Component.text(TextUtil.toTiny("Fijar spawn del equipo Rojo")))))
                .append(Component.text(TextUtil.toTiny("[ 🚩 BASE AZUL ]") + "\n", NamedTextColor.BLUE, TextDecoration.BOLD)
                        .clickEvent(ClickEvent.runCommand("/ttr set bluespawn"))
                        .hoverEvent(HoverEvent.showText(Component.text(TextUtil.toTiny("Fijar spawn del equipo Azul")))))
                .append(Component.text(TextUtil.toTiny("[ 🔒 JAULA ROJA ]") + "\n", NamedTextColor.DARK_RED, TextDecoration.BOLD)
                        .clickEvent(ClickEvent.runCommand("/ttr set redcage"))
                        .hoverEvent(HoverEvent.showText(Component.text(TextUtil.toTiny("Fijar jaula del equipo Rojo")))))
                .append(Component.text(TextUtil.toTiny("[ 🔒 JAULA AZUL ]") + "\n", NamedTextColor.DARK_BLUE, TextDecoration.BOLD)
                        .clickEvent(ClickEvent.runCommand("/ttr set bluecage"))
                        .hoverEvent(HoverEvent.showText(Component.text(TextUtil.toTiny("Fijar jaula del equipo Azul")))))
                .build();

        pages.add(page1);
        pages.add(page2);
        pages.add(page3);
        pages.add(page4);

        Book book = Book.book(
                Component.text(TextUtil.toTiny("Destiny Towers Config"), NamedTextColor.GOLD),
                Component.text(TextUtil.toTiny("DestinyOwners"), NamedTextColor.YELLOW),
                pages
        );

        player.openBook(book);
        player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1.0f, 1.0f);
    }
}
