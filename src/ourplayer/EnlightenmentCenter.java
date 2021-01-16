package ourplayer;

import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
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

    LinkedList<Integer> politicianIDs = new LinkedList<>();
    LinkedList<Integer> muckrakerIDs = new LinkedList<>();

    public EnlightenmentCenter() {
        activeSlanderers = new LinkedList<Integer>();
        bidder = new Bidder();
        spawnTurn = rc.getRoundNum();
    }

    public void run() throws GameActionException {
        age = rc.getRoundNum() - spawnTurn;
        int influence = rc.getInfluence();
        Direction dir = randomDirection();
        MapLocation myLoc = rc.getLocation();

        // scan all politician flags
        Iterator<Integer> politicianItr = politicianIDs.iterator();
        while (politicianItr.hasNext()) {
            int id = politicianItr.next();
            if (rc.canGetFlag(id)) {
                // do something cool
            } else {
                // RIP
                politicianItr.remove();
            }
        }

        // scan all muckraker flags
        Iterator<Integer> muckrakerItr = muckrakerIDs.iterator();
        while (muckrakerItr.hasNext()) {
            int id = muckrakerItr.next();
            if (rc.canGetFlag(id)) {
                // do something cool
            } else {
                // RIP
                muckrakerItr.remove();
            }
        }

        // scan for nearby enemies
        boolean nearbyEnemies = false;
        for (RobotInfo r : rc.senseNearbyRobots()) {
            if (r.team.equals(rc.getTeam().opponent())) {
                nearbyEnemies = true;
                break;
            }
        }
        // calculate best value slanderer
        int index = idealSlandererInfluence.length - 1;
        while (idealSlandererInfluence[index] > influence) {
            --index;
        }
        int bestSlanderer = idealSlandererInfluence[index];

        // spawn loop
        for (int i = 0; i < 8; i++) {
            // if (rc.getRoundNum() < 300 && slanderers.size() * politiciansPerSlanderer >
            // politicians.size()) {
            if (rc.getRoundNum() < 300 && slanderersBuilt * politiciansPerSlanderer > politiciansBuilt) {
                if (tryBuildRobot(RobotType.POLITICIAN, dir, 15 + influence / 100)) {
                    System.out.println("Successfully scanned new politician: "
                            + politicianIDs.add(rc.senseRobotAtLocation(myLoc.add(dir)).ID));
                    politiciansBuilt++;
                    break;
                }
            } else if (rc.getRoundNum() < 600 && slanderersBuilt * (politiciansPerSlanderer / 2) > politiciansBuilt) {
                if (tryBuildRobot(RobotType.POLITICIAN, dir, 15 + influence / 100)) {
                    politiciansBuilt++;
                    break;
                }
            } else if (rc.getRoundNum() < 900 && slanderersBuilt * (politiciansPerSlanderer / 4) > politiciansBuilt) {
                if (tryBuildRobot(RobotType.POLITICIAN, dir, 15 + influence / 100)) {
                    politiciansBuilt++;
                    break;
                }
            } else if (nearbyEnemies == false || spawnTurn == 1) {
                if (tryBuildRobot(RobotType.SLANDERER, dir, bestSlanderer)) {
                    slanderersBuilt++;
                    // activeSlanderers.add(bestSlanderer / 20);
                    // if(activeSlanderers.size() > 50) {
                    // activeSlanderers.poll();
                    // }
                    break;
                }
            }
            if (tryBuildRobot(RobotType.MUCKRAKER, dir, 1)) {
                System.out.println("Successfully scanned new muckraker: "
                        + muckrakerIDs.add(rc.senseRobotAtLocation(myLoc.add(dir)).ID));
            }
            dir = dir.rotateRight();
        }
        // } else {
        // for (int i = 0; i < 8; i++) {
        // if (tryBuildRobot(RobotType.MUCKRAKER, dir, rc.getInfluence())) {
        // break;
        // }
        // dir = dir.rotateRight();
        // }
        // }

        // if (rc.getRoundNum() == 1){
        // tryBuildRobot(RobotType.MUCKRAKER, directions[1], 50);
        // }
        // if (influence + influencePerTurn > Integer.MAX_VALUE - 500000000){
        // rc.bid(influencePerTurn);
        // }

        //
        if (rc.getRoundNum() > startBiddingRound) {
            if (rc.getInfluence() > minBiddingInfluence) {
                bidder.bid();
            } else {
                if (rc.canBid(1)) {
                    rc.bid(1);
                }
            }
        }

        /*** Adds to the too slow HashSets ***/
        // for (Direction d : directions) {
        // if (rc.onTheMap(rc.adjacentLocation(d))) {
        // RobotInfo r = rc.senseRobotAtLocation(rc.adjacentLocation(d));
        // if (r != null && r.team.equals(rc.getTeam())) {
        // if (r.type.equals(RobotType.POLITICIAN)) {
        // politicians.add(r.ID);
        // } else if (r.type.equals(RobotType.SLANDERER)) {
        // slanderers.add(r.ID);
        // }
        // }
        // }

        if (rc.getRoundNum() % 50 == 0) {
            System.out.println("I have " + influence + " influence on round " + rc.getRoundNum());
        }
    }
}
