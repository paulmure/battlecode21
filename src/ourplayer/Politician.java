package ourplayer;

import battlecode.common.*;

public class Politician extends RobotPlayer implements RoleController {

    private double targetRadius;
    private MapLocation spawnEC;
    private int spawnECid;
    private int age;
    MapLocation exploreLoc;

    public Politician() {
        age = 0;
        targetRadius = 60;
    }

    public Politician(MapLocation ec, int ecID) {
        this();
        // shut the fuck
        spawnEC = ec;

        spawnECid = ecID;
        // System.out.println("joe mama: " + ec);
    }

    private void runFirstTurn() {
        // first turn stuff
        // set spawnEC

        if (spawnEC == null) {

            RobotInfo[] nearby = rc.senseNearbyRobots();
            for (int i = 0; i < nearby.length; i++) {

                // This WILL break if there is more than one EC next to
                if (nearby[i].type == RobotType.ENLIGHTENMENT_CENTER) {
                    spawnEC = nearby[i].location;
                    spawnECid = nearby[i].ID;
                }
            }
            // System.out.println("Initialized Politician, ID: "+rc.getID()+" EC:
            // "+spawnEC);

        } else {
            // System.out.println("Initialized Politician from Slanderer. ID: "+rc.getID()+"
            // EC: "+ spawnEC);

        }

    }

    private boolean nearbyEnemy(){
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

        if (age == 0) {
            runFirstTurn();
        } 

        Direction bestMove = null;
        if(spawnEC != null){
            if(rc.getRoundNum() < 250){
                bestMove = getBestVortex(getPossibleMoves(false, spawnEC), spawnEC, targetRadius/2.0, 0.0);
            }else{
                //0.65 is roughly 2 thick
                bestMove = getBestVortex(getPossibleMoves(false, spawnEC), spawnEC, targetRadius, 0.65);
            }
        }

        for (RobotInfo r : rc.senseNearbyRobots()) {
            if (!r.team.equals(rc.getTeam()) && rc.getLocation().isWithinDistanceSquared(r.location, 9)
                    && rc.canEmpower(9)) {
                rc.empower(9);
            }
        }
        if (rc.getRoundNum() < 600) {
            bestMove = null;
            if (spawnEC != null) {
                if (rc.getRoundNum() < 250) {
                    bestMove = getBestVortex(getPossibleMoves(false, spawnEC), spawnEC, 2 * targetRadius / 3.0, 0.0);
                } else {
                    bestMove = getBestVortex(getPossibleMoves(false, spawnEC), spawnEC, targetRadius, 0.65);
                }
            }
        }

//             if (bestMove != null) {
//                 tryMove(bestMove);
//             }
//         } else if (rc.getRoundNum() == 600) {
//             exploreLoc = new MapLocation(10 * (rc.getLocation().x - spawnEC.x) + spawnEC.x,
//                     10 * (rc.getLocation().y - spawnEC.y) + spawnEC.y);
//         } else if (rc.getRoundNum() < 900) {
//             tryMove(getRoughMoveTowards(exploreLoc, 2));


        // detects if enemy is nearby
        if(nearbyEnemy()){
            rc.empower(rc.getType().actionRadiusSquared);

        }

        // if (age >= 250){
        //     if (Math.random() > 0.4 && rc.canEmpower(1)){
        //         rc.empower(1);
        //     }
            
        // }
        age++;

    }

}
