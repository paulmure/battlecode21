package ourplayer;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;

import battlecode.common.*;

public class Muckracker extends RobotPlayer implements RoleController {
    boolean foundPath = false;
    MapLocation loc;
    MapLocation newLoc;
    private MapLocation spawnEC;
    private int spawnECid;
    ArrayList<RobotInfo> previouslySentECs = new ArrayList<RobotInfo>();
    Deque<FlagInfo> flagsToSend = new LinkedList<FlagInfo>();
    RobotInfo targetEC = null;
    boolean isHunter = false;
    int[] walls = { -1, -1, -1, -1 }; // N, E, S, W
    Direction targetDirection = null;
    MapLocation targetLocation = null;
    int wallRepulsionDistance = 3;
    boolean orthogonal = false;

    public Muckracker() throws GameActionException {
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
            if (rc.canGetFlag(spawnECid)) {
                int flag = rc.getFlag(spawnECid);
                if (flag != 0) {
                    RobotInfo info = new FlagInfo(flag, rc.getTeam(), spawnEC).targetInfo;
                    if (info.team.equals(Team.NEUTRAL)) { // Scout
                        targetDirection = directions[(info.influence / 8) & 7];
                        targetLocation = spawnEC.translate(targetDirection.dx * 64, targetDirection.dy * 64);
                        if (directionToInt(targetDirection) % 2 == 0) {
                            orthogonal = true;
                        }
                    } else { // Hunter
                        isHunter = true;
                        targetEC = info;
                    }
                }
            }
        }
    }

    public void run() throws GameActionException {
        MapLocation myLoc = rc.getLocation();
        rc.setFlag(0);

        RobotInfo closestEnemySlanderer = null;
        RobotInfo strongestCloseSlanderer = null;
        int closestSlandererDist = 1000;

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
                    // flagsToSend.add(new FlagInfo(true, rc.getTeam(), loc, spawnEC, i * 2));//
                    // hacky lmao
                    // System.out.println("Found wall at " + loc);
                }
            }
        }

        ArrayList<MapLocation> allies = new ArrayList<MapLocation>();

        if (walls[0] != -1 && walls[0] - myLoc.y <= wallRepulsionDistance) {
            System.out.println("near north wall");
            allies.add(new MapLocation(myLoc.x, walls[0]));
        }
        if (walls[1] != -1 && walls[1] - myLoc.x <= wallRepulsionDistance) {
            System.out.println("near east wall");
            allies.add(new MapLocation(walls[1], myLoc.y));
        }
        if (walls[2] != -1 && myLoc.y - walls[2] <= wallRepulsionDistance) {
            System.out.println("near south wall");
            allies.add(new MapLocation(myLoc.x, walls[2]));
        }
        if (walls[3] != -1 && myLoc.x - walls[3] <= wallRepulsionDistance) {
            System.out.println("near west wall");
            allies.add(new MapLocation(walls[3], myLoc.y));

        }

        for (RobotInfo r : rc.senseNearbyRobots()) {
            if (r.type.equals(RobotType.SLANDERER) && !r.team.equals(rc.getTeam())) {
                if (chebyshevDistance(myLoc, r.location) < closestSlandererDist) {
                    closestEnemySlanderer = r;
                    closestSlandererDist = chebyshevDistance(myLoc, r.location);
                }
                if (r.location.distanceSquaredTo(myLoc) <= 12) {
                    if (strongestCloseSlanderer == null || r.influence > strongestCloseSlanderer.influence) {
                        strongestCloseSlanderer = r;
                    }
                }
            }
            if (r.team.equals(rc.getTeam())
                    && (r.type.equals(RobotType.MUCKRAKER) || r.type.equals(RobotType.POLITICIAN))) {
                allies.add(r.location);
            }
            if (r.type.equals(RobotType.ENLIGHTENMENT_CENTER)) {
                if (!flagsToSend.contains(r) && r.location != spawnEC) {
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
                        flagsToSend.add(new FlagInfo(r, rc.getTeam(), spawnEC));
                    }
                }
            }
        }

        if (!flagsToSend.isEmpty()) {
            rc.setFlag(flagsToSend.poll().generateFlag());
        }

        if (closestEnemySlanderer != null) {
            if (strongestCloseSlanderer != null && rc.canExpose(strongestCloseSlanderer.location)) {
                rc.expose(strongestCloseSlanderer.location);
            }
            tryMove(getRoughMoveTowards(closestEnemySlanderer.location, 2));
        }

        if (isHunter && targetEC == null && rc.canGetFlag(spawnECid)) {
            int flag = rc.getFlag(spawnECid);
            if (flag != 0) {
                RobotInfo ec = new FlagInfo(flag, rc.getTeam(), spawnEC).targetInfo;
                if (ec.team.equals(rc.getTeam().opponent())) {
                    targetEC = ec;
                }
            }
        }

        if (targetEC != null) {
            if (rc.canSenseLocation(targetEC.location)) {
                targetEC = rc.senseRobotAtLocation(targetEC.location);
                Direction bestMove = getBestCloud(getPossibleMoves(), targetEC.location, 10, allies);
                if (bestMove != null) {
                    tryMove(bestMove);
                }
            } else {
                tryMove(getRoughMoveTowards(targetEC.location, 2));
            }
        }

        if (targetLocation != null) {
            boolean exhausted = true;
            for (int i = 0; i < walls.length; ++i) {
                if (walls[i] == -1) {
                    exhausted = false;
                    break;
                }
            }
            if (exhausted) {
                targetDirection = null;
                targetLocation = null;
            } else if (orthogonal && walls[directionToInt(targetDirection) / 2] != -1) {
                if (walls[directionToInt(targetDirection.rotateLeft().rotateLeft()) / 2] == -1) {
                    targetDirection = targetDirection.rotateLeft().rotateLeft();
                    targetLocation = myLoc.translate(targetDirection.dx * 64, targetDirection.dy * 64);
                } else if (walls[directionToInt(targetDirection.rotateRight().rotateRight()) / 2] == -1) {
                    targetDirection = targetDirection.rotateRight().rotateRight();
                    targetLocation = myLoc.translate(targetDirection.dx * 64, targetDirection.dy * 64);
                } else {
                    targetDirection = null;
                    targetLocation = null;
                }
            } else if (!orthogonal && (walls[directionToInt(targetDirection.rotateLeft()) / 2] != -1
                    || walls[directionToInt(targetDirection.rotateLeft()) / 2] != -1)) {
                if (walls[directionToInt(targetDirection.rotateLeft()) / 2] != -1) {
                    targetDirection = targetDirection.rotateRight().rotateRight();
                } else {
                    targetDirection = targetDirection.rotateLeft().rotateLeft();
                }
                targetLocation = myLoc.translate(targetDirection.dx * 64, targetDirection.dy * 64);
            } 
            if (targetLocation != null) {
                tryMove(getRoughMoveTowards(targetLocation, 2));
            }
        }

        Direction bestMove = getBestCloud(getPossibleMoves(), spawnEC, 0, allies);
        if (bestMove != null) {
            tryMove(bestMove);
        }
    }
}
