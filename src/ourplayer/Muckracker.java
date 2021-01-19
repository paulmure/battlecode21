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
    boolean[] foundWall = new boolean[4]; //N, E, S, W

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
                    isHunter = true;
                    targetEC = new FlagInfo(flag, rc.getTeam(), spawnEC).targetInfo;
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

        //look for walls
        for(int i = 0; i < 4; ++i){
            if(!foundWall[i]){
                MapLocation loc = null;
                Direction awayFromWall = Direction.CENTER;
                switch(i){
                    case 0: //NORTH
                        loc = rc.getLocation().translate(0, 5);
                        awayFromWall = Direction.SOUTH;
                        break;
                    case 1: //EAST
                        loc = rc.getLocation().translate(5, 0);
                        awayFromWall = Direction.WEST;
                        break;
                    case 2: //SOUTH
                        loc = rc.getLocation().translate(0, -5);
                        awayFromWall = Direction.NORTH;
                        break;
                    case 3: //WEST
                        loc = rc.getLocation().translate(-5, 0);
                        awayFromWall = Direction.EAST;
                }
                
                if(!rc.onTheMap(loc)){
                    while(!rc.onTheMap(loc.add(awayFromWall))){
                        loc = loc.add(awayFromWall);
                    }
                    foundWall[i] = true;
                    flagsToSend.add(new FlagInfo(true, rc.getTeam(), loc, spawnEC, i));//hacky lmao
                    //System.out.println("Found wall at " + loc);
                }
            }
        }

        ArrayList<MapLocation> allies = new ArrayList<MapLocation>();
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

        tryMove(getBestSpacing(allies));
        // Direction bestMove = getBestCloud(getPossibleMoves(), spawnEC, 0, allies);
        // if (bestMove != null) {
        // tryMove(bestMove);
        // }
    }
}
