package dekk.pw.pokemate.tasks;

import POGOProtos.Enums.PokemonIdOuterClass;
import com.pokegoapi.api.map.*;
import com.pokegoapi.api.pokemon.Pokemon;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import dekk.pw.pokemate.Context;

import java.util.*;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by TimD on 7/21/2016.
 */
public class ReleasePokemon implements Task {
    public void run(Context context) {
        try {
            Map<PokemonIdOuterClass.PokemonId, List<Pokemon>> groups = context.getApi().getInventories().getPokebank().getPokemons().stream().collect(Collectors.groupingBy(Pokemon::getPokemonId));
            for (List<Pokemon> list : groups.values()) {
                Collections.sort(list, (a, b) -> a.getCp() - b.getCp());
                for (int i = 0; i < list.size() - 1; i++) {
                    System.out.println("Transferring " + (i + 1) + "/" + list.size() + " " + list.get(i).getPokemonId() + " lvl " + list.get(i).getCp());
                    list.get(i).transferPokemon();
                }
            }
        } catch (LoginFailedException | RemoteServerException e) {
            e.printStackTrace();
        }
    }
}
