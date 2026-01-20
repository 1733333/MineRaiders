import org.bukkit.plugin.java.JavaPlugin;

public enum Recipes {
    INSTANCE;
    JavaPlugin plugin;
    public void setPlugin(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    public void registerRecipe(){

    }
}
