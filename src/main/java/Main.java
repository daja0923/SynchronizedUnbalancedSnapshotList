import snapshot.SnapshotList;
import snapshot.SynchronizedUnbalancedSnapshotList;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        SynchronizedUnbalancedSnapshotList<Integer> snapshotList =  new SynchronizedUnbalancedSnapshotList<>();
        AtomicInteger verifyingIndex = new AtomicInteger(3);
        AtomicInteger verifyingVersion = new AtomicInteger(3);

        Runnable runnableTask = () -> {
            snapshotList.add(0);
            snapshotList.snapshot();
            snapshotList.snapshot();
            snapshotList.version();
            snapshotList.add(1);
            snapshotList.add(2);
            snapshotList.add(3);
            snapshotList.snapshot();
            snapshotList.add(13);
            snapshotList.add(23);
            snapshotList.add(33);
            snapshotList.snapshot();
            snapshotList.dropPriorSnapshots(2);

            int res = snapshotList.getAtVersion(verifyingIndex.get(), verifyingVersion.get());
            System.out.println(String.format("Value at index: %d at version %d is %d",
                    verifyingIndex.intValue(), verifyingVersion.intValue(), res));

            verifyingVersion.compareAndSet(3, snapshotList.version());
            if(verifyingVersion.get() != 3)
                snapshotList.replace(3, 15);

            res = snapshotList.getAtVersion(verifyingIndex.intValue(), verifyingVersion.intValue());
            System.out.println(String.format("Value at index: %d at version %d is %d",
                    verifyingIndex.intValue(), verifyingVersion.intValue(), res));
        };
        for(int i = 0; i < 100; i++){
            Thread thread = new Thread(runnableTask);
            thread.start();
            thread.join();
        }
    }
}
