package ourplayer;

import battlecode.common.*;
import java.util.*;
import ourplayer.utils.Vector;

public strictfp class AStarSearch extends RobotPlayer {

    private static final int[] neighborsOffset = { 1, 14, 13, 12, -1, -14, -13, -12 };
    private static final Direction[] directions = { Direction.NORTH, Direction.NORTHEAST, Direction.EAST, Direction.SOUTHEAST,
            Direction.SOUTH, Direction.SOUTHWEST, Direction.WEST, Direction.NORTHWEST, };

    private final int stateSpace = 13 * 13;
    private MapLocation[] locs;

    private int[] gScores;
    private int[] fScores;
    private int[] prev;
    private boolean[] visited;
    private boolean[] inFrontier;
    private final MapLocation source;
    private final MapLocation target;
    private final int sourceIdx = 84;
    private final int targetIdx;

    private int locToIdx(MapLocation loc) {
        return 13 * (loc.x - source.x + 6) + (loc.y - source.y + 6);
    }

    public AStarSearch(MapLocation target) {
        source = rc.getLocation();

        this.target = target;
        targetIdx = locToIdx(target);

        locs = new MapLocation[stateSpace];
        gScores = new int[stateSpace];
        fScores = new int[stateSpace];
        prev = new int[stateSpace];
        visited = new boolean[stateSpace];
        inFrontier = new boolean[stateSpace];

        for (int i = 1; i < stateSpace; ++i) {
            gScores[i] = Integer.MAX_VALUE;
            fScores[i] = Integer.MAX_VALUE;
            prev[i] = Integer.MAX_VALUE;
        }

        locs[locToIdx(source)] = source;
        gScores[0] = 0;
        fScores[0] = 0;
    }

    private static int heuristics(MapLocation a, MapLocation b) {
        return Math.max(Math.abs(a.x - b.x), Math.abs(a.y - b.y));
    }

    public ArrayList<Direction> findPath() throws GameActionException{
        Vector frontier = new Vector(stateSpace);

        frontier.append(locToIdx(source));
        inFrontier[locToIdx(source)] = true;

        int counter = 0;

        int current = 0;
        int currentIdx = 0;
        while (frontier.size > 0) {
            int bytecodeBefore = Clock.getBytecodeNum();

            int bb = Clock.getBytecodeNum();
            current = frontier.arr[0];
            currentIdx = 0;
            for (int i = 1; i < frontier.size; ++i) {
                int j = frontier.arr[i];
                if(fScores[j] < fScores[current]) {
                    current = j;
                    currentIdx = i;
                }
            }
            int _diff = Clock.getBytecodeNum() - bb;
            System.out.printf("t h e l o o p t o o k    %d bytecode\n", _diff);

            if (current == targetIdx) {
                System.out.println("found path!!!!!!!!!");
                return reconstructPath(current);
            }
            visited[current] = true;
            inFrontier[current] = false;
            frontier.remove(currentIdx);

            int[] neighbors = getNeighbors(locs[current]);

            int tentativeGScore = gScores[current] + (int) (1 / rc.sensePassability(locs[current]));

            int diff = Clock.getBytecodeNum() - bytecodeBefore;
            System.out.printf("Iteration %d, bytecode used in first half: %d\n", counter, diff);

            bytecodeBefore = Clock.getBytecodeNum();

            for (int neighbor : neighbors) {
                if (visited[neighbor] || neighbor == -1 || !rc.canSenseLocation(locs[neighbor])) continue;

                if (tentativeGScore < gScores[neighbor]) {
                    gScores[neighbor] = tentativeGScore;
                    fScores[neighbor] = gScores[neighbor] + heuristics(locs[neighbor], target);
                    prev[neighbor] = current;
                    
                    if (!inFrontier[neighbor]) {
                        frontier.append(neighbor);
                        inFrontier[neighbor] = true;
                    }
                }
            }

            diff = Clock.getBytecodeNum() - bytecodeBefore;
            System.out.printf("Iteration %d, bytecode used in second half: %d\n", counter, diff);
            ++counter;
        }

        return null;
    }

    private ArrayList<Direction> reconstructPath(int currentIdx) {
        System.out.println("reconstructing path");
        ArrayList<Direction> res = new ArrayList<Direction>(stateSpace / 4);
        MapLocation currentLoc = locs[currentIdx];
        while (currentIdx != sourceIdx) {
            rc.setIndicatorDot(currentLoc, 0, 255, 0);
            int prevIdx = prev[currentIdx];
            System.out.printf("currentIdx = %d, prevIdx = %d, targetIdx = %d\n", currentIdx, prevIdx, targetIdx);
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