package net.acomputerdog.togglepvp;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class PluginTogglePVP extends JavaPlugin {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (sender.hasPermission("togglepvp.command")) {
                switch (command.getName()) {
                    case "togglepvp":
                        onTogglePvp(sender, command, label, args);
                        break;
                    case "setpvp":
                        onSetPvp(sender, command, label, args);
                        break;
                    default:
                        sender.sendMessage(ChatColor.RED + "Unknown command!");
                        break;
                }
            } else {
                sender.sendMessage(ChatColor.RED + "You do not have permission!");
            }
        return true;
    }

    private void onTogglePvp(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 1) {
            sender.sendMessage(ChatColor.RED + "Incorrect usage!  Use /togglepvp [world] or /setpvp <state> [world].");
        } else if (args.length == 1) {
            World world = getServer().getWorld(args[0]);
            if (world != null) {
                boolean state = !world.getPVP();
                setPvpIn(sender, world, state);
            } else {
                sender.sendMessage(ChatColor.RED + "That world could not be found!");
            }
        } else {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                World world = player.getWorld();
                boolean state = !world.getPVP();
                setPvpIn(sender, world, state);
            } else {
                sender.sendMessage(ChatColor.RED + "This command must be run as a player, or a world must be specified.");
            }
        }
    }

    private void onSetPvp(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                boolean state = parseBool(args[0]);
                World world = player.getWorld();
                setPvpIn(sender, world, state);
            } else {
                sender.sendMessage(ChatColor.RED + "This command must be run as a player, or a world must be specified.");
            }
        } else if (args.length == 2) {
            boolean state = parseBool(args[0]);
            World world = getServer().getWorld(args[1]);
            if (world != null) {
                setPvpIn(sender, world, state);
            } else {
                sender.sendMessage(ChatColor.RED + "That world could not be found!");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Incorrect usage!  Use /setpvp <state> [world] or /togglepvp [world].");
        }
    }


    private void setPvpIn(CommandSender sender, World world, boolean state) {
        world.setPVP(state);
        sender.sendMessage(ChatColor.YELLOW + "PVP set to: " + (state ? "on" : "off"));
    }

    private boolean parseBool(String bool) {
        return bool.equalsIgnoreCase("true") || bool.equalsIgnoreCase("on") || bool.equalsIgnoreCase("pvp");
    }
}
