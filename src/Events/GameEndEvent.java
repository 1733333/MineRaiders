package Events;

import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GameEndEvent extends Event {
    private static final HandlerList HANDLERS_LIST = new HandlerList();
    World world;

    public GameEndEvent(World w){
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
