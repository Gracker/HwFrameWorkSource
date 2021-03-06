package com.android.server;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.server.hidata.wavemapping.modelservice.ModelBaseService;
import com.huawei.android.bastet.IBastetListener;
import com.huawei.android.bastet.IBastetListener.Stub;
import com.huawei.android.bastet.IBastetManager;
import com.huawei.pgmng.IPGPlugCallbacks;
import com.huawei.pgmng.PGAction;
import com.huawei.pgmng.PGPlug;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class HwBastetService extends SystemService {
    private static final int ACTION_BASTET_SET_DSCP = 10;
    private static final int ACTION_BASTET_UNSET_DSCP = 11;
    private static final int ACTION_BASTET_UPDATE_CONFIG = 9;
    private static final int ACTION_BOOT_COMPLETED = 3;
    private static final int ACTION_PACKAGE_ADDED = 4;
    private static final int ACTION_PACKAGE_CHANGED = 5;
    private static final int ACTION_PACKAGE_FIRST_LAUNCH = 7;
    private static final int ACTION_PACKAGE_REMOVED = 8;
    private static final int ACTION_PACKAGE_RESTARTED = 6;
    private static final int ACTION_SCREEN_OFF = 2;
    private static final int ACTION_SCREEN_ON = 1;
    private static final int ACTION_SEND_HEARTBEAT = 0;
    private static final int ACTION_SEND_NRT = 1;
    private static final int ACTION_UNKNOWN = 0;
    private static final String BASTET_ACTION_SEND_HEARTBEAT = "com.huawei.android.bastet.ACTION_SEND_HEARTBEAT";
    private static final String BASTET_ACTION_SEND_NRT = "com.huawei.android.bastet.ACTION_SEND_NRT";
    private static final String BASTET_DSCP_SET_ACTION = "huawei.intent.action.PUSH_HW_BASTET_SET_DSCP_ACTION";
    private static final String BASTET_DSCP_UNSET_ACTION = "huawei.intent.action.PUSH_HW_BASTET_UNSET_DSCP_ACTION";
    private static final String BASTET_SERVICE = "BastetService";
    private static final int CONNECT_INTERVAL = 5000;
    private static final int CONNECT_TIMES = 5;
    private static final String HW_BASTET_PARTNER_PATH = "/data/bastet/hw_bastet_partner.xml";
    private static final int INTENT_PACKAGE_NAME_OFFSET = 8;
    private static final int INVALID_UID = -1;
    private static final int MESSAGE_BROADCAST_ACTION = 1;
    private static final int MESSAGE_CHECK_SERVICE_TIMEOUT = 2;
    private static final int MESSAGE_PARSE_ACC_APP_LIST = 3;
    private static final int MESSAGE_UNKNOWN = 0;
    private static final String PROPEL_HW_BASTET_CONFIG_ACTION = "huawei.intent.action.PUSH_HW_BASTET_CONFIG_ACTION";
    private static final String PROPEL_TYPE_EXTRA = "pushType";
    private static final int PROPEL_TYPE_PARTNER = 1;
    private static final int PROPEL_TYPE_UNKNOWN = 0;
    private static final String PROPEL_TYPE_UPDATE_BASTET_PARTNER = "hw_bastet_partner";
    private static final String PROPEL_URI_EXTRA = "uri";
    private static final String TAG = "HwBastetService";
    private ArrayList<String> mAccAppList = new ArrayList();
    private AlarmManager mAlarmManager;
    private final BroadcastReceiver mAlarmReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                if (action.equals(HwBastetService.BASTET_ACTION_SEND_NRT)) {
                    HwBastetService.this.handleNotifyNrt();
                } else if (action.equals(HwBastetService.BASTET_ACTION_SEND_HEARTBEAT)) {
                    HwBastetService.this.handleAlarmTimeout(0);
                }
            }
        }
    };
    private IBinder mBastetService;
    private int mConnectTimes = 0;
    private Context mContext;
    private DeathRecipient mDeathRecipient = new DeathRecipient() {
        public void binderDied() {
            Log.e(HwBastetService.TAG, "Bastet service has died!");
            synchronized (HwBastetService.this) {
                if (HwBastetService.this.mBastetService != null) {
                    HwBastetService.this.mBastetService.unlinkToDeath(this, 0);
                    HwBastetService.this.mBastetService = null;
                    HwBastetService.this.mIBastetManager = null;
                }
                HwBastetService.this.mHrtAppList.clear();
                HwBastetService.this.mAccAppList.clear();
            }
            HwBastetService.this.mAlarmManager.cancel(HwBastetService.this.mNrtPendingIntent);
            HwBastetService.this.mAlarmManager.cancel(HwBastetService.this.mHeartBeatPendingIntent);
            HwBastetService.this.mConnectTimes = 0;
            HwBastetService.this.checkBastetService();
        }
    };
    private int mForegroundUid = -1;
    private Handler mHandler;
    private PendingIntent mHeartBeatPendingIntent = null;
    private ArrayList<Integer> mHrtAppList = new ArrayList();
    protected IBastetListener mIBastetListener = new Stub() {
        public void onProxyIndicateMessage(int proxyId, int err, int ext) throws RemoteException {
            String str = HwBastetService.TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("onProxyIndicateMessage: proxyId=");
            stringBuilder.append(proxyId);
            stringBuilder.append(", err=");
            stringBuilder.append(err);
            stringBuilder.append(", ext=");
            stringBuilder.append(ext);
            Log.e(str, stringBuilder.toString());
            switch (err) {
                case ModelBaseService.DISCRIMINATE_RET_CODE_ERROR3 /*-23*/:
                    cancelAlarm(ext);
                    return;
                case -22:
                    HwBastetService.this.mAlarmManager.setExact(2, SystemClock.elapsedRealtime() + ((long) ext), HwBastetService.this.mHeartBeatPendingIntent);
                    return;
                case ModelBaseService.DISCRIMINATE_RET_CODE_ERROR1 /*-21*/:
                    HwBastetService.this.removeHrtApp(ext);
                    return;
                case ModelBaseService.TRAINMODEL_RET_ERROR_CODE_20 /*-20*/:
                    HwBastetService.this.addHrtApp(ext);
                    return;
                case ModelBaseService.TRAINMODEL_RET_ERROR_CODE_18 /*-18*/:
                    HwBastetService.this.mAlarmManager.setExact(2, SystemClock.elapsedRealtime() + ((long) ext), HwBastetService.this.mNrtPendingIntent);
                    if (HwBastetService.this.mTelephonyManager != null) {
                        HwBastetService.this.mTelephonyManager.listen(HwBastetService.this.mPhoneStateListener, 128);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }

        private void cancelAlarm(int operation) {
            PendingIntent pi;
            switch (operation) {
                case 0:
                    pi = HwBastetService.this.mHeartBeatPendingIntent;
                    break;
                case 1:
                    pi = HwBastetService.this.mNrtPendingIntent;
                    break;
                default:
                    return;
            }
            if (pi == null) {
                Log.e(HwBastetService.TAG, "cancelAlarm opration is null");
            } else {
                HwBastetService.this.mAlarmManager.cancel(pi);
            }
        }
    };
    private IBastetManager mIBastetManager;
    private int mLastAccUid = -1;
    private int mLastHrtUid = -1;
    private PendingIntent mNrtPendingIntent = null;
    private PGPlug mPGPlug;
    private PgEventProcesser mPgEventProcesser = new PgEventProcesser(this, null);
    private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        public void onDataActivity(int direction) {
            if (direction == 1 || direction == 3) {
                HwBastetService.this.handleNotifyNrt();
            }
        }
    };
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                int actionId = parseAction(action, intent);
                String str = HwBastetService.TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("BroadcastReceiver ");
                stringBuilder.append(action);
                Log.d(str, stringBuilder.toString());
                if (actionId != 0) {
                    Message msg = HwBastetService.this.mHandler.obtainMessage();
                    msg.what = 1;
                    msg.arg1 = actionId;
                    switch (actionId) {
                        case 4:
                        case 5:
                        case 6:
                        case 7:
                        case 8:
                            msg.obj = intent.getDataString().substring(8);
                            break;
                        case 9:
                            String value = intent.getStringExtra(HwBastetService.PROPEL_TYPE_EXTRA);
                            if (value != null && value.equals(HwBastetService.PROPEL_TYPE_UPDATE_BASTET_PARTNER)) {
                                msg.arg2 = 1;
                            }
                            msg.obj = intent.getStringExtra(HwBastetService.PROPEL_URI_EXTRA);
                            break;
                    }
                    HwBastetService.this.mHandler.sendMessage(msg);
                }
            }
        }

        private int parseAction(String action, Intent intent) {
            if (action.equals("android.intent.action.SCREEN_ON")) {
                return 1;
            }
            if (action.equals("android.intent.action.SCREEN_OFF")) {
                HwBastetService.this.setAppBackground();
                return 2;
            } else if (action.equals("android.intent.action.BOOT_COMPLETED")) {
                HwBastetService.this.initPgPlugThread();
                return 3;
            } else if (action.equals(HwBastetService.PROPEL_HW_BASTET_CONFIG_ACTION)) {
                return 9;
            } else {
                if (action.equals("android.intent.action.PACKAGE_ADDED")) {
                    return 4;
                }
                if (action.equals("android.intent.action.PACKAGE_CHANGED")) {
                    return 5;
                }
                if (action.equals("android.intent.action.PACKAGE_RESTARTED")) {
                    return 6;
                }
                if (action.equals("android.intent.action.PACKAGE_FIRST_LAUNCH")) {
                    return 7;
                }
                if (action.equals("android.intent.action.PACKAGE_REMOVED")) {
                    HwBastetService.this.removeHrtApp(intent.getDataString().substring(8));
                    return 8;
                } else if (action.equals(HwBastetService.BASTET_DSCP_SET_ACTION)) {
                    return 10;
                } else {
                    if (action.equals(HwBastetService.BASTET_DSCP_UNSET_ACTION)) {
                        return 11;
                    }
                    return 0;
                }
            }
        }
    };
    private TelephonyManager mTelephonyManager;
    private HandlerThread mThread;

    private class HwBastetHandler extends Handler {
        public HwBastetHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    Log.e(HwBastetService.TAG, "MESSAGE_UNKNOWN");
                    return;
                case 1:
                    HwBastetService.this.doBroadcastMessage(msg);
                    return;
                case 2:
                    HwBastetService.this.checkBastetService();
                    return;
                case 3:
                    HwBastetService.this.parseAccAppList(msg);
                    return;
                default:
                    return;
            }
        }
    }

    private class PgEventProcesser implements IPGPlugCallbacks {
        private PgEventProcesser() {
        }

        /* synthetic */ PgEventProcesser(HwBastetService x0, AnonymousClass1 x1) {
            this();
        }

        public void onDaemonConnected() {
        }

        public void onConnectedTimeout() {
        }

        public boolean onEvent(int actionID, String msg) {
            if (PGAction.checkActionType(actionID) == 1 && PGAction.checkActionFlag(actionID) == 3) {
                HwBastetService.this.filterAccApp(msg, HwBastetService.this.filterHrtApp(msg));
            }
            return true;
        }
    }

    public void onStart() {
        Log.i(TAG, "start HwBastetService");
        initBroadcastReceiver();
        initHandlerThread();
        initTelephonyService();
        getBastetService();
    }

    public HwBastetService(Context context) {
        super(context);
        Log.i(TAG, TAG);
        this.mContext = context;
    }

    private void initGenericReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SCREEN_ON");
        filter.addAction("android.intent.action.SCREEN_OFF");
        filter.addAction("android.intent.action.BOOT_COMPLETED");
        this.mContext.registerReceiver(this.mReceiver, filter);
    }

    private void initPackageReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.PACKAGE_ADDED");
        filter.addAction("android.intent.action.PACKAGE_REMOVED");
        filter.addAction("android.intent.action.PACKAGE_CHANGED");
        filter.addAction("android.intent.action.PACKAGE_RESTARTED");
        filter.addAction("android.intent.action.PACKAGE_FIRST_LAUNCH");
        filter.addDataScheme("package");
        this.mContext.registerReceiver(this.mReceiver, filter);
    }

    private void initPropelReceiver() {
        this.mContext.registerReceiver(this.mReceiver, new IntentFilter(PROPEL_HW_BASTET_CONFIG_ACTION));
    }

    private void initAlarmReceiver() {
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        if (this.mAlarmManager == null) {
            Log.e(TAG, "Failed to get alarm service");
            return;
        }
        this.mNrtPendingIntent = PendingIntent.getBroadcast(this.mContext, 0, new Intent(BASTET_ACTION_SEND_NRT, null), 0);
        IntentFilter filter = new IntentFilter();
        filter.addAction(BASTET_ACTION_SEND_NRT);
        this.mHeartBeatPendingIntent = PendingIntent.getBroadcast(this.mContext, 0, new Intent(BASTET_ACTION_SEND_HEARTBEAT, null), 0);
        filter.addAction(BASTET_ACTION_SEND_HEARTBEAT);
        this.mContext.registerReceiver(this.mAlarmReceiver, filter);
    }

    private void initDscpReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BASTET_DSCP_SET_ACTION);
        filter.addAction(BASTET_DSCP_UNSET_ACTION);
        this.mContext.registerReceiver(this.mReceiver, filter);
    }

    private void initTelephonyService() {
        this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        if (this.mTelephonyManager == null) {
            Log.e(TAG, "Failed to get telephony service");
        }
    }

    private void initBroadcastReceiver() {
        initGenericReceiver();
        initPackageReceiver();
        initPropelReceiver();
        initAlarmReceiver();
        initDscpReceiver();
    }

    private void handleNotifyNrt() {
        try {
            getBastetService();
            synchronized (this) {
                if (this.mIBastetManager != null) {
                    this.mIBastetManager.notifyNrtTimeout();
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        this.mAlarmManager.cancel(this.mNrtPendingIntent);
        if (this.mTelephonyManager != null) {
            this.mTelephonyManager.listen(this.mPhoneStateListener, 0);
        }
    }

    private void handleAlarmTimeout(int operation) {
        try {
            getBastetService();
            synchronized (this) {
                if (this.mIBastetManager != null) {
                    this.mIBastetManager.notifyAlarmTimeout(operation);
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void initHandlerThread() {
        this.mThread = new HandlerThread("HwBastetHandlerThread");
        this.mThread.start();
        this.mHandler = new HwBastetHandler(this.mThread.getLooper());
    }

    private boolean getBastetService() {
        synchronized (this) {
            if (this.mBastetService == null) {
                this.mBastetService = ServiceManager.getService(BASTET_SERVICE);
                if (this.mBastetService == null) {
                    Log.e(TAG, "Failed to get bastet service!");
                    return false;
                }
                try {
                    this.mBastetService.linkToDeath(this.mDeathRecipient, 0);
                    this.mIBastetManager = IBastetManager.Stub.asInterface(this.mBastetService);
                    if (this.mIBastetManager.initHwBastetService(this.mIBastetListener) != 0) {
                        Log.e(TAG, "initHwBastetService Failed!");
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    private boolean checkBastetService() {
        boolean isConnected = false;
        if (this.mConnectTimes != 0) {
            isConnected = getBastetService();
        }
        this.mConnectTimes++;
        if (!isConnected && this.mConnectTimes < 5) {
            Message timeoutMsg = this.mHandler.obtainMessage();
            timeoutMsg.what = 2;
            this.mHandler.sendMessageDelayed(timeoutMsg, 5000);
        } else if (isConnected) {
            this.mConnectTimes = 0;
        } else if (this.mConnectTimes >= 5) {
            Log.e(TAG, "checkBastetService failed");
        }
        return isConnected;
    }

    private static InputStream getInputStreamByUri(Context context, Uri uri) {
        InputStream in = null;
        try {
            return context.getContentResolver().openInputStream(uri);
        } catch (FileNotFoundException e) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Uri :(");
            stringBuilder.append(uri);
            stringBuilder.append(") not found");
            Log.w(str, stringBuilder.toString());
            return null;
        }
    }

    private static File getBastetFile(String path) {
        if (path == null || path.length() == 0) {
            return null;
        }
        File file = new File(path);
        if (file.exists()) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("File Path: ");
            stringBuilder.append(path);
            stringBuilder.append(" is already exists.");
            Log.i(str, stringBuilder.toString());
        } else {
            String str2;
            StringBuilder stringBuilder2;
            try {
                File bastetDir = file.getParentFile();
                if (bastetDir.exists() || bastetDir.mkdirs()) {
                    bastetDir.setExecutable(true, false);
                    bastetDir.setReadable(true, false);
                    if (file.createNewFile()) {
                        String str3 = TAG;
                        StringBuilder stringBuilder3 = new StringBuilder();
                        stringBuilder3.append("File Path: ");
                        stringBuilder3.append(path);
                        stringBuilder3.append(" is created!");
                        Log.i(str3, stringBuilder3.toString());
                        file.setExecutable(false);
                        file.setReadable(true, false);
                        file.setWritable(true);
                    } else {
                        str2 = TAG;
                        stringBuilder2 = new StringBuilder();
                        stringBuilder2.append("File Path: ");
                        stringBuilder2.append(path);
                        stringBuilder2.append(" is already exists.");
                        Log.i(str2, stringBuilder2.toString());
                    }
                } else {
                    str2 = TAG;
                    stringBuilder2 = new StringBuilder();
                    stringBuilder2.append("Failed to create file:");
                    stringBuilder2.append(bastetDir);
                    Log.e(str2, stringBuilder2.toString());
                    return null;
                }
            } catch (IOException e) {
                str2 = TAG;
                stringBuilder2 = new StringBuilder();
                stringBuilder2.append("Unable to prepare bastet file: ");
                stringBuilder2.append(file);
                Log.w(str2, stringBuilder2.toString(), e);
                e.printStackTrace();
                return null;
            }
        }
        return file;
    }

    private boolean copyBastetXml(String destPath, Uri srcUri) {
        boolean ret = false;
        InputStream in = getInputStreamByUri(this.mContext, srcUri);
        File file = getBastetFile(destPath);
        if (file == null || in == null) {
            Log.w(TAG, "Cannot update bastet xml, Bastet file or inputstream is null!");
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    Log.e(TAG, "close inputstream error");
                }
            }
            return false;
        }
        FileWriter fw = null;
        Reader input = null;
        try {
            fw = new FileWriter(file);
            input = new InputStreamReader(in, Charset.defaultCharset());
            char[] buffer = new char[1024];
            while (true) {
                int read = input.read(buffer);
                int length = read;
                if (read == -1) {
                    break;
                }
                fw.write(buffer, 0, length);
            }
            Log.i(TAG, "Copy file: /data/bastet/hw_bastet_partner.xml success.");
            ret = true;
            try {
                input.close();
            } catch (IOException e2) {
                Log.e(TAG, "close input error");
            }
            try {
                fw.close();
            } catch (IOException e3) {
                Log.e(TAG, "close fw error");
            }
        } catch (IOException e4) {
            Log.e(TAG, "Failed to copy file!", e4);
            e4.printStackTrace();
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e5) {
                    Log.e(TAG, "close input error");
                }
            }
            if (fw != null) {
                fw.close();
            }
        } catch (Throwable th) {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e6) {
                    Log.e(TAG, "close input error");
                }
            }
            if (fw != null) {
                try {
                    fw.close();
                } catch (IOException e7) {
                    Log.e(TAG, "close fw error");
                }
            }
        }
        return ret;
    }

    private boolean updateBastetConfig(int type, String strUri) {
        if (1 != type) {
            return false;
        }
        Uri uri = Uri.parse(strUri);
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Update Xml Uri :");
        stringBuilder.append(uri);
        Log.i(str, stringBuilder.toString());
        return copyBastetXml(HW_BASTET_PARTNER_PATH, uri);
    }

    private void doBroadcastMessage(Message msg) {
        int actionId = msg.arg1;
        String name = msg.obj;
        switch (actionId) {
            case 1:
            case 2:
            case 3:
            case 10:
            case 11:
                break;
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
                try {
                    getBastetService();
                    synchronized (this) {
                        if (this.mIBastetManager != null) {
                            this.mIBastetManager.packageChangedReceived(actionId, name);
                        }
                    }
                    return;
                } catch (RemoteException e) {
                    e.printStackTrace();
                    return;
                }
            case 9:
                if (!updateBastetConfig(msg.arg2, name)) {
                    return;
                }
                break;
            default:
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Unknown action id: ");
                stringBuilder.append(actionId);
                Log.e(str, stringBuilder.toString());
                return;
        }
        try {
            getBastetService();
            synchronized (this) {
                if (this.mIBastetManager != null) {
                    this.mIBastetManager.broadcastReceived(actionId);
                }
            }
        } catch (RemoteException e2) {
            e2.printStackTrace();
        }
    }

    private void parseAccAppList(Message msg) {
        String appString = msg.getData().getString("applist");
        if (appString == null) {
            Log.e(TAG, "parseAccAPPList the AppList is NULL.");
            return;
        }
        String[] appList = appString.split(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
        int n = appList.length;
        for (int i = 0; i < n; i++) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("parseAccAPPList AppList[");
            stringBuilder.append(i);
            stringBuilder.append("] is ");
            stringBuilder.append(appList[i]);
            Log.d(str, stringBuilder.toString());
            synchronized (this) {
                this.mAccAppList.add(appList[i]);
            }
        }
    }

    private void addHrtApp(int uid) {
        synchronized (this) {
            boolean found = false;
            int len = this.mHrtAppList.size();
            for (int i = 0; i < len; i++) {
                if (uid == ((Integer) this.mHrtAppList.get(i)).intValue()) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("add hrt uid: ");
                stringBuilder.append(uid);
                Log.e(str, stringBuilder.toString());
                this.mHrtAppList.add(Integer.valueOf(uid));
                if (uid == this.mForegroundUid) {
                    str = TAG;
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("foreground uid: ");
                    stringBuilder.append(uid);
                    Log.i(str, stringBuilder.toString());
                    setAppActivity(0, uid);
                }
            }
        }
    }

    private void removeHrtApp(int uid) {
        synchronized (this) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("uid: ");
            stringBuilder.append(uid);
            Log.e(str, stringBuilder.toString());
            int len = this.mHrtAppList.size();
            for (int i = 0; i < len; i++) {
                if (uid == ((Integer) this.mHrtAppList.get(i)).intValue()) {
                    String str2 = TAG;
                    StringBuilder stringBuilder2 = new StringBuilder();
                    stringBuilder2.append("remove hrt uid: ");
                    stringBuilder2.append(uid);
                    Log.i(str2, stringBuilder2.toString());
                    this.mHrtAppList.remove(i);
                    break;
                }
            }
        }
    }

    private void removeHrtApp(String name) {
        synchronized (this) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("name: ");
            stringBuilder.append(name);
            Log.e(str, stringBuilder.toString());
            int len = this.mHrtAppList.size();
            int i = 0;
            try {
                int uid = this.mContext.getPackageManager().getPackageUid(name, 0);
                String str2 = TAG;
                StringBuilder stringBuilder2 = new StringBuilder();
                stringBuilder2.append("len: ");
                stringBuilder2.append(len);
                stringBuilder2.append(", name: ");
                stringBuilder2.append(name);
                stringBuilder2.append(", uid: ");
                stringBuilder2.append(uid);
                Log.e(str2, stringBuilder2.toString());
                while (i < len) {
                    if (uid == ((Integer) this.mHrtAppList.get(i)).intValue()) {
                        str2 = TAG;
                        stringBuilder2 = new StringBuilder();
                        stringBuilder2.append("remove hrt uid: ");
                        stringBuilder2.append(uid);
                        Log.i(str2, stringBuilder2.toString());
                        this.mHrtAppList.remove(i);
                        break;
                    }
                    i++;
                }
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean filterHrtApp(String msg) {
        String[] splits = msg.split("\t");
        String pkgName = null;
        boolean found = false;
        try {
            if (splits.length > 0) {
                pkgName = splits[0];
            }
        } catch (Exception e) {
        }
        try {
            int uid = this.mContext.getPackageManager().getPackageUid(pkgName, 0);
            synchronized (this) {
                this.mForegroundUid = uid;
                int len = this.mHrtAppList.size();
                if (len > 0) {
                    for (int i = 0; i < len; i++) {
                        if (uid == ((Integer) this.mHrtAppList.get(i)).intValue()) {
                            String str = TAG;
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append("foreground uid: ");
                            stringBuilder.append(uid);
                            Log.i(str, stringBuilder.toString());
                            setAppActivity(0, uid);
                            found = true;
                            break;
                        }
                    }
                    if (!found && this.mLastHrtUid > 0) {
                        String str2 = TAG;
                        StringBuilder stringBuilder2 = new StringBuilder();
                        stringBuilder2.append("background uid: ");
                        stringBuilder2.append(this.mLastHrtUid);
                        Log.i(str2, stringBuilder2.toString());
                        setAppActivity(1, this.mLastHrtUid);
                    }
                }
            }
        } catch (NameNotFoundException e2) {
            e2.printStackTrace();
        }
        return found;
    }

    private void filterAccApp(String msg, boolean isFound) {
        if (msg != null) {
            String[] splits = msg.split("\t");
            String pkgName = null;
            if (splits.length > 0) {
                pkgName = splits[0];
            }
            try {
                int uid = this.mContext.getPackageManager().getPackageUid(pkgName, 0);
                synchronized (this) {
                    boolean found = false;
                    int len = this.mAccAppList.size();
                    if (len > 0) {
                        for (int i = 0; i < len; i++) {
                            if (((String) this.mAccAppList.get(i)).equals(pkgName)) {
                                String str = TAG;
                                StringBuilder stringBuilder = new StringBuilder();
                                stringBuilder.append("filterAccApp foreground uid: ");
                                stringBuilder.append(uid);
                                Log.i(str, stringBuilder.toString());
                                setAppActivity(0, uid);
                                this.mLastAccUid = uid;
                                found = true;
                                break;
                            }
                        }
                        if (!(found || isFound || this.mLastAccUid <= 0)) {
                            String str2 = TAG;
                            StringBuilder stringBuilder2 = new StringBuilder();
                            stringBuilder2.append("filterAccApp background uid: ");
                            stringBuilder2.append(this.mLastAccUid);
                            Log.i(str2, stringBuilder2.toString());
                            setAppActivity(1, this.mLastAccUid);
                            this.mLastAccUid = -1;
                        }
                    }
                }
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void setAppActivity(int activity, int uid) {
        try {
            getBastetService();
            synchronized (this) {
                if (this.mIBastetManager != null) {
                    this.mIBastetManager.hrtAppActivity(activity, uid);
                }
            }
            if (activity == 0) {
                this.mLastHrtUid = uid;
            } else if (activity == 1) {
                this.mLastHrtUid = -1;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void setAppBackground() {
        synchronized (this) {
            if (this.mLastHrtUid > 0) {
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("background uid: ");
                stringBuilder.append(this.mLastHrtUid);
                Log.i(str, stringBuilder.toString());
                setAppActivity(1, this.mLastHrtUid);
            }
        }
    }

    private void initPgPlugThread() {
        this.mPGPlug = new PGPlug(this.mPgEventProcesser, TAG);
        new Thread(this.mPGPlug, TAG).start();
    }
}
