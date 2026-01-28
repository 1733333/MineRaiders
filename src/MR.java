import Listeners.ContainerListener;
import Listeners.GadgetListener;
import Universal.BoxPool;
import Universal.Recipes;
import commands.DebugCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class MR extends JavaPlugin {
    @Override
    public void onEnable() {
        Recipes recipes = Recipes.INSTANCE;
        BoxPool boxPool = BoxPool.INSTANCE;

        PluginManager manager = this.getServer().getPluginManager();

        DebugCommand debugCommand = new DebugCommand();
        ContainerListener containerListener = new ContainerListener();
        GadgetListener gadgetListener = new GadgetListener();

        manager.registerEvents(containerListener,this);
        manager.registerEvents(gadgetListener,this);

        this.getCommand("mineraidersdebug").setExecutor(debugCommand);

        containerListener.setPlugin(this);
        gadgetListener.setPlugin(this);
        recipes.setPlugin(this);

        recipes.registerStack();
        recipes.registerRecipe();
        boxPool.registerBooks();
        boxPool.registerHorns();
        boxPool.registerPotions();
    }

    @Override
    public void onDisable() {

    }
}
