package ourplayer;

import java.util.ArrayList;

import battlecode.common.*;
import ourplayer.utils.Node;

public class EnlightenmentCenter extends RobotPlayer implements RoleController {
    private Bidder bidder;
    int[] idealSlandererInfluence = { 0, 21, 41, 63, 85, 107, 130, 154, 178, 203, 228, 255, 282, 310, 339, 368, 399,
            431, 463, 497, 532, 568, 605, 643, 683, 724, 766, 810, 855, 902, 949, Integer.MAX_VALUE };
    int spawnTurn;
    int age;
    int startBiddingRound = 300;
    int minBiddingInfluence = 500;
    int influenceToSave = 800;
    int minHunterInfluence = 100;
    double percentPoliticians = 0.25; // should to add to 1
    double percentSlanderers = 0.25; // with
    double percentMuckrakers = 0.5; // these

    Node politicianIDs = new Node(-1);
    int politicians = 0;
    Node muckrakerIDs = new Node(-1);
    int muckrakers = 0;
    Node slandererIDs = new Node(-1);
    int slanderers = 0;

    int mostRecentID = -1;
    boolean recentlyFlagged = false;
    boolean recentHunter = false;
    int hunterCounter = 3;
    int muckHunterRatio = 5;
    int turnsToDefend = 50;
    int turnsSinceMuckNear = turnsToDefend;

    ArrayList<RobotInfo> ecList = new ArrayList<RobotInfo>();
    ArrayList<RobotInfo> recentlyTargeted = new ArrayList<RobotInfo>();

    public EnlightenmentCenter() {
        bidder = new Bidder();
        spawnTurn = rc.getRoundNum();
    }

    public void run() throws GameActionException {
        int roundNum = rc.getRoundNum();
        age = roundNum - spawnTurn;
        int influence = rc.getInfluence();
        Team team = rc.getTeam();
        MapLocation myLoc = rc.getLocation();
        if (!recentlyFlagged) {
            rc.setFlag(0);
        }
        recentlyFlagged = false;
        ++turnsSinceMuckNear;
        if (muckrakers >= 10) {
            percentMuckrakers = 0;
            percentPoliticians = 0.75;
        }

        // scan all politician flags
        Node pointer = politicianIDs;
        while (pointer.next != null) {
            pointer = pointer.next;
            int id = pointer.value;
            if (rc.canGetFlag(id)) {
                int flag = rc.getFlag(id);
                if (flag != 0) {
                    RobotInfo received = new FlagInfo(flag, team, myLoc).targetInfo;
                    boolean newEC = true;
                    for (int i = 0; i < ecList.size(); ++i) {
                        RobotInfo r = ecList.get(i);
                        if (r.location.equals(received.location)) {
                            ecList.set(i, received);
                            newEC = false;
                        }
                    }
                    if (newEC) {
                        ecList.add(received);
                    }
                }
            } else {
                // RIP
                pointer = pointer.remove();
                --politicians;
            }
        }

        // scan all muckraker flags
        pointer = muckrakerIDs;
        while (pointer.next != null) {
            pointer = pointer.next;
            int id = pointer.value;
            if (rc.canGetFlag(id)) {
                int flag = rc.getFlag(id);
                if (flag != 0) {
                    RobotInfo received = new FlagInfo(flag, team, myLoc).targetInfo;
                    boolean newEC = true;
                    for (int i = 0; i < ecList.size(); ++i) {
                        RobotInfo r = ecList.get(i);
                        if (r.location.equals(received.location)) {
                            ecList.set(i, received);
                            newEC = false;
                        }
                    }
                    if (newEC) {
                        ecList.add(received);
                    }
                }
            } else {
                // RIP
                pointer = pointer.remove();
                --muckrakers;
            }
        }

        // scan all slanderer flags
        pointer = slandererIDs;
        while (pointer.next != null) {
            pointer = pointer.next;
            int id = pointer.value;
            if (rc.canGetFlag(id)) {
                if (rc.getFlag(id) != 420 && id != mostRecentID) {
                    politicianIDs.add(pointer.value);
                    ++politicians;
                } else {
                    continue;
                }

            }
            // RIP
            pointer = pointer.remove();
            --slanderers;
        }

        ArrayList<RobotInfo> allyECs = new ArrayList<RobotInfo>();
        ArrayList<RobotInfo> neutralECs = new ArrayList<RobotInfo>();
        ArrayList<RobotInfo> enemyECs = new ArrayList<RobotInfo>();
        for (RobotInfo ec : ecList) {
            if (ec.team.equals(team)) {
                allyECs.add(ec);
            } else if (ec.team.equals(Team.NEUTRAL)) {
                neutralECs.add(ec);
            } else {
                enemyECs.add(ec);
            }
        }

        int totalUnits = politicians + muckrakers + slanderers;

        // scan for nearby enemies
        int biggestNearbyEnemy = 0;
        boolean canSeeEnemyEC = false; // crossstitch lmao
        for (RobotInfo r : rc.senseNearbyRobots()) {
            if (r.team.equals(rc.getTeam().opponent())) {
                if(r.type != RobotType.ENLIGHTENMENT_CENTER) {
                    biggestNearbyEnemy = Math.max(biggestNearbyEnemy, r.influence);
                    if (r.type.equals(RobotType.MUCKRAKER)) {
                        turnsSinceMuckNear = 0;
                    }
                } else {
                    canSeeEnemyEC = true;
                }
            }
        }
        // if (biggestNearbyEnemy > 0 && roundNum == spawnTurn) { // dont start spawning
        // slanderers if its contested
        // turnsSinceMuckNear = 0; // actually it might be ok lets try it
        // }
        influence -= biggestNearbyEnemy; // don't use all our influence if we're gonna die

        // calculate best value slanderer
        int index = idealSlandererInfluence.length - 1;
        while (idealSlandererInfluence[index] > Math.max(influence, 0)) {
            --index;
        }
        int bestSlanderer = idealSlandererInfluence[index];

        // get the lowest hp neutral ec we know about
        RobotInfo lowestNeutralEC = null;
        for (RobotInfo r : neutralECs) {
            if (!recentlyTargeted.contains(r)) {
                if (lowestNeutralEC == null || r.influence < lowestNeutralEC.influence) {
                    lowestNeutralEC = r;
                } else if (r.influence == lowestNeutralEC.influence
                        && r.location.distanceSquaredTo(myLoc) 
                        < lowestNeutralEC.location.distanceSquaredTo(myLoc)) {
                    lowestNeutralEC = r;
                }
            }
        }
        // get the closest enemy ec we know about
        RobotInfo closestEnemyEC = null;
        for (RobotInfo r : enemyECs) {
            if (closestEnemyEC == null
                    || chebyshevDistance(myLoc, r.location) < chebyshevDistance(myLoc, closestEnemyEC.location)) {
                closestEnemyEC = r;
            }
        }

        // spawn direction selection
        Direction dir = randomDirection();
        for (int i = 0; i < 8; i++) {
            if (rc.isLocationOccupied(myLoc.add(dir))) {
                dir = dir.rotateRight();
            }
        }

        if (lowestNeutralEC != null && lowestNeutralEC.influence + 11 <= influence) {
            if (tryBuildRobot(RobotType.POLITICIAN, dir, lowestNeutralEC.influence + 11, politicianIDs)) {
                rc.setFlag(new FlagInfo(lowestNeutralEC, team, myLoc).generateFlag());
                recentlyFlagged = true;
                recentlyTargeted.add(lowestNeutralEC);
                ++politicians;
            }
        } else if (slanderers <= percentSlanderers * totalUnits
                && (turnsSinceMuckNear > turnsToDefend || spawnTurn == 1) && !canSeeEnemyEC) {
            if (tryBuildRobot(RobotType.SLANDERER, dir, bestSlanderer, slandererIDs)) {
                ++slanderers;
            }
        } else if (closestEnemyEC != null && !recentHunter && influence > influenceToSave + minHunterInfluence) {
            if (hunterCounter < muckHunterRatio) {
                if (tryBuildRobot(RobotType.POLITICIAN, dir, influence - influenceToSave, politicianIDs)) {
                    rc.setFlag(new FlagInfo(closestEnemyEC, team, myLoc).generateFlag());
                    recentlyFlagged = true;
                    recentHunter = true;
                    ++politicians;
                    ++hunterCounter;
                }
            } else {
                if (tryBuildRobot(RobotType.MUCKRAKER, dir, influence - influenceToSave, muckrakerIDs)) {
                    RobotInfo randomEnemyEC = enemyECs.get((int) (Math.random() * enemyECs.size()));
                    rc.setFlag(new FlagInfo(randomEnemyEC, team, myLoc).generateFlag());
                    recentlyFlagged = true;
                    recentHunter = true;
                    ++politicians;
                    hunterCounter = 0;
                }
            }
        } else if (politicians <= percentPoliticians * totalUnits) {
            if (tryBuildRobot(RobotType.POLITICIAN, dir, 15 + influence / 100, politicianIDs)) {
                recentHunter = false;
                ++politicians;
            }
        }
        if (tryBuildRobot(RobotType.MUCKRAKER, dir, 1, muckrakerIDs)) { // someday flag mucks to tank if influence
                                                                        // is negative
            ++muckrakers;
        }

        // BIDDING
        //
        if (rc.getRoundNum() > startBiddingRound && rc.getInfluence() > minBiddingInfluence) {

            int desiredBid = bidder.bid();
            if (rc.canBid(desiredBid)) {
                rc.bid(desiredBid);
            }

            System.out.println("bid " + desiredBid);
            bidder.setLastBid(desiredBid);
        } else {

            if (rc.canBid(1)) {
                System.out.println("bid 1 (from else block)");
                rc.bid(1);
            }

        }

        if (rc.getRoundNum() % 50 == 0) {
            System.out.println("I have " + influence + " influence on round " + rc.getRoundNum());
        }
    }

    boolean tryBuildRobot(RobotType type, Direction dir, int influence) throws GameActionException {
        if (rc.canBuildRobot(type, dir, influence)) {
            rc.buildRobot(type, dir, influence);
            return true;
        } else {
            return false;
        }
    }

    boolean tryBuildRobot(RobotType type, Direction dir, int influence, Node list) throws GameActionException {
        if (tryBuildRobot(type, dir, influence)) {
            mostRecentID = rc.senseRobotAtLocation(rc.getLocation().add(dir)).ID;
            list.add(mostRecentID);
            return true;
        }
        return false;
    }
}
