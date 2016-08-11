package dekk.pw.pokemate.tasks;

import com.pokegoapi.api.pokemon.Pokemon;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;

import POGOProtos.Inventory.Item.ItemIdOuterClass.ItemId;
import dekk.pw.pokemate.Context;
import dekk.pw.pokemate.PokeMateUI;

/**
 * Created by Wolmain on 08/10/2016.
 */
class HealPokemon extends Task implements Runnable {

    HealPokemon(final Context context) {
        super(context);
    }

    @Override
    public void run() {
        
		try {
			for(Pokemon p : context.getApi().getInventories().getPokebank().getPokemons()) {
				
				if(p.isFainted()) {
					p.useRevive(ItemId.ITEM_REVIVE);
					
					PokeMateUI.addMessageToLog("Revive " + p.getPokemonId().toString());
                    context.setConsoleString("HealPokemon", "Revive " + p.getPokemonId().toString());
				
				} else if(p.isInjured()) {
					p.heal();
                    
                    PokeMateUI.addMessageToLog("Heal " + p.getPokemonId().toString());
                    context.setConsoleString("HealPokemon", "Heal " + p.getPokemonId().toString());
				}	
				
			}
		} catch (LoginFailedException | RemoteServerException e) {
			e.printStackTrace();
		} finally {
            context.addTask(new HealPokemon(context));
        }
		
    }
}
