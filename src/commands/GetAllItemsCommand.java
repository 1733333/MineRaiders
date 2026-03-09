package commands;

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

public class GetAllItemsCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(commandSender instanceof Player p){
            Inventory inv = Bukkit.createInventory(p,9, ChatColor.AQUA + "物品图鉴");
            inv.setItem(0,armors());
            inv.setItem(1,weapons());
            inv.setItem(2,gadgets());
            inv.setItem(3,drops());
            inv.setItem(4,loots());
            inv.setItem(5,recipe());
            inv.setItem(6,freeRecipe());
            PlayerStats.playerMenuStatus.put(p.getName(), PlayerStats.MenuStatus.COOKBOOK_MENU);
            p.openInventory(inv);
        }
        return true;
    }
    public ItemStack armors() {
        ItemStack item = new ItemStack(Material.IRON_CHESTPLATE);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.AQUA + "装备图鉴");
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "点击打开装备图鉴");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack weapons() {
        ItemStack item = new ItemStack(Material.IRON_SWORD);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.AQUA + "武器图鉴");
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "点击打开武器图鉴");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack drops() {
        ItemStack item = new ItemStack(Material.SKELETON_SKULL);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.AQUA + "掉落物图鉴");
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "点击打开掉落物图鉴");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack freeRecipe() {
        ItemStack item = new ItemStack(Material.WRITABLE_BOOK);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.AQUA + "免费配方图鉴");
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "点击打开免费配方图鉴");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack recipe() {
        ItemStack item = new ItemStack(Material.WRITTEN_BOOK);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.AQUA + "配方图鉴");
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "点击打开配方图鉴");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack gadgets() {
        ItemStack item = new ItemStack(Material.CREEPER_SPAWN_EGG);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.AQUA + "道具图鉴");
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "点击打开道具图鉴");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack loots() {
        ItemStack item = new ItemStack(Material.CHEST);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.AQUA + "战利品图鉴");
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "点击打开战利品图鉴");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
}
