package pro.oneredpixel.hiqmp3smartwatchwidget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ExtensionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        //Log.d(SampleExtensionService.LOG_TAG, "onReceive: " + intent.getAction());
        intent.setClass(context, HiQWidgetExtensionService.class);
        context.startService(intent);
    }

}
