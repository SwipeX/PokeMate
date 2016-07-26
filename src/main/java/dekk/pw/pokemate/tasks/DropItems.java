package dekk.pw.pokemate.tasks;

import POGOProtos.Inventory.Item.ItemIdOuterClass;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import dekk.pw.pokemate.Context;

import static POGOProtos.Inventory.Item.ItemIdOuterClass.*;

/**
 * Created by TimD on 7/22/2016.
 */
public class DropItems implements Task {
    ItemId[] UNWANTED = new ItemId[]{ItemId.ITEM_POTION,
            ItemId.ITEM_SUPER_POTION, ItemId.ITEM_MAX_POTION, ItemId.ITEM_RAZZ_BERRY,
            ItemId.ITEM_REVIVE, ItemId.ITEM_MAX_REVIVE,};

    @Override
    public void run(Context context) {
        try {
            for (ItemId aUNWANTED : UNWANTED) {
                int count = context.getApi().getInventories().getItemBag().getItem(aUNWANTED).getCount();
                context.getApi().getInventories().getItemBag().removeItem(aUNWANTED, count);
                if (count > 0 ) {
                    System.out.println("Removed " + count + " " + aUNWANTED);
                }
            }
        } catch (RemoteServerException | LoginFailedException e) {
            e.printStackTrace();
        }
    }
}
