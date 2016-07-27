package dekk.pw.pokemate.tasks;

import com.pokegoapi.api.pokemon.HatchedEgg;
import com.pokegoapi.api.pokemon.Pokemon;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import dekk.pw.pokemate.Context;
import dekk.pw.pokemate.PokeMateUI;
import javafx.scene.image.Image;

import java.util.List;

class HatchEgg extends Task {
	HatchEgg(final Context context) {
        super(context);
    }

    @Override
    public void run() {
        try {
            List<HatchedEgg> eggs = context.getApi().getInventories().getHatchery().queryHatchedEggs();
            eggs.forEach(egg -> {
                Pokemon hatchedPokemon = context.getApi().getInventories().getPokebank().getPokemonById(egg.getId());
                String details = String.format("candy: %s  exp: %s  stardust: %s", egg.getCandy(), egg.getExperience(), egg.getStardust());
                if (hatchedPokemon == null) {
                    PokeMateUI.toast("Hatched egg " + egg.getId() + " " + details);
                    PokeMateUI.showNotification("Hatched egg!", "Hatched egg " + egg.getId() + " " + details, new Image(("icons/egg.png"),64,64,false,false));
                } else {
                    PokeMateUI.toast("Hatched " + hatchedPokemon.getPokemonId() + " with " + hatchedPokemon.getCp() + " CP " + " - " + details);
                    PokeMateUI.showNotification("Hatched egg!", "Hatched " + hatchedPokemon.getPokemonId() + " with " + hatchedPokemon.getCp() + " CP " + " - " + details, new Image(("icons/egg.png"),64,64,false,false));
                }
            });
        } catch (LoginFailedException | RemoteServerException e) {
            e.printStackTrace();
        }
    }
}