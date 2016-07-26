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
            for (ItemIdOuterClass.ItemId id : UNWANTED) {
                int count = context.getApi().getInventories().getItemBag().getItem(id).getCount();
                context.getApi().getInventories().getItemBag().removeItem(id, count);
                if (count > 0) {
                    System.out.println("Removed " + count + " " + id);
                }
            }
        } catch (RemoteServerException | LoginFailedException e) {
            e.printStackTrace();
        }
    }
}