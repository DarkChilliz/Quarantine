package me.DDoS.Quarantine;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.net.URL;
import java.net.URLConnection;

import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import me.DDoS.Quarantine.command.*;
import me.DDoS.Quarantine.zone.ZoneLoader;
import me.DDoS.Quarantine.listener.QListener;
import me.DDoS.Quarantine.zone.Zone;
import me.DDoS.Quarantine.permission.Permissions;
import me.DDoS.Quarantine.permission.PermissionsHandler;
import me.DDoS.Quarantine.util.EconomyConverter;
import me.DDoS.Quarantine.zone.region.provider.*;
import me.DDoS.Quarantine.util.Metrics;
import me.DDoS.Quarantine.util.Metrics.Graph;
import me.DDoS.Quarantine.util.Metrics.Plotter;
import me.DDoS.Quarantine.player.QPlayer;
import me.DDoS.Quarantine.util.Messages;

import net.milkbowl.vault.economy.Economy;

public final class Quarantine extends JavaPlugin {

    public static final Logger log = Logger.getLogger("Minecraft");
    private final Map<String, Zone> zones = new HashMap<>();
    private ZoneLoader zoneLoader;
    private FileConfiguration config;
    private Permissions permissions;
    private RegionProvider regionProvider;
    private EconomyConverter economyConverter;

    public Quarantine() {
        checkLibs();
    }

    @Override
    public void onEnable() {
        CommandExecutor ace = new AdminCommandExecutor(this);
        CommandExecutor pce = new PlayerCommandExecutor(this);
        CommandExecutor sce = new SetupCommandExecutor(this);

        getCommand("qload").setExecutor(ace);
        getCommand("qunload").setExecutor(ace);
        getCommand("qrespawnmobs").setExecutor(ace);

        getCommand("qjoin").setExecutor(pce);
        getCommand("qenter").setExecutor(pce);
        getCommand("qleave").setExecutor(pce);
        getCommand("qmoney").setExecutor(pce);
        getCommand("qkeys").setExecutor(pce);
        getCommand("qscore").setExecutor(pce);
        getCommand("qzones").setExecutor(pce);
        getCommand("qplayers").setExecutor(pce);
        getCommand("qkit").setExecutor(pce);
        getCommand("qkits").setExecutor(pce);
        getCommand("qconvertmoney").setExecutor(pce);

        getCommand("qsetlobby").setExecutor(sce);
        getCommand("qsetentrance").setExecutor(sce);

        checkFiles();

        config = getConfig();
        zoneLoader = ZoneLoader.loadZoneLoader(this, config);

        findRegionProvider();
        setupEconomyConverter();

        permissions = new PermissionsHandler(this).getPermissions();
        getServer().getPluginManager().registerEvents(new QListener(this), this);

        loadStartUpZones();
        startMetrics();

        Messages.load(this);

        log.info("[Quarantine] Plugin enabled. v" + getDescription().getVersion() + ", by DDoS");
    }

    @Override
    public void onDisable() {
        unloadAllZones();
        zones.clear();
        log.info("[Quarantine] Plugin disabled. v" + getDescription().getVersion() + ", by DDoS");
    }

    public boolean hasZoneLoader() {
        return zoneLoader != null;
    }

    public ZoneLoader getZoneLoader() {
        return zoneLoader;
    }

    public Collection<Zone> getZones() {
        return zones.values();
    }

    public boolean hasZone(String zoneName) {
        return zones.containsKey(zoneName.toLowerCase());
    }

    public Zone getZoneByName(String zoneName) {
        return zones.get(zoneName.toLowerCase());
    }

    public Zone addZone(String zoneName, Zone zone) {
        return zones.put(zoneName.toLowerCase(), zone);
    }

    public void removeZone(String zoneName) {
        zones.remove(zoneName.toLowerCase());
    }

    public boolean isQuarantinePlayer(String playerName) {
        for (Zone zone : getZones()) {
            if (zone.hasPlayer(playerName)) {
                return true;
            }
        }
        return false;
    }

    public QPlayer getQuarantinePlayer(String playerName) {
        for (Zone zone : getZones()) {
            if (zone.hasPlayer(playerName)) {
                return zone.getPlayer(playerName);
            }
        }
        return null;
    }

    public Zone getZoneByPlayer(String playerName) {
        for (Zone zone : getZones()) {
            if (zone.hasPlayer(playerName)) {
                return zone;
            }
        }
        return null;
    }

    public Zone getZoneByMob(LivingEntity living) {
        for (Zone zone : getZones()) {
            if (zone.hasMob(living)) {
                return zone;
            }
        }
        return null;
    }

    public Zone getZoneByLocation(Location loc) {
        for (Zone zone : getZones()) {
            if (zone.isInZone(loc)) {
                return zone;
            }
        }
        return null;
    }

    public Zone getZoneByChunk(Chunk chunk) {
        for (Zone zone : getZones()) {
            if (zone.isInZone(chunk)) {
                return zone;
            }
        }
        return null;
    }

    public boolean hasRegionProvider() {
        return regionProvider != null;
    }

    public RegionProvider getRegionProvider() {
        return regionProvider;
    }

    public EconomyConverter getEconomyConverter() {
        return economyConverter;
    }

    public boolean hasEconomyConverter() {
        return economyConverter != null;
    }

    public Permissions getPermissions() {
        return permissions;
    }

    public FileConfiguration getConfigFile() {
        return config;
    }

    private void findRegionProvider() {
        String providerName = config.getString("Region_Provider_Plugin");

        if (providerName != null && providerName.equalsIgnoreCase("worldguard")) {
            regionProvider = new WorldGuardRegionProvider();
            log.info("[Quarantine] Will be using WorldGuard as a region provider.");
        } else {
            log.warning("[Quarantine] No region provider defined! This plugin will not work!");
        }
    }

    private void setupEconomyConverter() {
        ConfigurationSection configSec = config.getConfigurationSection("External_Economy_Link");

        if (!configSec.getBoolean("enabled")) return;

        Plugin plugin = getServer().getPluginManager().getPlugin("Vault");
        if (plugin == null) {
            log.info("[Quarantine] No Vault detected. Economy converter disabled.");
            return;
        }

        RegisteredServiceProvider<Economy> economyProvider = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (economyProvider == null) {
            log.info("[Quarantine] No economy. Economy converter disabled.");
            return;
        }

        Economy economy = economyProvider.getProvider();
        if (economy == null) {
            log.info("[Quarantine] No economy. Economy converter disabled.");
            return;
        }

        float externalToInternalRate = configSec.getBoolean("external_to_internal.allow") ?
                (float) configSec.getDouble("external_to_internal.rate") : -1f;

        float internalToExternalRate = configSec.getBoolean("internal_to_external.allow") ?
                (float) configSec.getDouble("internal_to_external.rate") : -1f;

        economyConverter = new EconomyConverter(economy, externalToInternalRate, internalToExternalRate);
        log.info("[Quarantine] Vault detected. Economy converter enabled.");
    }

    private void unloadAllZones() {
        for (Zone zone : getZones()) {
            unloadZone(zone);
        }
    }

    public void unloadZone(Zone zone) {
        zone.removeAllPlayers();
        zone.saveLocations(config);
    }

    private void loadStartUpZones() {
        if (regionProvider == null || zoneLoader == null) return;

        for (String zoneToLoad : config.getStringList("Load_on_start")) {
            Zone zone = zoneLoader.loadZone(zoneToLoad);
            if (zone == null) {
                log.info("[Quarantine] Couldn't load zone " + zoneToLoad + " on start up.");
                return;
            }
            addZone(zoneToLoad, zone);
            log.info("[Quarantine] Loaded zone " + zoneToLoad + ".");
        }
    }

    private void checkFiles() {
        File mainDir = new File("plugins/Quarantine");
        if (!mainDir.exists()) mainDir.mkdir();

        File configDir = new File(mainDir.getPath() + "/config.yml");
        if (!configDir.exists()) saveDefaultConfig();
    }

    private void checkLibs() {
        File libDir = new File("plugins/Quarantine/lib");
        if (!libDir.exists()) libDir.mkdir();

        File jedisFile = new File(libDir.getPath() + "/jedis-2.0.0.jar");

        if (!jedisFile.exists()) {
            log.info("[Quarantine] Downloading 'jedis-2.0.0.jar' library.");
            if (downloadFile("https://repo1.maven.org/maven2/redis/clients/jedis/2.0.0/jedis-2.0.0.jar", jedisFile)) {
                log.info("[Quarantine] Downloading done.");
            } else {
                log.info("[Quarantine] Couldn't download 'jedis-2.0.0.jar' library.");
            }
        }
    }

    private boolean downloadFile(String urlString, File outputFile) {
        try {
            URL url = new URL(urlString);
            URLConnection connection = url.openConnection();
            DataInputStream inputStream = new DataInputStream(connection.getInputStream());
            byte[] fileData = new byte[connection.getContentLength()];
            for (int x = 0; x < fileData.length; x++) {
                fileData[x] = inputStream.readByte();
            }
            inputStream.close();
            FileOutputStream outputStream = new FileOutputStream(outputFile);
            outputStream.write(fileData);
            outputStream.close();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private void startMetrics() {
        try {
            Metrics metrics = new Metrics(this);
            Graph generalInfo = metrics.createGraph("General info");

            generalInfo.addPlotter(new Plotter("Number of Zones") {
                @Override public int getValue() {
                    return zones.size();
                }
            });

            generalInfo.addPlotter(new Plotter("Number of Players") {
                @Override public int getValue() {
                    int playerCount = 0;
                    for (Zone zone : getZones()) {
                        playerCount += zone.getNumberOfPlayers();
                    }
                    return playerCount;
                }
            });

            if (regionProvider != null) {
                Graph regionProviderInfo = metrics.createGraph("Region Providers");
                regionProviderInfo.addPlotter(new Plotter(regionProvider.getName()) {
                    @Override public int getValue() {
                        return 1;
                    }
                });
            }

            metrics.start();
        } catch (IOException ex) {
            log.info("[Quarantine] Couldn't start Plugin Metrics. Error: " + ex.getMessage());
        }
    }
}
