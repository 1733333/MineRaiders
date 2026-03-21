package Listeners;

import Events.*;
import Universal.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.*;

import java.util.*;

public class GameListener implements Listener {
    private static JavaPlugin plugin = null;
    private static final Map<World, GameSession> activeGames = new HashMap<>();
    Kit k = Kit.INSTANCE;
    PlayerStats playerStats = PlayerStats.INSTANCE;
    GameStatus gameStatus = GameStatus.INSTANCE;
    // 配置参数
    private static final int MIN_DISTANCE_TO_SPAWN = 10;       // 怪物生成点与玩家生成点最小距离
    private static final int TRIGGER_DISTANCE = 15;           // 触发怪物刷新的距离
    private static final int EVACUATION_CHARGE_SECONDS = 60;   // 铁傀儡充能时间（秒）
    private static final double EVACUATION_RADIUS = 5.0;      // 撤离生效半径

    public GameListener(JavaPlugin plugin) {
        GameListener.plugin = plugin;
    }

    // ========================= 游戏会话内部类 =========================
    private static class GameSession {
        final World world;
        final Set<Player> players;
        final Map<UUID, Double> profits;          // 玩家收益
        final Scoreboard scoreboard;
        final Objective objective;
        final BukkitTask timerTask;
        int remainingSeconds;                      // 剩余时间（秒）
        boolean isEnding = false;                  // 游戏是否正在结束（等待充能完成）
        final Set<IronGolem> activeGolems = new HashSet<>(); // 正在充能的铁傀儡

        // 怪物刷新盔甲架列表
        final List<ArmorStand> monsterTriggers = new ArrayList<>();
        // 普通撤离点铁傀儡列表
        final List<IronGolem> evacuationGolems = new ArrayList<>();

        GameSession(World world, Set<Player> players, int totalSeconds) {
            this.world = world;
            this.players = new HashSet<>(players);
            this.profits = new HashMap<>();
            for (Player p : players) {
                profits.put(p.getUniqueId(), 0.0);
            }
            this.remainingSeconds = totalSeconds;

            // 创建计分板
            ScoreboardManager manager = Bukkit.getScoreboardManager();
            this.scoreboard = manager.getNewScoreboard();
            this.objective = scoreboard.registerNewObjective("game", "dummy", "§6MineRaiders");
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);
            updateScoreboard();

            for (Player p : players) {
                p.setScoreboard(scoreboard);
            }

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
                String message = "";
                if (remainingSeconds == 600) message = "§e游戏剩余时间：10分钟！";
                else if (remainingSeconds == 300) message = "§e游戏剩余时间：5分钟！";
                else if (remainingSeconds == 60) message = "§c游戏剩余时间：1分钟！";
                for (Player p : players) {
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
                // 没有激活的铁傀儡，直接结束
                endGame();
            } else {
                // 有激活的铁傀儡，等待它们完成
                for (Player p : players) {
                    if (p.isOnline()) {
                        p.sendMessage("§c游戏时间已到，等待所有撤离点关闭...");
                    }
                }
                // 游戏结束标志已设置，无需额外操作
            }
        }

        void updateScoreboard() {
            // 为每个玩家独立更新计分板条目（动态文本）
            for (Player p : players) {
                if (!p.isOnline()) continue;

                Scoreboard sb = p.getScoreboard();
                if (sb != scoreboard) {
                    p.setScoreboard(scoreboard);
                }

                Objective obj = sb.getObjective("game");
                if (obj == null) continue;

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

                // 按顺序添加条目（时间在上，收益在下）
                obj.getScore(timeEntry).setScore(1);
                obj.getScore(profitEntry).setScore(0);
            }
        }

        void addProfit(Player player, double amount) {
            UUID uuid = player.getUniqueId();
            profits.put(uuid, profits.getOrDefault(uuid, 0.0) + amount);
            updateScoreboard();
        }

        void removePlayer(Player player) {
            players.remove(player);
            player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
            // 如果所有玩家都已离开，游戏应该结束（无论是否在等待充能）
            if (players.isEmpty()) {
                endGame();
            }
        }

        void endGame() {
            timerTask.cancel();
            // 清理所有实体
            for (ArmorStand as : monsterTriggers) {
                as.remove();
            }
            for (IronGolem golem : evacuationGolems) {
                golem.remove();
            }
            // 强制所有在游戏中的玩家撤离失败（使用副本避免并发修改）
            for (Player p : new ArrayList<>(players)) {
                handleExtract(p, false, this);
            }
            // 处理观战本世界的玩家
            int worldId = GameStatus.INSTANCE.getWorldId(world.getName());
            for (Player p : world.getPlayers()) {
                if (PlayerStats.INSTANCE.isSpectating(p) && PlayerStats.INSTANCE.getSpectatingStatus(p) == worldId) {
                    Location spawn = world.getSpawnLocation();
                    p.teleport(spawn);
                    p.setGameMode(org.bukkit.GameMode.SURVIVAL);
                    PlayerStats.INSTANCE.stopSpectating(p);
                    p.sendMessage("§c游戏结束，返回上层。");
                }
            }
            Bukkit.getPluginManager().callEvent(new GameEndEvent(world));
            for (Entity e : world.getEntities()) {
                if (!(e instanceof Player)) {
                    e.remove();
                }
            }
            activeGames.remove(world);
        }

        void addActiveGolem(IronGolem golem) {
            activeGolems.add(golem);
        }

        void removeActiveGolem(IronGolem golem) {
            activeGolems.remove(golem);
            // 如果游戏处于结束等待状态且没有其他激活的铁傀儡，则结束游戏
            if (isEnding && activeGolems.isEmpty()) {
                endGame();
            }
        }
    }

    // ========================= 辅助方法 =========================
    private void startContainerHighlight(Player player, World world) {
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
                            Block block = world.getBlockAt(bLoc);
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

    // ========================= 事件处理 =========================
    @EventHandler
    public void onGameStart(GameStartEvent event) {
        World world = event.getWorld();
        if (activeGames.containsKey(world)) {
            activeGames.get(world).endGame();
        }

        // 收集准备就绪的玩家
        Set<Player> readyPlayers = new HashSet<>();
        for (Player p : world.getPlayers()) {
            if (PlayerStats.INSTANCE.isReady(p)) {
                readyPlayers.add(p);
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

        // 随机分配玩家到生成点，并标记为游戏中，清除准备状态
        List<Player> playersToTeleport = new ArrayList<>(readyPlayers);
        Collections.shuffle(playersToTeleport);
        for (int i = 0; i < playersToTeleport.size(); i++) {
            Location spawnLoc = spawnList.get(i % spawnList.size());
            playersToTeleport.get(i).teleport(spawnLoc);
            PlayerStats.INSTANCE.setInGame(playersToTeleport.get(i));
            PlayerStats.INSTANCE.stopReady(playersToTeleport.get(i));
        }

        // 为每个玩家添加容器高亮任务
        for (Player p : playersToTeleport) {
            startContainerHighlight(p, world);
        }

        // 创建游戏会话（使用准备玩家作为游戏玩家）
        GameSession session = new GameSession(world, readyPlayers, 30 * 60);

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
            if (tooClose) continue;
            ArmorStand as = world.spawn(loc, ArmorStand.class);
            as.setVisible(false);
            as.setGravity(false);
            as.setMarker(true);
            as.setInvulnerable(true);
            as.setCustomName("MonsterTrigger");
            as.setCustomNameVisible(false);
            session.monsterTriggers.add(as);
        }

        // 处理特殊怪物点
        Map<String, Location> specialMonsterSpawns = LocationManagerUI.getLocationsByType(world.getName(),
                LocationManagerUI.LocationType.SPECIAL_MONSTER_SPAWN);
        Random rand = new Random();
        for (Map.Entry<String, Location> entry : specialMonsterSpawns.entrySet()) {
            String name = entry.getKey();
            Location loc = entry.getValue();
            LocationManagerUI.LocationData data = LocationManagerUI.getLocation(world.getName(), name);
            int id = (data != null && data.extraId() != null) ? data.extraId() : rand.nextInt(8);
            Monsters.INSTANCE.summonArc(loc, id);
        }

        // 处理普通撤离点：生成无AI铁傀儡
        Map<String, Location> evacuationSpawns = LocationManagerUI.getLocationsByType(world.getName(),
                LocationManagerUI.LocationType.EVACUATION);
        for (Location loc : evacuationSpawns.values()) {
            IronGolem golem = world.spawn(loc, IronGolem.class);
            golem.setAI(false);
            golem.setGravity(false);
            golem.setInvulnerable(true);
            golem.setSilent(true);
            golem.setCustomName("§a撤离点");
            golem.setCustomNameVisible(true);
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
                    for (Player p : sess.players) {
                        if (p.isOnline() && p.getWorld().equals(world) &&
                                p.getLocation().distance(as.getLocation()) <= TRIGGER_DISTANCE) {
                            Location triggerLoc = as.getLocation();
                            Monsters.INSTANCE.randomMobs(triggerLoc, 4);
                            for (int i = 0; i < 2; i++) {
                                Monsters.INSTANCE.summonArc(triggerLoc, rand.nextInt(8));
                            }
                            as.remove();
                            it.remove();
                            break;
                        }
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
            // 没有正在进行的游戏
            player.sendMessage("§c当前没有可加入的游戏。");
            return;
        }
        if (session.isEnding) {
            player.sendMessage("§c游戏即将结束，无法中途加入。");
            return;
        }
        if (session.players.contains(player)) {
            // 已经在游戏中，无需处理
            return;
        }

        // 获取玩家生成点
        Map<String, Location> playerSpawns = LocationManagerUI.getLocationsByType(world.getName(),
                LocationManagerUI.LocationType.PLAYER_SPAWN);
        List<Location> spawnList = new ArrayList<>(playerSpawns.values());
        if (spawnList.isEmpty()) {
            player.sendMessage("§c游戏配置错误：缺少玩家生成点，无法加入。");
            return;
        }

        // 随机传送至一个生成点
        Random rand = new Random();
        Location spawnLoc = spawnList.get(rand.nextInt(spawnList.size()));
        player.teleport(spawnLoc);

        // 添加到游戏会话
        session.players.add(player);
        session.profits.put(player.getUniqueId(), 0.0);
        // 设置计分板
        player.setScoreboard(session.scoreboard);
        // 更新计分板显示
        session.updateScoreboard();

        // 标记为游戏中
        PlayerStats.INSTANCE.setInGame(player);

        // 启动容器高亮任务
        startContainerHighlight(player, world);

        // 发送欢迎消息
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
        if (!session.players.contains(player)) return;

        Entity clicked = event.getRightClicked();
        if (!(clicked instanceof IronGolem golem)) return;
        if (!session.evacuationGolems.contains(golem)) return;

        event.setCancelled(true); // 防止交互默认行为

        // 游戏正在结束中（时间已到），禁止新的激活
        if (session.isEnding) {
            player.sendMessage("§c游戏已结束，无法激活撤离点！");
            return;
        }

        // 检查是否已经充能中（通过Metadata标记）
        if (golem.hasMetadata("charging")) {
            player.sendMessage("§c撤离点正在充能中，请稍后！");
            return;
        }

        // 开始充能
        player.sendMessage("§a你激活了撤离点！" + EVACUATION_CHARGE_SECONDS + "秒后撤离。");
        golem.setCustomName("§e撤离点 (充能中)");
        golem.setMetadata("charging", new FixedMetadataValue(plugin, true));
        session.addActiveGolem(golem);

        // 充能倒计时
        new BukkitRunnable() {
            int timeLeft = EVACUATION_CHARGE_SECONDS;

            @Override
            public void run() {
                if (golem.isDead() || !session.evacuationGolems.contains(golem)) {
                    // 铁傀儡已消失，取消
                    golem.removeMetadata("charging", plugin);
                    session.removeActiveGolem(golem);
                    cancel();
                    return;
                }
                if (timeLeft <= 0) {
                    // 充能完成，执行撤离
                    golem.removeMetadata("charging", plugin);
                    Location center = golem.getLocation();
                    List<Player> toExtract = new ArrayList<>();
                    for (Player p : session.players) {
                        if (p.isOnline() && p.getWorld().equals(world) &&
                                p.getLocation().distance(center) <= EVACUATION_RADIUS) {
                            toExtract.add(p);
                        }
                    }
                    if (toExtract.isEmpty()) {
                        golem.setCustomName("§a撤离点");
                    } else {
                        for (Player p : toExtract) {
                            handleExtract(p, true, session);
                        }
                    }
                    // 撤离后移除铁傀儡
                    golem.remove();
                    session.evacuationGolems.remove(golem);
                    session.removeActiveGolem(golem);
                    cancel();
                    return;
                }

                // 显示充能粒子效果
                Location loc = golem.getLocation().add(0, 1, 0);
                world.spawnParticle(Particle.END_ROD, loc, 20, 0.5, 0.5, 0.5, 0.1);
                world.spawnParticle(Particle.FIREWORK, loc, 10, 0.5, 0.5, 0.5, 0.05);
                world.playSound(loc, Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.0f);
                k.spawnCircleParticles(golem.getLocation(), EVACUATION_RADIUS, 50);
                // 更新显示倒计时
                golem.setCustomName("§e即将撤离，剩余时间： " + timeLeft + " 秒");
                timeLeft--;
            }
        }.runTaskTimer(plugin, 0L, 20L); // 每秒执行
    }

    @EventHandler
    public void onPlayerPickupItem(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player) {
            World world = player.getWorld();
            GameSession session = activeGames.get(world);
            if (session == null) return;
            if (!session.players.contains(player)) return;

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
        if (!session.players.contains(player)) return;

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
        if (!session.players.contains(player)) return;

        handleExtract(player, false, session);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        GameSession session = activeGames.get(world);
        if (session == null) return;
        if (!session.players.contains(player)) return;

        handleExtract(player, false, session);
    }

    @EventHandler
    public void onGameEnd(GameEndEvent event) {
        World world = event.getWorld();
        GameSession session = activeGames.get(world);
        if (session != null) {
            activeGames.remove(world);
            for (Player p : session.players) {
                if (p.isOnline()) {
                    p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
                }
            }
        }
    }

    // ========================= 辅助方法 =========================
    private static void handleExtract(Player player, boolean success, GameSession session) {
        World world = session.world;
        Location spawn = world.getSpawnLocation();

        if (success) {
            player.teleport(spawn);
            PlayerStats.INSTANCE.stopInGame(player);
            player.sendMessage("§a撤离成功！返回上层");
        } else {
            player.teleport(spawn);
            player.getInventory().clear();
            PlayerStats.INSTANCE.stopInGame(player);
            player.sendMessage("§c撤离失败！下次再接再厉");
        }

        session.removePlayer(player);

        // 注意：如果游戏已在等待结束（isEnding），但所有玩家都已离开，endGame 会在 removePlayer 中触发
        // 无需额外操作
    }

    private double getItemValue(ItemStack item) {
        int rarity = LootPool.INSTANCE.getRarity(item);
        if (rarity < 0) return 0;
        double[] prices = LootPool.INSTANCE.getPrices();
        if (rarity >= prices.length) return 0;
        return prices[rarity];
    }

// ========================= 公共静态方法 =========================
    /**
     * 检查指定世界是否有正在进行的游戏
     */
    public static boolean isGameActive(World world) {
        return activeGames.containsKey(world);
    }

    /**
     * 获取指定世界当前游戏中的玩家数量
     */
    public static int getPlayerCount(World world) {
        GameSession session = activeGames.get(world);
        return session != null ? session.players.size() : 0;
    }

    /**
     * 获取指定世界的游戏会话（仅限内部使用）
     */
    public static GameSession getSession(World world) {
        return activeGames.get(world);
    }
}