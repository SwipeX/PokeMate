package dekk.pw.pokemate.util;

/**
 * created on 27.07.2016 by iDreamInBinary
 */
public class StringConverter {

    public static String convertPokename(String pokeName){
        return pokeName.substring(0, 1) + pokeName.substring(1).toLowerCase();
    }

    public static String convertItem(String itemName){
        String result = "";
        for (String x: itemName.split("_")) {
            result += x.substring(0,1) + x.substring(1).toLowerCase() + " ";
        }
        return result.substring(5);
    }
}
