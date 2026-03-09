package Events;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerOpenContainerEvent extends Event {
    private static final HandlerList HANDLERS_LIST = new HandlerList();
    Player player;
    Block container;

    public PlayerOpenContainerEvent(Player p,Block c){
        player = p;
        container = c;
    }

    public Player getPlayer() {
        return player;
    }

    public Block getContainer() {
        return container;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }
    public static HandlerList getHandlerList(){
        return HANDLERS_LIST;
    }
}
