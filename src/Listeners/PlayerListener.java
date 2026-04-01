package Listeners;

import Events.*;
import Universal.GameStatus;
import Universal.Kit;
import Universal.PlayerStats;
import Universal.Recipes;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

import static Universal.PlayerStats.playerMenuStatus;

public class PlayerListener implements Listener {
    JavaPlugin plugin;

    Kit k = Kit.INSTANCE;
    Recipes re = Recipes.INSTANCE;
    Random r = new Random();
    PlayerStats playerStats = PlayerStats.INSTANCE;
    GameStatus gameStatus = GameStatus.INSTANCE;
    HashSet<Player> reviving = new HashSet<>();
    HashSet<Player> beingRevive = new HashSet<>();
    HashMap<Player, Player> whoIsReviving = new HashMap<>();
    HashMap<String, BukkitRunnable> playerTask = new HashMap<>();
    // 倒地倒计时任务映射（改为 public static，以便外部事件监听器访问）
    public static final Map<Player, BukkitRunnable> dyingTimers = new HashMap<>();

    public PlayerListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void playerDropItem(PlayerDropItemEvent dropItemEvent){
        Item i = dropItemEvent.getItemDrop();
        ItemStack stack = i.getItemStack();
        World w = i.getWorld();
        if(stack.getType() == Material.RESIN_CLUMP){
            if(r.nextInt(5) == 0){
                dropItemEvent.setCancelled(true);
                w.playSound(i.getLocation(), Sound.BLOCK_SLIME_BLOCK_BREAK,1,1);
                w.playSound(i.getLocation(), Sound.BLOCK_SLIME_BLOCK_BREAK,1,1);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void playerShieldDamageReduction(EntityDamageEvent damageEvent) {
        if (damageEvent.isCancelled()) return;
        Entity damaged = damageEvent.getEntity();
        double damage = damageEvent.getDamage();
        double aDamage = damageEvent.getOriginalDamage(EntityDamageEvent.DamageModifier.ARMOR);
        DamageType type = damageEvent.getDamageSource().getDamageType();
        if (damaged instanceof Player p) {
            if (p.getNoDamageTicks() > 10) {
                damageEvent.setCancelled(true);
                return;
            }
            if (playerStats.isShieldOn(p)) {
                if (type != DamageType.FALL && type != DamageType.STARVE && !p.isBlocking()) {
                    if (playerStats.isShieldOn(p)) {
                        if (playerStats.hasShield(p)) {
                            Bukkit.getPluginManager().callEvent(new PlayerShieldAmountChangeEvent(p, -damage));
                            damage *= 0.6;
                        }
                    }
                }
            }
        }
        damage -= aDamage;
        damageEvent.setDamage(damage);
    }
    @EventHandler(priority = EventPriority.HIGH)
    public void playerInteractArmorStand(PlayerArmorStandManipulateEvent event) {
        ArmorStand a = event.getRightClicked();
        if (a.getCustomName() != null) {
            // 如果是遗物盔甲架，则不取消事件
            if (GameListener.isLootStand(a)) {
                return;
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void armorStandDamage(EntityDamageEvent damageEvent) {
        Entity e = damageEvent.getEntity();
        if (e instanceof ArmorStand a) {
            if (a.getCustomName() != null) {
                damageEvent.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void playerItemDamage(PlayerItemDamageEvent damageEvent) {
        ItemStack item = damageEvent.getItem();
        int damage = damageEvent.getDamage();
        if (item.getType() == Material.SHIELD) {
            damageEvent.setDamage(damage * 3);
        }
    }

    @EventHandler
    public void playerSlide(PlayerToggleSneakEvent sneakEvent) {
        Player p = sneakEvent.getPlayer();
        Location loc = p.getEyeLocation();
        Vector vec = loc.getDirection();
        Vector slide = (vec.setY(0.5)).normalize();
        if (k.angle(vec, new Vector(0, 1, 0)) > 0.9) {
            slide.multiply(0.3);
        }
        if (p.isSprinting()
                && p.isSneaking()
                && p.isOnGround()
                && p.getCooldown(Material.LIGHT) == 0) {
            p.setVelocity(slide.multiply(0.9));
            p.setCooldown(Material.LIGHT, 20);
            if(playerStats.isInGame(p)) {
                int food = p.getFoodLevel();
                p.setFoodLevel(Math.max(0, food - 2));
            }
        }
    }

    public void crawling(Player p) {
        World w = p.getWorld();
        Shulker top = (Shulker) w.spawnEntity(p.getEyeLocation(), EntityType.SHULKER);
        top.getAttribute(Attribute.SCALE).setBaseValue(0.8);
        top.setSilent(true);
        top.setAI(false);
        top.setCollidable(false);
        top.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, PotionEffect.INFINITE_DURATION, 10));
        top.setInvisible(true);
        BukkitRunnable crawling = new BukkitRunnable() {
            @Override
            public void run() {
                if (!playerStats.isDying(p)) {
                    top.remove();
                    this.cancel();
                }
                top.teleport(p.getLocation().add(0, 1, 0));
            }
        };
        crawling.runTaskTimer(plugin, 0L, 1L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void playerDeath(EntityDamageEvent damageEvent) {
        if (damageEvent.isCancelled()) return;
        double damage = damageEvent.getFinalDamage();
        DamageType damageType = damageEvent.getDamageSource().getDamageType();
        if (damageType.equals(DamageType.OUT_OF_WORLD)) return;
        if (damageType.equals(DamageType.FIREWORKS)) {
            damageEvent.setCancelled(true);
        }
        Entity entity = damageEvent.getEntity();
        if (entity instanceof Player p) {
            if(!playerStats.isInGame(p))return;
            World w = p.getWorld();
            if (damage >= p.getHealth()) {
                damageEvent.setCancelled(true);
                ItemStack offHand = p.getEquipment().getItemInOffHand();
                if (offHand.getType() == Material.TOTEM_OF_UNDYING) {
                    w.playSound(p.getLocation(), Sound.ITEM_TOTEM_USE, 1, 1);
                    w.spawnParticle(Particle.TOTEM_OF_UNDYING, p.getLocation().add(0, 1, 0), 50, 1, 1, 1, 0.1);
                    p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 600, 1));
                    p.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 100, 1));
                    offHand.setAmount(0);
                    return;
                }
                if (!playerStats.isDying(p)) {
                    for (PotionEffect po : p.getActivePotionEffects()) {
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
                    for (Entity e : p.getNearbyEntities(32, 32, 32)) {
                        if (e instanceof Mob m) {
                            if (m.getTarget() == p) {
                                m.setTarget(null);
                            }
                        }
                    }

                    // 启动60秒倒计时，结束后自动撤离失败
                    BukkitRunnable timer = new BukkitRunnable() {
                        int count = 0;
                        int maxTime = 90;
                        @Override
                        public void run() {
                            if(count >= maxTime) {
                                if (playerStats.isDying(p)) {
                                    triggerFailExtract(p);
                                }
                                dyingTimers.remove(p);
                                this.cancel();
                                return;
                            }
                            p.sendTitle("",ChatColor.RED + "" +  (maxTime - count),0,10,10);
                            p.playSound(p.getLocation(),Sound.BLOCK_NOTE_BLOCK_HAT,1,1);
                            count ++;
                        }
                    };
                    timer.runTaskTimer(plugin, 0,20L); // 60秒
                    dyingTimers.put(p, timer);
                } else {
                    // 玩家已经倒地，再次受到致命伤害 -> 撤离失败
                    triggerFailExtract(p);
                }
            }
        }
    }

    /**
     * 触发玩家撤离失败（倒地无人救起或主动放弃）
     * 只负责取消倒计时并触发事件，具体清理由事件监听器完成
     */
    private void triggerFailExtract(Player p) {
        if (!playerStats.isDying(p)) return;
        // 取消倒地倒计时
        BukkitRunnable timer = dyingTimers.remove(p);
        if (timer != null) timer.cancel();
        // 触发事件，由事件监听器完成后续清理
        Bukkit.getPluginManager().callEvent(new PlayerFailToExtractEvent(p));
    }

    public void sos(final Player p) {
        final World w = p.getWorld();
        Firework firework = (Firework) w.spawnEntity(p.getLocation().add(0, 2, 0), EntityType.FIREWORK_ROCKET);
        FireworkMeta meta = firework.getFireworkMeta();
        meta.setPower(3);
        meta.addEffect(FireworkEffect.builder()
                .withColor(Color.RED)
                .flicker(true)
                .with(FireworkEffect.Type.BALL_LARGE).build());
        firework.setFireworkMeta(meta);
        BukkitRunnable smoke = new BukkitRunnable() {
            @Override
            public void run() {
                if (firework.isDead()) {
                    w.playSound(p.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 4F, 1F);
                    w.playSound(p.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 4F, 1F);
                    this.cancel();
                }
                w.spawnParticle(Particle.CAMPFIRE_SIGNAL_SMOKE, firework.getLocation(), 0);
                w.spawnParticle(Particle.FLASH, firework.getLocation(), 0, Color.RED);
            }
        };
        BukkitRunnable sos = new BukkitRunnable() {
            int count = 0;

            public void run() {
                switch (count) {
                    case 0, 1, 2, 4, 6, 8, 10, 11, 12:
                        w.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 1.0F, 4F);
                        break;
                }
                if (count == 12)
                    cancel();
                count++;
            }
        };
        sos.runTaskTimer(plugin, 0L, 3L);
        smoke.runTaskTimer(plugin, 0L, 1L);
    }

    @EventHandler
    public void playerInteractEntity(PlayerInteractAtEntityEvent interact) {
        Player p = interact.getPlayer();
        Entity clicked = interact.getRightClicked();
        if (clicked instanceof Player p1) {
            if(p.getGameMode() == GameMode.SPECTATOR)return;
            if (p.isSneaking()) {
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

    public void reviveTeammate(Player p, Player reviver, int time) {
        if (p.getGameMode().equals(GameMode.SPECTATOR)) return;
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
                        // 取消倒地倒计时
                        BukkitRunnable timer = dyingTimers.remove(p);
                        if (timer != null) timer.cancel();
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

    public String reviveProgress(int total, int step) {
        StringBuilder progress = new StringBuilder();
        if (step >= total) return "复活成功！";
        progress.append("复活进度：");
        for (int i = 0; i < total; i++) {
            if (i < step) {
                progress.append("|");
            } else {
                progress.append("·");
            }
        }
        return progress.toString();
    }

    @EventHandler
    public void playerEquipArmor(ArmorEquipEvent equipEvent) {
        Player p = equipEvent.getPlayer();
        if(!playerStats.isInGame(p)) return;
        double shield = playerStats.getShield(p);
        int maxShield = playerStats.getMaxShield();
        ItemStack newPiece = equipEvent.getNewArmorPiece();
        Bukkit.getPluginManager().callEvent(new PlayerShieldAmountChangeEvent(p, 0));
        if (newPiece == null || newPiece.getType() == Material.AIR) {
            BukkitRunnable later = new BukkitRunnable() {
                @Override
                public void run() {
                    if (!k.isArmored(p)) {
                        if (playerStats.isShieldOn(p)) {
                            playerStats.closeShield(p);
                        }
                    }
                }
            };
            later.runTaskLater(plugin, 1L);
        } else {
            if (shield == -1 || !playerStats.isShieldOn(p)) {
                if (shield == -1) {
                    GadgetListener gadgetListener = new GadgetListener(plugin);
                    gadgetListener.battery(p,10,30,new ItemStack(Material.NETHER_PORTAL));
                }
                if (!playerStats.isShieldOn(p)) {
                    playerStats.openShield(p);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void PlayerShieldDamage(PlayerShieldAmountChangeEvent changeEvent) {
        Player p = changeEvent.getPlayer();
        World w = p.getWorld();
        double amount = changeEvent.getAmount();
        double shield = playerStats.getShield(p);
        int maxShield = playerStats.getMaxShield();
        double newShield = shield + amount;
        if (amount < 0) {
            playerStats.setShield(p, Math.max(0, newShield));
        } else {
            playerStats.setShield(p, Math.min(maxShield, newShield));
        }
        BossBar bar = PlayerStats.playerShieldBar.get(p.getName());
        if (bar != null) {
            double progress = newShield / maxShield;
            if (progress < 0) {
                progress = 0;
                bar.setColor(BarColor.WHITE);
            } else if (progress > 1) {
                progress = 1;
            }
            bar.setProgress(progress);
            bar.setTitle(ChatColor.AQUA + "" + ChatColor.BOLD + "护盾丨电量：" + String.format("%.2f", Math.min(Math.max(newShield, 0), maxShield)));
        }
        if (shield > 0 && newShield <= 0) {
            //play crack effect
            if (k.isArmored(p)) {
                k.shieldBreakEffect(p.getLocation().add(0,1,0));
                Bukkit.getPluginManager().callEvent(new PlayerShieldBreakEvent(p));
            }
        }
        if (shield < maxShield && newShield >= maxShield) {
            w.playSound(p.getLocation(), Sound.BLOCK_CONDUIT_DEACTIVATE, 1, 1);
        }
        if (!playerStats.isShieldOn(p)) return;
        if (playerStats.hasShield(p)) {
            w.playSound(p.getLocation(), Sound.ENTITY_CREAKING_SWAY, 1, 1.5f);
            w.spawnParticle(Particle.TRIAL_OMEN, p.getLocation().add(0, 1, 0),
                    20, 0.2, 0.4, 0.2);
        }
    }

    @EventHandler
    public void playerCheckRecipe(PlayerInteractEvent interactEvent) {
        Action action = interactEvent.getAction();
        Player p = interactEvent.getPlayer();
        World w = p.getWorld();
        if (p.getGameMode() == GameMode.SPECTATOR) return;
        ItemStack hand = p.getInventory().getItemInMainHand();
        boolean rightClick = action.equals(Action.RIGHT_CLICK_AIR)
                || action.equals(Action.RIGHT_CLICK_BLOCK);
        if (hand.getType() == Material.WRITTEN_BOOK) {
            if (rightClick) {
                ItemMeta meta = hand.getItemMeta();
                String name = meta.getDisplayName();
                int index = name.indexOf(ChatColor.GOLD + "配方");
                if (index > 0) {
                    String itemName = name.substring(0, index);
                    int key = re.getRecipeKeys().getOrDefault(itemName, -1);
                    if (key > -1) {
                        NamespacedKey namespacedKey = NamespacedKey.fromString("r" + key, plugin);
                        Recipe r = Bukkit.getRecipe(namespacedKey);
                        ItemStack item = r.getResult();
                        Inventory inv = Bukkit.createInventory(p, InventoryType.WORKBENCH,
                                ChatColor.RED + "" + ChatColor.BOLD + "配方");
                        inv.setItem(0, item);
                        ItemStack[] content = new ItemStack[0];
                        if (r instanceof ShapedRecipe sr) {
                            content = re.getRecipeFlat(sr);
                        } else if (r instanceof ShapelessRecipe sl) {
                            content = sl.getIngredientList().toArray(new ItemStack[0]);
                        }
                        if (content.length > 0) {
                            boolean open = true;
                            for (int i = 0; i < 9; i++) {
                                if (i >= content.length) break;
                                ItemStack itemStack = content[i];
                                if (itemStack == null) continue;
                                inv.setItem(i + 1, itemStack);
                            }
                            if (action == Action.RIGHT_CLICK_BLOCK) {
                                Block clicked = interactEvent.getClickedBlock();
                                if (clicked.getType() == Material.CRAFTING_TABLE && p.isSneaking()) {
                                    ItemStack[] missing = k.checkMaterials(p, content);
                                    if (missing.length == 0) {
                                        open = false;
                                        Item i = w.dropItem(p.getEyeLocation(), item);
                                        i.setVelocity(p.getEyeLocation().getDirection().multiply(0.1));
                                        w.playSound(p.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 1);
                                        w.playSound(p.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1, 1);
                                        if (p.getGameMode() != GameMode.CREATIVE) {
                                            k.removeItems(p, content);
                                        }
                                    } else {
                                        inv = Bukkit.createInventory(p, 9,
                                                ChatColor.RED + "" + ChatColor.BOLD + "缺少物品列表");
                                        for (ItemStack i : missing) {
                                            inv.addItem(i);
                                        }
                                    }
                                }
                            }
                            if (open) {
                                p.openInventory(inv);
                                playerMenuStatus.put(p.getName(), PlayerStats.MenuStatus.CRAFTING_MENU);
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void playerCrafting(CraftItemEvent craftEvent) {
        if (craftEvent.getWhoClicked() instanceof Player p) {
            ItemStack result = craftEvent.getRecipe().getResult();
            ItemMeta resultMeta = result.getItemMeta();
            HashMap<String, Integer> keyMap = re.getRecipeKeys();
            List<String> freeRecipeString = Arrays.stream(re.getFreeRecipes()).toList();
            Inventory inv = p.getInventory();
            if (resultMeta.hasDisplayName()) {
                String resultName = k.getLore(result);
                int key = keyMap.getOrDefault(resultName, -1);
                if (key != -1) {
                    if (!freeRecipeString.contains(resultName)) {
                        craftEvent.setCancelled(true);
                        for (ItemStack i : inv.getContents()) {
                            if (i == null) continue;
                            if (i.getType() == Material.WRITTEN_BOOK) {
                                ItemMeta meta = i.getItemMeta();
                                String bookName = meta.getDisplayName();
                                int index = bookName.indexOf(ChatColor.GOLD + "配方");
                                if (index != -1) {
                                    String itemName = resultName.substring(0, index);
                                    if (itemName.equals(resultName)) {
                                        craftEvent.setCancelled(false);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ==================== 游戏结束处理 ====================
    @EventHandler
    public void onGameEnd(GameEndEvent event) {
        World world = event.getWorld();
        Location spawn = world.getSpawnLocation();
        for (Player p : world.getPlayers()) {
            // 如果玩家是旁观者模式且未在游戏中，则传送至出生点并切换为生存模式
            if (p.getGameMode() == GameMode.SPECTATOR && !playerStats.isInGame(p)) {
                p.teleport(spawn);
                p.setGameMode(GameMode.SURVIVAL);
                p.sendMessage("§a游戏结束，你已被传送回出生点。");
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        int level = PlayerStats.INSTANCE.getIslandLevel(player);
        Kit.INSTANCE.setInventoryLimit(player, level);
    }
}