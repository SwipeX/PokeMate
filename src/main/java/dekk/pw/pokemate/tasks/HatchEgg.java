package dekk.pw.pokemate.tasks;

import POGOProtos.Networking.Responses.UseItemEggIncubatorResponseOuterClass;
import com.pokegoapi.api.inventory.EggIncubator;
import com.pokegoapi.api.pokemon.EggPokemon;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import dekk.pw.pokemate.Context;

import java.util.stream.Stream;

/**
 * Created by $ Tim Dekker on 7/23/2016.
 */
public class HatchEgg implements Task {
    @Override
    public void run(Context context) {
        try {
            Stream<EggIncubator> incubators = context.getApi().getInventories().getIncubators().stream().filter(i -> !i.isInUse());
            Stream<EggPokemon> eggs = context.getApi().getInventories().getHatchery().getEggs().stream().filter(egg -> egg.getEggIncubatorId() == null || egg.getEggIncubatorId().isEmpty());
            if (incubators.count() > 0 && eggs.count() > 0) {
                UseItemEggIncubatorResponseOuterClass.UseItemEggIncubatorResponse.Result result = incubators.findFirst().get().hatchEgg(eggs.findFirst().get());
                if (result.equals(UseItemEggIncubatorResponseOuterClass.UseItemEggIncubatorResponse.Result.SUCCESS)) {
                    System.out.println("Now hatching egg # " + eggs.findFirst().get().getEggIncubatorId());
                }
            }
        } catch (LoginFailedException | RemoteServerException e) {
            e.printStackTrace();
        }
    }
}
