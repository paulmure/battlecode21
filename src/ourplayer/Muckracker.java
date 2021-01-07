package ourplayer;

import battlecode.common.*;

public class Muckracker extends RobotPlayer implements RoleController {
    boolean foundPath = false;
    MapLocation loc;
    MapLocation newLoc;


    public void run() throws GameActionException {
        // loc = rc.getLocation();
        // newLoc = new MapLocation(loc.x - 5, loc.y - 2);    
        // System.out.printf("bytecode used before initializing AStarSearch object: %d\n", Clock.getBytecodeNum());
        // AStarSearch aStar = new AStarSearch(newLoc);
        // System.out.printf("bytecode used after initializing AStarSearch object: %d\n", Clock.getBytecodeNum());
        // aStar.findPath();

        if(!foundPath){
            loc = rc.getLocation();
            newLoc = new MapLocation(loc.x - 4, loc.y - 3);    
            System.out.printf("bytecode used before initializing AStarSearch object: %d\n", Clock.getBytecodeNum());
            AStarSearch aStar = new AStarSearch(newLoc);
            System.out.printf("bytecode used after initializing AStarSearch object: %d\n", Clock.getBytecodeNum());
            aStar.findPath();
            foundPath = true;
        } else {
            tryMove(getRoughMoveTowards(newLoc));
        }
        
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
