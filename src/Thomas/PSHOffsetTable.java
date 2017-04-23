package Thomas;

import Thomas.Vec3I;
import Thomas.MathGeneral;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Created by Thomas on 8/9/14.
 * This class is used to create perfect spatial hashing for a set of 3D indices.
 * This class takes as input a list of 3d index (3D integer vector), and creates a mapping that can be used to pair a 3d index with a value.
 * The best part about this type of hash map is that it can compress 3d spatial data in such a way that there spatial coherency (3d indices near each other have paired values near each other in the hash table) and the lookup time is O(1).
 * Since it's perfect hashing there is no hash collisions
 * This hashmap could be used for a very efficient hash table on the GPU due to coherency and only 2 lookups from a texture-hash-table would be needed, one for the offset to help create the hash, and one for the actual value indexed by the final hash.
 * This implementation is based off the paper: http://hhoppe.com/perfecthash.pdf, Perfect Spatial Hashing by Sylvain Lefebvre &Hugues Hopp, Microsoft Research
 *
 *  To use:
 *  accumulate your spatial data in a list to pass to the PSHOffsetTable class
 *  construct the table with the list
 *  you now use this class just as your "mapping", it has the hash function for your hash table
 *  create your 3D hash with the chosen width from PSHOffsetTable.hashTableWidth.
 *  Then to get the index into your hash table, just use PSHOffsetTable.hash(key).
 *  That's it.
 *
 *  If you want to update the offsetable, you can do so by using the updateOffsets() with the modified list of spatial data.
 */

public class PSHOffsetTable {

    private ArrayList<Vec3I> elements;
    private OffsetBucket offsetBuckets[][][];
    public Vec3I offsetTable[][][];
    private boolean hashFilled[][][];
    public int offsetTableWidth;
    public int hashTableWidth;
    int n;
    private static int offsetFindLimit =120;
    private static int tableCreateLimit = 10;
    private int creationAttempts = 0;

    Random random = new Random(System.currentTimeMillis());

    private class OffsetBucket {
        List<Vec3I> contents = new LinkedList<>();
        Vec3I index; //index in offset table
        OffsetBucket(Vec3I index) {
            this.index = index;
        }
    }

    public void updateOffsets(ArrayList<Vec3I> elements) {
        int size = elements.size();
        n = size;
        hashTableWidth = calcHashTableWidth(size);
        int oldOffsetWidth = offsetTableWidth;
        offsetTableWidth = calcOffsetTableWidth(size); //this breaks if original creation didn't use initial table calculated width

        hashFilled = new boolean[hashTableWidth][hashTableWidth][hashTableWidth];
        offsetBuckets = new OffsetBucket[offsetTableWidth][offsetTableWidth][offsetTableWidth];


        this.elements = elements;

        if(oldOffsetWidth != offsetTableWidth) {
            offsetTable = new Vec3I[offsetTableWidth][offsetTableWidth][offsetTableWidth]; clearOffsstsToZero();
            calculateOffsets();

            cleanUp();
        }
        else {


            putElementsIntoBuckets();
            List<OffsetBucket> bucketList = createSortedBucketList();

            for(int i = 0; i < bucketList.size(); i++) {
                OffsetBucket bucket = bucketList.get(i);
                Vec3I offset = offsetTable[bucket.index.x][bucket.index.y][bucket.index.z];
                if(!OffsetWorks(bucket, offset)) {
                    offset = findOffsetRandom(bucket);
                    if(offset == null) {
                        tryCreateAgain();
                        break;
                    }
                    offsetTable[bucket.index.x][bucket.index.y][bucket.index.z] = offset;
                }
                fillHashCheck(bucket, offset);
//            if(checkForBadCollisions(bucket)) {
//                tryCreateAgain();
//                break;
//            }
            }
        }


    }

    private int calcHashTableWidth(int size) {
        float d = (float)Math.pow(size*1.1, 1.0f/3);
        return (int)(d + 1.1f);
    }
    private int calcOffsetTableWidth(int size) {
        float d = (float)Math.pow(size/4, 1.0f/3);
        int width = (int)(d + 1.1f);
        while(MathGeneral.gcd(width, hashTableWidth) > 1) { //make sure there are no common facctors
            width++;
        }
        return width;

    }
    public PSHOffsetTable(ArrayList<Vec3I> elements) {
        int size = elements.size();
        n=size;
        hashTableWidth = calcHashTableWidth(size);
        offsetTableWidth = calcOffsetTableWidth(size);

        hashFilled = new boolean[hashTableWidth][hashTableWidth][hashTableWidth];
        offsetBuckets = new OffsetBucket[offsetTableWidth][offsetTableWidth][offsetTableWidth];

        offsetTable = new Vec3I[offsetTableWidth][offsetTableWidth][offsetTableWidth]; clearOffsstsToZero();
        this.elements = elements;

        calculateOffsets();

        cleanUp();


    }
    private void cleanUp() {
        this.elements = null;
        this.offsetBuckets = null;
        this.hashFilled = null;

    }
    private void putElementsIntoBuckets() {
        for(int i = 0; i < n; i++) {
            Vec3I ele = elements.get(i);
            Vec3I index = hash1(ele);
            OffsetBucket bucket = offsetBuckets[index.x][index.y][index.z];
            if(bucket == null) {
                bucket = new OffsetBucket(new Vec3I(index.x, index.y, index.z));
                offsetBuckets[index.x][index.y][index.z] = bucket;
            }
            bucket.contents.add(ele);
        }
    }
    private List<OffsetBucket> createSortedBucketList() {
        List<OffsetBucket> bucketList = new ArrayList<>(offsetTableWidth*offsetTableWidth*offsetTableWidth);//(offsetTableWidth*offsetTableWidth*offsetTableWidth);

        for(int x = 0; x < offsetTableWidth; x++ ) { //put the buckets into the bucketlist and sort
            for(int y = 0; y < offsetTableWidth; y++ ) {
                for(int z = 0; z  < offsetTableWidth; z++ ) {
                    if(offsetBuckets[x][y][z] != null) {
                        bucketList.add(offsetBuckets[x][y][z]);
                    }
                }
            }
        }
        quicksort(bucketList, 0, bucketList.size()-1);
        return bucketList;
    }
    private void calculateOffsets() {

        putElementsIntoBuckets();
        List<OffsetBucket> bucketList = createSortedBucketList();



        for(int i = 0; i < bucketList.size(); i++) {
            OffsetBucket bucket = bucketList.get(i);
//            if(checkForBadCollisions(bucket)) {
//                tryCreateAgain();
//                break;
//            }
            //Vec3I offset = findOffset(bucket);
            Vec3I offset = findOffsetRandom(bucket);

            if(offset == null) {
                tryCreateAgain();
                break;
            }
            offsetTable[bucket.index.x][bucket.index.y][bucket.index.z] = offset;
            fillHashCheck(bucket, offset);

        }

    }
    public void quicksort(List<OffsetBucket> bucketList, int start, int end) {
        int i = start;
        int j = end;
        int pivot = bucketList.get(start + (end - start)/2).contents.size();
        while(i<=j) {
            while(bucketList.get(i).contents.size() > pivot) {
                i++;
            }
            while(bucketList.get(j).contents.size() < pivot) {
                j--;
            }
            if (i <= j) {
                OffsetBucket temp = bucketList.get(i);
                bucketList.set(i, bucketList.get(j));
                bucketList.set(j,temp);
                i++;
                j--;
            }

        }
        if (start < j)
            quicksort(bucketList, start, j);
        if (i < end)
            quicksort(bucketList, i, end);
    }

    private void tryCreateAgain() {
        creationAttempts++;
        if(creationAttempts >= tableCreateLimit) {
            throw new RuntimeException("this class doesn't fucking work bad fucking code idiot");}
        resizeOffsetTable();
        clearFilled();
        calculateOffsets();

    }

    private boolean checkForBadCollisions(OffsetBucket bucket) {
        ArrayList<Vec3I> testList = new ArrayList<>(10);
        for(int i = 0; i < bucket.contents.size(); i++) {

            Vec3I ele = bucket.contents.get(i);
            Vec3I hash = hash0(ele);
            if(testList.contains(hash))  {
                return true;

            }
            else{
                testList.add(hash);
            }

        }
        return false;

    }
    private void fillHashCheck(OffsetBucket bucket, Vec3I offset) {
        for(int i = 0; i < bucket.contents.size(); i++) {
            Vec3I ele = bucket.contents.get(i);
            Vec3I hash = hash(ele, offset);
            hashFilled[hash.x][hash.y][hash.z] = true;
        }

    }

    private Vec3I findOffsetRandom(OffsetBucket bucket) {

        ArrayList<Vec3I> badOffests = new ArrayList<>();
        Vec3I offset = new Vec3I(0);
        Vec3I index;
        index = Vec3I.add(bucket.index, new Vec3I(1,0,0)); index = hash1(index);
        offset = offsetTable[index.x][index.y][index.z];
        if(!badOffests.contains(offset)){if(OffsetWorks(bucket, offset)) return offset; badOffests.add(offset);}
        index = Vec3I.add(bucket.index, new Vec3I(0,1,0)); index = hash1(index);
        offset = offsetTable[index.x][index.y][index.z];
        if(!badOffests.contains(offset)){if(OffsetWorks(bucket, offset)) return offset; badOffests.add(offset);}
        index = Vec3I.add(bucket.index, new Vec3I(0,0,1)); index = hash1(index);
        offset = offsetTable[index.x][index.y][index.z];
        if(!badOffests.contains(offset)){if(OffsetWorks(bucket, offset)) return offset; badOffests.add(offset);}
        index = Vec3I.add(bucket.index, new Vec3I(-1,0,0)); index = hash1(index);
        offset = offsetTable[index.x][index.y][index.z];
        if(!badOffests.contains(offset)){if(OffsetWorks(bucket, offset)) return offset; badOffests.add(offset);}
        index = Vec3I.add(bucket.index, new Vec3I(0,-1,0)); index = hash1(index);
        offset = offsetTable[index.x][index.y][index.z];
        if(!badOffests.contains(offset)){if(OffsetWorks(bucket, offset)) return offset; badOffests.add(offset);}
        index = Vec3I.add(bucket.index, new Vec3I(0,0,-1)); index = hash1(index);
        offset = offsetTable[index.x][index.y][index.z];
        if(!badOffests.contains(offset)){if(OffsetWorks(bucket, offset)) return offset; badOffests.add(offset);}

//        Vec3I emptyHash = findAEmptyHash();
//        offset = Vec3I.subtract(emptyHash , hash0(bucket.contents.get(0)));
//        if(OffsetWorks(bucket, offset)) return offset;

        Vec3I seed = new Vec3I(random.nextInt(hashTableWidth) - hashTableWidth/2, random.nextInt(hashTableWidth) - hashTableWidth/2, random.nextInt(hashTableWidth) - hashTableWidth/2);
        for(int i = 0; i <= 5; i++) {
            for(int x = i; x < hashTableWidth; x+=5 ) {
                for(int y = i; y < hashTableWidth; y+=5 ) {
                    for(int z = i; z  < hashTableWidth; z+=5 ) {
                        index = Vec3I.add(seed, new Vec3I(x,y,z));
                        index = hash0(index);
                        if(!hashFilled[index.x][index.y][index.z]) {
                            offset = Vec3I.subtract(index , hash0(bucket.contents.get(0)));
                            if(OffsetWorks(bucket, offset)) return offset;
                        }
                    }
                }
            }
        }
//        if(bucket.contents.size() >  1) {
//
//
//
//            for(int attempt = 0; attempt < 1000; attempt++) {
//
////                while(badOffests.contains(offset)) {
////                    offset = new Vec3I(random.nextInt(hashTableWidth) - hashTableWidth/2, random.nextInt(hashTableWidth) - hashTableWidth/2, random.nextInt(hashTableWidth) - hashTableWidth/2);
////                }
////                if(OffsetWorks(bucket, offset)) return offset;
//                emptyHash = findAEmptyHash(emptyHash);
//                offset = Vec3I.subtract(emptyHash , hash0(bucket.contents.get(0)));
//                if(OffsetWorks(bucket, offset)) return offset;
//                badOffests.add(offset);
//            }
//        }
//        else {
//            emptyHash = findAEmptyHash();
//            offset = Vec3I.subtract(emptyHash , hash0(bucket.contents.get(0)));
//            if(offset == null) {
//                return offset;
//            }
//            return offset;
//
//        }

        return null;
    }
    private Vec3I findAEmptyHash() {
        Vec3I seed = new Vec3I(random.nextInt(hashTableWidth) - hashTableWidth/2, random.nextInt(hashTableWidth) - hashTableWidth/2, random.nextInt(hashTableWidth) - hashTableWidth/2);
        for(int x = 0; x < hashTableWidth; x++ ) {
            for(int y = 0; y < hashTableWidth; y++ ) {
                for(int z = 0; z  < hashTableWidth; z++ ) {
                    Vec3I index = Vec3I.add(seed, new Vec3I(x,y,z));
                    index = hash0(index);
                    if(!hashFilled[index.x][index.y][index.z]) return index;
                }
            }
        }
        return null;
    }
    private Vec3I findAEmptyHash(Vec3I start) {
        for(int x = 0; x < hashTableWidth; x++ ) {
            for(int y = 0; y < hashTableWidth; y++ ) {
                for(int z = 0; z  < hashTableWidth; z++ ) {
                    if(x + y + z == 0) continue;
                    Vec3I index = Vec3I.add(start, new Vec3I(x,y,z));
                    index = hash0(index);
                    if(!hashFilled[index.x][index.y][index.z]) return index;
                }
            }
        }
        return null;
    }
    private boolean OffsetWorks (OffsetBucket bucket, Vec3I offset) {
        for(int i = 0; i < bucket.contents.size(); i++) {
            Vec3I ele = bucket.contents.get(i);
            Vec3I hash = hash(ele, offset);
            if(hashFilled[hash.x][hash.y][hash.z]) {
                return false;
            }
        }
        return true;
    }


    private Vec3I hash1(Vec3I key) {
        return Vec3I.mod(key, offsetTableWidth);
    }
    private Vec3I hash0(Vec3I key) {
        return Vec3I.mod(key, hashTableWidth);
    }
    public Vec3I hash(Vec3I key) {
        Vec3I index = hash1(key);
        return hash0(hash0(key).add(offsetTable[index.x][index.y][index.z]));
    }
    private Vec3I hash(Vec3I key, Vec3I offset) {
        return hash0(hash0(key).add(offset));
    }
    private void resizeOffsetTable() {
        offsetTableWidth+=5; //test
        while(MathGeneral.gcd(offsetTableWidth, hashTableWidth) > 1) {
            offsetTableWidth++;
        }
        offsetBuckets = new OffsetBucket[offsetTableWidth][offsetTableWidth][offsetTableWidth];
        offsetTable = new Vec3I[offsetTableWidth][offsetTableWidth][offsetTableWidth];
        clearOffsstsToZero();

    }
    private void clearFilled() {
        for(int x = 0; x < hashTableWidth; x++ ) {
            for(int y = 0; y < hashTableWidth; y++ ) {
                for(int z = 0; z  < hashTableWidth; z++ ) {
                    hashFilled[x][y][z] = false;
                }
            }
        }
    }
    private void clearOffsstsToZero() {
        for(int x = 0; x < offsetTableWidth; x++ ) {
            for(int y = 0; y < offsetTableWidth; y++ ) {
                for(int z = 0; z  < offsetTableWidth; z++ ) {
                    offsetTable[x][y][z] = new Vec3I(0);
                }
            }
        }
    }


}
