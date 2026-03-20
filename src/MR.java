import Listeners.*;
import OtherStuff.KiryuKazuma;
import Universal.BoxPool;
import Universal.Kit;
import Universal.Monsters;
import Universal.Recipes;
import Commands.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class MR extends JavaPlugin {
    @Override
    public void onEnable() {
        PluginManager manager = this.getServer().getPluginManager();
        Recipes recipes = Recipes.INSTANCE;
        BoxPool boxPool = BoxPool.INSTANCE;
        Monsters monsters = Monsters.INSTANCE;
        Kit k = Kit.INSTANCE;

        DebugCommand debugCommand = new DebugCommand(this);
        LootCommand lootCommand =  new LootCommand();
        GadgetCommand gadgetCommand = new GadgetCommand();
        WeaponCommand weaponCommand = new WeaponCommand();
        ArmorCommand armorCommand = new ArmorCommand();
        RecipeCommand recipeCommand = new RecipeCommand();
        DropCommand dropCommand = new DropCommand();
        FreeRecipeCommand freeRecipeCommand = new FreeRecipeCommand();
        GetAllItemsCommand getAllItemsCommand = new GetAllItemsCommand();
        ArmorEquipListener armorEquipListener = new ArmorEquipListener();
        ContainerListener containerListener = new ContainerListener(this);
        GadgetListener gadgetListener = new GadgetListener(this);
        MonsterListener monsterListener = new MonsterListener(this);
        InventoryListener inventoryListener = new InventoryListener(this);
        PlayerListener playerListener = new PlayerListener(this);
        ArmorListener armorListener = new ArmorListener(this);
        WeaponListener weaponListener = new WeaponListener(this);
        LocationManagerUI.init(this);

        manager.registerEvents(containerListener,this);
        manager.registerEvents(gadgetListener,this);
        manager.registerEvents(monsterListener,this);
        manager.registerEvents(inventoryListener,this);
        manager.registerEvents(playerListener,this);
        manager.registerEvents(armorListener,this);
        manager.registerEvents(armorEquipListener,this);
        manager.registerEvents(weaponListener,this);

        this.getCommand("mineraidersdebug").setExecutor(debugCommand);
        this.getCommand("getloots").setExecutor(lootCommand);
        this.getCommand("getgadgets").setExecutor(gadgetCommand);
        this.getCommand("getweapons").setExecutor(weaponCommand);
        this.getCommand("getarmors").setExecutor(armorCommand);
        this.getCommand("getrecipes").setExecutor(recipeCommand);
        this.getCommand("getfreerecipes").setExecutor(freeRecipeCommand);
        this.getCommand("getdrops").setExecutor(dropCommand);
        this.getCommand("getall").setExecutor(getAllItemsCommand);

        k.setPlugin(this);
        recipes.setPlugin(this);
        monsters.setPlugin(this);

        recipes.registerFreeRecipe();
        recipes.registerRecipe();
        recipes.registerStack();
        boxPool.registerBooks();
        boxPool.registerHorns();
        boxPool.registerPotions();

        Bukkit.broadcastMessage(ChatColor.AQUA + "插件已重载");
        for (Player p : Bukkit.getOnlinePlayers()){
            p.playSound(p, Sound.ENTITY_PLAYER_LEVELUP,1,2);
        }
    }

    @Override
    public void onDisable() {
        Bukkit.broadcastMessage(ChatColor.RED + "插件已卸载");
        for (Player p : Bukkit.getOnlinePlayers()){
            p.playSound(p, Sound.ENTITY_EXPERIENCE_ORB_PICKUP,1,1);
        }
        LocationManagerUI.saveToConfig();
    }
}
