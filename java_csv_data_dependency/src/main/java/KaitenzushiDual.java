import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class KaitenzushiDual {

    public static void main(String[] args) {
//        int ret = new KaitenzushiDual().getMaximumEatenDishCount(6, new int[] { 1, 2, 3, 3, 2, 1 }, 1);
//      int ret = new KaitenzushiDual().getMaximumEatenDishCount(6, new int[] { 1, 2, 3, 3, 2, 1 }, 2);
        int ret = new KaitenzushiDual().getMaximumEatenDishCount(7, new int[] { 1, 2, 1, 2, 1, 2, 1 }, 2);

//      int ret = new KaitenzushiDual().getMaximumEatenDishCount(6, new int[] { 3, 1, 1, 3 }, 0);
        System.out.println("Kaitenzushi.enclosing_method() max = " + ret);

    }

    public int getMaximumEatenDishCount(int N, int[] D, int K) {
        Set<Integer> recentlyEatenSet = new HashSet<>();
        List<Integer> recentlyEatenList = new LinkedList<>();
        int eaten = 0;
        for (int dishCurrent : D) {
            if (recentlyEatenSet.contains(dishCurrent)) {
                // do not eat it
            } else {
                // eat it
                ++eaten;
                System.out.println("Kaitenzushi.getMaximumEatenDishCount() eating " + dishCurrent);
                recentlyEatenList.add(dishCurrent);
                if (recentlyEatenList.size() > K) {
                    int leastRecentlyEaten = recentlyEatenList.remove(0);
                    recentlyEatenSet.remove(leastRecentlyEaten);
                }
                recentlyEatenSet.add(dishCurrent);
            }

        }

        return eaten;
    }

}
