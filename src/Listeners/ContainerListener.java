package Listeners;

import Universal.*;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Door;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.entity.EntityBreakDoorEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.*;

public class ContainerListener implements Listener {
    Material[] normalContainer = {
            Material.PALE_OAK_TRAPDOOR,
            Material.BIRCH_TRAPDOOR,
            Material.WARPED_DOOR,
            Material.MANGROVE_DOOR,
            Material.DAYLIGHT_DETECTOR,
            Material.FLOWER_POT,
            Material.COMPOSTER,
            Material.CHISELED_BOOKSHELF,
            Material.CAULDRON,
            Material.LAVA_CAULDRON,
            Material.WATER_CAULDRON,
            Material.CARVED_PUMPKIN,
            Material.JUKEBOX,
            Material.MUDDY_MANGROVE_ROOTS
    };
    Material[] goodContainer = {
            Material.BEEHIVE,
            Material.WAXED_COPPER_GOLEM_STATUE,
            Material.WAXED_WEATHERED_COPPER_GOLEM_STATUE,
            Material.WAXED_OXIDIZED_COPPER_GOLEM_STATUE,
            Material.WAXED_EXPOSED_COPPER_GOLEM_STATUE,
            Material.WAXED_WEATHERED_COPPER_CHEST,
            Material.WAXED_OXIDIZED_COPPER_CHEST,
            Material.WAXED_EXPOSED_COPPER_CHEST,
            Material.WAXED_COPPER_CHEST,
            Material.CHEST,
            Material.FURNACE,
            Material.BARREL,
            Material.BEE_NEST,
            Material.LECTERN,
            Material.DECORATED_POT
    };
    Material[] bestContainer = {
            Material.CRAFTER,
            Material.BLAST_FURNACE,
            Material.SMOKER,
    };
    Kit k = Kit.INSTANCE;
    LootPool lp = LootPool.INSTANCE;
    ArmorPool ap = ArmorPool.INSTANCE;
    BoxPool bp = BoxPool.INSTANCE;
    WeaponPool wp = WeaponPool.INSTANCE;
    PlayerStats playerStats = PlayerStats.INSTANCE;
    GameStatus gameStatus = GameStatus.INSTANCE;
    Random r = new Random();
    JavaPlugin plugin;
    HashSet<Block> hasContent = new HashSet<>();
    HashMap<Block, ItemStack[]>blockContent = new HashMap<>();
    HashMap<Block,Player>blockSearcherMap = new HashMap<>();
    HashMap<Block,Integer>doorBreakMap = new HashMap<>();
    HashMap<Player,Block>playerSearchBlockMap = new HashMap<>();
    Material[]interactBlocks = new Material[]{
        Material.CRAFTING_TABLE,
        Material.ANVIL,
        Material.ENCHANTING_TABLE,
        Material.ENDER_CHEST,
    };

    public ContainerListener(JavaPlugin plugin){
        this.plugin = plugin;
    }

    public int getContainerRarity(Block b){
        List<Material> normalContainerList = Arrays.stream(normalContainer).toList();
        List<Material> goodContainerList = Arrays.stream(goodContainer).toList();
        List<Material> bestContainerList = Arrays.stream(bestContainer).toList();
        Material m = b.getType();
        if (normalContainerList.contains(m)) return 0;
        if (goodContainerList.contains(m)) return 1;
        if (bestContainerList.contains(m)) return 2;
        return switch (m) {
            case SMITHING_TABLE -> -2;
            case FLETCHING_TABLE -> -3;
            case LOOM -> -4;
            case BREWING_STAND -> -5;
            default -> -1;
        };
    }

    public float[] getContainerValue(Block b) {
        return switch (getContainerRarity(b)) {
            //普通容器概率，稀有度从低到高
            case 0 -> new float[]{5f, 3.3f, 1.5f, 0.15f, 0.049f, 0.001f};
            //中等容器概率，稀有度从低到高
            case 1 -> new float[]{3.75f, 3f, 2.25f, 0.7f, 0.25f, 0.05f};
            //高级容器概率，稀有度从低到高
            case 2 -> new float[]{2f, 2f, 2f, 2.25f, 1.5f, 0.25f};
            default -> new float[0];
        };
    }
    public int getContainerCount(Block b) {
        int rarity = getContainerRarity(b);
        int count,max;
        double chance;
        switch (rarity){
            case 0:{
                count = 1;
                max = 4;
                chance = 0.9;
            }
            break;
            case 1:{
                count = 3;
                max = 8;
                chance = 0.9;
            }
            break;
            case 2:{
                count = 2;
                max = 5;
                chance = 0.8;
            }
            break;
            default:{
                count = 1;
                max = 5;
                chance = 0.9;
            }
        }
        for (int i = 1; i <= max; i++) {
            if (r.nextDouble() > chance || count >= max) {
                break;
            } else {
                count += 1;
                chance *= chance;
            }
        }
        return count;
    }
    @EventHandler
    public void playerInteract(PlayerInteractEvent interactEvent) {
        Player p = interactEvent.getPlayer();
        ItemStack hand = p.getEquipment().getItemInMainHand();
        if(playerStats.isDying(p) || p.getGameMode() == GameMode.SPECTATOR) {
            interactEvent.setCancelled(true);
            return;
        }
        World w = p.getWorld();
        Action action = interactEvent.getAction();
        if (action.equals(Action.RIGHT_CLICK_BLOCK)) {
            Block b = interactEvent.getClickedBlock();
            if(b.getType() == Material.STONECUTTER){
                ItemStack[]drops = lp.getRecycle(hand);
                if(drops != null){
                    interactEvent.setCancelled(true);
                    Location bLoc = b.getLocation();
                    Location pLoc = p.getEyeLocation();
                    Vector offSet = pLoc.toVector().subtract(bLoc.toVector());
                    for(ItemStack i : drops) {
                        w.dropItem(bLoc.add(offSet.multiply(0.5)), i);
                    }
                    w.playSound(b.getLocation(),Sound.UI_STONECUTTER_TAKE_RESULT,1,1);
                }
            }
            if(!playerStats.isInGame(p))return;
            if(b.getType() == Material.IRON_DOOR){
                Block b1 = w.getBlockAt(b.getLocation().add(0,1,0));
                Block b2 = w.getBlockAt(b.getLocation().add(0,-1,0));
                int count = doorBreakMap.getOrDefault(b,0);
                if(p.getCooldown(Material.IRON_DOOR) == 0){
                    p.setCooldown(Material.IRON_DOOR,40);
                    if(r.nextDouble() < 0.15 || count >= 6){
                        doorBreakMap.remove(b);
                        doorBreakMap.remove(b1);
                        doorBreakMap.remove(b2);
                        p.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                                TextComponent.fromLegacy("门被踹开了"));
                        w.playSound(b.getLocation(),Sound.BLOCK_HEAVY_CORE_BREAK,1,1);
                        w.playSound(b.getLocation(),Sound.BLOCK_HEAVY_CORE_BREAK,1,1);
                        w.playSound(b.getLocation(),Sound.BLOCK_HEAVY_CORE_BREAK,1,1);
                        w.playSound(b.getLocation(),Sound.BLOCK_IRON_DOOR_OPEN,1,1);
                        w.spawnParticle(Particle.BLOCK,b.getLocation(),20,0.5,0.5,0.5,0.1,
                                Bukkit.createBlockData(Material.IRON_DOOR));
                        Door door = (Door) b.getBlockData();
                        door.setOpen(true);
                        b.setBlockData(door);
                        gameStatus.addDoor(w,b);
                        if(b1.getType() == Material.IRON_DOOR){
                            Door door1 = (Door) b1.getBlockData();
                            door1.setOpen(true);
                            b1.setBlockData(door1);
                        }
                        if(b2.getType() == Material.IRON_DOOR){
                            Door door2 = (Door) b2.getBlockData();
                            door2.setOpen(true);
                            b2.setBlockData(door2);
                        }
                    }else {
                        int damage = 1;
                        w.playSound(b.getLocation(),Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR,3,1);
                        doorBreakMap.put(b,count + damage);
                        if(b1.getType() == Material.IRON_DOOR){
                            doorBreakMap.put(b1,count + damage);
                        }
                        if(b2.getType() == Material.IRON_DOOR){
                            doorBreakMap.put(b2,count + damage);
                        }
                        p.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                                TextComponent.fromLegacy("门松动了一些，再试试吧"));
                        w.spawnParticle(Particle.LARGE_SMOKE,b.getLocation(),10,0.5,0.5,0.5,0.05);
                    }
                }
            }
            if(getContainerRarity(b) == -1) {
                Material type = b.getType();
                if(type.isInteractable()) {
                    if (type.name().contains("BUTTON")) return;
                    List<Material> whitelist = Arrays.stream(interactBlocks).toList();
                    if (!whitelist.contains(type)) {
                        interactEvent.setCancelled(true);
                    }
                }
            }else {
                interactEvent.setCancelled(true);
                if(p.getCooldown(Material.COMMAND_BLOCK_MINECART) == 0) {
                    p.setCooldown(Material.COMMAND_BLOCK_MINECART,10);
                    openContainer(p, b);
                }
            }
        }
    }
    public void openContainer(Player p,Block container) {
        World w = p.getWorld();
        if(gameStatus.isEmpty(container)){
            p.sendTitle("", ChatColor.AQUA + "这个容器已经空了", 10, 30, 10);
            return;
        }
        Player searcher = blockSearcherMap.getOrDefault(container, null);
        int rarity = getContainerRarity(container);
        if (searcher == null) {
            if (rarity != -1) {
                ItemStack[] content = blockContent.getOrDefault(container, new ItemStack[0]);
                if (content.length == 0) {
                    if (rarity >= 0) {
                        float[] weights = getContainerValue(container);
                        int count = getContainerCount(container);
                        content = lp.getContent(count, weights);
                    } else {
                        switch (rarity) {
                            case -2 -> content = smithContent();
                            case -3 -> content = arrowContent();
                            case -4 -> content = loomContent();
                            case -5 -> content = potionContent();
                        }
                    }
                    hasContent.add(container);
                    blockContent.put(container, content);
                    Block b1 = w.getBlockAt(container.getLocation().add(0,1,0));
                    Block b2 = w.getBlockAt(container.getLocation().add(0,-1,0));
                    if(b1.getType().name().contains("DOOR")){
                        blockContent.put(b1, content);
                    }
                    if(b2.getType().name().contains("DOOR")){
                        blockContent.put(b2, content);
                    }
                }
                Sound s = switch (rarity) {
                    case 0 -> Sound.BLOCK_CHEST_OPEN;
                    case 1 -> Sound.BLOCK_BARREL_OPEN;
                    case 2 -> Sound.BLOCK_ENDER_CHEST_OPEN;
                    default -> Sound.BLOCK_COPPER_CHEST_OPEN;
                };
                w.playSound(container.getLocation(), s, 1, 1);
                blockSearcherMap.put(container, p);
                boolean isUsing = false;
                ItemStack hand = p.getEquipment().getItemInMainHand();
                if (k.getLore(hand).equals("§f奇袭者工具")) {
                    isUsing = true;
                    p.playSound(p.getLocation(), Sound.BLOCK_IRON_TRAPDOOR_OPEN, 1, 1F);
                    Damageable d = (Damageable) hand.getItemMeta();
                    int damage = d.getDamage();
                    d.setDamage(damage + 1);
                    hand.setItemMeta(d);
                    if (damage + 1 > d.getMaxDamage()) {
                        p.playSound(p.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1F);
                        w.spawnParticle(Particle.ITEM, p.getEyeLocation(), 50, 0.1, 0.1, 0.1, 0.1, hand);
                        hand = null;
                    }
                    p.getEquipment().setItemInMainHand(hand);
                }
                checkContainer(p, container, isUsing);
            }
        } else {
            if (searcher.equals(p)) {
                p.sendTitle("", ChatColor.AQUA + "正在搜索该容器", 10, 10, 10);
            } else {
                p.sendTitle("", ChatColor.AQUA + "其他人正在搜索该容器", 10, 10, 10);
            }
        }
    }

    public void checkContainer(Player p,Block container,boolean isUsingCrowbar){
        World w = p.getWorld();
        ItemStack[]content = blockContent.getOrDefault(container,new ItemStack[0]);
        if(content.length == 0) {
            hasContent.remove(container);
            blockContent.remove(container);
            blockSearcherMap.remove(container);
            gameStatus.setEmpty(container);
            Block b1 = w.getBlockAt(container.getLocation().add(0,1,0));
            Block b2 = w.getBlockAt(container.getLocation().add(0,-1,0));
            if(b1.getType().name().contains("DOOR")){
                hasContent.remove(b1);
                blockContent.remove(b1);
                blockSearcherMap.remove(b1);
                gameStatus.setEmpty(b1);
            }
            if(b2.getType().name().contains("DOOR")){
                hasContent.remove(b2);
                blockContent.remove(b2);
                blockSearcherMap.remove(b2);
                gameStatus.setEmpty(b2);
            }
            return;
        }
        BukkitRunnable check = new BukkitRunnable() {
            int step = 0;
            int count = 0;
            float pitch = 0.8f;
            @Override
            public void run() {
                if(step % 2 == 0){
                    boolean stop = false;
                    RayTraceResult result = p.rayTraceBlocks(4);
                    if(result != null){
                        Block b = result.getHitBlock();
                        if(b == null || !b.equals(container)){
                            stop = true;
                        }
                    }else {
                        stop = true;
                    }
                    if(stop){
                        blockSearcherMap.remove(container);
                        p.playSound(p,Sound.ENTITY_ITEM_BREAK,1,1);
                        p.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                                TextComponent.fromLegacy(ChatColor.RED + "搜索中断，视线中没有容器或距离过远"));
                        this.cancel();
                        return;
                    }else {
                        p.playSound(p, Sound.BLOCK_NOTE_BLOCK_HAT,1,pitch);
                        p.playSound(p, Sound.BLOCK_NOTE_BLOCK_HAT,1,pitch);
                        p.playSound(p, Sound.BLOCK_NOTE_BLOCK_HAT,1,pitch);
                        pitch += 0.1f;
                        count += 1;
                    }
                }
                ItemStack item = content[0];
                ItemStack item2 = null;
                int rarity = lp.getRarity(item);
                int bound = 5 + Math.abs(rarity);
                if(p.hasPotionEffect(PotionEffectType.HASTE)){
                    PotionEffect effect = p.getPotionEffect(PotionEffectType.HASTE);
                    int amp = effect.getAmplifier() + 1;
                    bound = Math.max(bound - amp, 1);
                }
                if(p.hasPotionEffect(PotionEffectType.LUCK)){
                    if(r.nextInt(4) == 0){
                        ItemStack[]items2 = lp.getContent(1,getContainerValue(container));
                        item2 = items2[0];
                    }
                }
                String message = searchProgress(bound,count);
                p.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                        TextComponent.fromLegacy(ChatColor.AQUA + message
                                + "(" + content.length +")"));
                if(count >= bound) {
                    if (getContainerRarity(container) < 0) {
                        Sound s = switch (getContainerRarity(container)) {
                            case -2 -> Sound.BLOCK_ANVIL_USE;
                            case -3 -> Sound.BLOCK_BARREL_OPEN;
                            case -4 -> Sound.ITEM_ARMOR_EQUIP_NETHERITE;
                            case -5 -> Sound.BLOCK_BREWING_STAND_BREW;
                            default -> Sound.ENTITY_ITEM_PICKUP;
                        };
                        p.playSound(p, s, 1, 1);
                    }
                    Location bLoc = container.getLocation();
                    Location pLoc = p.getEyeLocation();
                    Vector offSet = pLoc.toVector().subtract(bLoc.toVector());
                    w.dropItem(bLoc.add(offSet.multiply(0.5)), item).setTicksLived(5400);
                    if (item2 != null) {
                        w.dropItem(bLoc.add(offSet.multiply(0.5)), item2).setTicksLived(5400);;
                        w.spawnParticle(Particle.TOTEM_OF_UNDYING,bLoc,20,1,1,1,0.5);
                        int rarity2 = lp.getRarity(item2);
                        if(rarity2 > rarity){
                            rarity = rarity2;
                        }
                    }
                    Sound s = switch (rarity) {
                        case 0, 1, 2 -> Sound.UI_LOOM_TAKE_RESULT;
                        case 3 -> Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
                        case 4 -> Sound.ENTITY_PLAYER_LEVELUP;
                        case 5 -> Sound.UI_TOAST_CHALLENGE_COMPLETE;
                        default -> Sound.ENTITY_ITEM_PICKUP;
                    };
                    p.playSound(container.getLocation(), s, 1, 1);
                    w.playSound(container.getLocation(),Sound.UI_LOOM_TAKE_RESULT,1,1);
                    w.spawnParticle(Particle.EXPLOSION, container.getLocation(), 1);
                    w.spawnParticle(Particle.BLOCK, container.getLocation()
                            , 50, 1.5, 1.5, 1.5, container.getBlockData());
                    ItemStack[] newContent = new ItemStack[content.length - 1];
                    System.arraycopy(content, 1, newContent, 0, newContent.length);
                    blockContent.put(container, newContent);
                    Block b1 = w.getBlockAt(container.getLocation().add(0,1,0));
                    Block b2 = w.getBlockAt(container.getLocation().add(0,-1,0));
                    if(b1.getType().name().contains("DOOR")){
                        blockContent.put(b1, newContent);
                    }
                    if(b2.getType().name().contains("DOOR")){
                        blockContent.put(b2, newContent);
                    }
                    checkContainer(p, container,isUsingCrowbar);
                    this.cancel();
                }
                step += 1;
            }
        };
        long timer = 4L;
        if(isUsingCrowbar){
            timer = 3L;
        }
        check.runTaskTimer(plugin,0L,timer);
    }
    @EventHandler
    public void itemParticle(ItemSpawnEvent spawnEvent) {
        Item item = spawnEvent.getEntity();
        World w = item.getWorld();
        ItemStack stack = item.getItemStack();
        int rarity = lp.getRarity(stack);
        if (rarity > 0) {
            BukkitRunnable particle = new BukkitRunnable() {
                @Override
                public void run() {
                    if (item.isDead()) {
                        this.cancel();
                    }
                    Particle p = switch (rarity){
                        case 1 -> Particle.COPPER_FIRE_FLAME;
                        case 2 -> Particle.SOUL_FIRE_FLAME;
                        case 3 -> Particle.REVERSE_PORTAL;
                        case 4 -> Particle.FLAME;
                        case 5 -> Particle.RAID_OMEN;
                        case 6 -> Particle.SCULK_SOUL;
                        default -> Particle.CLOUD;
                    };
                    if(rarity == 5){
                        w.spawnParticle(p, item.getLocation().add(0, 1.5, 0),
                                0, 0, 1, 0, 0.1);
                        w.spawnParticle(p, item.getLocation().add(0, 0.7, 0),
                                0, 0, 1, 0, 0.1);
                    }
                    w.spawnParticle(p, item.getLocation().add(0, 0.4, 0),
                            0, 0, 1, 0, 0.07 + rarity / 50.0);

                }
            };
            particle.runTaskTimer(plugin, 0L, 3L);
        }
    }
    public String searchProgress(int total,int step){
        StringBuilder progress = new StringBuilder();
        progress.append("搜索进度：");
        progress.append(ChatColor.BOLD);
        for(int i = 0;i < total;i ++){
            if(i < step){
                progress.append("|");
            }else {
                progress.append("·");
            }
        }
        return progress.toString();
    }
    public ItemStack[] smithContent(){
        List<ItemStack>contentList = new ArrayList<>();
        ItemStack[]weapons = wp.getContainerWeapons();
        ItemStack rw = weapons[r.nextInt(weapons.length)];
        contentList.add(rw);
        return contentList.toArray(new ItemStack[0]);
    }
    public ItemStack[] arrowContent() {
        List<ItemStack> contentList = new ArrayList<>();
        ItemStack bow;
        if (r.nextBoolean()) {
            bow = new ItemStack(Material.BOW);
        } else {
            bow = new ItemStack(Material.CROSSBOW);
        }
        contentList.add(bow);
        contentList.add(new ItemStack(Material.ARROW, r.nextInt(4, 17)));
        return contentList.toArray(new ItemStack[0]);
    }
    public ItemStack[] loomContent(){
        List<ItemStack>contentList = new ArrayList<>();
        ItemStack[]armors = ap.getContainerArmors();
        for(int i =0;i < 3;i++){
            contentList.add(armors[r.nextInt(armors.length)]);
        }
        return contentList.toArray(new ItemStack[0]);
    }
    public ItemStack[] potionContent() {
        List<ItemStack> contentList = new ArrayList<>();
        ItemStack[] potions = bp.getPotions();
        contentList.add(potions[r.nextInt(potions.length)]);
        return contentList.toArray(new ItemStack[0]);
    }
}
