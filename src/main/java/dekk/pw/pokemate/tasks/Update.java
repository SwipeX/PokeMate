package dekk.pw.pokemate.tasks;

import com.pokegoapi.exceptions.AsyncPokemonGoException;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import dekk.pw.pokemate.Context;
import dekk.pw.pokemate.util.Time;

/**
 * Created by TimD on 7/22/2016.
 */
public class Update extends Task implements Runnable{

    public Update(final Context context) {
        super(context);
    }

    @Override
    public void run() {
        try {
            context.refreshInventories();
            context.refreshMap();
            context.getProfile().updateProfile();
            context.setConsoleString("Update", "Cache Updated");
        } catch (LoginFailedException e) {
            context.setConsoleString("Update", "Login Failed, attempting to login again.");
            Context.Login(context.getHttp());
        } catch (RemoteServerException e) {
            context.setConsoleString("Update", "Server Error");
        }  catch (AsyncPokemonGoException e) {
            context.setConsoleString("Update", "API Async Error");
        }
    }
}
