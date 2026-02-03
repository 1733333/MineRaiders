package Universal;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

public enum Monsters {
    INSTANCE;
    JavaPlugin plugin;
    Kit k = Kit.INSTANCE;
    Random r = new Random();
    HashSet<Entity>isShooting = new HashSet<>();

    public void setPlugin(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void shredder(Location loc) {
        World w = loc.getWorld();
        WitherSkeleton s = (WitherSkeleton) w.spawnEntity(loc, EntityType.WITHER_SKELETON);
        ItemStack chest = new ItemStack(Material.LEATHER_CHESTPLATE);
        ItemStack leg = new ItemStack(Material.LEATHER_LEGGINGS);
        LeatherArmorMeta meta1 = (LeatherArmorMeta) chest.getItemMeta();
        LeatherArmorMeta meta2 = (LeatherArmorMeta) leg.getItemMeta();
        meta1.setColor(Color.BLACK);
        meta2.setColor(Color.BLACK);
        chest.setItemMeta(meta1);
        leg.setItemMeta(meta2);
        double health = 100;
        s.getEquipment().clear();
        s.getEquipment().setHelmet(new ItemStack(Material.OBSERVER));
        s.getEquipment().setChestplate(chest);
        s.getEquipment().setLeggings(leg);
        s.setCustomName(ChatColor.RED + "粉碎者");
        s.getAttribute(Attribute.SCALE).setBaseValue(0.8);
        s.getAttribute(Attribute.MAX_HEALTH).setBaseValue(health);
        s.getAttribute(Attribute.KNOCKBACK_RESISTANCE).setBaseValue(1);
        s.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0.30);
        s.setInvisible(true);
        s.setCustomNameVisible(false);
        s.setHealth(health);
        BukkitRunnable getTarget = new BukkitRunnable() {
            @Override
            public void run() {
                if (s.isDead()) {
                    this.cancel();
                    return;
                }
                w.playSound(s, Sound.ENTITY_PHANTOM_FLAP, 1, 1);
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
                    if(!isShooting.contains(s)) {
                        if (k.distance(t, s) <= 4 &&
                                !s.hasPotionEffect(PotionEffectType.SLOWNESS)) {
                            shredderShoot(s);
                            isShooting.add(s);
                        }
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
                for (int i = 0; i < 15; i++) {
                    double x = r.nextDouble() - r.nextDouble();
                    double y = r.nextDouble() - r.nextDouble();
                    double z = r.nextDouble() - r.nextDouble();
                    Vector spread = new Vector(x, y, z).normalize();
                    Vector shoot = (new Vector(0, -1, 0).add(spread.multiply(0.8))).multiply(2);
                    w.spawnParticle(Particle.CLOUD, s.getLocation().add(0, 1, 0)
                            , 0, shoot.getX(), shoot.getY(), shoot.getZ(), 0.1);
                }
            }
        };
        getTarget.runTaskTimer(plugin, 0L, 100L);
        shoot.runTaskTimer(plugin, 0L, 80L);
        particle.runTaskTimer(plugin, 0L, 10L);
    }

    public void shredderShoot(LivingEntity shooter) {
        World w = shooter.getWorld();
        shooter.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 200, 10));
        BukkitRunnable shoot = new BukkitRunnable() {
            int count = 0;

            @Override
            public void run() {
                if (shooter.isDead() || count > 2) {
                    if (count > 2) {
                        shooter.removePotionEffect(PotionEffectType.SLOWNESS);
                    }
                    this.cancel();
                    isShooting.remove(shooter);
                    return;
                }
                w.playSound(shooter, Sound.ENTITY_WITHER_BREAK_BLOCK, 1, 1);
                w.playSound(shooter, Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 0.5f, 1);
                w.playSound(shooter, Sound.ENTITY_GENERIC_EXPLODE, 1, 2);
                w.spawnParticle(Particle.FIREWORK, shooter.getEyeLocation(), 15, 0, 0, 0, 2);
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
                for (int i = 0; i < 31; i++) {
                    Snowball b = (Snowball) w.spawnEntity(shooter.getLocation(), EntityType.SNOWBALL);
                    b.setItem(new ItemStack(Material.FLINT));
                    b.setShooter(shooter);
                    double x = r.nextDouble() - r.nextDouble();
                    double y = r.nextDouble() - r.nextDouble();
                    double z = r.nextDouble() - r.nextDouble();
                    Vector spread = new Vector(x, y, z).normalize();
                    if(i == 0){
                        spread = shooter.getEyeLocation().getDirection();
                    }
                    b.setVelocity(spread.multiply(1.5));
                    BukkitRunnable particle = new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (b.isDead()) {
                                for (Entity e : b.getNearbyEntities(0.5, 0.5, 0.5)) {
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
                count += 1;
            }
        };
        BukkitRunnable charge = new BukkitRunnable() {
            int count = 0;

            @Override
            public void run() {
                if (shooter.isDead() || count > 3) {
                    if (count > 3) {
                        shoot.runTaskTimer(plugin, 0L, 5L);
                    }
                    this.cancel();
                    return;
                }
                if (count < 3) {
                    Color c = switch (count) {
                        case 0 -> Color.YELLOW;
                        case 1 -> Color.ORANGE;
                        case 2 -> Color.RED;
                        default -> Color.WHITE;
                    };
                    Particle.DustOptions dust = new Particle.DustOptions(c, 1);
                    w.spawnParticle(Particle.DUST, shooter.getLocation().add(0, 1, 0),
                            20, 0.8, 1, 0.8, dust);
                    if (count == 0) {
                        w.playSound(shooter, Sound.ENTITY_WITHER_AMBIENT, 1, 1);
                    }
                } else {
                    w.playSound(shooter, Sound.UI_BUTTON_CLICK, 1, 1);
                }
                count += 1;
            }
        };
        charge.runTaskTimer(plugin, 0L, 7L);
    }
    public void flea(Location loc){
        World w = loc.getWorld();
        CaveSpider s = (CaveSpider) w.spawnEntity(loc,EntityType.CAVE_SPIDER);
        s.getAttribute(Attribute.SCALE).setBaseValue(0.5);
        s.getAttribute(Attribute.MAX_HEALTH).setBaseValue(1);
        s.setCustomName(ChatColor.GREEN + "跳蚤");
    }
    public void pop(Location loc){
        World w = loc.getWorld();
        Creeper s = (Creeper) w.spawnEntity(loc,EntityType.CREEPER);
        s.getAttribute(Attribute.SCALE).setBaseValue(0.5);
        s.getAttribute(Attribute.MAX_HEALTH).setBaseValue(5);
        s.setCustomName(ChatColor.GREEN + "爆爆");
        s.setExplosionRadius(2);
        s.setFuseTicks(20);
    }
}
