package dekk.pw.pokemate.tasks;

import POGOProtos.Inventory.Item.ItemIdOuterClass;
import POGOProtos.Networking.Responses.CatchPokemonResponseOuterClass;
import com.pokegoapi.api.inventory.Item;
import com.pokegoapi.api.inventory.Pokeball;
import com.pokegoapi.api.map.pokemon.CatchResult;
import com.pokegoapi.api.map.pokemon.CatchablePokemon;
import com.pokegoapi.api.map.pokemon.EncounterResult;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import dekk.pw.pokemate.Config;
import dekk.pw.pokemate.Context;
import dekk.pw.pokemate.Walking;

import java.util.List;

/**
 * Created by TimD on 7/21/2016.
 */
public class CatchPokemon implements Task {

    public void run(Context context) {
        try {
            Pokeball pokeball = null;
            int flag=0;
            List<CatchablePokemon> pokemon = context.getApi().getMap().getCatchablePokemon();
            if (pokemon.size() > 0) {
            	if (Config.getPreferredBall()==-1)
            	{
            		Item item_pokeball = context.getApi().getInventories().getItemBag().getItem(ItemIdOuterClass.ItemId.forNumber(ItemIdOuterClass.ItemId.ITEM_POKE_BALL_VALUE));
            		Item item_greatball = context.getApi().getInventories().getItemBag().getItem(ItemIdOuterClass.ItemId.forNumber(ItemIdOuterClass.ItemId.ITEM_GREAT_BALL_VALUE));
            		Item item_ultraball = context.getApi().getInventories().getItemBag().getItem(ItemIdOuterClass.ItemId.forNumber(ItemIdOuterClass.ItemId.ITEM_ULTRA_BALL_VALUE));
            		Item item_masterball = context.getApi().getInventories().getItemBag().getItem(ItemIdOuterClass.ItemId.forNumber(ItemIdOuterClass.ItemId.ITEM_MASTER_BALL_VALUE));
                    if ((item_pokeball != null &&item_greatball != null &&item_ultraball != null && item_masterball!=null)&& (item_pokeball.getCount()+item_greatball.getCount()+item_ultraball.getCount()+item_masterball.getCount()) > 0) {
                        if (item_pokeball.getCount()>0) {
                        	pokeball=Pokeball.POKEBALL;
                        	flag=0;
                        	System.out.println("Remaining POKEball:"+item_pokeball.getCount());
                        	System.out.println("Remaining GREATball:"+item_greatball.getCount());
                        	System.out.println("Remaining ULTRAball:"+item_ultraball.getCount());
                        	System.out.println("Remaining MASTERball:"+item_masterball.getCount());
                        }
                        else if (item_greatball.getCount()>0) {
                        	pokeball=Pokeball.GREATBALL;
                        	flag=1;
                        	System.out.println("Remaining POKEball:"+item_pokeball.getCount());
                        	System.out.println("Remaining GREATball:"+item_greatball.getCount());
                        	System.out.println("Remaining ULTRAball:"+item_ultraball.getCount());
                        	System.out.println("Remaining MASTERball:"+item_masterball.getCount());
                        }
                        else if (item_ultraball.getCount()>0) {
                        	pokeball=Pokeball.ULTRABALL;
                        	flag=2;
                        	System.out.println("Remaining POKEball:"+item_pokeball.getCount());
                        	System.out.println("Remaining GREATball:"+item_greatball.getCount());
                        	System.out.println("Remaining ULTRAball:"+item_ultraball.getCount());
                        	System.out.println("Remaining MASTERball:"+item_masterball.getCount());
                        }
                        else if (item_masterball.getCount()>0) {
                        	pokeball=Pokeball.MASTERBALL;
                        	flag=3;
                        	System.out.println("Remaining POKEball:"+item_pokeball.getCount());
                        	System.out.println("Remaining GREATball:"+item_greatball.getCount());
                        	System.out.println("Remaining ULTRAball:"+item_ultraball.getCount());
                        	System.out.println("Remaining MASTERball:"+item_masterball.getCount());
                        }
                    }
            	}
            	else{
	                Item ball = context.getApi().getInventories().getItemBag().getItem(ItemIdOuterClass.ItemId.forNumber(Config.getPreferredBall()));
	                if (ball != null && ball.getCount() > 0) {
	                    pokeball = getBall(Config.getPreferredBall());
	                    if(pokeball==Pokeball.POKEBALL) flag=0;
	                    if(pokeball==Pokeball.GREATBALL) flag=1;
	                    if(pokeball==Pokeball.ULTRABALL) flag=2;
	                    if(pokeball==Pokeball.MASTERBALL) flag=3;
	                } else {
	                    pokeball = Pokeball.POKEBALL;
	                    flag=0;
	                }
            	}
                CatchablePokemon target = pokemon.get(0);
                if (target != null) {
                    Walking.setLocation(context);
                    EncounterResult encounterResult = target.encounterPokemon();
                    if (encounterResult.wasSuccessful()) {
                        CatchResult catchResult = target.catchPokemon(pokeball);
                        if (catchResult.getStatus().equals(CatchPokemonResponseOuterClass.CatchPokemonResponse.CatchStatus.CATCH_SUCCESS)) {
                        	Item tempball=null;
                        	if(flag==0) tempball=context.getApi().getInventories().getItemBag().getItem(ItemIdOuterClass.ItemId.forNumber(ItemIdOuterClass.ItemId.ITEM_POKE_BALL_VALUE));
                        	if(flag==1) tempball=context.getApi().getInventories().getItemBag().getItem(ItemIdOuterClass.ItemId.forNumber(ItemIdOuterClass.ItemId.ITEM_GREAT_BALL_VALUE));
                        	if(flag==2) tempball=context.getApi().getInventories().getItemBag().getItem(ItemIdOuterClass.ItemId.forNumber(ItemIdOuterClass.ItemId.ITEM_ULTRA_BALL_VALUE));
                        	if(flag==3) tempball=context.getApi().getInventories().getItemBag().getItem(ItemIdOuterClass.ItemId.forNumber(ItemIdOuterClass.ItemId.ITEM_MASTER_BALL_VALUE));
                            System.out.println("Caught a " + target.getPokemonId()+ " using a " + tempball.getItemId().name());
                        } else {
                            System.out.println(target.getPokemonId() + " failed.");
                        }
                    }
                }
            }
        } catch (LoginFailedException | RemoteServerException e) {
            e.printStackTrace();
        }
    }

    private Pokeball getBall(int id) {
        switch (id) {
            case ItemIdOuterClass.ItemId.ITEM_GREAT_BALL_VALUE:
                return Pokeball.GREATBALL;
            case ItemIdOuterClass.ItemId.ITEM_ULTRA_BALL_VALUE:
                return Pokeball.ULTRABALL;
            case ItemIdOuterClass.ItemId.ITEM_MASTER_BALL_VALUE:
                return Pokeball.MASTERBALL;
            default:
                return Pokeball.POKEBALL;
        }
    }
}
