package pro.oneredpixel.hiqmp3smartwatchwidget;

import android.content.Context;
import android.text.format.DateUtils;

import com.sonyericsson.extras.liveware.aef.widget.Widget;
import com.sonyericsson.extras.liveware.extension.util.SmartWatchConst;
import com.sonyericsson.extras.liveware.extension.util.widget.WidgetExtension;


public class HiQWidget extends WidgetExtension {
	
	public static final int WIDTH = 128;
    public static final int HEIGHT = 110;

    private static final long UPDATE_INTERVAL = 5 * DateUtils.SECOND_IN_MILLIS;

    HiQLogic hql;
    
    
    HiQWidget(final String hostAppPackageName, final Context context) {
        super(context, hostAppPackageName);
    }

    /**
     * Start refreshing the widget. The widget is now visible.
     */
    @Override
    public void onStartRefresh() {
    	hql=new HiQLogic(mContext);
 
        cancelScheduledRefresh(HiQWidgetExtensionService.EXTENSION_KEY);
        scheduleRepeatingRefresh(System.currentTimeMillis(), UPDATE_INTERVAL,
        		HiQWidgetExtensionService.EXTENSION_KEY);
    }
    
 
    
    /**
     * Stop refreshing the widget. The widget is no longer visible.
     */
    @Override
    public void onStopRefresh() {
    	if (hql!=null) hql.setTextStatus("");

    	// Cancel pending clock updates
        cancelScheduledRefresh(HiQWidgetExtensionService.EXTENSION_KEY);
    }

    @Override
    public void onScheduledRefresh() {
        //Log.d(SampleExtensionService.LOG_TAG, "scheduledRefresh()");
        updateWidget();
    }

    /**
     * Unregister update clock receiver, cancel pending updates
     */
    @Override
    public void onDestroy() {
        //Log.d(SampleExtensionService.LOG_TAG, "onDestroy()");
        onStopRefresh();
    }

    /**
     * The widget has been touched.
     *
     * @param type The type of touch event.
     * @param x The x position of the touch event.
     * @param y The y position of the touch event.
     */
    @Override
    public void onTouch(final int type, final int x, final int y) {
        //check that clicked in widget area
        if (!SmartWatchConst.ACTIVE_WIDGET_TOUCH_AREA.contains(x, y)) return;

        if (type == Widget.Intents.EVENT_TYPE_SHORT_TAP) {

        	
        	if (hql.getApplicationInstalledStatus()) {
        		//Intent intent;
	    		if (SmartWatchConst.ACTIVE_WIDGET_TOUCH_AREA.centerX()>x) {
	    			hql.sendStart();
	    		} else {
	    			hql.sendStop();
	    		}
        	} else {

	    		hql.sendInstallApp(SmartWatchConst.ACTIVE_WIDGET_TOUCH_AREA.centerX()<=x);
        		
        	}
    		
            updateWidget();
        }
    }
    
    /**
     * Update the widget.
     */
    private void updateWidget() {
    	if (hql!=null) {
	    	if (hql.getApplicationInstalledStatus()) {
	    		hql.updateStatus();
		        showBitmap(new HiQWidgetImage(mContext, hql.getTextStatus()).getBitmap());
	    	} else {
	    		//show install app helper
	    		showBitmap(new HiQWidgetImageNoApp(mContext).getBitmap());
	    		hql.updateApplicationInstalledStatus();
	    	}
    	}
    }
    

}
