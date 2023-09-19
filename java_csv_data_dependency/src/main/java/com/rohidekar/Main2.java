package com.rohidekar;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

// Write any import statements here

class Main2 {

    @Deprecated
    public long getMaxAdditionalDinersCountTooLong(long N, long K, int M, long[] S) {

        long lastTaken = 1 - K;
        Set<Long> taken = new HashSet<>(Arrays.stream(S).boxed().collect(Collectors.toList()));
//        System.out.println("Main2.getMaxAdditionalDinersCount() " + taken);
        long totalTaken = 0;

        for (long tableNum = 1 - K; tableNum <= N + K; tableNum++) {
            System.err.println("Main2.getMaxAdditionalDinersCount() i = " + tableNum);
            if (taken.contains(tableNum)) {
                System.err.println("Main2.getMaxAdditionalDinersCount() occupied: " + tableNum);
                // not big enough gap.
                // fast forward and start again
                lastTaken = tableNum;
            }
            long consecutiveFree = tableNum - lastTaken;
            if (consecutiveFree < 2 * K + 1) {
                System.err.println("Main2.getMaxAdditionalDinersCount() not enough distance: " + consecutiveFree);
                // keep going
            } else {
                System.err.println("Main2.getMaxAdditionalDinersCount() found enough distance: " + consecutiveFree);
                totalTaken++;
                lastTaken = lastTaken + K + 1;
                System.out.println("Main2.getMaxAdditionalDinersCount() free: " + lastTaken);
            }
        }
        return totalTaken;
    }

    public static void main(String[] args) {
        // 1 2 3 4 5 6 7 8 9 10
        // | |
        long count = new Main2().getMaxAdditionalDinersCount(10L, 1L, 2, new long[] { 2L, 6L });
//        long count = new Main2().getMaxAdditionalDinersCount(15L, 2L, 3, new long[] { 11L, 6L, 14L });
        System.out.println("Main2.enclosing_method() count " + count);
    }

    public long getMaxAdditionalDinersCount(long N, long K, int M, long[] S) {
        Arrays.sort(S);
        int seatableTotal = 0;
        // first
        {
            long freeSeatsOnLeft = S[0] - 1;
            long seatablePeople = freeSeatsOnLeft / (K + 1);
            seatableTotal += seatablePeople;
            System.out.printf(
                    "Main2.getMaxAdditionalDinersCount() On left before %d, there are %d free seats that can house %d people spaced apart by %d\n",
                    S[0], freeSeatsOnLeft, seatablePeople, K);
        }
        // last
        {
            long freeSeatsOnRight = N - S[S.length - 1];
            long seatablePeople = freeSeatsOnRight / (K + 1);
            seatableTotal += seatablePeople;
            System.out.printf(
                    "Main2.getMaxAdditionalDinersCount() On right from %d to %d, there are %d free seats that can house %d people spaced apart by %d\n",
                    S[S.length - 1], N, freeSeatsOnRight, seatablePeople, K);
        }
        for (int i = 0; i < S.length - 1; i++) {
            long freeSeatsConsecutive = S[i + 1] - S[i] - 1;
            long seatablePeople = (freeSeatsConsecutive) / (2 * K + 1);
            System.out.printf(
                    "Main2.getMaxAdditionalDinersCount() %d free contiguous seats between %d and %d can house %d people\n",
                    freeSeatsConsecutive, S[i], S[i + 1], seatablePeople);
            seatableTotal += seatablePeople;
        }
        return seatableTotal;
    }

}
