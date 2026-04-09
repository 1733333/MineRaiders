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

/**
 * 地牢冒险插件核心类
 * 实现命令执行和事件监听，管理地牢生成、房间交互、Boss战等核心逻辑
 */
public class RoguelikePlugin implements CommandExecutor, Listener {

    private final JavaPlugin plugin;
    private DungeonGenerator activeDungeon; // 当前激活的地牢实例

    /**
     * 插件初始化
     * @param plugin 主插件实例
     */
    public RoguelikePlugin(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getCommand("roguelike").setExecutor(this);
    }

    /**
     * 处理/roguelike命令
     */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        // 处理start子命令 - 启动地牢
        if (args.length > 0 && args[0].equalsIgnoreCase("start")) {
            if (activeDungeon != null) {
                player.sendMessage(ChatColor.RED + "A dungeon is already active. Finish or clean it first.");
                return true;
            }

            // 初始化玩家状态：生存模式 + 基础装备
            player.setGameMode(GameMode.SURVIVAL);
            ItemStack ironSword = new ItemStack(Material.IRON_SWORD);
            ItemStack steak = new ItemStack(Material.COOKED_BEEF, 10);
            HashMap<Integer, ItemStack> remaining = player.getInventory().addItem(ironSword, steak);
            for (ItemStack leftover : remaining.values()) {
                player.getWorld().dropItem(player.getLocation(), leftover);
            }
            player.sendMessage(ChatColor.GREEN + "你已进入生存模式，并获得铁剑和牛排！");

            // 生成地牢
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

    /**
     * 处理门/祭坛交互事件
     */
    @EventHandler
    public void onDoorInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null || activeDungeon == null) return;

        // 门交互 - 扩展地牢
        if (activeDungeon.isDoorBlock(block)) {
            event.setCancelled(true);
            activeDungeon.expandFromDoor(block, event.getPlayer());
        }
        // 祭坛交互 - 触发奖励/惩罚
        else if (activeDungeon.isAltarBlock(block)) {
            event.setCancelled(true);
            activeDungeon.onAltarInteract(event.getPlayer(), block);
        }
    }

    /**
     * 处理Boss死亡事件
     */
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

    /**
     * 插件禁用时清理地牢
     */
    public void onDisable() {
        if (activeDungeon != null) {
            activeDungeon.cleanup();
        }
    }

    // ==================== 内部类：地牢生成器 ====================
    private static class DungeonGenerator {
        // ========== 核心配置参数 ==========
        private static final int GRID_SIZE = 4;          // 地牢网格大小（房间数量）
        private static final int ROOM_SIZE = 13;         // 单房间尺寸（方块数）
        private static final int DOOR_WIDTH = 7;         // 门宽度
        private static final int DOOR_HEIGHT = 4;        // 门高度
        private static final int WALL_HEIGHT = 6;        // 墙壁高度
        private static final double ENEMY_CHANCE = 0.35; // 怪物房间概率
        private static final double TRAP_CHANCE = 0.20;  // 陷阱房间概率
        private static final double TREASURE_CHANCE = 0.20; // 宝藏房间概率
        private static final double BUFF_CHANCE = 0.15;  // 增益房间概率
        private static final double ALTAR_CHANCE = 0.10; // 祭坛房间概率

        // 地板材质列表（随机噪声选择）
        private static final Material[] FLOOR_MATERIALS = {
                Material.STONE_BRICKS, Material.CRACKED_STONE_BRICKS,
                Material.MOSSY_STONE_BRICKS, Material.SMOOTH_STONE,
                Material.ANDESITE, Material.DIORITE, Material.GRANITE
        };

        // 墙壁材质列表（随机噪声选择）
        private static final Material[] WALL_MATERIALS = {
                Material.STONE_BRICKS, Material.MOSSY_STONE_BRICKS,
                Material.CHISELED_STONE_BRICKS, Material.POLISHED_ANDESITE,
                Material.POLISHED_DIORITE, Material.POLISHED_GRANITE
        };

        // ========== 核心成员变量 ==========
        private final JavaPlugin plugin;
        private final Player player;
        private final World world;
        private final int centerX, centerY, centerZ; // 地牢中心坐标（玩家初始位置）
        private final Random random = new Random();
        private final SimplexNoiseGenerator noiseFloor; // 地板材质噪声生成器
        private final SimplexNoiseGenerator noiseWall;  // 墙壁材质噪声生成器

        private boolean[][] layout;                 // 网格房间存在标记
        private Room[][] rooms;                     // 房间实例数组
        private final Map<Location, Door> doorMap = new HashMap<>(); // 门方块映射
        private final Set<Location> altars = new HashSet<>();        // 祭坛位置集合

        private Zombie boss;          // Boss实体
        private Block startChestBlock;// 起点奖励箱
        private boolean bossSpawned = false; // Boss是否已生成

        // ========== 内部枚举：房间类型 ==========
        private enum RoomType {
            EMPTY,    // 空房间（起点）
            ENEMY,    // 怪物房间
            TRAP,     // 陷阱房间
            TREASURE, // 宝藏房间
            BUFF,     // 增益房间
            ALTAR,    // 祭坛房间
            BOSS      // Boss房间
        }

        // ========== 内部类：门 ==========
        private static class Door {
            BlockFace facing;          // 门朝向（指向邻居房间）
            Location primaryBlock;     // 门中心底部方块
            List<Location> blocks;     // 门所有方块位置
            int fromX, fromZ;          // 源房间网格坐标
            int toX, toZ;              // 目标房间网格坐标

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

        // ========== 内部类：房间 ==========
        private class Room {
            final int gridX, gridZ;    // 房间网格坐标
            final RoomType type;       // 房间类型
            boolean generated;         // 是否已生成
            final int worldX, worldZ;  // 房间世界坐标（西北角）
            final List<Block> placedBlocks = new ArrayList<>(); // 房间生成的方块
            final List<Entity> spawnedEntities = new ArrayList<>(); // 房间生成的实体

            Room(int gridX, int gridZ, RoomType type) {
                this.gridX = gridX;
                this.gridZ = gridZ;
                this.type = type;
                this.worldX = centerX + gridX * ROOM_SIZE;
                this.worldZ = centerZ + gridZ * ROOM_SIZE;
                this.generated = false;
            }

            /**
             * 生成房间完整内容（地板、墙壁、交互物、门）
             */
            void generate() {
                if (generated) return;

                // 生成基础结构
                placeFloorAndWalls();
                // 生成房间特色内容（怪物/陷阱/宝藏等）
                placeRoomContents();
                // 创建房间所有门
                createAllDoors();

                generated = true;

                // Boss房间特殊处理：生成Boss
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

            /**
             * 放置房间地板和墙壁
             */
            private void placeFloorAndWalls() {
                int floorY = centerY - 1;
                int half = ROOM_SIZE / 2;

                // 1. 生成地板（随机材质）
                for (int dx = -half; dx <= half; dx++) {
                    for (int dz = -half; dz <= half; dz++) {
                        Block floor = world.getBlockAt(worldX + dx, floorY, worldZ + dz);
                        floor.setType(getFloorMaterial(worldX + dx, worldZ + dz));
                        placedBlocks.add(floor);
                    }
                }

                // 2. 生成金块路径（通往各门）
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

                // 3. 生成墙壁（避开门区域）
                boolean neighborNorth = (gridZ + 1 < GRID_SIZE) && layout[gridX][gridZ + 1];
                boolean neighborSouth = (gridZ - 1 >= 0) && layout[gridX][gridZ - 1];
                boolean neighborEast  = (gridX + 1 < GRID_SIZE) && layout[gridX + 1][gridZ];
                boolean neighborWest  = (gridX - 1 >= 0) && layout[gridX - 1][gridZ];

                for (int dx = -half; dx <= half; dx++) {
                    for (int dz = -half; dz <= half; dz++) {
                        // 仅处理房间边缘
                        if (Math.abs(dx) != half && Math.abs(dz) != half) continue;

                        boolean isNorth = (dz == half);
                        boolean isSouth = (dz == -half);
                        boolean isEast  = (dx == half);
                        boolean isWest  = (dx == -half);

                        // 跳过邻居房间方向（避免墙壁重叠）
                        if (isNorth && neighborNorth) continue;
                        if (isSouth && neighborSouth) continue;
                        if (isEast  && neighborEast)  continue;
                        if (isWest  && neighborWest)  continue;

                        // 跳过门区域
                        boolean isDoorArea = false;
                        if (isNorth && hasDoor(BlockFace.NORTH) && Math.abs(dx) <= DOOR_WIDTH / 2) isDoorArea = true;
                        if (isSouth && hasDoor(BlockFace.SOUTH) && Math.abs(dx) <= DOOR_WIDTH / 2) isDoorArea = true;
                        if (isEast  && hasDoor(BlockFace.EAST)  && Math.abs(dz) <= DOOR_WIDTH / 2) isDoorArea = true;
                        if (isWest  && hasDoor(BlockFace.WEST)  && Math.abs(dz) <= DOOR_WIDTH / 2) isDoorArea = true;

                        // 生成墙壁（随机材质）
                        for (int h = 0; h < WALL_HEIGHT; h++) {
                            if (isDoorArea && h < DOOR_HEIGHT) continue;
                            Block wall = world.getBlockAt(worldX + dx, floorY + 1 + h, worldZ + dz);
                            wall.setType(getWallMaterial(worldX + dx, floorY + 1 + h, worldZ + dz));
                            placedBlocks.add(wall);
                        }
                    }
                }
            }

            /**
             * 检查房间是否有指定方向的门
             */
            private boolean hasDoor(BlockFace face) {
                return switch (face) {
                    case NORTH -> (gridZ + 1 < GRID_SIZE) && layout[gridX][gridZ + 1];
                    case SOUTH -> (gridZ - 1 >= 0) && layout[gridX][gridZ - 1];
                    case EAST  -> (gridX + 1 < GRID_SIZE) && layout[gridX + 1][gridZ];
                    case WEST  -> (gridX - 1 >= 0) && layout[gridX - 1][gridZ];
                    default -> false;
                };
            }

            /**
             * 创建房间所有方向的门
             */
            private void createAllDoors() {
                if (hasDoor(BlockFace.NORTH)) createDoorWithNeighbor(BlockFace.NORTH, gridX, gridZ + 1);
                if (hasDoor(BlockFace.SOUTH)) createDoorWithNeighbor(BlockFace.SOUTH, gridX, gridZ - 1);
                if (hasDoor(BlockFace.EAST))  createDoorWithNeighbor(BlockFace.EAST,  gridX + 1, gridZ);
                if (hasDoor(BlockFace.WEST))  createDoorWithNeighbor(BlockFace.WEST,  gridX - 1, gridZ);
            }

            /**
             * 创建指定方向的门（与邻居房间连接）
             */
            private void createDoorWithNeighbor(BlockFace direction, int neighborX, int neighborZ) {
                if (!layout[neighborX][neighborZ]) return;

                // 计算门中心坐标
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

                // 门宽度方向（垂直于门朝向）
                BlockFace widthDir = getPerpendicular(doorFacing);
                int widthStart = -(DOOR_WIDTH / 2);
                List<Location> doorBlocks = new ArrayList<>();
                Location primaryBlock = null;
                int doorBaseY = centerY;

                // 检查门是否已存在（避免重复创建）
                boolean alreadyExists = false;
                for (int w = 0; w < DOOR_WIDTH; w++) {
                    int offsetX = widthDir.getModX() * (widthStart + w);
                    int offsetZ = widthDir.getModZ() * (widthStart + w);
                    for (int h = 0; h < DOOR_HEIGHT; h++) {
                        Location loc = new Location(world, doorCenterX + offsetX, doorBaseY + h, doorCenterZ + offsetZ);
                        if (doorMap.containsKey(loc)) {
                            alreadyExists = true;
                            break;
                        }
                    }
                    if (alreadyExists) break;
                }
                if (alreadyExists) return;

                // 创建门方块（铁块）
                for (int w = 0; w < DOOR_WIDTH; w++) {
                    int offsetX = widthDir.getModX() * (widthStart + w);
                    int offsetZ = widthDir.getModZ() * (widthStart + w);
                    for (int h = 0; h < DOOR_HEIGHT; h++) {
                        int blockX = doorCenterX + offsetX;
                        int blockZ = doorCenterZ + offsetZ;
                        int blockY = doorBaseY + h;
                        Location loc = new Location(world, blockX, blockY, blockZ);
                        Block target = world.getBlockAt(loc);

                        // 移除原有方块（若存在）
                        if (target.getType() != Material.AIR) {
                            placedBlocks.remove(target);
                        }

                        // 设置门方块
                        target.setType(Material.IRON_BLOCK);
                        placedBlocks.add(target);
                        doorBlocks.add(loc);

                        // 记录门中心方块
                        if (h == 0 && w == DOOR_WIDTH / 2) primaryBlock = loc;
                    }
                }
                if (primaryBlock == null) return;

                // 注册门到映射表
                Door door = new Door(doorFacing, primaryBlock, doorBlocks, gridX, gridZ, neighborX, neighborZ);
                for (Location loc : doorBlocks) doorMap.put(loc, door);
            }

            /**
             * 放置房间特色内容（根据房间类型）
             */
            private void placeRoomContents() {
                int floorY = centerY - 1;
                Location center = new Location(world, worldX + 0.5, centerY, worldZ + 0.5);

                // 根据房间类型生成内容
                switch (type) {
                    case ENEMY -> spawnEnemies(center);       // 怪物房间
                    case TRAP -> placeTrap(worldX, worldZ, floorY); // 陷阱房间
                    case TREASURE -> placeTreasureChest(worldX, worldZ, floorY); // 宝藏房间
                    case BUFF -> applyBuffToPlayer(player, center); // 增益房间
                    case ALTAR -> placeAltar(worldX, worldZ, floorY); // 祭坛房间
                    default -> {} // 空房间/起点/Boss房间无额外内容
                }

                // 房间中心红石火把标记
                Block torch = world.getBlockAt(worldX, centerY, worldZ);
                torch.setType(Material.REDSTONE_TORCH);
                placedBlocks.add(torch);
            }

            /**
             * 生成怪物（怪物房间）
             */
            private void spawnEnemies(Location center) {
                int count = random.nextInt(3) + 1; // 1-3只怪物
                for (int i = 0; i < count; i++) {
                    // 随机怪物类型：僵尸/骷髅/蜘蛛
                    EntityType type = switch (random.nextInt(3)) {
                        case 0 -> EntityType.ZOMBIE;
                        case 1 -> EntityType.SKELETON;
                        default -> EntityType.SPIDER;
                    };

                    // 生成怪物
                    Entity e = world.spawnEntity(center.clone().add(random.nextDouble() - 0.5, 0, random.nextDouble() - 0.5), type);
                    if (e instanceof Monster m) m.setRemoveWhenFarAway(false);
                    spawnedEntities.add(e);
                }

                // 播放特效
                world.playSound(center, Sound.ENTITY_ZOMBIE_AMBIENT, 1.0f, 0.8f);
                world.spawnParticle(Particle.ENTITY_EFFECT, center, 15, 0.5, 0.5, 0.5, 0.2, Color.ORANGE);
            }

            /**
             * 放置陷阱（陷阱房间：岩浆块）
             */
            private void placeTrap(int roomX, int roomZ, int floorY) {
                int half = ROOM_SIZE / 2;
                for (int dx = -half + 1; dx <= half - 1; dx++) {
                    for (int dz = -half + 1; dz <= half - 1; dz++) {
                        if (random.nextDouble() < 0.3) { // 30%概率生成岩浆块
                            Block b = world.getBlockAt(roomX + dx, floorY, roomZ + dz);
                            b.setType(Material.MAGMA_BLOCK);
                            placedBlocks.add(b);
                        }
                    }
                }

                // 播放特效
                Location trapCenter = new Location(world, roomX + 0.5, floorY + 0.5, roomZ + 0.5);
                world.playSound(trapCenter, Sound.BLOCK_LAVA_AMBIENT, 1.0f, 1.0f);
                world.spawnParticle(Particle.LAVA, trapCenter, 10, 0.5, 0, 0.5, 0.1);
            }

            /**
             * 放置宝藏箱（宝藏房间）
             */
            private void placeTreasureChest(int roomX, int roomZ, int floorY) {
                Block chestBlock = world.getBlockAt(roomX, floorY, roomZ);
                chestBlock.setType(Material.CHEST);
                placedBlocks.add(chestBlock);

                // 填充宝箱奖励
                if (chestBlock.getState() instanceof Chest chest) {
                    chest.getInventory().addItem(generateReward());
                }

                // 播放特效
                Location chestLoc = chestBlock.getLocation().add(0.5, 0.5, 0.5);
                world.playSound(chestLoc, Sound.BLOCK_ANVIL_PLACE, 1.0f, 1.2f);
                world.spawnParticle(Particle.HEART, chestLoc, 10, 0.5, 0.5, 0.5, 0.1);
            }

            /**
             * 生成宝箱奖励
             */
            private ItemStack generateReward() {
                int r = random.nextInt(10);
                if (r < 3) return new ItemStack(Material.IRON_INGOT, random.nextInt(5) + 1);  // 铁锭（1-5）
                if (r < 6) return new ItemStack(Material.GOLD_INGOT, random.nextInt(3) + 1);  // 金锭（1-3）
                if (r < 9) return new ItemStack(Material.DIAMOND, 1);                        // 钻石（1）
                return new ItemStack(Material.EMERALD, random.nextInt(3) + 1);               // 绿宝石（1-3）
            }

            /**
             * 给玩家添加增益（增益房间）
             */
            private void applyBuffToPlayer(Player target, Location center) {
                target.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 30 * 20, 1));    // 速度II（30秒）
                target.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 30 * 20, 0)); // 力量I（30秒）
                target.sendMessage(ChatColor.LIGHT_PURPLE + "你进入了一个祝福房间！获得速度 II 和力量 I (30秒)");

                // 播放特效
                world.playSound(center, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                world.spawnParticle(Particle.HAPPY_VILLAGER, center, 30, 0.5, 0.5, 0.5, 0.1);
            }

            /**
             * 放置祭坛（祭坛房间）
             */
            private void placeAltar(int roomX, int roomZ, int floorY) {
                Block altar = world.getBlockAt(roomX, floorY, roomZ);
                altar.setType(Material.GOLD_BLOCK);
                placedBlocks.add(altar);
                altars.add(altar.getLocation());

                // 提示玩家
                player.sendMessage(ChatColor.GOLD + "你发现了一个祭坛！右键金块以获得奖励（或惩罚）");

                // 播放特效
                world.playSound(altar.getLocation().add(0.5, 0.5, 0.5), Sound.BLOCK_ANVIL_LAND, 0.8f, 1.2f);
            }

            /**
             * 清理房间（移除方块和实体）
             */
            void cleanup() {
                // 移除实体
                for (Entity e : spawnedEntities) {
                    if (e != null && !e.isDead()) e.remove();
                }
                spawnedEntities.clear();

                // 移除方块
                for (Block b : placedBlocks) {
                    if (b.getType() != Material.AIR) b.setType(Material.AIR);
                }
                placedBlocks.clear();
            }
        }

        // ========== DungeonGenerator 核心方法 ==========
        /**
         * 构造地牢生成器
         */
        public DungeonGenerator(JavaPlugin plugin, Player player) {
            this.plugin = plugin;
            this.player = player;
            this.world = player.getWorld();
            Location loc = player.getLocation();
            this.centerX = loc.getBlockX();
            this.centerY = loc.getBlockY(); // 玩家脚部高度
            this.centerZ = loc.getBlockZ();

            // 生成地牢布局（房间网格）
            generateLayout();

            // 初始化噪声生成器（用于随机材质）
            long seed = world.getSeed() + centerX * 31L + centerZ * 71L;
            noiseFloor = new SimplexNoiseGenerator((int) seed);
            noiseWall = new SimplexNoiseGenerator((int) (seed + 12345));
        }

        /**
         * 生成地牢布局（随机游走 + 分支）
         */
        private void generateLayout() {
            layout = new boolean[GRID_SIZE][GRID_SIZE];
            rooms = new Room[GRID_SIZE][GRID_SIZE];

            // 1. 随机游走生成主路径（从起点到Boss房间）
            boolean[][] visited = new boolean[GRID_SIZE][GRID_SIZE];
            int curX = 0, curZ = 0; // 起点网格坐标
            visited[curX][curZ] = true;
            int steps = 0;
            int maxSteps = 200;
            Random r = new Random();

            // 游走直到到达Boss房间（右下角）或步数耗尽
            while ((curX != GRID_SIZE - 1 || curZ != GRID_SIZE - 1) && steps < maxSteps) {
                List<int[]> directions = Arrays.asList(
                        new int[]{0, 1}, new int[]{0, -1},
                        new int[]{1, 0}, new int[]{-1, 0}
                );
                Collections.shuffle(directions, r);
                boolean moved = false;

                // 尝试向随机方向移动
                for (int[] dir : directions) {
                    int nx = curX + dir[0];
                    int nz = curZ + dir[1];
                    if (nx >= 0 && nx < GRID_SIZE && nz >= 0 && nz < GRID_SIZE) {
                        curX = nx;
                        curZ = nz;
                        visited[curX][curZ] = true;
                        moved = true;
                        break;
                    }
                }
                if (!moved) break;
                steps++;
            }
            visited[GRID_SIZE - 1][GRID_SIZE - 1] = true; // 强制标记Boss房间为存在

            // 2. 随机添加分支房间
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

            // 3. 复制布局到最终数组
            for (int i = 0; i < GRID_SIZE; i++) {
                System.arraycopy(visited[i], 0, layout[i], 0, GRID_SIZE);
            }

            // 4. 分配房间类型
            for (int i = 0; i < GRID_SIZE; i++) {
                for (int j = 0; j < GRID_SIZE; j++) {
                    if (!layout[i][j]) continue;

                    RoomType type;
                    if (i == 0 && j == 0) {
                        type = RoomType.EMPTY; // 起点房间为空
                    } else if (i == GRID_SIZE - 1 && j == GRID_SIZE - 1) {
                        type = RoomType.BOSS;  // 右下角为Boss房间
                    } else {
                        type = selectRandomRoomType(); // 随机分配房间类型
                    }
                    rooms[i][j] = new Room(i, j, type);
                }
            }
        }

        /**
         * 随机选择房间类型（按概率）
         */
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

        /**
         * 获取地板随机材质（基于噪声）
         */
        private Material getFloorMaterial(int worldX, int worldZ) {
            double noise = noiseFloor.noise(worldX * 0.1, worldZ * 0.1, 0.0);
            int index = (int) Math.floor((noise + 1) / 2 * FLOOR_MATERIALS.length);
            index = Math.max(0, Math.min(index, FLOOR_MATERIALS.length - 1));
            return FLOOR_MATERIALS[index];
        }

        /**
         * 获取墙壁随机材质（基于噪声）
         */
        private Material getWallMaterial(int worldX, int worldY, int worldZ) {
            double noise = noiseWall.noise(worldX * 0.1, worldY * 0.05, worldZ * 0.1);
            int index = (int) Math.floor((noise + 1) / 2 * WALL_MATERIALS.length);
            index = Math.max(0, Math.min(index, WALL_MATERIALS.length - 1));
            return WALL_MATERIALS[index];
        }

        /**
         * 获取路径材质（固定金块）
         */
        private Material getPathMaterial() {
            return Material.GOLD_BLOCK;
        }

        /**
         * 获取门宽度方向（垂直于门朝向）
         */
        private BlockFace getPerpendicular(BlockFace face) {
            return switch (face) {
                case NORTH, SOUTH -> BlockFace.EAST;
                case EAST, WEST -> BlockFace.NORTH;
                default -> BlockFace.NORTH;
            };
        }

        /**
         * 检查地牢区域是否被阻挡（有非空/非液体方块）
         */
        public String checkBlocked() {
            int floorY = centerY - 1;
            for (int i = 0; i < GRID_SIZE; i++) {
                for (int j = 0; j < GRID_SIZE; j++) {
                    if (!layout[i][j]) continue;

                    int worldX = centerX + i * ROOM_SIZE;
                    int worldZ = centerZ + j * ROOM_SIZE;
                    int half = ROOM_SIZE / 2;

                    // 检查房间周围扩展区域
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

        /**
         * 生成起点房间
         */
        public void generateStartRoom() {
            int floorY = centerY - 1;
            startChestBlock = world.getBlockAt(centerX, floorY, centerZ);
            startChestBlock.setType(Material.BEDROCK); // 起点标记（基岩）

            // 生成起点房间
            Room startRoom = rooms[0][0];
            startRoom.generate();

            // 提示玩家
            player.sendMessage(ChatColor.GREEN + "起点房间已生成！右键铁门探索新区域。");
            Location startLoc = new Location(world, centerX + 0.5, centerY, centerZ + 0.5);
            world.playSound(startLoc, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            world.spawnParticle(Particle.HAPPY_VILLAGER, startLoc, 30, 1, 1, 1, 0.1);
        }

        /**
         * 从门扩展地牢（生成邻居房间）
         */
        public void expandFromDoor(Block clickedBlock, Player clicker) {
            Door door = doorMap.get(clickedBlock.getLocation());
            if (door == null) return;

            // 获取邻居房间坐标
            int neighborX = door.toX, neighborZ = door.toZ;
            if (neighborX < 0 || neighborX >= GRID_SIZE || neighborZ < 0 || neighborZ >= GRID_SIZE) {
                clicker.sendMessage(ChatColor.RED + "这个方向没有房间。");
                return;
            }
            if (!layout[neighborX][neighborZ]) {
                clicker.sendMessage(ChatColor.RED + "这个方向没有房间。");
                return;
            }

            // 检查邻居房间是否已生成
            Room neighborRoom = rooms[neighborX][neighborZ];
            if (neighborRoom.generated) {
                clicker.sendMessage(ChatColor.RED + "这个方向已经探索过了！");
                return;
            }

            // 移除门方块
            for (Location loc : door.blocks) {
                Block b = world.getBlockAt(loc);
                if (b.getType() == Material.IRON_BLOCK) {
                    b.setType(Material.AIR);
                }
                doorMap.remove(loc);
            }

            // 播放开门特效
            Location doorCenter = door.blocks.get(0).clone().add(0.5, 0.5, 0.5);
            world.playSound(doorCenter, Sound.BLOCK_IRON_DOOR_OPEN, 1.0f, 1.0f);
            world.spawnParticle(Particle.CLOUD, doorCenter, 20, 0.5, 0.5, 0.5, 0.1);

            // 生成邻居房间
            neighborRoom.generate();
            clicker.sendMessage(ChatColor.GREEN + "铁门消失，新的区域出现了！");
        }

        /**
         * 处理祭坛交互（奖励/惩罚）
         */
        public void onAltarInteract(Player clicker, Block altarBlock) {
            Location loc = altarBlock.getLocation();
            if (!altars.contains(loc)) return;
            altars.remove(loc);

            // 查找祭坛所属房间
            Room targetRoom = null;
            for (int i = 0; i < GRID_SIZE; i++) {
                for (int j = 0; j < GRID_SIZE; j++) {
                    if (rooms[i][j] != null && rooms[i][j].placedBlocks.contains(altarBlock)) {
                        targetRoom = rooms[i][j];
                        break;
                    }
                }
            }

            // 随机触发奖励/惩罚
            int r = random.nextInt(10);
            if (r < 4) { // 40%概率：增益效果
                PotionEffectType type = switch (random.nextInt(3)) {
                    case 0 -> PotionEffectType.SPEED;
                    case 1 -> PotionEffectType.STRENGTH;
                    default -> PotionEffectType.REGENERATION;
                };
                clicker.addPotionEffect(new PotionEffect(type, 30 * 20, 1));
                clicker.sendMessage(ChatColor.AQUA + "祭坛赐予你 " + type.getName() + " 效果！");
                world.playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                world.spawnParticle(Particle.HAPPY_VILLAGER, loc.add(0.5, 0.5, 0.5), 20, 0.5, 0.5, 0.5, 0.1);
            } else if (r < 7) { // 30%概率：钻石奖励
                ItemStack reward = new ItemStack(Material.DIAMOND, random.nextInt(2) + 1);
                clicker.getInventory().addItem(reward);
                clicker.sendMessage(ChatColor.YELLOW + "祭坛给了你 " + reward.getAmount() + " 颗钻石！");
                world.playSound(loc, Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.0f);
                world.spawnParticle(Particle.HEART, loc.add(0.5, 0.5, 0.5), 15, 0.5, 0.5, 0.5, 0.1);
            } else { // 30%概率：陷阱（生成僵尸）
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

            // 移除祭坛方块
            altarBlock.setType(Material.AIR);
            if (targetRoom != null) targetRoom.placedBlocks.remove(altarBlock);
        }

        /**
         * 在Boss房间生成Boss
         */
        private void spawnBossAt(Room bossRoom) {
            Location loc = new Location(world, bossRoom.worldX + 0.5, centerY, bossRoom.worldZ + 0.5);
            boss = (Zombie) world.spawnEntity(loc, EntityType.ZOMBIE);

            // 设置Boss属性
            boss.setCustomName(ChatColor.RED + "地牢守卫");
            boss.setCustomNameVisible(true);
            boss.setMaxHealth(80.0);
            boss.setHealth(80.0);
            boss.getEquipment().setItemInMainHand(new ItemStack(Material.IRON_SWORD));
            boss.getEquipment().setHelmet(new ItemStack(Material.IRON_HELMET));
            boss.getEquipment().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
            bossRoom.spawnedEntities.add(boss);

            // 播放Boss生成特效
            world.playSound(loc, Sound.ENTITY_WITHER_SPAWN, 1.5f, 0.8f);
            world.spawnParticle(Particle.EXPLOSION, loc, 5, 0, 0, 0, 0);
            world.spawnParticle(Particle.LARGE_SMOKE, loc, 20, 1, 1, 1, 0.2);
            world.spawnParticle(Particle.FLAME, loc, 30, 1, 1, 1, 0.1);
        }

        /**
         * 地牢通关处理（奖励 + 清理）
         */
        public void complete(Player killer) {
            int floorY = centerY - 1;

            // 生成通关奖励箱（起点位置）
            startChestBlock.setType(Material.CHEST);
            if (startChestBlock.getState() instanceof Chest chest) {
                chest.getInventory().addItem(new ItemStack(Material.DIAMOND, 5));
            }

            // 提示玩家
            killer.sendMessage(ChatColor.GOLD + "恭喜通关！获得 5 颗钻石！");

            // 播放通关特效
            Location completeLoc = startChestBlock.getLocation().add(0.5, 1, 0.5);
            world.playSound(completeLoc, Sound.ENTITY_ENDER_DRAGON_DEATH, 1.0f, 1.0f);
            world.spawnParticle(Particle.FIREWORK, completeLoc, 50, 2, 2, 2, 0.2);
            world.spawnParticle(Particle.HAPPY_VILLAGER, completeLoc, 30, 1, 1, 1, 0.1);

            // 延迟1秒清理地牢
            plugin.getServer().getScheduler().runTaskLater(plugin, this::cleanup, 20L);
        }

        /**
         * 清理整个地牢
         */
        public void cleanup() {
            // 清理所有房间
            for (int i = 0; i < GRID_SIZE; i++) {
                for (int j = 0; j < GRID_SIZE; j++) {
                    if (rooms[i][j] != null) rooms[i][j].cleanup();
                }
            }

            // 清理映射表
            doorMap.clear();
            altars.clear();

            // 提示玩家
            if (player.isOnline()) player.sendMessage(ChatColor.GRAY + "地牢已清理。");
        }

        // ========== 工具方法 ==========
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