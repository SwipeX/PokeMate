package dekk.pw.pokemate.tasks;

import POGOProtos.Inventory.Item.ItemIdOuterClass;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import dekk.pw.pokemate.Context;
import dekk.pw.pokemate.PokeMateUI;

import static POGOProtos.Inventory.Item.ItemIdOuterClass.*;

/**
 * Created by TimD on 7/22/2016.
 */
public class DropItems implements Task {
    ItemId[] UNWANTED = new ItemId[]{ItemId.ITEM_POTION,
            ItemId.ITEM_SUPER_POTION, ItemId.ITEM_MAX_POTION, ItemId.ITEM_HYPER_POTION, ItemId.ITEM_RAZZ_BERRY,
            ItemId.ITEM_REVIVE, ItemId.ITEM_MAX_REVIVE};

    @Override
    public void run(Context context) {
        try {
            for (ItemId id : UNWANTED) {
                int count = context.getApi().getInventories().getItemBag().getItem(id).getCount();
                context.getApi().getInventories().getItemBag().removeItem(id, count);
                if (count > 0) {
                    PokeMateUI.toast("Removed " + count + " " + id.name());

                }
            }
        } catch (RemoteServerException | LoginFailedException e) {
            e.printStackTrace();
        }
    }
}
