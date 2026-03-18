package Events;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Warden;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GameStartEvent extends Event {
    private static final HandlerList HANDLERS_LIST = new HandlerList();
    World world;

    public GameStartEvent(World w){
        world = w;
    }

    public World getWorld() {
        return world;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }
    public static HandlerList getHandlerList(){
        return HANDLERS_LIST;
    }
}
