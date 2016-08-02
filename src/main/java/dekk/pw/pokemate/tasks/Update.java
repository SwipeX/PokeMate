package dekk.pw.pokemate.tasks;

import com.pokegoapi.api.player.PlayerLevelUpRewards;
import com.pokegoapi.api.player.PlayerProfile;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import dekk.pw.pokemate.Context;
import dekk.pw.pokemate.PokeMate;
import dekk.pw.pokemate.PokeMateUI;
import dekk.pw.pokemate.util.Time;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;


import static dekk.pw.pokemate.util.StringConverter.convertItemAwards;
import static dekk.pw.pokemate.util.Time.sleep;

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
            Time.sleepRate();
            context.refreshInventories();
            Time.sleepRate();
            context.refreshMap();
            Time.sleepRate();
            context.getProfile().updateProfile();

        } catch (LoginFailedException e) {
            System.out.println("[Update] Login Failed, attempting to login again.");
            Context.Login(context.getHttp());
        } catch (RemoteServerException e) {
            context.setConsoleString("Update", "[" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "] - " + " Exceeded Rate Limit");
        } finally {
            context.addTask(new Update(context));
        }
    }

    public void runOnce() {
        try {
            Time.sleepRate();
            context.refreshInventories();
            Time.sleepRate();
            context.refreshMap();
            Time.sleepRate();
            context.getProfile().updateProfile();
        } catch (LoginFailedException e) {
            System.out.println("[Update] Login Failed, attempting to login again.");
            Context.Login(context.getHttp());
        } catch (RemoteServerException e) {
            context.setConsoleString("Update", "[" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "] - " + " Exceeded Rate Limit");
        }
    }
}
