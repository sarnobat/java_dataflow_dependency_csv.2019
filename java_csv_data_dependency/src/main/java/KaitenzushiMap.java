import java.util.HashMap;
import java.util.Map;

public class KaitenzushiMap {

    public static void main(String[] args) {
//        int ret = new Kaitenzushi().getMaximumEatenDishCount(6, new int[] { 1, 2, 3, 3, 2, 1 }, 1);
//        int ret = new Kaitenzushi().getMaximumEatenDishCount(6, new int[] { 1, 2, 3, 3, 2, 1 }, 2);
        int ret = new KaitenzushiMap().getMaximumEatenDishCount(7, new int[] { 1, 2, 1, 2, 1, 2, 1 }, 2);

//        int ret = new Kaitenzushi().getMaximumEatenDishCount(6, new int[] { 3, 1, 1, 3 }, 0);
        System.out.println("Kaitenzushi.enclosing_method() max = " + ret);

    }

    @Deprecated
    public int getMaximumEatenDishCount(int N, int[] D, int K) {
        return getMaximumEatenDishCount(N, D, K, new HashMap<>());
    }

    @Deprecated
    private static int getMaximumEatenDishCount(int N, int[] dishes, int K, Map<Integer, Integer> recent) {
        int eaten = 0;
        int index = 0;
        int indexkBehind = 0 - K;
        for (int dishCurrent : dishes) {
            if (recentlyEncountered(dishCurrent, recent)) {
                // do not eat it
            } else {
                // eat it
                ++eaten;
                System.out.println("Kaitenzushi.getMaximumEatenDishCount() eating " + dishCurrent);
            }

            addRecent(dishCurrent, recent);
            if (indexkBehind > -1) {
                int dishNoLongerRecent = dishes[indexkBehind];
                removeNonRecent(dishNoLongerRecent, recent);
            }
            // sanity check
            checkRecentCount(recent, K);

            ++index;
            ++indexkBehind;

        }

        return eaten;
    }

    @Deprecated
    private static void addRecent(int dishCurrent, Map<Integer, Integer> recent) {
        if (recent.containsKey(dishCurrent)) {
            int count = recent.get(dishCurrent);
            recent.put(dishCurrent, count + 1);
        } else {
            recent.put(dishCurrent, 1);
        }
    }

    @Deprecated
    private static void checkRecentCount(Map<Integer, Integer> recent, int k) {
        int total = 0;
        for (int count : recent.values()) {
            total += count;
        }
        System.out.println("Kaitenzushi.enclosing_method() total = " + total);
        if (total > k) {
            throw new RuntimeException("developer error 2");
        }
    }

    @Deprecated
    private static void removeNonRecent(int dishNoLongerRecent, Map<Integer, Integer> recent) {
        if (recent.containsKey(dishNoLongerRecent)) {
            int count = recent.get(dishNoLongerRecent);
            recent.put(dishNoLongerRecent, count - 1);
        } else {
            // throw new RuntimeException("Developer error");
        }
    }

    @Deprecated
    private static boolean recentlyEncountered(int dishCurrent, Map<Integer, Integer> recent) {
        return recent.containsKey(dishCurrent) && recent.get(dishCurrent) > 0;
    }
}
