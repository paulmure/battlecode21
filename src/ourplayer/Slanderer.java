package ourplayer;

import battlecode.common.*;

public class Slanderer extends RobotPlayer implements RoleController {
    
    public void run() throws GameActionException {
        if (tryMove(randomDirection())) {
            // System.out.println("I moved!");
        }
    }

}
