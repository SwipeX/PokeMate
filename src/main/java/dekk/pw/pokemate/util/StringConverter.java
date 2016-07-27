package dekk.pw.pokemate.util;

/**
 * created on 27.07.2016 by iDreamInBinary
 */
public class StringConverter {

    //return a pokemon name thats not all uppercase
    public static String convertPokename(String pokeName){
        return pokeName.substring(0, 1).toUpperCase() + pokeName.substring(1).toLowerCase();
    }

    //return an item name thats not all uppercase and doesn't have underscores
    public static String convertItem(String itemName){
        String[] result = (itemName.substring(5,6).toUpperCase() + itemName.substring(6).replaceAll("_", " ").toLowerCase()).split("\\s+");
        return result[0] + " " + result[1].substring(0,1).toUpperCase() + result[1].substring(1);
    }

}
