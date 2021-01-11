package ourplayer;

import battlecode.common.*;

public class Muckracker extends RobotPlayer implements RoleController {
    boolean foundPath = false;
    MapLocation loc;
    MapLocation newLoc;

    public void run() throws GameActionException {
        loc = rc.getLocation();
        newLoc = new MapLocation(loc.x + 2, loc.y + 5);
        System.out.printf("bytecode used before initializing AStarSearch object:%d\n", Clock.getBytecodeNum());
        AStarSearch aStar = new AStarSearch(newLoc);
        aStar.findPath();
        System.out.printf("bytecode used after AStarSearch:%d\n", Clock.getBytecodeNum());

        MapLocation myLoc = rc.getLocation();

        RobotInfo closestEnemySlanderer = null;
        int closestSlandererDist = 1000;
        for (RobotInfo r : rc.senseNearbyRobots()) {
            if (r.type.equals(RobotType.SLANDERER) && !r.team.equals(rc.getTeam())
                    && chebyshevDistance(myLoc, r.location) < closestSlandererDist) {
                closestEnemySlanderer = r;
                closestSlandererDist = chebyshevDistance(myLoc, r.location);
            }
        }

        if (closestEnemySlanderer != null) {
            if (rc.canExpose(closestEnemySlanderer.location)) {
                rc.expose(closestEnemySlanderer.location);
            }
            tryMove(getRoughMoveTowards(closestEnemySlanderer.location, 2));
        }

        // if (!foundPath) {
        // loc = rc.getLocation();
        // if (rc.getTeam().equals(Team.A)) {
        // newLoc = new MapLocation(loc.x + 11, loc.y + 25);
        // } else {
        // newLoc = new MapLocation(loc.x - 11, loc.y + 25);
        // }
        // // System.out.printf("bytecode used before initializing AStarSearch object:
        // // %d\n", Clock.getBytecodeNum());
        // // AStarSearch aStar = new AStarSearch(newLoc);
        // // System.out.printf("bytecode used after initializing AStarSearch object:
        // // %d\n", Clock.getBytecodeNum());
        // // aStar.findPath();
        // foundPath = true;
        // } else {
        // Direction d;
        // if (rc.getTeam().equals(Team.A)) {
        // d = getRoughMoveTowards(newLoc, 2);
        // } else {
        // d = getRoughMoveTowards(newLoc, 1);
        // }
        // tryMove(d);
        // }
    }
}
