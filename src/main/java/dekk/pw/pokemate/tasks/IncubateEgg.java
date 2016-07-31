package dekk.pw.pokemate.tasks;

import POGOProtos.Networking.Responses.UseItemEggIncubatorResponseOuterClass;
import com.pokegoapi.api.inventory.EggIncubator;
import com.pokegoapi.api.pokemon.EggPokemon;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import dekk.pw.pokemate.Context;
import dekk.pw.pokemate.PokeMateUI;
import java.util.List;
import java.util.stream.Collectors;
import static dekk.pw.pokemate.util.Time.sleep;

/**
 * Created by $ Tim Dekker on 7/23/2016.
 */
public class IncubateEgg extends Task implements Runnable {

    IncubateEgg(final Context context) {
        super(context);
    }

    @Override
    public void run() {
        try {
            List<EggIncubator> incubators = context.getApi().getInventories().getIncubators().stream().filter(i -> {
                try {
                    return !i.isInUse();
                } catch (LoginFailedException e) {
                    e.printStackTrace();
                    return false;
                } catch (RemoteServerException e) {
                    e.printStackTrace();
                    return false;
                }
            }).collect(Collectors.toList());
            List<EggPokemon> eggs = context.getApi().getInventories().getHatchery().getEggs().stream().filter(egg -> egg.getEggIncubatorId() == null || egg.getEggIncubatorId().isEmpty()).collect(Collectors.toList());
            if (incubators.size() > 0 && eggs.size() > 0) {
                UseItemEggIncubatorResponseOuterClass.UseItemEggIncubatorResponse.Result result = incubators.get(0).hatchEgg(eggs.get(0));
                if (result.equals(UseItemEggIncubatorResponseOuterClass.UseItemEggIncubatorResponse.Result.SUCCESS)) {
                    String eggresult = "Now incubating egg ( " + eggs.get(0).getEggKmWalkedTarget()+"km)";
                    PokeMateUI.toast(eggresult,"Egg Incubated!","icons/items/egg.png");
                }


                List<EggPokemon> eggs = context.getApi().getInventories().getHatchery().getEggs().stream().filter(egg -> egg.getEggIncubatorId() == null || egg.getEggIncubatorId().isEmpty()).collect(Collectors.toList());
                if (incubators.size() > 0 && eggs.size() > 0) {
                    UseItemEggIncubatorResponseOuterClass.UseItemEggIncubatorResponse.Result result = incubators.get(0).hatchEgg(eggs.get(0));
                    if (result.equals(UseItemEggIncubatorResponseOuterClass.UseItemEggIncubatorResponse.Result.SUCCESS)) {
                        String eggresult = "Now incubating egg ( " + eggs.get(0).getEggKmWalkedTarget() + "km)";
                        PokeMateUI.toast(eggresult, "Egg Incubated!", "icons/items/egg.png");
                    }
                }
            } catch (LoginFailedException | RemoteServerException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                System.out.println("[] Error - Timed out waiting for API");
                // e.printStackTrace();
            }finally   {
                context.APILock.release();
            }
        }
    }
}
