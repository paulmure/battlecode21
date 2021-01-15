package stinkybot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import battlecode.common.*;

public strictfp class RobotPlayer {
    static RobotController rc;

    static final RobotType[] spawnableRobot = { RobotType.POLITICIAN, RobotType.SLANDERER, RobotType.MUCKRAKER, };

    static final Direction[] directions = { Direction.NORTH, Direction.NORTHEAST, Direction.EAST, Direction.SOUTHEAST,
            Direction.SOUTH, Direction.SOUTHWEST, Direction.WEST, Direction.NORTHWEST, };

    static int turnCount;
    static Set<Integer> builtMuckrakers;
    static Direction toBuildDirection = Direction.NORTH;
    static MapLocation enemyEC;

    static MapLocation exploreLoc;
    static MapLocation spawnEC;
    static int spawnECid;
    static double ecPassability;
    static boolean imptFlagSet = false;

    /**
     * run() is the method that is called when a robot is instantiated in the
     * Battlecode world. If this method returns, the robot dies!
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        // This is the RobotController object. You use it to perform actions from this
        // robot,
        // and to get information on its current status.
        RobotPlayer.rc = rc;

        turnCount = 0;
        enemyEC = null;

        switch (rc.getType()) {
            case ENLIGHTENMENT_CENTER:
                builtMuckrakers = new HashSet<>();

                break;
            case POLITICIAN:

                break;
            case SLANDERER:

                break;
            case MUCKRAKER:
                RobotInfo[] nearby = rc.senseNearbyRobots();
                for (int i = 0; i < nearby.length; i++) {
                    // This WILL break if there is more than one EC next to
                    if (nearby[i].type == RobotType.ENLIGHTENMENT_CENTER) {
                        spawnEC = nearby[i].location;
                        spawnECid = nearby[i].ID;
                    }
                }

                if (spawnEC != null) {

                    exploreLoc = new MapLocation(100 * (rc.getLocation().x - spawnEC.x) + spawnEC.x,
                            100 * (rc.getLocation().y - spawnEC.y) + spawnEC.y);

                }

                ecPassability = rc.sensePassability(spawnEC);
                break;
        }

        // System.out.println("I'm a " + rc.getType() + " and I just got created!");
        while (true) {
            turnCount++;
            // Try/catch blocks stop unhandled exceptions, which cause your robot to freeze

            try {
                // Here, we've separated the controls into a different method for each
                // RobotType.
                // You may rewrite this into your own control structure if you wish.
                // System.out.println("I'm a " + rc.getType() + "! Location " +
                // rc.getLocation());
                switch (rc.getType()) {
                    case ENLIGHTENMENT_CENTER:
                        runEnlightenmentCenter();
                        break;
                    case POLITICIAN:
                        runPolitician();
                        break;
                    case SLANDERER:
                        runSlanderer();
                        break;
                    case MUCKRAKER:
                        runMuckraker();
                        break;
                }

                // Clock.yield() makes the robot wait until the next turn, then it will perform
                // this loop again
                Clock.yield();

            } catch (Exception e) {
                // System.out.println(rc.getType() + " Exception");
                e.printStackTrace();
            }
        }
    }

    static void runEnlightenmentCenter() throws GameActionException {
        for (int id : builtMuckrakers) {
            if (rc.canGetFlag(id) && rc.getFlag(id) != 0) {
                int flag = rc.getFlag(id);
                if (!imptFlagSet && ((flag >>> 23) & 1) == 1) {
                    imptFlagSet = true;
                    rc.setFlag(flag);
                } else {
                    rc.setFlag(flag);
                }
            }
        }

        for (

        RobotInfo bobotInfo : rc.senseNearbyRobots()) {
            builtMuckrakers.add(bobotInfo.ID);
        }

        if (rc.canBuildRobot(RobotType.MUCKRAKER, toBuildDirection, 1)) {
            rc.buildRobot(RobotType.MUCKRAKER, toBuildDirection, 1);
            toBuildDirection = toBuildDirection.rotateRight();
        }

        if (rc.canBid(rc.getInfluence())) {
            rc.bid(rc.getInfluence());
        }
    }

    static void runPolitician() throws GameActionException {
        Team enemy = rc.getTeam().opponent();
        int actionRadius = rc.getType().actionRadiusSquared;
        RobotInfo[] attackable = rc.senseNearbyRobots(actionRadius, enemy);
        if (attackable.length != 0 && rc.canEmpower(actionRadius)) {
            // System.out.println("empowering...");
            rc.empower(actionRadius);
            // System.out.println("empowered");
            return;
        }
        if (tryMove(randomDirection()))
            ;
        // System.out.println("I moved!");
    }

    static void runSlanderer() throws GameActionException {
        if (tryMove(randomDirection()))
            ;
        // System.out.println("I moved!");
    }

    static void runMuckraker() throws GameActionException {

        int actionRadius = rc.getType().actionRadiusSquared;

        RobotInfo[] targets = rc.senseNearbyRobots(actionRadius);
        RobotInfo target = null;

        if (rc.canGetFlag(spawnECid) && rc.getFlag(spawnECid) != 0) {
            exploreLoc = flagToLoc(rc.getFlag(spawnECid), spawnEC);
        }

        for (RobotInfo potentialTarget : targets) {
            if (potentialTarget.getTeam() == rc.getTeam().opponent()) {
                if (potentialTarget.getType() == RobotType.SLANDERER
                        || potentialTarget.getType() == RobotType.ENLIGHTENMENT_CENTER) {
                    int flag = locToFlag(spawnEC, potentialTarget.getLocation());
                    if (potentialTarget.getType() == RobotType.ENLIGHTENMENT_CENTER) {
                        flag |= (1 << 23);
                    }
                    rc.setFlag(flag);
                }
                if (potentialTarget.getType() == RobotType.SLANDERER) {
                    exploreLoc = potentialTarget.location;
                    target = potentialTarget;
                }
            }
        }

        if (target != null) {
            if (rc.canExpose(target.location)) {
                rc.expose(target.location);
            }
        }

        tryMove(getRoughMoveTowards(exploreLoc, 2));

    }

    protected static ArrayList<Direction> getPossibleMoves(MapLocation loc) throws GameActionException {
        ArrayList<Direction> possibleMoves = new ArrayList<>();
        for (Direction d : Direction.values()) {
            MapLocation testLoc = loc.add(d);
            if (rc.canSenseLocation(testLoc) && !rc.isLocationOccupied(testLoc)) {
                possibleMoves.add(d);
            }
        }
        return possibleMoves;
    }

    protected static Direction getRoughMoveTowards(MapLocation target) throws GameActionException {
        return getRoughMoveTowards(target, 1);
    }

    protected static double getRoughMoveInner(MapLocation me, MapLocation target, int depth)
            throws GameActionException {
        int dx = target.x - me.x;
        int dy = target.y - me.y;
        double bestWeight = 0;
        for (Direction d : getPossibleMoves(me)) {
            double dotProduct = (dx * d.dx + dy * d.dy)
                    / (Math.sqrt(dx * dx + dy * dy) * Math.sqrt(d.dx * d.dx + d.dy * d.dy));
            double weight = rc.sensePassability(me.add(d)) * dotProduct;
            // System.out.println("legal direction " + d + "has weight " + weight);
            if (depth > 1 && dotProduct > 0) {
                weight += getRoughMoveInner(me.add(d), target, depth - 1);
            }
            if (weight > bestWeight) {
                bestWeight = weight;
            }
        }
        return bestWeight / 2;// change probably
    }

    protected static int locToFlag(MapLocation spawnEC, MapLocation target) {
        return (((target.y - spawnEC.y + 63) & 0x7f) << 7) + ((target.x - spawnEC.x + 63) & 0x7f);
    }

    protected static MapLocation flagToLoc(int flag, MapLocation spawnEC) {
        return new MapLocation(spawnEC.x + (flag & 0x7F) - 63, spawnEC.y + ((flag >> 7) & 0x7F) - 63);
    }

    protected static Direction getRoughMoveTowards(MapLocation target, int depth) throws GameActionException {
        MapLocation me = rc.getLocation();
        if (target.isAdjacentTo(me)) {
            return me.directionTo(target);
        }
        int dx = target.x - me.x;
        int dy = target.y - me.y;
        double bestWeight = 0;
        Direction bestDirection = Direction.CENTER;
        for (Direction d : getPossibleMoves(me)) {
            double dotProduct = (dx * d.dx + dy * d.dy)
                    / (Math.sqrt(dx * dx + dy * dy) * Math.sqrt(d.dx * d.dx + d.dy * d.dy));
            double weight = rc.sensePassability(me.add(d)) * dotProduct;
            // System.out.println("legal direction " + d + "has weight " + weight);
            if (depth > 1 && dotProduct > 0) {
                weight += getRoughMoveInner(me.add(d), target, depth - 1);
            }
            if (weight > bestWeight) {
                bestWeight = weight;
                bestDirection = d;
            }
        }
        return bestDirection;
    }

    /**
     * Returns a random Direction.
     *
     * @return a random Direction
     */
    static Direction randomDirection() {
        return directions[(int) (Math.random() * directions.length)];
    }

    /**
     * Returns a random spawnable RobotType
     *
     * @return a random RobotType
     */
    static RobotType randomSpawnableRobotType() {
        return spawnableRobot[(int) (Math.random() * spawnableRobot.length)];
    }

    /**
     * Attempts to move in a given direction.
     *
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryMove(Direction dir) throws GameActionException {
        // System.out.println("I am trying to move " + dir + "; " + rc.isReady() + " " +
        // rc.getCooldownTurns() + " " + rc.canMove(dir));
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        } else
            return false;
    }
}
