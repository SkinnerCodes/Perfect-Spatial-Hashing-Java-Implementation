# Perfect-Spatial-Hashing-Java-Implementation
 * This class is used to create perfect spatial hashing for a set of 3D indices.
 * This class takes as input a list of 3d index (3D integer vector), and creates a mapping that can be used to pair a 3d index with a value.
 * The best part about this type of hash map is that it can compress 3d spatial data in such a way that there spatial coherency (3d indices near each other have paired values near each other in the hash table) and the lookup time is O(1).
 * Since it's perfect hashing there is no hash collisions
 * This hashmap could be used for a very efficient hash table on the GPU due to coherency and only 2 lookups from a texture-hash-table would be needed, one for the offset to help create the hash, and one for the actual value indexed by the final hash.
 * This implementation is based off the paper: http://hhoppe.com/perfecthash.pdf, Perfect Spatial Hashing by Sylvain Lefebvre &Hugues Hopp, Microsoft Research
 
 *  To use:
 *  accumulate your spatial data in a list to pass to the PSHOffsetTable class
 *  construct the table with the list
 *  you now use this class just as your "mapping", it has the hash function for your hash table
 *  create your 3D hash with the chosen width from PSHOffsetTable.hashTableWidth.
 *  Then to get the index into your hash table, just use PSHOffsetTable.hash(key).
 *  That's it.
 
 *  If you want to update the offsetable, you can do so by using the updateOffsets() with the modified list of spatial data.
 
