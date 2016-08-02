package dekk.pw.pokemate.tasks;

import POGOProtos.Enums.PokemonIdOuterClass;
import com.pokegoapi.api.pokemon.Pokemon;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import dekk.pw.pokemate.Config;
import dekk.pw.pokemate.Context;
import dekk.pw.pokemate.PokeMateUI;
import dekk.pw.pokemate.util.Time;
import dekk.pw.pokemate.util.Time;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Date;
import java.text.SimpleDateFormat;

import static dekk.pw.pokemate.util.Time.sleep;

/**
 * Created by TimD on 7/21/2016.
 */
public class ReleasePokemon extends Task implements Runnable {

    ReleasePokemon(final Context context) {
        super(context);
    }

    @Override
    public void run() {
        Map<PokemonIdOuterClass.PokemonId, List<Pokemon>> groups = null;
        try {
            groups = context.getInventories().getPokebank().getPokemons().stream().collect(Collectors.groupingBy(Pokemon::getPokemonId));
            for (List<Pokemon> list : groups.values()) {
                if (Config.isTransferPrefersIV()) {
                    Collections.sort(list, (a, b) -> context.getIvRatio(a) - context.getIvRatio(b));
                } else {
                    Collections.sort(list, (a, b) -> a.getCp() - b.getCp());
                }
                int minCP = Config.getMinCP();
                list.stream().filter(p -> (minCP <= 1 || p.getCp() < minCP) &&
                    list.indexOf(p) < list.size() - 1 &&
                    context.getIvRatio(p) < Config.getIvRatio() &&
                    !Config.getNeverTransferPokemon().contains(p.getPokemonId())).forEach(p -> {
                    //Passing this filter means they are not a 'perfect pokemon'
                    try {
                        p.transferPokemon();
                        Time.sleepRate();
                        PokeMateUI.addMessageToLog("Transferring " + (list.indexOf(p) + 1) + "/" + list.size() + " " + p.getPokemonId() + " CP " + p.getCp() + " [" + p.getIndividualAttack() + "/" + p.getIndividualDefense() + "/" + p.getIndividualStamina() + "]");
                        context.setConsoleString("ReleasePokemon", "[" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "] - " + ("Transferred " + (list.indexOf(p) + 1) + "/" + list.size() + " " + p.getPokemonId() + " CP " + p.getCp() + " [" + p.getIndividualAttack() + "/" + p.getIndividualDefense() + "/" + p.getIndividualStamina() + "]"));
                    } catch (LoginFailedException | RemoteServerException e) {
                        e.printStackTrace();
                    }

                });
            }
        } finally {
            context.addTask(new ReleasePokemon(context));
        }
    }
}
