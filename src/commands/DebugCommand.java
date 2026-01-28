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
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (commandSender instanceof Player p) {
            try {
                if (p.isOp()) {
                    World w = p.getWorld();
                    int num = Integer.parseInt(strings[0]);
                    ItemStack[] items = switch (num) {
                        case 1 -> ap.getContainerArmors();
                        case 2 -> gp.getGadgets();
                        case 3 -> re.getRecipes();
                        case 4 -> lp.getBoxes();
                        default -> wp.getRecipeWeapons();
                    };
                    for (ItemStack i : items) {
                        w.dropItem(p.getLocation(), i);
                    }
                }
            } catch (Exception ignored) {

            }
        }
        return true;
    }
}
