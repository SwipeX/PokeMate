package dekk.pw.pokemate.tasks;

import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import dekk.pw.pokemate.Context;
import dekk.pw.pokemate.util.Time;

import java.text.SimpleDateFormat;
import java.util.Date;

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
            context.setConsoleString("Update", "[" + new SimpleDateFormat("HH:mm:ss").format(new Date())+ "] Cache Updated");
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
