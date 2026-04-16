package RogueLike;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.noise.SimplexNoiseGenerator;

import java.util.*;

/**
 * 地牢生成器，负责结构生成、门溶解、刷怪等
 */
public class DungeonGenerator {

    private final Dungeon dungeon;
    private final Random random = new Random();
    private final SimplexNoiseGenerator noise = new SimplexNoiseGenerator(random);

    public DungeonGenerator(Dungeon dungeon) {
        this.dungeon = dungeon;
    }

    /**
     * 生成完整地牢（仅起点房间可见）
     */
    public void generate() {
        initializeGrid();
        placeSpecialRooms();
        mergeRooms();
        generateDoorsAndPaths();
        generateStartRoom();
    }

    private void initializeGrid() {
        Dungeon.GridCell[][] grid = dungeon.getGrid();
        Location base = dungeon.getBaseLocation();
        int size = Dungeon.ROOM_SIZE;
        int thick = Dungeon.WALL_THICKNESS;

        for (int x = 0; x < 4; x++) {
            for (int z = 0; z < 4; z++) {
                Dungeon.GridCell cell = new Dungeon.GridCell(x, z);
                cell.setOrigin(base.clone().add(x * (size + thick * 2), 0, z * (size + thick * 2)));
                grid[x][z] = cell;
            }
        }

        dungeon.setStartRoom(grid[0][0]);
        dungeon.setBossRoom(grid[3][3]);
        dungeon.getStartRoom().setRoomType(Dungeon.RoomType.START);
        dungeon.getBossRoom().setRoomType(Dungeon.RoomType.BOSS);
    }

    private void placeSpecialRooms() {
        Dungeon.GridCell[][] grid = dungeon.getGrid();
        List<Dungeon.GridCell> available = new ArrayList<>();

        // 收集所有普通房间（排除起点和Boss）
        for (int x = 0; x < 4; x++) {
            for (int z = 0; z < 4; z++) {
                Dungeon.GridCell cell = grid[x][z];
                if (cell.getRoomType() == Dungeon.RoomType.NORMAL) {
                    available.add(cell);
                }
            }
        }
        Collections.shuffle(available);

        // 特殊房间类型池
        Dungeon.RoomType[] specialTypes = {
                Dungeon.RoomType.TREASURE,
                Dungeon.RoomType.LIBRARY,
                Dungeon.RoomType.PRISON,
                Dungeon.RoomType.SHRINE
        };
        int specialCount = random.nextInt(3) + 1;  // 1~3个特殊房间

        for (int i = 0; i < Math.min(specialCount, available.size()); i++) {
            Dungeon.RoomType type = specialTypes[random.nextInt(specialTypes.length)];
            available.get(i).setRoomType(type);
        }
    }

    private void mergeRooms() {
        Dungeon.GridCell[][] grid = dungeon.getGrid();
        for (int x = 0; x < 4; x++) {
            for (int z = 0; z < 4; z++) {
                Dungeon.GridCell cell = grid[x][z];
                if (cell.isMerged()) continue;
                if (cell == dungeon.getStartRoom() || cell == dungeon.getBossRoom()) continue;

                if (x < 3 && random.nextDouble() < 0.3) {
                    mergeCells(cell, grid[x + 1][z]);
                }
                if (z < 3 && random.nextDouble() < 0.3) {
                    mergeCells(cell, grid[x][z + 1]);
                }
            }
        }
    }

    private void mergeCells(Dungeon.GridCell a, Dungeon.GridCell b) {
        if (a.isMerged() && b.isMerged() && a.getMergedGroupId() == b.getMergedGroupId()) return;

        int groupId = a.isMerged() ? a.getMergedGroupId() :
                (b.isMerged() ? b.getMergedGroupId() : random.nextInt(10000));

        a.setMerged(true);
        a.setMergedGroupId(groupId);
        b.setMerged(true);
        b.setMergedGroupId(groupId);
    }

    private void generateDoorsAndPaths() {
        Dungeon.GridCell[][] grid = dungeon.getGrid();
        boolean[][] visited = new boolean[4][4];
        Stack<Dungeon.GridCell> stack = new Stack<>();

        visited[0][0] = true;
        stack.push(grid[0][0]);

        while (!stack.isEmpty()) {
            Dungeon.GridCell current = stack.peek();
            List<Dungeon.GridCell> neighbors = getUnvisitedNeighbors(current, visited);

            if (!neighbors.isEmpty()) {
                Dungeon.GridCell next = neighbors.get(random.nextInt(neighbors.size()));
                visited[next.getX()][next.getZ()] = true;
                stack.push(next);
                createDoor(current, next);
            } else {
                stack.pop();
            }
        }

        // 确保所有房间连通
        for (int x = 0; x < 4; x++) {
            for (int z = 0; z < 4; z++) {
                if (!visited[x][z]) {
                    connectToNearestVisited(grid[x][z], visited);
                    visited[x][z] = true;
                }
            }
        }
    }

    private List<Dungeon.GridCell> getUnvisitedNeighbors(Dungeon.GridCell cell, boolean[][] visited) {
        List<Dungeon.GridCell> neighbors = new ArrayList<>();
        int x = cell.getX(), z = cell.getZ();
        Dungeon.GridCell[][] grid = dungeon.getGrid();

        if (z + 1 < 4 && !visited[x][z + 1]) neighbors.add(grid[x][z + 1]);
        if (z - 1 >= 0 && !visited[x][z - 1]) neighbors.add(grid[x][z - 1]);
        if (x - 1 >= 0 && !visited[x - 1][z]) neighbors.add(grid[x - 1][z]);
        if (x + 1 < 4 && !visited[x + 1][z]) neighbors.add(grid[x + 1][z]);

        return neighbors;
    }

    private void connectToNearestVisited(Dungeon.GridCell cell, boolean[][] visited) {
        int x = cell.getX(), z = cell.getZ();
        Dungeon.GridCell[][] grid = dungeon.getGrid();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (Math.abs(dx) + Math.abs(dz) != 1) continue;
                int nx = x + dx, nz = z + dz;
                if (nx >= 0 && nx < 4 && nz >= 0 && nz < 4 && visited[nx][nz]) {
                    createDoor(cell, grid[nx][nz]);
                    return;
                }
            }
        }
    }

    private void createDoor(Dungeon.GridCell from, Dungeon.GridCell to) {
        Dungeon.Door door = new Dungeon.Door();
        door.setFromRoom(from);
        door.setToRoom(to);

        BlockFace direction;
        if (to.getX() > from.getX()) direction = BlockFace.EAST;
        else if (to.getX() < from.getX()) direction = BlockFace.WEST;
        else if (to.getZ() > from.getZ()) direction = BlockFace.SOUTH;
        else direction = BlockFace.NORTH;
        door.setDirection(direction);

        Location fromOrigin = from.getOrigin();
        int doorX, doorZ;
        int size = Dungeon.ROOM_SIZE;
        int thick = Dungeon.WALL_THICKNESS;

        doorZ = switch (direction) {
            case EAST -> {
                doorX = fromOrigin.getBlockX() + size + thick;
                yield fromOrigin.getBlockZ() + size / 2;
            }
            case WEST -> {
                doorX = fromOrigin.getBlockX() - thick;
                yield fromOrigin.getBlockZ() + size / 2;
            }
            case SOUTH -> {
                doorX = fromOrigin.getBlockX() + size / 2;
                yield fromOrigin.getBlockZ() + size + thick;
            }
            default -> {
                doorX = fromOrigin.getBlockX() + size / 2;
                yield fromOrigin.getBlockZ() - thick;
            }
        };

        door.setLocation(new Location(dungeon.getWorld(), doorX, fromOrigin.getY() + 1, doorZ));
        dungeon.getDoors().add(door);
        from.getDoors().add(direction);
        to.getDoors().add(getOppositeDirection(direction));
    }

    private BlockFace getOppositeDirection(BlockFace face) {
        return switch (face) {
            case NORTH -> BlockFace.SOUTH;
            case SOUTH -> BlockFace.NORTH;
            case EAST -> BlockFace.WEST;
            case WEST -> BlockFace.EAST;
            default -> face;
        };
    }

    private void generateStartRoom() {
        buildRoomStructure(dungeon.getStartRoom());
        dungeon.getStartRoom().setGenerated(true);
    }

    // ==================== 房间建造（新材质系统） ====================
    private void buildRoomStructure(Dungeon.GridCell cell) {
        Location origin = cell.getOrigin();
        Dungeon.RoomType type = cell.getRoomType();
        Dungeon.RoomMaterialPalette palette = getPaletteForRoom(type);
        int size = Dungeon.ROOM_SIZE;
        int thick = Dungeon.WALL_THICKNESS;

        // 地板（带噪声）
        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                Location loc = origin.clone().add(x, 0, z);
                Material mat = getNoiseBasedMaterial(x, z, palette, false);
                loc.getBlock().setType(mat);
                dungeon.getGeneratedBlocks().add(loc.clone());
            }
        }

        // 墙壁（若不合并）
        if (!cell.isMerged()) {
            buildWallWithNoise(origin, -thick, -thick, size + thick, 0, palette);
            buildWallWithNoise(origin, -thick, size, size + thick, size + thick, palette);
            buildWallWithNoise(origin, -thick, 0, 0, size, palette);
            buildWallWithNoise(origin, size, 0, size + thick, size, palette);
        }

        // 放置门方块
        for (Dungeon.Door door : dungeon.getDoors()) {
            if ((door.getFromRoom() == cell || door.getToRoom() == cell) && !door.isOpen()) {
                door.getLocation().getBlock().setType(Dungeon.DOOR_MATERIAL);
                door.getLocation().clone().add(0, 1, 0).getBlock().setType(Dungeon.DOOR_MATERIAL);
                dungeon.getGeneratedBlocks().add(door.getLocation().clone());
                dungeon.getGeneratedBlocks().add(door.getLocation().clone().add(0, 1, 0));
            }
        }
    }

    private void buildWallWithNoise(Location origin, int xStart, int zStart, int xEnd, int zEnd, Dungeon.RoomMaterialPalette palette) {
        for (int x = xStart; x < xEnd; x++) {
            for (int z = zStart; z < zEnd; z++) {
                for (int y = 0; y < 4; y++) {
                    Location loc = origin.clone().add(x, y, z);
                    Material mat = getNoiseBasedMaterial(x, z, palette, true);
                    loc.getBlock().setType(mat);
                    dungeon.getGeneratedBlocks().add(loc.clone());
                }
            }
        }
    }

    /**
     * 根据噪声值选择材质组中的具体方块
     * @param x,z 房间内相对坐标（0 ~ ROOM_SIZE-1）
     * @param palette 材质组
     * @param isWall true表示墙壁，false表示地板
     */
    private Material getNoiseBasedMaterial(int x, int z, Dungeon.RoomMaterialPalette palette, boolean isWall) {
        // 将坐标缩放到噪声域
        double nx = x * 0.15;
        double nz = z * 0.15;
        double noiseVal = noise.noise(nx, nz);  // 返回 -1.0 ~ 1.0

        if (noiseVal > 0.5) {
            return isWall ? palette.primaryWall : palette.primaryFloor;
        } else if (noiseVal > -0.5) {
            return isWall ? palette.secondaryWall : palette.secondaryFloor;
        } else {
            // 小概率使用点缀材质
            return random.nextDouble() < 0.1 ? palette.accent :
                    (isWall ? palette.primaryWall : palette.primaryFloor);
        }
    }

    /**
     * 根据房间类型返回材质组
     */
    private Dungeon.RoomMaterialPalette getPaletteForRoom(Dungeon.RoomType type) {
        return switch (type) {
            case START, NORMAL -> Dungeon.RoomMaterialPalette.STONE;
            case TREASURE -> Dungeon.RoomMaterialPalette.DEEPSLATE;
            case LIBRARY -> Dungeon.RoomMaterialPalette.WOOD;
            case PRISON -> Dungeon.RoomMaterialPalette.PRISMARINE;
            case SHRINE -> Dungeon.RoomMaterialPalette.NETHER;
            case BOSS -> Dungeon.RoomMaterialPalette.BOSS;
            default -> Dungeon.RoomMaterialPalette.STONE;
        };
    }

    private void buildPath(Dungeon.Door door) {
        Location from = door.getFromRoom().getOrigin().clone().add(Dungeon.ROOM_SIZE / 2.0, 0, Dungeon.ROOM_SIZE / 2.0);
        Location to = door.getToRoom().getOrigin().clone().add(Dungeon.ROOM_SIZE / 2.0, 0, Dungeon.ROOM_SIZE / 2.0);

        int steps = (int) from.distance(to);
        for (int i = 0; i <= steps; i++) {
            double t = (double) i / steps;
            Location point = from.clone().add(
                    (to.getX() - from.getX()) * t,
                    0,
                    (to.getZ() - from.getZ()) * t
            );

            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    Location loc = point.clone().add(dx, 0, dz);
                    if (loc.getBlock().getType() == Material.AIR || loc.getBlock().getType() == Dungeon.DEFAULT_FLOOR) {
                        loc.getBlock().setType(Dungeon.PATH_MATERIAL);
                        dungeon.getGeneratedBlocks().add(loc.clone());
                    }
                }
            }
        }
    }

    // ==================== 开门逻辑 ====================
    public void openDoor(Dungeon.Door door, Plugin plugin) {
        if (door.isOpen()) return;
        door.setOpen(true);

        dissolveDoorBlocks(door, plugin);

        if (!door.getToRoom().isGenerated()) {
            buildRoomStructure(door.getToRoom());
            door.getToRoom().setGenerated(true);

            if (door.getToRoom() == dungeon.getBossRoom()) {
                spawnBoss(door.getToRoom());
            } else if (door.getToRoom().getRoomType() != Dungeon.RoomType.START) {
                spawnMonsters(door.getToRoom());
            }
        }

        buildPath(door);
    }

    private void dissolveDoorBlocks(Dungeon.Door door, Plugin plugin) {
        new BukkitRunnable() {
            int radius = 0;
            final int maxRadius = 5;
            final Location center = door.getLocation().clone();

            @Override
            public void run() {
                if (radius > maxRadius) {
                    this.cancel();
                    return;
                }

                for (int dx = -radius; dx <= radius; dx++) {
                    for (int dy = -radius; dy <= radius; dy++) {
                        for (int dz = -radius; dz <= radius; dz++) {
                            if (Math.abs(dx) + Math.abs(dy) + Math.abs(dz) != radius) continue;

                            Location loc = center.clone().add(dx, dy, dz);
                            if (loc.getBlock().getType() == Dungeon.DOOR_MATERIAL) {
                                loc.getBlock().setType(Material.AIR);
                                dungeon.getGeneratedBlocks().remove(loc);
                                dungeon.getGeneratedBlocks().remove(loc.clone().add(0, 1, 0));
                                // 粒子效果
                                loc.getWorld().spawnParticle(Particle.BLOCK, loc.clone().add(0.5, 0.5, 0.5),
                                        10, 0.2, 0.2, 0.2, Dungeon.DOOR_MATERIAL.createBlockData());
                                loc.getWorld().playSound(loc, Sound.BLOCK_METAL_BREAK, 1.0f, 1.0f);
                            }
                        }
                    }
                }
                radius++;
            }
        }.runTaskTimer(plugin, 0, 2);
    }

    private void spawnBoss(Dungeon.GridCell room) {
        Location center = room.getOrigin().clone().add(Dungeon.ROOM_SIZE / 2.0, 1, Dungeon.ROOM_SIZE / 2.0);
        dungeon.getWorld().spawnEntity(center, EntityType.WITHER_SKELETON);
        dungeon.getWorld().spawnEntity(center.clone().add(1, 0, 0), EntityType.BLAZE);
    }

    private void spawnMonsters(Dungeon.GridCell room) {
        Location center = room.getOrigin().clone().add(Dungeon.ROOM_SIZE / 2.0, 1, Dungeon.ROOM_SIZE / 2.0);
        int count = random.nextInt(3) + 2;

        for (int i = 0; i < count; i++) {
            Location spawn = center.clone().add(random.nextInt(5) - 2, 0, random.nextInt(5) - 2);
            EntityType type;
            double r = random.nextDouble();
            if (r < 0.4) type = EntityType.ZOMBIE;
            else if (r < 0.7) type = EntityType.SKELETON;
            else if (r < 0.9) type = EntityType.SPIDER;
            else type = EntityType.CAVE_SPIDER;
            dungeon.getWorld().spawnEntity(spawn, type);
        }
    }
}