package dekk.pw.pokemate.tasks;

import com.pokegoapi.api.player.PlayerProfile;
import dekk.pw.pokemate.Context;

import java.text.DecimalFormat;

/**
 * Created by TimD on 7/22/2016.
 */
public class Update implements Task {

    int[] requiredXp = new int[]{0, 1000, 3000, 6000, 10000, 15000, 21000, 28000, 36000, 45000, 55000, 65000, 75000,
            85000, 100000, 120000, 140000, 160000, 185000, 210000, 260000, 335000, 435000, 560000, 710000, 900000, 1100000,
            1350000, 1650000, 2000000, 2500000, 3000000, 3750000, 4750000, 6000000, 7500000, 9500000, 12000000, 15000000, 20000000};

    @Override
    public void run(Context context) {
        PlayerProfile player;
        context.setProfile(player = context.getApi().getPlayerProfile(true));
        int nextXP = requiredXp[player.getStats().getLevel()] - requiredXp[player.getStats().getLevel() - 1];
        long curLevelXP = player.getStats().getExperience() - requiredXp[player.getStats().getLevel() - 1];
        String ratio = new DecimalFormat("#0.00").format(curLevelXP / nextXP * 100.D);
        System.out.println("Profile update : " + player.getStats().getExperience() + " XP on LVL " + player.getStats().getLevel() +
                " " + curLevelXP/nextXP + " % to LVL " + (player.getStats().getLevel() + 1));

    }
}
