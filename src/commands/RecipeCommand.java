package commands;

import Universal.LootPool;
import Universal.PlayerStats;
import Universal.Recipes;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class RecipeCommand implements CommandExecutor {
    LootPool lp = LootPool.INSTANCE;
    Recipes re = Recipes.INSTANCE;
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(commandSender instanceof Player p) {
            Inventory inv = Bukkit.createInventory(p, 54,
                    ChatColor.RED + "" + ChatColor.BOLD + "配方列表|点击物品即可查询配方");
            ItemStack[] weapons = re.getRecipeItems();
            for (int i = 0; i < 52; i++) {
                if (i >= weapons.length) break;
                inv.setItem(i,weapons[i]);
            }
            inv.setItem(52, lp.pageUp());
            inv.setItem(53, lp.pageDown());
            PlayerStats.playerMenuStatus.put(p.getName(), PlayerStats.MenuStatus.RECIPE_MENU);
            p.openInventory(inv);
        }
        return true;
    }
}
