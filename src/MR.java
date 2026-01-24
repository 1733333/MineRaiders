import Listeners.ContainerListener;
import commands.DebugCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class MR extends JavaPlugin {
    @Override
    public void onEnable() {
        PluginManager manager = this.getServer().getPluginManager();

        DebugCommand debugCommand = new DebugCommand();
        ContainerListener containerListener = new ContainerListener();

        manager.registerEvents(containerListener,this);

        this.getCommand("mineraidersdebug").setExecutor(debugCommand);

        containerListener.setPlugin(this);
    }

    @Override
    public void onDisable() {

    }
}
