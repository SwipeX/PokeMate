package dekk.pw.pokemate.tasks;

import com.pokegoapi.api.map.pokemon.EvolutionResult;
import com.pokegoapi.api.pokemon.Pokemon;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import dekk.pw.pokemate.Config;
import dekk.pw.pokemate.Context;
import dekk.pw.pokemate.PokeMateUI;

import java.io.DataInputStream;
import java.util.HashMap;
import java.util.ListIterator;

/**
 * Created by TimD on 7/22/2016.
 */
public class EvolvePokemon implements Task {
    private static final HashMap<Integer, Integer> CANDY_AMOUNTS = new HashMap<>();

    static {
        try {
            //We will read in from the compacted file...
            ClassLoader classloader = Thread.currentThread().getContextClassLoader();
            DataInputStream dis = new DataInputStream(classloader.getResourceAsStream("evolve.dat"));
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
            ListIterator<Pokemon> iterator = context.getApi().getInventories().getPokebank().getPokemons().listIterator();
            while (iterator.hasNext()) {
                Pokemon pokemon = iterator.next();
                if (!Config.isWhitelistEnabled() || Config.getWhitelistedPokemon().contains(pokemon.getPokemonId().getNumber())) {
                    int number = pokemon.getPokemonId().getNumber();
                    if (CANDY_AMOUNTS.containsKey(number)) {
                        int required = CANDY_AMOUNTS.get(number);
                        if (required < 1) continue;
                        if(required > 12) continue;
                        if (pokemon.getCandy() >= required) {
                            EvolutionResult result = pokemon.evolve();
                            if (result.isSuccessful()) {
                                String evolutionresult = pokemon.getPokemonId() + " has evolved into " + result.getEvolvedPokemon().getPokemonId() + " costing " + required + " candies";
                                PokeMateUI.toast(evolutionresult);
                                //PokeMateUI.notification("Evolution result!", evolutionresult);
                            }
                        }
                    }
                }
            }
        } catch (LoginFailedException | RemoteServerException e) {
            e.printStackTrace();
        }
    }
}
