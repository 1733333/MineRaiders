package Commands;

import Universal.LootPool;
import Universal.PlayerStats;
import Universal.WeaponPool;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class WeaponCommand implements CommandExecutor {
    LootPool lp = LootPool.INSTANCE;
    WeaponPool wp = WeaponPool.INSTANCE;
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(commandSender instanceof Player p) {
            Inventory inv = Bukkit.createInventory(p, 54,
                    ChatColor.RED + "" + ChatColor.BOLD + "武器列表");
            ItemStack[] weapons = wp.getPluginWeapons();
            for (int i = 0; i < 51; i++) {
                if (i >= weapons.length) break;
                inv.setItem(i,weapons[i]);
            }
            inv.setItem(51, lp.pageUp());
            inv.setItem(52, lp.close());
            inv.setItem(53, lp.pageDown());
            p.openInventory(inv);
            PlayerStats.playerMenuStatus.put(p.getName(), PlayerStats.MenuStatus.WEAPON_MENU);
        }
        return true;
    }
}
