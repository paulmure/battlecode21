package ourplayer;

import battlecode.common.*;
import java.util.*;

public strictfp class AStarSearch extends RobotPlayer {

    private int stateSpace;

    // to avoid having to implement my own keyed priority queue,
    // I did this
    // this is bad !!!!!!
    private HashMap<MapLocation, Integer> locToIdx;
    private MapLocation[] locs;
    private int highestIdx = 0;

    private int[] gScores;
    private int[] fScores;
    private int[] prev;
    private boolean[] visited;
    private MapLocation source;
    private MapLocation target;

    public AStarSearch(MapLocation target) {
        this.target = target;
        source = rc.getLocation();
        int width = 2 * heuristics(source, target) + 1;
        stateSpace = width * width * 2;
        locToIdx = new HashMap<MapLocation, Integer>();
        locs = new MapLocation[stateSpace];
        
        gScores = new int[stateSpace];
        fScores = new int[stateSpace];
        prev = new int[stateSpace];
        visited = new boolean[stateSpace];

        for (int i = 1; i < stateSpace; ++i) {
            gScores[i] = Integer.MAX_VALUE;
            fScores[i] = Integer.MAX_VALUE;
            prev[i] = Integer.MAX_VALUE;
        }

        locs[highestIdx] = source;
        locToIdx.put(source, highestIdx++);
        gScores[0] = 0;
        fScores[0] = 0;
    }

    private static int heuristics(MapLocation a, MapLocation b) {
        return Math.max(Math.abs(a.x - b.x), Math.abs(a.y - b.y));
    }

    public ArrayList<Direction> findPath() {
        PriorityQueue<Integer> frontier = new PriorityQueue<Integer>((i, j) -> {
            return fScores[i] > fScores[j] ? 1 : fScores[i] < fScores[j] ? -1 : 0;
        });

        // put source into frontier
        frontier.add(0);

        while (!frontier.isEmpty()) {
            int current = frontier.poll();
            if (locs[current] == target) {
                System.out.println("found path");
                return reconstructPath(current);
            }
            visited[current] = true;

            for (int neighbor: getNeighbors(locs[current])) {
                if (visited[neighbor] || !rc.canSenseLocation(locs[neighbor])) continue;

                double passability;
                try {
                    passability = rc.sensePassability(locs[neighbor]);
                } catch (GameActionException e) {
                    continue;
                }
                int tentativeGScore = gScores[current] + (int) (1 / passability);
                if (tentativeGScore < gScores[neighbor]) {
                    gScores[neighbor] = tentativeGScore;
                    fScores[neighbor] = gScores[neighbor] + heuristics(locs[neighbor], target);
                    prev[neighbor] = current;
                    
                    if (frontier.contains(neighbor)) {
                        frontier.remove(neighbor);
                    }
                    frontier.add(neighbor);
                }
            }
        }

        return null;
    }

    private ArrayList<Direction> reconstructPath(int currentIdx) {
        System.out.println("reconstructing path");
        ArrayList<Direction> res = new ArrayList<Direction>(stateSpace / 4);
        MapLocation currentLoc = locs[currentIdx];
        while (currentIdx != 0) {
            // this is bad
            rc.setIndicatorDot(currentLoc, 0, 255, 0);
            int prevIdx = prev[currentIdx];
            MapLocation prevLoc = locs[prevIdx];
            res.add(0, prevLoc.directionTo(currentLoc));
            currentIdx = prevIdx;
            currentLoc = prevLoc;
        }
        System.out.println(res);
        return res;
    }

    private int[] getNeighbors(MapLocation center) {
        int[] res = new int[8];
        int i = 0;
        for (Direction dir: directions) {
            MapLocation neighbor = center.add(dir);
            if (!locToIdx.containsKey(neighbor)) {
                if (highestIdx >= stateSpace) continue;
                locs[highestIdx] = neighbor;
                locToIdx.put(neighbor, highestIdx++);
            }
            res[i++] = locToIdx.get(neighbor);
        }
        return Arrays.copyOfRange(res, 0, i);
    }

}