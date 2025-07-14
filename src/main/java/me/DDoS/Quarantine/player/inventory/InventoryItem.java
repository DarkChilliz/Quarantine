package me.DDoS.Quarantine.player.inventory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public class InventoryItem implements Serializable {

    private static final long serialVersionUID = -6698956186490861187L;

    private final String materialName;
    private final int amount;
    private final int damage; // replaces 'durability'
    private final Map<String, Integer> enchantments = new HashMap<>();

    public InventoryItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            this.materialName = "AIR";
            this.amount = 0;
            this.damage = 0;
        } else {
            this.materialName = item.getType().name();
            this.amount = item.getAmount();

            ItemMeta meta = item.getItemMeta();
            if (meta instanceof Damageable damageable) {
                this.damage = damageable.getDamage();
            } else {
                this.damage = 0;
            }

            for (Entry<Enchantment, Integer> entry : item.getEnchantments().entrySet()) {
                enchantments.put(entry.getKey().getKey().getKey(), entry.getValue()); // Store as namespaced key (e.g. "sharpness")
            }
        }
    }

    public ItemStack getItem() {
        Material material = Material.matchMaterial(materialName);
        if (material == null || material == Material.AIR) return null;

        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();

        if (meta instanceof Damageable damageable) {
            damageable.setDamage(damage);
            item.setItemMeta((ItemMeta) damageable);
        }

        for (Entry<String, Integer> entry : enchantments.entrySet()) {
            Enchantment enchantment = Enchantment.getByKey(org.bukkit.NamespacedKey.minecraft(entry.getKey()));
            if (enchantment != null) {
                try {
                    item.addUnsafeEnchantment(enchantment, entry.getValue());
                } catch (IllegalArgumentException ignored) {}
            }
        }

        return item;
    }
}
