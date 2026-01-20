import Listeners.WorldListener;
import commands.DebugCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class MR extends JavaPlugin {
    @Override
    public void onEnable() {
        PluginManager manager = this.getServer().getPluginManager();

        DebugCommand debugCommand = new DebugCommand();
        WorldListener worldListener = new WorldListener();

        manager.registerEvents(worldListener,this);

        this.getCommand("mineraidersdebug").setExecutor(debugCommand);

        worldListener.setPlugin(this);
    }

    @Override
    public void onDisable() {

    }
}
