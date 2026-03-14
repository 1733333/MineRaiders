package Commands;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Switch;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DoorManager implements CommandExecutor, Listener {
    private final JavaPlugin plugin;
    private final Map<String, DoorData> doors = new HashMap<>();
    private final File doorsFile;          // 改为不立即初始化
    private FileConfiguration doorsConfig;

    private final File buttonsFile;         // 改为不立即初始化
    private FileConfiguration buttonsConfig;
    private final Map<Location, String> buttonToDoor = new HashMap<>();
    private final Map<Player, String> bindingPlayers = new HashMap<>();

    public DoorManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.doorsFile = new File(plugin.getDataFolder(), "doors.yml");
        this.buttonsFile = new File(plugin.getDataFolder(), "buttons.yml");
        loadDoors();
        loadButtons(); // 加载按钮绑定数据
    }

    private void loadDoors() {
        if (!doorsFile.exists()) {
            try {
                doorsFile.getParentFile().mkdirs();
                doorsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().warning("无法创建 doors.yml: " + e.getMessage());
            }
        }
        doorsConfig = YamlConfiguration.loadConfiguration(doorsFile);
        for (String id : doorsConfig.getKeys(false)) {
            String worldName = doorsConfig.getString(id + ".world");
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                plugin.getLogger().warning("世界 " + worldName + " 不存在，跳过门 " + id);
                continue;
            }
            int minX = doorsConfig.getInt(id + ".minX");
            int maxX = doorsConfig.getInt(id + ".maxX");
            int minY = doorsConfig.getInt(id + ".minY");
            int maxY = doorsConfig.getInt(id + ".maxY");
            int minZ = doorsConfig.getInt(id + ".minZ");
            int maxZ = doorsConfig.getInt(id + ".maxZ");
            boolean open = doorsConfig.getBoolean(id + ".open");
            DoorData data = new DoorData(world, minX, maxX, minY, maxY, minZ, maxZ);
            data.setOpen(open);
            doors.put(id, data);
        }
        plugin.getLogger().info("已加载 " + doors.size() + " 个门");
    }

    private void saveDoors() {
        doorsConfig = new YamlConfiguration();
        for (Map.Entry<String, DoorData> entry : doors.entrySet()) {
            String id = entry.getKey();
            DoorData data = entry.getValue();
            doorsConfig.set(id + ".world", data.getWorld().getName());
            doorsConfig.set(id + ".minX", data.getMinX());
            doorsConfig.set(id + ".maxX", data.getMaxX());
            doorsConfig.set(id + ".minY", data.getMinY());
            doorsConfig.set(id + ".maxY", data.getMaxY());
            doorsConfig.set(id + ".minZ", data.getMinZ());
            doorsConfig.set(id + ".maxZ", data.getMaxZ());
            doorsConfig.set(id + ".open", data.isOpen());
        }
        try {
            doorsConfig.save(doorsFile);
        } catch (IOException e) {
            plugin.getLogger().warning("保存门数据失败: " + e.getMessage());
        }
    }

    private void loadButtons() {
        if (!buttonsFile.exists()) {
            try {
                buttonsFile.getParentFile().mkdirs();
                buttonsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().warning("无法创建 buttons.yml: " + e.getMessage());
            }
        }
        buttonsConfig = YamlConfiguration.loadConfiguration(buttonsFile);
        for (String key : buttonsConfig.getKeys(false)) {
            String[] parts = key.split(",");
            if (parts.length != 4) continue;
            try {
                World world = Bukkit.getWorld(parts[0]);
                if (world == null) continue;
                int x = Integer.parseInt(parts[1]);
                int y = Integer.parseInt(parts[2]);
                int z = Integer.parseInt(parts[3]);
                Location loc = new Location(world, x, y, z);
                String doorId = buttonsConfig.getString(key);
                if (doorId != null && doors.containsKey(doorId)) {
                    buttonToDoor.put(loc, doorId);
                }
            } catch (NumberFormatException ignored) {}
        }
        plugin.getLogger().info("已加载 " + buttonToDoor.size() + " 个按钮绑定");
    }

    private void saveButtons() {
        buttonsConfig = new YamlConfiguration();
        for (Map.Entry<Location, String> entry : buttonToDoor.entrySet()) {
            Location loc = entry.getKey();
            String key = loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
            buttonsConfig.set(key, entry.getValue());
        }
        try {
            buttonsConfig.save(buttonsFile);
        } catch (IOException e) {
            plugin.getLogger().warning("保存按钮绑定失败: " + e.getMessage());
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length < 1) {
            sendHelp(sender);
            return true;
        }
        String sub = args[0].toLowerCase();
        return switch (sub) {
            case "create" -> createDoor(sender, args);
            case "toggle" -> toggleDoor(sender, args);
            case "remove" -> removeDoor(sender, args);
            case "bind" -> bindButton(sender, args);
            case "unbind" -> unbindButton(sender, args);
            default -> {
                sender.sendMessage("未知子命令，可用: create, toggle, remove, bind, unbind");
                yield false;
            }
        };
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "===== 门插件指令帮助 =====");
        sender.sendMessage(ChatColor.YELLOW + "/door create <id> <x1> <y1> <z1> <x2> <y2> <z2>");
        sender.sendMessage(ChatColor.GRAY + "  创建一扇新门，填充铁块。");
        sender.sendMessage(ChatColor.YELLOW + "/door toggle <id>");
        sender.sendMessage(ChatColor.GRAY + "  开关指定ID的门。开门从下往上消失，关门从上往下出现。");
        sender.sendMessage(ChatColor.YELLOW + "/door remove <id>");
        sender.sendMessage(ChatColor.GRAY + "  删除指定ID的门，清除所有铁块。");
        sender.sendMessage(ChatColor.YELLOW + "/door bind <id>");
        sender.sendMessage(ChatColor.GRAY + "  进入绑定模式，手持物品右键点击按钮，将其绑定到该门。");
        sender.sendMessage(ChatColor.YELLOW + "/door unbind");
        sender.sendMessage(ChatColor.GRAY + "  手持物品右键点击按钮，解除其绑定。");
        sender.sendMessage(ChatColor.GOLD + "===========================");
    }

    private boolean createDoor(CommandSender sender, String[] args) {
        if (args.length != 8) {
            sender.sendMessage("用法: /door create <id> <x1> <y1> <z1> <x2> <y2> <z2>");
            return false;
        }
        String id = args[1];
        if (doors.containsKey(id)) {
            sender.sendMessage("ID已存在，请换一个");
            return false;
        }
        try {
            int x1 = Integer.parseInt(args[2]);
            int y1 = Integer.parseInt(args[3]);
            int z1 = Integer.parseInt(args[4]);
            int x2 = Integer.parseInt(args[5]);
            int y2 = Integer.parseInt(args[6]);
            int z2 = Integer.parseInt(args[7]);

            int minX = Math.min(x1, x2);
            int maxX = Math.max(x1, x2);
            int minY = Math.min(y1, y2);
            int maxY = Math.max(y1, y2);
            int minZ = Math.min(z1, z2);
            int maxZ = Math.max(z1, z2);

            if ((maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1) > 500) {
                sender.sendMessage("门太大，请限制在500方块内");
                return true;
            }

            World world;
            if (sender instanceof Player) {
                world = ((Player) sender).getWorld();
            } else {
                world = Bukkit.getWorlds().get(0);
            }

            // 填充铁块（门初始关闭）
            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        world.getBlockAt(x, y, z).setType(Material.POLISHED_TUFF_WALL);
                    }
                }
            }

            doors.put(id, new DoorData(world, minX, maxX, minY, maxY, minZ, maxZ));
            saveDoors();
            sender.sendMessage("门已创建，ID: " + id);
            return true;
        } catch (NumberFormatException e) {
            sender.sendMessage("坐标必须是整数");
            return false;
        }
    }

    private boolean toggleDoor(CommandSender sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage("用法: /door toggle <id>");
            return false;
        }
        String id = args[1];
        DoorData data = doors.get(id);
        if (data == null) {
            sender.sendMessage("门不存在");
            return false;
        }
        performToggle(id, data);
        return true;
    }

    // 实际开关门逻辑（供按钮调用）
    private void performToggle(String id, DoorData data) {
        if (data.isAnimating()) return;

        data.setAnimating(true);
        World world = data.getWorld();
        int minX = data.getMinX(), maxX = data.getMaxX();
        int minY = data.getMinY(), maxY = data.getMaxY();
        int minZ = data.getMinZ(), maxZ = data.getMaxZ();
        boolean isOpen = data.isOpen(); // true: 门存在（关），false: 门消失（开）
        boolean newOpen = !isOpen;

        // 音效
        Location center = new Location(world, (minX + maxX) / 2.0, (minY + maxY) / 2.0, (minZ + maxZ) / 2.0);
        Sound sound = isOpen ? Sound.BLOCK_IRON_DOOR_OPEN : Sound.BLOCK_IRON_DOOR_CLOSE;
        world.playSound(center, sound, 1.0f, 1.0f);
        int layers = maxY - minY + 1;

        BukkitRunnable animation = new BukkitRunnable() {
            int tick = 0;
            int processedLayers = 0;

            @Override
            public void run() {
                tick++;
                int targetLayer = (int) Math.ceil((double) tick * layers / 20);
                if (targetLayer > layers) targetLayer = layers;

                if (isOpen) { // 开门：从下往上消失
                    for (int layer = processedLayers; layer < targetLayer; layer++) {
                        int y = minY + layer;
                        if(y == minY){
                            for (int i = 0; i < 10; i++) {
                                double x = minX + Math.random() * (maxX - minX + 1);
                                double z = minZ + Math.random() * (maxZ - minZ + 1);
                                world.spawnParticle(Particle.CLOUD, x, minY, z, 5, 0.3, 0.3, 0.3, 0.02);
                            }
                        }
                        for (int x = minX; x <= maxX; x++) {
                            for (int z = minZ; z <= maxZ; z++) {
                                world.getBlockAt(x, y, z).setType(Material.AIR);
                            }
                        }
                    }
                } else { // 关门：从上往下出现
                    for (int layer = processedLayers; layer < targetLayer; layer++) {
                        int y = maxY - layer;
                        if(y == minY){
                            for (int i = 0; i < 10; i++) {
                                double x = minX + Math.random() * (maxX - minX + 1);
                                double z = minZ + Math.random() * (maxZ - minZ + 1);
                                world.spawnParticle(Particle.CLOUD, x, minY, z, 5, 0.3, 0.3, 0.3, 0.02);
                            }
                        }
                        for (int x = minX; x <= maxX; x++) {
                            for (int z = minZ; z <= maxZ; z++) {
                                world.getBlockAt(x, y, z).setType(Material.POLISHED_TUFF_WALL);
                            }
                        }
                    }
                }
                processedLayers = targetLayer;

                if (tick >= 20) {
                    // 确保完成
                    if (isOpen) {
                        for (int layer = processedLayers; layer < layers; layer++) {
                            int y = minY + layer;
                            for (int x = minX; x <= maxX; x++) {
                                for (int z = minZ; z <= maxZ; z++) {
                                    world.getBlockAt(x, y, z).setType(Material.AIR);
                                }
                            }
                        }
                    } else {
                        for (int layer = processedLayers; layer < layers; layer++) {
                            int y = maxY - layer;
                            for (int x = minX; x <= maxX; x++) {
                                for (int z = minZ; z <= maxZ; z++) {
                                    world.getBlockAt(x, y, z).setType(Material.POLISHED_TUFF_WALL);
                                }
                            }
                        }
                    }
                    data.setOpen(newOpen);
                    data.setAnimating(false);
                    data.setTask(null);
                    saveDoors();
                    cancel();
                }
            }
        };
        BukkitTask task = animation.runTaskTimer(plugin, 0L, 1L);
        data.setTask(task);
    }

    private boolean removeDoor(CommandSender sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage("用法: /door remove <id>");
            return false;
        }
        String id = args[1];
        DoorData data = doors.remove(id);
        if (data == null) {
            sender.sendMessage("门不存在");
            return false;
        }

        if (data.getTask() != null) {
            data.getTask().cancel();
        }

        World world = data.getWorld();
        int minX = data.getMinX(), maxX = data.getMaxX();
        int minY = data.getMinY(), maxY = data.getMaxY();
        int minZ = data.getMinZ(), maxZ = data.getMaxZ();

        // 清除该区域所有铁块
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    world.getBlockAt(x, y, z).setType(Material.AIR);
                }
            }
        }

        // 移除所有绑定到该门的按钮
        buttonToDoor.entrySet().removeIf(entry -> entry.getValue().equals(id));
        saveButtons();

        saveDoors();
        sender.sendMessage("门已删除，ID: " + id);
        return true;
    }

    private boolean bindButton(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("只有玩家可以使用此命令");
            return false;
        }
        if (args.length != 2) {
            sender.sendMessage("用法: /door bind <id>");
            return false;
        }
        String id = args[1];
        if (!doors.containsKey(id)) {
            sender.sendMessage("门不存在");
            return false;
        }
        Player p = (Player) sender;
        bindingPlayers.put(p, id);
        p.sendMessage(ChatColor.GREEN + "请右键点击一个按钮将其绑定到门 " + id);
        return true;
    }

    private boolean unbindButton(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("只有玩家可以使用此命令");
            return false;
        }
        Player p = (Player) sender;
        bindingPlayers.put(p, null); // null 表示解除绑定模式
        p.sendMessage(ChatColor.GREEN + "请右键点击一个按钮以解除其绑定");
        return true;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = e.getClickedBlock();
        if (block == null || !(block.getBlockData() instanceof Switch)) return; // 必须是按钮

        Player p = e.getPlayer();
        Location loc = block.getLocation();

        // 处理绑定模式
        if (bindingPlayers.containsKey(p)) {
            e.setCancelled(true); // 阻止按钮正常触发
            String doorId = bindingPlayers.remove(p);
            if (doorId == null) { // 解绑模式
                if (buttonToDoor.remove(loc) != null) {
                    p.sendMessage(ChatColor.GREEN + "按钮绑定已解除");
                    saveButtons();
                } else {
                    p.sendMessage(ChatColor.RED + "该按钮未绑定任何门");
                }
            } else { // 绑定模式
                if (buttonToDoor.containsKey(loc)) {
                    p.sendMessage(ChatColor.RED + "该按钮已被绑定，请先解除");
                } else {
                    buttonToDoor.put(loc, doorId);
                    p.sendMessage(ChatColor.GREEN + "按钮已绑定到门 " + doorId);
                    saveButtons();
                }
            }
            return;
        }

        // 正常按钮点击：查找绑定的门并触发
        String doorId = buttonToDoor.get(loc);
        if (doorId == null) return; // 未绑定，忽略

        DoorData data = doors.get(doorId);
        if (data == null) {
            // 门已不存在，清理无效绑定
            buttonToDoor.remove(loc);
            saveButtons();
            return;
        }

        if (data.isAnimating()) {
            p.sendMessage(ChatColor.RED + "门正在移动，请稍后");
            return;
        }

        // 触发开关
        performToggle(doorId, data);
        // 可选：播放按钮点击音效
        p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
    }
    private static class DoorData {
        private final World world;
        private final int minX, maxX, minY, maxY, minZ, maxZ;
        private boolean open; // true: 门存在（关），false: 门消失（开）
        private boolean animating;
        private BukkitTask task;

        public DoorData(World world, int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
            this.world = world;
            this.minX = minX; this.maxX = maxX;
            this.minY = minY; this.maxY = maxY;
            this.minZ = minZ; this.maxZ = maxZ;
            this.open = true; // 默认门是关闭的（存在）
            this.animating = false;
            this.task = null;
        }

        public World getWorld() { return world; }
        public int getMinX() { return minX; }
        public int getMaxX() { return maxX; }
        public int getMinY() { return minY; }
        public int getMaxY() { return maxY; }
        public int getMinZ() { return minZ; }
        public int getMaxZ() { return maxZ; }
        public boolean isOpen() { return open; }
        public void setOpen(boolean open) { this.open = open; }
        public boolean isAnimating() { return animating; }
        public void setAnimating(boolean animating) { this.animating = animating; }
        public BukkitTask getTask() { return task; }
        public void setTask(BukkitTask task) { this.task = task; }
    }
}
