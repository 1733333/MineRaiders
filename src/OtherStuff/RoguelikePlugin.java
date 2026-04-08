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
import org.bukkit.util.noise.SimplexNoiseGenerator;

import java.util.*;

// ======================== 核心逻辑类（不继承 JavaPlugin） ========================
public class RoguelikePlugin implements CommandExecutor, Listener {

    private final JavaPlugin plugin;          // 持有插件实例，用于调度等
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

    // ========================= 内部类：地牢生成器（无变动） =========================
    private static class DungeonGenerator {

        // ========== 可调参数（直接修改此处数值即可） ==========
        private static final int DUNGEON_RADIUS = 30;          // 地牢边界半径
        private static final int ROOM_SIZE = 3;                // 单个房间边长（建议奇数）
        private static final int DOOR_SIZE = 1;                // 铁门宽度（铁块数量）
        private static final double BIG_ROOM_CHANCE = 0.25;    // 生成大房间组合的概率
        private static final int BIG_ROOM_GRID = 2;            // 大房间由 N x N 个标准房间组成
        // ===================================================

        private final JavaPlugin plugin;
        private final Player player;
        private final World world;
        private final int centerX, centerY, centerZ;
        private final Random random = new Random();
        private final SimplexNoiseGenerator noise;

        private final List<Block> placedBlocks = new ArrayList<>();
        private final List<Entity> spawnedEntities = new ArrayList<>();
        private final Set<Location> generatedRoomCenters = new HashSet<>();
        private final Map<Location, BlockFace> doorFacingMap = new HashMap<>();

        private Zombie boss;
        private Block startChestBlock;
        private boolean bossSpawned = false;

        private enum RoomType { EMPTY, ENEMY, TRAP, TREASURE }

        public DungeonGenerator(JavaPlugin plugin, Player player) {
            this.plugin = plugin;
            this.player = player;
            this.world = player.getWorld();
            Location loc = player.getLocation();
            this.centerX = loc.getBlockX();
            this.centerY = loc.getBlockY();
            this.centerZ = loc.getBlockZ();
            this.noise = new SimplexNoiseGenerator(random.nextLong());
        }

        public boolean isDoorBlock(Block block) {
            return doorFacingMap.containsKey(block.getLocation());
        }

        public String checkBlocked() {
            int half = ROOM_SIZE / 2;
            for (int x = -half; x <= half; x++) {
                for (int z = -half; z <= half; z++) {
                    for (int y = 0; y < 5; y++) {
                        Block block = world.getBlockAt(centerX + x, centerY + y, centerZ + z);
                        if (!block.isEmpty() && !block.isLiquid()) {
                            return "Area blocked at " + block.getLocation().toVector();
                        }
                    }
                }
            }
            return null;
        }

        public void generateStartRoom() {
            startChestBlock = world.getBlockAt(centerX, centerY, centerZ);
            startChestBlock.setType(Material.BEDROCK);
            placedBlocks.add(startChestBlock);

            Location startCenter = new Location(world, centerX, centerY, centerZ);
            generatedRoomCenters.add(startCenter);
            placeRoom(centerX, centerY, centerZ, RoomType.EMPTY);
            player.sendMessage(ChatColor.GREEN + "起点房间已生成！右键铁门探索新区域。");
        }

        public void expandFromDoor(Block doorBlock, Player clicker) {
            Location doorLoc = doorBlock.getLocation();
            BlockFace facing = doorFacingMap.get(doorLoc);
            if (facing == null) return;

            int offset = (ROOM_SIZE / 2) + 1;
            int newCenterX = doorLoc.getBlockX() + facing.getModX() * (offset + ROOM_SIZE / 2);
            int newCenterZ = doorLoc.getBlockZ() + facing.getModZ() * (offset + ROOM_SIZE / 2);

            Location newCenter = new Location(world, newCenterX, centerY, newCenterZ);
            if (generatedRoomCenters.contains(newCenter)) {
                clicker.sendMessage(ChatColor.RED + "这个方向已经探索过了！");
                return;
            }

            if (Math.abs(newCenterX - centerX) > DUNGEON_RADIUS || Math.abs(newCenterZ - centerZ) > DUNGEON_RADIUS) {
                clicker.sendMessage(ChatColor.RED + "无法扩展：超出地牢边界。");
                return;
            }

            if (!canPlaceRoom(newCenterX, centerY, newCenterZ)) {
                clicker.sendMessage(ChatColor.RED + "目标位置被阻挡，无法生成房间。");
                return;
            }

            removeDoorBlocks(doorLoc, facing);

            boolean isBigRoom = random.nextDouble() < BIG_ROOM_CHANCE;
            if (isBigRoom) {
                generateBigRoom(newCenterX, centerY, newCenterZ, facing);
            } else {
                RoomType type = selectRoomType();
                placeRoom(newCenterX, centerY, newCenterZ, type);
                generatedRoomCenters.add(newCenter);
            }

            if (generatedRoomCenters.size() >= 5 && !bossSpawned) {
                spawnBossAt(newCenterX, centerY, newCenterZ);
                bossSpawned = true;
                clicker.sendMessage(ChatColor.RED + "你感受到了强大的敌意... Boss 出现了！");
            }

            clicker.sendMessage(ChatColor.GREEN + "铁门消失，新的区域出现了！");
        }

        private void removeDoorBlocks(Location doorLoc, BlockFace facing) {
            BlockFace perpendicular = getPerpendicular(facing);
            int half = DOOR_SIZE / 2;
            for (int i = -half; i <= half; i++) {
                Block b = world.getBlockAt(doorLoc.clone().add(perpendicular.getModX() * i, 0, perpendicular.getModZ() * i));
                if (b.getType() == Material.IRON_BLOCK) {
                    b.setType(Material.AIR);
                    placedBlocks.remove(b);
                    doorFacingMap.remove(b.getLocation());
                }
            }
        }

        private BlockFace getPerpendicular(BlockFace face) {
            return switch (face) {
                case NORTH, SOUTH -> BlockFace.EAST;
                case EAST, WEST -> BlockFace.NORTH;
                default -> BlockFace.NORTH;
            };
        }

        private boolean canPlaceRoom(int centerX, int y, int centerZ) {
            int half = ROOM_SIZE / 2;
            for (int dx = -half; dx <= half; dx++) {
                for (int dz = -half; dz <= half; dz++) {
                    for (int dy = 0; dy < 4; dy++) {
                        Block b = world.getBlockAt(centerX + dx, y + dy, centerZ + dz);
                        if (!b.isEmpty() && !b.isLiquid()) return false;
                    }
                }
            }
            return true;
        }

        private void generateBigRoom(int centerX, int y, int centerZ, BlockFace entranceFace) {
            int startX = centerX - (BIG_ROOM_GRID * ROOM_SIZE) / 2 + ROOM_SIZE / 2;
            int startZ = centerZ - (BIG_ROOM_GRID * ROOM_SIZE) / 2 + ROOM_SIZE / 2;
            for (int gx = 0; gx < BIG_ROOM_GRID; gx++) {
                for (int gz = 0; gz < BIG_ROOM_GRID; gz++) {
                    int rx = startX + gx * ROOM_SIZE;
                    int rz = startZ + gz * ROOM_SIZE;
                    Location roomCenter = new Location(world, rx, y, rz);
                    generatedRoomCenters.add(roomCenter);
                    RoomType type = selectRoomType();
                    placeRoom(rx, y, rz, type);
                }
            }
            player.sendMessage(ChatColor.LIGHT_PURPLE + "你发现了一个巨大的组合房间！");
        }

        private RoomType selectRoomType() {
            double r = random.nextDouble();
            if (r < 0.4) return RoomType.EMPTY;
            if (r < 0.7) return RoomType.ENEMY;
            if (r < 0.9) return RoomType.TRAP;
            return RoomType.TREASURE;
        }

        private void placeRoom(int x, int y, int z, RoomType type) {
            int half = ROOM_SIZE / 2;
            for (int dx = -half; dx <= half; dx++) {
                for (int dz = -half; dz <= half; dz++) {
                    Block floor = world.getBlockAt(x + dx, y, z + dz);
                    Block ceiling = world.getBlockAt(x + dx, y + 3, z + dz);
                    Material floorMat = getNoiseMaterial(x + dx, z + dz);
                    floor.setType(floorMat);
                    ceiling.setType(Material.STONE_BRICKS);
                    placedBlocks.add(floor);
                    placedBlocks.add(ceiling);
                }
            }

            if (ROOM_SIZE > 1) {
                Block torch = world.getBlockAt(x, y + 1, z);
                torch.setType(Material.REDSTONE_TORCH);
                placedBlocks.add(torch);
            }

            Location center = new Location(world, x + 0.5, y + 1, z + 0.5);
            switch (type) {
                case ENEMY -> spawnEnemies(center);
                case TRAP -> placeTrap(x, y, z);
                case TREASURE -> placeTreasureChest(x, y, z);
            }

            createIronDoors(x, y, z);
        }

        private Material getNoiseMaterial(int bx, int bz) {
            double n = noise.noise(bx * 0.1, bz * 0.1);
            if (n < -0.3) return Material.CRACKED_STONE_BRICKS;
            if (n < 0.0) return Material.MOSSY_STONE_BRICKS;
            if (n < 0.3) return Material.STONE_BRICKS;
            if (n < 0.6) return Material.ANDESITE;
            return Material.POLISHED_ANDESITE;
        }

        private void createIronDoors(int x, int y, int z) {
            BlockFace[] faces = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
            for (BlockFace face : faces) {
                if (random.nextDouble() > 0.5) continue;

                int doorX = x + face.getModX() * (ROOM_SIZE / 2 + 1);
                int doorZ = z + face.getModZ() * (ROOM_SIZE / 2 + 1);

                if (Math.abs(doorX - centerX) > DUNGEON_RADIUS || Math.abs(doorZ - centerZ) > DUNGEON_RADIUS) continue;

                BlockFace perpendicular = getPerpendicular(face);
                int half = DOOR_SIZE / 2;
                for (int i = -half; i <= half; i++) {
                    if (DOOR_SIZE % 2 == 0 && i == half) continue;
                    int bx = doorX + perpendicular.getModX() * i;
                    int bz = doorZ + perpendicular.getModZ() * i;
                    Block doorBlock = world.getBlockAt(bx, y, bz);
                    Block above = world.getBlockAt(bx, y + 1, bz);
                    if (!doorBlock.isEmpty() || !above.isEmpty()) continue;

                    doorBlock.setType(Material.IRON_BLOCK);
                    above.setType(Material.IRON_BLOCK);
                    placedBlocks.add(doorBlock);
                    placedBlocks.add(above);
                    doorFacingMap.put(doorBlock.getLocation(), face);
                    doorFacingMap.put(above.getLocation(), face);
                }
            }
        }

        private void spawnEnemies(Location center) {
            int count = random.nextInt(3) + 1;
            for (int i = 0; i < count; i++) {
                EntityType type = switch (random.nextInt(3)) {
                    case 0 -> EntityType.ZOMBIE;
                    case 1 -> EntityType.SKELETON;
                    default -> EntityType.SPIDER;
                };
                Entity e = world.spawnEntity(center.clone().add(random.nextDouble()-0.5, 0, random.nextDouble()-0.5), type);
                if (e instanceof Monster m) m.setRemoveWhenFarAway(false);
                spawnedEntities.add(e);
            }
        }

        private void placeTrap(int x, int y, int z) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (random.nextDouble() < 0.3) {
                        Block b = world.getBlockAt(x + dx, y, z + dz);
                        b.setType(Material.MAGMA_BLOCK);
                        placedBlocks.add(b);
                    }
                }
            }
        }

        private void placeTreasureChest(int x, int y, int z) {
            Block chestBlock = world.getBlockAt(x, y, z);
            chestBlock.setType(Material.CHEST);
            placedBlocks.add(chestBlock);
            if (chestBlock.getState() instanceof Chest chest) {
                chest.getInventory().addItem(generateReward());
            }
        }

        private ItemStack generateReward() {
            int r = random.nextInt(10);
            if (r < 3) return new ItemStack(Material.IRON_INGOT, random.nextInt(5)+1);
            if (r < 6) return new ItemStack(Material.GOLD_INGOT, random.nextInt(3)+1);
            if (r < 9) return new ItemStack(Material.DIAMOND, 1);
            return new ItemStack(Material.EMERALD, random.nextInt(3)+1);
        }

        private void spawnBossAt(int x, int y, int z) {
            Location loc = new Location(world, x + 0.5, y + 1, z + 0.5);
            boss = (Zombie) world.spawnEntity(loc, EntityType.ZOMBIE);
            boss.setCustomName(ChatColor.RED + "地牢守卫");
            boss.setCustomNameVisible(true);
            boss.setMaxHealth(80.0);
            boss.setHealth(80.0);
            boss.getEquipment().setItemInMainHand(new ItemStack(Material.IRON_SWORD));
            boss.getEquipment().setHelmet(new ItemStack(Material.IRON_HELMET));
            boss.getEquipment().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
            spawnedEntities.add(boss);
        }

        public void complete(Player killer) {
            startChestBlock.setType(Material.CHEST);
            if (startChestBlock.getState() instanceof Chest chest) {
                chest.getInventory().addItem(new ItemStack(Material.DIAMOND, 5));
            }
            killer.sendMessage(ChatColor.GOLD + "恭喜通关！获得 5 颗钻石！");
            plugin.getServer().getScheduler().runTaskLater(plugin, this::cleanup, 20L);
        }

        public void cleanup() {
            for (Entity e : spawnedEntities) {
                if (e != null && !e.isDead()) e.remove();
            }
            spawnedEntities.clear();
            for (Block b : placedBlocks) {
                b.setType(Material.AIR);
            }
            placedBlocks.clear();
            doorFacingMap.clear();
            if (player.isOnline()) {
                player.sendMessage(ChatColor.GRAY + "地牢已清理。");
            }
        }

        public Zombie getBoss() {
            return boss;
        }
    }
}