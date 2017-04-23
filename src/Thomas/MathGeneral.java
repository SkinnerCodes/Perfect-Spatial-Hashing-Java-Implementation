package Thomas;

/**
 * Created by Thomas on 8/15/14.
 */
public class MathGeneral {
    public static int gcd(int a, int b)
    {
        if(a == 0 || b == 0) return a+b; // base case
        return gcd(b,a%b);
    }

    public static void quicksort(int[] array, int start, int end) {
        int i = start;
        int j = end;
        int pivot = array[start + (end - start)/2];
        while(i<=j) {
            while(array[i] < pivot) {
                i++;
            }
            while(array[j] > pivot) {
                j--;
            }
            if (i <= j) {
                exchange( array, i, j);
                i++;
                j--;
            }

        }
        if (start < j)
            quicksort(array, start, j);
        if (i < end)
            quicksort(array, i, end);
    }
    private static void exchange(int[] numbers, int i, int j) {
        int temp = numbers[i];
        numbers[i] = numbers[j];
        numbers[j] = temp;
    }
}

