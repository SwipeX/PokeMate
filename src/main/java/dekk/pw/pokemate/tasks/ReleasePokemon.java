package dekk.pw.pokemate.tasks;

import POGOProtos.Enums.PokemonIdOuterClass;
import com.pokegoapi.api.pokemon.Pokemon;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import dekk.pw.pokemate.Config;
import dekk.pw.pokemate.Context;
import dekk.pw.pokemate.PokeMateUI;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by TimD on 7/21/2016.
 */
public class ReleasePokemon implements Task {


    public void run(Context context) {
        Map<PokemonIdOuterClass.PokemonId, List<Pokemon>> groups = context.getApi().getInventories().getPokebank().getPokemons().stream().collect(Collectors.groupingBy(Pokemon::getPokemonId));
        for (List<Pokemon> list : groups.values()) {
            Collections.sort(list, (a, b) -> a.getCp() - b.getCp());
            list.stream().filter(p -> p.getCp() < Config.getMinCP() && list.indexOf(p) < list.size() - 1 && !p.getFavorite() && context.getIvRatio(p) < Config.getIvRatio() && !Config.getNeverTransferPokemon().contains(p.getPokemonId().getNumber())).forEach(p -> {
                //Passing this filter means they are not a 'perfect pokemon'
                try {
                    System.out.println("Transfering " + p.getPokemonId() +  " has been " +p.transferPokemon().toString());
                } catch (LoginFailedException | RemoteServerException e) {
                    e.printStackTrace();
                }

            });
        }
    }

}
