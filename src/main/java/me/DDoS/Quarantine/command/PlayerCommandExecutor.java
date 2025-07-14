package me.DDoS.Quarantine.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.DDoS.Quarantine.Quarantine;
import me.DDoS.Quarantine.permission.Permission;
import me.DDoS.Quarantine.player.QPlayer;
import me.DDoS.Quarantine.util.Messages;
import me.DDoS.Quarantine.util.QUtil;
import me.DDoS.Quarantine.zone.Zone;

public class PlayerCommandExecutor implements CommandExecutor {

    private final Quarantine plugin;

    public PlayerCommandExecutor(Quarantine plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String string, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used in-game.");
            return true;
        }

        final Player player = (Player) sender;

        if (!plugin.getPermissions().hasPermission(player, Permission.PLAY.getNodeString())) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        final String cmdName = cmd.getName();

        if (cmdName.equalsIgnoreCase("qjoin") && args.length >= 1) {
            if (!plugin.hasZone(args[0])) {
                QUtil.tell(player, Messages.get("ZoneNotFound"));
                return true;
            }

            if (plugin.isQuarantinePlayer(player.getName())) {
                QUtil.tell(player, Messages.get("AlreadyInZoneError"));
                return true;
            }

            plugin.getZoneByName(args[0]).joinPlayer(player);
            return true;
        }

        if (cmdName.equalsIgnoreCase("qenter")) {
            Zone zone = plugin.getZoneByPlayer(player.getName());
            if (zone != null) {
                zone.enterPlayer(player);
                return true;
            }
            QUtil.tell(player, Messages.get("NoZonesJoinedError"));
            return true;
        }

        if (cmdName.equalsIgnoreCase("qleave")) {
            Zone zone = plugin.getZoneByPlayer(player.getName());
            if (zone != null) {
                zone.leavePlayer(player);
                return true;
            }
            QUtil.tell(player, Messages.get("NoZonesJoinedError"));
            return true;
        }

        if (cmdName.equalsIgnoreCase("qmoney")) {
            QPlayer qPlayer = plugin.getQuarantinePlayer(player.getName());
            if (qPlayer != null) {
                qPlayer.tellMoney();
                return true;
            }
            QUtil.tell(player, Messages.get("NoZonesEnteredError"));
            return true;
        }

        if (cmdName.equalsIgnoreCase("qkeys")) {
            QPlayer qPlayer = plugin.getQuarantinePlayer(player.getName());
            if (qPlayer != null) {
                qPlayer.tellKeys();
                return true;
            }
            QUtil.tell(player, Messages.get("NoZonesEnteredError"));
            return true;
        }

        if (cmdName.equalsIgnoreCase("qscore")) {
            QPlayer qPlayer = plugin.getQuarantinePlayer(player.getName());
            if (qPlayer != null) {
                qPlayer.tellScore();
                return true;
            }
            QUtil.tell(player, Messages.get("NoZonesEnteredError"));
            return true;
        }

        if (cmdName.equalsIgnoreCase("qzones")) {
            QUtil.tell(player, "Zones command is disabled or GUI handler removed.");
            return true;
        }

        if (cmdName.equalsIgnoreCase("qplayers")) {
            QUtil.tell(player, "Players command is disabled or GUI handler removed.");
            return true;
        }

        if (cmdName.equalsIgnoreCase("qkit")) {
            if (args.length < 1) {
                QUtil.tell(player, Messages.get("KitNameNotProvidedError"));
                return true;
            }

            Zone zone = plugin.getZoneByPlayer(player.getName());
            if (zone != null) {
                zone.giveKit(player, args[0]);
                return true;
            }

            QUtil.tell(player, Messages.get("NoZonesEnteredError"));
            return true;
        }

        if (cmdName.equalsIgnoreCase("qkits")) {
            QPlayer qPlayer = plugin.getQuarantinePlayer(player.getName());
            if (qPlayer != null) {
                qPlayer.tellKits();
                return true;
            }
            QUtil.tell(player, Messages.get("NoZonesEnteredError"));
            return true;
        }

        if (cmdName.equalsIgnoreCase("qconvertmoney")) {
            if (!plugin.hasEconomyConverter()) {
                QUtil.tell(player, Messages.get("MoneyConversionNotEnabled"));
                return true;
            }

            if (args.length < 2) {
                QUtil.tell(player, Messages.get("NotEnoughArgumentsError"));
                return true;
            }

            if (args[0].equalsIgnoreCase("IntToExt")) {
                int amount;
                try {
                    amount = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    QUtil.tell(player, Messages.get("AmountNotANumber"));
                    return true;
                }

                Zone zone = plugin.getZoneByPlayer(player.getName());
                if (zone != null) {
                    if (plugin.getPermissions().hasPermission(player,
                            "quarantine.convertmoney." + zone.getProperties().getZoneName() + ".inttoext")) {
                        plugin.getEconomyConverter().transfertInternalToExternal(
                                zone.getPlayer(player.getName()), amount);
                    } else {
                        QUtil.tell(player, Messages.get("MoneyConversionNotPermitted"));
                    }
                } else {
                    QUtil.tell(player, Messages.get("NoZonesJoinedError"));
                }
                return true;

            } else if (args[0].equalsIgnoreCase("ExtToInt")) {
                double amount;
                try {
                    amount = Double.parseDouble(args[1]);
                } catch (NumberFormatException e) {
                    QUtil.tell(player, Messages.get("AmountNotANumber"));
                    return true;
                }

                Zone zone = plugin.getZoneByPlayer(player.getName());
                if (zone != null) {
                    if (plugin.getPermissions().hasPermission(player,
                            "quarantine.convertmoney." + zone.getProperties().getZoneName() + ".exttoint")) {
                        plugin.getEconomyConverter().transfertExternalToInternal(
                                zone.getPlayer(player.getName()), amount);
                    } else {
                        QUtil.tell(player, Messages.get("MoneyConversionNotPermitted"));
                    }
                } else {
                    QUtil.tell(player, Messages.get("NoZonesJoinedError"));
                }
                return true;

            } else {
                QUtil.tell(player, Messages.get("MoneyConversionInvalidFirstArgument"));
                return true;
            }
        }

        return false;
    }
}
