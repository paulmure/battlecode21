package ourplayer;

import battlecode.common.*;

public class Politician extends RobotPlayer implements RoleController {
    
    public void run() throws GameActionException {
        /*
         * Team enemy = rc.getTeam().opponent(); int actionRadius =
         * rc.getType().actionRadiusSquared; RobotInfo[] attackable =
         * rc.senseNearbyRobots(actionRadius, enemy); if (attackable.length != 0 &&
         * rc.canEmpower(actionRadius)) { System.out.println("empowering...");
         * rc.empower(actionRadius); System.out.println("empowered"); return; } if
         * (tryMove(randomDirection())) System.out.println("I moved!");
         */
        if (rc.canEmpower(1)) {
            rc.empower(1);
        }
    }

}
