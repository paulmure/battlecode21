package ourplayer;

import battlecode.common.*;

public class Slanderer extends RobotPlayer implements RoleController {
    
    public void run() throws GameActionException {
        System.out.println("I am a slanderer!!!!!!!");
        if (tryMove(randomDirection())) {
            // System.out.println("I moved!");
        }
    }

}
