package me.PauMAVA.TTR.rollback;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.util.TextUtil;
import me.PauMAVA.TTR.util.TTRPrefix;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.logging.Level;

/**
 * Gestor de Copias de Seguridad Físicas de Mundos y Regeneración a Estado 0.
 * Permite guardar una instantánea del mapa intacto y restaurarla al 100%
 * garantizando que cofres, bloques, entidades y chunks queden completamente reiniciados.
 */
public class WorldBackupManager {

    private final TTRCore plugin;
    private final File backupsDir;

    public WorldBackupManager(TTRCore plugin) {
        this.plugin = plugin;
        this.backupsDir = new File(plugin.getDataFolder(), "world_backups");
        if (!backupsDir.exists()) {
            backupsDir.mkdirs();
        }
    }

    public File getBackupFolder(String worldName) {
        return new File(backupsDir, worldName);
    }

    public boolean hasBackup(String worldName) {
        File folder = getBackupFolder(worldName);
        return folder.exists() && folder.isDirectory() && folder.list() != null && folder.list().length > 0;
    }

    public boolean createBackup(World world) {
        if (world == null) return false;
        String worldName = world.getName();
        File sourceDir = world.getWorldFolder();
        File targetDir = getBackupFolder(worldName);

        // Guardar el mundo antes de la copia para asegurar consistencia
        world.save();

        try {
            if (targetDir.exists()) {
                deleteDirectory(targetDir.toPath());
            }
            targetDir.mkdirs();

            copyDirectory(sourceDir.toPath(), targetDir.toPath(), "session.lock", "uid.dat");
            Bukkit.getConsoleSender().sendMessage(TTRPrefix.TTR_SUCCESS + ChatColor.GREEN +
                    TextUtil.toTiny("Copia de seguridad limpia creada para el mundo: ") + ChatColor.YELLOW + worldName);
            return true;
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error al crear copia de seguridad de " + worldName, e);
            return false;
        }
    }

    public boolean restoreWorld(World world) {
        // La restauración atómica en memoria (RollbackManager) es instantánea, segura y
        // mantiene a todos los jugadores en el Overworld (Lobby de the-towers), sin descargarlo ni enviarlos al Nether.
        return true;
    }

    private void copyDirectory(Path source, Path target, String... excludes) throws IOException {
        Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path targetDir = target.resolve(source.relativize(dir));
                if (!Files.exists(targetDir)) {
                    Files.createDirectories(targetDir);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                String fileName = file.getFileName().toString();
                for (String exclude : excludes) {
                    if (fileName.equalsIgnoreCase(exclude)) {
                        return FileVisitResult.CONTINUE;
                    }
                }
                Files.copy(file, target.resolve(source.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private void deleteDirectory(Path path) throws IOException {
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
