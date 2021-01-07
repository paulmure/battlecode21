package ourplayer;

import battlecode.common.*;
import java.util.*;

public strictfp class PathFinder extends RobotPlayer {
    private static int heuristics(MapLocation a, MapLocation b) {
        return Math.max(Math.abs(a.x - b.x), Math.abs(a.y - b.y));
    }

    public static ArrayList<MapLocation> AStar(RobotController rc, MapLocation target) {
        MapLocation start = rc.getLocation();
        HashSet<MapLocation> explored = new HashSet<MapLocation>();
        HashMap<MapLocation, Integer> gScores = new HashMap<MapLocation, Integer>();
        gScores.put(start, 0);

        // priority queue with custom compare function
        PriorityQueue<MapLocation> queue = new PriorityQueue<MapLocation>(10,
            new Comparator<MapLocation>() {
                public int compare(MapLocation i, MapLocation j) {
                    int iScore = heuristics(i, target) + gScores.getOrDefault(i, Integer.MAX_VALUE);
                    int jScore = heuristics(j, target) + gScores.getOrDefault(j, Integer.MAX_VALUE);

                    if (iScore > jScore) {
                        return 1;
                    }

                    if (iScore < jScore) {
                        return -1;
                    }

                    return 0;
                }
            });
        queue.add(start);

        HashMap<MapLocation, MapLocation> prev = new HashMap<MapLocation, MapLocation>();
        
        while (!queue.isEmpty()) {
            MapLocation current = queue.poll();
            if (current.equals(target)) {
                break;
            }

            for (Direction dir: directions) {
                MapLocation neighbor = current.add(dir);

                if (!explored.contains(neighbor) && rc.canSenseLocation(neighbor)) {
                    int tentativeGScore;
                    try {
                        tentativeGScore = (int) (1 / rc.sensePassability(neighbor)) + gScores.get(current);
                    } catch (GameActionException e) {
                        continue;
                    }

                    if (tentativeGScore < gScores.getOrDefault(neighbor, Integer.MAX_VALUE)) {
                        prev.put(current, neighbor);

                        // might have to remove then add back if already in queue
                        if (!queue.contains(neighbor)) {
                            queue.add(neighbor);
                        }
                    }
                }
            }
        }

        ArrayList<MapLocation> res = new ArrayList<MapLocation>();
        MapLocation current = start;
        while (!current.equals(target)) {
            res.add(current);
            if (prev.containsKey(current)) {
                current = prev.get(current);
            } else {
                return null;
            }
        }
        res.add(target);
        return res;
    }

    public static void main(String[] args) {

    }
}