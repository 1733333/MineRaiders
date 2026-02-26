package Listeners;

import Events.PlayerShieldBreakEvent;
import Universal.Kit;
import Universal.PlayerStats;
import org.bukkit.*;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Random;


public class ArmorListener implements Listener {
    JavaPlugin plugin;
    Kit k = Kit.INSTANCE;
    Random r = new Random();
    PlayerStats playerStats = PlayerStats.INSTANCE;

    public void setPlugin(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isFullSet(Player p) {
        EntityEquipment e = p.getEquipment();
        ItemStack i1 = e.getHelmet();
        ItemStack i2 = e.getChestplate();
        ItemStack i3 = e.getLeggings();
        ItemStack i4 = e.getBoots();
        String name1 = k.getLore(i1);
        String name2 = k.getLore(i2);
        String name3 = k.getLore(i3);
        String name4 = k.getLore(i4);
        int index1 = name1.indexOf("头盔");
        int index2 = name2.indexOf("胸甲");
        int index3 = name3.indexOf("护腿");
        int index4 = name4.indexOf("靴子");
        String sub1 = name1.substring(0, index1);
        String sub2 = name2.substring(0, index2);
        String sub3 = name3.substring(0, index3);
        String sub4 = name4.substring(0, index4);
        return sub1.equals(sub2) && sub2.equals(sub3) && sub3.equals(sub4);
    }

    public int hasArmor(Player p, String name) {
        EntityEquipment e = p.getEquipment();
        ItemStack i1 = e.getHelmet();
        ItemStack i2 = e.getChestplate();
        ItemStack i3 = e.getLeggings();
        ItemStack i4 = e.getBoots();
        int match = 0;
        if (i1 != null) {
            if (k.getLore(i1).contains(name)) {
                match += 1;
            }
        }
        if (i2 != null) {
            if (k.getLore(i2).contains(name)) {
                match += 1;
            }
        }
        if (i3 != null) {
            if (k.getLore(i3).contains(name)) {
                match += 1;
            }
        }
        if (i4 != null) {
            if (k.getLore(i4).contains(name)) {
                match += 1;
            }
        }
        return match;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void playerDamaged(EntityDamageEvent damageEvent) {
        Entity damaged = damageEvent.getEntity();
        World w = damaged.getWorld();
        if (damaged instanceof Player p) {
            double health = p.getHealth();
            double damage = damageEvent.getFinalDamage();
            if (p.getNoDamageTicks() > p.getMaximumNoDamageTicks()) {
                damageEvent.setCancelled(true);
                return;
            }
            if (playerStats.hasShield(p)) {
                Particle.DustOptions dust = new Particle.DustOptions(Color.AQUA,1);
                w.spawnParticle(Particle.DUST,p.getLocation().add(0,1,0),
                        50,0,1,0,dust);
                damage *= 0.9;
            }
            EntityEquipment e = p.getEquipment();
            ItemStack chest = e.getChestplate();
            String cName = k.getLore(chest);
            if (isFullSet(p)) {
                if (health > 19) {
                    if (cName.contains("黑曜石")) {
                        damage *= 0.2;
                        w.playSound(p.getLocation(), Sound.BLOCK_NETHER_BRICKS_BREAK, 1, 1);
                        w.playSound(p.getLocation(), Sound.BLOCK_NETHER_BRICKS_BREAK, 1, 1);
                        w.playSound(p.getLocation(), Sound.BLOCK_NETHER_BRICKS_BREAK, 1, 1);
                    }
                }
            }
            if (hasArmor(p, "幻翼") > 0) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 1));
            }
            if (hasArmor(p, "干草") > 0) {
                int amount = hasArmor(p, "干草");
                int hunger = p.getFoodLevel();
                p.setFoodLevel(Math.min(20, hunger + amount));
            }
            damageEvent.setDamage(damage);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void playerDamagedByEntity(EntityDamageByEntityEvent damageEvent) {
        Entity damaged = damageEvent.getEntity();
        Entity attacker = damageEvent.getDamager();
        World w = damaged.getWorld();
        if (damaged instanceof Player p) {
            if (p.getNoDamageTicks() > p.getMaximumNoDamageTicks()) {
                damageEvent.setCancelled(true);
                return;
            }
            double damage = damageEvent.getFinalDamage();
            EntityEquipment e = p.getEquipment();
            if (isFullSet(p)) {
                ItemStack chest = e.getChestplate();
                String cName = k.getLore(chest);
                if (attacker instanceof Player) {
                    if (cName.contains("紫水晶")) {
                        double newDamage = damage * 0.75;
                        damageEvent.setDamage(newDamage);
                    }
                }
                if (attacker instanceof Mob) {
                    if (cName.contains("回响")) {
                        double newDamage = damage * 0.5;
                        damageEvent.setDamage(newDamage);
                    }
                }
            }
        }
    }

    @EventHandler
    public void playerShieldBreakEffect(PlayerShieldBreakEvent event) {
        Player p = event.getPlayer();
        World w = p.getWorld();
        EntityEquipment e = p.getEquipment();
        ItemStack chest = e.getChestplate();
        String cName = k.getLore(chest);
        if (!playerStats.isDying(p)) {
            if (isFullSet(p)) {
                switch (cName) {
                    case "§f幻翼胸甲" -> {
                        if (p.getCooldown(Material.PHANTOM_MEMBRANE) == 0) {
                            p.setCooldown(Material.PHANTOM_MEMBRANE, 600);
                            k.smoke(p, 10, 3);
                        }
                    }
                    case "§f绿宝石胸甲" -> {
                        if (p.getCooldown(Material.EMERALD) == 0) {
                            p.setCooldown(Material.EMERALD, 600);
                            p.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 600, 1));
                        }
                    }
                    case "§f紫水晶胸甲" -> {
                        for (int i = 0; i < 20; i++) {
                            Vector shootVec = new Vector(r.nextDouble() - r.nextDouble(),
                                    r.nextDouble() - r.nextDouble(), r.nextDouble() - r.nextDouble());
                            Arrow a = w.spawnArrow(p.getLocation().add(0, 1, 0), shootVec, 2, 0);
                            a.setDamage(4);
                            a.setShooter(p);
                            a.setTicksLived(1200);
                        }
                    }
                    case "§f回响胸甲" -> {
                        if (p.getCooldown(Material.ECHO_SHARD) == 0) {
                            p.setCooldown(Material.ECHO_SHARD, 600);
                            k.bait(p, p, 30, 0.08);
                        }
                    }
                }
            }
        }
    }
}

