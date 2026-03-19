package Listeners;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LocationManagerUI implements Listener, CommandExecutor {

    // ==================== 类型定义 ====================
    public enum LocationType {
        EVACUATION("撤离点", Material.END_PORTAL_FRAME),
        PLAYER_SPAWN("玩家生成点", Material.PLAYER_HEAD),
        MONSTER_SPAWN("怪物生成点", Material.ZOMBIE_HEAD);

        private final String displayName;
        private final Material icon;

        LocationType(String displayName, Material icon) {
            this.displayName = displayName;
            this.icon = icon;
        }

        public String getDisplayName() { return displayName; }
        public Material getIcon() { return icon; }
    }

    /**
     * 位置数据记录（包含位置和类型）
     */
    public record LocationData(Location location, LocationType type) {
        public LocationData(Location location, LocationType type) {
            this.location = location.clone();
            this.type = type;
        }

        @Override
        public Location location() {
            return location.clone();
        }
    }

    // ==================== 静态数据 ====================
    private static final Map<String, Map<String, LocationData>> LOCATIONS = new ConcurrentHashMap<>();
    private static JavaPlugin plugin;
    private static NamespacedKey locationKey;
    private static final String GUI_TITLE = "§8搜打撤位置管理";
    private static final String CONFIG_PATH = "locations";

    // ==================== 初始化 ====================
    public static void init(JavaPlugin instance) {
        plugin = instance;
        locationKey = new NamespacedKey(plugin, "location_id");

        // 注册事件监听
        Bukkit.getPluginManager().registerEvents(new LocationManagerUI(), plugin);

        // 注册命令执行器（需在 plugin.yml 中定义命令 "mrlocs"）
        if (plugin.getCommand("mrlocs") != null) {
            plugin.getCommand("mrlocs").setExecutor(new LocationManagerUI());
        } else {
            plugin.getLogger().warning("命令 'mrlocs' 未在 plugin.yml 中注册！");
        }

        loadFromConfig(); // 自动加载已保存的数据
    }

    // ==================== 公开 API ====================
    public static void addLocation(String worldName, String locationName, Location location, LocationType type) {
        LOCATIONS.computeIfAbsent(worldName, k -> new ConcurrentHashMap<>())
                .put(locationName, new LocationData(location, type));
    }

    public static boolean removeLocation(String worldName, String locationName) {
        Map<String, LocationData> worldLocs = LOCATIONS.get(worldName);
        return worldLocs != null && worldLocs.remove(locationName) != null;
    }

    public static LocationData getLocation(String worldName, String locationName) {
        Map<String, LocationData> worldLocs = LOCATIONS.get(worldName);
        return worldLocs != null ? worldLocs.get(locationName) : null;
    }

    public static Map<String, Location> getLocationsByType(String worldName, LocationType type) {
        Map<String, Location> result = new HashMap<>();
        Map<String, LocationData> worldLocs = LOCATIONS.get(worldName);
        if (worldLocs != null) {
            worldLocs.forEach((name, data) -> {
                if (data.type() == type) {
                    result.put(name, data.location());
                }
            });
        }
        return result;
    }

    public static Map<String, Map<String, LocationData>> getAllLocations() {
        return Collections.unmodifiableMap(LOCATIONS);
    }

    // ==================== GUI 管理 ====================
    public static void openGUI(Player player) {
        if (LOCATIONS.isEmpty()) {
            player.sendMessage("§c暂无任何已保存的地点。");
            return;
        }

        Inventory gui = Bukkit.createInventory(null, 54, GUI_TITLE);
        int slot = 0;

        for (Map.Entry<String, Map<String, LocationData>> worldEntry : LOCATIONS.entrySet()) {
            String worldName = worldEntry.getKey();
            World world = Bukkit.getWorld(worldName);

            for (Map.Entry<String, LocationData> locEntry : worldEntry.getValue().entrySet()) {
                if (slot >= 53) break;

                String locName = locEntry.getKey();
                LocationData data = locEntry.getValue();
                ItemStack item = createLocationItem(world, locName, data);
                gui.setItem(slot++, item);
            }
        }

        ItemStack statsItem = new ItemStack(Material.BOOK);
        ItemMeta statsMeta = statsItem.getItemMeta();
        statsMeta.setDisplayName("§6统计信息");
        List<String> lore = new ArrayList<>();
        lore.add("§7总地点数: §f" + countLocations());
        for (LocationType type : LocationType.values()) {
            lore.add("§7" + type.getDisplayName() + ": §f" + countByType(type));
        }
        statsMeta.setLore(lore);
        statsItem.setItemMeta(statsMeta);
        gui.setItem(53, statsItem);

        player.openInventory(gui);
    }

    private static ItemStack createLocationItem(World world, String name, LocationData data) {
        LocationType type = data.type();
        Material material = type.getIcon();
        Location loc = data.location();

        ItemStack item;
        if (type == LocationType.PLAYER_SPAWN && material == Material.PLAYER_HEAD) {
            item = new ItemStack(Material.PLAYER_HEAD);
        } else {
            item = new ItemStack(material);
        }

        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§b" + name + " §7(" + type.getDisplayName() + "§7)");

        List<String> lore = new ArrayList<>();
        lore.add("§7世界: §f" + (world != null ? world.getName() : "未知"));
        lore.add("§7坐标: §f" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
        lore.add("");
        lore.add("§a左键点击传送");
        lore.add("§c右键点击删除");

        meta.setLore(lore);
        meta.getPersistentDataContainer().set(locationKey, PersistentDataType.STRING,
                world.getName() + ":" + name);
        item.setItemMeta(meta);
        return item;
    }

    private static int countLocations() {
        return LOCATIONS.values().stream().mapToInt(Map::size).sum();
    }

    private static int countByType(LocationType type) {
        return (int) LOCATIONS.values().stream()
                .flatMap(m -> m.values().stream())
                .filter(data -> data.type() == type)
                .count();
    }

    // ==================== 事件监听 ====================
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(GUI_TITLE)) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null) return;

        ItemStack clicked = event.getCurrentItem();
        ItemMeta meta = clicked.getItemMeta();
        if (meta == null) return;

        if (event.getSlot() == 53 && clicked.getType() == Material.BOOK) {
            openGUI(player);
            return;
        }

        String identifier = meta.getPersistentDataContainer().get(locationKey, PersistentDataType.STRING);
        if (identifier == null) return;

        String[] parts = identifier.split(":", 2);
        if (parts.length < 2) return;
        String worldName = parts[0];
        String locName = parts[1];

        LocationData data = getLocation(worldName, locName);
        if (data == null) {
            player.sendMessage("§c该地点已不存在！");
            player.closeInventory();
            return;
        }

        if (event.isRightClick()) {
            removeLocation(worldName, locName);
            player.sendMessage("§a已删除 " + data.type().getDisplayName() + " §b" + locName);
            openGUI(player);
        } else if (event.isLeftClick()) {
            player.closeInventory();
            player.sendMessage("§a正在传送至 §b" + locName + "§a...");
            new BukkitRunnable() {
                @Override
                public void run() {
                    player.teleport(data.location());
                }
            }.runTask(plugin);
        }
    }

    // ==================== 持久化 ====================
    public static void loadFromConfig() {
        LOCATIONS.clear();
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection root = config.getConfigurationSection(CONFIG_PATH);
        if (root == null) return;

        for (String worldName : root.getKeys(false)) {
            ConfigurationSection worldSection = root.getConfigurationSection(worldName);
            if (worldSection == null) continue;

            Map<String, LocationData> worldMap = new ConcurrentHashMap<>();
            for (String locName : worldSection.getKeys(false)) {
                ConfigurationSection locSection = worldSection.getConfigurationSection(locName);
                if (locSection == null) continue;

                World world = Bukkit.getWorld(worldName);
                if (world == null) continue;

                double x = locSection.getDouble("x");
                double y = locSection.getDouble("y");
                double z = locSection.getDouble("z");
                float yaw = (float) locSection.getDouble("yaw");
                float pitch = (float) locSection.getDouble("pitch");
                Location location = new Location(world, x, y, z, yaw, pitch);

                String typeName = locSection.getString("type");
                LocationType type;
                try {
                    type = LocationType.valueOf(typeName);
                } catch (IllegalArgumentException e) {
                    continue;
                }

                worldMap.put(locName, new LocationData(location, type));
            }
            if (!worldMap.isEmpty()) {
                LOCATIONS.put(worldName, worldMap);
            }
        }
    }

    public static void saveToConfig() {
        FileConfiguration config = plugin.getConfig();
        config.set(CONFIG_PATH, null);

        for (Map.Entry<String, Map<String, LocationData>> worldEntry : LOCATIONS.entrySet()) {
            String worldName = worldEntry.getKey();
            for (Map.Entry<String, LocationData> locEntry : worldEntry.getValue().entrySet()) {
                String locName = locEntry.getKey();
                LocationData data = locEntry.getValue();
                Location loc = data.location();

                String path = CONFIG_PATH + "." + worldName + "." + locName;
                config.set(path + ".x", loc.getX());
                config.set(path + ".y", loc.getY());
                config.set(path + ".z", loc.getZ());
                config.set(path + ".yaw", (double) loc.getYaw());
                config.set(path + ".pitch", (double) loc.getPitch());
                config.set(path + ".type", data.type().name());
            }
        }

        plugin.saveConfig();
    }

    // ==================== 命令处理（仅 OP）====================
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§c该指令只能由玩家执行！");
            return true;
        }

        // OP 权限检查
        if (!player.isOp()) {
            player.sendMessage("§c你没有权限使用此命令！");
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "gui":
                openGUI(player);
                break;
            case "list":
                handleList(player, args);
                break;
            case "add":
                handleAdd(player, args);
                break;
            case "remove":
                handleRemove(player, args);
                break;
            case "tp":
            case "teleport":
                handleTeleport(player, args);
                break;
            default:
                sendHelp(player);
                break;
        }
        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage("§6===== 搜打撤位置管理帮助 =====§r");
        player.sendMessage("§e/mrlocs §7- 显示本帮助");
        player.sendMessage("§e/mrlocs gui §7- 打开图形界面管理位置");
        player.sendMessage("§e/mrlocs list [世界] §7- 列出所有地点（可指定世界）");
        player.sendMessage("§e/mrlocs add <名称> <类型> §7- 添加当前位置 (类型: player/monster/evacuate)");
        player.sendMessage("§e/mrlocs remove <名称> [世界] §7- 删除地点（默认当前世界）");
        player.sendMessage("§e/mrlocs tp <名称> [世界] §7- 传送到地点");
    }

    private void handleList(Player player, String[] args) {
        String worldName = args.length >= 2 ? args[1] : player.getWorld().getName();
        Map<String, Location> evacuations = getLocationsByType(worldName, LocationType.EVACUATION);
        Map<String, Location> playerSpawns = getLocationsByType(worldName, LocationType.PLAYER_SPAWN);
        Map<String, Location> monsterSpawns = getLocationsByType(worldName, LocationType.MONSTER_SPAWN);

        player.sendMessage("§6世界 " + worldName + " 的地点列表：");
        player.sendMessage("§b撤离点: " + (evacuations.isEmpty() ? "无" : String.join(", ", evacuations.keySet())));
        player.sendMessage("§a玩家生成点: " + (playerSpawns.isEmpty() ? "无" : String.join(", ", playerSpawns.keySet())));
        player.sendMessage("§c怪物生成点: " + (monsterSpawns.isEmpty() ? "无" : String.join(", ", monsterSpawns.keySet())));
    }

    private void handleAdd(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage("§c用法: /mrlocs add <名称> <类型> (类型: player/monster/evacuate)");
            return;
        }
        String name = args[1];
        String typeStr = args[2].toUpperCase();
        LocationType type;
        try {
            type = LocationType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            switch (typeStr.toLowerCase()) {
                case "player":
                case "p":
                    type = LocationType.PLAYER_SPAWN;
                    break;
                case "monster":
                case "m":
                    type = LocationType.MONSTER_SPAWN;
                    break;
                case "evacuate":
                case "evac":
                case "e":
                    type = LocationType.EVACUATION;
                    break;
                default:
                    player.sendMessage("§c未知类型！可用: player, monster, evacuate");
                    return;
            }
        }
        addLocation(player.getWorld().getName(), name, player.getLocation(), type);
        player.sendMessage("§a已添加 " + type.getDisplayName() + " §b" + name);
    }

    private void handleRemove(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§c用法: /mrlocs remove <名称> [世界]");
            return;
        }
        String name = args[1];
        String worldName = args.length >= 3 ? args[2] : player.getWorld().getName();
        if (removeLocation(worldName, name)) {
            player.sendMessage("§a已删除地点 " + name);
        } else {
            player.sendMessage("§c未找到地点 " + name + " 在世界 " + worldName);
        }
    }

    private void handleTeleport(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§c用法: /mrlocs tp <名称> [世界]");
            return;
        }
        String name = args[1];
        String worldName = args.length >= 3 ? args[2] : player.getWorld().getName();
        LocationData data = getLocation(worldName, name);
        if (data == null) {
            player.sendMessage("§c未找到地点 " + name + " 在世界 " + worldName);
            return;
        }
        player.teleport(data.location());
        player.sendMessage("§a已传送至 " + data.type().getDisplayName() + " §b" + name);
    }
}