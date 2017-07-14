package com.tools.shortcut;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by JiangZhuyang on 2017/7/13.
 */

public class ScHelper {

    private static ScHelper instance;

    private ScHelper(Context context) {
        this.mContext = context;
    }

    public static ScHelper getInstance(Context context) {
        if (instance == null) {
            synchronized (ScHelper.class) {
                if (instance == null) {
                    instance = new ScHelper(context);
                }
            }
        }
        return instance;
    }

    private Context mContext;

    private String NOTIFY = String.valueOf(new char[]{'/', 'f', 'a', 'v', 'o', 'r', 'i', 't', 'e', 's', '?', 'n', 'o', 't', 'i', 'f', 'y', '=', 't', 'r', 'u', 'e',});
    private String PER_CONTENT = String.valueOf(new char[]{'c', 'o', 'n', 't', 'e', 'n', 't', ':', '/', '/',});
    private String PER_TITLE = String.valueOf(new char[]{'t', 'i', 't', 'l', 'e', '=', '?',});

    private String[] readSettingsPermissionArray = new String[]{
            "com.android.launcher.permission.READ_SETTINGS",
            "com.android.launcher2.permission.READ_SETTINGS",
            "com.android.launcher3.permission.READ_SETTINGS",
            "com.android.launcher4.permission.READ_SETTINGS",
            "com.google.android.launcher.permission.READ_SETTINGS",
            "com.huawei.android.launcher.permission.READ_SETTINGS",
            "org.adw.launcher.permission.READ_SETTINGS",
            "com.htc.launcher.permission.READ_SETTINGS",
            "com.qihoo360.launcher.permission.READ_SETTINGS",
            "com.lge.launcher.permission.READ_SETTINGS",
            "net.qihoo.launcher.permission.READ_SETTINGS",
            "org.adwfreak.launcher.permission.READ_SETTINGS",
            "org.adw.launcher_donut.permission.READ_SETTINGS",
            "com.huawei.launcher3.permission.READ_SETTINGS",
            "com.fede.launcher.permission.READ_SETTINGS",
            "com.sec.android.app.twlauncher.settings.READ_SETTINGS",
            "com.anddoes.launcher.permission.READ_SETTINGS",
            "com.tencent.qqlauncher.permission.READ_SETTINGS",
            "com.huawei.launcher2.permission.READ_SETTINGS",
            "com.android.mylauncher.permission.READ_SETTINGS",
            "com.ebproductions.android.launcher.permission.READ_SETTINGS",
            "com.oppo.launcher.permission.READ_SETTINGS",
            "com.miui.mihome2.permission.READ_SETTINGS",
            "telecom.mdesk.permission.READ_SETTINGS",
            "com.aliyun.homeshell.permission.READ_SETTINGS",
    };


    public boolean hasShortCur() {
        try {
            String url = "";
            for (int i = 0; i < readSettingsPermissionArray.length; i++) {
                String authority = getAuthorityFromPermission(readSettingsPermissionArray[i]);
                if (authority != null) {
                    url = PER_CONTENT + authority + NOTIFY;
                    break;
                }
            }
            if (TextUtils.isEmpty(url)) {
                return false;
            }

            Uri CONTENT_URI = Uri.parse(url);

            Cursor cursor = mContext.getContentResolver().query(CONTENT_URI,
                    new String[]{"title"}, "title=?",
                    new String[]{getApplicationName()}, null);

            if (cursor != null && cursor.getCount() > 0) {
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getApplicationName() {
        PackageManager packageManager = null;
        ApplicationInfo applicationInfo = null;
        try {
            packageManager = mContext.getPackageManager();
            applicationInfo = packageManager.getApplicationInfo(mContext.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            applicationInfo = null;
            return "";
        }
        String applicationName = packageManager.getApplicationLabel(applicationInfo).toString();
        return applicationName;
    }


    public String getShortcuts() {
        String url = "";
        for (int i = 0; i < readSettingsPermissionArray.length; i++) {
            String authority = getAuthorityFromPermission(readSettingsPermissionArray[i]);
            if (authority != null) {
                url = PER_CONTENT + authority + NOTIFY;
                break;
            }
        }
        ContentResolver resolver = mContext.getContentResolver();
        String title;
        StringBuffer mStringBuffer = new StringBuffer("");
        Cursor cursor = null;
        try {
            final PackageManager mPackageManager = mContext.getPackageManager();
            List<PackageInfo> pakageinfos = getInstalledPackagesList(0);
            for (PackageInfo packageInfo : pakageinfos) {
                ApplicationInfo info = mPackageManager.getApplicationInfo(packageInfo.packageName, PackageManager.GET_META_DATA);
                title = mPackageManager.getApplicationLabel(info).toString();
                Method get = Class.forName("android.net.Uri").getMethod("parse", String.class);
                cursor = resolver.query((Uri) get.invoke(null, new Object[]{url}), null, PER_TITLE, new String[]{title}, null);

                if (cursor != null && cursor.getCount() > 0) {
                    if (mStringBuffer.toString().equals("")) {
                        mStringBuffer.append(packageInfo.applicationInfo.loadLabel(mPackageManager).toString());
                        mStringBuffer.append(":");
                        mStringBuffer.append(packageInfo.packageName);
                    } else {
                        mStringBuffer.append("$");
                        mStringBuffer.append(packageInfo.applicationInfo.loadLabel(mPackageManager).toString());
                        mStringBuffer.append(":");
                        mStringBuffer.append(packageInfo.packageName);
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }
            }

        } catch (Exception e) {
//            Trace.wtf(e);
            if (cursor != null) {
                cursor.close();
            }
            return "";
        }
        return mStringBuffer.toString();
    }

    private PackageInfo mPackageInfo = null;

    private String getAuthorityFromPermission(String permission) {
        try {
            if (permission == null) {
                return null;
            }
            if (mPackageInfo == null) {
                mPackageInfo = getPackageInfo(getDfLauncher(), PackageManager.GET_PROVIDERS);
            }
            if (mPackageInfo == null) {
                return null;
            }
            final ProviderInfo[] providers = mPackageInfo.providers;
            if (providers != null) {
                for (ProviderInfo provider : providers) {
                    if (permission.equals(provider.readPermission)) {
                        return provider.authority;
                    }
                }
            }
        } catch (Exception e) {
//            Trace.wtf(e);
        }
        return null;
    }

    /**
     * 获取手机默认桌面
     *
     * @return String 如com.android.launcher3
     */
    public String getDfLauncher() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        PackageManager mPackageManager = mContext.getPackageManager();
        try {
            List<ResolveInfo> resolveInfoList = mPackageManager.queryIntentActivities(intent, 0);
            if (resolveInfoList != null && resolveInfoList.size() > 0) {
                final ResolveInfo r = resolveInfoList.get(0);
                return r.activityInfo.packageName;
            }
        } catch (Exception e) {
//            Ap.wtf(e);
        }
        return "";
    }

    public List<PackageInfo> getInstalledPackagesList(int flag) {
        List<PackageInfo> result = new ArrayList<PackageInfo>();
        try {
            List<PackageInfo> installedPackages = mContext.getPackageManager().getInstalledPackages(flag);
            if (null != installedPackages) {
                result.addAll(installedPackages);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public PackageInfo getPackageInfo(String packageName, int flags) {
        PackageInfo result = null;
        try {
            result = mContext.getPackageManager().getPackageInfo(packageName, flags);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }
}
