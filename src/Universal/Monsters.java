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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public enum Monsters {
    INSTANCE;
    JavaPlugin plugin;
    Kit k = Kit.INSTANCE;
    Random r = new Random();
    ArmorPool ap = ArmorPool.INSTANCE;
    HashSet<Entity>isShooting = new HashSet<>();

    public void setPlugin(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void shredder(Location loc) {
        World w = loc.getWorld();
        WitherSkeleton s = (WitherSkeleton) w.spawnEntity(loc, EntityType.WITHER_SKELETON);
        s.getEquipment().clear();
        s.getEquipment().setHelmet(new ItemStack(Material.OBSERVER));
        s.getEquipment().setChestplate(ap.mobChest(Color.BLACK));
        s.getEquipment().setLeggings(ap.mobLeg(Color.BLACK));
        double health = 100;
        s.setCustomName(ChatColor.RED + "粉碎者");
        s.getAttribute(Attribute.SCALE).setBaseValue(0.8);
        s.getAttribute(Attribute.MAX_HEALTH).setBaseValue(health);
        s.getAttribute(Attribute.KNOCKBACK_RESISTANCE).setBaseValue(1);
        s.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0.30);
        s.getAttribute(Attribute.ARMOR).setBaseValue(6);
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
        s.getEquipment().setHelmet(new ItemStack(Material.IRON_BLOCK));
        s.getAttribute(Attribute.SCALE).setBaseValue(0.5);
        s.getAttribute(Attribute.MAX_HEALTH).setBaseValue(5);
        s.setCustomName(ChatColor.GREEN + "爆爆");
        s.setExplosionRadius(2);
        s.setFuseTicks(20);
        s.setPowered(true);
    }
    public void fireBall(Location loc){
        World w = loc.getWorld();
        Zombie z = (Zombie) w.spawnEntity(loc,EntityType.ZOMBIE);
        z.getEquipment().clear();
        z.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0.25);
        z.getAttribute(Attribute.MAX_HEALTH).setBaseValue(8);
        z.getAttribute(Attribute.ARMOR).setBaseValue(4);
        z.setCustomName(ChatColor.GOLD + "火球");
        z.getEquipment().setHelmet(new ItemStack(Material.NETHERITE_BLOCK));
        z.getEquipment().setChestplate(ap.mobChest(Color.BLACK));
        z.getEquipment().setLeggings(ap.mobLeg(Color.BLACK));
        z.getEquipment().setBoots(ap.mobBoot(Color.BLACK));
        z.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 86400, 1));
        z.setBaby();
        BukkitRunnable getTarget = new BukkitRunnable() {
            @Override
            public void run() {
                if (z.isDead()) {
                    this.cancel();
                    return;
                }
                if (z.getTarget() == null) {
                    double radius = 10;
                    for (Entity e : z.getNearbyEntities(radius, radius, radius)) {
                        if (e instanceof Player p) {
                            if (k.distance(z, p) > radius) continue;
                            if (p.getGameMode().equals(GameMode.SURVIVAL)) {
                                z.setTarget(p);
                            }
                        }
                    }
                }
            }
        };
        BukkitRunnable shoot = new BukkitRunnable() {
            @Override
            public void run() {
                if (z.isDead()) {
                    this.cancel();
                    return;
                }
                if (z.getTarget() != null) {
                    LivingEntity t = z.getTarget();
                    if(!isShooting.contains(z)) {
                        if (k.distance(t, z) <= 6 &&
                                !z.hasPotionEffect(PotionEffectType.SLOWNESS)) {
                            fireBallShoot(z);
                            isShooting.add(z);
                        }
                    }
                }
            }
        };
        getTarget.runTaskTimer(plugin, 0L, 100L);
        shoot.runTaskTimer(plugin, 0L, 80L);
    }
    public void fireBallShoot(LivingEntity shooter){
        World w = shooter.getWorld();
        shooter.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 200, 10));
        shooter.getEquipment().setHelmet(new ItemStack(Material.IRON_BLOCK));
        shooter.removePotionEffect(PotionEffectType.RESISTANCE);
        w.playSound(shooter.getLocation(),Sound.ENTITY_BLAZE_HURT,1,1);
        int range = 6;
        BukkitRunnable shoot = new BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {
                if(shooter.isDead() || count > 9){
                    if(count > 9){
                        shooter.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 86400, 1));
                        shooter.getEquipment().setHelmet(new ItemStack(Material.NETHERITE_BLOCK));
                        shooter.removePotionEffect(PotionEffectType.SLOWNESS);
                    }
                    isShooting.remove(shooter);
                    this.cancel();
                    return;
                }
                Location shootLoc = shooter.getEyeLocation();
                Vector stabVec = shootLoc.getDirection().setY(0);
                w.playSound(shootLoc,Sound.ITEM_FIRECHARGE_USE,1,1);
                List<Entity> entities = shooter.getNearbyEntities(range, range, range);
                for (Entity e : entities) {
                    if (e instanceof LivingEntity l) {
                        if(e instanceof Player p){
                            if(p.getGameMode() == GameMode.SPECTATOR)continue;
                        }
                        double distance = k.distance(l,shooter);
                        double ballDistance = k.locDistance(shootLoc.clone()
                                .add(stabVec.multiply(0.9)),l.getLocation());
                        Vector lVec = l.getEyeLocation().toVector();
                        Vector pVec = shooter.getEyeLocation().toVector();
                        Vector sVec = lVec.clone().subtract(pVec);
                        double angle = k.angle(stabVec, sVec);
                        if (distance <= range && angle > 0.95 || ballDistance < 2) {
                            l.damage(1, DamageSource.builder(DamageType.IN_FIRE)
                                    .withCausingEntity(shooter).build());
                            int fire = l.getFireTicks();
                            l.setFireTicks(fire + 30);
                            w.playSound(l.getEyeLocation(),
                                    Sound.ENTITY_PLAYER_HURT_ON_FIRE, 1, 1);
                        }
                    }
                }
                for(int i = 0;i < 20;i++){
                    double x = r.nextDouble() - r.nextDouble();
                    double y = r.nextDouble() - r.nextDouble();
                    double z = r.nextDouble() - r.nextDouble();
                    Vector spread = new Vector(x, y, z).normalize();
                    Vector shoot = stabVec.clone().add(spread.multiply(0.3));
                    w.spawnParticle(Particle.FLAME,shootLoc,0,
                            shoot.getX(),shoot.getY(),shoot.getZ(),0.5);
                }
                count += 1;
            }
        };
        shoot.runTaskTimer(plugin,30L,5L);
    }
}
