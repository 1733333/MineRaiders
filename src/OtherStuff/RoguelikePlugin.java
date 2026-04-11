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
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
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
 * 完全重构后的完整插件，整合了所有核心逻辑
 */
public class RoguelikePlugin implements CommandExecutor, Listener {
    private final JavaPlugin plugin;
    private DungeonManager activeDungeon; // 当前激活的地牢实例，使用新的管理类

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
            player.sendMessage(ChatColor.GREEN + "你已进入生存模式，并获得初始装备：铁剑、牛排和治疗药水！");

            // 生成地牢
            DungeonManager generator = new DungeonManager(plugin, player);
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
                player.sendMessage(ChatColor.GREEN + "地牢已清理完成！");
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
     * 处理Boss死亡事件
     */
    @EventHandler
    public void onBossDeath(EntityDeathEvent event) {
        if (activeDungeon == null || activeDungeon.isCompleted()) return;
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

    // ==================== 内部类：地牢核心管理类（重构后的完整逻辑） ====================
    private static class DungeonManager {
        // ========== 核心配置参数（可根据需求调整） ==========
        private static final int GRID_SIZE = 5;              // 地牢网格大小（房间数量，更大的网格带来更长的冒险）
        private static final int ROOM_SIZE = 13;             // 单房间尺寸（方块数）
        private static final int DOOR_WIDTH = 7;             // 门宽度
        private static final int DOOR_HEIGHT = 4;            // 门高度
        private static final int WALL_HEIGHT = 6;            // 墙壁高度
        private static final double ENEMY_CHANCE = 0.30;     // 普通怪物房间概率
        private static final double ELITE_ENEMY_CHANCE = 0.10;// 精英怪物房间概率
        private static final double TRAP_CHANCE = 0.15;      // 陷阱房间概率
        private static final double TREASURE_CHANCE = 0.20;  // 宝藏房间概率
        private static final double RARE_TREASURE_CHANCE = 0.05; // 稀有宝藏房间概率
        private static final double BUFF_CHANCE = 0.12;      // 增益房间概率
        private static final double ALTAR_CHANCE = 0.08;     // 祭坛房间概率

        // 地板材质列表（随机噪声选择）
        private static final Material[] FLOOR_MATERIALS = {
                Material.STONE_BRICKS, Material.CRACKED_STONE_BRICKS,
                Material.MOSSY_STONE_BRICKS, Material.SMOOTH_STONE,
                Material.ANDESITE, Material.DIORITE, Material.GRANITE,
                Material.DEEPSLATE_BRICKS
        };

        // 墙壁材质列表（随机噪声选择）
        private static final Material[] WALL_MATERIALS = {
                Material.STONE_BRICKS, Material.MOSSY_STONE_BRICKS,
                Material.CHISELED_STONE_BRICKS, Material.POLISHED_ANDESITE,
                Material.POLISHED_DIORITE, Material.POLISHED_GRANITE,
                Material.POLISHED_DEEPSLATE
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

        private Monster boss;          // Boss实体
        private boolean bossSpawned = false; // Boss是否已生成
        private boolean isCompleted = false; // 地牢是否已完成

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
            final int distanceFromStart; // 距离起点的曼哈顿距离，用于难度缩放

            Room(int gridX, int gridZ, RoomType type) {
                this.gridX = gridX;
                this.gridZ = gridZ;
                this.type = type;
                this.worldX = centerX + gridX * ROOM_SIZE;
                this.worldZ = centerZ + gridZ * ROOM_SIZE;
                this.generated = false;
                // 计算距离起点的距离，用于难度缩放
                this.distanceFromStart = Math.abs(gridX) + Math.abs(gridZ);
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
                int count = isElite ? random.nextInt(2) + 2 : random.nextInt(3) + 1; // 精英房间更多怪物

                for (int i = 0; i < count; i++) {
                    // 随机怪物类型，精英房间有更高概率出强力怪物
                    EntityType type;
                    if (isElite) {
                        type = switch (random.nextInt(4)) {
                            case 0 -> EntityType.ZOMBIE;
                            case 1 -> EntityType.SKELETON;
                            case 2 -> EntityType.SPIDER;
                            default -> EntityType.EVOKER; // 精英房间会出唤魔者
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

                        // 根据房间距离给怪物加装备和属性（难度缩放）
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
             * 给怪物应用装备和属性，根据等级和是否精英
             */
            private void applyMonsterEquipment(Monster monster, int level, boolean isElite) {
                // 提升生命
                double health = 20.0 + level * 5.0;
                if (isElite) health *= 1.5;
                monster.getAttribute(Attribute.MAX_HEALTH).setBaseValue(health);
                monster.setHealth(health);

                // 提升攻击伤害
                double damage = 3.0 + level * 1.0;
                if (isElite) damage *= 1.3;
                monster.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(damage);

                // 精英怪物加药水效果
                if (isElite) {
                    monster.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, PotionEffect.INFINITE_DURATION, 0));
                    monster.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, PotionEffect.INFINITE_DURATION, 0));
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
                    monster.getEquipment().setItemInMainHandDropChance(0.1f); // 低概率掉落
                }
            }

            /**
             * 放置高级陷阱（岩浆块+隐藏压力板+发射器）
             */
            private void placeAdvancedTrap(int roomX, int roomZ, int floorY) {
                int half = ROOM_SIZE / 2;

                // 1. 岩浆块陷阱
                for (int dx = -half + 1; dx <= half - 1; dx++) {
                    for (int dz = -half + 1; dz <= half - 1; dz++) {
                        if (random.nextDouble() < 0.35) { // 35%概率生成岩浆块
                            Block b = world.getBlockAt(roomX + dx, floorY, roomZ + dz);
                            b.setType(Material.MAGMA_BLOCK);
                            placedBlocks.add(b);
                        }
                    }
                }

                // 2. 隐藏压力板陷阱
                if (random.nextBoolean()) {
                    int px = roomX + random.nextInt(half*2) - half;
                    int pz = roomZ + random.nextInt(half*2) - half;
                    Block plate = world.getBlockAt(px, floorY, pz);
                    plate.setType(Material.STONE_PRESSURE_PLATE);
                    placedBlocks.add(plate);

                    // 在压力板后面放发射器
                    Block dispenser = world.getBlockAt(px, floorY + 1, pz);
                    dispenser.setType(Material.DISPENSER);
                    placedBlocks.add(dispenser);

                    // 填充发射器的箭
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
             * 放置宝藏箱（支持普通/稀有）
             */
            private void placeTreasureChest(int roomX, int roomZ, int floorY, boolean isRare) {
                Block chestBlock = world.getBlockAt(roomX, floorY, roomZ);
                chestBlock.setType(Material.CHEST);
                placedBlocks.add(chestBlock);

                // 填充宝箱奖励
                if (chestBlock.getState() instanceof Chest chest) {
                    if (isRare) {
                        // 稀有宝藏：附魔装备、药水、钻石
                        chest.getInventory().addItem(generateRareReward());
                        chest.getInventory().addItem(new ItemStack(Material.DIAMOND, random.nextInt(3) + 2));
                        chest.getInventory().addItem(new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 1));
                    } else {
                        // 普通宝藏：基础资源
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
                // 随机附魔武器
                ItemStack weapon = new ItemStack(Material.DIAMOND_SWORD);
                weapon.addEnchantment(Enchantment.SHARPNESS, 3);
                weapon.addEnchantment(Enchantment.LOOTING, 2);
                ItemMeta meta = weapon.getItemMeta();
                meta.setDisplayName(ChatColor.AQUA + "地牢征服者之剑");
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                weapon.setItemMeta(meta);
                return weapon;
            }

            /**
             * 给玩家添加高级增益（比原来更强，持续更久）
             */
            private void applyAdvancedBuffToPlayer(Player target, Location center) {
                // 更长的持续时间，更强的效果
                target.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60 * 20, 2));    // 速度III（60秒）
                target.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 60 * 20, 1)); // 力量II（60秒）
                target.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 30 * 20, 1)); // 生命恢复II（30秒）
                target.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 45 * 20, 0)); // 抗性I（45秒）

                target.sendMessage(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "✨ 你进入了祝福圣殿！获得了强大的祝福效果！");

                // 播放特效
                world.playSound(center, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                world.spawnParticle(Particle.HAPPY_VILLAGER, center, 50, 0.5, 0.5, 0.5, 0.1);
            }

            /**
             * 放置祭坛（祭坛房间）
             */
            private void placeAltar(int roomX, int roomZ, int floorY) {
                Block altar = world.getBlockAt(roomX, floorY, roomZ);
                altar.setType(Material.NETHERITE_BLOCK); // 用下界合金块代替原来的金块，更稀有
                placedBlocks.add(altar);
                altars.add(altar.getLocation());

                // 提示玩家
                player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "⛩ 你发现了神秘的命运祭坛！右键它来赌上你的命运...");

                // 播放特效
                world.playSound(altar.getLocation().add(0.5, 0.5, 0.5), Sound.BLOCK_BEACON_ACTIVATE, 0.8f, 1.2f);
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

        // ========== DungeonManager 核心方法 ==========
        /**
         * 构造地牢管理器
         * @param plugin 主插件实例
         * @param player 进入地牢的玩家
         */
        public DungeonManager(JavaPlugin plugin, Player player) {
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
         * 生成地牢布局（随机游走 + 分支，保证Boss在最远点）
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

                // 尝试向随机方向移动，优先向Boss方向移动
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
                    if (visited[i][j] && r.nextDouble() < 0.4) { // 更高的分支概率，更多选择
                        for (int[] dir : new int[][]{{0, 1}, {0, -1}, {1, 0}, {-1, 0}}) {
                            int ni = i + dir[0];
                            int nj = j + dir[1];
                            if (ni >= 0 && ni < GRID_SIZE && nj >= 0 && nj < GRID_SIZE && !visited[ni][nj] && r.nextDouble() < 0.6) {
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
         * 随机选择房间类型（按概率权重）
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
         * 获取路径材质
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

        // ========== 对外暴露的接口方法（主类调用） ==========

        /**
         * 检查地牢区域是否被阻挡
         * @return 错误信息，如果没问题返回null
         */
        public String checkBlocked() {
            int floorY = centerY - 1;
            for (int i = 0; i < GRID_SIZE; i++) {
                for (int j = 0; j < GRID_SIZE; j++) {
                    if (!layout[i][j]) continue;
                    int worldX = centerX + i * ROOM_SIZE;
                    int worldZ = centerZ + j * ROOM_SIZE;

                    // 检查3x3的区域是否有阻挡
                    for (int dx = -ROOM_SIZE/2; dx <= ROOM_SIZE/2; dx++) {
                        for (int dz = -ROOM_SIZE/2; dz <= ROOM_SIZE/2; dz++) {
                            for (int h = 0; h <= WALL_HEIGHT; h++) {
                                Block b = world.getBlockAt(worldX + dx, floorY + h, worldZ + dz);
                                if (!b.isEmpty() && !b.isLiquid()) {
                                    return "区域被阻挡: " + b.getType();
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
            rooms[0][0].generate();
            player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "🏰 地牢已开启！探索房间，击败Boss来通关！");
            player.sendMessage(ChatColor.GRAY + "提示：点击铁门来解锁并进入新的房间，输入/roguelike clean可以手动清理地牢");
        }

        /**
         * 检查方块是否是门方块
         */
        public boolean isDoorBlock(Block block) {
            return doorMap.containsKey(block.getLocation());
        }

        /**
         * 从门扩展地牢
         */
        public void expandFromDoor(Block block, Player player) {
            Door door = doorMap.get(block.getLocation());
            if (door == null) return;

            Room targetRoom = rooms[door.toX][door.toZ];
            if (targetRoom != null && !targetRoom.generated) {
                targetRoom.generate();
            }

            // 移除门方块，让玩家可以通过
            for (Location loc : door.blocks) {
                world.getBlockAt(loc).setType(Material.AIR);
                doorMap.remove(loc);
            }

            player.sendMessage(ChatColor.YELLOW + "你打开了一扇门，新的区域展现在你面前...");
        }

        /**
         * 检查方块是否是祭坛方块
         */
        public boolean isAltarBlock(Block block) {
            return altars.contains(block.getLocation());
        }

        /**
         * 处理祭坛交互
         */
        public void onAltarInteract(Player player, Block block) {
            altars.remove(block.getLocation());

            // 命运随机：70%好结果，30%坏结果
            double roll = random.nextDouble();
            if (roll < 0.7) {
                // 好结果
                if (roll < 0.3) {
                    // 永久生命提升
                    player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(player.getAttribute(Attribute.MAX_HEALTH).getValue() + 2.0);
                    player.setHealth(player.getHealth() + 2.0);
                    player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "✨ 祭坛赐予你力量！你的最大生命值永久提升了2点！");
                } else if (roll < 0.5) {
                    // 满生命+饥饿
                    player.setHealth(player.getMaxHealth());
                    player.setFoodLevel(20);
                    player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "💚 祭坛治愈了你的所有伤势！");
                } else {
                    // 强力临时buff
                    player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 120 * 20, 2));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 120 * 20, 1));
                    player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "💪 祭坛赐予你战斗祝福！2分钟的强力增益！");
                }

                world.playSound(block.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 1.0f);
                world.spawnParticle(Particle.TOTEM_OF_UNDYING, block.getLocation().add(0.5, 0.5, 0.5), 30, 0.5, 0.5, 0.5, 0.1);
            } else {
                // 坏结果
                if (roll < 0.85) {
                    // 扣血
                    player.damage(6.0);
                    player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "💔 祭坛的力量反噬了你！你受到了6点伤害！");
                } else {
                    // 负面效果
                    player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 30 * 20, 1));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 30 * 20, 1));
                    player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "☠ 祭坛诅咒了你！30秒的虚弱与缓慢！");
                }

                world.playSound(block.getLocation(), Sound.ENTITY_WITHER_HURT, 1.0f, 1.0f);
                world.spawnParticle(Particle.SQUID_INK, block.getLocation().add(0.5, 0.5, 0.5), 30, 0.5, 0.5, 0.5, 0.1);
            }

            // 移除祭坛方块
            block.setType(Material.AIR);
        }

        /**
         * 获取Boss实体
         */
        public Monster getBoss() {
            return boss;
        }

        /**
         * 生成Boss
         */
        private void spawnBossAt(Room room) {
            Location center = new Location(world, room.worldX + 0.5, centerY, room.worldZ + 0.5);

            // 生成Boss：变异僵尸守卫
            Zombie zombie = (Zombie) world.spawnEntity(center, EntityType.ZOMBIE);
            zombie.setBaby(false);
            zombie.setCustomName(ChatColor.RED + "" + ChatColor.BOLD + "地牢守护者");
            zombie.setCustomNameVisible(true);

            // Boss属性
            zombie.getAttribute(Attribute.MAX_HEALTH).setBaseValue(150.0);
            zombie.setHealth(150.0);
            zombie.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(8.0);
            zombie.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0.35);
            zombie.getAttribute(Attribute.KNOCKBACK_RESISTANCE).setBaseValue(0.8);

            // Boss装备
            ItemStack bossSword = new ItemStack(Material.NETHERITE_SWORD);
            bossSword.addEnchantment(Enchantment.SHARPNESS, 5);
            bossSword.addEnchantment(Enchantment.FIRE_ASPECT, 2);
            bossSword.addEnchantment(Enchantment.SWEEPING_EDGE, 3);
            ItemMeta meta = bossSword.getItemMeta();
            meta.setDisplayName(ChatColor.RED + "守护者的巨剑");
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            bossSword.setItemMeta(meta);

            zombie.getEquipment().setItemInMainHand(bossSword);
            zombie.getEquipment().setItemInMainHandDropChance(1.0f); // 必掉武器

            ItemStack bossHelmet = new ItemStack(Material.NETHERITE_HELMET);
            bossHelmet.addEnchantment(Enchantment.PROTECTION, 4);
            zombie.getEquipment().setHelmet(bossHelmet);

            // Boss永久buff
            zombie.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, PotionEffect.INFINITE_DURATION, 1));
            zombie.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, PotionEffect.INFINITE_DURATION, 1));
            zombie.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, PotionEffect.INFINITE_DURATION, 0));

            this.boss = zombie;
            room.spawnedEntities.add(zombie);
        }

        /**
         * 完成地牢
         */
        public void complete(Player killer) {
            if (isCompleted) return;
            isCompleted = true;

            // 给玩家通关奖励
            killer.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "🎉 恭喜你！你击败了地牢守护者，通关了地牢！");
            killer.sendMessage(ChatColor.GRAY + "地牢正在清理中...");

            // 播放通关特效
            world.playSound(killer.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
            world.spawnParticle(Particle.FIREWORK, killer.getLocation(), 100, 1, 1, 1, 0.5);

            // 给玩家经验奖励
            killer.giveExpLevels(10);

            // 自动清理地牢
            cleanup();
        }

        /**
         * 清理整个地牢
         */
        public void cleanup() {
            // 清理所有房间
            for (int i = 0; i < GRID_SIZE; i++) {
                for (int j = 0; j < GRID_SIZE; j++) {
                    if (rooms[i][j] != null) {
                        rooms[i][j].cleanup();
                    }
                }
            }

            // 清理Boss
            if (boss != null && !boss.isDead()) {
                boss.remove();
            }

            // 清空映射
            doorMap.clear();
            altars.clear();
        }

        /**
         * 检查地牢是否已完成
         */
        public boolean isCompleted() {
            return isCompleted;
        }
    }
}
