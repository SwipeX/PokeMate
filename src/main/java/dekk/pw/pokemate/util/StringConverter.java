package dekk.pw.pokemate.util;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author kylestev
 */
public class StringConverter {
	public static String titleCase(final String sentence) {
        return Arrays.asList(sentence.split("\\s|_")).stream()
                .map(StringConverter::titlizeWord)
                .collect(Collectors.joining(" "));
    }

    private static String titlizeWord(final String word) {
        return word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase();
    }
}
