package Listeners;

import Universal.Kit;
import org.bukkit.*;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

public class MonsterListener implements Listener {
    JavaPlugin plugin;
    Kit k = Kit.INSTANCE;

    public void setPlugin(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    @EventHandler
    public void mobMeleeAttack(EntityDamageByEntityEvent damageEvent){
        LivingEntity attacker = (LivingEntity) damageEvent.getDamager();
        LivingEntity damaged = (LivingEntity) damageEvent.getEntity();
        DamageType damageType = damageEvent.getDamageSource().getDamageType();
        String aName = attacker.getName();
        if(aName.equals("§c粉碎者")){
            if(!damageType.equals(DamageType.ARROW)) {
                damageEvent.setCancelled(true);
            }
        }
        if(aName.equals("§a爆爆")){
            if(!damageType.equals(DamageType.EXPLOSION)) {
                damageEvent.setCancelled(true);
            }
        }
        if(aName.equals("§a跳蚤")) {
            damageEvent.setCancelled(true);
            if(damaged instanceof Player p) {
                if (!attacker.hasPotionEffect(PotionEffectType.LUCK)) {
                    p.addPassenger(attacker);
                    p.sendTitle(ChatColor.RED + "！被跳蚤缠上了！",
                            ChatColor.RED + "使用 Shift 挣脱", 10, 40, 10);
                    BukkitRunnable blind = new BukkitRunnable() {
                        @Override
                        public void run() {
                            if(p.getPassengers().isEmpty()){
                                this.cancel();
                            }
                            p.addPotionEffect(new PotionEffect(PotionEffectType.POISON,
                                    100, 0));
                            p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS,
                                    100, 0));
                        }
                    };
                    blind.runTaskTimer(plugin,0L,100L);
                }
            }
        }
    }
    @EventHandler
    public void playerRidFlea(PlayerToggleSneakEvent sneakEvent){
        Player p = sneakEvent.getPlayer();
        if(!p.getPassengers().isEmpty()){
            for(Entity e : p.getPassengers()){
                if(e.getName().equals("§a跳蚤")){
                    p.eject();
                    e.setVelocity(p.getEyeLocation().getDirection().setY(0));
                    if(e instanceof LivingEntity l){
                        l.addPotionEffect(new PotionEffect(PotionEffectType.LUCK,
                                100,0));
                    }
                }
            }
        }
    }

    @EventHandler
    public void mobTarget(EntityTargetEvent targetEvent){
        Entity e = targetEvent.getEntity();
        if(e instanceof LivingEntity mob) {
            if (mob.getName().equals("§a爆爆")) {
                mob.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,
                        100, 0));
            }
        }
    }

    @EventHandler
    public void mobExplode(EntityExplodeEvent explodeEvent){
        explodeEvent.setCancelled(true);
        Entity e = explodeEvent.getEntity();
        World w = e.getWorld();
        if(e instanceof LivingEntity mob) {
            if (mob.getName().equals("§a爆爆")) {
                k.explode(mob,mob,10,1,5);
                w.spawnParticle(Particle.EXPLOSION_EMITTER,mob.getLocation(),1);
                w.playSound(mob.getLocation(), Sound.ENTITY_GENERIC_EXPLODE,1,1);
                mob.remove();
            }
        }
    }
}
