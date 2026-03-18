package Listeners;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 单类GUI地点管理器 - 按世界分类存储地点
 * 其他类可通过静态方法调用：添加/删除/获取/传送地点
 */
public class LocationManagerUI implements Listener {
    private static final Map<String, Map<String, Location>> LOCATIONS = new ConcurrentHashMap<>();
    private static final String GUI_TITLE = "§8世界地点传送门";
    private static JavaPlugin plugin;

    // 初始化方法（在你的主类onEnable中调用）
    public static void init(JavaPlugin instance) {
        plugin = instance;
        Bukkit.getPluginManager().registerEvents(new LocationManagerUI(), plugin);
        loadDefaultLocations(); // 可选：加载示例数据
    }

    // ==================== 公开API（供其他类调用） ====================
    public static void addLocation(String worldName, String locationName, Location location) {
        LOCATIONS.computeIfAbsent(worldName, k -> new ConcurrentHashMap<>()).put(locationName, location.clone());
    }

    public static void removeLocation(String worldName, String locationName) {
        Map<String, Location> worldLocs = LOCATIONS.get(worldName);
        if (worldLocs != null) worldLocs.remove(locationName);
    }

    public static Location getLocation(String worldName, String locationName) {
        Map<String, Location> worldLocs = LOCATIONS.get(worldName);
        return worldLocs != null ? worldLocs.get(locationName) : null;
    }

    public static Map<String, Map<String, Location>> getAllLocations() {
        return Collections.unmodifiableMap(LOCATIONS);
    }

    // 为玩家打开GUI
    public static void openGUI(Player player) {
        if (LOCATIONS.isEmpty()) {
            player.sendMessage("§c暂无任何已保存的地点。");
            return;
        }

        Inventory gui = Bukkit.createInventory(null, 54, GUI_TITLE);
        int slot = 0;

        for (Map.Entry<String, Map<String, Location>> worldEntry : LOCATIONS.entrySet()) {
            String worldName = worldEntry.getKey();
            World world = Bukkit.getWorld(worldName);

            for (Map.Entry<String, Location> locEntry : worldEntry.getValue().entrySet()) {
                if (slot >= 53) break; // 保留最后一个槽位给控制按钮

                Location loc = locEntry.getValue();
                ItemStack item = createLocationItem(world, locEntry.getKey(), loc);
                gui.setItem(slot++, item);
            }
        }

        // 添加全局控制按钮（可选）
        ItemStack controlItem = new ItemStack(Material.COMPASS);
        ItemMeta meta = controlItem.getItemMeta();
        meta.setDisplayName("§6当前世界传送点");
        meta.setLore(Arrays.asList("§7点击传送至当前世界的所有地点"));
        controlItem.setItemMeta(meta);
        gui.setItem(53, controlItem);

        player.openInventory(gui);
    }

    // ==================== 内部辅助方法 ====================
    private static ItemStack createLocationItem(World world, String name, Location loc) {
        Material material = world != null ? Material.valueOf(world.getEnvironment().name() + "_MAP") : Material.FILLED_MAP;
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§b" + name);
        meta.setLore(Arrays.asList(
                "§7世界: §f" + (world != null ? world.getName() : "未知"),
                "§7坐标: §f" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ(),
                "",
                "§a左键点击传送",
                "§c右键点击删除"
        ));
        // 存储数据到物品（用于点击识别）
        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "location_data"),
                org.bukkit.persistence.PersistentDataType.STRING,
                name + ":" + (world != null ? world.getName() : "unknown")
        );
        item.setItemMeta(meta);
        return item;
    }

    private static void loadDefaultLocations() {
        // 示例数据：您可以从配置文件加载或留空
        World world = Bukkit.getWorlds().get(0);
        if (world != null) {
            addLocation(world.getName(), "spawn", world.getSpawnLocation());
        }
    }

    // ==================== 事件监听 ====================
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(GUI_TITLE)) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null) return;

        ItemStack clickedItem = event.getCurrentItem();
        ItemMeta meta = clickedItem.getItemMeta();
        if (meta == null) return;

        // 处理控制按钮
        if (event.getSlot() == 53 && clickedItem.getType() == Material.COMPASS) {
            openCurrentWorldGUI(player);
            return;
        }

        // 处理地点物品
        String data = meta.getPersistentDataContainer().get(
                new NamespacedKey(plugin, "location_data"),
                org.bukkit.persistence.PersistentDataType.STRING
        );
        if (data == null) return;

        String[] parts = data.split(":");
        if (parts.length < 2) return;
        String locName = parts[0];
        String worldName = parts[1];

        Location targetLoc = getLocation(worldName, locName);
        if (targetLoc == null) {
            player.sendMessage("§c该地点已不存在！");
            player.closeInventory();
            return;
        }

        // 右键删除
        if (event.isRightClick()) {
            removeLocation(worldName, locName);
            player.sendMessage("§a已删除地点: " + locName);
            player.closeInventory();
            openGUI(player); // 刷新GUI
        }
        // 左键传送
        else if (event.isLeftClick()) {
            player.closeInventory();
            player.sendMessage("§a正在传送至: " + locName);
            new BukkitRunnable() {
                @Override
                public void run() {
                    player.teleport(targetLoc);
                }
            }.runTask(plugin);
        }
    }

    private void openCurrentWorldGUI(Player player) {
        String currentWorld = player.getWorld().getName();
        Map<String, Location> worldLocs = LOCATIONS.get(currentWorld);
        if (worldLocs == null || worldLocs.isEmpty()) {
            player.sendMessage("§c当前世界没有保存任何地点。");
            return;
        }

        Inventory gui = Bukkit.createInventory(null, 54, "§8当前世界: " + currentWorld);
        int slot = 0;
        for (Map.Entry<String, Location> entry : worldLocs.entrySet()) {
            if (slot >= 54) break;
            ItemStack item = createLocationItem(player.getWorld(), entry.getKey(), entry.getValue());
            gui.setItem(slot++, item);
        }
        player.openInventory(gui);
    }
}