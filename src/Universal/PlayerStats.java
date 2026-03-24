package Universal;

import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;

public enum PlayerStats {
    INSTANCE;
    public final int maxShield = 20;
    public static HashMap<String, MenuStatus> playerMenuStatus = new HashMap<>();
    public static HashMap<String, Double> playerShield = new HashMap<>();
    public static HashMap<String, BossBar> playerShieldBar = new HashMap<>();

    public enum MenuStatus {
        NOT_MENU,
        LOOT_MENU,
        GADGET_MENU,
        ARMOR_MENU,
        WEAPON_MENU,
        RECIPE_MENU,
        FREE_RECIPE_MENU,
        DROP_MENU,
        CRAFTING_MENU,
        COOKBOOK_MENU,
        DEV_MENU,
        MAP_MENU,
    }

    HashSet<Player> isDying = new HashSet<>();
    HashSet<Player> isInGame = new HashSet<>();
    HashSet<Player> isShieldOn = new HashSet<>();
    HashMap<Player,Integer> PlayerSpectatingStatus = new HashMap<>();
    HashMap<Player, Integer> playerReadyStatus = new HashMap<>();

    public int getMaxShield() {
        return maxShield;
    }

    public boolean isDying(Player p) {
        return isDying.contains(p);
    }
    public void setDying(Player p) {
        isDying.add(p);
    }
    public void stopDying(Player p) {
        isDying.remove(p);
    }

    public boolean isInGame(Player p) {
        return isInGame.contains(p);
    }
    public void setInGame(Player p) {
        isInGame.add(p);
    }
    public void stopInGame(Player p) {
        isInGame.remove(p);
    }

    public boolean isShieldOn(Player p) {
        return isShieldOn.contains(p);
    }
    public void openShield(Player p) {
        isShieldOn.add(p);
    }
    public void closeShield(Player p) {
        isShieldOn.remove(p);
    }
    public boolean hasShield(Player p) {
        return playerShield.getOrDefault(p.getName(), -1D) > 0;
    }
    public double getShield(Player p) {
        return playerShield.getOrDefault(p.getName(), -1D);
    }
    public void setShield(Player p, double shield) {
        playerShield.put(p.getName(), shield);
    }
    public void removePlayerShield(Player p) {
        playerShield.remove(p.getName());
    }
    // 准备状态相关方法
    public boolean isReady(Player p) {
        return playerReadyStatus.containsKey(p);
    }
    public int getReadyStatus(Player p) {
        return playerReadyStatus.getOrDefault(p, -1);
    }
    public void setReady(Player p, int mapId) {
        playerReadyStatus.put(p, mapId);
    }
    public void stopReady(Player p) {
        playerReadyStatus.remove(p);
    }
    // 观战状态相关方法
    public boolean isSpectating(Player p) {
        return PlayerSpectatingStatus.containsKey(p);
    }
    public int getSpectatingStatus(Player p) {
        return PlayerSpectatingStatus.getOrDefault(p, -1);
    }
    public void setSpectating(Player p, int worldId) {
        PlayerSpectatingStatus.put(p, worldId);
    }
    public void stopSpectating(Player p) {
        PlayerSpectatingStatus.remove(p);
    }

}