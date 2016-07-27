package dekk.pw.pokemate.tasks;

import POGOProtos.Inventory.Item.ItemAwardOuterClass;
import com.pokegoapi.api.map.MapObjects;
import com.pokegoapi.api.map.fort.Pokestop;
import com.pokegoapi.api.map.fort.PokestopLootResult;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import dekk.pw.pokemate.Config;
import dekk.pw.pokemate.Context;
import dekk.pw.pokemate.PokeMateUI;
import dekk.pw.pokemate.Walking;
import javafx.scene.image.Image;

import java.util.ArrayList;

/**
 * Created by TimD on 7/21/2016.
 */
public class TagPokestop extends Task {

    TagPokestop(final Context context) {
        super(context);
    }

    @Override
    public void run() {
        try {
            MapObjects map = context.getApi().getMap().getMapObjects();
            ArrayList<Pokestop> pokestops = new ArrayList<>(map.getPokestops());
            if (pokestops.size() == 0) {
                return;
            }

            pokestops.stream()
                    .filter(Pokestop::canLoot)
                    .forEach(near -> {
                        Walking.setLocation(context);
                        try {
                            String result = resultMessage(near.loot());
                            PokeMateUI.toast(result, Config.POKE + "Stop interaction!", "icons/pokestop.png");
                        } catch (LoginFailedException | RemoteServerException e) {
                            e.printStackTrace();
                        }
                    });
        } catch (LoginFailedException | RemoteServerException e) {
            e.printStackTrace();
        }
    }

    private String resultMessage(final PokestopLootResult result) {
        switch (result.getResult()) {
            case SUCCESS:
                String retstr = "Tagged pokestop [+" + result.getExperience() + "xp]";
                int pokeBall = 0, greatBall = 0, ultraBall = 0, masterBall = 0, potion = 0, superPotion = 0, hyperPotion = 0, maxPotion = 0, razzBerry = 0, revive = 0, maxRevive = 0;
                for (ItemAwardOuterClass.ItemAward item : result.getItemsAwarded()) {
                    switch (item.getItemId().name()) {
                        case "ITEM_POKE_BALL":
                            pokeBall++;
                            break;
                        case "ITEM_GREAT_BALL":
                            greatBall++;
                            break;
                        case "ITEM_ULTRA_BALL":
                            ultraBall++;
                            break;
                        case "ITEM_MASTER_BALL":
                            masterBall++;
                            break;
                        case "ITEM_POTION":
                            potion++;
                            break;
                        case "ITEM_SUPER_POTION":
                            superPotion++;
                            break;
                        case "ITEM_HYPER_POTION":
                            hyperPotion++;
                            break;
                        case "ITEM_MAX_POTION":
                            maxPotion++;
                            break;
                        case "ITEM_REVIVE":
                            revive++;
                            break;
                        case "ITEM_MAX_REVIVE":
                            maxRevive++;
                            break;
                        case "ITEM_RAZZ_BERRY":
                            razzBerry++;
                            break;
                    }
                }
                if (pokeBall > 0)
                    retstr += " - Poke Ball (x" + pokeBall + ")";
                if (greatBall > 0)
                    retstr += " - Great Ball (x" + greatBall + ")";
                if (masterBall > 0)
                    retstr += " - Master Ball (x" + masterBall + ")";
                if (ultraBall > 0)
                    retstr += " - Ultra Ball (x" + ultraBall + ")";
                if (potion > 0)
                    retstr += " - Potion (x" + potion + ")";
                if (superPotion > 0)
                    retstr += " - Super Potion (x" + superPotion + ")";
                if (hyperPotion > 0)
                    retstr += " - Hyper Potion (x" + hyperPotion + ")";
                if (maxPotion > 0)
                    retstr += " - Max Potion (x" + maxPotion + ")";
                if (revive > 0)
                    retstr += " - Revive (x" + revive + ")";
                if (maxRevive > 0)
                    retstr += " - Max Revive Potion (x" + maxRevive + ")";
                if (razzBerry > 0)
                    retstr += " - Razz Berry (x" + razzBerry + ")";
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
