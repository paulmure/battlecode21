package ourplayer;

import java.util.ArrayList;

import battlecode.common.*;

public class Slanderer extends RobotPlayer implements RoleController {

    private int age;

    private MapLocation spawnEC;
    public MapLocation getSpawnEc(){
        // System.out.println("Passing EC..."+spawnEC);
        return spawnEC;
    }

    private int spawnECid;
    public int getSpawnEcId(){
        return spawnECid;
    }

    private double ecPassability;
    public double getEcPassability() {
        return ecPassability;
    }

    final int minRadius = 10;
    final double passabilityMultiplier = 55; // change this to change outer radius^2
    private double endRadius2;
    private double beginRadius2;

    public Slanderer() {
        age = 0;
        endRadius2 = 35;
        // change this to change the inner radius^2
        beginRadius2 = 4;
    }

    public void run() throws GameActionException {
        if (age == 0) {
            runFirstTurn();
        }

        MapLocation myLoc = rc.getLocation();
        RobotInfo closestEnemyMuck = null;
        int closestMuckDist = 1000;
        for (RobotInfo r : rc.senseNearbyRobots()) {
            if (r.type.equals(RobotType.MUCKRAKER) && !r.team.equals(rc.getTeam()) && 
                    chebyshevDistance(myLoc, r.location) < closestMuckDist) {
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
            Direction bestMove = getBestVortex(restrictPossibleMoves(getPossibleMoves(false, spawnEC)), 
            spawnEC, calculateTargetRadius2(), 0.0);
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


        // ((double) age * endRadius2) / 300

        // for (int x = -6; x <= 6; x++) {
        //     for (int y = -6; y <= 6; y++) {

        //         if (x * x + y * y <= 40 && x * x + y * y >= 32) {
        //             MapLocation thisLocation = new MapLocation(spawnEC.x+x, spawnEC.y+y);

        //             rc.setIndicatorDot(thisLocation, 50, 205, 50);
        //             // System.out.println("drawn dot at "+thisLocation);
                    
        //         }
        //     }
        // }
        // return ((double) age * endRadius2) / 300;
        return (((double) endRadius2 - beginRadius2)/300 * age) + beginRadius2;
    }

    private void runFirstTurn() throws GameActionException{
        // first turn stuff

        // set spawnEC
        RobotInfo[] nearby = rc.senseNearbyRobots();
        for (int i = 0; i < nearby.length; i++) {

            // This WILL break if there is more than one EC next to
            if (nearby[i].type == RobotType.ENLIGHTENMENT_CENTER) {
                spawnEC = nearby[i].location;
                spawnECid = nearby[i].ID;
            }
        }
        ecPassability = rc.sensePassability(spawnEC);
        endRadius2 = minRadius + ecPassability * passabilityMultiplier;
    }

}
