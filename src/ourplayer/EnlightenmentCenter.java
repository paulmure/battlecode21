package ourplayer;

import battlecode.common.*;

public class EnlightenmentCenter extends RobotPlayer implements RoleController {

    public void run() throws GameActionException {
        // RobotType toBuild = RobotType.SLANDERER;
        int influence = rc.getInfluence();
        for (Direction dir : directions) {
            if (rc.canBuildRobot(RobotType.SLANDERER, dir, influence - influence % 20)) {
                rc.buildRobot(RobotType.SLANDERER, dir, influence - influence % 20);
                break;
            }
        }
        if (rc.getRoundNum() % 50 == 0) {
            System.out.println("I have " + influence + " influence on round " + rc.getRoundNum());
        }
    }
}
