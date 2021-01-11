package ourplayer;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Test;
import battlecode.common.*;

public class flagsTest {

    private void testSingleLoc(MapLocation spawnEC, MapLocation target) {
        int flag = RobotPlayer.locToFlag(spawnEC, target);
        MapLocation recovered = RobotPlayer.flagToLoc(flag, spawnEC);
        if (!target.equals(recovered)) {
            System.out.println("SpawnEC: " + spawnEC);
            System.out.println("target: " + target);
            System.out.println("recovered: " + recovered);
        }
        Assert.assertEquals(target, recovered);
    }

	@Test
	public void testFlags() {
        MapLocation spawnEC = new MapLocation(11, 24);
        testSingleLoc(spawnEC, new MapLocation(31, 12));
        testSingleLoc(spawnEC, new MapLocation(11, 24));
        testSingleLoc(spawnEC, new MapLocation(0, 0));
        testSingleLoc(spawnEC, new MapLocation(73, 0));

        spawnEC = new MapLocation(2003, 3000);
        testSingleLoc(spawnEC, new MapLocation(1987, 3044));
        testSingleLoc(spawnEC, new MapLocation(2050, 3011));
        testSingleLoc(spawnEC, new MapLocation(1999, 3001));

        spawnEC = new MapLocation(0, 0);
        testSingleLoc(spawnEC, new MapLocation(64, 64));
        spawnEC = new MapLocation(64, 64);
        testSingleLoc(spawnEC, new MapLocation(1, 1));
	}
}
