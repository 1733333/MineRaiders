package Commands;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LocationManagerUI implements Listener, CommandExecutor {

    // ==================== 类型定义 ====================
    public enum LocationType {
        EVACUATION("撤离点", Material.END_PORTAL_FRAME),
        PLAYER_SPAWN("玩家生成点", Material.PLAYER_HEAD),
        MONSTER_SPAWN("怪物生成点", Material.ZOMBIE_HEAD),
        SPECIAL_EVACUATION("特殊撤离点", Material.END_CRYSTAL),
        SPECIAL_MONSTER_SPAWN("特殊怪物点", Material.CREEPER_HEAD);

        private final String displayName;
        private final Material icon;

        LocationType(String displayName, Material icon) {
            this.displayName = displayName;
            this.icon = icon;
        }

        public String getDisplayName() { return displayName; }
        public Material getIcon() { return icon; }
    }

    public record LocationData(Location location, LocationType type, Integer extraId) {
        public LocationData(Location location, LocationType type, Integer extraId) {
            this.location = location.clone();
            this.type = type;
            this.extraId = extraId;
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
    private static final String GUI_TITLE = "§8MineRaiders位置管理";
    private static final String CONFIG_PATH = "locations";

    // 图形化添加菜单相关
    private static final String ADD_GUI_TITLE = "§8选择添加地点类型";
    private static final Map<UUID, AddState> AWAITING_INPUT = new HashMap<>();
    private static NamespacedKey addTypeKey;

    // 独立配置文件
    private static FileConfiguration locationConfig;
    private static File locationFile;

    private enum AddStep { WAITING_NAME, WAITING_ID }
    private static class AddState {
        LocationType type;
        AddStep step;
        String name;

        AddState(LocationType type, AddStep step) {
            this.type = type;
            this.step = step;
        }
    }

    // ==================== 初始化 ====================
    public static void init(JavaPlugin instance) {
        plugin = instance;
        locationKey = new NamespacedKey(plugin, "location_id");
        addTypeKey = new NamespacedKey(plugin, "add_type");

        // 初始化配置文件对象（不创建文件）
        locationFile = new File(plugin.getDataFolder(), "location.yml");
        locationConfig = YamlConfiguration.loadConfiguration(locationFile);

        // 注册事件监听
        Bukkit.getPluginManager().registerEvents(new LocationManagerUI(), plugin);

        // 注意：不再注册 /mrlocs 命令，完全通过 /mr locs 子命令调用
        // 命令执行器由 MineRaidersCommand 负责调用，因此不需要在此处注册

        loadFromConfig(); // 尝试加载现有数据（如果文件存在）
    }

    // ==================== 公开 API ====================
    public static void addLocation(String worldName, String locationName, Location location, LocationType type) {
        addLocation(worldName, locationName, location, type, null);
    }

    public static void addLocation(String worldName, String locationName, Location location, LocationType type, Integer extraId) {
        LOCATIONS.computeIfAbsent(worldName, k -> new ConcurrentHashMap<>())
                .put(locationName, new LocationData(location, type, extraId));
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

        ItemStack item = new ItemStack(material);
        if (type == LocationType.PLAYER_SPAWN && material == Material.PLAYER_HEAD) {
            item = new ItemStack(Material.PLAYER_HEAD);
        }

        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§b" + name + " §7(" + type.getDisplayName() + "§7)");

        List<String> lore = new ArrayList<>();
        lore.add("§7世界: §f" + (world != null ? world.getName() : "未知"));
        lore.add("§7坐标: §f" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
        if (type == LocationType.SPECIAL_MONSTER_SPAWN && data.extraId() != null) {
            lore.add("§7特殊ID: §f" + data.extraId());
        }
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

    // ==================== 打开添加地点类型选择菜单 ====================
    public static void openAddGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 9, ADD_GUI_TITLE);

        // 撤离点
        ItemStack evacItem = new ItemStack(LocationType.EVACUATION.getIcon());
        ItemMeta evacMeta = evacItem.getItemMeta();
        evacMeta.setDisplayName("§b撤离点");
        evacMeta.setLore(Collections.singletonList("§7点击选择撤离点类型"));
        evacMeta.getPersistentDataContainer().set(addTypeKey, PersistentDataType.STRING, LocationType.EVACUATION.name());
        evacItem.setItemMeta(evacMeta);
        gui.setItem(0, evacItem);

        // 玩家生成点
        ItemStack playerSpawnItem = new ItemStack(LocationType.PLAYER_SPAWN.getIcon());
        ItemMeta playerMeta = playerSpawnItem.getItemMeta();
        playerMeta.setDisplayName("§a玩家生成点");
        playerMeta.setLore(Collections.singletonList("§7点击选择玩家生成点类型"));
        playerMeta.getPersistentDataContainer().set(addTypeKey, PersistentDataType.STRING, LocationType.PLAYER_SPAWN.name());
        playerSpawnItem.setItemMeta(playerMeta);
        gui.setItem(2, playerSpawnItem);

        // 怪物生成点
        ItemStack monsterSpawnItem = new ItemStack(LocationType.MONSTER_SPAWN.getIcon());
        ItemMeta monsterMeta = monsterSpawnItem.getItemMeta();
        monsterMeta.setDisplayName("§c怪物生成点");
        monsterMeta.setLore(Collections.singletonList("§7点击选择怪物生成点类型"));
        monsterMeta.getPersistentDataContainer().set(addTypeKey, PersistentDataType.STRING, LocationType.MONSTER_SPAWN.name());
        monsterSpawnItem.setItemMeta(monsterMeta);
        gui.setItem(4, monsterSpawnItem);

        // 特殊撤离点
        ItemStack specialEvacItem = new ItemStack(LocationType.SPECIAL_EVACUATION.getIcon());
        ItemMeta specialEvacMeta = specialEvacItem.getItemMeta();
        specialEvacMeta.setDisplayName("§d特殊撤离点");
        specialEvacMeta.setLore(Collections.singletonList("§7点击选择特殊撤离点类型"));
        specialEvacMeta.getPersistentDataContainer().set(addTypeKey, PersistentDataType.STRING, LocationType.SPECIAL_EVACUATION.name());
        specialEvacItem.setItemMeta(specialEvacMeta);
        gui.setItem(6, specialEvacItem);

        // 特殊怪物点
        ItemStack specialMonsterItem = new ItemStack(LocationType.SPECIAL_MONSTER_SPAWN.getIcon());
        ItemMeta specialMonsterMeta = specialMonsterItem.getItemMeta();
        specialMonsterMeta.setDisplayName("§5特殊怪物点");
        specialMonsterMeta.setLore(Collections.singletonList("§7点击选择特殊怪物点类型"));
        specialMonsterMeta.getPersistentDataContainer().set(addTypeKey, PersistentDataType.STRING, LocationType.SPECIAL_MONSTER_SPAWN.name());
        specialMonsterItem.setItemMeta(specialMonsterMeta);
        gui.setItem(8, specialMonsterItem);

        player.openInventory(gui);
    }

    // ==================== 事件监听 ====================
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();

        if (title.equals(ADD_GUI_TITLE)) {
            event.setCancelled(true);
            if (!(event.getWhoClicked() instanceof Player player)) return;
            if (event.getCurrentItem() == null) return;

            ItemStack clicked = event.getCurrentItem();
            ItemMeta meta = clicked.getItemMeta();
            if (meta == null) return;

            String typeName = meta.getPersistentDataContainer().get(addTypeKey, PersistentDataType.STRING);
            if (typeName == null) return;

            LocationType type;
            try {
                type = LocationType.valueOf(typeName);
            } catch (IllegalArgumentException e) {
                player.sendMessage("§c无效的地点类型！");
                return;
            }

            player.closeInventory();

            AWAITING_INPUT.put(player.getUniqueId(), new AddState(type, AddStep.WAITING_NAME));
            player.sendMessage("§a请输入该地点的名称（在聊天框输入），输入 §ccancel §a取消：");
            return;
        }

        if (!title.equals(GUI_TITLE)) return;
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

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!AWAITING_INPUT.containsKey(uuid)) return;

        event.setCancelled(true);
        AddState state = AWAITING_INPUT.get(uuid);
        String message = event.getMessage().trim();

        if (message.equalsIgnoreCase("cancel")) {
            AWAITING_INPUT.remove(uuid);
            player.sendMessage("§c已取消添加地点。");
            return;
        }

        if (state.step == AddStep.WAITING_NAME) {
            if (message.isEmpty() || message.length() > 30) {
                player.sendMessage("§c名称不能为空且长度不能超过30！请重新输入（输入 §ccancel §a取消）：");
                return;
            }

            String worldName = player.getWorld().getName();
            if (getLocation(worldName, message) != null) {
                player.sendMessage("§c在当前世界已存在名为 " + message + " 的地点，请使用其他名称。");
                player.sendMessage("§a请重新输入名称（输入 §ccancel §a取消）：");
                return;
            }

            if (state.type == LocationType.SPECIAL_MONSTER_SPAWN) {
                state.name = message;
                state.step = AddStep.WAITING_ID;
                AWAITING_INPUT.put(uuid, state);
                player.sendMessage("§a请输入该特殊怪物点的ID（整数），输入 §ccancel §a取消：");
                return;
            } else {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        addLocation(player.getWorld().getName(), message, player.getLocation(), state.type);
                        player.sendMessage("§a成功添加 " + state.type.getDisplayName() + " §b" + message);
                        AWAITING_INPUT.remove(uuid);
                    }
                }.runTask(plugin);
                return;
            }
        }

        if (state.step == AddStep.WAITING_ID && state.type == LocationType.SPECIAL_MONSTER_SPAWN) {
            int id;
            try {
                id = Integer.parseInt(message);
            } catch (NumberFormatException e) {
                player.sendMessage("§cID必须是整数！请重新输入（输入 §ccancel §a取消）：");
                return;
            }

            final int finalId = id;
            new BukkitRunnable() {
                @Override
                public void run() {
                    addLocation(player.getWorld().getName(), state.name, player.getLocation(), state.type, finalId);
                    player.sendMessage("§a成功添加 " + state.type.getDisplayName() + " §b" + state.name + " (ID: " + finalId + ")");
                    AWAITING_INPUT.remove(uuid);
                }
            }.runTask(plugin);
        }
    }

    // ==================== 持久化（使用独立 location.yml）====================
    public static void loadFromConfig() {
        LOCATIONS.clear();
        if (!locationFile.exists()) {
            // 文件不存在，不加载任何数据
            return;
        }
        if (locationConfig == null) {
            plugin.getLogger().warning("locationConfig 未初始化，无法加载配置！");
            return;
        }
        ConfigurationSection root = locationConfig.getConfigurationSection(CONFIG_PATH);
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

                Integer extraId = null;
                if (locSection.contains("extraId")) {
                    extraId = locSection.getInt("extraId");
                }

                worldMap.put(locName, new LocationData(location, type, extraId));
            }
            if (!worldMap.isEmpty()) {
                LOCATIONS.put(worldName, worldMap);
            }
        }
    }

    public static void saveToConfig() {
        if (locationConfig == null) {
            plugin.getLogger().warning("locationConfig 未初始化，无法保存配置！");
            return;
        }
        locationConfig.set(CONFIG_PATH, null);

        for (Map.Entry<String, Map<String, LocationData>> worldEntry : LOCATIONS.entrySet()) {
            String worldName = worldEntry.getKey();
            for (Map.Entry<String, LocationData> locEntry : worldEntry.getValue().entrySet()) {
                String locName = locEntry.getKey();
                LocationData data = locEntry.getValue();
                Location loc = data.location();

                String path = CONFIG_PATH + "." + worldName + "." + locName;
                locationConfig.set(path + ".x", loc.getX());
                locationConfig.set(path + ".y", loc.getY());
                locationConfig.set(path + ".z", loc.getZ());
                locationConfig.set(path + ".yaw", (double) loc.getYaw());
                locationConfig.set(path + ".pitch", (double) loc.getPitch());
                locationConfig.set(path + ".type", data.type().name());
                if (data.extraId() != null) {
                    locationConfig.set(path + ".extraId", data.extraId());
                } else {
                    locationConfig.set(path + ".extraId", null);
                }
            }
        }

        try {
            // 确保父目录存在
            if (!locationFile.getParentFile().exists()) {
                locationFile.getParentFile().mkdirs();
            }
            locationConfig.save(locationFile);
        } catch (IOException e) {
            plugin.getLogger().severe("无法保存 location.yml: " + e.getMessage());
        }
    }

    public static void backupLocationConfig() {
        if (plugin == null) return;
        File dataFolder = plugin.getDataFolder();
        File backupDir = new File(dataFolder, "backup");
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }
        String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmm").format(new java.util.Date());
        File locationFile = new File(dataFolder, "location.yml");
        if (locationFile.exists()) {
            File backupFile = new File(backupDir, "location_" + timestamp + ".yml");
            try {
                java.nio.file.Files.copy(locationFile.toPath(), backupFile.toPath(),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                plugin.getLogger().info("已备份 location.yml 到 " + backupFile.getName());
            } catch (IOException e) {
                plugin.getLogger().warning("备份 location.yml 失败: " + e.getMessage());
            }
        }
    }

    // ==================== 命令处理 ====================
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§c该指令只能由玩家执行！");
            return true;
        }

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
            case "addgui":
                openAddGUI(player);
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
        player.sendMessage("§6===== MineRaiders 地点管理帮助 =====");
        player.sendMessage("§e/mr locs §7- 显示本帮助");
        player.sendMessage("§e/mr locs gui §7- 打开图形界面管理地点");
        player.sendMessage("§e/mr locs addgui §7- 打开添加地点菜单");
        player.sendMessage("§e/mr locs list [世界] §7- 列出所有地点（可指定世界）");
        player.sendMessage("§e/mr locs add <名称> <类型> [id] §7- 添加当前位置");
        player.sendMessage("   类型: player / monster / evacuate / special_evac / special_monster");
        player.sendMessage("   special_monster 需要额外指定 id");
        player.sendMessage("§e/mr locs remove <名称> [世界] §7- 删除地点（默认当前世界）");
        player.sendMessage("§e/mr locs tp <名称> [世界] §7- 传送到地点");
    }

    private void handleList(Player player, String[] args) {
        String worldName = args.length >= 2 ? args[1] : player.getWorld().getName();
        Map<String, Location> evacuations = getLocationsByType(worldName, LocationType.EVACUATION);
        Map<String, Location> playerSpawns = getLocationsByType(worldName, LocationType.PLAYER_SPAWN);
        Map<String, Location> monsterSpawns = getLocationsByType(worldName, LocationType.MONSTER_SPAWN);
        Map<String, Location> specialEvacs = getLocationsByType(worldName, LocationType.SPECIAL_EVACUATION);
        Map<String, Location> specialMonsters = getLocationsByType(worldName, LocationType.SPECIAL_MONSTER_SPAWN);

        player.sendMessage("§6世界 " + worldName + " 的地点列表：");
        player.sendMessage("§b撤离点: " + (evacuations.isEmpty() ? "无" : String.join(", ", evacuations.keySet())));
        player.sendMessage("§d特殊撤离点: " + (specialEvacs.isEmpty() ? "无" : String.join(", ", specialEvacs.keySet())));
        player.sendMessage("§a玩家生成点: " + (playerSpawns.isEmpty() ? "无" : String.join(", ", playerSpawns.keySet())));
        player.sendMessage("§c怪物生成点: " + (monsterSpawns.isEmpty() ? "无" : String.join(", ", monsterSpawns.keySet())));
        player.sendMessage("§5特殊怪物点: " + (specialMonsters.isEmpty() ? "无" : String.join(", ", specialMonsters.keySet())));
    }

    private void handleAdd(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage("§c用法: /mr locs add <名称> <类型> [id]");
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
                case "special_evac":
                case "se":
                    type = LocationType.SPECIAL_EVACUATION;
                    break;
                case "special_monster":
                case "sm":
                    type = LocationType.SPECIAL_MONSTER_SPAWN;
                    break;
                default:
                    player.sendMessage("§c未知类型！可用: player, monster, evacuate, special_evac, special_monster");
                    return;
            }
        }

        Integer extraId = null;
        if (type == LocationType.SPECIAL_MONSTER_SPAWN) {
            if (args.length < 4) {
                player.sendMessage("§c特殊怪物点需要指定ID！用法: /mr locs add <名称> special_monster <ID>");
                return;
            }
            try {
                extraId = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                player.sendMessage("§cID必须是整数！");
                return;
            }
        } else if (args.length >= 4) {
            player.sendMessage("§c非特殊怪物点不需要ID参数，已忽略。");
        }

        addLocation(player.getWorld().getName(), name, player.getLocation(), type, extraId);
        player.sendMessage("§a已添加 " + type.getDisplayName() + " §b" + name +
                (extraId != null ? " (ID: " + extraId + ")" : ""));
    }

    private void handleRemove(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§c用法: /mr locs remove <名称> [世界]");
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
            player.sendMessage("§c用法: /mr locs tp <名称> [世界]");
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