package ourplayer;

import battlecode.common.*;

public class Politician extends RobotPlayer implements RoleController {
    
    public void run() throws GameActionException {
        System.out.println("Politician running");
        Team enemy = rc.getTeam().opponent();
        int actionRadius = rc.getType().actionRadiusSquared;
        RobotInfo[] attackable = rc.senseNearbyRobots(actionRadius, enemy);
        if (attackable.length != 0 && rc.canEmpower(actionRadius)) {
            System.out.println("j a b i n g u s: "+rc.getConviction());
            rc.empower(actionRadius);
            // System.out.println("empowered");
            return;
        }
        tryMove(randomDirection());
            // System.out.println("I moved!");
        
    }

}
