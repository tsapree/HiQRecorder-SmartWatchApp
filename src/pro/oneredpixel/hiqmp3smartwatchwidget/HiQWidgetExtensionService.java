package pro.oneredpixel.hiqmp3smartwatchwidget;

import com.sonyericsson.extras.liveware.extension.util.ExtensionService;
import com.sonyericsson.extras.liveware.extension.util.registration.RegistrationInformation;
import com.sonyericsson.extras.liveware.extension.util.widget.WidgetExtension;


public class HiQWidgetExtensionService extends ExtensionService {

    /**
     * Extension key
     */
	//TODO: ÇÀ×ÅÌ?
    public static final String EXTENSION_KEY = "pro.oneredpixel.hiqmp3smartwatchwidget.key";

    public HiQWidgetExtensionService() {
        super(EXTENSION_KEY);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected RegistrationInformation getRegistrationInformation() {
        return new HiQWidgetRegistrationInformation(this);
    }

    @Override
    protected boolean keepRunningWhenConnected() {
        return false;
    }

    @Override
    public WidgetExtension createWidgetExtension(String hostAppPackageName) {
        return new HiQWidget(hostAppPackageName, this);
    }
	
}
