package ourplayer;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;

import battlecode.common.*;

public class EnlightenmentCenter extends RobotPlayer implements RoleController {
    Deque<Integer> activeSlanderers;

    public EnlightenmentCenter() {
        activeSlanderers = new LinkedList<Integer>();
    }

    public void run() throws GameActionException {
        // RobotType toBuild = RobotType.SLANDERER;

        // FIRST TIME
        MapLocation loc = rc.getLocation();

        for (int x = -6; x <= 6; x++) {
            for (int y = -6; y <= 6; y++) {

                if (x * x + y * y <= 40 && x * x + y * y >= 32) {
                    MapLocation thisLocation = new MapLocation(rc.getLocation().x+x, rc.getLocation().y+y);

                    rc.setIndicatorDot(thisLocation, 50, 205, 50);
                    // System.out.println("drawn dot at "+thisLocation);
                    
                }
            }
        }
        

        int influence = rc.getInfluence();

        Direction dir = randomDirection();

        for(int i = 0; i < 8; i++){
            if (rc.getRoundNum() < 300 && rc.getRoundNum() % 3 == 0){
                if(tryBuildRobot(RobotType.POLITICIAN, dir, 75)){
                    break;
                }
            } else {
                int buildInfluence = influence - influence % 20;
                if(tryBuildRobot(RobotType.SLANDERER, dir, buildInfluence)){
                    activeSlanderers.add(buildInfluence / 20);
                    if(activeSlanderers.size() > 50) {
                        activeSlanderers.poll();
                    }
                    break;
                }
            } 
            dir = dir.rotateRight();
        }

        if (rc.getRoundNum() == 1){
            tryBuildRobot(RobotType.MUCKRAKER, directions[1], 50);
        }
        if (influence + influencePerTurn > Integer.MAX_VALUE - 500000000){
            rc.bid(influencePerTurn);
        }
        if (rc.getRoundNum() % 50 == 0) {
            System.out.println("I have " + influence + " influence on round " + rc.getRoundNum());
        }
    }

}
