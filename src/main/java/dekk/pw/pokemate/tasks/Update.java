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

    private static final int[] REQUIRED_EXPERIENCES = new int[]{0, 1000, 3000, 6000, 10000, 15000, 21000, 28000, 36000, 45000, 55000, 65000, 75000,
            85000, 100000, 120000, 140000, 160000, 185000, 210000, 260000, 335000, 435000, 560000, 710000, 900000, 1100000,
            1350000, 1650000, 2000000, 2500000, 3000000, 3750000, 4750000, 6000000, 7500000, 9500000, 12000000, 15000000, 20000000};

    private static int experienceGained = 0;
    private static long lastExperience = 0;
    private static int lastLevel;
	private static double xpHr;
    private static DecimalFormat ratioFormat = new DecimalFormat("#0.00");


    Update(final Context context) {
        super(context);
    }

    @Override
    public void run() {
        try {
            PlayerProfile player;
            Time.sleepRate();
            context.setProfile(player = context.getApi().getPlayerProfile());

            player.updateProfile();

            Time.sleepRate();
            context.getApi().getInventories().updateInventories(true);

            long runTime = System.currentTimeMillis() - PokeMate.startTime;
            long curTotalXP = player.getStats().getExperience();
            if (curTotalXP > lastExperience) {
                if (lastExperience != 0) {
                    experienceGained += curTotalXP - lastExperience;
                }
                lastExperience = curTotalXP;
            }
            int curLevel = player.getStats().getLevel();
            if (curLevel > lastLevel) {
                PlayerLevelUpRewards rewards = player.acceptLevelUpRewards(curLevel - 1);
                if (rewards.getStatus() == PlayerLevelUpRewards.Status.NEW) {
                    String levelUp = "New level: " + curLevel;
                    levelUp += convertItemAwards(rewards.getRewards());
                    PokeMateUI.toast(levelUp, "Level Up", "icons/items/backpack.png");
                }
                lastLevel = curLevel;
            }

            PokeMateUI.toast("XP Update: " + new DecimalFormat("###,###,###").format((experienceGained / (runTime / 3.6E6))) + "XP/H", "Progress Monitor", "icons/items/backpack.png");
            context.setConsoleString("Update", String.format("[%s] %5sXP/H", new SimpleDateFormat("HH:mm:ss").format(new Date()), new DecimalFormat("###,###,###").format((experienceGained / (runTime / 3.6E6)))));

        } catch (LoginFailedException e) {
            //e.printStackTrace();
            System.out.println("[Update] Login Failed, attempting to login again.");
            Context.Login(context.getHttp());
        } catch (RemoteServerException e) {
            context.setConsoleString("Update", "[" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "] - " + " Exceeded Rate Limit");
        } finally {
            context.addTask(new Update(context));
        }
    }
	
	public static String getXpHr() {
		return String.format("%.2f", xpHr);
	}

}
