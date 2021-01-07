package ourplayer;

import java.util.ArrayList;
import java.util.Map;

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

    private double endRadius2;

    public Slanderer() {
        age = 0;
        // change this to change outer radius^2
        endRadius2 = 35;
    }

    public void run() throws GameActionException {
        if (age == 0) {
            runFirstTurn();
        }

        Direction bestMove = getBest(getPossibleMoves(), spawnEC, calculateTargetRadius2(), 0.0);
        if (bestMove != null) {
            tryMove(bestMove);
        }

        age++;
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
        return ((double) age * endRadius2) / 300;
    }

    private void runFirstTurn() {
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

    }

}
