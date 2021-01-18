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

    public Politician() throws GameActionException {
        spawnRound = rc.getRoundNum();
        // set spawnEC
        if (spawnEC == null) {
            RobotInfo[] nearby = rc.senseNearbyRobots();
            for (int i = 0; i < nearby.length; i++) {
                // This WILL break if there is more than one EC next to
                if (nearby[i].type == RobotType.ENLIGHTENMENT_CENTER && (spawnEC == null || nearby[i].location
                        .distanceSquaredTo(rc.getLocation()) < spawnEC.distanceSquaredTo(rc.getLocation()))) {
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

    private boolean nearbyEnemy() {
        Team enemy = rc.getTeam().opponent();

        // change radius
        int actionRadius = rc.getType().actionRadiusSquared;
        RobotInfo[] attackable = rc.senseNearbyRobots(actionRadius, enemy);
        if (attackable.length != 0 && rc.canEmpower(actionRadius)) {
            return true;
        }
        return false;
    }

    public void run() throws GameActionException {
        if (rc.getRoundNum() >= 1495 && rc.getTeamVotes() < 750 && rc.getRobotCount() > 100) { // cause it would suck to take the map at the end
            if (rc.canEmpower(9)) { // and not kill 2 1hp politicians and lose on votes
                rc.empower(9);
            }
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

        int mucksInRange = 0;
        RobotInfo closestEnemyMuck = null;
        int closestMuckDist = 1000;
        RobotInfo notOurEC = null;
        int closestECDist = 1000;
        ArrayList<MapLocation> allies = new ArrayList<MapLocation>();
        ArrayList<MapLocation> nearbyAllies = new ArrayList<MapLocation>();
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
            }
            // save allies for this turn only
            if (r.team.equals(rc.getTeam()) && r.type.equals(RobotType.POLITICIAN)) {
                allies.add(r.location);
                if (r.location.distanceSquaredTo(myLoc) <= 8) {
                    nearbyAllies.add(r.location);
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
                if (targetEC != null && targetEC.location.equals(r.location)) {
                    if (r.team.equals(rc.getTeam())) {
                        targetEC = null;
                    } else {
                        targetEC = r;
                    }
                }
            }
        }

        if (!ecsToSend.isEmpty()) {
            FlagInfo ecFlag = new FlagInfo(ecsToSend.poll(), rc.getTeam(), spawnEC);
            rc.setFlag(ecFlag.generateFlag());
        }

        if (notOurEC != null && (targetEC == null || !notOurEC.location.equals(targetEC.location))) {
            if (rc.getConviction() >= 0.5 * notOurEC.conviction) {
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

        if (targetEC != null) {
            int d2toTarget = myLoc.distanceSquaredTo(targetEC.location);
            if (rc.canEmpower(d2toTarget)) {
                if (rc.senseNearbyRobots(d2toTarget).length == 1) {
                    rc.empower(d2toTarget);
                } else if (targetEC.team.equals(Team.NEUTRAL) && turnsWaited >= neutralTurnsToWait) {
                    rc.empower(d2toTarget);
                } else if (targetEC.team.equals(rc.getTeam().opponent()) 
                        && (turnsWaited >= enemyTurnsToWait || d2toTarget == 1)) {
                    rc.empower(d2toTarget);
                } else {
                    ++turnsWaited;
                }
            } else {
                turnsWaited = 0;
            }
            if (targetEC.team.equals(Team.NEUTRAL)){
                tryMove(getRoughMoveTowards(targetEC.location, 2));
            } else {
                tryMove(getRoughMoveAvoid(targetEC.location, 2, nearbyAllies));
            }
            return;
        }

        int buffer = 3;
        if (closestEnemyMuck != null) {
            int d2toMuck = myLoc.distanceSquaredTo(closestEnemyMuck.location);
            if (rc.canEmpower(9) && mucksInRange > 1) {// previously d2muck (adjacent to)
                rc.empower(9);
            } else if (spawnEC.distanceSquaredTo(closestEnemyMuck.location) <= getTargetRadius()
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

}
