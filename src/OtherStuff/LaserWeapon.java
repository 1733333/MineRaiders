package OtherStuff;

import Universal.Kit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound; // 新增：导入音效类
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LaserWeapon implements Listener {

    Kit k = Kit.INSTANCE;
    private final JavaPlugin plugin;
    private final Map<Player, Long> startTimes = new ConcurrentHashMap<>();      // 开始按住的时间戳
    private final Map<Player, Integer> taskIds = new HashMap<>();                // 运行中的任务ID

    public LaserWeapon(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    // 检查物品lore是否包含“宽恕者”
    private boolean isExemptWeapon(ItemStack item) {
        String lore = k.getLore(item);
        return "§f宽恕者".equals(lore);
    }

    // 发射一次激光
    private void shootLaser(Player player, long holdTime) {
        // 计算扩散角度（度），按住时间越长扩散越小，最大0.5秒后接近0
        double spread = Math.max(0, 10.0 - (holdTime / 1000.0) * 20.0);
        if (spread < 0) spread = 0;

        // 获取玩家视线方向，并添加随机扩散
        Vector direction = player.getEyeLocation().getDirection().clone();
        if (spread > 0) {
            double rad = Math.toRadians(spread);
            double yaw = Math.toRadians(player.getLocation().getYaw());
            double pitch = Math.toRadians(player.getLocation().getPitch());
            double randomYaw = (Math.random() - 0.5) * 2 * rad;
            double randomPitch = (Math.random() - 0.5) * 2 * rad;
            direction = new Vector(
                    Math.cos(pitch + randomPitch) * Math.cos(yaw + randomYaw),
                    Math.sin(pitch + randomPitch),
                    Math.cos(pitch + randomPitch) * Math.sin(yaw + randomYaw)
            ).normalize();
        }

        // 光线追踪：从玩家眼睛出发，最大距离30格，忽略玩家自身
        Location start = player.getEyeLocation();
        RayTraceResult result = player.getWorld().rayTraceEntities(start, direction, 30, entity ->
                entity != player && entity instanceof LivingEntity);

        // ========== 新增：激光发射基础音效 ==========
        // 播放激光发射的滋滋声（音量1.0，音调随按住时间变化，按住越久音调越高）
        float pitch = (float) Math.min(1.5, 0.8 + (holdTime / 1000.0) * 1.4);
        player.getWorld().playSound(start, Sound.BLOCK_BEACON_ACTIVATE, 1.0f, pitch);

        // 粒子效果：沿射线每0.2格生成粒子
        for (double i = 0; i <= 30; i += 0.2) {
            Location point = start.clone().add(direction.clone().multiply(i));

            // 原有END_ROD粒子（激光主线）
            player.getWorld().spawnParticle(Particle.END_ROD, point, 0, 0, 0, 0, 0);

            // ========== 新增：额外粒子效果 ==========
            // 1. 红色红石粒子（激光光晕）
            player.getWorld().spawnParticle(Particle.DUST, point, 0,
                    new Particle.DustOptions(org.bukkit.Color.RED, 0.8f));

            // 2. 少量火花粒子（增加视觉冲击）
            if (Math.random() < 0.3) { // 30%概率生成，避免粒子过多
                player.getWorld().spawnParticle(Particle.FIREWORK, point, 0,
                        0.05, 0.05, 0.05, 0.02);
            }
        }

        // 伤害判定：击中实体则造成4点伤害（2颗心）
        if (result != null && result.getHitEntity() instanceof LivingEntity hitEntity) {
            hitEntity.damage(4, player);

            // ========== 新增：击中实体的音效和粒子 ==========
            Location hitLoc = hitEntity.getLocation();
            // 击中音效（爆炸小音效）
            player.getWorld().playSound(hitLoc, Sound.ENTITY_GENERIC_EXPLODE, 0.7f, 1.2f);
            // 击中位置生成爆炸粒子
            player.getWorld().spawnParticle(Particle.EXPLOSION, hitLoc, 8, 0.5, 0.5, 0.5, 0.1);
            // 击中位置生成红色烟雾粒子
            player.getWorld().spawnParticle(Particle.LARGE_SMOKE, hitLoc, 4, 0.3, 0.3, 0.3, 0.05);
        }
    }

    // 开始持续发射
    private void startShooting(Player player) {
        if (taskIds.containsKey(player)) return; // 已经在发射中

        long start = System.currentTimeMillis();
        startTimes.put(player, start);

        // 循环任务
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                // 如果玩家不再手持宽恕者武器，则停止
                if (!isExemptWeapon(player.getInventory().getItemInMainHand())) {
                    stopShooting(player);
                    return;
                }

                long holdTime = System.currentTimeMillis() - startTimes.get(player);
                // 射速间隔：按住时间越长间隔越短，最大0.5秒后稳定在2 tick（0.1秒）
                int delay = Math.max(2, (int) (20 - (holdTime / 1000.0) * 18));
                shootLaser(player, holdTime);

                // 下次发射时间
                this.runTaskLater(plugin, delay);
            }
        };
        int taskId = runnable.runTaskTimer(plugin, 0, 20).getTaskId(); // 先执行一次，再定期
        taskIds.put(player, taskId);
    }

    // 停止发射并重置
    private void stopShooting(Player player) {
        if (!isExemptWeapon(player.getInventory().getItemInMainHand())) return; // 只有在手持宽恕者武器时才处理停止
        Integer taskId = taskIds.remove(player);
        if (taskId != null) {
            plugin.getServer().getScheduler().cancelTask(taskId);
        }
        startTimes.remove(player);

        // ========== 新增：停止射击时的收尾音效 ==========
        Location playerLoc = player.getLocation();
        player.getWorld().playSound(playerLoc, Sound.BLOCK_BEACON_DEACTIVATE, 0.8f, 0.5f);
    }

    // 事件：右键开始
    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction().name().startsWith("RIGHT_CLICK")) {
            ItemStack item = player.getInventory().getItemInMainHand();
            if (isExemptWeapon(item)) {
                startShooting(player);
            }
        }
    }

}