package Listeners;

import Universal.Kit;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Random;

public class WeaponListener implements Listener {
    JavaPlugin plugin;
    Kit k = Kit.INSTANCE;
    Random r = new Random();
    public void setPlugin(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    @EventHandler
    public void playerShootBow(EntityShootBowEvent shootBowEvent) {
        Entity e = shootBowEvent.getEntity();
        Entity pr = shootBowEvent.getProjectile();
        World w = e.getWorld();
        if (e instanceof Player p) {
            Location shootLoc = p.getEyeLocation();
            Vector shootVec = shootLoc.getDirection();
            ItemStack bow = shootBowEvent.getBow();
            String tag = k.getLore(bow);
            switch (tag) {
                case "§f烈焰弓" -> {
                    if (shootBowEvent.getForce() > 0.5) {
                        Vector v = pr.getVelocity();
                        pr.setVelocity(v.multiply(1.1));
                        w.playSound(p.getLocation(), Sound.ITEM_FIRECHARGE_USE, 1, 1);
                    }
                }
                case "§f风之弓" -> {
                    if (shootBowEvent.getForce() > 0.5) {
                        Vector v = pr.getVelocity();
                        pr.setVelocity(v.multiply(1.3));
                        w.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1, 1);
                    }
                }
                case "§f回响长弓" -> {
                    if (shootBowEvent.getForce() > 0.9) {
                        pr.remove();
                        w.playSound(p.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 2, 1);
                        Location pLoc = shootLoc.clone();
                        HashSet<Entity>damaged = new HashSet<>();
                        for (int i = 0; i < 10; i++) {
                            Block b = w.getBlockAt(pLoc);
                            if(b.getType() != Material.AIR)break;
                            w.spawnParticle(Particle.SONIC_BOOM, pLoc, 1);
                            for (Entity entity : w.getNearbyEntities(pLoc, 1, 1, 1)) {
                                if (entity instanceof LivingEntity l) {
                                    if (l instanceof Player p1) {
                                        if (p == p1) continue;
                                    }
                                    if (damaged.contains(l)) continue;
                                    l.damage(13, DamageSource.builder(DamageType.SONIC_BOOM)
                                            .withDirectEntity(p).build());
                                    damaged.add(l);
                                }
                            }
                            pLoc.add(shootVec.multiply(1.5));
                        }
                    }
                }
                case "§f费洛" -> {
                    w.playSound(shootLoc, Sound.ENTITY_GENERIC_EXPLODE, 2, 2);
                    w.spawnParticle(Particle.EXPLOSION, shootLoc.add(shootVec), 1);
                    Arrow a = w.spawnArrow(shootLoc, shootVec, 5, 0);
                    a.setShooter(p);
                    a.setCritical(true);
                    a.setDamage(1.5);
                    shootBowEvent.setProjectile(a);
                    p.setCooldown(bow.getType(), 20);
                }
            }
        }
    }
    @EventHandler
    public void playerInteract(PlayerInteractEvent interactEvent) {
        Action action = interactEvent.getAction();
        Player p = interactEvent.getPlayer();
        if (p.getGameMode() == GameMode.SPECTATOR) return;
        ItemStack hand = p.getInventory().getItemInMainHand();
        ItemStack offHand = p.getInventory().getItemInOffHand();
        boolean rightClick = action.equals(Action.RIGHT_CLICK_AIR)
                || action.equals(Action.RIGHT_CLICK_BLOCK);
        if (hand.getType() != Material.AIR) {
            String tag = k.getLore(hand);
            if (rightClick) {
                switch (tag){
                    case "§f回响战斧" ->{

                    }

                }
            }
        }
    }
    @EventHandler
    public void playerMeleeAttack(EntityDamageByEntityEvent damageEvent){
        Entity attacker = damageEvent.getDamager();
        Entity damaged = damageEvent.getEntity();
        double damage = damageEvent.getDamage();
        World w = attacker.getWorld();
        if(attacker instanceof Player p){
            double aDamage = p.getAttribute(Attribute.ATTACK_DAMAGE).getBaseValue();
            if(damage < aDamage * 0.9)return;
            if(damaged instanceof LivingEntity l){
                ItemStack hand = p.getInventory().getItemInMainHand();
                if (hand.getType() != Material.AIR) {
                    String tag = k.getLore(hand);
                    switch (tag){
                        case "§f大骨棒" ->{
                            w.playSound(l.getLocation(), Sound.BLOCK_ANVIL_PLACE,1,1);
                            w.playSound(l.getLocation(), Sound.BLOCK_BONE_BLOCK_PLACE,1,1);
                            w.playSound(l.getLocation(), Sound.BLOCK_BONE_BLOCK_PLACE,1,1);
                            w.playSound(l.getLocation(), Sound.BLOCK_BONE_BLOCK_PLACE,1,1);
                            l.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS,60,0,false));
                            l.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS,40,4,false));
                        }
                        case "§f战锤" -> {
                            double newDamage = damage;
                            double armor = l.getAttribute(Attribute.ARMOR).getBaseValue();
                            newDamage += armor / 2;
                            damageEvent.setDamage(newDamage);
                            if (damage < newDamage) {
                                w.playSound(l.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
                                w.playSound(l.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
                            }
                        }
                        case "§f竹叶青" ->{
                            w.playSound(l.getLocation(),Sound.BLOCK_BAMBOO_BREAK,1,1);
                            w.playSound(l.getLocation(),Sound.BLOCK_BAMBOO_BREAK,1,1);
                            l.addPotionEffect(new PotionEffect(PotionEffectType.POISON,300,0,false));
                        }
                        case "§f紫水晶刺剑" ->{
                            w.playSound(l.getLocation(),Sound.BLOCK_AMETHYST_BLOCK_BREAK,1,1);
                            w.playSound(l.getLocation(),Sound.BLOCK_AMETHYST_BLOCK_BREAK,1,1);
                            w.playSound(l.getLocation(),Sound.BLOCK_AMETHYST_BLOCK_BREAK,1,1);
                            l.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS,20,0,false));
                            if(!(l instanceof Shulker)) {
                                l.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 200, 0,false));
                            }
                        }
                        case "§f回响战斧" ->{
                            l.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 60, 0,false));
                        }
                        case "§f回响之刃" ->{
                            l.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 0,false));
                        }
                        case "§f仙人掌剑" ->{
                            p.damage(1,p);
                            w.playSound(p.getLocation(),Sound.ENCHANT_THORNS_HIT,1,1);
                        }
                    }
                }
            }
        }
    }
}
