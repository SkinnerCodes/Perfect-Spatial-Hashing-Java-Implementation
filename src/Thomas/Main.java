package Thomas;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Random;

public class Main {

    //this contains a stress test of of the PSHOffsetTable
    public static void main(String[] args) {
        Random random = new Random(System.currentTimeMillis());
        int n = 64*64*64;
        int testSize = n;

        //generate random spatial data list for testing only
        ArrayList<Vec3I> elelist = new ArrayList<>();
        for(int i = 0; i < n; i++) {
            Vec3I ele = new Vec3I(random.nextInt(56), random.nextInt(56), random.nextInt(56)); //56 is just arbitrary, arbitrary limit on spatial problem space
            if(!elelist.contains(ele)) {
                elelist.add(ele);
            }
        }

        PSHOffsetTable table;
        table = new PSHOffsetTable(elelist);
        for(int i = 0; i < 10; i++) {
            Long timestart = System.currentTimeMillis();
            Vec3I ele = new Vec3I(random.nextInt(56), random.nextInt(56), random.nextInt(56)); //56 is just arbitrary, arbitrary limit on spatial problem space
            if(!elelist.contains(ele)) {
                elelist.add(ele);
            }
            table.updateOffsets(elelist);
            System.out.println("Time to do an update of offsettable: " + (System.currentTimeMillis() - timestart));
        }

        //check for collisions, there should be none
        ArrayList<Vec3I> hashCheck = new ArrayList<>();
        for(int i = 0; i < elelist.size(); i++) {
            Vec3I hash = table.hash(elelist.get(i));
            if(!hashCheck.contains(hash)) {
                hashCheck.add(hash);
            }
            else {
                System.out.println("if this prints there is a hash collision which means there's a bug");
            }

        }
        System.out.println("stress test fin");
        }
}
