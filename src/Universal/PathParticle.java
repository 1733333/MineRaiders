package Universal;

import MineRaiders.MRD;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class PathParticle {

    /**
     * 使用 A* 算法寻找路径，并沿着路径逐步显示粒子。
     *
     * @param plugin            插件实例（用于调度任务）
     * @param start             起点位置
     * @param end               终点位置
     * @param particle          粒子类型（例如 Particle.FLAME）
     * @param delayBetweenSteps 每显示一个粒子之间的间隔（单位：tick）
     * @param maxIterations     寻路最大迭代次数（防止卡死）
     */
    public static void showPathWithAStar(Plugin plugin, Location start, Location end,
                                         Particle particle, long delayBetweenSteps,
                                         int maxIterations) {
        // 第一步：寻找路径
        List<Location> path = findPath(start, end, maxIterations);
        if (path.isEmpty()) {
            // 路径不存在，可在此处添加日志或提示
            return;
        }

        // 第二步：沿着路径逐步显示粒子
        new BukkitRunnable() {
            int index = 0;

            @Override
            public void run() {
                if (index >= path.size()) {
                    this.cancel();
                    return;
                }
                Location loc = path.get(index);
                loc.getWorld().spawnParticle(particle, loc, 1, 0, 0, 0, 0);
                loc.getWorld().playSound(loc, Sound.ENTITY_ENDER_EYE_DEATH, 1f, 1.0f);
                index++;
            }
        }.runTaskTimer(plugin, 0, delayBetweenSteps);
    }

    // ---------- 以下为 A* 寻路算法实现 ----------

    /**
     * 判断一个位置是否可通行（非固体且非危险方块，可根据需要修改）
     */
    private static boolean isPassable(Location loc) {
        Block block = loc.getBlock();
        // 如果方块是固体（如石头、泥土等），则不可通行
        if (block.getType() != Material.AIR){
            MRD mrd = (MRD) Bukkit.getPluginManager().getPlugin("MineRaidersDoor");
            if(mrd.isLocationInDoor(loc)){
                return true;
            }else return !block.getType().isSolid();
        }
        // 可根据需要添加更多条件，例如水、岩浆等
        return true;
    }

    /**
     * A* 寻路算法，返回从 start 到 end 的路径点列表（包含起点和终点）。
     * 如果找不到路径，返回空列表。
     */
    private static List<Location> findPath(Location start, Location end, int maxIterations) {
        World world = start.getWorld();
        if (!world.equals(end.getWorld())) return Collections.emptyList();

        // 使用字符串作为键（x,y,z）加快查找
        Set<String> closedSet = new HashSet<>();
        Map<String, Node> openSetMap = new HashMap<>();
        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingDouble(n -> n.f));

        Node startNode = new Node(start);
        Node endNode = new Node(end);
        startNode.g = 0;
        startNode.h = heuristic(startNode, endNode);
        startNode.f = startNode.g + startNode.h;
        openSet.add(startNode);
        openSetMap.put(startNode.key, startNode);

        int iterations = 0;
        while (!openSet.isEmpty() && iterations < maxIterations) {
            Node current = openSet.poll();
            if (current.key.equals(endNode.key)) {
                // 重建路径
                List<Location> path = new ArrayList<>();
                Node node = current;
                while (node != null) {
                    path.add(node.loc);
                    node = node.parent;
                }
                Collections.reverse(path);
                return path;
            }

            closedSet.add(current.key);
            openSetMap.remove(current.key);

            // 遍历邻居（6个方向：上下左右前后，可扩展为斜向）
            for (Location neighborLoc : getNeighbors(current.loc)) {
                if (!isPassable(neighborLoc)) continue;

                String neighborKey = key(neighborLoc);
                if (closedSet.contains(neighborKey)) continue;

                Node neighbor = openSetMap.get(neighborKey);
                double tentativeG = current.g + distance(current.loc, neighborLoc);

                if (neighbor == null) {
                    neighbor = new Node(neighborLoc);
                    neighbor.g = tentativeG;
                    neighbor.h = heuristic(neighbor, endNode);
                    neighbor.f = neighbor.g + neighbor.h;
                    neighbor.parent = current;
                    openSet.add(neighbor);
                    openSetMap.put(neighborKey, neighbor);
                } else if (tentativeG < neighbor.g) {
                    neighbor.g = tentativeG;
                    neighbor.f = neighbor.g + neighbor.h;
                    neighbor.parent = current;
                    // 由于优先级队列不自动更新，需要重新添加
                    openSet.remove(neighbor);
                    openSet.add(neighbor);
                }
            }
            iterations++;
        }
        return Collections.emptyList();
    }

    /**
     * 获取一个位置周围的邻居（六方向）
     */
    private static List<Location> getNeighbors(Location loc) {
        List<Location> neighbors = new ArrayList<>(6);
        World world = loc.getWorld();
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();
        neighbors.add(new Location(world, x + 1, y, z));
        neighbors.add(new Location(world, x - 1, y, z));
        neighbors.add(new Location(world, x, y + 1, z));
        neighbors.add(new Location(world, x, y - 1, z));
        neighbors.add(new Location(world, x, y, z + 1));
        neighbors.add(new Location(world, x, y, z - 1));
        return neighbors;
    }

    /**
     * 曼哈顿距离启发函数（可改为欧几里得）
     */
    private static double heuristic(Node a, Node b) {
        return Math.abs(a.loc.getX() - b.loc.getX()) +
                Math.abs(a.loc.getY() - b.loc.getY()) +
                Math.abs(a.loc.getZ() - b.loc.getZ());
    }

    /**
     * 欧几里得距离（用于计算实际移动代价）
     */
    private static double distance(Location a, Location b) {
        return a.distance(b);
    }

    private static String key(Location loc) {
        return loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
    }

    /**
     * 节点类，用于 A* 算法
     */
    private static class Node {
        Location loc;
        String key;
        Node parent;
        double g; // 从起点到当前节点的实际代价
        double h; // 启发式估计代价
        double f; // g + h

        Node(Location loc) {
            this.loc = loc.clone();
            this.key = key(loc);
        }
    }
}