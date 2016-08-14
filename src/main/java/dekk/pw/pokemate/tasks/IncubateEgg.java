package dekk.pw.pokemate.tasks;

import POGOProtos.Networking.Responses.UseItemEggIncubatorResponseOuterClass;
import com.pokegoapi.api.inventory.EggIncubator;
import com.pokegoapi.api.pokemon.EggPokemon;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import dekk.pw.pokemate.Context;
import dekk.pw.pokemate.PokeMateUI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by $ Tim Dekker on 7/23/2016.
 */
class IncubateEgg extends Task implements Runnable{

    private static final Logger logger = LogManager.getLogger(IncubateEgg.class);

    IncubateEgg(final Context context) {
        super(context);
    }

    @Override
    public void run() {
        try {
            List<EggIncubator> incubators = context.getInventories().getIncubators().stream().filter(i -> {
                try {
                    return !i.isInUse();
                } catch (Exception e) {
                    logger.error("Incubator Error", e);
                    return false;
                }
            }).collect(Collectors.toList());

            List<EggPokemon> eggs = context.getInventories().getHatchery().getEggs().stream().filter(egg ->
                egg.getEggIncubatorId() == null || egg.getEggIncubatorId().isEmpty()).collect(Collectors.toList());
            if (incubators.size() > 0 && eggs.size() > 0) {
                UseItemEggIncubatorResponseOuterClass.UseItemEggIncubatorResponse.Result result = incubators.get(0).hatchEgg(eggs.get(0));
                if (result.equals(UseItemEggIncubatorResponseOuterClass.UseItemEggIncubatorResponse.Result.SUCCESS)) {
                    String eggresult = "Now incubating egg ( " + eggs.get(0).getEggKmWalkedTarget() + "km)";
                    PokeMateUI.toast(eggresult, "Egg Incubated!", "icons/items/egg.png");
                    context.setConsoleString("IncubateEgg", eggresult);
                }
            }
        } catch (LoginFailedException e) {
            logger.error("Login Failed", e);
            context.setConsoleString("IncubateEgg", "Login Error.");
        } catch (RemoteServerException e) {
            context.setConsoleString("IncubateEgg", "Server Error.");
            logger.error("Remote Server Exception", e);
        } finally {
            context.addTask(new IncubateEgg(context));
        }
    }
}
