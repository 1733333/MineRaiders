package Listeners;

import Events.PlayerShieldAmountChangeEvent;
import Universal.*;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.data.BlockData;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class GadgetListener implements Listener {
    JavaPlugin plugin;
    Kit k = Kit.INSTANCE;
    Random r = new Random();
    ArmorPool ap = ArmorPool.INSTANCE;
    BoxPool bp = BoxPool.INSTANCE;
    GadgetPool gp = GadgetPool.INSTANCE;
    LootPool lp = LootPool.INSTANCE;
    Recipes rp = Recipes.INSTANCE;
    WeaponPool wp = WeaponPool.INSTANCE;
    PlayerStats playerStats = PlayerStats.INSTANCE;
    HashSet<Player>isPlaying = new HashSet<>();
    HashSet<Player> isChargingShield = new HashSet<>();
    HashMap<String,BukkitRunnable> playerTask = new HashMap<>();
    int[]musicScore1 = new int[]{
            8,
            0,0,
            8,
            0,0,0,0,
            11,
            0,0,0,0,0,
            15,
            0,0,0,0,0,
            11,
            0,0,
            13,
            0,0,0,0,0,0,0,0,0,0,0,0,
            11,
            0,
            13,
            0,
            11,
            0,0,0,
            13,
            0,0,0,0,0,
            15,
            0,0,0,0,0,
            8,
            0,0,
            8,
    };
    int[]musicScore2 = new int[]{};

    public void setPlugin(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void armorStandDeath(EntityDeathEvent deathEvent) {
        Entity e = deathEvent.getEntity();
        if (e instanceof ArmorStand a) {
            if (a.getCustomName() != null) {
                deathEvent.getDrops().clear();
            }
        }
    }
    @EventHandler
    public void playerInteract(PlayerInteractEvent interactEvent) {
        Action action = interactEvent.getAction();
        Player p = interactEvent.getPlayer();
        if(playerStats.isDying(p)){
            interactEvent.setCancelled(true);
            return;
        }
        if (p.getGameMode() == GameMode.SPECTATOR) return;
        ItemStack hand = p.getInventory().getItemInMainHand();
        if(hand.getType() == Material.NAME_TAG){
            interactEvent.setCancelled(true);
        }
        ItemStack offHand = p.getInventory().getItemInOffHand();
        boolean rightClick = action.equals(Action.RIGHT_CLICK_AIR)
                || action.equals(Action.RIGHT_CLICK_BLOCK);
        String tag = k.getLore(hand);
        String tag1 = k.getLore(offHand);
        if (rightClick) {
            if (offHand.getType() != Material.AIR) {
                switch (tag1) {
                    case "§f可以修复物品的粉末":
                        mendingPowder(p,offHand);
                        interactEvent.setCancelled(true);
                        break;
                }
            } else if (hand.getType() != Material.AIR) {
                switch (tag) {
                    case "§f破片手雷":
                        grenade(p, hand);
                        interactEvent.setCancelled(true);
                        break;
                    case "§f火焰手雷":
                        fireGrenade(p, hand);
                        interactEvent.setCancelled(true);
                        break;
                    case "§f火球燃烧炉":
                        smallFireGrenade(p, hand);
                        interactEvent.setCancelled(true);
                        break;
                    case "§f毒气手雷":
                        gasGrenade(p, hand);
                        interactEvent.setCancelled(true);
                        break;
                    case "§f烟雾手雷":
                        smokeGrenade(p, hand);
                        interactEvent.setCancelled(true);
                        break;
                    case "§f紊乱手雷":
                        glitchGrenade(p, hand);
                        interactEvent.setCancelled(true);
                        break;
                    case "§f诱饵手雷":
                        baitGrenade(p, hand);
                        interactEvent.setCancelled(true);
                        break;
                    case "§f察觉之锣":
                        soulCamp(p, hand);
                        interactEvent.setCancelled(true);
                        break;
                    case "§f狂欢之锣":
                        fireCamp(p, hand);
                        interactEvent.setCancelled(true);
                        break;
                    case "§f荒野大笛客":
                        interactEvent.setCancelled(true);
                        flute(p);
                        break;
                    case "§f狼群":
                        wolfPack(p, hand);
                        interactEvent.setCancelled(true);
                        break;
                    case "§f跳跃者脉冲单元":
                        leaperUnit(p, hand);
                        interactEvent.setCancelled(true);
                        break;
                }
                if (tag.contains("收纳盒")) {
                    openBox(p, hand, tag);
                    interactEvent.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void playerConsume(PlayerItemConsumeEvent consumeEvent) {
        Player p = consumeEvent.getPlayer();
        if(playerStats.isDying(p)){
            consumeEvent.setCancelled(true);
            return;
        }
        ItemStack item = consumeEvent.getItem();
        String tag = k.getLore(item);
        double shield = playerStats.getShield(p);
        switch (tag) {
            case "§f爆炸地雷":
                explodeMine(p);
                break;
            case "§f火焰地雷":
                pyroMine(p);
                break;
            case "§f毒气地雷":
                gasMine(p);
                break;
            case "§f电击地雷":
                shockMine(p);
                break;
            case "§f霜雪图腾":
                snowGolem(p);
                break;
            case "§f钢铁图腾":
                ironGolem(p);
                break;
            case "§f狩猎图腾":
                wolfGolem(p,consumeEvent);
                break;
            case "§f瘟疫图腾":
                zombieGolem(p, item);
                break;
            case "§f压缩浓汤":
                p.setFoodLevel(20);
                p.setSaturation(20);
                break;
            case "§f贪婪肉排":
                p.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, 1200, 4));
                BukkitRunnable later = new BukkitRunnable() {
                    @Override
                    public void run() {
                        p.setFoodLevel(0);
                    }
                };
                later.runTaskLater(plugin, 1L);
                break;
            case "§f生命针剂":
                p.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 100, 1));
                p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 1));
                p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 4));
                p.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 100, 1));
                break;
            case "§f肾上腺素":
                if(p.getHealth() <= 5){
                    Bukkit.broadcastMessage(ChatColor.RED + p.getName() + "飞升到了肾上腺素星球");
                }
                p.damage(5, DamageSource.builder(DamageType.MAGIC).build());
                p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 400, 1));
                p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 400, 1));
                break;
            case "§f铜质电池":
                if(shield == -1 || shield == 20){
                    consumeEvent.setCancelled(true);
                }else {
                    p.setCooldown(item, 160);
                    battery(p, 8, 8,item);
                }
                break;
            case "§f铁质电池":
                if(shield == -1 || shield == 20){
                    consumeEvent.setCancelled(true);
                }else {
                    p.setCooldown(item, 80);
                    battery(p, 4, 8,item);
                }
                break;
            case "§f黄金电池":
                if(shield == -1 || shield == 20){
                    consumeEvent.setCancelled(true);
                }else {
                    p.setCooldown(item, 120);
                    battery(p, 6, 12,item);
                }
                break;
            case "§f钻石电池":
                if(shield == -1 || shield == 20){
                    consumeEvent.setCancelled(true);
                }else {
                    p.setCooldown(item, 80);
                    battery(p, 4, 16,item);
                }
                break;
            case "§f下界电池":
                if(shield == 20){
                    consumeEvent.setCancelled(true);
                }else {
                    p.setCooldown(item, 80);
                    Bukkit.getPluginManager().callEvent(new PlayerShieldAmountChangeEvent(p, 20));
                }
                break;
            case "§f死线":
                deadline(p);
                break;
        }
    }

    public void openBox(Player p, ItemStack hand, String tag) {
        World w = p.getWorld();
        String box = tag.substring(0, 4);
        ItemStack[] loots;
        int amount = 1;
        switch (box) {
            case "§f食物":
                loots = bp.getFoods();
                break;
            case "§f植物":
                loots = bp.getPlants();
                break;
            case "§f树苗":
                loots = bp.getSaplings();
                break;
            case "§f海洋":
                loots = bp.getSea();
                break;
            case "§f唱片":
                loots = bp.getDiscs();
                break;
            case "§f道具":
                loots = gp.getGadgets();
                break;
            case "§f武器":
                loots = wp.getBoxWeapons();
                break;
            case "§f盔甲":
                loots = ap.getContainerArmors();
                amount = 2;
                break;
            case "§f魔咒":
                loots = bp.getEnchantedBooks();
                amount = 2;
                break;
            case "§f陶片":
                loots = bp.getPotteries();
                break;
            case "§f号角":
                loots = bp.getHorns();
                break;
            case "§f纹饰":
                loots = bp.getPatterns();
                break;
            case "§f钥匙":
                loots = lp.getKeys();
                break;
            case "§f配方":
                loots = rp.getBoxRecipeBooks();
                break;
            default:
                return;
        }
        if (loots.length > 0) {
            w.playSound(p, Sound.BLOCK_SHULKER_BOX_OPEN, 1, 1);
            for (int i = 0; i < amount; i++) {
                w.dropItem(p.getLocation(), loots[r.nextInt(loots.length)]);
            }
            if (!p.getGameMode().equals(GameMode.CREATIVE)) {
                int count = hand.getAmount();
                hand.setAmount(count - 1);
            }
        }
    }

    public void snowGolem(Player p) {
        World w = p.getWorld();
        w.playSound(p.getLocation(), Sound.BLOCK_SNOW_BREAK, 1, 1);
        w.playSound(p.getLocation(), Sound.BLOCK_SNOW_BREAK, 1, 1);
        w.playSound(p.getLocation(), Sound.BLOCK_SNOW_BREAK, 1, 1);
        double maxHealth = 20;
        Snowman turret = (Snowman) w.spawnEntity(p.getLocation(), EntityType.SNOW_GOLEM);
        turret.getAttribute(Attribute.ATTACK_DAMAGE.MOVEMENT_SPEED).setBaseValue(0);
        turret.getAttribute(Attribute.ATTACK_DAMAGE.KNOCKBACK_RESISTANCE).setBaseValue(1);
        turret.getAttribute(Attribute.ATTACK_DAMAGE.MAX_HEALTH).setBaseValue(maxHealth);
        turret.setHealth(maxHealth);
        turret.setSilent(true);
        turret.setCustomName(ChatColor.AQUA + p.getName() + "的炮塔");
        turret.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, PotionEffect.INFINITE_DURATION, 0));
        BukkitRunnable target = new BukkitRunnable() {
            @Override
            public void run() {
                if (turret.isDead()) {
                    this.cancel();
                    return;
                }
                w.playSound(turret.getEyeLocation(), Sound.BLOCK_STONE_PRESSURE_PLATE_CLICK_ON, 1, 1);
                for (Entity e : turret.getNearbyEntities(10, 10, 10)) {
                    if (e instanceof Player p1) {
                        if (p1 == p) continue;
                    }
                    if (e instanceof LivingEntity l) {
                        if (l.hasPotionEffect(PotionEffectType.INVISIBILITY)) continue;
                        Vector pEye = turret.getEyeLocation().toVector();
                        Vector lEye = l.getEyeLocation().toVector();
                        Vector ray = lEye.clone().subtract(pEye);
                        RayTraceResult result = w.rayTraceBlocks(turret.getEyeLocation(), ray, k.distance(turret, e));
                        if (result == null) {
                            turret.setTarget(l);
                            w.playSound(turret.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 1, 1);
                            break;
                        }
                    }
                }
                Entity target = turret.getTarget();
                if (target != null) {
                    if (k.distance(target, turret) > 10) {
                        turret.setTarget(null);
                    }
                }
            }
        };
        BukkitRunnable shoot = new BukkitRunnable() {
            @Override
            public void run() {
                if (turret.isDead()) {
                    this.cancel();
                    return;
                }
                turret.damage(maxHealth * 0.04);
                if (turret.getTarget() != null) {
                    w.playSound(turret.getEyeLocation(), Sound.ENTITY_EGG_THROW, 1, 1);
                    Vector shootVec = turret.getEyeLocation().getDirection();
                    Snowball ball = (Snowball) w.spawnEntity(turret.getEyeLocation(), EntityType.SNOWBALL);
                    ball.setVelocity(shootVec.multiply(1));
                    BukkitRunnable hit = new BukkitRunnable() {
                        @Override
                        public void run() {
                            w.spawnParticle(Particle.ITEM, ball.getLocation(), 0, new ItemStack(Material.SNOWBALL));
                            if (ball.isDead()) {
                                this.cancel();
                                return;
                            }
                            for (Entity e : ball.getNearbyEntities(1, 1, 1)) {
                                if (e instanceof LivingEntity l) {
                                    if (e == turret) continue;
                                    if (e instanceof Player p1) {
                                        if (p1 == p) continue;
                                        if (p1.getGameMode() == GameMode.SPECTATOR) continue;
                                    }
                                    l.damage(1, DamageSource.builder(DamageType.FREEZE)
                                            .withCausingEntity(turret)
                                            .withDirectEntity(p).build());
                                    l.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20, 2));
                                    w.playSound(l.getLocation(), Sound.BLOCK_SNOW_BREAK, 1, 1);
                                    this.cancel();
                                    break;
                                }
                            }
                        }
                    };
                    hit.runTaskTimer(plugin, 0L, 1L);
                }
            }
        };
        target.runTaskTimer(plugin, 0L, 100L);
        shoot.runTaskTimer(plugin, 0L, 20L);
    }

    public void ironGolem(Player p) {
        World w = p.getWorld();
        IronGolem g = (IronGolem) w.spawnEntity(p.getLocation(), EntityType.IRON_GOLEM);
        w.playSound(p.getLocation(), Sound.BLOCK_IRON_BREAK, 1, 1);
        w.playSound(p.getLocation(), Sound.BLOCK_IRON_BREAK, 1, 1);
        w.playSound(p.getLocation(), Sound.BLOCK_IRON_BREAK, 1, 1);
        g.setPlayerCreated(true);
        g.setCustomName(ChatColor.AQUA + p.getName() + "的铁傀儡");
        double maxHealth = 100;
        g.getAttribute(Attribute.ATTACK_DAMAGE.MAX_HEALTH).setBaseValue(maxHealth);
        BukkitRunnable damage = new BukkitRunnable() {
            @Override
            public void run() {
                if (g.isDead()) {
                    this.cancel();
                    return;
                }
                g.damage(maxHealth * 0.04);
            }
        };
        damage.runTaskTimer(plugin, 0L, 20L);
    }

    public void wolfGolem(Player p,PlayerItemConsumeEvent consumeEvent) {
        World w = p.getWorld();
        w.playSound(p.getLocation(), Sound.ENTITY_SKELETON_DEATH, 1, 1);
        Player nearest = null;
        double nearestDistanceSquared = Double.MAX_VALUE;
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.equals(p)) continue;
            double distanceSquared = k.distance(p, online);
            if (distanceSquared < nearestDistanceSquared) {
                nearest = online;
                nearestDistanceSquared = distanceSquared;
            }
        }
        if (nearest != null) {
            Player p1 = nearest;
            if(p.hasPotionEffect(PotionEffectType.UNLUCK)) {
                PotionEffect effect = p.getPotionEffect(PotionEffectType.UNLUCK);
                int duration = effect.getDuration();
                p.addPotionEffect(new PotionEffect(PotionEffectType.UNLUCK,1200 + duration,0));
            }else {
                p.addPotionEffect(new PotionEffect(PotionEffectType.UNLUCK, 1200, 0));
                BukkitRunnable chase = new BukkitRunnable() {
                    int count = 0;
                    @Override
                    public void run() {
                        double distance = k.distance(p,p1);
                        if(!p.hasPotionEffect(PotionEffectType.UNLUCK) || distance <= 5){
                            if(distance <= 5){
                                p1.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING,200,0));
                                p.removePotionEffect(PotionEffectType.UNLUCK);
                            }
                            this.cancel();
                            return;
                        }
                        Location sLoc = p.getEyeLocation();
                        Vector sVec = sLoc.getDirection();
                        EnderSignal s = (EnderSignal) w.spawnEntity(sLoc.add(sVec.multiply(2)),EntityType.EYE_OF_ENDER);
                        s.setItem(new ItemStack(Material.SKELETON_SKULL));
                        Vector pVec = p.getEyeLocation().toVector();
                        Vector p1Vec = p1.getEyeLocation().toVector();
                        Vector vec = (p1Vec.subtract(pVec)).normalize();
                        s.setVelocity(vec.multiply(0.25));
                        s.setGlowing(true);
                        for(Player p2 : w.getPlayers()){
                            if(p2 == p)continue;
                            p2.hideEntity(plugin,s);
                        }
                        BukkitRunnable remove = new BukkitRunnable() {
                            @Override
                            public void run() {
                                s.remove();
                            }
                        };
                        remove.runTaskLater(plugin,20L);
                        count += 1;
                    }
                };
                chase.runTaskTimer(plugin,0L,40L);
            }
        }else {
            consumeEvent.setCancelled(true);
        }
    }



    public void zombieGolem(Player p,ItemStack hand) {
        World w = p.getWorld();
        Material material = hand.getType();
        if (p.getCooldown(material) == 0) {
            p.setCooldown(material, 20);
            Location shootLoc = p.getEyeLocation();
            Vector shootVec = shootLoc.getDirection();
            Snowball nade = (Snowball) w.spawnEntity(shootLoc, EntityType.SNOWBALL);
            nade.setVelocity(shootVec.multiply(2));
            nade.setItem(hand);
            nade.setShooter(p);
            nade.setGlowing(true);
            w.playSound(shootLoc, Sound.ENTITY_EGG_THROW, 1, 1);
            BukkitRunnable land = new BukkitRunnable() {
                @Override
                public void run() {
                    Particle.DustOptions dust = new Particle.DustOptions(Color.LIME, 1);
                    w.spawnParticle(Particle.DUST, nade.getLocation(), 0, dust);
                    if (nade.isDead() || k.hitBallBlock(nade)) {
                        this.cancel();
                        nade.remove();
                        w.spawnParticle(Particle.EXPLOSION, nade.getLocation(), 1);
                        for (int i = 0; i < 2; i++) {
                            Zombie z = (Zombie) w.spawnEntity(nade.getLocation(), EntityType.ZOMBIE);
                            z.setBaby();
                        }
                    }
                }
            };
            land.runTaskTimer(plugin, 2L, 1L);
        }
    }

    public void grenade(Player p, ItemStack hand) {
        World w = p.getWorld();
        Material material = hand.getType();
        if (p.getCooldown(material) == 0) {
            p.setCooldown(material, 20);
            if (p.getGameMode() != GameMode.CREATIVE) {
                int amount = hand.getAmount();
                hand.setAmount(amount - 1);
            }
            Location shootLoc = p.getEyeLocation();
            Vector shootVec = shootLoc.getDirection();
            Snowball nade = (Snowball) w.spawnEntity(shootLoc, EntityType.SNOWBALL);
            nade.setVelocity(shootVec.multiply(2));
            nade.setItem(hand);
            nade.setShooter(p);
            nade.setGlowing(true);
            w.playSound(shootLoc, Sound.ENTITY_EGG_THROW, 1, 1);
            BukkitRunnable hit = new BukkitRunnable() {
                @Override
                public void run() {
                    w.spawnParticle(Particle.CRIT, nade.getLocation(), 0);
                    if (nade.isDead()) {
                        nade.remove();
                        w.spawnParticle(Particle.EXPLOSION, nade.getLocation(), 1);
                        BukkitRunnable flash = new BukkitRunnable() {
                            int count = 0;
                            @Override
                            public void run() {
                                if(count > 5){
                                    this.cancel();
                                    return;
                                }
                                w.spawnParticle(Particle.FLASH, nade.getLocation(), 1,Color.RED);
                                count += 1;
                            }
                        };
                        BukkitRunnable later = new BukkitRunnable() {
                            @Override
                            public void run() {
                                w.spawnParticle(Particle.EXPLOSION_EMITTER, nade.getLocation(), 1);
                                k.explode(p, nade, 18, 1.5, 5,1);
                                for (int i = 0; i < 20; i++) {
                                    Vector shootVec = new Vector(r.nextDouble() - r.nextDouble(),
                                            r.nextDouble() - r.nextDouble(), r.nextDouble() - r.nextDouble());
                                    Arrow a = w.spawnArrow(nade.getLocation(), shootVec, 1, 0);
                                    a.setDamage(5);
                                    a.setShooter(p);
                                    a.setTicksLived(1200);
                                }
                                w.playSound(nade.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 2, 1);
                            }
                        };
                        later.runTaskLater(plugin, 30L);
                        flash.runTaskTimer(plugin,0L,6L);
                        this.cancel();
                    }
                }
            };
            hit.runTaskTimer(plugin, 2L, 1L);
        }
    }
    public void gasGrenade(Player p, ItemStack hand) {
        World w = p.getWorld();
        Material material = hand.getType();
        if (p.getCooldown(material) == 0) {
            p.setCooldown(material, 20);
            if (p.getGameMode() != GameMode.CREATIVE) {
                int amount = hand.getAmount();
                hand.setAmount(amount - 1);
            }
            Location shootLoc = p.getEyeLocation();
            Vector shootVec = shootLoc.getDirection();
            Snowball nade = (Snowball) w.spawnEntity(shootLoc, EntityType.SNOWBALL);
            nade.setVelocity(shootVec.multiply(2));
            nade.setItem(hand);
            nade.setShooter(p);
            nade.setGlowing(true);
            w.playSound(shootLoc, Sound.ENTITY_EGG_THROW, 1, 1);
            BukkitRunnable land = new BukkitRunnable() {
                @Override
                public void run() {
                    Particle.DustOptions dust = new Particle.DustOptions(Color.LIME, 1);
                    w.spawnParticle(Particle.DUST, nade.getLocation(), 0, dust);
                    if (nade.isDead()) {
                        this.cancel();
                        w.playSound(nade.getLocation(), Sound.BLOCK_LANTERN_BREAK, 2, 1);
                        w.spawnParticle(Particle.EXPLOSION,nade.getLocation(),1);
                        k.gas(p, nade, 10,4);
                    }
                }
            };
            land.runTaskTimer(plugin, 0L, 1L);
        }
    }
    public void fireGrenade(Player p, ItemStack hand) {
        World w = p.getWorld();
        Material material = hand.getType();
        if (p.getCooldown(material) == 0) {
            p.setCooldown(material, 20);
            if (p.getGameMode() != GameMode.CREATIVE) {
                int amount = hand.getAmount();
                hand.setAmount(amount - 1);
            }
            Location shootLoc = p.getEyeLocation();
            Vector shootVec = shootLoc.getDirection();
            Snowball nade = (Snowball) w.spawnEntity(shootLoc, EntityType.SNOWBALL);
            nade.setVelocity(shootVec.multiply(2));
            nade.setItem(hand);
            nade.setShooter(p);
            nade.setGlowing(true);
            w.playSound(shootLoc, Sound.ENTITY_EGG_THROW, 1, 1);
            BukkitRunnable land = new BukkitRunnable() {
                @Override
                public void run() {
                    w.spawnParticle(Particle.FLAME, nade.getLocation(), 0);
                    if (nade.isDead()) {
                        this.cancel();
                        w.playSound(nade.getLocation(), Sound.BLOCK_GLASS_BREAK, 2, 1);
                        w.playSound(nade.getLocation(), Sound.BLOCK_GLASS_BREAK, 2, 1);
                        w.spawnParticle(Particle.EXPLOSION,nade.getLocation(),1);
                        k.fire(p, nade, 7,3,4);
                    }
                }
            };
            land.runTaskTimer(plugin, 0L, 1L);
        }
    }
    public void smallFireGrenade(Player p, ItemStack hand) {
        World w = p.getWorld();
        Material material = hand.getType();
        if (p.getCooldown(material) == 0) {
            p.setCooldown(material, 20);
            if (p.getGameMode() != GameMode.CREATIVE) {
                int amount = hand.getAmount();
                hand.setAmount(amount - 1);
            }
            Location shootLoc = p.getEyeLocation();
            Vector shootVec = shootLoc.getDirection();
            Snowball nade = (Snowball) w.spawnEntity(shootLoc, EntityType.SNOWBALL);
            nade.setVelocity(shootVec.multiply(2));
            nade.setItem(hand);
            nade.setShooter(p);
            nade.setGlowing(true);
            w.playSound(shootLoc, Sound.ENTITY_EGG_THROW, 1, 1);
            BukkitRunnable land = new BukkitRunnable() {
                @Override
                public void run() {
                    w.spawnParticle(Particle.FLAME, nade.getLocation(), 0);
                    if (nade.isDead()) {
                        this.cancel();
                        w.playSound(nade.getLocation(), Sound.BLOCK_GLASS_BREAK, 2, 1);
                        w.playSound(nade.getLocation(), Sound.BLOCK_GLASS_BREAK, 2, 1);
                        w.spawnParticle(Particle.EXPLOSION,nade.getLocation(),1);
                        k.fire(p, nade, 5,1,2);
                    }
                }
            };
            land.runTaskTimer(plugin, 0L, 1L);
        }
    }
    public void smokeGrenade(Player p, ItemStack hand) {
        World w = p.getWorld();
        Material material = hand.getType();
        if (p.getCooldown(material) == 0) {
            p.setCooldown(material, 20);
            if (p.getGameMode() != GameMode.CREATIVE) {
                int amount = hand.getAmount();
                hand.setAmount(amount - 1);
            }
            Location shootLoc = p.getEyeLocation();
            Vector shootVec = shootLoc.getDirection();
            Snowball nade = (Snowball) w.spawnEntity(shootLoc, EntityType.SNOWBALL);
            nade.setVelocity(shootVec.multiply(2));
            nade.setItem(hand);
            nade.setShooter(p);
            nade.setGlowing(true);
            w.playSound(shootLoc, Sound.ENTITY_EGG_THROW, 1, 1);
            BukkitRunnable land = new BukkitRunnable() {
                @Override
                public void run() {
                    w.spawnParticle(Particle.CRIT, nade.getLocation(), 0);
                    if (nade.isDead()) {
                        this.cancel();
                        w.playSound(nade.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 2, 1);
                        k.smoke(nade, 20, 4);
                    }
                }
            };
            land.runTaskTimer(plugin, 0L, 1L);
        }
    }
    public void glitchGrenade(Player p, ItemStack hand) {
        World w = p.getWorld();
        Material material = hand.getType();
        if (p.getCooldown(material) == 0) {
            p.setCooldown(material, 20);
            if (p.getGameMode() != GameMode.CREATIVE) {
                int amount = hand.getAmount();
                hand.setAmount(amount - 1);
            }
            Location shootLoc = p.getEyeLocation();
            Vector shootVec = shootLoc.getDirection();
            Snowball nade = (Snowball) w.spawnEntity(shootLoc, EntityType.SNOWBALL);
            nade.setVelocity(shootVec.multiply(2));
            nade.setItem(hand);
            nade.setShooter(p);
            nade.setGlowing(true);
            w.playSound(shootLoc, Sound.ENTITY_EGG_THROW, 1, 1);
            BukkitRunnable land = new BukkitRunnable() {
                @Override
                public void run() {
                    w.spawnParticle(Particle.CRIT, nade.getLocation(), 0);
                    if (nade.isDead()) {
                        this.cancel();
                        w.playSound(nade.getLocation(), Sound.ENTITY_ARMOR_STAND_BREAK, 2, 1);
                        k.glitch(p,nade,3,4);
                    }
                }
            };
            land.runTaskTimer(plugin, 0L, 1L);
        }
    }
    public void baitGrenade(Player p, ItemStack hand) {
        World w = p.getWorld();
        Material material = hand.getType();
        if (p.getCooldown(material) == 0) {
            p.setCooldown(material, 20);
            if (p.getGameMode() != GameMode.CREATIVE) {
                int amount = hand.getAmount();
                hand.setAmount(amount - 1);
            }
            Location shootLoc = p.getEyeLocation();
            Vector shootVec = shootLoc.getDirection();
            Snowball nade = (Snowball) w.spawnEntity(shootLoc, EntityType.SNOWBALL);
            nade.setVelocity(shootVec.multiply(2));
            nade.setItem(hand);
            nade.setShooter(p);
            nade.setGlowing(true);
            w.playSound(shootLoc, Sound.ENTITY_EGG_THROW, 1, 1);
            BukkitRunnable land = new BukkitRunnable() {
                @Override
                public void run() {
                    w.spawnParticle(Particle.CRIT, nade.getLocation(), 0);
                    if (nade.isDead() || k.hitBallBlock(nade)) {
                        this.cancel();
                        nade.remove();
                        k.bait(p,nade,40,0.08);
                    }
                }
            };
            land.runTaskTimer(plugin, 3L, 1L);
        }
    }
    public void explodeMine(Player p) {
        World w = p.getWorld();
        Location shootLoc = p.getEyeLocation();
        Vector shootVec = shootLoc.getDirection().normalize();
        ArmorStand g = (ArmorStand) w.spawnEntity(shootLoc, EntityType.ARMOR_STAND);
        if(!p.isSneaking()) {
            g.setVelocity(shootVec.multiply(0.5));
        }
        w.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_IRON, 1, 1);
        w.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_IRON, 1, 1);
        w.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_IRON, 1, 1);
        g.setCustomName(ChatColor.AQUA + p.getName() + "的爆炸地雷");
        g.setSmall(true);
        g.getEquipment().setHelmet(new ItemStack(Material.CREEPER_HEAD));
        g.getEquipment().setChestplate(ap.mobChest(Color.LIME));
        g.getEquipment().setLeggings(ap.mobLeg(Color.LIME));
        g.getEquipment().setBoots(ap.mobBoot(Color.LIME));
        BukkitRunnable trigger = new BukkitRunnable() {
            @Override
            public void run() {
                if (g.isDead() || g.hasPotionEffect(PotionEffectType.LUCK)) {
                    this.cancel();
                    return;
                }
                for (Entity e : g.getNearbyEntities(5, 5, 5)) {
                    if (e instanceof Player p1) {
                        if (p1 == p) continue;
                    }
                    if (e instanceof LivingEntity l) {
                        if (l.hasPotionEffect(PotionEffectType.INVISIBILITY)) continue;
                        if (l instanceof ArmorStand) continue;
                        Vector pEye = g.getEyeLocation().toVector();
                        Vector lEye = l.getEyeLocation().toVector();
                        Vector ray = lEye.clone().subtract(pEye);
                        RayTraceResult result = w.rayTraceBlocks(g.getEyeLocation(), ray, k.distance(g, e));
                        if (result == null) {
                            w.playSound(g, Sound.BLOCK_ANVIL_PLACE, 1, 2);
                            g.setVelocity(new Vector(0, 0.5, 0));
                            g.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, PotionEffect.INFINITE_DURATION, 0));
                            g.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, PotionEffect.INFINITE_DURATION, 0));
                            BukkitRunnable explode = new BukkitRunnable() {
                                @Override
                                public void run() {
                                    k.explode(p, g, 12, 0, 5, 0);
                                    w.playSound(g.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1, 1);
                                    w.spawnParticle(Particle.EXPLOSION_EMITTER, g.getLocation(), 1);
                                    g.remove();
                                }
                            };
                            explode.runTaskLater(plugin, 10L);
                            break;
                        }
                    }
                }
            }
        };
        trigger.runTaskTimer(plugin, 30L, 20L);
    }
    public void pyroMine(Player p) {
        World w = p.getWorld();
        Location shootLoc = p.getEyeLocation();
        Vector shootVec = shootLoc.getDirection().normalize();
        ArmorStand g = (ArmorStand) w.spawnEntity(shootLoc, EntityType.ARMOR_STAND);
        if(!p.isSneaking()) {
            g.setVelocity(shootVec.multiply(0.5));
        }
        w.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_IRON, 1, 1);
        w.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_IRON, 1, 1);
        w.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_IRON, 1, 1);
        g.setCustomName(ChatColor.AQUA + p.getName() + "的火焰地雷");
        g.setSmall(true);
        g.getEquipment().setHelmet(new ItemStack(Material.PIGLIN_HEAD));
        g.getEquipment().setChestplate(ap.mobChest(Color.ORANGE));
        g.getEquipment().setLeggings(ap.mobLeg(Color.ORANGE));
        g.getEquipment().setBoots(ap.mobBoot(Color.ORANGE));
        BukkitRunnable trigger = new BukkitRunnable() {
            @Override
            public void run() {
                if (g.isDead() || g.hasPotionEffect(PotionEffectType.LUCK)) {
                    this.cancel();
                    return;
                }
                for (Entity e : g.getNearbyEntities(5, 5, 5)) {
                    if (e instanceof Player p1) {
                        if (p1 == p) continue;
                    }
                    if (e instanceof LivingEntity l) {
                        if (l.hasPotionEffect(PotionEffectType.INVISIBILITY)) continue;
                        if (l instanceof ArmorStand) continue;
                        Vector pEye = g.getEyeLocation().toVector();
                        Vector lEye = l.getEyeLocation().toVector();
                        Vector ray = lEye.clone().subtract(pEye);
                        RayTraceResult result = w.rayTraceBlocks(g.getEyeLocation(), ray, k.distance(g, e));
                        if (result == null) {
                            w.playSound(g, Sound.BLOCK_ANVIL_PLACE, 1, 2);
                            g.setVelocity(new Vector(0, 0.5, 0));
                            g.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, PotionEffect.INFINITE_DURATION, 0));
                            g.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, PotionEffect.INFINITE_DURATION, 0));
                            BukkitRunnable explode = new BukkitRunnable() {
                                @Override
                                public void run() {
                                    k.explode(p, g, 4, 0, 4, 0);
                                    k.fire(p, g, 5, 5, 4);
                                    w.playSound(g.getLocation(), Sound.ITEM_CROSSBOW_SHOOT, 1, 1);
                                    w.playSound(g.getLocation(), Sound.ENTITY_ARMOR_STAND_BREAK, 1, 1);
                                    w.spawnParticle(Particle.EXPLOSION, g.getLocation(), 1);
                                    g.remove();
                                }
                            };
                            explode.runTaskLater(plugin, 10L);
                            break;
                        }
                    }
                }
            }
        };
        trigger.runTaskTimer(plugin, 30L, 20L);
    }
    public void gasMine(Player p) {
        World w = p.getWorld();
        Location shootLoc = p.getEyeLocation();
        Vector shootVec = shootLoc.getDirection().normalize();
        ArmorStand g = (ArmorStand) w.spawnEntity(shootLoc, EntityType.ARMOR_STAND);
        if(!p.isSneaking()) {
            g.setVelocity(shootVec.multiply(0.5));
        }
        w.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_IRON, 1, 1);
        w.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_IRON, 1, 1);
        w.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_IRON, 1, 1);
        g.setCustomName(ChatColor.AQUA + p.getName() + "的毒气地雷");
        g.setSmall(true);
        g.getEquipment().setHelmet(new ItemStack(Material.ZOMBIE_HEAD));
        g.getEquipment().setChestplate(ap.mobChest(Color.GREEN));
        g.getEquipment().setLeggings(ap.mobLeg(Color.GREEN));
        g.getEquipment().setBoots(ap.mobBoot(Color.GREEN));
        BukkitRunnable trigger = new BukkitRunnable() {
            @Override
            public void run() {
                if (g.isDead() || g.hasPotionEffect(PotionEffectType.LUCK)) {
                    this.cancel();
                    return;
                }
                for (Entity e : g.getNearbyEntities(5, 5, 5)) {
                    if (e instanceof Player p1) {
                        if (p1 == p) continue;
                    }
                    if (e instanceof LivingEntity l) {
                        if (l.hasPotionEffect(PotionEffectType.INVISIBILITY)) continue;
                        if (l instanceof ArmorStand) continue;
                        Vector pEye = g.getEyeLocation().toVector();
                        Vector lEye = l.getEyeLocation().toVector();
                        Vector ray = lEye.clone().subtract(pEye);
                        RayTraceResult result = w.rayTraceBlocks(g.getEyeLocation(), ray, k.distance(g, e));
                        if (result == null) {
                            w.playSound(g, Sound.BLOCK_ANVIL_PLACE, 1, 2);
                            g.setVelocity(new Vector(0, 0.5, 0));
                            g.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, PotionEffect.INFINITE_DURATION, 0));
                            g.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, PotionEffect.INFINITE_DURATION, 0));
                            BukkitRunnable explode = new BukkitRunnable() {
                                @Override
                                public void run() {
                                    k.gas(p, g, 10, 4);
                                    w.playSound(g.getLocation(), Sound.BLOCK_LANTERN_BREAK, 1, 1);
                                    w.playSound(g.getLocation(), Sound.BLOCK_LANTERN_BREAK, 1, 1);
                                    w.playSound(g.getLocation(), Sound.BLOCK_LANTERN_BREAK, 1, 1);
                                    w.spawnParticle(Particle.EXPLOSION, g.getLocation(), 1);
                                    g.remove();
                                }
                            };
                            explode.runTaskLater(plugin, 10L);
                            break;
                        }
                    }
                }
            }
        };
        trigger.runTaskTimer(plugin, 30L, 20L);
    }
    public void shockMine(Player p) {
        World w = p.getWorld();
        Location shootLoc = p.getEyeLocation();
        Vector shootVec = shootLoc.getDirection().normalize();
        ArmorStand g = (ArmorStand) w.spawnEntity(shootLoc, EntityType.ARMOR_STAND);
        if(!p.isSneaking()) {
            g.setVelocity(shootVec.multiply(0.5));
        }
        w.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_IRON, 1, 1);
        w.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_IRON, 1, 1);
        w.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_IRON, 1, 1);
        g.setCustomName(ChatColor.AQUA + p.getName() + "的电击地雷");
        g.setSmall(true);
        g.getEquipment().setHelmet(new ItemStack(Material.WITHER_SKELETON_SKULL));
        g.getEquipment().setChestplate(ap.mobChest(Color.BLACK));
        g.getEquipment().setLeggings(ap.mobLeg(Color.BLACK));
        g.getEquipment().setBoots(ap.mobBoot(Color.BLACK));
        double radius = 5;
        BukkitRunnable trigger = new BukkitRunnable() {
            @Override
            public void run() {
                if (g.isDead() || g.hasPotionEffect(PotionEffectType.LUCK)) {
                    this.cancel();
                    return;
                }
                for (Entity e : g.getNearbyEntities(radius, radius, radius)) {
                    if (e instanceof Player p1) {
                        if (p1 == p) continue;
                    }
                    if (e instanceof LivingEntity l) {
                        if (l.hasPotionEffect(PotionEffectType.INVISIBILITY)) continue;
                        if (l instanceof ArmorStand) continue;
                        Vector pEye = g.getEyeLocation().toVector();
                        Vector lEye = l.getEyeLocation().toVector();
                        Vector ray = lEye.clone().subtract(pEye);
                        RayTraceResult result = w.rayTraceBlocks(g.getEyeLocation(), ray, k.distance(g, e));
                        if (result == null) {
                            w.playSound(g, Sound.BLOCK_ANVIL_PLACE, 1, 2);
                            g.setVelocity(new Vector(0, 0.5, 0));
                            g.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, PotionEffect.INFINITE_DURATION, 0));
                            g.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, PotionEffect.INFINITE_DURATION, 0));
                            BukkitRunnable explode = new BukkitRunnable() {
                                @Override
                                public void run() {
                                    for (Entity e : g.getNearbyEntities(5, 5, 5)) {
                                        if (e instanceof LivingEntity l) {
                                            if (e instanceof Player p1) {
                                                p1.sendTitle(" ", ChatColor.AQUA + "！被电击！", 10, 40, 10);
                                            }
                                            l.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 2));
                                            l.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 100, 4));
                                        }
                                    }
                                    w.playSound(g.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 1, 1);
                                    w.spawnParticle(Particle.EXPLOSION, g.getLocation(), 1);
                                    w.spawnParticle(Particle.ELECTRIC_SPARK, g.getLocation(), 100, radius / 2, radius / 2, radius / 2);
                                    g.remove();
                                }
                            };
                            explode.runTaskLater(plugin, 10L);
                            break;
                        }
                    }
                }
            }
        };
        trigger.runTaskTimer(plugin, 30L, 20L);
    }
    public void fireCamp(Player p,ItemStack hand){
        if (p.getCooldown(hand.getType()) == 0) {
            p.setCooldown(hand.getType(), 20);
            World w = p.getWorld();
            if (p.getGameMode() != GameMode.CREATIVE) {
                int amount = hand.getAmount();
                hand.setAmount(amount - 1);
            }
            for(Entity e : w.getNearbyEntities(p.getLocation(),24,24,24)){
                if(e instanceof Player p1){
                    for(PotionEffect po : p1.getActivePotionEffects()){
                        p1.removePotionEffect(po.getType());
                    }
                    p1.setFireTicks(0);
                    p1.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,100,4));
                    p1.playSound(p1.getLocation(),Sound.BLOCK_BELL_USE,1,1);
                    p1.playSound(p1.getLocation(),Sound.BLOCK_BELL_USE,1,1);
                    p1.playSound(p1.getLocation(),Sound.BLOCK_BELL_USE,1,1);
                }
            }
        }
    }
    public void soulCamp(Player p,ItemStack hand){
        if (p.getCooldown(hand.getType()) == 0) {
            p.setCooldown(hand.getType(), 20);
            World w = p.getWorld();
            if (p.getGameMode() != GameMode.CREATIVE) {
                int amount = hand.getAmount();
                hand.setAmount(amount - 1);
            }
            for(Entity e : w.getNearbyEntities(p.getLocation(),24,24,24)){
                if(e instanceof LivingEntity l){
                    l.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS,100,4));
                    l.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING,100,4));
                    l.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS,120,4));
                }
                if(e instanceof Player p1){
                    p1.playSound(p1.getLocation(),Sound.BLOCK_BELL_USE,1,1);
                    p1.playSound(p1.getLocation(),Sound.BLOCK_BELL_USE,1,1);
                    p1.playSound(p1.getLocation(),Sound.BLOCK_BELL_USE,1,1);
                }
            }
        }
    }
    public void flute(Player p){
        BukkitRunnable task = playerTask.getOrDefault(p.getName(), null);
        World w = p.getWorld();
        if (task == null) {
            BukkitRunnable play = new BukkitRunnable() {
                int count = 0;
                @Override
                public void run() {
                    if (count >= musicScore1.length) {
                        this.cancel();
                        playerTask.remove(p.getName());
                        return;
                    }
                    if(musicScore1[count] > 0){
                        Note n = new Note(musicScore1[count]);
                        w.playSound(p.getLocation(),Sound.BLOCK_NOTE_BLOCK_FLUTE,1,n.getPitch());
                    }
                    count += 1;
                }
            };
            play.runTaskTimer(plugin,0L,1L);
            playerTask.put(p.getName(),play);
        }
    }
    public void wolfPack(Player p,ItemStack hand){
        World w = p.getWorld();
        Material material = hand.getType();
        if (p.getCooldown(material) == 0) {
            p.setCooldown(material, 20);
            if (p.getGameMode() != GameMode.CREATIVE) {
                int amount = hand.getAmount();
                hand.setAmount(amount - 1);
            }
            Location shootLoc = p.getEyeLocation();
            Vector shootVec = shootLoc.getDirection();
            Snowball nade = (Snowball) w.spawnEntity(shootLoc, EntityType.SNOWBALL);
            nade.setVelocity(shootVec.multiply(2));
            nade.setItem(hand);
            nade.setShooter(p);
            nade.setGlowing(true);
            w.playSound(shootLoc, Sound.ENTITY_EGG_THROW, 1, 1);
            BukkitRunnable land = new BukkitRunnable() {
                @Override
                public void run() {
                    Particle.DustOptions dust = new Particle.DustOptions(Color.WHITE, 1);
                    w.spawnParticle(Particle.DUST, nade.getLocation(), 0, dust);
                    if (nade.isDead() || nade.getTicksLived() > 10) {
                        if(nade.getTicksLived() > 10){
                            for(int i = 0;i < 12;i++){
                                Arrow a = w.spawnArrow(nade.getLocation(),nade.getVelocity(),1,50);
                                a.setShooter(p);
                                a.setGlowing(true);
                                a.setTicksLived(1200);
                                a.setDamage(10);
                                a.setCustomName(ChatColor.YELLOW + p.getName() + "的狼群");
                                BukkitRunnable traceEnemy = new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        List<Entity> entityInRange = a.getNearbyEntities(40, 40, 40);
                                        LivingEntity nearest = null;
                                        double nearestDistanceSquared = Double.MAX_VALUE;
                                        for (Entity entity : entityInRange) {
                                            if (entity instanceof Mob l) {
                                                if (l.isInvisible() && l.getType() == EntityType.SHULKER) continue;
                                                double distanceSquared = k.distance(a, l);
                                                if (distanceSquared < nearestDistanceSquared) {
                                                    nearest = l;
                                                    nearestDistanceSquared = distanceSquared;
                                                }
                                            }
                                        }
                                        double distance = -1;
                                        if (nearest != null) {
                                            Vector arrowLocVector = a.getLocation().toVector();
                                            Vector entityVector = (nearest.getEyeLocation().toVector()).subtract(arrowLocVector);
                                            a.setVelocity(entityVector.normalize());
                                            distance = k.distance(a,nearest);
                                        }
                                        if ((distance > 0 && distance < 2) || a.isDead()) {
                                            if ((distance > 0 && distance < 2)) {
                                                int count = 0;
                                                for (Entity e : a.getNearbyEntities(5, 5, 5)) {
                                                    if (e instanceof Arrow a1) {
                                                        if(a1.getName().contains("狼群")) {
                                                            a1.remove();
                                                            count += 1;
                                                        }
                                                    }
                                                }
                                                nearest.damage(count * 40);
                                                w.playSound(a.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 2, 1);
                                                w.spawnParticle(Particle.EXPLOSION_EMITTER, a.getLocation(), 1);
                                            }
                                            this.cancel();
                                        }
                                        w.spawnParticle(Particle.END_ROD, a.getLocation(), 0);
                                    }
                                };
                                traceEnemy.runTaskTimer(plugin, 20L,2L);
                            }
                        }
                        nade.remove();
                        w.playSound(nade.getLocation(), Sound.ENTITY_WOLF_AMBIENT, 3, 1);
                        Firework firework = (Firework)w.spawnEntity(nade.getLocation(), EntityType.FIREWORK_ROCKET);
                        firework.setVelocity(nade.getVelocity());
                        FireworkMeta meta = firework.getFireworkMeta();
                        meta.setPower(3);
                        meta.addEffect(FireworkEffect.builder()
                                .withColor(Color.GRAY)
                                .flicker(true)
                                .with(FireworkEffect.Type.BURST).build());
                        meta.addEffect(FireworkEffect.builder()
                                .withColor(Color.YELLOW)
                                .flicker(true)
                                .with(FireworkEffect.Type.BURST).build());
                        firework.setFireworkMeta(meta);
                        firework.detonate();
                        w.spawnParticle(Particle.EXPLOSION,nade.getLocation(),1);
                        this.cancel();
                    }
                }
            };
            land.runTaskTimer(plugin, 0L, 1L);
        }
    }
    @EventHandler
    public void shieldDamageEvent(PlayerShieldAmountChangeEvent changeEvent){
        Player p = changeEvent.getPlayer();
        double amount = changeEvent.getAmount();
        if(amount < 0){
            isChargingShield.remove(p);
        }
    }
    public void battery(Player p,int seconds,double shieldAmount,ItemStack item){
        isChargingShield.add(p);
        BukkitRunnable recover = new BukkitRunnable() {
            int count = 0;
            int step = seconds * 4;
            //add here?
            @Override
            public void run() {
                double shield = playerStats.getShield(p);
                if(count > step - 1 || shield == 20 || !isChargingShield.contains(p)){
                    isChargingShield.remove(p);
                    p.setCooldown(item,0);
                    this.cancel();
                    return;
                }
                Bukkit.getPluginManager().callEvent(new PlayerShieldAmountChangeEvent(p,shieldAmount / step));
                count += 1;
            }
        };
        recover.runTaskTimer(plugin,0L,5L);
    }
    public void deadline(Player p) {
        World w = p.getWorld();
        Location shootLoc = p.getEyeLocation();
        Vector shootVec = shootLoc.getDirection().normalize();
        ArmorStand g = (ArmorStand) w.spawnEntity(shootLoc, EntityType.ARMOR_STAND);
        if(!p.isSneaking()) {
            g.setVelocity(shootVec.multiply(0.5));
        }
        w.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_IRON, 1, 1);
        w.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_IRON, 1, 1);
        w.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_IRON, 1, 1);
        g.setCustomName(ChatColor.AQUA + p.getName() + "的死线");
        g.setSmall(true);
        g.getEquipment().setHelmet(new ItemStack(Material.LODESTONE));
        g.getEquipment().setChestplate(ap.mobChest(Color.GRAY));
        g.getEquipment().setLeggings(ap.mobLeg(Color.GRAY));
        g.getEquipment().setBoots(ap.mobBoot(Color.GRAY));
        g.setBasePlate(false);
        BukkitRunnable trigger = new BukkitRunnable() {
            int count = 0;

            @Override
            public void run() {
                if (count >= 20 || g.isDead()) {
                    if(count >= 20){
                        g.remove();
                        Location pLoc = g.getLocation().clone();
                        BukkitRunnable sweep = new BukkitRunnable() {
                            int count = 0;
                            @Override
                            public void run() {
                                if(count > 3){
                                    this.cancel();
                                    return;
                                }
                                double padX = pLoc.getX();
                                double padY = pLoc.getY();
                                double padZ = pLoc.getZ();
                                double i = Math.PI;
                                for (int j = 0; j <= 100; j++) {
                                    double x = padX + ((3 + count * 2) * Math.sin((3 + count * 2) * i + 0.5 * j));
                                    double z = padZ + ((3 + count * 2) * Math.cos((3 + count * 2) * i + 0.5 * j));
                                    Location areaP = new Location(w, x, padY, z);
                                    BlockData data = Bukkit.createBlockData(Material.GRAVEL);
                                    w.spawnParticle(Particle.DUST_PILLAR,areaP,1,data);
                                    w.spawnParticle(Particle.EXPLOSION,areaP,1);
                                }
                                w.spawnParticle(Particle.FLASH,g.getLocation(),1,Color.YELLOW);
                                w.spawnParticle(Particle.LAVA,g.getLocation(),10,1,1,1,0.1);
                                count += 1;
                            }
                        };
                        w.spawnParticle(Particle.CAMPFIRE_COSY_SMOKE,g.getLocation(),100,0,0,0,0.2);
                        sweep.runTaskTimer(plugin,0L,2L);
                        w.playSound(g.getLocation(),Sound.ENTITY_GENERIC_EXPLODE,3,1);
                        w.spawnParticle(Particle.EXPLOSION_EMITTER,g.getLocation(),1);
                        double radius = 10;
                        for(Entity e : g.getNearbyEntities(radius,radius,radius)){
                            if(e instanceof LivingEntity l){
                                Location shooterLoc = g.getEyeLocation();
                                Location targetLoc = l.getEyeLocation();
                                Vector sV = shooterLoc.toVector();
                                Vector tV = targetLoc.toVector();
                                RayTraceResult result = w.rayTraceBlocks(shooterLoc, tV.subtract(sV), k.distance(g, e));
                                if (result != null) continue;
                                l.damage(250);
                            }
                        }
                    }
                    this.cancel();
                    return;
                }
                Color c = Color.RED;
                if(count == 14){
                    w.playSound(g.getLocation(),Sound.ITEM_GOAT_HORN_SOUND_3,2,1.7f);
                    w.playSound(g.getLocation(),Sound.ITEM_GOAT_HORN_SOUND_3,2,1.65f);
                }else if(count == 13){
                    w.playSound(g.getLocation(),Sound.ITEM_GOAT_HORN_SOUND_3,2,1.6f);
                    w.playSound(g.getLocation(),Sound.ITEM_GOAT_HORN_SOUND_3,2,1.55f);
                } else if(count < 13) {
                    c = Color.YELLOW;
                    w.playSound(g.getLocation(), Sound.UI_BUTTON_CLICK, 2, 0.5f + count * 0.05f);
                    w.playSound(g.getLocation(), Sound.BLOCK_COPPER_BULB_TURN_OFF, 2, 0.5f + count * 0.05f);
                    w.playSound(g.getLocation(), Sound.BLOCK_COPPER_BULB_TURN_OFF, 2, 0.5f + count * 0.05f);
                    w.playSound(g.getLocation(), Sound.BLOCK_COPPER_BULB_TURN_OFF, 2, 0.5f + count * 0.05f);
                }
                double y = 1 + count / 10.0;
                Particle.DustOptions dust = new Particle.DustOptions(c,1.5f);
                w.spawnParticle(Particle.DUST,g.getLocation().add(0,y,0)
                        ,50,0,y / 2,0,dust);
                Entity nearest = null;
                double nearestDistanceSquared = Double.MAX_VALUE;
                for (Entity e : g.getNearbyEntities(5, 5, 5)) {
                    if (e instanceof Mob l) {
                        if (l.isInvisible() && l.getType() == EntityType.SHULKER) continue;
                        if (!l.getPassengers().isEmpty()) continue;
                        double distanceSquared = k.distance(g, l);
                        if (distanceSquared < nearestDistanceSquared) {
                            nearest = l;
                            nearestDistanceSquared = distanceSquared;
                        }
                    }
                    if (nearest != null) {
                        nearest.addPassenger(g);
                    }
                }
                count += 1;
            }
        };
        trigger.runTaskTimer(plugin,20L,8L);
    }
    public void leaperUnit(Player p,ItemStack hand){
        World w = p.getWorld();
        Material material = hand.getType();
        if (p.getCooldown(material) == 0) {
            p.setCooldown(material, 20);
            if (p.getGameMode() != GameMode.CREATIVE) {
                int amount = hand.getAmount();
                hand.setAmount(amount - 1);
            }
            Location shootLoc = p.getEyeLocation();
            Vector shootVec = shootLoc.getDirection();
            Snowball nade = (Snowball) w.spawnEntity(shootLoc, EntityType.SNOWBALL);
            nade.setVelocity(shootVec.multiply(2));
            nade.setItem(hand);
            nade.setShooter(p);
            nade.setGlowing(true);
            w.playSound(shootLoc, Sound.ENTITY_EGG_THROW, 1, 1);
            BukkitRunnable land = new BukkitRunnable() {
                @Override
                public void run() {
                    w.spawnParticle(Particle.END_ROD, nade.getLocation(), 0);
                    if (nade.isDead() || k.hitBallBlock(nade)) {
                        this.cancel();
                        nade.remove();
                        EnderSignal s = (EnderSignal) w.spawnEntity(nade.getLocation(),EntityType.EYE_OF_ENDER);
                        s.setItem(new ItemStack(Material.FIREWORK_STAR));
                        s.setGlowing(true);
                        BukkitRunnable boom = new BukkitRunnable() {
                            int count = 0;
                            @Override
                            public void run() {
                                if(count > 5){
                                    s.remove();
                                    this.cancel();
                                    return;
                                }
                                if(count < 5){
                                    if(count == 0) {
                                        w.playSound(nade.getLocation(), Sound.BLOCK_TRIAL_SPAWNER_ABOUT_TO_SPAWN_ITEM, 2, 1);
                                        w.playSound(nade.getLocation(), Sound.BLOCK_TRIAL_SPAWNER_ABOUT_TO_SPAWN_ITEM, 2, 1);
                                        w.playSound(nade.getLocation(), Sound.BLOCK_TRIAL_SPAWNER_ABOUT_TO_SPAWN_ITEM, 2, 1);
                                    }
                                    Particle.DustOptions dust = new Particle.DustOptions(Color.YELLOW,1);
                                    w.spawnParticle(Particle.DUST,nade.getLocation(),count * 10,
                                            (count+1)/2.0,(count+1)/2.0,(count+1)/2.0,dust);
                                    for(Entity e : nade.getNearbyEntities(5,5,5)) {
                                        if (e instanceof LivingEntity l1) {
                                            if(e instanceof Monster)continue;
                                            if (e instanceof Player p) {
                                                if(playerStats.isDying(p))continue;
                                                if (p.getGameMode() != GameMode.SURVIVAL) continue;
                                            }
                                            k.knockBack(l1, nade.getLocation(), -0.25);
                                        }
                                    }
                                }else {
                                    Location pLoc = nade.getLocation();
                                    k.explode(p,nade,22,1,5,0);
                                    BukkitRunnable sweep = new BukkitRunnable() {
                                        int count = 0;
                                        @Override
                                        public void run() {
                                            if(count > 5){
                                                this.cancel();
                                                return;
                                            }
                                            double padX = pLoc.getX();
                                            double padY = pLoc.getY();
                                            double padZ = pLoc.getZ();
                                            double i = Math.PI;
                                            for (int j = 0; j <= 100; j++) {
                                                double x = padX + ((1 + count) * Math.sin((1 + count) * i + 0.5 * j));
                                                double z = padZ + ((1 + count) * Math.cos((1 + count) * i + 0.5 * j));
                                                Location areaP = new Location(w, x, padY, z);
                                                BlockData data = Bukkit.createBlockData(Material.YELLOW_CONCRETE);
                                                w.spawnParticle(Particle.DUST_PILLAR,areaP,1,data);
                                                if(count == 2) {
                                                    w.spawnParticle(Particle.FIREFLY, areaP, 1);
                                                }
                                            }
                                            count += 1;
                                        }
                                    };
                                    w.spawnParticle(Particle.CAMPFIRE_COSY_SMOKE,nade.getLocation(),200,0,0,0,0.1);
                                    sweep.runTaskTimer(plugin,0L,2L);
                                    w.playSound(nade.getLocation(),Sound.ENTITY_GENERIC_EXPLODE,3,1);
                                    w.spawnParticle(Particle.EXPLOSION_EMITTER,nade.getLocation(),1);
                                    w.spawnParticle(Particle.FLASH,nade.getLocation(),1,Color.YELLOW);
                                }
                                count += 1;
                            }
                        };
                        boom.runTaskTimer(plugin,0L,10L);
                    }
                }
            };
            land.runTaskTimer(plugin, 3L, 1L);
        }
    }
    public void mendingPowder(Player p, ItemStack hand) {
        World w = p.getWorld();
        EntityEquipment e = p.getEquipment();
        Material material = hand.getType();
        ItemStack mainHand = e.getItemInMainHand();
        boolean repaired = false;
        if (p.getCooldown(material) == 0) {
            p.setCooldown(material, 20);
            if (mainHand.getType() != Material.AIR) {
                ItemMeta meta = mainHand.getItemMeta();
                if(meta instanceof Damageable d){
                    int maxDamage = d.getMaxDamage();
                    int damage = d.getDamage();
                    d.setDamage((int) Math.max(0,damage - (maxDamage * 0.2)));
                    mainHand.setItemMeta(d);
                    e.setItemInMainHand(mainHand);
                    w.playSound(p.getLocation(),Sound.BLOCK_ENCHANTMENT_TABLE_USE,1,1);
                    repaired = true;
                }
            }
        }
        if(repaired) {
            if (p.getGameMode() != GameMode.CREATIVE) {
                int amount = hand.getAmount();
                hand.setAmount(amount - 1);
            }
            p.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    TextComponent.fromLegacy(ChatColor.AQUA + "修理成功！"));
        }else {
            p.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    TextComponent.fromLegacy(ChatColor.RED + "修理失败！主手没有物品或者主手物品无法被修理"));
        }
    }
}
