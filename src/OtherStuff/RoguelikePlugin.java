package OtherStuff;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.noise.SimplexNoiseGenerator;

import java.util.*;

public class RoguelikePlugin implements CommandExecutor, Listener {

    private final JavaPlugin plugin;
    private DungeonGenerator activeDungeon;

    public RoguelikePlugin(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getCommand("roguelike").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }
        if (args.length > 0 && args[0].equalsIgnoreCase("start")) {
            if (activeDungeon != null) {
                player.sendMessage(ChatColor.RED + "A dungeon is already active. Finish or clean it first.");
                return true;
            }
            // 开局调整：改为生存模式，并给予铁剑和牛排
            player.setGameMode(GameMode.SURVIVAL);
            ItemStack ironSword = new ItemStack(Material.IRON_SWORD);
            ItemStack steak = new ItemStack(Material.COOKED_BEEF, 10);
            HashMap<Integer, ItemStack> remaining = player.getInventory().addItem(ironSword, steak);
            for (ItemStack leftover : remaining.values()) {
                player.getWorld().dropItem(player.getLocation(), leftover);
            }
            player.sendMessage(ChatColor.GREEN + "你已进入生存模式，并获得铁剑和牛排！");

            DungeonGenerator generator = new DungeonGenerator(plugin, player);
            String error = generator.checkBlocked();
            if (error != null) {
                player.sendMessage(ChatColor.RED + "Cannot generate dungeon: " + error);
                return true;
            }
            generator.generateStartRoom();
            activeDungeon = generator;
            return true;
        }
        return false;
    }

    @EventHandler
    public void onDoorInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null || activeDungeon == null) return;
        if (activeDungeon.isDoorBlock(block)) {
            event.setCancelled(true);
            activeDungeon.expandFromDoor(block, event.getPlayer());
        } else if (activeDungeon.isAltarBlock(block)) {
            event.setCancelled(true);
            activeDungeon.onAltarInteract(event.getPlayer(), block);
        }
    }

    @EventHandler
    public void onBossDeath(EntityDeathEvent event) {
        if (activeDungeon == null) return;
        if (event.getEntity().equals(activeDungeon.getBoss())) {
            Player killer = event.getEntity().getKiller();
            if (killer != null) {
                activeDungeon.complete(killer);
                activeDungeon = null;
            }
        }
    }

    public void onDisable() {
        if (activeDungeon != null) {
            activeDungeon.cleanup();
        }
    }

    // ==================== 内部类 DungeonGenerator ====================
    private static class DungeonGenerator {
        // ========== 可调参数 ==========
        private static final int GRID_SIZE = 4;
        private static final int ROOM_SIZE = 13;
        private static final int DOOR_WIDTH = 7;
        private static final int DOOR_HEIGHT = 4;
        private static final int WALL_HEIGHT = 6;
        private static final double ENEMY_CHANCE = 0.35;
        private static final double TRAP_CHANCE = 0.20;
        private static final double TREASURE_CHANCE = 0.20;
        private static final double BUFF_CHANCE = 0.15;
        private static final double ALTAR_CHANCE = 0.10;
        private static final Material[] FLOOR_MATERIALS = {
                Material.STONE_BRICKS, Material.CRACKED_STONE_BRICKS,
                Material.MOSSY_STONE_BRICKS, Material.SMOOTH_STONE,
                Material.ANDESITE, Material.DIORITE, Material.GRANITE
        };
        private static final Material[] WALL_MATERIALS = {
                Material.STONE_BRICKS, Material.MOSSY_STONE_BRICKS,
                Material.CHISELED_STONE_BRICKS, Material.POLISHED_ANDESITE,
                Material.POLISHED_DIORITE, Material.POLISHED_GRANITE
        };
        // =================================

        private final JavaPlugin plugin;
        private final Player player;
        private final World world;
        private final int centerX, centerY, centerZ;
        private final Random random = new Random();
        private final SimplexNoiseGenerator noiseFloor;
        private final SimplexNoiseGenerator noiseWall;

        private boolean[][] layout;                 // 网格是否存在房间
        private Room[][] rooms;                     // 房间对象数组
        private final Map<Location, Door> doorMap = new HashMap<>();
        private final Set<Location> altars = new HashSet<>();

        private Zombie boss;
        private Block startChestBlock;
        private boolean bossSpawned = false;

        private enum RoomType {EMPTY, ENEMY, TRAP, TREASURE, BUFF, ALTAR, BOSS}

        private static class Door {
            BlockFace facing;          // 从当前房间指向邻居的方向
            Location primaryBlock;     // 门中心底部方块
            List<Location> blocks;     // 所有门方块的位置
            int fromX, fromZ;          // 当前房间坐标
            int toX, toZ;              // 邻居房间坐标
            Door(BlockFace facing, Location primaryBlock, List<Location> blocks, int fromX, int fromZ, int toX, int toZ) {
                this.facing = facing;
                this.primaryBlock = primaryBlock;
                this.blocks = blocks;
                this.fromX = fromX;
                this.fromZ = fromZ;
                this.toX = toX;
                this.toZ = toZ;
            }
        }

        // ==================== 内部类 Room ====================
        private class Room {
            final int gridX, gridZ;
            final RoomType type;
            boolean generated;
            final int worldX, worldZ;      // 房间西北角坐标（实际地板范围以中心对称）
            final List<Block> placedBlocks = new ArrayList<>();
            final List<Entity> spawnedEntities = new ArrayList<>();

            Room(int gridX, int gridZ, RoomType type) {
                this.gridX = gridX;
                this.gridZ = gridZ;
                this.type = type;
                this.worldX = centerX + (gridX - 0) * ROOM_SIZE;
                this.worldZ = centerZ + (gridZ - 0) * ROOM_SIZE;
                this.generated = false;
            }

            /** 生成房间所有内容（地板、墙壁、内容、门） */
            void generate() {
                if (generated) return;
                placeFloorAndWalls();
                placeRoomContents();
                createAllDoors();
                generated = true;
                // 如果是 Boss 房间且尚未生成 Boss，则生成
                if (type == RoomType.BOSS && !bossSpawned) {
                    spawnBossAt(this);
                    bossSpawned = true;
                    player.sendMessage(ChatColor.RED + "你进入了Boss房间！强大的敌人出现了！");
                }
                // 播放生成特效
                Location center = new Location(world, worldX + 0.5, centerY, worldZ + 0.5);
                world.playSound(center, Sound.BLOCK_STONE_PLACE, 0.8f, 0.8f);
                world.spawnParticle(Particle.ELECTRIC_SPARK, center, 20, 0.5, 1, 0.5, 0.1);
            }

            private void placeFloorAndWalls() {
                int floorY = centerY - 1;
                int half = ROOM_SIZE / 2;

                // 地板
                for (int dx = -half; dx <= half; dx++) {
                    for (int dz = -half; dz <= half; dz++) {
                        Block floor = world.getBlockAt(worldX + dx, floorY, worldZ + dz);
                        floor.setType(getFloorMaterial(worldX + dx, worldZ + dz));
                        placedBlocks.add(floor);
                    }
                }

                // 金块路径（通往各门）
                if (hasDoor(BlockFace.NORTH)) {
                    for (int step = 1; step <= half; step++) {
                        Block path = world.getBlockAt(worldX, floorY, worldZ + step);
                        path.setType(getPathMaterial());
                        placedBlocks.add(path);
                    }
                }
                if (hasDoor(BlockFace.SOUTH)) {
                    for (int step = 1; step <= half; step++) {
                        Block path = world.getBlockAt(worldX, floorY, worldZ - step);
                        path.setType(getPathMaterial());
                        placedBlocks.add(path);
                    }
                }
                if (hasDoor(BlockFace.EAST)) {
                    for (int step = 1; step <= half; step++) {
                        Block path = world.getBlockAt(worldX + step, floorY, worldZ);
                        path.setType(getPathMaterial());
                        placedBlocks.add(path);
                    }
                }
                if (hasDoor(BlockFace.WEST)) {
                    for (int step = 1; step <= half; step++) {
                        Block path = world.getBlockAt(worldX - step, floorY, worldZ);
                        path.setType(getPathMaterial());
                        placedBlocks.add(path);
                    }
                }

                // 墙壁（避开门的区域）
                boolean neighborNorth = (gridZ + 1 < GRID_SIZE) && layout[gridX][gridZ + 1];
                boolean neighborSouth = (gridZ - 1 >= 0) && layout[gridX][gridZ - 1];
                boolean neighborEast  = (gridX + 1 < GRID_SIZE) && layout[gridX + 1][gridZ];
                boolean neighborWest  = (gridX - 1 >= 0) && layout[gridX - 1][gridZ];

                for (int dx = -half; dx <= half; dx++) {
                    for (int dz = -half; dz <= half; dz++) {
                        if (Math.abs(dx) != half && Math.abs(dz) != half) continue;
                        boolean isNorth = (dz == half);
                        boolean isSouth = (dz == -half);
                        boolean isEast  = (dx == half);
                        boolean isWest  = (dx == -half);

                        if (isNorth && neighborNorth) continue;
                        if (isSouth && neighborSouth) continue;
                        if (isEast  && neighborEast)  continue;
                        if (isWest  && neighborWest)  continue;

                        boolean isDoorArea = false;
                        if (isNorth && hasDoor(BlockFace.NORTH)) {
                            if (Math.abs(dx) <= DOOR_WIDTH / 2) isDoorArea = true;
                        }
                        if (isSouth && hasDoor(BlockFace.SOUTH)) {
                            if (Math.abs(dx) <= DOOR_WIDTH / 2) isDoorArea = true;
                        }
                        if (isEast && hasDoor(BlockFace.EAST)) {
                            if (Math.abs(dz) <= DOOR_WIDTH / 2) isDoorArea = true;
                        }
                        if (isWest && hasDoor(BlockFace.WEST)) {
                            if (Math.abs(dz) <= DOOR_WIDTH / 2) isDoorArea = true;
                        }

                        for (int h = 0; h < WALL_HEIGHT; h++) {
                            if (isDoorArea && h < DOOR_HEIGHT) continue;
                            Block wall = world.getBlockAt(worldX + dx, floorY + 1 + h, worldZ + dz);
                            wall.setType(getWallMaterial(worldX + dx, floorY + 1 + h, worldZ + dz));
                            placedBlocks.add(wall);
                        }
                    }
                }
            }

            private boolean hasDoor(BlockFace face) {
                return switch (face) {
                    case NORTH -> (gridZ + 1 < GRID_SIZE) && layout[gridX][gridZ + 1];
                    case SOUTH -> (gridZ - 1 >= 0) && layout[gridX][gridZ - 1];
                    case EAST  -> (gridX + 1 < GRID_SIZE) && layout[gridX + 1][gridZ];
                    case WEST  -> (gridX - 1 >= 0) && layout[gridX - 1][gridZ];
                    default -> false;
                };
            }

            private void createAllDoors() {
                if (hasDoor(BlockFace.NORTH)) createDoorWithNeighbor(BlockFace.NORTH, gridX, gridZ + 1);
                if (hasDoor(BlockFace.SOUTH)) createDoorWithNeighbor(BlockFace.SOUTH, gridX, gridZ - 1);
                if (hasDoor(BlockFace.EAST))  createDoorWithNeighbor(BlockFace.EAST,  gridX + 1, gridZ);
                if (hasDoor(BlockFace.WEST))  createDoorWithNeighbor(BlockFace.WEST,  gridX - 1, gridZ);
            }

            private void createDoorWithNeighbor(BlockFace direction, int neighborX, int neighborZ) {
                if (!layout[neighborX][neighborZ]) return;
                // 计算门的位置
                int half = ROOM_SIZE / 2;
                int doorCenterX, doorCenterZ;
                BlockFace doorFacing;
                switch (direction) {
                    case NORTH -> {
                        doorCenterX = worldX;
                        doorCenterZ = worldZ + half;
                        doorFacing = BlockFace.NORTH;
                    }
                    case SOUTH -> {
                        doorCenterX = worldX;
                        doorCenterZ = worldZ - half;
                        doorFacing = BlockFace.SOUTH;
                    }
                    case EAST -> {
                        doorCenterX = worldX + half;
                        doorCenterZ = worldZ;
                        doorFacing = BlockFace.EAST;
                    }
                    default -> { // WEST
                        doorCenterX = worldX - half;
                        doorCenterZ = worldZ;
                        doorFacing = BlockFace.WEST;
                    }
                }

                BlockFace widthDir = getPerpendicular(doorFacing);
                int widthStart = -(DOOR_WIDTH / 2);
                List<Location> doorBlocks = new ArrayList<>();
                Location primaryBlock = null;
                int doorBaseY = centerY;

                // 检查门是否已经存在（避免重复创建）
                boolean alreadyExists = false;
                for (int w = 0; w < DOOR_WIDTH; w++) {
                    int offsetX = widthDir.getModX() * (widthStart + w);
                    int offsetZ = widthDir.getModZ() * (widthStart + w);
                    for (int h = 0; h < DOOR_HEIGHT; h++) {
                        int blockX = doorCenterX + offsetX;
                        int blockZ = doorCenterZ + offsetZ;
                        int blockY = doorBaseY + h;
                        Location loc = new Location(world, blockX, blockY, blockZ);
                        if (doorMap.containsKey(loc)) {
                            alreadyExists = true;
                            break;
                        }
                    }
                    if (alreadyExists) break;
                }
                if (alreadyExists) return;

                // 创建门方块
                for (int w = 0; w < DOOR_WIDTH; w++) {
                    int offsetX = widthDir.getModX() * (widthStart + w);
                    int offsetZ = widthDir.getModZ() * (widthStart + w);
                    for (int h = 0; h < DOOR_HEIGHT; h++) {
                        int blockX = doorCenterX + offsetX;
                        int blockZ = doorCenterZ + offsetZ;
                        int blockY = doorBaseY + h;
                        Location loc = new Location(world, blockX, blockY, blockZ);
                        Block target = world.getBlockAt(loc);
                        if (target.getType() != Material.AIR) {
                            // 如果已经有方块（可能是其他房间的墙壁），先移除记录
                            placedBlocks.remove(target);
                        }
                        target.setType(Material.IRON_BLOCK);
                        placedBlocks.add(target);
                        doorBlocks.add(loc);
                        if (h == 0 && w == DOOR_WIDTH / 2) primaryBlock = loc;
                    }
                }
                if (primaryBlock == null) return;

                Door door = new Door(doorFacing, primaryBlock, doorBlocks, gridX, gridZ, neighborX, neighborZ);
                for (Location loc : doorBlocks) doorMap.put(loc, door);
            }

            private void placeRoomContents() {
                int floorY = centerY - 1;
                Location center = new Location(world, worldX + 0.5, centerY, worldZ + 0.5);
                switch (type) {
                    case ENEMY -> spawnEnemies(center);
                    case TRAP -> placeTrap(worldX, worldZ, floorY);
                    case TREASURE -> placeTreasureChest(worldX, worldZ, floorY);
                    case BUFF -> applyBuffToPlayer(player, center);
                    case ALTAR -> placeAltar(worldX, worldZ, floorY);
                    default -> {}
                }
                // 房间中心红石火把标记
                Block torch = world.getBlockAt(worldX, centerY, worldZ);
                torch.setType(Material.REDSTONE_TORCH);
                placedBlocks.add(torch);
            }

            private void spawnEnemies(Location center) {
                int count = random.nextInt(3) + 1;
                for (int i = 0; i < count; i++) {
                    EntityType type = switch (random.nextInt(3)) {
                        case 0 -> EntityType.ZOMBIE;
                        case 1 -> EntityType.SKELETON;
                        default -> EntityType.SPIDER;
                    };
                    Entity e = world.spawnEntity(center.clone().add(random.nextDouble() - 0.5, 0, random.nextDouble() - 0.5), type);
                    if (e instanceof Monster m) m.setRemoveWhenFarAway(false);
                    spawnedEntities.add(e);
                }
                world.playSound(center, Sound.ENTITY_ZOMBIE_AMBIENT, 1.0f, 0.8f);
                world.spawnParticle(Particle.ENTITY_EFFECT, center, 15, 0.5, 0.5, 0.5, 0.2, Color.ORANGE);
            }

            private void placeTrap(int roomX, int roomZ, int floorY) {
                int half = ROOM_SIZE / 2;
                for (int dx = -half + 1; dx <= half - 1; dx++) {
                    for (int dz = -half + 1; dz <= half - 1; dz++) {
                        if (random.nextDouble() < 0.3) {
                            Block b = world.getBlockAt(roomX + dx, floorY, roomZ + dz);
                            b.setType(Material.MAGMA_BLOCK);
                            placedBlocks.add(b);
                        }
                    }
                }
                Location trapCenter = new Location(world, roomX + 0.5, floorY + 0.5, roomZ + 0.5);
                world.playSound(trapCenter, Sound.BLOCK_LAVA_AMBIENT, 1.0f, 1.0f);
                world.spawnParticle(Particle.LAVA, trapCenter, 10, 0.5, 0, 0.5, 0.1);
            }

            private void placeTreasureChest(int roomX, int roomZ, int floorY) {
                Block chestBlock = world.getBlockAt(roomX, floorY, roomZ);
                chestBlock.setType(Material.CHEST);
                placedBlocks.add(chestBlock);
                if (chestBlock.getState() instanceof Chest chest) {
                    chest.getInventory().addItem(generateReward());
                }
                Location chestLoc = chestBlock.getLocation().add(0.5, 0.5, 0.5);
                world.playSound(chestLoc, Sound.BLOCK_ANVIL_PLACE, 1.0f, 1.2f);
                world.spawnParticle(Particle.HEART, chestLoc, 10, 0.5, 0.5, 0.5, 0.1);
            }

            private ItemStack generateReward() {
                int r = random.nextInt(10);
                if (r < 3) return new ItemStack(Material.IRON_INGOT, random.nextInt(5) + 1);
                if (r < 6) return new ItemStack(Material.GOLD_INGOT, random.nextInt(3) + 1);
                if (r < 9) return new ItemStack(Material.DIAMOND, 1);
                return new ItemStack(Material.EMERALD, random.nextInt(3) + 1);
            }

            private void applyBuffToPlayer(Player target, Location center) {
                target.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 30 * 20, 1));
                target.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 30 * 20, 0));
                target.sendMessage(ChatColor.LIGHT_PURPLE + "你进入了一个祝福房间！获得速度 II 和力量 I (30秒)");
                world.playSound(center, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                world.spawnParticle(Particle.HAPPY_VILLAGER, center, 30, 0.5, 0.5, 0.5, 0.1);
            }

            private void placeAltar(int roomX, int roomZ, int floorY) {
                Block altar = world.getBlockAt(roomX, floorY, roomZ);
                altar.setType(Material.GOLD_BLOCK);
                placedBlocks.add(altar);
                altars.add(altar.getLocation());
                player.sendMessage(ChatColor.GOLD + "你发现了一个祭坛！右键金块以获得奖励（或惩罚）");
                world.playSound(altar.getLocation().add(0.5, 0.5, 0.5), Sound.BLOCK_ANVIL_LAND, 0.8f, 1.2f);
            }

            void cleanup() {
                for (Entity e : spawnedEntities) {
                    if (e != null && !e.isDead()) e.remove();
                }
                spawnedEntities.clear();
                for (Block b : placedBlocks) {
                    if (b.getType() != Material.AIR) b.setType(Material.AIR);
                }
                placedBlocks.clear();
            }
        }

        // ==================== DungeonGenerator 构造与布局 ====================
        public DungeonGenerator(JavaPlugin plugin, Player player) {
            this.plugin = plugin;
            this.player = player;
            this.world = player.getWorld();
            Location loc = player.getLocation();
            this.centerX = loc.getBlockX();
            this.centerY = loc.getBlockY();      // 玩家脚部高度
            this.centerZ = loc.getBlockZ();
            generateLayout();

            long seed = world.getSeed() + centerX * 31L + centerZ * 71L;
            noiseFloor = new SimplexNoiseGenerator((int) seed);
            noiseWall = new SimplexNoiseGenerator((int) (seed + 12345));
        }

        private void generateLayout() {
            layout = new boolean[GRID_SIZE][GRID_SIZE];
            rooms = new Room[GRID_SIZE][GRID_SIZE];

            // 随机游走生成主路径
            boolean[][] visited = new boolean[GRID_SIZE][GRID_SIZE];
            int curX = 0, curY = 0;
            visited[curX][curY] = true;
            int steps = 0;
            int maxSteps = 200;
            Random r = new Random();
            while ((curX != GRID_SIZE - 1 || curY != GRID_SIZE - 1) && steps < maxSteps) {
                List<int[]> directions = Arrays.asList(
                        new int[]{0, 1}, new int[]{0, -1},
                        new int[]{1, 0}, new int[]{-1, 0}
                );
                Collections.shuffle(directions, r);
                boolean moved = false;
                for (int[] dir : directions) {
                    int nx = curX + dir[0];
                    int ny = curY + dir[1];
                    if (nx >= 0 && nx < GRID_SIZE && ny >= 0 && ny < GRID_SIZE) {
                        curX = nx;
                        curY = ny;
                        visited[curX][curY] = true;
                        moved = true;
                        break;
                    }
                }
                if (!moved) break;
                steps++;
            }
            visited[GRID_SIZE - 1][GRID_SIZE - 1] = true;

            // 随机添加分支
            for (int i = 0; i < GRID_SIZE; i++) {
                for (int j = 0; j < GRID_SIZE; j++) {
                    if (visited[i][j] && r.nextDouble() < 0.3) {
                        for (int[] dir : new int[][]{{0, 1}, {0, -1}, {1, 0}, {-1, 0}}) {
                            int ni = i + dir[0];
                            int nj = j + dir[1];
                            if (ni >= 0 && ni < GRID_SIZE && nj >= 0 && nj < GRID_SIZE && !visited[ni][nj] && r.nextDouble() < 0.5) {
                                visited[ni][nj] = true;
                                break;
                            }
                        }
                    }
                }
            }

            for (int i = 0; i < GRID_SIZE; i++) {
                System.arraycopy(visited[i], 0, layout[i], 0, GRID_SIZE);
            }

            // 分配房间类型
            for (int i = 0; i < GRID_SIZE; i++) {
                for (int j = 0; j < GRID_SIZE; j++) {
                    if (!layout[i][j]) continue;
                    RoomType type;
                    if (i == 0 && j == 0) {
                        type = RoomType.EMPTY;
                    } else if (i == GRID_SIZE - 1 && j == GRID_SIZE - 1) {
                        type = RoomType.BOSS;
                    } else {
                        type = selectRandomRoomType();
                    }
                    rooms[i][j] = new Room(i, j, type);
                }
            }
        }

        private RoomType selectRandomRoomType() {
            double r = random.nextDouble();
            double cum = ENEMY_CHANCE;
            if (r < cum) return RoomType.ENEMY;
            cum += TRAP_CHANCE;
            if (r < cum) return RoomType.TRAP;
            cum += TREASURE_CHANCE;
            if (r < cum) return RoomType.TREASURE;
            cum += BUFF_CHANCE;
            if (r < cum) return RoomType.BUFF;
            return RoomType.ALTAR;
        }

        // ==================== 噪声纹理 ====================
        private Material getFloorMaterial(int worldX, int worldZ) {
            double noise = noiseFloor.noise(worldX * 0.1, worldZ * 0.1, 0.0);
            int index = (int) Math.floor((noise + 1) / 2 * FLOOR_MATERIALS.length);
            index = Math.max(0, Math.min(index, FLOOR_MATERIALS.length - 1));
            return FLOOR_MATERIALS[index];
        }

        private Material getWallMaterial(int worldX, int worldY, int worldZ) {
            double noise = noiseWall.noise(worldX * 0.1, worldY * 0.05, worldZ * 0.1);
            int index = (int) Math.floor((noise + 1) / 2 * WALL_MATERIALS.length);
            index = Math.max(0, Math.min(index, WALL_MATERIALS.length - 1));
            return WALL_MATERIALS[index];
        }

        private Material getPathMaterial() {
            return Material.GOLD_BLOCK;
        }

        private BlockFace getPerpendicular(BlockFace face) {
            return switch (face) {
                case NORTH, SOUTH -> BlockFace.EAST;
                case EAST, WEST -> BlockFace.NORTH;
                default -> BlockFace.NORTH;
            };
        }

        // ==================== 地牢生成与交互 ====================
        public String checkBlocked() {
            int floorY = centerY - 1;
            for (int i = 0; i < GRID_SIZE; i++) {
                for (int j = 0; j < GRID_SIZE; j++) {
                    if (!layout[i][j]) continue;
                    int worldX = centerX + (i - 0) * ROOM_SIZE;
                    int worldZ = centerZ + (j - 0) * ROOM_SIZE;
                    int half = ROOM_SIZE / 2;
                    for (int dx = -half - 1; dx <= half + 1; dx++) {
                        for (int dz = -half - 1; dz <= half + 1; dz++) {
                            for (int dy = 0; dy < WALL_HEIGHT + 3; dy++) {
                                Block b = world.getBlockAt(worldX + dx, floorY + dy, worldZ + dz);
                                if (!b.isEmpty() && !b.isLiquid()) {
                                    return "Area blocked at " + b.getLocation().toVector();
                                }
                            }
                        }
                    }
                }
            }
            return null;
        }

        public void generateStartRoom() {
            int floorY = centerY - 1;
            startChestBlock = world.getBlockAt(centerX, floorY, centerZ);
            startChestBlock.setType(Material.BEDROCK);
            // 起点房间也是普通房间，使用 Room 生成
            Room startRoom = rooms[0][0];
            startRoom.generate();
            player.sendMessage(ChatColor.GREEN + "起点房间已生成！右键铁门探索新区域。");
            Location startLoc = new Location(world, centerX + 0.5, centerY, centerZ + 0.5);
            world.playSound(startLoc, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            world.spawnParticle(Particle.HAPPY_VILLAGER, startLoc, 30, 1, 1, 1, 0.1);
        }

        public void expandFromDoor(Block clickedBlock, Player clicker) {
            Door door = doorMap.get(clickedBlock.getLocation());
            if (door == null) return;
            int neighborX = door.toX, neighborZ = door.toZ;
            if (neighborX < 0 || neighborX >= GRID_SIZE || neighborZ < 0 || neighborZ >= GRID_SIZE) {
                clicker.sendMessage(ChatColor.RED + "这个方向没有房间。");
                return;
            }
            if (!layout[neighborX][neighborZ]) {
                clicker.sendMessage(ChatColor.RED + "这个方向没有房间。");
                return;
            }
            Room neighborRoom = rooms[neighborX][neighborZ];
            if (neighborRoom.generated) {
                clicker.sendMessage(ChatColor.RED + "这个方向已经探索过了！");
                return;
            }
            // 移除当前门的所有方块
            for (Location loc : door.blocks) {
                Block b = world.getBlockAt(loc);
                if (b.getType() == Material.IRON_BLOCK) {
                    b.setType(Material.AIR);
                }
                doorMap.remove(loc);
            }
            Location doorCenter = door.blocks.get(0).clone().add(0.5, 0.5, 0.5);
            world.playSound(doorCenter, Sound.BLOCK_IRON_DOOR_OPEN, 1.0f, 1.0f);
            world.spawnParticle(Particle.CLOUD, doorCenter, 20, 0.5, 0.5, 0.5, 0.1);
            // 生成邻居房间
            neighborRoom.generate();
            clicker.sendMessage(ChatColor.GREEN + "铁门消失，新的区域出现了！");
        }

        public void onAltarInteract(Player clicker, Block altarBlock) {
            Location loc = altarBlock.getLocation();
            if (!altars.contains(loc)) return;
            altars.remove(loc);

            // 查找祭坛所在的房间
            Room targetRoom = null;
            for (int i = 0; i < GRID_SIZE; i++) {
                for (int j = 0; j < GRID_SIZE; j++) {
                    if (rooms[i][j] != null && rooms[i][j].placedBlocks.contains(altarBlock)) {
                        targetRoom = rooms[i][j];
                        break;
                    }
                }
            }

            int r = random.nextInt(10);
            if (r < 4) {
                PotionEffectType type = switch (random.nextInt(3)) {
                    case 0 -> PotionEffectType.SPEED;
                    case 1 -> PotionEffectType.STRENGTH;
                    default -> PotionEffectType.REGENERATION;
                };
                clicker.addPotionEffect(new PotionEffect(type, 30 * 20, 1));
                clicker.sendMessage(ChatColor.AQUA + "祭坛赐予你 " + type.getName() + " 效果！");
                world.playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                world.spawnParticle(Particle.HAPPY_VILLAGER, loc.add(0.5, 0.5, 0.5), 20, 0.5, 0.5, 0.5, 0.1);
            } else if (r < 7) {
                ItemStack reward = new ItemStack(Material.DIAMOND, random.nextInt(2) + 1);
                clicker.getInventory().addItem(reward);
                clicker.sendMessage(ChatColor.YELLOW + "祭坛给了你 " + reward.getAmount() + " 颗钻石！");
                world.playSound(loc, Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.0f);
                world.spawnParticle(Particle.HEART, loc.add(0.5, 0.5, 0.5), 15, 0.5, 0.5, 0.5, 0.1);
            } else {
                int mobCount = random.nextInt(2) + 1;
                for (int i = 0; i < mobCount; i++) {
                    Entity e = world.spawnEntity(loc.clone().add(0.5, 1, 0.5), EntityType.ZOMBIE);
                    if (e instanceof Monster m) m.setRemoveWhenFarAway(false);
                    if (targetRoom != null) targetRoom.spawnedEntities.add(e);
                }
                clicker.sendMessage(ChatColor.RED + "祭坛触发了陷阱！怪物出现了！");
                world.playSound(loc, Sound.ENTITY_ZOMBIE_AMBIENT, 1.0f, 1.0f);
                world.spawnParticle(Particle.LARGE_SMOKE, loc.add(0.5, 0.5, 0.5), 20, 0.5, 0.5, 0.5, 0.05);
            }
            altarBlock.setType(Material.AIR);
            if (targetRoom != null) targetRoom.placedBlocks.remove(altarBlock);
        }

        private void spawnBossAt(Room bossRoom) {
            Location loc = new Location(world, bossRoom.worldX + 0.5, centerY, bossRoom.worldZ + 0.5);
            boss = (Zombie) world.spawnEntity(loc, EntityType.ZOMBIE);
            boss.setCustomName(ChatColor.RED + "地牢守卫");
            boss.setCustomNameVisible(true);
            boss.setMaxHealth(80.0);
            boss.setHealth(80.0);
            boss.getEquipment().setItemInMainHand(new ItemStack(Material.IRON_SWORD));
            boss.getEquipment().setHelmet(new ItemStack(Material.IRON_HELMET));
            boss.getEquipment().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
            bossRoom.spawnedEntities.add(boss);
            world.playSound(loc, Sound.ENTITY_WITHER_SPAWN, 1.5f, 0.8f);
            world.spawnParticle(Particle.EXPLOSION, loc, 5, 0, 0, 0, 0);
            world.spawnParticle(Particle.LARGE_SMOKE, loc, 20, 1, 1, 1, 0.2);
            world.spawnParticle(Particle.FLAME, loc, 30, 1, 1, 1, 0.1);
        }

        public void complete(Player killer) {
            int floorY = centerY - 1;
            startChestBlock.setType(Material.CHEST);
            if (startChestBlock.getState() instanceof Chest chest) {
                chest.getInventory().addItem(new ItemStack(Material.DIAMOND, 5));
            }
            killer.sendMessage(ChatColor.GOLD + "恭喜通关！获得 5 颗钻石！");
            Location completeLoc = startChestBlock.getLocation().add(0.5, 1, 0.5);
            world.playSound(completeLoc, Sound.ENTITY_ENDER_DRAGON_DEATH, 1.0f, 1.0f);
            world.spawnParticle(Particle.FIREWORK, completeLoc, 50, 2, 2, 2, 0.2);
            world.spawnParticle(Particle.HAPPY_VILLAGER, completeLoc, 30, 1, 1, 1, 0.1);
            plugin.getServer().getScheduler().runTaskLater(plugin, this::cleanup, 20L);
        }

        public void cleanup() {
            for (int i = 0; i < GRID_SIZE; i++) {
                for (int j = 0; j < GRID_SIZE; j++) {
                    if (rooms[i][j] != null) rooms[i][j].cleanup();
                }
            }
            doorMap.clear();
            altars.clear();
            if (player.isOnline()) player.sendMessage(ChatColor.GRAY + "地牢已清理。");
        }

        public boolean isDoorBlock(Block block) {
            return doorMap.containsKey(block.getLocation());
        }

        public boolean isAltarBlock(Block block) {
            return altars.contains(block.getLocation());
        }

        public Zombie getBoss() {
            return boss;
        }
    }
}