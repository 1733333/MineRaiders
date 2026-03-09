package commands;

import Universal.ArmorPool;
import Universal.GadgetPool;
import Universal.LootPool;
import Universal.PlayerStats;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ArmorCommand implements CommandExecutor {
    LootPool lp = LootPool.INSTANCE;
    ArmorPool ap = ArmorPool.INSTANCE;
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(commandSender instanceof Player p) {
            Inventory inv = Bukkit.createInventory(p, 54,
                    ChatColor.RED + "" + ChatColor.BOLD + "盔甲列表");
            ItemStack[] weapons = ap.getRecipeArmors();
            for (int i = 0; i < 51; i++) {
                if (i >= weapons.length) break;
                inv.setItem(i,weapons[i]);
            }
            inv.setItem(51, lp.pageUp());
            inv.setItem(52, lp.close());
            inv.setItem(53, lp.pageDown());
            PlayerStats.playerMenuStatus.put(p.getName(), PlayerStats.MenuStatus.ARMOR_MENU);
            p.openInventory(inv);
        }
        return true;
    }
}
