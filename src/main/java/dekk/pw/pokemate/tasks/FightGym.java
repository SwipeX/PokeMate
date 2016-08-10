package dekk.pw.pokemate.tasks;

import java.util.List;

import com.pokegoapi.api.gym.Battle;
import com.pokegoapi.api.gym.Gym;
import com.pokegoapi.api.pokemon.Pokemon;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import POGOProtos.Networking.Responses.StartGymBattleResponseOuterClass.StartGymBattleResponse.Result;
import dekk.pw.pokemate.Context;
import dekk.pw.pokemate.PokeMateUI;

/**
 * Created by Wolmain on 08/10/2016.
 */
class FightGym extends Task implements Runnable {

	FightGym(final Context context) {
        super(context);
    }

    @Override
    public void run() {
        
		try {
			
			List<Pokemon> pokemons = context.getApi().getInventories().getPokebank().getPokemons();
			Pokemon[] attackers = new Pokemon[6];

			for (int i = 0; i < 6; i++) {
				attackers[i] = pokemons.get(i);
			}


			for (Gym gym : context.getApi().getMap().getGyms()) {
				if (gym.isAttackable()) {
					Battle battle = gym.battle(attackers);
					Result result = battle.start();

					if (result == Result.SUCCESS) {
						
						PokeMateUI.addMessageToLog("Gym fight start !");
	                    context.setConsoleString("FightGym", "Gym fight start !");
						
						while (!battle.isConcluded()) {
							battle.attack(5);
							Thread.sleep(500);
						}
						
						PokeMateUI.addMessageToLog("Gym fight win : " + battle.getOutcome());
	                    context.setConsoleString("FightGym", "Gym fight win : " + battle.getOutcome());

					} else {
						PokeMateUI.addMessageToLog("Gym can't be fight : " + result);
	                    context.setConsoleString("FightGym", "Gym can't be fight : " + result);
					}
				}
			}
			
		} catch (LoginFailedException | RemoteServerException | InterruptedException e) {
			e.printStackTrace();
		} finally {
            context.addTask(new FightGym(context));
        }
		
    }
}
