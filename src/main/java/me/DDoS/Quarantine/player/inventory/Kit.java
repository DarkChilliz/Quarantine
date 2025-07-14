package me.DDoS.Quarantine.player.inventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import me.DDoS.Quarantine.util.QUtil;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class Kit {

    private final List<ItemStack> kit = new ArrayList<>();

    private Kit(List<String> itemStrings) {
        for (String itemString : itemStrings) {
            try {
                String[] s1 = itemString.split("\\Q|\\E"); // Split parts of item string
                String[] s2 = s1[0].split("-");
                ItemStack item = QUtil.toItemStack(s2[0], Integer.parseInt(s2[1]));

                if (item != null) {
                    for (int i = 1; i < s1.length; i++) {
                        applyEnchantment(item, s1[i]); // Apply each enchantment
                    }
                    kit.add(item);
                }

            } catch (Exception ex) {
                ex.printStackTrace(); // Always log exceptions during parsing
            }
        }
    }

    private void applyEnchantment(ItemStack item, String enchantmentString) {
        try {
            String[] s = enchantmentString.split("-");
            String enchantmentKey = s[0].toLowerCase(); // e.g. "sharpness"
            int level = Integer.parseInt(s[1]);

            Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(enchantmentKey));
            if (enchantment != null && enchantment.canEnchantItem(item) && !item.containsEnchantment(enchantment)) {
                item.addEnchantment(enchantment, level);
            }

        } catch (Exception ex) {
            ex.printStackTrace(); // Show errors for misformatted enchantments
        }
    }

    public void giveKit(Player player) {
        Inventory inventory = player.getInventory();
        for (ItemStack item : kit) {
            inventory.addItem(item.clone()); // Use clone to avoid sharing reference
        }
        player.updateInventory(); // Optional in recent versions but safe
    }

    public static Map<String, Kit> loadKits(ConfigurationSection config) {
        Map<String, Kit> kits = new HashMap<>();
        for (String kitName : config.getKeys(false)) {
            kits.put(kitName, new Kit(config.getStringList(kitName)));
        }
        return kits;
    }
}
