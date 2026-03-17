package OtherStuff; // 建议改为有意义的包名，如 com.yourplugin.bosses

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class KiryuKazuma implements Listener {

    private final JavaPlugin plugin;
    private final Map<UUID, BossData> bossMap = new HashMap<>();
    private final Map<UUID, Boolean> dragonBoostMap = new HashMap<>();
    private final Map<UUID, Player> grabbedPlayerMap = new HashMap<>();

    private enum Form {
        STREET,   // 流氓风格
        QUICK,    // 快攻手
        POWER,    // 火爆浪子
        DRAGON    // 堂岛之龙
    }

    private static class BossData {
        UUID uuid;
        Form currentForm;
        BossBar bar;
        LivingEntity boss;
        double maxHealth; // 实际最大生命值

        BossData(UUID uuid, Form form, BossBar bar, LivingEntity boss, double maxHealth) {
            this.uuid = uuid;
            this.currentForm = form;
            this.bar = bar;
            this.boss = boss;
            this.maxHealth = maxHealth;
        }
    }

    public KiryuKazuma(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void spawnBoss(Location loc) {
        Skeleton boss = (Skeleton) loc.getWorld().spawnEntity(loc, EntityType.SKELETON);
        boss.setCustomName("桐生一马");
        boss.setCustomNameVisible(true);
        boss.setRemoveWhenFarAway(false);
        boss.getEquipment().clear();
        boss.getEquipment().setItemInMainHand(null);
        boss.getEquipment().setItemInOffHand(null);
        boss.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(5);
        boss.getAttribute(Attribute.SCALE).setBaseValue(1.1);
        // 防止阳光燃烧
        boss.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, PotionEffect.INFINITE_DURATION, 0, false, false));

        // 设置最大生命值为1000
        AttributeInstance maxHealthAttr = boss.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealthAttr != null) {
            maxHealthAttr.setBaseValue(1000);
        }
        boss.setHealth(1000);

        BossBar bar = Bukkit.createBossBar("桐生一马 - 流氓风格", BarColor.BLUE, BarStyle.SEGMENTED_10);
        for (Player player : Bukkit.getOnlinePlayers()) {
            bar.addPlayer(player);
        }
        bar.setProgress(1.0);

        // 动态获取最大生命值
        double maxHealth = boss.getAttribute(Attribute.MAX_HEALTH).getBaseValue();
        BossData data = new BossData(boss.getUniqueId(), Form.STREET, bar, boss, maxHealth);
        bossMap.put(boss.getUniqueId(), data);

        setFormAttributes(boss, Form.STREET);

        loc.getWorld().playSound(loc, Sound.ENTITY_WITHER_SPAWN, 1.5f, 0.8f);
        loc.getWorld().playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.2f);
        loc.getWorld().strikeLightningEffect(loc);

        startMinecartSkill(boss.getUniqueId());
        startFormSkills(boss.getUniqueId());
    }

    private void setFormAttributes(LivingEntity boss, Form form) {
        AttributeInstance speed = boss.getAttribute(Attribute.MOVEMENT_SPEED);
        if (speed == null) return;
        switch (form) {
            case STREET -> speed.setBaseValue(0.23);
            case QUICK  -> speed.setBaseValue(0.28);
            case POWER  -> speed.setBaseValue(0.18);
            case DRAGON -> speed.setBaseValue(0.25);
        }
    }

    private void startMinecartSkill(UUID bossUUID) {
        new BukkitRunnable() {
            @Override
            public void run() {
                BossData data = bossMap.get(bossUUID);
                if (data == null || !data.boss.isValid() || data.boss.isDead()) {
                    cancel();
                    return;
                }
                // 确保是 Mob 类型再获取目标
                if (data.currentForm == Form.POWER && data.boss instanceof Mob mob) {
                    Player target = (Player) mob.getTarget();
                    if (target != null && target.isOnline() && Math.random() < 0.2) {
                        throwMinecart(mob, target);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 40L);
    }

    private void startFormSkills(UUID bossUUID) {
        new BukkitRunnable() {
            @Override
            public void run() {
                BossData data = bossMap.get(bossUUID);
                if (data == null || !data.boss.isValid() || data.boss.isDead()) {
                    // 如果data存在，清理BossBar
                    if (data != null && data.bar != null) {
                        data.bar.removeAll();
                    }
                    bossMap.remove(bossUUID);          // 从全局map移除
                    dragonBoostMap.remove(bossUUID);
                    grabbedPlayerMap.remove(bossUUID);
                    cancel();
                    return;
                }

                LivingEntity boss = data.boss;
                Form form = data.currentForm;

                // 快攻手：每2秒有概率触发疾风骤雨（提高概率使技能更明显）
                if (form == Form.QUICK && Math.random() < 0.25) { // 原0.15 → 0.25
                    quickStorm(boss);
                }

                // 火爆浪子：每2秒有概率触发范围震击
                if (form == Form.POWER && Math.random() < 0.2) { // 原0.1 → 0.2
                    powerStomp(boss);
                }

                // 堂岛之龙：管理神龙爆发状态
                if (form == Form.DRAGON) {
                    if (!dragonBoostMap.containsKey(bossUUID) || !dragonBoostMap.get(bossUUID)) {
                        if (Math.random() < 0.2) {
                            activateDragonBoost(bossUUID, boss);
                        }
                    }
                } else {
                    // 非龙形态强制清除强化状态
                    dragonBoostMap.remove(bossUUID);
                }
            }
        }.runTaskTimer(plugin, 60L, 40L);
    }

    private void quickStorm(LivingEntity boss) {
        boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.2f, 1.5f);
        boss.getWorld().spawnParticle(Particle.SWEEP_ATTACK, boss.getLocation().add(0, 1, 0), 10, 2, 1, 2, 0);

        for (Entity entity : boss.getNearbyEntities(5, 2, 5)) {
            if (entity instanceof Player player) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 2));
                player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 40, 0));
                player.sendMessage("§b疾风骤雨！你的动作变慢了！");
            }
        }
    }

    private void powerStomp(LivingEntity boss) {
        boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_IRON_GOLEM_ATTACK, 1.5f, 0.8f);
        // 替换过时的 EXPLOSION 粒子
        boss.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, boss.getLocation().add(0, 1, 0), 5, 1, 0, 1, 0);

        for (Entity entity : boss.getNearbyEntities(4, 2, 4)) {
            if (entity instanceof Player player) {
                Vector knockback = player.getLocation().toVector()
                        .subtract(boss.getLocation().toVector()).normalize().multiply(1.2).setY(0.5);
                player.setVelocity(knockback);
                player.damage(6.0, boss);
                player.sendMessage("§6震击！地面在震动！");
            }
        }
    }

    private void activateDragonBoost(UUID bossUUID, LivingEntity boss) {
        dragonBoostMap.put(bossUUID, true);
        boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 1.8f, 0.6f);
        boss.getWorld().spawnParticle(Particle.FLASH, boss.getLocation().add(0, 1, 0), 1,Color.RED);

        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                // 如果 boss 已死亡或不再是龙形态，提前结束
                if (!boss.isValid() || boss.isDead() || !bossMap.containsKey(bossUUID) ||
                        bossMap.get(bossUUID).currentForm != Form.DRAGON) {
                    dragonBoostMap.remove(bossUUID);
                    cancel();
                    return;
                }
                if (!dragonBoostMap.containsKey(bossUUID) || !dragonBoostMap.get(bossUUID)) {
                    cancel();
                    return;
                }
                boss.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, boss.getLocation().add(0, 1, 0), 5, 0.5, 0.5, 0.5, 0.02);
                ticks += 5;
                if (ticks >= 160) { // 8秒
                    dragonBoostMap.put(bossUUID, false);
                    boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_WITHER_SHOOT, 1.0f, 0.8f);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 5L);
    }

    private void throwMinecart(Mob boss, Player target) {
        Location loc = boss.getEyeLocation().add(boss.getLocation().getDirection().normalize());
        boss.getWorld().playSound(loc, Sound.ENTITY_EGG_THROW, 1.0f, 1.5f);
        boss.getWorld().playSound(loc, Sound.ENTITY_ARROW_SHOOT, 0.8f, 0.9f);

        Minecart minecart = boss.getWorld().spawn(loc, Minecart.class);
        Vector direction = target.getLocation().toVector().subtract(loc.toVector()).normalize();
        minecart.setVelocity(direction.multiply(1.5));

        new BukkitRunnable() {
            @Override
            public void run() {
                if (minecart.isValid()) minecart.remove();
            }
        }.runTaskLater(plugin, 100L);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity victim)) return;
        if(victim.getNoDamageTicks() > 10){
            event.setCancelled(true);
            return;
        }
        UUID victimId = victim.getUniqueId();
        if (!bossMap.containsKey(victimId)) return;
        BossData data = bossMap.get(victimId);

        if (!(event.getDamager() instanceof Player player)) return;

        Form form = data.currentForm;

        // === 快攻手：瞬移闪避（优先处理，如果闪避则取消事件并返回，后续技能不再执行） ===
        if (form == Form.QUICK && Math.random() < 0.3) {
            event.setCancelled(true);
            Location loc = victim.getLocation();
            double angle = Math.random() * 2 * Math.PI;
            double dist = 3 + Math.random() * 2;
            loc.add(Math.cos(angle) * dist, 0, Math.sin(angle) * dist);
            loc.setY(victim.getWorld().getHighestBlockYAt(loc) + 1);
            victim.teleport(loc);
            victim.getWorld().playSound(loc, Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
            victim.getWorld().playSound(loc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.8f, 1.8f);
            return; // 闪避后不再处理其他反击逻辑
        }

        // === 快攻手：眩晕连击（仅在攻击未被闪避时触发） ===
        if (form == Form.QUICK && !event.isCancelled() && Math.random() < 0.2) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1));
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0));
            player.sendMessage("§d快攻手连击！你感到头晕目眩！");
            victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 1.8f);
        }

        // === 火爆浪子：抓取投掷 ===
        if (form == Form.POWER && !grabbedPlayerMap.containsKey(victimId) && Math.random() < 0.15) {
            event.setCancelled(true);
            grabbedPlayerMap.put(victimId, player);

            victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_IRON_GOLEM_REPAIR, 1.0f, 0.7f);
            player.sendMessage("§6你被桐生抓住了！");

            new BukkitRunnable() {
                @Override
                public void run() {
                    Player grabbed = grabbedPlayerMap.remove(victimId);
                    if (grabbed != null && grabbed.isOnline() && victim.isValid()) {
                        Location throwLoc = victim.getLocation().add(victim.getLocation().getDirection().multiply(5));
                        throwLoc.setY(throwLoc.getY() + 2);
                        grabbed.teleport(throwLoc);
                        grabbed.damage(10.0, victim);
                        grabbed.setVelocity(new Vector(0, 1, 0));
                        victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.2f, 1.0f);
                        grabbed.sendMessage("§c你被狠狠扔了出去！");
                    }
                }
            }.runTaskLater(plugin, 40L);
            return;
        }

        // === 流氓风格：坚毅反击 ===
        if (form == Form.STREET && Math.random() < 0.25) {
            event.setDamage(event.getDamage() * 0.5); // 减伤50%

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (player.isOnline() && victim.isValid() && player.getLocation().distance(victim.getLocation()) < 4) {
                        player.damage(4.0, victim);
                        Vector knockback = player.getLocation().toVector()
                                .subtract(victim.getLocation().toVector()).normalize().multiply(1.2).setY(0.3);
                        player.setVelocity(knockback);
                        player.sendMessage("§3坚毅反击！你被反打了！");
                        victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_PLAYER_ATTACK_STRONG, 1.0f, 0.9f);
                    }
                }
            }.runTask(plugin);

            victim.getWorld().playSound(victim.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.8f, 1.2f);
        }

        // === 堂岛之龙：三大奥义 ===
        if (form == Form.DRAGON) {
            double rand = Math.random();
            boolean boosted = dragonBoostMap.getOrDefault(victimId, false);
            double damageMultiplier = boosted ? 1.5 : 1.0;

            if (rand < 0.33) { // 硬撼
                event.setCancelled(true);
                victim.getWorld().playSound(victim.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.0f, 1.2f);
                victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_IRON_GOLEM_HURT, 0.8f, 0.7f);

                player.damage(event.getDamage() * 0.3 * damageMultiplier, victim);
                Vector knockback = player.getLocation().toVector().subtract(victim.getLocation().toVector()).normalize()
                        .multiply(1 * (boosted ? 2.0 : 1.0)).setY(0.5);
                player.setVelocity(knockback);
                player.sendMessage(boosted ? "§c§l神龙·硬撼！你被狠狠弹开了！" : "§c硬撼！你被弹开了！");

            } else if (rand < 0.66) { // 打虎
                victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 1.2f, 0.8f);
                victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 0.6f);

                double originalDamage = event.getDamage();
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (player.isOnline() && victim.isValid()) {
                            player.damage(originalDamage * 0.5 * damageMultiplier, victim);
                            Vector knockback = player.getLocation().toVector().subtract(victim.getLocation().toVector()).normalize()
                                    .multiply(1 * (boosted ? 2.5 : 1.0)).setY(0.8);
                            player.setVelocity(knockback);
                            player.sendMessage(boosted ? "§c§l神龙·打虎！痛彻心扉！" : "§c打虎！好痛！");
                        }
                    }
                }.runTask(plugin);
                // 事件继续，Boss受伤

            } else { // 化劲
                event.setCancelled(true);
                victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.8f, 1.5f);
                victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_PLAYER_BIG_FALL, 1.0f, 0.9f);

                Location behind = victim.getLocation().add(victim.getLocation().getDirection().multiply(-2));
                player.teleport(behind);
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, boosted ? 4 : 2));
                player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 80, 0));
                player.sendMessage(boosted ? "§c§l神龙·化劲！你被彻底摔懵了！" : "§c化劲！你被摔晕了！");
            }
            return;
        }

        // 形态3：火爆浪子 - 伤害减免（非抓取时仍生效）
        if (form == Form.POWER && !event.isCancelled()) {
            event.setDamage(event.getDamage() * 0.5);
            victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_IRON_GOLEM_HURT, 0.6f, 0.8f);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!bossMap.containsKey(event.getEntity().getUniqueId())) return;
        UUID id = event.getEntity().getUniqueId();
        BossData data = bossMap.get(id);
        LivingEntity boss = (LivingEntity) event.getEntity();

        // 只有未被取消的事件才更新BossBar和形态切换
        if (!event.isCancelled()) {
            double newHealth = boss.getHealth() - event.getFinalDamage();
            double progress = Math.max(0, Math.min(1, newHealth / data.maxHealth));
            data.bar.setProgress(progress);

            Form newForm = getFormByHealth(progress);
            if (newForm != data.currentForm) {
                switchForm(boss, data, newForm);
            }
        }
    }

    private Form getFormByHealth(double progress) {
        if (progress > 0.75) return Form.STREET;
        if (progress > 0.5)  return Form.QUICK;
        if (progress > 0.25) return Form.POWER;
        return Form.DRAGON;
    }

    private void switchForm(LivingEntity boss, BossData data, Form newForm) {
        Form oldForm = data.currentForm;
        data.currentForm = newForm;
        setFormAttributes(boss, newForm);

        // 如果离开龙形态，清除强化状态
        if (oldForm == Form.DRAGON && newForm != Form.DRAGON) {
            dragonBoostMap.remove(boss.getUniqueId());
        }

        String title;
        BarColor color;
        switch (newForm) {
            case STREET -> { title = "桐生一马 - 流氓风格"; color = BarColor.BLUE; }
            case QUICK  -> { title = "桐生一马 - 快攻手";   color = BarColor.PINK; }
            case POWER  -> { title = "桐生一马 - 火爆浪子"; color = BarColor.YELLOW; }
            case DRAGON -> { title = "桐生一马 - 堂岛之龙"; color = BarColor.RED; }
            default -> { title = "桐生一马"; color = BarColor.WHITE; }
        }
        data.bar.setTitle(title);
        data.bar.setColor(color);

        boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.2f, 1.0f);
        boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.8f, 1.3f);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        UUID id = event.getEntity().getUniqueId();
        BossData data = bossMap.remove(id);
        if (data != null && data.bar != null) {
            data.bar.removeAll();
            event.getEntity().getWorld().playSound(event.getEntity().getLocation(), Sound.ENTITY_WITHER_DEATH, 1.0f, 0.7f);
        }
        dragonBoostMap.remove(id);
        grabbedPlayerMap.remove(id);
    }

    // 处理玩家退出，从BossBar中移除
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        for (BossData data : bossMap.values()) {
            data.bar.removePlayer(player);
        }
    }
}