package pro.oneredpixel.hiqmp3smartwatchwidget;

import android.content.Context;
import android.widget.LinearLayout;

import com.sonyericsson.extras.liveware.extension.util.widget.SmartWatchWidgetImage;

public class HiQWidgetImageNoApp  extends SmartWatchWidgetImage {

    public HiQWidgetImageNoApp(final Context context) {
        super(context);
        setInnerLayoutResourceId(R.layout.widgetlink);
    }
    
    @Override
    protected void applyInnerLayout(LinearLayout innerLayout) {
        // Set time
        //((TextView)innerLayout.findViewById(R.id.widget_status)).setText(mStatus);
    }
}
