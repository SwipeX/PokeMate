package dekk.pw.pokemate.tasks;

import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import dekk.pw.pokemate.Context;
import dekk.pw.pokemate.PokeMateUI;
import javafx.scene.image.Image;

import static POGOProtos.Inventory.Item.ItemIdOuterClass.ItemId;

/**
 * Created by TimD on 7/22/2016.
 */
public class DropItems extends Task {
    ItemId[] UNWANTED = new ItemId[]{ItemId.ITEM_POTION,
            ItemId.ITEM_SUPER_POTION, ItemId.ITEM_MAX_POTION, ItemId.ITEM_HYPER_POTION, ItemId.ITEM_RAZZ_BERRY,
            ItemId.ITEM_REVIVE, ItemId.ITEM_MAX_REVIVE};

    DropItems(final Context context) {
        super(context);
    }

    @Override
    public void run() {
        try {
            for (ItemId id : UNWANTED) {
                int count = context.getApi().getInventories().getItemBag().getItem(id).getCount();
                context.getApi().getInventories().getItemBag().removeItem(id, count);
                if (count > 0) {
                    String removedItem = "Removed " + count + " " + id.name();
                    PokeMateUI.toast(removedItem,"Items removed!", "icons/items/"+id.getNumber()+".png");
                }
            }
        } catch (RemoteServerException | LoginFailedException e) {
            e.printStackTrace();
        }
    }
}
