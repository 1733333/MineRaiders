package commands;

import Universal.*;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import java.util.Random;

public class DebugCommand implements CommandExecutor {
    WeaponPool wp = WeaponPool.INSTANCE;
    ArmorPool ap = ArmorPool.INSTANCE;
    GadgetPool gp = GadgetPool.INSTANCE;
    Recipes re = Recipes.INSTANCE;
    LootPool lp = LootPool.INSTANCE;
    Random r = new Random();
    Monsters m = Monsters.INSTANCE;
    Kit k = Kit.INSTANCE;
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (commandSender instanceof Player p) {
            try {
                if (p.isOp()) {
                    World w = p.getWorld();
                    int num = Integer.parseInt(strings[0]);
                    switch (num){
                        case 0->m.shredder(p.getLocation());
                        case 1->m.flea(p.getLocation());
                        case 2->m.pop(p.getLocation());
                        case 3->m.fireBall(p.getLocation());
                    }
                }
            } catch (Exception ignored) {

            }
        }
        return true;
    }
}
