package Commands;

import Universal.*;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (commandSender instanceof Player p) {
            try {
                if (p.isOp()) {
                    World w = p.getWorld();
                    ItemStack hand = p.getEquipment().getItemInMainHand();
                    int num = Integer.parseInt(strings[0]);
                    switch (num){
                        case -3 ->w.dropItem(p.getLocation(),flute());
                        case -2 ->damageTest(p.getLocation());
                        case -1 ->{
                            Bukkit.resetRecipes();
                            Bukkit.broadcastMessage(ChatColor.RED + "自定义配方已清除");
                        }
                        case 0->m.shredder(p.getLocation());
                        case 1->m.flea(p.getLocation());
                        case 2->m.pop(p.getLocation());
                        case 3->m.fireBall(p.getLocation());
                        case 4->m.snitch(p.getLocation());
                        case 5->m.leaper(p.getLocation());
                        case 6->m.bastion(p.getLocation());
                        case 7->m.dukeMinion(p.getLocation(),false);
                        case 8->m.duke(p.getLocation());
                        case 9->mimic(p.getLocation(),hand);
                    }
                }
            } catch (Exception ignored) {

            }
        }
        return true;
    }
    public void damageTest(Location loc){
        World w = loc.getWorld();
        WitherSkeleton s = (WitherSkeleton) w.spawnEntity(loc, EntityType.WITHER_SKELETON);
        s.setCustomName(ChatColor.RED + "伤害测试假人——半兵卫");
        s.setSilent(true);
        s.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0);
        s.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(0);
        s.getAttribute(Attribute.MAX_HEALTH).setBaseValue(100);
        s.setHealth(100);
    }
    public void boss(Location loc){
        World w = loc.getWorld();
        Vex v = (Vex) w.spawnEntity(loc,EntityType.VEX);
        Zombie z = (Zombie) w.spawnEntity(loc,EntityType.ZOMBIE);
        v.setSilent(true);
        z.setSilent(true);
        z.getEquipment().clear();
        z.getEquipment().setHelmet(new ItemStack(Material.LODESTONE));
        z.getAttribute(Attribute.SCALE).setBaseValue(3);
        z.setInvisible(true);
        v.setInvisible(true);
        v.addPassenger(z);
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
