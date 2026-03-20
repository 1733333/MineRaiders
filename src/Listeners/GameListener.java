package Listeners;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.HashSet;

public class GameListener implements Listener {
    JavaPlugin plugin;
    public GameListener(JavaPlugin plugin){
        this.plugin = plugin;
    }
}
