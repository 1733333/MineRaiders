package Universal;

import Events.PlayerShieldAmountChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;

public enum PlayerStats {
    INSTANCE;
    public static final int MAX_SHIELD = 20;
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
        END_MENU,
        MAIN_MENU
    }

    HashSet<String> isDying = new HashSet<>();
    HashSet<String> isInGame = new HashSet<>();
    HashMap<String, Integer> PlayerSpectatingStatus = new HashMap<>();
    HashMap<String, Integer> playerReadyStatus = new HashMap<>();
    HashMap<String, Integer> playerIslandLevel = new HashMap<>();

    public int getMaxShield() {
        return MAX_SHIELD;
    }

    public boolean isDying(Player p) {
        return isDying.contains(p.getName());
    }

    public void setDying(Player p) {
        isDying.add(p.getName());
    }

    public void stopDying(Player p) {
        isDying.remove(p.getName());
    }

    public boolean isInGame(Player p) {
        return isInGame.contains(p.getName());
    }

    public void setInGame(Player p) {
        isInGame.add(p.getName());
    }

    public void stopInGame(Player p) {
        isInGame.remove(p.getName());
    }
    //护盾相关函数
    public boolean isShieldOn(Player p) {
        BossBar shieldBar = playerShieldBar.getOrDefault(p.getName(), null);
        if (shieldBar == null) return false;
        return shieldBar.isVisible();
    }

    public void openShield(Player p) {
        BossBar shieldBar = playerShieldBar.getOrDefault(p.getName(), null);
        if (shieldBar != null) {
            shieldBar.setVisible(true);
            double progress = getShield(p) / MAX_SHIELD;
            if (progress < 0) {
                progress = 0;
            }
            shieldBar.setProgress(Math.min(1, progress));
        }
    }

    public void closeShield(Player p) {
        BossBar shieldBar = playerShieldBar.getOrDefault(p.getName(), null);
        if (shieldBar != null) {
            shieldBar.removeAll();
            playerShieldBar.remove(p.getName());
        }
    }

    public void createShieldBar(Player p){
        BossBar bar = Bukkit.createBossBar(ChatColor.AQUA + "" + ChatColor.BOLD + "护盾",
                BarColor.BLUE, BarStyle.SEGMENTED_10);
        bar.addPlayer(p);
        bar.setProgress(0);
        bar.setColor(BarColor.WHITE);
        playerShieldBar.put(p.getName(), bar);
    }

    public void removeShieldBar(Player p){
        BossBar shieldBar = playerShieldBar.getOrDefault(p.getName(), null);
        if (shieldBar != null) {
            shieldBar.removeAll();
        }
        playerShieldBar.remove(p.getName());
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

    // 准备状态相关方法
    public boolean isReady(Player p) {
        return playerReadyStatus.containsKey(p.getName());
    }

    public int getReadyStatus(Player p) {
        return playerReadyStatus.getOrDefault(p.getName(), -1);
    }

    public void setReady(Player p, int mapId) {
        playerReadyStatus.put(p.getName(), mapId);
    }

    public void stopReady(Player p) {
        playerReadyStatus.remove(p.getName());
    }

    // 观战状态相关方法
    public boolean isSpectating(Player p) {
        return PlayerSpectatingStatus.containsKey(p.getName());
    }

    public int getSpectatingStatus(Player p) {
        return PlayerSpectatingStatus.getOrDefault(p.getName(), -1);
    }

    public void setSpectating(Player p, int worldId) {
        PlayerSpectatingStatus.put(p.getName(), worldId);
    }

    public void stopSpectating(Player p) {
        PlayerSpectatingStatus.remove(p.getName());
    }

    // ========== 岛屿等级相关方法 ==========
    /**
     * 获取玩家的岛屿等级，未设置时返回 0
     */
    public int getIslandLevel(Player p) {
        return playerIslandLevel.getOrDefault(p.getName(), 0);
    }

    /**
     * 设置玩家的岛屿等级
     */
    public void setIslandLevel(Player p, int level) {
        playerIslandLevel.put(p.getName(), level);
    }

    /**
     * 增加（或减少）玩家的岛屿等级
     */
    public void addIslandLevel(Player p, int amount) {
        int current = getIslandLevel(p);
        setIslandLevel(p, current + amount);
    }

    /**
     * 检查玩家是否有岛屿等级记录
     */
    public boolean hasIslandLevel(Player p) {
        return playerIslandLevel.containsKey(p.getName());
    }

    /**
     * 从配置节加载所有岛屿等级（内部使用）
     */
    public void loadIslandLevels(ConfigurationSection section) {
        if (section == null) return;
        for (String playerName : section.getKeys(false)) {
            int level = section.getInt(playerName, 0);
            playerIslandLevel.put(playerName, level);
        }
    }

    /**
     * 将所有玩家的岛屿等级保存到配置节（内部使用）
     */
    public void saveIslandLevels(ConfigurationSection section) {
        if (section == null) return;
        for (String playerName : playerIslandLevel.keySet()) {
            section.set(playerName, playerIslandLevel.get(playerName));
        }
    }

    // ========== 集成到插件配置的便捷方法 ==========
    /**
     * 从插件的配置文件中加载岛屿等级数据
     * @param plugin 插件实例
     * @param configPath 配置路径，如 "island_levels"
     */
    public void loadIslandLevelsFromConfig(JavaPlugin plugin, String configPath) {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection(configPath);
        if (section == null) {
            section = plugin.getConfig().createSection(configPath);
        }
        loadIslandLevels(section);
    }

    /**
     * 将所有岛屿等级数据保存到插件的配置文件
     * @param plugin 插件实例
     * @param configPath 配置路径，如 "island_levels"
     */
    public void saveIslandLevelsToConfig(JavaPlugin plugin, String configPath) {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection(configPath);
        if (section == null) {
            section = plugin.getConfig().createSection(configPath);
        }
        saveIslandLevels(section);
        plugin.saveConfig(); // 立即写入磁盘
    }
}