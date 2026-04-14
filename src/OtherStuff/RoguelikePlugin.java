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
 * 重构说明：使用随机房间放置+最小生成树(MST)算法重构地牢生成机制，
 * 替代原BSP算法，生成灵活的独立房间式地牢，保留明确的房间类型划分。
 *
 * 自定义配置：请修改下方 "===== 自定义房间与门参数 =====" 区域的常量。
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

        if (args.length > 0 && args[0].equalsIgnoreCase("start")) {
            if (activeDungeon != null && !activeDungeon.isCompleted()) {
                player.sendMessage(ChatColor.RED + "A dungeon is already active. Finish or clean it first.");
                return true;
            }

            // 重置无尽模式状态
            currentFloor = 1;
            difficultyFactor = 1.0;
            playerScore = 0;

            // 初始化玩家状态
            player.setGameMode(GameMode.SURVIVAL);
            player.getInventory().clear();
            ItemStack ironSword = new ItemStack(Material.IRON_SWORD);
            ItemStack steak = new ItemStack(Material.COOKED_BEEF, 16);
            ItemStack healthPotion = new ItemStack(Material.POTION, 1);
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

            DungeonManager generator = new DungeonManager(plugin, player, difficultyFactor, currentFloor);
            String error = generator.checkBlocked();
            if (error != null) {
                player.sendMessage(ChatColor.RED + "Cannot generate dungeon: " + error);
                return true;
            }
            generator.generateStartRoom();
            activeDungeon = generator;
            return true;

        } else if (args.length > 0 && args[0].equalsIgnoreCase("clean")) {
            if (activeDungeon != null) {
                activeDungeon.cleanup();
                activeDungeon = null;
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

    @EventHandler
    public void onDoorInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null || activeDungeon == null || activeDungeon.isCompleted()) return;

        if (activeDungeon.isDoorBlock(block)) {
            event.setCancelled(true);
            activeDungeon.expandFromDoor(block, event.getPlayer());
        } else if (activeDungeon.isAltarBlock(block)) {
            event.setCancelled(true);
            activeDungeon.onAltarInteract(event.getPlayer(), block);
        }
    }

    @EventHandler
    public void onEntityKill(EntityDeathEvent event) {
        if (activeDungeon == null) return;
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        Entity entity = event.getEntity();
        if (entity.equals(activeDungeon.getBoss())) {
            playerScore += 200 * currentFloor;
        } else if (entity instanceof Monster monster) {
            boolean isElite = monster.getAttribute(Attribute.MAX_HEALTH).getValue() > 30;
            playerScore += isElite ? 50 : 10;
        }
    }

    @EventHandler
    public void onBossDeath(EntityDeathEvent event) {
        if (activeDungeon == null || activeDungeon.isCompleted()) return;
        if (event.getEntity().equals(activeDungeon.getBoss())) {
            Player killer = event.getEntity().getKiller();
            if (killer != null) {
                activeDungeon.complete(killer);
                currentFloor++;
                difficultyFactor += 0.5;
                playerScore += 100 * currentFloor;
                killer.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "🎉 你击败了Boss！进入地牢第" + currentFloor + "层！");
                killer.sendMessage(ChatColor.GRAY + "新的难度系数: " + String.format("%.1f", difficultyFactor));

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
                }, 20L);
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (activeDungeon == null) return;

        int finalScore = playerScore;
        int finalFloor = currentFloor;
        player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "💀 你在地牢中死亡了！");
        player.sendMessage(ChatColor.GOLD + "=== 最终结算 ===");
        player.sendMessage(ChatColor.YELLOW + "通关层数: " + finalFloor);
        player.sendMessage(ChatColor.YELLOW + "最终得分: " + finalScore);
        player.sendMessage(ChatColor.GRAY + "输入/roguelike start 来重新开始挑战！");

        activeDungeon.cleanup();
        activeDungeon = null;
        currentFloor = 1;
        difficultyFactor = 1.0;
        playerScore = 0;
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_DEATH, 1.0f, 1.0f);
    }

    public void onDisable() {
        if (activeDungeon != null) {
            activeDungeon.cleanup();
        }
    }

    // ==================== 内部类：地牢核心管理类（已修复所有地形生成问题） ====================
    private static class DungeonManager {

        // ========== 自定义房间与门参数（可根据需求自由修改） ==========
        private static final int MIN_ROOM_SIZE = 13;
        private static final int MAX_ROOM_SIZE = 17;
        private static final int MAX_ROOMS = 15;
        private static final int MIN_ROOMS = 8;
        private static final int DOOR_WIDTH = 3;           // 门宽度（方块数）
        private static final int DOOR_HEIGHT = 3;          // 门高度（方块数）
        private static final int WALL_HEIGHT = 6;          // 墙壁高度
        private static final int CORRIDOR_WIDTH = 3;       // 走廊宽度（暂未使用走廊填充）
        private final int dungeonSize = 64;                // 地牢总范围尺寸

        // 动态概率，随难度调整
        private double ENEMY_CHANCE = 0.30;
        private double ELITE_ENEMY_CHANCE = 0.10;
        private double TRAP_CHANCE = 0.15;
        private double TREASURE_CHANCE = 0.20;
        private double RARE_TREASURE_CHANCE = 0.05;
        private double BUFF_CHANCE = 0.12;
        private double ALTAR_CHANCE = 0.08;

        // 分层材质池（保持不变）
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
        private final int centerX, centerY, centerZ;
        private final Random random = new Random();
        private final SimplexNoiseGenerator noiseFloor;
        private final SimplexNoiseGenerator noiseWall;
        private final double difficultyFactor;
        private final int currentFloor;
        private List<Room> rooms;
        private final Map<Location, Door> doorMap = new HashMap<>(); // 键为门任意方块的Location
        private final Set<Location> altars = new HashSet<>();
        private Monster boss;
        private boolean bossSpawned = false;
        private boolean isCompleted = false;
        private Room startRoom;

        private static class Edge {
            Room from, to;
            double distance;
            Edge(Room from, Room to, double distance) {
                this.from = from;
                this.to = to;
                this.distance = distance;
            }
        }

        public enum RoomType {
            EMPTY, ENEMY, ELITE_ENEMY, TRAP, TREASURE, RARE_TREASURE, BUFF, ALTAR, BOSS
        }

        private static class Door {
            BlockFace facing;
            Location primaryBlock; // 门中心底部方块（用于快速查找）
            List<Location> blocks; // 门所有方块的Location列表
            Room fromRoom;
            Room toRoom;
            Door(BlockFace facing, Location primaryBlock, List<Location> blocks, Room fromRoom, Room toRoom) {
                this.facing = facing;
                this.primaryBlock = primaryBlock;
                this.blocks = blocks;
                this.fromRoom = fromRoom;
                this.toRoom = toRoom;
            }
        }

        private class Room {
            final int worldX, worldZ;
            final int localX, localZ;
            final int width, height;
            RoomType type;
            boolean generated;
            final List<Block> placedBlocks = new ArrayList<>();
            final List<Entity> spawnedEntities = new ArrayList<>();
            int distanceFromStart;
            final Map<BlockFace, Door> doors = new HashMap<>();

            Room(int worldX, int worldZ, int localX, int localZ, int width, int height, RoomType type, int distFromStart) {
                this.worldX = worldX;
                this.worldZ = worldZ;
                this.localX = localX;
                this.localZ = localZ;
                this.width = width;
                this.height = height;
                this.type = type;
                this.generated = false;
                this.distanceFromStart = distFromStart;
            }

            void generate() {
                if (generated) return;
                // 清空房间区域空气（避免原有方块干扰）
                clearRoomArea();
                placeFloorAndWalls();
                placeRoomContents();
                createAllDoors();
                generated = true;

                if (type == RoomType.BOSS && !bossSpawned) {
                    spawnBossAt(this);
                    bossSpawned = true;
                    player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "⚠ 你进入了Boss房间！强大的地牢守护者出现了！");
                    player.playSound(player.getLocation(), Sound.ENTITY_WITHER_AMBIENT, 1.0f, 0.5f);
                } else if (type == RoomType.ELITE_ENEMY) {
                    player.sendMessage(ChatColor.DARK_PURPLE + "你感受到了强大的气息...这里有精英怪物！");
                }

                Location center = new Location(world, worldX + 0.5, centerY, worldZ + 0.5);
                world.playSound(center, Sound.BLOCK_STONE_PLACE, 0.8f, 0.8f);
                world.spawnParticle(Particle.ELECTRIC_SPARK, center, 20, 0.5, 1, 0.5, 0.1);
            }

            private void clearRoomArea() {
                int floorY = centerY - 1;
                int halfX = width / 2;
                int halfZ = height / 2;
                for (int dx = -halfX; dx <= halfX; dx++) {
                    for (int dz = -halfZ; dz <= halfZ; dz++) {
                        for (int y = 0; y < WALL_HEIGHT + 1; y++) {
                            world.getBlockAt(worldX + dx, floorY + y, worldZ + dz).setType(Material.AIR);
                        }
                    }
                }
            }

            private void placeFloorAndWalls() {
                int floorY = centerY - 1;
                int halfX = width / 2;
                int halfZ = height / 2;

                Material[] floorMaterials;
                Material[] wallMaterials;
                if (type == RoomType.ENEMY || type == RoomType.EMPTY) {
                    floorMaterials = getFloorMaterialsForFloor();
                    wallMaterials = getWallMaterialsForFloor();
                } else {
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
                            floorMaterials = new Material[]{Material.GOLD_BLOCK, Material.GOLD_ORE, Material.GILDED_BLACKSTONE};
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
                    }
                }

                // 地板
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

                // 路径材质
                Material pathMat;
                switch (type) {
                    case ELITE_ENEMY: pathMat = Material.PURPLE_CONCRETE; break;
                    case TRAP: pathMat = Material.RED_CONCRETE; break;
                    case TREASURE: pathMat = Material.YELLOW_CONCRETE; break;
                    case RARE_TREASURE: pathMat = Material.GOLD_BLOCK; break;
                    case BUFF: pathMat = Material.PINK_CONCRETE; break;
                    case ALTAR: pathMat = Material.BLACK_CONCRETE; break;
                    case BOSS: pathMat = Material.CRIMSON_HYPHAE; break;
                    default: pathMat = currentFloor <= 2 ? Material.GOLD_BLOCK :
                            currentFloor <=4 ? Material.QUARTZ_BLOCK : Material.NETHER_QUARTZ_ORE;
                }
                for (Door door : doors.values()) {
                    int pathWidth = Math.min(DOOR_WIDTH, (door.facing == BlockFace.NORTH || door.facing == BlockFace.SOUTH) ? width : height);
                    int halfPath = pathWidth / 2;
                    if (door.facing == BlockFace.NORTH) {
                        for (int step = 1; step <= halfZ; step++) {
                            for (int offset = -halfPath; offset <= halfPath; offset++) {
                                Block path = world.getBlockAt(worldX + offset, floorY, worldZ + step);
                                path.setType(pathMat);
                                placedBlocks.add(path);
                            }
                        }
                    } else if (door.facing == BlockFace.SOUTH) {
                        for (int step = 1; step <= halfZ; step++) {
                            for (int offset = -halfPath; offset <= halfPath; offset++) {
                                Block path = world.getBlockAt(worldX + offset, floorY, worldZ - step);
                                path.setType(pathMat);
                                placedBlocks.add(path);
                            }
                        }
                    } else if (door.facing == BlockFace.EAST) {
                        for (int step = 1; step <= halfX; step++) {
                            for (int offset = -halfPath; offset <= halfPath; offset++) {
                                Block path = world.getBlockAt(worldX + step, floorY, worldZ + offset);
                                path.setType(pathMat);
                                placedBlocks.add(path);
                            }
                        }
                    } else if (door.facing == BlockFace.WEST) {
                        for (int step = 1; step <= halfX; step++) {
                            for (int offset = -halfPath; offset <= halfPath; offset++) {
                                Block path = world.getBlockAt(worldX - step, floorY, worldZ + offset);
                                path.setType(pathMat);
                                placedBlocks.add(path);
                            }
                        }
                    }
                }

                // 墙壁
                for (int dx = -halfX; dx <= halfX; dx++) {
                    for (int dz = -halfZ; dz <= halfZ; dz++) {
                        if (Math.abs(dx) != halfX && Math.abs(dz) != halfZ) continue;
                        boolean isNorth = (dz == halfZ);
                        boolean isSouth = (dz == -halfZ);
                        boolean isEast  = (dx == halfX);
                        boolean isWest  = (dx == -halfX);

                        for (int h = 0; h < WALL_HEIGHT; h++) {
                            // 检查是否处于门洞内
                            boolean isDoorHole = false;
                            if (h < DOOR_HEIGHT) {
                                int doorWidth = Math.min(DOOR_WIDTH, (isNorth || isSouth) ? width : height);
                                int halfDoor = doorWidth / 2;
                                if (isNorth && hasDoor(BlockFace.NORTH) && Math.abs(dx) <= halfDoor) isDoorHole = true;
                                else if (isSouth && hasDoor(BlockFace.SOUTH) && Math.abs(dx) <= halfDoor) isDoorHole = true;
                                else if (isEast && hasDoor(BlockFace.EAST) && Math.abs(dz) <= halfDoor) isDoorHole = true;
                                else if (isWest && hasDoor(BlockFace.WEST) && Math.abs(dz) <= halfDoor) isDoorHole = true;
                            }
                            if (isDoorHole) continue;

                            Block wall = world.getBlockAt(worldX + dx, floorY + 1 + h, worldZ + dz);
                            double noise = noiseWall.noise((worldX + dx) * 0.1, (floorY + 1 + h) * 0.05, (worldZ + dz) * 0.1);
                            int index = (int) Math.floor((noise + 1) / 2 * wallMaterials.length);
                            index = Math.max(0, Math.min(index, wallMaterials.length - 1));
                            wall.setType(wallMaterials[index]);
                            placedBlocks.add(wall);

                            if (h == 2 && random.nextDouble() < 0.05) {
                                Block torch = world.getBlockAt(worldX + dx, floorY + 3 + h, worldZ + dz);
                                torch.setType(Material.WALL_TORCH);
                                placedBlocks.add(torch);
                            }
                        }
                    }
                }
            }

            private boolean hasDoor(BlockFace face) {
                return doors.containsKey(face);
            }

            private void createAllDoors() {
                for (Door door : doors.values()) {
                    if (!door.fromRoom.equals(this)) continue;
                    createDoor(door);
                }
            }

            private void createDoor(Door door) {
                int halfX = width / 2;
                int halfZ = height / 2;
                int doorCenterX, doorCenterZ;
                BlockFace doorFacing = door.facing;

                switch (doorFacing) {
                    case NORTH -> { doorCenterX = worldX; doorCenterZ = worldZ + halfZ; }
                    case SOUTH -> { doorCenterX = worldX; doorCenterZ = worldZ - halfZ; }
                    case EAST  -> { doorCenterX = worldX + halfX; doorCenterZ = worldZ; }
                    case WEST  -> { doorCenterX = worldX - halfX; doorCenterZ = worldZ; }
                    default -> { return; }
                }

                int doorWidth = Math.min(DOOR_WIDTH, (doorFacing == BlockFace.NORTH || doorFacing == BlockFace.SOUTH) ? width : height);
                int halfDoor = doorWidth / 2;
                List<Location> doorBlocks = new ArrayList<>();

                for (int offset = -halfDoor; offset <= halfDoor; offset++) {
                    for (int h = 0; h < DOOR_HEIGHT; h++) {
                        int bx = doorCenterX;
                        int bz = doorCenterZ;
                        if (doorFacing == BlockFace.NORTH || doorFacing == BlockFace.SOUTH) {
                            bx += offset;
                        } else {
                            bz += offset;
                        }
                        Block b = world.getBlockAt(bx, centerY + h + 1, bz);
                        b.setType(Material.IRON_BLOCK);
                        doorBlocks.add(b.getLocation());
                        placedBlocks.add(b);
                    }
                }

                door.primaryBlock = new Location(world, doorCenterX, centerY, doorCenterZ);
                door.blocks = doorBlocks;
                // 将每个门方块都注册到doorMap，方便右键交互
                for (Location loc : doorBlocks) {
                    doorMap.put(loc, door);
                }
            }

            private void placeRoomContents() {
                switch (type) {
                    case ENEMY -> spawnEnemies(this, 2 + (int)(difficultyFactor * 2));
                    case ELITE_ENEMY -> spawnEliteEnemy(this);
                    case TRAP -> spawnTraps(this);
                    case TREASURE -> spawnTreasure(this, false);
                    case RARE_TREASURE -> spawnTreasure(this, true);
                    case BUFF -> spawnBuffAltar(this);
                    case ALTAR -> spawnRandomAltar(this);
                }
            }

            private void spawnEnemies(Room room, int count) {
                int floorY = centerY;
                for (int i = 0; i < count; i++) {
                    int x = room.worldX + random.nextInt(room.width - 2) - room.width/2;
                    int z = room.worldZ + random.nextInt(room.height - 2) - room.height/2;
                    Location loc = new Location(world, x + 0.5, floorY, z + 0.5);
                    Monster entity;
                    double roll = random.nextDouble();
                    if (roll < 0.3) entity = (Zombie) world.spawnEntity(loc, EntityType.ZOMBIE);
                    else if (roll < 0.5) entity = (Skeleton) world.spawnEntity(loc, EntityType.SKELETON);
                    else entity = (Creeper) world.spawnEntity(loc, EntityType.CREEPER);

                    double health = entity.getAttribute(Attribute.MAX_HEALTH).getValue() * difficultyFactor;
                    entity.getAttribute(Attribute.MAX_HEALTH).setBaseValue(health);
                    entity.setHealth(health);
                    entity.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(
                            entity.getAttribute(Attribute.ATTACK_DAMAGE).getValue() * difficultyFactor);
                    room.spawnedEntities.add(entity);
                }
            }

            private void spawnEliteEnemy(Room room) {
                Location loc = new Location(world, room.worldX + 0.5, centerY, room.worldZ + 0.5);
                Evoker evoker = (Evoker) world.spawnEntity(loc, EntityType.EVOKER);
                double health = evoker.getAttribute(Attribute.MAX_HEALTH).getValue() * difficultyFactor * 1.5;
                evoker.getAttribute(Attribute.MAX_HEALTH).setBaseValue(health);
                evoker.setHealth(health);
                evoker.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(
                        evoker.getAttribute(Attribute.ATTACK_DAMAGE).getValue() * difficultyFactor);
                room.spawnedEntities.add(evoker);
            }

            private void spawnTraps(Room room) {
                int floorY = centerY - 1;
                for (int i = 0; i < 3; i++) {
                    int x = room.worldX + random.nextInt(room.width - 2) - room.width/2;
                    int z = room.worldZ + random.nextInt(room.height - 2) - room.height/2;
                    Block b = world.getBlockAt(x, floorY + 1, z);
                    if (b.getType().isSolid()) {
                        b.setType(Material.TRIPWIRE_HOOK);
                        room.placedBlocks.add(b);
                    }
                }
            }

            private void spawnTreasure(Room room, boolean isRare) {
                Location loc = new Location(world, room.worldX + 0.5, centerY, room.worldZ + 0.5);
                Block chestBlock = world.getBlockAt(loc);
                chestBlock.setType(isRare ? Material.ENDER_CHEST : Material.CHEST);
                room.placedBlocks.add(chestBlock);
                Chest chest = (Chest) chestBlock.getState();
                if (isRare) {
                    ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
                    sword.addEnchantment(Enchantment.SHARPNESS, 3);
                    sword.addEnchantment(Enchantment.FIRE_ASPECT, 2);
                    chest.getInventory().addItem(sword);
                    ItemStack armor = new ItemStack(Material.DIAMOND_CHESTPLATE);
                    armor.addEnchantment(Enchantment.PROTECTION, 4);
                    chest.getInventory().addItem(armor);
                    ItemStack potion = new ItemStack(Material.POTION, 2);
                    PotionMeta meta = (PotionMeta) potion.getItemMeta();
                    meta.addCustomEffect(new PotionEffect(PotionEffectType.REGENERATION, 200, 1), true);
                    meta.setColor(Color.RED);
                    potion.setItemMeta(meta);
                    chest.getInventory().addItem(potion);
                } else {
                    ItemStack ironArmor = new ItemStack(Material.IRON_HELMET);
                    ironArmor.addEnchantment(Enchantment.PROTECTION, 1);
                    chest.getInventory().addItem(ironArmor);
                    chest.getInventory().addItem(new ItemStack(Material.GOLDEN_CARROT, 8));
                    ItemStack potion = new ItemStack(Material.POTION, 1);
                    PotionMeta meta = (PotionMeta) potion.getItemMeta();
                    meta.addCustomEffect(new PotionEffect(PotionEffectType.INSTANT_HEALTH, 1, 2), true);
                    meta.setColor(Color.RED);
                    potion.setItemMeta(meta);
                    chest.getInventory().addItem(potion);
                }
            }

            private void spawnBuffAltar(Room room) {
                Location loc = new Location(world, room.worldX + 0.5, centerY, room.worldZ + 0.5);
                Block b = world.getBlockAt(loc);
                b.setType(Material.BEACON);
                altars.add(loc);
                room.placedBlocks.add(b);
            }

            private void spawnRandomAltar(Room room) {
                Location loc = new Location(world, room.worldX + 0.5, centerY, room.worldZ + 0.5);
                Block b = world.getBlockAt(loc);
                b.setType(Material.ENCHANTING_TABLE);
                altars.add(loc);
                room.placedBlocks.add(b);
            }

            private void spawnBossAt(Room room) {
                Location loc = new Location(world, room.worldX + 0.5, centerY, room.worldZ + 0.5);
                Wither bossEntity = (Wither) world.spawnEntity(loc, EntityType.WITHER);
                double health = bossEntity.getAttribute(Attribute.MAX_HEALTH).getValue() * difficultyFactor;
                bossEntity.getAttribute(Attribute.MAX_HEALTH).setBaseValue(health);
                bossEntity.setHealth(health);
                boss = bossEntity;
                room.spawnedEntities.add(bossEntity);
            }
        }

        // ========== DungeonManager 构造函数与生成逻辑 ==========
        public DungeonManager(JavaPlugin plugin, Player player, double difficultyFactor, int currentFloor) {
            this.plugin = plugin;
            this.player = player;
            this.world = player.getWorld();
            this.centerX = player.getLocation().getBlockX();
            this.centerY = player.getLocation().getBlockY();
            this.centerZ = player.getLocation().getBlockZ();
            this.difficultyFactor = difficultyFactor;
            this.currentFloor = currentFloor;
            this.noiseFloor = new SimplexNoiseGenerator(random);
            this.noiseWall = new SimplexNoiseGenerator(random);
            this.rooms = new ArrayList<>();

            generateRandomRooms();
            generateMinimumSpanningTree();
            assignRoomTypes();
        }

        private void generateRandomRooms() {
            List<Room> tempRooms = new ArrayList<>();
            int attempts = 0;
            while (tempRooms.size() < MAX_ROOMS && attempts < 100) {
                int w = random.nextInt(MAX_ROOM_SIZE - MIN_ROOM_SIZE) + MIN_ROOM_SIZE;
                int h = random.nextInt(MAX_ROOM_SIZE - MIN_ROOM_SIZE) + MIN_ROOM_SIZE;
                int localX = random.nextInt(dungeonSize - w - 4) + 2;
                int localZ = random.nextInt(dungeonSize - h - 4) + 2;
                int worldX = centerX + localX - dungeonSize/2 + w/2;
                int worldZ = centerZ + localZ - dungeonSize/2 + h/2;

                boolean overlaps = false;
                for (Room other : tempRooms) {
                    // 修复重叠检测逻辑：两个矩形在X和Z轴都相交才算重叠
                    int pad = 2;
                    boolean xOverlap = (worldX - w/2) < (other.worldX + other.width/2 + pad) &&
                            (worldX + w/2) > (other.worldX - other.width/2 - pad);
                    boolean zOverlap = (worldZ - h/2) < (other.worldZ + other.height/2 + pad) &&
                            (worldZ + h/2) > (other.worldZ - other.height/2 - pad);
                    if (xOverlap && zOverlap) {
                        overlaps = true;
                        break;
                    }
                }
                if (!overlaps) {
                    tempRooms.add(new Room(worldX, worldZ, localX, localZ, w, h, RoomType.EMPTY, 0));
                }
                attempts++;
            }
            if (tempRooms.size() < MIN_ROOMS) {
                for (int i = tempRooms.size(); i < MIN_ROOMS; i++) {
                    int w = MIN_ROOM_SIZE;
                    int h = MIN_ROOM_SIZE;
                    int localX = i * 10;
                    int localZ = 0;
                    int worldX = centerX + localX - dungeonSize/2 + w/2;
                    int worldZ = centerZ + localZ - dungeonSize/2 + h/2;
                    tempRooms.add(new Room(worldX, worldZ, localX, localZ, w, h, RoomType.EMPTY, 0));
                }
            }
            this.rooms = tempRooms;
        }

        private void generateMinimumSpanningTree() {
            Set<Room> inTree = new HashSet<>();
            inTree.add(rooms.get(0));
            while (inTree.size() < rooms.size()) {
                Edge bestEdge = null;
                double minDist = Double.MAX_VALUE;
                for (Room from : inTree) {
                    for (Room to : rooms) {
                        if (inTree.contains(to)) continue;
                        double dist = Math.sqrt(Math.pow(from.worldX - to.worldX, 2) + Math.pow(from.worldZ - to.worldZ, 2));
                        if (dist < minDist) {
                            minDist = dist;
                            bestEdge = new Edge(from, to, dist);
                        }
                    }
                }
                if (bestEdge == null) break;
                inTree.add(bestEdge.to);
                connectRooms(bestEdge.from, bestEdge.to);
            }
            // 额外边
            for (int i = 0; i < rooms.size() / 3; i++) {
                Room a = rooms.get(random.nextInt(rooms.size()));
                Room b = rooms.get(random.nextInt(rooms.size()));
                if (a == b) continue;
                boolean alreadyConnected = false;
                for (Door door : a.doors.values()) {
                    if (door.toRoom == b || door.fromRoom == b) {
                        alreadyConnected = true;
                        break;
                    }
                }
                if (!alreadyConnected && random.nextDouble() < 0.3) {
                    connectRooms(a, b);
                }
            }
        }

        private void connectRooms(Room a, Room b) {
            int dx = b.worldX - a.worldX;
            int dz = b.worldZ - a.worldZ;
            BlockFace faceA, faceB;
            if (Math.abs(dx) > Math.abs(dz)) {
                faceA = dx > 0 ? BlockFace.EAST : BlockFace.WEST;
                faceB = faceA.getOppositeFace();
            } else {
                faceA = dz > 0 ? BlockFace.SOUTH : BlockFace.NORTH;
                faceB = faceA.getOppositeFace();
            }
            Door door = new Door(faceA, null, null, a, b);
            a.doors.put(faceA, door);
            b.doors.put(faceB, door);
        }

        private void assignRoomTypes() {
            startRoom = rooms.get(0);
            startRoom.type = RoomType.EMPTY;
            for (Room room : rooms) {
                room.distanceFromStart = Math.abs(room.worldX - startRoom.worldX) + Math.abs(room.worldZ - startRoom.worldZ);
            }
            Room farthestRoom = startRoom;
            int maxDist = 0;
            for (Room room : rooms) {
                if (room.distanceFromStart > maxDist) {
                    maxDist = room.distanceFromStart;
                    farthestRoom = room;
                }
            }
            farthestRoom.type = RoomType.BOSS;

            List<Room> otherRooms = new ArrayList<>(rooms);
            otherRooms.remove(startRoom);
            otherRooms.remove(farthestRoom);
            Collections.shuffle(otherRooms, random);

            for (Room room : otherRooms) {
                double roll = random.nextDouble();
                if (roll < ENEMY_CHANCE) room.type = RoomType.ENEMY;
                else if (roll < ENEMY_CHANCE + ELITE_ENEMY_CHANCE) room.type = RoomType.ELITE_ENEMY;
                else if (roll < ENEMY_CHANCE + ELITE_ENEMY_CHANCE + TRAP_CHANCE) room.type = RoomType.TRAP;
                else if (roll < ENEMY_CHANCE + ELITE_ENEMY_CHANCE + TRAP_CHANCE + TREASURE_CHANCE) room.type = RoomType.TREASURE;
                else if (roll < ENEMY_CHANCE + ELITE_ENEMY_CHANCE + TRAP_CHANCE + TREASURE_CHANCE + RARE_TREASURE_CHANCE) room.type = RoomType.RARE_TREASURE;
                else if (roll < ENEMY_CHANCE + ELITE_ENEMY_CHANCE + TRAP_CHANCE + TREASURE_CHANCE + RARE_TREASURE_CHANCE + BUFF_CHANCE) room.type = RoomType.BUFF;
                else room.type = RoomType.ALTAR;
            }
        }

        public String checkBlocked() {
            for (int x = -dungeonSize/2; x < dungeonSize/2; x++) {
                for (int z = -dungeonSize/2; z < dungeonSize/2; z++) {
                    Block b = world.getBlockAt(centerX + x, centerY, centerZ + z);
                    if (b.getType() == Material.BEDROCK) {
                        return "Cannot build in bedrock area.";
                    }
                }
            }
            return null;
        }

        public void generateStartRoom() {
            startRoom.generate();
            player.teleport(new Location(world, startRoom.worldX + 0.5, centerY, startRoom.worldZ + 0.5));
        }

        public void expandFromDoor(Block block, Player player) {
            Location loc = block.getLocation();
            // 由于所有门方块都注册了，直接查找
            Door door = doorMap.get(loc);
            if (door == null) return;
            if (!door.toRoom.generated) {
                door.toRoom.generate();
            }
        }

        public boolean isDoorBlock(Block block) {
            return doorMap.containsKey(block.getLocation());
        }

        public boolean isAltarBlock(Block block) {
            return altars.contains(block.getLocation());
        }

        public void onAltarInteract(Player player, Block block) {
            Location loc = block.getLocation();
            Material type = block.getType();
            if (type == Material.BEACON) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 600, 1));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 600, 1));
                player.sendMessage(ChatColor.GREEN + "你获得了临时增益效果！");
                block.setType(Material.AIR);
                altars.remove(loc);
            } else if (type == Material.ENCHANTING_TABLE) {
                if (random.nextBoolean()) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 1200, 1));
                    player.sendMessage(ChatColor.GREEN + "祭坛赐予你力量！");
                } else {
                    player.damage(5.0);
                    player.sendMessage(ChatColor.RED + "祭坛对你造成了伤害...");
                }
                block.setType(Material.AIR);
                altars.remove(loc);
            }
        }

        public Monster getBoss() { return boss; }
        public boolean isCompleted() { return isCompleted; }
        public void complete(Player player) { isCompleted = true; }

        public void cleanup() {
            for (Room room : rooms) {
                if (room.generated) {
                    for (Block b : room.placedBlocks) {
                        b.setType(Material.AIR);
                    }
                    for (Entity e : room.spawnedEntities) {
                        if (e.isValid()) e.remove();
                    }
                }
            }
            doorMap.clear();
            altars.clear();
        }

        private Material[] getFloorMaterialsForFloor() {
            if (currentFloor <= 2) return FLOOR_MATERIALS_EARLY;
            else if (currentFloor <= 4) return FLOOR_MATERIALS_MID;
            else return FLOOR_MATERIALS_LATE;
        }

        private Material[] getWallMaterialsForFloor() {
            if (currentFloor <= 2) return WALL_MATERIALS_EARLY;
            else if (currentFloor <= 4) return WALL_MATERIALS_MID;
            else return WALL_MATERIALS_LATE;
        }
    }
}