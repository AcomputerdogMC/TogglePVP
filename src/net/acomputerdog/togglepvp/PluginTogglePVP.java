package net.acomputerdog.togglepvp;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class PluginTogglePVP extends JavaPlugin implements Listener {

    private PVPMode globalMode;
    private Map<World, PVPMode> worldMap;

    @Override
    public void onEnable() {
        if (!new File(super.getDataFolder(), "config.yml").exists()) {
            getLogger().warning("Configuration file does not exist, it will be created.");
            saveDefaultConfig();
        }

        worldMap = new HashMap<>();

        reloadConfig();
        String mode = getConfig().getString("global_mode");
        this.globalMode = PVPMode.parse(mode);

        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        globalMode = null;
        worldMap = null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (sender.hasPermission("togglepvp.command")) {
                switch (command.getName()) {
                    case "togglepvp":
                        onTogglePvp(sender, args);
                        break;
                    case "setpvp":
                        onSetPvp(sender, args);
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

    private void onTogglePvp(CommandSender sender, String[] args) {
        if (args.length > 1) {
            sender.sendMessage(ChatColor.RED + "Incorrect usage!  Use /togglepvp [world] or /setpvp <state> [world].");
        } else if (args.length == 1) {
            World world = getServer().getWorld(args[0]);
            if (world != null) {
                PVPMode mode = invertPVP(world);
                setPvpIn(sender, world, mode);
            } else {
                sender.sendMessage(ChatColor.RED + "That world could not be found!");
            }
        } else {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                World world = player.getWorld();
                PVPMode mode = invertPVP(world);
                setPvpIn(sender, world, mode);
            } else {
                sender.sendMessage(ChatColor.RED + "This command must be run as a player, or a world must be specified.");
            }
        }
    }

    private void onSetPvp(CommandSender sender, String[] args) {
        if (args.length == 1) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                PVPMode mode = parseMode(args[0]);
                if (mode != null) {
                    World world = player.getWorld();
                    setPvpIn(sender, world, mode);
                } else {
                    sender.sendMessage(ChatColor.RED + "Invalid mode!  Please use ON, OFF, MIXED, or BYPASS.");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "This command must be run as a player, or a world must be specified.");
            }
        } else if (args.length == 2) {
            PVPMode mode = parseMode(args[0]);
            if (mode != null) {
                World world = getServer().getWorld(args[1]);
                if (world != null) {
                    setPvpIn(sender, world, mode);
                } else {
                    sender.sendMessage(ChatColor.RED + "That world could not be found!");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "Invalid mode!  Please use ON, OFF, MIXED, or BYPASS.");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Incorrect usage!  Use /setpvp <state> [world] or /togglepvp [world].");
        }
    }


    private void setPvpIn(CommandSender sender, World world, PVPMode mode) {
        world.setPVP(mode.getWorldState());
        sender.sendMessage(ChatColor.YELLOW + "PVP set to: " + mode);
    }

    private PVPMode parseMode(String mode) {
        String lower = mode.toLowerCase();
        switch (lower) {
            case "ignore":
            case "bypass":
                return PVPMode.BYPASS;
            case "mixed":
            case "auto":
                return PVPMode.MIXED;
            case "on":
            case "pvp":
            case "true":
                return PVPMode.ON;
            case "off":
            case "pve":
            case "false":
                return PVPMode.OFF;
            default:
                return null;
        }
    }

    private PVPMode invertPVP(World world) {
        if (world.getPVP()) {
            return PVPMode.OFF;
        } else {
            return PVPMode.ON;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLoadWorld(WorldLoadEvent e) {
        PVPMode mode = PVPMode.parse(getConfig().getString("world." + e.getWorld().getName())); //get world setting
        if (globalMode != PVPMode.BYPASS || mode == null) { //check if world setting is missing or global should override
            mode = globalMode; //use global setting
        }
        if (mode != PVPMode.BYPASS) { //if the world-specific (or fallback) setting is BYPASS, then bypass.
            e.getWorld().setPVP(mode.getWorldState());
        }

    }

    @EventHandler(priority = EventPriority.LOW)
    public void onEntityHurt(EntityDamageByEntityEvent e) {
        PVPMode mode = worldMap.get(e.getDamager().getWorld());
        if (mode == PVPMode.MIXED) {
            if (e.getEntityType() == EntityType.PLAYER && e.getDamager().getType() == EntityType.PLAYER) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onUnloadWorld(WorldUnloadEvent e) {
        worldMap.remove(e.getWorld());
    }

    /*
    private boolean parseBool(String bool) {
        return bool.equalsIgnoreCase("true") || bool.equalsIgnoreCase("on") || bool.equalsIgnoreCase("pvp");
    }
    */
}
