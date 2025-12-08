package me.PauMAVA.TTR.lang;

import me.PauMAVA.TTR.TTRCore;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;

public class LanguageManager {

    private final TTRCore plugin;
    private Locale selectedLocale;
    private FileConfiguration languageFile;

    public LanguageManager(TTRCore plugin) {
        this.plugin = plugin;
        setUpLocales();
        extractLanguageFiles();

        // Cargar el idioma seleccionado en la config
        String shortName = plugin.getConfigManager().getLocale();
        if (!setLocale(LocaleRegistry.getLocaleByShortName(shortName))) {
            plugin.getLogger().warning("Couldn't load lang " + shortName + "!");
            plugin.getLogger().warning("Loading default language lang_en...");

            // Intentar cargar inglés por defecto
            if (!setLocale(LocaleRegistry.getLocaleByShortName("en"))) {
                plugin.getLogger().severe("Failed to load default language! Plugin won't work properly!");
            } else {
                plugin.getLogger().info("Successfully loaded lang_en.yml!");
            }
        } else {
            plugin.getLogger().info("Successfully loaded '" + shortName + "' locale!");
        }
    }

    private void setUpLocales() {
        LocaleRegistry.registerLocale(new Locale("ENGLISH", "en", "PauMAVA"));
    }

    private void extractLanguageFiles() {
        for (Locale locale : LocaleRegistry.getLocales()) {
            File destination = new File(plugin.getDataFolder().getPath() + "/lang_" + locale.getShortName() + ".yml");
            String resourcePath = "/lang-packages/lang_" + locale.getShortName() + ".yml";
            InputStream in = LanguageManager.class.getResourceAsStream(resourcePath);

            if (in == null) {
                in = LanguageManager.class.getResourceAsStream("/lang_" + locale.getShortName() + ".yml");
            }

            if (in == null) {
                plugin.getLogger().warning("No se encontró el archivo de idioma interno: " + resourcePath);
                continue;
            }

            try {
                if (!destination.exists()) {
                    if (destination.getParentFile() != null) {
                        destination.getParentFile().mkdirs();
                    }
                    destination.createNewFile();
                    byte[] buffer = new byte[in.available()];
                    int bytesRead = in.read(buffer);
                    if (bytesRead != -1) {
                        try (OutputStream out = new FileOutputStream(destination)) {
                            out.write(buffer);
                        }
                    }
                }
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean setLocale(Locale locale) {
        if (locale == null) return false;
        File targetFile = new File(plugin.getDataFolder().toString() + "/lang_" + locale.getShortName() + ".yml");
        if (targetFile.exists()) {
            this.selectedLocale = locale;
            this.languageFile = YamlConfiguration.loadConfiguration(targetFile);
            return true;
        }
        return false;
    }

    public Locale getSelectedLocale() {
        return selectedLocale;
    }

    public String getString(PluginString string) {
        return getStringByPath(string.getPath());
    }

    public String getStringByPath(String path) {
        if (this.languageFile != null && this.languageFile.isSet(path)) {
            String unprocessed = this.languageFile.getString(path);
            if (unprocessed == null) {
                return "";
            }
            return unprocessed.replace("&", "§");
        }
        return "";
    }
}