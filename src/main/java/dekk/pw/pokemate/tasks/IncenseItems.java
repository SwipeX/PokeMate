package dekk.pw.pokemate.tasks;

import POGOProtos.Inventory.Item.ItemIdOuterClass;
import POGOProtos.Networking.Requests.Messages.UseIncenseMessageOuterClass;
import POGOProtos.Networking.Requests.RequestTypeOuterClass;
import POGOProtos.Networking.Responses.UseIncenseResponseOuterClass;
import com.pokegoapi.api.inventory.Item;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import com.pokegoapi.main.ServerRequest;
import dekk.pw.pokemate.Context;
import dekk.pw.pokemate.PokeMateUI;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Kyle on 8/1/2016.
 */
public class IncenseItems extends Task implements Runnable {

    private static final long INCENSE_TIME_OUT = 1000 * 60 * 5;
    private long lastUsed = -1L;

    public IncenseItems(final Context context)
    {
        super(context);
    }

    @Override
    public void run() {

        try {

            final Item incense = context.getApi().getInventories().getItemBag().getItem(ItemIdOuterClass.ItemId.ITEM_INCENSE_ORDINARY);
            final long curTime = context.getApi().currentTimeMillis();

            // We have no incense or we have recently tried to use incense
            if(incense.getCount() <= 0 || ((curTime - lastUsed) <= INCENSE_TIME_OUT && lastUsed != -1L))
                return;

            // Perform the request
            UseIncenseResponseOuterClass.UseIncenseResponse response = useIncenseResponse(ItemIdOuterClass.ItemId.ITEM_INCENSE_ORDINARY);

            // If the request goes through then 'toast' and mark the current time
            final UseIncenseResponseOuterClass.UseIncenseResponse.Result result = response.getResult();
            if(result == UseIncenseResponseOuterClass.UseIncenseResponse.Result.SUCCESS) {
                PokeMateUI.toast("Used Ordinary Incense", "Incense Item Used", "icons/items/" + ItemIdOuterClass.ItemId.ITEM_INCENSE_ORDINARY_VALUE + ".png");
                context.setConsoleString("IncenseItems", "[" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "] - Used Ordinary Incense");
                lastUsed = curTime;
            }
            else if( result == UseIncenseResponseOuterClass.UseIncenseResponse.Result.INCENSE_ALREADY_ACTIVE) {
                context.setConsoleString("IncenseItems", "Ordinary Incense Still Active");
            }

        } catch(final Exception ex) {
            ex.printStackTrace();
        } finally {
            context.addTask(new IncenseItems(context));
        }
    }


    private UseIncenseResponseOuterClass.UseIncenseResponse useIncenseResponse(ItemIdOuterClass.ItemId type) throws RemoteServerException, LoginFailedException {
        UseIncenseMessageOuterClass.UseIncenseMessage useIncenseMessage =
                UseIncenseMessageOuterClass.UseIncenseMessage.newBuilder()
                        .setIncenseType(type)
                        .setIncenseTypeValue(type.getNumber())
                        .build();

        ServerRequest useIncenseRequest = new ServerRequest(RequestTypeOuterClass.RequestType.USE_INCENSE,
                useIncenseMessage);
        context.getApi().getRequestHandler().sendServerRequests(useIncenseRequest);

        UseIncenseResponseOuterClass.UseIncenseResponse response = null;
        try {
            response = UseIncenseResponseOuterClass.UseIncenseResponse.parseFrom(useIncenseRequest.getData());
        } catch(Exception ex) {}

        return response;
    }

}
