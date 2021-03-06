package com.android.server;

import android.app.ActivityManagerNative;
import android.app.AppGlobals;
import android.app.AppOpsManager;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.IPackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.IBinder;
import android.os.IInterface;
import android.os.LocaleList;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.text.style.SuggestionSpan;
import android.util.EventLog;
import android.util.Log;
import android.util.LruCache;
import android.util.Pair;
import android.util.PrintWriterPrinter;
import android.util.Printer;
import android.util.Slog;
import android.view.IWindowManager;
import android.view.InputChannel;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputBinding;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodSubtype;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.content.PackageMonitor;
import com.android.internal.inputmethod.IInputContentUriToken;
import com.android.internal.inputmethod.InputMethodUtils;
import com.android.internal.inputmethod.InputMethodUtils.InputMethodSettings;
import com.android.internal.os.HandlerCaller;
import com.android.internal.os.SomeArgs;
import com.android.internal.view.IInputContext;
import com.android.internal.view.IInputMethod;
import com.android.internal.view.IInputMethodClient;
import com.android.internal.view.IInputMethodSession;
import com.android.internal.view.IInputSessionCallback;
import com.android.internal.view.IInputSessionCallback.Stub;
import com.android.internal.view.InputBindResult;
import com.android.internal.view.InputMethodClient;
import com.android.server.hidata.arbitration.HwArbitrationDEFS;
import com.android.server.security.trustcircle.tlv.command.query.DATA_TCIS_ERROR_STEP;
import com.android.server.statusbar.StatusBarManagerService;
import com.android.server.wm.WindowManagerInternal;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class HwSecureInputMethodManagerService extends AbsInputMethodManagerService implements ServiceConnection, Callback {
    static final boolean DEBUG = false;
    static final boolean DEBUG_FLOW = Log.HWINFO;
    static final boolean DEBUG_RESTORE = false;
    static final int MSG_ATTACH_TOKEN = 1040;
    static final int MSG_BIND_CLIENT = 3010;
    static final int MSG_BIND_INPUT = 1010;
    static final int MSG_CREATE_SESSION = 1050;
    static final int MSG_HIDE_SOFT_INPUT = 1030;
    static final int MSG_REPORT_FULLSCREEN_MODE = 3045;
    static final int MSG_SET_ACTIVE = 3020;
    static final int MSG_SET_INTERACTIVE = 3030;
    static final int MSG_SET_USER_ACTION_NOTIFICATION_SEQUENCE_NUMBER = 3040;
    static final int MSG_SHOW_SOFT_INPUT = 1020;
    static final int MSG_START_INPUT = 2000;
    static final int MSG_UNBIND_CLIENT = 3000;
    static final int MSG_UNBIND_INPUT = 1000;
    private static final int NOT_A_SUBTYPE_ID = -1;
    static final int SECURE_SUGGESTION_SPANS_MAX_SIZE = 20;
    public static final String SETTINGS_SECURE_KEYBOARD_CONTROL = "secure_keyboard";
    static final String TAG = "SecInputMethodManagerService";
    static final long TIME_TO_RECONNECT = 3000;
    static final int UNBIND_SECIME_IF_SHOULD = 10000;
    private static final String secImeId = "com.huawei.secime/.SoftKeyboard";
    private final AppOpsManager mAppOpsManager;
    int mBackDisposition = 0;
    boolean mBoundToMethod;
    final HandlerCaller mCaller;
    final HashMap<IBinder, ClientState> mClients = new HashMap();
    final Context mContext;
    EditorInfo mCurAttribute;
    ClientState mCurClient;
    private boolean mCurClientInKeyguard;
    IBinder mCurFocusedWindow;
    ClientState mCurFocusedWindowClient;
    int mCurFocusedWindowSoftInputMode;
    String mCurId;
    IInputContext mCurInputContext;
    int mCurInputContextMissingMethods;
    Intent mCurIntent;
    IInputMethod mCurMethod;
    String mCurMethodId;
    int mCurSeq;
    IBinder mCurToken;
    int mCurUserActionNotificationSequenceNumber = 0;
    SessionState mEnabledSession;
    boolean mHaveConnection;
    private final IPackageManager mIPackageManager = AppGlobals.getPackageManager();
    final IWindowManager mIWindowManager;
    int mImeWindowVis;
    boolean mInFullscreenMode;
    boolean mInputShown;
    boolean mIsInteractive = true;
    private KeyguardManager mKeyguardManager;
    long mLastBindTime;
    private LocaleList mLastSystemLocales;
    final ArrayList<InputMethodInfo> mMethodList = new ArrayList();
    final HashMap<String, InputMethodInfo> mMethodMap = new HashMap();
    private final MyPackageMonitor mMyPackageMonitor = new MyPackageMonitor();
    final InputBindResult mNoBinding = new InputBindResult(-1, null, null, null, -1, -1);
    final Resources mRes;
    private SecureSettingsObserver mSecureSettingsObserver;
    private final LruCache<SuggestionSpan, InputMethodInfo> mSecureSuggestionSpans = new LruCache(20);
    final InputMethodSettings mSettings;
    private final HashMap<InputMethodInfo, ArrayList<InputMethodSubtype>> mShortcutInputMethodsAndSubtypes = new HashMap();
    private boolean mShouldSetActive;
    boolean mShowExplicitlyRequested;
    boolean mShowForced;
    boolean mShowRequested;
    private final String mSlotIme;
    @GuardedBy("mMethodMap")
    private final WeakHashMap<IBinder, StartInputInfo> mStartInputMap = new WeakHashMap();
    private StatusBarManagerService mStatusBar;
    boolean mSystemReady = false;
    private int mUnbindCounter = 0;
    private final UserManager mUserManager;
    boolean mVisibleBound = false;
    final ServiceConnection mVisibleConnection = new MySerServiceConnection();
    final WindowManagerInternal mWindowManagerInternal;

    static final class ClientState {
        final InputBinding binding = new InputBinding(null, this.inputContext.asBinder(), this.uid, this.pid);
        final IInputMethodClient client;
        SessionState curSession;
        final IInputContext inputContext;
        final int pid;
        boolean sessionRequested;
        final int uid;

        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("ClientState{");
            stringBuilder.append(Integer.toHexString(System.identityHashCode(this)));
            stringBuilder.append(" uid ");
            stringBuilder.append(this.uid);
            stringBuilder.append(" pid ");
            stringBuilder.append(this.pid);
            stringBuilder.append("}");
            return stringBuilder.toString();
        }

        ClientState(IInputMethodClient _client, IInputContext _inputContext, int _uid, int _pid) {
            this.client = _client;
            this.inputContext = _inputContext;
            this.uid = _uid;
            this.pid = _pid;
        }
    }

    class ImmsBroadcastReceiver extends BroadcastReceiver {
        ImmsBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.intent.action.USER_ADDED".equals(action) || "android.intent.action.USER_REMOVED".equals(action)) {
                HwSecureInputMethodManagerService.this.updateCurrentProfileIds();
                return;
            }
            String str = HwSecureInputMethodManagerService.TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Unexpected intent ");
            stringBuilder.append(intent);
            Slog.w(str, stringBuilder.toString());
        }
    }

    private static final class MethodCallback extends Stub {
        private final InputChannel mChannel;
        private final IInputMethod mMethod;
        private final HwSecureInputMethodManagerService mParentIMMS;

        MethodCallback(HwSecureInputMethodManagerService imms, IInputMethod method, InputChannel channel) {
            this.mParentIMMS = imms;
            this.mMethod = method;
            this.mChannel = channel;
        }

        public void sessionCreated(IInputMethodSession session) {
            long ident = Binder.clearCallingIdentity();
            try {
                this.mParentIMMS.onSessionCreated(this.mMethod, session, this.mChannel);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    public static final class MyLifecycle extends SystemService {
        private HwSecureInputMethodManagerService mService;

        public MyLifecycle(Context context) {
            super(context);
            this.mService = new HwSecureInputMethodManagerService(context);
        }

        public void onStart() {
            publishBinderService(HwInputMethodManagerService.SECURITY_INPUT_SERVICE_NAME, this.mService);
        }

        public void onSwitchUser(int userHandle) {
            this.mService.onSwitchUser(userHandle);
        }

        public void onBootPhase(int phase) {
            if (phase == 550) {
                this.mService.systemRunning((StatusBarManagerService) ServiceManager.getService("statusbar"));
            }
        }

        public void onUnlockUser(int userHandle) {
            this.mService.onUnlockUser(userHandle);
        }
    }

    class MyPackageMonitor extends PackageMonitor {
        MyPackageMonitor() {
        }

        private boolean isChangingPackagesOfCurrentUser() {
            return getChangingUserId() == HwSecureInputMethodManagerService.this.mSettings.getCurrentUserId();
        }

        public boolean onHandleForceStop(Intent intent, String[] packages, int uid, boolean doit) {
            return false;
        }

        public void onSomePackagesChanged() {
            if (isChangingPackagesOfCurrentUser()) {
                synchronized (HwSecureInputMethodManagerService.this.mMethodMap) {
                    HwSecureInputMethodManagerService.this.buildInputMethodListLocked(false);
                }
            }
        }

        public boolean onPackageChanged(String packageName, int uid, String[] components) {
            onSomePackagesChanged();
            if (components != null) {
                for (String name : components) {
                    if (packageName.equals(name)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    static class MySerServiceConnection implements ServiceConnection {
        MySerServiceConnection() {
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
        }

        public void onServiceDisconnected(ComponentName name) {
        }
    }

    private class SecureSettingsObserver extends ContentObserver {
        boolean mRegistered = false;
        int mUserId;

        public SecureSettingsObserver() {
            super(new Handler());
        }

        public void registerContentObserverInner(int userId) {
            String str = HwSecureInputMethodManagerService.TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("SecureSettingsObserver mRegistered=");
            stringBuilder.append(this.mRegistered);
            stringBuilder.append(" new user=");
            stringBuilder.append(userId);
            stringBuilder.append(" current user=");
            stringBuilder.append(this.mUserId);
            Slog.d(str, stringBuilder.toString());
            if (!this.mRegistered || this.mUserId != userId) {
                ContentResolver resolver = HwSecureInputMethodManagerService.this.mContext.getContentResolver();
                if (this.mRegistered) {
                    resolver.unregisterContentObserver(this);
                    this.mRegistered = false;
                }
                this.mUserId = userId;
                resolver.registerContentObserver(Secure.getUriFor("secure_keyboard"), false, this, userId);
                this.mRegistered = true;
            }
        }

        public void onChange(boolean selfChange, Uri uri) {
            if (uri != null) {
                String str = HwSecureInputMethodManagerService.TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("SecureSettingsObserver onChange, uri = ");
                stringBuilder.append(uri.toString());
                Slog.i(str, stringBuilder.toString());
                if (Secure.getUriFor("secure_keyboard").equals(uri) && !HwSecureInputMethodManagerService.this.isSecureIMEEnable()) {
                    synchronized (HwSecureInputMethodManagerService.this.mMethodMap) {
                        HwSecureInputMethodManagerService.this.hideCurrentInputLocked(0, null);
                        HwSecureInputMethodManagerService.this.mCurMethodId = null;
                        HwSecureInputMethodManagerService.this.unbindCurrentMethodLocked(false);
                    }
                }
            }
        }
    }

    static class SessionState {
        InputChannel channel;
        final ClientState client;
        final IInputMethod method;
        IInputMethodSession session;

        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("SessionState{uid ");
            stringBuilder.append(this.client.uid);
            stringBuilder.append(" pid ");
            stringBuilder.append(this.client.pid);
            stringBuilder.append(" method ");
            stringBuilder.append(Integer.toHexString(System.identityHashCode(this.method)));
            stringBuilder.append(" session ");
            stringBuilder.append(Integer.toHexString(System.identityHashCode(this.session)));
            stringBuilder.append(" channel ");
            stringBuilder.append(this.channel);
            stringBuilder.append("}");
            return stringBuilder.toString();
        }

        SessionState(ClientState _client, IInputMethod _method, IInputMethodSession _session, InputChannel _channel) {
            this.client = _client;
            this.method = _method;
            this.session = _session;
            this.channel = _channel;
        }
    }

    private static class StartInputInfo {
        private static final AtomicInteger sSequenceNumber = new AtomicInteger(0);
        final int mClientBindSequenceNumber;
        final EditorInfo mEditorInfo;
        final String mImeId;
        final IBinder mImeToken;
        final boolean mRestarting;
        final int mSequenceNumber = sSequenceNumber.getAndIncrement();
        final int mStartInputReason;
        final IBinder mTargetWindow;
        final int mTargetWindowSoftInputMode;
        final long mTimestamp = SystemClock.uptimeMillis();
        final long mWallTime = System.currentTimeMillis();

        StartInputInfo(IBinder imeToken, String imeId, int startInputReason, boolean restarting, IBinder targetWindow, EditorInfo editorInfo, int targetWindowSoftInputMode, int clientBindSequenceNumber) {
            this.mImeToken = imeToken;
            this.mImeId = imeId;
            this.mStartInputReason = startInputReason;
            this.mRestarting = restarting;
            this.mTargetWindow = targetWindow;
            this.mEditorInfo = editorInfo;
            this.mTargetWindowSoftInputMode = targetWindowSoftInputMode;
            this.mClientBindSequenceNumber = clientBindSequenceNumber;
        }
    }

    private final class LocalSecureServiceImpl implements HwSecureInputMethodManagerInternal {
        private LocalSecureServiceImpl() {
        }

        /* synthetic */ LocalSecureServiceImpl(HwSecureInputMethodManagerService x0, AnonymousClass1 x1) {
            this();
        }

        public void setClientActiveFlag() {
            HwSecureInputMethodManagerService.this.mShouldSetActive = true;
        }
    }

    protected boolean isSecureIME(String packageName) {
        return HwInputMethodManagerService.SECURE_IME_PACKAGENAME.equals(packageName);
    }

    protected boolean shouldBuildInputMethodList(String packageName) {
        return HwInputMethodManagerService.SECURE_IME_PACKAGENAME.equals(packageName);
    }

    private boolean isSecureIMEEnable() {
        return Secure.getIntForUser(this.mContext.getContentResolver(), "secure_keyboard", 1, this.mSettings.getCurrentUserId()) == 1;
    }

    /* JADX WARNING: Missing block: B:15:0x002e, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean setClientActiveIfShould() {
        if (!this.mShouldSetActive || !this.mIsInteractive) {
            return false;
        }
        synchronized (this.mMethodMap) {
            if (this.mCurClient == null || this.mCurClient.client == null) {
            } else {
                executeOrSendMessage(this.mCurClient.client, this.mCaller.obtainMessageIO(MSG_SET_ACTIVE, 1, this.mCurClient));
                this.mShouldSetActive = false;
                return true;
            }
        }
    }

    protected void switchUserExtra(int userId) {
        if (this.mSecureSettingsObserver != null) {
            this.mSecureSettingsObserver.registerContentObserverInner(userId);
        }
        if (!isSecureIMEEnable()) {
            hideCurrentInputLocked(0, null);
            this.mCurMethodId = null;
            unbindCurrentMethodLocked(false);
        }
    }

    void onUnlockUser(int userId) {
        synchronized (this.mMethodMap) {
            int currentUserId = this.mSettings.getCurrentUserId();
            if (userId != currentUserId) {
                return;
            }
            this.mSettings.switchCurrentUser(currentUserId, this.mSystemReady ^ 1);
            buildInputMethodListLocked(false);
        }
    }

    void onSwitchUser(int userId) {
        synchronized (this.mMethodMap) {
            switchUserLocked(userId);
        }
    }

    public HwSecureInputMethodManagerService(Context context) {
        Context context2 = context;
        this.mContext = context2;
        this.mRes = context.getResources();
        this.mIWindowManager = IWindowManager.Stub.asInterface(ServiceManager.getService("window"));
        this.mWindowManagerInternal = (WindowManagerInternal) LocalServices.getService(WindowManagerInternal.class);
        this.mCaller = new HandlerCaller(context2, null, new HandlerCaller.Callback() {
            public void executeMessage(Message msg) {
                HwSecureInputMethodManagerService.this.handleMessage(msg);
            }
        }, true);
        this.mAppOpsManager = (AppOpsManager) this.mContext.getSystemService(AppOpsManager.class);
        this.mUserManager = (UserManager) this.mContext.getSystemService(UserManager.class);
        this.mSlotIme = this.mContext.getString(17041186);
        new Bundle().putBoolean("android.allowDuringSetup", true);
        IntentFilter broadcastFilter = new IntentFilter();
        broadcastFilter.addAction("android.intent.action.USER_ADDED");
        broadcastFilter.addAction("android.intent.action.USER_REMOVED");
        this.mContext.registerReceiver(new ImmsBroadcastReceiver(), broadcastFilter);
        int userId = 0;
        try {
            userId = ActivityManagerNative.getDefault().getCurrentUser().id;
        } catch (RemoteException e) {
            Slog.w(TAG, "Couldn't get current user ID; guessing it's 0", e);
        }
        this.mMyPackageMonitor.register(this.mContext, null, UserHandle.ALL, true);
        this.mSettings = new InputMethodSettings(this.mRes, context.getContentResolver(), this.mMethodMap, this.mMethodList, userId, this.mSystemReady ^ 1);
        updateCurrentProfileIds();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.LOCALE_CHANGED");
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                synchronized (HwSecureInputMethodManagerService.this.mMethodMap) {
                    HwSecureInputMethodManagerService.this.resetStateIfCurrentLocaleChangedLocked();
                }
            }
        }, filter);
    }

    private void resetAllInternalStateLocked(boolean updateOnlyWhenLocaleChanged, boolean resetDefaultEnabledIme) {
        if (this.mSystemReady) {
            LocaleList newLocales = this.mRes.getConfiguration().getLocales();
            if (!(updateOnlyWhenLocaleChanged && (newLocales == null || newLocales.equals(this.mLastSystemLocales)))) {
                if (!updateOnlyWhenLocaleChanged) {
                    hideCurrentInputLocked(0, null);
                    resetCurrentMethodAndClient(6);
                }
                buildInputMethodListLocked(resetDefaultEnabledIme);
                this.mLastSystemLocales = newLocales;
            }
        }
    }

    private void resetStateIfCurrentLocaleChangedLocked() {
        resetAllInternalStateLocked(true, true);
    }

    private void switchUserLocked(int newUserId) {
        boolean useCopyOnWriteSettings = (this.mSystemReady && this.mUserManager.isUserUnlockingOrUnlocked(newUserId)) ? false : true;
        this.mSettings.switchCurrentUser(newUserId, useCopyOnWriteSettings);
        updateCurrentProfileIds();
        resetAllInternalStateLocked(false, false);
        switchUserExtra(newUserId);
    }

    void updateCurrentProfileIds() {
        this.mSettings.setCurrentProfileIds(this.mUserManager.getProfileIdsWithDisabled(this.mSettings.getCurrentUserId()));
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        try {
            return super.onTransact(code, data, reply, flags);
        } catch (RuntimeException e) {
            if (!(e instanceof SecurityException)) {
                Slog.wtf(TAG, "Input Method Manager Crash", e);
            }
            throw e;
        }
    }

    public void systemRunning(StatusBarManagerService statusBar) {
        this.mSecureSettingsObserver = new SecureSettingsObserver();
        this.mSecureSettingsObserver.registerContentObserverInner(this.mSettings.getCurrentUserId());
        if (LocalServices.getService(HwSecureInputMethodManagerInternal.class) == null) {
            LocalServices.addService(HwSecureInputMethodManagerInternal.class, new LocalSecureServiceImpl(this, null));
        }
        synchronized (this.mMethodMap) {
            if (!this.mSystemReady) {
                this.mSystemReady = true;
                int currentUserId = this.mSettings.getCurrentUserId();
                this.mSettings.switchCurrentUser(currentUserId, this.mUserManager.isUserUnlockingOrUnlocked(currentUserId) ^ true);
                this.mKeyguardManager = (KeyguardManager) this.mContext.getSystemService(KeyguardManager.class);
                this.mStatusBar = statusBar;
                if (this.mStatusBar != null) {
                    this.mStatusBar.setIconVisibility(this.mSlotIme, false);
                }
                updateSystemUiLocked(this.mCurToken, this.mImeWindowVis, this.mBackDisposition);
                buildInputMethodListLocked(true);
                this.mLastSystemLocales = this.mRes.getConfiguration().getLocales();
            }
        }
    }

    /* JADX WARNING: Missing block: B:9:0x003f, code:
            return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean calledFromValidUser() {
        int uid = Binder.getCallingUid();
        int userId = UserHandle.getUserId(uid);
        if (uid == 1000 || this.mSettings.isCurrentProfile(userId) || this.mContext.checkCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS_FULL") == 0) {
            return true;
        }
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("--- IPC called from background users. Ignore. callers=");
        stringBuilder.append(Debug.getCallers(10));
        Slog.w(str, stringBuilder.toString());
        return false;
    }

    private boolean calledWithValidToken(IBinder token) {
        if (token == null || this.mCurToken != token) {
            return false;
        }
        return true;
    }

    private boolean bindCurrentInputMethodService(Intent service, ServiceConnection conn, int flags) {
        if (service != null && conn != null) {
            return this.mContext.bindServiceAsUser(service, conn, flags, new UserHandle(this.mSettings.getCurrentUserId()));
        }
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("--- bind failed: service = ");
        stringBuilder.append(service);
        stringBuilder.append(", conn = ");
        stringBuilder.append(conn);
        Slog.e(str, stringBuilder.toString());
        return false;
    }

    public List<InputMethodInfo> getInputMethodList() {
        if (!calledFromValidUser()) {
            return Collections.emptyList();
        }
        List arrayList;
        synchronized (this.mMethodMap) {
            arrayList = new ArrayList(this.mMethodList);
        }
        return arrayList;
    }

    public List<InputMethodInfo> getVrInputMethodList() {
        return getInputMethodList(true);
    }

    private List<InputMethodInfo> getInputMethodList(boolean isVrOnly) {
        if (!calledFromValidUser()) {
            return Collections.emptyList();
        }
        ArrayList<InputMethodInfo> methodList;
        synchronized (this.mMethodMap) {
            methodList = new ArrayList();
            Iterator it = this.mMethodList.iterator();
            while (it.hasNext()) {
                InputMethodInfo info = (InputMethodInfo) it.next();
                if (info.isVrOnly() == isVrOnly) {
                    methodList.add(info);
                }
            }
        }
        return methodList;
    }

    public List<InputMethodInfo> getEnabledInputMethodList() {
        return Collections.emptyList();
    }

    public List<InputMethodSubtype> getEnabledInputMethodSubtypeList(String imiId, boolean allowsImplicitlySelectedSubtypes) {
        return Collections.emptyList();
    }

    public void addClient(IInputMethodClient client, IInputContext inputContext, int uid, int pid) {
        if (calledFromValidUser()) {
            synchronized (this.mMethodMap) {
                this.mClients.put(client.asBinder(), new ClientState(client, inputContext, uid, pid));
            }
        }
    }

    public void removeClient(IInputMethodClient client) {
        if (calledFromValidUser()) {
            synchronized (this.mMethodMap) {
                ClientState cs = (ClientState) this.mClients.remove(client.asBinder());
                if (cs != null) {
                    clearClientSessionLocked(cs);
                    if (this.mCurClient == cs) {
                        this.mCurClient = null;
                    }
                    if (this.mCurFocusedWindowClient == cs) {
                        this.mCurFocusedWindowClient = null;
                    }
                }
            }
        }
    }

    void executeOrSendMessage(IInterface target, Message msg) {
        if (target != null) {
            if (target.asBinder() instanceof Binder) {
                this.mCaller.sendMessage(msg);
            } else {
                handleMessage(msg);
                msg.recycle();
            }
        }
    }

    void unbindCurrentClientLocked(int unbindClientReason) {
        if (this.mCurClient != null) {
            if (this.mBoundToMethod) {
                this.mBoundToMethod = false;
                if (this.mCurMethod != null) {
                    executeOrSendMessage(this.mCurMethod, this.mCaller.obtainMessageO(1000, this.mCurMethod));
                }
            }
            executeOrSendMessage(this.mCurClient.client, this.mCaller.obtainMessageIO(MSG_SET_ACTIVE, 0, this.mCurClient));
            executeOrSendMessage(this.mCurClient.client, this.mCaller.obtainMessageIIO(3000, this.mCurSeq, unbindClientReason, this.mCurClient.client));
            this.mCurClient.sessionRequested = false;
            this.mCurClient = null;
        }
    }

    private int getImeShowFlags() {
        if (this.mShowForced) {
            return 0 | 3;
        }
        if (this.mShowExplicitlyRequested) {
            return 0 | 1;
        }
        return 0;
    }

    private int getAppShowFlags() {
        if (this.mShowForced) {
            return 0 | 2;
        }
        if (this.mShowExplicitlyRequested) {
            return 0;
        }
        return 0 | 1;
    }

    InputBindResult attachNewInputLocked(int startInputReason, boolean initial) {
        if (!this.mBoundToMethod) {
            executeOrSendMessage(this.mCurMethod, this.mCaller.obtainMessageOO(1010, this.mCurMethod, this.mCurClient.binding));
            this.mBoundToMethod = true;
        }
        SessionState session = this.mCurClient.curSession;
        Binder startInputToken = new Binder();
        this.mStartInputMap.put(startInputToken, new StartInputInfo(this.mCurToken, this.mCurId, startInputReason, initial ^ 1, this.mCurFocusedWindow, this.mCurAttribute, this.mCurFocusedWindowSoftInputMode, this.mCurSeq));
        if (session != null) {
            executeOrSendMessage(session.method, this.mCaller.obtainMessageIIOOOO(2000, this.mCurInputContextMissingMethods, initial ^ 1, startInputToken, session, this.mCurInputContext, this.mCurAttribute));
        }
        InputChannel inputChannel = null;
        if (this.mShowRequested) {
            if (DEBUG_FLOW) {
                Slog.v(TAG, "Attach new input asks to show input");
            }
            showCurrentInputLocked(getAppShowFlags(), null);
        }
        if (session == null) {
            return null;
        }
        IInputMethodSession iInputMethodSession = session.session;
        if (session.channel != null) {
            inputChannel = session.channel.dup();
        }
        return new InputBindResult(0, iInputMethodSession, inputChannel, this.mCurId, this.mCurSeq, this.mCurUserActionNotificationSequenceNumber);
    }

    InputBindResult startInputLocked(int startInputReason, IInputMethodClient client, IInputContext inputContext, int missingMethods, EditorInfo attribute, int controlFlags) {
        if (this.mCurMethodId == null) {
            return this.mNoBinding;
        }
        ClientState cs = (ClientState) this.mClients.get(client.asBinder());
        String str;
        StringBuilder stringBuilder;
        if (cs == null) {
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("unknown client ");
            stringBuilder2.append(client.asBinder());
            throw new IllegalArgumentException(stringBuilder2.toString());
        } else if (attribute == null) {
            str = TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("Ignoring startInput with null EditorInfo. uid=");
            stringBuilder.append(cs.uid);
            stringBuilder.append(" pid=");
            stringBuilder.append(cs.pid);
            Slog.w(str, stringBuilder.toString());
            return null;
        } else {
            try {
                if (!this.mIWindowManager.inputMethodClientHasFocus(cs.client)) {
                    str = TAG;
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("Starting input on non-focused client ");
                    stringBuilder.append(cs.client);
                    stringBuilder.append(" (uid=");
                    stringBuilder.append(cs.uid);
                    stringBuilder.append(" pid=");
                    stringBuilder.append(cs.pid);
                    stringBuilder.append(")");
                    Slog.w(str, stringBuilder.toString());
                    return null;
                }
            } catch (RemoteException e) {
            }
            return startInputUncheckedLocked(cs, inputContext, missingMethods, attribute, controlFlags, startInputReason);
        }
    }

    InputBindResult startInputUncheckedLocked(ClientState cs, IInputContext inputContext, int missingMethods, EditorInfo attribute, int controlFlags, int startInputReason) {
        ClientState clientState = cs;
        EditorInfo editorInfo = attribute;
        int i = controlFlags;
        if (this.mCurMethodId == null) {
            return this.mNoBinding;
        }
        if (InputMethodUtils.checkIfPackageBelongsToUid(this.mAppOpsManager, clientState.uid, editorInfo.packageName)) {
            if (this.mCurClient != clientState) {
                this.mCurClientInKeyguard = isKeyguardLocked();
                unbindCurrentClientLocked(1);
                if (this.mIsInteractive) {
                    executeOrSendMessage(clientState.client, this.mCaller.obtainMessageIO(MSG_SET_ACTIVE, this.mIsInteractive, clientState));
                }
            }
            this.mCurSeq++;
            if (this.mCurSeq <= 0) {
                this.mCurSeq = 1;
            }
            this.mCurClient = clientState;
            this.mCurInputContext = inputContext;
            this.mCurInputContextMissingMethods = missingMethods;
            this.mCurAttribute = editorInfo;
            if ((65536 & i) != 0) {
                this.mShowRequested = true;
            }
            int i2;
            if (this.mCurId == null || !this.mCurId.equals(this.mCurMethodId)) {
                i2 = startInputReason;
            } else {
                boolean z = false;
                if (clientState.curSession != null) {
                    if ((i & 256) != 0) {
                        z = true;
                    }
                    return attachNewInputLocked(startInputReason, z);
                }
                i2 = startInputReason;
                if (this.mHaveConnection) {
                    if (this.mCurMethod != null) {
                        requestClientSessionLocked(cs);
                        return new InputBindResult(1, null, null, this.mCurId, this.mCurSeq, this.mCurUserActionNotificationSequenceNumber);
                    } else if (SystemClock.uptimeMillis() < this.mLastBindTime + 3000) {
                        return new InputBindResult(2, null, null, this.mCurId, this.mCurSeq, this.mCurUserActionNotificationSequenceNumber);
                    } else {
                        EventLog.writeEvent(32000, new Object[]{this.mCurMethodId, Long.valueOf(SystemClock.uptimeMillis() - this.mLastBindTime), Integer.valueOf(0)});
                    }
                }
            }
            try {
                return startInputInnerLocked();
            } catch (RuntimeException e) {
                RuntimeException runtimeException = e;
                Slog.w(TAG, "Unexpected exception", e);
                return null;
            }
        }
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Rejecting this client as it reported an invalid package name. uid=");
        stringBuilder.append(clientState.uid);
        stringBuilder.append(" package=");
        stringBuilder.append(editorInfo.packageName);
        Slog.e(str, stringBuilder.toString());
        return this.mNoBinding;
    }

    InputBindResult startInputInnerLocked() {
        if (this.mCurMethodId == null || !this.mMethodMap.containsKey(this.mCurMethodId)) {
            return this.mNoBinding;
        }
        if (!this.mSystemReady) {
            return new InputBindResult(7, null, null, this.mCurMethodId, this.mCurSeq, this.mCurUserActionNotificationSequenceNumber);
        }
        InputMethodInfo info = (InputMethodInfo) this.mMethodMap.get(this.mCurMethodId);
        String str;
        StringBuilder stringBuilder;
        if (info == null) {
            str = TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("info == null id: ");
            stringBuilder.append(this.mCurMethodId);
            Slog.w(str, stringBuilder.toString());
            return this.mNoBinding;
        }
        unbindCurrentMethodLocked(true);
        this.mCurIntent = new Intent("android.view.InputMethod");
        this.mCurIntent.setComponent(info.getComponent());
        this.mCurIntent.putExtra("android.intent.extra.client_label", 17040221);
        this.mCurIntent.putExtra("android.intent.extra.client_intent", PendingIntent.getActivity(this.mContext, 0, new Intent("android.settings.INPUT_METHOD_SETTINGS"), 0));
        if (bindCurrentInputMethodService(this.mCurIntent, this, 1610612741)) {
            this.mLastBindTime = SystemClock.uptimeMillis();
            this.mHaveConnection = true;
            this.mCurId = info.getId();
            this.mCurToken = new Binder();
            try {
                str = TAG;
                stringBuilder = new StringBuilder();
                stringBuilder.append("Adding window token: ");
                stringBuilder.append(this.mCurToken);
                Slog.v(str, stringBuilder.toString());
                this.mIWindowManager.addWindowToken(this.mCurToken, HwArbitrationDEFS.MSG_ARBITRATION_REQUEST_MPLINK, 0);
            } catch (RemoteException e) {
            }
            return new InputBindResult(2, null, null, this.mCurId, this.mCurSeq, this.mCurUserActionNotificationSequenceNumber);
        }
        this.mCurIntent = null;
        String str2 = TAG;
        StringBuilder stringBuilder2 = new StringBuilder();
        stringBuilder2.append("Failure connecting to input method service: ");
        stringBuilder2.append(this.mCurIntent);
        Slog.w(str2, stringBuilder2.toString());
        return null;
    }

    private InputBindResult startInput(int startInputReason, IInputMethodClient client, IInputContext inputContext, int missingMethods, EditorInfo attribute, int controlFlags) {
        if (!calledFromValidUser()) {
            return null;
        }
        InputBindResult startInputLocked;
        synchronized (this.mMethodMap) {
            long ident = Binder.clearCallingIdentity();
            try {
                startInputLocked = startInputLocked(startInputReason, client, inputContext, missingMethods, attribute, controlFlags);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
        return startInputLocked;
    }

    public void finishInput(IInputMethodClient client) {
    }

    /* JADX WARNING: Missing block: B:19:0x0066, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onServiceConnected(ComponentName name, IBinder service) {
        synchronized (this.mMethodMap) {
            if (this.mCurIntent != null && name.equals(this.mCurIntent.getComponent())) {
                this.mCurMethod = IInputMethod.Stub.asInterface(service);
                if (this.mCurToken == null) {
                    Slog.w(TAG, "Service connected without a token!");
                    unbindCurrentMethodLocked(false);
                    return;
                }
                if (DEBUG_FLOW) {
                    String str = TAG;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Initiating attach with token: ");
                    stringBuilder.append(this.mCurToken);
                    Slog.v(str, stringBuilder.toString());
                }
                executeOrSendMessage(this.mCurMethod, this.mCaller.obtainMessageOO(MSG_ATTACH_TOKEN, this.mCurMethod, this.mCurToken));
                if (this.mCurClient != null) {
                    clearClientSessionLocked(this.mCurClient);
                    requestClientSessionLocked(this.mCurClient);
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:21:0x0057, code:
            return;
     */
    /* JADX WARNING: Missing block: B:23:0x0059, code:
            r9.dispose();
     */
    /* JADX WARNING: Missing block: B:24:0x005c, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void onSessionCreated(IInputMethod method, IInputMethodSession session, InputChannel channel) {
        synchronized (this.mMethodMap) {
            if (this.mCurMethod == null || method == null || this.mCurMethod.asBinder() != method.asBinder() || this.mCurClient == null) {
            } else {
                if (DEBUG_FLOW) {
                    Slog.v(TAG, "IME session created");
                }
                clearClientSessionLocked(this.mCurClient);
                this.mCurClient.curSession = new SessionState(this.mCurClient, method, session, channel);
                InputBindResult res = attachNewInputLocked(9, true);
                if (res == null) {
                } else if (res.method != null) {
                    executeOrSendMessage(this.mCurClient.client, this.mCaller.obtainMessageOO(MSG_BIND_CLIENT, this.mCurClient.client, res));
                }
            }
        }
    }

    void unbindCurrentMethodLocked(boolean savePosition) {
        if (this.mVisibleBound) {
            this.mContext.unbindService(this.mVisibleConnection);
            this.mVisibleBound = false;
        }
        if (this.mHaveConnection) {
            this.mContext.unbindService(this);
            this.mHaveConnection = false;
        }
        if (this.mCurToken != null) {
            try {
                if ((this.mImeWindowVis & 1) != 0 && savePosition) {
                    this.mWindowManagerInternal.saveLastInputMethodWindowForTransition();
                }
                this.mIWindowManager.removeWindowToken(this.mCurToken, 0);
            } catch (RemoteException e) {
            }
            this.mCurToken = null;
        }
        this.mCurId = null;
        clearCurMethodLocked();
    }

    void resetCurrentMethodAndClient(int unbindClientReason) {
        this.mCurMethodId = null;
        unbindCurrentMethodLocked(false);
        unbindCurrentClientLocked(unbindClientReason);
    }

    void requestClientSessionLocked(ClientState cs) {
        if (!cs.sessionRequested) {
            InputChannel[] channels = InputChannel.openInputChannelPair(cs.toString());
            cs.sessionRequested = true;
            executeOrSendMessage(this.mCurMethod, this.mCaller.obtainMessageOOO(MSG_CREATE_SESSION, this.mCurMethod, channels[1], new MethodCallback(this, this.mCurMethod, channels[0])));
        }
    }

    void clearClientSessionLocked(ClientState cs) {
        finishSessionLocked(cs.curSession);
        cs.curSession = null;
        cs.sessionRequested = false;
    }

    private void finishSessionLocked(SessionState sessionState) {
        if (sessionState != null) {
            if (sessionState.session != null) {
                try {
                    sessionState.session.finishSession();
                } catch (RemoteException e) {
                    Slog.w(TAG, "Session failed to close due to remote exception", e);
                    updateSystemUiLocked(this.mCurToken, 0, this.mBackDisposition);
                }
                sessionState.session = null;
            }
            if (sessionState.channel != null) {
                sessionState.channel.dispose();
                sessionState.channel = null;
            }
        }
    }

    void clearCurMethodLocked() {
        if (this.mCurMethod != null) {
            for (ClientState cs : this.mClients.values()) {
                clearClientSessionLocked(cs);
            }
            finishSessionLocked(this.mEnabledSession);
            this.mEnabledSession = null;
            this.mCurMethod = null;
        }
        if (this.mStatusBar != null) {
            this.mStatusBar.setIconVisibility(this.mSlotIme, false);
        }
        this.mInFullscreenMode = false;
    }

    public void onServiceDisconnected(ComponentName name) {
        synchronized (this.mMethodMap) {
            if (!(this.mCurMethod == null || this.mCurIntent == null || !name.equals(this.mCurIntent.getComponent()))) {
                clearCurMethodLocked();
                this.mLastBindTime = SystemClock.uptimeMillis();
                this.mShowRequested = this.mInputShown;
                this.mInputShown = false;
                if (this.mCurClient != null) {
                    executeOrSendMessage(this.mCurClient.client, this.mCaller.obtainMessageIIO(3000, 3, this.mCurSeq, this.mCurClient.client));
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:31:0x007f, code:
            android.os.Binder.restoreCallingIdentity(r0);
     */
    /* JADX WARNING: Missing block: B:32:0x0083, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void updateStatusIcon(IBinder token, String packageName, int iconId) {
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mMethodMap) {
                String str;
                if (!calledWithValidToken(token)) {
                    int uid = Binder.getCallingUid();
                    str = TAG;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Ignoring updateStatusIcon due to an invalid token. uid:");
                    stringBuilder.append(uid);
                    stringBuilder.append(" token:");
                    stringBuilder.append(token);
                    Slog.e(str, stringBuilder.toString());
                    Binder.restoreCallingIdentity(ident);
                } else if (iconId == 0) {
                    if (this.mStatusBar != null) {
                        this.mStatusBar.setIconVisibility(this.mSlotIme, false);
                    }
                } else if (packageName != null) {
                    str = null;
                    CharSequence contentDescription = null;
                    try {
                        contentDescription = this.mContext.getPackageManager().getApplicationLabel(this.mIPackageManager.getApplicationInfo(packageName, 0, this.mSettings.getCurrentUserId()));
                    } catch (RemoteException e) {
                    }
                    if (this.mStatusBar != null) {
                        StatusBarManagerService statusBarManagerService = this.mStatusBar;
                        String str2 = this.mSlotIme;
                        if (contentDescription != null) {
                            str = contentDescription.toString();
                        }
                        statusBarManagerService.setIcon(str2, packageName, iconId, 0, str);
                        this.mStatusBar.setIconVisibility(this.mSlotIme, true);
                    }
                }
            }
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(ident);
        }
    }

    private boolean shouldShowImeSwitcherLocked(int visibility) {
        return false;
    }

    private boolean isKeyguardLocked() {
        return this.mKeyguardManager != null && this.mKeyguardManager.isKeyguardLocked();
    }

    private boolean isScreenLocked() {
        return this.mKeyguardManager != null && this.mKeyguardManager.isKeyguardLocked() && this.mKeyguardManager.isKeyguardSecure();
    }

    public void setImeWindowStatus(IBinder token, IBinder startInputToken, int vis, int backDisposition) {
        if (calledWithValidToken(token)) {
            StartInputInfo info;
            boolean dismissImeOnBackKeyPressed;
            synchronized (this.mMethodMap) {
                info = (StartInputInfo) this.mStartInputMap.get(startInputToken);
                this.mImeWindowVis = vis;
                this.mBackDisposition = backDisposition;
                updateSystemUiLocked(token, vis, backDisposition);
            }
            boolean z = false;
            switch (backDisposition) {
                case 1:
                    dismissImeOnBackKeyPressed = false;
                    break;
                case 2:
                    dismissImeOnBackKeyPressed = true;
                    break;
                default:
                    if ((vis & 2) == 0) {
                        dismissImeOnBackKeyPressed = false;
                        break;
                    } else {
                        dismissImeOnBackKeyPressed = true;
                        break;
                    }
            }
            WindowManagerInternal windowManagerInternal = this.mWindowManagerInternal;
            if ((vis & 2) != 0) {
                z = true;
            }
            windowManagerInternal.updateInputMethodWindowStatus(token, z, dismissImeOnBackKeyPressed, info != null ? info.mTargetWindow : null);
            return;
        }
        int uid = Binder.getCallingUid();
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Ignoring setImeWindowStatus due to an invalid token. uid:");
        stringBuilder.append(uid);
        stringBuilder.append(" token:");
        stringBuilder.append(token);
        Slog.e(str, stringBuilder.toString());
    }

    private void updateSystemUi(IBinder token, int vis, int backDisposition) {
        synchronized (this.mMethodMap) {
            updateSystemUiLocked(token, vis, backDisposition);
        }
    }

    private void updateSystemUiLocked(IBinder token, int vis, int backDisposition) {
        if (calledWithValidToken(token)) {
            long ident = Binder.clearCallingIdentity();
            if (vis != 0) {
                try {
                    if (isKeyguardLocked() && !this.mCurClientInKeyguard) {
                        vis = 0;
                    }
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(ident);
                }
            }
            boolean needsToShowImeSwitcher = shouldShowImeSwitcherLocked(vis);
            if (this.mStatusBar != null) {
                this.mStatusBar.setImeWindowStatus(token, vis, backDisposition, needsToShowImeSwitcher);
            }
            Binder.restoreCallingIdentity(ident);
            return;
        }
        int uid = Binder.getCallingUid();
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Ignoring updateSystemUiLocked due to an invalid token. uid:");
        stringBuilder.append(uid);
        stringBuilder.append(" token:");
        stringBuilder.append(token);
        Slog.e(str, stringBuilder.toString());
    }

    public void registerSuggestionSpansForNotification(SuggestionSpan[] spans) {
        if (calledFromValidUser()) {
            synchronized (this.mMethodMap) {
                InputMethodInfo currentImi = (InputMethodInfo) this.mMethodMap.get(this.mCurMethodId);
                for (SuggestionSpan ss : spans) {
                    if (!TextUtils.isEmpty(ss.getNotificationTargetClassName())) {
                        this.mSecureSuggestionSpans.put(ss, currentImi);
                    }
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:16:0x0030, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void reportFullscreenMode(IBinder token, boolean fullscreen) {
        if (calledFromValidUser()) {
            synchronized (this.mMethodMap) {
                if (!calledWithValidToken(token)) {
                } else if (!(this.mCurClient == null || this.mCurClient.client == null)) {
                    this.mInFullscreenMode = fullscreen;
                    executeOrSendMessage(this.mCurClient.client, this.mCaller.obtainMessageIO(MSG_REPORT_FULLSCREEN_MODE, fullscreen, this.mCurClient));
                }
            }
        }
    }

    public boolean notifySuggestionPicked(SuggestionSpan span, String originalString, int index) {
        return false;
    }

    public boolean showSoftInput(IInputMethodClient client, int flags, ResultReceiver resultReceiver) {
        if (!calledFromValidUser()) {
            return false;
        }
        int uid = Binder.getCallingUid();
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mMethodMap) {
                if ((this.mCurClient == null || client == null || this.mCurClient.client.asBinder() != client.asBinder()) && client != null) {
                    try {
                        if (!this.mIWindowManager.inputMethodClientHasFocus(client)) {
                            String str = TAG;
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append("Ignoring showSoftInput of uid ");
                            stringBuilder.append(uid);
                            stringBuilder.append(": ");
                            stringBuilder.append(client);
                            Slog.w(str, stringBuilder.toString());
                            Binder.restoreCallingIdentity(ident);
                            return false;
                        }
                    } catch (RemoteException e) {
                        Binder.restoreCallingIdentity(ident);
                        return false;
                    }
                }
                if (DEBUG_FLOW) {
                    String str2 = TAG;
                    StringBuilder stringBuilder2 = new StringBuilder();
                    stringBuilder2.append("Client requesting input be shown, requestedUid=");
                    stringBuilder2.append(uid);
                    Slog.v(str2, stringBuilder2.toString());
                }
                boolean showCurrentInputLocked = showCurrentInputLocked(flags, resultReceiver);
                Binder.restoreCallingIdentity(ident);
                return showCurrentInputLocked;
            }
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(ident);
        }
    }

    boolean showCurrentInputLocked(int flags, ResultReceiver resultReceiver) {
        this.mShowRequested = true;
        if ((flags & 2) != 0) {
            this.mShowExplicitlyRequested = true;
            this.mShowForced = true;
        } else if ((flags & 1) == 0) {
            this.mShowExplicitlyRequested = true;
        }
        if (!this.mSystemReady) {
            return false;
        }
        boolean res = false;
        if (this.mCurMethod != null) {
            if (DEBUG_FLOW) {
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("showCurrentInputLocked: mCurToken=");
                stringBuilder.append(this.mCurToken);
                Slog.d(str, stringBuilder.toString());
            }
            executeOrSendMessage(this.mCurMethod, this.mCaller.obtainMessageIOO(1020, getImeShowFlags(), this.mCurMethod, resultReceiver));
            this.mInputShown = true;
            if (this.mHaveConnection && !this.mVisibleBound) {
                bindCurrentInputMethodService(this.mCurIntent, this.mVisibleConnection, 201326593);
                this.mVisibleBound = true;
            }
            res = true;
        } else if (this.mHaveConnection && SystemClock.uptimeMillis() >= this.mLastBindTime + 3000) {
            EventLog.writeEvent(32000, new Object[]{this.mCurMethodId, Long.valueOf(SystemClock.uptimeMillis() - this.mLastBindTime), Integer.valueOf(1)});
            Slog.w(TAG, "Force disconnect/connect to the IME in showCurrentInputLocked()");
            this.mContext.unbindService(this);
            bindCurrentInputMethodService(this.mCurIntent, this, DATA_TCIS_ERROR_STEP.ID);
        }
        return res;
    }

    public boolean hideSoftInput(IInputMethodClient client, int flags, ResultReceiver resultReceiver) {
        if (!calledFromValidUser()) {
            return false;
        }
        int pid = Binder.getCallingPid();
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mMethodMap) {
                if ((this.mCurClient == null || client == null || this.mCurClient.client.asBinder() != client.asBinder()) && client != null) {
                    try {
                        if (!this.mIWindowManager.inputMethodClientHasFocus(client)) {
                            Binder.restoreCallingIdentity(ident);
                            return false;
                        }
                    } catch (RemoteException e) {
                        Binder.restoreCallingIdentity(ident);
                        return false;
                    }
                }
                if (DEBUG_FLOW) {
                    String str = TAG;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Client requesting input be hidden, pid=");
                    stringBuilder.append(pid);
                    Slog.v(str, stringBuilder.toString());
                }
                boolean hideCurrentInputLocked = hideCurrentInputLocked(flags, resultReceiver);
                Binder.restoreCallingIdentity(ident);
                return hideCurrentInputLocked;
            }
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(ident);
        }
    }

    boolean hideCurrentInputLocked(int flags, ResultReceiver resultReceiver) {
        if ((flags & 1) != 0 && (this.mShowExplicitlyRequested || this.mShowForced)) {
            return false;
        }
        if (this.mShowForced && (flags & 2) != 0) {
            return false;
        }
        boolean z = true;
        if (this.mCurMethod == null || (!this.mInputShown && (this.mImeWindowVis & 1) == 0)) {
            z = false;
        }
        if (z) {
            executeOrSendMessage(this.mCurMethod, this.mCaller.obtainMessageOO(1030, this.mCurMethod, resultReceiver));
            z = true;
        } else {
            z = false;
        }
        if (!(this.mInputShown || this.mShowRequested)) {
            z = false;
        }
        if (this.mHaveConnection && this.mVisibleBound) {
            this.mContext.unbindService(this.mVisibleConnection);
            this.mVisibleBound = false;
        }
        this.mInputShown = false;
        this.mShowRequested = false;
        this.mShowExplicitlyRequested = false;
        this.mShowForced = false;
        return z;
    }

    /* JADX WARNING: Missing block: B:34:0x006c, code:
            return null;
     */
    /* JADX WARNING: Missing block: B:42:0x0077, code:
            if (r2 != 8) goto L_0x0083;
     */
    /* JADX WARNING: Missing block: B:43:0x0079, code:
            android.util.Slog.i(TAG, "startInputOrWindowGainedFocus, client deactive by imms, set active again to enable secure inputmethod");
            r1.mShouldSetActive = true;
     */
    /* JADX WARNING: Missing block: B:44:0x0083, code:
            r0 = secureImeStartInputOrWindowGainedFocus(r12, r13, r14, r15, r16, r17, r18, r19, r20);
     */
    /* JADX WARNING: Missing block: B:45:0x0087, code:
            if (r0 == null) goto L_0x0090;
     */
    /* JADX WARNING: Missing block: B:47:0x008b, code:
            if (r0 == r1.mNoBinding) goto L_0x0090;
     */
    /* JADX WARNING: Missing block: B:48:0x008d, code:
            setClientActiveIfShould();
     */
    /* JADX WARNING: Missing block: B:49:0x0090, code:
            return r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public InputBindResult startInputOrWindowGainedFocus(int startInputReason, IInputMethodClient client, IBinder windowToken, int controlFlags, int softInputMode, int windowFlags, EditorInfo attribute, IInputContext inputContext, int missingMethods, int unverifiedTargetSdkVersion) {
        int i = startInputReason;
        IBinder iBinder = windowToken;
        if (!calledFromValidUser()) {
            return null;
        }
        synchronized (this.mMethodMap) {
            long ident = Binder.clearCallingIdentity();
            try {
                if (this.mCurMethodId == null) {
                    if (isSecureIMEEnable()) {
                        this.mCurMethodId = "com.huawei.secime/.SoftKeyboard";
                        Slog.d(TAG, "startInputOrWindowGainedFocus, mCurMethodId is null, reset");
                        this.mShouldSetActive = true;
                    } else {
                        Slog.d(TAG, "startInputOrWindowGainedFocus, secure ime is disable");
                        Binder.restoreCallingIdentity(ident);
                        return null;
                    }
                }
                if (iBinder == null || i != 10000) {
                    this.mUnbindCounter = 0;
                    Binder.restoreCallingIdentity(ident);
                } else {
                    if (this.mCurFocusedWindow != iBinder && this.mHaveConnection && this.mUnbindCounter > 0 && !isScreenLocked()) {
                        Slog.d(TAG, "unbind secime");
                        unbindCurrentMethodLocked(true);
                        unbindCurrentClientLocked(10000);
                    }
                    this.mUnbindCounter++;
                    this.mCurFocusedWindow = iBinder;
                }
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    private InputBindResult secureImeStartInputOrWindowGainedFocus(int startInputReason, IInputMethodClient client, IBinder windowToken, int controlFlags, int softInputMode, int windowFlags, EditorInfo attribute, IInputContext inputContext, int missingMethods) {
        if (windowToken != null) {
            return windowGainedFocus(startInputReason, client, windowToken, controlFlags, softInputMode, windowFlags, attribute, inputContext, missingMethods);
        }
        return startInput(startInputReason, client, inputContext, missingMethods, attribute, controlFlags);
    }

    protected InputBindResult windowGainedFocus(int startInputReason, IInputMethodClient client, IBinder windowToken, int controlFlags, int softInputMode, int windowFlags, EditorInfo attribute, IInputContext inputContext, int missingMethods) {
        Throwable th;
        HashMap hashMap;
        boolean z;
        IBinder iBinder = windowToken;
        int i = softInputMode;
        EditorInfo editorInfo = attribute;
        boolean calledFromValidUser = calledFromValidUser();
        InputBindResult res = null;
        long ident = Binder.clearCallingIdentity();
        try {
            HashMap hashMap2 = this.mMethodMap;
            synchronized (hashMap2) {
                try {
                    ClientState cs = (ClientState) this.mClients.get(client.asBinder());
                    StringBuilder stringBuilder;
                    if (cs != null) {
                        String str;
                        try {
                            if (!this.mIWindowManager.inputMethodClientHasFocus(cs.client)) {
                                str = TAG;
                                stringBuilder = new StringBuilder();
                                stringBuilder.append("Focus gain on non-focused client ");
                                stringBuilder.append(cs.client);
                                stringBuilder.append(" (uid=");
                                stringBuilder.append(cs.uid);
                                stringBuilder.append(" pid=");
                                stringBuilder.append(cs.pid);
                                stringBuilder.append(")");
                                Slog.w(str, stringBuilder.toString());
                                try {
                                    Binder.restoreCallingIdentity(ident);
                                    return null;
                                } catch (Throwable th2) {
                                    th = th2;
                                    hashMap = hashMap2;
                                    z = calledFromValidUser;
                                    try {
                                        throw th;
                                    } catch (Throwable th3) {
                                        th = th3;
                                    }
                                }
                            }
                        } catch (RemoteException e) {
                        }
                        if (!calledFromValidUser) {
                            Slog.w(TAG, "A background user is requesting window. Hiding IME.");
                            Slog.w(TAG, "If you want to interect with IME, you need android.permission.INTERACT_ACROSS_USERS_FULL");
                            hideCurrentInputLocked(0, null);
                            Binder.restoreCallingIdentity(ident);
                            return null;
                        } else if (this.mCurFocusedWindow == iBinder) {
                            str = TAG;
                            stringBuilder = new StringBuilder();
                            stringBuilder.append("Window already focused, ignoring focus gain of: ");
                            stringBuilder.append(client);
                            stringBuilder.append(" attribute=");
                            stringBuilder.append(editorInfo);
                            stringBuilder.append(", token = ");
                            stringBuilder.append(iBinder);
                            Slog.w(str, stringBuilder.toString());
                            if (editorInfo != null) {
                                calledFromValidUser = cs;
                                hashMap = hashMap2;
                                InputBindResult startInputUncheckedLocked = startInputUncheckedLocked(cs, inputContext, missingMethods, editorInfo, controlFlags, startInputReason);
                                Binder.restoreCallingIdentity(ident);
                                return startInputUncheckedLocked;
                            }
                            hashMap = hashMap2;
                            z = calledFromValidUser;
                            calledFromValidUser = cs;
                            if (this.mInputShown) {
                                str = TAG;
                                stringBuilder = new StringBuilder();
                                stringBuilder.append("Window already focused, force refreshime kgon:");
                                stringBuilder.append(isKeyguardLocked());
                                Slog.i(str, stringBuilder.toString());
                                updateSystemUi(this.mCurToken, this.mImeWindowVis, this.mBackDisposition);
                            }
                            Binder.restoreCallingIdentity(ident);
                            return null;
                        } else {
                            hashMap = hashMap2;
                            z = calledFromValidUser;
                            calledFromValidUser = cs;
                            this.mCurFocusedWindow = iBinder;
                            this.mCurFocusedWindowSoftInputMode = i;
                            this.mCurFocusedWindowClient = calledFromValidUser;
                            boolean z2 = (i & 240) == 16 || this.mRes.getConfiguration().isLayoutSizeAtLeast(3);
                            boolean doAutoShow = z2;
                            boolean isTextEditor = (controlFlags & 2) != 0;
                            boolean didStart = false;
                            ResultReceiver resultReceiver;
                            int i2;
                            switch (i & 15) {
                                case 0:
                                    resultReceiver = null;
                                    i2 = 1;
                                    if (!isTextEditor || !doAutoShow) {
                                        if (LayoutParams.mayUseInputMethod(windowFlags)) {
                                            if (DEBUG_FLOW) {
                                                Slog.v(TAG, "Unspecified window will hide input");
                                            }
                                            hideCurrentInputLocked(2, resultReceiver);
                                            break;
                                        }
                                    } else if ((i & 256) != 0) {
                                        if (DEBUG_FLOW) {
                                            Slog.v(TAG, "Unspecified window will show input");
                                        }
                                        if (editorInfo != null) {
                                            didStart = true;
                                            res = startInputUncheckedLocked(calledFromValidUser, inputContext, missingMethods, editorInfo, controlFlags, startInputReason);
                                        }
                                        showCurrentInputLocked(i2, resultReceiver);
                                        break;
                                    }
                                    break;
                                case 2:
                                    resultReceiver = null;
                                    if ((i & 256) != 0) {
                                        if (DEBUG_FLOW) {
                                            Slog.v(TAG, "Window asks to hide input going forward");
                                        }
                                        hideCurrentInputLocked(0, resultReceiver);
                                        break;
                                    }
                                    break;
                                case 3:
                                    resultReceiver = null;
                                    if (DEBUG_FLOW) {
                                        Slog.v(TAG, "Window asks to hide input");
                                    }
                                    hideCurrentInputLocked(0, resultReceiver);
                                    break;
                                case 4:
                                    resultReceiver = null;
                                    i2 = 1;
                                    if ((i & 256) != 0) {
                                        if (DEBUG_FLOW) {
                                            Slog.v(TAG, "Window asks to show input going forward");
                                        }
                                        if (editorInfo != null) {
                                            didStart = true;
                                            res = startInputUncheckedLocked(calledFromValidUser, inputContext, missingMethods, editorInfo, controlFlags, startInputReason);
                                        }
                                        showCurrentInputLocked(i2, resultReceiver);
                                        break;
                                    }
                                    break;
                                case 5:
                                    if (DEBUG_FLOW) {
                                        Slog.v(TAG, "Window asks to always show input");
                                    }
                                    if (editorInfo != null) {
                                        resultReceiver = null;
                                        i2 = 1;
                                        didStart = true;
                                        res = startInputUncheckedLocked(calledFromValidUser, inputContext, missingMethods, editorInfo, controlFlags, startInputReason);
                                    } else {
                                        resultReceiver = null;
                                        i2 = 1;
                                    }
                                    showCurrentInputLocked(i2, resultReceiver);
                                    break;
                            }
                            if (!(didStart || editorInfo == null)) {
                                res = startInputUncheckedLocked(calledFromValidUser, inputContext, missingMethods, editorInfo, controlFlags, startInputReason);
                            }
                            Binder.restoreCallingIdentity(ident);
                            return res;
                        }
                    }
                    hashMap = hashMap2;
                    z = calledFromValidUser;
                    calledFromValidUser = cs;
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("unknown client ");
                    stringBuilder.append(client.asBinder());
                    throw new IllegalArgumentException(stringBuilder.toString());
                } catch (Throwable th4) {
                    th = th4;
                    throw th;
                }
            }
        } catch (Throwable th5) {
            th = th5;
            z = calledFromValidUser;
            Binder.restoreCallingIdentity(ident);
            throw th;
        }
    }

    public void showInputMethodPickerFromClient(IInputMethodClient client, int auxiliarySubtypeMode) {
    }

    public void setInputMethod(IBinder token, String id) {
    }

    public void setInputMethodAndSubtype(IBinder token, String id, InputMethodSubtype subtype) {
    }

    public void showInputMethodAndSubtypeEnablerFromClient(IInputMethodClient client, String inputMethodId) {
    }

    public boolean isInputMethodPickerShownForTest() {
        return false;
    }

    public boolean switchToNextInputMethod(IBinder token, boolean onlyCurrentIme) {
        return false;
    }

    public boolean shouldOfferSwitchingToNextInputMethod(IBinder token) {
        return false;
    }

    /* JADX WARNING: Missing block: B:26:0x0053, code:
            return null;
     */
    /* JADX WARNING: Missing block: B:31:0x0058, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public InputMethodSubtype getLastInputMethodSubtype() {
        if (!calledFromValidUser()) {
            return null;
        }
        synchronized (this.mMethodMap) {
            Pair<String, String> lastIme = this.mSettings.getLastInputMethodAndSubtypeLocked();
            if (lastIme == null || TextUtils.isEmpty((CharSequence) lastIme.first) || TextUtils.isEmpty((CharSequence) lastIme.second)) {
            } else {
                InputMethodInfo lastImi = (InputMethodInfo) this.mMethodMap.get(lastIme.first);
                if (lastImi == null) {
                    return null;
                }
                try {
                    int lastSubtypeId = InputMethodUtils.getSubtypeIdFromHashCode(lastImi, Integer.parseInt((String) lastIme.second));
                    if (lastSubtypeId < 0 || lastSubtypeId >= lastImi.getSubtypeCount()) {
                    } else {
                        InputMethodSubtype subtypeAt = lastImi.getSubtypeAt(lastSubtypeId);
                        return subtypeAt;
                    }
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }
    }

    public void setAdditionalInputMethodSubtypes(String imiId, InputMethodSubtype[] subtypes) {
    }

    public int getInputMethodWindowVisibleHeight() {
        return this.mWindowManagerInternal.getInputMethodWindowVisibleHeight();
    }

    public void clearLastInputMethodWindowForTransition(IBinder token) {
        if (calledFromValidUser()) {
            long ident = Binder.clearCallingIdentity();
            try {
                synchronized (this.mMethodMap) {
                    if (calledWithValidToken(token)) {
                        this.mWindowManagerInternal.clearLastInputMethodWindowForTransition();
                        Binder.restoreCallingIdentity(ident);
                        return;
                    }
                    int uid = Binder.getCallingUid();
                    String str = TAG;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Ignoring clearLastInputMethodWindowForTransition due to an invalid token. uid:");
                    stringBuilder.append(uid);
                    stringBuilder.append(" token:");
                    stringBuilder.append(token);
                    Slog.e(str, stringBuilder.toString());
                    Binder.restoreCallingIdentity(ident);
                }
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    public void notifyUserAction(int sequenceNumber) {
    }

    /* JADX WARNING: Missing block: B:20:0x0067, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void hideMySoftInput(IBinder token, int flags) {
        if (calledFromValidUser()) {
            synchronized (this.mMethodMap) {
                if (calledWithValidToken(token)) {
                    if (DEBUG_FLOW) {
                        String str = TAG;
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("hideMySoftInput, pid=");
                        stringBuilder.append(Binder.getCallingPid());
                        stringBuilder.append(", token=");
                        stringBuilder.append(token);
                        Slog.v(str, stringBuilder.toString());
                    }
                    long ident = Binder.clearCallingIdentity();
                    try {
                        hideCurrentInputLocked(flags, null);
                    } finally {
                        Binder.restoreCallingIdentity(ident);
                    }
                } else {
                    int uid = Binder.getCallingUid();
                    String str2 = TAG;
                    StringBuilder stringBuilder2 = new StringBuilder();
                    stringBuilder2.append("Ignoring hideInputMethod due to an invalid token. uid:");
                    stringBuilder2.append(uid);
                    stringBuilder2.append(" token:");
                    stringBuilder2.append(token);
                    Slog.e(str2, stringBuilder2.toString());
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:17:0x0041, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void showMySoftInput(IBinder token, int flags) {
        if (calledFromValidUser()) {
            synchronized (this.mMethodMap) {
                if (calledWithValidToken(token)) {
                    long ident = Binder.clearCallingIdentity();
                    try {
                        showCurrentInputLocked(flags, null);
                    } finally {
                        Binder.restoreCallingIdentity(ident);
                    }
                } else {
                    int uid = Binder.getCallingUid();
                    String str = TAG;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Ignoring showMySoftInput due to an invalid token. uid:");
                    stringBuilder.append(uid);
                    stringBuilder.append(" token:");
                    stringBuilder.append(token);
                    Slog.e(str, stringBuilder.toString());
                }
            }
        }
    }

    void setEnabledSessionInMainThread(SessionState session) {
        if (this.mEnabledSession != session) {
            if (!(this.mEnabledSession == null || this.mEnabledSession.session == null)) {
                try {
                    this.mEnabledSession.method.setSessionEnabled(this.mEnabledSession.session, false);
                } catch (RemoteException e) {
                }
            }
            this.mEnabledSession = session;
            if (this.mEnabledSession != null && this.mEnabledSession.session != null) {
                try {
                    this.mEnabledSession.method.setSessionEnabled(this.mEnabledSession.session, true);
                } catch (RemoteException e2) {
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:30:0x00a1, code:
            if (android.os.Binder.isProxy(r1) != false) goto L_0x00a3;
     */
    /* JADX WARNING: Missing block: B:31:0x00a3, code:
            r3.channel.dispose();
     */
    /* JADX WARNING: Missing block: B:39:0x00cc, code:
            if (android.os.Binder.isProxy(r1) != false) goto L_0x00a3;
     */
    /* JADX WARNING: Missing block: B:67:0x013c, code:
            if (android.os.Binder.isProxy(r1) != false) goto L_0x0155;
     */
    /* JADX WARNING: Missing block: B:77:0x0153, code:
            if (android.os.Binder.isProxy(r1) != false) goto L_0x0155;
     */
    /* JADX WARNING: Missing block: B:78:0x0155, code:
            r3.dispose();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean handleMessage(Message msg) {
        boolean z = false;
        SomeArgs args;
        int missingMethods;
        switch (msg.what) {
            case 1000:
                try {
                    ((IInputMethod) msg.obj).unbindInput();
                } catch (RemoteException e) {
                }
                return true;
            case 1010:
                args = (SomeArgs) msg.obj;
                try {
                    ((IInputMethod) args.arg1).bindInput((InputBinding) args.arg2);
                } catch (RemoteException e2) {
                }
                args.recycle();
                return true;
            case 1020:
                args = (SomeArgs) msg.obj;
                try {
                    ((IInputMethod) args.arg1).showSoftInput(msg.arg1, (ResultReceiver) args.arg2);
                } catch (RemoteException e3) {
                }
                args.recycle();
                return true;
            case 1030:
                args = (SomeArgs) msg.obj;
                try {
                    ((IInputMethod) args.arg1).hideSoftInput(0, (ResultReceiver) args.arg2);
                } catch (RemoteException e4) {
                }
                args.recycle();
                return true;
            case MSG_ATTACH_TOKEN /*1040*/:
                args = (SomeArgs) msg.obj;
                try {
                    ((IInputMethod) args.arg1).attachToken((IBinder) args.arg2);
                } catch (RemoteException e5) {
                }
                args.recycle();
                return true;
            case MSG_CREATE_SESSION /*1050*/:
                args = msg.obj;
                IInputMethod method = args.arg1;
                InputChannel channel = args.arg2;
                try {
                    method.createSession(channel, (IInputSessionCallback) args.arg3);
                    if (channel != null) {
                        break;
                    }
                } catch (RemoteException e6) {
                    if (channel != null) {
                        break;
                    }
                } catch (Throwable th) {
                    if (channel != null && Binder.isProxy(method)) {
                        channel.dispose();
                    }
                }
                args.recycle();
                return true;
            case 2000:
                missingMethods = msg.arg1;
                boolean restarting = msg.arg2 != 0;
                SomeArgs args2 = msg.obj;
                IBinder startInputToken = args2.arg1;
                SessionState session = args2.arg2;
                IInputContext inputContext = args2.arg3;
                EditorInfo editorInfo = args2.arg4;
                try {
                    setEnabledSessionInMainThread(session);
                    session.method.startInput(startInputToken, inputContext, missingMethods, editorInfo, restarting);
                } catch (RemoteException e7) {
                }
                args2.recycle();
                return true;
            case 3000:
                try {
                    ((IInputMethodClient) msg.obj).onUnbindMethod(msg.arg1, msg.arg2);
                } catch (RemoteException e8) {
                }
                return true;
            case MSG_BIND_CLIENT /*3010*/:
                args = msg.obj;
                IInputMethodClient client = args.arg1;
                InputBindResult res = args.arg2;
                try {
                    client.onBindMethod(res);
                    if (res.channel != null) {
                        break;
                    }
                } catch (RemoteException e9) {
                    String str = TAG;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Client died receiving input method ");
                    stringBuilder.append(args.arg2);
                    Slog.w(str, stringBuilder.toString());
                    if (res.channel != null) {
                        break;
                    }
                } catch (Throwable th2) {
                    if (res.channel != null && Binder.isProxy(client)) {
                        res.channel.dispose();
                    }
                }
                args.recycle();
                return true;
            case MSG_SET_ACTIVE /*3020*/:
                try {
                    if (msg.arg1 == 1) {
                        IInputMethodClient iInputMethodClient = ((ClientState) msg.obj).client;
                        if (msg.arg2 != 0) {
                            z = true;
                        }
                        iInputMethodClient.setActive(true, z);
                    }
                } catch (RemoteException e10) {
                    String str2 = TAG;
                    StringBuilder stringBuilder2 = new StringBuilder();
                    stringBuilder2.append("Got RemoteException sending setActive(false) notification to pid ");
                    stringBuilder2.append(((ClientState) msg.obj).pid);
                    stringBuilder2.append(" uid ");
                    stringBuilder2.append(((ClientState) msg.obj).uid);
                    Slog.w(str2, stringBuilder2.toString());
                }
                return true;
            case MSG_SET_INTERACTIVE /*3030*/:
                if (msg.arg1 != 0) {
                    z = true;
                }
                handleSetInteractive(z);
                return true;
            case MSG_SET_USER_ACTION_NOTIFICATION_SEQUENCE_NUMBER /*3040*/:
                missingMethods = msg.arg1;
                ClientState clientState = msg.obj;
                try {
                    clientState.client.setUserActionNotificationSequenceNumber(missingMethods);
                } catch (RemoteException e11) {
                    String str3 = TAG;
                    StringBuilder stringBuilder3 = new StringBuilder();
                    stringBuilder3.append("Got RemoteException sending setUserActionNotificationSequenceNumber(");
                    stringBuilder3.append(missingMethods);
                    stringBuilder3.append(") notification to pid ");
                    stringBuilder3.append(clientState.pid);
                    stringBuilder3.append(" uid ");
                    stringBuilder3.append(clientState.uid);
                    Slog.w(str3, stringBuilder3.toString());
                }
                return true;
            default:
                return false;
        }
    }

    private void handleSetInteractive(boolean interactive) {
        synchronized (this.mMethodMap) {
            this.mIsInteractive = interactive;
            updateSystemUiLocked(this.mCurToken, interactive ? this.mImeWindowVis : 0, this.mBackDisposition);
            if (!(this.mCurClient == null || this.mCurClient.client == null)) {
                executeOrSendMessage(this.mCurClient.client, this.mCaller.obtainMessageIIO(MSG_SET_ACTIVE, this.mIsInteractive, this.mInFullscreenMode, this.mCurClient));
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:10:0x0086 A:{ExcHandler: org.xmlpull.v1.XmlPullParserException (r6_7 'e' java.lang.Exception), Splitter: B:8:0x0070} */
    /* JADX WARNING: Missing block: B:10:0x0086, code:
            r6 = move-exception;
     */
    /* JADX WARNING: Missing block: B:11:0x0087, code:
            r7 = TAG;
            r8 = new java.lang.StringBuilder();
            r8.append("Unable to load input method ");
            r8.append(r5);
            android.util.Slog.w(r7, r8.toString(), r6);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void buildInputMethodListLocked(boolean resetDefaultEnabledIme) {
        this.mMethodList.clear();
        this.mMethodMap.clear();
        List<ResolveInfo> services = this.mContext.getPackageManager().queryIntentServicesAsUser(new Intent("android.view.InputMethod"), 32896, this.mSettings.getCurrentUserId());
        for (int i = 0; i < services.size(); i++) {
            ResolveInfo ri = (ResolveInfo) services.get(i);
            ServiceInfo si = ri.serviceInfo;
            ComponentName compName = new ComponentName(si.packageName, si.name);
            if (!"android.permission.BIND_INPUT_METHOD".equals(si.permission)) {
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Skipping input method ");
                stringBuilder.append(compName);
                stringBuilder.append(": it does not require the permission ");
                stringBuilder.append("android.permission.BIND_INPUT_METHOD");
                Slog.w(str, stringBuilder.toString());
            } else if (shouldBuildInputMethodList(si.packageName)) {
                try {
                    InputMethodInfo p = new InputMethodInfo(this.mContext, ri);
                    this.mMethodList.add(p);
                    this.mMethodMap.put(p.getId(), p);
                } catch (Exception e) {
                }
            }
        }
        updateSecureIMEStatus();
    }

    private Pair<InputMethodInfo, InputMethodSubtype> findLastResortApplicableShortcutInputMethodAndSubtypeLocked(String mode) {
        for (InputMethodInfo imi : this.mSettings.getEnabledInputMethodListLocked()) {
            InputMethodUtils.getOverridingImplicitlyEnabledSubtypes(imi, mode);
        }
        return null;
    }

    public InputMethodSubtype getCurrentInputMethodSubtype() {
        return null;
    }

    /* JADX WARNING: Missing block: B:9:0x0024, code:
            return r1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public List getShortcutInputMethodsAndSubtypes() {
        synchronized (this.mMethodMap) {
            ArrayList<Object> ret = new ArrayList();
            if (this.mShortcutInputMethodsAndSubtypes.size() == 0) {
                Pair<InputMethodInfo, InputMethodSubtype> info = findLastResortApplicableShortcutInputMethodAndSubtypeLocked("voice");
                if (info != null) {
                    ret.add(info.first);
                    ret.add(info.second);
                }
            } else {
                return ret;
            }
        }
    }

    public boolean setCurrentInputMethodSubtype(InputMethodSubtype subtype) {
        return false;
    }

    private static String imeWindowStatusToString(int imeWindowVis) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        if ((imeWindowVis & 1) != 0) {
            sb.append("Active");
            first = false;
        }
        if ((imeWindowVis & 2) != 0) {
            if (!first) {
                sb.append("|");
            }
            sb.append("Visible");
        }
        return sb.toString();
    }

    public IInputContentUriToken createInputContentUriToken(IBinder token, Uri contentUri, String packageName) {
        if (!calledFromValidUser()) {
            return null;
        }
        if (token == null) {
            throw new NullPointerException("token");
        } else if (packageName == null) {
            throw new NullPointerException("packageName");
        } else if (contentUri != null) {
            if ("content".equals(contentUri.getScheme())) {
                synchronized (this.mMethodMap) {
                    int uid = Binder.getCallingUid();
                    String str;
                    StringBuilder stringBuilder;
                    if (this.mCurMethodId == null) {
                        return null;
                    } else if (this.mCurToken != token) {
                        str = TAG;
                        stringBuilder = new StringBuilder();
                        stringBuilder.append("Ignoring createInputContentUriToken mCurToken=");
                        stringBuilder.append(this.mCurToken);
                        stringBuilder.append(" token=");
                        stringBuilder.append(token);
                        Slog.e(str, stringBuilder.toString());
                        return null;
                    } else if (TextUtils.equals(this.mCurAttribute.packageName, packageName)) {
                        int imeUserId = UserHandle.getUserId(uid);
                        int appUserId = UserHandle.getUserId(this.mCurClient.uid);
                        InputContentUriTokenHandler inputContentUriTokenHandler = new InputContentUriTokenHandler(ContentProvider.getUriWithoutUserId(contentUri), uid, packageName, ContentProvider.getUserIdFromUri(contentUri, imeUserId), appUserId);
                        return inputContentUriTokenHandler;
                    } else {
                        str = TAG;
                        stringBuilder = new StringBuilder();
                        stringBuilder.append("Ignoring createInputContentUriToken mCurAttribute.packageName=");
                        stringBuilder.append(this.mCurAttribute.packageName);
                        stringBuilder.append(" packageName=");
                        stringBuilder.append(packageName);
                        Slog.e(str, stringBuilder.toString());
                        return null;
                    }
                }
            }
            throw new InvalidParameterException("contentUri must have content scheme");
        } else {
            throw new NullPointerException("contentUri");
        }
    }

    public boolean switchToPreviousInputMethod(IBinder token) {
        return false;
    }

    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Permission Denial: can't dump InputMethodManager from from pid=");
            stringBuilder.append(Binder.getCallingPid());
            stringBuilder.append(", uid=");
            stringBuilder.append(Binder.getCallingUid());
            pw.println(stringBuilder.toString());
            return;
        }
        ClientState ci;
        ClientState client;
        IInputMethod method;
        StringBuilder stringBuilder2;
        Printer p = new PrintWriterPrinter(pw);
        synchronized (this.mMethodMap) {
            StringBuilder stringBuilder3;
            p.println("  Clients:");
            for (ClientState ci2 : this.mClients.values()) {
                stringBuilder3 = new StringBuilder();
                stringBuilder3.append("  Client ");
                stringBuilder3.append(ci2);
                stringBuilder3.append(":");
                p.println(stringBuilder3.toString());
                stringBuilder3 = new StringBuilder();
                stringBuilder3.append("    client=");
                stringBuilder3.append(ci2.client);
                p.println(stringBuilder3.toString());
                stringBuilder3 = new StringBuilder();
                stringBuilder3.append("    inputContext=");
                stringBuilder3.append(ci2.inputContext);
                p.println(stringBuilder3.toString());
                stringBuilder3 = new StringBuilder();
                stringBuilder3.append("    sessionRequested=");
                stringBuilder3.append(ci2.sessionRequested);
                p.println(stringBuilder3.toString());
                stringBuilder3 = new StringBuilder();
                stringBuilder3.append("    curSession=");
                stringBuilder3.append(ci2.curSession);
                p.println(stringBuilder3.toString());
            }
            StringBuilder stringBuilder4 = new StringBuilder();
            stringBuilder4.append("  mCurMethodId=");
            stringBuilder4.append(this.mCurMethodId);
            p.println(stringBuilder4.toString());
            client = this.mCurClient;
            StringBuilder stringBuilder5 = new StringBuilder();
            stringBuilder5.append("  mCurClient=");
            stringBuilder5.append(client);
            stringBuilder5.append(" mCurSeq=");
            stringBuilder5.append(this.mCurSeq);
            p.println(stringBuilder5.toString());
            stringBuilder5 = new StringBuilder();
            stringBuilder5.append("  mCurFocusedWindow=");
            stringBuilder5.append(this.mCurFocusedWindow);
            stringBuilder5.append(" softInputMode=");
            stringBuilder5.append(InputMethodClient.softInputModeToString(this.mCurFocusedWindowSoftInputMode));
            stringBuilder5.append(" client=");
            stringBuilder5.append(this.mCurFocusedWindowClient);
            p.println(stringBuilder5.toString());
            ci2 = this.mCurFocusedWindowClient;
            stringBuilder3 = new StringBuilder();
            stringBuilder3.append("  mCurFocusedWindowClient=");
            stringBuilder3.append(ci2);
            p.println(stringBuilder3.toString());
            stringBuilder3 = new StringBuilder();
            stringBuilder3.append("  mCurId=");
            stringBuilder3.append(this.mCurId);
            stringBuilder3.append(" mHaveConnect=");
            stringBuilder3.append(this.mHaveConnection);
            stringBuilder3.append(" mBoundToMethod=");
            stringBuilder3.append(this.mBoundToMethod);
            p.println(stringBuilder3.toString());
            stringBuilder3 = new StringBuilder();
            stringBuilder3.append("  mCurToken=");
            stringBuilder3.append(this.mCurToken);
            p.println(stringBuilder3.toString());
            stringBuilder3 = new StringBuilder();
            stringBuilder3.append("  mCurIntent=");
            stringBuilder3.append(this.mCurIntent);
            p.println(stringBuilder3.toString());
            method = this.mCurMethod;
            stringBuilder2 = new StringBuilder();
            stringBuilder2.append("  mCurMethod=");
            stringBuilder2.append(this.mCurMethod);
            p.println(stringBuilder2.toString());
            stringBuilder2 = new StringBuilder();
            stringBuilder2.append("  mEnabledSession=");
            stringBuilder2.append(this.mEnabledSession);
            p.println(stringBuilder2.toString());
            stringBuilder2 = new StringBuilder();
            stringBuilder2.append("  mImeWindowVis=");
            stringBuilder2.append(imeWindowStatusToString(this.mImeWindowVis));
            p.println(stringBuilder2.toString());
            stringBuilder2 = new StringBuilder();
            stringBuilder2.append("  mShowRequested=");
            stringBuilder2.append(this.mShowRequested);
            stringBuilder2.append(" mShowExplicitlyRequested=");
            stringBuilder2.append(this.mShowExplicitlyRequested);
            stringBuilder2.append(" mShowForced=");
            stringBuilder2.append(this.mShowForced);
            stringBuilder2.append(" mInputShown=");
            stringBuilder2.append(this.mInputShown);
            p.println(stringBuilder2.toString());
            stringBuilder2 = new StringBuilder();
            stringBuilder2.append("  mInFullscreenMode=");
            stringBuilder2.append(this.mInFullscreenMode);
            p.println(stringBuilder2.toString());
            stringBuilder2 = new StringBuilder();
            stringBuilder2.append("  mCurUserActionNotificationSequenceNumber=");
            stringBuilder2.append(this.mCurUserActionNotificationSequenceNumber);
            p.println(stringBuilder2.toString());
            stringBuilder2 = new StringBuilder();
            stringBuilder2.append("  mSystemReady=");
            stringBuilder2.append(this.mSystemReady);
            stringBuilder2.append(" mInteractive=");
            stringBuilder2.append(this.mIsInteractive);
            p.println(stringBuilder2.toString());
            p.println("  mSettings:");
        }
        p.println(" ");
        if (client != null) {
            pw.flush();
            try {
                client.client.asBinder().dump(fd, args);
            } catch (RemoteException e) {
                stringBuilder2 = new StringBuilder();
                stringBuilder2.append("Input method client dead: ");
                stringBuilder2.append(e);
                p.println(stringBuilder2.toString());
            }
        } else {
            p.println("No input method client.");
        }
        if (!(ci2 == null || client == ci2)) {
            p.println(" ");
            p.println("Warning: Current input method client doesn't match the last focused. window.");
            p.println("Dumping input method client in the last focused window just in case.");
            p.println(" ");
            pw.flush();
            try {
                ci2.client.asBinder().dump(fd, args);
            } catch (RemoteException e2) {
                stringBuilder2 = new StringBuilder();
                stringBuilder2.append("Input method client in focused window dead: ");
                stringBuilder2.append(e2);
                p.println(stringBuilder2.toString());
            }
        }
        p.println(" ");
        if (method != null) {
            pw.flush();
            try {
                method.asBinder().dump(fd, args);
            } catch (RemoteException e22) {
                stringBuilder2 = new StringBuilder();
                stringBuilder2.append("Input method service dead: ");
                stringBuilder2.append(e22);
                p.println(stringBuilder2.toString());
            }
        } else {
            p.println("No input method service.");
        }
    }

    public IBinder getHwInnerService() {
        return null;
    }
}
