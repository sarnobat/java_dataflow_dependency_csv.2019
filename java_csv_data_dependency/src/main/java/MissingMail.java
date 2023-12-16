import java.util.Iterator;

public class MissingMail {

    public static void main(String[] args) {
        double maxExpectedProfit = new MissingMail().getMaxExpectedProfit(5, new int[] { 10, 2, 8, 6, 4 }, 5, 0);
        System.out.println("MissingMail.enclosing_method() " + maxExpectedProfit);
    }

    public double getMaxExpectedProfit(int N, int[] V, int C, double S) {
        int numberOfDaysTotal = N;
        int collectAllFee = C;
        double stolenProbability = S;
        int[] deliverySchedule = V;
        // int i = 1;// day number
        int valueBeforeToday = 0;
        double valueCollectedMax = 0;
        // delivery happens at the beginning of the day
        // 
        // Stealing happens at the end of the day, after observation
        for (int i = 0; i < N; i++) {
            int todaysValue = deliverySchedule[i];
            double todaysValueWithStealing = (1 - S) * valueBeforeToday - S * valueBeforeToday;
//            double valueCollected = (1 - S) * valueBeforeToday - S * valueBeforeToday - collectAllFee;
            System.out.println("MissingMail.getMaxExpectedProfit() todaysValueWithStealing = " + todaysValueWithStealing);
            if (todaysValueWithStealing > valueCollectedMax) {
                valueCollectedMax = todaysValueWithStealing;
            }
            valueBeforeToday += todaysValue;
        }
        return valueCollectedMax;
    }

}
