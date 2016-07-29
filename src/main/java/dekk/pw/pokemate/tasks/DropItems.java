package dekk.pw.pokemate.tasks;

import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import dekk.pw.pokemate.Context;
import dekk.pw.pokemate.PokeMateUI;
import dekk.pw.pokemate.Config;
import dekk.pw.pokemate.util.Time;
import javafx.scene.image.Image;
import dekk.pw.pokemate.util.StringConverter;

import java.util.List;
import java.util.stream.Collectors;

import static POGOProtos.Inventory.Item.ItemIdOuterClass.ItemId;

/**
 * Created by TimD on 7/22/2016.
 */
public class DropItems extends Task {

    DropItems(final Context context) {
        super(context);
    }

    @Override
    public void run() {
        List<Item> itemsToDrop = context.getApi().getInventories().getItemBag().getItems().stream().filter(i-> Config.getDroppedItems().contains(i.getItemId().toString())).collect(Collectors.toList());

        itemsToDrop.forEach(itemToDrop -> {
            ItemId id = itemToDrop.getItemId();
            try {
                int count = itemToDrop.getCount();
                Time.sleepRate();
                if (count > 0) {
                    context.getApi().getInventories().getItemBag().removeItem(id, count);
                    Time.sleepRate();
                    String removedItem = "Removed " + StringConverter.convertItem(id.name()) + "(x" + count + ")";
                    PokeMateUI.toast(removedItem, "Items removed!", "icons/items/" + id.getNumber() + ".png");
                }
            } catch (RemoteServerException | LoginFailedException e) {
                e.printStackTrace();
            }
        });
    }
}
