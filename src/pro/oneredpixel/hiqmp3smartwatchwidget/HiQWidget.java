package pro.oneredpixel.hiqmp3smartwatchwidget;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;

import com.sonyericsson.extras.liveware.aef.widget.Widget;
import com.sonyericsson.extras.liveware.extension.util.SmartWatchConst;
import com.sonyericsson.extras.liveware.extension.util.widget.WidgetExtension;


public class HiQWidget extends WidgetExtension {

	String packageNameFull = "yuku.mp3recorder.full";
	String packageNameLite = "yuku.mp3recorder.lite";

	String actionRecord = "yuku.mp3recorder.action.RECORD";
	String actionStop = "yuku.mp3recorder.action.STOP";
	
	public static final int WIDTH = 128;
    public static final int HEIGHT = 110;

    private static final long UPDATE_INTERVAL = 5 * DateUtils.SECOND_IN_MILLIS;

    SharedPreferences sp;
    
    String status = "";
    String path="";

    final static int REC_UNKNOWN = 0;
    final static int REC_RECORDING = 1;
    final static int REC_STOPPED = 2;
    
    long lastSizeOfDirectory;
    int lastRecordingStatus=REC_UNKNOWN;

    boolean applicationInstalled=true;
    
    HiQWidget(final String hostAppPackageName, final Context context) {
        super(context, hostAppPackageName);
    }

    /**
     * Start refreshing the widget. The widget is now visible.
     */
    @Override
    public void onStartRefresh() {
    	
    	applicationInstalled=checkForAppInstalled();
    	
        cancelScheduledRefresh(HiQWidgetExtensionService.EXTENSION_KEY);
        scheduleRepeatingRefresh(System.currentTimeMillis(), UPDATE_INTERVAL,
        		HiQWidgetExtensionService.EXTENSION_KEY);
    }
    
    boolean checkForAppInstalled() {
    	boolean result = (isPackageInstalled(mContext,packageNameLite) || isPackageInstalled(mContext,packageNameFull));
    	//if (!result) Toast.makeText(mContext, "No app found", Toast.LENGTH_SHORT).show();
    	return result;
    }
    
    /**
     * Stop refreshing the widget. The widget is no longer visible.
     */
    @Override
    public void onStopRefresh() {
    	status="";

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

        	if (applicationInstalled) {
        		Intent intent;
	    		if (SmartWatchConst.ACTIVE_WIDGET_TOUCH_AREA.centerX()>x) {
	    			//pressed left half of screen, RECORD!
	    			intent = new Intent(actionRecord);
	    			status="Starting...";
	    			statusUnknown=0;
	    			resetRecordingStatus();
	    		} else {
	    			//pressed right half of screen, STOP!
	    			intent = new Intent(actionStop);
	    			status="Stopping...";
	    			statusUnknown=0;
	    			resetRecordingStatus();
	    		}
	    		mContext.sendBroadcast(intent);
        	} else {
        		String selectedAppName;
	    		if (SmartWatchConst.ACTIVE_WIDGET_TOUCH_AREA.centerX()>x) {
	    			selectedAppName=packageNameLite;
	    			//Toast.makeText(mContext, "Need to install lite version", Toast.LENGTH_SHORT).show();
	    		} else {
	    			selectedAppName=packageNameFull;
	    			//Toast.makeText(mContext, "Need to install full version", Toast.LENGTH_SHORT).show();
	    		}
	    		try {
	    		    mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id="+selectedAppName)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
	    		} catch (android.content.ActivityNotFoundException anfe) {
	    			mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id="+selectedAppName)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
	    		}
        		
        	}
    		
            updateWidget();
        }
    }
    
    boolean isPackageInstalled(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        boolean available = false;
        try {
            pm.getPackageInfo(packageName, 0);
            available = true;
        } catch (NameNotFoundException e) {
        }
            
        return available;
    }

    /**
    * @param context The application's environment.
    * @param action The Intent action to check for availability.
    *
    * @return True if an Intent with the specified action can be sent and
    *         responded to, false otherwise.
    */
    public static boolean isIntentAvailable(Context context, String action) {
     final PackageManager packageManager = context.getPackageManager();
     final Intent intent = new Intent(action);
     List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
                              PackageManager.MATCH_DEFAULT_ONLY);
     return list.size() > 0;
    }

    int statusUnknown=0;
    
    /**
     * Update the widget.
     */
    private void updateWidget() {
    	
    	if (applicationInstalled) {
	    	switch (getRecordingStatus()) {
	    	case REC_UNKNOWN:
	    		statusUnknown++;
	    		if (statusUnknown>3) status=""; //if no directory
	    		break;
	    	case REC_RECORDING:
	    		statusUnknown=0;
	    		if (!status.startsWith("Recording")) status="Recording";
	    		break;
	    	case REC_STOPPED:
	    		if (!status.startsWith("Stopped")) status="Stopped";
	    		break;
	    	};
	        showBitmap(new HiQWidgetImage(mContext, status).getBitmap());
    	} else {
    		//show install app helper
    		showBitmap(new HiQWidgetImageNoApp(mContext).getBitmap());
    		applicationInstalled=checkForAppInstalled();
    	}
    }
    
    //нужно сделать так:
    //1. если не знаем имя файла, ищем имя (саммый новый файл, возможно - новый, но не новее сейчас
    //2. берем размер и сверяем с предыдущим, если поменялся - статус запись идет
    //3. если размер не менялся - постоянно приходится сканить директорию, не айс
    //
    //пока - возвращаю размер директории
    
    void resetRecordingStatus() {
    	lastSizeOfDirectory=-1;
    	lastRecordingStatus=REC_UNKNOWN;
    }
    
    int getRecordingStatus() {
    	long size=0;
    	int result = REC_UNKNOWN;
    	String dirpath;
    	
        sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        path = sp.getString("path", "");
    	
    	if (path!=null && path.length()>0) {
    		dirpath=path;
    	} else {    	
    		File sdPath = android.os.Environment.getExternalStorageDirectory();
    		dirpath=sdPath.getAbsolutePath()+"/Recordings";
    	};
    	
    	File d=new File(dirpath);
    	if (!d.isDirectory()) {
    		//TODO: KILL
    		//Toast.makeText(mContext, String.format("checked dir: %s",dirpath), Toast.LENGTH_SHORT).show();
    		return result;
    	}
    	File[] files=d.listFiles();
    	for (int i = 0; i < files.length; i++) {
    		if (files[i].isFile()) {
    			size+=files[i].length();
    		};
    	}
    	if (lastSizeOfDirectory>=0) { 
    		if (lastSizeOfDirectory!=size) {
    			//Toast.makeText(mContext, String.format("Recording! checked dir: %s, newsize=%d",dirpath,size), Toast.LENGTH_SHORT).show();
    			result=REC_RECORDING;
    		}
    		else {
    			//Toast.makeText(mContext, String.format("Stopped! checked dir: %s newsize=%d",dirpath,size), Toast.LENGTH_SHORT).show();
    			result=REC_STOPPED;
    		}
    	} else {
    		//Toast.makeText(mContext, String.format("Skipped! checked dir: %s, newsize=%d",dirpath,size), Toast.LENGTH_SHORT).show();
    	}
    	lastSizeOfDirectory=size;
    	
    	//попытка убрать дребезжание
    	if (lastRecordingStatus!=result) {
    		lastRecordingStatus=result;
    		result=REC_UNKNOWN;
    	};
    	return result;
    }
}
