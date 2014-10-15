package geocaching.login;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class AuthService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return new Authnticator(this).getIBinder();
    }
}
