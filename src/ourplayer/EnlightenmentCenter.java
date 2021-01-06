package ourplayer;

import java.util.ArrayList;

import battlecode.common.*;

public class EnlightenmentCenter extends RobotPlayer implements RoleController {

    public void run() throws GameActionException {
        // RobotType toBuild = RobotType.SLANDERER;

        // FIRST TIME
        if (rc.getRoundNum() == 1) {
            ArrayList<MapLocation> outerRing = new ArrayList<>();

            MapLocation loc = rc.getLocation();

            // FOR THE LOVE OF GOD, DELETE THIS LINE AT SOME POINT
            rc.buildRobot(RobotType.SLANDERER, Direction.NORTH, 50);
            //////

            for (int x = -5; x < 5; x++) {
                for (int y = -5; y < 5; y++) {

                    // if it is in the outer beltway

                    if (x * x + y * y <= 29 && x * x + y * y >= 25) {
                        MapLocation thisLocation = new MapLocation(x, y);
                        outerRing.add(thisLocation);
                    }
                }
            }
        }

        int influence = rc.getInfluence();

        /*
         * for (Direction dir : directions) { if (rc.canBuildRobot(RobotType.SLANDERER,
         * dir, influence)) { rc.buildRobot(RobotType.SLANDERER, dir, influence); break;
         * } }
         */

        if (rc.getRoundNum() % 50 == 0) {
            System.out.println("I have " + influence + " influence on round " + rc.getRoundNum());
        }
    }
}
