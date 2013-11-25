package pro.oneredpixel.hiqmp3smartwatchwidget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Handler;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sonyericsson.extras.liveware.aef.control.Control;
import com.sonyericsson.extras.liveware.extension.util.control.ControlExtension;
import com.sonyericsson.extras.liveware.extension.util.control.ControlTouchEvent;


public class HiQControlSmartWatch extends ControlExtension {

    HiQLogic hql;
	
    private static final Bitmap.Config BITMAP_CONFIG = Bitmap.Config.RGB_565;
    private final int width;
    private final int height;
    private Handler mHandler;
    private PeriodicUpdate mPeriodicUpdate = null;
    private Bitmap mBackground;
    
    private static final long UPDATE_INTERVAL = 5 * DateUtils.SECOND_IN_MILLIS;

    HiQControlSmartWatch(final String hostAppPackageName, final Context context,
            Handler handler) {
        super(context, hostAppPackageName);
        if (handler == null) {
            throw new IllegalArgumentException("handler == null");
        }
        mHandler = handler;
        width = getSupportedControlWidth(context);
        height = getSupportedControlHeight(context);
        
        hql=new HiQLogic(mContext);
    }

    public static int getSupportedControlWidth(Context context) {
        return context.getResources().getDimensionPixelSize(R.dimen.smart_watch_control_width);
    }

    public static int getSupportedControlHeight(Context context) {
        return context.getResources().getDimensionPixelSize(R.dimen.smart_watch_control_height);
    }

    @Override
    public void onDestroy() {

    };

    @Override
    public void onStart() {
        // Nothing to do. Animation is handled in onResume.
    }

    @Override
    public void onStop() {
        // Nothing to do. Animation is handled in onPause.
    }

    @Override
    public void onResume() {
    	mPeriodicUpdate = new PeriodicUpdate();
    	mPeriodicUpdate.run();
    }

    @Override
    public void onPause() {
    	mPeriodicUpdate.stop();
    }

    @Override
    public void onTouch(final ControlTouchEvent event) {
    	//TODO: ПРОБЛЕМА с SmartWatch2, сдвинута граница срабатывания, пофиксить.
        if ((hql!=null) && (event.getAction() == Control.Intents.TOUCH_ACTION_RELEASE)) {
        	if (hql.getApplicationInstalledStatus()) {
        		//Intent intent;
	    		if (event.getX()<(width/2)) {
	    			hql.sendStart();
	    		} else {
	    			hql.sendStop();
	    		}
        	} else {
	    		hql.sendInstallApp(event.getX()>=(width/2));
 		
        	}
        }
        draw();
    }

    public void draw () {

        LinearLayout root = new LinearLayout(mContext);
        root.setLayoutParams(new LayoutParams(width, height));

        LinearLayout controlLayout;
        
        if (hql!=null) {
	    	if (hql.getApplicationInstalledStatus()) {
	    		hql.updateStatus();
	    		controlLayout = (LinearLayout)LinearLayout.inflate(mContext, R.layout.control, root);
	    		((TextView)controlLayout.findViewById(R.id.widget_status)).setText(hql.getTextStatus());
		        //showBitmap(new HiQWidgetImage(mContext, hql.getTextStatus()).getBitmap());
	    	} else {
	    		//show install app helper
	    		controlLayout = (LinearLayout)LinearLayout.inflate(mContext, R.layout.controllink, root);
	    		//showBitmap(new HiQWidgetImageNoApp(mContext).getBitmap());
	    		hql.updateApplicationInstalledStatus();
	    	}

	        controlLayout.measure(width, height);
	        controlLayout.layout(0, 0, controlLayout.getMeasuredWidth(), controlLayout.getMeasuredHeight());

	        Canvas canvas = new Canvas(mBackground);
	        canvas.drawColor(0xFF000000);
	        controlLayout.draw(canvas);

	        showBitmap(mBackground);

    	}

    }
    
    /**
     * The animation class shows an animation on the accessory. The animation
     * runs until mHandler.removeCallbacks has been called.
     */
    private class PeriodicUpdate implements Runnable {

        private boolean mIsStopped = false;

        /**
         * Create animation.
         */
        PeriodicUpdate() {
            mBackground = Bitmap.createBitmap(width, height, BITMAP_CONFIG);
            // Set default density to avoid scaling.
            mBackground.setDensity(DisplayMetrics.DENSITY_DEFAULT);

            mIsStopped = false;
            draw (); 
        }

        /**
         * Stop the animation.
         */
        public void stop() {
            mIsStopped = true;
        }

        public void run() {
            if (!mIsStopped) {
                draw();
            }
            if (mHandler != null && !mIsStopped) {
                mHandler.postDelayed(this, UPDATE_INTERVAL);
            }
        }
    };

	
}
