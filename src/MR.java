import Listeners.*;
import Universal.*;
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

        ArmorEquipListener armorEquipListener = new ArmorEquipListener();
        ContainerListener containerListener = new ContainerListener(this);
        GadgetListener gadgetListener = new GadgetListener(this);
        MonsterListener monsterListener = new MonsterListener(this);
        InventoryListener inventoryListener = new InventoryListener(this);
        PlayerListener playerListener = new PlayerListener(this);
        ArmorListener armorListener = new ArmorListener(this);
        WeaponListener weaponListener = new WeaponListener(this);
        GameListener gameListener = new GameListener(this);
        LobbyCommand lobbyCommand = new LobbyCommand(this);
        LocationManagerUI.init(this);

        manager.registerEvents(containerListener,this);
        manager.registerEvents(gadgetListener,this);
        manager.registerEvents(monsterListener,this);
        manager.registerEvents(inventoryListener,this);
        manager.registerEvents(playerListener,this);
        manager.registerEvents(armorListener,this);
        manager.registerEvents(armorEquipListener,this);
        manager.registerEvents(weaponListener,this);
        manager.registerEvents(gameListener,this);
        manager.registerEvents(lobbyCommand,this);

        getCommand("mineraiders").setExecutor(new MineRaidersCommand(this));

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
