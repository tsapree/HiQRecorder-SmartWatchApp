package pro.oneredpixel.hiqmp3smartwatchwidget;

import java.io.File;
import java.util.List;

import com.sonyericsson.extras.liveware.extension.util.SmartWatchConst;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.preference.PreferenceManager;

public class HiQLogic {

	private String packageNameFull = "yuku.mp3recorder.full";
	private String packageNameLite = "yuku.mp3recorder.lite";

	private String actionRecord = "yuku.mp3recorder.action.RECORD";
	private String actionStop = "yuku.mp3recorder.action.STOP";

    final static int REC_UNKNOWN = 0;
    final static int REC_RECORDING = 1;
    final static int REC_STOPPED = 2;
    
    private String status = "";
    private String path="";
    
    private SharedPreferences sp;
    
    private long lastSizeOfDirectory;
    private int lastRecordingStatus=REC_UNKNOWN;

    private boolean applicationInstalled=true;
    
    private int statusUnknown=0;
    
    private Context mContext;

    HiQLogic(Context c) {
    	mContext=c;
    	updateApplicationInstalledStatus();
    }
    
    boolean getApplicationInstalledStatus() {
    	return applicationInstalled;
    }
    
    void updateApplicationInstalledStatus() {
    	applicationInstalled=checkForAppInstalled();
    }
    
    void setTextStatus(String str) {
    	status=str;
    }
    
    String getTextStatus() {
    	return status;
    }
    
    void sendStart() {
    	Intent intent;
    	intent = new Intent(actionRecord);
		status="Starting...";
		statusUnknown=0;
		resetRecordingStatus();
		mContext.sendBroadcast(intent);
    }
    
    void sendStop() {
    	Intent intent;
    	intent = new Intent(actionStop);
		status="Stopping...";
		statusUnknown=0;
		resetRecordingStatus();
		mContext.sendBroadcast(intent);
    }
    
    void sendInstallApp(boolean full) {
    	String selectedAppName;
    	if (!full) {
			selectedAppName=packageNameLite;
		} else {
			selectedAppName=packageNameFull;
		}
    	try {
		    mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id="+selectedAppName)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
		} catch (android.content.ActivityNotFoundException anfe) {
			mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id="+selectedAppName)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
		}
    }
    
    void updateStatus() {
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
    }
    
    boolean checkForAppInstalled() {
    	boolean result = (isPackageInstalled(mContext,packageNameLite) || isPackageInstalled(mContext,packageNameFull));
    	//if (!result) Toast.makeText(mContext, "No app found", Toast.LENGTH_SHORT).show();
    	return result;
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
