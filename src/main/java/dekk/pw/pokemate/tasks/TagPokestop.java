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
import dekk.pw.pokemate.util.Time;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static dekk.pw.pokemate.tasks.Navigate.navigationType;
import static dekk.pw.pokemate.util.StringConverter.convertItemAwards;

/**
 * Created by TimD on 7/21/2016.
 */
public class TagPokestop extends Task implements Runnable {

    private MapObjects map;
    
    public TagPokestop(final Context context) {
        super(context);
    }

    @Override
    public void run() {

        try {
            map = context.getMap().getMapObjects();
            ArrayList<Pokestop> pokestops = new ArrayList<>(map.getPokestops());
            if (pokestops.size() == 0) {
                return;
            }
            pokestops.stream()
                .filter(Pokestop::canLoot)
                .forEach(near -> {
                    Walking.setLocation(context);
                    String result = null;
                    try {
                        Time.sleepRate();
                        result = resultMessage(near.loot());
                        PokeMateUI.toast(result, Config.POKE + "Stop interaction!", "icons/pokestop.png");
                        context.setConsoleString("TagPokestop", "[" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "] - " + result);
                    } catch (LoginFailedException e) {
                        //System.out.println("[Tag PokeStop] Ending Loop - Login Failed");
                        e.printStackTrace();
                    } catch (RemoteServerException e) {
                        //System.out.println("[Tag PokeStop] Exceeded Rate Limit While looting");
                        context.setConsoleString("TagPokestop", "[" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "] - " + "Exceeded Rate Limit While looting");
                        e.printStackTrace();
                    }
                });
        } catch (RemoteServerException e) {
        System.out.println("[Tag PokeStop] Ending Loop - Exceeded Rate Limit Finding PokeStops ");
        e.printStackTrace();
        return;
        } catch (LoginFailedException e) {
            e.printStackTrace();
            System.out.println("[Tag PokeStop] Ending Loop - Login Failed");
        } finally {
                switch (navigationType) {
                    case POKESTOPS:
                        break;
                    case POKEMON:
                        //TODO: walk dynamically to nearest pokemon
                        break;
                    default:
                        Time.sleepRate();
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
