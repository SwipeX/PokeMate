package dekk.pw.pokemate.tasks;

import POGOProtos.Inventory.ItemIdOuterClass;
import POGOProtos.Networking.Responses.CatchPokemonResponseOuterClass;
import com.pokegoapi.api.inventory.Item;
import com.pokegoapi.api.inventory.Pokeball;
import com.pokegoapi.api.map.Pokemon.CatchResult;
import com.pokegoapi.api.map.Pokemon.CatchablePokemon;
import com.pokegoapi.api.map.Pokemon.EncounterResult;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import dekk.pw.pokemate.Context;

import java.util.List;

/**
 * Created by TimD on 7/21/2016.
 */
public class CatchPokemon implements Task {

    public void run(Context context) {
        try {
            Pokeball pokeball;
            List<CatchablePokemon> pokemon = context.getApi().getMap().getCatchablePokemon();
            if (!pokemon.isEmpty()) {
                Item ball = context.getApi().getBag().getItem(ItemIdOuterClass.ItemId.forNumber(context.getPreferredBall()));
                if (ball != null && ball.getCount() > 0) {
                    pokeball = getBall(context.getPreferredBall());
                } else {
                    pokeball = Pokeball.POKEBALL;
                }
                CatchablePokemon target = pokemon.get(0);
                if (target != null) {
                    EncounterResult encounterResult = target.encounterPokemon();
                    if (encounterResult.wasSuccessful()) {
                        CatchResult catchResult = target.catchPokemon(pokeball);
                        if (catchResult.getStatus().equals(CatchPokemonResponseOuterClass.CatchPokemonResponse.CatchStatus.CATCH_SUCCESS)) {
                            System.out.println("Caught a " + target.getPokemonId() + " using a " + ball.getItemId().name());
                        } else {
                            System.out.println(target.getPokemonId() + " fled.");
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
