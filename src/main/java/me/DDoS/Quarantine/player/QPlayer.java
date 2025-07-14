package me.DDoS.Quarantine.player;

import me.DDoS.Quarantine.util.Messages;
import me.DDoS.Quarantine.util.QUtil;
import me.DDoS.Quarantine.zone.Zone;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public abstract class QPlayer extends PlayerData {

    protected QPlayer(Player player, Zone zone) {
        super(player, zone);
    }

    protected QPlayer(PlayerData player) {
        super(player);
    }

    public abstract boolean join();

    public abstract boolean enter();

    public abstract boolean commandLeave();

    public abstract void quitLeave();

    public abstract void forceLeave();

    public abstract boolean teleportLeave(PlayerTeleportEvent event);

    public abstract void dieLeave(PlayerDeathEvent event);

    public abstract PlayerType getType();

    public void removeMoney(int amount) {
        money -= amount;
        QUtil.tell(player, Messages.get("MoneySubstractionSuccess", amount));
        tellMoney();
    }

    public void giveMoney(int amount) {
        money += amount;
        QUtil.tell(player, Messages.get("MoneyAdditionSuccess", amount));
        tellMoney();
    }

    public void tellKits() {
        QUtil.tell(player, Messages.get("KitListHeader"));
        QUtil.tell(player, QUtil.toString(zone.getKitNames()));
    }

    public void tellKeys() {
        QUtil.tell(player, Messages.get("KeyList", QUtil.toString(keys)));
    }

    public void tellScore() {
        QUtil.tell(player, Messages.get("Score", score));
    }

    public void tellMoney() {
        QUtil.tell(player, Messages.get("CurrentMoney", money));
    }
}
