package dekk.pw.pokemate.tasks;

import com.pokegoapi.api.map.fort.Pokestop;
import com.pokegoapi.api.map.fort.PokestopLootResult;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import dekk.pw.pokemate.Config;
import dekk.pw.pokemate.Context;
import dekk.pw.pokemate.PokeMateUI;
import dekk.pw.pokemate.Walking;

import java.util.ArrayList;


import static dekk.pw.pokemate.tasks.Navigate.navigationType;
import static dekk.pw.pokemate.util.StringConverter.convertItemAwards;

/**
 * Created by TimD on 7/21/2016.
 */
public class TagPokestop extends Task implements Runnable {
    
    public TagPokestop(final Context context) {
        super(context);
    }

    @Override
    public void run() {
        try {
            ArrayList<Pokestop> pokestops = new ArrayList<>(context.getMap().getMapObjects().getPokestops());
            if (pokestops.size() == 0) {
                return;
            }
            pokestops.stream()
                .filter(Pokestop::canLoot)
                .forEach(near -> {
                    Walking.setLocation(context);
                    String result = null;
                    try {
                        result = resultMessage(near.loot());
                        PokeMateUI.toast(result, Config.POKE + "Stop interaction!", "icons/pokestop.png");
                        context.setConsoleString("TagPokestop", result);
                    } catch (LoginFailedException | RemoteServerException e) {
                        context.setConsoleString("TagPokestop", "Server Error.");
                        e.printStackTrace();
                    }
                });
            //System.out.println("[Tag PokeStop] Ending Loop");
        } catch (LoginFailedException | RemoteServerException e) {
            context.setConsoleString("TagPokestop", "Server Error");
        } finally {
            switch (navigationType) {
                case POKESTOPS:
                    break;
                case POKEMON:
                    //TODO: walk dynamically to nearest pokemon
                    break;
                default:
                    context.addTask(new TagPokestop(context));
            }
        }
    }

    private String resultMessage(final PokestopLootResult result) {
        switch (result.getResult()) {
            case SUCCESS:
                String retstr = "Tagged pokestop [" + result.getExperience() + "xp]";
                retstr += convertItemAwards(result.getItemsAwarded());
                return retstr;
            case INVENTORY_FULL:
                return "Tagged pokestop, but bag is full [+" + result.getExperience() + "xp]";
            case OUT_OF_RANGE:
                return "[CRITICAL]: COULD NOT TAG POKESTOP BECAUSE IT WAS OUT OF RANGE";
            case IN_COOLDOWN_PERIOD:
                return "[CRITICAL]: COULD NOT TAG POKESTOP BECAUSE IT WAS IN COOLDOWN";
            default:
                return "Failed Pokestop";
        }
    }

}
