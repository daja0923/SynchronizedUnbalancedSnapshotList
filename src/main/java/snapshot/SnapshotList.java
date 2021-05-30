package snapshot;


/**
 * A <pre>List</pre> that offers the possibility to create snapshots of the
 * elements currently stored in it. <pre>SnapshotList</pre> may not remove
 * existing elements and it can only grow by appending elements, not inserting.
 */
public interface SnapshotList<E> extends List<E> {
    /**
     * Removes all versions prior to the specified one.
     *
     * The version must be no greater than the current version.
     */
    void dropPriorSnapshots(int version);

    /**
     * Retrieves the element at the specified position for the version.
     *
     * @param index the specified position in the list
     * @param version the specified snapshot
     */
    E getAtVersion(int index, int version);

    /**
     * Creates a snapshot of the current <pre>List</pre>.
     *
     * @return the version after the snapshot
     */
    int snapshot();

    /**
     * Indicates the version of the current <pre>SnapshotList</pre>; it may
     * also be regarded as the number of times that {@link #snapshot()} was
     * called on the instance.
     *
     * @return the version of the instance
     */
    int version();
}
