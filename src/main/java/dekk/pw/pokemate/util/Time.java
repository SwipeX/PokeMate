package dekk.pw.pokemate.util;

/**
 * Created by TimD on 7/29/2016.
 */
public class Time {

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
