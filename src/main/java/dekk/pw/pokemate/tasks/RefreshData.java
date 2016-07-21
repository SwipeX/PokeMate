package dekk.pw.pokemate.tasks;

import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import dekk.pw.pokemate.Context;

/**
 * Created by TimD on 7/21/2016.
 */
public class RefreshData implements Task {
    public void run(Context context) {
        try {
            context.setMapObjects(context.getApi().getMap().getMapObjects());
        } catch (LoginFailedException | RemoteServerException e) {
            e.printStackTrace();
        }
    }
}
