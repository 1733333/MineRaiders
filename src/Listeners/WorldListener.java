package Listeners;

import Universal.LootPool;
import Universal.Kit;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.*;

public class WorldListener implements Listener {
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
            Material.BREWING_STAND,
            Material.FURNACE,
            Material.BARREL,
            Material.BEE_NEST,
            Material.LECTERN
    };
    Material[] bestContainer = {
            Material.CRAFTER,
            Material.BLAST_FURNACE,
            Material.SMOKER,
    };
    Kit k = Kit.INSTANCE;
    LootPool lp = LootPool.INSTANCE;
    Random r = new Random();
    JavaPlugin plugin;
    HashSet<Block> hasContent = new HashSet<>();
    HashMap<Block, ItemStack[]>blockContent = new HashMap<>();
    HashMap<Block,Player>blockSearcherMap = new HashMap<>();
    HashMap<Player,Block>playerSearchBlockMap = new HashMap<>();

    public void setPlugin(JavaPlugin plugin) {
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
        return -1;
    }

    public float[] getContainerValue(Block b) {
        return switch (getContainerRarity(b)) {
            case 0 -> new float[]{5f, 3f, 1.375f, 0.55f, 0.05f, 0.025f};
            case 1 -> new float[]{3.75f, 2.75f, 1.75f, 1.5f, 0.15f, 0.1f};
            case 2 -> new float[]{2f, 2f, 2f, 2f, 1.25f, 0.75f};
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
                max = 7;
                chance = 0.95;
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
        World w = p.getWorld();
        Action action = interactEvent.getAction();
        if (action.equals(Action.RIGHT_CLICK_BLOCK)) {
            Block b = interactEvent.getClickedBlock();
            if(getContainerRarity(b)>=0) {
                interactEvent.setCancelled(true);
                openContainer(p, b);
            }
        }
    }
    public void openContainer(Player p,Block container){
        float[] weights = getContainerValue(container);
        Player searcher = blockSearcherMap.getOrDefault(container,null);
        if(searcher == null) {
            if (p.getCooldown(container.getType()) == 0) {
                if (weights.length > 0) {
                    hasContent.add(container);
                    blockSearcherMap.put(container, p);
                    p.setCooldown(container.getType(), 10);
                    ItemStack[] content = blockContent.getOrDefault(container, new ItemStack[0]);
                    if (content.length == 0) {
                        int count = getContainerCount(container);
                        content = lp.getContent(count, weights);
                        blockContent.put(container, content);
                    }
                    checkContainer(p, container);
                }
            }
        }else {
            if(searcher.equals(p)){
                p.sendTitle("",ChatColor.AQUA + "正在搜索该容器",10,10,10);
            }else {
                p.sendTitle("",ChatColor.AQUA + "其他人正在搜索该容器",10,10,10);
            }
        }
    }

    public void checkContainer(Player p,Block container){
        World w = p.getWorld();
        ItemStack[]content = blockContent.getOrDefault(container,new ItemStack[0]);
        if(content.length == 0) {
            hasContent.remove(container);
            blockContent.remove(container);
            blockSearcherMap.remove(container);
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
                                TextComponent.fromLegacy(ChatColor.RED + "搜索中断"));
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
                int rarity = lp.getRarity(item);
                int bound = 5 + rarity;
                String message = searchProgress(bound,count);
                p.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                        TextComponent.fromLegacy(ChatColor.AQUA + message
                                + "(" + content.length +")"));
                if(count >= bound){
                    Sound s = switch (rarity){
                        case 0,1,2 -> Sound.UI_LOOM_TAKE_RESULT;
                        case 3->Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
                        case 4->Sound.ENTITY_PLAYER_LEVELUP;
                        case 5->Sound.UI_TOAST_CHALLENGE_COMPLETE;
                        default -> Sound.ENTITY_ITEM_PICKUP;
                    };
                    p.playSound(p,s,1,1);
                    Location bLoc = container.getLocation();
                    Location pLoc = p.getEyeLocation();
                    Vector offSet = pLoc.toVector().subtract(bLoc.toVector());
                    w.dropItem(bLoc.add(offSet.multiply(0.5)),item);
                    w.spawnParticle(Particle.EXPLOSION,container.getLocation(),1);
                    w.spawnParticle(Particle.BLOCK,container.getLocation()
                            ,50,1.5,1.5,1.5,container.getBlockData());
                    ItemStack[]newContent = new ItemStack[content.length-1];
                    System.arraycopy(content, 1, newContent, 0, newContent.length);
                    blockContent.put(container,newContent);
                    checkContainer(p,container);
                    this.cancel();
                }
                step += 1;
            }
        };
        check.runTaskTimer(plugin,0L,4L);
    }
    public String searchProgress(int total,int step){
        StringBuilder progress = new StringBuilder();
        progress.append("搜索进度：");
        progress.append(ChatColor.BOLD);
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
