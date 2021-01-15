package sprintplayer;

import java.util.Deque;
import java.util.LinkedList;

import battlecode.common.*;

public class EnlightenmentCenter extends RobotPlayer implements RoleController {
    Deque<Integer> activeSlanderers;
    private Bidder bidder;
    int[] idealSlandererInfluence = { 0, 21, 41, 63, 85, 107, 130, 154, 178, 203, 228, 255, 282, 310, 339, 368, 399,
            431, 463, 497, 532, 568, 605, 643, 683, 724, 766, 810, 855, 902, 949, Integer.MAX_VALUE };
    int spawnTurn;
    int age;
    int startBiddingRound = 300;
    int minBiddingInfluence = 500;
    double politiciansPerSlanderer = 8;
    int slanderersBuilt = 0;
    int politiciansBuilt = 0;

    public EnlightenmentCenter() {
        activeSlanderers = new LinkedList<Integer>();
        bidder = new Bidder();
        spawnTurn = rc.getRoundNum();
    }

    public void run() throws GameActionException {
        age = rc.getRoundNum() - spawnTurn;
        // RobotType toBuild = RobotType.SLANDERER;

        // FIRST TIME
        /*
         * MapLocation loc = rc.getLocation();
         * 
         * for (int x = -6; x <= 6; x++) { for (int y = -6; y <= 6; y++) {
         * 
         * if (x * x + y * y <= 40 && x * x + y * y >= 32) { MapLocation thisLocation =
         * new MapLocation(rc.getLocation().x + x, rc.getLocation().y + y);
         * 
         * rc.setIndicatorDot(thisLocation, 50, 205, 50); //
         * System.out.println("drawn dot at "+thisLocation);
         * 
         * } } }
         */

        int influence = rc.getInfluence();

        Direction dir = randomDirection();

        int index = idealSlandererInfluence.length - 1;
        while (idealSlandererInfluence[index] > influence) {
            --index;
        }
        int bestSlanderer = idealSlandererInfluence[index];

        if (spawnTurn == 1) {
            for (int i = 0; i < 8; i++) {
                if (rc.getRoundNum() < 300 && slanderersBuilt * politiciansPerSlanderer > politiciansBuilt) {
                    if (tryBuildRobot(RobotType.POLITICIAN, dir, 15 + influence / 100)) {
                        politiciansBuilt++;
                        break;
                    }
                } else if (rc.getRoundNum() < 600
                        && slanderersBuilt * (politiciansPerSlanderer / 2) > politiciansBuilt) {
                    if (tryBuildRobot(RobotType.POLITICIAN, dir, 15 + influence / 100)) {
                        politiciansBuilt++;
                        break;
                    }
                } else if (rc.getRoundNum() < 900
                        && slanderersBuilt * (politiciansPerSlanderer / 4) > politiciansBuilt) {
                    if (tryBuildRobot(RobotType.POLITICIAN, dir, 15 + influence / 100)) {
                        politiciansBuilt++;
                        break;
                    }
                } else {
                    if (tryBuildRobot(RobotType.SLANDERER, dir, bestSlanderer)) {
                        slanderersBuilt++;
                        // activeSlanderers.add(bestSlanderer / 20);
                        // if(activeSlanderers.size() > 50) {
                        // activeSlanderers.poll();
                        // }
                        break;
                    }
                }
                dir = dir.rotateRight();
            }
        } else {
            for (int i = 0; i < 8; i++) {
                if (tryBuildRobot(RobotType.MUCKRAKER, dir, rc.getInfluence())) {
                    break;
                }
                dir = dir.rotateRight();
            }
        }

        // if (rc.getRoundNum() == 1){
        // tryBuildRobot(RobotType.MUCKRAKER, directions[1], 50);
        // }
        // if (influence + influencePerTurn > Integer.MAX_VALUE - 500000000){
        // rc.bid(influencePerTurn);
        // }
        if (rc.getRoundNum() > startBiddingRound) {
            if (rc.getInfluence() > minBiddingInfluence) {
                bidder.bid();
            } else {
                if (rc.canBid(1)) {
                    rc.bid(1);
                }
            }
        }

        if (rc.getRoundNum() % 50 == 0) {
            System.out.println("I have " + influence + " influence on round " + rc.getRoundNum());
        }
    }
}
