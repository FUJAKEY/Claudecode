package com.schematicsbuilder.client;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;

/**
 * A* Pathfinding for smarter movement to chests
 */
public class SmartPathfinder {

    private static final int MAX_ITERATIONS = 1000;
    private static final int MAX_PATH_LENGTH = 100;

    /**
     * Find path from start to goal
     * Returns list of positions to walk through, or empty if no path
     */
    public static List<BlockPos> findPath(World world, BlockPos start, BlockPos goal) {
        if (start.equals(goal)) {
            return Collections.singletonList(goal);
        }

        // A* algorithm
        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingDouble(n -> n.fScore));
        Map<BlockPos, Node> allNodes = new HashMap<>();
        Set<BlockPos> closedSet = new HashSet<>();

        Node startNode = new Node(start, null, 0, heuristic(start, goal));
        openSet.add(startNode);
        allNodes.put(start, startNode);

        int iterations = 0;

        while (!openSet.isEmpty() && iterations < MAX_ITERATIONS) {
            iterations++;

            Node current = openSet.poll();

            if (current.pos.closerThan(goal, 2.0)) {
                // Found path - reconstruct
                return reconstructPath(current);
            }

            closedSet.add(current.pos);

            // Check neighbors
            for (BlockPos neighbor : getNeighbors(world, current.pos)) {
                if (closedSet.contains(neighbor))
                    continue;

                double tentativeG = current.gScore + 1;

                // Check if we can walk here
                if (!isWalkable(world, neighbor))
                    continue;

                Node neighborNode = allNodes.get(neighbor);

                if (neighborNode == null) {
                    neighborNode = new Node(neighbor, current, tentativeG, heuristic(neighbor, goal));
                    allNodes.put(neighbor, neighborNode);
                    openSet.add(neighborNode);
                } else if (tentativeG < neighborNode.gScore) {
                    // Found better path
                    openSet.remove(neighborNode);
                    neighborNode.parent = current;
                    neighborNode.gScore = tentativeG;
                    neighborNode.fScore = tentativeG + heuristic(neighbor, goal);
                    openSet.add(neighborNode);
                }
            }
        }

        // No path found - return direct path as fallback
        return Collections.singletonList(goal);
    }

    private static double heuristic(BlockPos a, BlockPos b) {
        // Manhattan distance
        return Math.abs(a.getX() - b.getX()) +
                Math.abs(a.getY() - b.getY()) +
                Math.abs(a.getZ() - b.getZ());
    }

    private static List<BlockPos> getNeighbors(World world, BlockPos pos) {
        List<BlockPos> neighbors = new ArrayList<>();

        // Horizontal movement
        neighbors.add(pos.north());
        neighbors.add(pos.south());
        neighbors.add(pos.east());
        neighbors.add(pos.west());

        // Diagonals
        neighbors.add(pos.north().east());
        neighbors.add(pos.north().west());
        neighbors.add(pos.south().east());
        neighbors.add(pos.south().west());

        // Up/Down (stairs, ladders)
        neighbors.add(pos.above());
        neighbors.add(pos.below());

        // Jump up movement
        neighbors.add(pos.north().above());
        neighbors.add(pos.south().above());
        neighbors.add(pos.east().above());
        neighbors.add(pos.west().above());

        return neighbors;
    }

    private static boolean isWalkable(World world, BlockPos pos) {
        // Check if we can stand here
        // Need: solid ground below, air at feet and head level

        boolean groundSolid = !world.getBlockState(pos.below()).isAir();
        boolean feetClear = world.getBlockState(pos).isAir() ||
                world.getBlockState(pos).getMaterial().isReplaceable();
        boolean headClear = world.getBlockState(pos.above()).isAir() ||
                world.getBlockState(pos.above()).getMaterial().isReplaceable();

        // Water is walkable (swimming)
        if (world.getBlockState(pos).getMaterial().isLiquid()) {
            feetClear = true;
        }

        return groundSolid && feetClear && headClear;
    }

    private static List<BlockPos> reconstructPath(Node endNode) {
        List<BlockPos> path = new ArrayList<>();
        Node current = endNode;

        while (current != null && path.size() < MAX_PATH_LENGTH) {
            path.add(0, current.pos);
            current = current.parent;
        }

        // Simplify path - remove unnecessary waypoints
        return simplifyPath(path);
    }

    private static List<BlockPos> simplifyPath(List<BlockPos> path) {
        if (path.size() <= 2)
            return path;

        List<BlockPos> simplified = new ArrayList<>();
        simplified.add(path.get(0));

        for (int i = 1; i < path.size() - 1; i++) {
            BlockPos prev = path.get(i - 1);
            BlockPos curr = path.get(i);
            BlockPos next = path.get(i + 1);

            // Check if direction changes
            int dx1 = curr.getX() - prev.getX();
            int dz1 = curr.getZ() - prev.getZ();
            int dx2 = next.getX() - curr.getX();
            int dz2 = next.getZ() - curr.getZ();

            if (dx1 != dx2 || dz1 != dz2 || curr.getY() != prev.getY()) {
                simplified.add(curr);
            }
        }

        simplified.add(path.get(path.size() - 1));
        return simplified;
    }

    /**
     * A* Node
     */
    private static class Node {
        final BlockPos pos;
        Node parent;
        double gScore;
        double fScore;

        Node(BlockPos pos, Node parent, double gScore, double hScore) {
            this.pos = pos;
            this.parent = parent;
            this.gScore = gScore;
            this.fScore = gScore + hScore;
        }
    }
}
