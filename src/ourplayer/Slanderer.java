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
    final double passabilityMultiplier = 55; // change this to change outer radius^2
    private double endRadius2 = 35;
    private double beginRadius2 = 4;

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
    }

    public void run() throws GameActionException {
        MapLocation myLoc = rc.getLocation();
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
            Direction bestMove = getBestVortex(restrictPossibleMoves(getPossibleMoves(false, spawnEC)), spawnEC,
                    calculateTargetRadius2(), 0.0);
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
            if (testLoc.distanceSquaredTo(spawnEC) <= endRadius2) {
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
