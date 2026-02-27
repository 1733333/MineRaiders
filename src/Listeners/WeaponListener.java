package Listeners;

import Universal.Kit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Shulker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

public class WeaponListener implements Listener {
    JavaPlugin plugin;
    Kit k = Kit.INSTANCE;
    Random r = new Random();
    public void setPlugin(JavaPlugin plugin) {
        this.plugin = plugin;
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
                    }
                }
            }
        }
    }
}
