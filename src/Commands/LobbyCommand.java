package Commands;

import Events.PlayerJoinMidgameEvent;
import Listeners.GameListener;
import Listeners.LocationManagerUI;
import Universal.GameStatus;
import Universal.PlayerStats;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class LobbyCommand implements CommandExecutor, Listener {
    private final JavaPlugin plugin;
    private static final String MENU_TITLE = "§6大厅菜单，点击地图即可加入对应大厅";
    // 记录玩家当前的观战模式状态
    private final Map<UUID, Boolean> spectatorMode = new HashMap<>();
    GameStatus gameStatus = GameStatus.INSTANCE;
    public LobbyCommand(JavaPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);

        if (plugin.getCommand("lobby") != null) {
            plugin.getCommand("lobby").setExecutor(this);
        } else {
            plugin.getLogger().warning("命令 'lobby' 未在 plugin.yml 中注册！");
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§c该指令只能由玩家执行！");
            return true;
        }
        openWorldMenu(player);
        return true;
    }

    /**
     * 打开世界选择菜单
     */
    private void openWorldMenu(Player player) {
        List<String> worldNames = getAvailableWorlds();
        if (worldNames.isEmpty()) {
            player.sendMessage("§c当前没有可用的游戏世界！");
            return;
        }

        // 计算需要的菜单大小（每行9格，世界数量 + 1 个观战模式切换按钮）
        int size = ((worldNames.size() + 1) / 9 + 1) * 9;
        Inventory inv = Bukkit.createInventory(null, size, MENU_TITLE);

        // 添加世界按钮
        int slot = 0;
        for (String worldName : worldNames) {
            World world = Bukkit.getWorld(worldName);
            if (world == null) continue;

            ItemStack item = new ItemStack(Material.LODESTONE);
            ItemMeta meta = item.getItemMeta();
            String innerWorldName = gameStatus.getWorldName(worldName);

            meta.setDisplayName("§b" + innerWorldName);

            // 获取游戏状态信息
            boolean isActive = GameListener.isGameActive(world);
            int playerCount = GameListener.getPlayerCount(world);
            List<String> lore = new ArrayList<>();
            if (isActive) {
                lore.add("§a游戏中 §7(§e玩家数量：" + playerCount);
                lore.add("§7点击加入进行中的游戏");
            } else {
                lore.add("§c等待中");
                lore.add("§7点击准备加入游戏");
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.setItem(slot, item);
            slot++;
        }

        // 最后一格：观战模式切换按钮（放在最后一个格子）
        boolean isSpectating = spectatorMode.getOrDefault(player.getUniqueId(), false);
        ItemStack modeItem = new ItemStack(isSpectating ? Material.ENDER_EYE : Material.COMPASS);
        ItemMeta modeMeta = modeItem.getItemMeta();
        if (isSpectating) {
            modeMeta.setDisplayName("§a观战模式 §7(开启)");
            modeMeta.setLore(Collections.singletonList("§7点击关闭观战模式"));
        } else {
            modeMeta.setDisplayName("§c观战模式 §7(关闭)");
            modeMeta.setLore(Collections.singletonList("§7点击开启观战模式"));
        }
        modeItem.setItemMeta(modeMeta);
        // 放在最后一格（菜单最后）
        inv.setItem(inv.getSize() - 1, modeItem);

        player.openInventory(inv);
    }

    /**
     * 获取所有可用世界（配置了玩家生成点的世界）
     */
    private List<String> getAvailableWorlds() {
        Set<String> worlds = new HashSet<>();
        // 遍历所有世界，检查是否配置了玩家生成点
        for (World world : Bukkit.getWorlds()) {
            Map<String, Location> spawns = LocationManagerUI.getLocationsByType(world.getName(), LocationManagerUI.LocationType.PLAYER_SPAWN);
            if (!spawns.isEmpty()) {
                worlds.add(world.getName());
            }
        }
        List<String> sorted = new ArrayList<>(worlds);
        Collections.sort(sorted);
        return sorted;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!event.getView().getTitle().equals(MENU_TITLE)) return;
        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();
        int lastSlot = event.getInventory().getSize() - 1;

        // 点击观战模式切换按钮
        if (slot == lastSlot) {
            boolean current = spectatorMode.getOrDefault(player.getUniqueId(), false);
            spectatorMode.put(player.getUniqueId(), !current);
            openWorldMenu(player); // 刷新菜单
            return;
        }

        // 获取点击的世界名称（通过槽位对应的世界）
        List<String> worlds = getAvailableWorlds();
        int worldIndex = slot;
        if (worldIndex >= worlds.size()) return; // 无效槽位

        String worldName = worlds.get(worldIndex);
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            player.sendMessage("§c世界不存在！");
            return;
        }

        boolean isSpectating = spectatorMode.getOrDefault(player.getUniqueId(), false);
        boolean isGameActive = GameListener.isGameActive(world);

        if (isSpectating) {
            // 观战模式：直接进入观战
            if (!isGameActive) {
                player.sendMessage("§c游戏尚未开始，无法观战！");
                return;
            }
            // 获取世界ID并存储观战状态
            int worldId = gameStatus.getWorldId(worldName);
            PlayerStats.INSTANCE.setSpectating(player, worldId);
            player.setGameMode(org.bukkit.GameMode.SPECTATOR);
            Location spawn = world.getSpawnLocation();
            player.teleport(spawn);
            player.sendMessage("§a你已进入观战模式，正在观看 " + worldName + " 的游戏。");
            player.closeInventory();
        } else {
            // 准备/加入模式
            if (isGameActive) {
                // 游戏进行中，尝试中途加入
                Bukkit.getPluginManager().callEvent(new PlayerJoinMidgameEvent(world, player));
                player.closeInventory();
            } else {
                // 游戏未开始，准备加入
                // 使用世界名 hash 作为地图ID（确保与其他地方一致）
                int mapId = getMapId(worldName);
                PlayerStats.INSTANCE.setReady(player, mapId);
                player.sendMessage("§a你已准备加入世界 " + worldName + "，等待游戏开始...");
                player.closeInventory();
            }
        }
    }

    /**
     * 根据世界名返回地图ID（用于 PlayerStats 存储）
     */
    private int getMapId(String worldName) {
        // 简单使用世界名的 hashCode 作为 ID，实际应统一管理
        return Math.abs(worldName.hashCode());
    }
}