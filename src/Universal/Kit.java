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
import org.bukkit.inventory.Inventory;
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
    private static final String LOCK_MARK = "§7已锁定";
    JavaPlugin plugin;
    Random r = new Random();
    ArmorPool ap = ArmorPool.INSTANCE;
    WeaponPool wp = WeaponPool.INSTANCE;
    GadgetPool gp = GadgetPool.INSTANCE;
    public int[] gameOver = new int[]{
            0, 1, 0, 0, 0, 1, 1, 1,
            0, 1, 0, 0, 0, 0, 0, 1,
            0, 1, 0, 0, 1, 1, 0, 1,
            0, 1, 0, 0, 0, 1, 0, 1,
            0, 1, 0, 0, 1, 1, 1, 1,
            0, 1, 0, 1, 0, 1, 1, 0,
            0, 1, 0, 0, 0, 1, 0, 1,
            0, 1, 0, 1, 0, 0, 1, 0,
    };
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
                if (l != source && l != jar) {
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
                    Location loc = l.getLocation();
                    loc.setPitch(r.nextInt(180) - 90);
                    loc.setYaw(r.nextInt(180) - 90);
                    l.teleport(loc);
                }
                l.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 2));
                l.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 100, 4));
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
        spawnCircleParticles(center,radius,points,Particle.FLAME);
    }
    public void spawnCircleParticles(Location center, double radius, int points,Particle particle) {
        World world = center.getWorld();
        double y = center.getY();
        double step = 2 * Math.PI / points;
        for (int i = 0; i < points; i++) {
            double angle = i * step;
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            world.spawnParticle(particle, x, y, z, 1, 0, 0, 0, 0);
        }
    }
    public ItemStack createMenuItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (lore.length > 0) {
            meta.setLore(Arrays.asList(lore));
        }
        item.setItemMeta(meta);
        return item;
    }
    public void esterEgg0(Location loc){
        World world = loc.getWorld();
        new BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {
                if (count >= gameOver.length) {
                    this.cancel();
                    return;
                }
                float pitch = 0.9f;
                if (gameOver[count] > 0) {
                    pitch = 1.5f;
                }
                world.playSound(loc, Sound.BLOCK_NOTE_BLOCK_BIT, 1, pitch);
                count++;
            }
        }.runTaskTimer(plugin, 0L, 5L);
    }

    public void shieldBreakEffect(Location loc){
        World w = loc.getWorld();
        w.spawnParticle(Particle.SONIC_BOOM, loc, 1);
        BukkitRunnable sound = new BukkitRunnable() {
            int count = 0;

            @Override
            public void run() {
                if (count > 21) {
                    this.cancel();
                }
                if (count < 7) {
                    if (count == 0) {
                        w.playSound(loc, Sound.ITEM_TRIDENT_HIT_GROUND, 1, 0.5f);
                        w.playSound(loc, Sound.BLOCK_BEACON_DEACTIVATE, 1, 0.5f);
                        w.playSound(loc, Sound.ENTITY_FIREWORK_ROCKET_TWINKLE_FAR, 1, 0.8f);
                        w.playSound(loc, Sound.ITEM_TRIDENT_RIPTIDE_3, 1, 0.7f);
                        w.playSound(loc, Sound.ITEM_TRIDENT_THUNDER, 0.3f, 1);
                        w.playSound(loc, Sound.BLOCK_GLASS_BREAK, 1, 1);
                        w.playSound(loc, Sound.BLOCK_GLASS_BREAK, 1, 1);
                        w.playSound(loc, Sound.BLOCK_GLASS_BREAK, 1, 1);
                    }
                    w.playSound(loc, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1, 2 - (0.1f * count));
                } else if (count > 10) {
                    if (count % 3 == 1) {
                        w.playSound(loc, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1, 1.8f);
                    }
                }
                count += 1;
            }
        };
        sound.runTaskTimer(plugin, 0L, 2L);
    }
    
    public void setInventoryLimit(Player player, int level) {
        // 限制等级范围
        level = Math.max(0, Math.min(3, level));
        int unlockedHotbar = 6 + level;
        int unlockedCols = 3 + 2 * level;
        var inventory = player.getInventory();

        // 1. 清理旧的锁定物品（所有带LOCK_MARK的物品）
        for (int i = 0; i < 36; i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                if (meta.hasLore() && meta.getLore().contains(LOCK_MARK)) {
                    inventory.setItem(i, null);
                }
            }
        }
        if (level >= 3) return; // 全部解锁直接返回

        // 2. 收集锁定格子内的玩家物品，清空锁定槽
        List<ItemStack> playerItems = new ArrayList<>();
        // 处理快捷栏
        for (int slot = unlockedHotbar; slot <= 8; slot++) {
            ItemStack item = inventory.getItem(slot);
            if (item != null && !isLockedItem(item)) playerItems.add(item);
            inventory.setItem(slot, null);
        }
        // 处理主背包
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                if (col >= unlockedCols) {
                    int slot = 9 + row * 9 + col;
                    ItemStack item = inventory.getItem(slot);
                    if (item != null && !isLockedItem(item)) playerItems.add(item);
                    inventory.setItem(slot, null);
                }
            }
        }

        // 3. 将玩家物品转移到解锁槽，放不下的掉落
        for (ItemStack item : playerItems) {
            boolean added = false;
            // 优先放入主背包解锁槽
            for (int row = 0; row < 3 && !added; row++) {
                for (int col = 0; col < unlockedCols; col++) {
                    int slot = 9 + row * 9 + col;
                    if (inventory.getItem(slot) == null) {
                        inventory.setItem(slot, item);
                        added = true;
                        break;
                    }
                }
            }
            if (added) continue;
            // 其次放入快捷栏解锁槽
            for (int slot = 0; slot < unlockedHotbar; slot++) {
                if (inventory.getItem(slot) == null) {
                    inventory.setItem(slot, item);
                    added = true;
                    break;
                }
            }
            // 都放不下则掉落
            if (!added) player.getWorld().dropItemNaturally(player.getLocation(), item);
        }

        // 4. 创建锁定玻璃板并填充锁定槽（根据所需等级区分颜色）
        // 填充快捷栏锁定槽
        for (int slot = unlockedHotbar; slot <= 8; slot++) {
            int requiredLevel = slot - 5; // 快捷栏索引6→等级1，7→2，8→3
            inventory.setItem(slot, createLockedPane(requiredLevel));
        }
        // 填充主背包锁定槽
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                if (col >= unlockedCols) {
                    int requiredLevel;
                    if (col < 3) {
                        requiredLevel = 0; // 不会进入此分支，因为col>=unlockedCols且unlockedCols≥3
                    } else {
                        requiredLevel = (col - 3) / 2 + 1; // col:3,4→1; 5,6→2; 7,8→3
                    }
                    int slot = 9 + row * 9 + col;
                    inventory.setItem(slot, createLockedPane(requiredLevel));
                }
            }
        }
    }

    /**
     * 根据所需等级创建锁定玻璃板
     * @param requiredLevel 所需等级（1/2/3）
     * @return 对应颜色的锁定玻璃板
     */
    private ItemStack createLockedPane(int requiredLevel) {
        Material material;
        switch (requiredLevel) {
            case 1:
                material = Material.ORANGE_STAINED_GLASS_PANE;
                break;
            case 2:
                material = Material.GREEN_STAINED_GLASS_PANE;
                break;
            case 3:
                material = Material.BLUE_STAINED_GLASS_PANE;
                break;
            default:
                material = Material.RED_STAINED_GLASS_PANE;
        }
        ItemStack pane = new ItemStack(material);
        ItemMeta meta = pane.getItemMeta();
        meta.setDisplayName("§c未解锁的格子");
        List<String> lore = new ArrayList<>();
        lore.add(LOCK_MARK);
        lore.add("§7需要空岛阶段达到 " + requiredLevel + " 阶解锁");
        meta.setLore(lore);
        pane.setItemMeta(meta);
        return pane;
    }

    // 工具方法：判断物品是否是锁定的玻璃板
    public boolean isLockedItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        return meta.hasLore() && meta.getLore().contains(LOCK_MARK);
    }

    public void clearInventory(Player player) {
        Inventory inv = player.getInventory();
        ItemStack[] contents = inv.getContents();
        player.getEquipment().clear();
        for(ItemStack item : contents) {
            if(item != null && !isLockedItem(item)) {
                inv.remove(item);
            }
        }
    }
    public String progressMessage(String title,String end,String done, String undone,int total,int step){
        StringBuilder progress = new StringBuilder();
        progress.append(title);
        progress.append(ChatColor.BOLD);
        for(int i = 0;i < total;i ++){
            if(i < step){
                progress.append(done);
            }else {
                progress.append(undone);
            }
        }
        progress.append(end);
        return progress.toString();
    }

    public void freeKit(Player p){
        Inventory inv = p.getInventory();
        EntityEquipment equipment = p.getEquipment();
        equipment.setHelmet(ap.woodHelm());
        equipment.setChestplate(ap.woodChest());
        equipment.setLeggings(ap.woodLeg());
        equipment.setBoots(ap.woodBoot());
        inv.addItem(new ItemStack(Material.WOODEN_SWORD));
        inv.addItem(wp.ferro());
        inv.addItem(new ItemStack(Material.ARROW, 16));
        inv.addItem(gp.copperBattery());
        inv.addItem(gp.baitNade());
        inv.addItem(new ItemStack(Material.BREAD, 5));
    }
}
