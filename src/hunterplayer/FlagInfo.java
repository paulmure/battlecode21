package hunterplayer;

import javax.tools.DocumentationTool.Location;

import battlecode.common.*;

public class FlagInfo {

    // experimental
    public RobotInfo targetInfo;

    // public RobotType targType;
    // public Team targTeam;
    // public MapLocation location;

    private Team allyTeam;
    private MapLocation spawnEC;

    // CONSTRUCTOR FOR WALLS
    public FlagInfo(boolean isWall, Team allyTeam, MapLocation location, MapLocation spawnEC) {
        this.spawnEC = spawnEC;
        this.allyTeam = allyTeam;
        targetInfo = new RobotInfo(0, Team.NEUTRAL, null, 0, 0, location);
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
            // [min(2^9, influence/4)][type][LOCATIONx][LOCATIONy]
            // (24)7 3 7 7
        }

        int influence = (flag >>> 15) & 0x1fc;

        targetInfo = new RobotInfo(0, targTeam, targType, influence, influence, flagToLoc(flag, spawnEC));
        this.spawnEC = spawnEC;
        this.allyTeam = allyTeam;
    }

    protected int generateFlag() {
        int typeBits = 0;
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
                break;
            default:
                typeBits = 0b110;
        }

        // location
        int flag = (((targetInfo.location.y - spawnEC.y + 63) & 0x7f) << 7)
                | ((targetInfo.location.x - spawnEC.x + 63) & 0x7f);
        // type
        flag |= (typeBits & 0x7) << 14;

        // influence
        flag |= ((Math.min(targetInfo.influence + 3, 511) / 4) & 0x7f) << 17;
        return flag;
    }

    protected static MapLocation flagToLoc(int flag, MapLocation spawnEC) {
        return new MapLocation(spawnEC.x + ((flag & 0x7f) - 63), spawnEC.y + (((flag >>> 7) & 0x7f) - 63));
    }
}
