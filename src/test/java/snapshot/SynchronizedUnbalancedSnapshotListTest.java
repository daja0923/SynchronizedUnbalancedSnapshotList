package snapshot;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class SynchronizedUnbalancedSnapshotListTest {
    private SynchronizedUnbalancedSnapshotList<Integer> snapshotList;

    @Before
    public void setup() {
        snapshotList = new SynchronizedUnbalancedSnapshotList<>();
    }

    @Test
    public void basicTest(){
        int verifyingIndex = 3;
        int verifyingVersion = 3;
        snapshotList.add(0);
        snapshotList.snapshot();
        snapshotList.snapshot();
        snapshotList.getAtVersion(0, 2);
        assertEquals(2, snapshotList.version());
        assertEquals(1, snapshotList.size());
        snapshotList.replace(0, 1);
        snapshotList.add(1);
        snapshotList.add(2);
        snapshotList.add(3);
        snapshotList.snapshot();
        snapshotList.getAtVersion(verifyingIndex, verifyingVersion);
        snapshotList.add(13);
        snapshotList.add(23);
        snapshotList.add(33);
        snapshotList.snapshot();
        snapshotList.dropPriorSnapshots(2);
        assertEquals(7, snapshotList.size());
        int res = snapshotList.getAtVersion(verifyingIndex, verifyingVersion);
        System.out.println(String.format("Value at index: %d at version %d is %d",
                verifyingIndex, verifyingVersion, res));

        for(int i = 0; i < 100000; i++){
            snapshotList.getAtVersion(verifyingIndex, verifyingVersion);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testListThrowsWhenGetAccessOnOutOfBoundIndex() {
        snapshotList.add(5);
        snapshotList.add(4);
        snapshotList.add(3);

        snapshotList.snapshot();

        snapshotList.add(0);
        snapshotList.getAtVersion(0, 3);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testListThrowsWhenNotExistentVersionAccessed() {
        snapshotList.add(5);
        snapshotList.add(4);
        snapshotList.add(3);
        assertNull(snapshotList.getAtVersion(0, 1));
    }


    @Test(expected = IllegalArgumentException.class)
    public void testDropWithGreaterThanCurrentVersion(){
        snapshotList.add(5);
        snapshotList.add(4);
        snapshotList.snapshot();
        snapshotList.add(3);
        snapshotList.snapshot();
        assertEquals(2, snapshotList.version());
        snapshotList.dropPriorSnapshots(3);
    }

    @Test
    public void testDropVersions() {
        snapshotList.add(5);
        snapshotList.add(4);
        snapshotList.snapshot();
        snapshotList.replace(0, 15);
        snapshotList.add(3);

        assertEquals(5, snapshotList.getAtVersion(0, 0).intValue());
        assertEquals(15, snapshotList.getAtVersion(0, 1).intValue());
        assertEquals(2, snapshotList.snapshot());
        assertEquals(2, snapshotList.version());


        //verify after dropping versions below 0 and 1, version 1's value is in effect to upper versions
        assertEquals(15, snapshotList.getAtVersion(0, 1).intValue());
        assertEquals(15, snapshotList.getAtVersion(0, 2).intValue());
        snapshotList.dropPriorSnapshots(2);
        assertEquals(15, snapshotList.getAtVersion(0, 2).intValue());

        snapshotList.snapshot();
        snapshotList.snapshot();
        assertEquals(4, snapshotList.version());
        assertEquals(15, snapshotList.getAtVersion(0, 4).intValue());

    }

    @Test
    public void testLifeCycle() {
        assertEquals(0, snapshotList.version());
        snapshotList.add(5);
        snapshotList.add(4);
        snapshotList.add(3);

        snapshotList.snapshot();
        snapshotList.snapshot();

        snapshotList.replace(0, 3);
        snapshotList.replace(2, 1);

        snapshotList.snapshot();

        snapshotList.add(0);
        snapshotList.add(3);

        snapshotList.replace(4, 1);

        snapshotList.snapshot();

        snapshotList.add(2);

        snapshotList.snapshot();
        snapshotList.snapshot();
        snapshotList.snapshot();
        snapshotList.snapshot();

        snapshotList.replace(0, 13);
        snapshotList.replace(1, 15);

        snapshotList.snapshot();

        snapshotList.replace(1, 25);
        snapshotList.replace(2, 3);

        assertEquals(10, snapshotList.snapshot());

        assertEquals(10, snapshotList.version());


        //verify index 0
        assertEquals(5, snapshotList.getAtVersion(0, 0).intValue());
        assertEquals(5, snapshotList.getAtVersion(0, 1).intValue());
        for(int ver = 2; ver < 8; ver++) {
            assertEquals(3, snapshotList.getAtVersion(0, ver).intValue());
        }
        for(int ver = 8; ver <= 10; ver++) {
            assertEquals(13, snapshotList.getAtVersion(0, ver).intValue());
        }


        //verify index 1

        for(int ver = 0; ver < 8; ver++){
            assertEquals(4, snapshotList.getAtVersion( 1, ver).intValue());
        }

        assertEquals(15, snapshotList.getAtVersion(1, 8).intValue());
        assertEquals(25, snapshotList.getAtVersion(1, 9).intValue());
        assertEquals(25, snapshotList.getAtVersion(1, 10).intValue());

        //verify index 2
        assertEquals(3, snapshotList.getAtVersion(2, 0).intValue());
        assertEquals(3, snapshotList.getAtVersion(2, 1).intValue());
        for(int i = 2; i < 9; i++){
            assertEquals(1, snapshotList.getAtVersion(2, i).intValue());
        }
        assertEquals(3, snapshotList.getAtVersion(2, 9).intValue());
        assertEquals(3, snapshotList.getAtVersion(2, 10).intValue());

        //verify index 3, 4, 5
        assertEquals(0, snapshotList.getAtVersion(3, 3).intValue());
        assertEquals(1, snapshotList.getAtVersion(4, 3).intValue());
        for(int ver = 4; ver <= 10; ver++){
            assertEquals(0, snapshotList.getAtVersion(3, ver).intValue());
            assertEquals(1, snapshotList.getAtVersion(4, ver).intValue());
            assertEquals(2, snapshotList.getAtVersion(5, ver).intValue());
        }


        //delete versions prior to 5
        snapshotList.dropPriorSnapshots(5);
        try {
            snapshotList.getAtVersion(0, 4);
            fail("Must throw IllegalArgumentException");
        } catch (IllegalArgumentException ignored){}


        //verify index 0
        for(int ver = 5; ver < 8; ver++) {
            assertEquals(3, snapshotList.getAtVersion(0, ver).intValue());
        }
        for(int ver = 8; ver <= 10; ver++) {
            assertEquals(13, snapshotList.getAtVersion(0, ver).intValue());
        }


        //verify index 1

        for(int ver = 5; ver < 8; ver++){
            assertEquals(4, snapshotList.getAtVersion( 1, ver).intValue());
        }

        assertEquals(15, snapshotList.getAtVersion(1, 8).intValue());
        assertEquals(25, snapshotList.getAtVersion(1, 9).intValue());
        assertEquals(25, snapshotList.getAtVersion(1, 10).intValue());

        //verify index 2
        for(int ver = 5; ver < 9; ver++){
            assertEquals(1, snapshotList.getAtVersion(2, ver).intValue());
        }
        assertEquals(3, snapshotList.getAtVersion(2, 9).intValue());
        assertEquals(3, snapshotList.getAtVersion(2, 10).intValue());

        //verify index 3, 4, 5
        for(int ver = 5; ver <= 10; ver++){
            assertEquals(0, snapshotList.getAtVersion(3, ver).intValue());
            assertEquals(1, snapshotList.getAtVersion(4, ver).intValue());
            assertEquals(2, snapshotList.getAtVersion(5, ver).intValue());
        }

        assertEquals(10, snapshotList.version());

    }

    @Test
    public void testGetAtVersionPerformance(){
        int verifyingIndex = 3;
        int verifyingVersion = 3;
        snapshotList.add(0);
        snapshotList.snapshot();
        snapshotList.snapshot();
        snapshotList.add(1);
        snapshotList.add(2);
        snapshotList.snapshot();
        snapshotList.add(3);
        assertEquals(4, snapshotList.snapshot());
        assertEquals(4, snapshotList.version());
        snapshotList.replace(3, 10000);
        snapshotList.snapshot();
        snapshotList.snapshot();
        snapshotList.add(13);
        snapshotList.snapshot();
        snapshotList.add(23);
        snapshotList.add(33);
        snapshotList.snapshot();
        snapshotList.snapshot();
        snapshotList.snapshot();
        snapshotList.snapshot();

        snapshotList.add(23);
        snapshotList.add(54);
        snapshotList.snapshot();

        long thresholdMillis = 300;
        int times = 10000000;
        long startTime = System.currentTimeMillis();
        for(int i = 0; i < times; i++){
            snapshotList.getAtVersion(verifyingIndex, verifyingVersion);
        }
        long endTime = System.currentTimeMillis();
        System.out.println(String.format("Time took for executing 'getAtVersion()' %d times: %d millis", times, endTime - startTime));
        assertTrue(endTime - startTime < thresholdMillis);
    }
}
