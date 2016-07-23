package dekk.pw.pokemate.tasks;

import com.pokegoapi.api.map.pokemon.EvolutionResult;
import com.pokegoapi.api.pokemon.Pokemon;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import dekk.pw.pokemate.Context;

/**
 * Created by TimD on 7/22/2016.
 */
public class EvolvePokemon implements Task {
    @Override
    public void run(Context context) {
        try {
            for (Pokemon pokemon : context.getApi().getInventories().getPokebank().getPokemons()) {
                if (pokemon.getCandy() >= 100) {
                    int count = pokemon.getCandy();
                    EvolutionResult result = pokemon.evolve();
                    if (result.isSuccessful()) {
                        System.out.println(pokemon.getPokemonId() + " has evolved into " + result.getEvolvedPokemon().getPokemonId() + " costing " + (count - result.getEvolvedPokemon().getCandy()) + " candies");
                    }
                }
            }
        } catch (LoginFailedException | RemoteServerException e) {
            e.printStackTrace();
        }
    }
}
