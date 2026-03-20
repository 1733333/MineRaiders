package Universal;

import Events.PlayerShieldAmountChangeEvent;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.*;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.*;

public enum Kit {
    INSTANCE;
    JavaPlugin plugin;
    Random r = new Random();

    public void setPlugin(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void shuffleInt(Integer[] numbers) {
        for (int i = numbers.length - 1; i > 0; i--) {
            int randomNum = r.nextInt(i + 1);
            int num = numbers[randomNum];
            numbers[randomNum] = numbers[i];
            numbers[i] = num;
        }
    }

    public void shuffleItems(ItemStack[] items) {
        for (int i = items.length - 1; i > 0; i--) {
            int randomNum = r.nextInt(i + 1);
            ItemStack temp = items[randomNum];
            items[randomNum] = items[i];
            items[i] = temp;
        }
    }

    public Block rayTraceBlock(Location loc, Vector vec, int radius){
        Vector eVec = vec.normalize();
        Location bLoc = loc.clone();
        for(int i = 0;i <= radius;i ++){
            Block b = bLoc.getBlock();
            Material type = b.getType();
            if(type != Material.AIR && type != Material.LIGHT){
                return b;
            }
            bLoc.add(eVec.clone());
        }
        return null;
    }
    public String getLore(ItemStack item) {
        if(item != null) {
            if (item.getItemMeta() != null) {
                ItemMeta meta = item.getItemMeta();
                if (meta.hasLore()) {
                    String[] lore = meta.getLore().toArray(new String[0]);
                    return lore[0];
                } else return "";
            } else return "";
        }else return "";
    }

    public double distance(Entity e1,Entity e2){
        Vector l1 = e1.getLocation().toVector();
        Vector l2 = e2.getLocation().toVector();
        return Math.max(0.1, (l1.subtract(l2)).length());
    }
    public double locDistance(Location l1,Location l2){
        return (l1.subtract(l2)).length();
    }
    public double angle(Vector v1,Vector v2){
        Vector v1N = v1.clone().normalize();
        Vector v2N = v2.clone().normalize();
        return v1N.dot(v2N);
    }
    public void explode(LivingEntity source, Entity jar, double damage, double amp, int radius, double selfDamage) {
        World w = source.getWorld();
        Collection<Entity> entities = w.getNearbyEntities(jar.getLocation(), radius, radius, radius);
        for (Entity e : entities) {
            if (distance(jar, e) > radius) continue;
            if (e instanceof LivingEntity l) {
                int distance = (int) distance(jar, l);
                if (l != source) {
                    Location shooterLoc = source.getEyeLocation();
                    Location targetLoc = l.getEyeLocation();
                    Vector sV = shooterLoc.toVector();
                    Vector tV = targetLoc.toVector();
                    RayTraceResult result = w.rayTraceBlocks(shooterLoc, tV.subtract(sV), distance(jar, e));
                    if (result != null) continue;
                }
                if (distance >= 1) {
                    double reduce = distance * amp;
                    damage -= reduce;
                }
                if (l == source) {
                    if (selfDamage != 0) {
                        damage *= selfDamage;
                    }
                }
                if (source.getName().equals("§a爆爆")) {
                    l.damage(damage);
                    source.remove();
                } else {
                    l.damage(Math.abs(damage), DamageSource.builder(DamageType.EXPLOSION)
                            .withDirectEntity(source).build());
                }
            }
        }
    }
    public void gas(Player p,Entity jar,int duration,double radius){
        World w = p.getWorld();
        Location loc = jar.getLocation();
        jar.remove();
        AreaEffectCloud cloud = (AreaEffectCloud) w.spawnEntity(loc, EntityType.AREA_EFFECT_CLOUD);
        Particle.DustOptions dustOptions = new Particle.DustOptions(Color.LIME,1);
        cloud.setDuration(duration * 20);
        cloud.setParticle(Particle.DUST,dustOptions);
        cloud.setRadius((float) radius);
        cloud.setCustomName(p.getName() + "的毒气");
        BukkitRunnable gassing = new BukkitRunnable() {
            @Override
            public void run() {
                if(cloud.isDead()){
                    this.cancel();
                    return;
                }
                Collection<Entity> entities = w.getNearbyEntities(loc,radius,radius,radius);
                for(Entity e : entities){
                    if(distance(jar,e) > radius)continue;
                    if(e instanceof LivingEntity l){
                        if(l instanceof Player p1) {
                            if(p1.getGameMode() == GameMode.SPECTATOR)continue;
                            int hunger = p1.getFoodLevel();
                            p1.setFoodLevel(Math.max(0,hunger - 5));
                            w.playSound(p1.getLocation(),Sound.ENTITY_PLAYER_BURP,1,1);
                        }
                    }
                }
                Particle.DustOptions dust = new Particle.DustOptions(Color.LIME,3);
                w.spawnParticle(Particle.DUST,loc,150,radius/2,radius/2,radius/2,0.1,dust);
            }
        };
        gassing.runTaskTimer(plugin,0L,20L);
    }
    public void fire(LivingEntity source,Entity jar,int duration,int radius,double damage) {
        World w = source.getWorld();
        Location loc = jar.getLocation();
        AreaEffectCloud cloud = (AreaEffectCloud) w.spawnEntity(loc, EntityType.AREA_EFFECT_CLOUD);
        cloud.setDuration(duration * 20);
        cloud.setParticle(Particle.FLAME);
        cloud.setRadius((float) radius);
        cloud.setCustomName(source.getName() + "的火");
        BukkitRunnable fireDamage = new BukkitRunnable() {
            @Override
            public void run() {
                if (cloud.isDead()) {
                    this.cancel();
                    return;
                }
                w.spawnParticle(Particle.FLAME, loc, 200, radius / 2.0, radius / 2.0, radius / 2.0, 0);
                Collection<Entity> entities = w.getNearbyEntities(loc, radius, radius, radius);
                for (Entity e : entities) {
                    if (distance(jar, e) > radius) continue;
                    if (e instanceof LivingEntity l) {
                        if (e instanceof Player p1) {
                            if (p1.getGameMode() == GameMode.SPECTATOR) continue;
                        }
                        int fire = l.getFireTicks();
                        l.setFireTicks(fire + 140);
                        l.damage(damage, DamageSource.builder(DamageType.ON_FIRE)
                                .withDirectEntity(cloud).build());
                        w.playSound(l.getLocation(), Sound.ENTITY_PLAYER_HURT_ON_FIRE, 1, 1);
                    }
                }
            }
        };
        fireDamage.runTaskTimer(plugin, 0L, 20L);
    }
    public boolean isArmored(Player p){
        EntityEquipment e = p.getEquipment();
        if(e.getHelmet() != null)return true;
        if(e.getChestplate() != null)return true;
        if(e.getLeggings() != null)return true;
        if(e.getBoots() != null)return true;
        return false;
    }
    public void particleLine(Entity e1,Entity e2,Color c) {
        World w = e1.getWorld();
        Location eLoc = e1.getLocation();
        Vector eVec = eLoc.toVector();
        Location pLoc = e2.getLocation();
        Vector pVec = pLoc.toVector();
        Vector subVec = pVec.subtract(eVec);
        Location subLoc = eLoc.clone().add(0, 0.2, 0);
        Particle.DustOptions dust = new Particle.DustOptions(c, 3);
        for (int i = 0; i < subVec.length(); i++) {
            w.spawnParticle(Particle.DUST, subLoc, 1, dust);
            subLoc.add(subVec.clone().normalize());
        }
    }
    public void particleLineColors(Entity e1,Entity e2,Color c1,Color c2) {
        World w = e1.getWorld();
        Location eLoc = e1.getLocation();
        Vector eVec = eLoc.toVector();
        Location pLoc = e2.getLocation();
        Vector pVec = pLoc.toVector();
        Vector subVec = pVec.subtract(eVec);
        Location subLoc = eLoc.clone().add(0, 0.2, 0);
        Particle.DustTransition dust = new Particle.DustTransition(c1,c2,3);
        for (int i = 0; i < subVec.length(); i++) {
            w.spawnParticle(Particle.DUST_COLOR_TRANSITION, subLoc, 1, dust);
            subLoc.add(subVec.clone().normalize());
        }
    }
    public void knockBack(Entity who,Location from,double power){
        try {
            if(who instanceof Player p){
                if(p.getGameMode() == GameMode.SPECTATOR)return;
            }
            Location entityLoc = who.getLocation();
            Location subLoc = entityLoc.subtract(from);
            Vector knockBackVec = subLoc.toVector().normalize().clone();
            who.setVelocity(knockBackVec.multiply(power));
        }catch (Exception ignored){}
    }
    public void smoke(Entity jar,int duration,int radius){
        World w = jar.getWorld();
        Location loc = jar.getLocation();
        AreaEffectCloud cloud = (AreaEffectCloud) w.spawnEntity(loc,EntityType.AREA_EFFECT_CLOUD);
        cloud.setDuration(duration * 20);
        cloud.setParticle(Particle.EXPLOSION_EMITTER);
        cloud.setRadius(1);
        cloud.setCustomName("烟雾");
        BukkitRunnable smoking = new BukkitRunnable() {
            @Override
            public void run() {
                if(cloud.isDead()){
                    this.cancel();
                    return;
                }
                Collection<Entity> entities = w.getNearbyEntities(loc,radius,radius,radius);
                for(Entity e : entities){
                    if(distance(jar,e) > radius)continue;
                    if(e instanceof Monster m){
                        m.setTarget(null);
                    }
                    if(e instanceof AreaEffectCloud a){
                        if(a.getName().contains("火")){
                            a.remove();
                            w.playSound(a.getLocation(),Sound.BLOCK_FIRE_EXTINGUISH,2,1);
                        }
                    }
                    if(e instanceof LivingEntity l){
                        l.setFireTicks(0);
                    }
                }
            }
        };
        smoking.runTaskTimer(plugin,0L,10L);
    }

    public void electric(Entity jar, double radius){
        World w = jar.getWorld();
        w.playSound(jar.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 1, 1);
        w.spawnParticle(Particle.ELECTRIC_SPARK,jar.getLocation(), (int) (20 * radius)
        ,radius / 2,radius / 2,radius / 2);
        for (Entity e : jar.getNearbyEntities(radius, radius, radius)) {
            if (e instanceof LivingEntity l) {
                if (e instanceof Player player) {
                    player.sendTitle(" ", ChatColor.AQUA + "！被电击！", 10, 40, 10);
                    w.playSound(player.getLocation(),Sound.ENTITY_ARMOR_STAND_BREAK,1,1);
                    Bukkit.getPluginManager().callEvent(new PlayerShieldAmountChangeEvent(player, -20));
                }
                l.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 2));
                l.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 100, 4));
                Location loc = l.getLocation();
                loc.setPitch(r.nextInt(180) - 90);
                loc.setYaw(r.nextInt(180) - 90);
                l.teleport(loc);
            }
        }
    }
    public void bait(Player p,Entity jar,double health,double amp){
        World w = p.getWorld();
        Allay a = (Allay) w.spawnEntity(jar.getLocation(),EntityType.ALLAY);
        a.getAttribute(Attribute.MAX_HEALTH).setBaseValue(health);
        a.setHealth(health);
        BukkitRunnable damage = new BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {
                if(a.isDead()){
                    this.cancel();
                    return;
                }
                a.damage(health * amp);
                if(count % 2 == 0){
                    for(Entity e : a.getNearbyEntities(12,12,12)){
                        if(e instanceof Mob m){
                            m.setTarget(a);
                        }
                    }
                }
                count += 1;
            }
        };
        damage.runTaskTimer(plugin,0L,20L);
    }
    public boolean hitBallBlock(Entity e,double radius){
        World w = e.getWorld();
        Location loc = e.getLocation();
        double add = Math.min(radius,1);
        for(double x =-radius;x<=radius;x += add) {
            for (double y = -radius; y <= radius; y += add) {
                for (double z = -radius; z <= radius; z += add) {
                    if (x * x + y * y + z * z <= radius) {
                        int blockX = loc.getBlockX();
                        int blockY = loc.getBlockY();
                        int blockZ = loc.getBlockZ();
                        Block nearbyBlock = w.getBlockAt(new Location(w,blockX + x, blockY + y, blockZ + z));
                        if (isFullBlock(nearbyBlock)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
    public boolean hitBallBlock(Entity e){
        return hitBallBlock(e,1);
    }
    public boolean isFullBlock(Block b){
        Material type = b.getType();
        String name = type.toString();
        if(type == Material.FIRE)return false;
        if(type == Material.AIR)return false;
        if(type == Material.LIGHT)return false;
        if(type == Material.IRON_BARS)return false;
        if(type == Material.POWDER_SNOW)return false;
        if(name.contains("BUTTON")) {
            return false;
        }
        if(name.contains("PANE")) {
            return false;
        }
        if(name.contains("TRAPDOOR")){
            return false;
        }
        if(name.contains("CARPET")){
            return false;
        }
        if(name.contains("SIGN")){
            return false;
        }
        if(name.contains("FENCE")){
            return false;
        }
        if(name.contains("WALL")){
            return false;
        }
        return true;
    }
    public boolean isFullSet(Player p) {
        EntityEquipment e = p.getEquipment();
        ItemStack i1 = e.getHelmet();
        ItemStack i2 = e.getChestplate();
        ItemStack i3 = e.getLeggings();
        ItemStack i4 = e.getBoots();
        String name1 = getLore(i1);
        String name2 = getLore(i2);
        String name3 = getLore(i3);
        String name4 = getLore(i4);
        int index1 = name1.indexOf("头盔");
        int index2 = name2.indexOf("胸甲");
        int index3 = name3.indexOf("护腿");
        int index4 = name4.indexOf("靴子");
        if(index1 < 0 || index2 < 0 || index3 < 0 || index4 < 0)return false;
        String sub1 = name1.substring(0, index1);
        String sub2 = name2.substring(0, index2);
        String sub3 = name3.substring(0, index3);
        String sub4 = name4.substring(0, index4);
        return sub1.equals(sub2) && sub2.equals(sub3) && sub3.equals(sub4);
    }
    public boolean bounce(Entity e, double amp) {
        World w = e.getWorld();
        Location eLoc = e.getLocation();
        Location loc = w.getBlockAt(eLoc).getLocation();
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x * x + y * y + z * z <= 1) {
                        Block nearbyBlock = w.getBlockAt(loc.clone().add(x, y, z));
                        if (isFullBlock(nearbyBlock)) {
                            Location blockLoc = nearbyBlock.getLocation();
                            Vector locVec = loc.toVector();
                            Vector blockVec = blockLoc.toVector();
                            Vector bounceVec = (locVec.subtract(blockVec)).clone();
                            Vector eVec = e.getVelocity();
                            e.setVelocity((bounceVec.add(eVec)).multiply(amp));
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
    public boolean dikBounce(Entity e, double amp) {
        World world = e.getWorld();
        Location start = e.getLocation();
        Vector velocity = e.getVelocity();

        // 忽略速度过小的实体（防止误判）
        if (velocity.lengthSquared() < 0.01) {
            return false;
        }

        // 沿速度方向进行射线检测，长度等于当前速度大小（即一 tick 的移动距离）
        RayTraceResult result = world.rayTraceBlocks(start, velocity, velocity.length(),
                FluidCollisionMode.NEVER, true);

        if (result != null && result.getHitBlock() != null && result.getHitBlockFace() != null) {
            BlockFace face = result.getHitBlockFace();          // 撞击面
            Vector normal = face.getDirection();                // 法线向量

            // 将实体精确移动到撞击点（防止卡入方块）
            Location hitLoc = result.getHitPosition().toLocation(world);
            e.teleport(hitLoc);

            // 反射公式：R = V - 2*(V·N)*N
            double dot = velocity.dot(normal);
            Vector reflected = velocity.clone().subtract(normal.clone().multiply(2 * dot));
            reflected.multiply(amp);   // 应用反弹系数（例如 0.8 模拟能量损失）

            e.setVelocity(reflected);
            return true;
        }
        return false;
    }
    public ItemStack[] checkMaterials(Player player, ItemStack[] required) {
        List<ItemStack> items = new ArrayList<>();
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() != Material.AIR) items.add(item.clone());
        }
        List<ItemStack> missing = new ArrayList<>();
        for (ItemStack req : required) {
            if(req == null)continue;
            int need = req.getAmount();
            Iterator<ItemStack> iter = items.iterator();
            while (iter.hasNext() && need > 0) {
                ItemStack item = iter.next();
                if (item.isSimilar(req)) {
                    int amount = item.getAmount();
                    if (amount <= need) {
                        need -= amount;
                        iter.remove();
                    } else {
                        item.setAmount(amount - need);
                        need = 0;
                    }
                }
            }
            if (need > 0) {
                ItemStack miss = req.clone();
                miss.setAmount(need);
                missing.add(miss);
            }
        }
        return missing.toArray(new ItemStack[0]);
    }
    public void removeItems(Player player, ItemStack[] required) {
        for (ItemStack req : required) {
            if(req == null)continue;
            int need = req.getAmount();
            ItemStack[] contents = player.getInventory().getContents();
            for (int i = 0; i < contents.length && need > 0; i++) {
                ItemStack item = contents[i];
                if (item != null && item.isSimilar(req)) {
                    int amount = item.getAmount();
                    if (amount <= need) {
                        need -= amount;
                        player.getInventory().setItem(i, null);
                    } else {
                        item.setAmount(amount - need);
                        need = 0;
                    }
                }
            }
        }
    }
    public void drawBlockOutline(Player player, Block block, Particle particle) {
        BoundingBox box = block.getBoundingBox();
        double minX = box.getMinX(), minY = box.getMinY(), minZ = box.getMinZ();
        double maxX = box.getMaxX(), maxY = box.getMaxY(), maxZ = box.getMaxZ();

        // 定义12条边的两个端点
        double[][][] edges = {
                {{minX,minY,minZ}, {maxX,minY,minZ}}, // 底面 X 边
                {{minX,minY,maxZ}, {maxX,minY,maxZ}},
                {{minX,minY,minZ}, {minX,minY,maxZ}}, // 底面 Z 边
                {{maxX,minY,minZ}, {maxX,minY,maxZ}},
                {{minX,maxY,minZ}, {maxX,maxY,minZ}}, // 顶面 X 边
                {{minX,maxY,maxZ}, {maxX,maxY,maxZ}},
                {{minX,maxY,minZ}, {minX,maxY,maxZ}}, // 顶面 Z 边
                {{maxX,maxY,minZ}, {maxX,maxY,maxZ}},
                {{minX,minY,minZ}, {minX,maxY,minZ}}, // 垂直 X 边
                {{maxX,minY,minZ}, {maxX,maxY,minZ}},
                {{minX,minY,maxZ}, {minX,maxY,maxZ}},
                {{maxX,minY,maxZ}, {maxX,maxY,maxZ}}
        };

        int pointsPerEdge = 5;
        List<double[]> positions = new ArrayList<>();
        for (double[][] edge : edges) {
            double[] start = edge[0], end = edge[1];
            for (int i = 0; i < pointsPerEdge; i++) {
                double t = i / (double)(pointsPerEdge - 1);
                double x = start[0] + t * (end[0] - start[0]);
                double y = start[1] + t * (end[1] - start[1]);
                double z = start[2] + t * (end[2] - start[2]);
                positions.add(new double[]{x, y, z});
            }
        }

        // 按 Y 坐标排序（从低到高）
        positions.sort(Comparator.comparingDouble(p -> p[1]));

        Iterator<double[]> iter = positions.iterator();
        new BukkitRunnable() {
            public void run() {
                for (int i = 0; i < 3 && iter.hasNext(); i++) {
                    double[] p = iter.next();
                    player.spawnParticle(particle, p[0], p[1], p[2], 0);
                }
                if (!iter.hasNext()) cancel();
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    public void spawnCircleParticles(Location center, double radius, int points) {
        World world = center.getWorld();
        double y = center.getY();
        double step = 2 * Math.PI / points;
        for (int i = 0; i < points; i++) {
            double angle = i * step;
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            world.spawnParticle(Particle.FLAME, x, y, z, 1, 0, 0, 0, 0);
        }
    }
}
