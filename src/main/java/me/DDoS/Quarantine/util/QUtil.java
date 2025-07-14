package me.DDoS.Quarantine.util;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class QUtil {

	public static void tell(Player player, String msg) {
		if (!msg.isEmpty()) {
			player.sendMessage(Messages.get("MessageHeader") + msg);
		}
	}

	public static boolean checkForSign(Block block) {
		BlockState state = block.getState();
		return state instanceof Sign;
	}

	public static String toString(Collection<?> objects) {
		StringBuilder sb = new StringBuilder();
		for (Object obj : objects) {
			sb.append(obj).append(", ");
		}
		if (sb.length() >= 2) {
			sb.setLength(sb.length() - 2);
		}
		return sb.toString();
	}

	public static ItemStack toItemStack(String string, int amount) {
		String[] parts = string.split(":");
		try {
			Material mat = Material.matchMaterial(parts[0]);
			if (mat == null || mat.isAir()) return null;

			ItemStack item = new ItemStack(mat, amount);
			// Optionally handle damage/data values here if needed (1.12 or older)
			return item;
		} catch (Exception e) {
			return null;
		}
	}

	public static List<ItemStack> parseItemList(String[] lines, int amount) {
		List<ItemStack> items = new ArrayList<>();
		for (String line : lines) {
			String[] splits = line.split("-");
			for (String split : splits) {
				ItemStack item = toItemStack(split, amount);
				if (item != null) {
					items.add(item);
				}
			}
		}
		return items;
	}

	public static String join(String[] strings) {
		StringBuilder sb = new StringBuilder();
		for (String s : strings) sb.append(s);
		return sb.toString();
	}

	public static boolean acceptsMobs(Block block) {
		Material mat = block.getType();
		switch (mat) {
			case STONE:
			case DIRT:
			case GRASS_BLOCK:
			case COBBLESTONE:
			case SAND:
			case SANDSTONE:
			case NETHERRACK:
			case OBSIDIAN:
			case END_STONE:
			case GLASS:
			case GLOWSTONE:
			case BRICKS:
			case BOOKSHELF:
			case PUMPKIN:
			case JACK_O_LANTERN:
			case MELON:
			case LAPIS_BLOCK:
			case LAPIS_ORE:
			case COAL_ORE:
			case IRON_ORE:
			case GOLD_ORE:
			case DIAMOND_ORE:
			case REDSTONE_ORE:
			case EMERALD_ORE:
			case QUARTZ_BLOCK:
			case NETHER_QUARTZ_ORE: // renamed from QUARTZ_ORE
			case MYCELIUM:
			case SNOW_BLOCK:
			case ICE:
			case PACKED_ICE:
			case BLUE_ICE:
			case NETHER_BRICKS:
			case MAGMA_BLOCK:
			case END_STONE_BRICKS: // renamed from END_BRICKS
			case BLACKSTONE:
			case POLISHED_BLACKSTONE:
			case BASALT:
			case POLISHED_BASALT:
				return true;
			default:
				return false;
		}
	}
}
