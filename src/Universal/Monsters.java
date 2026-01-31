package Universal;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jspecify.annotations.Nullable;

import java.util.Random;

public enum Monsters {
    INSTANCE;
    JavaPlugin plugin;
    Kit k = Kit.INSTANCE;
    Random r = new Random();
    public void setPlugin(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    public void shredder(Location loc) {
        World w = loc.getWorld();
        WitherSkeleton s = (WitherSkeleton) w.spawnEntity(loc, EntityType.WITHER_SKELETON);
        double health = 100;
        s.getEquipment().clear();
        s.getEquipment().setHelmet(new ItemStack(Material.OBSERVER));
        s.setCustomName(ChatColor.RED + "粉碎者");
        s.setCustomNameVisible(false);
        s.getAttribute(Attribute.SCALE).setBaseValue(0.8);
        s.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(0);
        s.getAttribute(Attribute.MAX_HEALTH).setBaseValue(health);
        s.getAttribute(Attribute.ARMOR).setBaseValue(6);
        s.getAttribute(Attribute.KNOCKBACK_RESISTANCE).setBaseValue(1);
        s.setHealth(health);
        BukkitRunnable getTarget = new BukkitRunnable() {
            @Override
            public void run() {
                if (s.isDead()) {
                    this.cancel();
                    return;
                }
                w.playSound(s,Sound.ENTITY_PHANTOM_FLAP,1,1);
                if (s.getTarget() == null) {
                    double radius = 10;
                    for (Entity e : s.getNearbyEntities(radius, radius, radius)) {
                        if (e instanceof Player p) {
                            if (k.distance(s, p) > radius) continue;
                            if (p.getGameMode().equals(GameMode.SURVIVAL)) {
                                s.setTarget(p);
                            }
                        }
                    }
                }
            }
        };
        BukkitRunnable shoot = new BukkitRunnable() {
            @Override
            public void run() {
                if (s.isDead()) {
                    this.cancel();
                    return;
                }
                if (s.getTarget() != null) {
                    LivingEntity t = s.getTarget();
                    if (k.distance(t, s) <= 4) {
                        shredderShoot(s);
                    }
                }
            }
        };
        BukkitRunnable particle = new BukkitRunnable() {
            @Override
            public void run() {
                if (s.isDead()) {
                    this.cancel();
                    return;
                }
                w.spawnParticle(Particle.CLOUD,s.getLocation(),10,0,0,0,0.1);
            }
        };
        getTarget.runTaskTimer(plugin, 0L, 100L);
        shoot.runTaskTimer(plugin, 0L, 70L);
        particle.runTaskTimer(plugin, 0L, 20L);
    }
    public void shredderShoot(LivingEntity shooter) {
        World w = shooter.getWorld();
        shooter.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 200, 10));
        BukkitRunnable shoot = new BukkitRunnable() {
            int count = 0;

            @Override
            public void run() {
                if (shooter.isDead() || count > 8) {
                    if (count > 8) {
                        shooter.removePotionEffect(PotionEffectType.SLOWNESS);
                    }
                    this.cancel();
                    return;
                }
                if (count < 5) {
                    Color c = switch (count) {
                        case 0 -> Color.YELLOW;
                        case 2 -> Color.ORANGE;
                        case 4 -> Color.RED;
                        default -> Color.WHITE;
                    };
                    Particle.DustOptions dust = new Particle.DustOptions(c, 1);
                    w.spawnParticle(Particle.DUST, shooter.getLocation().add(0, 1, 0),
                            20, 0.8, 1, 0.8, dust);
                    if (count == 0) {
                        w.playSound(shooter, Sound.ENTITY_WITHER_AMBIENT, 1, 1);
                    }
                } else if (count == 5) {
                    w.playSound(shooter, Sound.UI_BUTTON_CLICK, 1, 1);
                } else {
                    w.playSound(shooter, Sound.ENTITY_WITHER_BREAK_BLOCK, 1, 1);
                    w.playSound(shooter, Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 0.5f, 1);
                    w.playSound(shooter, Sound.ENTITY_GENERIC_EXPLODE, 1, 2);
                    double radius = 10;
                    for (Entity e : shooter.getNearbyEntities(radius, radius, radius)) {
                        if (e instanceof LivingEntity l) {
                            double distance = k.distance(shooter, l);
                            if (distance > radius) continue;
                            if (e == shooter) continue;
                            if (e instanceof Player p) {
                                if (!p.getGameMode().equals(GameMode.SURVIVAL)) continue;
                            }
                            Location shooterLoc = shooter.getEyeLocation();
                            Location targetLoc = l.getEyeLocation();
                            Vector sV = shooterLoc.toVector();
                            Vector tV = targetLoc.toVector();
                            RayTraceResult result = w.rayTraceBlocks(shooterLoc, tV.subtract(sV), distance);
                            if (result != null) continue;
                            l.damage(10, DamageSource.builder(DamageType.ARROW)
                                    .withDirectEntity(shooter).build());
                        }
                    }
                    for (int i = 0; i < 30; i++) {
                        Snowball b = (Snowball) w.spawnEntity(shooter.getLocation(), EntityType.SNOWBALL);
                        b.setItem(new ItemStack(Material.FLINT));
                        b.setShooter(shooter);
                        double x = r.nextDouble() - r.nextDouble();
                        double y = r.nextDouble() - r.nextDouble();
                        double z = r.nextDouble() - r.nextDouble();
                        Vector spread = new Vector(x, y, z).normalize();
                        b.setVelocity(spread.multiply(1.5));
                        BukkitRunnable particle = new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (b.isDead()) {
                                    for (Entity e : shooter.getNearbyEntities(0.5, 0.5, 0.5)) {
                                        if (e instanceof LivingEntity l) {
                                            double distance = k.distance(shooter, l);
                                            if (distance > 0.5) continue;
                                            if (e == shooter) continue;
                                            if (e instanceof Player p) {
                                                if (!p.getGameMode().equals(GameMode.SURVIVAL)) continue;
                                            }
                                            l.damage(10, DamageSource.builder(DamageType.ARROW)
                                                    .withDirectEntity(shooter).build());
                                        }
                                    }
                                    w.spawnParticle(Particle.LAVA, b.getLocation(), 1);
                                    this.cancel();
                                    return;
                                }
                                w.spawnParticle(Particle.FLAME, b.getLocation(), 0);
                            }
                        };
                        particle.runTaskTimer(plugin, 0L, 1L);
                    }
                }
                count += 1;
            }
        };
        shoot.runTaskTimer(plugin, 0L, 5L);
    }
}
