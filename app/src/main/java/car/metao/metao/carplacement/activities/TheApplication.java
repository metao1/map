package car.metao.metao.carplacement.activities;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

/**
 * Created by metao on 2/14/2017.
 */
public class TheApplication extends Application {

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(context);
        MultiDex.install(this);
    }
    @Override
    public void onCreate() {
        super.onCreate();


    }
}