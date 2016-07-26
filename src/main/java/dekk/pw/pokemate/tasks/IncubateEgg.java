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

/**
 * Created by $ Tim Dekker on 7/23/2016.
 */
public class IncubateEgg implements Task {
    @Override
    public void run(Context context) {
        try {
            List<EggIncubator> incubators = context.getApi().getInventories().getIncubators().stream().filter(i -> !i.isInUse()).collect(Collectors.toList());
            List<EggPokemon> eggs = context.getApi().getInventories().getHatchery().getEggs().stream().filter(egg -> egg.getEggIncubatorId() == null || egg.getEggIncubatorId().isEmpty()).collect(Collectors.toList());
            if (incubators.size() > 0 && eggs.size() > 0) {
                UseItemEggIncubatorResponseOuterClass.UseItemEggIncubatorResponse.Result result = incubators.get(0).hatchEgg(eggs.get(0));
                if (result.equals(UseItemEggIncubatorResponseOuterClass.UseItemEggIncubatorResponse.Result.SUCCESS)) {
                    PokeMateUI.toast("Now incubating egg ( " + eggs.get(0).getEggKmWalkedTarget()+"km)");
                }
            }
        } catch (LoginFailedException | RemoteServerException e) {
            e.printStackTrace();
        }
    }
}
