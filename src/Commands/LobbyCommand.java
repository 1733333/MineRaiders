package Commands;

import Events.PlayerJoinMidgameEvent;
import Listeners.GameListener;
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
    private final Map<UUID, Boolean> spectatorMode = new HashMap<>();
    // 防抖：记录玩家最后一次点击菜单的时间（毫秒）
    private final Map<UUID, Long> lastClickTime = new HashMap<>();
    private static final long CLICK_COOLDOWN_MS = 300; // 300毫秒内重复点击忽略

    GameStatus gameStatus = GameStatus.INSTANCE;
    Random r = new Random();

    public LobbyCommand(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
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

    private void openWorldMenu(Player player) {
        List<String> worldNames = getAvailableWorlds();
        if (worldNames.isEmpty()) {
            player.sendMessage("§c当前没有可用的游戏世界！");
            return;
        }

        int size = ((worldNames.size() + 2) / 9 + 1) * 9;
        Inventory inv = Bukkit.createInventory(null, size, MENU_TITLE);

        int slot = 0;
        for (String worldName : worldNames) {
            World world = Bukkit.getWorld(worldName);
            if (world == null) continue;

            int mapId = gameStatus.getWorldId(worldName);
            ItemStack item = new ItemStack(Material.LODESTONE);
            ItemMeta meta = item.getItemMeta();

            String innerWorldName = gameStatus.getWorldNameByID(mapId);
            meta.setDisplayName("§b" + innerWorldName);

            boolean isActive = GameListener.isGameActive(world);
            int playerCount = GameListener.getPlayerCount(world);
            int readyCount = gameStatus.getReadyCount(worldName);

            List<String> lore = new ArrayList<>();
            if (isActive) {
                lore.add("§a游戏中 §7(§e玩家数量：" + playerCount);
                lore.add("§7点击加入进行中的游戏");
            } else {
                lore.add("§c等待中");
                lore.add("§7准备人数: §e" + readyCount);
                lore.add("§7点击准备加入游戏");
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.setItem(slot, item);
            slot++;
        }

        int cancelSlot = inv.getSize() - 2;
        int currentReadyMap = PlayerStats.INSTANCE.getReadyStatus(player);
        boolean isReady = currentReadyMap != -1;
        ItemStack cancelItem = new ItemStack(isReady ? Material.BARRIER : Material.GRAY_DYE);
        ItemMeta cancelMeta = cancelItem.getItemMeta();
        if (isReady) {
            cancelMeta.setDisplayName("§c取消准备");
            cancelMeta.setLore(Collections.singletonList("§7点击取消当前世界的准备"));
        } else {
            cancelMeta.setDisplayName("§8未准备");
            cancelMeta.setLore(Collections.singletonList("§7没有正在准备的世界"));
        }
        cancelItem.setItemMeta(cancelMeta);
        inv.setItem(cancelSlot, cancelItem);

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
        inv.setItem(inv.getSize() - 1, modeItem);

        player.openInventory(inv);
    }

    private List<String> getAvailableWorlds() {
        Set<String> worlds = new HashSet<>();
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
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!event.getView().getTitle().equals(MENU_TITLE)) return;

        // 防抖：避免快速重复点击
        long now = System.currentTimeMillis();
        Long last = lastClickTime.get(player.getUniqueId());
        if (last != null && (now - last) < CLICK_COOLDOWN_MS) {
            return; // 忽略本次点击
        }
        lastClickTime.put(player.getUniqueId(), now);

        event.setCancelled(true);

        // 检查点击的物品是否存在
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        int slot = event.getSlot();
        int cancelSlot = event.getInventory().getSize() - 2;
        int lastSlot = event.getInventory().getSize() - 1;

        // 取消准备按钮
        if (slot == cancelSlot) {
            int currentReadyMap = PlayerStats.INSTANCE.getReadyStatus(player);
            if (currentReadyMap != -1) {
                String worldName = gameStatus.getWorlds(currentReadyMap);
                gameStatus.removeReadyPlayer(worldName, player.getUniqueId());
                PlayerStats.INSTANCE.stopReady(player);
                player.sendMessage("§a已取消准备。");
                Bukkit.broadcastMessage("§e" + player.getName() + "§a取消了准备");
            } else {
                player.sendMessage("§c你还没有准备任何世界！");
            }
            openWorldMenu(player);
            return;
        }

        // 观战模式切换按钮
        if (slot == lastSlot) {
            boolean current = spectatorMode.getOrDefault(player.getUniqueId(), false);
            spectatorMode.put(player.getUniqueId(), !current);
            openWorldMenu(player);
            return;
        }

        // 世界槽位处理
        List<String> worlds = getAvailableWorlds();
        if (slot >= worlds.size() || slot < 0) return;
        String worldName = worlds.get(slot);
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            player.sendMessage("§c世界不存在！");
            return;
        }

        boolean isSpectating = spectatorMode.getOrDefault(player.getUniqueId(), false);
        boolean isGameActive = GameListener.isGameActive(world);

        if (isSpectating) {
            if (!isGameActive) {
                player.sendMessage("§c游戏尚未开始，无法观战！");
                return;
            }
            int worldId = gameStatus.getWorldId(worldName);
            PlayerStats.INSTANCE.setSpectating(player, worldId);
            player.setGameMode(org.bukkit.GameMode.SPECTATOR);
            List<Player> playerList = new ArrayList<>();
            for (Player playing : world.getPlayers()) {
                if (PlayerStats.INSTANCE.getReadyStatus(playing) == worldId) {
                    playerList.add(playing);
                }
            }
            if (!playerList.isEmpty()) {
                player.teleport(playerList.get(r.nextInt(playerList.size())));
            }
            String innerWorldName = gameStatus.getWorldNameByID(worldId);
            player.sendMessage("§a你已进入观战模式，正在观看 " + innerWorldName + " 的游戏。");
            player.closeInventory();
        } else {
            if (isGameActive) {
                // 中途加入，先关闭菜单再触发事件
                player.closeInventory();
                Bukkit.getPluginManager().callEvent(new PlayerJoinMidgameEvent(world, player));
            } else {
                int mapId = gameStatus.getWorldId(worldName);
                if (mapId >= 0) {
                    int currentReadyMap = PlayerStats.INSTANCE.getReadyStatus(player);
                    if (currentReadyMap == mapId) {
                        player.sendMessage("§e你已经准备加入这个地图了！");
                        openWorldMenu(player);
                    } else {
                        // 移除旧的准备
                        if (currentReadyMap != -1) {
                            String oldWorld = gameStatus.getWorlds(currentReadyMap);
                            gameStatus.removeReadyPlayer(oldWorld, player.getUniqueId());
                        }
                        // 设置新准备
                        PlayerStats.INSTANCE.setReady(player, mapId);
                        gameStatus.addReadyPlayer(worldName, player.getUniqueId());
                        int readyCount = gameStatus.getReadyCount(worldName);
                        String innerWorldName = gameStatus.getWorldNameByID(mapId);

                        player.sendMessage("§a你已准备加入地图 " + innerWorldName + "，等待游戏开始...");
                        Bukkit.broadcastMessage("§b" + player.getName() + "§a准备加入地图§e" + innerWorldName
                                + "§a，当前准备人数：§e" + readyCount);

                        // 延迟刷新菜单，避免事件冲突
                        Bukkit.getScheduler().runTaskLater(plugin, () -> openWorldMenu(player), 1L);
                    }
                } else {
                    player.sendMessage("§c世界ID无效，请联系管理员！");
                }
            }
        }
    }
}