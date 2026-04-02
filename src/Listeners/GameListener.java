package Listeners;

import Commands.LocationManagerUI;
import Events.*;
import MineRaiders.MRD;
import Universal.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.*;
import org.bukkit.util.Vector;

import java.util.*;

public class GameListener implements Listener {
    Random rand = new Random();
    private static JavaPlugin plugin = null;
    private static final Map<World, GameSession> activeGames = new HashMap<>();
    Kit k = Kit.INSTANCE;
    LootPool lp = LootPool.INSTANCE;
    PlayerStats playerStats = PlayerStats.INSTANCE;
    GameStatus gameStatus = GameStatus.INSTANCE;
    // 配置参数
    private static final int MIN_DISTANCE_TO_SPAWN = 20;       // 怪物生成点与玩家生成点最小距离
    private static final int TRIGGER_DISTANCE = 15;           // 触发怪物刷新的距离
    private static final int CHARGE_DURATION = 45;            // 撤离点充能时间（秒）
    private static final int EVACUATION_WINDOW_DURATION = 20; // 撤离窗口期（秒）
    private static final double EVACUATION_RADIUS = 5.0;      // 撤离生效半径
    // 撤离失败物品存储（盔甲架 -> 物品列表）
    private static final Map<ArmorStand, List<ItemStack>> lootMap = new HashMap<>();

    public GameListener(JavaPlugin plugin) {
        GameListener.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    // ========================= 辅助方法：清理准备状态 =========================
    /**
     * 清除单个玩家的准备状态（同时清除 PlayerStats 和 GameStatus）
     */
    private void clearPlayerReady(Player player, String worldName) {
        PlayerStats.INSTANCE.stopReady(player);
        GameStatus.INSTANCE.removeReadyPlayer(worldName, player.getUniqueId());
    }

    /**
     * 清除整个世界的准备状态
     */
    private void clearWorldReady(String worldName) {
        GameStatus.INSTANCE.clearReadyPlayers(worldName);
    }

    // ========================= 内部类：游戏会话 =========================
    static class GameSession {
        final World world;
        final Set<UUID> allPlayers;                 // 所有参与过本局游戏的玩家（包括离线）
        final Set<Player> onlinePlayers;            // 当前在线的玩家
        final Map<UUID, Double> profits;            // 玩家收益
        final Map<Player, Scoreboard> playerScoreboards = new HashMap<>();   // 独立计分板
        final Map<Player, Objective> playerObjectives = new HashMap<>();     // 独立显示目标
        final BukkitTask timerTask;
        final int totalGameSeconds;
        int remainingSeconds;                      // 剩余时间（秒）
        boolean isEnding = false;                  // 游戏是否正在结束（等待充能）
        boolean ended = false;                     // 游戏是否已经结束（防止重复执行endGame）
        final Set<Snowman> activeGolems = new HashSet<>(); // 正在充能或处于窗口期的撤离点
        // 本局游戏中已经退出（成功撤离或撤离失败）的玩家名称集合，用于禁止中途以玩家身份加入
        final Set<String> exitedPlayers = new HashSet<>();
        // 怪物刷新盔甲架列表
        final List<ArmorStand> monsterTriggers = new ArrayList<>();
        // 普通撤离点雪傀儡列表
        final List<Snowman> evacuationGolems = new ArrayList<>();
        // 怪物触发点原始坐标（用于重生）以及半量标记映射
        final List<Location> monsterTriggerLocations = new ArrayList<>();
        final Map<Location, Boolean> halfAmountTriggers = new HashMap<>();   // 是否半量
        // 重生任务列表（用于游戏结束时取消）
        final List<BukkitTask> regenerationTasks = new ArrayList<>();
        // 撤离点充能任务管理
        final Map<Snowman, BukkitTask> chargeTasks = new HashMap<>();
        // 撤离窗口期任务管理
        final Map<Snowman, BukkitTask> evacuationWindows = new HashMap<>();
        // 外部监听器引用（用于调用容器高亮等方法）
        private final GameListener listener;

        GameSession(World world, Set<Player> players, int totalSeconds, GameListener listener) {
            this.world = world;
            this.listener = listener;
            this.onlinePlayers = new HashSet<>(players);
            this.allPlayers = new HashSet<>();
            for (Player p : players) {
                allPlayers.add(p.getUniqueId());
            }
            this.profits = new HashMap<>();
            this.totalGameSeconds = totalSeconds;  // 记录总时长
            for (Player p : players) {
                profits.put(p.getUniqueId(), 0.0);
            }
            this.remainingSeconds = totalSeconds;
            // 为每个玩家创建独立计分板
            ScoreboardManager manager = Bukkit.getScoreboardManager();
            for (Player p : onlinePlayers) {
                Scoreboard sb = manager.getNewScoreboard();
                Objective obj = sb.registerNewObjective("game", "dummy", "§6MineRaiders");
                obj.setDisplaySlot(DisplaySlot.SIDEBAR);
                playerScoreboards.put(p, sb);
                playerObjectives.put(p, obj);
                p.setScoreboard(sb);
            }
            updateScoreboard();
            // 启动计时器
            timerTask = new BukkitRunnable() {
                @Override
                public void run() {
                    if (remainingSeconds <= 0) {
                        cancel();
                        scheduleEnd(); // 游戏时间到，触发结束流程
                    } else {
                        remainingSeconds--;
                        // 预警检查
                        checkAndWarn();
                        updateScoreboard();
                    }
                }
            }.runTaskTimer(plugin, 0L, 20L);
        }

        void checkAndWarn() {
            // 只在剩余时间精确等于 600、300、60 秒时发送一次预警
            if (remainingSeconds == 600 || remainingSeconds == 300 || remainingSeconds == 60) {
                String message;
                if (remainingSeconds == 600) message = "§e游戏剩余时间：10分钟！";
                else if (remainingSeconds == 300) message = "§e游戏剩余时间：5分钟！";
                else message = "§c游戏剩余时间：1分钟！";
                for (Player p : onlinePlayers) {
                    if (p.isOnline()) {
                        p.sendTitle("§6⚠ 时间预警", message, 10, 70, 20);
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
                    }
                }
            }
        }

        void scheduleEnd() {
            if (isEnding) return;
            isEnding = true;
            if (activeGolems.isEmpty()) {
                // 没有激活的撤离点，直接结束
                endGame();
            } else {
                // 有激活的撤离点（充能中或窗口期），等待它们完成
                for (Player p : onlinePlayers) {
                    if (p.isOnline()) {
                        p.sendMessage("§c游戏时间已到，等待所有撤离点关闭...");
                    }
                }
                // 超时强制结束：30秒后若还有激活撤离点则强制结束
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (!ended && !activeGolems.isEmpty()) {
                            plugin.getLogger().warning("游戏 " + world.getName() + " 强制结束：存在未完成的撤离点");
                            endGame();
                        }
                    }
                }.runTaskLater(plugin, 600L); // 30秒 = 600 ticks
            }
        }

        void updateScoreboard() {
            // 为每个在线玩家独立更新计分板条目
            for (Player p : onlinePlayers) {
                if (!p.isOnline()) continue;
                Scoreboard sb = playerScoreboards.get(p);
                Objective obj = playerObjectives.get(p);
                if (sb == null || obj == null) continue;
                // 清除旧的条目（保留 objective 本身）
                for (String entry : sb.getEntries()) {
                    if (entry.startsWith("§e剩余时间:") || entry.startsWith("§a当前收益:")) {
                        sb.resetScores(entry);
                    }
                }
                // 生成动态文本
                int minutes = remainingSeconds / 60;
                int seconds = remainingSeconds % 60;
                String timeStr = String.format("%02d:%02d", minutes, seconds);
                String timeEntry = "§e剩余时间: §f" + timeStr;
                double profit = profits.getOrDefault(p.getUniqueId(), 0.0);
                String profitEntry = "§a当前收益: §f" + String.format("%.1f", profit);
                // 按顺序添加条目
                obj.getScore(timeEntry).setScore(1);
                obj.getScore(profitEntry).setScore(0);
            }
        }

        void addProfit(Player player, double amount) {
            UUID uuid = player.getUniqueId();
            profits.put(uuid, profits.getOrDefault(uuid, 0.0) + amount);
            updateScoreboard();
        }

        /**
         * 从游戏中彻底移除玩家（撤离成功/失败或游戏结束）
         */
        void removePlayer(Player player) {
            onlinePlayers.remove(player);
            allPlayers.remove(player.getUniqueId());
            playerScoreboards.remove(player);
            playerObjectives.remove(player);
            if (player.isOnline()) {
                player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
            }
            if (onlinePlayers.isEmpty()) {
                endGame();
            }
        }

        /**
         * 玩家掉线时调用（保留 allPlayers，仅移出 onlinePlayers）
         */
        void playerDisconnect(Player player) {
            onlinePlayers.remove(player);
            // 计分板映射不清除，重连时会重新设置
            if (player.isOnline()) {
                player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
            }
            // 如果所有玩家都掉线了，游戏不会立即结束，等时间到或重连
        }

        /**
         * 玩家重连时调用（重新加入 onlinePlayers）
         */
        void playerReconnect(Player player) {
            onlinePlayers.add(player);
            // 恢复计分板
            ScoreboardManager manager = Bukkit.getScoreboardManager();
            Scoreboard sb = manager.getNewScoreboard();
            Objective obj = sb.registerNewObjective("game", "dummy", "§6MineRaiders");
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);
            playerScoreboards.put(player, sb);
            playerObjectives.put(player, obj);
            player.setScoreboard(sb);
            updateScoreboard();  // 立即刷新显示
            // 恢复游戏状态
            player.setGameMode(GameMode.ADVENTURE);
            player.setHealth(20);
            player.setFoodLevel(20);
            player.setCustomNameVisible(false);
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20, 0));
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
            PlayerStats.INSTANCE.setInGame(player);
            // 恢复容器高亮
            listener.startContainerHighlight(player);
        }

        /**
         * 游戏结束入口：仅触发 GameEndEvent，所有清理工作由监听器处理
         */
        void endGame() {
            if (ended) return;
            ended = true;
            if (timerTask != null && !timerTask.isCancelled()) {
                timerTask.cancel();
            }
            // 触发游戏结束事件，所有清理工作交由事件监听器处理
            Bukkit.getPluginManager().callEvent(new GameEndEvent(world));
        }

        void addActiveGolem(Snowman golem) {
            activeGolems.add(golem);
        }

        void removeActiveGolem(Snowman golem) {
            activeGolems.remove(golem);
            // 如果游戏处于结束等待状态且没有其他激活的铁傀儡，则结束游戏
            if (isEnding && activeGolems.isEmpty()) {
                endGame();
            }
        }

        /**
         * 在指定位置重新生成一个怪物触发盔甲架，并添加到列表中
         *
         * @param loc    位置
         * @param isHalf 是否为半量触发点
         */
        void regenerateMonsterTrigger(Location loc, boolean isHalf) {
            ArmorStand as = world.spawn(loc, ArmorStand.class);
            as.setVisible(false);
            as.setGravity(false);
            as.setMarker(true);
            as.setInvulnerable(true);
            as.setCustomName("MonsterTrigger");
            as.setCustomNameVisible(false);
            // 存储半量标记
            if (isHalf) {
                as.setMetadata("half_amount", new FixedMetadataValue(plugin, true));
            }
            monsterTriggers.add(as);
        }

        /**
         * 安排一个延迟任务，在5分钟后重新生成怪物触发点（保持原有的半量属性）
         */
        void scheduleRegeneration(Location loc) {
            // 获取该位置对应的半量标记
            boolean isHalf = halfAmountTriggers.getOrDefault(loc, false);
            BukkitTask task = new BukkitRunnable() {
                @Override
                public void run() {
                    if (isEnding || onlinePlayers.isEmpty()) return; // 游戏已结束或无玩家，不再重生
                    regenerateMonsterTrigger(loc, isHalf);
                }
            }.runTaskLater(plugin, 6000L); // 5分钟 = 6000 ticks
            regenerationTasks.add(task);
        }
    }

    // ========================= 辅助方法 =========================
    private void startContainerHighlight(Player player) {
        ContainerListener cl = new ContainerListener(plugin);
        BukkitRunnable highLight = new BukkitRunnable() {
            @Override
            public void run() {
                if (!playerStats.isInGame(player) || !player.isOnline()) {
                    this.cancel();
                    return;
                }
                int radius = 10;
                for (int a = -radius; a <= radius; a++) {
                    for (int b = -radius; b <= radius; b++) {
                        for (int c = -radius; c <= radius; c++) {
                            Location bLoc = player.getLocation().add(a, b, c).clone();
                            Block block = player.getWorld().getBlockAt(bLoc);
                            if (gameStatus.isEmpty(block)) continue;
                            int rarity = cl.getContainerRarity(block);
                            if (rarity != -1) {
                                Particle particle = switch (rarity) {
                                    case -2, -3, -4, -5 -> Particle.HAPPY_VILLAGER;
                                    case 0 -> Particle.WAX_OFF;
                                    case 1 -> Particle.SCRAPE;
                                    case 2 -> Particle.WAX_ON;
                                    default -> Particle.ENTITY_EFFECT;
                                };
                                k.drawBlockOutline(player, block, particle);
                            }
                        }
                    }
                }
            }
        };
        highLight.runTaskTimer(plugin, 0L, 70L);
    }

    // ========================= 撤离点充能 + 窗口期逻辑 =========================
    /**
     * 开始撤离点充能（倒计时60秒）
     */
    private void startCharging(Snowman golem, GameSession session, Player activator) {
        // 检查是否已经在充能中或处于窗口期
        if (session.chargeTasks.containsKey(golem) || session.evacuationWindows.containsKey(golem)) return;
        // 标记充能中
        golem.setGlowing(false);
        golem.setCustomName("§e撤离点 (充能中 " + CHARGE_DURATION + "s)");
        golem.setRotation(0, 0);
        session.addActiveGolem(golem);
        Location golemLoc = golem.getLocation();
        for (Player p : session.onlinePlayers) {
            if (p.isOnline()) {
                p.sendMessage("§b" + activator.getName() + "§a激活了位于§e" +
                        "[" + golemLoc.getBlockX() + "," + golemLoc.getBlockY() + "," + golemLoc.getBlockZ() + "]" +
                        "§a的撤离点，" + CHARGE_DURATION + "秒后开放撤离窗口！");
            }
        }
        // 创建充能任务
        BukkitTask chargeTask = new BukkitRunnable() {
            int timeLeft = CHARGE_DURATION;
            @Override
            public void run() {
                if (golem.isDead() || !session.evacuationGolems.contains(golem)) {
                    // 铁傀儡消失，取消任务并清理状态
                    cancel();
                    session.chargeTasks.remove(golem);
                    session.removeActiveGolem(golem);
                    return;
                }
                if (timeLeft <= 0) {
                    // 充能完成，进入撤离窗口期
                    cancel();
                    session.chargeTasks.remove(golem);
                    startEvacuationWindow(golem, session);
                    return;
                }
                // 充能中：显示粒子效果和倒计时名称
                Location loc = golem.getLocation().add(0, 1, 0);
                golem.getWorld().spawnParticle(Particle.END_ROD, loc, 20, 0.5, 0.5, 0.5, 0.1);
                golem.getWorld().spawnParticle(Particle.FIREWORK, loc, 10, 0.5, 0.5, 0.5, 0.05);
                golem.getWorld().playSound(loc, Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.0f);
                k.spawnCircleParticles(golem.getLocation(), EVACUATION_RADIUS, 50);
                golem.setCustomName("§e撤离点 (充能中 " + timeLeft + "s)");
                timeLeft--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
        session.chargeTasks.put(golem, chargeTask);
        activator.sendMessage("§a你激活了撤离点！" + CHARGE_DURATION + "秒后开放撤离窗口。");
    }

    /**
     * 开始撤离窗口期（持续 EVACUATION_WINDOW_DURATION 秒，期间玩家右键即可撤离）
     */
    private void startEvacuationWindow(Snowman golem, GameSession session) {
        // 如果窗口期已存在则忽略
        if (session.evacuationWindows.containsKey(golem)) return;
        // 设置窗口期外观
        golem.setGlowing(true);
        golem.setRotation(0, 0);
        golem.setCustomName("§6撤离点 (可右键撤离, " + EVACUATION_WINDOW_DURATION + "s)");
        // 广播坐标给所有游戏内玩家
        Location loc = golem.getLocation();
        for (Player p : session.onlinePlayers) {
            if (p.isOnline()) {
                p.sendMessage("位于§e" + "[" + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ() + "]" +
                        "§a的撤离点已经开启，使用§b鼠标右键§a点击雪傀儡即可撤离");
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f);
            }
        }
        // 创建窗口期任务
        BukkitTask windowTask = new BukkitRunnable() {
            int timeLeft = EVACUATION_WINDOW_DURATION;
            @Override
            public void run() {
                if (golem.isDead() || !session.evacuationGolems.contains(golem)) {
                    // 铁傀儡消失，清理
                    cancel();
                    session.evacuationWindows.remove(golem);
                    session.removeActiveGolem(golem);
                    return;
                }
                if (timeLeft <= 0) {
                    // 窗口期结束，无人撤离 -> 重置撤离点
                    cancel();
                    session.evacuationWindows.remove(golem);
                    golem.setCustomName("§a撤离点");
                    golem.setRotation(0, 0);
                    golem.setGlowing(true);
                    session.removeActiveGolem(golem);
                    for (Player p : session.onlinePlayers) {
                        if (p.isOnline()) {
                            p.sendMessage("§c一个撤离点已失效（窗口期结束），可重新激活。");
                        }
                    }
                    return;
                }
                // 更新倒计时显示
                golem.setCustomName("§6撤离点 (可右键撤离, " + timeLeft + "s)");
                // 粒子效果提醒
                Location loc = golem.getLocation().add(0, 1, 0);
                golem.getWorld().spawnParticle(Particle.END_ROD, loc, 15, 0.5, 0.5, 0.5, 0.5);
                golem.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, loc, 5, 0.5, 0.5, 0.5, 0.05);
                timeLeft--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
        session.evacuationWindows.put(golem, windowTask);
        // 窗口期仍视为活跃撤离点，避免游戏提前结束
        // session.addActiveGolem(golem);   // 已经在充能时加入，不需要重复添加
    }

    /**
     * 玩家右键撤离点执行撤离
     */
    private void evacuatePlayer(Player player, Snowman golem, GameSession session) {
        // 触发撤离事件
        Bukkit.getPluginManager().callEvent(new PlayerExtractEvent(player));
        // 清理该撤离点的窗口期任务和充能任务（如果有）
        cancelEvacuationWindow(golem, session);
        cancelChargeTask(golem, session);
        // 重置雪傀儡外观（取消任务时已重置，但再次确保）
        if (!golem.isDead()) {
            golem.setGlowing(true);
            golem.setCustomName("§a撤离点");
            golem.setRotation(0, 0);
        }
        // 从激活集合中移除，但保留在撤离点列表中，以便其他玩家重新激活
        session.removeActiveGolem(golem);
        // 注意：不要移除雪傀儡本身！ session.evacuationGolems 中仍保留
        player.sendMessage("§a你通过撤离点成功撤离！");
    }

    /**
     * 取消撤离窗口期任务（用于游戏结束或清理）
     */
    private void cancelEvacuationWindow(Snowman golem, GameSession session) {
        BukkitTask task = session.evacuationWindows.remove(golem);
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
    }

    /**
     * 取消撤离点的充能任务（用于游戏结束或清理）
     */
    private void cancelChargeTask(Snowman golem, GameSession session) {
        BukkitTask task = session.chargeTasks.remove(golem);
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
        // 重置状态（如果雪傀儡还在）
        if (!golem.isDead()) {
            golem.setGlowing(true);
            golem.setCustomName("§a撤离点");
            golem.setRotation(0, 0);
        }
        session.removeActiveGolem(golem);
    }

    // ========================= 统一撤离失败处理 =========================
    private void performFailedExtract(Player player) {
        performFailedExtract(player, false);
    }

    private void performFailedExtract(Player player, boolean isGameEnded) {
        World world = player.getWorld();
        GameSession session = activeGames.get(world);
        // 处理倒地状态（如果有）
        if (PlayerStats.INSTANCE.isDying(player)) {
            BukkitRunnable timer = PlayerListener.dyingTimers.remove(player);
            if (timer != null) timer.cancel();
            PlayerStats.INSTANCE.stopDying(player);
            player.removePotionEffect(PotionEffectType.WEAKNESS);
            player.removePotionEffect(PotionEffectType.MINING_FATIGUE);
        }
        // 从游戏会话中移除玩家（如果还在游戏中）
        if (session != null && session.allPlayers.contains(player.getUniqueId())) {
            session.removePlayer(player);
        }
        if (!isGameEnded) {
            // 记录退出玩家名称（用于禁止中途加入）
            if (session != null) {
                session.exitedPlayers.add(player.getName());
            }
            // 收集玩家的所有物品，用于生成遗物盔甲架
            List<ItemStack> playerItems = new ArrayList<>();
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && !item.getType().isAir() && !k.isLockedItem(item)) {
                    playerItems.add(item);
                }
            }
            // 在玩家位置生成遗物盔甲架
            if (!playerItems.isEmpty()) { // 仅当玩家有物品时生成
                ArmorStand lootStand = world.spawn(player.getLocation(), ArmorStand.class);
                // 设置小盔甲架属性
                lootStand.setSmall(true);
                lootStand.setAI(false);
                lootStand.setInvulnerable(true);
                lootStand.setCustomName("§c" + player.getName() + " §7的遗物");
                // 设置装备：玩家头颅 + 皮革护甲
                // 玩家头颅
                ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();
                skullMeta.setOwningPlayer(player);
                playerHead.setItemMeta(skullMeta);
                // 皮革护甲
                ItemStack leatherChest = new ItemStack(Material.LEATHER_CHESTPLATE);
                ItemStack leatherLegs = new ItemStack(Material.LEATHER_LEGGINGS);
                ItemStack leatherBoots = new ItemStack(Material.LEATHER_BOOTS);
                // 应用装备到盔甲架
                lootStand.getEquipment().setHelmet(playerHead);
                lootStand.getEquipment().setChestplate(leatherChest);
                lootStand.getEquipment().setLeggings(leatherLegs);
                lootStand.getEquipment().setBoots(leatherBoots);
                // 存储物品到lootMap，关联盔甲架
                lootMap.put(lootStand, playerItems);
            }
        }
        k.clearInventory(player);
        // 传送到世界出生点
        player.teleport(world.getSpawnLocation());
        player.sendMessage("§c撤离失败，所有物品已丢失！");
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setCustomNameVisible(false);
        // 清除游戏状态标记
        PlayerStats.INSTANCE.stopInGame(player);
        PlayerStats.INSTANCE.removeShieldBar(player);
        if (session != null) {
            clearPlayerReady(player, world.getName());
        }
    }

    // ========================= 事件处理 =========================
    @EventHandler
    public void onGameStart(GameStartEvent event) {
        World world = event.getWorld();
        int worldId = gameStatus.getWorldId(world.getName());
        // 如果该世界已有活跃游戏，则拒绝重新开始并返回错误信息
        if (activeGames.containsKey(world)) {
            for (Player p : world.getPlayers()) {
                p.sendMessage("§c游戏已经开始，无法再次开始！");
            }
            plugin.getLogger().warning("尝试在已进行的游戏中再次启动游戏，已阻止。世界: " + world.getName());
            return;
        }
        MRD mrd = (MRD) Bukkit.getPluginManager().getPlugin("MineRaidersDoor");
        mrd.setAllDoors(true);
        GameStatus.INSTANCE.recoverDoor(world);
        GameStatus.INSTANCE.refillContainers(world);
        // 收集准备就绪的玩家：遍历所有在线玩家，支持大厅准备的玩家
        Set<Player> readyPlayers = new HashSet<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (playerStats.isReady(p)) {
                if (playerStats.getReadyStatus(p) == worldId) {
                    readyPlayers.add(p);
                }
            }
        }
        if (readyPlayers.isEmpty()) {
            plugin.getLogger().info("没有玩家准备，游戏未启动。");
            return;
        }
        // 获取玩家生成点
        Map<String, Location> playerSpawns = LocationManagerUI.getLocationsByType(world.getName(),
                LocationManagerUI.LocationType.PLAYER_SPAWN);
        List<Location> spawnList = new ArrayList<>(playerSpawns.values());
        if (spawnList.isEmpty()) {
            plugin.getLogger().warning("世界 " + world.getName() + " 没有玩家生成点，无法启动游戏。");
            return;
        }
        // 随机分配玩家到生成点
        List<Player> playersToTeleport = new ArrayList<>(readyPlayers);
        Collections.shuffle(playersToTeleport);
        Collections.shuffle(spawnList);
        for (int i = 0; i < playersToTeleport.size(); i++) {
            Player t = playersToTeleport.get(i);
            Location spawnLoc = spawnList.get(i % spawnList.size());
            PlayerStats.INSTANCE.setInGame(playersToTeleport.get(i));
            // 清除准备状态
            clearPlayerReady(t, world.getName());
            t.teleport(spawnLoc);
            t.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20, 0));
            t.playSound(t.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
            t.setGameMode(GameMode.ADVENTURE);
            t.setHealth(20);
            t.setFoodLevel(20);
            t.setCustomNameVisible(false);
            playerStats.createShieldBar(t);
            if (k.isArmored(t)) {
                playerStats.openShield(t);
                GadgetListener gadgetListener = new GadgetListener(plugin);
                gadgetListener.battery(t, 5, 20, new ItemStack(Material.NETHER_PORTAL));
            } else {
                playerStats.closeShield(t);
            }
        }
        // 为每个玩家添加容器高亮任务
        for (Player p : playersToTeleport) {
            startContainerHighlight(p);
        }
        // 创建游戏会话
        GameSession session = new GameSession(world, readyPlayers, 20 * 60, this);
        // 处理怪物生成点（普通）
        Map<String, Location> monsterSpawns = LocationManagerUI.getLocationsByType(world.getName(),
                LocationManagerUI.LocationType.MONSTER_SPAWN);
        List<Location> spawnPoints = new ArrayList<>(playerSpawns.values());
        for (Location loc : monsterSpawns.values()) {
            boolean tooClose = false;
            for (Location spawn : spawnPoints) {
                if (loc.distance(spawn) < MIN_DISTANCE_TO_SPAWN) {
                    tooClose = true;
                    break;
                }
            }
            boolean isHalf = tooClose;
            session.halfAmountTriggers.put(loc, isHalf);
            session.monsterTriggerLocations.add(loc.clone());
            session.regenerateMonsterTrigger(loc, isHalf);
        }
        // 处理特殊怪物点
        Map<String, Location> specialMonsterSpawns = LocationManagerUI.getLocationsByType(world.getName(),
                LocationManagerUI.LocationType.SPECIAL_MONSTER_SPAWN);
        for (Map.Entry<String, Location> entry : specialMonsterSpawns.entrySet()) {
            String name = entry.getKey();
            Location loc = entry.getValue();
            LocationManagerUI.LocationData data = LocationManagerUI.getLocation(world.getName(), name);
            int id = (data != null && data.extraId() != null) ? data.extraId() : rand.nextInt(8);
            Monsters.INSTANCE.summonArc(loc, id);
        }
        // 处理普通撤离点
        Map<String, Location> evacuationSpawns = LocationManagerUI.getLocationsByType(world.getName(),
                LocationManagerUI.LocationType.EVACUATION);
        for (Location loc : evacuationSpawns.values()) {
            Snowman golem = world.spawn(loc, Snowman.class);
            golem.setAI(false);
            golem.setGravity(false);
            golem.setInvulnerable(true);
            golem.setSilent(true);
            golem.setGlowing(true);
            golem.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, PotionEffect.INFINITE_DURATION, 10));
            golem.setCustomName("§a撤离点");
            golem.setCustomNameVisible(true);
            golem.setRotation(0, 0);
            session.evacuationGolems.add(golem);
        }
        // 怪物触发检测
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!activeGames.containsKey(world)) {
                    cancel();
                    return;
                }
                GameSession sess = activeGames.get(world);
                if (sess == null) {
                    cancel();
                    return;
                }
                Iterator<ArmorStand> it = sess.monsterTriggers.iterator();
                while (it.hasNext()) {
                    ArmorStand as = it.next();
                    if (as.isDead()) {
                        it.remove();
                        continue;
                    }
                    boolean triggered = false;
                    for (Player p : sess.onlinePlayers) {
                        if (p.getGameMode() == GameMode.SPECTATOR) continue;
                        if (p.isOnline() && p.getWorld().equals(world) &&
                                p.getLocation().distance(as.getLocation()) <= TRIGGER_DISTANCE) {
                            triggered = true;
                            break;
                        }
                    }
                    if (triggered) {
                        Location triggerLoc = as.getLocation();
                        boolean isHalf = as.hasMetadata("half_amount");
                        int normalCount = isHalf ? 1 : 2;
                        Monsters.INSTANCE.randomMobs(triggerLoc, normalCount);
                        if (isHalf) {
                            if (new Random().nextBoolean()) {
                                Monsters.INSTANCE.randomArc(triggerLoc, 1);
                            }
                        } else {
                            Monsters.INSTANCE.randomArc(triggerLoc, 1);
                        }
                        as.remove();
                        it.remove();
                        sess.scheduleRegeneration(triggerLoc);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 10L);
        activeGames.put(world, session);
    }

    @EventHandler
    public void onPlayerJoinMidgame(PlayerJoinMidgameEvent event) {
        World world = event.getWorld();
        Player player = event.getPlayer();
        GameSession session = activeGames.get(world);
        if (session == null) {
            player.sendMessage("§c当前没有可加入的游戏。");
            return;
        }
        if (session.isEnding) {
            player.sendMessage("§c游戏即将结束，无法中途加入。");
            return;
        }

        // 判断玩家是否曾经属于本局游戏（掉线重连）
        if (session.allPlayers.contains(player.getUniqueId())) {
            // 掉线重连，恢复游戏状态
            session.playerReconnect(player);
            player.sendMessage("§a你已重新加入游戏！剩余时间：" +
                    String.format("%02d:%02d", session.remainingSeconds / 60, session.remainingSeconds % 60));
            Bukkit.broadcastMessage("§e" + player.getName() + " 重新加入了游戏！");
            return;
        }

        // 以下为中途新加入的逻辑（原代码不变）
        if (session.exitedPlayers.contains(player.getName())) {
            player.sendMessage("§c你已在本局游戏中退出，无法重新加入，只能观战。");
            player.setGameMode(GameMode.SPECTATOR);
            // 随机传送到一个在游戏中的玩家位置
            List<Player> onlinePlayers = new ArrayList<>(session.onlinePlayers);
            onlinePlayers.removeIf(p -> !p.isOnline() || p.getGameMode() == GameMode.SPECTATOR);
            if (!onlinePlayers.isEmpty()) {
                Player target = onlinePlayers.get(rand.nextInt(onlinePlayers.size()));
                player.teleport(target.getLocation());
                player.sendMessage("§7你正在观战 " + target.getName() + " 的游戏");
            } else {
                player.teleport(world.getSpawnLocation());
                player.sendMessage("§c当前没有其他玩家可供观战");
            }
            // 设置观战状态标记
            PlayerStats.INSTANCE.setSpectating(player, GameStatus.INSTANCE.getWorldId(world.getName()));
            // 清除可能残留的准备状态
            clearPlayerReady(player, world.getName());
            return;
        }
        Map<String, Location> playerSpawns = LocationManagerUI.getLocationsByType(world.getName(),
                LocationManagerUI.LocationType.PLAYER_SPAWN);
        List<Location> spawnList = new ArrayList<>(playerSpawns.values());
        if (spawnList.isEmpty()) {
            player.sendMessage("§c游戏配置错误：缺少玩家生成点，无法加入。");
            return;
        }
        Location spawnLoc = spawnList.get(rand.nextInt(spawnList.size()));
        player.setHealth(20);
        player.setFoodLevel(20);
        player.teleport(spawnLoc);
        player.setGameMode(GameMode.ADVENTURE);
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20, 0));
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
        session.onlinePlayers.add(player);
        session.allPlayers.add(player.getUniqueId());
        session.profits.put(player.getUniqueId(), 0.0);
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard sb = manager.getNewScoreboard();
        Objective obj = sb.registerNewObjective("game", "dummy", "§6MineRaiders");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        session.playerScoreboards.put(player, sb);
        session.playerObjectives.put(player, obj);
        player.setScoreboard(sb);
        session.updateScoreboard();
        PlayerStats.INSTANCE.setInGame(player);
        // 中途加入时清除其准备状态
        clearPlayerReady(player, world.getName());
        startContainerHighlight(player);
        player.sendMessage("§a你已中途加入游戏！剩余时间：" +
                String.format("%02d:%02d", session.remainingSeconds / 60, session.remainingSeconds % 60));
        Bukkit.broadcastMessage("§e" + player.getName() + " 中途加入了游戏！");
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        GameSession session = activeGames.get(world);
        if (session == null) return;
        if (!session.onlinePlayers.contains(player)) return;
        if (player.getGameMode() == GameMode.SPECTATOR) return;
        Entity clicked = event.getRightClicked();
        // 处理撤离点雪傀儡的交互
        if (!(clicked instanceof Snowman golem)) return;
        if (!session.evacuationGolems.contains(golem)) return;
        event.setCancelled(true);
        if (player.getCooldown(Material.JACK_O_LANTERN) == 0) {
            player.setCooldown(Material.JACK_O_LANTERN, 10);
            // 如果游戏已经结束（等待充能中），禁止新的激活
            if (session.isEnding) {
                player.sendMessage("§c游戏已结束，无法激活撤离点！");
                return;
            }
            // 优先检查是否处于撤离窗口期
            if (session.evacuationWindows.containsKey(golem)) {
                double radius = EVACUATION_RADIUS;
                List<Player> playersToEvacuate = new ArrayList<>();
                for (Entity e : golem.getNearbyEntities(radius, radius, radius)) {
                    if (e instanceof Player p && e.getLocation().distanceSquared(golem.getLocation()) <= radius * radius) {
                        playersToEvacuate.add(p);
                    }
                }
                // 如果有玩家撤离，先取消窗口任务（避免重复操作）
                if (!playersToEvacuate.isEmpty()) {
                    cancelEvacuationWindow(golem, session);
                    cancelChargeTask(golem, session);
                    for (Player p : playersToEvacuate) {
                        evacuatePlayer(p, golem, session);
                    }
                }
                return;
            }
            // 检查是否正在充能
            if (session.chargeTasks.containsKey(golem)) {
                player.sendMessage("§c撤离点正在充能中，请稍后！");
                return;
            }
            // 未激活状态 -> 开始充能
            startCharging(golem, session, player);
        }
    }

    @EventHandler
    public void PlayerClickArmorStand(PlayerArmorStandManipulateEvent event) {
        ArmorStand stand = event.getRightClicked();
        // 处理遗物盔甲架的右键交互
        if (isLootStand(stand)) {
            event.setCancelled(true);
            spawnItems(stand);
        }
    }

    @EventHandler
    public void onPlayerPickupItem(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player) {
            World world = player.getWorld();
            GameSession session = activeGames.get(world);
            if (session == null) return;
            if (!session.onlinePlayers.contains(player)) return;
            ItemStack item = event.getItem().getItemStack();
            double value = getItemValue(item);
            if (value > 0) {
                session.addProfit(player, value);
            }
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        GameSession session = activeGames.get(world);
        if (session == null) return;
        if (!session.onlinePlayers.contains(player)) return;
        ItemStack item = event.getItemDrop().getItemStack();
        double value = getItemValue(item);
        if (value > 0) {
            session.addProfit(player, -value);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        World world = player.getWorld();
        GameSession session = activeGames.get(world);
        if (session == null) return;
        if (!session.allPlayers.contains(player.getUniqueId())) return;
        // 执行撤离失败处理
        performFailedExtract(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        // 遍历所有活跃游戏，找到玩家所在的会话
        for (Map.Entry<World, GameSession> entry : activeGames.entrySet()) {
            GameSession session = entry.getValue();
            // 检查该玩家是否属于此游戏（通过 allPlayers 判断）
            if (session.allPlayers.contains(player.getUniqueId())) {
                // 玩家掉线，不执行撤离失败，仅从在线集合中移除
                session.playerDisconnect(player);
                break;
            }
        }

        // 无论玩家是否在游戏中，退出时都要清除其准备状态，避免状态残留
        if (PlayerStats.INSTANCE.isReady(player)) {
            int readyMapId = PlayerStats.INSTANCE.getReadyStatus(player);
            if (readyMapId != -1) {
                String worldName = GameStatus.INSTANCE.getWorlds(readyMapId);
                if (worldName != null && !worldName.isEmpty()) {
                    // 清除该玩家的准备状态
                    clearPlayerReady(player, worldName);
                    // 广播通知所有玩家，该玩家因退出取消了准备
                    String mapName = GameStatus.INSTANCE.getWorldNameByID(readyMapId);
                    Bukkit.broadcastMessage("§e" + player.getName() + "§a退出了游戏，取消了对地图§e" + mapName + "§a的准备");
                }
            }
        }
    }

    @EventHandler
    public void onPlayerExtract(PlayerExtractEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        GameSession session = activeGames.get(world);
        if (session == null) return;
        if (!session.allPlayers.contains(player.getUniqueId())) return;
        // 记录成功撤离的玩家（加入已退出集合）
        session.exitedPlayers.add(player.getName());
        // 如果玩家处于倒地状态，清除倒地状态
        if (PlayerStats.INSTANCE.isDying(player)) {
            BukkitRunnable timer = PlayerListener.dyingTimers.remove(player);
            if (timer != null) timer.cancel();
            PlayerStats.INSTANCE.stopDying(player);
            player.removePotionEffect(PotionEffectType.WEAKNESS);
            player.removePotionEffect(PotionEffectType.MINING_FATIGUE);
        }
        // 从游戏会话中移除玩家
        session.removePlayer(player);
        PlayerStats.INSTANCE.stopInGame(player);
        PlayerStats.INSTANCE.removeShieldBar(player);
        player.setCustomNameVisible(true);
        // 清除准备状态
        clearPlayerReady(player, world.getName());
        // 传送到世界出生点
        Location spawn = world.getSpawnLocation();
        player.teleport(spawn);
        // 计算用时（秒）
        int elapsedSeconds = session.totalGameSeconds - session.remainingSeconds;
        int minutes = elapsedSeconds / 60;
        int seconds = elapsedSeconds % 60;
        String timeStr = String.format("%d分%d秒", minutes, seconds);
        // 获取收益
        double profit = session.profits.getOrDefault(player.getUniqueId(), 0.0);
        // 发送消息给玩家本人
        player.sendMessage("§a[撤离成功] 用时: " + timeStr + " 收益: " + String.format("%.1f", profit));
    }

    @EventHandler
    public void onPlayerFailToExtract(PlayerFailToExtractEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        GameSession session = activeGames.get(world);
        if (session == null || !session.allPlayers.contains(player.getUniqueId())) return;

        // 执行统一的撤离失败清理
        performFailedExtract(player);
    }

    /**
     * 游戏结束事件处理：统一清理所有游戏资源
     */
    @EventHandler
    public void onGameEnd(GameEndEvent event) {
        World world = event.getWorld();
        GameSession session = activeGames.get(world);
        if (session == null) return;
        // 1. 取消所有重生任务
        for (BukkitTask task : session.regenerationTasks) {
            if (task != null && !task.isCancelled()) task.cancel();
        }
        session.regenerationTasks.clear();
        // 2. 取消所有撤离点充能任务
        for (Map.Entry<Snowman, BukkitTask> entry : session.chargeTasks.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isCancelled()) {
                entry.getValue().cancel();
            }
            // 重置撤离点外观（如果还没移除）
            Snowman golem = entry.getKey();
            if (!golem.isDead()) {
                golem.setGlowing(true);
                golem.setCustomName("§a撤离点");
            }
        }
        session.chargeTasks.clear();
        // 3. 取消所有撤离窗口期任务
        for (Map.Entry<Snowman, BukkitTask> entry : session.evacuationWindows.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isCancelled()) {
                entry.getValue().cancel();
            }
            Snowman golem = entry.getKey();
            if (!golem.isDead()) {
                golem.setGlowing(true);
                golem.setCustomName("§a撤离点");
            }
        }
        session.evacuationWindows.clear();
        // 4. 清理所有游戏实体
        for (ArmorStand as : session.monsterTriggers) {
            if (!as.isDead()) as.remove();
        }
        session.monsterTriggers.clear();
        for (Snowman golem : session.evacuationGolems) {
            if (!golem.isDead()) golem.remove();
        }
        session.evacuationGolems.clear();
        // 5. 清理所有玩家的独立计分板
        for (Player p : new ArrayList<>(session.playerScoreboards.keySet())) {
            if (p.isOnline()) {
                p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
            }
        }
        session.playerScoreboards.clear();
        session.playerObjectives.clear();
        // 6. 处理观战该世界的玩家
        int worldId = GameStatus.INSTANCE.getWorldId(world.getName());
        for (Player p : world.getPlayers()) {
            if (PlayerStats.INSTANCE.isSpectating(p) && PlayerStats.INSTANCE.getSpectatingStatus(p) == worldId) {
                p.teleport(world.getSpawnLocation());
                p.setGameMode(GameMode.SURVIVAL);
                PlayerStats.INSTANCE.stopSpectating(p);
                // 观战玩家也清除准备状态
                clearPlayerReady(p, world.getName());
                p.sendMessage("§c游戏结束，返回上层。");
            }
        }
        // 7. 清除世界中所有非玩家实体（防止残留）
        for (Entity e : world.getEntities()) {
            if (!(e instanceof Player)) {
                e.remove();
            }
        }
        // 8. 对所有游戏内玩家执行撤离失败
        for (Player p : new ArrayList<>(session.onlinePlayers)) {
            if (p.isOnline()) {
                performFailedExtract(p, true);
            }
        }
        // 9. 清除该世界的所有准备状态
        clearWorldReady(world.getName());
        // 10. 从全局活跃游戏映射中移除
        activeGames.remove(world);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Snowman golem)) return;
        World world = entity.getWorld();
        GameSession session = activeGames.get(world);
        if (session == null) return;
        if (!session.evacuationGolems.contains(golem)) return;
        // 如果撤离点在充能或窗口期，取消任务
        cancelChargeTask(golem, session);
        cancelEvacuationWindow(golem, session);
        session.evacuationGolems.remove(golem);
        session.removeActiveGolem(golem);
    }

    private void spawnItems(ArmorStand stand) {
        // 获取物品列表，并立即从映射中移除，避免重复触发
        List<ItemStack> items = lootMap.remove(stand);
        if (items == null) return;
        // 获取位置并移除盔甲架（避免再次交互）
        Location eyeLoc = stand.getEyeLocation();
        World world = stand.getWorld();
        if (!stand.isDead()) stand.remove();
        // 若没有物品，直接播放效果后返回
        if (items.isEmpty()) {
            world.playSound(eyeLoc, Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
            world.spawnParticle(Particle.EXPLOSION, eyeLoc, 1);
            return;
        }
        // 使用迭代器逐个掉落物品，每掉落一个就从列表中移除，防止重复
        Iterator<ItemStack> iterator = items.iterator();
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!iterator.hasNext()) {
                    this.cancel();
                    return;
                }
                ItemStack stack = iterator.next();
                iterator.remove();  // 已处理，从列表中移除
                double x = rand.nextDouble() - rand.nextDouble();
                double y = 5;
                double z = rand.nextDouble() - rand.nextDouble();
                Vector spread = new Vector(x, y, z);
                Item item = world.dropItem(eyeLoc, stack);
                int rarity = lp.getRarity(stack);
                Sound s = switch (rarity) {
                    case 0, 1, 2 -> Sound.UI_LOOM_TAKE_RESULT;
                    case 3 -> Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
                    case 4 -> Sound.ENTITY_PLAYER_LEVELUP;
                    case 5 -> Sound.UI_TOAST_CHALLENGE_COMPLETE;
                    default -> Sound.ENTITY_ITEM_PICKUP;
                };
                if (rarity < 0) rarity = 2;
                item.setTicksLived(6000 - (200 * (rarity + 1)));
                item.setVelocity(spread.multiply(0.1));
                world.playSound(eyeLoc, s, 1, 1);
                world.spawnParticle(Particle.EXPLOSION, eyeLoc, 1);
            }
        }.runTaskTimer(plugin, 0L, 5L);
        // 保留随机音符盒特效（不影响物品掉落）
        if (rand.nextInt(10) == 0) {
            k.esterEgg0(eyeLoc);
        }
    }

    // ========================= 辅助方法 =========================
    private double getItemValue(ItemStack item) {
        int rarity = LootPool.INSTANCE.getRarity(item);
        if (rarity < 0) return 0;
        double[] prices = LootPool.INSTANCE.getPrices();
        if (rarity >= prices.length) return 0;
        return prices[rarity];
    }

    // ========================= 公共静态方法 =========================
    public static boolean isLootStand(ArmorStand stand) {
        return lootMap.containsKey(stand);
    }

    public static boolean isGameActive(World world) {
        return activeGames.containsKey(world);
    }

    public static int getPlayerCount(World world) {
        GameSession session = activeGames.get(world);
        return session != null ? session.onlinePlayers.size() : 0;
    }

    public static GameSession getSession(World world) {
        return activeGames.get(world);
    }
}