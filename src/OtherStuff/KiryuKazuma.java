package OtherStuff; // 建议改为有意义的包名，如 com.yourplugin.bosses

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
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
import org.bukkit.metadata.FixedMetadataValue;
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

        BossBar bar = Bukkit.createBossBar("桐生一马 - 流氓风格", BarColor.BLUE, BarStyle.SEGMENTED_12);
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
                if (data.currentForm == Form.POWER && data.boss instanceof Mob mob) {
                    Player target = (Player) mob.getTarget();
                    if (target != null && target.isOnline() && Math.random() < 0.2) {
                        warnThrowMinecart(mob, target); // 预警+延迟投掷
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
                    if (data != null && data.bar != null) {
                        data.bar.removeAll();
                    }
                    bossMap.remove(bossUUID);
                    dragonBoostMap.remove(bossUUID);
                    grabbedPlayerMap.remove(bossUUID);
                    cancel();
                    return;
                }

                LivingEntity boss = data.boss;
                Form form = data.currentForm;

                // 快攻手：疾风骤雨（预警+延迟）
                if (form == Form.QUICK && Math.random() < 0.25) {
                    warnQuickStorm(boss);
                }

                // 火爆浪子：范围震击（预警+延迟）
                if (form == Form.POWER && Math.random() < 0.2) {
                    warnPowerStomp(boss);
                }

                // 堂岛之龙：管理神龙爆发状态
                if (form == Form.DRAGON) {
                    if (!dragonBoostMap.containsKey(bossUUID) || !dragonBoostMap.get(bossUUID)) {
                        if (Math.random() < 0.2) {
                            activateDragonBoost(bossUUID, boss);
                        }
                    }
                } else {
                    dragonBoostMap.remove(bossUUID);
                }
            }
        }.runTaskTimer(plugin, 60L, 40L);
    }

    // === 预警方法（所有特殊攻击均先预警，再延迟执行） ===

    private void warnQuickStorm(LivingEntity boss) {
        Location loc = boss.getLocation().add(0, 1, 0);
        boss.getWorld().playSound(loc, Sound.BLOCK_NOTE_BLOCK_PLING, 2.0f, 1.2f);
        boss.getWorld().spawnParticle(Particle.WITCH, loc, 30, 3, 1, 3, 0);

        // 向附近玩家发送反制提示
        for (Entity entity : boss.getNearbyEntities(5, 2, 5)) {
            if (entity instanceof Player player) {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§e[提示] 潜行可以躲避疾风骤雨！"));
            }
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!boss.isValid() || boss.isDead()) return;
                quickStorm(boss);
            }
        }.runTaskLater(plugin, 20L); // 1秒后执行
    }

    private void warnPowerStomp(LivingEntity boss) {
        Location loc = boss.getLocation().add(0, 1, 0);
        boss.getWorld().playSound(loc, Sound.ENTITY_WARDEN_SONIC_BOOM, 1.5f, 1.5f);
        boss.getWorld().spawnParticle(Particle.REVERSE_PORTAL, loc, 40, 2, 0.5, 2, 0);

        // 向附近玩家发送反制提示
        for (Entity entity : boss.getNearbyEntities(4, 2, 4)) {
            if (entity instanceof Player player) {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§e[提示] 跳跃可以减少震击伤害并避免击飞！"));
            }
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!boss.isValid() || boss.isDead()) return;
                powerStomp(boss);
            }
        }.runTaskLater(plugin, 20L);
    }

    private void warnThrowMinecart(Mob boss, Player target) {
        Location loc = boss.getEyeLocation();
        boss.getWorld().playSound(loc, Sound.ENTITY_BLAZE_SHOOT, 1.2f, 0.8f);
        boss.getWorld().spawnParticle(Particle.SONIC_BOOM, loc.add(0, 1, 0), 1, 0, 0, 0, 0);

        // 向目标玩家发送反制提示
        target.sendMessage("§e[提示] 你可以击毁飞来的矿车！");

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!boss.isValid() || boss.isDead() || !target.isOnline()) return;
                throwMinecart(boss, target);
            }
        }.runTaskLater(plugin, 20L);
    }

    // === 技能实际效果（带反制检测） ===

    private void quickStorm(LivingEntity boss) {
        boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.2f, 1.5f);
        boss.getWorld().spawnParticle(Particle.SWEEP_ATTACK, boss.getLocation().add(0, 1, 0), 10, 2, 1, 2, 0);

        for (Entity entity : boss.getNearbyEntities(5, 2, 5)) {
            if (entity instanceof Player player) {
                // 反制：潜行免疫
                if (player.isSneaking()) {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§a你潜行躲过了疾风骤雨！"));
                    continue;
                }
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 2));
                player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 40, 0));
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§b疾风骤雨！你的动作变慢了！"));
            }
        }
    }

    private void powerStomp(LivingEntity boss) {
        boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_IRON_GOLEM_ATTACK, 1.5f, 0.8f);
        boss.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, boss.getLocation().add(0, 1, 0), 5, 1, 0, 1, 0);

        for (Entity entity : boss.getNearbyEntities(4, 2, 4)) {
            if (entity instanceof Player player) {
                // 反制：跳跃可免疫击飞，伤害减半
                boolean isJumping = !player.isOnGround(); // 简单判断：不在地面视为跳跃
                Vector knockback = player.getLocation().toVector()
                        .subtract(boss.getLocation().toVector()).normalize().multiply(isJumping ? 0 : 1.2).setY(isJumping ? 0 : 0.5);
                if (!isJumping) {
                    player.setVelocity(knockback);
                }
                double damage = isJumping ? 3.0 : 6.0; // 跳跃时伤害减半
                player.damage(damage, boss);
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(isJumping ? "§a你跳起来减轻了震击！" : "§6震击！地面在震动！"));
            }
        }
    }

    private void throwMinecart(Mob boss, Player target) {
        Location loc = boss.getEyeLocation().add(boss.getLocation().getDirection().normalize());
        boss.getWorld().playSound(loc, Sound.ENTITY_EGG_THROW, 1.0f, 1.5f);
        boss.getWorld().playSound(loc, Sound.ENTITY_ARROW_SHOOT, 0.8f, 0.9f);

        Minecart minecart = boss.getWorld().spawn(loc, Minecart.class);
        minecart.setMetadata("kiryu_minecart", new FixedMetadataValue(plugin, true)); // 标记用于事件识别
        Vector direction = target.getLocation().toVector().subtract(loc.toVector()).normalize();
        minecart.setVelocity(direction.multiply(1.5));

        // 碰撞与落地检测
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                ticks++;
                if (!minecart.isValid() || minecart.isDead()) {
                    cancel();
                    return;
                }
                // 检测玩家碰撞
                for (Entity e : minecart.getNearbyEntities(1.5, 1.5, 1.5)) {
                    if (e instanceof Player player && !player.isDead() && player.isOnline()) {
                        player.damage(8.0, boss);
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§c你被矿车撞到了！"));
                        minecart.remove();
                        cancel();
                        return;
                    }
                }
                // 检测落地（速度极小且在地面）
                if (minecart.getVelocity().lengthSquared() < 0.01 && minecart.isOnGround()) {
                    minecart.remove();
                    cancel();
                }
                // 超时保护（100 tick ≈ 5秒）
                if (ticks >= 100) {
                    minecart.remove();
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void activateDragonBoost(UUID bossUUID, LivingEntity boss) {
        dragonBoostMap.put(bossUUID, true);
        boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 1.8f, 0.6f);
        boss.getWorld().spawnParticle(Particle.FLASH, boss.getLocation().add(0, 1, 0), 1, Color.RED);

        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
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

    // === 事件处理（含预警和反制） ===

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity victim)) return;
        if (victim.getNoDamageTicks() > 10) {
            event.setCancelled(true);
            return;
        }
        UUID victimId = victim.getUniqueId();
        if (!bossMap.containsKey(victimId)) return;
        BossData data = bossMap.get(victimId);

        if (!(event.getDamager() instanceof Player player)) return;

        Form form = data.currentForm;

        // === 快攻手：瞬移闪避（增加预警） ===
        if (form == Form.QUICK && Math.random() < 0.3) {
            event.setCancelled(true);
            Location loc = victim.getLocation();
            // 预警
            victim.getWorld().playSound(loc, Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 1.5f);
            victim.getWorld().spawnParticle(Particle.PORTAL, loc.add(0, 1, 0), 30, 1, 1, 1, 0.1);
            // 提示玩家（虽无直接反制，但告知攻击可能落空）
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§e[提示] 桐生即将瞬移，攻击可能落空！"));
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!victim.isValid() || victim.isDead()) return;
                    Location newLoc = victim.getLocation();
                    double angle = Math.random() * 2 * Math.PI;
                    double dist = 3 + Math.random() * 2;
                    newLoc.add(Math.cos(angle) * dist, 0, Math.sin(angle) * dist);
                    newLoc.setY(victim.getWorld().getHighestBlockYAt(newLoc) + 1);
                    victim.teleport(newLoc);
                    victim.getWorld().playSound(newLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                    victim.getWorld().playSound(newLoc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.8f, 1.8f);
                }
            }.runTaskLater(plugin, 10L); // 0.5秒后瞬移
            return;
        }

        // === 快攻手：眩晕连击（预警+延迟+反制提示） ===
        if (form == Form.QUICK && !event.isCancelled() && Math.random() < 0.2) {
            event.setCancelled(true); // 先取消本次攻击，改为预警后的眩晕
            victim.getWorld().playSound(victim.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1.5f, 1.2f);
            victim.getWorld().spawnParticle(Particle.END_ROD, victim.getLocation().add(0, 1, 0), 20, 1, 1, 1, 0.1);
            // 提示玩家
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§e[提示] 潜行可以躲避接下来的眩晕连击！"));
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!player.isOnline() || !victim.isValid()) return;
                    // 反制：潜行免疫
                    if (player.isSneaking()) {
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§a你潜行避开了连击！"));
                        return;
                    }
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0));
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§d快攻手连击！你感到头晕目眩！"));
                    victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 1.8f);
                }
            }.runTaskLater(plugin, 20L);
            return;
        }

        // === 火爆浪子：抓取投掷（预警+延迟+反制提示） ===
        if (form == Form.POWER && !grabbedPlayerMap.containsKey(victimId) && Math.random() < 0.15) {
            event.setCancelled(true);
            victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_IRON_GOLEM_HURT, 1.0f, 0.5f);
            victim.getWorld().spawnParticle(Particle.ENCHANT, victim.getLocation().add(0, 1, 0), 50, 2, 1, 2, 0);
            // 提示玩家
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§e[提示] 举盾可以免疫抓取！"));
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!victim.isValid() || victim.isDead() || !player.isOnline()) return;
                    // 反制：举盾免疫抓取
                    if (player.isBlocking()) {
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§a你用盾牌挡住了抓取！"));
                        return;
                    }
                    // 执行抓取
                    grabbedPlayerMap.put(victimId, player);
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§6你被桐生抓住了！"));
                    victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_IRON_GOLEM_REPAIR, 1.0f, 0.7f);

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
                }
            }.runTaskLater(plugin, 20L);
            return;
        }

        // === 流氓风格：坚毅反击（保持原样，但增加反制提示？坚毅反击是反打，玩家无法反制，所以不加） ===
        if (form == Form.STREET && Math.random() < 0.25) {
            event.setDamage(event.getDamage() * 0.5);
            victim.getWorld().playSound(victim.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.8f, 1.2f);
            // 提示玩家？坚毅反击是boss的反击，玩家无法主动规避，所以不提示
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (player.isOnline() && victim.isValid() && player.getLocation().distance(victim.getLocation()) < 4) {
                        player.damage(4.0, victim);
                        Vector knockback = player.getLocation().toVector()
                                .subtract(victim.getLocation().toVector()).normalize().multiply(1.2).setY(0.3);
                        player.setVelocity(knockback);
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§3坚毅反击！你被反打了！"));
                        victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_PLAYER_ATTACK_STRONG, 1.0f, 0.9f);
                    }
                }
            }.runTask(plugin);
        }

        // === 堂岛之龙：三大奥义（预警+延迟+反制提示） ===
        if (form == Form.DRAGON) {
            double rand = Math.random();
            boolean boosted = dragonBoostMap.getOrDefault(victimId, false);
            double damageMultiplier = boosted ? 1.5 : 1.0;

            // 统一先播放预警和提示
            victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_WITHER_SHOOT, 1.2f, 1.8f);
            victim.getWorld().spawnParticle(Particle.SONIC_BOOM, victim.getLocation().add(0, 1, 0), 1, 0, 0, 0, 0);
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§e[提示] 举盾可以减轻奥义伤害和效果！"));

            if (rand < 0.33) { // 硬撼
                event.setCancelled(true);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (!victim.isValid() || victim.isDead() || !player.isOnline()) return;
                        // 反制：举盾减伤
                        double dmg = event.getDamage() * 0.3 * damageMultiplier;
                        if (player.isBlocking()) dmg *= 0.5;
                        player.damage(dmg, victim);
                        Vector knockback = player.getLocation().toVector().subtract(victim.getLocation().toVector()).normalize()
                                .multiply(boosted ? 2.0 : 1.0).setY(0.5);
                        player.setVelocity(knockback);
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(boosted ? "§c§l神龙·硬撼！" + (player.isBlocking() ? "但盾牌减轻了伤害！" : "") : "§c硬撼！你被弹开了！"));
                    }
                }.runTaskLater(plugin, 15L);
            } else if (rand < 0.66) { // 打虎
                // 不取消事件，boss受伤，但之后反击
                double originalDamage = event.getDamage();
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (player.isOnline() && victim.isValid()) {
                            double dmg = originalDamage * 0.5 * damageMultiplier;
                            if (player.isBlocking()) dmg *= 0.5;
                            player.damage(dmg, victim);
                            Vector knockback = player.getLocation().toVector().subtract(victim.getLocation().toVector()).normalize()
                                    .multiply(boosted ? 2.5 : 1.0).setY(0.8);
                            player.setVelocity(knockback);
                            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(boosted ? "§c§l神龙·打虎！痛彻心扉！" + (player.isBlocking() ? "但盾牌挡住了部分！" : "") : "§c打虎！好痛！"));
                        }
                    }
                }.runTaskLater(plugin, 15L);
            } else { // 化劲
                event.setCancelled(true);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (!victim.isValid() || victim.isDead() || !player.isOnline()) return;
                        Location behind = victim.getLocation().add(victim.getLocation().getDirection().multiply(-2));
                        player.teleport(behind);
                        int slownessAmp = boosted ? 4 : 2;
                        if (player.isBlocking()) slownessAmp = 1; // 举盾减轻
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, slownessAmp));
                        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 80, 0));
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(boosted ? "§c§l神龙·化劲！你被彻底摔懵了！" + (player.isBlocking() ? "但盾牌让你清醒些！" : "") : "§c化劲！你被摔晕了！"));
                    }
                }.runTaskLater(plugin, 15L);
            }
            return;
        }

        // 形态3：火爆浪子 - 伤害减免（非抓取时仍生效）
        if (form == Form.POWER && !event.isCancelled()) {
            event.setDamage(event.getDamage() * 0.5);
            victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_IRON_GOLEM_HURT, 0.6f, 0.8f);
        }
    }

    // 矿车可被玩家攻击提前移除（反制）
    @EventHandler
    public void onMinecartDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Minecart cart && cart.hasMetadata("kiryu_minecart")) {
            if (event.getDamager() instanceof Player) {
                cart.remove();
                event.setCancelled(true);
                event.getDamager().sendMessage("§a你击毁了矿车！");
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!bossMap.containsKey(event.getEntity().getUniqueId())) return;
        UUID id = event.getEntity().getUniqueId();
        BossData data = bossMap.get(id);
        LivingEntity boss = (LivingEntity) event.getEntity();

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

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        for (BossData data : bossMap.values()) {
            data.bar.removePlayer(player);
        }
    }
}