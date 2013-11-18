package pro.oneredpixel.hiqmp3smartwatchwidget;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sonyericsson.extras.liveware.extension.util.widget.SmartWatchWidgetImage;

public class HiQWidgetImage extends SmartWatchWidgetImage {
	private String mStatus;

    public HiQWidgetImage(final Context context, final String status) {
        super(context);
        setInnerLayoutResourceId(R.layout.widget);
        mStatus = status;
    }

    @Override
    protected void applyInnerLayout(LinearLayout innerLayout) {
        // Set time
        ((TextView)innerLayout.findViewById(R.id.widget_status)).setText(mStatus);
    }
}
