package commands;

import Universal.ArmorPool;
import Universal.GadgetPool;
import Universal.WeaponPool;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class DebugCommand implements CommandExecutor {
    WeaponPool wp = WeaponPool.INSTANCE;
    ArmorPool ap = ArmorPool.INSTANCE;
    GadgetPool gp = GadgetPool.INSTANCE;
    Random r = new Random();
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (commandSender instanceof Player p) {
            try {
                if (p.isOp()) {
                    World w = p.getWorld();
                    int num = Integer.parseInt(strings[0]);
                    ItemStack[] items = switch (num) {
                        case 1 -> ap.getArmors();
                        case 2 -> gp.getGadgets();
                        default -> wp.getRecipeWeapons();
                    };
                    for (ItemStack i : items) {
                        w.dropItem(p.getLocation(), i);
                    }
                }
            } catch (Exception ignored) {

            }
        }
        return false;
    }
}
