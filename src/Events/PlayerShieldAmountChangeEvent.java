package Events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerShieldAmountChangeEvent extends Event {
    private static final HandlerList HANDLERS_LIST = new HandlerList();
    Player player;
    double amount;

    public PlayerShieldAmountChangeEvent(Player p, double a){
        player = p;
        amount = a;
    }

    public Player getPlayer() {
        return player;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }
    public static HandlerList getHandlerList(){
        return HANDLERS_LIST;
    }
}
