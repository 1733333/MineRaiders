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
    public void mobMeleeAttack(EntityDamageByEntityEvent damageEvent) {
        DamageType damageType = damageEvent.getDamageSource().getDamageType();
        World w = damageEvent.getDamager().getWorld();
        if ((damageEvent.getDamager() instanceof LivingEntity attacker)) {
            String aName = attacker.getName();
            if (aName.equals("§c粉碎者")) {
                if (!damageType.equals(DamageType.ARROW)) {
                    damageEvent.setCancelled(true);
                }
            }
            if (aName.equals("§6火球")) {
                if (!damageType.equals(DamageType.IN_FIRE)) {
                    damageEvent.setCancelled(true);
                }
            }
            if (aName.equals("§a跳蚤")) {
                damageEvent.setCancelled(true);
                if (damageEvent.getEntity() instanceof Player p) {
                    if (!attacker.hasPotionEffect(PotionEffectType.LUCK)) {
                        p.addPassenger(attacker);
                        p.sendTitle(ChatColor.RED + "！被跳蚤缠上了！",
                                ChatColor.RED + "使用 潜行键 挣脱", 10, 40, 10);
                        BukkitRunnable blind = new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (p.getPassengers().isEmpty()) {
                                    this.cancel();
                                }
                                p.addPotionEffect(new PotionEffect(PotionEffectType.POISON,
                                        100, 0));
                                p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS,
                                        100, 0));
                            }
                        };
                        blind.runTaskTimer(plugin, 0L, 100L);
                    }
                }
            }
        }
        if (damageEvent.getEntity() instanceof LivingEntity damaged) {
            String dName = damaged.getName();
            if (dName.equals("§6火球")) {
                if (damaged.hasPotionEffect(PotionEffectType.RESISTANCE)) {
                    w.playSound(damaged, Sound.ITEM_SHIELD_BLOCK, 1, 1);
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
            }
        }
    }

    @EventHandler
    public void mobDeath(EntityDeathEvent deathEvent){
        LivingEntity mob = deathEvent.getEntity();
        World w = mob.getWorld();
        if (mob.getName().equals("§6火球")) {
            BukkitRunnable fire = new BukkitRunnable() {
                @Override
                public void run() {
                    k.fire(mob,mob.getLocation(),5,2);
                }
            };
            fire.runTaskLater(plugin,20L);
            w.spawnParticle(Particle.EXPLOSION,mob.getLocation(),1);
            w.spawnParticle(Particle.FLAME,mob.getLocation(),20,0,0,0,0.3);
        }
    }
}
