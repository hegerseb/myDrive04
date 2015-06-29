package Helper;

/**
 * Created by dev on 22.05.2015.
 */
public class AppSingleton {

    private static double noise = 1; // 1 m/s2 noise of acceleration meter
    private static int waitForNetwork  = 10000; // ten seconds
    private static boolean tracking = false;
    private static boolean uploading = false;

    public static String getStarttime() {
        return starttime;
    }

    public static void setStarttime(String starttime) {
        AppSingleton.starttime = starttime;
    }

    private static String starttime = "";
    public static boolean isUploading() {
        return uploading;
    }

    public static void setUploading(boolean uploading) {
        AppSingleton.uploading = uploading;
    }




    public static boolean isTracking() {
        return tracking;
    }

    public static void setTracking(boolean tracking) {
        AppSingleton.tracking = tracking;
    }



    public static int getWaitForNetwork() {
        return waitForNetwork;
    }

    public static void setWaitForNetwork(int waitForNetwork) {
        AppSingleton.waitForNetwork = waitForNetwork;
    }



    public static void setNoise (double n){
        noise = n;
    }

    public static double getNoise (){
        return noise;
    }

}
