package dekk.pw.pokemate.tasks;

import com.pokegoapi.api.player.PlayerLevelUpRewards;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import dekk.pw.pokemate.Config;
import dekk.pw.pokemate.Context;
import dekk.pw.pokemate.PokeMate;
import dekk.pw.pokemate.PokeMateUI;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;

import static dekk.pw.pokemate.Context.millisToTimeString;
import static dekk.pw.pokemate.util.StringConverter.convertItemAwards;

/**
 * Created by Andrew Sidhu on 8/1/2016.
 */
public class UpdateInterface extends Task implements Runnable {

    private static final int[] REQUIRED_EXPERIENCES = new int[]{0, 1000, 3000, 6000, 10000, 15000, 21000, 28000, 36000, 45000, 55000, 65000, 75000,
        85000, 100000, 120000, 140000, 160000, 185000, 210000, 260000, 335000, 435000, 560000, 710000, 900000, 1100000,
        1350000, 1650000, 2000000, 2500000, 3000000, 3750000, 4750000, 6000000, 7500000, 9500000, 12000000, 15000000, 20000000};

    private static int experienceGained = 0;
    private static long lastExperience = 0;
    private static int lastLevel;

    UpdateInterface(final Context context) {
        super(context);
    }

    @Override
    public void run() {
        // Clears old console output. (Probably won't work on windows)
        System.out.print("\033[H\033[2J");
        System.out.println(header());
        context.getConsoleStrings().forEach( (key,value) -> {
            if (value.isEmpty()) {
                System.out.println(key + ":");
            } else {
                System.out.printf("%-15.15s %-30s\n", "\t" + key, value);
            }
        });
    }

    private String header() {
        try {
            long runTime = System.currentTimeMillis() - PokeMate.startTime;
            long curTotalXP = context.getProfile().getStats().getExperience();

            if (curTotalXP > lastExperience) {
                if (lastExperience != 0) {
                    experienceGained += curTotalXP - lastExperience;
                     }
                lastExperience = curTotalXP;
            }
            int curLevel = context.getProfile().getStats().getLevel();
            double nextXP = REQUIRED_EXPERIENCES[context.getProfile().getStats().getLevel()] - REQUIRED_EXPERIENCES[context.getProfile().getStats().getLevel() - 1];
            double curLevelXP = context.getProfile().getStats().getExperience() - REQUIRED_EXPERIENCES[context.getProfile().getStats().getLevel() - 1];

            if (curLevel > lastLevel) {
                PlayerLevelUpRewards rewards = context.getProfile().acceptLevelUpRewards(curLevel - 1);
                if (rewards.getStatus() == PlayerLevelUpRewards.Status.NEW && Config.isShowUI()) {
                    final String levelUp = "New level: " + curLevel + convertItemAwards(rewards.getRewards());
                    PokeMateUI.toast(levelUp, "Level Up", "icons/items/backpack.png");
                }
                lastLevel = curLevel;
            }

            return String.format("Name: %-15s [%s] Level %d - %,.2fXP/H - Next Level in %,.0fXP - Runtime: %s",
                                    context.getProfile().getPlayerData().getUsername(),
                                    new SimpleDateFormat("HH:mm:ss").format(new Date()),
                                    curLevel,
                                    experienceGained / (runTime / 3.6E6),
                                    nextXP-curLevelXP,
                                    millisToTimeString(runTime));
        } catch (LoginFailedException | RemoteServerException e) {
            e.printStackTrace();
            return "Error Updating Header";
        }
    }
}
