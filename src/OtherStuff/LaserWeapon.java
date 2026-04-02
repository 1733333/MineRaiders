package OtherStuff;

import Universal.Kit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 激光武器 "宽恕者"
 * - 右键切换射击状态
 * - 按住时间越长，射速越快（最小间隔 2 ticks）
 * - 按住时间越长，扩散越小（0.5秒后完全精准）
 * - 包含粒子特效和音效
 * - 修复弹道偏左问题：改用圆锥均匀分布算法
 */
public class LaserWeapon implements Listener {

    // ======================== 可调整参数 ========================
    private static final double MAX_SPREAD_DEG = 10.0;          // 最大扩散角度（度）
    private static final int SPREAD_DURATION_TICKS = 10;        // 扩散归零所需时间（ticks，10 tick = 0.5秒）
    private static final int MAX_DELAY_TICKS = 20;              // 最慢射速间隔（ticks）
    private static final int MIN_DELAY_TICKS = 2;               // 最快射速间隔（ticks）
    private static final double MAX_RANGE = 30.0;               // 激光最大距离（格）
    private static final double DAMAGE = 4.0;                   // 每次伤害
    private static final double PARTICLE_STEP = 0.2;            // 粒子步长

    private final Kit kit = Kit.INSTANCE;
    private final JavaPlugin plugin;
    private final Map<Player, Long> startTimes = new ConcurrentHashMap<>();
    private final Map<Player, Long> lastShotTimes = new ConcurrentHashMap<>();
    private final Map<Player, BukkitRunnable> activeTasks = new ConcurrentHashMap<>();

    private static final boolean DEBUG = false;   // 调试开关

    public LaserWeapon(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    // ======================== 核心逻辑 ========================

    private boolean isExemptWeapon(ItemStack item) {
        if (item == null) return false;
        String lore = kit.getLore(item);
        return "§f宽恕者".equals(lore);
    }

    private double calculateSpread(long holdTimeTicks) {
        double progress = Math.min(1.0, (double) holdTimeTicks / SPREAD_DURATION_TICKS);
        return MAX_SPREAD_DEG * (1.0 - progress);
    }

    private int calculateNextDelay(long holdTimeTicks) {
        double progress = Math.min(1.0, (double) holdTimeTicks / SPREAD_DURATION_TICKS);
        int delay = (int) (MAX_DELAY_TICKS - (MAX_DELAY_TICKS - MIN_DELAY_TICKS) * progress);
        return Math.max(MIN_DELAY_TICKS, Math.min(MAX_DELAY_TICKS, delay));
    }

    /**
     * 生成带有随机扩散的方向向量（圆锥均匀分布，无偏左/偏右）
     * @param player 玩家
     * @param spreadDeg 最大扩散角度（度）
     * @return 偏移后的方向向量
     */
    private Vector getSpreadDirection(Player player, double spreadDeg) {
        Vector direction = player.getEyeLocation().getDirection().clone();
        if (spreadDeg <= 0) return direction;

        double rad = Math.toRadians(spreadDeg);
        // 1. 随机生成圆锥内的偏移角度（均匀分布）
        double theta = Math.random() * 2 * Math.PI;          // 圆周方向随机
        double phi = Math.acos(1 - Math.random() * (1 - Math.cos(rad))); // 均匀分布圆锥立体角

        // 2. 构建局部坐标系：以 direction 为 Z 轴，任意垂直向量为 X，叉积得 Y
        Vector up = new Vector(0, 1, 0);
        Vector axisX;
        if (Math.abs(direction.dot(up)) > 0.9999) {
            // 视线几乎垂直时，改用水平轴
            axisX = new Vector(1, 0, 0);
        } else {
            axisX = direction.clone().crossProduct(up).normalize();
        }
        Vector axisY = direction.clone().crossProduct(axisX).normalize();

        // 3. 计算偏移向量
        double dx = Math.sin(phi) * Math.cos(theta);
        double dy = Math.sin(phi) * Math.sin(theta);
        double dz = Math.cos(phi);
        Vector offset = axisX.clone().multiply(dx)
                .add(axisY.clone().multiply(dy))
                .add(direction.clone().multiply(dz));
        return offset.normalize();
    }

    private void spawnLaserParticles(Location start, Vector direction) {
        for (double i = 0; i <= MAX_RANGE; i += PARTICLE_STEP) {
            Location point = start.clone().add(direction.clone().multiply(i));
            point.getWorld().spawnParticle(Particle.END_ROD, point, 0, 0, 0, 0, 0);
            point.getWorld().spawnParticle(Particle.DUST, point, 0,
                    new Particle.DustOptions(Color.RED, 0.8f));
            if (Math.random() < 0.3) {
                point.getWorld().spawnParticle(Particle.FIREWORK, point, 0,
                        0.05, 0.05, 0.05, 0.02);
            }
        }
    }

    private void shootLaser(Player player, long holdTimeTicks) {
        double spread = calculateSpread(holdTimeTicks);
        Vector direction = getSpreadDirection(player, spread);
        Location start = player.getEyeLocation();

        if (DEBUG) {
            plugin.getLogger().info(player.getName() +
                    " holdTime=" + holdTimeTicks + "ticks spread=" + String.format("%.2f", spread) +
                    " nextDelay=" + calculateNextDelay(holdTimeTicks) + "ticks");
        }

        float pitch = (float) Math.min(1.5, 0.8 + (holdTimeTicks / 20.0) * 1.4);
        player.getWorld().playSound(start, Sound.BLOCK_BEACON_ACTIVATE, 1.0f, pitch);
        spawnLaserParticles(start, direction);

        RayTraceResult result = player.getWorld().rayTraceEntities(start, direction, MAX_RANGE,
                entity -> entity != player && entity instanceof LivingEntity);
        if (result != null && result.getHitEntity() instanceof LivingEntity hit) {
            hit.damage(DAMAGE, player);
            Location hitLoc = hit.getLocation();
            player.getWorld().playSound(hitLoc, Sound.ENTITY_GENERIC_EXPLODE, 0.7f, 1.2f);
            player.getWorld().spawnParticle(Particle.EXPLOSION, hitLoc, 8, 0.5, 0.5, 0.5, 0.1);
            player.getWorld().spawnParticle(Particle.LARGE_SMOKE, hitLoc, 4, 0.3, 0.3, 0.3, 0.05);
        }
    }

    private void startShooting(Player player) {
        stopShooting(player);
        long now = System.currentTimeMillis();
        startTimes.put(player, now);
        lastShotTimes.put(player, now);

        // 立即发射第一发（holdTime = 0）
        shootLaser(player, 0);

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || !isExemptWeapon(player.getInventory().getItemInMainHand())) {
                    stopShooting(player);
                    return;
                }
                Long start = startTimes.get(player);
                Long lastShot = lastShotTimes.get(player);
                if (start == null || lastShot == null) {
                    stopShooting(player);
                    return;
                }

                long currentTime = System.currentTimeMillis();
                long holdTimeMs = currentTime - start;
                long holdTimeTicks = holdTimeMs / 50;
                int requiredDelay = calculateNextDelay(holdTimeTicks);
                long timeSinceLastShotMs = currentTime - lastShot;
                long timeSinceLastShotTicks = timeSinceLastShotMs / 50;

                if (timeSinceLastShotTicks >= requiredDelay) {
                    shootLaser(player, holdTimeTicks);
                    lastShotTimes.put(player, currentTime);
                }
            }
        };
        task.runTaskTimer(plugin, 0, 1);
        activeTasks.put(player, task);
    }

    private void stopShooting(Player player) {
        BukkitRunnable task = activeTasks.remove(player);
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
        startTimes.remove(player);
        lastShotTimes.remove(player);
        if (player.isOnline()) {
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 0.8f, 0.5f);
        }
    }

    // ======================== 事件监听 ========================

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (!event.getAction().name().startsWith("RIGHT_CLICK")) return;
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!isExemptWeapon(item)) return;

        if (activeTasks.containsKey(player)) {
            stopShooting(player);
        } else {
            startShooting(player);
        }
    }

    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        if (!activeTasks.containsKey(player)) return;
        ItemStack newItem = player.getInventory().getItem(event.getNewSlot());
        if (!isExemptWeapon(newItem)) {
            stopShooting(player);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        stopShooting(event.getPlayer());
    }
}