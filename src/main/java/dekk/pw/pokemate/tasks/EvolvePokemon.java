package dekk.pw.pokemate.tasks;

import com.pokegoapi.api.map.pokemon.EvolutionResult;
import com.pokegoapi.api.pokemon.Pokemon;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import dekk.pw.pokemate.Config;
import dekk.pw.pokemate.Context;
import dekk.pw.pokemate.PokeMateUI;
import dekk.pw.pokemate.util.StringConverter;

import java.io.DataInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by TimD on 7/22/2016.
 */
public class EvolvePokemon extends Task implements Runnable {

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
            final EvolveMethod method = Config.getEvolveMethod();
            for (Pokemon pokemon : pokeList)
                switch (method) {
                    case WHITELIST:
                        performWhiteList(pokemon);
                    case OPTIMAL:
                        performOptimal(pokemon);
                    default:
                        break;
                }
        } catch (RemoteServerException | LoginFailedException e1) {
            System.out.println("[EvolvePokemon] Hit Rate Limited");
            e1.printStackTrace();
        } finally {
            context.addTask(new EvolvePokemon(context));
        }
    }

    private void performWhiteList(Pokemon pokemon) throws LoginFailedException, RemoteServerException {
        if (Config.isWhitelistEnabled() && Config.getWhitelistedPokemon().contains(pokemon.getPokemonId())) {
            int number = pokemon.getPokemonId().getNumber();
            if (CANDY_AMOUNTS.containsKey(number)) {
                int required = CANDY_AMOUNTS.get(number);
                if (required < 1) return;
                if (pokemon.getCandy() >= required) {
                    EvolutionResult result = pokemon.evolve();
                    if (result != null && result.isSuccessful()) {
                        String evolutionresult = StringConverter.titleCase(pokemon.getPokemonId().name()) + " has evolved into " + StringConverter.titleCase(result.getEvolvedPokemon().getPokemonId().name()) + " costing " + required + " candies";
                        PokeMateUI.toast(evolutionresult, Config.POKE + "mon evolved!", "icons/" + pokemon.getPokemonId().getNumber() + ".png");
                        context.setConsoleString("EvolvePokemon", "[" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "] - " + evolutionresult);
                    }
                }
            }
        }
    }

    private void performOptimal(Pokemon pokemon) throws LoginFailedException, RemoteServerException {
        // Pokemon is below the IV threshold or it is already evolved
        final int minIV = Config.getIvRatio();
        final int pokeIV = (int) (pokemon.getIvRatio() * 100.0);
        if(pokeIV < minIV || pokemon.getEvolutionForm().isFullyEvolved())
            return;

        // See if we have the candy amount for this pokemon
        final int number = pokemon.getPokemonId().getNumber();
        if(!CANDY_AMOUNTS.containsKey(number))
            return;

        final int requiredCandy = CANDY_AMOUNTS.get(number);
        if(requiredCandy < 1)
            return;

        // Check to see if we can evolve the pokemon. If so... do it!
        if(pokemon.getCandy() >= requiredCandy)
        {
            EvolutionResult result = pokemon.evolve();
            if(result != null && result.isSuccessful()) {
                String evolutionresult = StringConverter.titleCase(pokemon.getPokemonId().name()) + " has evolved into " + StringConverter.titleCase(result.getEvolvedPokemon().getPokemonId().name()) + " costing " + requiredCandy + " candies";
                PokeMateUI.toast(evolutionresult, Config.POKE + "mon evolved!", "icons/" + pokemon.getPokemonId().getNumber() + ".png");
                context.setConsoleString("EvolvePokemon", "[" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "] - " + evolutionresult);
            }
        }
    }

    public enum EvolveMethod {
        WHITELIST,
        OPTIMAL
    }

}
