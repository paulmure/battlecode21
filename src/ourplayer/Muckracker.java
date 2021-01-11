package ourplayer;

import battlecode.common.*;

public class Muckracker extends RobotPlayer implements RoleController {
    boolean foundPath = false;
    MapLocation loc;
    MapLocation newLoc;

    public void run() throws GameActionException {
        System.out.println("starting a*");
        loc = rc.getLocation();
        newLoc = new MapLocation(loc.x + 5, loc.y + 2);
        System.out.printf("bytecode used before initializing AStarSearch object: %d\n", Clock.getBytecodeNum());
        AStarSearch aStar = new AStarSearch(newLoc);
        System.out.printf("bytecode used after initializing AStarSearch object: %d\n", Clock.getBytecodeNum());
        System.out.println(aStar.findPath());
        System.out.printf("bytecode used after findPath: %d\n", Clock.getBytecodeNum());

        // if (!foundPath) {
        //     loc = rc.getLocation();
        //     if (rc.getTeam().equals(Team.A)) {
        //         newLoc = new MapLocation(loc.x + 11, loc.y + 25);
        //     } else {
        //         newLoc = new MapLocation(loc.x - 11, loc.y + 25);
        //     }
        //     // System.out.printf("bytecode used before initializing AStarSearch object:
        //     // %d\n", Clock.getBytecodeNum());
        //     // AStarSearch aStar = new AStarSearch(newLoc);
        //     // System.out.printf("bytecode used after initializing AStarSearch object:
        //     // %d\n", Clock.getBytecodeNum());
        //     // aStar.findPath();
        //     foundPath = true;
        // } else {
        //     Direction d;
        //     if (rc.getTeam().equals(Team.A)) {
        //         d = getRoughMoveTowards(newLoc, 2);
        //     } else {
        //         d = getRoughMoveTowards(newLoc, 1);
        //     }
        //     tryMove(d);
        // }
    }
}
