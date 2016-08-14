package dekk.pw.pokemate.tasks;

import com.pokegoapi.api.pokemon.HatchedEgg;
import com.pokegoapi.api.pokemon.Pokemon;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import dekk.pw.pokemate.Context;
import dekk.pw.pokemate.PokeMateUI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

class HatchEgg extends Task implements Runnable{

    private static final Logger logger = LogManager.getLogger(HatchEgg.class);

    HatchEgg(final Context context) {
        super(context);
    }

    @Override
    public void run() {
        try {
            List<HatchedEgg> eggs = context.getInventories().getHatchery().queryHatchedEggs();
            eggs.forEach(egg -> {
                Pokemon hatchedPokemon = null;
                hatchedPokemon = context.getInventories().getPokebank().getPokemonById(egg.getId());
                String details = String.format("candy: %s  exp: %s  stardust: %s", egg.getCandy(), egg.getExperience(), egg.getStardust());
                if (hatchedPokemon == null) {
                    PokeMateUI.toast("Hatched egg " + egg.getId() + " " + details, "Hatched egg!", "icons/items/egg.png");
                    context.setConsoleString("HatchEgg", "Hatched egg " + egg.getId() + " " + details);
                } else {
                    PokeMateUI.toast("Hatched " + hatchedPokemon.getPokemonId() + " with " + hatchedPokemon.getCp() + " CP " + " - " + details,
                        "Hatched egg!",
                        "icons/items/egg.png");
                    context.setConsoleString("HatchEgg", "Hatched " + hatchedPokemon.getPokemonId() + " with " + hatchedPokemon.getCp() + " CP " + " - " + details);
                }
            });
        } catch (LoginFailedException e) {
            logger.error("Login Failed", e);

        } catch (RemoteServerException e) {
            context.setConsoleString("HatchEgg", "Server Error");
            logger.error("Remote Server Exception", e);
        } finally {
            context.addTask(new HatchEgg(context));
        }
    }
}
