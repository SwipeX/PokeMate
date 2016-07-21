package dekk.pw.pokemate.tasks;

import com.pokegoapi.api.pokemon.Pokemon;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import dekk.pw.pokemate.Context;

import java.util.*;

/**
 * Created by TimD on 7/21/2016.
 */
public class ReleasePokemon implements Task {
    public void run(Context context) {
        try {
            HashMap<Integer, List<Pokemon>> groups = new HashMap<>();
            for (Pokemon pokemon : context.getApi().getPokebank().getPokemons()) {
                if (!groups.containsKey(pokemon.getPokemonId().getNumber())) {
                    groups.put(pokemon.getPokemonId().getNumber(), new ArrayList<>());
                }
                groups.get(pokemon.getPokemonId().getNumber()).add(pokemon);
            }
            for (List<Pokemon> list : groups.values()) {
                Collections.sort(list, (a, b) -> a.getCp() - b.getCp());
                for (int i = 0; i < list.size() - 1; i++) {
                    System.out.println("Transferring " + i + 1 + "/" + list.size() + " " + list.get(i).getPokemonId() + " lvl " + list.get(i).getCp());
                    list.get(i).transferPokemon();
                }
            }
        } catch (LoginFailedException | RemoteServerException e) {
            e.printStackTrace();
        }
    }
}
