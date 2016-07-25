package dekk.pw.pokemate.tasks;

//import com.pokegoapi.api.pokemon.HatchedEgg;
//import com.pokegoapi.api.pokemon.Pokemon;
//import com.pokegoapi.exceptions.LoginFailedException;
//import com.pokegoapi.exceptions.RemoteServerException;
import dekk.pw.pokemate.Context;

import java.util.List;

/**
 * Created by TimD on 7/25/2016.
 */
public class HatchEgg implements Task {
    @Override
    public void run(Context context) {
 //       try {
            //We need to wait until this is in the master branch
//            List<HatchedEgg> eggs = context.getApi().getInventories().getHatchery().queryHatchedEggs();
//            eggs.forEach(egg -> {
//                Pokemon hatchedPokemon = context.getApi().getInventories().getPokebank().getPokemonById(egg.getId());
//                String details = String.format("candy: %s  exp: %s  stardust: %s", egg.getCandy(), egg.getExperience(), egg.getStardust());
//                if (hatchedPokemon == null) {
//                    System.out.println("Hatched egg " + egg.getId() + " " + details);
//                } else {
//                    System.out.println("Hatched " + hatchedPokemon.getPokemonId() + " with " + hatchedPokemon.getCp() + " CP " + " - " + details);
//                }
//            });
//        } catch (RemoteServerException | LoginFailedException e) {
//            e.printStackTrace();
//        }
    }
}
