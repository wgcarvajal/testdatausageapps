package aranda.com.testdatausageapps;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Wilson Carvajal on 14/05/19.
 */
public class TestDeviceAdmin  extends DeviceAdminReceiver{


    @Override
    public void onProfileProvisioningComplete(Context context, Intent intent) {
        super.onProfileProvisioningComplete(context, intent);
    }
}
