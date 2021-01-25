package ourplayer;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;

import battlecode.common.*;

public class Politician extends RobotPlayer implements RoleController {

    final int minRadius = 16;
    final double passabilityMultiplier = 64;
    final double wideningMultiplier = 0.3; // 0.25
    final double wideningExponent = 1; // 0.93
    final double standingWeight = 0; // 0.5
    final int hunterInfluence = 50;
    final int neutralTurnsToWait = 5;
    final int enemyTurnsToWait = 15;
    final int minStableInfluence = 200;
    final int minEnemyBigmuckInfluence = 50;
    boolean converted = false;
    boolean isHunter = false;
    int turnsWaited = 0;
    private double maxRadius;
    private MapLocation spawnEC;
    private double ecPassability;
    private int spawnECid;
    private int age;
    MapLocation exploreLoc;
    int spawnRound;
    ArrayList<RobotInfo> previouslySentECs = new ArrayList<RobotInfo>();
    Deque<RobotInfo> ecsToSend = new LinkedList<RobotInfo>();
    RobotInfo targetEC = null;
    int[] walls = { -1, -1, -1, -1 }; // N, E, S, W
    int wallRepulsionDistance = 3;
    int[] possibleEmpowerDists = { 1, 2, 4, 5, 8, 9 };

    public Politician() throws GameActionException {
        spawnRound = rc.getRoundNum();
        // set spawnEC
        if (spawnEC == null) {
            RobotInfo[] nearby = rc.senseNearbyRobots();
            for (int i = 0; i < nearby.length; i++) {
                // This WILL break if there is more than one EC next to
                if (nearby[i].type == RobotType.ENLIGHTENMENT_CENTER && (spawnEC == null || nearby[i].location
                        .distanceSquaredTo(rc.getLocation()) < spawnEC.distanceSquaredTo(rc.getLocation()))
                        && nearby[i].team.equals(rc.getTeam())) {
                    spawnEC = nearby[i].location;
                    spawnECid = nearby[i].ID;
                }
            }
            if (spawnEC == null) {
                converted = true;
                return;
            } else {
                ecPassability = rc.sensePassability(spawnEC);
                if (rc.canGetFlag(spawnECid)) {
                    int flag = rc.getFlag(spawnECid);
                    if (flag != 0) {
                        isHunter = true;
                        targetEC = new FlagInfo(flag, rc.getTeam(), spawnEC).targetInfo;
                    }
                }
            }
        }
        maxRadius = minRadius + ecPassability * passabilityMultiplier;
    }

    public Politician(MapLocation ec, int ecID, double ecP) throws GameActionException {
        spawnEC = ec;
        spawnECid = ecID;
        ecPassability = ecP;
        // System.out.println("joe mama: " + ec);
        spawnRound = rc.getRoundNum();
        maxRadius = minRadius + ecPassability * passabilityMultiplier;
        isHunter = Math.random() < 0.5 ? true : false;
    }

    private double getTargetRadius() {
        if (rc.getRoundNum() < 300) {
            return minRadius + (maxRadius - minRadius) * rc.getRoundNum() / 300;
        } else {
            return maxRadius + Math.pow(age, wideningExponent) * ecPassability * wideningMultiplier;
        }
    }

    public void run() throws GameActionException {
        if (rc.getRoundNum() >= 1495 && rc.getTeamVotes() < 750 && rc.getRobotCount() > 100) { // cause it would suck to
                                                                                               // take the map at the
                                                                                               // end
            if (rc.canEmpower(9)) { // and not kill 2 1hp politicians and lose on votes
                rc.empower(9);
            }
        } else if (rc.getConviction() <= 10 && rc.canEmpower(1)) {
            rc.empower(1);
        }

        if (converted) {
            convertedRun();
            return;
        }

        RobotInfo[] nearbyRobots = rc.senseNearbyRobots();
        MapLocation myLoc = rc.getLocation();
        age = rc.getRoundNum() - spawnRound;
        rc.setFlag(0);

        if (rc.getRoundNum() <= 300) { // careful with this stuff
            age = 0;
        } else {
            age = rc.getRoundNum() - Math.max(spawnRound, 300);
        }

        // look for walls
        for (int i = 0; i < 4; ++i) {
            if (walls[i] == -1) {
                MapLocation loc = null;
                Direction awayFromWall = Direction.CENTER;
                switch (i) {
                    case 0: // NORTH
                        loc = rc.getLocation().translate(0, 5);
                        awayFromWall = Direction.SOUTH;
                        break;
                    case 1: // EAST
                        loc = rc.getLocation().translate(5, 0);
                        awayFromWall = Direction.WEST;
                        break;
                    case 2: // SOUTH
                        loc = rc.getLocation().translate(0, -5);
                        awayFromWall = Direction.NORTH;
                        break;
                    case 3: // WEST
                        loc = rc.getLocation().translate(-5, 0);
                        awayFromWall = Direction.EAST;
                }

                if (!rc.onTheMap(loc)) {
                    while (!rc.onTheMap(loc.add(awayFromWall))) {
                        loc = loc.add(awayFromWall);
                    }
                    walls[i] = i % 2 == 0 ? loc.y : loc.x;
                    // System.out.println("Found wall at " + loc);
                }
            }
        }

        int mucksInRange = 0;
        RobotInfo closestEnemyMuck = null;
        RobotInfo biggestEnemyMuck = null;
        int closestMuckDist = 1000;
        RobotInfo notOurEC = null;
        int closestECDist = 1000;
        ArrayList<MapLocation> allies = new ArrayList<MapLocation>();
        ArrayList<MapLocation> nearbyAllies = new ArrayList<MapLocation>();
        RobotInfo closestVisibleEC = null;

        ////
        if (walls[0] != -1 && walls[0] - myLoc.y <= wallRepulsionDistance) {
            // System.out.println("near north wall");
            allies.add(new MapLocation(myLoc.x, walls[0]));
        }
        if (walls[1] != -1 && walls[1] - myLoc.x <= wallRepulsionDistance) {
            // System.out.println("near east wall");
            allies.add(new MapLocation(walls[1], myLoc.y));
        }
        if (walls[2] != -1 && myLoc.y - walls[2] <= wallRepulsionDistance) {
            // System.out.println("near south wall");
            allies.add(new MapLocation(myLoc.x, walls[2]));
        }
        if (walls[3] != -1 && myLoc.x - walls[3] <= wallRepulsionDistance) {
            // System.out.println("near west wall");
            allies.add(new MapLocation(walls[3], myLoc.y));

        }
        ////
        for (RobotInfo r : nearbyRobots) {
            // detect muckrakers in range as well as the closest one
            if (r.type.equals(RobotType.MUCKRAKER) && !r.team.equals(rc.getTeam())) {
                if (myLoc.distanceSquaredTo(r.location) <= 9) {
                    mucksInRange++;
                }
                if (chebyshevDistance(myLoc, r.location) < closestMuckDist) {
                    closestEnemyMuck = r;
                    closestMuckDist = chebyshevDistance(myLoc, r.location);
                }
                if (biggestEnemyMuck == null || r.conviction > biggestEnemyMuck.conviction) {
                    biggestEnemyMuck = r;
                }
            }
            // save allies for this turn only
            if (r.team.equals(rc.getTeam())) {
                if (r.type.equals(RobotType.POLITICIAN)) {
                    allies.add(r.location);
                    if (r.location.distanceSquaredTo(myLoc) <= 8) {
                        nearbyAllies.add(r.location);
                    }
                } else if (r.type.equals(RobotType.ENLIGHTENMENT_CENTER)) {
                    allies.add(r.location);
                }
            }

            // if you see a new enlightenment center, flag it
            if (r.type.equals(RobotType.ENLIGHTENMENT_CENTER)) {
                if (!ecsToSend.contains(r) && r.location != spawnEC) {
                    boolean newInfo = true;
                    for (int i = 0; i < previouslySentECs.size(); ++i) {
                        RobotInfo ec = previouslySentECs.get(i);
                        if (ec.location.equals(r.location)) {
                            if (!ec.equals(r)) {
                                previouslySentECs.remove(i);
                            } else {
                                newInfo = false;
                            }
                            break;
                        }
                    }
                    if (newInfo) {
                        previouslySentECs.add(r);
                        ecsToSend.add(r);
                    }
                }
                if (!r.team.equals(rc.getTeam()) && chebyshevDistance(myLoc, r.location) < closestECDist) {
                    notOurEC = r;
                    closestECDist = chebyshevDistance(myLoc, r.location);
                }
                if (closestVisibleEC == null
                        || chebyshevDistance(myLoc, r.location) < chebyshevDistance(myLoc, closestVisibleEC.location)) {
                    closestVisibleEC = r;
                }
                if (targetEC != null && targetEC.location.equals(r.location)) {
                    if (r.team.equals(rc.getTeam()) && r.influence > minStableInfluence) {
                        targetEC = null;
                    } else {
                        targetEC = r;
                    }
                }
            }
        }

        // if (closestVisibleEC != null) {
        // int ecDist = myLoc.distanceSquaredTo(closestVisibleEC.location);
        // if (rc.canEmpower(ecDist) && 3 * rc.senseNearbyRobots(ecDist).length <
        // rc.getEmpowerFactor(rc.getTeam(), 0)) {
        // rc.empower(ecDist);
        // }
        // }

        if (rc.isReady()) {
            int efficientEmpowerRadius = efficientEmpowerRadius();
            if (efficientEmpowerRadius > 0) {
                rc.empower(efficientEmpowerRadius);
            }
        }

        if (!ecsToSend.isEmpty()) {
            FlagInfo ecFlag = new FlagInfo(ecsToSend.poll(), rc.getTeam(), spawnEC);
            rc.setFlag(ecFlag.generateFlag());
        }

        if (notOurEC != null && (targetEC == null || !notOurEC.location.equals(targetEC.location))) {
            if (rc.getConviction() * rc.getEmpowerFactor(rc.getTeam(), 0) >= 0.5 * notOurEC.conviction) {
                if (rc.canEmpower(myLoc.distanceSquaredTo(notOurEC.location))
                        && myLoc.isAdjacentTo(notOurEC.location)) {
                    rc.empower(myLoc.distanceSquaredTo(notOurEC.location));
                } else {
                    tryMove(getRoughMoveTowards(notOurEC.location, 2));
                }
            }
        }

        // if you're too big to be on muckraker duty go attack enemy ECs
        if (/* isHunter && */rc.getConviction() >= hunterInfluence && targetEC == null && rc.canGetFlag(spawnECid)) {
            int flag = rc.getFlag(spawnECid);
            if (flag != 0) {
                RobotInfo ec = new FlagInfo(flag, rc.getTeam(), spawnEC).targetInfo;
                if (ec.team.equals(rc.getTeam().opponent())) {
                    targetEC = ec;
                }
            }
        }

        if (biggestEnemyMuck != null && (targetEC == null || !rc.canSenseLocation(targetEC.location))) {
            if (rc.getConviction() > biggestEnemyMuck.conviction * 0.25
                    && biggestEnemyMuck.conviction > minEnemyBigmuckInfluence) {
                int d2toMuck = myLoc.distanceSquaredTo(biggestEnemyMuck.location);
                if (rc.canEmpower(d2toMuck)
                        && influenceEmpowered(rc.senseNearbyRobots(d2toMuck).length) > biggestEnemyMuck.conviction) {// previously
                                                                                                                     // d2muck
                                                                                                                     // (adjacent
                                                                                                                     // to)
                    rc.empower(d2toMuck);
                } else if (rc.canEmpower(d2toMuck) && d2toMuck <= 2) {
                    rc.empower(d2toMuck);
                }
                else {
                    // System.out.println("running at big muck");
                    tryMove(getRoughMoveTowards(biggestEnemyMuck.location, 2));
                }
            }
        }

        if (targetEC != null) {
            int d2toTarget = myLoc.distanceSquaredTo(targetEC.location);
            if (rc.canEmpower(d2toTarget)) {
                if (rc.senseNearbyRobots(d2toTarget).length == 1) {
                    rc.empower(d2toTarget);
                } else if (targetEC.team.equals(Team.NEUTRAL) && turnsWaited >= neutralTurnsToWait) {
                    rc.empower(d2toTarget);
                } else if ((targetEC.team.equals(rc.getTeam().opponent()) || targetEC.team.equals(rc.getTeam()))
                        && (turnsWaited >= enemyTurnsToWait || d2toTarget == 1)) {
                    rc.empower(d2toTarget);
                } else {
                    ++turnsWaited;
                }
            } else {
                turnsWaited = 0;
            }
            if (targetEC.team.equals(Team.NEUTRAL)) {
                tryMove(getRoughMoveTowards(targetEC.location, 2));
            } else {
                if (rc.canSenseLocation(targetEC.location)) {
                    tryMove(getRoughMoveAvoid(targetEC.location, 2, nearbyAllies));
                } else {
                    tryMove(getRoughMoveTowards(targetEC.location, 2));
                }
            }
            return;
        }

        int buffer = 4;
        int emergencyBuffer = 1;
        if (closestEnemyMuck != null) {
            int d2toMuck = myLoc.distanceSquaredTo(closestEnemyMuck.location);
            if (spawnEC.distanceSquaredTo(closestEnemyMuck.location) <= getTargetRadius()
                    + 2 * Math.sqrt(getTargetRadius()) * emergencyBuffer + emergencyBuffer * emergencyBuffer) { // foiling
                // System.out.println("weewoo");
                if (canKill(closestEnemyMuck)) {
                    rc.empower(d2toMuck);
                }
            }
            if (spawnEC.distanceSquaredTo(closestEnemyMuck.location) <= getTargetRadius()
                    + 2 * Math.sqrt(getTargetRadius()) * buffer + buffer * buffer) { // foiling
                if (rc.canEmpower(d2toMuck) && d2toMuck <= 2) {
                    rc.empower(d2toMuck);
                }
                tryMove(getRoughMoveTowards(closestEnemyMuck.location, 2));
            }
        }

        Direction bestMove = null;
        if (spawnEC != null) {
            bestMove = getBestCloud(getPossibleMoves(false, spawnEC), spawnEC, getTargetRadius(), allies);
        }

        if (bestMove != null) {
            tryMove(bestMove);
        }
    }

    public void convertedRun() throws GameActionException {
        if (rc.getRoundNum() >= 1495 && rc.getTeamVotes() < 750 && rc.getRobotCount() > 100) { // cause it would suck to
            // take the map at the
            // end
            if (rc.canEmpower(9)) { // and not kill 2 1hp politicians and lose on votes
                rc.empower(9);
            }
        }

        MapLocation myLoc = rc.getLocation();
        RobotInfo[] nearbyRobots = rc.senseNearbyRobots();

        int mucksInRange = 0;
        RobotInfo closestEnemyMuck = null;
        int closestMuckDist = 1000;
        RobotInfo notOurEC = null;
        int closestECDist = 1000;
        ArrayList<MapLocation> allies = new ArrayList<MapLocation>();
        for (RobotInfo r : nearbyRobots) {
            if (r.type.equals(RobotType.MUCKRAKER) && !r.team.equals(rc.getTeam())) {
                if (myLoc.distanceSquaredTo(r.location) <= 9) {
                    mucksInRange++;
                }
                if (chebyshevDistance(myLoc, r.location) < closestMuckDist) {
                    closestEnemyMuck = r;
                    closestMuckDist = chebyshevDistance(myLoc, r.location);
                }
            }
            if (r.team.equals(rc.getTeam()) && r.type.equals(RobotType.POLITICIAN)) {
                allies.add(r.location);
            }
            if (r.type.equals(RobotType.ENLIGHTENMENT_CENTER)) {
                if (!r.team.equals(rc.getTeam()) && chebyshevDistance(myLoc, r.location) < closestECDist) {
                    notOurEC = r;
                    closestECDist = chebyshevDistance(myLoc, r.location);
                }
                if (r.team.equals(rc.getTeam())) {
                    spawnEC = r.location;
                    spawnECid = r.ID;
                    converted = false;
                    run();
                    return;
                }
            }
        }

        if (rc.isReady()) {
            int efficientEmpowerRadius = efficientEmpowerRadius();
            if (efficientEmpowerRadius > 0) {
                rc.empower(efficientEmpowerRadius);
            }
        }

        if (notOurEC != null) {
            if (rc.getConviction() >= 0.5 * notOurEC.conviction) {
                if (rc.canEmpower(myLoc.distanceSquaredTo(notOurEC.location))
                        && myLoc.isAdjacentTo(notOurEC.location)) {
                    rc.empower(myLoc.distanceSquaredTo(notOurEC.location));
                } else {
                    tryMove(getRoughMoveTowards(notOurEC.location, 2));
                }
            }
        }

        if (closestEnemyMuck != null) {
            int d2toMuck = myLoc.distanceSquaredTo(closestEnemyMuck.location);
            if (rc.canEmpower(9) && mucksInRange > 1) {// previously d2muck (adjacent to)
                rc.empower(9);
            } else if (rc.canEmpower(d2toMuck) && d2toMuck <= 2) {
                rc.empower(d2toMuck);
            }
            tryMove(getRoughMoveTowards(closestEnemyMuck.location, 2));
        }

        tryMove(getBestSpacing(allies));
    }

    public int efficientEmpowerRadius() {
        double bestScore = 1;
        int bestRadius = 0;
        int myConviction = rc.getConviction();
        for (int i : possibleEmpowerDists) {
            double score = empowerScore(rc.senseNearbyRobots(i), myConviction);
            if (score > bestScore) {
                bestScore = score;
                bestRadius = i;
            }
        }
        return bestRadius;
    }

    public double empowerScore(RobotInfo[] included, int politicianInfluence) {
        if (included.length == 0) {
            return 0;
        }
        Team team = rc.getTeam();
        int distributed = (int) (((politicianInfluence - 10) / included.length) * rc.getEmpowerFactor(team, 0));
        int distributedAlly = ((politicianInfluence - 10) / included.length);
        int influenceUsed = 0;
        double unitsKilled = 0;
        for (RobotInfo r : included) {
            if (r.team.equals(team)) {
                if (r.type.equals(RobotType.ENLIGHTENMENT_CENTER)) {
                    influenceUsed += distributedAlly;
                } else if (r.type.equals(RobotType.MUCKRAKER)) {
                    influenceUsed += Math.min(distributed, Math.max(r.influence * 0.7 - r.conviction, 0));
                } else {
                    influenceUsed += Math.min(distributed, r.influence - r.conviction);
                }
            } else {
                if (distributed > r.conviction) {
                    ++unitsKilled;
                }
                if (r.type.equals(RobotType.POLITICIAN)) {
                    influenceUsed += Math.min(distributed, r.influence + r.conviction);
                } else if (r.type.equals(RobotType.ENLIGHTENMENT_CENTER)) {
                    if (distributed > r.conviction) {
                        influenceUsed += r.conviction + distributedAlly * (1 - ((double) r.conviction) / distributed);
                    } else {
                        influenceUsed += distributed;
                    }
                } else {
                    influenceUsed += Math.min(distributed, r.conviction);
                }
            }
        }
        return unitsKilled * (influenceUsed + 10) / (politicianInfluence < 50 ? 16 : politicianInfluence) ;
    }

    boolean canKill(RobotInfo r) {
        int d2toUnit = rc.getLocation().distanceSquaredTo(r.location);
        return rc.canEmpower(d2toUnit) && (int) (((rc.getConviction() - 10) / rc.senseNearbyRobots(d2toUnit).length) 
                * rc.getEmpowerFactor(rc.getTeam(), 0)) > r.conviction;
    }

}
