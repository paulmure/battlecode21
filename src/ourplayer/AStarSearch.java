package ourplayer;

import battlecode.common.*;
import java.util.*;
import ourplayer.utils.LinkedListPQ;

public strictfp class AStarSearch extends RobotPlayer {

    private static final int[] neighborsOffset = { 1, 14, 13, 12, -1, -14, -13, -12 };
    private static final Direction[] directions = { Direction.NORTH, Direction.NORTHEAST, Direction.EAST,
            Direction.SOUTHEAST, Direction.SOUTH, Direction.SOUTHWEST, Direction.WEST, Direction.NORTHWEST, };

    private final int stateSpace = 13 * 13;
    private MapLocation[] locs;
    private LinkedListPQ.Node[] nodes;

    private double[] gScores;
    private int[] prev;
    private boolean[] visited;
    private final MapLocation source;
    private final MapLocation target;
    private final int sourceIdx = 84;
    private final int targetIdx;

    private final int locToIdx(MapLocation loc) {
        return 13 * (loc.x - source.x + 6) + (loc.y - source.y + 6);
    }

    public AStarSearch(MapLocation target) {
        source = rc.getLocation();

        this.target = target;
        targetIdx = locToIdx(target);

        locs = new MapLocation[stateSpace];
        gScores = new double[stateSpace];
        prev = new int[stateSpace];
        visited = new boolean[stateSpace];
        nodes = new LinkedListPQ.Node[stateSpace];

        for (int i = 1; i < stateSpace; ++i) {
            gScores[i] = Integer.MAX_VALUE;
            prev[i] = Integer.MAX_VALUE;
        }

        locs[sourceIdx] = source;
        gScores[sourceIdx] = 0;
    }

    private final double heuristics(MapLocation a, MapLocation b) {
        int dx = Math.abs(a.x - b.x);
        int dy = Math.abs(a.y - b.y);
        int chebyshev = Math.max(dx, dy);
        return chebyshev;
        // int euclidean = dx * dx + dy * dy;
        // return (chebyshev + euclidean * 0.00001) / 0.752;
    }

    public ArrayList<Direction> findPath() throws GameActionException {
        LinkedListPQ frontier = new LinkedListPQ();

        LinkedListPQ.Node sourceNode = frontier.push(0, sourceIdx);
        nodes[sourceIdx] = sourceNode;

        int current = 0;
        int counter = 0;
        while (!frontier.isEmpty()) {
            counter++;

            current = frontier.pop();
            System.out.println("entering " + (locs[current].x - source.x) + ", " + (locs[current].y - source.y)
                    + " with fscore " + nodes[current].key);
            // frontier.print();
            if (current == targetIdx) {
                System.out.println("total loop: " + counter);
                return reconstructPath(current);
            }
            visited[current] = true;
            nodes[current] = null;

            int[] neighbors = getNeighbors(locs[current]);

            double tentativeGScore = gScores[current] + (1 / rc.sensePassability(locs[current]));

            for (int neighbor : neighbors) {
                if (visited[neighbor] || neighbor == -1 || !rc.canSenseLocation(locs[neighbor]))
                    continue;

                if (tentativeGScore < gScores[neighbor]) {
                    gScores[neighbor] = tentativeGScore;
                    prev[neighbor] = current;

                    double fScore = gScores[neighbor] + heuristics(locs[neighbor], target);
                    if (nodes[neighbor] == null) {
                        nodes[neighbor] = frontier.push(fScore, neighbor);
                    } else {
                        frontier.decreaseKey(nodes[neighbor], fScore);
                    }
                }
            }
        }

        return null;
    }

    private final ArrayList<Direction> reconstructPath(int currentIdx) {
        ArrayList<Direction> res = new ArrayList<Direction>(stateSpace / 4);
        MapLocation currentLoc = locs[currentIdx];
        while (currentIdx != sourceIdx) {
            rc.setIndicatorDot(currentLoc, 0, 255, 0);
            int prevIdx = prev[currentIdx];
            MapLocation prevLoc = locs[prevIdx];
            res.add(0, prevLoc.directionTo(currentLoc));
            currentIdx = prevIdx;
            currentLoc = prevLoc;
        }
        return res;
    }

    private final int[] getNeighbors(MapLocation center) {
        int[] res = new int[8];
        int centerIdx = locToIdx(center);

        int offset;
        int neighborIdx;

        for (int j = 0; j < 8; ++j) {
            offset = neighborsOffset[j];
            neighborIdx = centerIdx + offset;

            if (neighborIdx < 0 || neighborIdx >= stateSpace) {
                res[j] = -1;
                continue;
            }
            if (locs[neighborIdx] == null) {
                locs[neighborIdx] = center.add(directions[j]);
            }
            res[j] = neighborIdx;
        }

        return res;
    }

}