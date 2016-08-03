package dekk.pw.pokemate.tasks;

import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import dekk.pw.pokemate.Config;
import dekk.pw.pokemate.Context;
import dekk.pw.pokemate.PokeMateUI;
import dekk.pw.pokemate.util.StringConverter;
import dekk.pw.pokemate.util.Time;

import java.text.SimpleDateFormat;
import java.util.Date;

import static POGOProtos.Inventory.Item.ItemIdOuterClass.ItemId;

/**
 * Created by TimD on 7/22/2016.
 */
class DropItems extends Task implements Runnable {

    DropItems(final Context context) {
        super(context);
    }

    @Override
    public void run() {
        Config.getDroppedItems().forEach((itemToDrop, minAmount) -> {
            ItemId id = ItemId.valueOf(itemToDrop);
            try {
                Time.sleepRate();
                int countToDrop = context.getApi().getInventories().getItemBag().getItem(id).getCount() - minAmount;
                Time.sleepRate();
                if (countToDrop > 0) {
                    context.getApi().getInventories().getItemBag().removeItem(id, countToDrop);
                    String removedItem = "Removed " + StringConverter.titleCase(id.name()) + "(x" + countToDrop + ")";
                    PokeMateUI.toast(removedItem, "Items removed!", "icons/items/" + id.getNumber() + ".png");
                    context.setConsoleString("DropItems", removedItem);
                }
            } catch (RemoteServerException | LoginFailedException e) {
                context.setConsoleString("Debug", "[" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "] - " + context.getConsoleStrings().get("Debug") + "    [DropItems] Exceeded Rate Limit\n");
                e.printStackTrace();
            }
        });
    }
}
