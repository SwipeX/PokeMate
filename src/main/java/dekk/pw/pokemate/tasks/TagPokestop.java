package dekk.pw.pokemate.tasks;

import com.pokegoapi.api.map.MapObjects;
import com.pokegoapi.api.map.fort.Pokestop;
import com.pokegoapi.api.map.fort.PokestopLootResult;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import dekk.pw.pokemate.Config;
import dekk.pw.pokemate.Context;
import dekk.pw.pokemate.PokeMateUI;
import dekk.pw.pokemate.Walking;

import java.util.ArrayList;

import static dekk.pw.pokemate.util.StringConverter.convertItemAwards;
import static dekk.pw.pokemate.util.Time.sleep;

/**
 * Created by TimD on 7/21/2016.
 */
public class TagPokestop extends Task implements Runnable {

    private MapObjects map;
    
    TagPokestop(final Context context) {
        super(context);
    }

    @Override
    public void run() {
            try {
                APIStartTime = System.currentTimeMillis();
                map = context.getApi().getMap().getMapObjects();
                APIElapsedTime = System.currentTimeMillis() - APIStartTime;
                if (APIElapsedTime < context.getMinimumAPIWaitTime()) {
                    sleep(context.getMinimumAPIWaitTime() - APIElapsedTime);
                }
            } catch (RemoteServerException e) {
               System.out.println("[Tag PokeStop] Ending Loop - Exceeded Rate Limit Finding PokeStops ");
                return;
            } catch (LoginFailedException e) {
                //e.printStackTrace();
                System.out.println("[Tag PokeStop] Ending Loop - Login Failed");
            }
            ArrayList<Pokestop> pokestops = new ArrayList<>(map.getPokestops());
            if (pokestops.size() == 0) {
               // System.out.println("[Tag PokeStop] Ending Loop - No Stops Found");
                return;
            }
            pokestops.stream()
                .filter(Pokestop::canLoot)
                .forEach(near -> {
                    Walking.setLocation(context);
                   
                    String result = null;
                    try {
                        APIStartTime = System.currentTimeMillis();
                        result = resultMessage(near.loot());
                        APIElapsedTime = System.currentTimeMillis() - APIStartTime;
                        if (APIElapsedTime < context.getMinimumAPIWaitTime()) {
                            sleep(context.getMinimumAPIWaitTime() - APIElapsedTime);
                        }
                        PokeMateUI.toast(result, Config.POKE + "Stop interaction!", "icons/pokestop.png");
                    } catch (LoginFailedException e) {
                        //System.out.println("[Tag PokeStop] Ending Loop - Login Failed");
                        //e.printStackTrace();
                    } catch (RemoteServerException e) {
                        //System.out.println("[Tag PokeStop] Exceeded Rate Limit While looting");
                        //e.printStackTrace();
                    }
                });
            //System.out.println("[Tag PokeStop] Ending Loop");
        context.addTask(new TagPokestop(context));

    }

    private String resultMessage(final PokestopLootResult result) {
        switch (result.getResult()) {
            case SUCCESS:
                String retstr = "Tagged pokestop [+" + result.getExperience() + "xp]";
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
