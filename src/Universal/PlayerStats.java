package Universal;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;

public enum PlayerStats {
    INSTANCE;
    public final int maxShield = 20;
    public static HashMap<String,MenuStatus>playerMenuStatus = new HashMap<>();
    public static HashMap<String,Double>playerShield = new HashMap<>();
    public enum MenuStatus{
        NOT_MENU,
        LOOT_MENU,
        GADGET_MENU,
        ARMOR_MENU,
        WEAPON_MENU,
        RECIPE_MENU,
        FREE_RECIPE_MENU,
        DROP_MENU,
        CRAFTING_MENU,
        DEV_MENU,
    }
    HashSet<Player> isDying = new HashSet<>();
    HashSet<Player> isShieldOn = new HashSet<>();
    public int getMaxShield() {return maxShield;}
    public boolean isDying(Player p){return isDying.contains(p);}
    public void setDying(Player p){isDying.add(p);}
    public void stopDying(Player p){isDying.remove(p);}
    public boolean isShieldOn(Player p){return isShieldOn.contains(p);}
    public void openShield(Player p){isShieldOn.add(p);}
    public void closeShield(Player p){isShieldOn.remove(p);}
    public boolean hasShield(Player p){
        return playerShield.getOrDefault(p.getName(),-1D) > 0;
    }
    public double getShield(Player p){
        return playerShield.getOrDefault(p.getName(),-1D);
    }
    public void setShield(Player p, double shield){
        playerShield.put(p.getName(),shield);
    }
    public void removePlayerShield(Player p){
        playerShield.remove(p.getName());
    }
}
