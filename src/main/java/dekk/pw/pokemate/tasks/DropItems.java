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
public class DropItems extends Task implements Runnable {

    DropItems(final Context context) {
        super(context);
    }

    @Override
    public void run() {
        while(context.getRunStatus()) {
            System.out.println("[DropItems] Starting Loop");
            try {
                context.APILock.attempt(1000);
                Config.getDroppedItems().stream().forEach(itemToDrop -> {
                    ItemId id = ItemId.valueOf(itemToDrop);
                    APIStartTime = System.currentTimeMillis();
                    int count = context.getApi().getInventories().getItemBag().getItem(id).getCount();
                    APIElapsedTime = System.currentTimeMillis() - APIStartTime;
                    if (APIElapsedTime < context.getMinimumAPIWaitTime()) {
                        sleep(context.getMinimumAPIWaitTime() - APIElapsedTime);
                    }
                    if (count > Config.getMinItemAmount()) {
                        APIStartTime = System.currentTimeMillis();
                        try {
                            context.getApi().getInventories().getItemBag().removeItem(id, count - (count - Config.getMinItemAmount()));
                        } catch (RemoteServerException e) {
                            System.out.println("[DropItems] Exceeded request rate");
                            //e.printStackTrace();
                            context.APILock.release();
                        } catch (LoginFailedException e) {
                            //e.printStackTrace();
                            context.APILock.release();
                        }
                        APIElapsedTime = System.currentTimeMillis() - APIStartTime;
                        if (APIElapsedTime < context.getMinimumAPIWaitTime()) {
                            sleep(context.getMinimumAPIWaitTime() - APIElapsedTime);
                        }
                        String removedItem = "Removed " + StringConverter.titleCase(id.name()) + "(x" + count + ")";
                        PokeMateUI.toast(removedItem, "Items removed!", "icons/items/" + id.getNumber() + ".png");
                    }
                });
                //context.APILock.release();
            } catch (InterruptedException e) {
                System.out.println("[] Error - Timed out waiting for API");
                //context.APILock.release();
                // e.printStackTrace();
            } finally   {
                context.APILock.release();
            }
            System.out.println("[DropItems] Ending Loop");
        }
    }
}
