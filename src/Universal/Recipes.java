package Universal;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public enum Recipes {
    INSTANCE;
    JavaPlugin plugin;
    WeaponPool wp = WeaponPool.INSTANCE;
    ArmorPool ap = ArmorPool.INSTANCE;
    GadgetPool gp = GadgetPool.INSTANCE;
    ItemStack[] recipes = new ItemStack[0];
    HashMap<ItemStack,ItemStack>recipeMap = new HashMap<>();
    public void setPlugin(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    public void registerStack(){
        ItemStack[] weapons = wp.getRecipeWeapons();
        ItemStack[] gadgets = gp.getGadgets();
        List<ItemStack>recipeList = new ArrayList<>();
        for(ItemStack i : weapons) {
            ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
            ItemMeta itemMeta = i.getItemMeta();
            ItemMeta bookMeta = book.getItemMeta();
            if (!itemMeta.hasDisplayName()) continue;
            bookMeta.setDisplayName(itemMeta.getDisplayName()
                    + ChatColor.GOLD + "配方");
            ArrayList<String> lore = new ArrayList<>();
            lore.add(ChatColor.WHITE + "一本古老的书");
            lore.add(ChatColor.WHITE + "记载着合成" + itemMeta.getDisplayName()
                    + ChatColor.WHITE + "的配方");
            lore.add(ChatColor.WHITE + "放在背包里才能合成对应物品");
            bookMeta.setLore(lore);
            book.setItemMeta(bookMeta);
            recipeMap.put(i,book);
            recipeList.add(book);
        }
        for(ItemStack i : gadgets) {
            ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
            ItemMeta itemMeta = i.getItemMeta();
            ItemMeta bookMeta = book.getItemMeta();
            if (!itemMeta.hasDisplayName()) continue;
            bookMeta.setDisplayName(itemMeta.getDisplayName()
                    + ChatColor.GOLD + "配方");
            ArrayList<String> lore = new ArrayList<>();
            lore.add(ChatColor.WHITE + "一本古老的书");
            lore.add(ChatColor.WHITE + "记载着合成" + itemMeta.getDisplayName()
                    + ChatColor.WHITE + "的配方");
            lore.add(ChatColor.WHITE + "放在背包里才能合成对应物品");
            bookMeta.setLore(lore);
            book.setItemMeta(bookMeta);
            recipeMap.put(i,book);
            recipeList.add(book);
        }
        recipes = recipeList.toArray(new ItemStack[0]);
    }

    public ItemStack[] getRecipes() {
        return recipes.clone();
    }

    public void registerRecipe(){
        NamespacedKey w0 = new NamespacedKey(plugin,"bone_stick");
        ShapedRecipe w0r = new ShapedRecipe(w0,wp.boneStick());
        w0r.shape(" B "," B "," S ");
        w0r.setIngredient('B', Material.BONE_BLOCK);
        w0r.setIngredient('S', Material.BONE);
        Bukkit.addRecipe(w0r);
    }
}
