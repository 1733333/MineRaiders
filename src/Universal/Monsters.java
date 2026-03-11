package Universal;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.data.BlockData;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.*;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.*;

public enum Monsters {
    INSTANCE;
    JavaPlugin plugin;
    Kit k = Kit.INSTANCE;
    Random r = new Random();
    HashSet<Entity>isShooting = new HashSet<>();
    ArmorPool ap = ArmorPool.INSTANCE;
    PlayerStats playerStats =  PlayerStats.INSTANCE;
    HashMap<LivingEntity,Player> snitchPlayerMap = new HashMap<>();
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
        double health = 150;
        s.setCustomName(ChatColor.RED + "粉碎者");
        s.getAttribute(Attribute.SCALE).setBaseValue(0.8);
        s.getAttribute(Attribute.MAX_HEALTH).setBaseValue(health);
        s.getAttribute(Attribute.KNOCKBACK_RESISTANCE).setBaseValue(1);
        s.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0.30);
        s.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS,PotionEffect.INFINITE_DURATION,10));
        s.setInvisible(true);
        s.setCustomNameVisible(false);
        s.setHealth(health);
        BukkitRunnable getTarget = new BukkitRunnable() {
            @Override
            public void run() {
                if (s.isDead()) {
                    cancel();
                    return;
                }
                w.playSound(s, Sound.ENTITY_PHANTOM_FLAP, 1, 1);
                if (s.getTarget() == null) {
                    double radius = 10;
                    for (Entity e : s.getNearbyEntities(radius, radius, radius)) {
                        if (e instanceof Player p) {
                            if (k.distance(s, p) > radius) continue;
                            if(playerStats.isDying(p))continue;
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
                    cancel();
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
                    cancel();
                    return;
                }
                for (int i = 0; i < 15; i++) {
                    double x = r.nextDouble() - r.nextDouble();
                    double y = r.nextDouble() - r.nextDouble();
                    double z = r.nextDouble() - r.nextDouble();
                    Vector spread = new Vector(x, y, z).normalize();
                    Vector shoot = (new Vector(0, -1, 0).add(spread.multiply(0.8))).multiply(2);
                    w.spawnParticle(Particle.SOUL_FIRE_FLAME, s.getLocation().add(0, 1, 0)
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
                if (shooter.isDead() || count > 3) {
                    if (count > 3) {
                        shooter.removePotionEffect(PotionEffectType.SLOWNESS);
                    }
                    cancel();
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
                                cancel();
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
                        shoot.runTaskTimer(plugin, 0L, 4L);
                    }
                    cancel();
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
        s.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS,PotionEffect.INFINITE_DURATION,10));
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
    }
    public void fireBall(Location loc) {
        World w = loc.getWorld();
        Skeleton s = (Skeleton) w.spawnEntity(loc, EntityType.SKELETON);
        s.getEquipment().clear();
        s.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0.25D);
        s.getAttribute(Attribute.MAX_HEALTH).setBaseValue(8.0D);
        s.getAttribute(Attribute.ARMOR).setBaseValue(4.0D);
        s.getAttribute(Attribute.SCALE).setBaseValue(0.5);
        s.setCustomName(ChatColor.GOLD + "火球");
                s.getEquipment().setHelmet(new ItemStack(Material.NETHERITE_BLOCK));
        s.getEquipment().setChestplate(ap.mobChest(Color.BLACK));
        s.getEquipment().setLeggings(ap.mobLeg(Color.BLACK));
        s.getEquipment().setBoots(ap.mobBoot(Color.BLACK));
        s.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 86400, 1));
        s.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS,PotionEffect.INFINITE_DURATION,10));
        BukkitRunnable getTarget = new BukkitRunnable() {
            public void run() {
                if (s.isDead()) {
                    cancel();
                    return;
                }
                if (s.getTarget() == null) {
                    double radius = 10.0D;
                    for (Entity e : s.getNearbyEntities(radius, radius, radius)) {
                        if (e instanceof Player p) {
                            if(playerStats.isDying(p))continue;
                            if (k.distance(s, p) <= radius &&
                                    p.getGameMode().equals(GameMode.SURVIVAL))
                                s.setTarget(p);
                        }
                    }
                }
            }
        };
        BukkitRunnable shoot = new BukkitRunnable() {
            public void run() {
                if (s.isDead()) {
                    cancel();
                    return;
                }
                if (s.getTarget() != null) {
                    LivingEntity t = s.getTarget();
                    if (!isShooting.contains(s) &&
                            k.distance(t, s) <= 6.0D &&
                            !s.hasPotionEffect(PotionEffectType.SLOWNESS)) {
                        fireBallShoot(s);
                        isShooting.add(s);
                    }
                }
            }
        };
        getTarget.runTaskTimer(plugin, 0L, 100L);
        shoot.runTaskTimer(plugin, 0L, 80L);
    }

    public void fireBallShoot(LivingEntity shooter) {
        final World w = shooter.getWorld();
        shooter.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 200, 10));
        shooter.getEquipment().setHelmet(new ItemStack(Material.IRON_BLOCK));
        shooter.removePotionEffect(PotionEffectType.RESISTANCE);
        w.playSound(shooter.getLocation(), Sound.ENTITY_BLAZE_HURT, 1.0F, 1.0F);
        final int range = 6;
        BukkitRunnable shoot = new BukkitRunnable() {
            int count = 0;

            public void run() {
                if (shooter.isDead() || count > 9) {
                    if (count > 9) {
                        shooter.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 86400, 1));
                        shooter.getEquipment().setHelmet(new ItemStack(Material.NETHERITE_BLOCK));
                        shooter.removePotionEffect(PotionEffectType.SLOWNESS);
                    }
                    isShooting.remove(shooter);
                    cancel();
                    return;
                }
                Location shootLoc = shooter.getEyeLocation();
                Vector stabVec = shootLoc.getDirection().setY(0);
                w.playSound(shootLoc, Sound.ITEM_FIRECHARGE_USE, 1.0F, 1.0F);
                List<Entity> entities = shooter.getNearbyEntities(range, range, range);
                for (Entity e : entities) {
                    if (e instanceof LivingEntity l) {
                        if (e instanceof Player p) {
                            if (p.getGameMode() == GameMode.SPECTATOR)
                                continue;
                        }
                        double distance = k.distance(l, shooter);
                        double ballDistance = k.locDistance(shootLoc.clone()
                                .add(stabVec.multiply(0.9D)), l.getLocation());
                        Vector lVec = l.getEyeLocation().toVector();
                        Vector pVec = shooter.getEyeLocation().toVector();
                        Vector sVec = lVec.clone().subtract(pVec);
                        double angle = k.angle(stabVec, sVec);
                        if ((distance <= range && angle > 0.95D) || ballDistance < 2.0D) {
                            l.damage(1.0D, DamageSource.builder(DamageType.IN_FIRE)
                                    .withCausingEntity(shooter).build());
                            int fire = l.getFireTicks();
                            l.setFireTicks(fire + 30);
                            w.playSound(l.getEyeLocation(), Sound.ENTITY_PLAYER_HURT_ON_FIRE, 1.0F, 1.0F);
                        }
                    }
                }
                for (int i = 0; i < 20; i++) {
                    double x = r.nextDouble() - r.nextDouble();
                    double y = r.nextDouble() - r.nextDouble();
                    double z = r.nextDouble() - r.nextDouble();
                    Vector spread = (new Vector(x, y, z)).normalize();
                    Vector shoot = stabVec.clone().add(spread.multiply(0.3D));
                    w.spawnParticle(Particle.FLAME, shootLoc, 0, shoot
                            .getX(), shoot.getY(), shoot.getZ(), 0.5D);
                }
                count++;
            }
        };
        shoot.runTaskTimer(plugin, 30L, 5L);
    }
    public void snitch(Location loc){
        World w = loc.getWorld();
        Bat s = (Bat) w.spawnEntity(loc,EntityType.BAT);
        s.getAttribute(Attribute.SCALE).setBaseValue(1.75);
        s.getAttribute(Attribute.MAX_HEALTH).setBaseValue(12);
        s.setHealth(12);
        s.setCustomName(ChatColor.GRAY + "告密者");
        s.setSilent(true);
        snitchScan(s);
    }
    public void snitchScan(LivingEntity l){
        double radius = 10;
        World w = l.getWorld();
        BukkitRunnable scan = new BukkitRunnable() {
            boolean secondScan = false;
            @Override
            public void run() {
                if(l.isDead()){
                    this.cancel();
                    return;
                }
                w.playSound(l.getEyeLocation(),Sound.ENTITY_GUARDIAN_AMBIENT_LAND,1,1);
                Player snitched = snitchPlayerMap.getOrDefault(l,null);
                if(snitched == null) {
                    for (Entity e : l.getNearbyEntities(radius, radius, radius)) {
                        if (e instanceof LivingEntity l1) {
                            if (k.distance(l, l1) > radius) continue;
                            if (l1 instanceof Player p) {
                                if(playerStats.isDying(p))continue;
                                if(p.getGameMode().equals(GameMode.SURVIVAL)){
                                    Vector pVec = p.getEyeLocation().toVector();
                                    Vector lVec = l.getEyeLocation().toVector();
                                    Vector trace = pVec.subtract(lVec);
                                    RayTraceResult result = w.rayTraceBlocks(l.getEyeLocation(),trace,k.distance(l,p));
                                    if(result == null) {
                                        snitchPlayerMap.put(l, p);
                                        p.sendTitle("", ChatColor.GRAY + "！被告密者标记了！", 10, 40, 10);
                                        k.particleLineColors(l, p, Color.AQUA, Color.YELLOW);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }else{
                    if(!secondScan) {
                        k.particleLineColors(l, snitched, Color.YELLOW, Color.RED);
                        secondScan = true;
                    }else {
                        l.remove();
                        snitchPlayerMap.remove(l);
                        w.spawnParticle(Particle.EXPLOSION,l.getLocation(),1);
                        w.playSound(l.getLocation(),Sound.ENTITY_GUARDIAN_DEATH,1,1);
                        for(int i = 0;i < 3;i++){
                            Zombie z = (Zombie) w.spawnEntity(l.getLocation(),EntityType.ZOMBIE);
                            z.setBaby();
                            z.getEquipment().clear();
                            if(k.distance(snitched,z) < radius){
                                z.setTarget(snitched);
                            }
                        }
                    }
                }
            }
        };
        scan.runTaskTimer(plugin,0L,50L);
    }
    public void leaper(Location loc){
        World w = loc.getWorld();
        double health = 500;
        MagmaCube s = (MagmaCube) w.spawnEntity(loc,EntityType.MAGMA_CUBE);
        s.setCustomName(ChatColor.GRAY + "跳跃者");
        s.setSize(4);
        s.getAttribute(Attribute.MAX_HEALTH).setBaseValue(health);
        s.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(0);
        s.getAttribute(Attribute.FOLLOW_RANGE).setBaseValue(32);
        s.getAttribute(Attribute.KNOCKBACK_RESISTANCE).setBaseValue(1);
        s.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS,PotionEffect.INFINITE_DURATION,10));
        s.setSilent(true);
        s.setHealth(health);
        BukkitRunnable getTarget = new BukkitRunnable() {
            @Override
            public void run() {
                if (s.isDead()) {
                    cancel();
                    return;
                }
                if (s.getTarget() == null) {
                    double radius = 20;
                    for (Entity e : s.getNearbyEntities(radius, radius, radius)) {
                        if (e instanceof Player p) {
                            if (k.distance(s, p) > radius) continue;
                            if(playerStats.isDying(p))continue;
                            if (p.getGameMode().equals(GameMode.SURVIVAL)) {
                                s.setTarget(p);
                            }
                        }
                    }
                }
            }
        };
        BukkitRunnable leap = new BukkitRunnable() {
            @Override
            public void run() {
                if (s.isDead()) {
                    cancel();
                    return;
                }
                if(s.getTarget() != null) {
                    if(!s.hasPotionEffect(PotionEffectType.SLOWNESS)) {
                        if(!s.isOnGround()) {
                            leaperJump(s);
                        }
                    }
                }
            }
        };
        BukkitRunnable boom = new BukkitRunnable() {
            @Override
            public void run() {
                if (s.isDead()) {
                    cancel();
                    return;
                }
                if(s.getTarget() != null) {
                    leaperBoom(s);
                }
            }
        };
        getTarget.runTaskTimer(plugin,0L,100L);
        leap.runTaskTimer(plugin,0L,20L);
        boom.runTaskTimer(plugin,100L,300L);
    }
    public void leaperJump(LivingEntity l){
        World w = l.getWorld();
        w.playSound(l.getLocation(),Sound.ENTITY_IRON_GOLEM_HURT,1,1);
        l.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING,30,0));
        l.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS,PotionEffect.INFINITE_DURATION,0));
        l.setVelocity(new Vector(0,1,0));
        BukkitRunnable later = new BukkitRunnable() {
            @Override
            public void run() {
                if(l instanceof MagmaCube m) {
                    Entity target = m.getTarget();
                    if (target != null) {
                        k.knockBack(m, target.getLocation(), -1);
                    }
                }
            }
        };
        later.runTaskLater(plugin,20L);
        BukkitRunnable jump = new BukkitRunnable() {
            @Override
            public void run() {
                if(l.isDead()){
                    this.cancel();
                    return;
                }
                for (int i = 0; i < 5; i++) {
                    double x = r.nextDouble() - r.nextDouble();
                    double y = r.nextDouble() - r.nextDouble();
                    double z = r.nextDouble() - r.nextDouble();
                    Vector spread = new Vector(x, y, z).normalize();
                    Vector shoot = (new Vector(0, -1, 0).add(spread.multiply(0.8))).multiply(2);
                    w.spawnParticle(Particle.CLOUD, l.getLocation().add(0, 1, 0)
                            , 0, shoot.getX(), shoot.getY(), shoot.getZ(), 0.1);
                }
                if(l.isOnGround()){
                    k.explode(l,l,10,0,5,0);
                    Location pLoc = l.getEyeLocation();
                    double padX = pLoc.getX();
                    double padY = pLoc.getY();
                    double padZ = pLoc.getZ();
                    double i = Math.PI;
                    for (int j = 0; j <= 100; j++) {
                        double x = padX + ((3) * Math.sin((3) * i + 0.5 * j));
                        double z = padZ + ((3) * Math.cos((3) * i + 0.5 * j));
                        Location areaP = new Location(w, x, padY, z);
                        BlockData data = Bukkit.createBlockData(Material.YELLOW_CONCRETE);
                        w.spawnParticle(Particle.DUST_PILLAR,areaP,1,data);
                    }
                    l.removePotionEffect(PotionEffectType.SLOWNESS);
                    w.spawnParticle(Particle.EXPLOSION_EMITTER,l.getLocation(),1);
                    w.playSound(l.getLocation(),Sound.ENTITY_GENERIC_EXPLODE,3,1);
                    this.cancel();
                }
            }
        };
        jump.runTaskTimer(plugin,5L,2L);
    }
    public void leaperBoom(LivingEntity l){
        World w = l.getWorld();
        w.playSound(l.getLocation(),Sound.ENTITY_WITHER_SHOOT,1,1);
        l.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS,PotionEffect.INFINITE_DURATION,10));
        BukkitRunnable boom = new BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {
                if(l.isDead() || count > 5){
                    this.cancel();
                    return;
                }
                if(count < 5){
                    if(count == 0) {
                        w.playSound(l.getLocation(), Sound.BLOCK_TRIAL_SPAWNER_ABOUT_TO_SPAWN_ITEM, 2, 1);
                        w.playSound(l.getLocation(), Sound.BLOCK_TRIAL_SPAWNER_ABOUT_TO_SPAWN_ITEM, 2, 1);
                        w.playSound(l.getLocation(), Sound.BLOCK_TRIAL_SPAWNER_ABOUT_TO_SPAWN_ITEM, 2, 1);
                    }
                    Particle.DustOptions dust = new Particle.DustOptions(Color.YELLOW,1);
                    w.spawnParticle(Particle.DUST,l.getLocation(),count * 10,
                            (count+1)/2.0,(count+1)/2.0,(count+1)/2.0,dust);
                    for(Entity e : l.getNearbyEntities(10,10,10)) {
                        if (e instanceof LivingEntity l1) {
                            if(e instanceof Monster)continue;
                            if (e instanceof Player p) {
                                if(playerStats.isDying(p))continue;
                                if (p.getGameMode() != GameMode.SURVIVAL) continue;
                            }
                            Location shooterLoc = l.getEyeLocation();
                            Location targetLoc = l1.getEyeLocation();
                            Vector sV = shooterLoc.toVector();
                            Vector tV = targetLoc.toVector();
                            RayTraceResult result = w.rayTraceBlocks(shooterLoc, tV.subtract(sV), k.distance(l, l1));
                            if (result != null) continue;
                            k.knockBack(l1, l.getLocation(), -0.25);
                        }
                    }
                }else {
                    Location pLoc = l.getEyeLocation();
                    k.explode(l,l,22,1,10,0);
                    BukkitRunnable sweep = new BukkitRunnable() {
                        int count = 0;
                        @Override
                        public void run() {
                            if(count > 5){
                                this.cancel();
                                return;
                            }
                            double padX = pLoc.getX();
                            double padY = pLoc.getY();
                            double padZ = pLoc.getZ();
                            double i = Math.PI;
                            for (int j = 0; j <= 100; j++) {
                                double x = padX + ((1 + count * 2) * Math.sin((1 + count * 2) * i + 0.5 * j));
                                double z = padZ + ((1 + count * 2) * Math.cos((1 + count * 2) * i + 0.5 * j));
                                Location areaP = new Location(w, x, padY, z);
                                BlockData data = Bukkit.createBlockData(Material.YELLOW_CONCRETE);
                                w.spawnParticle(Particle.DUST_PILLAR,areaP,1,data);
                                if(count == 2) {
                                    w.spawnParticle(Particle.FIREFLY, areaP, 1);
                                }
                            }
                            count += 1;
                        }
                    };
                    w.spawnParticle(Particle.CAMPFIRE_COSY_SMOKE,l.getLocation(),200,0,0,0,0.1);
                    sweep.runTaskTimer(plugin,0L,2L);
                    w.playSound(l.getLocation(),Sound.ENTITY_GENERIC_EXPLODE,3,1);
                    w.spawnParticle(Particle.EXPLOSION_EMITTER,l.getLocation(),1);
                    w.spawnParticle(Particle.FLASH,l.getLocation(),1,Color.YELLOW);
                    l.removePotionEffect(PotionEffectType.SLOWNESS);
                }
                count += 1;
            }
        };
        boom.runTaskTimer(plugin,0L,10L);
    }
    public void bastion(Location loc) {
        World w = loc.getWorld();
        Ravager b = (Ravager) w.spawnEntity(loc, EntityType.RAVAGER);
        Pillager p = (Pillager) w.spawnEntity(loc, EntityType.PILLAGER);
        b.addPassenger(p);
        double max = 700;
        double scale = 1.35;
        b.getAttribute(Attribute.SCALE).setBaseValue(scale);
        b.getAttribute(Attribute.MAX_HEALTH).setBaseValue(max);
        b.getAttribute(Attribute.ARMOR).setBaseValue(16);
        b.getAttribute(Attribute.KNOCKBACK_RESISTANCE).setBaseValue(1);
        b.setHealth(max);
        b.setCustomName(ChatColor.GRAY + "堡垒底盘");
        b.setSilent(true);
        b.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, PotionEffect.INFINITE_DURATION, 2));
        b.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, PotionEffect.INFINITE_DURATION, 10));
        p.getAttribute(Attribute.SCALE).setBaseValue(scale);
        p.getAttribute(Attribute.MAX_HEALTH).setBaseValue(max);
        p.getAttribute(Attribute.KNOCKBACK_RESISTANCE).setBaseValue(1);
        p.setHealth(max);
        p.setCustomName(ChatColor.GRAY + "堡垒炮塔");
        p.setSilent(true);
        BukkitRunnable getTarget = new BukkitRunnable() {
            @Override
            public void run() {
                if (p.isDead()) {
                    cancel();
                    return;
                }
                if (p.getTarget() == null) {
                    double radius = 64;
                    for (Entity e : p.getNearbyEntities(radius, radius, radius)) {
                        if (e instanceof Player p1) {
                            if (k.distance(p, p1) > radius) continue;
                            if (playerStats.isDying(p1)) continue;
                            if (p1.getGameMode().equals(GameMode.SURVIVAL)) {
                                p.setTarget(p1);
                                b.setTarget(p1);
                            }
                        }
                    }
                }
            }
        };
        BukkitRunnable shooting = new BukkitRunnable() {
            @Override
            public void run() {
                if (p.isDead()) {
                    cancel();
                    return;
                }
                Color c = Color.AQUA;
                if (p.getTarget() != null) {
                    c = Color.RED;
                    if (!p.hasPotionEffect(PotionEffectType.LUCK)) {
                        BukkitRunnable shoot = new BukkitRunnable() {
                            int count = 0;

                            @Override
                            public void run() {
                                if (p.isDead() || count > 50) {
                                    if (count > 50) {
                                        p.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, 80, 0));
                                    }
                                    this.cancel();
                                }
                                w.playSound(p.getLocation(), Sound.ENTITY_CREAKING_ACTIVATE, 1, 1.5f);
                                Location shootLoc = p.getEyeLocation();
                                Vector shootVec = shootLoc.getDirection();
                                if (p.getTarget() != null) {
                                    LivingEntity target = p.getTarget();
                                    Vector lVec = target.getEyeLocation().toVector();
                                    Vector sVec = shootLoc.toVector();
                                    shootVec = (lVec.subtract(sVec)).normalize();
                                }
                                Arrow a = w.spawnArrow(shootLoc, shootVec, 2.5f, 5);
                                a.setTicksLived(1200);
                                a.setDamage(0.75);
                                a.setCritical(true);
                                a.setShooter(p);
                                count += 1;
                            }
                        };
                        shoot.runTaskTimer(plugin, 0L, 1L);
                    }
                }
                Location subLoc = p.getEyeLocation();
                Vector subVec = p.getEyeLocation().getDirection();
                Particle.DustOptions dust = new Particle.DustOptions(c, 1);
                for (int i = 0; i < 40; i++) {
                    w.spawnParticle(Particle.DUST, subLoc, 1, dust);
                    subLoc.add(subVec.clone().normalize().multiply(0.5));
                }
            }
        };
        getTarget.runTaskTimer(plugin, 0L, 100L);
        shooting.runTaskTimer(plugin, 0L, 50L);
    }
    public void dukeMinion(Location loc){
        World w = loc.getWorld();
        Skeleton s = (Skeleton) w.spawnEntity(loc, EntityType.SKELETON);
        Vex v = (Vex) w.spawnEntity(loc, EntityType.VEX);
        s.setCustomName(ChatColor.YELLOW + "机魂");
        v.setCustomName(ChatColor.YELLOW + "机魂推进器");
        s.setSilent(true);
        s.setInvisible(true);
        v.addPassenger(s);
        v.setBound(loc);
        v.setInvisible(true);
        v.setSilent(true);
        v.getEquipment().clear();
        v.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 86400, 10));
        EntityEquipment e = s.getEquipment();
        e.clear();
        e.setHelmet(new ItemStack(Material.DISPENSER));
        e.setChestplate(ap.mobChest(Color.GRAY));
        e.setItemInMainHand(new ItemStack(Material.BOW));
        BukkitRunnable getTarget = new BukkitRunnable() {
            @Override
            public void run() {
                if (s.isDead()) {
                    cancel();
                    return;
                }
                if (s.getTarget() == null) {
                    double radius = 20;
                    for (Entity e : s.getNearbyEntities(radius, radius, radius)) {
                        if (e instanceof Player p1) {
                            if (k.distance(s, p1) > radius) continue;
                            if (playerStats.isDying(p1)) continue;
                            if (p1.getGameMode().equals(GameMode.SURVIVAL)) {
                                s.setTarget(p1);
                                v.setTarget(p1);
                            }
                        }
                    }
                }
            }
        };
        BukkitRunnable dash = new BukkitRunnable() {
            @Override
            public void run() {
                if(s.isDead()){
                    this.cancel();
                }
                if(s.getTarget()!= null){
                    LivingEntity t = s.getTarget();
                    k.knockBack(v,t.getLocation(),-0.1);
                }
            }
        };
        dash.runTaskTimer(plugin,0L,20L);
        getTarget.runTaskTimer(plugin, 0L, 100L);
    }
    public void duke(Location loc) {
        World w = loc.getWorld();
        WitherSkeleton s = (WitherSkeleton) w.spawnEntity(loc, EntityType.WITHER_SKELETON);
        Wither p = (Wither) w.spawnEntity(loc, EntityType.WITHER);
        p.setCustomName(ChatColor.RED + "公爵引擎");
        p.addPassenger(s);
        p.setInvisible(true);
        p.setSilent(true);
        s.getEquipment().clear();
        s.getEquipment().setHelmet(new ItemStack(Material.DISPENSER));
        s.getEquipment().setChestplate(new ItemStack(Material.NETHERITE_CHESTPLATE));
        double maxHealth = 1000;
        s.setCustomName(ChatColor.RED + "公爵");
        s.getAttribute(Attribute.SCALE).setBaseValue(3);
        p.getAttribute(Attribute.SCALE).setBaseValue(1);
        p.getAttribute(Attribute.FLYING_SPEED).setBaseValue(0.1);
        s.getAttribute(Attribute.MAX_HEALTH).setBaseValue(maxHealth);
        p.getAttribute(Attribute.MAX_HEALTH).setBaseValue(maxHealth);
        s.getAttribute(Attribute.KNOCKBACK_RESISTANCE).setBaseValue(1);
        s.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS,PotionEffect.INFINITE_DURATION,10));
        s.setInvisible(true);
        s.setCustomNameVisible(false);
        s.setHealth(maxHealth);
        p.setHealth(maxHealth);
        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 86400, 10));
        BossBar bar = Bukkit.createBossBar(s.getCustomName(), BarColor.RED, BarStyle.SOLID, BarFlag.DARKEN_SKY);
        new BukkitRunnable(){
            @Override
            public void run() {
                p.getBossBar().removeAll();
            }
        }.runTaskLater(plugin,1L);
        for(Player player : Bukkit.getOnlinePlayers()){
            bar.addPlayer(player);
        }
        BukkitRunnable bossBar = new BukkitRunnable() {
            @Override
            public void run() {
                if(s.isDead()){
                    this.cancel();
                    bar.removeAll();
                }
                double health = s.getHealth();
                double progress = health / maxHealth;
                bar.setProgress(Math.min(1,Math.max(0,progress)));
            }
        };
        BukkitRunnable getTarget = new BukkitRunnable() {
            @Override
            public void run() {
                if (s.isDead()) {
                    cancel();
                    return;
                }
                w.playSound(s, Sound.ENTITY_PHANTOM_FLAP, 1, 1);
                if (s.getTarget() == null) {
                    double radius = 64;
                    for (Entity e : s.getNearbyEntities(radius, radius, radius)) {
                        if (e instanceof Player player) {
                            if (k.distance(s, player) > radius) continue;
                            if(playerStats.isDying(player))continue;
                            if (player.getGameMode().equals(GameMode.SURVIVAL)) {
                                s.setTarget(player);
                                p.setTarget(player);
                            }
                        }
                    }
                }
            }
        };
        BukkitRunnable particle = new BukkitRunnable() {
            @Override
            public void run() {
                if (s.isDead()) {
                    cancel();
                    return;
                }
                for (int i = 0; i < 30; i++) {
                    double x = r.nextDouble() - r.nextDouble();
                    double y = r.nextDouble() - r.nextDouble();
                    double z = r.nextDouble() - r.nextDouble();
                    Vector spread = new Vector(x, y, z).normalize();
                    Vector shoot = (new Vector(0, -1, 0).add(spread.multiply(0.8))).multiply(2);
                    w.spawnParticle(Particle.CLOUD, s.getLocation().add(0, 3, 0)
                            , 0, shoot.getX(), shoot.getY(), shoot.getZ(), 0.2);
                }
            }
        };
        BukkitRunnable attack = new BukkitRunnable() {
            int count = 0;
            int skill = 0;
            @Override
            public void run() {
                if(s.isDead()){
                    this.cancel();
                    return;
                }
                if(count > 0 && count % 4 == 0){
                    switch (skill){
                        case 0 ->dukeBomb(s);
                        case 1 ->dukeMissile(s);
                        case 2 -> {
                            dukeMinion(s.getLocation());
                            dukeMinion(s.getLocation());
                            dukeMinion(s.getLocation());
                            w.playSound(s.getLocation(),Sound.ENTITY_EVOKER_PREPARE_SUMMON,1,1);
                        }
                    }
                    skill++;
                    if(skill > 2){
                        skill = 0;
                    }
                }else {
                    dukeShoot(s);
                }
                count ++;
            }
        };
        bossBar.runTaskTimer(plugin,0L,2L);
        getTarget.runTaskTimer(plugin, 0L, 100L);
        particle.runTaskTimer(plugin, 0L, 10L);
        attack.runTaskTimer(plugin,20L,80L);
    }
    public void dukeShoot(LivingEntity l){
        World w = l.getWorld();
        if(l instanceof Mob m){
            if(m.getTarget() == null)return;
        }
        BukkitRunnable shoot = new BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {
                if(count > 2){
                    this.cancel();
                    return;
                }
                Location shootLoc = l.getEyeLocation();
                Vector shootVec = shootLoc.getDirection();
                if(l instanceof Mob m){
                    if(m.getTarget() != null) {
                        LivingEntity target = m.getTarget();
                        Vector lVec = target.getEyeLocation().toVector();
                        Vector sVec = shootLoc.toVector();
                        shootVec = (lVec.subtract(sVec)).normalize();
                    }
                }
                w.playSound(l.getLocation(),Sound.ENTITY_GENERIC_EXPLODE,2,2);
                for(int i = 0;i < 20;i++){
                    Arrow a = w.spawnArrow(shootLoc,shootVec,2,25);
                    a.setDamage(2);
                    a.setShooter(l);
                    a.setCritical(true);
                    a.setTicksLived(1200);
                    a.setPierceLevel(10);
                    a.setColor(Color.ORANGE);
                }
                count++;
            }
        };
        shoot.runTaskTimer(plugin,0L,10L);
    }
    public void dukeBomb(LivingEntity shooter){
        World w = shooter.getWorld();
        w.playSound(shooter.getLocation(),Sound.ENTITY_WITHER_AMBIENT,2,1);
        BukkitRunnable shoot = new BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {
                if (shooter.isDead() || count > 2) {
                    cancel();
                    return;
                }
                w.playSound(shooter, Sound.ENTITY_WITHER_BREAK_BLOCK, 2, 1);
                w.playSound(shooter, Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 0.5f, 1);
                w.playSound(shooter, Sound.ENTITY_GENERIC_EXPLODE, 2, 2);
                w.playSound(shooter, Sound.UI_BUTTON_CLICK, 2, 1);
                w.spawnParticle(Particle.FIREWORK, shooter.getEyeLocation(), 15, 0, 0, 0, 2);
                for (int i = 0; i < 30; i++) {
                    Snowball b = (Snowball) w.spawnEntity(shooter.getEyeLocation().add(0,-3,0), EntityType.SNOWBALL);
                    b.setItem(new ItemStack(Material.WITHER_SKELETON_SKULL));
                    b.setVisualFire(true);
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
                                k.explode(shooter,b,10,0,3,0);
                                w.spawnParticle(Particle.LAVA, b.getLocation(), 1);
                                w.spawnParticle(Particle.EXPLOSION_EMITTER,b.getLocation(),1);
                                w.playSound(b.getLocation(),Sound.ENTITY_GENERIC_EXPLODE,2,1);
                                cancel();
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
        shoot.runTaskTimer(plugin, 40L, 10L);
    }
    public void dukeMissile(LivingEntity shooter){
        World w = shooter.getWorld();
        Location lLoc = shooter.getLocation();
        w.playSound(shooter.getLocation(), Sound.BLOCK_TRIAL_SPAWNER_ABOUT_TO_SPAWN_ITEM, 2, 1);
        w.playSound(shooter.getLocation(), Sound.BLOCK_TRIAL_SPAWNER_ABOUT_TO_SPAWN_ITEM, 2, 1);
        w.playSound(shooter.getLocation(), Sound.BLOCK_TRIAL_SPAWNER_ABOUT_TO_SPAWN_ITEM, 2, 1);
        if(shooter instanceof Mob m){
            if(m.getTarget() != null){
                Location targetLoc = m.getTarget().getLocation();
                targetLoc.setY(lLoc.getY());
                k.knockBack(shooter,targetLoc,-1);
            }
        }
        final int range = 64;
        BukkitRunnable shoot = new BukkitRunnable() {
            public void run() {
                if (shooter.isDead()) {
                    cancel();
                    return;
                }
                List<Player>nearbyPlayer = new ArrayList<>();
                for(Entity e: shooter.getNearbyEntities(range,range,range)){
                    if(e instanceof Player p){
                        nearbyPlayer.add(p);
                    }
                }
                for(int i = 0;i < 20;i++){
                    double x = r.nextDouble() - r.nextDouble();
                    double y = r.nextDouble() - r.nextDouble();
                    double z = r.nextDouble() - r.nextDouble();
                    Vector spread = new Vector(x, y, z).normalize();
                    ShulkerBullet sb = (ShulkerBullet) w.spawnEntity(shooter.getEyeLocation(),EntityType.SHULKER_BULLET);
                    sb.setVelocity(spread);
                    if(!nearbyPlayer.isEmpty()){
                        sb.setTarget(nearbyPlayer.get(r.nextInt(nearbyPlayer.size())));
                    }
                    new BukkitRunnable(){
                        @Override
                        public void run() {
                            double distance = Double.MAX_VALUE;
                            if(sb.getTarget() != null){
                                distance = k.distance(sb,sb.getTarget());
                            }
                            if(sb.isDead() || distance < 2){
                                this.cancel();
                                k.explode(shooter,sb,10,0,3,0);
                                w.spawnParticle(Particle.GUST_EMITTER_SMALL,sb.getLocation(),1);
                                w.playSound(sb.getLocation(),Sound.ENTITY_GENERIC_EXPLODE,2,1);
                            }
                        }
                    }.runTaskTimer(plugin,0L,2L);
                }
            }
        };
        shoot.runTaskLater(plugin, 40L);
    }
}
