package dekk.pw.pokemate.tasks;

import com.pokegoapi.api.player.PlayerLevelUpRewards;
import com.pokegoapi.api.player.PlayerProfile;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import dekk.pw.pokemate.Context;
import dekk.pw.pokemate.PokeMate;
import dekk.pw.pokemate.PokeMateUI;
import dekk.pw.pokemate.util.Time;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static dekk.pw.pokemate.util.StringConverter.convertItemAwards;

/**
 * Created by Andrew Sidhu on 8/1/2016.
 */
public class ConsoleGUIUpdate extends Task implements Runnable {

    private static final int[] REQUIRED_EXPERIENCES = new int[]{0, 1000, 3000, 6000, 10000, 15000, 21000, 28000, 36000, 45000, 55000, 65000, 75000,
        85000, 100000, 120000, 140000, 160000, 185000, 210000, 260000, 335000, 435000, 560000, 710000, 900000, 1100000,
        1350000, 1650000, 2000000, 2500000, 3000000, 3750000, 4750000, 6000000, 7500000, 9500000, 12000000, 15000000, 20000000};

    private static int experienceGained = 0;
    private static long lastExperience = 0;
    private static int lastLevel;
    private static DecimalFormat ratioFormat = new DecimalFormat("#0.00");

    ConsoleGUIUpdate(final Context context) {
        super(context);
    }

    @Override
    public void run() {


        calcXPH();
        Set set = context.getConsoleStrings().entrySet();
        Iterator i = set.iterator();

        // Clears old console output. (Probably won't work on windows)
        System.out.print("\033[H\033[2J");
        System.out.print("Console GUI: [" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "]\n");

        context.getConsoleStrings().forEach( (key,value) -> {
            if (value.isEmpty()) {
                System.out.println(key + ":");
            } else {
                System.out.printf("%-15.15s %-30s\n", "\t" + key, value);
            }
        });
        context.addTask(new ConsoleGUIUpdate(context));
    }

    private void calcXPH() {
        try {
            long runTime = System.currentTimeMillis() - PokeMate.startTime;
            long curTotalXP = context.getProfile().getStats().getExperience();

            if (curTotalXP > lastExperience) {
                if (lastExperience != 0) {
                    experienceGained += curTotalXP - lastExperience;
                    context.setConsoleString("Update", String.format("[%s] %5sXP/H", new SimpleDateFormat("HH:mm:ss").format(new Date()), new DecimalFormat("###,###,###").format((experienceGained / (runTime / 3.6E6)))));
                }
                lastExperience = curTotalXP;
            }
            int curLevel = context.getProfile().getStats().getLevel();
            if (curLevel > lastLevel) {
                PlayerLevelUpRewards rewards = context.getProfile().acceptLevelUpRewards(curLevel - 1);
                if (rewards.getStatus() == PlayerLevelUpRewards.Status.NEW) {
                    String levelUp = "New level: " + curLevel;
                    levelUp += convertItemAwards(rewards.getRewards());
                    PokeMateUI.toast(levelUp, "Level Up", "icons/items/backpack.png");
                }
                lastLevel = curLevel;
            }
        } catch (LoginFailedException e) {
            e.printStackTrace();
        } catch (RemoteServerException e) {
            e.printStackTrace();
        }
    }
}
