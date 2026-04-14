import Listeners.*;
import OtherStuff.RoguelikePlugin;
import OtherStuff.VampireSurvivorGame;
import Universal.*;
import Commands.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class MR extends JavaPlugin {
    @Override
    public void onEnable() {
        Recipes recipes = Recipes.INSTANCE;
        BoxPool boxPool = BoxPool.INSTANCE;
        LootPool lootPool = LootPool.INSTANCE;
        Monsters monsters = Monsters.INSTANCE;
        Kit k = Kit.INSTANCE;

        new ArmorEquipListener(this);
        new ContainerListener(this);
        new GadgetListener(this);
        new MonsterListener(this);
        new InventoryListener(this);
        new PlayerListener(this);
        new ArmorListener(this);
        new WeaponListener(this);
        new GameListener(this);
        new LobbyCommand(this);
        new RoguelikePlugin(this);
        LocationManagerUI.init(this);

        getCommand("survivor").setExecutor(new VampireSurvivorGame(this));
        getCommand("mineraiders").setExecutor(new MineRaidersCommand(this));
        getCommand("mr").setTabCompleter(new MineRaidersCommand(this));


        k.setPlugin(this);
        recipes.setPlugin(this);
        monsters.setPlugin(this);

        recipes.registerFreeRecipe();
        recipes.registerRecipe();
        recipes.registerStack();
        boxPool.registerBooks();
        boxPool.registerHorns();
        boxPool.registerPotions();
        lootPool.registerRecycleMap();

        Bukkit.broadcastMessage(ChatColor.AQUA + "插件已重载");
        for (Player p : Bukkit.getOnlinePlayers()){
            p.playSound(p, Sound.ENTITY_PLAYER_LEVELUP,1,2);
        }
        PlayerStats.INSTANCE.loadIslandLevelsFromConfig(this,"island_levels");
    }

    @Override
    public void onDisable() {
        Bukkit.broadcastMessage(ChatColor.RED + "插件已卸载");
        for (Player p : Bukkit.getOnlinePlayers()){
            p.playSound(p, Sound.ENTITY_EXPERIENCE_ORB_PICKUP,1,1);
        }
        LocationManagerUI.saveToConfig();
        LocationManagerUI.backupLocationConfig();
        PlayerStats.INSTANCE.saveIslandLevelsToConfig(this,"island_levels");
    }
}
