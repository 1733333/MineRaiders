package OtherStuff;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.noise.SimplexNoiseGenerator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import java.util.*;
/**
 * 地牢冒险插件主类
 * 无尽地牢模式：击败Boss进入下一层，玩家死亡结算分数
 * 重构说明：使用BSP二叉空间分割算法重构地牢生成机制，支持随机大小房间、自然随机布局，特殊房间添加专属材质区分
 */
public class RoguelikePlugin implements CommandExecutor, Listener {
    private final JavaPlugin plugin;
    private DungeonManager activeDungeon; // 当前激活的地牢实例
    // 无尽模式状态变量
    private int currentFloor = 1;          // 当前地牢层数
    private double difficultyFactor = 1.0; // 当前难度系数
    private int playerScore = 0;           // 玩家当前分数
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
            if (activeDungeon != null && !activeDungeon.isCompleted()) {
                player.sendMessage(ChatColor.RED + "A dungeon is already active. Finish or clean it first.");
                return true;
            }
            // 重置无尽模式状态，新游戏从第一层开始
            currentFloor = 1;
            difficultyFactor = 1.0;
            playerScore = 0;
            // 初始化玩家状态：生存模式 + 基础装备
            player.setGameMode(GameMode.SURVIVAL);
            player.getInventory().clear(); // 清空背包，避免干扰
            ItemStack ironSword = new ItemStack(Material.IRON_SWORD);
            ItemStack steak = new ItemStack(Material.COOKED_BEEF, 16);
            ItemStack healthPotion = new ItemStack(Material.POTION, 1);
            // 给初始治疗药水
            PotionMeta potionMeta = (PotionMeta) healthPotion.getItemMeta();
            potionMeta.addCustomEffect(new PotionEffect(PotionEffectType.INSTANT_HEALTH, 1, 1), true);
            potionMeta.setColor(Color.RED);
            healthPotion.setItemMeta(potionMeta);
            HashMap<Integer, ItemStack> remaining = player.getInventory().addItem(ironSword, steak, healthPotion);
            for (ItemStack leftover : remaining.values()) {
                player.getWorld().dropItem(player.getLocation(), leftover);
            }
            player.sendMessage(ChatColor.GREEN + "你已进入无尽地牢模式！");
            player.sendMessage(ChatColor.GRAY + "第1层，难度系数: 1.0，击败Boss进入下一层，死亡则结算分数！");
            // 生成地牢
            DungeonManager generator = new DungeonManager(plugin, player, difficultyFactor, currentFloor);
            String error = generator.checkBlocked();
            if (error != null) {
                player.sendMessage(ChatColor.RED + "Cannot generate dungeon: " + error);
                return true;
            }
            generator.generateStartRoom();
            activeDungeon = generator;
            return true;
        }
        // 处理clean命令 - 清理地牢
        else if (args.length > 0 && args[0].equalsIgnoreCase("clean")) {
            if (activeDungeon != null) {
                activeDungeon.cleanup();
                activeDungeon = null;
                // 重置状态
                currentFloor = 1;
                difficultyFactor = 1.0;
                playerScore = 0;
                player.sendMessage(ChatColor.GREEN + "地牢已清理完成，游戏已重置！");
            } else {
                player.sendMessage(ChatColor.RED + "没有激活的地牢可以清理。");
            }
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
        if (block == null || activeDungeon == null || activeDungeon.isCompleted()) return;
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
     * 处理怪物死亡，统计分数
     */
    @EventHandler
    public void onEntityKill(EntityDeathEvent event) {
        if (activeDungeon == null) return;
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;
        // 根据怪物类型加分
        Entity entity = event.getEntity();
        if (entity.equals(activeDungeon.getBoss())) {
            // Boss加分
            playerScore += 200 * currentFloor;
        } else if (entity instanceof Monster monster) {
            // 检查是否是精英怪
            boolean isElite = monster.getAttribute(Attribute.MAX_HEALTH).getValue() > 30;
            if (isElite) {
                playerScore += 50;
            } else {
                playerScore += 10;
            }
        }
    }
    /**
     * 处理Boss死亡事件
     */
    @EventHandler
    public void onBossDeath(EntityDeathEvent event) {
        if (activeDungeon == null || activeDungeon.isCompleted()) return;
        if (event.getEntity().equals(activeDungeon.getBoss())) {
            Player killer = event.getEntity().getKiller();
            if (killer != null) {
                // 完成当前层，清理当前地牢
                activeDungeon.complete(killer);
                // 层数+1，难度提升
                currentFloor++;
                difficultyFactor += 0.5;
                // 通关当前层加分
                playerScore += 100 * currentFloor;
                killer.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "🎉 你击败了Boss！进入地牢第" + currentFloor + "层！");
                killer.sendMessage(ChatColor.GRAY + "新的难度系数: " + String.format("%.1f", difficultyFactor));
                // 生成新的一层地牢
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    try {
                        DungeonManager newDungeon = new DungeonManager(plugin, killer, difficultyFactor, currentFloor);
                        String error = newDungeon.checkBlocked();
                        if (error != null) {
                            killer.sendMessage(ChatColor.RED + "无法生成下一层: " + error);
                            return;
                        }
                        newDungeon.generateStartRoom();
                        activeDungeon = newDungeon;
                    } catch (Exception e) {
                        killer.sendMessage(ChatColor.RED + "生成下一层时出错: " + e.getMessage());
                    }
                }, 20L); // 延迟1秒生成，让玩家看完提示
            }
        }
    }
    /**
     * 处理玩家死亡，结算分数
     */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (activeDungeon == null) return;
        // 结算分数
        int finalScore = playerScore;
        int finalFloor = currentFloor;
        // 发送结算信息
        player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "💀 你在地牢中死亡了！");
        player.sendMessage(ChatColor.GOLD + "=== 最终结算 ===");
        player.sendMessage(ChatColor.YELLOW + "通关层数: " + finalFloor);
        player.sendMessage(ChatColor.YELLOW + "最终得分: " + finalScore);
        player.sendMessage(ChatColor.GRAY + "输入/roguelike start 来重新开始挑战！");
        // 清理地牢
        activeDungeon.cleanup();
        activeDungeon = null;
        // 重置状态
        currentFloor = 1;
        difficultyFactor = 1.0;
        playerScore = 0;
        // 播放死亡特效
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_DEATH, 1.0f, 1.0f);
    }
    /**
     * 插件禁用时清理地牢
     */
    public void onDisable() {
        if (activeDungeon != null) {
            activeDungeon.cleanup();
        }
    }
    // ==================== 内部类：地牢核心管理类（重构后使用BSP算法） ====================
    private static class DungeonManager {
        // ========== 核心配置参数（可根据需求调整） ==========
        private static final int MIN_ROOM_SIZE = 8;              // 房间最小尺寸
        private static final int MAX_ROOM_SIZE = 15;             // 房间最大尺寸
        private static final int DOOR_WIDTH = 7;             // 门宽度
        private static final int DOOR_HEIGHT = 4;            // 门高度
        private static final int WALL_HEIGHT = 6;            // 墙壁高度
        private final int dungeonSize;                                  // 整个地牢的总尺寸
        // 动态概率，随难度调整
        private double ENEMY_CHANCE = 0.30;     // 普通怪物房间概率
        private double ELITE_ENEMY_CHANCE = 0.10;// 精英怪物房间概率
        private double TRAP_CHANCE = 0.15;      // 陷阱房间概率
        private double TREASURE_CHANCE = 0.20;  // 宝藏房间概率
        private double RARE_TREASURE_CHANCE = 0.05; // 稀有宝藏房间概率
        private double BUFF_CHANCE = 0.12;      // 增益房间概率
        private double ALTAR_CHANCE = 0.08;     // 祭坛房间概率
        // 分层材质池
        private static final Material[] FLOOR_MATERIALS_EARLY = {
                Material.STONE_BRICKS, Material.CRACKED_STONE_BRICKS,
                Material.MOSSY_STONE_BRICKS, Material.SMOOTH_STONE,
                Material.ANDESITE, Material.DIORITE, Material.GRANITE
        };
        private static final Material[] FLOOR_MATERIALS_MID = {
                Material.DEEPSLATE_BRICKS, Material.CRACKED_DEEPSLATE_BRICKS,
                Material.POLISHED_DEEPSLATE, Material.COBBLED_DEEPSLATE,
                Material.TUFF, Material.POLISHED_TUFF
        };
        private static final Material[] FLOOR_MATERIALS_LATE = {
                Material.NETHER_BRICKS, Material.CRACKED_NETHER_BRICKS,
                Material.CHISELED_NETHER_BRICKS, Material.BASALT,
                Material.POLISHED_BASALT, Material.SOUL_SOIL
        };
        private static final Material[] WALL_MATERIALS_EARLY = {
                Material.STONE_BRICKS, Material.MOSSY_STONE_BRICKS,
                Material.CHISELED_STONE_BRICKS, Material.POLISHED_ANDESITE,
                Material.POLISHED_DIORITE, Material.POLISHED_GRANITE
        };
        private static final Material[] WALL_MATERIALS_MID = {
                Material.POLISHED_DEEPSLATE, Material.DEEPSLATE_TILES,
                Material.DEEPSLATE_BRICKS, Material.CHISELED_DEEPSLATE
        };
        private static final Material[] WALL_MATERIALS_LATE = {
                Material.NETHER_BRICKS, Material.RED_NETHER_BRICKS,
                Material.CHISELED_NETHER_BRICKS, Material.BASALT,
                Material.SMOOTH_BASALT
        };
        // ========== 核心成员变量 ==========
        private final JavaPlugin plugin;
        private final Player player;
        private final World world;
        private final int centerX, centerY, centerZ; // 地牢中心坐标（玩家初始位置）
        private final Random random = new Random();
        private final SimplexNoiseGenerator noiseFloor; // 地板材质噪声生成器
        private final SimplexNoiseGenerator noiseWall;  // 墙壁材质噪声生成器
        private final double difficultyFactor; // 当前难度系数
        private final int currentFloor; // 当前层数
        private List<Room> rooms;                     // 房间实例列表
        private final Map<Location, Door> doorMap = new HashMap<>(); // 门方块映射
        private final Set<Location> altars = new HashSet<>();        // 祭坛位置集合
        private Monster boss;          // Boss实体
        private boolean bossSpawned = false; // Boss是否已生成
        private boolean isCompleted = false; // 地牢是否已完成
        private Room startRoom; // 起点房间
        // ========== 内部枚举：房间类型 ==========
        public enum RoomType {
            EMPTY,        // 空房间（起点）
            ENEMY,       // 普通怪物房间
            ELITE_ENEMY, // 精英怪物房间
            TRAP,        // 陷阱房间
            TREASURE,    // 普通宝藏房间
            RARE_TREASURE,// 稀有宝藏房间
            BUFF,        // 增益房间
            ALTAR,       // 祭坛房间
            BOSS         // Boss房间
        }
        // ========== 内部类：门 ==========
        private static class Door {
            BlockFace facing;          // 门朝向（指向邻居房间）
            Location primaryBlock;     // 门中心底部方块
            List<Location> blocks;     // 门所有方块位置
            Room fromRoom;          // 源房间
            Room toRoom;              // 目标房间
            Door(BlockFace facing, Location primaryBlock, List<Location> blocks, Room fromRoom, Room toRoom) {
                this.facing = facing;
                this.primaryBlock = primaryBlock;
                this.blocks = blocks;
                this.fromRoom = fromRoom;
                this.toRoom = toRoom;
            }
        }
        // ========== 内部类：BSP分割节点 ==========
        private class BSPNode {
            int x, z, width, height;
            BSPNode left, right;
            Room room;

            public BSPNode(int x, int z, int width, int height) {
                this.x = x;
                this.z = z;
                this.width = width;
                this.height = height;
            }

            public boolean split() {
                if (left != null || right != null) return false;

                boolean splitHorizontal = random.nextBoolean();
                if (width > height && (double)width / height >= 1.25) {
                    splitHorizontal = false;
                } else if (height > width && (double)height / width >= 1.25) {
                    splitHorizontal = true;
                }

                int max = splitHorizontal ? height : width;
                if (max <= MIN_ROOM_SIZE * 2) return false;

                int split = random.nextInt(max - MIN_ROOM_SIZE * 2) + MIN_ROOM_SIZE;

                if (splitHorizontal) {
                    left = new BSPNode(x, z, width, split);
                    right = new BSPNode(x, z + split, width, height - split);
                } else {
                    left = new BSPNode(x, z, split, height);
                    right = new BSPNode(x + split, z, width - split, height);
                }
                return true;
            }

            public void createRooms() {
                if (left != null || right != null) {
                    if (left != null) left.createRooms();
                    if (right != null) right.createRooms();
                    if (left != null && right != null) {
                        connectNodes(left, right);
                    }
                } else {
                    int roomW = random.nextInt(Math.min(width - 2, MAX_ROOM_SIZE) - MIN_ROOM_SIZE) + MIN_ROOM_SIZE;
                    int roomH = random.nextInt(Math.min(height - 2, MAX_ROOM_SIZE) - MIN_ROOM_SIZE) + MIN_ROOM_SIZE;
                    int roomX = x + random.nextInt(width - roomW - 1) + 1;
                    int roomZ = z + random.nextInt(height - roomH - 1) + 1;

                    int worldRoomX = centerX + roomX - dungeonSize / 2;
                    int worldRoomZ = centerZ + roomZ - dungeonSize / 2;

                    Room room = new Room(worldRoomX + roomW / 2, worldRoomZ + roomH / 2, roomW, roomH, RoomType.EMPTY, 0);
                    this.room = room;
                    rooms.add(room);
                }
            }

            private void connectNodes(BSPNode left, BSPNode right) {
                Room r1 = left.getRoom();
                Room r2 = right.getRoom();
                if (r1 == null || r2 == null) return;

                BlockFace face;
                int doorX, doorZ;
                if (left.x == right.x) {
                    face = BlockFace.NORTH;
                    doorX = r1.worldX;
                    doorZ = r1.worldZ + r1.height/2 + 1;
                } else {
                    face = BlockFace.EAST;
                    doorX = r1.worldX + r1.width/2 + 1;
                    doorZ = r1.worldZ;
                }

                createDoorBetweenRooms(r1, r2, face, doorX, doorZ);
            }

            public Room getRoom() {
                if (left != null || right != null) {
                    Room r = left != null ? left.getRoom() : null;
                    if (r == null) r = right.getRoom();
                    return r;
                } else {
                    return room;
                }
            }
        }
        // ========== 内部类：房间 ==========
        private class Room {
            final int worldX, worldZ;  // 房间世界坐标（中心）
            final int width, height;    // 房间宽高
            RoomType type;       // 房间类型
            boolean generated;         // 是否已生成
            final List<Block> placedBlocks = new ArrayList<>(); // 房间生成的方块
            final List<Entity> spawnedEntities = new ArrayList<>(); // 房间生成的实体
            int distanceFromStart; // 距离起点的曼哈顿距离，用于难度缩放
            final Map<BlockFace, Door> doors = new HashMap<>(); // 房间的门

            Room(int worldX, int worldZ, int width, int height, RoomType type, int distFromStart) {
                this.worldX = worldX;
                this.worldZ = worldZ;
                this.width = width;
                this.height = height;
                this.type = type;
                this.generated = false;
                this.distanceFromStart = distFromStart;
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
                // 特殊房间处理
                if (type == RoomType.BOSS && !bossSpawned) {
                    spawnBossAt(this);
                    bossSpawned = true;
                    player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "⚠ 你进入了Boss房间！强大的地牢守护者出现了！");
                    player.playSound(player.getLocation(), Sound.ENTITY_WITHER_AMBIENT, 1.0f, 0.5f);
                } else if (type == RoomType.ELITE_ENEMY) {
                    player.sendMessage(ChatColor.DARK_PURPLE + "你感受到了强大的气息...这里有精英怪物！");
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
                int halfX = width / 2;
                int halfZ = height / 2;

                // 根据房间类型选择专属材质
                Material[] floorMaterials;
                Material[] wallMaterials;
                if (type == RoomType.ENEMY || type == RoomType.EMPTY) {
                    // 普通房间使用原有的分层通用材质
                    floorMaterials = getFloorMaterialsForFloor();
                    wallMaterials = getWallMaterialsForFloor();
                } else {
                    // 特殊房间使用专属材质组
                    switch (type) {
                        case ELITE_ENEMY:
                            floorMaterials = new Material[]{Material.POLISHED_DEEPSLATE, Material.DEEPSLATE_BRICKS, Material.CRACKED_DEEPSLATE_BRICKS};
                            wallMaterials = new Material[]{Material.PURPUR_BLOCK, Material.PURPUR_PILLAR};
                            break;
                        case TRAP:
                            floorMaterials = new Material[]{Material.RED_SANDSTONE, Material.CUT_RED_SANDSTONE, Material.SMOOTH_RED_SANDSTONE};
                            wallMaterials = new Material[]{Material.RED_NETHER_BRICKS, Material.NETHER_BRICKS};
                            break;
                        case TREASURE:
                            floorMaterials = new Material[]{Material.QUARTZ_BLOCK, Material.SMOOTH_QUARTZ, Material.QUARTZ_BRICKS};
                            wallMaterials = new Material[]{Material.POLISHED_ANDESITE, Material.POLISHED_DIORITE, Material.POLISHED_GRANITE};
                            break;
                        case RARE_TREASURE:
                            floorMaterials = new Material[] {Material.GOLD_BLOCK,Material.GOLD_ORE,Material.GILDED_BLACKSTONE};
                            wallMaterials = new Material[]{Material.PRISMARINE, Material.DARK_PRISMARINE, Material.PRISMARINE_BRICKS};
                            break;
                        case BUFF:
                            floorMaterials = new Material[]{Material.CHERRY_PLANKS, Material.SMOOTH_STONE, Material.PINK_CONCRETE};
                            wallMaterials = new Material[]{Material.PINK_CONCRETE, Material.WHITE_CONCRETE, Material.CHERRY_WOOD};
                            break;
                        case ALTAR:
                            floorMaterials = new Material[]{Material.POLISHED_BASALT, Material.BASALT, Material.SMOOTH_BASALT};
                            wallMaterials = new Material[]{Material.BLACKSTONE, Material.POLISHED_BLACKSTONE};
                            break;
                        case BOSS:
                            floorMaterials = new Material[]{Material.SOUL_SOIL, Material.SOUL_SAND, Material.CRIMSON_NYLIUM};
                            wallMaterials = new Material[]{Material.CRIMSON_STEM, Material.CRIMSON_PLANKS, Material.NETHER_BRICKS};
                            break;
                        default:
                            floorMaterials = getFloorMaterialsForFloor();
                            wallMaterials = getWallMaterialsForFloor();
                            break;
                    }
                }

                // 1. 生成地板（随机材质，根据房间类型选择）
                for (int dx = -halfX; dx <= halfX; dx++) {
                    for (int dz = -halfZ; dz <= halfZ; dz++) {
                        Block floor = world.getBlockAt(worldX + dx, floorY, worldZ + dz);
                        double noise = noiseFloor.noise((worldX + dx) * 0.1, (worldZ + dz) * 0.1, 0.0);
                        int index = (int) Math.floor((noise + 1) / 2 * floorMaterials.length);
                        index = Math.max(0, Math.min(index, floorMaterials.length - 1));
                        floor.setType(floorMaterials[index]);
                        placedBlocks.add(floor);
                    }
                }
                // 2. 生成路径（根据房间类型选择专属路径材质）
                Material pathMat;
                switch (type) {
                    case ELITE_ENEMY:
                        pathMat = Material.PURPLE_CONCRETE;
                        break;
                    case TRAP:
                        pathMat = Material.RED_CONCRETE;
                        break;
                    case TREASURE:
                        pathMat = Material.YELLOW_CONCRETE;
                        break;
                    case RARE_TREASURE:
                        pathMat = Material.GOLD_BLOCK;
                        break;
                    case BUFF:
                        pathMat = Material.PINK_CONCRETE;
                        break;
                    case ALTAR:
                        pathMat = Material.BLACK_CONCRETE;
                        break;
                    case BOSS:
                        pathMat = Material.CRIMSON_HYPHAE;
                        break;
                    default:
                        pathMat = currentFloor <= 2 ? Material.GOLD_BLOCK :
                                currentFloor <=4 ? Material.QUARTZ_BLOCK : Material.NETHER_QUARTZ_ORE;
                        break;
                }
                for (Door door : doors.values()) {
                    if (door.facing == BlockFace.NORTH) {
                        for (int step = 1; step <= halfZ; step++) {
                            Block path = world.getBlockAt(worldX, floorY, worldZ + step);
                            path.setType(pathMat);
                            placedBlocks.add(path);
                        }
                    }
                    if (door.facing == BlockFace.SOUTH) {
                        for (int step = 1; step <= halfZ; step++) {
                            Block path = world.getBlockAt(worldX, floorY, worldZ - step);
                            path.setType(pathMat);
                            placedBlocks.add(path);
                        }
                    }
                    if (door.facing == BlockFace.EAST) {
                        for (int step = 1; step <= halfX; step++) {
                            Block path = world.getBlockAt(worldX + step, floorY, worldZ);
                            path.setType(pathMat);
                            placedBlocks.add(path);
                        }
                    }
                    if (door.facing == BlockFace.WEST) {
                        for (int step = 1; step <= halfX; step++) {
                            Block path = world.getBlockAt(worldX - step, floorY, worldZ);
                            path.setType(pathMat);
                            placedBlocks.add(path);
                        }
                    }
                }
                // 3. 生成墙壁（避开门区域）
                for (int dx = -halfX; dx <= halfX; dx++) {
                    for (int dz = -halfZ; dz <= halfZ; dz++) {
                        // 仅处理房间边缘
                        if (Math.abs(dx) != halfX && Math.abs(dz) != halfZ) continue;
                        boolean isNorth = (dz == halfZ);
                        boolean isSouth = (dz == -halfZ);
                        boolean isEast  = (dx == halfX);
                        boolean isWest  = (dx == -halfX);
                        // 跳过有门的方向（避免墙壁重叠）
                        if (isNorth && doors.containsKey(BlockFace.NORTH)) continue;
                        if (isSouth && doors.containsKey(BlockFace.SOUTH)) continue;
                        if (isEast  && doors.containsKey(BlockFace.EAST))  continue;
                        if (isWest  && doors.containsKey(BlockFace.WEST))  continue;
                        // 跳过门区域
                        boolean isDoorArea = false;
                        int doorWidth = Math.min(DOOR_WIDTH, width - 2);
                        if (isNorth && hasDoor(BlockFace.NORTH) && Math.abs(dx) <= doorWidth / 2) isDoorArea = true;
                        if (isSouth && hasDoor(BlockFace.SOUTH) && Math.abs(dx) <= doorWidth / 2) isDoorArea = true;
                        if (isEast  && hasDoor(BlockFace.EAST)  && Math.abs(dz) <= doorWidth / 2) isDoorArea = true;
                        if (isWest  && hasDoor(BlockFace.WEST)  && Math.abs(dz) <= doorWidth / 2) isDoorArea = true;
                        // 生成墙壁（随机材质）
                        for (int h = 0; h < WALL_HEIGHT; h++) {
                            if (isDoorArea && h < DOOR_HEIGHT) continue;
                            Block wall = world.getBlockAt(worldX + dx, floorY + 1 + h, worldZ + dz);
                            double noise = noiseWall.noise((worldX + dx) * 0.1, (floorY + 1 + h) * 0.05, (worldZ + dz) * 0.1);
                            int index = (int) Math.floor((noise + 1) / 2 * wallMaterials.length);
                            index = Math.max(0, Math.min(index, wallMaterials.length - 1));
                            wall.setType(wallMaterials[index]);
                            placedBlocks.add(wall);
                            // 随机在墙上插火把装饰
                            if (h == 2 && random.nextDouble() < 0.05) {
                                Block torch = world.getBlockAt(worldX + dx, floorY + 3 + h, worldZ + dz);
                                torch.setType(Material.WALL_TORCH);
                                placedBlocks.add(torch);
                            }
                        }
                    }
                }
            }
            /**
             * 检查房间是否有指定方向的门
             */
            private boolean hasDoor(BlockFace face) {
                return doors.containsKey(face);
            }
            /**
             * 创建房间所有方向的门
             */
            private void createAllDoors() {
                for (Door door : doors.values()) {
                    if (!door.fromRoom.equals(this)) continue;
                    createDoor(door);
                }
            }
            /**
             * 创建指定的门
             */
            private void createDoor(Door door) {
                // 计算门中心坐标
                int halfX = width / 2;
                int halfZ = height / 2;
                int doorCenterX, doorCenterZ;
                BlockFace doorFacing = door.facing;
                switch (doorFacing) {
                    case NORTH -> {
                        doorCenterX = worldX;
                        doorCenterZ = worldZ + halfZ;
                    }
                    case SOUTH -> {
                        doorCenterX = worldX;
                        doorCenterZ = worldZ - halfZ;
                    }
                    case EAST -> {
                        doorCenterX = worldX + halfX;
                        doorCenterZ = worldZ;
                    }
                    default -> { // WEST
                        doorCenterX = worldX - halfX;
                        doorCenterZ = worldZ;
                    }
                }
                // 门宽度方向（垂直于门朝向）
                BlockFace widthDir = getPerpendicular(doorFacing);
                int doorWidth = Math.min(DOOR_WIDTH, width - 2);
                int widthStart = -(doorWidth / 2);
                List<Location> doorBlocks = new ArrayList<>();
                Location primaryBlock = null;
                int doorBaseY = centerY;
                // 检查门是否已存在
                boolean alreadyExists = false;
                for (int w = 0; w < doorWidth; w++) {
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
                // 门的材质随层数变化
                Material doorMat = currentFloor <=2 ? Material.IRON_BLOCK :
                        currentFloor <=4 ? Material.DEEPSLATE_BRICKS : Material.NETHER_BRICKS;
                // 创建门方块
                for (int w = 0; w < doorWidth; w++) {
                    int offsetX = widthDir.getModX() * (widthStart + w);
                    int offsetZ = widthDir.getModZ() * (widthStart + w);
                    for (int h = 0; h < DOOR_HEIGHT; h++) {
                        int blockX = doorCenterX + offsetX;
                        int blockZ = doorCenterZ + offsetZ;
                        int blockY = doorBaseY + h;
                        Location loc = new Location(world, blockX, blockY, blockZ);
                        Block target = world.getBlockAt(loc);
                        // 移除原有方块
                        if (target.getType() != Material.AIR) {
                            placedBlocks.remove(target);
                        }
                        // 设置门方块
                        target.setType(doorMat);
                        placedBlocks.add(target);
                        doorBlocks.add(loc);
                        // 记录门中心方块
                        if (h == 0 && w == doorWidth / 2) primaryBlock = loc;
                    }
                }
                if (primaryBlock == null) return;
                // 注册门到映射表
                door.primaryBlock = primaryBlock;
                door.blocks = doorBlocks;
                for (Location loc : doorBlocks) doorMap.put(loc, door);
            }
            /**
             * 放置房间特色内容
             */
            private void placeRoomContents() {
                int floorY = centerY - 1;
                Location center = new Location(world, worldX + 0.5, centerY, worldZ + 0.5);
                // 根据房间类型生成内容
                switch (type) {
                    case ENEMY -> spawnEnemies(center, false);       // 普通怪物房间
                    case ELITE_ENEMY -> spawnEnemies(center, true);  // 精英怪物房间
                    case TRAP -> placeAdvancedTrap(worldX, worldZ, floorY); // 高级陷阱房间
                    case TREASURE -> placeTreasureChest(worldX, worldZ, floorY, false); // 普通宝藏
                    case RARE_TREASURE -> placeTreasureChest(worldX, worldZ, floorY, true); // 稀有宝藏
                    case BUFF -> applyAdvancedBuffToPlayer(player, center); // 高级增益房间
                    case ALTAR -> placeAltar(worldX, worldZ, floorY); // 祭坛房间
                    default -> {} // 空房间/Boss房间无额外内容
                }
                // 房间中心火把标记
                Block torch = world.getBlockAt(worldX, centerY, worldZ);
                torch.setType(type == RoomType.BOSS ? Material.SOUL_TORCH : Material.REDSTONE_TORCH);
                placedBlocks.add(torch);
            }
            /**
             * 生成怪物（支持普通/精英，带难度缩放）
             */
            private void spawnEnemies(Location center, boolean isElite) {
                int count = isElite ? random.nextInt(2) + 2 : random.nextInt(3) + 1;
                for (int i = 0; i < count; i++) {
                    // 随机怪物类型
                    EntityType type;
                    if (isElite) {
                        type = switch (random.nextInt(4)) {
                            case 0 -> EntityType.ZOMBIE;
                            case 1 -> EntityType.SKELETON;
                            case 2 -> EntityType.SPIDER;
                            default -> EntityType.EVOKER;
                        };
                    } else {
                        type = switch (random.nextInt(3)) {
                            case 0 -> EntityType.ZOMBIE;
                            case 1 -> EntityType.SKELETON;
                            default -> EntityType.SPIDER;
                        };
                    }
                    // 生成怪物
                    Entity e = world.spawnEntity(center.clone().add(random.nextDouble() - 0.5, 0, random.nextDouble() - 0.5), type);
                    if (e instanceof Monster monster) {
                        monster.setRemoveWhenFarAway(false);
                        // 根据房间距离给怪物加装备和属性（难度缩放，乘以全局难度系数）
                        int level = Math.min(distanceFromStart, 5);
                        applyMonsterEquipment(monster, level, isElite);
                        spawnedEntities.add(monster);
                    }
                }
                // 播放特效
                if (isElite) {
                    world.playSound(center, Sound.ENTITY_RAVAGER_AMBIENT, 1.0f, 0.8f);
                    world.spawnParticle(Particle.ENTITY_EFFECT, center, 30, 0.5, 0.5, 0.5, 0.2, Color.PURPLE);
                } else {
                    world.playSound(center, Sound.ENTITY_ZOMBIE_AMBIENT, 1.0f, 0.8f);
                    world.spawnParticle(Particle.ENTITY_EFFECT, center, 15, 0.5, 0.5, 0.5, 0.2, Color.ORANGE);
                }
            }
            /**
             * 给怪物应用装备和属性，根据等级和是否精英，以及全局难度系数
             */
            private void applyMonsterEquipment(Monster monster, int level, boolean isElite) {
                // 提升生命，乘以难度系数
                double health = (20.0 + level * 5.0) * difficultyFactor;
                if (isElite) health *= 1.5;
                monster.getAttribute(Attribute.MAX_HEALTH).setBaseValue(health);
                monster.setHealth(health);
                // 提升攻击伤害，乘以难度系数
                double damage = (3.0 + level * 1.0) * difficultyFactor;
                if (isElite) damage *= 1.3;
                monster.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(damage);
                // 精英怪物加药水效果
                if (isElite) {
                    monster.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 0));
                    monster.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 0));
                }
                // 给怪物加武器
                ItemStack weapon;
                if (level >= 4) {
                    weapon = new ItemStack(isElite ? Material.DIAMOND_SWORD : Material.IRON_SWORD);
                    if (isElite) {
                        weapon.addEnchantment(Enchantment.SHARPNESS, 2);
                        weapon.addEnchantment(Enchantment.FIRE_ASPECT, 1);
                    }
                } else if (level >= 2) {
                    weapon = new ItemStack(Material.IRON_SWORD);
                } else {
                    weapon = new ItemStack(Material.STONE_SWORD);
                }
                if (weapon != null) {
                    monster.getEquipment().setItemInMainHand(weapon);
                    monster.getEquipment().setItemInMainHandDropChance(0.1f);
                }
            }
            /**
             * 放置高级陷阱
             */
            private void placeAdvancedTrap(int roomX, int roomZ, int floorY) {
                int halfX = width / 2;
                int halfZ = height / 2;
                // 1. 岩浆块陷阱
                for (int dx = -halfX + 1; dx <= halfX - 1; dx++) {
                    for (int dz = -halfZ + 1; dz <= halfZ - 1; dz++) {
                        if (random.nextDouble() < 0.35) {
                            Block b = world.getBlockAt(roomX + dx, floorY, roomZ + dz);
                            b.setType(Material.MAGMA_BLOCK);
                            placedBlocks.add(b);
                        }
                    }
                }
                // 2. 隐藏压力板陷阱
                if (random.nextBoolean()) {
                    int px = roomX + random.nextInt(width) - halfX;
                    int pz = roomZ + random.nextInt(height) - halfZ;
                    Block plate = world.getBlockAt(px, floorY, pz);
                    plate.setType(Material.STONE_PRESSURE_PLATE);
                    placedBlocks.add(plate);
                    Block dispenser = world.getBlockAt(px, floorY + 1, pz);
                    dispenser.setType(Material.DISPENSER);
                    placedBlocks.add(dispenser);
                    org.bukkit.block.Dispenser dState = (org.bukkit.block.Dispenser) dispenser.getState();
                    dState.getInventory().addItem(new ItemStack(Material.ARROW, 16));
                    dState.update();
                }
                // 播放特效
                Location trapCenter = new Location(world, roomX + 0.5, floorY + 0.5, roomZ + 0.5);
                world.playSound(trapCenter, Sound.BLOCK_LAVA_AMBIENT, 1.0f, 1.0f);
                world.spawnParticle(Particle.LAVA, trapCenter, 15, 0.5, 0, 0.5, 0.1);
            }
            /**
             * 放置宝藏箱
             */
            private void placeTreasureChest(int roomX, int roomZ, int floorY, boolean isRare) {
                Block chestBlock = world.getBlockAt(roomX, floorY, roomZ);
                chestBlock.setType(Material.CHEST);
                placedBlocks.add(chestBlock);
                // 填充宝箱奖励
                if (chestBlock.getState() instanceof Chest chest) {
                    if (isRare) {
                        chest.getInventory().addItem(generateRareReward());
                        chest.getInventory().addItem(new ItemStack(Material.DIAMOND, random.nextInt(3) + 2));
                        chest.getInventory().addItem(new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 1));
                    } else {
                        chest.getInventory().addItem(generateNormalReward());
                    }
                }
                // 播放特效
                Location chestLoc = chestBlock.getLocation().add(0.5, 0.5, 0.5);
                if (isRare) {
                    world.playSound(chestLoc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                    world.spawnParticle(Particle.TOTEM_OF_UNDYING, chestLoc, 20, 0.5, 0.5, 0.5, 0.1);
                } else {
                    world.playSound(chestLoc, Sound.BLOCK_ANVIL_PLACE, 1.0f, 1.2f);
                    world.spawnParticle(Particle.HEART, chestLoc, 10, 0.5, 0.5, 0.5, 0.1);
                }
            }
            /**
             * 生成普通宝箱奖励
             */
            private ItemStack generateNormalReward() {
                int r = random.nextInt(10);
                if (r < 3) return new ItemStack(Material.IRON_INGOT, random.nextInt(5) + 1);
                if (r < 6) return new ItemStack(Material.GOLD_INGOT, random.nextInt(3) + 1);
                if (r < 9) return new ItemStack(Material.DIAMOND, 1);
                return new ItemStack(Material.EMERALD, random.nextInt(3) + 1);
            }
            /**
             * 生成稀有宝箱奖励
             */
            private ItemStack generateRareReward() {
                ItemStack weapon = new ItemStack(Material.DIAMOND_SWORD);
                weapon.addEnchantment(Enchantment.SHARPNESS, 3);
                weapon.addEnchantment(Enchantment.LOOTING, 2);
                ItemMeta meta = weapon.getItemMeta();
                meta.setDisplayName(ChatColor.AQUA + "地牢征服者之剑");
                meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
                weapon.setItemMeta(meta);
                return weapon;
            }
            /**
             * 给玩家添加高级增益
             */
            private void applyAdvancedBuffToPlayer(Player target, Location center) {
                target.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60 * 20, 2));
                target.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 60 * 20, 1));
                target.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 30 * 20, 1));
                target.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 45 * 20, 0));
                target.sendMessage(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "✨ 你进入了祝福圣殿！获得了强大的祝福效果！");
                world.playSound(center, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                world.spawnParticle(Particle.HAPPY_VILLAGER, center, 50, 0.5, 0.5, 0.5, 0.1);
            }
            /**
             * 放置祭坛
             */
            private void placeAltar(int roomX, int roomZ, int floorY) {
                Block altar = world.getBlockAt(roomX, floorY, roomZ);
                altar.setType(Material.NETHERITE_BLOCK);
                placedBlocks.add(altar);
                altars.add(altar.getLocation());
                player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "⛩ 你发现了神秘的命运祭坛！右键它来赌上你的命运...");
                world.playSound(altar.getLocation().add(0.5, 0.5, 0.5), Sound.BLOCK_BEACON_ACTIVATE, 0.8f, 1.2f);
            }
            /**
             * 清理房间
             */
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
        // ========== DungeonManager 核心方法 ==========
        /**
         * 构造地牢管理器
         * @param plugin 主插件实例
         * @param player 进入地牢的玩家
         * @param difficultyFactor 难度系数
         * @param currentFloor 当前层数
         */
        public DungeonManager(JavaPlugin plugin, Player player, double difficultyFactor, int currentFloor) {
            this.plugin = plugin;
            this.player = player;
            this.world = player.getWorld();
            this.difficultyFactor = difficultyFactor;
            this.currentFloor = currentFloor;
            Location loc = player.getLocation();
            this.centerX = loc.getBlockX();
            this.centerY = loc.getBlockY();
            this.centerZ = loc.getBlockZ();
            // 动态调整地牢大小，层数越高越大
            this.dungeonSize = Math.min(80 + (currentFloor - 1) * 15, 160);
            // 动态调整房间概率，层数越高，危险房间越多
            this.ENEMY_CHANCE = Math.min(0.30 + currentFloor * 0.02, 0.40);
            this.ELITE_ENEMY_CHANCE = Math.min(0.10 + currentFloor * 0.02, 0.20);
            this.TRAP_CHANCE = Math.min(0.15 + currentFloor * 0.01, 0.25);
            this.TREASURE_CHANCE = Math.max(0.20 - currentFloor * 0.01, 0.10);
            this.RARE_TREASURE_CHANCE = Math.max(0.05 - currentFloor * 0.005, 0.02);
            // 生成地牢布局
            generateLayout();
            // 初始化噪声生成器
            long seed = world.getSeed() + centerX * 31L + centerZ * 71L;
            noiseFloor = new SimplexNoiseGenerator((int) seed);
            noiseWall = new SimplexNoiseGenerator((int) (seed + 12345));
        }
        // 根据层数获取对应的地板材质池
        private Material[] getFloorMaterialsForFloor() {
            if (currentFloor <= 2) return FLOOR_MATERIALS_EARLY;
            if (currentFloor <= 4) return FLOOR_MATERIALS_MID;
            return FLOOR_MATERIALS_LATE;
        }
        // 根据层数获取对应的墙壁材质池
        private Material[] getWallMaterialsForFloor() {
            if (currentFloor <= 2) return WALL_MATERIALS_EARLY;
            if (currentFloor <= 4) return WALL_MATERIALS_MID;
            return WALL_MATERIALS_LATE;
        }
        /**
         * 在两个房间之间创建门
         */
        private void createDoorBetweenRooms(Room r1, Room r2, BlockFace face, int doorX, int doorZ) {
            Door door = new Door(face, null, null, r1, r2);
            r1.doors.put(face, door);
            BlockFace opposite = face.getOppositeFace();
            r2.doors.put(opposite, door);
        }
        /**
         * 生成地牢布局（BSP算法）
         */
        private void generateLayout() {
            rooms = new ArrayList<>();
            // 1. 初始化BSP根节点
            BSPNode root = new BSPNode(0, 0, dungeonSize, dungeonSize);
            // 2. 递归分割
            List<BSPNode> nodes = new ArrayList<>();
            nodes.add(root);
            for (int i = 0; i < nodes.size(); i++) {
                BSPNode node = nodes.get(i);
                if (node.split()) {
                    nodes.add(node.left);
                    nodes.add(node.right);
                }
            }
            // 3. 生成房间
            root.createRooms();
            // 4. 计算每个房间到起点的距离（起点是最中心的房间）
            startRoom = rooms.get(0);
            int minDist = Integer.MAX_VALUE;
            for (Room room : rooms) {
                int dist = Math.abs(room.worldX - centerX) + Math.abs(room.worldZ - centerZ);
                if (dist < minDist) {
                    minDist = dist;
                    startRoom = room;
                }
            }
            // 5. 计算所有房间到起点的曼哈顿距离（BFS）
            Map<Room, Integer> distMap = new HashMap<>();
            Queue<Room> queue = new LinkedList<>();
            queue.add(startRoom);
            distMap.put(startRoom, 0);
            while (!queue.isEmpty()) {
                Room current = queue.poll();
                for (Door door : current.doors.values()) {
                    Room neighbor = door.toRoom.equals(current) ? door.fromRoom : door.toRoom;
                    if (!distMap.containsKey(neighbor)) {
                        distMap.put(neighbor, distMap.get(current) + 1);
                        queue.add(neighbor);
                    }
                }
            }
            // 6. 更新房间的distanceFromStart
            for (Room room : rooms) {
                room.distanceFromStart = distMap.getOrDefault(room, 0);
            }
            // 7. 分配房间类型
            // 起点房间是空房间
            startRoom.type = RoomType.EMPTY;
            // 最远的房间是Boss房间
            Room bossRoom = startRoom;
            int maxDist = -1;
            for (Room room : rooms) {
                if (room.distanceFromStart > maxDist) {
                    maxDist = room.distanceFromStart;
                    bossRoom = room;
                }
            }
            bossRoom.type = RoomType.BOSS;
            // 其他房间随机分配类型
            for (Room room : rooms) {
                if (room == startRoom || room == bossRoom) continue;
                room.type = selectRandomRoomType();
            }
        }
        /**
         * 随机选择房间类型
         */
        private RoomType selectRandomRoomType() {
            double r = random.nextDouble();
            double cum = 0;
            cum += ENEMY_CHANCE;
            if (r < cum) return RoomType.ENEMY;
            cum += ELITE_ENEMY_CHANCE;
            if (r < cum) return RoomType.ELITE_ENEMY;
            cum += TRAP_CHANCE;
            if (r < cum) return RoomType.TRAP;
            cum += TREASURE_CHANCE;
            if (r < cum) return RoomType.TREASURE;
            cum += RARE_TREASURE_CHANCE;
            if (r < cum) return RoomType.RARE_TREASURE;
            cum += BUFF_CHANCE;
            if (r < cum) return RoomType.BUFF;
            return RoomType.ALTAR;
        }
        /**
         * 获取门宽度方向
         */
        private BlockFace getPerpendicular(BlockFace face) {
            return switch (face) {
                case NORTH, SOUTH -> BlockFace.EAST;
                case EAST, WEST -> BlockFace.NORTH;
                default -> BlockFace.NORTH;
            };
        }
        // ========== 对外接口 ==========
        public String checkBlocked() {
            int floorY = centerY - 1;
            for (Room room : rooms) {
                int halfX = room.width / 2;
                int halfZ = room.height / 2;
                int worldX = room.worldX;
                int worldZ = room.worldZ;
                for (int dx = -halfX; dx <= halfX; dx++) {
                    for (int dz = -halfZ; dz <= halfZ; dz++) {
                        for (int h = 0; h <= WALL_HEIGHT; h++) {
                            Block b = world.getBlockAt(worldX + dx, floorY + h, worldZ + dz);
                            if (!b.isEmpty() && !b.isLiquid()) {
                                return "区域被阻挡: " + b.getType();
                            }
                        }
                    }
                }
            }
            return null;
        }
        public void generateStartRoom() {
            startRoom.generate();
            player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "🏰 第" + currentFloor + "层地牢已开启！");
            player.sendMessage(ChatColor.GRAY + "提示：点击铁门来解锁并进入新的房间，输入/roguelike clean可以手动清理地牢");
        }
        public boolean isDoorBlock(Block block) {
            return doorMap.containsKey(block.getLocation());
        }
        public void expandFromDoor(Block block, Player player) {
            Door door = doorMap.get(block.getLocation());
            if (door == null) return;
            Room targetRoom = door.toRoom.generated ? door.fromRoom : door.toRoom;
            if (targetRoom != null && !targetRoom.generated) {
                targetRoom.generate();
            }
            for (Location loc : door.blocks) {
                world.getBlockAt(loc).setType(Material.AIR);
                doorMap.remove(loc);
            }
            player.sendMessage(ChatColor.YELLOW + "你打开了一扇门，新的区域展现在你面前...");
        }
        public boolean isAltarBlock(Block block) {
            return altars.contains(block.getLocation());
        }
        public void onAltarInteract(Player player, Block block) {
            altars.remove(block.getLocation());
            double roll = random.nextDouble();
            if (roll < 0.7) {
                if (roll < 0.3) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 200, 1));
                    player.sendMessage(ChatColor.GREEN + "祭坛赐予你生命的祝福！");
                } else {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 200, 1));
                    player.sendMessage(ChatColor.GREEN + "祭坛赐予你力量的祝福！");
                }
            } else {
                if (roll < 0.85) {
                    player.damage(5.0);
                    player.sendMessage(ChatColor.RED + "祭坛的诅咒！你受到了伤害！");
                } else {
                    player.getInventory().addItem(new ItemStack(Material.GOLD_NUGGET, random.nextInt(5) + 1));
                    player.sendMessage(ChatColor.YELLOW + "祭坛给了你一些金币...");
                }
            }
        }
        public Monster getBoss() {
            return boss;
        }
        private void spawnBossAt(Room room) {
            Location center = new Location(world, room.worldX + 0.5, centerY, room.worldZ + 0.5);
            Entity e = world.spawnEntity(center, EntityType.WITHER_SKELETON);
            if (e instanceof Monster monster) {
                monster.setRemoveWhenFarAway(false);
                // Boss属性
                double health = 100.0 * difficultyFactor;
                monster.getAttribute(Attribute.MAX_HEALTH).setBaseValue(health);
                monster.setHealth(health);
                double damage = 8.0 * difficultyFactor;
                monster.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(damage);
                // Boss装备
                ItemStack sword = new ItemStack(Material.NETHERITE_SWORD);
                sword.addEnchantment(Enchantment.SHARPNESS, 5);
                monster.getEquipment().setItemInMainHand(sword);
                monster.getEquipment().setItemInMainHandDropChance(0.5f);
                boss = monster;
                room.spawnedEntities.add(monster);
            }
        }
        public boolean isCompleted() {
            return isCompleted;
        }
        public void complete(Player player) {
            isCompleted = true;
        }
        public void cleanup() {
            for (Room room : rooms) {
                room.cleanup();
            }
            for (Location loc : doorMap.keySet()) {
                world.getBlockAt(loc).setType(Material.AIR);
            }
            doorMap.clear();
            altars.clear();
        }
    }
}
