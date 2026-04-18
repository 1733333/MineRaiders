package OtherStuff;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;
import org.bukkit.util.Vector;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.noise.SimplexNoiseGenerator;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
/**
 * 吸血鬼幸存者完整版 - 修改版
 * 已修改：移除竞技场地形生成、新增武器与被动、怪物免疫窒息伤害
 * 包含：更多武器、敌人种类、详细描述的被动道具、经验球、BOSS波次、炫酷博彩特效
 */
public class VampireSurvivorGame implements Listener, CommandExecutor {
    private final JavaPlugin plugin;
    private Player player;
    private final Set<LivingEntity> enemies = new HashSet<>();
    private final List<Weapon> weapons = new ArrayList<>();
    private final List<PassiveItem> passives = new ArrayList<>();
    private final Random random = new Random();
    // 竞技场相关已移除地形生成，仅保留中心定位
    private Location arenaCenter;
    // 游戏状态
    private int killCount = 0;
    private int totalKills = 0;
    private int exp = 0;
    private int expToNextLevel = 10;
    private int playerLevel = 1;
    private int wave = 1;
    private int enemiesToSpawn = 10;
    private int spawnedThisWave = 0;
    private boolean bossWave = false;
    private boolean running = false;
    private BukkitRunnable gameTask;
    // UI组件
    private BossBar expBar;
    private Scoreboard scoreboard;
    private Objective objective;
    private UpgradeMenu currentMenu;
    private LivingEntity currentBoss;
    private long bossLastSkillTime;
    private BossBar bossBar;
    // 玩家属性
    private double maxHealth = 20.0;
    private double armor = 0.0;
    private double pickupRange = 3.0;
    private double damageMultiplier = 1.0;
    private double cooldownReduction = 0.0;
    private double areaRadius = 30.0;
    private long lastRegenTime = 0;
    // 经验球列表
    private final List<ExperienceOrb> expOrbs = new ArrayList<>();
    // 博彩特效相关
    private final String[] slotSymbols = {"♠", "♥", "♦", "♣", "★", "☀", "♛", "♚", "♞", "♝"};
    private int slotTaskId = -1;
    public VampireSurvivorGame(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    // ---------- 命令 ----------
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(ChatColor.RED + "只有玩家可以使用此命令。");
            return true;
        }
        if (running) stop();
        this.player = p;
        start();
        return true;
    }
    // ---------- 游戏控制 ----------
    public void start() {
        if (running || player == null) return;
        running = true;
        // 重置玩家
        player.setGameMode(GameMode.ADVENTURE);
        player.getInventory().clear();
        AttributeInstance maxHpAttr = player.getAttribute(Attribute.MAX_HEALTH);
        if (maxHpAttr != null) maxHpAttr.setBaseValue(maxHealth);
        player.setHealth(maxHealth);
        player.setFoodLevel(20);
        player.setLevel(playerLevel);      // 显示等级数字
        player.setSaturation(5);
        arenaCenter = player.getLocation().clone();
        // 已移除：generateArena 不再生成竞技场地形
        weapons.clear();
        passives.clear();
        weapons.add(new WhipWeapon());
        // 初始赠送一个小磁铁被动
        passives.add(new MagnetPassive());
        // UI初始化
        initScoreboard();
        expBar = Bukkit.createBossBar("经验值", BarColor.GREEN, BarStyle.SEGMENTED_10);
        expBar.addPlayer(player);
        expBar.setProgress(0);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        lastRegenTime = System.currentTimeMillis();
        gameTask = new BukkitRunnable() {
            int tick = 0;
            @Override
            public void run() {
                if (!player.isOnline() || player.isDead() || !running) {
                    stop();
                    return;
                }
                // 边界限制（保留，防止玩家跑太远）
                if (player.getLocation().distance(arenaCenter) > areaRadius) {
                    Vector back = arenaCenter.toVector().subtract(player.getLocation().toVector()).normalize();
                    player.setVelocity(back.multiply(0.6));
                }
                Location playerLoc = player.getLocation();
                // 敌人AI
                Iterator<LivingEntity> enemyIter = enemies.iterator();
                while (enemyIter.hasNext()) {
                    LivingEntity e = enemyIter.next();
                    if (!e.isValid() || e.isDead()) {
                        enemyIter.remove();
                        continue;
                    }
                    Vector dir = playerLoc.toVector().subtract(e.getLocation().toVector()).normalize();
                    if (e == currentBoss) {
                        // BOSS专属AI
                        long now = System.currentTimeMillis();
                        // 每5秒释放一次技能
                        if (now - bossLastSkillTime > 5000) {
                            bossLastSkillTime = now;
                            int skill = random.nextInt(3);
                            if (skill == 0) {
                                // 冲刺技能
                                e.setVelocity(dir.multiply(1.5));
                                player.sendMessage(ChatColor.RED + "⚠ BOSS正在向你冲刺！快躲开！");
                                player.playSound(playerLoc, Sound.ENTITY_WITHER_SPAWN, 1.0f, 0.5f);
                            } else if (skill == 1) {
                                // 震地技能
                                if (e.getLocation().distance(playerLoc) < 4) {
                                    player.damage(6.0);
                                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1));
                                    player.getWorld().spawnParticle(Particle.CLOUD, e.getLocation(), 20, 2, 0, 2);
                                    player.playSound(playerLoc, Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 1.0f, 0.8f);
                                }
                            } else {
                                // 召唤小怪技能
                                for (int i = 0; i < 3; i++) {
                                    Location loc = e.getLocation().add(random.nextDouble() * 4 - 2, 0, random.nextDouble() * 4 - 2);
                                    Zombie zombie = (Zombie) e.getWorld().spawnEntity(loc, EntityType.ZOMBIE);
                                    zombie.setAdult();
                                    AttributeInstance zHp = zombie.getAttribute(Attribute.MAX_HEALTH);
                                    if (zHp != null) zHp.setBaseValue(10);
                                    zombie.setHealth(10);
                                    zombie.setRemoveWhenFarAway(false);
                                    enemies.add(zombie);
                                }
                                player.sendMessage(ChatColor.RED + "⚠ BOSS召唤了小怪支援！");
                                player.playSound(playerLoc, Sound.ENTITY_ZOMBIE_AMBIENT, 1.0f, 0.6f);
                            }
                        } else {
                            // BOSS普通移动，比普通怪更快
                            e.setVelocity(dir.multiply(0.25));
                        }
                        // 更新BOSS血条
                        if (bossBar != null) {
                            AttributeInstance bossMaxHp = e.getAttribute(Attribute.MAX_HEALTH);
                            if (bossMaxHp != null) {
                                bossBar.setProgress(Math.min(1.0, e.getHealth() / bossMaxHp.getValue()));
                            }
                        }
                    } else {
                        // 普通敌人AI
                        e.setVelocity(dir.multiply(e instanceof Monster ? 0.18 : 0.1));
                    }
                }
                // 武器攻击
                long effectiveCooldown = (long) (1.0 / (1.0 + cooldownReduction * 0.01));
                for (Weapon w : weapons) {
                    w.tryUse(player, enemies, playerLoc, effectiveCooldown);
                }
                // 经验球拾取
                double actualPickupRange = pickupRange;
                Iterator<ExperienceOrb> orbIter = expOrbs.iterator();
                while (orbIter.hasNext()) {
                    ExperienceOrb orb = orbIter.next();
                    if (!orb.isValid()) {
                        orbIter.remove();
                        continue;
                    }
                    orb.tick();
                    if (orb.getLocation().distance(playerLoc) <= actualPickupRange) {
                        addExperience(orb.getValue());
                        orb.remove();
                        orbIter.remove();
                        // 博彩拾取特效
                        player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, orb.getLocation(), 5, 0.3, 0.3, 0.3);
                        player.playSound(playerLoc, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.5f);
                    }
                }
                // 再生护符效果
                int regenCount = Math.toIntExact(passives.stream().filter(p -> p instanceof RegenAmuletPassive).count());
                if (regenCount > 0) {
                    long now = System.currentTimeMillis();
                    if (now - lastRegenTime > 5000) { // 每5秒回复一次
                        lastRegenTime = now;
                        double heal = 1.0 + regenCount * 0.5;
                        player.setHealth(Math.min(player.getHealth() + heal, maxHealth));
                        player.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0,1,0), 2);
                    }
                }
                // 波次管理
                if (enemies.isEmpty() && spawnedThisWave >= enemiesToSpawn) {
                    advanceWave();
                } else if (tick % 20 == 0 && enemies.size() < 15 && spawnedThisWave < enemiesToSpawn) {
                    spawnEnemy();
                    spawnedThisWave++;
                }
                // 更新计分板
                updateScoreboard();
                // 更新BossBar（基于内部exp）
                expBar.setProgress(Math.min(1.0, (double) exp / expToNextLevel));
                expBar.setTitle(ChatColor.GOLD + "⚡ 等级 " + playerLevel + " | 经验 " + exp + "/" + expToNextLevel + " ⚡");
                tick++;
            }
        };
        gameTask.runTaskTimer(plugin, 0L, 1L);
        player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "★ 吸血鬼幸存者开始！活下来！ ★");
        player.sendMessage(ChatColor.YELLOW + "击杀敌人获得经验，升级选择强化！");
        playSlotMachineEffect(player, "游戏开始", 40);
    }
    public void stop() {
        if (!running) return;
        running = false;
        if (gameTask != null) gameTask.cancel();
        HandlerList.unregisterAll(plugin);
        enemies.forEach(Entity::remove);
        enemies.clear();
        expOrbs.forEach(ExperienceOrb::remove);
        expOrbs.clear();
        // 已移除：removeArena 不再需要清理竞技场方块
        if (expBar != null) {
            expBar.removeAll();
            expBar = null;
        }
        if (bossBar != null) {
            bossBar.removeAll();
            bossBar = null;
        }
        if (scoreboard != null) {
            player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        }
        if (player != null && player.isOnline()) {
            player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "游戏结束！ 总计杀敌: " + totalKills + " 达到第 " + wave + " 波");
            player.setGameMode(GameMode.SURVIVAL);
            AttributeInstance maxHpAttr = player.getAttribute(Attribute.MAX_HEALTH);
            if (maxHpAttr != null) maxHpAttr.setBaseValue(20);
            // 重置移动速度
            AttributeInstance speedAttr = player.getAttribute(Attribute.MOVEMENT_SPEED);
            if (speedAttr != null) speedAttr.setBaseValue(0.2);
        }
        // 重置属性
        killCount = 0;
        totalKills = 0;
        exp = 0;
        playerLevel = 1;
        wave = 1;
        enemiesToSpawn = 10;
        spawnedThisWave = 0;
        weapons.clear();
        passives.clear();
        player = null;
    }
    private void advanceWave() {
        wave++;
        bossWave = (wave % 5 == 0);
        enemiesToSpawn = bossWave ? 5 : 10 + wave * 2;
        spawnedThisWave = 0;
        player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "⚔ 第 " + wave + " 波来袭！ ⚔");
        if (bossWave) {
            player.sendMessage(ChatColor.DARK_RED + "" + ChatColor.BOLD + "☠ BOSS 出现！ ☠");
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                spawnBoss();
            }, 60L);
        }
        // 波次奖励经验
        addExperience(20 * wave);
        // 波次博彩特效
        playWaveEffect();
    }
    private void spawnBoss() {
        Location spawnLoc = getSpawnLocation(12);
        Zombie giant = (Zombie) arenaCenter.getWorld().spawnEntity(spawnLoc, EntityType.ZOMBIE);
        giant.setCustomName(ChatColor.DARK_RED + "☠ 巨型僵尸王 ☠");
        giant.setCustomNameVisible(true);
        giant.getAttribute(Attribute.SCALE).setBaseValue(2);
        AttributeInstance hpAttr = giant.getAttribute(Attribute.MAX_HEALTH);
        double maxHp = 150 + wave * 30;
        if (hpAttr != null) hpAttr.setBaseValue(maxHp);
        giant.setHealth(maxHp);
        giant.setRemoveWhenFarAway(false);
        enemies.add(giant);
        spawnedThisWave++;
        // 初始化BOSS状态
        this.currentBoss = giant;
        this.bossLastSkillTime = System.currentTimeMillis();
        // 创建BOSS专属血条
        this.bossBar = Bukkit.createBossBar(giant.getCustomName(), BarColor.RED, BarStyle.SOLID);
        bossBar.addPlayer(player);
    }
    private void addExperience(int amount) {
        exp += amount;
        while (exp >= expToNextLevel) {
            exp -= expToNextLevel;
            levelUp();
        }
        // 不再调用 player.setLevel(exp) 或 player.setExp(...)
        // 完全依赖内部 exp 变量与 BossBar 显示
    }
    private void levelUp() {
        playerLevel++;
        expToNextLevel = (int) (expToNextLevel * 1.3);
        player.setLevel(playerLevel);  // 仅显示等级数字，非经验值
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f);
        player.sendMessage(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "✨ 升级！等级 " + playerLevel + " ✨");
        // 博彩老虎机特效
        playSlotMachineEffect(player, "升级奖励", 60);
        // 恢复部分生命
        player.setHealth(Math.min(player.getHealth() + 2, maxHealth));
        player.setFoodLevel(Math.min(player.getFoodLevel() + 2, 20));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, PotionEffect.INFINITE_DURATION, 10));
        // 打开升级菜单
        currentMenu = new UpgradeMenu(player, weapons, passives, this::onUpgradeSelected);
        currentMenu.open();
    }
    private void onUpgradeSelected(Consumer<Player> action) {
        action.accept(player);
        currentMenu = null;
        player.closeInventory();
        // 升级后重新计算属性
        recalculateStats();
    }
    private void recalculateStats() {
        maxHealth = 20.0 + passives.stream().filter(p -> p instanceof HealthPassive).count() * 4;
        armor = passives.stream().filter(p -> p instanceof ArmorPassive).count() * 2.0;
        pickupRange = 3.0 + passives.stream().filter(p -> p instanceof MagnetPassive).count() * 2.0;
        damageMultiplier = 1.0 + passives.stream().filter(p -> p instanceof StrengthPassive).count() * 0.2;
        cooldownReduction = passives.stream().filter(p -> p instanceof CooldownPassive).count() * 10.0;
        AttributeInstance maxHpAttr = player.getAttribute(Attribute.MAX_HEALTH);
        if (maxHpAttr != null) maxHpAttr.setBaseValue(maxHealth);
        if (player.getHealth() > maxHealth) player.setHealth(maxHealth);

        // 处理移动速度
        int speedCount = Math.toIntExact(passives.stream().filter(p -> p instanceof SwiftBootsPassive).count());
        double speedBonus = speedCount * 0.05;
        AttributeInstance speedAttr = player.getAttribute(Attribute.MOVEMENT_SPEED);
        if (speedAttr != null) {
            speedAttr.setBaseValue(0.2 * (1 + speedBonus)); // 基础移动速度是0.2
        }
    }
    private Location getSpawnLocation(double dist) {
        double angle = random.nextDouble() * 2 * Math.PI;
        Location loc = arenaCenter.clone().add(Math.cos(angle) * dist, 0, Math.sin(angle) * dist);
        loc.setY(arenaCenter.getWorld().getHighestBlockYAt(loc) + 1);
        return loc;
    }
    private void spawnEnemy() {
        Location spawnLoc = getSpawnLocation(11 + random.nextInt(3));
        EntityType type;
        double r = random.nextDouble();
        if (wave >= 4 && r < 0.3) type = EntityType.SKELETON;
        else if (wave >= 3 && r < 0.5) type = EntityType.SPIDER;
        else type = EntityType.ZOMBIE;
        LivingEntity enemy = (LivingEntity) arenaCenter.getWorld().spawnEntity(spawnLoc, type);
        enemy.setRemoveWhenFarAway(false);
        if (enemy instanceof Zombie z) z.setAdult();
        enemy.setCustomName(ChatColor.RED + enemy.getType().name());
        enemy.setCustomNameVisible(true);
        AttributeInstance hpAttr = enemy.getAttribute(Attribute.MAX_HEALTH);
        if (hpAttr != null) hpAttr.setBaseValue(15 + wave * 2);
        enemy.setHealth(hpAttr != null ? hpAttr.getBaseValue() : 20);
        enemies.add(enemy);
    }
    // ---------- 事件 ----------
    // 怪物免疫窒息伤害
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!running) return;
        if (event.getEntity() instanceof LivingEntity && enemies.contains(event.getEntity())) {
            if (event.getCause() == EntityDamageEvent.DamageCause.SUFFOCATION) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!running) return;
        LivingEntity e = event.getEntity();
        if (enemies.remove(e)) {
            killCount++;
            totalKills++;
            event.getDrops().clear();
            event.setDroppedExp(0);
            // 处理BOSS死亡清理
            if (e == currentBoss) {
                if (bossBar != null) {
                    bossBar.removeAll();
                    bossBar = null;
                }
                currentBoss = null;
            }
            // 掉落经验球
            int orbValue = 5 + (e instanceof Giant ? 30 : 0);
            ExperienceOrb orb = new ExperienceOrb(e.getLocation(), orbValue);
            expOrbs.add(orb);
            // 击杀特效
            e.getWorld().spawnParticle(Particle.FIREWORK, e.getLocation().add(0, 1, 0), 8, 0.5, 0.5, 0.5, 0.1);
            if (random.nextDouble() < 0.2) {
                e.getWorld().playSound(e.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 2f);
            }
            // 如果是BOSS，额外奖励
            if (e instanceof Giant) {
                addExperience(100);
                player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "🎰 BOSS 击败！获得大量经验！ 🎰");
                playJackpotEffect(player);
                // 已移除：竞技场材质切换，不再修改地形
            }
        }
    }
    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (!running) return;
        if (!event.getEntity().equals(player)) return;
        if (event.getDamager() instanceof LivingEntity damager && enemies.contains(damager)) {
            double damage = 3.0;
            if (damager instanceof Giant) damage = 8.0;
            damage = damage * (1 - armor / 100.0);
            player.damage(damage, player);
            event.setDamage(0);

            // 荆棘反弹伤害
            int thornCount = Math.toIntExact(passives.stream().filter(p -> p instanceof ThornArmorPassive).count());
            if (thornCount > 0) {
                double thornPercent = 0.1 + thornCount * 0.05;
                double reflectDamage = damage * thornPercent;
                damager.damage(reflectDamage, player);
                player.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, damager.getLocation().add(0,1,0), 2);
            }
        }
    }

    // 吸血符文效果处理
    @EventHandler
    public void onPlayerAttackEnemy(EntityDamageByEntityEvent event) {
        if (!running) return;
        if (event.getDamager().equals(player) && event.getEntity() instanceof LivingEntity && enemies.contains(event.getEntity())) {
            // 吸血符文效果
            int lifestealCount = Math.toIntExact(passives.stream().filter(p -> p instanceof LifestealRunePassive).count());
            if (lifestealCount > 0) {
                double chance = 0.15 + lifestealCount * 0.05;
                if (random.nextDouble() < chance) {
                    double heal = 1.0 + lifestealCount * 0.5;
                    player.setHealth(Math.min(player.getHealth() + heal, maxHealth));
                    player.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0,1,0), 1);
                }
            }
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!running) return;
        if (event.getEntity() instanceof Fireball fb && "MagicMissile".equals(fb.getCustomName())) {
            if (event.getHitEntity() instanceof LivingEntity le && enemies.contains(le)) {
                le.damage(12.0 * damageMultiplier, player);
                fb.getWorld().spawnParticle(Particle.FLASH, fb.getLocation(), 1, Color.ORANGE);
            }
            fb.remove();
        } else if (event.getEntity() instanceof Arrow arrow && "PoisonDart".equals(arrow.getCustomName())) {
            if (event.getHitEntity() instanceof LivingEntity le && enemies.contains(le)) {
                Weapon poisonWeapon = weapons.stream().filter(w -> w instanceof PoisonDartWeapon).findFirst().orElse(null);
                int dartLevel = poisonWeapon != null ? poisonWeapon.level : 1;
                double damage = (8.0 + dartLevel * 1.5) * damageMultiplier;
                le.damage(damage, player);
                int poisonDuration = 60 + dartLevel * 20; // 中毒持续时间，等级越高越长
                le.addPotionEffect(new PotionEffect(PotionEffectType.POISON, poisonDuration, 1));
                player.getWorld().spawnParticle(Particle.ITEM_SLIME, le.getLocation(), 10, 0.3, 0.3, 0.3);
            }
            arrow.remove();
        } else if (event.getEntity() instanceof Arrow arrow && "PierceArrow".equals(arrow.getCustomName())) {
            if (event.getHitEntity() instanceof LivingEntity le && enemies.contains(le)) {
                Weapon pierceWeapon = weapons.stream().filter(w -> w instanceof PierceArrowWeapon).findFirst().orElse(null);
                int pierceLevel = pierceWeapon != null ? pierceWeapon.level : 1;
                double damage = (10.0 + pierceLevel * 2) * damageMultiplier;
                le.damage(damage, player);
                player.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, le.getLocation().add(0,1,0), 3, 0.2, 0.2, 0.2);
            }
            // 穿透箭不需要移除，让它继续飞行
        }
    }
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getWhoClicked().equals(player)) return;
        if (currentMenu == null) return;
        event.setCancelled(true);
        if (currentMenu.handleClick(event.getSlot())) {
            currentMenu = null;
        }
    }
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (event.getPlayer().equals(player)) stop();
    }
    // ---------- 计分板 ----------
    private void initScoreboard() {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        scoreboard = manager.getNewScoreboard();
        objective = scoreboard.registerNewObjective("vs", "dummy", ChatColor.GOLD + "" + ChatColor.BOLD + "★ 吸血鬼幸存者 ★");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        player.setScoreboard(scoreboard);
    }
    private void updateScoreboard() {
        for (String entry : scoreboard.getEntries()) {
            scoreboard.resetScores(entry);
        }
        int line = 10;
        objective.getScore(ChatColor.YELLOW + "⚡ 等级: " + ChatColor.WHITE + playerLevel).setScore(line--);
        objective.getScore(ChatColor.RED + "❤ 生命: " + ChatColor.WHITE + (int) player.getHealth() + "/" + (int) maxHealth).setScore(line--);
        objective.getScore(ChatColor.AQUA + "☠ 杀敌: " + ChatColor.WHITE + totalKills).setScore(line--);
        objective.getScore(ChatColor.LIGHT_PURPLE + "🌊 波次: " + ChatColor.WHITE + wave).setScore(line--);
        objective.getScore(ChatColor.GREEN + "🗡 武器: " + ChatColor.WHITE + weapons.size()).setScore(line--);
        objective.getScore("").setScore(line--);
        objective.getScore(ChatColor.GRAY + "经验: " + exp + "/" + expToNextLevel).setScore(line--);
    }
    // ---------- 博彩特效 ----------
    private void playSlotMachineEffect(Player player, String title, int durationTicks) {
        if (slotTaskId != -1) Bukkit.getScheduler().cancelTask(slotTaskId);
        final int[] count = {0};
        slotTaskId = new BukkitRunnable() {
            @Override
            public void run() {
                if (!running || count[0]++ > durationTicks / 2) {
                    player.sendTitle("", "", 0, 5, 0);
                    this.cancel();
                    slotTaskId = -1;
                    return;
                }
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < 3; i++) {
                    sb.append(slotSymbols[random.nextInt(slotSymbols.length)]).append(" ");
                }
                player.sendTitle(title, ChatColor.GOLD + sb.toString(), 0, 10, 0);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.5f, 1.5f);
            }
        }.runTaskTimer(plugin, 0L, 2L).getTaskId();
    }
    private void playWaveEffect() {
        player.getWorld().playSound(arenaCenter, Sound.ENTITY_ENDER_DRAGON_GROWL, 0.7f, 1f);
        for (int i = 0; i < 36; i++) {
            double angle = i * 10 * Math.PI / 180;
            double x = Math.cos(angle) * 10;
            double z = Math.sin(angle) * 10;
            player.getWorld().spawnParticle(Particle.END_ROD, arenaCenter.clone().add(x, 0.5, z), 1, 0, 0, 0, 0);
        }
    }
    private void playJackpotEffect(Player player) {
        player.sendTitle(ChatColor.GOLD + "💰 JACKPOT! 💰", ChatColor.YELLOW + "大量经验奖励", 10, 40, 10);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 1f, 1f);
        for (int i = 0; i < 20; i++) {
            player.getWorld().spawnParticle(Particle.FIREWORK, player.getLocation().add(0, 1, 0), 0, 0.5, 0.5, 0.5, 0.2);
        }
    }
    // ---------- 内部类：经验球 ----------
    private class ExperienceOrb {
        private final Location location;
        private final int value;
        private int age = 0;
        private final Item displayItem;
        public ExperienceOrb(Location loc, int value) {
            this.location = loc.clone();
            this.value = value;
            this.displayItem = loc.getWorld().dropItem(loc, new ItemStack(Material.EXPERIENCE_BOTTLE));
            displayItem.setPickupDelay(9999);
            displayItem.setGlowing(true);
            displayItem.setVelocity(new Vector(0, 0.1, 0));
        }
        public void tick() {
            age++;
            if (age % 5 == 0) {
                location.getWorld().spawnParticle(Particle.EFFECT, location, 1, 0, 0, 0, 0, new Particle.Spell(Color.LIME, 1));
            }
            // 向玩家缓慢移动
            if (player != null && location.distance(player.getLocation()) < pickupRange + 2) {
                Vector dir = player.getLocation().toVector().subtract(location.toVector()).normalize();
                displayItem.setVelocity(dir.multiply(0.2));
            }
        }
        public boolean isValid() {
            return displayItem != null && displayItem.isValid();
        }
        public Location getLocation() {
            return displayItem.getLocation();
        }
        public int getValue() {
            return value;
        }
        public void remove() {
            displayItem.remove();
        }
    }
    // ---------- 武器系统基类 ----------
    private static abstract class Weapon {
        protected final String name;
        protected final Material icon;
        protected int level = 1;
        protected long lastUseTime = 0;
        protected final long baseCooldownMillis;
        public Weapon(String name, Material icon, long cooldownMillis) {
            this.name = name;
            this.icon = icon;
            this.baseCooldownMillis = cooldownMillis;
        }
        public boolean canUse(long cooldownFactor) {
            long effectiveCd = (long) (baseCooldownMillis / (1 + cooldownFactor * 0.01));
            return System.currentTimeMillis() - lastUseTime >= effectiveCd;
        }
        public void tryUse(Player player, Collection<LivingEntity> enemies, Location center, long cooldownFactor) {
            if (!canUse(cooldownFactor)) return;
            doAttack(player, enemies, center);
            lastUseTime = System.currentTimeMillis();
        }
        protected abstract void doAttack(Player player, Collection<LivingEntity> enemies, Location center);
        public void levelUp() {
            level++;
            onLevelUp();
        }
        protected void onLevelUp() {}
        public String getName() { return name + (level > 1 ? " +" + (level - 1) : ""); }
        public Material getIcon() { return icon; }
        public int getLevel() { return level; }
    }
    // ---------- 具体武器 ----------
    private class WhipWeapon extends Weapon {
        public WhipWeapon() { super("鞭子", Material.LEAD, 700); }
        @Override
        protected void doAttack(Player player, Collection<LivingEntity> enemies, Location center) {
            double range = 3.2 + level * 0.4;
            double damage = (5.0 + level) * damageMultiplier;
            player.getWorld().playSound(center, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.8f, 1.3f);
            player.getWorld().playSound(center, Sound.ENTITY_ARROW_SHOOT, 0.4f, 1.8f);
            for (int i = 0; i < 20; i++) {
                double angle = (i / 20.0) * Math.PI;
                double x = Math.cos(angle) * range;
                double z = Math.sin(angle) * range;
                Location loc = center.clone().add(x, 0.8, z);
                player.getWorld().spawnParticle(Particle.SWEEP_ATTACK, loc, 1);
                player.getWorld().spawnParticle(Particle.ENCHANTED_HIT, loc, 1, 0.1, 0.1, 0.1);
            }
            for (LivingEntity e : new ArrayList<>(enemies)) {
                if (e.getLocation().distance(center) <= range) {
                    e.damage(damage, player);
                    Location hitLoc = e.getLocation().add(0, 1, 0);
                    player.getWorld().spawnParticle(Particle.FLASH, hitLoc, 3, 0.3, 0.3, 0.3, Color.WHITE);
                    player.getWorld().spawnParticle(Particle.CLOUD, hitLoc, 5, 0.2, 0.2, 0.2);
                }
            }
        }
    }
    private class MagicWandWeapon extends Weapon {
        public MagicWandWeapon() { super("魔法杖", Material.BLAZE_ROD, 1000); }
        @Override
        protected void doAttack(Player player, Collection<LivingEntity> enemies, Location center) {
            if (enemies.isEmpty()) return;
            LivingEntity target = enemies.stream()
                    .min((a, b) -> (int) (a.getLocation().distanceSquared(center) - b.getLocation().distanceSquared(center)))
                    .orElse(null);
            if (target == null) return;
            Location eye = player.getEyeLocation();
            Vector dir = target.getLocation().toVector().subtract(eye.toVector()).normalize();
            Fireball fireball = player.getWorld().spawn(eye.add(dir), Fireball.class);
            fireball.setVelocity(dir.multiply(1.8));
            fireball.setIsIncendiary(false);
            fireball.setYield(0f);
            fireball.setShooter(player);
            fireball.setCustomName("MagicMissile");
            fireball.setCustomNameVisible(false);
            player.getWorld().playSound(center, Sound.ENTITY_BLAZE_SHOOT, 0.6f, 2f);
            player.getWorld().playSound(center, Sound.ENTITY_ILLUSIONER_CAST_SPELL, 0.5f, 1.5f);
            new BukkitRunnable() {
                int tick = 0;
                @Override
                public void run() {
                    if (!fireball.isValid() || tick++ > 40) {
                        this.cancel();
                        return;
                    }
                    Location fbLoc = fireball.getLocation();
                    player.getWorld().spawnParticle(Particle.SOUL, fbLoc, 2, 0, 0, 0);
                    player.getWorld().spawnParticle(Particle.FLAME, fbLoc, 1, 0.1, 0.1, 0.1);
                }
            }.runTaskTimer(plugin, 0L, 1L);
            if (level >= 3) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    Fireball fb2 = player.getWorld().spawn(eye, Fireball.class);
                    fb2.setVelocity(dir.clone().rotateAroundY(0.3).multiply(1.8));
                    fb2.setShooter(player);
                    fb2.setCustomName("MagicMissile");
                    new BukkitRunnable() {
                        int t = 0;
                        @Override
                        public void run() {
                            if (!fb2.isValid() || t++ > 40) cancel();
                            player.getWorld().spawnParticle(Particle.SOUL, fb2.getLocation(), 1);
                        }
                    }.runTaskTimer(plugin, 0L, 1L);
                }, 2L);
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    Fireball fb3 = player.getWorld().spawn(eye, Fireball.class);
                    fb3.setVelocity(dir.clone().rotateAroundY(-0.3).multiply(1.8));
                    fb3.setShooter(player);
                    fb3.setCustomName("MagicMissile");
                    new BukkitRunnable() {
                        int t = 0;
                        @Override
                        public void run() {
                            if (!fb3.isValid() || t++ > 40) cancel();
                            player.getWorld().spawnParticle(Particle.SOUL, fb3.getLocation(), 1);
                        }
                    }.runTaskTimer(plugin, 0L, 1L);
                }, 4L);
            }
        }
    }
    private class GarlicWeapon extends Weapon {
        public GarlicWeapon() { super("大蒜", Material.BEETROOT_SOUP, 200); }
        @Override
        protected void doAttack(Player player, Collection<LivingEntity> enemies, Location center) {
            double range = 2.5 + level * 0.4;
            double damage = (2.0 + level * 0.8) * damageMultiplier;
            player.getWorld().playSound(center, Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 0.6f, 1.2f);
            for (int r = 1; r <= range; r++) {
                for (int i = 0; i < 16; i++) {
                    double angle = (i / 16.0) * 2 * Math.PI;
                    double x = Math.cos(angle) * r;
                    double z = Math.sin(angle) * r;
                    Location loc = center.clone().add(x, 0.5, z);
                    Color color = Color.fromRGB(255, 100 + (int)(r*30), 100);
                    player.getWorld().spawnParticle(Particle.DUST, loc, 1, 0, 0, 0, 0, new Particle.DustOptions(color, 1.2f));
                    if (r == 1) player.getWorld().spawnParticle(Particle.HEART, loc, 1);
                }
            }
            for (LivingEntity e : new ArrayList<>(enemies)) {
                double dist = e.getLocation().distance(center);
                if (dist <= range) {
                    e.damage(damage, player);
                    Vector push = e.getLocation().toVector().subtract(center.toVector()).normalize().multiply(0.3);
                    e.setVelocity(push);
                }
            }
        }
    }
    private class AxeWeapon extends Weapon {
        public AxeWeapon() { super("飞斧", Material.IRON_AXE, 1200); }
        @Override
        protected void doAttack(Player player, Collection<LivingEntity> enemies, Location center) {
            Vector dir = player.getLocation().getDirection().setY(0).normalize();
            Location spawn = player.getEyeLocation().add(dir);
            player.getWorld().playSound(center, Sound.ENTITY_PLAYER_ATTACK_CRIT, 0.8f, 1.5f);
            ArmorStand axe = player.getWorld().spawn(spawn, ArmorStand.class);
            ItemStack axeItem = new ItemStack(Material.IRON_AXE);
            axe.getEquipment().setItemInMainHand(axeItem);
            axe.setVisible(false);
            axe.setSmall(true);
            axe.setGravity(false);
            axe.setMarker(true);
            axe.setVelocity(dir.multiply(1.2));
            new BukkitRunnable() {
                int tick = 0;
                @Override
                public void run() {
                    if (tick++ > 20 || !axe.isValid()) {
                        axe.remove();
                        cancel();
                        return;
                    }
                    Location axeLoc = axe.getLocation();
                    player.getWorld().spawnParticle(Particle.CRIT, axeLoc, 3, 0.2, 0.2, 0.2);
                    player.getWorld().spawnParticle(Particle.FLAME, axeLoc, 1, 0.1, 0.1, 0.1);
                    axe.setRightArmPose(new EulerAngle(0, tick * 0.5, 0));
                    for (LivingEntity e : new ArrayList<>(enemies)) {
                        if (e.getLocation().distance(axeLoc) < 1.5) {
                            e.damage((6 + level * 1.5) * damageMultiplier, player);
                            player.getWorld().spawnParticle(Particle.BLOCK, axeLoc, 15, 0.3, 0.3, 0.3, Material.IRON_BLOCK.createBlockData());
                            player.getWorld().spawnParticle(Particle.FLASH, axeLoc, 5, 0.2, 0.2, 0.2,Color.RED);
                            player.getWorld().playSound(axeLoc, Sound.ENTITY_PLAYER_ATTACK_STRONG, 1f, 1f);
                            axe.remove();
                            cancel();
                            break;
                        }
                    }
                }
            }.runTaskTimer(plugin, 0L, 1L);
        }
    }
    private class CrossWeapon extends Weapon {
        public CrossWeapon() { super("神圣十字架", Material.IRON_SHOVEL, 900); }
        @Override
        protected void doAttack(Player player, Collection<LivingEntity> enemies, Location center) {
            player.getWorld().playSound(center, Sound.ENTITY_ENDER_EYE_LAUNCH, 0.7f, 1.4f);
            ArmorStand cross = player.getWorld().spawn(center.clone().add(0, 1, 0), ArmorStand.class);
            ItemStack crossItem = new ItemStack(Material.IRON_SHOVEL);
            cross.getEquipment().setItemInMainHand(crossItem);
            cross.setVisible(false);
            cross.setSmall(true);
            cross.setGravity(false);
            cross.setMarker(true);
            final double maxDist = 5 + level;
            final double speed = 0.3;
            final Location startLoc = center.clone();
            new BukkitRunnable() {
                int tick = 0;
                boolean returning = false;
                @Override
                public void run() {
                    if (!cross.isValid()) {
                        cancel();
                        return;
                    }
                    Location crossLoc = cross.getLocation();
                    if (!returning) {
                        double dist = crossLoc.distance(startLoc);
                        if (dist >= maxDist) {
                            returning = true;
                        } else {
                            double angle = tick * 0.2;
                            double x = Math.cos(angle) * speed;
                            double z = Math.sin(angle) * speed;
                            cross.setVelocity(new Vector(x, 0, z));
                        }
                    } else {
                        Vector dir = center.toVector().subtract(crossLoc.toVector()).normalize();
                        cross.setVelocity(dir.multiply(speed * 1.5));
                        if (crossLoc.distance(center) < 1) {
                            cross.remove();
                            cancel();
                            return;
                        }
                    }
                    player.getWorld().spawnParticle(Particle.END_ROD, crossLoc, 2, 0.1, 0.1, 0.1);
                    player.getWorld().spawnParticle(Particle.ENCHANT, crossLoc, 1, 0.2, 0.2, 0.2);
                    for (LivingEntity e : new ArrayList<>(enemies)) {
                        if (e.getLocation().distance(crossLoc) < 1.2) {
                            e.damage((7 + level * 1.2) * damageMultiplier, player);
                            player.getWorld().spawnParticle(Particle.FLASH, e.getLocation().add(0,1,0), 2,Color.BLUE);
                        }
                    }
                    if (tick++ > 60) {
                        cross.remove();
                        cancel();
                    }
                }
            }.runTaskTimer(plugin, 0L, 1L);
        }
    }
    private class FireBladeWeapon extends Weapon {
        public FireBladeWeapon() { super("火焰之刃", Material.DIAMOND_SWORD, 500); }
        @Override
        protected void doAttack(Player player, Collection<LivingEntity> enemies, Location center) {
            double range = 3 + level * 0.5;
            double damage = (4 + level) * damageMultiplier;
            player.getWorld().playSound(center, Sound.ENTITY_BLAZE_BURN, 0.6f, 1.3f);
            for (int i = 0; i < 3; i++) {
                double startAngle = (i / 3.0) * 2 * Math.PI;
                ArmorStand blade = player.getWorld().spawn(center.clone().add(0, 1, 0), ArmorStand.class);
                ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
                blade.getEquipment().setItemInMainHand(sword);
                blade.setVisible(false);
                blade.setSmall(true);
                blade.setGravity(false);
                blade.setMarker(true);
                new BukkitRunnable() {
                    int tick = 0;
                    @Override
                    public void run() {
                        if (!blade.isValid() || tick++ > 20) {
                            blade.remove();
                            cancel();
                            return;
                        }
                        double angle = startAngle + tick * 0.3;
                        double x = Math.cos(angle) * range;
                        double z = Math.sin(angle) * range;
                        Location loc = center.clone().add(x, 1, z);
                        blade.teleport(loc);
                        player.getWorld().spawnParticle(Particle.FLAME, loc, 3, 0.1, 0.1, 0.1);
                        player.getWorld().spawnParticle(Particle.LAVA, loc, 1, 0.05, 0.05, 0.05);
                        for (LivingEntity e : new ArrayList<>(enemies)) {
                            if (e.getLocation().distance(loc) < 1) {
                                e.damage(damage, player);
                                e.setFireTicks(20);
                            }
                        }
                    }
                }.runTaskTimer(plugin, 0L, 1L);
            }
        }
    }
    private class LightningStaffWeapon extends Weapon {
        public LightningStaffWeapon() { super("闪电法杖", Material.BREEZE_ROD, 1100); }
        @Override
        protected void doAttack(Player player, Collection<LivingEntity> enemies, Location center) {
            if (enemies.isEmpty()) return;
            LivingEntity firstTarget = enemies.stream()
                    .min((a, b) -> (int) (a.getLocation().distanceSquared(center) - b.getLocation().distanceSquared(center)))
                    .orElse(null);
            if (firstTarget == null) return;
            player.getWorld().playSound(center, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.5f, 2f);
            player.getWorld().playSound(center, Sound.ENTITY_CREEPER_PRIMED, 0.4f, 1.5f);
            int jumpCount = 2 + level;
            Set<LivingEntity> hit = new HashSet<>();
            LivingEntity current = firstTarget;
            for (int jump = 0; jump < jumpCount; jump++) {
                if (current == null) break;
                hit.add(current);
                Location from = jump == 0 ? player.getEyeLocation() : hit.stream().skip(jump-1).findFirst().get().getEyeLocation();
                Location to = current.getEyeLocation();
                Vector dir = to.toVector().subtract(from.toVector()).normalize();
                double dist = from.distance(to);
                for (double d = 0; d < dist; d += 0.3) {
                    Location loc = from.clone().add(dir.clone().multiply(d));
                    loc.add(random.nextGaussian()*0.1, random.nextGaussian()*0.1, random.nextGaussian()*0.1);
                    player.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, loc, 1);
                    player.getWorld().spawnParticle(Particle.SCRAPE, loc, 1);
                }
                current.damage((10 + level * 2) * damageMultiplier, player);
                player.getWorld().spawnParticle(Particle.FLASH, current.getLocation(), 5, 0.5, 0.5, 0.5,Color.WHITE);
                LivingEntity next = null;
                double minDist = 4;
                for (LivingEntity e : enemies) {
                    if (!hit.contains(e)) {
                        double d = e.getLocation().distance(current.getLocation());
                        if (d < minDist) {
                            minDist = d;
                            next = e;
                        }
                    }
                }
                current = next;
            }
        }
    }

    // ---------- 新增武器 ----------
    private class PoisonDartWeapon extends Weapon {
        public PoisonDartWeapon() { super("淬毒飞镖", Material.SPECTRAL_ARROW, 600); }
        @Override
        protected void doAttack(Player player, Collection<LivingEntity> enemies, Location center) {
            if (enemies.isEmpty()) return;
            LivingEntity target = enemies.stream()
                    .min((a, b) -> (int) (a.getLocation().distanceSquared(center) - b.getLocation().distanceSquared(center)))
                    .orElse(null);
            if (target == null) return;
            Location eye = player.getEyeLocation();
            Vector dir = target.getLocation().toVector().subtract(eye.toVector()).normalize();
            Arrow arrow = player.getWorld().spawnArrow(eye, dir, 1.5f, 0);
            arrow.setShooter(player);
            arrow.setCustomName("PoisonDart");
            arrow.setCustomNameVisible(false);
            arrow.setPickupStatus(Arrow.PickupStatus.DISALLOWED);
            player.getWorld().playSound(center, Sound.ENTITY_SKELETON_SHOOT, 0.6f, 1.8f);
            new BukkitRunnable() {
                int tick = 0;
                @Override
                public void run() {
                    if (!arrow.isValid() || tick++ > 40) {
                        arrow.remove();
                        cancel();
                        return;
                    }
                    player.getWorld().spawnParticle(Particle.SPORE_BLOSSOM_AIR, arrow.getLocation(), 1);
                }
            }.runTaskTimer(plugin, 0L, 1L);
            // 等级3以上多发镖
            if (level >= 3) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    Arrow a2 = player.getWorld().spawnArrow(eye, dir.clone().rotateAroundY(0.2), 1.5f, 0);
                    a2.setShooter(player);
                    a2.setPickupStatus(Arrow.PickupStatus.DISALLOWED);
                    a2.setCustomName("PoisonDart");
                }, 1L);
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    Arrow a3 = player.getWorld().spawnArrow(eye, dir.clone().rotateAroundY(-0.2), 1.5f, 0);
                    a3.setShooter(player);
                    a3.setPickupStatus(Arrow.PickupStatus.DISALLOWED);
                    a3.setCustomName("PoisonDart");
                }, 2L);
            }
        }
    }

    private class PierceArrowWeapon extends Weapon {
        public PierceArrowWeapon() { super("穿透破甲箭", Material.ARROW, 800); }
        @Override
        protected void doAttack(Player player, Collection<LivingEntity> enemies, Location center) {
            Location eye = player.getEyeLocation();
            Vector dir = player.getLocation().getDirection().normalize();
            Arrow arrow = player.getWorld().spawnArrow(eye, dir, 2.0f, 0);
            arrow.setShooter(player);
            arrow.setCustomName("PierceArrow");
            arrow.setCustomNameVisible(false);
            arrow.setPickupStatus(Arrow.PickupStatus.DISALLOWED);
            arrow.setPierceLevel((byte) (2 + level)); // 穿透的数量，等级越高，穿透越多
            player.getWorld().playSound(center, Sound.ENTITY_ARROW_SHOOT, 0.7f, 1.2f);
            new BukkitRunnable() {
                int tick = 0;
                @Override
                public void run() {
                    if (!arrow.isValid() || tick++ > 60) {
                        arrow.remove();
                        cancel();
                        return;
                    }
                    player.getWorld().spawnParticle(Particle.CRIT, arrow.getLocation(), 2);
                }
            }.runTaskTimer(plugin, 0L, 1L);
        }
    }

    private class VampireDaggerWeapon extends Weapon {
        public VampireDaggerWeapon() { super("吸血匕首", Material.GOLDEN_SWORD, 400); }
        @Override
        protected void doAttack(Player player, Collection<LivingEntity> enemies, Location center) {
            double range = 2.5 + level * 0.3;
            double damage = (6.0 + level * 1.2) * damageMultiplier;
            double healAmount = 0.5 + level * 0.2; // 命中后回复的生命
            player.getWorld().playSound(center, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.7f, 1.5f);
            // 扇形攻击
            for (LivingEntity e : new ArrayList<>(enemies)) {
                double dist = e.getLocation().distance(center);
                if (dist <= range) {
                    e.damage(damage, player);
                    // 命中后回复生命
                    player.setHealth(Math.min(player.getHealth() + healAmount, maxHealth));
                    Location hitLoc = e.getLocation().add(0,1,0);
                    player.getWorld().spawnParticle(Particle.HEART, hitLoc, 1);
                    player.getWorld().spawnParticle(Particle.CRIT, hitLoc, 3);
                }
            }
            // 粒子效果
            for (int i = 0; i < 15; i++) {
                double angle = (i / 15.0) * Math.PI;
                double x = Math.cos(angle) * range;
                double z = Math.sin(angle) * range;
                Location loc = center.clone().add(x, 0.8, z);
                player.getWorld().spawnParticle(Particle.ENCHANTED_HIT, loc, 1);
            }
        }
    }

    // ---------- 被动道具 ----------
    private static abstract class PassiveItem {
        protected final String name;
        protected final Material icon;
        public PassiveItem(String name, Material icon) { this.name = name; this.icon = icon; }
        public String getName() { return name; }
        public Material getIcon() { return icon; }
    }
    private static class HealthPassive extends PassiveItem {
        public HealthPassive() { super("生命之心", Material.APPLE); }
    }
    private static class ArmorPassive extends PassiveItem {
        public ArmorPassive() { super("铁布衫", Material.IRON_CHESTPLATE); }
    }
    private static class MagnetPassive extends PassiveItem {
        public MagnetPassive() { super("磁铁", Material.HOPPER); }
    }
    private static class StrengthPassive extends PassiveItem {
        public StrengthPassive() { super("力量手套", Material.STONE_SWORD); }
    }
    private static class CooldownPassive extends PassiveItem {
        public CooldownPassive() { super("怀表", Material.CLOCK); }
    }

    // ---------- 新增被动 ----------
    private static class LifestealRunePassive extends PassiveItem {
        public LifestealRunePassive() { super("吸血符文", Material.REDSTONE); }
    }

    private static class SwiftBootsPassive extends PassiveItem {
        public SwiftBootsPassive() { super("疾风之靴", Material.DIAMOND_BOOTS); }
    }

    private static class RegenAmuletPassive extends PassiveItem {
        public RegenAmuletPassive() { super("再生护符", Material.GOLDEN_CARROT); }
    }

    private static class ThornArmorPassive extends PassiveItem {
        public ThornArmorPassive() { super("荆棘铠甲", Material.CACTUS); }
    }

    // 获取被动详细描述
    private String getPassiveDescription(PassiveItem passive) {
        if (passive instanceof HealthPassive) {
            return "永久增加4点最大生命值";
        } else if (passive instanceof ArmorPassive) {
            return "永久增加2%伤害减免";
        } else if (passive instanceof MagnetPassive) {
            return "永久增加2格经验拾取范围";
        } else if (passive instanceof StrengthPassive) {
            return "永久增加20%所有武器伤害";
        } else if (passive instanceof CooldownPassive) {
            return "永久减少10%武器冷却时间";
        } else if (passive instanceof LifestealRunePassive) {
            return "攻击时有15%概率回复1点生命，每级+5%概率+0.5回复";
        } else if (passive instanceof SwiftBootsPassive) {
            return "永久增加5%移动速度";
        } else if (passive instanceof RegenAmuletPassive) {
            return "每5秒回复1点生命，每级+0.5回复量";
        } else if (passive instanceof ThornArmorPassive) {
            return "受到攻击时反弹10%伤害，每级+5%反弹比例";
        }
        return "获得被动效果";
    }

    // 获取武器详细描述
    private String getWeaponDescription(Weapon w) {
        if (w instanceof WhipWeapon) {
            return "扇形范围攻击，击退并伤害周围敌人";
        } else if (w instanceof MagicWandWeapon) {
            return "发射追踪魔法弹，自动锁定最近的敌人";
        } else if (w instanceof GarlicWeapon) {
            return "环绕自身的光环，持续伤害并推开敌人";
        } else if (w instanceof AxeWeapon) {
            return "投掷飞斧，直线飞行造成高额伤害";
        } else if (w instanceof CrossWeapon) {
            return "十字架飞出后返回，可来回伤害敌人";
        } else if (w instanceof FireBladeWeapon) {
            return "旋转火焰刃，灼烧范围内的所有敌人";
        } else if (w instanceof LightningStaffWeapon) {
            return "召唤闪电，连锁攻击多个敌人";
        } else if (w instanceof PoisonDartWeapon) {
            return "发射淬毒飞镖，造成伤害并附加中毒效果";
        } else if (w instanceof PierceArrowWeapon) {
            return "发射穿透箭，可穿透多个敌人造成破甲伤害";
        } else if (w instanceof VampireDaggerWeapon) {
            return "快速近战挥砍，命中敌人可回复自身生命";
        }
        return "未知武器";
    }

    // ---------- 升级菜单 ----------
    private class UpgradeMenu {
        private final Player player;
        private final Inventory inv;
        private final List<Consumer<Player>> actions;
        public UpgradeMenu(Player player, List<Weapon> weapons, List<PassiveItem> passives, Consumer<Consumer<Player>> onSelectCallback) {
            this.player = player;
            this.inv = Bukkit.createInventory(null, 27, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "🎰 选择升级奖励 🎰");
            this.actions = new ArrayList<>();
            int slot = 10;
            for (Weapon w : weapons) {
                if (slot > 16) break;
                ItemStack icon = new ItemStack(w.getIcon());
                ItemMeta meta = icon.getItemMeta();
                meta.setDisplayName(ChatColor.GOLD + "升级 " + w.getName());
                meta.setLore(Arrays.asList(
                        ChatColor.GRAY + "当前等级: " + w.getLevel(),
                        ChatColor.GREEN + "点击升级至 " + (w.getLevel() + 1) + " 级",
                        ChatColor.YELLOW + "效果: " + getWeaponDescription(w)
                ));
                icon.setItemMeta(meta);
                inv.setItem(slot, icon);
                final Weapon weapon = w;
                actions.add(p -> weapon.levelUp());
                slot++;
            }

            if (weapons.size() < 6 && slot <= 16) {
                ItemStack newWep = new ItemStack(Material.NETHER_STAR);
                ItemMeta meta = newWep.getItemMeta();
                meta.setDisplayName(ChatColor.LIGHT_PURPLE + "随机武器");
                meta.setLore(Arrays.asList(
                        ChatColor.GRAY + "从所有武器中随机一种",
                        ChatColor.GREEN + "若已拥有则升级该武器",
                        ChatColor.YELLOW + "若未拥有则获得新武器"
                ));
                newWep.setItemMeta(meta);
                inv.setItem(slot, newWep);

                // 所有武器构造器列表
                Supplier<Weapon>[] allWeapons = new Supplier[] {
                        WhipWeapon::new,
                        MagicWandWeapon::new,
                        GarlicWeapon::new,
                        AxeWeapon::new,
                        CrossWeapon::new,
                        FireBladeWeapon::new,
                        LightningStaffWeapon::new,
                        PoisonDartWeapon::new,
                        PierceArrowWeapon::new,
                        VampireDaggerWeapon::new
                };

                actions.add(p -> {
                    // 随机选择一种武器
                    Supplier<Weapon> selectedSupplier = allWeapons[random.nextInt(allWeapons.length)];
                    Weapon testWeapon = selectedSupplier.get();
                    Class<? extends Weapon> selectedClass = testWeapon.getClass();

                    // 检查是否已拥有同类型武器
                    Optional<Weapon> existing = weapons.stream()
                            .filter(w -> w.getClass().equals(selectedClass))
                            .findFirst();

                    if (existing.isPresent()) {
                        // 已拥有：升级该武器
                        Weapon owned = existing.get();
                        owned.levelUp();
                        p.sendMessage(ChatColor.GREEN + "🎲 升级已有武器: " + owned.getName() + " → 等级 " + owned.getLevel());
                        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.8f);
                    } else {
                        // 未拥有：添加新武器
                        Weapon newWeapon = selectedSupplier.get(); // 重新创建新实例
                        weapons.add(newWeapon);
                        p.sendMessage(ChatColor.GREEN + "🎲 获得新武器: " + newWeapon.getName());
                        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 2f);
                    }
                });
                slot++;
            }
            List<PassiveItem> availablePassives = Arrays.asList(
                    new HealthPassive(), new ArmorPassive(), new MagnetPassive(), new StrengthPassive(), new CooldownPassive(),
                    new LifestealRunePassive(), new SwiftBootsPassive(), new RegenAmuletPassive(), new ThornArmorPassive()
            );
            Collections.shuffle(availablePassives);
            for (int i = 0; i < 3 && slot <= 16; i++) {
                PassiveItem passive = availablePassives.get(i);
                ItemStack icon = new ItemStack(passive.getIcon());
                ItemMeta meta = icon.getItemMeta();
                meta.setDisplayName(ChatColor.AQUA + passive.getName());
                meta.setLore(Collections.singletonList(ChatColor.GRAY + getPassiveDescription(passive)));
                icon.setItemMeta(meta);
                inv.setItem(slot, icon);
                final PassiveItem finalPassive = passive;
                actions.add(p -> {
                    passives.add(finalPassive);
                    p.sendMessage(ChatColor.AQUA + "🎁 获得被动: " + finalPassive.getName());
                    p.getWorld().playSound(p.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.5f);
                });
                slot++;
            }
            for (int i = 0; i < 27; i++) {
                if (inv.getItem(i) == null) {
                    inv.setItem(i, createGlassPane());
                }
            }
        }
        private ItemStack createGlassPane() {
            ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            ItemMeta meta = pane.getItemMeta();
            meta.setDisplayName(ChatColor.BLACK + "🎰");
            pane.setItemMeta(meta);
            return pane;
        }
        public void open() {
            player.openInventory(inv);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.8f, 1f);
        }
        public boolean handleClick(int slot) {
            int index = slot - 10;
            if (index >= 0 && index < actions.size()) {
                actions.get(index).accept(player);
                player.closeInventory();
                player.removePotionEffect(PotionEffectType.RESISTANCE);
                return true;
            }
            return false;
        }
    }
}