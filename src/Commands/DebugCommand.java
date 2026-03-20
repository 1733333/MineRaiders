package Commands;

import OtherStuff.KiryuKazuma;
import Universal.*;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Random;

public class DebugCommand implements CommandExecutor {
    WeaponPool wp = WeaponPool.INSTANCE;
    ArmorPool ap = ArmorPool.INSTANCE;
    GadgetPool gp = GadgetPool.INSTANCE;
    Recipes re = Recipes.INSTANCE;
    LootPool lp = LootPool.INSTANCE;
    Random r = new Random();
    Monsters m = Monsters.INSTANCE;
    Kit k = Kit.INSTANCE;
    JavaPlugin plugin;
    KiryuKazuma kiryuKazuma;
    GameStatus gameStatus = GameStatus.INSTANCE;

    public DebugCommand(JavaPlugin plugin) {
        this.plugin = plugin;
        kiryuKazuma = new KiryuKazuma(plugin);
        plugin.getServer().getPluginManager().registerEvents(kiryuKazuma,plugin);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (commandSender instanceof Player p) {
                if (p.isOp()) {
                    if(strings.length == 0){
                        openGUI(p);
                        return true;
                    }
                    World w = p.getWorld();
                    ItemStack hand = p.getEquipment().getItemInMainHand();
                    int num = Integer.parseInt(strings[0]);
                    switch (num){
                        case 0 ->w.dropItem(p.getLocation(),flute());
                        case 1 ->damageTest(p.getLocation());
                        case 2 ->{
                            Bukkit.resetRecipes();
                            Bukkit.broadcastMessage(ChatColor.RED + "自定义配方已清除");
                        }
                        case 3 ->{
                            gameStatus.refillContainers(w);
                            Bukkit.broadcastMessage(ChatColor.GREEN + "所有容器的状态已经刷新");
                        }
                        case 4 ->m.shredder(p.getLocation());
                        case 5 ->m.flea(p.getLocation());
                        case 6 ->m.pop(p.getLocation());
                        case 7 ->m.fireBall(p.getLocation());
                        case 8 ->m.snitch(p.getLocation());
                        case 9 ->m.leaper(p.getLocation());
                        case 10->m.bastion(p.getLocation());
                        case 11->m.dukeMinion(p.getLocation(),false);
                        case 12->m.duke(p.getLocation());
                        case 13->mimic(p.getLocation(),hand);
                        case 14->{
                            w.strikeLightningEffect(p.getLocation());
                            kiryuKazuma.spawnBoss(p.getLocation());
                        }
                    }
                }
        }
        return true;
    }
    public void openGUI(Player p){
        Inventory inv = Bukkit.createInventory(p, 27,
                ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "测试菜单");
        inv.addItem(mrd0());
        inv.addItem(mrd1());
        inv.addItem(mrd2());
        inv.addItem(mrd3());
        inv.addItem(mrd4());
        inv.addItem(mrd5());
        inv.addItem(mrd6());
        inv.addItem(mrd7());
        inv.addItem(mrd8());
        inv.addItem(mrd9());
        inv.addItem(mrd10());
        inv.addItem(mrd11());
        inv.addItem(mrd12());
        inv.addItem(mrd13());
        inv.addItem(mrd14());
        p.openInventory(inv);
    }
    public ItemStack mrd0(){
        ItemStack item = new ItemStack(Material.STICK);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.AQUA + "获得荒野大笛客");
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "点击执行指令");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack mrd1(){
        ItemStack item = new ItemStack(Material.IRON_SWORD);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.AQUA + "召唤伤害测试假人");
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "点击执行指令");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack mrd2(){
        ItemStack item = new ItemStack(Material.WRITABLE_BOOK);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.AQUA + "清除自定义配方");
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "点击执行指令");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack mrd3(){
        ItemStack item = new ItemStack(Material.CHEST);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.AQUA + "重置容器状态");
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "点击执行指令");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack mrd4(){
        ItemStack item = new ItemStack(Material.OBSERVER);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.AQUA + "召唤粉碎者");
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "点击执行指令");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack mrd5(){
        ItemStack item = new ItemStack(Material.FERMENTED_SPIDER_EYE);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.AQUA + "召唤跳蚤");
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "点击执行指令");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack mrd6(){
        ItemStack item = new ItemStack(Material.CREEPER_HEAD);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.AQUA + "召唤爆爆");
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "点击执行指令");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack mrd7(){
        ItemStack item = new ItemStack(Material.MAGMA_CREAM);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.AQUA + "召唤火球");
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "点击执行指令");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack mrd8(){
        ItemStack item = new ItemStack(Material.SPYGLASS);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.AQUA + "召唤告密者");
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "点击执行指令");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack mrd9(){
        ItemStack item = new ItemStack(Material.FIREWORK_STAR);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.AQUA + "召唤跳跃者");
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "点击执行指令");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack mrd10(){
        ItemStack item = new ItemStack(Material.CREAKING_HEART);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.AQUA + "召唤堡垒");
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "点击执行指令");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack mrd11(){
        ItemStack item = new ItemStack(Material.LODESTONE);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.AQUA + "召唤机魂");
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "点击执行指令");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack mrd12(){
        ItemStack item = new ItemStack(Material.DISPENSER);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.AQUA + "召唤公爵");
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "点击执行指令");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack mrd13(){
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.AQUA + "召唤模仿者");
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "点击执行指令");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack mrd14(){
        ItemStack item = new ItemStack(Material.DRAGON_HEAD);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.AQUA + "召唤“桐生一马”");
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "点击执行指令");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }

    public void damageTest(Location loc){
        World w = loc.getWorld();
        WitherSkeleton s = (WitherSkeleton) w.spawnEntity(loc, EntityType.WITHER_SKELETON);
        s.setCustomName(ChatColor.RED + "伤害测试假人——真刃刀魔");
        s.setSilent(true);
        s.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0);
        s.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(0);
        s.getAttribute(Attribute.MAX_HEALTH).setBaseValue(100);
        s.setHealth(100);
    }
    public ItemStack flute(){
        ItemStack item = new ItemStack(Material.STICK);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.AQUA + "荒野大笛客");
        itemMeta.setMaxStackSize(4);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "荒野大笛客");
        lore.add(ChatColor.WHITE + "可以用来感化口人磨");
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
    public void mimic(Location loc,ItemStack hand){
        World w = loc.getWorld();
        Skeleton s = (Skeleton) w.spawnEntity(loc,EntityType.SKELETON);
        EntityEquipment e = s.getEquipment();
        e.clear();
        e.setHelmet(ap.mobHelm(Color.LIME));
        e.setItemInMainHand(hand);
        s.setCustomName(ChatColor.GREEN + "模仿者");
        s.setCustomNameVisible(true);
    }
}
