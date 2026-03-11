package Listeners;

import Universal.Kit;
import Universal.PlayerStats;
import com.sun.nio.sctp.ShutdownNotification;
import org.bukkit.*;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;

public class MonsterListener implements Listener {
    JavaPlugin plugin;
    Kit k = Kit.INSTANCE;
    PlayerStats playerStats = PlayerStats.INSTANCE;

    public void setPlugin(JavaPlugin plugin) {
        this.plugin = plugin;
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void damageEvent(EntityDamageEvent damageEvent) {
        Entity damaged = damageEvent.getEntity();
        if (damaged instanceof LivingEntity l) {
            if (l.getName().contains("伤害测试假人")) {
                double damage = damageEvent.getDamage();
                if (damage > 0) {
                    for (Entity e : l.getNearbyEntities(20, 20, 20)) {
                        if(e instanceof Player p) {
                            p.sendMessage(ChatColor.GREEN
                                    + ""
                                    + ChatColor.BOLD
                                    + "造成伤害："
                                    + ChatColor.RED
                                    + ChatColor.BOLD
                                    + String.format("%.2f", damage));
                        }
                    }
                }
            }
        }
    }
    @EventHandler
    public void mobShoot(ProjectileLaunchEvent launchEvent){
        Projectile pr = launchEvent.getEntity();
        ProjectileSource shooter = pr.getShooter();
        World w = pr.getWorld();
        if(shooter instanceof Snowman s){
            if(s.getCustomName() != null){
                launchEvent.setCancelled(true);
            }
        }
        if(shooter instanceof LivingEntity l){
            switch (l.getName()){
                case"§7堡垒炮塔":
                case"§c公爵引擎":
                    launchEvent.setCancelled(true);
            }
        }
    }
    @EventHandler(priority = EventPriority.LOWEST)
    public void mobMeleeAttack(EntityDamageByEntityEvent damageEvent) {
        DamageType damageType = damageEvent.getDamageSource().getDamageType();
        World w = damageEvent.getDamager().getWorld();
        Entity a = damageEvent.getDamager();
        Entity d = damageEvent.getEntity();
        double damage = damageEvent.getFinalDamage();
        if (a instanceof LivingEntity attacker) {
            String aName = attacker.getName();
            if (aName.equals("§c粉碎者") &&
                    !damageType.equals(DamageType.ARROW)) {
                damageEvent.setCancelled(true);
                damageEvent.setDamage(0);
            }
            if (aName.equals("§6火球") &&
                    !damageType.equals(DamageType.IN_FIRE)) {
                damageEvent.setCancelled(true);
                damageEvent.setDamage(0);
            }
            if (aName.equals("§7跳跃者") &&
                    !damageType.equals(DamageType.EXPLOSION)) {
                damageEvent.setCancelled(true);
                damageEvent.setDamage(0);
            }
            if (aName.equals("§7堡垒底盘")) {
                damageEvent.setCancelled(true);
                damageEvent.setDamage(0);
            }
            if (aName.equals("§a跳蚤")) {
                damageEvent.setCancelled(true);
                damageEvent.setDamage(0);
                Entity entity1 = damageEvent.getEntity();
                if (entity1 instanceof Player p) {
                    if (!attacker.hasPotionEffect(PotionEffectType.LUCK)) {
                        p.addPassenger(attacker);
                        p.sendTitle(ChatColor.RED + "！被跳蚤缠上了！", ChatColor.RED + "使用 潜行键 挣脱", 10, 40, 10);
                        BukkitRunnable blind = new BukkitRunnable() {
                            public void run() {
                                if (p.getPassengers().isEmpty())
                                    cancel();
                                p.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 40, 0));
                                p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0));
                            }
                        };
                        blind.runTaskTimer(plugin, 0L, 40L);
                    }
                }
            }
            if (damageEvent.getEntity() instanceof Player p) {
                if (playerStats.isDying(p)) {
                    if (attacker instanceof Mob m) {
                        m.setTarget(null);
                    }
                }
            }
        }
        if (d instanceof LivingEntity damaged) {
            String dName = damaged.getName();
            if (dName.equals("§6火球") &&
                    damaged.hasPotionEffect(PotionEffectType.RESISTANCE))
                w.playSound(damaged, Sound.ITEM_SHIELD_BLOCK, 1.0F, 1.0F);
            if (dName.equals("§7跳跃者")) {
                if (damaged instanceof Mob m) {
                    if (a instanceof LivingEntity l) {
                        m.setTarget(l);
                    }
                }
                if (damaged.getLastDamage() > 0) {
                    w.playSound(damaged.getLocation(), Sound.ENTITY_BLAZE_HURT, 1, 1);
                }
            }
            if (dName.equals("§7堡垒底盘") || dName.equals("§7堡垒炮塔")) {
                if (damaged instanceof Mob m) {
                    if (a instanceof LivingEntity l) {
                        m.setTarget(l);
                    }
                }
                if (!damaged.getPassengers().isEmpty()) {
                    for (Entity e : damaged.getPassengers()) {
                        if (e instanceof LivingEntity l) {
                            l.damage(damage);
                        }
                    }
                }
                if (damaged.getVehicle() != null) {
                    Entity v = damaged.getVehicle();
                    if (v instanceof LivingEntity l) {
                        l.damage(damage);
                    }
                }
//                boolean stab = false;
//                if (a instanceof LivingEntity attacker) {
//                    Vector stabVec = damaged.getEyeLocation().getDirection();
//                    Vector lVec = damaged.getEyeLocation().toVector();
//                    Vector pVec = attacker.getEyeLocation().toVector();
//                    Vector sVec = lVec.clone().subtract(pVec);
//                    double angle = k.angle(stabVec, sVec);
//                    if (angle > 0.5) {
//                        damage *= 3;
//                        damageEvent.setDamage(damage);
//                        stab = true;
//                    }
//                }
            }
        }
    }
    @EventHandler
    public void entityDeath(EntityDeathEvent deathEvent){
        LivingEntity dead = deathEvent.getEntity();
        World w = dead.getWorld();
        String dName = dead.getName();
        switch (dName){
            case"§7堡垒底盘":
            case"§7堡垒炮塔":
            case"§e机魂":
            case"§e机魂推进器":
            case"§c公爵":
            case"§c公爵引擎":
                if(!dead.getPassengers().isEmpty()){
                    for(Entity e : dead.getPassengers()){
                        if(e instanceof LivingEntity l){
                            l.setHealth(0);
                        }
                    }
                }
                if(dead.getVehicle() != null){
                    Entity v = dead.getVehicle();
                    if(v instanceof LivingEntity l){
                        l.setHealth(0);
                    }
                }
        }
        if (dName.equals("§6火球")) {
            BukkitRunnable fire = new BukkitRunnable() {
                public void run() {
                    MonsterListener.this.k.fire(dead, dead, 5, 3,3);
                }
            };
            fire.runTaskLater(plugin, 20L);
            w.spawnParticle(Particle.EXPLOSION, dead.getLocation(), 1);
            w.spawnParticle(Particle.FLAME, dead.getLocation(), 20, 0.0D, 0.0D, 0.0D, 0.3D);
        }
    }
    @EventHandler
    public void mobDeathSound(EntityDeathEvent deathEvent) {
        LivingEntity mob = deathEvent.getEntity();
        World w = mob.getWorld();
        String dName = mob.getName();
        switch (dName){
            case "§7堡垒底盘":
            case "§7堡垒炮塔":
            case "§7跳跃者":
            case "§6火球":
            case "§c粉碎者":
            case "§a跳蚤":
            case "§a爆爆":
            case "§e机魂":
            case"§c公爵":
                w.playSound(mob.getLocation(),Sound.ENTITY_BREEZE_DEATH,2,0.75f);
                w.playSound(mob.getLocation(),Sound.ENTITY_GENERIC_EXPLODE,2,1.5f);
        }
    }
    @EventHandler
    public void damageSoundEffect(EntityDamageEvent damageEvent) {
        Entity e = damageEvent.getEntity();
        World w = e.getWorld();
        if(damageEvent.getDamage() > 3) {
            if (e instanceof LivingEntity d) {
                String dName = d.getName();
                Sound s = switch (dName){
                    case "§7堡垒底盘" ->Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR;
                    case "§7堡垒炮塔" -> Sound.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR;
                    case "§7跳跃者" ->Sound.ENTITY_BLAZE_HURT;
                    case "§c粉碎者" ->Sound.ENTITY_WITHER_HURT;
                    case "§6火球" -> Sound.ITEM_SHIELD_BLOCK;
                    case "§e机魂" -> Sound.ENTITY_VEX_HURT;
                    case "§c公爵" ->Sound.ENTITY_WITHER_BREAK_BLOCK;
                    default -> Sound.UI_BUTTON_CLICK;
                };
                w.playSound(d.getLocation(),s,2,1);
            }
        }
    }
    @EventHandler
    public void playerRidFlea(PlayerToggleSneakEvent sneakEvent) {
        Player p = sneakEvent.getPlayer();
        if (!p.getPassengers().isEmpty()) {
            for (Entity e : p.getPassengers()) {
                if (e.getName().equals("§a跳蚤")) {
                    p.eject();
                    e.setVelocity(p.getEyeLocation().getDirection().setY(0));
                    if (e instanceof LivingEntity l) {
                        l.addPotionEffect(new PotionEffect(PotionEffectType.LUCK,
                                100, 0));
                    }
                }
            }
        }
    }

    @EventHandler
    public void mobTarget(EntityTargetEvent targetEvent) {
        Entity e = targetEvent.getEntity();
        Entity target = targetEvent.getTarget();
        if (e instanceof LivingEntity mob) {
            if (mob.getName().equals("§a爆爆")) {
                mob.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,
                        100, 0));
            }
        }
        if(target instanceof Player p){
            if(playerStats.isDying(p)){
                targetEvent.setTarget(null);
            }
        }
        if(target instanceof Shulker s){
            if(s.isInvisible()){
                targetEvent.setTarget(null);
            }
        }
    }

    @EventHandler
    public void mobExplode(EntityExplodeEvent explodeEvent) {
        explodeEvent.setCancelled(true);
        Entity e = explodeEvent.getEntity();
        World w = e.getWorld();
        if (e instanceof LivingEntity mob) {
            if (mob.getName().equals("§a爆爆")) {
                k.explode(mob, mob, 10, 1, 5,1);
                w.spawnParticle(Particle.EXPLOSION_EMITTER, mob.getLocation(), 1);
                w.playSound(mob.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1, 1);
            }
        }
    }

    @EventHandler
    public void slimeSplit(SlimeSplitEvent splitEvent){
        Entity e = splitEvent.getEntity();
        if(e.getCustomName() != null){
            splitEvent.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void mobDamageReduction(EntityDamageEvent damageEvent){
        Entity e = damageEvent.getEntity();
        DamageSource source = damageEvent.getDamageSource();
        DamageType type = source.getDamageType();
        double damage = damageEvent.getFinalDamage();
        if(e instanceof LivingEntity l){
            String name = l.getCustomName();
            if(name != null){
                switch (name){
                    case "§e机魂"->{
                        if(type == DamageType.IN_WALL){
                            damageEvent.setDamage(0);
                            damageEvent.setCancelled(true);
                        }
                    }
                    case "§c公爵"->{
                        if(type == DamageType.IN_WALL){
                            damageEvent.setDamage(0);
                            damageEvent.setCancelled(true);
                        }else{
                            damageEvent.setDamage(damage / 10);
                        }
                    }
                }
            }
        }
    }
}
