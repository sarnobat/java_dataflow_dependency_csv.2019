import java.util.*;

public class Frogs {

    public static void main(String[] args) {
        long output = new Frogs().getSecondsRequired(3, 1, new long[] { 1 });
//        long output = new Frogs().getSecondsRequired(6, 3, new long[] { 5, 2, 4 });
        System.out.println("Frogs.main() " + output);
    }

    public long getSecondsRequired(long N, int F, long[] P) {
        Set<Long> padsOccupied = new LinkedHashSet<>();
        {
            for (long pad : P) {
                padsOccupied.add(pad);
            }
        }
        System.out.println("Frogs.getSecondsRequired() padsOccupied = " + padsOccupied);
        int reachedShore = 0;
        int hops = 0;
        long currentPad = 1;
        while (reachedShore < F) {
            // sanity check
            if (currentPad > N) {
                throw new RuntimeException("" + currentPad);
            }

            System.out.printf("Frogs.getSecondsRequired() currentPad=%d\treachedShore=%d\n", currentPad, reachedShore);
            if (padsOccupied.contains(currentPad)) {
                // find next available to jump to
                long nextAvailable;
                {
                    long j = currentPad;
                    while (padsOccupied.contains(j)) {
                        System.out.printf("Frogs.getSecondsRequired() currentPad=%d\treachedShore=%d\tj=%d\n", currentPad, reachedShore, j);
                        ++j;
                    }
                    nextAvailable = j;
                    System.out.println("Frogs.getSecondsRequired() nextAvailable = " + nextAvailable);
                }
                // jump to the next available
                {
                    padsOccupied.remove(currentPad);
                    if (nextAvailable < F + 2) {
                        padsOccupied.add(nextAvailable);
                        System.out.printf("Frogs.getSecondsRequired() jumped from %d to %d\n", currentPad, nextAvailable);
                    } else {
                        // frog reached shore
                        ++reachedShore;
                        System.out.printf("Frogs.getSecondsRequired() frog reached shore: %d\n", nextAvailable);
                    }
                    ++hops;
                }
                // reduce the scope
                ++currentPad;
            } else {
                System.out.println("Frogs.getSecondsRequired() Not occupied: " + currentPad);
                ++currentPad;
                continue;
            }
        }
        return hops;
    }

}
