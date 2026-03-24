package Commands;

import OtherStuff.KiryuKazuma;
import Universal.*;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class MineRaidersCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final Map<String, CommandExecutor> subCommands = new HashMap<>();

    public MineRaidersCommand(JavaPlugin plugin) {
        this.plugin = plugin;
        // 初始化子命令执行器
        subCommands.put("armor", new ArmorCommand());
        subCommands.put("drop", new DropCommand());
        subCommands.put("freerecipe", new FreeRecipeCommand());
        subCommands.put("gadget", new GadgetCommand());
        subCommands.put("gameend", new GameEndCommand());
        subCommands.put("gamestart", new GameStartCommand());
        subCommands.put("getallitems", new GetAllItemsCommand());
        subCommands.put("lobby", new LobbyCommand(plugin));
        subCommands.put("loot", new LootCommand());
        subCommands.put("recipe", new RecipeCommand());
        subCommands.put("weapon", new WeaponCommand());
        subCommands.put("summondamagetester", new SummonDamageTester());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player p) {
                openMainMenu(p);
            } else {
                sender.sendMessage("§c该指令只能由玩家执行！");
            }
            return true;
        }

        String sub = args[0].toLowerCase();

        // 特殊处理 debug 子命令（内嵌逻辑）
        if (sub.equals("debug")) {
            return handleDebug(sender, args);
        }

        // 其他子命令
        CommandExecutor executor = subCommands.get(sub);
        if (executor != null) {
            // 构造新参数（去掉第一个子命令名）
            String[] newArgs = new String[args.length - 1];
            System.arraycopy(args, 1, newArgs, 0, newArgs.length);
            return executor.onCommand(sender, command, sub, newArgs);
        } else {
            sender.sendMessage(ChatColor.RED + "未知的子命令！使用 /mr 打开主菜单。");
            return true;
        }
    }

    // ========================== 主菜单 ==========================
    private void openMainMenu(Player p) {
        Inventory inv = Bukkit.createInventory(p, 27, ChatColor.GOLD + "" + ChatColor.BOLD + "MineRaiders 主菜单");

        // 第一行：图鉴类
        inv.setItem(0, createMenuItem(Material.IRON_CHESTPLATE, "§b装备图鉴", "查看所有盔甲"));
        inv.setItem(1, createMenuItem(Material.IRON_SWORD, "§b武器图鉴", "查看所有武器"));
        inv.setItem(2, createMenuItem(Material.CREEPER_SPAWN_EGG, "§b道具图鉴", "查看所有道具"));
        inv.setItem(3, createMenuItem(Material.SKELETON_SKULL, "§b掉落物图鉴", "查看所有掉落物"));
        inv.setItem(4, createMenuItem(Material.CHEST, "§b战利品图鉴", "查看所有战利品"));
        inv.setItem(5, createMenuItem(Material.WRITTEN_BOOK, "§b配方图鉴", "查看需要合成的物品配方"));
        inv.setItem(6, createMenuItem(Material.WRITABLE_BOOK, "§b免费配方图鉴", "查看无需合成的物品配方"));
        inv.setItem(7, createMenuItem(Material.KNOWLEDGE_BOOK, "§b物品总览", "查看所有物品分类"));

        // 第二行：游戏与功能
        inv.setItem(9, createMenuItem(Material.LODESTONE, "§a开始游戏", "选择地图并开始游戏"));
        inv.setItem(10, createMenuItem(Material.NETHER_STAR, "§a大厅", "选择游戏世界并加入/观战"));
        inv.setItem(11, createMenuItem(Material.BARRIER, "§c强制结束游戏", "结束当前游戏 (OP)"));
        inv.setItem(12, createMenuItem(Material.COMMAND_BLOCK, "§d调试菜单", "打开调试功能菜单 (OP)"));
        inv.setItem(13, createMenuItem(Material.ARMOR_STAND, "§e伤害测试假人", "在脚下召唤一个伤害测试假人"));
        inv.setItem(14, createMenuItem(Material.BOOK, "§7帮助", "查看命令帮助"));

        p.openInventory(inv);
        PlayerStats.playerMenuStatus.put(p.getName(), PlayerStats.MenuStatus.MAIN_MENU);
    }

    private ItemStack createMenuItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (lore.length > 0) {
            meta.setLore(Arrays.asList(lore));
        }
        item.setItemMeta(meta);
        return item;
    }

    // ========================== Debug 子命令逻辑 ==========================
    private boolean handleDebug(CommandSender sender, String[] args) {
        if (!(sender instanceof Player p) || !p.isOp()) {
            sender.sendMessage(ChatColor.RED + "你没有权限使用此命令！");
            return true;
        }

        // 无参数：打开调试菜单
        if (args.length == 1) {
            openDebugMenu(p);
            return true;
        }

        // 有参数：执行调试动作
        int num;
        try {
            num = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            p.sendMessage(ChatColor.RED + "参数必须为数字！");
            return true;
        }

        World w = p.getWorld();
        ItemStack hand = p.getEquipment().getItemInMainHand();

        Monsters m = Monsters.INSTANCE;
        GameStatus gameStatus = GameStatus.INSTANCE;

        switch (num) {
            case 0 -> w.dropItem(p.getLocation(), debugFlute());
            case 1 -> damageTest(p.getLocation());
            case 2 -> {
                Bukkit.resetRecipes();
                Bukkit.broadcastMessage(ChatColor.RED + "自定义配方已清除");
            }
            case 3 -> {
                gameStatus.refillContainers(w);
                Bukkit.broadcastMessage(ChatColor.GREEN + "所有容器的状态已经刷新");
            }
            case 4 -> m.shredder(p.getLocation());
            case 5 -> m.flea(p.getLocation());
            case 6 -> m.pop(p.getLocation());
            case 7 -> m.fireBall(p.getLocation());
            case 8 -> m.snitch(p.getLocation());
            case 9 -> m.leaper(p.getLocation());
            case 10 -> m.bastion(p.getLocation());
            case 11 -> m.dukeMinion(p.getLocation(), false);
            case 12 -> m.duke(p.getLocation());
            case 13 -> mimic(p.getLocation(), hand);
            case 14 -> {
                w.strikeLightningEffect(p.getLocation());
                KiryuKazuma kiryuKazuma = new KiryuKazuma(plugin);
                kiryuKazuma.spawnBoss(p.getLocation());
            }
            default -> p.sendMessage(ChatColor.RED + "无效的数字参数！");
        }
        return true;
    }

    private void openDebugMenu(Player p) {
        Inventory inv = Bukkit.createInventory(p, 27,
                ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "测试菜单");
        inv.addItem(debugItem(0, "获得荒野大笛客"));
        inv.addItem(debugItem(1, "召唤伤害测试假人"));
        inv.addItem(debugItem(2, "清除自定义配方"));
        inv.addItem(debugItem(3, "重置容器状态"));
        inv.addItem(debugItem(4, "召唤粉碎者"));
        inv.addItem(debugItem(5, "召唤跳蚤"));
        inv.addItem(debugItem(6, "召唤爆爆"));
        inv.addItem(debugItem(7, "召唤火球"));
        inv.addItem(debugItem(8, "召唤告密者"));
        inv.addItem(debugItem(9, "召唤跳跃者"));
        inv.addItem(debugItem(10, "召唤堡垒"));
        inv.addItem(debugItem(11, "召唤机魂"));
        inv.addItem(debugItem(12, "召唤公爵"));
        inv.addItem(debugItem(13, "召唤模仿者"));
        inv.addItem(debugItem(14, "召唤“桐生一马”"));
        p.openInventory(inv);
        PlayerStats.playerMenuStatus.put(p.getName(), PlayerStats.MenuStatus.DEV_MENU);
    }

    private ItemStack debugItem(int id, String name) {
        Material[] mats = {
                Material.STICK, Material.IRON_SWORD, Material.WRITABLE_BOOK, Material.CHEST,
                Material.OBSERVER, Material.FERMENTED_SPIDER_EYE, Material.CREEPER_HEAD,
                Material.MAGMA_CREAM, Material.SPYGLASS, Material.FIREWORK_STAR,
                Material.CREAKING_HEART, Material.LODESTONE, Material.DISPENSER,
                Material.PLAYER_HEAD, Material.DRAGON_HEAD
        };
        ItemStack item = new ItemStack(mats[id]);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + name);
        meta.setLore(Collections.singletonList(ChatColor.WHITE + "点击执行指令"));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack debugFlute() {
        ItemStack item = new ItemStack(Material.STICK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "荒野大笛客");
        meta.setMaxStackSize(4);
        meta.setLore(Arrays.asList(ChatColor.WHITE + "荒野大笛客", ChatColor.WHITE + "可以用来感化口人磨"));
        item.setItemMeta(meta);
        return item;
    }

    private void damageTest(Location loc) {
        World w = loc.getWorld();
        WitherSkeleton s = (WitherSkeleton) w.spawnEntity(loc, EntityType.WITHER_SKELETON);
        s.setCustomName(ChatColor.RED + "伤害测试假人——真刃刀魔");
        s.setSilent(true);
        Objects.requireNonNull(s.getAttribute(Attribute.MOVEMENT_SPEED)).setBaseValue(0);
        Objects.requireNonNull(s.getAttribute(Attribute.ATTACK_DAMAGE)).setBaseValue(0);
        Objects.requireNonNull(s.getAttribute(Attribute.MAX_HEALTH)).setBaseValue(100);
        s.setHealth(100);
    }

    private void mimic(Location loc, ItemStack hand) {
        World w = loc.getWorld();
        Skeleton s = (Skeleton) w.spawnEntity(loc, EntityType.SKELETON);
        EntityEquipment e = s.getEquipment();
        e.clear();
        e.setHelmet(ArmorPool.INSTANCE.mobHelm(Color.LIME));
        e.setItemInMainHand(hand);
        s.setCustomName(ChatColor.GREEN + "模仿者");
        s.setCustomNameVisible(true);
    }
}