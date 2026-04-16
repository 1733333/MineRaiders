package RogueLike;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;

import java.util.*;

/**
 * 地牢数据容器，包含网格房间和门的数据定义
 */
public class Dungeon {

    // 方块材质常量（保留作为后备，实际使用材质组）
    public static final Material DEFAULT_WALL = Material.STONE_BRICKS;
    public static final Material DEFAULT_FLOOR = Material.STONE_BRICKS;
    public static final Material PATH_MATERIAL = Material.SMOOTH_STONE;
    public static final Material DOOR_MATERIAL = Material.IRON_BLOCK;

    public static final int ROOM_SIZE = 13;
    public static final int WALL_THICKNESS = 1;

    // ==================== 房间类型枚举 ====================
    public enum RoomType {
        START,      // 起点房间
        NORMAL,     // 普通战斗房间
        TREASURE,   // 宝藏房（特殊1）
        LIBRARY,    // 图书馆（特殊2）
        PRISON,     // 监狱（特殊3）
        SHRINE,     // 祭坛（特殊4）
        BOSS        // BOSS房间
    }

    // ==================== 材质组：墙壁和地板使用同一主题 ====================
    public static class RoomMaterialPalette {
        public final Material primaryWall;
        public final Material primaryFloor;
        public final Material secondaryWall;
        public final Material secondaryFloor;
        public final Material accent;

        public RoomMaterialPalette(Material pw, Material pf, Material sw, Material sf, Material ac) {
            this.primaryWall = pw;
            this.primaryFloor = pf;
            this.secondaryWall = sw;
            this.secondaryFloor = sf;
            this.accent = ac;
        }

        // 预定义材质组
        public static final RoomMaterialPalette STONE = new RoomMaterialPalette(
                Material.STONE_BRICKS, Material.STONE_BRICKS,
                Material.MOSSY_STONE_BRICKS, Material.CRACKED_STONE_BRICKS,
                Material.CHISELED_STONE_BRICKS
        );
        public static final RoomMaterialPalette DEEPSLATE = new RoomMaterialPalette(
                Material.DEEPSLATE_BRICKS, Material.POLISHED_DEEPSLATE,
                Material.CRACKED_DEEPSLATE_BRICKS, Material.DEEPSLATE_TILES,
                Material.CHISELED_DEEPSLATE
        );
        public static final RoomMaterialPalette NETHER = new RoomMaterialPalette(
                Material.NETHER_BRICKS, Material.NETHER_BRICKS,
                Material.RED_NETHER_BRICKS, Material.CRACKED_NETHER_BRICKS,
                Material.CHISELED_NETHER_BRICKS
        );
        public static final RoomMaterialPalette PRISMARINE = new RoomMaterialPalette(
                Material.PRISMARINE_BRICKS, Material.PRISMARINE,
                Material.DARK_PRISMARINE, Material.SEA_LANTERN,
                Material.PRISMARINE_BRICKS
        );
        public static final RoomMaterialPalette WOOD = new RoomMaterialPalette(
                Material.OAK_PLANKS, Material.OAK_PLANKS,
                Material.SPRUCE_PLANKS, Material.DARK_OAK_PLANKS,
                Material.STRIPPED_OAK_LOG
        );
        public static final RoomMaterialPalette BOSS = new RoomMaterialPalette(
                Material.OBSIDIAN, Material.CRYING_OBSIDIAN,
                Material.OBSIDIAN, Material.CRYING_OBSIDIAN,
                Material.GILDED_BLACKSTONE
        );
    }

    private final World world;
    private final Location baseLocation;
    private final String owner;
    private final GridCell[][] grid = new GridCell[4][4];
    private final List<Door> doors = new ArrayList<>();
    private final Set<Location> generatedBlocks = new HashSet<>();

    private GridCell startRoom;
    private GridCell bossRoom;

    public Dungeon(World world, Location baseLocation, String owner) {
        this.world = world;
        this.baseLocation = baseLocation.clone();
        this.owner = owner;
    }

    // Getters
    public World getWorld() { return world; }
    public Location getBaseLocation() { return baseLocation; }
    public GridCell[][] getGrid() { return grid; }
    public List<Door> getDoors() { return doors; }
    public Set<Location> getGeneratedBlocks() { return generatedBlocks; }
    public GridCell getStartRoom() { return startRoom; }
    public void setStartRoom(GridCell cell) { this.startRoom = cell; }
    public GridCell getBossRoom() { return bossRoom; }
    public void setBossRoom(GridCell cell) { this.bossRoom = cell; }

    /**
     * 根据位置查找对应的门
     */
    public Door getDoorAt(Location loc) {
        for (Door door : doors) {
            if (door.location.getBlockX() == loc.getBlockX() &&
                    door.location.getBlockY() == loc.getBlockY() &&
                    door.location.getBlockZ() == loc.getBlockZ()) {
                return door;
            }
            Location above = door.location.clone().add(0, 1, 0);
            if (above.getBlockX() == loc.getBlockX() &&
                    above.getBlockY() == loc.getBlockY() &&
                    above.getBlockZ() == loc.getBlockZ()) {
                return door;
            }
        }
        return null;
    }

    /**
     * 清除地牢所有方块
     */
    public void clean() {
        for (Location loc : generatedBlocks) {
            loc.getBlock().setType(Material.AIR);
        }
        generatedBlocks.clear();
        doors.clear();
    }

    // ==================== 内部类：网格房间 ====================
    public static class GridCell {
        private final int x, z;
        private RoomType roomType = RoomType.NORMAL;  // 房间类型，默认为普通
        private boolean isMerged = false;
        private int mergedGroupId = -1;
        private boolean isGenerated = false;
        private Location origin;
        private final Set<BlockFace> doors = new HashSet<>();

        public GridCell(int x, int z) {
            this.x = x;
            this.z = z;
        }

        public int getX() { return x; }
        public int getZ() { return z; }
        public RoomType getRoomType() { return roomType; }
        public void setRoomType(RoomType roomType) { this.roomType = roomType; }
        public boolean isMerged() { return isMerged; }
        public void setMerged(boolean merged) { isMerged = merged; }
        public int getMergedGroupId() { return mergedGroupId; }
        public void setMergedGroupId(int mergedGroupId) { this.mergedGroupId = mergedGroupId; }
        public boolean isGenerated() { return isGenerated; }
        public void setGenerated(boolean generated) { isGenerated = generated; }
        public Location getOrigin() { return origin; }
        public void setOrigin(Location origin) { this.origin = origin; }
        public Set<BlockFace> getDoors() { return doors; }
    }

    // ==================== 内部类：门 ====================
    public static class Door {
        private Location location;
        private BlockFace direction;
        private GridCell fromRoom;
        private GridCell toRoom;
        private boolean open = false;

        public Location getLocation() { return location; }
        public void setLocation(Location location) { this.location = location; }
        public BlockFace getDirection() { return direction; }
        public void setDirection(BlockFace direction) { this.direction = direction; }
        public GridCell getFromRoom() { return fromRoom; }
        public void setFromRoom(GridCell fromRoom) { this.fromRoom = fromRoom; }
        public GridCell getToRoom() { return toRoom; }
        public void setToRoom(GridCell toRoom) { this.toRoom = toRoom; }
        public boolean isOpen() { return open; }
        public void setOpen(boolean open) { this.open = open; }
    }
}