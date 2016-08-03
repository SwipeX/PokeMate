package dekk.pw.pokemate.tasks;

import com.pokegoapi.api.map.pokemon.EvolutionResult;
import com.pokegoapi.api.pokemon.Pokemon;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import dekk.pw.pokemate.Config;
import dekk.pw.pokemate.Context;
import dekk.pw.pokemate.PokeMateUI;
import dekk.pw.pokemate.util.StringConverter;
import dekk.pw.pokemate.util.Time;

import java.io.DataInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by TimD on 7/22/2016.
 */
class EvolvePokemon extends Task implements Runnable {
    private static final ConcurrentHashMap<Integer, Integer> CANDY_AMOUNTS = new ConcurrentHashMap<>();

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

    EvolvePokemon(final Context context) {
        super(context);
    }

    @Override
    public void run() {
        // System.out.println("[Evolve] Activating..");
        try {
            CopyOnWriteArrayList<Pokemon> pokeList = new CopyOnWriteArrayList<>(context.getInventories().getPokebank().getPokemons());
            for (Pokemon pokemon : pokeList)
                if (!Config.isWhitelistEnabled() || Config.getWhitelistedPokemon().contains(pokemon.getPokemonId())) {
                    int number = pokemon.getPokemonId().getNumber();
                    if (CANDY_AMOUNTS.containsKey(number)) {
                        int required = CANDY_AMOUNTS.get(number);
                        if (required < 1) continue;
                        if (pokemon.getCandy() >= required) {
                            EvolutionResult result = pokemon.evolve();
                            if (result != null && result.isSuccessful()) {
                                String evolutionresult = StringConverter.titleCase(pokemon.getPokemonId().name()) + " has evolved into " + StringConverter.titleCase(result.getEvolvedPokemon().getPokemonId().name()) + " costing " + required + " candies. (+" + result.getCandyAwarded() + (result.getCandyAwarded() > 1 ? " candies " : "candy") + " , " + result.getExpAwarded() + "xp)";
                                PokeMateUI.toast(evolutionresult, Config.POKE + "mon evolved!", "icons/" + pokemon.getPokemonId().getNumber() + ".png");
                                context.setConsoleString("EvolvePokemon", "[" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "] - " + evolutionresult);
                            }
                        }
                    }
                }
        } catch (RemoteServerException | LoginFailedException e1) {
            System.out.println("[EvolvePokemon] Hit Rate Limited");
            e1.printStackTrace();
        } finally {
            Time.sleepRate();
            context.addTask(new EvolvePokemon(context));
        }
    }
}
