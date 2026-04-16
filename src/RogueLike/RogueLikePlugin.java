package RogueLike;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.*;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Roguelike地牢插件主控制类 - 不继承JavaPlugin，供主插件调用
 * 整合了命令注册、地牢管理、事件监听
 */
public class RogueLikePlugin implements Listener {

    private final Plugin plugin;
    private final Map<String, Dungeon> activeDungeons = new ConcurrentHashMap<>();

    public RogueLikePlugin(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * 注册命令和事件监听（供主插件调用）
     */
    public void register() {
        registerCommand();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getLogger().info("RogueLike Dungeon registered!");
    }

    /**
     * 清理所有地牢（插件卸载时调用）
     */
    public void cleanup() {
        for (Dungeon dungeon : activeDungeons.values()) {
            dungeon.clean();
        }
        activeDungeons.clear();
    }

    // ==================== 命令注册与处理 ====================
    private void registerCommand() {
        try {
            Field commandMapField = SimplePluginManager.class.getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            CommandMap commandMap = (CommandMap) commandMapField.get(plugin.getServer().getPluginManager());

            commandMap.register("rougelike", new BukkitCommand("rougelike") {
                @Override
                public boolean execute(CommandSender sender, String alias, String[] args) {
                    return handleCommand(sender, args);
                }
            });
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to register rougelike command: " + e.getMessage());
        }
    }

    private boolean handleCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("start")) {
            startDungeon(player);
        } else if (args[0].equalsIgnoreCase("clean")) {
            cleanDungeon(player);
        } else {
            player.sendMessage(ChatColor.RED + "Unknown subcommand. Use /rougelike start or /rougelike clean");
        }
        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== RogueLike Dungeon ===");
        player.sendMessage(ChatColor.YELLOW + "/rougelike start" + ChatColor.WHITE + " - 开始地牢");
        player.sendMessage(ChatColor.YELLOW + "/rougelike clean" + ChatColor.WHITE + " - 清除地牢");
    }

    // ==================== 地牢管理 ====================
    private void startDungeon(Player player) {
        String name = player.getName();

        if (activeDungeons.containsKey(name)) {
            player.sendMessage(ChatColor.RED + "You already have an active dungeon! Use /rougelike clean first.");
            return;
        }

        player.sendMessage(ChatColor.GREEN + "Generating Roguelike Dungeon...");

        Location startLoc = player.getLocation().clone();
        startLoc.setY(startLoc.getY() - 1);

        Dungeon dungeon = new Dungeon(player.getWorld(), startLoc, name);
        activeDungeons.put(name, dungeon);

        new BukkitRunnable() {
            @Override
            public void run() {
                DungeonGenerator generator = new DungeonGenerator(dungeon);
                generator.generate();
                player.sendMessage(ChatColor.GREEN + "Dungeon generated! Find the exit at the top-right corner.");
                player.sendMessage(ChatColor.YELLOW + "Right-click iron block doors to open them!");
            }
        }.runTask(plugin);
    }

    private void cleanDungeon(Player player) {
        String name = player.getName();
        Dungeon dungeon = activeDungeons.remove(name);

        if (dungeon == null) {
            player.sendMessage(ChatColor.RED + "No active dungeon found for you!");
            return;
        }

        player.sendMessage(ChatColor.YELLOW + "Cleaning dungeon...");
        new BukkitRunnable() {
            @Override
            public void run() {
                dungeon.clean();
                player.sendMessage(ChatColor.GREEN + "Dungeon cleaned!");
            }
        }.runTask(plugin);
    }

    // ==================== 事件监听 ====================
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block clicked = event.getClickedBlock();
        if (clicked == null || clicked.getType() != Dungeon.DOOR_MATERIAL) return;

        Player player = event.getPlayer();
        Dungeon dungeon = activeDungeons.get(player.getName());
        if (dungeon == null) return;

        Dungeon.Door door = dungeon.getDoorAt(clicked.getLocation());
        if (door == null || door.isOpen()) return;

        event.setCancelled(true);

        DungeonGenerator generator = new DungeonGenerator(dungeon);
        generator.openDoor(door, plugin);
    }
}