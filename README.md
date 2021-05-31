# SynchronizedUnbalancedSnapshotList
A List that offers the possibility to create snapshots of the elements currently stored in it. SnapshotList may not remove  existing elements and it can only grow by appending elements, not inserting.

## How to run:
1. Install Gradle 6.7.1 or above
2. Pull the repository : ``` git pull https://github.com/daja0923/SynchronizedUnbalancedSnapshotList.git ```
3. In project directory run test: ``` ./gradlew test ```


## Assumptions on the interface:
Current version is not a snapshot and its elements are subject to updates.
However, only current version's elements can be changed and snapshot versions are immutable.
Method ```dropPriorSnapshots(int version)``` drops snapshots prior to the given version (not inclusive).

Read access performances have higher priority.
```snapshot()``` must also perform as fast as possible since it is called frequently.
Performance of ```ddropPriorSnapshots``` is the lowest in priority since it is called very rarely.

Data structure must be memory efficient


## Performance on each method (Worst case)
1. getAtVersion() -> O(Log(V)) where V represents number of versions on index
2. version() -> O(1)
3. size() -> O(1)
4. snapshot() -> O(1)
5. add() -> O(1)
6. replace() -> O(Log(V)), where V represents current number of versions
7. dropPriorSnapshots() -> O(N * v * LOG(V)), where n represents size of the list, v represents provided version as argument and
V represents number of versions

## Memory space used (Worst case): 
O(N*V) where N represents list size and V represents number of versions

## Memory usage (Best case, when update is very rare, or only certain elements updated repeatedly): 
O(N) where N stands for list size.
#### Explanation: 
When we keep adding elements and create snapshots, list just keeps adding one-element treeMap to
snapshotMap. The element for the index represents for all the snapshot from the start version (where it was added) to above.
Only when the element for the current version is updated with ```replace()```, 
we add additional value to the index. So, if no update is done the memory usage will be size of the list.

## Note:
I created my own List interface to keep it simple and clean
##### Reason for using TreeMap for versions
It is mainly to make ```getAtVersion()``` faster. 
If simple HashMap is used its speed will go down to O(V) where V stands for the number of versions for the index.