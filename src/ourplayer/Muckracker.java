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

        // if(!foundPath){
        //     loc = rc.getLocation();
        //     newLoc = new MapLocation(loc.x + 25, loc.y + 12);    
        //     //System.out.printf("bytecode used before initializing AStarSearch object: %d\n", Clock.getBytecodeNum());
        //     //AStarSearch aStar = new AStarSearch(newLoc);
        //     //System.out.printf("bytecode used after initializing AStarSearch object: %d\n", Clock.getBytecodeNum());
        //     //aStar.findPath();
        //     foundPath = true;
        // } else {
        //     //tryMove(getRoughMoveTowards(newLoc));
        //     tryMove(rc.getLocation().directionTo(newLoc));
        // }
    }
}
