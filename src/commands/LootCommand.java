package commands;

import Universal.LootPool;
import Universal.PlayerStats;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class LootCommand implements CommandExecutor {
    LootPool lp = LootPool.INSTANCE;
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(commandSender instanceof Player p) {
            Inventory inv = Bukkit.createInventory(p, 54,
                    ChatColor.RED + "" + ChatColor.BOLD + "战利品列表");
            ItemStack[] weapons = lp.getAllLoots();
            for (int i = 0; i < 51; i++) {
                if (i >= weapons.length) break;
                inv.setItem(i,weapons[i]);
            }
            inv.setItem(51, lp.pageUp());
            inv.setItem(52, lp.close());
            inv.setItem(53, lp.pageDown());
            PlayerStats.playerMenuStatus.put(p.getName(), PlayerStats.MenuStatus.LOOT_MENU);
            p.openInventory(inv);
        }
        return true;
    }
}
