package oldeco;

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
    Deque<RobotInfo> ecsToSend = new LinkedList<RobotInfo>();

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
        }
    }

    public void run() throws GameActionException {
        MapLocation myLoc = rc.getLocation();
        rc.setFlag(0);

        RobotInfo closestEnemySlanderer = null;
        int closestSlandererDist = 1000;
        ArrayList<MapLocation> allies = new ArrayList<MapLocation>();
        for (RobotInfo r : rc.senseNearbyRobots()) {
            if (r.type.equals(RobotType.SLANDERER) && !r.team.equals(rc.getTeam())
                    && chebyshevDistance(myLoc, r.location) < closestSlandererDist) {
                closestEnemySlanderer = r;
                closestSlandererDist = chebyshevDistance(myLoc, r.location);
            }
            if (r.team.equals(rc.getTeam()) && r.type.equals(RobotType.MUCKRAKER)) {
                allies.add(r.location);
            }
            if (r.type.equals(RobotType.ENLIGHTENMENT_CENTER)) {
                if (!ecsToSend.contains(r) && r.location != spawnEC) {
                    boolean newInfo = true;
                    for (int i = 0; i < previouslySentECs.size(); ++i) {
                        RobotInfo ec = previouslySentECs.get(i);
                        if (ec.location.equals(r.location)) {
                            if (!ec.equals(r)){
                                previouslySentECs.remove(i);
                            } else {
                                newInfo = false;
                            }
                            break;
                        }
                    }
                    if(newInfo) {
                        previouslySentECs.add(r);
                        ecsToSend.add(r);
                    }            
                }
            }
        }

        if (!ecsToSend.isEmpty()) {
            FlagInfo ecFlag = new FlagInfo(ecsToSend.poll(), rc.getTeam(), spawnEC);
            rc.setFlag(ecFlag.generateFlag());
        }

        if (closestEnemySlanderer != null) {
            if (rc.canExpose(closestEnemySlanderer.location)) {
                rc.expose(closestEnemySlanderer.location);
            }
            tryMove(getRoughMoveTowards(closestEnemySlanderer.location, 2));
        }

        tryMove(getBestSpacing(allies));
    }
}
