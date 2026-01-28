package Listeners;

import Universal.*;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

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

    public void setPlugin(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    @EventHandler
    public void playerInteract(PlayerInteractEvent interactEvent) {
        Action action = interactEvent.getAction();
        Player p = interactEvent.getPlayer();
        if(p.getGameMode() == GameMode.SPECTATOR)return;
        ItemStack hand = p.getInventory().getItemInMainHand();
        ItemStack offHand = p.getInventory().getItemInOffHand();
        boolean rightClick = action.equals(Action.RIGHT_CLICK_AIR)
                || action.equals(Action.RIGHT_CLICK_BLOCK);
        if (hand.getType() != Material.AIR) {
            String tag = k.getLore(hand);
            if (rightClick) {
                switch (tag) {
                }
                if(tag.contains("收纳盒")){
                    openBox(p,hand,tag);
                }
            }
        } else if (offHand.getType() != Material.AIR) {

        }
    }
    public void openBox(Player p,ItemStack hand,String tag) {
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
                loots = rp.getRecipes();
                break;
            default:
                return;
        }
        if(loots.length > 0) {
            w.playSound(p, Sound.BLOCK_SHULKER_BOX_OPEN, 1, 1);
            for(int i = 0; i < amount;i ++) {
                w.dropItem(p.getLocation(), loots[r.nextInt(loots.length)]);
            }
            if (!p.getGameMode().equals(GameMode.CREATIVE)) {
                int count = hand.getAmount();
                hand.setAmount(count - 1);
            }
        }
    }
}
