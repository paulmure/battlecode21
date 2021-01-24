package stinkierbot;

import battlecode.common.*;

public class FlagInfo {

    // experimental
    public RobotInfo targetInfo;

    // public RobotType targType;
    // public Team targTeam;
    // public MapLocation location;

    private Team allyTeam;
    private MapLocation spawnEC;

    // CONSTRUCTOR FOR WALLS/DIRECTIONS
    public FlagInfo(boolean isWall, Team allyTeam, MapLocation location, MapLocation spawnEC, int direction) {
        //direction: 0=NORTH, 1=EAST, 2=SOUTH, 3=WEST. SAVED IN "INFLUENCE"
        //direction: 0=NORTH, 1=NORTHEAST, 2=EAST, 3=SOUTHEAST, 
        //           4=SOUTH, 5=SOUTHWEST, 6=WEST, 7=NORTHWEST. SAVED IN "INFLUENCE"
        this.spawnEC = spawnEC;
        this.allyTeam = allyTeam;
        targetInfo = new RobotInfo(0, Team.NEUTRAL, null, direction*8, 0, location);
    }

    // CONSTRUCTOR FOR ROBOT --> FLAG
    public FlagInfo(RobotInfo bobot, Team allyTeam, MapLocation spawnEC) {
        // targTeam = bobot.team;
        // targType = bobot.type;
        // location = bobot.location;

        targetInfo = bobot;
        this.spawnEC = spawnEC;
        this.allyTeam = allyTeam;
    }

    // CONSTRUCTOR FOR FLAG --> ROBOT
    public FlagInfo(int flag, Team allyTeam, MapLocation spawnEC) {

        RobotType targType = null;
        Team targTeam = null;

        int targTypeBits = (flag >>> 14) & 0x7;
        switch (targTypeBits) {
            case 0:
                targType = RobotType.ENLIGHTENMENT_CENTER;
                targTeam = allyTeam.opponent();
                break;

            case 1:
                targType = RobotType.ENLIGHTENMENT_CENTER;
                targTeam = allyTeam;
                break;

            case 2:
                targType = RobotType.ENLIGHTENMENT_CENTER;
                targTeam = Team.NEUTRAL;
                break;

            case 3:
                targType = RobotType.MUCKRAKER;
                targTeam = allyTeam.opponent();
                break;

            case 4:
                targType = RobotType.POLITICIAN;
                targTeam = allyTeam.opponent();
                break;

            case 5:
                targType = RobotType.SLANDERER;
                targTeam = allyTeam.opponent();
                break;

            case 6:
                targType = null;
                targTeam = Team.NEUTRAL;
                break;

            // 000 EnemyEC
            // 001 AlliedEC
            // 010 NeutralEC
            // 011 Muckraker
            // 100 Politician
            // 101 Slanderer
            // 110 Wall
            //
            //
            // [isrobotinfo][min(2^9, influence/4)][type][LOCATIONx][LOCATIONy]
            // (24) 1 6 3 7 7
        }

        int influence = (flag >>> 14) & 0x1f8;

        targetInfo = new RobotInfo(0, targTeam, targType, influence, influence, flagToLoc(flag, spawnEC));
        this.spawnEC = spawnEC;
        this.allyTeam = allyTeam;
    }

    protected int generateFlag() {
        int typeBits = 0b110; //wall
        if(targetInfo.type != null){
            switch (targetInfo.type) {
                case ENLIGHTENMENT_CENTER:
                    if (targetInfo.team == allyTeam.opponent()) {
                        typeBits = 0b000;
                    } else if (targetInfo.team == allyTeam) {
                        typeBits = 0b001;
                    } else {
                        typeBits = 0b010;
                    }
                    break;
                case MUCKRAKER:
                    typeBits = 0b011;
                    break;
                case POLITICIAN:
                    typeBits = 0b100;
                    break;
                case SLANDERER:
                    typeBits = 0b101;      
            }
        } 

        // location
        int flag = (((targetInfo.location.y - spawnEC.y + 63) & 0x7f) << 7)
                | ((targetInfo.location.x - spawnEC.x + 63) & 0x7f);
        // type
        flag |= (typeBits & 0x7) << 14;

        // influence
        flag |= ((Math.min(targetInfo.influence + 7, 511) / 8) & 0x3f) << 17;
        return flag;
    }

    protected static MapLocation flagToLoc(int flag, MapLocation spawnEC) {
        return new MapLocation(spawnEC.x + ((flag & 0x7f) - 63), spawnEC.y + (((flag >>> 7) & 0x7f) - 63));
    }
}
