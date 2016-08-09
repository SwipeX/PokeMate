package dekk.pw.pokemate.util;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by $ Tim Dekker on 8/8/2016.
 */
public class Version {
    private static final String KEY = "itemprop=\"softwareVersion\">";
    private static final String VERSION = "0.33.0";

    public static boolean isCurrent(){
        return getAppVersion().equals(VERSION);
    }

    public static String getAppVersion() {
        try {
            URL url = new URL("https://play.google.com/store/apps/details?id=com.nianticlabs.pokemongo");
            URLConnection hc = url.openConnection();
            hc.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
            // Read all the text returned by the server
            BufferedReader in = new BufferedReader(new InputStreamReader(hc.getInputStream()));
            String str = "";
            String temp;
            while ((temp = in.readLine()) != null) {
                str += temp;
                // System.out.println(str);
                // str is one line of text; readLine() strips the newline character(s)
            }
            in.close();

            int idx = str.lastIndexOf(KEY) + KEY.length();
            return str.substring(idx, str.indexOf("<", idx)).trim();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
