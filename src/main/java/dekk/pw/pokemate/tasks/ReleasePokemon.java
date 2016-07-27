package dekk.pw.pokemate.tasks;

import POGOProtos.Enums.PokemonIdOuterClass;
import com.pokegoapi.api.pokemon.Pokemon;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import dekk.pw.pokemate.Config;
import dekk.pw.pokemate.Context;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by TimD on 7/21/2016.
 */
public class ReleasePokemon extends Task {

    ReleasePokemon(final Context context) {
        super(context);
    }

    @Override
    public void run() {
        Map<PokemonIdOuterClass.PokemonId, List<Pokemon>> groups = context.getApi().getInventories().getPokebank().getPokemons().stream().collect(Collectors.groupingBy(Pokemon::getPokemonId));
        for (List<Pokemon> list : groups.values()) {
            Collections.sort(list, (a, b) -> a.getCp() - b.getCp());
            list.stream().filter(p -> p.getCp() < Config.getMinCP() && list.indexOf(p) < list.size() - 1 && !p.isFavorite() && context.getIvRatio(p) < Config.getIvRatio() && !Config.getNeverTransferPokemon().contains(p.getPokemonId().getNumber())).forEach(p -> {
                //Passing this filter means they are not a 'perfect pokemon'
                try {
                    p.transferPokemon();
                } catch (LoginFailedException | RemoteServerException e) {
                    e.printStackTrace();
                }

            });
        }
    }

}
