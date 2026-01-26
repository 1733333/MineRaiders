package Listeners;

import Universal.Kit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class GadgetListener implements Listener {
    JavaPlugin plugin;
    Kit k = Kit.INSTANCE;

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
                    case "§f食物收纳盒":
                    case "§f种子收纳盒":
                    case "§f树苗收纳盒":
                    case "§f药水收纳盒":
                    case "§f唱片收纳盒":
                    case "§f道具收纳盒":
                    case "§f武器收纳盒":
                    case "§f盔甲收纳盒":
                    case "§f魔咒收纳盒":
                    case "§f陶片收纳盒":
                    case "§f号角收纳盒":
                    case "§f纹饰收纳盒":
                    case "§f钥匙收纳盒":
                    case "§f配方收纳盒":
                }
            }
        } else if (offHand.getType() != Material.AIR) {

        }
    }
}
