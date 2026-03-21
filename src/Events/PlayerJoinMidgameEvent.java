package Events;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerJoinMidgameEvent extends Event {
    private static final HandlerList HANDLERS_LIST = new HandlerList();
    Player player;
    World world;

    public PlayerJoinMidgameEvent(World w,Player p){
        world = w;
        player = p;
    }

    public World getWorld() {
        return world;
    }
    public Player getPlayer(){
        return player;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }
    public static HandlerList getHandlerList(){
        return HANDLERS_LIST;
    }
}
