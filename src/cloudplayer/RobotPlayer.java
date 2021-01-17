package cloudplayer;

import battlecode.common.*;
import java.util.ArrayList;

interface RoleController {
    public void run() throws GameActionException;
}

public strictfp class RobotPlayer {
    static RobotController rc;

    static final RobotType[] spawnableRobot = { RobotType.POLITICIAN, RobotType.SLANDERER, RobotType.MUCKRAKER, };

    static final Direction[] directions = { Direction.NORTH, Direction.NORTHEAST, Direction.EAST, Direction.SOUTHEAST,
            Direction.SOUTH, Direction.SOUTHWEST, Direction.WEST, Direction.NORTHWEST, };

    static int turnCount;

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

        RoleController controller;

        switch (rc.getType()) {
            case ENLIGHTENMENT_CENTER:
                controller = new EnlightenmentCenter();
                break;
            case POLITICIAN:
                // System.out.println("Initializing politician from here for some reason");
                // System.out.println("LOC: "+ rc.getLocation());
                controller = new Politician();
                break;
            case MUCKRAKER:
                controller = new Muckracker();
                break;
            case SLANDERER:
                controller = new Slanderer();
                break;
            default:
                return;
        }

        turnCount = 0;

        RobotType lastRoundType = rc.getType();
        // System.out.println("I'm a " + rc.getType() + " and I just got created!");
        while (true) {
            ++turnCount;

            // Try/catch blocks stop unhandled exceptions, which cause your robot to freeze
            try {
                // verify that type has not changed
                if (rc.getType() != lastRoundType) {

                    switch (rc.getType()) {
                        case ENLIGHTENMENT_CENTER:
                            controller = new EnlightenmentCenter();
                            break;
                        case POLITICIAN:
                            if (controller instanceof Slanderer) {
                                // System.out.println("passing ec info: " + ((Slanderer)
                                // controller).getSpawnEc());
                                controller = new Politician(((Slanderer) controller).getSpawnEc(),
                                        ((Slanderer) controller).getSpawnEcId(),
                                        ((Slanderer) controller).getEcPassability());
                            } else {
                                // System.out.println("Controller was not Instance of Slanderer. was
                                // "+controller);
                                controller = new Politician();
                            }
                            break;
                        case MUCKRAKER:
                            controller = new Muckracker();
                            break;
                        case SLANDERER:
                            controller = new Slanderer();
                            break;
                        default:
                            controller = null;
                            break;
                    }

                }
                // Here, we've separated the controls into a different method for each
                // RobotType.
                // You may rewrite this into your own control structure if you wish.
                // System.out.println("I'm a " + rc.getType() + "! Location " +
                // rc.getLocation());
                controller.run();
                // Clock.yield() makes the robot wait until the next turn, then it will perform
                // this loop again
                lastRoundType = rc.getType();
                Clock.yield();

            } catch (Exception e) {
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();
            }
        }
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

    static boolean tryBuildRobot(RobotType type, Direction dir, int influence) throws GameActionException {
        if (rc.canBuildRobot(type, dir, influence)) {
            rc.buildRobot(type, dir, influence);
            return true;
        } else {
            return false;
        }
    }

    int getECIncome() {
        return ((int) Math.sqrt(rc.getRoundNum())) / 5;
    }

    int chebyshevDistance(MapLocation a, MapLocation b) {
        return Math.max(Math.abs(a.x - b.x), Math.abs(a.y - b.y));
    }

    protected boolean strictFollowPath(ArrayList<Direction> path) throws GameActionException {
        if (path.size() > 0 && rc.canMove(path.get(0))) {
            rc.move(path.remove(0));
            return true;
        }
        return false;
    }

    protected Direction getRoughMoveTowards(MapLocation target) throws GameActionException {
        return getRoughMoveTowards(target, 1);
    }

    protected Direction getRoughMoveTowards(MapLocation target, int depth) throws GameActionException {
        MapLocation me = rc.getLocation();
        if (target.isAdjacentTo(me)) {
            return me.directionTo(target);
        }
        int dx = target.x - me.x;
        int dy = target.y - me.y;
        double bestWeight = 0;
        Direction bestDirection = Direction.CENTER;
        for (Direction d : getPossibleMoves()) {
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

    protected double getRoughMoveInner(MapLocation me, MapLocation target, int depth) throws GameActionException {
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

    protected boolean isOnHwy(MapLocation check, MapLocation ec) {
        // y = x
        // line defines northeast and southwest hwy
        if (Math.abs(check.x - ec.x) == Math.abs(check.y - ec.y)) {
            return true;
        }
        return false;
    };

    protected ArrayList<Direction> getPossibleMoves() {
        return getPossibleMoves(true, null);
    }

    protected ArrayList<Direction> getPossibleMoves(boolean highwayEnab, MapLocation ec) {
        ArrayList<Direction> possibleMoves = new ArrayList<>();
        for (Direction d : Direction.values()) {
            if (rc.canMove(d)) {
                // if (!highwayEnab) {
                // if (!isOnHwy(new MapLocation(rc.getLocation().x + d.dx, rc.getLocation().y +
                // d.dy), ec)) {
                // possibleMoves.add(d);
                // }
                // } else {
                // possibleMoves.add(d);
                // }
                possibleMoves.add(d);
            }
        }
        return possibleMoves;
    }

    protected ArrayList<Direction> getPossibleMoves(MapLocation loc) {
        ArrayList<Direction> possibleMoves = new ArrayList<>();
        for (Direction d : Direction.values()) {
            MapLocation testLoc = loc.add(d);
            if (rc.canSenseLocation(testLoc)) {
                possibleMoves.add(d);
            }
        }
        return possibleMoves;
    }

    protected Direction getBestSpacing(ArrayList<MapLocation> allies) {
        MapLocation myLoc = rc.getLocation();
        
        double totalDX = 0;
        double totalDY = 0;
        for (MapLocation loc : allies) {
            int dx = loc.x - myLoc.x;
            int dy = loc.y - myLoc.y;

            double forceVecMagicNumber = Math.pow(dx*dx + dy*dy, 3.0/2);
            totalDX +=dx/forceVecMagicNumber;
            totalDY += dy/forceVecMagicNumber;
        }

        double bestDot = 0.0;
        Direction bestDir = randomDirection();
        for(Direction d : getPossibleMoves()) {
            //dot product for closest match??
            double dot = -(d.dx * totalDX + d.dy * totalDY);
            if (dot > bestDot) {
                bestDot = dot;
                bestDir = d;
            }
        }
        return bestDir;
    }

    protected Direction getBestCloud(ArrayList<Direction> possibleMoves, MapLocation spawnEC, double targetRadius,
        ArrayList<MapLocation> allies) {
        // ~~~~~~~~ MODIFY THESE TO CHANGE PRIORITIZATION ~~~~~~~~~//
        // double kRadius = 0.5f;
        // double kClockwise = 0.5f;
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//

        // System.out.println("spawnEC: "+spawnEC);
        int ecVecX = rc.getLocation().x - spawnEC.x;
        int ecVecY = rc.getLocation().y - spawnEC.y;
        double ecVecLength = Math.sqrt(ecVecX * ecVecX + ecVecY * ecVecY);

        int targetVecX = 0;
        int targetVecY = 0;
        double targetVecLength = 1;

        MapLocation myLoc = rc.getLocation();
        
        double totalDX = 0;
        double totalDY = 0;
        for (MapLocation loc : allies) {
            int dx = loc.x - myLoc.x;
            int dy = loc.y - myLoc.y;

            double forceVecMagicNumber = Math.pow(dx*dx + dy*dy, 3.0/2);
            totalDX +=dx/forceVecMagicNumber;
            totalDY += dy/forceVecMagicNumber;
        }
        // a standing weight of 1 treats standing still as a perfectly perpendicular
        // move, 0 is a perfectly parallel
        double bestWeight = Integer.MIN_VALUE;
        Direction bestDirection = null;

        for (Direction d : possibleMoves) {

            // System.out.println("DIRECTION: "+d);
            // System.out.println("targetX: " + targetVecX + " dx: " + d.dx);
            // System.out.println("targetY: " + targetVecY + " dy: " + d.dy);
            targetVecX = d.dx;
            targetVecY = d.dy;
            targetVecLength = Math.sqrt(targetVecX * targetVecX + targetVecY * targetVecY);

            // to calculate r^2 to given move, use the current pos (ecVec) and add the
            // move's vector (targetVec), then r^2 it
            int moveR2 = (int) (Math.pow(ecVecX + targetVecX, 2) + Math.pow(ecVecY + targetVecY, 2));
            double deltaR2 = Math.abs(targetRadius - moveR2);
            // equation here for adjustment :
            // y = 1 / (ax + 1)
            // adjust a to produce steeper or smoother down
            double r2Weight = 1 / (1 + deltaR2); // (0, 1]

            double totalWeight = r2Weight + -0.5*(d.dx * totalDX + d.dy * totalDY);

            if (totalWeight > bestWeight) {
                bestWeight = totalWeight;
                bestDirection = d;
            }
        }
        return bestDirection;

        // step 1: turn directions into weighted edge graph where weight = desirablility
        // of possible move, weight (1 ... -1)

        // step 2: weight graph including a 0 cost path to itself
        // step 3: choose path of lowest cost
        // will prioritize standing still over moving backwards

        // obstacles:
        // easy: prioritize radius
        // hard?: weight moves by how clockwise they are (negative for backwards)
        // "perpendicularity" of vector to ec and vector to move
        // cross product
    }


    // Get best possible move to form a vortex
    // currently implements a switch back and forth in
    // the direction of rotation, from widdershins to deisul
    protected Direction getBestVortex(ArrayList<Direction> possibleMoves, MapLocation spawnEC, double targetRadius,
            double standingWeight) {

        // System.out.println("spawnEC: "+spawnEC);
        int ecVecX = rc.getLocation().x - spawnEC.x;
        int ecVecY = rc.getLocation().y - spawnEC.y;
        double ecVecLength = Math.sqrt(ecVecX * ecVecX + ecVecY * ecVecY);

        int targetVecX = 0;
        int targetVecY = 0;
        double targetVecLength = 1;
        // a standing weight of 1 treats standing still as a perfectly perpendicular
        // move, 0 is a perfectly parallel
        double bestWeight = standingWeight / (1 + (Math.abs(targetRadius - (ecVecX * ecVecX + ecVecY * ecVecY))));
        Direction bestDirection = null;

        for (Direction d : possibleMoves) {

            // System.out.println("DIRECTION: "+d);
            // System.out.println("targetX: " + targetVecX + " dx: " + d.dx);
            // System.out.println("targetY: " + targetVecY + " dy: " + d.dy);
            targetVecX = d.dx;
            targetVecY = d.dy;
            targetVecLength = Math.sqrt(targetVecX * targetVecX + targetVecY * targetVecY);

            // to calculate r^2 to given move, use the current pos (ecVec) and add the
            // move's vector (targetVec), then r^2 it
            int moveR2 = (int) (Math.pow(ecVecX + targetVecX, 2) + Math.pow(ecVecY + targetVecY, 2));
            double deltaR2 = Math.abs(targetRadius - moveR2);
            // equation here for adjustment :
            // y = 1 / (ax + 1)
            // adjust a to produce steeper or smoother down
            double r2Weight = 1 / (1 + deltaR2); // (0, 1]

            double crossProduct = (rc.getRoundNum() % 420 < 210 ? 1 : -1) * (ecVecX * targetVecY - ecVecY * targetVecX)
                    / (ecVecLength * targetVecLength); // (-1, 1)

            double totalWeight = r2Weight * crossProduct;

            if (totalWeight > bestWeight) {
                bestWeight = totalWeight;
                bestDirection = d;
            }
        }
        return bestDirection;

        // step 1: turn directions into weighted edge graph where weight = desirablility
        // of possible move, weight (1 ... -1)

        // step 2: weight graph including a 0 cost path to itself
        // step 3: choose path of lowest cost
        // will prioritize standing still over moving backwards

        // obstacles:
        // easy: prioritize radius
        // hard?: weight moves by how clockwise they are (negative for backwards)
        // "perpendicularity" of vector to ec and vector to move
        // cross product
    }

    protected static int locToFlag(MapLocation spawnEC, MapLocation target) {
        return (((target.y - spawnEC.y + 63) & 0x7f) << 7) | ((target.x - spawnEC.x + 63) & 0x7f);
    }

    protected static MapLocation flagToLoc(int flag, MapLocation spawnEC) {
        return new MapLocation(spawnEC.x + ((flag % 0x80) - 63), spawnEC.y + ((flag >>> 7) - 63));
    }

}
