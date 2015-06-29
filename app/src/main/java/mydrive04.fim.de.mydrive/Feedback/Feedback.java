package Feedback;

import android.content.SharedPreferences;

/**
 * Created by dev on 16.06.2015.
 */
public class Feedback {

    private static double CARBON_FACTOR = 2.5;
    private static double FUEL_COSTS = 1.5;

    private static int HP50TO100 = 0;
    private static int HP101TO150 = 1;
    private static int HP151TO200 = 2;
    private static int HP201TO250 = 3;
    private static int HPOVER251 = 4;



    public static String calculateEcologicalFeedback(double literPer100km){
        double emissions = literPer100km * CARBON_FACTOR;
        return String.valueOf(emissions);
    }

    public static String calculateEconomicFeedback (double literPer100km){
        // TODO weitere Kostenfaktoren?
        double costs = literPer100km * FUEL_COSTS;
        return String.valueOf(costs);
    }

    public static String calculateSocialFeedback(double literPer100km, double[][]distribution){
        int j = 0;
        int count=0;
        int sum = 0;
        while(distribution[j][0]<literPer100km){
            count = count + (int)distribution[j][1];
            j++;
        }
        for(int i=0;i<distribution.length;i++){
            sum = sum + (int)distribution[i][1];
        }
        double rank = (double)count/(double)sum;
        double result = rank * 1000.00;
        result = Math.round(result);
        result = result / 1000.00;
        result = result * 100.00;
        return result + "%";
    }




}
