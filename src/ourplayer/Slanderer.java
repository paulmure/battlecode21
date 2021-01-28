package ourplayer;

import java.util.ArrayList;

import battlecode.common.*;

public class Slanderer extends RobotPlayer implements RoleController {

    private int age;
    private MapLocation spawnEC = null;

    public MapLocation getSpawnEc() {
        // System.out.println("Passing EC..."+spawnEC);
        return spawnEC;
    }

    private int spawnECid;

    public int getSpawnEcId() {
        return spawnECid;
    }

    private double ecPassability;

    public double getEcPassability() {
        return ecPassability;
    }

    final int minRadius = 5;
    final int initialEndRadius2 = 6;
    final double passabilityMultiplier = 45; // change this to change outer radius^2
    private double endRadius2 = 35;
    private double beginRadius2 = 8;
    int[] walls = { -1, -1, -1, -1 }; // N, E, S, W

    public Slanderer() throws GameActionException {
        age = 0;

        // change this to change the inner radius^2

        // set spawnEC
        RobotInfo[] nearby = rc.senseNearbyRobots();
        for (int i = 0; i < nearby.length; i++) {

            // This WILL break if there is more than one EC next to
            if (nearby[i].type == RobotType.ENLIGHTENMENT_CENTER && (spawnEC == null || nearby[i].location
                    .distanceSquaredTo(rc.getLocation()) < spawnEC.distanceSquaredTo(rc.getLocation()))) {
                spawnEC = nearby[i].location;
                spawnECid = nearby[i].ID;
            }
        }
        ecPassability = rc.sensePassability(spawnEC);
        endRadius2 = minRadius + ecPassability * passabilityMultiplier;
        rc.setFlag(420); //this is funny because its the weed number
    }

    public void run() throws GameActionException {
        MapLocation myLoc = rc.getLocation();

        for (int i = 0; i < 4; ++i) {
            if (walls[i] == -1) {
                MapLocation loc = null;
                Direction awayFromWall = Direction.CENTER;
                switch (i) {
                    case 0: // NORTH
                        loc = rc.getLocation().translate(0, 4);
                        awayFromWall = Direction.SOUTH;
                        break;
                    case 1: // EAST
                        loc = rc.getLocation().translate(4, 0);
                        awayFromWall = Direction.WEST;
                        break;
                    case 2: // SOUTH
                        loc = rc.getLocation().translate(0, -4);
                        awayFromWall = Direction.NORTH;
                        break;
                    case 3: // WEST
                        loc = rc.getLocation().translate(-4, 0);
                        awayFromWall = Direction.EAST;
                }

                if (!rc.onTheMap(loc)) {
                    while (!rc.onTheMap(loc.add(awayFromWall))) {
                        loc = loc.add(awayFromWall);
                    }
                    walls[i] = i % 2 == 0 ? loc.y : loc.x;
                    // flagsToSend.add(new FlagInfo(true, rc.getTeam(), loc, spawnEC, i * 2));//
                    // hacky lmao
                    // System.out.println("Found wall at " + loc);
                }
            }
        }


        RobotInfo closestEnemyMuck = null;
        int closestMuckDist = 1000;
        for (RobotInfo r : rc.senseNearbyRobots()) {
            if (r.type.equals(RobotType.MUCKRAKER) && !r.team.equals(rc.getTeam())
                    && chebyshevDistance(myLoc, r.location) < closestMuckDist) {
                closestEnemyMuck = r;
                closestMuckDist = chebyshevDistance(myLoc, r.location);
            }
        }

        if (closestEnemyMuck != null) {
            Direction runDirection = Direction.CENTER;
            int runDist = closestMuckDist;
            for (Direction d : getPossibleMoves()) {
                int dist = myLoc.add(d).distanceSquaredTo(closestEnemyMuck.location);
                if (dist > runDist) {
                    runDist = dist;
                    runDirection = d;
                }
            }
            tryMove(runDirection);
        } else {
            Direction bestMove = getBestVortex(restrictPossibleMoves(getPossibleMoves(true, spawnEC)), spawnEC,
                    calculateTargetRadius2(), 0.4);
            if (bestMove != null) {
                tryMove(bestMove);
            }
        }
        age++;
    }

    private ArrayList<Direction> restrictPossibleMoves(ArrayList<Direction> moves) {
        ArrayList<Direction> possibleMoves = new ArrayList<>();
        MapLocation myLoc = rc.getLocation();
        for (Direction d : moves) {
            MapLocation testLoc = myLoc.add(d);
            if (testLoc.distanceSquaredTo(spawnEC) <= endRadius2 && testLoc.add(d).x != walls[1] && testLoc.add(d).x != walls[3] 
                    && testLoc.add(d).y != walls[0] && testLoc.add(d).y != walls[2]){
                possibleMoves.add(d);
            }
        }
        return possibleMoves;
    }

    private double calculateTargetRadius2() {
        double currentEndRadius2 = endRadius2;
        if (rc.getRoundNum() < 600) {
            currentEndRadius2 = ((double) endRadius2 - initialEndRadius2) / 600 * rc.getRoundNum() + initialEndRadius2;
        }
        return (((double) currentEndRadius2 - beginRadius2) / 300 * age) + beginRadius2;
    }
}
