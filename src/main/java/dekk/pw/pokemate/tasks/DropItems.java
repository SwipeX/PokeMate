package dekk.pw.pokemate.tasks;

import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import dekk.pw.pokemate.Context;
import dekk.pw.pokemate.PokeMateUI;
import dekk.pw.pokemate.Config;
import dekk.pw.pokemate.util.Time;
import javafx.scene.image.Image;
import dekk.pw.pokemate.util.StringConverter;

import static POGOProtos.Inventory.Item.ItemIdOuterClass.ItemId;
import static dekk.pw.pokemate.util.Time.sleep;

/**
 * Created by TimD on 7/22/2016.
 */
public class DropItems extends Task {

    DropItems(final Context context) {
        super(context);
    }

    @Override
    public void run() {
        while(context.getRunStatus()) {
            Config.getDroppedItems().stream().forEach(itemToDrop -> {
                ItemId id = ItemId.valueOf(itemToDrop);
                try {
                    context.APILock.attempt(1000);
                    APIStartTime = System.currentTimeMillis();
                    int count = context.getApi().getInventories().getItemBag().getItem(id).getCount();
                    APIElapsedTime = System.currentTimeMillis() - APIStartTime;
                    if (APIElapsedTime < context.getMinimumAPIWaitTime()) {
                        sleep(context.getMinimumAPIWaitTime() - APIElapsedTime);
                    }
                    context.APILock.release();

                    if (count > 0) {
                        context.APILock.attempt(1000);
                        APIStartTime = System.currentTimeMillis();
                        context.getApi().getInventories().getItemBag().removeItem(id, count);
                        APIElapsedTime = System.currentTimeMillis() - APIStartTime;
                        if (APIElapsedTime < context.getMinimumAPIWaitTime()) {
                            sleep(context.getMinimumAPIWaitTime() - APIElapsedTime);
                        }
                        context.APILock.release();

                        String removedItem = "Removed " + StringConverter.titleCase(id.name()) + "(x" + count + ")";
                        PokeMateUI.toast(removedItem, "Items removed!", "icons/items/" + id.getNumber() + ".png");
                    }
                } catch (RemoteServerException | LoginFailedException e) {
                    System.out.println("[DropItems] Hit Rate Limited");
                    //e.printStackTrace();
                } catch (InterruptedException e) {
                    System.out.println("[] Error - Timed out waiting for API");
                    // e.printStackTrace();
                }
            });
        }
    }
}
