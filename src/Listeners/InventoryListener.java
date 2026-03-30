package Listeners;

import Events.GameEndEvent;
import Events.GameStartEvent;
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
import org.bukkit.scheduler.BukkitRunnable;

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
    GameStatus gameStatus = GameStatus.INSTANCE;
    HashMap<String, Integer> playerPage = new HashMap<>();
    HashMap<String, PlayerStats.MenuStatus> playerPreviousStatus = new HashMap<>();
    JavaPlugin plugin;

    public InventoryListener(JavaPlugin plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void invClick(InventoryClickEvent clickEvent) {
        Player p = (Player) clickEvent.getWhoClicked();
        if (playerStats.isDying(p)) {
            clickEvent.setCancelled(true);
            return;
        }
        World w = p.getWorld();
        String name = p.getName();
        PlayerStats.MenuStatus status = playerMenuStatus.getOrDefault(name, PlayerStats.MenuStatus.NOT_MENU);
        int slot = clickEvent.getRawSlot();
        ItemStack item = clickEvent.getCurrentItem();
        if (item == null) return;
        if (status == PlayerStats.MenuStatus.MAIN_MENU) {
            clickEvent.setCancelled(true);
            String displayName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
            switch (displayName) {
                case "装备图鉴" -> p.performCommand("mr armor");
                case "武器图鉴" -> p.performCommand("mr weapon");
                case "道具图鉴" -> p.performCommand("mr gadget");
                case "掉落物图鉴" -> p.performCommand("mr drop");
                case "战利品图鉴" -> p.performCommand("mr loot");
                case "配方图鉴" -> p.performCommand("mr recipe");
                case "免费配方图鉴" -> p.performCommand("mr freerecipe");
                case "物品总览" -> p.performCommand("mr getallitems");
                case "开始游戏" -> p.performCommand("mr gamestart");
                case "大厅" -> p.performCommand("mr lobby");
                case "强制结束游戏" -> p.performCommand("mr gameend");
                case "调试菜单" -> p.performCommand("mr debug");
                case "伤害测试假人" -> p.performCommand("mr summondamagetester");
                case "地点管理" -> p.performCommand("mr locs gui");
                case "帮助" ->
                        p.sendMessage("§e使用 §f/mr <子命令> §e查看详细用法，子命令包括: armor, weapon, gadget, drop, loot, recipe, freerecipe, getallitems, gamestart, lobby, gameend, debug, summondamagetester");
                case "123456789" -> k.esterEgg0(p.getLocation());
            }
            return;
        }
        if(status == PlayerStats.MenuStatus.MAP_MENU){
            clickEvent.setCancelled(true);
            World world = Bukkit.getWorld(gameStatus.getWorlds(slot));
            if(world != null) {
                Bukkit.getPluginManager().callEvent(new GameStartEvent(world));
            }else {
                Bukkit.broadcastMessage(ChatColor.RED + "世界不存在！请联系管理员寻求帮助");
            }
            return;
        }
        if (status == PlayerStats.MenuStatus.COOKBOOK_MENU) {
            clickEvent.setCancelled(true);
            p.closeInventory();
            BukkitRunnable later = new BukkitRunnable() {
                @Override
                public void run() {
                    String command = switch (slot) {
                        case 0 -> "mr armor";
                        case 1 -> "mr weapon";
                        case 2 -> "mr gadget";
                        case 3 -> "mr drop";
                        case 4 -> "mr loot";
                        case 5 -> "mr recipe";
                        case 6 -> "mr freerecipe";
                        default -> "";
                    };
                    if (!command.isEmpty()) {
                        p.performCommand(command);
                    }
                }
            };
            later.runTaskLater(plugin, 1L);
            return;
        }
        if(status == PlayerStats.MenuStatus.DEV_MENU){
            clickEvent.setCancelled(true);
            p.closeInventory();
            p.performCommand("mrd " + slot);
            return;
        }
        if (status != PlayerStats.MenuStatus.NOT_MENU) {
            if (slot < 54) {
                ItemStack[] stack = switch (status) {
                    case LOOT_MENU -> lp.getAllLoots();
                    case WEAPON_MENU -> wp.getPluginWeapons();
                    case ARMOR_MENU -> ap.getRecipeArmors();
                    case GADGET_MENU -> gp.getGadgets();
                    case RECIPE_MENU -> re.getRecipeItems();
                    case FREE_RECIPE_MENU -> re.getFreeRecipeItems();
                    case DROP_MENU -> dp.getAllDrops();
                    default -> new ItemStack[0];
                };
                String invName = switch (status) {
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
                if (slot == 51) {
                    changePage(p, stack, true, invName, status);
                } else if (slot == 52) {
                    p.closeInventory();
                    BukkitRunnable later = new BukkitRunnable() {
                        @Override
                        public void run() {
                            p.performCommand("mr getallitems");
                        }
                    };
                    later.runTaskLater(plugin,1L);
                } else if (slot == 53) {
                    changePage(p, stack, false, invName, status);
                }
                if (slot < 51) {
                    if (status == PlayerStats.MenuStatus.RECIPE_MENU ||
                            status == PlayerStats.MenuStatus.FREE_RECIPE_MENU) {
                        HashMap<String, Integer> keyMap = re.getRecipeKeys();
                        int key = keyMap.getOrDefault(item.getItemMeta().getDisplayName(), -1);
                        if (key != -1) {
                            NamespacedKey k = NamespacedKey.fromString("r" + key, plugin);
                            Recipe r = Bukkit.getRecipe(k);
                            Inventory inv = Bukkit.createInventory(p, InventoryType.WORKBENCH,
                                    ChatColor.RED + "" + ChatColor.BOLD + "配方");
                            inv.setItem(0, item);
                            if (r instanceof ShapedRecipe sr) {
                                ItemStack[] content = re.getRecipeFlat(sr);
                                for (int i = 0; i < 9; i++) {
                                    if (i >= content.length) break;
                                    ItemStack itemStack = content[i];
                                    if (itemStack == null) continue;
                                    inv.setItem(i + 1, itemStack);
                                }
                            }
                            if (r instanceof ShapelessRecipe sl) {
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
                    } else if (status != PlayerStats.MenuStatus.CRAFTING_MENU) {
                        if (p.isOp()) {
                            ItemMeta meta = item.getItemMeta();
                            if(meta != null && meta.hasLore()){
                                List<String> lore = meta.getLore();
                                lore.removeIf(line -> line != null && line.contains("§0."));
                                lore.removeIf(line -> line != null && line.contains("§e权数: "));
                                item.setItemMeta(meta);
                            }
                            item.setAmount(1);
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
        if (status == PlayerStats.MenuStatus.CRAFTING_MENU) {
            if (pStatus == PlayerStats.MenuStatus.RECIPE_MENU) {
                p.performCommand("mr recipe");
            }
            if (pStatus == PlayerStats.MenuStatus.FREE_RECIPE_MENU) {
                p.performCommand("mr freerecipe");
            }
        }
        playerMenuStatus.put(name, PlayerStats.MenuStatus.NOT_MENU);
        playerPage.remove(name);
    }
    public void changePage(Player p, ItemStack[] stacks, boolean pageUp, String name, PlayerStats.MenuStatus status) {
        final int PAGE_SIZE = 51;

        int currentPage = playerPage.getOrDefault(p.getName(), 0);
        int totalItems = stacks.length;
        int maxPage = (totalItems == 0) ? 0 : (totalItems - 1) / PAGE_SIZE; // 最大页码（从0开始）

        int newPage;
        if (pageUp) {
            if (currentPage > 0) {
                newPage = currentPage - 1;
            } else {
                p.playSound(p.getEyeLocation(), Sound.ENCHANT_THORNS_HIT, 1, 1);
                return; // 无法向上翻页，直接返回
            }
        } else {
            if (currentPage < maxPage) {
                newPage = currentPage + 1;
            } else {
                p.playSound(p.getEyeLocation(), Sound.ENCHANT_THORNS_HIT, 1, 1);
                return; // 无法向下翻页，直接返回
            }
        }

        // 播放翻页音效
        p.playSound(p.getEyeLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1, 1);

        // 创建新页面库存
        Inventory inv = createPageInventory(p, stacks, newPage, name);
        p.openInventory(inv);

        // 更新玩家页码和菜单状态
        playerPage.put(p.getName(), newPage);
        playerMenuStatus.put(p.getName(), status);
    }

    private Inventory createPageInventory(Player p, ItemStack[] allStacks, int page, String title) {
        Inventory inv = Bukkit.createInventory(p, 54, title);
        int start = page * 51;
        int end = Math.min(start + 51, allStacks.length);
        int slot = 0;
        for (int i = start; i < end; i++) {
            inv.setItem(slot++, allStacks[i]);
        }
        // 放置导航按钮（最后三个槽位）
        inv.setItem(54 - 3, lp.pageUp());
        inv.setItem(54 - 2, lp.close());
        inv.setItem(54 - 1, lp.pageDown());
        return inv;
    }

}
