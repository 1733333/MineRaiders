package Events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerShieldBreakEvent extends Event {
    private static final HandlerList HANDLERS_LIST = new HandlerList();
    Player player;

    public PlayerShieldBreakEvent(Player p){
        player = p;
    }

    public Player getPlayer() {
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
