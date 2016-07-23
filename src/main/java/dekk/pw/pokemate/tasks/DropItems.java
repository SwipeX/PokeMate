package dekk.pw.pokemate.tasks;

import POGOProtos.Inventory.Item.ItemIdOuterClass;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import dekk.pw.pokemate.Context;

/**
 * Created by TimD on 7/22/2016.
 */
public class DropItems implements Task {
    ItemIdOuterClass.ItemId[] UNWANTED = new ItemIdOuterClass.ItemId[]{ItemIdOuterClass.ItemId.ITEM_POTION,
            ItemIdOuterClass.ItemId.ITEM_SUPER_POTION, ItemIdOuterClass.ItemId.ITEM_MAX_POTION, ItemIdOuterClass.ItemId.ITEM_RAZZ_BERRY,
            ItemIdOuterClass.ItemId.ITEM_REVIVE, ItemIdOuterClass.ItemId.ITEM_MAX_REVIVE,};

    @Override
    public void run(Context context) {
        try {
            for (int i = 0; i < UNWANTED.length; i++) {
                int count = context.getApi().getInventories().getItemBag().getItem(UNWANTED[i]).getCount();
                context.getApi().getInventories().getItemBag().removeItem(UNWANTED[i], count);
            }
        } catch (RemoteServerException | LoginFailedException e) {
            e.printStackTrace();
        }
    }
}
