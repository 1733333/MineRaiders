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
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.List;

public enum Kit {
    INSTANCE;
    JavaPlugin plugin;

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
        Location l1 = e1.getLocation();
        Location l2 = e2.getLocation();
        return (l1.subtract(l2)).length();
    }
    public double locDistance(Location l1,Location l2){
        return (l1.subtract(l2)).length();
    }
    public double angle(Vector v1,Vector v2){
        Vector v1N = v1.clone().normalize();
        Vector v2N = v2.clone().normalize();
        return v1N.dot(v2N);
    }
    public void explode(LivingEntity source,Entity jar,double damage,double amp,int radius){
        World w = source.getWorld();
        Collection<Entity> entities = w.getNearbyEntities(jar.getLocation(),radius,radius,radius);
        for(Entity e : entities){
            if(distance(jar,e) > radius)continue;
            if(e instanceof LivingEntity l){
                int distance = (int) distance(jar,l);
                if(distance >= 1){
                    damage -= distance * amp;
                }
                if (source.getName().equals("§a爆爆")) {
                    l.damage(damage);
                    source.remove();
                }else {
                    l.damage(damage, DamageSource.builder(DamageType.EXPLOSION)
                            .withCausingEntity(source).build());
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
                Location jarLoc = jar.getLocation();
                for(Entity e : entities){
                    if(distance(jar,e) > radius)continue;
                    if(e.getFireTicks() > 0) {
                        w.playSound(cloud.getLocation(), Sound.ITEM_FIRECHARGE_USE, 2, 1);
                        w.playSound(cloud.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 2, 1);
                        w.spawnParticle(Particle.EXPLOSION, cloud.getLocation(), 1);
                        w.spawnParticle(Particle.FLAME,loc,100,0,0,0,0.2);
                        cloud.remove();
                        break;
                    }
                    if(e instanceof LivingEntity l){
                        if(l instanceof Player p1) {
                            if(p1.getGameMode() == GameMode.SPECTATOR)continue;
                        }
                        if(l instanceof ArmorStand)continue;
                        double max = l.getAttribute(Attribute.MAX_HEALTH).getBaseValue();
                        Vector speed = l.getVelocity();
                        l.damage(max * 0.15,p);
                        l.setVelocity(speed);
                        w.playSound(l.getLocation(),Sound.ENTITY_PLAYER_HURT,1,1);
                    }
                }
                Particle.DustOptions dust = new Particle.DustOptions(Color.LIME,3);
                w.spawnParticle(Particle.DUST,loc,150,radius/2,radius/2,radius/2,0.1,dust);
            }
        };
        gassing.runTaskTimer(plugin,0L,20L);
    }
    public void fire(LivingEntity source,Location loc,int duration,int radius){
        World w = source.getWorld();
        AreaEffectCloud cloud = (AreaEffectCloud) w.spawnEntity(loc,EntityType.AREA_EFFECT_CLOUD);
        cloud.setDuration(duration * 30);
        cloud.setParticle(Particle.FLAME);
        cloud.setRadius((float) radius);
        cloud.setCustomName(source.getName() + "的火");
        BukkitRunnable fireDamage = new BukkitRunnable() {
            @Override
            public void run() {
                if(cloud.isDead()){
                    this.cancel();
                    return;
                }
                w.spawnParticle(Particle.FLAME,loc,200,radius/2.0,radius/2.0,radius/2.0,0);
                Collection<Entity>entities = w.getNearbyEntities(loc,radius,radius,radius);
                for(Entity e : entities){
                    if(locDistance(loc,e.getLocation()) > radius)continue;
                    if(e instanceof LivingEntity l){
                        if(e instanceof Player p1){
                            if(p1.getGameMode() == GameMode.SPECTATOR)continue;
                        }
                        l.setFireTicks(100);
                        l.damage(4, DamageSource.builder(DamageType.IN_FIRE)
                                .withCausingEntity(cloud).build());
                        w.playSound(l.getLocation(),Sound.ENTITY_PLAYER_HURT_ON_FIRE,1,1);
                    }
                }
            }
        };
        fireDamage.runTaskTimer(plugin,0L,30L);
    }

    public boolean isArmored(LivingEntity l){
        EntityEquipment e = l.getEquipment();
        return e.getArmorContents().length != 0;
    }
}
