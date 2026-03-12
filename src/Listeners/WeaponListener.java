package Listeners;

import Universal.Kit;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
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
            Location shootLoc = p.getEyeLocation().clone();
            Vector shootVec = shootLoc.getDirection().clone();
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
                    Vector v = pr.getVelocity();
                    pr.setVelocity(v.multiply(1.3));
                    pr.setTicksLived(1200);
                    w.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1, 1);
                    BukkitRunnable hit = new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (pr.isDead()) {
                                w.playSound(pr.getLocation(), Sound.ENTITY_BREEZE_SHOOT, 2, 1);
                                w.spawnParticle(Particle.GUST, pr.getLocation(), 1);
                                double radius = 3;
                                for (Entity e : pr.getNearbyEntities(radius, radius, radius)) {
                                    k.knockBack(e, pr.getLocation(), -1 * (shootBowEvent.getForce() / 3));
                                }
                                this.cancel();
                            }
                        }
                    };
                    hit.runTaskTimer(plugin, 0L, 2L);
                }
                case "§f回响长弓" -> {
                    if (shootBowEvent.getForce() >= 2.9) {
                        p.setCooldown(bow.getType(), 30);
                        Arrow a = w.spawnArrow(shootLoc.add(shootVec), shootVec, 3, 2);
                        a.setColor(Color.AQUA);
                        a.setDamage(3);
                        a.setGlowing(true);
                        a.setTicksLived(1200);
                        a.setPierceLevel(100);
                        shootBowEvent.setProjectile(a);
                        HashSet<Entity> damaged = new HashSet<>();
                        ArrayList<Location> locs = new ArrayList<>();
                        BukkitRunnable record = new BukkitRunnable() {

                            @Override
                            public void run() {
                                if (a.isDead()) {
                                    BukkitRunnable explode = new BukkitRunnable() {
                                        int count = locs.size() - 1;
                                        @Override
                                        public void run() {
                                            if (count < 0) {
                                                this.cancel();
                                                return;
                                            }
                                            Location pLoc = locs.get(count);
                                            w.spawnParticle(Particle.FLASH, pLoc, 1, Color.AQUA);
                                            w.spawnParticle(Particle.SONIC_BOOM, pLoc, 1);
                                            w.playSound(pLoc, Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST_FAR, 2, 1);
                                            for (Entity entity : w.getNearbyEntities(pLoc, 2, 2, 2)) {
                                                if (entity instanceof LivingEntity l) {
                                                    if (l instanceof Player p1) {
                                                        if (p == p1) continue;
                                                    }
                                                    if (damaged.contains(l)) continue;
                                                    double max = l.getAttribute(Attribute.MAX_HEALTH).getBaseValue();
                                                    l.damage(max * 0.05, DamageSource.builder(DamageType.SONIC_BOOM)
                                                            .withDirectEntity(p).build());
                                                    damaged.add(l);
                                                }
                                            }
                                            count -= 1;
                                        }
                                    };
                                    explode.runTaskTimer(plugin, 0L, 2L);
                                    this.cancel();
                                }
                                locs.add(a.getLocation());
                                w.spawnParticle(Particle.TRIAL_OMEN, a.getLocation(), 5, 0.1, 0.1, 0.1, 0.1);
                            }
                        };
                        record.runTaskTimer(plugin, 0L, 1L);
                    }
                }
                case "§f深渊十字弩" -> {
                    if (shootBowEvent.getForce() >= 1) {
                        pr.remove();
                        w.playSound(p.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 2, 1);
                        p.setCooldown(bow.getType(), 30);
                        Location pLoc = shootLoc.clone();
                        HashSet<Entity> damaged = new HashSet<>();
                        for (int i = 0; i < 48; i++) {
                            Block b = w.getBlockAt(pLoc);
                            if (b.getType() != Material.AIR) break;
                            w.spawnParticle(Particle.SONIC_BOOM, pLoc, 1);
                            for (Entity entity : w.getNearbyEntities(pLoc, 1, 1, 1)) {
                                if (entity instanceof LivingEntity l) {
                                    if (l instanceof Player p1) {
                                        if (p == p1) continue;
                                    }
                                    if (damaged.contains(l)) continue;
                                    double max = l.getAttribute(Attribute.MAX_HEALTH).getBaseValue();
                                    l.damage(3 + max * 0.05, DamageSource.builder(DamageType.SONIC_BOOM)
                                            .withDirectEntity(p).build());
                                    damaged.add(l);
                                }
                            }
                            pLoc.add(shootVec.multiply(1));
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
                case "§f猎头" -> {
                    if (shootBowEvent.getForce() >= 2.9) {
                        w.playSound(shootLoc, Sound.ITEM_CROSSBOW_SHOOT, 1, 1);
                        Arrow a = w.spawnArrow(shootLoc, shootVec, 4, 0);
                        a.setShooter(p);
                        a.setCritical(true);
                        a.setCustomName(ChatColor.RED+ p.getName() + "的猎头箭");
                        a.setDamage(0);
                        a.setTicksLived(1200);
                        shootBowEvent.setProjectile(a);
                        BukkitRunnable particle = new BukkitRunnable() {
                            @Override
                            public void run() {
                                if(a.isDead())this.cancel();
                                w.spawnParticle(Particle.SOUL,a.getLocation(),0);
                            }
                        };
                        particle.runTaskTimer(plugin,0L,1L);
                    }
                }
                case "§f测试弓" -> {
                    if (shootBowEvent.getForce() >= 2.9) {
                        pr.setTicksLived(1200);
                        pr.addPassenger(p);
                    }
                }
            }
        }
    }
    @EventHandler
    public void projectileHit(ProjectileHitEvent hitEvent){
        Projectile pr = hitEvent.getEntity();
        if(pr.getName().contains("猎头箭")) {
            if (hitEvent.getHitEntity() != null) {
                Entity e = hitEvent.getHitEntity();
                if (e instanceof Player player) {
                    double arrowY = pr.getLocation().getY();
                    double entityY = player.getEyeLocation().getY();
                    double damage = 8;
                    if (Math.abs(entityY - arrowY) < 0.4) {
                        damage = 12;
                        if (pr.getShooter() instanceof Player p) {
                            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                            p.playSound(p.getLocation(), Sound.ENTITY_WITHER_BREAK_BLOCK, 1, 1);
                        }
                    }
                    player.damage(damage, DamageSource.builder(DamageType.ARROW).build());
                }
            }
        }
    }
    @EventHandler
    public void playerInteract(PlayerInteractEvent interactEvent) {
        Action action = interactEvent.getAction();
        Player p = interactEvent.getPlayer();
        World w = p.getWorld();
        if (p.getGameMode() == GameMode.SPECTATOR) return;
        ItemStack hand = p.getInventory().getItemInMainHand();
        ItemStack offHand = p.getInventory().getItemInOffHand();
        boolean rightClick = action.equals(Action.RIGHT_CLICK_AIR)
                || action.equals(Action.RIGHT_CLICK_BLOCK);
        if (hand.getType() != Material.AIR) {
            String tag = k.getLore(hand);
            if (rightClick) {
                switch (tag){
                    case "§f海神重锤" ->{
                        smashGround(p,hand);
                        interactEvent.setCancelled(true);
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
            ItemStack hand = p.getInventory().getItemInMainHand();
            double aDamage = p.getAttribute(Attribute.ATTACK_DAMAGE).getValue();
            if(damage < aDamage * 0.8)return;
            damage -= damageEvent.getOriginalDamage(EntityDamageEvent.DamageModifier.ARMOR);
            if(damaged instanceof LivingEntity l){
                if (hand.getType() != Material.AIR) {
                    String tag = k.getLore(hand);
                    switch (tag){
                        case "§f大骨棒" -> {
                            w.playSound(l.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1, 1);
                            w.playSound(l.getLocation(), Sound.BLOCK_BONE_BLOCK_PLACE, 1, 1);
                            w.playSound(l.getLocation(), Sound.BLOCK_BONE_BLOCK_PLACE, 1, 1);
                            w.playSound(l.getLocation(), Sound.BLOCK_BONE_BLOCK_PLACE, 1, 1);
                            l.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0, false));
                            l.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 4, false));
                        }
                        case "§f战锤" -> {
                            double newDamage = damage;
                            double armor = l.getAttribute(Attribute.ARMOR).getBaseValue();
                            newDamage += armor / 3;
                            damageEvent.setDamage(newDamage);
                            if (damage < newDamage) {
                                w.playSound(l.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
                                w.playSound(l.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
                            }
                        }
                        case "§f竹叶青" ->{
                            w.playSound(l.getLocation(),Sound.BLOCK_BAMBOO_BREAK,1,1);
                            w.playSound(l.getLocation(),Sound.BLOCK_BAMBOO_BREAK,1,1);
                            l.addPotionEffect(new PotionEffect(PotionEffectType.POISON,140,0,false));
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
                            l.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 100, 1,false));
                        }
                        case "§f回响之刃" ->{
                            l.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 0,false));
                        }
                        case "§f仙人掌剑" ->{
                            p.damage(1, DamageSource.builder(DamageType.CACTUS).build());
                            w.playSound(p.getLocation(),Sound.ENCHANT_THORNS_HIT,1,1);
                        }
                        case "§f绿宝石剑" ->{
                            if(damaged instanceof Mob m){
                                m.damage(4, DamageSource.builder(DamageType.MAGIC)
                                        .withDirectEntity(p).build());
                            }
                        }
                    }
                }
            }
        }
    }
    @EventHandler
    public void playerConsume(PlayerItemConsumeEvent consumeEvent) {
        Player p = consumeEvent.getPlayer();
        World w = p.getWorld();
        ItemStack item = consumeEvent.getItem();
        String tag = k.getLore(item);
        switch (tag) {
            case "§f金胡萝卜神的赐福" ->{
                if(p.getFoodLevel() > 0) {
                    if (p.getCooldown(item.getType()) == 0) {
                        p.setCooldown(item.getType(), 20);
                        Location shootLoc = p.getEyeLocation();
                        Vector shootVec = shootLoc.getDirection();
                        Arrow a = w.spawnArrow(shootLoc.add(shootVec), shootVec, 2, 0);
                        a.setDamage(3.5);
                        a.setShooter(p);
                        a.setTicksLived(1200);
                        a.addCustomEffect(new PotionEffect(PotionEffectType.GLOWING, 200, 0), false);
                        int food = p.getFoodLevel();
                        p.setFoodLevel(Math.max(food - 1, 0));
                        BukkitRunnable later = new BukkitRunnable() {
                            @Override
                            public void run() {
                                if(p.getGameMode() != GameMode.CREATIVE) {
                                    Item i = w.dropItem(p.getLocation(), item);
                                    i.setPickupDelay(0);
                                    i.setOwner(p.getUniqueId());
                                }
                            }
                        };
                        later.runTaskLater(plugin,1L);
                    }
                }
            }
        }
    }
    public void smashGround(Player p,ItemStack hand){
        World w = p.getWorld();
        if(p.getCooldown(hand.getType()) == 0) {
            double radius = 3;
            p.setCooldown(hand.getType(), 120);
            Location eyeLoc = p.getLocation();
            Vector eyeVec = eyeLoc.getDirection();
            Vector dash = new Vector(eyeVec.getX(),0,eyeVec.getZ());
            p.setVelocity(dash.multiply(0.8));
            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS,15,10));
            p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE,15,0));
            w.playSound(p.getLocation(),Sound.ENTITY_PLAYER_BIG_FALL,1,1);
            w.playSound(p.getLocation(),Sound.ENTITY_PLAYER_BIG_FALL,1,1);
            w.spawnParticle(Particle.BUBBLE_COLUMN_UP,p.getLocation(), 100,1.5,0,1.5,0.1);
            BukkitRunnable slam = new BukkitRunnable() {
                @Override
                public void run() {
                    if(p.getGameMode() == GameMode.SPECTATOR)return;
                    w.playSound(p.getLocation(),Sound.ENTITY_PLAYER_ATTACK_SWEEP,1,0.7f);
                    w.playSound(p.getLocation(),Sound.BLOCK_GRAVEL_BREAK,1,1);
                    Vector stabVec = p.getEyeLocation().getDirection();
                    Vector forward = new Vector(stabVec.getX(),0,stabVec.getZ());
                    Location center = p.getLocation().clone().add(forward.multiply(radius));
                    w.spawnParticle(Particle.BUBBLE_COLUMN_UP,center, 100
                            ,radius/2
                            ,radius/2
                            ,radius/2
                            ,0.1);
                    Location pLoc = center.clone();
                    double padX = pLoc.getX();
                    double padY = pLoc.getY();
                    double padZ = pLoc.getZ();
                    double i = Math.PI;
                    for (int j = 0; j <= 100; j++) {
                        double x = padX + (radius * Math.sin(radius* i + 0.5 * j));
                        double z = padZ + (radius * Math.cos(radius * i + 0.5 * j));
                        Location areaP = new Location(w, x, padY, z);
                        BlockData data = Bukkit.createBlockData(Material.SEA_LANTERN);
                        w.spawnParticle(Particle.DUST_PILLAR,areaP,1,data);
                    }
                    boolean hit = false;
                    for(Entity e : w.getNearbyEntities(center,radius,radius,radius)){
                        if(center.distanceSquared(e.getLocation()) > radius)continue;
                        if(e instanceof LivingEntity l) {
                            if(e instanceof Player p1){
                                if(p1.getGameMode() == GameMode.SPECTATOR)continue;
                                if(p1 == p)continue;
                            }
                            l.damage(13,p);
                            w.spawnParticle(Particle.BUBBLE_COLUMN_UP,l.getLocation(), 50,0.5,1.5,0.5,0.1);
                            w.playSound(l.getEyeLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1, 1);
                            w.playSound(l.getEyeLocation(), Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1, 1);
                            hit = true;
                        }
                    }
                    if(hit){
                        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE,120,1,false));
                    }
                }
            };
            slam.runTaskLater(plugin,15L);
        }
    }
}
