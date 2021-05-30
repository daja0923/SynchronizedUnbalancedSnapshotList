package snapshot;

import java.util.List;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class SynchronizedUnbalancedSnapshotList<E> implements SnapshotList<E> {

    private int size = 0;
    private int currentVersion = 0;
    private final Map<Integer, TreeMap<Integer, E>> snapshotMap = new HashMap<>();

    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock readLock = rwLock.readLock();
    private final Lock writeLock = rwLock.writeLock();

    public SynchronizedUnbalancedSnapshotList() { }

    @Override
    public E getAtVersion(int index, int version) {
        readLock.lock();
        try{
            if(currentVersion < version)
                throw new IllegalArgumentException("Snapshot with version " + version + " does not exist");

            if(!snapshotMap.containsKey(index))
                throw new IllegalArgumentException("Snapshot " + version + " does not have element at index " + index);

            TreeMap<Integer, E> indexVersions = snapshotMap.get(index);
            if(indexVersions.containsKey(version)){
                return indexVersions.get(version);
            }
            Integer lowerKey = indexVersions.lowerKey(version);
            if(lowerKey == null){
                throw new IllegalArgumentException("Snapshot " + version + " does not have element at index " + index);
            }

            return indexVersions.get(lowerKey);
        }finally {
            readLock.unlock();
        }
    }

    @Override
    public int version() {
        return currentVersion;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public Iterator<E> iterator() {
        List<E> list = new LinkedList<>();;
        readLock.lock();
        try{
            for(int i = 0; i < size; i++){
                list.add(getAtVersion(i, currentVersion));
            }
        }finally {
            readLock.unlock();
        }

        return list.iterator();
    }

    @Override
    public int snapshot() {
        writeLock.lock();
        try{
            return ++currentVersion;
        }finally {
            writeLock.unlock();
        }
    }

    @Override
    public void add(E element) {
        writeLock.lock();
        try{
            TreeMap<Integer, E> indexVersions = snapshotMap.getOrDefault(size, new TreeMap<>());
            indexVersions.put(currentVersion, element);
            snapshotMap.put(size, indexVersions);
            size++;
        }finally {
            writeLock.unlock();
        }
    }

    @Override
    public void replace(int index, E element) {
        writeLock.lock();
        try{
            if(this.size <= index)
                throw new IllegalArgumentException("Current version does not have element at index " + index);
            snapshotMap.get(index).put(currentVersion, element);
        }finally {
            writeLock.unlock();
        }
    }

    @Override
    public void dropPriorSnapshots(int version) {
        writeLock.lock();
        try{
            if(version > currentVersion)
                throw new IllegalArgumentException("Cannot delete snapshot with higher than current version: " + currentVersion);

            snapshotMap
                    .keySet()
                    .forEach(index -> dropIndexPriorSnapshots(index, version));
        }finally {
            writeLock.unlock();
        }
    }

    private void dropIndexPriorSnapshots(int index, int version) {
        TreeMap<Integer, E> versionsAtIndex = snapshotMap.get(index);

        for(int ver = version - 1; ver >= 0; ver--) {
            if(versionsAtIndex.containsKey(ver)){
                if(!versionsAtIndex.containsKey(version)){
                    //Move the last snapshot prior element to the given version (if not already done) so that we don't lose data for upper versions
                    versionsAtIndex.put(version, versionsAtIndex.get(ver));
                }
                //remove any element prior to given version
                versionsAtIndex.remove(ver);
            }
        }
    }
}
