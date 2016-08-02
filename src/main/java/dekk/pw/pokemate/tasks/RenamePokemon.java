package dekk.pw.pokemate.tasks;

import POGOProtos.Networking.Responses.NicknamePokemonResponseOuterClass;
import com.pokegoapi.api.pokemon.Pokemon;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import dekk.pw.pokemate.Config;
import dekk.pw.pokemate.Context;
import dekk.pw.pokemate.PokeMateUI;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Kyle on 8/2/2016.
 */
public class RenamePokemon extends Task implements Runnable {

    RenamePokemon(final Context context) {
        super(context);
    }

    @Override
    public void run() {
        try {
            CopyOnWriteArrayList<Pokemon> pokeList = new CopyOnWriteArrayList<>(context.getInventories().getPokebank().getPokemons());
            for (final Pokemon pokemon : pokeList) {

                // Check to see if we should rename this pokemon
                final String name = pokemon.getNickname();
                final double pokeIV = pokemon.getIvRatio() * 100.0;
                final int pokeIVInt = (int) pokeIV;
                final String newName = String.format("%d_%s", pokeIVInt, pokemon.getPokemonId());
                if (name.equals(newName) ||
                        !name.equalsIgnoreCase(pokemon.getDefaultInstanceForType().getNickname())) continue;

                // check if the pokemon should be renamed
                if (!Config.isRenamingPokemonAll() && pokeIVInt <= Config.getIvRatio()) continue;

                NicknamePokemonResponseOuterClass.NicknamePokemonResponse.Result result = pokemon.renamePokemon(newName);
                if(result == NicknamePokemonResponseOuterClass.NicknamePokemonResponse.Result.SUCCESS) {
                    PokeMateUI.toast("Renamed " + name + " to " + newName, Config.POKE + "mon renamed", "icons/" + pokemon.getPokemonId().getNumber() + ".png");
                    context.setConsoleString("RenamePokemon", "[" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "] - " + name + " -> " + newName);
                }
            }

        } catch (RemoteServerException e) {
            e.printStackTrace();
        } catch (LoginFailedException e) {
            e.printStackTrace();
        } finally {
            context.addTask(new RenamePokemon(context));
        }

    }

}
