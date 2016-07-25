package dekk.pw.pokemate.tasks;

import com.pokegoapi.api.player.PlayerProfile;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import dekk.pw.pokemate.Config;
import dekk.pw.pokemate.Context;
import dekk.pw.pokemate.PokeMate;

import java.text.DecimalFormat;

/**
 * Created by TimD on 7/22/2016.
 */
public class Update implements Task {

    private static final int[] REQUIRED_EXPERIENCES = new int[]{0, 1000, 3000, 6000, 10000, 15000, 21000, 28000, 36000, 45000, 55000, 65000, 75000,
            85000, 100000, 120000, 140000, 160000, 185000, 210000, 260000, 335000, 435000, 560000, 710000, 900000, 1100000,
            1350000, 1650000, 2000000, 2500000, 3000000, 3750000, 4750000, 6000000, 7500000, 9500000, 12000000, 15000000, 20000000};

    private static int experienceGained = 0;
    private static long lastExperience = 0;

    private static DecimalFormat ratioFormat = new DecimalFormat("#0.00");

    @Override
    public void run(Context context) {
        try {
            PlayerProfile player;
            context.setProfile(player = context.getApi().getPlayerProfile());
            player.updateProfile();
            long nextXP = REQUIRED_EXPERIENCES[player.getStats().getLevel()] - REQUIRED_EXPERIENCES[player.getStats().getLevel() - 1];
            long curTotalXP = player.getStats().getExperience();
            long curLevelXP = curTotalXP - REQUIRED_EXPERIENCES[player.getStats().getLevel() - 1];
            String ratio = ratioFormat.format((double) curLevelXP / nextXP * 100.D);

            if (curTotalXP > lastExperience) {
                if (lastExperience != 0) {
                    experienceGained += curTotalXP - lastExperience;
                }
                lastExperience = curTotalXP;
            }

            long runTime = System.currentTimeMillis() - PokeMate.startTime;

            System.out.printf("Profile update : %d XP on LVL %d %s %% to LVL %d - Gained %d XP - XP/H: %.0f - Runtime: %s%n",
                    player.getStats().getExperience(),
                    player.getStats().getLevel(),
                    ratio,
                    player.getStats().getLevel() + 1,
                    experienceGained,
                    experienceGained / (runTime / 3.6E6),
                    millisToTimeString(runTime)
            );
        } catch (LoginFailedException | RemoteServerException e) {
            e.printStackTrace();
            System.out.println("Attempting to Login");
            Context.Login(context.getHttp());
        }
    }

    private static String millisToTimeString(long millis) {
        long seconds = (millis / 1000) % 60;
        long minutes = (millis / (1000 * 60)) % 60;
        long hours = (millis / (1000 * 60 * 60)) % 24;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

}
