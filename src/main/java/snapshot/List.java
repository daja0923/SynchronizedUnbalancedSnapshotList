package snapshot;

import java.util.Iterator;

public interface List<E> {
    /**
     * Adds element to the end of the list
     * @param element
     */
    void add(E element);

    /**
     * Replaces element at the index with the provided value
     * @param index index of the current element to be replaced
     * @param element a new value to be set at the index
     * @return
     */
    void replace(int index, E element);

    Iterator<E> iterator();
}
