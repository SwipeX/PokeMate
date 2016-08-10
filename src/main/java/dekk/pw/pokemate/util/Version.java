package dekk.pw.pokemate.util;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by $ Tim Dekker on 8/8/2016.
 */
public class Version {
    private static final String KEY = "itemprop=\"softwareVersion\">";
    private static final String VERSION = "0.33.0";

    public static boolean isCurrent() {
        return getAppVersion().equals(VERSION);
    }

    public static String getAppVersion() {
        try {
            String str = "";
            URL url = new URL("https://play.google.com/store/apps/details?id=com.nianticlabs.pokemongo");
            URLConnection hc = url.openConnection();
            hc.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
            BufferedReader in = new BufferedReader(new InputStreamReader(hc.getInputStream()));
            String temp;
            while ((temp = in.readLine()) != null) {
                str += temp;
            }
            in.close();
            int idx = str.lastIndexOf(KEY) + KEY.length();
            if (idx > 0)
                return str.substring(idx, str.indexOf("<", idx)).trim();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
