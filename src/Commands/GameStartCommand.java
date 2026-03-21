package Commands;

import Listeners.InventoryListener;
import Universal.PlayerStats;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

import static Universal.PlayerStats.playerMenuStatus;

public class GameStartCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (commandSender instanceof Player p) {
            openStartMenu(p);
        }
        return true;
    }

    public void openStartMenu(Player p){
        Inventory inv = Bukkit.createInventory(p, 9, ChatColor.GOLD +""+ ChatColor.BOLD + "选择地图");
        inv.addItem(xc2());
        p.openInventory(inv);
        playerMenuStatus.put(p.getName(), PlayerStats.MenuStatus.MAP_MENU);
    }

    public ItemStack xc2(){
        ItemStack item = new ItemStack(Material.LODESTONE);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.AQUA + "星辰山(测试版)");
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "开始游戏");
        lore.add(ChatColor.WHITE + "地图：星辰山(测试版)");
        lore.add(ChatColor.YELLOW + "地图提供者");
        lore.add(ChatColor.YELLOW + "woaibengkuiji");
        lore.add(ChatColor.YELLOW + "__007__");
        lore.add(ChatColor.YELLOW + "YUJIE69");
        lore.add(ChatColor.YELLOW + "以及其他参与地图建设");
        lore.add(ChatColor.YELLOW + "但是我记不住名字怎么拼的人");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }

}