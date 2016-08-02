package dekk.pw.pokemate.tasks;

import POGOProtos.Networking.Responses.UseItemEggIncubatorResponseOuterClass;
import com.pokegoapi.api.inventory.EggIncubator;
import com.pokegoapi.api.pokemon.EggPokemon;
import dekk.pw.pokemate.Context;
import dekk.pw.pokemate.PokeMateUI;
import dekk.pw.pokemate.util.Time;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by $ Tim Dekker on 7/23/2016.
 */
class IncubateEgg extends Task implements Runnable{

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
                    e.printStackTrace();
                    return false;
                }
            }).collect(Collectors.toList());

            List<EggPokemon> eggs = context.getInventories().getHatchery().getEggs().stream().filter(egg ->
                egg.getEggIncubatorId() == null || egg.getEggIncubatorId().isEmpty()).collect(Collectors.toList());
            if (incubators.size() > 0 && eggs.size() > 0) {
                Time.sleepRate();
                UseItemEggIncubatorResponseOuterClass.UseItemEggIncubatorResponse.Result result = incubators.get(0).hatchEgg(eggs.get(0));
                if (result.equals(UseItemEggIncubatorResponseOuterClass.UseItemEggIncubatorResponse.Result.SUCCESS)) {
                    String eggresult = "Now incubating egg ( " + eggs.get(0).getEggKmWalkedTarget() + "km)";
                    PokeMateUI.toast(eggresult, "Egg Incubated!", "icons/items/egg.png");
                    context.setConsoleString("IncubateEgg", "[" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "] - " + eggresult);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            context.addTask(new IncubateEgg(context));
        }
    }
}
