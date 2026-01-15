import Listeners.WorldListener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class MR extends JavaPlugin {
    @Override
    public void onEnable() {
        PluginManager manager = this.getServer().getPluginManager();

        WorldListener worldListener = new WorldListener();

        manager.registerEvents(worldListener,this);

        worldListener.setPlugin(this);
    }

    @Override
    public void onDisable() {

    }
}
