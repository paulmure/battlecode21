package ourplayer;

import battlecode.common.*;

public class Muckracker extends RobotPlayer implements RoleController {

    public void run() throws GameActionException {
        MapLocation loc = rc.getLocation();
        MapLocation newLoc = new MapLocation(loc.x + 5, loc.y + 2);
        System.out.printf("bytecode used before initializing AStarSearch object: %d\n", Clock.getBytecodeNum());
        AStarSearch aStar = new AStarSearch(newLoc);
        System.out.printf("bytecode used after initializing AStarSearch object: %d\n", Clock.getBytecodeNum());
        aStar.findPath();
    }

    // public void run() throws GameActionException {
    //     Team enemy = rc.getTeam().opponent();
    //     int actionRadius = rc.getType().actionRadiusSquared;
    //     for (RobotInfo robot : rc.senseNearbyRobots(actionRadius, enemy)) {
    //         if (robot.type.canBeExposed()) {
    //             // It's a slanderer... go get them!
    //             if (rc.canExpose(robot.location)) {
    //                 System.out.println("e x p o s e d");
    //                 rc.expose(robot.location);
    //                 return;
    //             }
    //         }
    //     }
    //     if (tryMove(randomDirection()))
    //         System.out.println("I moved!");
    // }
    
}
