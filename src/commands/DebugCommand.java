package commands;

import Universal.ItemPool;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class DebugCommand implements CommandExecutor {
    ItemPool ip = ItemPool.INSTANCE;
    Random r = new Random();
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(commandSender instanceof Player p){
            if(p.isOp()){
                World w = p.getWorld();
                ItemStack[]items = ip.getWeapons();
                for(ItemStack i : items){
                    w.dropItem(p.getLocation(),i);
                }
            }
        }
        return true;
    }
}
