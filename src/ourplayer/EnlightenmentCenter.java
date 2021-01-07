package ourplayer;

import java.util.ArrayList;

import battlecode.common.*;

public class EnlightenmentCenter extends RobotPlayer implements RoleController {

    public void run() throws GameActionException {
        // RobotType toBuild = RobotType.SLANDERER;

        // FIRST TIME
        
        ArrayList<MapLocation> outerRing = new ArrayList<>();

        MapLocation loc = rc.getLocation();

        for (int x = -6; x <= 6; x++) {
            for (int y = -6; y <= 6; y++) {

                if (x * x + y * y <= 40 && x * x + y * y >= 32) {
                    MapLocation thisLocation = new MapLocation(rc.getLocation().x+x, rc.getLocation().y+y);

                    rc.setIndicatorDot(thisLocation, 50, 205, 50);
                    // System.out.println("drawn dot at "+thisLocation);
                    
                }
            }
        }
        

        int influence = rc.getInfluence();

        Direction dir = randomDirection();

        if (rc.getRoundNum() == 1500){
            rc.buildRobot(RobotType.POLITICIAN, dir, 50);
        }
        
        if (rc.canBuildRobot(RobotType.SLANDERER, dir, 50)) {
            rc.buildRobot(RobotType.SLANDERER, dir, 50);
        } 
        

        if (rc.getRoundNum() % 50 == 0) {
            System.out.println("I have " + influence + " influence on round " + rc.getRoundNum());
        }
    }

}
