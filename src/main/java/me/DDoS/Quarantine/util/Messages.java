package me.DDoS.Quarantine.util;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import me.DDoS.Quarantine.Quarantine;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 *
 * @author DDoS
 */
public class Messages {

	private static final Map<String, String> messages = new HashMap<String, String>();

	public static void load(Quarantine plugin) {

		final FileConfiguration config = new YamlConfiguration();
		final File messageConfig = new File("plugins/Quarantine/messages.yml");

		if (!messageConfig.exists()) {
			Quarantine.log.info("[Quarantine] Couldn't find the message config. Using defaults from jar.");
			// Save the default messages.yml from the jar to disk
			plugin.saveResource("messages.yml", false);
		}

		try {
			config.load(messageConfig); // <-- FIXED LINE
		} catch (Exception ex) {
			Quarantine.log.info("[Quarantine] Couldn't load the message config: " + ex.getMessage());
			return;
		}

		for (String key : config.getKeys(false)) {
			messages.put(key, config.getString(key, ""));
		}

		for (Entry<String, String> entry : messages.entrySet()) {
			final String[] splits = entry.getValue().split("\\Q%\\E");

			for (int i = 0; i < splits.length; i++) {
				for (ChatColor color : ChatColor.values()) {
					if (splits[i].equalsIgnoreCase(color.name())) {
						splits[i] = color.toString();
					}
				}
			}

			entry.setValue(QUtil.join(splits));
		}
	}

	public static String get(String key) {

		final String message = messages.get(key);
		return message == null ? "-- Message Not Found! --" : message;

	}

	public static String get(String key, String... vars) {

		String message = get(key);

		for (int i = 0; i < vars.length; i++) {

			message = message.replaceAll("\\Qvar-" + i + "\\E", vars[i]);

		}

		return message;

	}

	public static String get(String key, int... vars) {

		String[] varStrings = new String[vars.length];

		for (int i = 0; i < vars.length; i++) {
			
			varStrings[i] = Integer.toString(vars[i]);
			
		}

		return get(key, varStrings);
		
	}
}
