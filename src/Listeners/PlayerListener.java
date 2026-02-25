package Listeners;

import Universal.Kit;
import Universal.PlayerStats;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;

public class PlayerListener implements Listener {
    JavaPlugin plugin;

    Kit k = Kit.INSTANCE;
    PlayerStats playerStats = PlayerStats.INSTANCE;
    HashSet<Player>reviving = new HashSet<>();
    HashSet<Player>beingRevive = new HashSet<>();
    HashMap<Player,Player>whoIsReviving = new HashMap<>();
    HashMap<String,BukkitRunnable>playerTask = new HashMap<>();

    public void setPlugin(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    @EventHandler
    public void playerItemDamage(PlayerItemDamageEvent damageEvent){
        ItemStack item = damageEvent.getItem();
        int damage = damageEvent.getDamage();
        if(item.getType() == Material.SHIELD){
            damageEvent.setDamage(damage * 3);
        }
    }
    @EventHandler
    public void playerSlide(PlayerToggleSneakEvent sneakEvent) {
        Player p = sneakEvent.getPlayer();
        Location loc = p.getEyeLocation();
        Vector vec = loc.getDirection();
        Vector slide = (vec.setY(0.5)).normalize();
        if(k.angle(vec,new Vector(0,1,0)) > 0.9){
            slide.multiply(0.3);
        }
        if (p.isSprinting()
                && p.isSneaking()
                && p.isOnGround()
                && p.getCooldown(Material.LIGHT) == 0) {
            int food = p.getFoodLevel();
            p.setFoodLevel(Math.max(0,food - 2));
            p.setCooldown(Material.LIGHT, 20);
            p.setVelocity(slide.multiply(0.9));
        }
    }
    public void crawling(Player p){
        World w = p.getWorld();
        Shulker s = (Shulker) w.spawnEntity(p.getEyeLocation(),EntityType.SHULKER);
        s.getAttribute(Attribute.SCALE).setBaseValue(0.7);
        s.setInvisible(true);
        s.setSilent(true);
        s.setAI(false);
        s.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE,PotionEffect.INFINITE_DURATION,10));
        BukkitRunnable crawling = new BukkitRunnable() {
            @Override
            public void run() {
                if(!playerStats.isDying(p)){
                    s.remove();
                    this.cancel();
                }
                s.teleport(p.getLocation().add(0,0.5,0));
            }
        };
        crawling.runTaskTimer(plugin,0L,1L);
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void playerDeath(EntityDamageEvent damageEvent) {
        double damage = damageEvent.getFinalDamage();
        DamageType damageType = damageEvent.getDamageSource().getDamageType();
        if(damageType.equals(DamageType.OUT_OF_WORLD))return;
        if(damageType.equals(DamageType.FIREWORKS)){
            damageEvent.setCancelled(true);
        }
        Entity entity = damageEvent.getEntity();
        if (entity instanceof Player p) {
            if(p.getLastDamage() == 0)return;
            World w = p.getWorld();
            double health = p.getHealth();
            if (damage >= p.getHealth()) {
                damageEvent.setCancelled(true);
                ItemStack offHand = p.getEquipment().getItemInOffHand();
                if(offHand.getType() == Material.TOTEM_OF_UNDYING){
                    w.playSound(p.getLocation(),Sound.ITEM_TOTEM_USE,1,1);
                    p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION,600,1));
                    p.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION,100,1));
                    offHand.setAmount(0);
                    return;
                }
                if(!playerStats.isDying(p)) {
                    for(PotionEffect po : p.getActivePotionEffects()){
                        p.removePotionEffect(po.getType());
                    }
                    p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, PotionEffect.INFINITE_DURATION, 10));
                    p.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, PotionEffect.INFINITE_DURATION, 10));
                    p.setHealth(20);
                    p.setFoodLevel(6);
                    p.setFireTicks(0);
                    crawling(p);
                    sos(p);
                    playerStats.setDying(p);
                    for(Entity e : p.getNearbyEntities(10,10,10)){
                        if(e instanceof Mob m){
                            if(m.getTarget() == p){
                                m.setTarget(null);
                            }
                        }
                    }
                }else {
                    p.setHealth(20);
                    playerStats.stopDying(p);
                    p.removePotionEffect(PotionEffectType.MINING_FATIGUE);
                    p.removePotionEffect(PotionEffectType.WEAKNESS);
                }
            }
            if (health >= 12) {
                damage *= 0.9;
                damageEvent.setDamage(damage);
            }
            if(!playerStats.isDying(p)) {
                if (this.k.isArmored(p) && health >= 12.0 && health - damage <= 12.0) {
                    if (p.getCooldown(Material.BARRIER) == 0) {
                        w.spawnParticle(Particle.SONIC_BOOM, p.getLocation().add(0, 1, 0), 1);
                        w.playSound(p.getLocation(), Sound.ITEM_TRIDENT_THUNDER, 1.0F, 1.0F);
                        w.playSound(p.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0F, 1.0F);
                        w.playSound(p.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1.0F, 1.0F);
                        p.setCooldown(Material.BARRIER, 100);
                    }
                }
            }
        }
    }

    public void sos(final Player p) {
        final World w = p.getWorld();
        Firework firework = (Firework)w.spawnEntity(p.getLocation().add(0,2,0), EntityType.FIREWORK_ROCKET);
        FireworkMeta meta = firework.getFireworkMeta();
        meta.setPower(3);
        meta.addEffect(FireworkEffect.builder()
                .withColor(Color.RED)
                .flicker(true)
                .with(FireworkEffect.Type.BALL_LARGE).build());
        firework.setFireworkMeta(meta);
        BukkitRunnable sos = new BukkitRunnable() {
            int count = 0;
            public void run() {
                switch (count) {
                    case 0,1,2,4,6,8,10,11,12:
                        w.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 1.0F, 2F);
                        break;
                }
                if (count == 12)
                    cancel();
                count++;
            }
        };
        sos.runTaskTimer(plugin, 0L, 3L);
    }
    @EventHandler
    public void playerInteractEntity(PlayerInteractAtEntityEvent interact){
        Player p = interact.getPlayer();
        Entity clicked = interact.getRightClicked();
        ItemStack hand = p.getInventory().getItemInMainHand();
        if(hand.getType() == Material.NAME_TAG){
            interact.setCancelled(true);
        }
        if(clicked instanceof Player p1){
            if(p.isSneaking()) {
                if (playerStats.isDying(p1) && !playerStats.isDying(p)) {
                    Player reviver = whoIsReviving.getOrDefault(p1, null);
                    if (reviver == null || reviver == p) {
                        reviveTeammate(p1, p, 5);
                    } else {
                        p.sendTitle(" ", ChatColor.AQUA + "正在被复活", 0, 10, 10);
                    }
                }
            }
        }
    }
    public void reviveTeammate(Player p,Player reviver,int time) {
        if(p.getGameMode().equals(GameMode.SPECTATOR))return;
        BukkitRunnable task = playerTask.getOrDefault(reviver.getName(), null);
        reviving.add(reviver);
        if (task == null) {
            whoIsReviving.put(p, reviver);
            BukkitRunnable checkReviving = new BukkitRunnable() {
                final int step = time * 2;
                int check = 0;
                @Override
                public void run() {
                    if (!reviving.contains(reviver) && check < step) {
                        beingRevive.remove(p);
                        reviver.playSound(reviver.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
                        playerTask.remove(reviver.getName());
                        whoIsReviving.remove(p);
                        reviver.sendTitle(" ", ChatColor.AQUA + "复活中断", 0, 10, 10);
                        this.cancel();
                    }
                    if (check >= step) {
                        reviver.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                        ChatColor c = ChatColor.AQUA;
                        Bukkit.broadcastMessage(c + reviver.getName() + ChatColor.YELLOW + "复活了" + c + p.getName());
                        playerTask.remove(reviver.getName());
                        whoIsReviving.remove(p);
                        p.setHealth(10);
                        p.removePotionEffect(PotionEffectType.MINING_FATIGUE);
                        p.removePotionEffect(PotionEffectType.WEAKNESS);
                        playerStats.stopDying(p);
                        this.cancel();
                    }
                    if (reviving.contains(reviver)) {
                        check += 1;
                        String progress = reviveProgress(step, check);
                        reviver.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                                TextComponent.fromLegacy(ChatColor.AQUA + "" + ChatColor.BOLD + progress));
                        if (!p.isDead()) {
                            reviver.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 2, 0.6f + 0.1f * check);
                            reviver.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 2, 0.6f + 0.1f * check);
                            reviver.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 2, 0.6f + 0.1f * check);
                        }
                        beingRevive.add(p);
                        p.sendTitle(" ", ChatColor.AQUA + "正在被复活", 0, 20, 0);
                        p.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                                TextComponent.fromLegacyText(ChatColor.AQUA + progress));
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 2, 0.6f + 0.1f * check);
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 2, 0.6f + 0.1f * check);
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 2, 0.6f + 0.1f * check);
                        reviving.remove(reviver);
                    }
                }
            };
            checkReviving.runTaskTimer(plugin, 0L, 10L);
            playerTask.put(reviver.getName(), checkReviving);
        }
    }
    public String reviveProgress(int total,int step){
        StringBuilder progress = new StringBuilder();
        if(step >= total)return "复活成功！";
        progress.append("复活进度：");
        for(int i = 0;i < total;i ++){
            if(i < step){
                progress.append("|");
            }else {
                progress.append(".");
            }
        }
        return progress.toString();
    }
}
