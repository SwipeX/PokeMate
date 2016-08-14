package dekk.pw.pokemate.tasks;

import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import dekk.pw.pokemate.Config;
import dekk.pw.pokemate.Context;
import dekk.pw.pokemate.PokeMate;
import dekk.pw.pokemate.PokeMateUI;
import dekk.pw.pokemate.util.StringConverter;
import dekk.pw.pokemate.util.Time;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.Date;

import static POGOProtos.Inventory.Item.ItemIdOuterClass.ItemId;

/**
 * Created by TimD on 7/22/2016.
 */
class DropItems extends Task implements Runnable {

    private static final Logger logger = LogManager.getLogger(DropItems.class);

    DropItems(final Context context) {
        super(context);
    }

    @Override
    public void run() {
        StringBuilder removedItemsString = new StringBuilder();
        try {
            Config.getDroppedItems().forEach( (itemToDrop, minAmount) -> {
                ItemId id = ItemId.valueOf(itemToDrop);
                try {
                    int countToDrop = context.getApi().getInventories().getItemBag().getItem(id).getCount() - minAmount;
                    if (countToDrop > 0) {
                        context.getApi().getInventories().getItemBag().removeItem(id, countToDrop);
                        String removedItem = "Removed " + StringConverter.titleCase(id.name()) + "(x" + countToDrop + ")";
                        removedItemsString.append(removedItem + " ");
                        PokeMateUI.toast(removedItem, "Items removed!", "icons/items/" + id.getNumber() + ".png");
                    }
                } catch (LoginFailedException e) {
                    logger.error("Login Failed", e);
                    context.setConsoleString("DropItems", "Login Failed.");
                } catch (RemoteServerException e) {
                    context.setConsoleString("DropItems", "Server Error.");
                    logger.error("Remote Server Exception", e);
                }
            });
        } finally {
            if (!removedItemsString.toString().equals(""))
                context.setConsoleString("DropItems", removedItemsString.toString());
            context.addTask(new DropItems(context));
        }
    }
}
