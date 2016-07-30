package dekk.pw.pokemate.tasks;

import com.pokegoapi.api.pokemon.HatchedEgg;
import com.pokegoapi.api.pokemon.Pokemon;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import dekk.pw.pokemate.Context;
import dekk.pw.pokemate.PokeMateUI;
import dekk.pw.pokemate.util.Time;
import javafx.scene.image.Image;

import java.util.List;
import static dekk.pw.pokemate.util.Time.sleep;

class HatchEgg extends Task  implements Runnable{
    HatchEgg(final Context context) {
        super(context);
    }

    @Override
    public void run() {
        while(context.getRunStatus()) {
            try {
                context.APILock.attempt(1000);
                APIStartTime = System.currentTimeMillis();
                List<HatchedEgg> eggs = context.getApi().getInventories().getHatchery().queryHatchedEggs();
                APIElapsedTime = System.currentTimeMillis() - APIStartTime;
                if (APIElapsedTime < context.getMinimumAPIWaitTime()) {
                    sleep(context.getMinimumAPIWaitTime() - APIElapsedTime);
                }
                context.APILock.release();

                eggs.forEach(egg -> {
                    try {
                        context.APILock.attempt(1000);

                        APIStartTime = System.currentTimeMillis();
                        Pokemon hatchedPokemon = context.getApi().getInventories().getPokebank().getPokemonById(egg.getId());
                        APIElapsedTime = System.currentTimeMillis() - APIStartTime;
                        if (APIElapsedTime < context.getMinimumAPIWaitTime()) {
                            sleep(context.getMinimumAPIWaitTime() - APIElapsedTime);
                        }

                        context.APILock.release();

                        String details = String.format("candy: %s  exp: %s  stardust: %s", egg.getCandy(), egg.getExperience(), egg.getStardust());
                        if (hatchedPokemon == null) {
                            PokeMateUI.toast("Hatched egg " + egg.getId() + " " + details, "Hatched egg!", "icons/items/egg.png");
                        } else {
                            PokeMateUI.toast("Hatched " + hatchedPokemon.getPokemonId() + " with " + hatchedPokemon.getCp() + " CP " + " - " + details,
                                "Hatched egg!",
                                "icons/items/egg.png");
                        }
                        Time.sleepRate();
                    } catch (InterruptedException e) {
                        System.out.println("[] Error - Timed out waiting for API");
                        // e.printStackTrace();
                    }
                });
            } catch (LoginFailedException e) {
                e.printStackTrace();
            } catch (RemoteServerException e) {
                System.out.println("[HatchEgg] Hit rate limit");
                //e.printStackTrace();
            } catch (InterruptedException e) {
                System.out.println("[] Error - Timed out waiting for API");
                // e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
