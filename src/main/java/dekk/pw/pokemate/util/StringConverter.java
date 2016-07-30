package dekk.pw.pokemate.util;

import POGOProtos.Inventory.Item.ItemAwardOuterClass;
import POGOProtos.Inventory.Item.ItemIdOuterClass;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * created on 27.07.2016 by iDreamInBinary
 */
public class StringConverter {

    public static String titleCase(final String sentence) {
        return Arrays.stream(sentence.split("\\s|_"))
            .map(StringConverter::titlizeWord)
            .collect(Collectors.joining(" "));
    }

    private static String titlizeWord(final String word) {
        return word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase();
    }

    public static String convertItemAwards(List<ItemAwardOuterClass.ItemAward> itemAwards) {
        String retStr = "";

        Map<Integer, Integer> receivedItems = new HashMap<>();

        //check what items we got from the pokestop
        for (ItemAwardOuterClass.ItemAward item : itemAwards) {
            receivedItems.put(item.getItemId().getNumber(), receivedItems.getOrDefault((item.getItemId().getNumber()),0) + 1);
        }
        //build the rest of the string
        for (Map.Entry<Integer, Integer> item : receivedItems.entrySet()) {
            retStr += " - " + StringConverter.titleCase(ItemIdOuterClass.ItemId.valueOf(item.getKey()).name()) + "(x" + item.getValue() + ")";
        }

        return retStr;
    }

}
