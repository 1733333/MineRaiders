package Listeners;

import Universal.*;
import com.sun.nio.sctp.ShutdownNotification;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static Universal.Monsters.entityMinionMap;

public class MonsterListener implements Listener {
    JavaPlugin plugin;
    Kit k = Kit.INSTANCE;
    PlayerStats playerStats = PlayerStats.INSTANCE;
    DropPool dp = DropPool.INSTANCE;
    LootPool lp = LootPool.INSTANCE;
    GadgetPool gp = GadgetPool.INSTANCE;
    Random r = new Random();

    public MonsterListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void damageEvent(EntityDamageEvent damageEvent) {
        Entity damaged = damageEvent.getEntity();
        double damage = damageEvent.getFinalDamage();
        if (damaged instanceof LivingEntity l) {
            String name = l.getName();
            if (name.contains("伤害测试假人")) {
                damage = damageEvent.getDamage();
                if (damage > 0) {
                    for (Entity e : l.getNearbyEntities(20, 20, 20)) {
                        if(e instanceof Player p) {
                            p.sendMessage(ChatColor.GREEN
                                    + ""
                                    + ChatColor.BOLD
                                    + "造成伤害："
                                    + ChatColor.RED
                                    + ChatColor.BOLD
                                    + String.format("%.2f", damage));
                        }
                    }
                }
            }
            switch (name){
                case "§7堡垒底盘","§7堡垒炮塔","§c公爵","§c公爵引擎"-> {
                    if (damageEvent.getDamageSource().getDamageType() != DamageType.OUT_OF_WORLD) {
                        for (Entity passenger : l.getPassengers()) {
                            if (passenger instanceof LivingEntity p) {
                                p.damage(damage, DamageSource.builder(DamageType.OUT_OF_WORLD).build());
                            }
                        }
                        Entity vehicle = l.getVehicle();
                        if (vehicle instanceof LivingEntity v) {
                            v.damage(damage, DamageSource.builder(DamageType.OUT_OF_WORLD).build());
                        }
                    }
                }
            }
        }
    }
    @EventHandler
    public void mobShoot(ProjectileLaunchEvent launchEvent){
        Projectile pr = launchEvent.getEntity();
        ProjectileSource shooter = pr.getShooter();
        World w = pr.getWorld();
        if(shooter instanceof Snowman s){
            if(s.getCustomName() != null){
                launchEvent.setCancelled(true);
            }
        }
        if(shooter instanceof LivingEntity l){
            switch (l.getName()){
                case"§7堡垒炮塔":
                case"§c公爵引擎":
                    launchEvent.setCancelled(true);
            }
        }
    }
    @EventHandler(priority = EventPriority.LOWEST)
    public void mobMeleeAttack(EntityDamageByEntityEvent damageEvent) {
        DamageType damageType = damageEvent.getDamageSource().getDamageType();
        Entity a = damageEvent.getDamager();
        Entity d = damageEvent.getEntity();
        if(!(a instanceof Player)){
            if(a == d){
                damageEvent.setCancelled(true);
                return;
            }
        }
        if (a instanceof LivingEntity attacker) {
            String aName = attacker.getName();
            switch (aName) {
                case "§c粉碎者" -> {
                    if (!damageType.equals(DamageType.ARROW)) {
                        damageEvent.setCancelled(true);
                        damageEvent.setDamage(0);
                    }
                }
                case "§6火球"->{
                    if (!damageType.equals(DamageType.IN_FIRE)) {
                        damageEvent.setCancelled(true);
                        damageEvent.setDamage(0);
                    }
                }
                case "§7跳跃者" ->{
                    if (!damageType.equals(DamageType.EXPLOSION)) {
                        damageEvent.setCancelled(true);
                        damageEvent.setDamage(0);
                    }
                }
                case "§7堡垒底盘" ->{
                    damageEvent.setCancelled(true);
                    damageEvent.setDamage(0);
                }
                case "§a跳蚤" ->{
                    damageEvent.setCancelled(true);
                    damageEvent.setDamage(0);
                    Entity entity1 = damageEvent.getEntity();
                    if (entity1 instanceof Player p) {
                        if (!attacker.hasPotionEffect(PotionEffectType.LUCK)) {
                            p.addPassenger(attacker);
                            p.sendTitle(ChatColor.RED + "！被跳蚤缠上了！", ChatColor.RED + "使用 潜行键 挣脱", 10, 40, 10);
                            new BukkitRunnable() {
                                public void run() {
                                    if (p.getPassengers().isEmpty())
                                        cancel();
                                    p.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 40, 0));
                                    p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0));
                                }
                            }.runTaskTimer(plugin, 0L, 40L);
                        }
                    }
                }
            }
        }
        if (d instanceof LivingEntity damaged) {
            String dName = damaged.getName();
            switch (dName){
                case "§7跳跃者","§7堡垒底盘","§7堡垒炮塔"->{
                    if (damaged instanceof Mob m) {
                        if (a instanceof LivingEntity l) {
                            m.setTarget(l);
                        }
                    }
                }
            }
        }
    }
    @EventHandler
    public void entityDeath(EntityDeathEvent deathEvent){
        LivingEntity dead = deathEvent.getEntity();
        World w = dead.getWorld();
        String dName = dead.getName();
        switch (dName){
            case"§7堡垒底盘":
            case"§7堡垒炮塔":
            case"§e机魂":
            case"§e机魂推进器":
            case"§c机魂":
            case"§c机魂推进器":
            case"§c公爵":
            case"§c公爵引擎":
                if(!dead.getPassengers().isEmpty()){
                    for(Entity e : dead.getPassengers()){
                        if(e instanceof LivingEntity l){
                            l.setHealth(0);
                        }
                    }
                }
                if(dead.getVehicle() != null){
                    Entity v = dead.getVehicle();
                    if(v instanceof LivingEntity l){
                        l.setHealth(0);
                    }
                }
        }
        if(dName.equals("§c机魂")){
            List<Entity> minions = entityMinionMap.getOrDefault("§c公爵",new ArrayList<>());
            minions.remove(dead);
            entityMinionMap.put("§c公爵",minions);
        }
        if (dName.equals("§6火球")) {
            BukkitRunnable fire = new BukkitRunnable() {
                public void run() {
                    k.fire(dead, dead, 5, 3,3);
                }
            };
            fire.runTaskLater(plugin, 20L);
            w.spawnParticle(Particle.EXPLOSION, dead.getLocation(), 1);
            w.spawnParticle(Particle.FLAME, dead.getLocation(), 20, 0.0D, 0.0D, 0.0D, 0.3D);
        }
    }
    @EventHandler
    public void mobDeathSound(EntityDeathEvent deathEvent) {
        LivingEntity mob = deathEvent.getEntity();
        World w = mob.getWorld();
        String dName = mob.getName();
        switch (dName){
            case "§7堡垒底盘":
            case "§7堡垒炮塔":
            case "§7跳跃者":
            case "§6火球":
            case "§c粉碎者":
            case "§a跳蚤":
            case "§a爆爆":
            case "§e机魂":
            case "§c公爵":
                w.playSound(mob.getLocation(),Sound.ENTITY_BREEZE_DEATH,2,0.75f);
                w.playSound(mob.getLocation(),Sound.ENTITY_GENERIC_EXPLODE,2,1.5f);
        }
    }
    @EventHandler
    public void mobDeathDrops(EntityDeathEvent deathEvent) {
        LivingEntity mob = deathEvent.getEntity();
        World w = mob.getWorld();
        String dName = mob.getName();
        List<ItemStack> drops = new ArrayList<>();
        switch (dName){
            case "§c粉碎者"->{
                if(r.nextDouble() < 0.35) {
                    w.dropItem(mob.getLocation(),dp.shredderCore());
                }
                drops.add(dp.blazeRod());
                drops.add(gp.mendingPowder());
                drops.add(lp.i55());
                drops.add(lp.i55());
                drops.add(lp.i41());
                drops.add(lp.i41());
                drops.add(lp.i41());
            }
            case "§a跳蚤"->{
                if(r.nextDouble() < 0.35) {
                    w.dropItem(mob.getLocation(),dp.tickEye());
                }
                drops.add(dp.bone());
                drops.add(dp.bone());
                drops.add(lp.i19());
                drops.add(lp.i19());
            }
            case "§a爆爆"->{
                if(r.nextDouble() < 0.35) {
                    w.dropItem(mob.getLocation(),dp.popCore());
                }
                drops.add(lp.i19());
                drops.add(lp.i19());
                drops.add(dp.flint());
                drops.add(dp.flint());
            }
            case "§6火球"->{
                if(r.nextDouble() < 0.35) {
                    w.dropItem(mob.getLocation(),dp.fireballCore());
                }
                drops.add(lp.i19());
                drops.add(lp.i19());
                drops.add(lp.i19());
                drops.add(lp.i19());
                drops.add(lp.i19());
                drops.add(lp.i58());
                drops.add(dp.gunpowder());
                drops.add(dp.gunpowder());
            }
            case "§7告密者"->{
                if(r.nextDouble() < 0.35) {
                    w.dropItem(mob.getLocation(),dp.snitchScanner());
                }
                drops.add(lp.i20());
                drops.add(lp.i20());
                drops.add(lp.i41());
                drops.add(lp.i41());
            }
            case "§7跳跃者"->{
                if(r.nextDouble() < 0.35) {
                    w.dropItem(mob.getLocation(),dp.leaperUnit());
                }
                drops.add(lp.i43());
                drops.add(lp.i43());
                drops.add(lp.i55());
                drops.add(lp.i55());
                drops.add(dp.blazeRod());
                drops.add(gp.mendingPowder());
            }
            case "§7堡垒炮塔"->{
                if(r.nextDouble() < 0.35) {
                    w.dropItem(mob.getLocation(),dp.bastionCore());
                }
                drops.add(lp.i73());
                drops.add(lp.i73());
                drops.add(lp.i55());
                drops.add(lp.i55());
                drops.add(dp.breezeRod());
                drops.add(gp.mendingPowder());

            }
            case "§e机魂"->{

            }
            case "§c机魂"->{

            }
            case "§c公爵"->{
                w.dropItem(mob.getLocation(),dp.dukeCore());
                drops.add(gp.mendingPowder());
                drops.add(gp.mendingPowder());
                drops.add(gp.mendingPowder());
                drops.add(lp.i68());
                drops.add(lp.i68());
                drops.add(lp.i75());
                drops.add(lp.i75());
                drops.add(lp.i74());
                drops.add(dp.ironBlock());
            }
            default ->{
                switch (mob.getType()) {
                    case ZOMBIE -> drops.add(dp.rottenFlesh());
                    case SKELETON -> drops.add(dp.bone());
                    case CREEPER -> drops.add(dp.gunpowder());
                    case SPIDER -> {
                        drops.add(dp.string());
                        if (r.nextBoolean()) {
                            drops.add(dp.spiderEye());
                        }
                    }
                    case HUSK -> {
                        drops.add(dp.rottenFlesh());
                        drops.add(lp.i22());
                    }
                    case DROWNED -> {
                        drops.add(dp.rottenFlesh());
                        drops.add(lp.i18());
                    }
                    case ZOMBIE_VILLAGER -> {
                        drops.add(dp.rottenFlesh());
                        if(r.nextInt(4) == 0){
                            drops.add(lp.i57());
                        }
                    }
                    case ZOMBIFIED_PIGLIN -> {
                        drops.add(dp.rottenFlesh());
                        drops.add(lp.i21());
                    }
                    case BOGGED -> {
                        drops.add(dp.bone());
                        drops.add(dp.grass());
                    }
                    case STRAY -> {
                        drops.add(dp.bone());
                        if(r.nextInt(4) == 0){
                            drops.add(lp.i54());
                        }
                    }
                    case PARCHED -> {
                        drops.add(dp.bone());
                        drops.add(lp.i8());
                    }
                    case WITHER_SKELETON -> {
                        drops.add(dp.bone());
                        drops.add(lp.i41());
                    }
                    case CAVE_SPIDER -> {
                        drops.add(dp.string());
                        drops.add(dp.spiderEye());
                    }
                }
            }
        }
        deathEvent.getDrops().clear();
        for(ItemStack i : drops){
            w.dropItem(mob.getLocation(),i);
        }
    }
    @EventHandler
    public void damageSoundEffect(EntityDamageByEntityEvent damageEvent) {
        Entity e = damageEvent.getEntity();
        World w = e.getWorld();
        if(damageEvent.getDamage() > 3) {
            if (e instanceof LivingEntity d) {
                if(e == damageEvent.getDamager())return;
                String dName = d.getName();
                Sound s = switch (dName){
                    case "§7堡垒底盘" ->Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR;
                    case "§7堡垒炮塔" -> Sound.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR;
                    case "§7跳跃者" ->Sound.ENTITY_BLAZE_HURT;
                    case "§c粉碎者" ->Sound.ENTITY_WITHER_HURT;
                    case "§6火球" -> Sound.ITEM_SHIELD_BLOCK;
                    case "§e机魂" -> Sound.ENTITY_VEX_HURT;
                    case "§c公爵" ->Sound.ENTITY_WITHER_BREAK_BLOCK;
                    default -> Sound.UI_BUTTON_CLICK;
                };
                w.playSound(d.getLocation(),s,2,1);
                if(damageEvent.getDamager() instanceof Player p){
                    p.playSound(p.getLocation(),s,1,1);
                }
            }
        }
    }
    @EventHandler
    public void playerRidFlea(PlayerToggleSneakEvent sneakEvent) {
        Player p = sneakEvent.getPlayer();
        if (!p.getPassengers().isEmpty()) {
            for (Entity e : p.getPassengers()) {
                if (e.getName().equals("§a跳蚤")) {
                    p.eject();
                    e.setVelocity(p.getEyeLocation().getDirection().setY(0));
                    if (e instanceof LivingEntity l) {
                        l.addPotionEffect(new PotionEffect(PotionEffectType.LUCK,
                                100, 0));
                    }
                }
            }
        }
    }

    @EventHandler
    public void mobTarget(EntityTargetEvent targetEvent) {
        Entity e = targetEvent.getEntity();
        Entity target = targetEvent.getTarget();
        if (e instanceof LivingEntity mob) {
            if (mob.getName().equals("§a爆爆")) {
                mob.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,
                        100, 0));
            }
        }
        if(target instanceof Player p){
            if(playerStats.isDying(p)){
                targetEvent.setTarget(null);
            }
        }
        if(target instanceof Shulker s){
            if(s.isInvisible()){
                targetEvent.setTarget(null);
            }
        }
    }

    @EventHandler
    public void mobExplode(EntityExplodeEvent explodeEvent) {
        explodeEvent.setCancelled(true);
        Entity e = explodeEvent.getEntity();
        World w = e.getWorld();
        if (e instanceof LivingEntity mob) {
            if (mob.getName().equals("§a爆爆")) {
                k.explode(mob, mob, 10, 1, 5,1);
                w.spawnParticle(Particle.EXPLOSION_EMITTER, mob.getLocation(), 1);
                w.playSound(mob.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1, 1);
            }
        }
    }

    @EventHandler
    public void slimeSplit(SlimeSplitEvent splitEvent){
        Entity e = splitEvent.getEntity();
        if(e.getCustomName() != null){
            splitEvent.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void mobDamageReduction(EntityDamageEvent damageEvent){
        Entity e = damageEvent.getEntity();
        DamageSource source = damageEvent.getDamageSource();
        DamageType type = source.getDamageType();
        double damage = damageEvent.getFinalDamage();
        if(e instanceof LivingEntity l){
            String name = l.getCustomName();
            if(name != null){
                switch (name){
                    case "§e机魂"->{
                        if(type == DamageType.IN_WALL){
                            damageEvent.setDamage(0);
                            damageEvent.setCancelled(true);
                        }
                    }
                    case "§c公爵","§c公爵引擎"->{
                        if(type == DamageType.IN_WALL){
                            damageEvent.setDamage(0);
                            damageEvent.setCancelled(true);
                        }else{
                            damageEvent.setDamage(damage / 2);
                        }
                    }
                }
            }
        }
    }
}
