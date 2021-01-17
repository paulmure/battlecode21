package cloudplayer;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
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
    HashSet<Integer> politicians = new HashSet<Integer>();
    HashSet<Integer> slanderers = new HashSet<Integer>();

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

        /*** This works but default HashSet is too slow, this is a certified Paul moment ***/
        // ArrayList<Integer> deadPoliticians = new ArrayList<Integer>();
        // for (Integer id : politicians) {
        //     if (rc.canGetFlag(id)) {
        //         int flag = rc.getFlag(id);
        //         if (flag != 0) {
        //             int influenceComponent = flag >>> 15;
        //             int enemyComponent = (flag >>> 14) % 2;
        //             MapLocation ecLoc = flagToLoc(flag % 0x4000, myLoc);
        //             // System.out.println((enemyComponent == 1 ? "Enemy" : "Neutral") + " EC at " 
        //             //     + ecLoc.x + ", " + ecLoc.y + " has " + influenceComponent + " influence.");
        //         }
        //     } else {
        //         deadPoliticians.add(id);
        //     }
        // }
        // for (Integer id : deadPoliticians) {
        //     politicians.remove(id);
        // }

        // ArrayList<Integer> deadSlanderers = new ArrayList<Integer>();
        // for (Integer id : slanderers) {
        //     if (!rc.canGetFlag(id)) {
        //         // deadSlanderers.add(id);
        //     }
        // }
        // for (Integer id : deadSlanderers) {
        //     slanderers.remove(id);
        // }

        boolean nearbyEnemies = false;
        for (RobotInfo r : rc.senseNearbyRobots()) {
            if (r.team.equals(rc.getTeam().opponent())) {
                nearbyEnemies = true;
                break;
            }
        }

        int index = idealSlandererInfluence.length - 1;
        while (idealSlandererInfluence[index] > influence) {
            --index;
        }
        int bestSlanderer = idealSlandererInfluence[index];

        // if (spawnTurn == 1) {
            for (int i = 0; i < 8; i++) {
                //if (rc.getRoundNum() < 300 && slanderers.size() * politiciansPerSlanderer > politicians.size()) {
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
                tryBuildRobot(RobotType.MUCKRAKER, dir, 1);
                dir = dir.rotateRight();
            }
        // } else {
        //     for (int i = 0; i < 8; i++) {
        //         if (tryBuildRobot(RobotType.MUCKRAKER, dir, rc.getInfluence())) {
        //             break;
        //         }
        //         dir = dir.rotateRight();
        //     }
        // }

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

        /*** Adds to the too slow HashSets ***/
        // for (Direction d : directions) {
        //     if (rc.onTheMap(rc.adjacentLocation(d))) {
        //         RobotInfo r = rc.senseRobotAtLocation(rc.adjacentLocation(d));
        //         if (r != null && r.team.equals(rc.getTeam())) {
        //             if (r.type.equals(RobotType.POLITICIAN)) {
        //                 politicians.add(r.ID);
        //             } else if (r.type.equals(RobotType.SLANDERER)) {
        //                 slanderers.add(r.ID);
        //             }
        //         }
        // }

        if (rc.getRoundNum() % 50 == 0) {
            System.out.println("I have " + influence + " influence on round " + rc.getRoundNum());
        }
    }
}
