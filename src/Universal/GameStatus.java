package Universal;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Door;
import org.bukkit.entity.Entity;

import java.util.*;
public enum GameStatus {
    INSTANCE;
    HashMap<String, HashSet<Block>> worldContainers = new HashMap<>();
    HashMap<String, List<Entity>> worldExtractions = new HashMap<>();
    HashMap<String, List<Block>> worldIronDoors = new HashMap<>();
    private final Map<String, Set<UUID>> worldReadyPlayers = new HashMap<>();
    String[]worlds = new String[]{
            "test",
    };
    String[] innerWorldNames = new String[]{
            "星辰山(测试版)",
    };
    public String getWorldNameByID(int id){
        if(id < 0 || id > innerWorldNames.length)return "锈带外围的未知区域";
        return innerWorldNames[id];
    }
    public boolean isEmpty(Block b) {
        String world = b.getWorld().getName();
        HashSet<Block> set = worldContainers.getOrDefault(world, new HashSet<>());
        return set.contains(b);
    }
    public void setEmpty(Block b){
        String world = b.getWorld().getName();
        HashSet<Block> set = worldContainers.getOrDefault(world, new HashSet<>());
        set.add(b);
        worldContainers.put(world,set);
    }
    public void refillContainers(World w){
        worldContainers.remove(w.getName());
    }
    public String getWorlds(int id){
        if(id >= worlds.length || id < 0){
            return "";
        }else {
            return worlds[id];
        }
    }
    public int getWorldId(String worldName) {
        for (int i = 0; i < worlds.length; i++) {
            if (worlds[i].equals(worldName)) {
                return i;
            }
        }
        return -1; // 未找到
    }
    public void addDoor(World w,Block b){
        List<Block> blocks = worldIronDoors.getOrDefault(w.getName(),new ArrayList<>());
        blocks.add(b);
        worldIronDoors.put(w.getName(),blocks);
    }
    public void recoverDoor(World w){
        List<Block> blocks = worldIronDoors.getOrDefault(w.getName(),new ArrayList<>());
        if(!blocks.isEmpty()){
            for(Block b : blocks) {
                Door door = (Door) b.getBlockData();
                door.setOpen(true);
                b.setBlockData(door);
                Block b1 = w.getBlockAt(b.getLocation().add(0, 1, 0));
                Block b2 = w.getBlockAt(b.getLocation().add(0, -1, 0));
                if (b1.getType() == Material.IRON_DOOR) {
                    Door door1 = (Door) b1.getBlockData();
                    door1.setOpen(false);
                    b1.setBlockData(door1);
                }
                if (b2.getType() == Material.IRON_DOOR) {
                    Door door2 = (Door) b2.getBlockData();
                    door2.setOpen(false);
                    b2.setBlockData(door2);
                }
            }
        }
    }
    public void addReadyPlayer(String worldName, UUID playerUUID) {
        worldReadyPlayers.computeIfAbsent(worldName, k -> new HashSet<>()).add(playerUUID);
    }
    public void removeReadyPlayer(String worldName, UUID playerUUID) {
        Set<UUID> players = worldReadyPlayers.get(worldName);
        if (players != null) {
            players.remove(playerUUID);
            if (players.isEmpty()) {
                worldReadyPlayers.remove(worldName);
            }
        }
    }
    public void clearReadyPlayers(String worldName) {
        worldReadyPlayers.remove(worldName);
    }
    public int getReadyCount(String worldName) {
        Set<UUID> players = worldReadyPlayers.get(worldName);
        if (players == null) {
            return 0;
        }
        // 仅统计在线玩家的数量，过滤离线玩家的残留UUID，保证准备人数统计准确
        int onlineReadyCount = 0;
        for (UUID playerUUID : players) {
            // 检查该UUID对应的玩家是否在线
            if (Bukkit.getPlayer(playerUUID) != null) {
                onlineReadyCount++;
            }
        }
        return onlineReadyCount;
    }
}