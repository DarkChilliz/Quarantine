package me.DDoS.Quarantine.player;

import java.util.ArrayList;
import me.DDoS.Quarantine.player.inventory.Kit;
import me.DDoS.Quarantine.util.Messages;
import me.DDoS.Quarantine.util.QUtil;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;

public class ZonePlayer extends QPlayer {

	private byte moneyReceivedCount = 0;
	private int moneyReceived = 0;

	public ZonePlayer(PlayerData player) {
		super(player);
	}

	public void addKey(String key, int cost) {
		if (!keys.contains(key)) {
			if (cost > money) {
				QUtil.tell(player, Messages.get("InsufficientFundsForKey"));
				return;
			}
			removeMoney(cost);
			keys.add(key);
			QUtil.tell(player, Messages.get("KeyPurchaseSuccess", key));
		} else {
			QUtil.tell(player, Messages.get("KeyAlreadyOwned"));
		}
	}

	public boolean useKey(String key, boolean oneTime) {
		boolean success = keys.contains(key);
		if (success && oneTime) {
			keys.remove(key);
			QUtil.tell(player, Messages.get("KeyOneTimeUse"));
		}
		return success;
	}

	public void addScore(int scoreToAdd) {
		score += scoreToAdd;
	}

	public void giveMoneyForKill(int amount) {
		money += amount;
		moneyReceived += amount;
		moneyReceivedCount++;

		if (moneyReceivedCount >= 5) {
			QUtil.tell(player, Messages.get("MoneyReceivedFromMobs", moneyReceived, moneyReceivedCount));
			moneyReceived = 0;
			moneyReceivedCount = 0;
		}
	}

	public void buyItem(ItemStack item, int cost) {
		if (cost > money) {
			QUtil.tell(player, Messages.get("InsufficientFundsForItem"));
			return;
		}
		QUtil.tell(player, Messages.get("ItemAddedToInventory"));
		removeMoney(cost);
		player.getInventory().addItem(item);
		player.updateInventory();
	}

	public void sellItem(ItemStack item, int cost) {
		if (!player.getInventory().contains(item)) {
			QUtil.tell(player, Messages.get("ItemForSaleNotFound", item.getType().name().toLowerCase()));
			return;
		}
		QUtil.tell(player, Messages.get("ItemRemovedFromInventory"));
		QUtil.tell(player, Messages.get("MoneyReceived", cost));
		money += cost;
		player.getInventory().removeItem(item);
		player.updateInventory();
	}

	public void addEnchantment(String enchantmentKey, int level, int cost) {
		var enchantment = org.bukkit.enchantments.Enchantment.getByKey(
				org.bukkit.NamespacedKey.minecraft(enchantmentKey.toLowerCase()));

		if (enchantment == null) {
			QUtil.tell(player, Messages.get("InvalidEnchantmentID"));
			return;
		}
		if (cost > money) {
			QUtil.tell(player, Messages.get("InsufficientFundsForEnchantment"));
			return;
		}
		var item = player.getInventory().getItemInMainHand();
		if (item == null || item.getType().isAir()) {
			QUtil.tell(player, Messages.get("EnchantmentUnappliable"));
			return;
		}
		if (!enchantment.canEnchantItem(item)) {
			QUtil.tell(player, Messages.get("EnchantmentUnappliable"));
			return;
		}
		if (item.containsEnchantment(enchantment)) {
			QUtil.tell(player, Messages.get("EnchantmentAlreadyApplied"));
			return;
		}
		item.addUnsafeEnchantment(enchantment, level);
		removeMoney(cost);
		QUtil.tell(player, Messages.get("EnchantmentAddedSuccess"));
	}

	@Override
	public boolean join() {
		QUtil.tell(player, Messages.get("AlreadyInZoneError"));
		return false;
	}

	@Override
	public boolean enter() {
		return join();
	}

	@Override
	public boolean commandLeave() {
		if (!saveData(true)) {
			QUtil.tell(player, Messages.get("DataSaveError"));
		}
		if (!saveInventory()) {
			QUtil.tell(player, Messages.get("InventorySaveError"));
		}
		clearInventory();
		player.teleport(zone.getLobby());
		player.setHealth(preGameHealth);
		player.setFoodLevel(preGameFoodLevel);
		QUtil.tell(player, Messages.get("Thanks"));
		return true;
	}

	@Override
	public void forceLeave() {
		QUtil.tell(player, Messages.get("ZoneUnloadNotice"));
		commandLeave();
	}

	@Override
	public void quitLeave() {
		saveData(true);
		saveInventory();
		clearInventory();
		player.teleport(zone.getLobby());
		player.setHealth(preGameHealth);
		player.setFoodLevel(preGameFoodLevel);
	}

	@Override
	public boolean teleportLeave(PlayerTeleportEvent event) {
		if (event.getTo().equals(zone.getLobby())) return false;
		if (zone.isInZone(event.getTo())) return false;

		event.setCancelled(true);
		QUtil.tell(player, Messages.get("LeaveZoneHelp"));
		return false;
	}

	@Override
	public void dieLeave(PlayerDeathEvent event) {
		score = 0;
		deleteInventory();

		if (zone.getProperties().keepMoneyOnDeath()) {
			int lastMoney = money;
			deletePlayerDataFile();
			if (!setData(lastMoney, 20, 20, 0, new ArrayList<>(), zone.getEntrance())) {
				QUtil.tell(player, Messages.get("DataSaveError"));
			}
		} else {
			deletePlayerDataFile();
		}

		event.getDrops().clear();
		event.setDroppedExp(0);

		if (zone.getProperties().keepXPOnRespawn()) {
			event.setKeepLevel(true);
		}
	}

	public void buyKit(Kit kit, int cost) {
		if (cost > money) {
			QUtil.tell(player, Messages.get("InsufficientFundsForKit"));
			return;
		}
		kit.giveKit(player);
		removeMoney(cost);
		QUtil.tell(player, Messages.get("KitAddedSuccess"));
	}

	@Override
	public PlayerType getType() {
		return PlayerType.ZONE_PLAYER;
	}
}
