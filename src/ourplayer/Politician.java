package ourplayer;

import java.util.ArrayList;

import battlecode.common.*;

public class Politician extends RobotPlayer implements RoleController {

    final int minRadius = 16;
    final double passabilityMultiplier = 64;
    final double wideningMultiplier = 0.3; // 0.25
    final double wideningExponent = 1; // 0.93
    final double standingWeight = 0.5;
    private double maxRadius;
    private MapLocation spawnEC;
    private double ecPassability;
    private int spawnECid;
    private int age;
    MapLocation exploreLoc;
    int spawnRound;

    public Politician() throws GameActionException{
        spawnRound = rc.getRoundNum();
        maxRadius = 60;

        // set spawnEC

        if (spawnEC == null) {

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
            // System.out.println("Initialized Politician, ID: "+rc.getID()+" EC:
            // "+spawnEC);

        } 
        maxRadius = minRadius + ecPassability * passabilityMultiplier;
    }

    public Politician(MapLocation ec, int ecID, double ecP) throws GameActionException{
        // shut the fuck
        spawnEC = ec;

        spawnECid = ecID;
        ecPassability = ecP;
        // System.out.println("joe mama: " + ec);

        spawnRound = rc.getRoundNum();
        maxRadius = 60;

        // set spawnEC

        maxRadius = minRadius + ecPassability * passabilityMultiplier;
    }

    private double getTargetRadius() {
        if (rc.getRoundNum() < 300) {
            return minRadius + (maxRadius - minRadius) * rc.getRoundNum() / 300;
        } else {
            return maxRadius + Math.pow(age, wideningExponent) * ecPassability * wideningMultiplier;
        }
    }

    private boolean nearbyEnemy() {
        Team enemy = rc.getTeam().opponent();

        // change radius
        int actionRadius = rc.getType().actionRadiusSquared;
        RobotInfo[] attackable = rc.senseNearbyRobots(actionRadius, enemy);
        if (attackable.length != 0 && rc.canEmpower(actionRadius)) {
            return true;
        }
        return false;
    }

    public void run() throws GameActionException {
        RobotInfo[] nearbyRobots = rc.senseNearbyRobots();
        MapLocation myLoc = rc.getLocation();
        age = rc.getRoundNum() - spawnRound;

        if (rc.getRoundNum() <= 300) { // careful with this stuff
            age = 0;
        } else {
            age = rc.getRoundNum() - Math.max(spawnRound, 300);
        }

        RobotInfo closestEnemyMuck = null;
        int closestMuckDist = 1000;
        ArrayList<MapLocation> allies = new ArrayList<MapLocation>();
        for (RobotInfo r : nearbyRobots) {
            if (r.type.equals(RobotType.MUCKRAKER) && !r.team.equals(rc.getTeam())
                    && chebyshevDistance(myLoc, r.location) < closestMuckDist) {
                closestEnemyMuck = r;
                closestMuckDist = chebyshevDistance(myLoc, r.location);
            }
            if (r.team.equals(rc.getTeam()) && r.type.equals(RobotType.POLITICIAN)) {
                allies.add(r.location);
            }
        }

        if (closestEnemyMuck != null) {
            int d2toMuck = myLoc.distanceSquaredTo(closestEnemyMuck.location);
            if (d2toMuck <= 2 && rc.canEmpower(d2toMuck)) {// adjacent to
                rc.empower(d2toMuck);
            }
            tryMove(getRoughMoveTowards(closestEnemyMuck.location, 2));
        }

        Direction bestMove = null;
        if (spawnEC != null) {
            //if (rc.getRoundNum() < 300) {
                bestMove = getBestCloud(getPossibleMoves(false, spawnEC), spawnEC, getTargetRadius(), allies);
            // } else {
            //     bestMove = getBestVortex(getPossibleMoves(false, spawnEC), spawnEC, getTargetRadius(), standingWeight);
            // }
        }

        if (bestMove != null) {
            tryMove(bestMove);
        }
        // } else if (rc.getRoundNum() == 600) {
        // exploreLoc = new MapLocation(10 * (rc.getLocation().x - spawnEC.x) +
        // spawnEC.x,
        // 10 * (rc.getLocation().y - spawnEC.y) + spawnEC.y);
        // } else if (rc.getRoundNum() < 900) {
        // tryMove(getRoughMoveTowards(exploreLoc, 2));

        // detects if enemy is nearby
        // if(nearbyEnemy()){
        // rc.empower(rc.getType().actionRadiusSquared);

        // }

        // if (age >= 250){
        // if (Math.random() > 0.4 && rc.canEmpower(1)){
        // rc.empower(1);
        // }

        // }
    }

}
