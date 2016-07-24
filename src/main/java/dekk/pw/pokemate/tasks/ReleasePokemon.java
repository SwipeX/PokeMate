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

    private static final int PERFECT_IV = 85;

    public void run(Context context) {
        Map<PokemonIdOuterClass.PokemonId, List<Pokemon>> groups = context.getApi().getInventories().getPokebank().getPokemons().stream().collect(Collectors.groupingBy(Pokemon::getPokemonId));
        for (List<Pokemon> list : groups.values()) {
            Collections.sort(list, (a, b) -> a.getCp() - b.getCp());
            list.stream().filter(p -> !p.getFavorite() || getIvRatio(p) < PERFECT_IV).forEach(p -> {
                //Passing this filter means they are not a 'perfect pokemon'
                try {
                    System.out.println("Transferring " + (list.indexOf(p) + 1) + "/" + list.size() + " " + p.getPokemonId() + " CP " + p.getCp() + " [" + p.getIndividualStamina() + "/" + p.getIndividualDefense() + "/" + p.getIndividualStamina() + "]");
                    p.transferPokemon();
                } catch (LoginFailedException | RemoteServerException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    /**
     * @param pokemon the pokemon for which an IV ratio is desired.
     * @return an integer 0-100 on the individual value of the pokemon.
     */
    public int getIvRatio(Pokemon pokemon) {
        return (pokemon.getIndividualAttack() + pokemon.getIndividualDefense() + pokemon.getIndividualStamina()) * 100 / 45;
    }
}
