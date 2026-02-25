package Universal;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
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
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.List;
import java.util.Random;

public enum Kit {
    INSTANCE;
    JavaPlugin plugin;
    Random r = new Random();

    public void setPlugin(JavaPlugin plugin) {
        this.plugin = plugin;
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
        if(item.getItemMeta() != null) {
            ItemMeta meta = item.getItemMeta();
            if (meta.hasLore()) {
                String[] lore = meta.getLore().toArray(new String[0]);
                return lore[0];
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
    public void explode(LivingEntity source,Entity jar,double damage,double amp,int radius,double selfDamage) {
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
                    if (selfDamage == 0) continue;
                    damage *= selfDamage;
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
        cloud.setDuration(duration * 30);
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
                        l.setFireTicks(fire + 100);
                        l.damage(damage, DamageSource.builder(DamageType.ON_FIRE)
                                .withDirectEntity(cloud).build());
                        w.playSound(l.getLocation(), Sound.ENTITY_PLAYER_HURT_ON_FIRE, 1, 1);
                    }
                }
            }
        };
        fireDamage.runTaskTimer(plugin, 0L, 30L);
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
    public void glitch(Player p,Entity jar,int duration,int radius){
        World w = p.getWorld();
        Location loc = jar.getLocation();
        AreaEffectCloud cloud = (AreaEffectCloud) w.spawnEntity(loc,EntityType.AREA_EFFECT_CLOUD);
        cloud.setDuration(duration * 40);
        Particle.DustOptions dust = new Particle.DustOptions(Color.BLUE,0.7f);
        cloud.setParticle(Particle.DUST,dust);
        cloud.setRadius((float) radius);
        cloud.setCustomName(p.getName() + "的紊乱云");
        BukkitRunnable glitching = new BukkitRunnable() {
            @Override
            public void run() {
                if(cloud.isDead()){
                    this.cancel();
                    return;
                }
                Collection<Entity> entities = w.getNearbyEntities(loc,radius,radius,radius);
                for(Entity e : entities){
                    if(distance(jar,e) > radius)continue;
                    if(e instanceof Player player){
                        if(player.getGameMode() == GameMode.SPECTATOR)continue;
                        player.sendTitle(" ",ChatColor.AQUA + "！被紊乱！",0,20,10);
                        w.playSound(player.getLocation(),Sound.ENTITY_ARMOR_STAND_BREAK,1,1);
                    }
                    if(e instanceof LivingEntity l){
                        if(l.getName().contains("滑索"))continue;
                        if(l instanceof Snowman s){
                            s.setTarget(null);
                        }
                        Location loc = l.getLocation();
                        loc.setPitch(r.nextInt(180) - 90);
                        loc.setYaw(r.nextInt(180) - 90);
                        l.teleport(loc);
                    }
                }
                Particle.DustOptions dust = new Particle.DustOptions(Color.BLUE,1);
                w.spawnParticle(Particle.DUST,loc,150,radius/2,radius/2,radius/2,0.1,dust);
            }
        };
        glitching.runTaskTimer(plugin,0L,40L);
    }
    public boolean hitBallBlock(Entity e){
        World w = e.getWorld();
        Location loc = e.getLocation();
        for(int x =-1;x<=1;x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x * x + y * y + z * z <= 1) {
                        int blockX = loc.getBlockX();
                        int blockY = loc.getBlockY();
                        int blockZ = loc.getBlockZ();
                        Block nearbyBlock = w.getBlockAt(blockX + x, blockY + y, blockZ + z);
                        if (isFullBlock(nearbyBlock)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
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
}
