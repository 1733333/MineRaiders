package Listeners;

import Universal.Kit;
import Universal.WeaponPool;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;

public class PlayerListener implements Listener {
    JavaPlugin plugin;
    Kit k = Kit.INSTANCE;
    HashSet<Player>isDying = new HashSet<>();
    public void setPlugin(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void playerDeath(EntityDamageEvent damageEvent) {
        if (damageEvent.getEntity() instanceof Player p) {
            World w = p.getWorld();
            double damage = damageEvent.getDamage();
            double health = p.getHealth();
            if (damage >= p.getHealth()) {
                damageEvent.setCancelled(true);
                p.setHealth(20);
                sos(p);
            }
            if(health - damage <= 12){
                damage *= 1.1;
                damageEvent.setDamage(damage);
            }
            if(k.isArmored(p)
                    && health >= 12
                    && health - damage <= 12){
                if(p.getCooldown(Material.BARRIER) == 0) {
                    w.spawnParticle(Particle.SONIC_BOOM, p.getLocation().add(0, 1, 0), 1);
                    w.spawnParticle(Particle.DUST_PLUME, p.getLocation().add(0, 1, 0),
                            15,1,1,1,0.1);
                    w.playSound(p.getLocation(), Sound.ITEM_TRIDENT_THUNDER, 1, 1);
                    p.setCooldown(Material.BARRIER,20);
                }
            }
        }
    }
    public void sos(Player p){
        World w = p.getWorld();
        Firework firework = (Firework) w.spawnEntity(p.getLocation(), EntityType.FIREWORK_ROCKET);
        FireworkMeta meta = firework.getFireworkMeta();
        meta.setPower(3);
        meta.addEffect(FireworkEffect.builder()
                .withColor(Color.RED)
                .flicker(true)
                .with(FireworkEffect.Type.BALL_LARGE).build());
        firework.setFireworkMeta(meta);
        BukkitRunnable sos = new BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {
                switch (count){
                    case 0,1,2,4,6,8,10,11,12:
                        w.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT,1,1.5f);
                }
                if(count == 14){
                    this.cancel();
                }
                count += 1;
            }
        };
        sos.runTaskTimer(plugin,0L,3L);
    }
}
