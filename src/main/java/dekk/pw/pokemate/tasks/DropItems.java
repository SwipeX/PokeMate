package dekk.pw.pokemate.tasks;

import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import dekk.pw.pokemate.Context;
import dekk.pw.pokemate.PokeMateUI;
import dekk.pw.pokemate.Config;
import javafx.scene.image.Image;

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
		Config.getDroppedItems().stream().forEach(itemToDrop -> {
			ItemId id = ItemId.valueOf(itemToDrop);
			try {
				int count = context.getApi().getInventories().getItemBag().getItem(id).getCount();
				context.getApi().getInventories().getItemBag().removeItem(id, count);
				if (count > 0) {
					String removedItem = "Removed " + count + " " + id.name();
					PokeMateUI.toast(removedItem,"Items removed!", "icons/items/"+id.getNumber()+".png");
				}
			} catch (RemoteServerException | LoginFailedException e) {
				e.printStackTrace();
			}
		});
	}
}
