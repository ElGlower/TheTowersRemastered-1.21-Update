package me.PauMAVA.TTR.modes;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.ui.DestinyTheme;
import me.PauMAVA.TTR.util.TextUtil;
import me.PauMAVA.TTR.util.TTRPrefix;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class ModeAnnouncementManager {

    private boolean announcing = false;
    private int secondsRemaining = 60;
    private BukkitTask task = null;
    private Runnable onComplete = null;
    private TeamSelectionMode currentMode = null;

    public boolean isAnnouncing() {
        return announcing;
    }

    public int getSecondsRemaining() {
        return secondsRemaining;
    }

    public TeamSelectionMode getCurrentMode() {
        return currentMode;
    }

    public void announceMode(TeamSelectionMode mode, Runnable onComplete) {
        cancel();
        this.currentMode = mode;
        this.onComplete = onComplete;

        TTRCore plugin = TTRCore.getInstance();
        int readingTime = plugin.getConfig().getInt("modes.explanation_seconds", 60);

        // Broadcast chat description
        broadcastExplanation(mode, readingTime);

        if (readingTime <= 0) {
            if (onComplete != null) onComplete.run();
            return;
        }

        this.announcing = true;
        this.secondsRemaining = readingTime;

        // Title and sound to all online players
        String title = TextUtil.color("&#FFFFFF§l" + TextUtil.toTiny("MODALIDAD: ") + mode.getDisplayName());
        String subtitle = TextUtil.color("&#EAEAEA" + TextUtil.toTiny("Lee las reglas en el chat. Inicio en ") + "&#FF2E2E§l" + readingTime + "s");

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendTitle(title, subtitle, 10, 70, 20);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1f, 1f);
            p.setLevel(readingTime);
            p.setExp(1.0f);
        }

        this.task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!announcing) {
                    cancel();
                    return;
                }

                secondsRemaining--;

                // Temporizador visual en la Barra de Experiencia
                float progress = (readingTime > 0) ? Math.max(0.0f, Math.min(1.0f, (float) secondsRemaining / readingTime)) : 0.0f;
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.setLevel(secondsRemaining);
                    p.setExp(progress);
                }

                // Action bar countdown
                String ab = TextUtil.color("&#FFFFFF⏱ " + TextUtil.toTiny("Tiempo para leer reglas: ") +
                        "&#FF2E2E§l" + secondsRemaining + "s" +
                        " &#888888[" + TextUtil.toTiny("Destiny puede forzar inicio") + "]");
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.sendActionBar(net.kyori.adventure.text.Component.text(ab));
                }

                if (secondsRemaining <= 5 && secondsRemaining > 0) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.2f);
                    }
                }

                if (secondsRemaining <= 0) {
                    finishAndRun();
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    public void forceStartNow() {
        if (!announcing) return;
        Bukkit.broadcastMessage(TTRPrefix.TTR_GAME + TextUtil.color("&#FFFFFF" + TextUtil.toTiny("Un administrador ha iniciado la fase sin esperar el tiempo de lectura.")));
        finishAndRun();
    }

    public void addSeconds(int s) {
        if (!announcing) return;
        this.secondsRemaining += s;
        if (this.secondsRemaining < 0) this.secondsRemaining = 0;
        Bukkit.broadcastMessage(TTRPrefix.TTR_ADMIN + ChatColor.YELLOW + TextUtil.toTiny("Tiempo de lectura modificado: ") +
                (s >= 0 ? ChatColor.GREEN + "+" + s : ChatColor.RED + "" + s) + "s" +
                ChatColor.GRAY + " (" + ChatColor.WHITE + secondsRemaining + "s restantes" + ChatColor.GRAY + ")");
    }

    public void setSeconds(int s) {
        if (!announcing) return;
        this.secondsRemaining = Math.max(0, s);
        Bukkit.broadcastMessage(TTRPrefix.TTR_ADMIN + ChatColor.YELLOW + TextUtil.toTiny("Tiempo de lectura establecido en: ") +
                ChatColor.GREEN + s + "s");
    }

    private void finishAndRun() {
        cancel();
        if (onComplete != null) {
            Runnable run = onComplete;
            this.onComplete = null;
            run.run();
        }
    }

    public void cancel() {
        this.announcing = false;
        if (task != null) {
            task.cancel();
            task = null;
        }
        // Restablecer la barra de experiencia a cero
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.setLevel(0);
            p.setExp(0.0f);
        }
    }

    private void broadcastExplanation(TeamSelectionMode mode, int seconds) {
        String bar = TextUtil.color("&#888888▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        Bukkit.broadcastMessage(bar);
        Bukkit.broadcastMessage(DestinyTheme.BRAND_STATIC + "  ▪  " + mode.getDisplayName());
        Bukkit.broadcastMessage(" ");

        switch (mode) {
            case STANDARD -> {
                Bukkit.broadcastMessage(ChatColor.WHITE + "» " + TextUtil.toTiny("Los jugadores eligen su equipo libremente o el sistema los equilibra."));
                Bukkit.broadcastMessage(ChatColor.WHITE + "» " + TextUtil.toTiny("Se compite sumando puntos anotando en las jaulas de las torres enemigas."));
                Bukkit.broadcastMessage(ChatColor.WHITE + "» " + TextUtil.toTiny("Gana el primer equipo en alcanzar los puntos máximos al expirar el tiempo."));
            }
            case AUCTION_DRAFT -> {
                Bukkit.broadcastMessage(ChatColor.YELLOW + "» " + TextUtil.toTiny("Fase 1: Elección de líderes por votación democrática en cada equipo."));
                Bukkit.broadcastMessage(ChatColor.YELLOW + "» " + TextUtil.toTiny("Fase 2: Los líderes elegidos pujan créditos en tiempo real por cada miembro."));
                Bukkit.broadcastMessage(ChatColor.YELLOW + "» " + TextUtil.toTiny("Fase 3: Conteo final antes de liberar a los equipos a la arena."));
                Bukkit.broadcastMessage(ChatColor.GRAY + "» " + TextUtil.toTiny("Consulta el tiempo restante en tu barra de experiencia."));
            }
        }

        Bukkit.broadcastMessage(" ");
        if (seconds > 0) {
            Bukkit.broadcastMessage(ChatColor.YELLOW + "⏱ " + TextUtil.toTiny("La fase comenzará en ") + ChatColor.GOLD + seconds + "s" + ChatColor.YELLOW + TextUtil.toTiny(" (Tiempo para leer reglas)."));
        }
        Bukkit.broadcastMessage(bar);
    }
}
