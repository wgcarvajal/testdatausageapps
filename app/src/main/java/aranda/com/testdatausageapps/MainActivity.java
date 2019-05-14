package aranda.com.testdatausageapps;

import android.annotation.TargetApi;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private PackageManager mPackageManager;
    private TextView textDataUsageApps;

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        NetworkStatsManager mNetstatsManager;

        textDataUsageApps = findViewById(R.id.textDataUsageApps);
        mPackageManager = getPackageManager();
        mNetstatsManager = (NetworkStatsManager)getSystemService(
                Context.NETWORK_STATS_SERVICE);


        List<List<NetworkStats.Bucket>> mListData = new ArrayList<>();
        NetworkStats.Bucket bucket;

        Date mStartDate = new Date();
        mStartDate.setMonth(mStartDate.getMonth() - 1);
        Date mEndDate = new Date();

        NetworkStats result = null;
        try {
            result = mNetstatsManager.queryDetails(
                    ConnectivityManager.TYPE_MOBILE, "", mStartDate.getTime(),
                    mEndDate.getTime());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        mListData.clear();
        SparseArray<List<NetworkStats.Bucket>> uidMap = new SparseArray<>();
        if (result != null) {
            while (result.hasNextBucket()) {
                bucket = new NetworkStats.Bucket();
                result.getNextBucket(bucket);
                final int uid = bucket.getUid();
                List<NetworkStats.Bucket> list = uidMap.get(uid);
                if (list == null) {
                    list = new ArrayList<NetworkStats.Bucket>();
                    mListData.add(list);
                    uidMap.put(uid, list);
                }
                list.add(bucket);
            }
        }

        String text = "[";
        for(List<NetworkStats.Bucket> item: mListData)
        {
            text = bindView(item,text);
        }

        text = text + "]";
        textDataUsageApps.setText(text);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private String bindView(List<NetworkStats.Bucket> item,String t)
    {
        NetworkStats.Bucket bucket = item.get(0);
        String text = "\n{";
        final int uid = bucket.getUid();
        switch (uid) {
            case NetworkStats.Bucket.UID_REMOVED: {
                text = text + "Title : uidRemovido";

            }
            break;
            case NetworkStats.Bucket.UID_TETHERING: {
                text = text + "Title : uidTethering";
            }
            break;
            case android.os.Process.SYSTEM_UID: {
                text = text + "Title : uidSystem";
            }
            break;
            default: {
                final String[] packageNames = mPackageManager.getPackagesForUid(uid);
                final int length = packageNames != null ? packageNames.length : 0;

                try {
                    if (length == 1) {
                        final String pkgName = packageNames[0];
                        final ApplicationInfo info = mPackageManager.getApplicationInfo(pkgName,
                                0 /* no flags */);
                        if (info != null) {
                            text = text + "app title : "+info.loadLabel(mPackageManager);
                        }
                    } else {
                        for (int i = 0; i < length; i++) {
                            final String packageName = packageNames[i];
                            final PackageInfo packageInfo = mPackageManager.getPackageInfo(
                                    packageName, 0 /* no flags */);
                            final ApplicationInfo appInfo = mPackageManager.getApplicationInfo(
                                    packageName, 0 /* no flags */);

                            if (appInfo != null && packageInfo != null) {
                                if (packageInfo.sharedUserLabel != 0) {
                                    text = text + "app title : " +mPackageManager.getText(packageName,
                                            packageInfo.sharedUserLabel,
                                            packageInfo.applicationInfo);
                                }
                            }
                        }
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    // keep the default activity icon
                }

                final int bucketsCount = item.size();
                if (bucketsCount == 1) {
                    text = text +" summary: "+(bucket.getRxBytes() + bucket.getTxBytes())+ "bytes";
                    switch (bucket.getState()) {
                        case NetworkStats.Bucket.STATE_FOREGROUND: {
                            text = text +" state: network_stats_foreground_state";
                        } break;
                        case NetworkStats.Bucket.STATE_DEFAULT: {
                            text = text +" state: network_stats_default_state";
                        } break;
                        case NetworkStats.Bucket.STATE_ALL: {
                            text = text +" state: network_stats_combined_state";
                        } break;
                    }
                } else {
                    text = text +" summary: network_stats_items count: "+bucketsCount;
                    text = text +" state: network_stats_combined_state";
                }
            }
        }

        text = text + " }";

        t = t + text;
        return  t;
    }
}
