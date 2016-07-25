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
public class ReleasePokemon implements Task {

	public void run(Context context) {
		Map<PokemonIdOuterClass.PokemonId, List<Pokemon>> groups = context.getApi().getInventories().getPokebank()
				.getPokemons().stream().collect(Collectors.groupingBy(Pokemon::getPokemonId));
		for (List<Pokemon> list : groups.values()) {
			// Sorts Pokemon by cp why? we filter below???
			Collections.sort(list, (a, b) -> a.getCp() - b.getCp());
			// Finds the weak pokemon
			list.stream().filter(
					p -> p.getCp() < Config.getMinCP() && !p.getFavorite() && getIvRatio(p) < Config.getIvRatio())
					.forEach(p -> {
						// Passing this filter means they are not a 'perfect pokemon'
						// Even if the pokemon is weak check to see if its the only one we have
						if (list.indexOf(p) < list.size() - 1) {
							System.out.println("Transferring " + (list.indexOf(p) + 1) + "/" + list.size() + " "
									+ p.getPokemonId() + " CP " + p.getCp() + " [" + p.getIndividualAttack() + "/"
									+ p.getIndividualDefense() + "/" + p.getIndividualStamina() + "]");
							try {
								p.transferPokemon();
							} catch (LoginFailedException | RemoteServerException e) {
								e.printStackTrace();
							}
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
