import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class KaitenzushiSet {

    public static void main(String[] args) {
//        int ret = new Kaitenzushi().getMaximumEatenDishCount(6, new int[] { 1, 2, 3, 3, 2, 1 }, 1);
//        int ret = new Kaitenzushi().getMaximumEatenDishCount(6, new int[] { 1, 2, 3, 3, 2, 1 }, 2);
        int ret = new KaitenzushiSet().getMaximumEatenDishCount(7, new int[] { 1, 2, 1, 2, 1, 2, 1 }, 2);

//        int ret = new Kaitenzushi().getMaximumEatenDishCount(6, new int[] { 3, 1, 1, 3 }, 0);
        System.out.println("Kaitenzushi.enclosing_method() max = " + ret);

    }

    public int getMaximumEatenDishCount(int N, int[] D, int K) {
        return getMaximumEatenDishCount(N, D, K, new LinkedHashSet<>());
    }

    private static int getMaximumEatenDishCount(int N, int[] dishes, int K, Set<Integer> recent) {
        int eaten = 0;
//        int indexkBehind = 0 - K;
        for (int dishCurrent : dishes) {
            if (recentlyEncountered(dishCurrent, recent)) {
                // do not eat it
            } else {
                // eat it
                ++eaten;
                System.out.println("Kaitenzushi.getMaximumEatenDishCount() eating " + dishCurrent);
//                if (indexkBehind > -1) {
//                    int dishNoLongerRecent = recent.remove();
//                    removeNonRecent(dishNoLongerRecent, recent);
//                }
                addRecent(dishCurrent, recent);
//                ++indexkBehind;
            }


        }

        return eaten;
    }

    private static void addRecent(int dishCurrent, Set<Integer> recent) {
        recent.add(dishCurrent);
    }

    private static void removeNonRecent(int dishNoLongerRecent, Set<Integer> recent) {
        if (recent.contains(dishNoLongerRecent)) {
            recent.remove(dishNoLongerRecent);
            System.out.println("Kaitenzushi.removeNonRecent() removed " + dishNoLongerRecent);
        } else {
            System.out
                    .println("Kaitenzushi.removeNonRecent() couldn't remove " + dishNoLongerRecent + " from " + recent);
//            throw new RuntimeException("Developer error");
        }
    }

    private static boolean recentlyEncountered(int dishCurrent, Set<Integer> recent) {
        return recent.contains(dishCurrent);
    }

}
