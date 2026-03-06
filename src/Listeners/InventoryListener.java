package Listeners;

import Universal.*;
import org.bukkit.*;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static Universal.PlayerStats.playerMenuStatus;

public class InventoryListener implements Listener {
    LootPool lp = LootPool.INSTANCE;
    WeaponPool wp = WeaponPool.INSTANCE;
    GadgetPool gp = GadgetPool.INSTANCE;
    ArmorPool ap = ArmorPool.INSTANCE;
    Recipes re = Recipes.INSTANCE;
    DropPool dp = DropPool.INSTANCE;
    Kit k = Kit.INSTANCE;
    PlayerStats playerStats = PlayerStats.INSTANCE;
    HashMap<String, Integer> playerPage = new HashMap<>();
    HashMap<String, PlayerStats.MenuStatus> playerPreviousStatus = new HashMap<>();
    JavaPlugin plugin;

    public void setPlugin(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void invClick(InventoryClickEvent clickEvent) {
        Player p = (Player) clickEvent.getWhoClicked();
        if(playerStats.isDying(p)){
            clickEvent.setCancelled(true);
            return;
        }
        World w = p.getWorld();
        String name = p.getName();
        PlayerStats.MenuStatus status = playerMenuStatus.getOrDefault(name, PlayerStats.MenuStatus.NOT_MENU);
        int slot = clickEvent.getRawSlot();
        ItemStack item = clickEvent.getCurrentItem();
        if (item == null) return;
        if (status != PlayerStats.MenuStatus.NOT_MENU) {
            if (slot < 54) {
                ItemStack[] stack = switch (status) {
                    case LOOT_MENU -> lp.getAllLoots();
                    case WEAPON_MENU -> wp.getPluginWeapons();
                    case ARMOR_MENU -> ap.getRecipeArmors();
                    case GADGET_MENU -> gp.getGadgets();
                    case RECIPE_MENU -> re.getMenuItems();
                    case FREE_RECIPE_MENU -> re.getFreeRecipeItems();
                    case DROP_MENU -> dp.getAllDrops();
                    default -> new ItemStack[0];
                };
                String invName = switch (status){
                    case LOOT_MENU -> ChatColor.RED + "" + ChatColor.BOLD + "战利品列表";
                    case WEAPON_MENU -> ChatColor.RED + "" + ChatColor.BOLD + "武器列表";
                    case ARMOR_MENU -> ChatColor.RED + "" + ChatColor.BOLD + "盔甲列表";
                    case GADGET_MENU -> ChatColor.RED + "" + ChatColor.BOLD + "道具列表";
                    case RECIPE_MENU -> ChatColor.RED + "" + ChatColor.BOLD + "配方列表|点击物品即可查询配方";
                    case FREE_RECIPE_MENU -> ChatColor.RED + "" + ChatColor.BOLD + "免费配方列表|点击物品即可查询配方";
                    case DROP_MENU -> ChatColor.RED + "" + ChatColor.BOLD + "掉落物列表";
                    default -> "";
                };
                clickEvent.setCancelled(true);
                if (slot == 52) {
                    changePage(p, stack, true, invName,status);
                } else if (slot == 53) {
                    changePage(p, stack, false, invName,status);
                }
                if (slot < 52) {
                    if (status == PlayerStats.MenuStatus.RECIPE_MENU ||
                            status == PlayerStats.MenuStatus.FREE_RECIPE_MENU) {
                        HashMap<String,Integer>keyMap = re.getRecipeKeys();
                        int key = keyMap.getOrDefault(item.getItemMeta().getDisplayName(),-1);
                        if(key != -1) {
                            NamespacedKey k = NamespacedKey.fromString("r" + key, plugin);
                            Recipe r = Bukkit.getRecipe(k);
                            Inventory inv = Bukkit.createInventory(p, InventoryType.WORKBENCH,
                                    ChatColor.RED + "" + ChatColor.BOLD + "配方");
                            inv.setItem(0, item);
                            if(r instanceof ShapedRecipe sr) {
                                ItemStack[] content = re.getRecipeFlat(sr);
                                for (int i = 0; i < 9; i++) {
                                    if (i >= content.length) break;
                                    ItemStack itemStack = content[i];
                                    if (itemStack == null) continue;
                                    inv.setItem(i + 1, itemStack);
                                }
                            }
                            if(r instanceof ShapelessRecipe sl){
                                List<ItemStack> content = sl.getIngredientList();
                                for (int i = 0; i < 9; i++) {
                                    if (i >= content.size()) break;
                                    ItemStack itemStack = content.get(i);
                                    if (itemStack == null) continue;
                                    inv.setItem(i + 1, itemStack);
                                }
                            }
                            p.openInventory(inv);
                            playerPreviousStatus.put(p.getName(), status);
                            playerMenuStatus.put(p.getName(), PlayerStats.MenuStatus.CRAFTING_MENU);
                        }
                    } else if(status != PlayerStats.MenuStatus.CRAFTING_MENU) {
                        if (p.isOp()) {
                            Item i = w.dropItem(p.getLocation(), item);
                            i.setPickupDelay(0);
                        }
                    }
                }
            }
        }
    }
    @EventHandler
    public void invClose(InventoryCloseEvent closeEvent) {
        Player p = (Player) closeEvent.getPlayer();
        String name = p.getName();
        PlayerStats.MenuStatus status = playerPreviousStatus.getOrDefault(name, PlayerStats.MenuStatus.NOT_MENU);
        PlayerStats.MenuStatus pStatus = playerPreviousStatus.getOrDefault(name, PlayerStats.MenuStatus.NOT_MENU);
        if(status == PlayerStats.MenuStatus.CRAFTING_MENU) {
            if (pStatus == PlayerStats.MenuStatus.RECIPE_MENU) {
                p.performCommand("getrecipes");
            }
            if (pStatus == PlayerStats.MenuStatus.FREE_RECIPE_MENU) {
                p.performCommand("getfreerecipes");
            }
        }
        playerMenuStatus.put(name, PlayerStats.MenuStatus.NOT_MENU);
        playerPage.remove(name);
    }

    public void changePage(Player p, ItemStack[]stacks, boolean pageUp, String name, PlayerStats.MenuStatus status){
        int currentPage = playerPage.getOrDefault(p.getName(),0);
        int maxPage = lp.getAllLoots().length / 52;
        if(pageUp){
            if(currentPage == 0){
                p.playSound(p.getEyeLocation(),Sound.ENCHANT_THORNS_HIT,1,1);
            }else {
                p.playSound(p.getEyeLocation(),Sound.ITEM_BOOK_PAGE_TURN,1,1);
                Inventory inv = Bukkit.createInventory(p,54,name);
                int positiveBound = currentPage * 52;
                int negativeBound = (currentPage - 1) * 52;
                int slot = 0;
                for(int i = negativeBound; i < positiveBound;i ++){
                    if(i >= stacks.length)break;
                    inv.setItem(slot,stacks[i]);
                    slot ++;
                }
                inv.setItem(52,lp.pageUp());
                inv.setItem(53,lp.pageDown());
                p.openInventory(inv);
                currentPage -= 1;
                playerPage.put(p.getName(),currentPage);
            }
        }else {
            if(currentPage + 1 > maxPage){
                p.playSound(p.getEyeLocation(),Sound.ENCHANT_THORNS_HIT,1,1);
            }else {
                currentPage += 1;
                p.playSound(p.getEyeLocation(),Sound.ITEM_BOOK_PAGE_TURN,1,1);
                Inventory inv = Bukkit.createInventory(p,54,name);
                int positiveBound = (currentPage + 1) * 52;
                int negativeBound = currentPage * 52;
                int slot = 0;
                for(int i = negativeBound; i < positiveBound;i ++){
                    if(i >= stacks.length)break;
                    inv.setItem(slot,stacks[i]);
                    slot ++;
                }
                inv.setItem(52,lp.pageUp());
                inv.setItem(53,lp.pageDown());
                p.openInventory(inv);
                playerPage.put(p.getName(),currentPage);
            }
        }
        playerMenuStatus.put(p.getName(), status);
    }

}
