package dekk.pw.pokemate.tasks;

import com.pokegoapi.api.map.pokemon.EvolutionResult;
import com.pokegoapi.api.pokemon.Pokemon;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import dekk.pw.pokemate.Context;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.util.HashMap;

/**
 * Created by TimD on 7/22/2016.
 */
public class EvolvePokemon implements Task {
    private static final HashMap<Integer, Integer> CANDY_AMOUNTS = new HashMap<>();

    static {
        try {
            //We will read in from the compacted file...
            DataInputStream dis = new DataInputStream(new FileInputStream("evolve.dat"));
            int count = dis.readInt();
            for (int i = 0; i < count; i++) {
                CANDY_AMOUNTS.put(dis.readInt(), dis.readInt());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run(Context context) {
        try {
            for (Pokemon pokemon : context.getApi().getInventories().getPokebank().getPokemons()) {
                int required = CANDY_AMOUNTS.get(pokemon.getPokemonId().getNumber());
                if (required < 1) continue;
                if (pokemon.getCandy() >= required) {
                    EvolutionResult result = pokemon.evolve();
                    if (result.isSuccessful()) {
                        System.out.println(pokemon.getPokemonId() + " has evolved into " + result.getEvolvedPokemon().getPokemonId() + " costing " + required + " candies");
                    }
                }
            }
        } catch (LoginFailedException | RemoteServerException e) {
            e.printStackTrace();
        }
    }
}
