package com.install.appinstall.xl;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ConfigurationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.provider.Settings;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;
import android.content.ClipboardManager;
import java.util.concurrent.ConcurrentHashMap;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.os.Process;
import java.lang.ref.WeakReference;
import java.util.WeakHashMap;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import com.install.appinstall.xl.util.PermissionName;
import android.app.AppOpsManager;
import android.widget.Switch;
import java.util.LinkedHashMap;
import org.json.JSONException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.io.OutputStream;
import java.io.FilenameFilter;
import java.util.Comparator;
import java.util.Collection;
import java.io.IOException;
import android.view.ViewParent;
import android.content.ContentResolver;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.pm.ServiceInfo;
import android.telephony.TelephonyManager;

import com.install.appinstall.xl.util.Portcf;
import com.install.appinstall.xl.util.ToastUtil;
import com.install.appinstall.xl.util.instar;
import com.install.appinstall.xl.util.Update;
import com.install.appinstall.xl.util.Spkill;
import com.install.appinstall.xl.util.foat;
import com.install.appinstall.xl.util.Selinuxhook;
import com.install.appinstall.xl.util.ShareHook;
import com.install.appinstall.xl.util.AntiDetection;
import com.install.appinstall.xl.util.VpnStatusFaker;
import com.install.appinstall.xl.util.ReaLog;
import com.install.appinstall.xl.util.DebugModeManager;
import com.install.appinstall.xl.util.Prsprn;
import com.install.appinstall.xl.util.PkgMgr;

public class HookInit implements IXposedHookLoadPackage {
    private foat foatInstance;
    private Prsprn mPrsprn;
    private PkgMgr mPkgMgr;

    public static final Map<String, Boolean> installStatusMap = new ConcurrentHashMap<>();
    public static final Map<String, Boolean> floatingShownMap = new ConcurrentHashMap<>();
    public static final Map<String, Float> floatingXMap = new ConcurrentHashMap<>();
    public static final Map<String, Float> floatingYMap = new ConcurrentHashMap<>();
    public static Map<String, Boolean> launchInterceptMap = new ConcurrentHashMap<>();
    public static final Map<String, Boolean> blockExitMap = new ConcurrentHashMap<>();
    public static final Map<String, Boolean> permissionFakeMap = new ConcurrentHashMap<>();
    public static final Map<String, List<String>> userDefinedPackagesMap = new ConcurrentHashMap<>();
    public static final Map<String, List<String>> excludedPackagesMap = new ConcurrentHashMap<>();
    public static final Map<String, List<PackageConfig>> packageConfigMap = new ConcurrentHashMap<>();
    public static final Set<String> globalCapturedPackages = ConcurrentHashMap.newKeySet();
    public static final Map<String, Map<String, String>> autoActionMap = new ConcurrentHashMap<>();
    public static final Map<String, Map<String, List<String>>> autoChoiceRecordsMap = new ConcurrentHashMap<>();
    public static final Map<String, Boolean> superBlockExitMap = new ConcurrentHashMap<>();
    public static final Map<String, List<InterceptPattern>> interceptPatternsMap = new ConcurrentHashMap<>();
    public static final Map<String, Boolean> selinuxFakeMap = new ConcurrentHashMap<>();
    public static final Map<String, Map<String, Boolean>> shareFakeDetailMap = new ConcurrentHashMap<>();
    public static final Map<String, Map<String, Boolean>> antiDetectionDetailMap = new ConcurrentHashMap<>();
    public static final Map<String, Map<String, Boolean>> vpnFakeDetailMap = new ConcurrentHashMap<>();
    public static final Map<String, Boolean> forceDefaultBackMap = new ConcurrentHashMap<>();
    public static final Map<String, Boolean> crashProtectEnabledMap = new ConcurrentHashMap<>();
    public static final Map<String, Map<String, Boolean>> permissionFakeDetailMap = new ConcurrentHashMap<>();
    public static final Map<String, Boolean> userDisabledAutoBlockMap = new ConcurrentHashMap<>();
    private final Set<Activity> pendingFinishSet = Collections.newSetFromMap(new WeakHashMap<>());
    public final Map<String, String> pathPackageCache = new ConcurrentHashMap<>();
    public static final Map<String, Boolean> permanentHiddenMap = new ConcurrentHashMap<>();
    public final Map<String, Integer> packageStatusCache = new ConcurrentHashMap<>();
    public static final Map<String, DetectedPackages> detectedCache = new ConcurrentHashMap<>();
    public static final Map<String, Map<String, Boolean>> reaLogPauseMap = new ConcurrentHashMap<>();
    private static final Set<String> sReflectSkipCache = ConcurrentHashMap.newKeySet();
    public final Map<String, Boolean> mSystemCoreCache = new ConcurrentHashMap<>();
    public final Map<String, Boolean> mRealSystemCache = new ConcurrentHashMap<>();


    private static final ScheduledExecutorService sSaveExecutor = Executors.newSingleThreadScheduledExecutor();
    private static ScheduledFuture<?> sSaveFuture = null;
    private static final int SAVE_DELAY_MS = 300;

    public static volatile List<String> sCachedAppDirNames = null;
    public static volatile File[] sCachedAppDirFiles = null;
    public static volatile int sCacheVersion = 0;
    public static final Object sCacheLock = new Object();
    public Boolean mIsSystemPackageCache = null;
    public static final Map<String, List<PackageInfo>> sInstalledPackagesCache = new ConcurrentHashMap<>();
    public static final Map<String, List<ApplicationInfo>> sInstalledApplicationsCache = new ConcurrentHashMap<>();

    private static final ActivityInfo[] EMPTY_ACTIVITY_INFO = new ActivityInfo[0];
    private static final ServiceInfo[] EMPTY_SERVICE_INFO = new ServiceInfo[0];
    private static final ActivityInfo[] EMPTY_RECEIVER_INFO = new ActivityInfo[0];
    private static final ProviderInfo[] EMPTY_PROVIDER_INFO = new ProviderInfo[0];
    private static final PermissionInfo[] EMPTY_PERMISSION_INFO = new PermissionInfo[0];
    private static final Signature[] FAKE_SIGNATURES = new Signature[]{new Signature("fake".getBytes())};

    private static final Pattern SPACE_PATTERN = Pattern.compile("\\s+");

    private boolean autoBlockTriggeredThisSession = false;
    private String currentTargetApp = "";
    private static final String MODULE_TAG = "InstallHook";
    private static final String MODULE_PACKAGE = "com.install.appinstall.xl";
    private final ArrayList<String> appCapturedPackages = new ArrayList<>();
    private Activity currentResumedActivity = null;

    private static boolean sConfigLoadedLogged = false;
    public static final Set<String> sLoggedPackageSet = Collections.newSetFromMap(new ConcurrentHashMap<>());
    public static boolean sGlobalListLogged = false;
    private static String sCachedProcessName = null;
    private static final Map<String, Long> sLastLogTime = new ConcurrentHashMap<>();
    private static final long LOG_DEDUP_INTERVAL_MS = 5000;
    private static final int MAX_LOG_CACHE_SIZE = 1000;
    private static final Set<String> sDefaultConfigLogged = new HashSet<>();

    public boolean isExitPending = false;
    public String pendingExitMethod = "";
    public Object pendingExitParam = null;
    public int isCurrentlyBlocking;
    public String blockText;
    public static int sCrashRecoverCount = 0;
    public static final String SP_CRASH_NAME = "crash_protect_sp";
    public static final String SP_KEY_COUNT = "crash_count";
    public static final String SP_KEY_LAST_TIME = "crash_last_time";

    private static volatile boolean isHandlingCrash = false;
    private static volatile int sCrashCount = 0;
    private static volatile long sLastCrashTime = 0;
    private static volatile Context sCachedAppContext = null;
    private static volatile Intent sCachedLaunchIntent = null;

    private static WeakReference<AlertDialog> sLastDialogRef = null;
    private static long sLastDialogCreateTime = 0;
    private static final int DIALOG_DEDUP_WINDOW_MS = 300;
    public static final int DOUBLE_CLICK_THRESHOLD = 500;
    public long lastClickTime = 0;
    public AlertDialog statusSwitchDialog;
    private static final String FALLBACK_PACKAGE = "com.小淋.虚假APP";

    private static final String[] DETECTION_PERMISSIONS = {
            "android.permission.QUERY_ALL_PACKAGES",
            "android.permission.GET_PACKAGE_SIZE",
            "com.android.permission.GET_INSTALLED_APPS",
            "android.permission.PACKAGE_USAGE_STATS",
            "android.permission.PACKAGE_VERIFICATION_AGENT",
            "android.permission.READ_DEFAULT_APPLICATIONS",
            "android.permission.SET_PREFERRED_APPLICATIONS",
    };
    private static final Set<String> DETECTION_PERMISSIONS_SET = new HashSet<>(Arrays.asList(DETECTION_PERMISSIONS));

    private static final Map<String, String> PERMISSION_INTENT_MAP = new HashMap<>();

    static {
        PERMISSION_INTENT_MAP.put("android.settings.MANAGE_OVERLAY_PERMISSION", "android.permission.SYSTEM_ALERT_WINDOW");
        PERMISSION_INTENT_MAP.put("android.settings.MANAGE_UNKNOWN_APP_SOURCES", "android.permission.REQUEST_INSTALL_PACKAGES");
        PERMISSION_INTENT_MAP.put("android.settings.NOTIFICATION_LISTENER_SETTINGS", "android.permission.BIND_NOTIFICATION_LISTENER_SERVICE");
        PERMISSION_INTENT_MAP.put("android.settings.ACCESSIBILITY_SETTINGS", "android.permission.BIND_ACCESSIBILITY_SERVICE");
        PERMISSION_INTENT_MAP.put("android.settings.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS", "android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS");
        PERMISSION_INTENT_MAP.put("android.settings.USAGE_ACCESS_SETTINGS", "android.permission.PACKAGE_USAGE_STATS");
        PERMISSION_INTENT_MAP.put("android.settings.MANAGE_ALL_FILES_ACCESS_PERMISSION", "android.permission.MANAGE_EXTERNAL_STORAGE");
        PERMISSION_INTENT_MAP.put("android.settings.PICTURE_IN_PICTURE_SETTINGS", "android.permission.PICTURE_IN_PICTURE");
        PERMISSION_INTENT_MAP.put("android.settings.DEVICE_ADMIN_SETTINGS", "android.permission.BIND_DEVICE_ADMIN");
        PERMISSION_INTENT_MAP.put("android.settings.EXTERNAL_STORAGE_ACCESS_SETTINGS", "android.permission.ACCESS_EXTERNAL_STORAGE");
    }

    private enum DataSourceGroup {
        CONTACTS(new String[]{"contacts", "com.android.contacts"},
                "android.permission.READ_CONTACTS", "android.permission.WRITE_CONTACTS"),
        SMS(new String[]{"sms", "mms", "mms-sms"},
                "android.permission.READ_SMS"),
        CALL_LOG(new String[]{"call_log"},
                "android.permission.READ_CALL_LOG"),
        CALENDAR(new String[]{"calendar"},
                "android.permission.READ_CALENDAR");

        final String[] authorities;
        final String[] permissions;

        DataSourceGroup(String[] authorities, String... permissions) {
            this.authorities = authorities;
            this.permissions = permissions;
        }

        static DataSourceGroup match(Uri uri) {
            if (uri == null) return null;
            String auth = uri.getAuthority();
            if (auth == null) return null;
            String lowerAuth = auth.toLowerCase();
            for (DataSourceGroup group : values()) {
                for (String a : group.authorities) {
                    if (lowerAuth.contains(a) || lowerAuth.equals(a)) {
                        return group;
                    }
                }
            }
            return null;
        }
    }

    private static final String[] ALL_VPN_KEYS = {
            "interface", "cmd", "proxy", "proxy_env", "capture", "netfiles", "net_detect",
            "ssl_trust", "ssl_pinning", "ssl_cert", "ssl_cert_hide"
    };

    private static final String[] ALL_ANTI_KEYS = {
            "class", "file", "pm", "proc", "cmd", "stacktrace", "root", "adb", "dev"
    };

    private final Map<Activity, Long> mPauseTimeMap = new WeakHashMap<>();
    private static volatile long sTimeOffset = 0;

    private static final List<QueryPattern> queryPatterns = new ArrayList<>();
    private static final Random random = new Random();

    private static class QueryPattern {
        String targetApp;
        String queriedPackage;
        int flags;
        long timestamp;

        QueryPattern(String targetApp, String queriedPackage, int flags) {
            this.targetApp = targetApp;
            this.queriedPackage = queriedPackage;
            this.flags = flags;
            this.timestamp = System.currentTimeMillis();
        }
    }

    private static final int MAX_CACHE_SIZE = 4096;
    private static final Map<String, PackageInfo> sPackageInfoCache = Collections.synchronizedMap(new LinkedHashMap<String, PackageInfo>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, PackageInfo> eldest) {
            return size() > MAX_CACHE_SIZE;
        }
    });
    private static final Map<String, ApplicationInfo> sAppInfoCache = Collections.synchronizedMap(new LinkedHashMap<String, ApplicationInfo>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, ApplicationInfo> eldest) {
            return size() > MAX_CACHE_SIZE;
        }
    });
    private static final Map<String, String> versionCache = new HashMap<>();
    private static final Map<String, Integer> versionCodeCache = new HashMap<>();
    private static final Map<String, Long> installTimeCache = new HashMap<>();
    private static final Map<String, String> installerCache = new HashMap<>();
    private static final Map<String, String> appNameCache = new HashMap<>();

    public static class PackageConfig {
        public String packageName;
        public String statusMode;

        public PackageConfig(String packageName) {
            this.packageName = packageName;
            this.statusMode = "follow";
        }
    }

    public static class InterceptPattern {
        public String patternHash;
        public List<String> installedPackages;
        public List<String> notInstalledPackages;
        public String userChoice;
        public int choiceCount;
        public long lastDetectedTime;
        public boolean silentIntercept;
        public ArrayList<String> recentChoices;

        public InterceptPattern(String patternHash, List<String> installedPackages, List<String> notInstalledPackages) {
            this.patternHash = patternHash;
            this.installedPackages = new ArrayList<>(installedPackages);
            this.notInstalledPackages = new ArrayList<>(notInstalledPackages);
            this.choiceCount = 0;
            this.lastDetectedTime = System.currentTimeMillis();
            this.silentIntercept = false;
            this.recentChoices = new ArrayList<>();
        }
    }

    public static class DetectedPackages {
        public List<String> installedPackages;
        public List<String> notInstalledPackages;
        public String patternHash;

        public DetectedPackages() {
            installedPackages = new ArrayList<>();
            notInstalledPackages = new ArrayList<>();
        }
    }

private static boolean matchesPattern(String identifier, String pattern) {
    if (identifier == null || pattern == null) return false;
    identifier = identifier.toLowerCase();
    pattern = pattern.toLowerCase();

    boolean isExclude = pattern.startsWith("!");
    String actualPattern = isExclude ? pattern.substring(1) : pattern;

    if (!actualPattern.contains("*")) {
        return isExclude ? !identifier.equals(actualPattern) : identifier.equals(actualPattern);
    }

    String regex = actualPattern.replace(".", "\\.").replace("*", ".*");
    try {
        boolean matched = identifier.matches(regex);
        return isExclude ? !matched : matched;
    } catch (java.util.regex.PatternSyntaxException e) {
        return false;
    }
}

  public static String getAutoAction(String app, String identifier) {
    Map<String, String> actions = autoActionMap.get(app);
    if (actions == null) return null;

    // 精确匹配
    String exact = actions.get(identifier);
    if (exact != null) return exact;

    // 通配符匹配：按模式长度降序（最长优先）
    String matched = null;
    int maxLen = -1;
    for (Map.Entry<String, String> entry : actions.entrySet()) {
        String pattern = entry.getKey();
        if (pattern.contains("*") && matchesPattern(identifier, pattern)) {
            int len = pattern.length();
            if (len > maxLen) {
                maxLen = len;
                matched = entry.getValue();
            }
        }
    }
    return matched;
}

    public static String checkAutoChoice(String app, String identifier) {
    Map<String, List<String>> records = autoChoiceRecordsMap.get(app);
    if (records == null) return null;

    // 精确匹配
    List<String> history = records.get(identifier);
    if (history != null && history.size() >= 3) {
        String first = history.get(0);
        for (int i = 1; i < history.size(); i++) {
            if (!first.equals(history.get(i))) return null;
        }
        return first;
    }

    // 通配符匹配：按模式长度降序
    String matched = null;
    int maxLen = -1;
    for (Map.Entry<String, List<String>> entry : records.entrySet()) {
        String pattern = entry.getKey();
        if (pattern.contains("*") && matchesPattern(identifier, pattern)) {
            List<String> rec = entry.getValue();
            if (rec != null && rec.size() >= 3) {
                String first = rec.get(0);
                boolean allSame = true;
                for (int i = 1; i < rec.size(); i++) {
                    if (!first.equals(rec.get(i))) {
                        allSame = false;
                        break;
                    }
                }
                if (allSame) {
                    int len = pattern.length();
                    if (len > maxLen) {
                        maxLen = len;
                        matched = first;
                    }
                }
            }
        }
    }
    return matched;
}

    public static void putAutoAction(String app, String finalidentifier, String action) {
        Map<String, String> actions = autoActionMap.get(app);
        if (actions == null) {
            actions = new HashMap<>();
            autoActionMap.put(app, actions);
        }
        actions.put(finalidentifier, action);
    }

    public static void removeAutoAction(String app, String finalidentifier) {
        Map<String, String> actions = autoActionMap.get(app);
        if (actions != null) {
            actions.remove(finalidentifier);
        }
    }

    public static void addAutoRecord(String app, String identifier, String choice) {
        Map<String, List<String>> records = autoChoiceRecordsMap.get(app);
        if (records == null) {
            records = new HashMap<>();
            autoChoiceRecordsMap.put(app, records);
        }
        List<String> history = records.get(identifier);
        if (history == null) {
            history = new ArrayList<>();
            records.put(identifier, history);
        }
        history.add(choice);
        if (history.size() > 3) {
            history.remove(0);
        }
    }

    public static Map<String, String> getAllAutoActions(String app) {
        return autoActionMap.getOrDefault(app, new HashMap<String, String>());
    }

    public static Map<String, List<String>> getAllAutoRecords(String app) {
        return autoChoiceRecordsMap.getOrDefault(app, new HashMap<String, List<String>>());
    }

    public static void removeAutoRecord(String app, String identifier) {
        Map<String, List<String>> records = autoChoiceRecordsMap.get(app);
        if (records != null) {
            records.remove(identifier);
        }
    }

    public void setCurrentResumedActivity(Activity activity) {
        this.currentResumedActivity = activity;
    }

    public Activity getCurrentResumedActivity() {
        return this.currentResumedActivity;
    }

    private final ConcurrentHashMap<Activity, Boolean> backPressFinishFlag = new ConcurrentHashMap<>();
    private static long lastBackPressTime = 0;
    private static final int BACK_PRESS_DOUBLE_CLICK_THRESHOLD = 500;
    private static boolean backPressDoubleClickFlag = false;

    public static void setBackPressDoubleClickFlag(boolean value) {
        backPressDoubleClickFlag = value;
    }

    public static boolean isBackPressDoubleClickFlag() {
        return backPressDoubleClickFlag;
    }

    public static boolean isBackPressDoubleClick() {
        long current = System.currentTimeMillis();
        if (current - lastBackPressTime < BACK_PRESS_DOUBLE_CLICK_THRESHOLD) {
            lastBackPressTime = 0;
            return true;
        }
        lastBackPressTime = current;
        return false;
    }

    public boolean isBackPressFinish(Activity activity) {
        return backPressFinishFlag.getOrDefault(activity, false);
    }

    private void markBackPressFinish(final Activity activity) {
        if (activity != null) {
            backPressFinishFlag.put(activity, true);
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    backPressFinishFlag.remove(activity);
                }
            }, 500);
        }
    }

    private static class MenuItem {
        String title;
        String desc;
        Runnable action;

        MenuItem(String title, String desc, Runnable action) {
            this.title = title;
            this.desc = desc;
            this.action = action;
        }
    }

    private static class DebounceEntry {
        WeakReference<Activity> activityRef;
        Intent originalIntent;
        String targetPkg;
        int requestCode;
        boolean reallyInstalled;
        Runnable pendingRunnable;

        DebounceEntry(Activity activity, Intent intent, String pkg, int code, boolean installed) {
            this.activityRef = new WeakReference<>(activity);
            this.originalIntent = intent;
            this.targetPkg = pkg;
            this.requestCode = code;
            this.reallyInstalled = installed;
        }
    }

    public static volatile long sFakeTimeOffset = 0;
    private Runnable mClearFakeTimeRunnable = new Runnable() {
        @Override
        public void run() {
            if (sFakeTimeOffset != 0) {
                ReaLog.log("time_fake", "清除虚假启动偏移 (原偏移 " + sFakeTimeOffset + "ms)");
            }
            sFakeTimeOffset = 0;
        }
    };

    private final Map<String, DebounceEntry> debounceMap = new ConcurrentHashMap<>();
    private Handler debounceHandler;

    private String generateDebounceKey(Intent intent, String targetPkg) {
        if (!TextUtils.isEmpty(targetPkg)) {
            return targetPkg;
        }
        Uri uri = intent.getData();
        if (uri != null) {
            String scheme = uri.getScheme();
            String host = uri.getHost();
            String path = uri.getPath();
            if (scheme != null && host != null) {
                return scheme + "://" + host + (path != null ? path : "");
            }
            return uri.toString();
        }
        return intent.toUri(0);
    }

private void debounceAndShowDialog(Activity activity, Intent originalIntent, String targetPkg,
        int requestCode, boolean reallyInstalled) {
    final String key = generateDebounceKey(originalIntent, targetPkg);
    if (key == null) {
        showLaunchConfirmDialog(activity, originalIntent, targetPkg, requestCode, reallyInstalled);
        return;
    }
    if (debounceHandler == null) {
        debounceHandler = new Handler(Looper.getMainLooper());
    }
    synchronized (debounceMap) {
        DebounceEntry existing = debounceMap.get(key);
        if (existing != null && existing.pendingRunnable != null) {
            debounceHandler.removeCallbacks(existing.pendingRunnable);
        }
        final DebounceEntry newEntry = new DebounceEntry(activity, originalIntent, targetPkg,
                requestCode, reallyInstalled);
        Runnable task = new Runnable() {
            @Override
            public void run() {
                DebounceEntry removed = debounceMap.remove(key);
                if (removed == newEntry) {
                    Activity act = newEntry.activityRef.get();
                    if (act != null && !act.isFinishing() && !act.isDestroyed()) {
                        showLaunchConfirmDialog(act, newEntry.originalIntent,
                                newEntry.targetPkg, newEntry.requestCode, newEntry.reallyInstalled);
                    }
                }
            }
        };
        newEntry.pendingRunnable = task;
        debounceMap.put(key, newEntry);
        debounceHandler.postDelayed(task, 300);
    }
}

    public JSONObject recordsToJson(Map<String, List<String>> records) {
        JSONObject json = new JSONObject();
        if (records == null) return json;
        for (Map.Entry<String, List<String>> entry : records.entrySet()) {
            JSONArray arr = new JSONArray();
            for (String s : entry.getValue()) {
                arr.put(s);
            }
            try {
                json.put(entry.getKey(), arr);
            } catch (Exception e) {
            }
        }
        return json;
    }

    public Map<String, List<String>> jsonToRecords(JSONObject json) {
        Map<String, List<String>> records = new HashMap<>();
        if (json == null) return records;
        Iterator<String> keys = json.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            try {
                JSONArray arr = json.getJSONArray(key);
                List<String> list = new ArrayList<>();
                for (int i = 0; i < arr.length(); i++) {
                    list.add(arr.getString(i));
                }
                records.put(key, list);
            } catch (Exception e) {
            }
        }
        return records;
    }

    private static volatile boolean bypassExitHook = false;
    public AlertDialog currentDialog;

    public static void setBypassExitHook(boolean bypass) {
        bypassExitHook = bypass;
    }

    private Map<String, String> sessionChoiceMap = new ConcurrentHashMap<>();

    public List<InterceptPattern> getInterceptPatterns() {
        return interceptPatternsMap.getOrDefault(currentTargetApp, new ArrayList<>());
    }

    public boolean removeInterceptPattern(String patternHash) {
        List<InterceptPattern> patterns = interceptPatternsMap.get(currentTargetApp);
        if (patterns != null) {
            Iterator<InterceptPattern> it = patterns.iterator();
            while (it.hasNext()) {
                if (it.next().patternHash.equals(patternHash)) {
                    it.remove();
                    saveConfigToFile();
                    return true;
                }
            }
        }
        return false;
    }

    public void removeAllInterceptPatterns() {
        interceptPatternsMap.put(currentTargetApp, new ArrayList<>());
        saveConfigToFile();
    }

    public void setInterceptPatternUserChoice(String patternHash, String newChoice) {
        List<InterceptPattern> patterns = interceptPatternsMap.get(currentTargetApp);
        if (patterns != null) {
            for (InterceptPattern pattern : patterns) {
                if (pattern.patternHash.equals(patternHash)) {
                    pattern.userChoice = newChoice;
                    pattern.silentIntercept = false;
                    pattern.recentChoices.clear();
                    pattern.choiceCount = 0;
                    sessionChoiceMap.remove(patternHash);
                    saveConfigToFile();
                    break;
                }
            }
        }
    }

    public void addPendingFinishActivity(Activity activity) {
        if (activity != null) {
            pendingFinishSet.add(activity);
        }
    }

    private static final String[] BASE_SYSTEM_PACKAGES = {
            "root", "system", "android", "com.android", "de.robv.android.",
            "com.google.", "com.google.android", "com.google.android.gms",
            "com.google.android.webview", "org.lsposed.", "com.lsposed.",
            "com.topjohnwu.", "io.va.exposed.", "org.meowcat.edxposed."
    };

    private static final String[] VENDOR_PACKAGE_PREFIXES = {
            "com.qualcomm", "com.samsung", "com.huawei", "com.hihonor",
            "com.miui", "com.xiaomi", "com.mi", "com.poco", "com.redmi",
            "com.oppo", "com.oplus", "com.oneplus", "com.realme", "com.heytap",
            "com.coloros", "com.oppo.nearme", "com.oppo.global", "com.realme.global",
            "com.vivo", "com.vivo.global", "com.vivo.browser",
            "com.meizu", "com.flyme", "com.lenovo", "com.zuk", "com.motorola",
            "com.asus", "com.nokia", "com.sony", "com.lg", "com.sharp",
            "com.infinix", "com.tecno", "com.itel", "com.blackshark",
            "cn.nubia", "com.zte", "com.acer", "com.bbk", "com.osp",
            "com.nubia", "com.zte", "com.sonyericsson", "com.yulong", "com.transsion"
    };

    public static final Map<String, Boolean> vendorChoiceMap = new ConcurrentHashMap<>();
    private boolean needVendorDialog = false;

    private boolean isSystemPackage(String packageName) {
        if (mPkgMgr != null) {
            return mPkgMgr.isSystemPackage(packageName);
        }
        return false;
    }

@Override
public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
    ReaLog.log("system", "初始化入口: " + lpparam.packageName);
    try {
        XposedHelpers.findAndHookMethod("android.webkit.WebView", null, "onDraw", android.graphics.Canvas.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                    }
                });
    } catch (Throwable ignored) {
    }
    try {
        XposedHelpers.findAndHookMethod("org.chromium.android_webview.AwContents", null, "destroy",
                new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam param) {
                        return null;
                    }
                });
    } catch (Throwable ignored) {
    }

    try {
        Class<?> bundleClass = Class.forName("android.os.Bundle");
        String[] bundleMethods = {"getString", "getInt", "getBoolean", "getLong", "getDouble", "getFloat", "getBundle", "getSerializable", "getParcelable", "containsKey"};
        for (final String methodName : bundleMethods) {
            try {
                XposedHelpers.findAndHookMethod(bundleClass, methodName, String.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        if (param.thisObject == null) {
                            setDefaultReturnValue(param);
                            return;
                        }
                        try {
                            ((Bundle) param.thisObject).hashCode();
                        } catch (Throwable e) {
                            setDefaultReturnValue(param);
                        }
                    }

                    private void setDefaultReturnValue(MethodHookParam param) {
                        if (methodName.equals("getString")) param.setResult("");
                        else if (methodName.equals("getInt")) param.setResult(0);
                        else if (methodName.equals("getBoolean")) param.setResult(false);
                        else if (methodName.equals("getLong")) param.setResult(0L);
                        else if (methodName.equals("getDouble")) param.setResult(0.0);
                        else if (methodName.equals("getFloat")) param.setResult(0.0f);
                        else if (methodName.equals("getBundle")) param.setResult(new Bundle());
                        else param.setResult(null);
                    }
                });
            } catch (Throwable ignored) {
            }
            try {
                XposedHelpers.findAndHookMethod(bundleClass, methodName, String.class, Object.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        if (param.thisObject == null) {
                            param.setResult(param.args[1]);
                            return;
                        }
                        try {
                            Bundle b = (Bundle) param.thisObject;
                            if (!b.containsKey((String) param.args[0]))
                                param.setResult(param.args[1]);
                        } catch (Throwable e) {
                            param.setResult(param.args[1]);
                        }
                    }
                });
            } catch (Throwable ignored) {
            }
        }
        XposedHelpers.findAndHookMethod(bundleClass, "getString", String.class, String.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (param.thisObject == null) {
                    param.setResult(param.args[1]);
                    return;
                }
                try {
                    Bundle b = (Bundle) param.thisObject;
                    if (!b.containsKey((String) param.args[0])) param.setResult(param.args[1]);
                } catch (Throwable e) {
                    param.setResult(param.args[1]);
                }
            }
        });
    } catch (Throwable ignored) {
    }

    Context appContext = getApplicationContext();

    if (appContext == null) {
        try {
            appContext = (Context) XposedHelpers.getObjectField(lpparam, "appContext");
            if (appContext != null) {
                ReaLog.log("system", "获取Context: 通过 LoadPackageParam 成功");
            }
        } catch (Throwable ignored) {
        }
    }

    if (appContext != null) {
        try {
            ApplicationInfo ai = appContext.getApplicationInfo();
            if (ai != null) {
                XposedHelpers.setObjectField(ai, "nativeLibraryDir", ai.nativeLibraryDir);
            }
        } catch (Throwable ignored) {
        }
    }

    ClassLoader realClassLoader = (appContext != null) ? appContext.getClassLoader() : lpparam.classLoader;
    String realPackageName = (appContext != null) ? appContext.getPackageName() : lpparam.packageName;

    if ("com.install.appinstall.xl".equals(lpparam.packageName)) {
        try {
            XposedHelpers.findAndHookMethod(
                    MainActivity.class.getName(),
                    lpparam.classLoader,
                    "isModuleActivated",
                    XC_MethodReplacement.returnConstant(true)
            );
            ReaLog.log("system", "模块已激活");
        } catch (Throwable t) {
            ReaLog.log("system", "模块激活失败: " + t.getMessage());
        }
        return;
    }

    // 初始化 PkgMgr 和 Binder 代理
    if (mPkgMgr == null) {
        mPkgMgr = new PkgMgr(this);
    }

    // 初始化 DebugModeManager
    DebugModeManager.init(this);

    // Binder层拦截
    if (DebugModeManager.isFeatureEnabled("hook_service_manager")) {
        mPkgMgr.installServiceManagerHook(lpparam.classLoader);
    }

    currentTargetApp = realPackageName;

    // 加载本地配置
    try {
        loadConfigFromFile();
        ReaLog.init(this);
    } catch (Throwable e) {
        createDefaultConfig();
    }

    doHook(realPackageName, realClassLoader, appContext);
}

private void doHook(final String packageName, final ClassLoader classLoader, Context appContext) {

    if (mPrsprn == null) {
        mPrsprn = new Prsprn(this);
    }
    if (mPkgMgr == null) {
        mPkgMgr = new PkgMgr(this);
    }

    DebugModeManager.init(this);
    if (DebugModeManager.isDebugModeActive()) {
        log("调试模式激活状态：" + DebugModeManager.isDebugModeActive());
    }

    boolean moduleEnabled = true;
    boolean isVendor = false;
    for (String vendorPrefix : VENDOR_PACKAGE_PREFIXES) {
        if (packageName.startsWith(vendorPrefix)) {
            isVendor = true;
            break;
        }
    }
    if (isVendor) {
        Boolean choice = vendorChoiceMap.get(packageName);
        if (choice == null) {
            if (mPkgMgr.isRealSystemPackage(packageName)) {
                vendorChoiceMap.put(packageName, false);
                needVendorDialog = true;
                choice = false;
            } else {
                vendorChoiceMap.put(packageName, true);
                needVendorDialog = false;
                choice = true;
            }
        }
        moduleEnabled = choice;
        ReaLog.log("system", "强制包: " + packageName + ", 模块启用: " + moduleEnabled);
    }

    if (DebugModeManager.isFeatureEnabled("hook_dialog_cancelable")) {
        hookDialogCancelableMethods(classLoader);
    }
    if (DebugModeManager.isFeatureEnabled("hook_window_flags")) {
        hookWindowFlags(classLoader);
    }
    if (foatInstance == null) foatInstance = new foat(this);
    foatInstance.initVolumeKeyDoubleClick(classLoader);
    foatInstance.hookActivityLifecycle(classLoader);
    if (DebugModeManager.isFeatureEnabled("hook_base_activity_lifecycle")) {
        hookBaseActivityLifecycle(classLoader);
    }

    if (!moduleEnabled) {
        ReaLog.log("system", "模块已禁用，仅Hook弹窗/截屏");
        log("模块已禁用，仅Hook弹窗/截屏");
        return;
    }

    ReaLog.log("system", "正式开始Hook: " + packageName);
    currentTargetApp = packageName;
    final boolean isFlutterApp = isFlutterApp(classLoader);

    for (String basePkg : BASE_SYSTEM_PACKAGES) {
        if (packageName.startsWith(basePkg)) {
            ReaLog.log("system", "基础系统包，跳过: " + packageName);
            return;
        }
    }

    try {
        if (!installStatusMap.containsKey(packageName)) installStatusMap.put(packageName, true);
        if (!floatingShownMap.containsKey(packageName)) floatingShownMap.put(packageName, true);
        if (!blockExitMap.containsKey(packageName)) blockExitMap.put(packageName, false);
        if (!superBlockExitMap.containsKey(packageName)) superBlockExitMap.put(packageName, false);
        if (!permissionFakeMap.containsKey(packageName)) permissionFakeMap.put(packageName, true);
        if (!userDefinedPackagesMap.containsKey(packageName))
            userDefinedPackagesMap.put(packageName, new ArrayList<String>());
        if (!excludedPackagesMap.containsKey(packageName))
            excludedPackagesMap.put(packageName, new ArrayList<String>());
    } catch (Throwable t) {
        log("初始化配置状态异常: " + t.getMessage());
        ReaLog.log("system", "初始化配置状态异常: " + t.getMessage());
    }

    try {
        if (DebugModeManager.isFeatureEnabled("crash_protect")) {
            applyCrashProtect(crashProtectEnabledMap.getOrDefault(packageName, true), appContext);
        }
        if (DebugModeManager.isFeatureEnabled("hook_exit_methods")) {
            hookExitMethods(classLoader);
        }
        if (DebugModeManager.isFeatureEnabled("hook_indirect_exit")) {
            hookIndirectExitMethods(classLoader);
        }
        if (DebugModeManager.isFeatureEnabled("hook_global_exit_sources")) {
            hookGlobalExitSources(classLoader);
        }
        //立即执行↑

        //极速执行
        new Handler(Looper.getMainLooper()).postAtFrontOfQueue(new Runnable() {
            @Override
            public void run() {
                if (DebugModeManager.isFeatureEnabled("hook_bundle_get_string")) {
                    hookBundleGetString(classLoader);
                }
                if (DebugModeManager.isFeatureEnabled("hook_bundle_empty")) {
                    hookBundleEmptyInstance(classLoader);
                }
                if (DebugModeManager.isFeatureEnabled("hook_so_path_fix")) {
                    hookSoPathInApp(classLoader);
                }
                if (DebugModeManager.isFeatureEnabled("hook_system_file_read")) {
                    hookSystemFileRead();
                }
                if (DebugModeManager.isFeatureEnabled("hook_package_manager_reflect")) {
                    hookPackageManagerReflect();
                }

                //包管理配置
                mPkgMgr.preloadPackageInfoCache();

                if (DebugModeManager.isFeatureEnabled("hook_get_package_info")) {
                    mPkgMgr.hookGetPackageInfo(classLoader);
                }
                if (DebugModeManager.isFeatureEnabled("hook_get_application_info")) {
                    mPkgMgr.hookGetApplicationInfo(classLoader);
                }
                if (DebugModeManager.isFeatureEnabled("hook_get_installed_packages")) {
                    mPkgMgr.hookGetInstalledPackages(classLoader);
                }
                if (DebugModeManager.isFeatureEnabled("hook_get_installed_applications")) {
                    mPkgMgr.hookGetInstalledApplications(classLoader);
                }

                if (DebugModeManager.isFeatureEnabled("hook_get_package_info_as_user")) {
                    mPkgMgr.hookGetPackageInfoAsUser(classLoader);
                }
                if (DebugModeManager.isFeatureEnabled("hook_installed_packages_as_user")) {
                    mPkgMgr.hookGetInstalledPackagesAsUser(classLoader);
                }
                if (DebugModeManager.isFeatureEnabled("hook_installed_applications_as_user")) {
                    mPkgMgr.hookGetInstalledApplicationsAsUser(classLoader);
                }
                if (DebugModeManager.isFeatureEnabled("hook_package_installer")) {
                    mPkgMgr.hookPackageInstaller(classLoader);
                    mPkgMgr.installExtraCaptureHooks(classLoader);
                }
                if (DebugModeManager.isFeatureEnabled("hook_is_application_enabled")) {
                    mPkgMgr.hookIsApplicationEnabled(classLoader);
                }
                if (DebugModeManager.isFeatureEnabled("hook_get_activity_info")) {
                    mPkgMgr.hookGetActivityInfo(classLoader);
                }
                if (DebugModeManager.isFeatureEnabled("hook_get_installer_package_name")) {
                    mPkgMgr.hookGetInstallerPackageName(classLoader);
                }
                if (DebugModeManager.isFeatureEnabled("hook_Desktop_App_Query")) {
                    hookDesktopAppQuery(classLoader);
                }

                //权限配置
                if (DebugModeManager.isFeatureEnabled("hook_check_permission")) {
                    mPrsprn.hookCheckPermission(classLoader);
                }
                if (DebugModeManager.isFeatureEnabled("hook_permission_request")) {
                    mPrsprn.hookPermissionRequest(classLoader);
                }
                if (DebugModeManager.isFeatureEnabled("hook_query_all_packages")) {
                    mPrsprn.hookQueryAllPackagesPermission(classLoader);
                }
                if (DebugModeManager.isFeatureEnabled("hook_package_usage_stats")) {
                    mPrsprn.hookPackageUsageStatsPermission(classLoader);
                }
                if (DebugModeManager.isFeatureEnabled("hook_app_ops")) {
                    mPrsprn.hookAppOpsForAllPermissions(classLoader);
                }
                if (DebugModeManager.isFeatureEnabled("hook_overlay_app_ops")) {
                    mPrsprn.hookOverlayAppOps(classLoader);
                }
                if (DebugModeManager.isFeatureEnabled("hook_write_settings")) {
                    mPrsprn.hookWriteSettingsPermission(classLoader);
                }
                if (DebugModeManager.isFeatureEnabled("hook_special_system_apis")) {
                    mPrsprn.hookSpecialSystemApis(classLoader);
                }

                //痕迹检测/网络代理
                AntiDetection.initForEmbed(classLoader, packageName, HookInit.this);
                VpnStatusFaker.installForEmbed(classLoader, HookInit.this, packageName);
            }
        });

        //普通执行
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                //权限配置
                if (DebugModeManager.isFeatureEnabled("hook_account_manager")) {
                    mPrsprn.hookAccountManager(classLoader);
                }
                if (DebugModeManager.isFeatureEnabled("hook_Content_Resolver_Query")) {
                    mPrsprn.hookContentResolverQuery(classLoader);
                }
                if (DebugModeManager.isFeatureEnabled("hook_Telephony_Getters")) {
                    mPrsprn.hookTelephonyGetters(classLoader);
                }
                if (DebugModeManager.isFeatureEnabled("hook_accessibility")) {
                    mPrsprn.hookAccessibilityManager(classLoader);
                }
                if (DebugModeManager.isFeatureEnabled("hook_app_widget")) {
                    mPrsprn.hookAppWidgetManager(classLoader);
                }
                if (DebugModeManager.isFeatureEnabled("hook_shortcut_widget")) {
                    mPrsprn.hookShortcutAndWidgetFake(classLoader);
                }
                if (DebugModeManager.isFeatureEnabled("hook_settings_provider")) {
                    mPrsprn.hookSettingsProvider(classLoader);
                }

                if (DebugModeManager.isFeatureEnabled("hook_system_properties")) {
                    hookSystemProperties(classLoader);
                }
                if (DebugModeManager.isFeatureEnabled("hook_start_activity")) {
                    hookStartActivity(classLoader);
                }
                if (DebugModeManager.isFeatureEnabled("hook_query_intent_activities")) {
                    hookQueryIntentActivities(classLoader);
                }
                if (DebugModeManager.isFeatureEnabled("hook_resolve_activity")) {
                    hookResolveActivity(classLoader);
                }
                if (DebugModeManager.isFeatureEnabled("hook_query_intent_services")) {
                    hookQueryIntentServices(classLoader);
                }
                if (DebugModeManager.isFeatureEnabled("hook_query_broadcast_receivers")) {
                    hookQueryBroadcastReceivers(classLoader);
                }

                if (DebugModeManager.isFeatureEnabled("hook_reflect_invoke")) {
                    mPkgMgr.hookReflectInstallCheck(classLoader);
                }
                if (DebugModeManager.isFeatureEnabled("hook_okhttp")) {
                    mPkgMgr.hookOkHttp(classLoader);
                }
                if (DebugModeManager.isFeatureEnabled("hook_runtime_exec")) {
                    mPkgMgr.hookRuntimeExecMethods(classLoader);
                }
                if (DebugModeManager.isFeatureEnabled("hook_process_builder")) {
                    mPkgMgr.hookProcessBuilder(classLoader);
                }
                if (DebugModeManager.isFeatureEnabled("hook_file_exists")) {
                    mPkgMgr.hookFileSystemInstallCheck(classLoader);
                }
                if (DebugModeManager.isFeatureEnabled("hook_data_app_listing")) {
                    mPkgMgr.hookDataAppDirectoryListing(classLoader);
                }
                if (DebugModeManager.isFeatureEnabled("hook_lib_directory")) {
                    mPkgMgr.hookLibDirectoryChecks(classLoader);
                }
                if (DebugModeManager.isFeatureEnabled("hook_os_stat")) {
                    mPkgMgr.hookOsLibcoreStat(classLoader);
                }
                if (DebugModeManager.isFeatureEnabled("hook_os_readdir")) {
                    mPkgMgr.hookOsLibcoreReaddir(classLoader);
                }
                if (DebugModeManager.isFeatureEnabled("hook_file_length")) {
                    mPkgMgr.hookFileLength(classLoader);
                }
                if (DebugModeManager.isFeatureEnabled("hook_proc_files")) {
                    mPkgMgr.hookProcFileSystem(classLoader);
                }
                if (DebugModeManager.isFeatureEnabled("hook_process_memory")) {
                    mPkgMgr.hookProcessMemoryInfo(classLoader);
                }
                if (DebugModeManager.isFeatureEnabled("hook_random_access_file")) {
                    mPkgMgr.hookRandomAccessFile(classLoader);
                }

                if (DebugModeManager.isFeatureEnabled("selinux_fake")) {
                    Selinuxhook.setEnabled(packageName, selinuxFakeMap.getOrDefault(packageName, true));
                    Selinuxhook.installHooks(classLoader);
                }
            }
        });

        //延迟执行
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (DebugModeManager.isFeatureEnabled("share_fake")) {
                    final Map<String, Boolean> shareMap = shareFakeDetailMap.getOrDefault(packageName, getDefaultShareDetailMap());
                    try {
                        ShareHook.hookWeChatShareOnly(classLoader, packageName, new ShareHook.BooleanProvider() {
                            @Override
                            public boolean get() {
                                return shareMap.getOrDefault("wechat", false);
                            }
                        });
                    } catch (Throwable t) {
                    }
                    try {
                        ShareHook.hookQQShareOnly(classLoader, new ShareHook.BooleanProvider() {
                            @Override
                            public boolean get() {
                                return shareMap.getOrDefault("qq", false);
                            }
                        });
                    } catch (Throwable t) {
                    }
                    try {
                        ShareHook.hookDingTalkShareOnly(classLoader, new ShareHook.BooleanProvider() {
                            @Override
                            public boolean get() {
                                return shareMap.getOrDefault("dingtalk", false);
                            }
                        });
                    } catch (Throwable t) {
                    }
                    try {
                        ShareHook.hookWeiboShareOnly(classLoader, new ShareHook.BooleanProvider() {
                            @Override
                            public boolean get() {
                                return shareMap.getOrDefault("weibo", false);
                            }
                        });
                    } catch (Throwable t) {
                    }
                }

                if (DebugModeManager.isFeatureEnabled("hook_time_fake")) {
                    installTimeFakeHooks(classLoader);
                }

                if (DebugModeManager.isFeatureEnabled("force_default_back")) {
                    hookActivityDefaultBack(classLoader);
                }

                if (isFlutterApp) {
                    mPkgMgr.installFlutterHooks(classLoader);
                }
            }
        }, 1000L);

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                maybeEnableBlockExit();
            }
        }, 2000L);

    } catch (Throwable t) {
        log("❌ Hook执行失败: " + t.getMessage());
        ReaLog.log("system", "全部Hook异常: " + t.getMessage());
        t.printStackTrace();
    }
}



    private boolean isFlutterApp(ClassLoader classLoader) {
        try {
            Class<?> flutterClass = Class.forName(
                    "io.flutter.embedding.engine.FlutterJNI",
                    false,
                    classLoader
            );
            return flutterClass != null;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
    
    private void hookBaseActivityLifecycle(ClassLoader classLoader) {
        try {
            XposedHelpers.findAndHookMethod(
                    "android.app.Activity",
                    classLoader,
                    "onWindowFocusChanged",
                    boolean.class,
                    new XC_MethodHook() {
                        private boolean shown = false;

                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            boolean hasFocus = (boolean) param.args[0];
                            if (!hasFocus) return;
                            if (shown) return;
                            if (!needVendorDialog) return;

                            final Activity activity = (Activity) param.thisObject;
                            if (activity == null || activity.isFinishing()) return;

                            final String pkg = currentTargetApp;
                            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (!needVendorDialog) return;
                                    if (activity.isFinishing() || activity.isDestroyed()) return;
                                    ReaLog.log("system", "显示强制包询问对话框: " + pkg);
                                    showVendorDialog(activity, pkg);
                                }
                            }, 1300);

                            shown = true;
                        }
                    }
            );
        } catch (Throwable t) {
            log("强制包询问对话框失败: " + t.getMessage());
        }
    }

    private void showVendorDialog(final Activity activity, final String packageName) {
        ReaLog.log("system", "显示不推荐包询问弹窗: " + packageName);
        if (activity == null || activity.isFinishing()) {
            ReaLog.log("system", "showVendorDialog: Activity无效");
            return;
        }
        if (!needVendorDialog) {
            ReaLog.log("system", "不推荐包已为false，退出");
            return;
        }
        needVendorDialog = false;
        String message = "<font color='#FF5722'><b>检测到当前应用</b></font><br>" +
                "可能是系统/不推荐启用的应用：<br><b><font color='#FF5722'>" + packageName + "</font></b><br><br>" +
                "是否强制启用\"安装防护模块\"？<br>" +
                "启用后可能导致当前应用出现异常问题，请谨慎选择！<br><br>";

        AlertDialog dialog = createBoundedDialog(
                activity,
                "系统提示",
                message,
                new String[]{"强制开启", "不启用"},
                new DialogInterface.OnClickListener[]{
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ReaLog.log("system", "不推荐包选择: 强制开启 " + packageName);
                                vendorChoiceMap.put(packageName, true);
                                mIsSystemPackageCache = null;
                                saveConfigImmediate();
                                ToastUtil.showUnique(activity, "✅ 已强制启用,应用即将停止...\n请重新启动应用！");
                                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        restartApplication(activity);
                                    }
                                }, 1500);
                            }
                        },
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ReaLog.log("system", "不推荐包选择: 不启用 " + packageName);
                                vendorChoiceMap.put(packageName, false);
                                mIsSystemPackageCache = null;
                                saveConfigImmediate();
                                ToastUtil.showUnique(activity, "❌ 已禁用模块");
                            }
                        }
                }
        );
        dialog.setCanceledOnTouchOutside(true);
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                ReaLog.log("system", "不推荐包选择: 取消弹窗，默认不启用 " + packageName);
                vendorChoiceMap.put(packageName, false);
                mIsSystemPackageCache = null;
                saveConfigImmediate();
            }
        });
        dialog.show();
        ReaLog.log("system", "启用包询问弹窗已显示");
    }

    private String getCurrentProcessName() {
        if (sCachedProcessName != null) {
            return sCachedProcessName;
        }
        try {
            sCachedProcessName = (String) XposedHelpers.callStaticMethod(
                    XposedHelpers.findClass("android.app.ActivityThread", null),
                    "currentProcessName"
            );
            if (sCachedProcessName != null && !sCachedProcessName.isEmpty()) {
                ReaLog.log("system", "获取进程名: " + sCachedProcessName);
                return sCachedProcessName;
            }
        } catch (Throwable e) {
        }
        try {
            Context ctx = (Context) XposedHelpers.callStaticMethod(
                    XposedHelpers.findClass("android.app.ActivityThread", null),
                    "currentApplication"
            );
            if (ctx != null) {
                sCachedProcessName = ctx.getPackageName();
                ReaLog.log("system", "通过Context获取进程名: " + sCachedProcessName);
                return sCachedProcessName;
            }
        } catch (Throwable e) {
        }
        sCachedProcessName = "";
        return sCachedProcessName;
    }

    public void restartApplication(Context context) {
        ReaLog.log("system", "开始重启应用");
        saveConfigToFile();

        try {
            Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
            if (launchIntent == null) {
                ToastUtil.showUnique(context, "无法自动重启，请手动结束应用");
                ReaLog.log("system", "重启失败: 无法获取启动Intent");
                Process.killProcess(Process.myPid());
                System.exit(0);
                return;
            }

            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            int pendingFlags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                pendingFlags |= PendingIntent.FLAG_IMMUTABLE;
            }
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, launchIntent, pendingFlags);

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            long triggerTime = System.currentTimeMillis() + 200;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            }

            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    Process.killProcess(Process.myPid());
                    System.exit(0);
                    ReaLog.log("system", "进程已终止");
                }
            }, 150);

            ReaLog.log("system", "重启任务已提交");
        } catch (Throwable e) {
            log("重启应用失败: " + e.getMessage());
            ReaLog.log("system", "重启应用失败: " + e.getMessage());
            ToastUtil.showUnique(context, "自动重启失败，请手动结束应用");
            Process.killProcess(Process.myPid());
            System.exit(0);
        }
    }

    public void showRestartConfirmDialog(final Activity activity) {
        ReaLog.log("system", "显示重启确认弹窗");
        if (activity == null || activity.isFinishing()) {
            ReaLog.log("system", "showRestartConfirmDialog: Activity无效");
            return;
        }

        String message = "<font color='#FF5722'><b>检测设置已保存</b></font>，" +
                "需 <font color='#2196F3'><b>重启应用</b></font> 才生效。<br><br>" +
                "<font color='#2196F3'><b>若重启失败，请手动启动应用</b></font>";

        AlertDialog dialog = createBoundedDialog(
                activity,
                "系统提示",
                message,
                new String[]{"立即重启", "稍后重启"},
                new DialogInterface.OnClickListener[]{
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface d, int which) {
                                ReaLog.log("system", "用户选择立即重启");
                                d.dismiss();
                                ToastUtil.showUnique(activity, "✅ 应用即将停止...\n请重新启动应用！");
                                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        restartApplication(activity);
                                    }
                                }, 1500);
                            }
                        },
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface d, int which) {
                                ReaLog.log("system", "用户选择稍后重启");
                                d.dismiss();
                                ToastUtil.showUnique(activity, "稍后手动重启");
                            }
                        }
                }
        );
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    public void log(String message) {
        String currentProcess = sCachedProcessName;
        if (currentProcess == null) {
            currentProcess = getCurrentProcessName();
        }
        boolean isMain = currentTargetApp != null && currentTargetApp.equals(currentProcess);

        if (message.contains("❌") || message.contains("错误:")) {
            try {
                ReaLog.log("other", "XP日志：[" + currentTargetApp + "]" + message);
                XposedBridge.log("[" + MODULE_TAG + "] [" + currentTargetApp + "] " + message);
            } catch (Throwable e) {
                android.util.Log.d(MODULE_TAG, "[" + currentTargetApp + "] " + message);
            }
            return;
        }

        if (!isMain) {
            return;
        }

        Long lastTime = sLastLogTime.get(message);
        long now = System.currentTimeMillis();
        if (lastTime != null && (now - lastTime) < LOG_DEDUP_INTERVAL_MS) {
            return;
        }
        sLastLogTime.put(message, now);

        if (sLastLogTime.size() > MAX_LOG_CACHE_SIZE) {
            sLastLogTime.clear();
        }

        try {
            ReaLog.log("other", "XP日志：[" + currentTargetApp + "]" + message);
            XposedBridge.log("[" + MODULE_TAG + "] [" + currentTargetApp + "] " + message);
        } catch (Throwable e) {
            android.util.Log.d(MODULE_TAG, "[" + currentTargetApp + "] " + message);
        }
    }

    private boolean isViewInReaLogDialog(View view) {
        if (view == null) return false;
        if ("rea_log_dialog".equals(view.getTag())) {
            return true;
        }
        ViewParent parent = view.getParent();
        while (parent != null) {
            if (parent instanceof View) {
                Object tag = ((View) parent).getTag();
                if ("rea_log_dialog".equals(tag)) {
                    return true;
                }
            }
            parent = parent.getParent();
        }
        return false;
    }

    private List<String> jsonArrayToStringList(JSONArray array) {
        List<String> list = new ArrayList<>();
        if (array != null) {
            for (int i = 0; i < array.length(); i++) {
                try {
                    String item = array.getString(i);
                    if (item != null && !item.isEmpty()) {
                        list.add(item);
                    }
                } catch (Exception e) {
                    try {
                        Object obj = array.get(i);
                        if (obj != null) {
                            list.add(obj.toString());
                        }
                    } catch (Exception ex) {
                    }
                }
            }
        }
        return list;
    }

    private JSONArray stringListToJsonArray(Collection<String> collection) {
        JSONArray array = new JSONArray();
        if (collection != null) {
            for (String item : collection) {
                if (item != null && !item.isEmpty()) {
                    array.put(item);
                }
            }
        }
        return array;
    }

    private JSONObject readJsonFromFile(File file) throws Exception {
        FileInputStream fis = new FileInputStream(file);
        BufferedReader reader = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        fis.close();
        return new JSONObject(sb.toString());
    }

    private void writeJsonToFile(File file, JSONObject root) throws Exception {
        OutputStream os = new FileOutputStream(file);
        OutputStreamWriter writer = new OutputStreamWriter(os, StandardCharsets.UTF_8);
        writer.write(root.toString(2));
        writer.flush();
        writer.close();
        os.close();
    }

    private void createDefaultConfig() {
        try {
            permissionFakeDetailMap.put(currentTargetApp, new HashMap<String, Boolean>());
            installStatusMap.put(currentTargetApp, true);
            floatingShownMap.put(currentTargetApp, true);
            permanentHiddenMap.put(currentTargetApp, false);
            blockExitMap.put(currentTargetApp, false);
            superBlockExitMap.put(currentTargetApp, false);
            permissionFakeMap.put(currentTargetApp, true);
            launchInterceptMap.put(currentTargetApp, true);
            selinuxFakeMap.put(currentTargetApp, true);
            shareFakeDetailMap.put(currentTargetApp, getDefaultShareDetailMap());
            forceDefaultBackMap.put(currentTargetApp, false);
            crashProtectEnabledMap.put(currentTargetApp, true);
            interceptPatternsMap.put(currentTargetApp, new ArrayList<InterceptPattern>());
            userDefinedPackagesMap.put(currentTargetApp, new ArrayList<String>());
            excludedPackagesMap.put(currentTargetApp, new ArrayList<String>());
            if (!packageConfigMap.containsKey(currentTargetApp)) {
                packageConfigMap.put(currentTargetApp, new ArrayList<PackageConfig>());
            }
            vpnFakeDetailMap.put(currentTargetApp, getDefaultVpnDetailMap());
            antiDetectionDetailMap.put(currentTargetApp, getDefaultAntiDetectionMap());
            reaLogPauseMap.put(currentTargetApp, new ConcurrentHashMap<>());

            ReaLog.log("config", "默认配置创建完成: " + currentTargetApp);
            log("✅ 创建默认配置完成");
        } catch (Throwable t) {
            log("❌ 创建默认配置异常: " + t.getMessage());
            ReaLog.log("config", "创建默认配置异常: " + t.getMessage());
        }
    }

    private File findConfigFile(String[] dirPaths, String currentPkg) {
        for (String dirPath : dirPaths) {
            File dir = new File(dirPath);
            if (!dir.exists() || !dir.isDirectory()) continue;

            File newFile = new File(dir, "installcf_" + currentPkg + ".json");
            if (newFile.exists() && newFile.length() > 0) {
                return newFile;
            }

            File oldFile = new File(dir, "install_fake_config.json");
            if (oldFile.exists() && oldFile.length() > 0) {
                try {
                    JSONObject root = readJsonFromFile(oldFile);
                    if (root.has(currentPkg)) {
                        writeJsonToFile(newFile, root);
                        final File f = oldFile;
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(3000);
                                    f.delete();
                                } catch (Exception ignored) {
                                }
                            }
                        }).start();
                        return newFile;
                    }
                } catch (Exception e) {
                    log("迁移旧配置失败: " + e.getMessage());
                    ReaLog.log("config", "迁移旧配置失败: " + e.getMessage());
                }
            }
        }
        return null;
    }

    public void loadConfigFromFile() {
        try {
            String currentPkg = currentTargetApp;
            File configFile = null;
            String foundPath = null;

            String[] privatePaths = {
                    "/data/data/" + currentPkg + "/files/",
                    "/data/user/0/" + currentPkg + "/files/"
            };
            configFile = findConfigFile(privatePaths, currentPkg);

            if (configFile == null) {
                String[] externalPaths = {
                        "/storage/emulated/0/Android/data/" + currentPkg + "/files/"
                };
                configFile = findConfigFile(externalPaths, currentPkg);
            }

            if (configFile == null) {
                String[] allPaths = {
                        "/data/data/" + currentPkg + "/files/",
                        "/data/user/0/" + currentPkg + "/files/",
                        "/storage/emulated/0/Android/data/" + currentPkg + "/files/"
                };
                for (String dirPath : allPaths) {
                    File dir = new File(dirPath);
                    if (!dir.exists() || !dir.isDirectory()) continue;
                    File[] files = dir.listFiles(new FilenameFilter() {
                        @Override
                        public boolean accept(File d, String name) {
                            return name.startsWith("installcf_") && name.endsWith(".json");
                        }
                    });
                    if (files != null && files.length > 0) {
                        Arrays.sort(files, new Comparator<File>() {
                            @Override
                            public int compare(File f1, File f2) {
                                return Long.compare(f2.lastModified(), f1.lastModified());
                            }
                        });
                        configFile = files[0];
                        foundPath = configFile.getAbsolutePath();
                        log("使用备份配置文件: " + foundPath);
                        ReaLog.log("config", "使用备份配置文件: " + foundPath);
                        break;
                    }
                }
            }

            if (configFile != null && configFile.exists()) {
                foundPath = configFile.getAbsolutePath();
                try {
                    JSONObject configJson = readJsonFromFile(configFile);
                    if (configJson.has(currentTargetApp)) {
                        JSONObject appConfig = configJson.getJSONObject(currentTargetApp);

                        installStatusMap.put(currentTargetApp, appConfig.optBoolean("install_status", true));
                        floatingShownMap.put(currentTargetApp, appConfig.optBoolean("floating_shown", true));
                        permanentHiddenMap.put(currentTargetApp, appConfig.optBoolean("permanent_hidden", false));
                        String xStr = appConfig.optString("floating_x", "null");
                        if (!"null".equals(xStr) && !xStr.isEmpty()) {
                            try {
                                floatingXMap.put(currentTargetApp, Float.parseFloat(xStr));
                            } catch (NumberFormatException ignored) {
                            }
                        }
                        String yStr = appConfig.optString("floating_y", "null");
                        if (!"null".equals(yStr) && !yStr.isEmpty()) {
                            try {
                                floatingYMap.put(currentTargetApp, Float.parseFloat(yStr));
                            } catch (NumberFormatException ignored) {
                            }
                        }
                        blockExitMap.put(currentTargetApp, appConfig.optBoolean("block_exit", false));
                        superBlockExitMap.put(currentTargetApp, appConfig.optBoolean("super_block_exit", false));
                        userDisabledAutoBlockMap.put(currentTargetApp, appConfig.optBoolean("user_disabled_auto_block", false));
                        permissionFakeMap.put(currentTargetApp, appConfig.optBoolean("permission_fake", true));
                        launchInterceptMap.put(currentTargetApp, appConfig.optBoolean("launch_intercept", true));
                        if (appConfig.has("vendor_enabled")) {
                            boolean vendorEnabled = appConfig.getBoolean("vendor_enabled");
                            vendorChoiceMap.put(currentTargetApp, vendorEnabled);
                        }
                        selinuxFakeMap.put(currentTargetApp, appConfig.optBoolean("selinux_fake", true));
                        forceDefaultBackMap.put(currentTargetApp, appConfig.optBoolean("force_default_back", false));
                        crashProtectEnabledMap.put(currentTargetApp, appConfig.optBoolean("crash_protect_enabled", true));

                        JSONArray userPackagesArray = appConfig.optJSONArray("user_defined_packages");
                        List<String> userPackages = userPackagesArray != null ? jsonArrayToStringList(userPackagesArray) : new ArrayList<String>();
                        userDefinedPackagesMap.put(currentTargetApp, userPackages);

                        JSONArray excludedArray = appConfig.optJSONArray("excluded_packages");
                        List<String> excludedPackages = excludedArray != null ? jsonArrayToStringList(excludedArray) : new ArrayList<String>();
                        excludedPackagesMap.put(currentTargetApp, excludedPackages);

                        JSONArray pkgConfigArray = appConfig.optJSONArray("package_configs");
                        List<PackageConfig> configs = new ArrayList<PackageConfig>();
                        if (pkgConfigArray != null) {
                            for (int i = 0; i < pkgConfigArray.length(); i++) {
                                JSONObject obj = pkgConfigArray.getJSONObject(i);
                                PackageConfig cfg = new PackageConfig(obj.getString("packageName"));
                                cfg.statusMode = obj.optString("statusMode", "follow");
                                configs.add(cfg);
                            }
                        }
                        packageConfigMap.put(currentTargetApp, configs);

                        JSONArray patternsArray = appConfig.optJSONArray("intercept_patterns");
                        List<InterceptPattern> patterns = new ArrayList<InterceptPattern>();
                        if (patternsArray != null) {
                            for (int i = 0; i < patternsArray.length(); i++) {
                                JSONObject obj = patternsArray.getJSONObject(i);
                                InterceptPattern pattern = new InterceptPattern(
                                        obj.getString("pattern_hash"),
                                        jsonArrayToStringList(obj.getJSONArray("installed_packages")),
                                        jsonArrayToStringList(obj.getJSONArray("not_installed_packages"))
                                );
                                pattern.userChoice = obj.optString("user_choice", "");
                                pattern.choiceCount = obj.optInt("choice_count", 0);
                                pattern.lastDetectedTime = obj.optLong("last_detected_time", System.currentTimeMillis());
                                pattern.silentIntercept = obj.optBoolean("silent_intercept", false);
                                patterns.add(pattern);
                            }
                        }
                        interceptPatternsMap.put(currentTargetApp, patterns);

                        JSONArray globalArray = appConfig.optJSONArray("global_captured_packages");
                        globalCapturedPackages.clear();
                        if (globalArray != null) {
                            for (int i = 0; i < globalArray.length(); i++) {
                                String pkg = globalArray.getString(i);
                                globalCapturedPackages.add(pkg);
                            }
                        }

                        JSONObject actionsJson = appConfig.optJSONObject("auto_actions");
                        Map<String, String> actions = new HashMap<String, String>();
                        if (actionsJson != null) {
                            Iterator<String> keys = actionsJson.keys();
                            while (keys.hasNext()) {
                                String key = keys.next();
                                actions.put(key, actionsJson.getString(key));
                            }
                        }
                        autoActionMap.put(currentTargetApp, actions);

                        JSONObject recordsJson = appConfig.optJSONObject("auto_records");
                        Map<String, List<String>> records = new HashMap<String, List<String>>();
                        if (recordsJson != null) {
                            Iterator<String> keys = recordsJson.keys();
                            while (keys.hasNext()) {
                                String key = keys.next();
                                JSONArray arr = recordsJson.getJSONArray(key);
                                List<String> list = new ArrayList<String>();
                                for (int i = 0; i < arr.length(); i++) {
                                    list.add(arr.getString(i));
                                }
                                records.put(key, list);
                            }
                        }
                        autoChoiceRecordsMap.put(currentTargetApp, records);

                        JSONObject permDetailJson = appConfig.optJSONObject("permission_fake_detail");
                        Map<String, Boolean> permDetail = new HashMap<String, Boolean>();
                        if (permDetailJson != null) {
                            Iterator<String> keys = permDetailJson.keys();
                            while (keys.hasNext()) {
                                String key = keys.next();
                                permDetail.put(key, permDetailJson.getBoolean(key));
                            }
                        }
                        permissionFakeDetailMap.put(currentTargetApp, permDetail);

                        JSONObject antiDetailJson = appConfig.optJSONObject("anti_detection_detail");
                        Map<String, Boolean> antiDetail = new HashMap<String, Boolean>();
                        if (antiDetailJson != null) {
                            Iterator<String> keys = antiDetailJson.keys();
                            while (keys.hasNext()) {
                                String key = keys.next();
                                antiDetail.put(key, antiDetailJson.getBoolean(key));
                            }
                        }
                        antiDetectionDetailMap.put(currentTargetApp, antiDetail);

                        JSONObject vpnDetailJson = appConfig.optJSONObject("vpn_fake_detail");
                        Map<String, Boolean> vpnDetail = new HashMap<String, Boolean>();
                        if (vpnDetailJson != null) {
                            Iterator<String> keys = vpnDetailJson.keys();
                            while (keys.hasNext()) {
                                String key = keys.next();
                                vpnDetail.put(key, vpnDetailJson.getBoolean(key));
                            }
                        }
                        vpnFakeDetailMap.put(currentTargetApp, vpnDetail);

                        JSONObject shareDetailJson = appConfig.optJSONObject("share_fake_detail");
                        Map<String, Boolean> shareDetail = new HashMap<String, Boolean>();
                        if (shareDetailJson != null) {
                            Iterator<String> keys = shareDetailJson.keys();
                            while (keys.hasNext()) {
                                String key = keys.next();
                                shareDetail.put(key, shareDetailJson.getBoolean(key));
                            }
                        }
                        shareFakeDetailMap.put(currentTargetApp, shareDetail);

                        JSONObject pauseJson = appConfig.optJSONObject("rea_log_pause");
                        Map<String, Boolean> pauseMap = new ConcurrentHashMap<>();
                        if (pauseJson != null) {
                            Iterator<String> keys = pauseJson.keys();
                            while (keys.hasNext()) {
                                String key = keys.next();
                                pauseMap.put(key, pauseJson.getBoolean(key));
                            }
                        }
                        reaLogPauseMap.put(currentTargetApp, pauseMap);

                        globalCapturedPackages.addAll(userPackages);
                        if (mPkgMgr != null) {
                            mPkgMgr.invalidateAppDirCache();
                        }

                        if (!sConfigLoadedLogged) {
                            log("配置加载完成 " + foundPath);
                            ReaLog.log("config", "配置加载完成 " + foundPath);
                            sConfigLoadedLogged = true;
                        }
                    } else {
                        log("配置文件中无当前应用数据，创建默认配置");
                        ReaLog.log("config", "配置文件中无当前应用数据，创建默认配置");
                        createDefaultConfig();
                    }
                } catch (Throwable e) {
                    log("读取配置文件异常: " + e.getMessage());
                    ReaLog.log("config", "读取配置文件异常: " + e.getMessage());
                    createDefaultConfig();
                }
            } else {
                log("未找到配置文件，创建默认配置");
                ReaLog.log("config", "未找到配置文件，创建默认配置");
                createDefaultConfig();
            }
        } catch (Throwable t) {
            log("加载配置异常: " + t.getMessage());
            ReaLog.log("config", "加载配置异常: " + t.getMessage());
            createDefaultConfig();
        }
        ReaLog.reloadPauseState();

        int removedCount = 0;
        synchronized (globalCapturedPackages) {
            Iterator<String> it = globalCapturedPackages.iterator();
            while (it.hasNext()) {
                String pkg = it.next();
                if (isBaseSystemPackage(pkg) || isVendorPackage(pkg) ||
                        (isSystemCorePackage(pkg) && isRealSystemPackage(pkg))) {
                    it.remove();
                    removedCount++;
                    ReaLog.log("config", "清理残留系统包: " + pkg);
                }
            }
        }
        if (removedCount > 0) {
            doSaveConfigToFile();
            ReaLog.log("config", "已清理 " + removedCount + " 个残留系统包并保存");
        }
        
// ========== 强制重新刷新调试模式状态 ==========
DebugModeManager.init(this);
// ========== 统一处理悬浮窗隐藏与调试模式 ==========
boolean isPermanentHidden = permanentHiddenMap.getOrDefault(currentTargetApp, false);
boolean isTemporaryHidden = !floatingShownMap.getOrDefault(currentTargetApp, true);
boolean isHidden = isPermanentHidden || isTemporaryHidden;

// 1. 若调试模式激活且悬浮窗隐藏 → 强制关闭调试模式
if (DebugModeManager.isDebugModeActive() && isHidden) {
    DebugModeManager.disableDebugMode();
    String msg = isPermanentHidden ? "检测到永久隐藏，调试模式强制关闭" : "检测到临时隐藏，调试模式强制关闭";
    showToastDelayed(msg);
}

// 2. 自动恢复临时隐藏（永久隐藏不恢复）
if (!isPermanentHidden && isTemporaryHidden) {
    floatingShownMap.put(currentTargetApp, true);
    saveConfigToFile(); // 持久化

    if (DebugModeManager.isDebugModeActive()) {
        DebugModeManager.disableDebugMode();
        showToastDelayed("临时隐藏已恢复显示并关闭调试模式");
    } else {
        showToastDelayed("临时隐藏已自动恢复显示");
    }
}
        mSystemCoreCache.clear();
        packageStatusCache.clear();
        needVendorDialog = false;
        // 重置悬浮窗收边状态（导入/重启后展开）
if (foatInstance != null) {
    foatInstance.resetCollapsedState();
}
    }
    
    private void showToastDelayed(final String msg) {
    final Context ctx = getApplicationContext();
    if (ctx != null) {
        ToastUtil.showUnique(ctx, msg);
    } else {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                Context ctx2 = getApplicationContext();
                if (ctx2 != null) {
                    ToastUtil.showUnique(ctx2, msg);
                }
            }
        }, 500);
    }
}

    public void saveConfigImmediate() {
        doSaveConfigToFile();
    }

    public void saveConfigToFile() {
        synchronized (sSaveExecutor) {
            if (sSaveFuture != null) {
                sSaveFuture.cancel(false);
            }
            sSaveFuture = sSaveExecutor.schedule(new Runnable() {
                @Override
                public void run() {
                    try {
                        doSaveConfigToFile();
                    } catch (Throwable t) {
                        log("异步保存异常: " + t.getMessage());
                        ReaLog.log("config", "异步保存异常: " + t.getMessage());
                    }
                }
            }, SAVE_DELAY_MS, TimeUnit.MILLISECONDS);
        }
    }

    private synchronized void doSaveConfigToFile() {
        if (currentTargetApp == null || currentTargetApp.isEmpty()) {
            log("配置保存失败：当前目标应用为空");
            return;
        }

        Boolean vendorChoice = vendorChoiceMap.get(currentTargetApp);
        if (vendorChoice != null && !vendorChoice && isSystemPackage(currentTargetApp)) {
            log("用户禁用模块，跳过保存: " + currentTargetApp);
            ReaLog.log("config", "用户禁用模块: " + currentTargetApp);
            return;
        }

        Boolean status = installStatusMap.get(currentTargetApp);
        if (status == null) {
            log("配置保存失败：未获取到应用安装状态");
            return;
        }

        String fileName = "installcf_" + currentTargetApp + ".json";
        String[] savePaths = {
                "/data/data/" + currentTargetApp + "/files/" + fileName,
                "/data/user/0/" + currentTargetApp + "/files/" + fileName,
                "/storage/emulated/0/Android/data/" + currentTargetApp + "/files/" + fileName,
        };
        boolean saveSuccess = false;
        String savedPath = null;
        Throwable lastError = null;

        for (String filePath : savePaths) {
            try {
                File configFile = new File(filePath);
                File parentDir = configFile.getParentFile();
                if (parentDir != null && !parentDir.exists()) {
                    if (!parentDir.mkdirs()) {
                        lastError = new Exception("无法创建目录 " + parentDir.getPath());
                        log("无法创建目录: " + parentDir.getPath());
                        continue;
                    }
                }

                JSONObject configJson = new JSONObject();
                if (configFile.exists() && configFile.length() > 0) {
                    try {
                        configJson = readJsonFromFile(configFile);
                    } catch (Exception e) {
                        log("读取旧配置失败，覆盖创建: " + e.getMessage());
                        configJson = new JSONObject();
                    }
                }

                JSONObject appConfig = new JSONObject();
                appConfig.put("install_status", status);
                appConfig.put("floating_shown", floatingShownMap.getOrDefault(currentTargetApp, true));
                appConfig.put("permanent_hidden", permanentHiddenMap.getOrDefault(currentTargetApp, false));
                Float x = floatingXMap.get(currentTargetApp);
                Float y = floatingYMap.get(currentTargetApp);
                appConfig.put("floating_x", x != null ? String.valueOf(x) : "null");
                appConfig.put("floating_y", y != null ? String.valueOf(y) : "null");
                appConfig.put("block_exit", blockExitMap.getOrDefault(currentTargetApp, false));
                appConfig.put("super_block_exit", superBlockExitMap.getOrDefault(currentTargetApp, false));
                appConfig.put("user_disabled_auto_block", userDisabledAutoBlockMap.getOrDefault(currentTargetApp, false));
                appConfig.put("permission_fake", permissionFakeMap.getOrDefault(currentTargetApp, true));
                appConfig.put("launch_intercept", launchInterceptMap.getOrDefault(currentTargetApp, true));
                appConfig.put("vendor_enabled", vendorChoiceMap.getOrDefault(currentTargetApp, false));
                appConfig.put("last_save_time", System.currentTimeMillis());
                appConfig.put("last_save_path", filePath);
                appConfig.put("selinux_fake", selinuxFakeMap.getOrDefault(currentTargetApp, true));
                appConfig.put("force_default_back", forceDefaultBackMap.getOrDefault(currentTargetApp, false));
                appConfig.put("crash_protect_enabled", crashProtectEnabledMap.getOrDefault(currentTargetApp, true));

                List<String> userPackages = userDefinedPackagesMap.getOrDefault(currentTargetApp, new ArrayList<>());
                appConfig.put("user_defined_packages", stringListToJsonArray(userPackages));

                List<String> excludedPackages = excludedPackagesMap.getOrDefault(currentTargetApp, new ArrayList<>());
                appConfig.put("excluded_packages", stringListToJsonArray(excludedPackages));

                List<PackageConfig> pkgConfigs = packageConfigMap.getOrDefault(currentTargetApp, new ArrayList<>());
                JSONArray pkgConfigArray = new JSONArray();
                for (PackageConfig config : pkgConfigs) {
                    JSONObject configObj = new JSONObject();
                    configObj.put("packageName", config.packageName);
                    configObj.put("statusMode", config.statusMode);
                    pkgConfigArray.put(configObj);
                }
                appConfig.put("package_configs", pkgConfigArray);

                List<InterceptPattern> patterns = interceptPatternsMap.get(currentTargetApp);
                if (patterns != null && !patterns.isEmpty()) {
                    JSONArray patternsArray = new JSONArray();
                    for (InterceptPattern pattern : patterns) {
                        JSONObject patternObj = new JSONObject();
                        patternObj.put("pattern_hash", pattern.patternHash);
                        patternObj.put("installed_packages", stringListToJsonArray(pattern.installedPackages));
                        patternObj.put("not_installed_packages", stringListToJsonArray(pattern.notInstalledPackages));
                        patternObj.put("user_choice", pattern.userChoice != null ? pattern.userChoice : "");
                        patternObj.put("choice_count", pattern.choiceCount);
                        patternObj.put("last_detected_time", pattern.lastDetectedTime);
                        patternObj.put("silent_intercept", pattern.silentIntercept);
                        patternsArray.put(patternObj);
                    }
                    appConfig.put("intercept_patterns", patternsArray);
                }

                appConfig.put("global_captured_packages", stringListToJsonArray(globalCapturedPackages));

                Map<String, String> actions = autoActionMap.get(currentTargetApp);
                if (actions != null && !actions.isEmpty()) {
                    appConfig.put("auto_actions", new JSONObject(actions));
                }

                Map<String, List<String>> records = autoChoiceRecordsMap.get(currentTargetApp);
                if (records != null && !records.isEmpty()) {
                    JSONObject recordsJson = new JSONObject();
                    for (Map.Entry<String, List<String>> entry : records.entrySet()) {
                        JSONArray arr = new JSONArray();
                        for (String s : entry.getValue()) arr.put(s);
                        recordsJson.put(entry.getKey(), arr);
                    }
                    appConfig.put("auto_records", recordsJson);
                }

                Map<String, Boolean> detailMap = permissionFakeDetailMap.get(currentTargetApp);
                if (detailMap != null && !detailMap.isEmpty()) {
                    appConfig.put("permission_fake_detail", new JSONObject(detailMap));
                }

                Map<String, Boolean> antiDetailMap = antiDetectionDetailMap.getOrDefault(currentTargetApp, new HashMap<>());
                appConfig.put("anti_detection_detail", new JSONObject(antiDetailMap));

                Map<String, Boolean> vpnDetail = vpnFakeDetailMap.getOrDefault(currentTargetApp, new HashMap<>());
                appConfig.put("vpn_fake_detail", new JSONObject(vpnDetail));

                Map<String, Boolean> shareDetail = shareFakeDetailMap.getOrDefault(currentTargetApp, getDefaultShareDetailMap());
                appConfig.put("share_fake_detail", new JSONObject(shareDetail));

                Map<String, Boolean> pauseMap = reaLogPauseMap.get(currentTargetApp);
                if (pauseMap != null && !pauseMap.isEmpty()) {
                    appConfig.put("rea_log_pause", new JSONObject(pauseMap));
                }

                configJson.put(currentTargetApp, appConfig);
                String finalJson = configJson.toString(2);

                FileOutputStream fos = new FileOutputStream(configFile);
                OutputStreamWriter writer = new OutputStreamWriter(fos, "UTF-8");
                writer.write(finalJson);
                writer.flush();
                writer.close();
                fos.close();

                saveSuccess = true;
                savedPath = filePath;
                packageStatusCache.clear();
                pathPackageCache.clear();
                mSystemCoreCache.clear();
                if (mPkgMgr != null) {
                    mPkgMgr.clearSmartFakeCache();
                }
                log("配置保存成功: " + savedPath);
                ReaLog.log("config", "配置保存成功: " + savedPath);
                break;
            } catch (Throwable t) {
                lastError = t;
                log("配置保存异常: " + t.getMessage() + " | 路径: " + filePath);
                ReaLog.log("config", "配置保存错误: " + t.getMessage() + " | 路径: " + filePath);
            }
        }

        if (!saveSuccess) {
            if (lastError != null) {
                log("配置保存最终失败(用缓存): " + lastError.getMessage());
                ReaLog.log("config", "配置错误回退仅缓存: " + lastError.getMessage());
            }
        }
    }

    private Map<String, Boolean> getDefaultAntiDetectionMap() {
        Map<String, Boolean> defaultMap = new LinkedHashMap<String, Boolean>();
        for (String key : ALL_ANTI_KEYS) {
            defaultMap.put(key, true);
        }
        if (sDefaultConfigLogged.add("anti_detection")) {
            ReaLog.log("misc", "获取痕迹检测配置: " + defaultMap.size() + " 项");
        }
        return defaultMap;
    }

    public static Map<String, Boolean> getDefaultVpnDetailMap() {
        Map<String, Boolean> defaultMap = new LinkedHashMap<String, Boolean>();
        defaultMap.put("interface", true);
        defaultMap.put("cmd", true);
        defaultMap.put("capture", true);
        defaultMap.put("netfiles", true);
        defaultMap.put("net_detect", true);
        defaultMap.put("proxy", true);
        defaultMap.put("proxy_env", true);
        defaultMap.put("ssl_trust", false);
        defaultMap.put("ssl_pinning", false);
        defaultMap.put("ssl_cert", false);
        defaultMap.put("ssl_cert_hide", false);
        if (sDefaultConfigLogged.add("vpn_detail")) {
            ReaLog.log("misc", "获取网络代理配置: " + defaultMap.size() + " 项");
        }
        return defaultMap;
    }

    public static Map<String, Boolean> getDefaultShareDetailMap() {
        Map<String, Boolean> defaultMap = new LinkedHashMap<String, Boolean>();
        defaultMap.put("wechat", false);
        defaultMap.put("qq", false);
        defaultMap.put("dingtalk", false);
        defaultMap.put("weibo", false);
        if (sDefaultConfigLogged.add("share_detail")) {
            ReaLog.log("misc", "获取虚假分享配置: " + defaultMap.size() + " 项");
        }
        return defaultMap;
    }

    public static boolean migrateAndCleanupShareConfig(JSONObject appConfig) {
        if (appConfig == null) return false;

        boolean hasOldFields = appConfig.has("wechat_share_fake") ||
                appConfig.has("qq_share_fake") ||
                appConfig.has("dingtalk_share_fake") ||
                appConfig.has("weibo_share_fake");

        if (!hasOldFields) {
            return false;
        }

        if (!appConfig.has("share_fake_detail")) {
            Map<String, Boolean> migratedMap = new HashMap<String, Boolean>();
            migratedMap.put("wechat", appConfig.optBoolean("wechat_share_fake", false));
            migratedMap.put("qq", appConfig.optBoolean("qq_share_fake", false));
            migratedMap.put("dingtalk", appConfig.optBoolean("dingtalk_share_fake", false));
            migratedMap.put("weibo", appConfig.optBoolean("weibo_share_fake", false));
            try {
                appConfig.put("share_fake_detail", new JSONObject(migratedMap));
                ReaLog.log("share", "迁移旧版字段到新格式: " + migratedMap);
            } catch (JSONException e) {
                ReaLog.log("share", "迁移配置异常: " + e.getMessage());
                return false;
            }
        }

        appConfig.remove("wechat_share_fake");
        appConfig.remove("qq_share_fake");
        appConfig.remove("dingtalk_share_fake");
        appConfig.remove("weibo_share_fake");
        ReaLog.log("share", "清理旧版字段完成");
        return true;
    }

    public int getChoiceCountForDetected(DetectedPackages detected) {
        Set<String> currentPkgSet = new HashSet<>();
        currentPkgSet.addAll(detected.installedPackages);
        currentPkgSet.addAll(detected.notInstalledPackages);
        List<InterceptPattern> patterns = interceptPatternsMap.get(currentTargetApp);
        if (patterns != null) {
            for (InterceptPattern pattern : patterns) {
                Set<String> patternPkgSet = new HashSet<>();
                patternPkgSet.addAll(pattern.installedPackages);
                patternPkgSet.addAll(pattern.notInstalledPackages);
                if (patternPkgSet.equals(currentPkgSet)) {
                    return pattern.choiceCount;
                }
            }
        }
        return 0;
    }

    private void hookExitMethods(ClassLoader classLoader) {
        try {
            XposedHelpers.findAndHookMethod(
                    "java.lang.System",
                    classLoader,
                    "exit",
                    int.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            if (bypassExitHook) {
                                return;
                            }
                            Spkill.handleAppExit(HookInit.this, "System.exit()", param);
                        }
                    }
            );

            XposedHelpers.findAndHookMethod(
                    "android.os.Process",
                    classLoader,
                    "killProcess",
                    int.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            if (bypassExitHook) {
                                return;
                            }
                            Spkill.handleAppExit(HookInit.this, "Process.killProcess()", param);
                        }
                    }
            );

            XposedHelpers.findAndHookMethod(
                    "android.app.Activity",
                    classLoader,
                    "finish",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            if (bypassExitHook) {
                                return;
                            }
                            Activity activity = (Activity) param.thisObject;
                            if (pendingFinishSet.contains(activity)) {
                                pendingFinishSet.remove(activity);
                                return;
                            }
                            boolean isSuperBlockEnabled = superBlockExitMap.getOrDefault(currentTargetApp, false);
                            if (isSuperBlockEnabled) {
                                Boolean isBackPressTrigger = backPressFinishFlag.getOrDefault(activity, false);
                                if (isBackPressTrigger) {
                                    backPressFinishFlag.remove(activity);
                                    return;
                                }
                                Spkill.handleActivityFinish(HookInit.this, activity, param);
                            } else {
                                Spkill.handleActivityFinish(HookInit.this, activity, param);
                            }
                        }
                    }
            );

            try {
                XposedHelpers.findAndHookMethod(
                        "android.app.Activity",
                        classLoader,
                        "finishAffinity",
                        new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param)
                                    throws Throwable {
                                if (bypassExitHook) {
                                    return;
                                }
                                Activity activity = (Activity) param.thisObject;
                                boolean isSuperBlockEnabled = superBlockExitMap.getOrDefault(currentTargetApp, false);

                                if (isSuperBlockEnabled) {
                                    Boolean isBackPressTrigger = backPressFinishFlag.getOrDefault(activity, false);
                                    if (isBackPressTrigger) {
                                        backPressFinishFlag.remove(activity);
                                        return;
                                    }
                                    Spkill.handleActivityFinish(HookInit.this, activity, param);
                                } else {
                                    Spkill.handleActivityFinish(HookInit.this, activity, param);
                                }
                            }
                        }
                );
            } catch (Throwable ignored) {
            }

            try {
                XposedHelpers.findAndHookMethod(
                        "android.app.Activity",
                        classLoader,
                        "finishAndRemoveTask",
                        new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param)
                                    throws Throwable {
                                if (bypassExitHook) {
                                    return;
                                }
                                Activity activity = (Activity) param.thisObject;
                                boolean isSuperBlockEnabled = superBlockExitMap.getOrDefault(currentTargetApp, false);

                                if (isSuperBlockEnabled) {
                                    Boolean isBackPressTrigger = backPressFinishFlag.getOrDefault(activity, false);
                                    if (isBackPressTrigger) {
                                        backPressFinishFlag.remove(activity);
                                        return;
                                    }
                                    Spkill.handleActivityFinish(HookInit.this, activity, param);
                                } else {
                                    Spkill.handleActivityFinish(HookInit.this, activity, param);
                                }
                            }
                        }
                );
            } catch (Throwable ignored) {
            }

            try {
                XposedHelpers.findAndHookMethod(
                        "android.app.Activity",
                        classLoader,
                        "finishAfterTransition",
                        new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param)
                                    throws Throwable {
                                if (bypassExitHook) {
                                    return;
                                }
                                Activity activity = (Activity) param.thisObject;
                                boolean isSuperBlockEnabled = superBlockExitMap.getOrDefault(currentTargetApp, false);

                                if (isSuperBlockEnabled) {
                                    Boolean isBackPressTrigger = backPressFinishFlag.getOrDefault(activity, false);
                                    if (isBackPressTrigger) {
                                        backPressFinishFlag.remove(activity);
                                        return;
                                    }
                                    Spkill.handleActivityFinish(HookInit.this, activity, param);
                                } else {
                                    Spkill.handleActivityFinish(HookInit.this, activity, param);
                                }
                            }
                        }
                );
            } catch (Throwable ignored) {
            }
        } catch (Throwable t) {
            log("Hook退出方法失败: " + t.getMessage());
        }
    }

    public void maybeEnableBlockExit() {
        if (userDisabledAutoBlockMap.getOrDefault(currentTargetApp, false)) {
            ReaLog.log("exit_intercept", "用户已禁用自动开启，跳过");
            return;
        }
        if (autoBlockTriggeredThisSession) {
            return;
        }

        DetectedPackages detected = analyzeDetectedPackages();
        if (!detected.installedPackages.isEmpty() || !detected.notInstalledPackages.isEmpty()) {
            Boolean current = blockExitMap.get(currentTargetApp);
            if (current == null || !current) {
                blockExitMap.put(currentTargetApp, true);
                ReaLog.log("exit_intercept", "自动开启普通拦截");

                if (!crashProtectEnabledMap.getOrDefault(currentTargetApp, true)) {
                    crashProtectEnabledMap.put(currentTargetApp, true);
                    applyCrashProtect(true, getApplicationContext());
                    ReaLog.log("exit_intercept", "自动开启Java异常");
                }

                saveConfigToFile();
                autoBlockTriggeredThisSession = true;

                final Context ctx = getApplicationContext();
                if (ctx != null) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtil.show(ctx, "检测到新包名，自动开启退出拦截");
                            log("检测到新包名，自动开启退出拦截");
                            ReaLog.log("exit_intercept", "检测到新包名，自动开启退出拦截" + ctx);
                        }
                    });
                }
                if (foatInstance != null) {
                    foatInstance.updateFloatingTextOnly();
                }
            }
        }
    }

    public Context getApplicationContext() {
        if (sCachedAppContext != null) {
            return sCachedAppContext;
        }

        try {
            Context ctx = (Context) XposedHelpers.callStaticMethod(
                    XposedHelpers.findClass("android.app.ActivityThread", null),
                    "currentApplication"
            );
            if (ctx != null) {
                ReaLog.log("system", "获取Context: 通过 currentApplication() 成功");
                sCachedAppContext = ctx;
                return ctx;
            }
        } catch (Throwable ignored) {
        }

        try {
            Context ctx = (Context) XposedHelpers.callStaticMethod(
                    XposedHelpers.findClass("android.app.AppGlobals", null),
                    "getInitialApplication"
            );
            if (ctx != null) {
                ReaLog.log("system", "获取Context: 通过 getInitialApplication() 成功");
                sCachedAppContext = ctx;
                return ctx;
            }
        } catch (Throwable ignored) {
        }

        try {
            Object thread = XposedHelpers.callStaticMethod(
                    XposedHelpers.findClass("android.app.ActivityThread", null),
                    "currentActivityThread"
            );
            if (thread != null) {
                Context ctx = (Context) XposedHelpers.callMethod(thread, "getApplication");
                if (ctx != null) {
                    ReaLog.log("system", "获取Context: 通过 getApplication() 成功");
                    sCachedAppContext = ctx;
                    return ctx;
                }
            }
        } catch (Throwable ignored) {
        }

        try {
            Class<?> threadClass = XposedHelpers.findClass("android.app.ActivityThread", null);
            Field field = threadClass.getDeclaredField("mInitialApplication");
            field.setAccessible(true);
            Object thread = XposedHelpers.callStaticMethod(threadClass, "currentActivityThread");
            if (thread != null) {
                Context ctx = (Context) field.get(thread);
                if (ctx != null) {
                    ReaLog.log("system", "获取Context: 通过 mInitialApplication 成功");
                    sCachedAppContext = ctx;
                    return ctx;
                }
            }
        } catch (Throwable ignored) {
        }

        try {
            Context ctx = (Context) Class.forName("android.app.ActivityThread")
                    .getMethod("currentApplication")
                    .invoke(null);
            if (ctx != null) {
                ReaLog.log("system", "获取Context: 通过反射 currentApplication() 成功");
                sCachedAppContext = ctx;
                return ctx;
            }
        } catch (Throwable ignored) {
        }

        ReaLog.log("system", "获取Context: 所有方法均失败，返回 null");
        return null;
    }

    public String getCurrentTargetApp() {
        return currentTargetApp;
    }

    public boolean isMainActivity(Activity activity) {
        if (activity == null) {
            ReaLog.log("system", "主界面: Activity为空");
            return false;
        }
        try {
            Intent intent = activity.getIntent();
            if (intent != null) {
                String action = intent.getAction();
                if (Intent.ACTION_MAIN.equals(action)) {
                    Set<String> categories = intent.getCategories();
                    if (categories != null && categories.contains(Intent.CATEGORY_LAUNCHER)) {
                        ReaLog.log("system", "主界面: true");
                        return true;
                    }
                }
            }
        } catch (Throwable t) {
            log("判断主Activity异常: " + t.getMessage());
            ReaLog.log("system", "主界面异常: " + t.getMessage());
        }
        ReaLog.log("system", "主界面: false");
        return false;
    }

    public DetectedPackages analyzeDetectedPackages() {
        if (mPkgMgr != null) {
            return mPkgMgr.analyzeDetectedPackages();
        }
        return new DetectedPackages();
    }

    private String generatePatternHash(List<String> allPackages) {
        if (mPkgMgr != null) {
            return mPkgMgr.generatePatternHash(allPackages);
        }
        return "default_hash";
    }

    public boolean checkSilentIntercept(DetectedPackages detected) {
        try {
            List<InterceptPattern> patterns = interceptPatternsMap.get(currentTargetApp);
            if (patterns == null) {
                return false;
            }
            for (InterceptPattern pattern : patterns) {
                if (pattern.patternHash.equals(detected.patternHash) &&
                        pattern.silentIntercept && pattern.userChoice.equals("intercept")) {
                    pattern.lastDetectedTime = System.currentTimeMillis();
                    return true;
                }
            }
        } catch (Throwable t) {
            log("检查静默拦截异常: " + t.getMessage());
        }
        return false;
    }
/*
    public void showSilentInterceptToast(DetectedPackages detected) {
        try {
            final Context ctx = getApplicationContext();
            if (ctx == null) return;

            int choiceCount = 0;
            List<InterceptPattern> patterns = interceptPatternsMap.get(currentTargetApp);
            if (patterns != null) {
                Set<String> currentPkgSet = new HashSet<>();
                currentPkgSet.addAll(detected.installedPackages);
                currentPkgSet.addAll(detected.notInstalledPackages);

                for (InterceptPattern pattern : patterns) {
                    Set<String> patternPkgSet = new HashSet<>();
                    patternPkgSet.addAll(pattern.installedPackages);
                    patternPkgSet.addAll(pattern.notInstalledPackages);
                    if (patternPkgSet.equals(currentPkgSet)) {
                        choiceCount = pattern.choiceCount;
                        break;
                    }
                }
            }

            int totalCount = detected.installedPackages.size() + detected.notInstalledPackages.size();
            String packageNames = "";
            if (totalCount <= 3) {
                List<String> allPackages = new ArrayList<>();
                allPackages.addAll(detected.installedPackages);
                allPackages.addAll(detected.notInstalledPackages);
                packageNames = String.join(", ", allPackages);
            } else {
                packageNames = detected.installedPackages.get(0) + " 等" + totalCount + "个应用";
            }
            final String message = "已自动拦截退出（基于 " + choiceCount + " 次历史选择）\n检测到：" + packageNames;
            ReaLog.log("exit_intercept", "已自动拦截退出（基于 " + choiceCount + " 次历史选择）\n检测到：" + packageNames);

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    ToastUtil.show(ctx, message);
                }
            });
        } catch (Throwable t) {
            log("显示静默拦截Toast异常: " + t.getMessage());
        }
    }*/
    public void showSilentInterceptToast(DetectedPackages detected) {
    try {
        // 1. 获取有效的Context
        Context ctx = getCurrentActivity(); // 优先当前Activity
        if (ctx == null) {
            ctx = getApplicationContext();
        }
        if (ctx == null) {
            ReaLog.log("exit_intercept", "自动拦截: Context为空，无法显示Toast");
            return;
        }

        // 2. 计算历史选择次数
        int choiceCount = 0;
        List<InterceptPattern> patterns = interceptPatternsMap.get(currentTargetApp);
        if (patterns != null) {
            Set<String> currentPkgSet = new HashSet<>();
            currentPkgSet.addAll(detected.installedPackages);
            currentPkgSet.addAll(detected.notInstalledPackages);

            for (InterceptPattern pattern : patterns) {
                Set<String> patternPkgSet = new HashSet<>();
                patternPkgSet.addAll(pattern.installedPackages);
                patternPkgSet.addAll(pattern.notInstalledPackages);
                if (patternPkgSet.equals(currentPkgSet)) {
                    choiceCount = pattern.choiceCount;
                    break;
                }
            }
        }

        // 3. 构建提示消息
        int totalCount = detected.installedPackages.size() + detected.notInstalledPackages.size();
        String packageNames;
        if (totalCount <= 3) {
            List<String> allPackages = new ArrayList<>();
            allPackages.addAll(detected.installedPackages);
            allPackages.addAll(detected.notInstalledPackages);
            packageNames = String.join(", ", allPackages);
        } else if (!detected.installedPackages.isEmpty()) {
            packageNames = detected.installedPackages.get(0) + " 等" + totalCount + "个应用";
        } else if (!detected.notInstalledPackages.isEmpty()) {
            packageNames = detected.notInstalledPackages.get(0) + " 等" + totalCount + "个应用";
        } else {
            packageNames = "未知应用";
        }

        final String message = "已自动拦截退出（基于 " + choiceCount + " 次历史选择）\n检测到：" + packageNames;
        ReaLog.log("exit_intercept", message);

        // 4. 在主线程显示Toast
        final Context finalCtx = ctx;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                ToastUtil.showUnique(finalCtx, message);
            }
        });
    } catch (Throwable t) {
        log("显示静默拦截Toast异常: " + t.getMessage());
        ReaLog.log("exit_intercept", "显示静默拦截Toast异常: " + t.getMessage());
    }
}

    public void handleInterceptChoice(DetectedPackages detected, String choice, boolean interceptSuccess) {
        try {
            List<InterceptPattern> patterns = interceptPatternsMap.get(currentTargetApp);
            if (patterns == null) {
                patterns = new ArrayList<>();
                interceptPatternsMap.put(currentTargetApp, patterns);
            }
            InterceptPattern existingPattern = null;
            for (InterceptPattern pattern : patterns) {
                if (pattern.patternHash.equals(detected.patternHash)) {
                    existingPattern = pattern;
                    break;
                }
            }
            if (existingPattern == null) {
                existingPattern = new InterceptPattern(
                        detected.patternHash,
                        new ArrayList<>(detected.installedPackages),
                        new ArrayList<>(detected.notInstalledPackages)
                );
                patterns.add(existingPattern);
            }

            existingPattern.userChoice = choice;
            existingPattern.choiceCount++;
            existingPattern.lastDetectedTime = System.currentTimeMillis();

            existingPattern.recentChoices.add(choice);
            if (existingPattern.recentChoices.size() > 3) {
                existingPattern.recentChoices.remove(0);
            }

            boolean threeSame = false;
            if (existingPattern.recentChoices.size() == 3) {
                String first = existingPattern.recentChoices.get(0);
                if (first.equals(existingPattern.recentChoices.get(1)) &&
                        first.equals(existingPattern.recentChoices.get(2))) {
                    threeSame = true;
                }
            }

            if (threeSame) {
                existingPattern.silentIntercept = true;
                log("启用静默，模式: " + detected.patternHash + "，选择: " + choice);
                ReaLog.log("exit_intercept", "启用静默，模式: " + detected.patternHash + "，选择: " + choice);
            } else {
                existingPattern.silentIntercept = false;
            }

            saveConfigToFile();
            log("拦截选择记录 - 模式: " + detected.patternHash +
                    ", 选择: " + choice +
                    ", 计数: " + existingPattern.choiceCount +
                    ", 静默: " + existingPattern.silentIntercept +
                    ", 最近选择: " + existingPattern.recentChoices);

            ReaLog.log("exit_intercept", "拦截选择记录 - 模式: " + detected.patternHash +
                    ", 选择: " + choice +
                    ", 计数: " + existingPattern.choiceCount +
                    ", 静默: " + existingPattern.silentIntercept +
                    ", 最近选择: " + existingPattern.recentChoices);
        } catch (Throwable t) {
            log("处理拦截选择异常: " + t.getMessage());
        }
    }

    public String getSilentAction(DetectedPackages detected) {
        List<InterceptPattern> patterns = interceptPatternsMap.get(currentTargetApp);
        if (patterns == null) return null;

        for (InterceptPattern pattern : patterns) {
            if (pattern.patternHash.equals(detected.patternHash) && pattern.silentIntercept) {
                return pattern.userChoice;
            }
        }

        for (InterceptPattern pattern : patterns) {
            if (pattern.silentIntercept && "intercept".equals(pattern.userChoice)) {
                return "intercept";
            }
        }
        return null;
    }

    public Activity getCurrentActivity() {
        if (currentResumedActivity != null &&
                !currentResumedActivity.isFinishing() &&
                !currentResumedActivity.isDestroyed()) {
            return currentResumedActivity;
        }
        return null;
    }
    public void showAddPackageDialog(final Activity activity) {
        try {
            ReaLog.log("system", "打开添加包名对话框");
            final String targetApp = currentTargetApp;
            if (!userDefinedPackagesMap.containsKey(targetApp)) {
                userDefinedPackagesMap.put(targetApp, new ArrayList<String>());
            }
            final List<String> userPackages = userDefinedPackagesMap.get(targetApp);
            final DisplayMetrics metrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            final LinearLayout[] packagesLayoutArr = new LinearLayout[1];
            final EditText[] inputEtArr = new EditText[1];
            LinearLayout topLayout = new LinearLayout(activity);
            topLayout.setOrientation(LinearLayout.HORIZONTAL);
            topLayout.setPadding(0, 0, 0, 20);
            final EditText inputEt = new EditText(activity);
            inputEt.setHint(com.install.appinstall.xl.ru.RuStrings.translateString(
                    "请输入包名\n(如com.a.b.c)"));
            inputEt.setPadding(30, 20, 30, 20);
            inputEt.setTextColor(0xFF000000);
            inputEt.setBackgroundColor(0xFFFFFFFF);
            inputEt.setHintTextColor(0xFF999999);
            inputEt.setBackgroundTintMode(null);
            inputEt.setOutlineProvider(null);
            try {
                Class<?> gradientDrawableClass = Class.forName("android.graphics.drawable.GradientDrawable");
                Object etDrawable = gradientDrawableClass.newInstance();
                Method setColorMethod = gradientDrawableClass.getDeclaredMethod("setColor", int.class);
                Method setCornerRadiusMethod = gradientDrawableClass.getDeclaredMethod("setCornerRadius", float.class);
                Method setStrokeMethod = gradientDrawableClass.getDeclaredMethod("setStroke", int.class, int.class);
                setColorMethod.invoke(etDrawable, 0xFFFFFFFF);
                setCornerRadiusMethod.invoke(etDrawable, 25f);
                setStrokeMethod.invoke(etDrawable, 2, 0xFFE0E0E0);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    inputEt.setBackground((android.graphics.drawable.Drawable) etDrawable);
                } else {
                    inputEt.setBackgroundDrawable((android.graphics.drawable.Drawable) etDrawable);
                }
            } catch (Exception e) {
                android.graphics.drawable.ShapeDrawable etShape = new android.graphics.drawable.ShapeDrawable();
                etShape.setShape(new android.graphics.drawable.shapes.RoundRectShape(
                        new float[]{25f, 25f, 25f, 25f, 25f, 25f, 25f, 25f},
                        null, null
                ));
                etShape.getPaint().setColor(0xFFFFFFFF);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    inputEt.setBackground(etShape);
                } else {
                    inputEt.setBackgroundDrawable(etShape);
                }
            }
            LinearLayout.LayoutParams etParams = new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1
            );
            topLayout.addView(inputEt, etParams);
            inputEtArr[0] = inputEt;
            final Spinner typeSpinner = new Spinner(activity);
            typeSpinner.setWillNotDraw(false);
            typeSpinner.setPopupBackgroundResource(android.R.color.transparent);
            List<String> spinnerItems = new ArrayList<String>();
            spinnerItems.add(com.install.appinstall.xl.ru.RuStrings.translateString("◀ [伪造]包名"));
            spinnerItems.add(com.install.appinstall.xl.ru.RuStrings.translateString("◀ [排除]包名"));
            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(activity, android.R.layout.simple_spinner_item, spinnerItems) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    TextView tv = (TextView) view;
                    tv.setTextColor(0xFF000000);
                    tv.setTextSize(14);
                    tv.setGravity(Gravity.CENTER);
                    tv.setPadding(40, 20, 40, 20);
                    tv.setBackgroundColor(0xFFF5F5F5);
                    tv.setBackgroundResource(android.R.drawable.list_selector_background);
                    return view;
                }

                @Override
                public View getDropDownView(int position, View convertView, ViewGroup parent) {
                    View view = super.getDropDownView(position, convertView, parent);
                    TextView tv = (TextView) view;
                    tv.setTextColor(0xFF000000);
                    tv.setTextSize(18);
                    tv.setGravity(Gravity.CENTER_VERTICAL);
                    tv.setPadding(20, 30, 20, 30);
                    tv.setBackgroundColor(0xFFF5F5F5);
                    tv.setBackgroundResource(android.R.drawable.list_selector_background);
                    return view;
                }
            };
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            typeSpinner.setAdapter(spinnerAdapter);
            typeSpinner.setSelection(0);
            typeSpinner.setBackgroundColor(0xFFFDFDFD);
            typeSpinner.setPopupBackgroundResource(android.R.color.white);
            try {
                Class<?> gradientDrawableClass = Class.forName("android.graphics.drawable.GradientDrawable");
                Object spinnerDrawable = gradientDrawableClass.newInstance();
                Method setColorMethod = gradientDrawableClass.getDeclaredMethod("setColor", int.class);
                Method setCornerRadiusMethod = gradientDrawableClass.getDeclaredMethod("setCornerRadius", float.class);
                Method setStrokeMethod = gradientDrawableClass.getDeclaredMethod("setStroke", int.class, int.class);
                setColorMethod.invoke(spinnerDrawable, 0xFFF5F5F5);
                setCornerRadiusMethod.invoke(spinnerDrawable, 25f);
                setStrokeMethod.invoke(spinnerDrawable, 2, 0xFFE0E0E0);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    typeSpinner.setBackground((android.graphics.drawable.Drawable) spinnerDrawable);
                } else {
                    typeSpinner.setBackgroundDrawable((android.graphics.drawable.Drawable) spinnerDrawable);
                }
            } catch (Exception e) {
                android.graphics.drawable.ShapeDrawable shapeDrawable = new android.graphics.drawable.ShapeDrawable();
                shapeDrawable.setShape(new android.graphics.drawable.shapes.RoundRectShape(
                        new float[]{25f, 25f, 25f, 25f, 25f, 25f, 25f, 25f},
                        null, null
                ));
                shapeDrawable.getPaint().setColor(0xFFF5F5F5);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    typeSpinner.setBackground(shapeDrawable);
                } else {
                    typeSpinner.setBackgroundDrawable(shapeDrawable);
                }
            }
            typeSpinner.setPadding(40, 20, 20, 20);
            LinearLayout.LayoutParams spinnerParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            spinnerParams.leftMargin = 10;
            spinnerParams.width = (int) (activity.getResources().getDisplayMetrics().density * 150);
            topLayout.addView(typeSpinner, spinnerParams);
            Button addBtn = new com.install.appinstall.xl.ru.RuButton(activity);
            addBtn.setText("确定");
            addBtn.setPadding(40, 20, 40, 20);
            addBtn.setTextColor(0xFFFFFFFF);
            addBtn.setBackgroundTintMode(null);
            addBtn.setOutlineProvider(null);
            try {
                Class<?> gradientDrawableClass = Class.forName("android.graphics.drawable.GradientDrawable");
                Object addBtnDrawable = gradientDrawableClass.newInstance();
                Method setColorMethod = gradientDrawableClass.getDeclaredMethod("setColor", int.class);
                Method setCornerRadiusMethod = gradientDrawableClass.getDeclaredMethod("setCornerRadius", float.class);
                setColorMethod.invoke(addBtnDrawable, 0xAA4CAF50);
                setCornerRadiusMethod.invoke(addBtnDrawable, 25f);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    addBtn.setBackground((android.graphics.drawable.Drawable) addBtnDrawable);
                } else {
                    addBtn.setBackgroundDrawable((android.graphics.drawable.Drawable) addBtnDrawable);
                }
            } catch (Exception e) {
                android.graphics.drawable.ShapeDrawable shapeDrawable = new android.graphics.drawable.ShapeDrawable();
                shapeDrawable.setShape(new android.graphics.drawable.shapes.RoundRectShape(
                        new float[]{25f, 25f, 25f, 25f, 25f, 25f, 25f, 25f},
                        null,
                        null
                ));
                shapeDrawable.getPaint().setColor(0xAA4CAF50);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    addBtn.setBackground(shapeDrawable);
                } else {
                    addBtn.setBackgroundDrawable(shapeDrawable);
                }
            }
            LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            btnParams.leftMargin = 15;
            topLayout.addView(addBtn, btnParams);
            final int maxScrollHeight = (int) (metrics.heightPixels * 0.5);
            ScrollView adaptiveScrollView = new ScrollView(activity) {
                @Override
                protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                    heightMeasureSpec = MeasureSpec.makeMeasureSpec(
                            maxScrollHeight,
                            MeasureSpec.AT_MOST
                    );
                    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                }
            };
            adaptiveScrollView.setPadding(0, 0, 0, 0);
            final LinearLayout packagesLayout = new LinearLayout(activity);
            packagesLayout.setOrientation(LinearLayout.VERTICAL);
            packagesLayout.setBackgroundColor(0xFFFFFFFF);
            packagesLayout.setPadding(10, 10, 10, 10);
            adaptiveScrollView.addView(packagesLayout);
            packagesLayoutArr[0] = packagesLayout;
            TextView tipTv = new com.install.appinstall.xl.ru.RuTextView(activity);
            tipTv.setText("请准确输入包名,否则数据设置失效\n批量输入:一行一个换行或中英文(逗号分号)");
            tipTv.setPadding(30, 20, 30, 10);
            tipTv.setTextSize(12);
            tipTv.setTextColor(0xFFFF5722);
            tipTv.setTextIsSelectable(true);
            packagesLayout.addView(tipTv);
            packagesLayout.requestLayout();
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    refreshPackagesLayout(packagesLayout, userPackages, activity);
                    packagesLayout.requestLayout();
                }
            }, 100);
            LinearLayout totalLayout = new LinearLayout(activity);
            totalLayout.setOrientation(LinearLayout.VERTICAL);
            totalLayout.setPadding(20, 20, 20, 20);
            totalLayout.setBackgroundColor(0xFFFFFFFF);
            totalLayout.addView(topLayout);
            totalLayout.addView(
                    adaptiveScrollView,
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    )
            );
            final AlertDialog dialog = createBoundedDialog(
                    activity,
                    "添加自定义包名",
                    "",
                    new String[]{"一键清空", "保存", "取消"},
                    new DialogInterface.OnClickListener[]{
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int which) {
                                    if (userPackages == null || userPackages.isEmpty()) {
                                        ToastUtil.showUnique(activity, "当前无已添加包名，无需清空");
                                        ReaLog.log("system", "清空包名: 当前无包名");
                                        return;
                                    }
                                    AlertDialog confirmDialog = createBoundedDialog(
                                            activity,
                                            "确认清空",
                                            "确定要清空所有手动添加的包名吗？<br><br><font color='#F44336'>此操作不可恢复！</font>",
                                            new String[]{"确认清空", "取消"},
                                            new DialogInterface.OnClickListener[]{
                                                    new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface d, int which) {
                                                            List<String> packagesToRemove = new ArrayList<String>(userPackages);
                                                            userPackages.clear();
                                                            excludedPackagesMap.put(targetApp, new ArrayList<String>());
                                                            synchronized (globalCapturedPackages) {
                                                                globalCapturedPackages.removeAll(packagesToRemove);
                                                            }
                                                            List<PackageConfig> configs = packageConfigMap.get(targetApp);
                                                            if (configs != null) {
                                                                Iterator<PackageConfig> it = configs.iterator();
                                                                while (it.hasNext()) {
                                                                    if (packagesToRemove.contains(it.next().packageName)) {
                                                                        it.remove();
                                                                    }
                                                                }
                                                                packageConfigMap.put(targetApp, configs);
                                                            }
                                                            saveConfigToFile();
                                                            refreshPackagesLayout(packagesLayoutArr[0], userPackages, activity);
                                                            ToastUtil.showUnique(activity, "✅ 已清空所有手动添加的包名");
                                                            ReaLog.log("misc", "一键清空包名: " + packagesToRemove.size() + " 个");
                                                            d.dismiss();
                                                        }
                                                    },
                                                    new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface d, int which) {
                                                            d.dismiss();
                                                        }
                                                    },
                                            }
                                    );
                                    confirmDialog.show();
                                }
                            },
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int which) {
                                    if (userPackages == null || userPackages.isEmpty()) {
                                        ToastUtil.showUnique(activity, "当前无已添加包名，无需保存");
                                        ReaLog.log("misc", "保存包名: 无包名");
                                        return;
                                    }
                                    saveConfigToFile();
                                    ToastUtil.showUnique(activity, "配置已保存");
                                    ReaLog.log("misc", "保存包名列表: " + userPackages.size() + " 个");
                                    dialogInterface.dismiss();
                                }
                            },
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int which) {
                                    ReaLog.log("misc", "取消添加包名");
                                    dialogInterface.dismiss();
                                }
                            },
                    },
                    totalLayout
            );
            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialogInterface) {
                    final Window window = dialog.getWindow();
                    if (window != null) {
                        WindowManager.LayoutParams params = window.getAttributes();
                        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
                        params.width = WindowManager.LayoutParams.MATCH_PARENT;
                        params.gravity = Gravity.CENTER;
                        window.setAttributes(params);
                        window.getDecorView().requestLayout();
                    }
                }
            });
            addBtn.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final String input = inputEt.getText().toString().trim();
                            if (input.isEmpty()) {
                                ToastUtil.showUnique(activity, "请输入有效包名");
                                ReaLog.log("system", "添加包名: 输入为空");
                                return;
                            }
                            Set<String> pkgSet = new HashSet<String>();
                            String[] splits = input.split("[\\n,，;；]+");
                            for (String pkg : splits) {
                                String trimmedPkg = pkg.trim();
                                if (!trimmedPkg.isEmpty() && isValidPackageName(trimmedPkg)) {
                                    pkgSet.add(trimmedPkg);
                                }
                            }
                            final List<String> pkgList = new ArrayList<String>(pkgSet);
                            if (pkgList.isEmpty()) {
                                ToastUtil.showUnique(activity, "未识别到有效包名");
                                ReaLog.log("system", "添加包名: 无有效包名");
                                return;
                            }
                            if (input.contains(" ")) {
                                AlertDialog spaceDialog = createBoundedDialog(
                                        activity,
                                        "检测到空格",
                                        "",
                                        new String[]{"去除空格", "强制添加"},
                                        new DialogInterface.OnClickListener[]{
                                                new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface d, int which) {
                                                        List<String> processedList = new ArrayList<String>();
                                                        for (String pkg : pkgList) {
                                                            processedList.add(pkg.replaceAll(" ", ""));
                                                        }
                                                        Set<String> processedSet = new HashSet<String>(processedList);
                                                        processedList = new ArrayList<String>(processedSet);
                                                        int selectedType = typeSpinner.getSelectedItemPosition();
                                                        batchHandlePackageAdd(processedList, userPackages, packagesLayout, activity, selectedType);
                                                        inputEt.setText("");
                                                        ReaLog.log("system", "添加包名(去除空格): " + processedList.size() + " 个");
                                                    }
                                                },
                                                new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface d, int which) {
                                                        int selectedType = typeSpinner.getSelectedItemPosition();
                                                        batchHandlePackageAdd(pkgList, userPackages, packagesLayout, activity, selectedType);
                                                        inputEt.setText("");
                                                        ReaLog.log("misc", "添加包名(强制): " + pkgList.size() + " 个");
                                                    }
                                                },
                                        }
                                );
                                spaceDialog.setMessage(com.install.appinstall.xl.ru.RuStrings.fromHtml(
                                        "包名包含空格可能导致数据失效，是否去除所有空格？<br><br>" +
                                                "将添加 <font color='#FF5722'><b>" + pkgList.size() + "</b></font> 个包名。"
                                ));
                                spaceDialog.show();
                                return;
                            }
                            int selectedType = typeSpinner.getSelectedItemPosition();
                            batchHandlePackageAdd(pkgList, userPackages, packagesLayout, activity, selectedType);
                            ReaLog.log("misc", "添加包名: " + pkgList.size() + " 个");
                            ToastUtil.show(activity, "成功添加: " + pkgList.size() + " 个");
                            inputEt.setText("");
                        }
                    }
            );
            dialog.show();
        } catch (Throwable t) {
            log("显示添加包名对话框异常: " + t.getMessage());
        }
    }

    private void showPackageConfigDialog(final Activity activity, final String packageName) {
        if (activity == null || TextUtils.isEmpty(packageName) || packageConfigMap == null) {
            ToastUtil.showUnique(activity, "操作异常");
            ReaLog.log("misc", "独立配置: 参数无效");
            return;
        }
        if (!packageConfigMap.containsKey(currentTargetApp)) {
            ToastUtil.showUnique(activity, "无当前应用配置");
            ReaLog.log("misc", "独立配置: 无当前应用配置");
            return;
        }
        PackageConfig tempConfig = null;
        List<PackageConfig> configs = packageConfigMap.get(currentTargetApp);
        for (PackageConfig config : configs) {
            if (packageName.equals(config.packageName)) {
                tempConfig = config;
                break;
            }
        }
        if (tempConfig == null) {
            ToastUtil.showUnique(activity, "未找到该包名配置");
            ReaLog.log("misc", "未找到独立包名配置: " + packageName);
            return;
        }
        final PackageConfig targetConfig = tempConfig;
        ReaLog.log("system", "打开包名配置对话框: " + packageName + ", 当前模式: " + targetConfig.statusMode);

        String btn1 = "固定为已安装";
        String btn2 = "固定为未安装";
        String btn3 = "跟随全局配置";
        if ("installed".equals(targetConfig.statusMode)) {
            btn1 += "（当前）";
        } else if ("not_installed".equals(targetConfig.statusMode)) {
            btn2 += "（当前）";
        } else {
            btn3 += "（当前）";
        }
        StringBuilder msg = new StringBuilder();
        msg.append("包名：<font color='#FF5722'><b>").append(packageName).append("</b></font><br><br>");
        msg.append("当前状态：");
        switch (targetConfig.statusMode) {
            case "installed":
                msg.append("<font color='#4CAF50'>固定为已安装</font>");
                break;
            case "not_installed":
                msg.append("<font color='#F44336'>固定为未安装</font>");
                break;
            default:
                msg.append("<font color='#9E9E9E'>跟随全局配置</font>");
                break;
        }
        AlertDialog dialog = createBoundedDialog(
                activity,
                "配置包名状态",
                msg.toString(),
                new String[]{btn1, btn2, btn3},
                new DialogInterface.OnClickListener[]{
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                targetConfig.statusMode = "installed";
                                saveConfigToFile();
                                ToastUtil.showUnique(activity, "已设置为固定为已安装");
                                ReaLog.log("misc", "修改包名配置: " + packageName + " -> 固定已安装");
                                dialog.dismiss();
                            }
                        },
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                targetConfig.statusMode = "not_installed";
                                saveConfigToFile();
                                ToastUtil.showUnique(activity, "已设置为固定为未安装");
                                ReaLog.log("misc", "修改包名配置: " + packageName + " -> 固定未安装");
                                dialog.dismiss();
                            }
                        },
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                targetConfig.statusMode = "follow";
                                saveConfigToFile();
                                ToastUtil.showUnique(activity, "已设置为跟随全局配置");
                                ReaLog.log("misc", "修改包名配置: " + packageName + " -> 跟随全局");
                                dialog.dismiss();
                            }
                        }
                }
        );
        if (!activity.isFinishing() && !dialog.isShowing()) {
            dialog.show();
            ReaLog.log("system", "包名配置对话框已显示: " + packageName);
        }
    }

    private void batchHandlePackageAdd(List<String> pkgList, List<String> userPackages, LinearLayout packagesLayout, Activity activity, int selectedType) {
        int successCount = 0;
        List<String> excludedPackages = excludedPackagesMap.getOrDefault(currentTargetApp, new ArrayList<String>());
        List<PackageConfig> configs = packageConfigMap.getOrDefault(currentTargetApp, new ArrayList<PackageConfig>());

        ReaLog.log("misc", "批量添加包名，类型: " + (selectedType == 0 ? "伪造" : "排除") + ", 数量: " + pkgList.size());

        for (String pkg : pkgList) {
            if (pkg == null || pkg.isEmpty() || !isValidPackageName(pkg)) {
                continue;
            }
            if (userPackages.contains(pkg)) {
                ReaLog.log("misc", "包名已存在，跳过: " + pkg);
                continue;
            }
            if (selectedType == 0) {
                if (excludedPackages.contains(pkg)) {
                    excludedPackages.remove(pkg);
                }
                userPackages.add(pkg);
                synchronized (globalCapturedPackages) {
                    if (!globalCapturedPackages.contains(pkg)) {
                        globalCapturedPackages.add(pkg);
                        if (mPkgMgr != null) {
                            mPkgMgr.invalidateAppDirCache();
                        }
                        packageStatusCache.clear();
                        mSystemCoreCache.clear();
                    }
                }
                boolean configExists = false;
                for (PackageConfig c : configs) {
                    if (c.packageName.equals(pkg)) {
                        configExists = true;
                        break;
                    }
                }
                if (!configExists) {
                    configs.add(new PackageConfig(pkg));
                }
                successCount++;
                ReaLog.log("misc", "添加伪造包名: " + pkg);
            } else {
                if (globalCapturedPackages.contains(pkg)) {
                    globalCapturedPackages.remove(pkg);
                    if (mPkgMgr != null) {
                        mPkgMgr.invalidateAppDirCache();
                    }
                    packageStatusCache.clear();
                    mSystemCoreCache.clear();
                }
                userPackages.add(pkg);
                if (!excludedPackages.contains(pkg)) {
                    excludedPackages.add(pkg);
                }
                Iterator<PackageConfig> it = configs.iterator();
                while (it.hasNext()) {
                    if (it.next().packageName.equals(pkg)) {
                        it.remove();
                        break;
                    }
                }
                successCount++;
                ReaLog.log("misc", "添加排除包名: " + pkg);
            }
        }

        excludedPackagesMap.put(currentTargetApp, excludedPackages);
        packageConfigMap.put(currentTargetApp, configs);

        refreshPackagesLayout(packagesLayout, userPackages, activity);
        if (mPkgMgr != null) {
            mPkgMgr.invalidateAppDirCache();
        }
        packageStatusCache.clear();
        mSystemCoreCache.clear();
        saveConfigToFile();
        ToastUtil.showUnique(activity, "处理完成\n批量添加 " + successCount + "/" + pkgList.size() + " 个包名");
        ReaLog.log("misc", "批量添加成功: " + successCount + "/" + pkgList.size() + " 个包名");
    }

    private void handlePackageAdd(String pkg, List<String> userPackages, LinearLayout packagesLayout, Activity activity, int selectedType) {
        if (pkg == null || pkg.isEmpty() || !isValidPackageName(pkg)) {
            ToastUtil.showUnique(activity, "包名格式无效");
            ReaLog.log("misc", "包名格式无效: " + pkg);
            return;
        }

        if (userPackages.contains(pkg) || globalCapturedPackages.contains(pkg)) {
            ToastUtil.showUnique(activity, "重复:包名已存在\n" +pkg);
            ReaLog.log("misc", "包名重复添加: " + pkg);
            return;
        }
        List<String> excludedPackages = excludedPackagesMap.getOrDefault(currentTargetApp, new ArrayList<>());
        if (selectedType == 0) {
            if (excludedPackages.contains(pkg)) {
                excludedPackages.remove(pkg);
            }
            userPackages.add(pkg);
            globalCapturedPackages.add(pkg);
            if (mPkgMgr != null) {
                mPkgMgr.invalidateAppDirCache();
            }
            packageStatusCache.clear();
            mSystemCoreCache.clear();
            List<PackageConfig> configs = packageConfigMap.getOrDefault(currentTargetApp, new ArrayList<>());
            configs.add(new PackageConfig(pkg));
            packageConfigMap.put(currentTargetApp, configs);

            ToastUtil.showUnique(activity, "已添加伪造包名：" + pkg);
            ReaLog.log("misc", "已添加伪造包名: " + pkg);
        } else {
            if (globalCapturedPackages.contains(pkg)) {
                globalCapturedPackages.remove(pkg);
                if (mPkgMgr != null) {
                    mPkgMgr.invalidateAppDirCache();
                }
                packageStatusCache.clear();
                mSystemCoreCache.clear();
            }
            userPackages.add(pkg);
            excludedPackages.add(pkg);
            excludedPackagesMap.put(currentTargetApp, excludedPackages);
            ToastUtil.showUnique(activity, "已添加排除包名：" + pkg);
            ReaLog.log("misc", "已添加排除包名: " + pkg);
        }

        refreshPackagesLayout(packagesLayout, userPackages, activity);
    }

    private void refreshPackagesLayout(final LinearLayout packagesLayout, final List<String> userPackages, final Activity activity) {
        for (int i = packagesLayout.getChildCount() - 1; i >= 1; i--) {
            packagesLayout.removeViewAt(i);
        }

        List<String> fakePackages = new ArrayList<String>();
        List<String> excludePackages = new ArrayList<String>();
        List<String> excludedGlobal = excludedPackagesMap.getOrDefault(currentTargetApp, new ArrayList<String>());
        for (String pkg : userPackages) {
            if (excludedGlobal.contains(pkg)) {
                excludePackages.add(pkg);
            } else {
                fakePackages.add(pkg);
            }
        }

        if (!fakePackages.isEmpty()) {
            TextView fakeTitle = new com.install.appinstall.xl.ru.RuTextView(activity);
            fakeTitle.setText("📌 伪造包名（" + fakePackages.size() + "个）");
            fakeTitle.setPadding(30, 15, 30, 10);
            fakeTitle.setTextSize(13);
            fakeTitle.setTextColor(0xFF4CAF50);
            fakeTitle.setTextIsSelectable(true);
            packagesLayout.addView(fakeTitle);

            for (int i = 0; i < fakePackages.size(); i++) {
                final int pos = userPackages.indexOf(fakePackages.get(i));
                final String pkg = fakePackages.get(i);
                LinearLayout itemLayout = new LinearLayout(activity);
                itemLayout.setOrientation(LinearLayout.HORIZONTAL);
                itemLayout.setPadding(30, 8, 30, 8);
                itemLayout.setGravity(Gravity.CENTER_VERTICAL);

                TextView pkgTv = new com.install.appinstall.xl.ru.RuTextView(activity);
                pkgTv.setText((i + 1) + ". [伪造] " + pkg);
                pkgTv.setPadding(0, 5, 0, 5);
                pkgTv.setTextSize(13);
                pkgTv.setTextColor(0xFF9E9E9E);
                pkgTv.setIncludeFontPadding(false);
                pkgTv.setWillNotDraw(false);
                pkgTv.setSingleLine(false);
                pkgTv.setMaxLines(2);
                pkgTv.setEllipsize(android.text.TextUtils.TruncateAt.END);
                pkgTv.setTextIsSelectable(true);
                LinearLayout.LayoutParams pkgParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
                itemLayout.addView(pkgTv, pkgParams);

                Button configBtn = new com.install.appinstall.xl.ru.RuButton(activity);
                configBtn.setText("配置");
                configBtn.setTextSize(11);
                configBtn.setPadding(20, 5, 20, 5);
                try {
                    Class<?> gradientDrawableClass = Class.forName("android.graphics.drawable.GradientDrawable");
                    Object delBtnDrawable = gradientDrawableClass.newInstance();
                    Method setColorMethod = gradientDrawableClass.getMethod("setColor", int.class);
                    Method setCornerRadiusMethod = gradientDrawableClass.getMethod("setCornerRadius", float.class);
                    setColorMethod.invoke(delBtnDrawable, 0xAA2196F3);
                    setCornerRadiusMethod.invoke(delBtnDrawable, 25f);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        configBtn.setBackground((android.graphics.drawable.Drawable) delBtnDrawable);
                    } else {
                        configBtn.setBackgroundDrawable((android.graphics.drawable.Drawable) delBtnDrawable);
                    }
                    configBtn.setTextColor(0xFFFFFFFF);
                    configBtn.setBackgroundTintMode(null);
                    configBtn.setOutlineProvider(null);
                } catch (Throwable e) {
                    android.graphics.drawable.ShapeDrawable shapeDrawable = new android.graphics.drawable.ShapeDrawable();
                    shapeDrawable.setShape(new android.graphics.drawable.shapes.RoundRectShape(
                            new float[]{25f, 25f, 25f, 25f, 25f, 25f, 25f, 25f}, null, null));
                    shapeDrawable.getPaint().setColor(0xAA2196F3);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        configBtn.setBackground(shapeDrawable);
                    } else {
                        configBtn.setBackgroundDrawable(shapeDrawable);
                    }
                }
                configBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ReaLog.log("system", "打开包名配置: " + pkg);
                        showPackageConfigDialog(activity, pkg);
                    }
                });
                itemLayout.addView(configBtn);

                Button delBtn = new com.install.appinstall.xl.ru.RuButton(activity);
                delBtn.setText("删除");
                delBtn.setTextSize(11);
                delBtn.setPadding(20, 5, 20, 5);
                try {
                    Class<?> gradientDrawableClass = Class.forName("android.graphics.drawable.GradientDrawable");
                    Object delBtnDrawable = gradientDrawableClass.newInstance();
                    Method setColorMethod = gradientDrawableClass.getMethod("setColor", int.class);
                    Method setCornerRadiusMethod = gradientDrawableClass.getMethod("setCornerRadius", float.class);
                    setColorMethod.invoke(delBtnDrawable, 0xAAF44336);
                    setCornerRadiusMethod.invoke(delBtnDrawable, 25f);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        delBtn.setBackground((android.graphics.drawable.Drawable) delBtnDrawable);
                    } else {
                        delBtn.setBackgroundDrawable((android.graphics.drawable.Drawable) delBtnDrawable);
                    }
                    delBtn.setTextColor(0xFFFFFFFF);
                    delBtn.setBackgroundTintMode(null);
                    delBtn.setOutlineProvider(null);
                } catch (Throwable e) {
                    android.graphics.drawable.ShapeDrawable shapeDrawable = new android.graphics.drawable.ShapeDrawable();
                    shapeDrawable.setShape(new android.graphics.drawable.shapes.RoundRectShape(
                            new float[]{25f, 25f, 25f, 25f, 25f, 25f, 25f, 25f}, null, null));
                    shapeDrawable.getPaint().setColor(0xAAF44336);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        delBtn.setBackground(shapeDrawable);
                    } else {
                        delBtn.setBackgroundDrawable(shapeDrawable);
                    }
                }
                delBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        userPackages.remove(pos);
                        List<String> excludedGlobal = excludedPackagesMap.getOrDefault(currentTargetApp, new ArrayList<String>());
                        excludedGlobal.remove(pkg);
                        excludedPackagesMap.put(currentTargetApp, excludedGlobal);
                        synchronized (globalCapturedPackages) {
                            globalCapturedPackages.remove(pkg);
                        }
                        List<PackageConfig> configs = packageConfigMap.getOrDefault(currentTargetApp, new ArrayList<PackageConfig>());
                        Iterator<PackageConfig> configIt = configs.iterator();
                        while (configIt.hasNext()) {
                            if (configIt.next().packageName.equals(pkg)) {
                                configIt.remove();
                                break;
                            }
                        }
                        packageConfigMap.put(currentTargetApp, configs);
                        saveConfigToFile();
                        refreshPackagesLayout(packagesLayout, userPackages, activity);
                        ToastUtil.showUnique(activity, "已删除伪造包名：" + pkg);
                        ReaLog.log("misc", "已删除伪造包名: " + pkg);
                    }
                });
                LinearLayout.LayoutParams delParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                delParams.leftMargin = 10;
                itemLayout.addView(delBtn, delParams);
                packagesLayout.addView(itemLayout);

                if (i != fakePackages.size() - 1) {
                    View divider = new View(activity);
                    divider.setBackgroundColor(0xFFE0E0E0);
                    LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1);
                    dividerParams.topMargin = 8;
                    packagesLayout.addView(divider, dividerParams);
                }
            }
        }

        if (!fakePackages.isEmpty() && !excludePackages.isEmpty()) {
            View mainDivider = new View(activity);
            mainDivider.setBackgroundColor(0xFF999999);
            LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2);
            dividerParams.topMargin = 15;
            dividerParams.bottomMargin = 15;
            packagesLayout.addView(mainDivider, dividerParams);
        }

        if (!excludePackages.isEmpty()) {
            TextView excludeTitle = new com.install.appinstall.xl.ru.RuTextView(activity);
            excludeTitle.setText("❌ 排除包名（" + excludePackages.size() + "个）");
            excludeTitle.setPadding(30, 15, 30, 10);
            excludeTitle.setTextSize(13);
            excludeTitle.setTextColor(0xFFF44336);
            excludeTitle.setTextIsSelectable(true);
            packagesLayout.addView(excludeTitle);

            for (int i = 0; i < excludePackages.size(); i++) {
                final int pos = userPackages.indexOf(excludePackages.get(i));
                final String pkg = excludePackages.get(i);
                LinearLayout itemLayout = new LinearLayout(activity);
                itemLayout.setOrientation(LinearLayout.HORIZONTAL);
                itemLayout.setPadding(30, 8, 30, 8);
                itemLayout.setGravity(Gravity.CENTER_VERTICAL);

                TextView pkgTv = new com.install.appinstall.xl.ru.RuTextView(activity);
                pkgTv.setText((i + 1) + ". [排除] " + pkg);
                pkgTv.setPadding(0, 5, 0, 5);
                pkgTv.setTextSize(13);
                pkgTv.setTextColor(0xFF9E9E9E);
                pkgTv.setIncludeFontPadding(false);
                pkgTv.setWillNotDraw(false);
                pkgTv.setSingleLine(false);
                pkgTv.setMaxLines(2);
                pkgTv.setEllipsize(android.text.TextUtils.TruncateAt.END);
                pkgTv.setTextIsSelectable(true);
                LinearLayout.LayoutParams pkgParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
                itemLayout.addView(pkgTv, pkgParams);

                Button delBtn = new com.install.appinstall.xl.ru.RuButton(activity);
                delBtn.setText("删除");
                delBtn.setTextSize(11);
                delBtn.setPadding(20, 5, 20, 5);
                try {
                    Class<?> gradientDrawableClass = Class.forName("android.graphics.drawable.GradientDrawable");
                    Object delBtnDrawable = gradientDrawableClass.newInstance();
                    Method setColorMethod = gradientDrawableClass.getMethod("setColor", int.class);
                    Method setCornerRadiusMethod = gradientDrawableClass.getMethod("setCornerRadius", float.class);
                    setColorMethod.invoke(delBtnDrawable, 0xAAF44336);
                    setCornerRadiusMethod.invoke(delBtnDrawable, 25f);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        delBtn.setBackground((android.graphics.drawable.Drawable) delBtnDrawable);
                    } else {
                        delBtn.setBackgroundDrawable((android.graphics.drawable.Drawable) delBtnDrawable);
                    }
                    delBtn.setTextColor(0xFFFFFFFF);
                    delBtn.setBackgroundTintMode(null);
                    delBtn.setOutlineProvider(null);
                } catch (Throwable e) {
                    android.graphics.drawable.ShapeDrawable shapeDrawable = new android.graphics.drawable.ShapeDrawable();
                    shapeDrawable.setShape(new android.graphics.drawable.shapes.RoundRectShape(
                            new float[]{25f, 25f, 25f, 25f, 25f, 25f, 25f, 25f}, null, null));
                    shapeDrawable.getPaint().setColor(0xAAF44336);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        delBtn.setBackground(shapeDrawable);
                    } else {
                        delBtn.setBackgroundDrawable(shapeDrawable);
                    }
                }
                delBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        userPackages.remove(pos);
                        List<String> excludedGlobal = excludedPackagesMap.getOrDefault(currentTargetApp, new ArrayList<String>());
                        excludedGlobal.remove(pkg);
                        excludedPackagesMap.put(currentTargetApp, excludedGlobal);
                        synchronized (globalCapturedPackages) {
                            globalCapturedPackages.remove(pkg);
                        }
                        saveConfigToFile();
                        refreshPackagesLayout(packagesLayout, userPackages, activity);
                        ToastUtil.showUnique(activity, "已删除排除包名：" + pkg);
                        ReaLog.log("misc", "已删除排除包: " + pkg);
                    }
                });
                itemLayout.addView(delBtn);
                packagesLayout.addView(itemLayout);

                if (i != excludePackages.size() - 1) {
                    View divider = new View(activity);
                    divider.setBackgroundColor(0xFFE0E0E0);
                    LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1);
                    dividerParams.topMargin = 8;
                    packagesLayout.addView(divider, dividerParams);
                }
            }
        }

        if (fakePackages.isEmpty() && excludePackages.isEmpty()) {
            TextView emptyTv = new com.install.appinstall.xl.ru.RuTextView(activity);
            emptyTv.setText("暂无已添加包名");
            emptyTv.setPadding(30, 15, 30, 15);
            emptyTv.setTextColor(0xFF9E9E9E);
            emptyTv.setTextSize(12);
            emptyTv.setTextIsSelectable(true);
            packagesLayout.addView(emptyTv);
        }
        ReaLog.log("system", "包名列表刷新完成, 伪造: " + fakePackages.size() + ", 排除: " + excludePackages.size());
    }

    private void showClearConfirmDialog(final Activity activity, final View floatingView) {
        ReaLog.log("system", "显示清理确认弹窗");
        final List<String> userPackages = userDefinedPackagesMap.getOrDefault(currentTargetApp, new ArrayList<String>());
        StringBuilder pkgText = new StringBuilder();
        pkgText.append("检测到 <font color='#FF5722'><b>" + userPackages.size() + "</b></font> 个手动添加的包名<br>");
        for (int i = 0; i < userPackages.size(); i++) {
            pkgText.append((i + 1)).append(". ").append(userPackages.get(i)).append("<br>");
        }
        pkgText.append("<br>==============<br>")
                .append("同步清空：删除自动捕获包名+删除手动添加包名 <font color='#FF5722'><b>" + userPackages.size() + "</b></font> 个<br>")
                .append("单独留下：删除自动捕获包名,<font color='#FF5722'><b>保留</b></font>手动添加包名<br>")
                .append("<font color='#9E9E9E'><small><b>请选择操作：</b></small></font>");
        AlertDialog dialog = createBoundedDialog(
                activity,
                "清理包名",
                pkgText.toString(),
                new String[]{"同步清空", "单独留下(" + userPackages.size() + ")", "取消"},
                new DialogInterface.OnClickListener[]{
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ReaLog.log("misc", "用户选择同步清空");
                                clearAllPackageLists(activity, floatingView);
                                dialog.dismiss();
                            }
                        },
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ReaLog.log("misc", "用户选择单独留下(" + userPackages.size() + ")个");
                                clearAutoCapturedPackagesOnly(activity, floatingView);
                                dialog.dismiss();
                            }
                        },
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ReaLog.log("misc", "用户取消清理");
                                dialog.dismiss();
                            }
                        },
                }
        );
        dialog.show();
    }

    private void clearAutoCapturedPackagesOnly(final Activity activity, final View floatingView) {
        try {
            List<String> userPackages = userDefinedPackagesMap.getOrDefault(currentTargetApp, new ArrayList<String>());
            synchronized (globalCapturedPackages) {
                Iterator<String> iterator = globalCapturedPackages.iterator();
                while (iterator.hasNext()) {
                    String pkg = iterator.next();
                    if (!userPackages.contains(pkg)) {
                        iterator.remove();
                        ReaLog.log("misc", "移除自动捕获包: " + pkg);
                    }
                }
            }
            appCapturedPackages.clear();
            List<PackageConfig> configs = packageConfigMap.getOrDefault(currentTargetApp, new ArrayList<PackageConfig>());
            Iterator<PackageConfig> configIterator = configs.iterator();
            while (configIterator.hasNext()) {
                PackageConfig config = configIterator.next();
                if (!userPackages.contains(config.packageName)) {
                    configIterator.remove();
                    ReaLog.log("misc", "移除自动捕获包配置: " + config.packageName);
                }
            }
            packageConfigMap.put(currentTargetApp, configs);
            List<InterceptPattern> patterns = interceptPatternsMap.get(currentTargetApp);
            if (patterns != null) {
                for (InterceptPattern pattern : patterns) {
                    Iterator<String> installedIt = pattern.installedPackages.iterator();
                    while (installedIt.hasNext()) {
                        String pkg = installedIt.next();
                        if (!userPackages.contains(pkg)) {
                            installedIt.remove();
                        }
                    }
                    Iterator<String> notInstalledIt = pattern.notInstalledPackages.iterator();
                    while (notInstalledIt.hasNext()) {
                        String pkg = notInstalledIt.next();
                        if (!userPackages.contains(pkg)) {
                            notInstalledIt.remove();
                        }
                    }
                }
            }
            clearAutoCapturedCache(userPackages);
            if (mPkgMgr != null) {
                mPkgMgr.invalidateAppDirCache();
            }
            packageStatusCache.clear();
            mSystemCoreCache.clear();
            saveConfigToFile();
            showRefreshConfirmDialog(activity, null);
            ToastUtil.showUnique(activity, "✅ 仅清理自动捕获的包，保留手动添加的包");
            ReaLog.log("misc", "仅清理自动捕获包完成");
        } catch (Throwable t) {
            log("仅清理自动捕获包异常: " + t.getMessage());
            ToastUtil.showUnique(activity, "清理失败");
        }
    }

    private void clearAutoCapturedCache(List<String> userPackages) {
        if (mPkgMgr != null) {
            mPkgMgr.clearAutoCapturedCache(userPackages);
        }
    }

    private void showMoreFunctionsDialog(final Activity activity, final TextView floatingView) {
        Activity validActivity = activity;
        if (validActivity == null || validActivity.isFinishing() ||
                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && validActivity.isDestroyed())) {
            if (ReaLog.sHookInit != null) {
                Activity current = ReaLog.sHookInit.getCurrentActivity();
                if (current != null && !current.isFinishing() &&
                        (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1 || !current.isDestroyed())) {
                    validActivity = current;
                }
            }
            if (validActivity == null) {
                Context ctx = ReaLog.sHookInit != null ? ReaLog.sHookInit.getApplicationContext() : null;
                if (ctx != null) {
                    ToastUtil.showUnique(ctx, "Activity 已失效，无法打开更多设置");
                }
                return;
            }
        }
        final Activity finalActivity = validActivity;

        ReaLog.log("system", "打开更多功能配置");
        final List<MenuItem> menuItems = new ArrayList<MenuItem>();

        menuItems.add(new MenuItem(" ◎ 启动拦截设置", "控制是否拦截第三方应用启动请求", new Runnable() {
            @Override
            public void run() {
                ReaLog.log("system", "打开启动拦截设置");
                showLaunchInterceptConfigDialog(finalActivity, floatingView);
            }
        }));
        menuItems.add(new MenuItem(" ◎ 权限防护设置", "控制 捕获当前目标权限并假装授权", new Runnable() {
            @Override
            public void run() {
                ReaLog.log("system", "打开权限防护设置");
                showCustomPermissionDialog(finalActivity, floatingView);
            }
        }));
        menuItems.add(new MenuItem(" ◎ 拦截退出设置", "阻止目标因检测到应用包而退出", new Runnable() {
            @Override
            public void run() {
                ReaLog.log("system", "打开拦截退出设置");
                showExitInterceptConfigDialog(finalActivity, floatingView);
            }
        }));
        menuItems.add(new MenuItem(" ◎ 返回键设置", "替换目标应用的返回键逻辑", new Runnable() {
            @Override
            public void run() {
                ReaLog.log("system", "打开返回键设置");
                showForceDefaultBackConfigDialog(finalActivity, floatingView);
            }
        }));
        menuItems.add(new MenuItem(" ◎ 伪装强制模式", "使应用检测到状态为 Enforcing", new Runnable() {
            @Override
            public void run() {
                ReaLog.log("system", "打开SELinux伪装设置");
                showSelinuxFakeConfigDialog(finalActivity, floatingView);
            }
        }));
        menuItems.add(new MenuItem(" ◎ 假装分享设置", "使应用内部的分享假装成功", new Runnable() {
            @Override
            public void run() {
                ReaLog.log("system", "打开假装分享设置");
                showShareFakeConfigDialog(finalActivity, floatingView);
            }
        }));
        menuItems.add(new MenuItem(" ◎ 网络代理设置", "隐藏VPN和代理检测", new Runnable() {
            @Override
            public void run() {
                ReaLog.log("system", "打开网络代理设置");
                showVpnFakeConfigDialog(finalActivity, floatingView);
            }
        }));
        menuItems.add(new MenuItem(" ◎ 痕迹检测设置", "基础隐藏Xposed痕迹", new Runnable() {
            @Override
            public void run() {
                ReaLog.log("system", "打开痕迹检测设置");
                showAntiDetectionConfigDialog(finalActivity, floatingView);
            }
        }));
        menuItems.add(new MenuItem(" ◎ 配置文件导出", "将当前配置保存到私有目录", new Runnable() {
            @Override
            public void run() {
                ReaLog.log("config", "打开配置文件导出");
                Portcf.showExportSelectionDialog(finalActivity, currentTargetApp, HookInit.this);
            }
        }));
        menuItems.add(new MenuItem(" ◎ 配置文件导入", "从私有目录加载配置", new Runnable() {
            @Override
            public void run() {
                ReaLog.log("config", "打开配置文件导入");
                Portcf.showImportSelectionDialog(finalActivity, currentTargetApp, floatingView, HookInit.this);
            }
        }));
        menuItems.add(new MenuItem(" ◎ <font color='#1E90FF'><b>查看日志记录</b></font>", "Hook拦截记录日志", new Runnable() {
            @Override
            public void run() {
                ReaLog.log("system", "打开日志记录");
                ReaLog.showCombinedDialog(finalActivity);
            }
        }));
        menuItems.add(new MenuItem(" ◎ <font color='#FF5722'><b>检查模块版本更新</b></font>", "检测GitHub是否有新版本", new Runnable() {
            @Override
            public void run() {
                ReaLog.log("system", "检查版本更新");
                Update.checkForUpdate(finalActivity);
            }
        }));

        ArrayAdapter<MenuItem> adapter = new ArrayAdapter<MenuItem>(finalActivity, 0, menuItems) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    LinearLayout itemLayout = new LinearLayout(finalActivity);
                    itemLayout.setOrientation(LinearLayout.VERTICAL);
                    int paddingPx = (int) (16 * finalActivity.getResources().getDisplayMetrics().density);
                    int paddingSmall = (int) (5 * finalActivity.getResources().getDisplayMetrics().density);
                    itemLayout.setPadding(paddingPx, paddingSmall, paddingPx, paddingSmall);

                    TextView titleView = new com.install.appinstall.xl.ru.RuTextView(finalActivity);
                    titleView.setId(android.R.id.text1);
                    titleView.setTextSize(16);
                    titleView.setTextColor(0xFF333333);

                    TextView descView = new com.install.appinstall.xl.ru.RuTextView(finalActivity);
                    descView.setId(android.R.id.text2);
                    descView.setTextSize(11);
                    descView.setTextColor(0xFF888888);
                    descView.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);

                    itemLayout.addView(titleView);
                    itemLayout.addView(descView);

                    View divider = new View(finalActivity);
                    divider.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2));
                    divider.setBackgroundColor(0xFFE0E0E0);
                    itemLayout.addView(divider);

                    convertView = itemLayout;
                    convertView.setTag(new View[]{titleView, descView, divider});
                }

                View[] views = (View[]) convertView.getTag();
                TextView titleView = (TextView) views[0];
                TextView descView = (TextView) views[1];
                View divider = views[2];

                MenuItem item = getItem(position);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    titleView.setText(com.install.appinstall.xl.ru.RuStrings.fromHtml(item.title, Html.FROM_HTML_MODE_LEGACY));
                    descView.setText(com.install.appinstall.xl.ru.RuStrings.fromHtml(item.desc, Html.FROM_HTML_MODE_LEGACY));
                } else {
                    titleView.setText(com.install.appinstall.xl.ru.RuStrings.fromHtml(item.title));
                    descView.setText(com.install.appinstall.xl.ru.RuStrings.fromHtml(item.desc));
                }

                divider.setVisibility(position == menuItems.size() - 1 ? View.GONE : View.VISIBLE);

                int leftPad = titleView.getPaddingLeft();
                descView.setPadding(leftPad, descView.getPaddingTop(),
                        descView.getPaddingRight(), descView.getPaddingBottom());

                if (position == menuItems.size() - 1) {
                    int topPad = (int) (2 * finalActivity.getResources().getDisplayMetrics().density);
                    titleView.setPadding(titleView.getPaddingLeft(), topPad,
                            titleView.getPaddingRight(), titleView.getPaddingBottom());
                } else {
                    titleView.setPadding(titleView.getPaddingLeft(), 0,
                            titleView.getPaddingRight(), 0);
                }
                return convertView;
            }
        };

        AlertDialog.Builder builder = new com.install.appinstall.xl.ru.RuDialogBuilder(finalActivity, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        builder.setTitle("更多配置设置");
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                MenuItem item = menuItems.get(which);
                if (item.action != null) {
                    ReaLog.log("system", "执行菜单项: " + item.title);
                    item.action.run();
                }
            }
        });
        builder.setNegativeButton("取消设置", null);

        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                try {
                    Window window = dialog.getWindow();
                    if (window != null) {
                        WindowManager.LayoutParams params = window.getAttributes();
                        params.width = WindowManager.LayoutParams.MATCH_PARENT;
                        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
                        params.gravity = Gravity.CENTER;
                        params.horizontalMargin = 0.05f;
                        window.setAttributes(params);
                    }
                } catch (Exception e) {
                    ReaLog.log("system", "更多功能对话框异常: " + e.getMessage());
                    ToastUtil.showUnique(activity, "错误：配置页无法打开");
                }
            }
        });

        if (finalActivity != null && !finalActivity.isFinishing() && !dialog.isShowing()) {
            dialog.show();
        }
    }

    private void showSelinuxFakeConfigDialog(final Activity activity, final TextView floatingView) {
        ReaLog.log("system", "打开SELinux伪装设置对话框");
        final boolean isEnabled = selinuxFakeMap.getOrDefault(currentTargetApp, true);
        String status = isEnabled ? "<font color='#4CAF50'>已开启</font>" : "<font color='#F44336'>已关闭</font>";

        String realMode = Selinuxhook.getRealSelinuxMode();
        String fakeMode = "Enforcing[强制模式]";

        String message = "当前 SELinux 伪装状态：" + status + "<br><br>" +
                "<font color='#9E9E9E'>• 本系统为 <b>" + realMode + " 模式[仅参考]</b></font><br>" +
                "<font color='#9E9E9E'>• 应用收到为 <b>" + fakeMode + "</b></font><br><br>" +
                "<font color='#9E9E9E'>• [开启] 系统状态将被强制返回 Enforcing</font><br>" +
                "<font color='#9E9E9E'>• [关闭] 系统状态不做任何处理</font><br><br>" +
                "<font color='#FF5722'><small><b>当前功能为测试版，部分应用可能无效！</small></b></font><br>" +
                "<font color='#FF5722'><small><b>注：设置后需要重启应用才能完全生效</small></b></font>";

        AlertDialog dialog = createBoundedDialog(
                activity, "伪装强制模式(Beta)", message,
                new String[]{isEnabled ? "关闭设置" : "开启设置", "取消"},
                new DialogInterface.OnClickListener[]{
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface d, int which) {
                                boolean newState = !isEnabled;
                                selinuxFakeMap.put(currentTargetApp, newState);
                                saveConfigToFile();
                                Selinuxhook.setEnabled(currentTargetApp, newState);
                                ReaLog.log("selinux", "SELinux伪装切换: " + (newState ? "开启" : "关闭"));
                                ToastUtil.showUnique(activity, newState ? "✅ 配置已保存" : "❌ 伪装已关闭");
                                d.dismiss();
                                showRestartConfirmDialog(activity);
                            }
                        },
                        null
                }
        );
        dialog.show();
    }

    private void showShareFakeConfigDialog(final Activity activity, final TextView floatingView) {
        ReaLog.log("system", "打开假装分享设置对话框");
        Map<String, Boolean> detailMap = shareFakeDetailMap.getOrDefault(currentTargetApp, getDefaultShareDetailMap());
        final Map<String, Boolean> workingMap = new HashMap<String, Boolean>(detailMap);
        final Map<String, Boolean> originalMap = new HashMap<String, Boolean>(detailMap);

        LinearLayout mainLayout = new LinearLayout(activity);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(40, 30, 40, 30);

        TextView tip = new com.install.appinstall.xl.ru.RuTextView(activity);
        String tipHtml = "请选择要假装分享成功的平台：<br>" +
                "<small><b>保存后<font color='#2196F3'>立即生效</font> 异常请禁用!</b></small><br>" +
                "<font color='#FF5722'><small><b>注：仅限应用自身调用官方分享SDK才生效</b></small></font>";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            tip.setText(com.install.appinstall.xl.ru.RuStrings.fromHtml(tipHtml, Html.FROM_HTML_MODE_LEGACY));
        } else {
            tip.setText(com.install.appinstall.xl.ru.RuStrings.fromHtml(tipHtml));
        }
        tip.setTextColor(0xFF333333);
        tip.setTextSize(14);
        tip.setPadding(0, 0, 0, 20);
        mainLayout.addView(tip);

        final String[] keys = {"wechat", "qq", "dingtalk", "weibo"};
        final String[] labels = {"微信", "QQ", "钉钉", "微博"};

        for (int i = 0; i < keys.length; i++) {
            final String key = keys[i];
            final String label = labels[i];
            boolean isCurrentlyDisabled = !workingMap.getOrDefault(key, false);

            final CheckBox cb = new com.install.appinstall.xl.ru.RuCheckBox(activity);
            updateCheckBoxText(cb, label, !isCurrentlyDisabled);
            cb.setChecked(!isCurrentlyDisabled);
            cb.setPadding(20, 10, 20, 10);
            cb.setTextSize(14);

            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    workingMap.put(key, isChecked);
                    updateCheckBoxText(cb, label, isChecked);
                    ReaLog.log("share", "分享设置变更: " + label + " -> " + (isChecked ? "启用" : "禁用"));
                }
            });

            mainLayout.addView(cb);

            View divider = new View(activity);
            divider.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
            divider.setBackgroundColor(0xFFE0E0E0);
            mainLayout.addView(divider);
        }

        AlertDialog dialog = createBoundedDialog(
                activity,
                "假装分享设置(Beta)",
                null,
                new String[]{"保存设置", "取消"},
                new DialogInterface.OnClickListener[]{
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface d, int which) {
                                if (originalMap.equals(workingMap)) {
                                    ToastUtil.showUnique(activity, "未修改设置");
                                    ReaLog.log("share", "保存分享设置: 无变更");
                                    d.dismiss();
                                    showShareFakeConfigDialog(activity, floatingView);
                                    return;
                                }
                                shareFakeDetailMap.put(currentTargetApp, new HashMap<String, Boolean>(workingMap));
                                saveConfigToFile();
                                ReaLog.log("share", "保存分享设置: " + getChangedKeys(originalMap, workingMap));
                                ToastUtil.showUnique(activity, "✅ 配置已保存");
                                d.dismiss();
                            }
                        },
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface d, int which) {
                                ReaLog.log("share", "取消分享设置");
                                d.dismiss();
                            }
                        }
                },
                mainLayout
        );
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void showAntiDetectionConfigDialog(final Activity activity, final TextView floatingView) {
        ReaLog.log("system", "打开痕迹检测设置对话框");
        Map<String, Boolean> detailMap = antiDetectionDetailMap.getOrDefault(currentTargetApp, new HashMap<String, Boolean>());
        if (detailMap.isEmpty()) {
            detailMap = getDefaultAntiDetectionMap();
            antiDetectionDetailMap.put(currentTargetApp, detailMap);
        }
        final Map<String, Boolean> workingMap = new HashMap<String, Boolean>(detailMap);
        final Map<String, Boolean> originalMap = new HashMap<String, Boolean>(detailMap);

        LinearLayout mainLayout = new LinearLayout(activity);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(40, 30, 40, 30);

        TextView tip = new com.install.appinstall.xl.ru.RuTextView(activity);
        String tipHtml = "请选择要启用的隐藏功能：<br>" +
                "<small><b>保存后需<font color='#2196F3'>重启应用</font>才能完全生效！</b></small><br>" +
                "<font color='#FF5722'><small><b>如有异常将自动禁用对应功能项</small></b></font>";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            tip.setText(com.install.appinstall.xl.ru.RuStrings.fromHtml(tipHtml, Html.FROM_HTML_MODE_LEGACY));
        } else {
            tip.setText(com.install.appinstall.xl.ru.RuStrings.fromHtml(tipHtml));
        }
        tip.setTextColor(0xFF333333);
        tip.setTextSize(14);
        tip.setPadding(0, 0, 0, 20);
        mainLayout.addView(tip);

        final String[] keys = ALL_ANTI_KEYS;
        final String[] labels = {
                "框架隐藏", "文件隐藏", "服务隐藏", "信息隐藏",
                "命令隐藏", "堆栈隐藏", "Root隐藏", "ADB调试隐藏", "开发者隐藏"
        };

        for (int i = 0; i < keys.length; i++) {
            final String key = keys[i];
            final String label = labels[i];
            boolean isDisabled = !workingMap.getOrDefault(key, true);

            final CheckBox cb = new com.install.appinstall.xl.ru.RuCheckBox(activity);
            updateCheckBoxText(cb, label, !isDisabled);
            cb.setChecked(!isDisabled);
            cb.setPadding(20, 10, 20, 10);
            cb.setTextSize(14);

            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    workingMap.put(key, isChecked);
                    updateCheckBoxText(cb, label, isChecked);
                    ReaLog.log("anti_detection", "痕迹检测设置变更: " + label + " -> " + (isChecked ? "启用" : "禁用"));
                }
            });

            mainLayout.addView(cb);

            View divider = new View(activity);
            divider.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
            divider.setBackgroundColor(0xFFE0E0E0);
            mainLayout.addView(divider);
        }

        AlertDialog dialog = createBoundedDialog(
                activity,
                "痕迹检测设置(Beta)",
                null,
                new String[]{"保存设置", "取消", "处理列表"},
                new DialogInterface.OnClickListener[]{
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface d, int which) {
                                if (originalMap.equals(workingMap)) {
                                    ToastUtil.showUnique(activity, "未修改设置");
                                    ReaLog.log("system", "保存痕迹检测: 无变更");
                                    d.dismiss();
                                    showAntiDetectionConfigDialog(activity, floatingView);
                                    return;
                                }

                                Map<String, Boolean> newDetail = new HashMap<String, Boolean>(workingMap);
                                antiDetectionDetailMap.put(currentTargetApp, newDetail);
                                saveConfigToFile();
                                ReaLog.log("anti_detection", "保存痕迹检测: " + getChangedKeys(originalMap, workingMap));

                                AntiDetection.setClassEnabled(workingMap.get("class"));
                                AntiDetection.setFileEnabled(workingMap.get("file"));
                                AntiDetection.setPmEnabled(workingMap.get("pm"));
                                AntiDetection.setProcEnabled(workingMap.get("proc"));
                                AntiDetection.setCmdEnabled(workingMap.get("cmd"));
                                AntiDetection.setStacktraceEnabled(workingMap.get("stacktrace"));
                                AntiDetection.setRootEnabled(workingMap.get("root"));
                                AntiDetection.setAdbEnabled(workingMap.get("adb"));
                                AntiDetection.setDevEnabled(workingMap.get("dev"));
                                AntiDetection.reset();
                                AntiDetection.initForEmbed(activity.getClassLoader(), currentTargetApp, HookInit.this);

                                ToastUtil.showUnique(activity, "✅ 配置已保存");
                                d.dismiss();
                                showRestartConfirmDialog(activity);
                            }
                        },
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface d, int which) {
                                ReaLog.log("system", "取消痕迹检测设置");
                                d.dismiss();
                            }
                        },
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface d, int which) {
                                ReaLog.log("system", "打开日志列表");
                                d.dismiss();
                                ReaLog.showCombinedDialog(activity);
                            }
                        }
                },
                mainLayout
        );
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void showVpnFakeConfigDialog(final Activity activity, final TextView floatingView) {
        ReaLog.log("system", "打开网络代理设置对话框");
        Map<String, Boolean> detailMap = vpnFakeDetailMap.getOrDefault(currentTargetApp, new HashMap<String, Boolean>());
        if (detailMap.isEmpty()) {
            detailMap = getDefaultVpnDetailMap();
            vpnFakeDetailMap.put(currentTargetApp, detailMap);
        }
        final Map<String, Boolean> workingMap = new HashMap<String, Boolean>(detailMap);
        final Map<String, Boolean> originalMap = new HashMap<String, Boolean>(detailMap);

        LinearLayout mainLayout = new LinearLayout(activity);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(40, 30, 40, 30);

        TextView tip = new com.install.appinstall.xl.ru.RuTextView(activity);
        String tipHtml = "请选择要启用的功能：<br>" +
                "<b><small>保存后<font color='#2196F3'>立即生效</font> SSL需重启应用。</small></b><br>" +
                "<font color='#FF5722'><small><b>如有异常将自动禁用对应功能项</small></b></font>";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            tip.setText(com.install.appinstall.xl.ru.RuStrings.fromHtml(tipHtml, Html.FROM_HTML_MODE_LEGACY));
        } else {
            tip.setText(com.install.appinstall.xl.ru.RuStrings.fromHtml(tipHtml));
        }
        tip.setTextColor(0xFF333333);
        tip.setTextSize(14);
        tip.setPadding(0, 0, 0, 20);
        mainLayout.addView(tip);

        final String[] keys = ALL_VPN_KEYS;
        final String[] labels = {
                "VPN接口隐藏", "深度接口隐藏", "代理检测隐藏",
                "代理环境隐藏", "抓包应用隐藏", "网络文件隐藏", "网络检测监听",
                "SSL信任链绕过", "SSL证书绑定绕过", "SSL证书信息隐藏", "SSL证书特征替换"
        };

        for (int i = 0; i < keys.length; i++) {
            final String key = keys[i];
            final String label = labels[i];
            boolean isCurrentlyDisabled = !workingMap.getOrDefault(key, true);

            final CheckBox cb = new com.install.appinstall.xl.ru.RuCheckBox(activity);
            updateCheckBoxText(cb, label, !isCurrentlyDisabled);
            cb.setChecked(!isCurrentlyDisabled);
            cb.setEnabled(true);
            cb.setPadding(20, 10, 20, 10);
            cb.setTextSize(14);

            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    workingMap.put(key, isChecked);
                    updateCheckBoxText(cb, label, isChecked);
                    ReaLog.log("vpn", "网络代理设置变更: " + label + " -> " + (isChecked ? "启用" : "禁用"));
                }
            });

            mainLayout.addView(cb);

            View divider = new View(activity);
            divider.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
            divider.setBackgroundColor(0xFFE0E0E0);
            mainLayout.addView(divider);
        }

        AlertDialog dialog = createBoundedDialog(
                activity,
                "网络代理设置(Beta)",
                null,
                new String[]{"保存设置", "取消", "处理列表"},
                new DialogInterface.OnClickListener[]{
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface d, int which) {
                                if (originalMap.equals(workingMap)) {
                                    ToastUtil.showUnique(activity, "未修改设置");
                                    ReaLog.log("system", "保存网络代理: 无变更");
                                    d.dismiss();
                                    showVpnFakeConfigDialog(activity, floatingView);
                                    return;
                                }

                                vpnFakeDetailMap.put(currentTargetApp, new HashMap<String, Boolean>(workingMap));
                                saveConfigToFile();
                                ReaLog.log("vpn", "保存网络代理: " + getChangedKeys(originalMap, workingMap));
                                ToastUtil.showUnique(activity, "✅ 配置已保存");
                                d.dismiss();

                                boolean sslChanged = false;
                                String[] sslKeys = {"ssl_trust", "ssl_pinning", "ssl_cert", "ssl_cert_hide"};
                                for (String key : sslKeys) {
                                    boolean oldVal = originalMap.getOrDefault(key, true);
                                    boolean newVal = workingMap.getOrDefault(key, true);
                                    if (oldVal != newVal) {
                                        sslChanged = true;
                                        break;
                                    }
                                }
                                if (sslChanged) {
                                    ReaLog.log("vpn", "SSL设置变更，需要重启");
                                    showRestartConfirmDialog(activity);
                                }
                            }
                        },
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface d, int which) {
                                ReaLog.log("vpn", "取消网络代理设置");
                                d.dismiss();
                            }
                        },
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface d, int which) {
                                ReaLog.log("system", "打开日志列表");
                                d.dismiss();
                                ReaLog.showCombinedDialog(activity);
                            }
                        }
                },
                mainLayout
        );
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void showForceDefaultBackConfigDialog(final Activity activity, final TextView floatingView) {
        ReaLog.log("system", "打开返回键设置对话框");
        final boolean isEnabled = forceDefaultBackMap.getOrDefault(currentTargetApp, false);
        String status = isEnabled ? "<font color='#F44336'><b>模块级</b></font>" : "<font color='#F44336'><b>应用自定义</b></font>";
        String message = "当前返回键状态：" + status + "<br><br>" +
                "<font color='#9E9E9E'>• [模块级] 返回键将替换为本模块控制，智能处理返回逻辑</font><br>" +
                "<font color='#9E9E9E'>• [自定义] 使用应用自身的返回键逻辑(如禁止返回)</font><br><br>" +
                "<font color='#FF5722'><small><b>• 部分应用可能行为异常(游戏/播放器)，异常请换回[自定义]！</b></small></font><br>" +
                "<font color='#FF5722'><small><b>• 当前[模块级]为测试版，仅适配多Activity架构</b></small></font><br>" +
                "<small><b>• 若无效需<font color='#2196F3'>重启应用</font>使得完全生效！</b></small>";

        AlertDialog dialog = createBoundedDialog(
                activity,
                "返回键设置(Beta)",
                message,
                new String[]{isEnabled ? "使用自定义" : "使用模块级", "取消设置"},
                new DialogInterface.OnClickListener[]{
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface d, int which) {
                                boolean newState = !isEnabled;
                                forceDefaultBackMap.put(currentTargetApp, newState);
                                saveConfigToFile();
                                ReaLog.log("back_key", "返回键切换为" + (newState ? "模块级" : "自定义"));
                                ToastUtil.showUnique(activity, newState ? "✅ 使用模块级" : "✅ 使用自定义");
                                d.dismiss();
                            }
                        },
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface d, int which) {
                                ReaLog.log("system", "取消返回键设置");
                                d.dismiss();
                            }
                        }
                }
        );
        dialog.show();
    }

    private void showExitInterceptConfigDialog(final Activity activity, final TextView floatingView) {
        ReaLog.log("system", "打开退出拦截设置对话框");
        boolean autoEnabled = !userDisabledAutoBlockMap.getOrDefault(currentTargetApp, false);
        boolean blockExit = blockExitMap.getOrDefault(currentTargetApp, false);
        boolean crashProtect = crashProtectEnabledMap.getOrDefault(currentTargetApp, true);
        boolean superBlock = superBlockExitMap.getOrDefault(currentTargetApp, false);

        final Map<String, Boolean> workingMap = new HashMap<String, Boolean>();
        workingMap.put("auto_enable", autoEnabled);
        workingMap.put("block_exit", blockExit);
        workingMap.put("crash_protect", crashProtect);
        workingMap.put("super_block", superBlock);
        final Map<String, Boolean> originalMap = new HashMap<String, Boolean>(workingMap);

        LinearLayout mainLayout = new LinearLayout(activity);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(40, 30, 40, 30);

        TextView tip = new com.install.appinstall.xl.ru.RuTextView(activity);
        String tipHtml = "请选择要启用的拦截功能：<br>" +
                "<small><b>保存后<font color='#2196F3'>立即生效</font> 异常请关闭！</small></b><br>" +
                "<font color='#FF5722'><small><b>如有异常将自动禁用对应功能项</small></b></font>";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            tip.setText(com.install.appinstall.xl.ru.RuStrings.fromHtml(tipHtml, Html.FROM_HTML_MODE_LEGACY));
        } else {
            tip.setText(com.install.appinstall.xl.ru.RuStrings.fromHtml(tipHtml));
        }
        tip.setTextColor(0xFF333333);
        tip.setTextSize(14);
        tip.setPadding(0, 0, 0, 20);
        mainLayout.addView(tip);

        final String[] keys = {"auto_enable", "block_exit", "crash_protect", "super_block"};
        final String[] labels = {
                "自动开启：捕获到新包名时开启普通拦截",
                "普通拦截：拦截应用常用的主流退出方案",
                "Java异常：全局捕获异常尽量吞噬数据",
                "超强拦截：增强普通拦截以外的补丁"
        };
        final Map<String, CheckBox> checkBoxMap = new HashMap<String, CheckBox>();

        for (int i = 0; i < keys.length; i++) {
            final String key = keys[i];
            final String label = labels[i];
            boolean isCurrentlyDisabled = !workingMap.getOrDefault(key, true);

            final CheckBox cb = new com.install.appinstall.xl.ru.RuCheckBox(activity);
            if ("auto_enable".equals(key)) {
                String displayText;
                if (!isCurrentlyDisabled) {
                    displayText = "<font color='#2196F3'><b>" + label + "</b></font>";
                } else {
                    displayText = "<font color='#F44336'><b>" + label + "</b></font>" +
                            "<font color='#FF5722'><small><b> (已禁用)</small></b></font>";
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    cb.setText(com.install.appinstall.xl.ru.RuStrings.fromHtml(displayText, Html.FROM_HTML_MODE_LEGACY));
                } else {
                    cb.setText(com.install.appinstall.xl.ru.RuStrings.fromHtml(displayText));
                }
            } else {
                updateCheckBoxText(cb, label, !isCurrentlyDisabled);
            }

            cb.setChecked(!isCurrentlyDisabled);
            cb.setEnabled(true);
            cb.setPadding(20, 10, 20, 10);
            cb.setTextSize(14);

            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    workingMap.put(key, isChecked);
                    ReaLog.log("exit_intercept", "退出拦截设置变更: " + label + " -> " + (isChecked ? "启用" : "禁用"));
                    if ("auto_enable".equals(key)) {
                        String displayText;
                        if (isChecked) {
                            displayText = "<font color='#2196F3'><b>" + label + "</b></font>";
                        } else {
                            displayText = "<font color='#F44336'><b>" + label + "</b></font>" +
                                    "<font color='#FF5722'><small><b> (已禁用)</small></b></font>";
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            cb.setText(com.install.appinstall.xl.ru.RuStrings.fromHtml(displayText, Html.FROM_HTML_MODE_LEGACY));
                        } else {
                            cb.setText(com.install.appinstall.xl.ru.RuStrings.fromHtml(displayText));
                        }
                    } else {
                        updateCheckBoxText(cb, label, isChecked);
                    }
                    if ("block_exit".equals(key) && isChecked) {
                        final CheckBox crashCb = checkBoxMap.get("crash_protect");
                        if (crashCb != null && !crashCb.isChecked()) {
                            crashCb.setChecked(true);
                            workingMap.put("crash_protect", true);
                            updateCheckBoxText(crashCb, "Java异常：全局捕获异常尽量吞噬数据", true);
                            ReaLog.log("exit_intercept", "普通拦截开启，智能联动开启捕获Java异常");
                        }
                    }
                }
            });

            mainLayout.addView(cb);
            checkBoxMap.put(key, cb);

            View divider = new View(activity);
            divider.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
            divider.setBackgroundColor(0xFFE0E0E0);
            mainLayout.addView(divider);
        }

        AlertDialog dialog = createBoundedDialog(
                activity,
                "退出拦截设置(Beta)",
                null,
                new String[]{"保存设置", "取消", "处理列表"},
                new DialogInterface.OnClickListener[]{
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface d, int which) {
                                if (originalMap.equals(workingMap)) {
                                    ToastUtil.showUnique(activity, "未修改设置");
                                    ReaLog.log("system", "保存退出拦截设置: 无变更");
                                    d.dismiss();
                                    showExitInterceptConfigDialog(activity, floatingView);
                                    return;
                                }

                                boolean autoNew = workingMap.get("auto_enable");
                                userDisabledAutoBlockMap.put(currentTargetApp, !autoNew);
                                blockExitMap.put(currentTargetApp, workingMap.get("block_exit"));
                                superBlockExitMap.put(currentTargetApp, workingMap.get("super_block"));
                                boolean crashNew = workingMap.get("crash_protect");
                                crashProtectEnabledMap.put(currentTargetApp, crashNew);
                                saveConfigToFile();
                                applyCrashProtect(crashNew, activity);
                                ReaLog.log("exit_intercept", "保存退出拦截设置: auto=" + autoNew +
                                        ", block=" + workingMap.get("block_exit") +
                                        ", super=" + workingMap.get("super_block") +
                                        ", crash=" + crashNew);

                                if (floatingView != null) {
                                    String statusText = installStatusMap.getOrDefault(currentTargetApp, true) ? "已安装" : "未安装";
                                    String blockText = (blockExitMap.getOrDefault(currentTargetApp, false) ||
                                            superBlockExitMap.getOrDefault(currentTargetApp, false)) ? "[拦截]" : "";
                                    floatingView.setText("安装防护(" + statusText + ")" + blockText);
                                }

                                ToastUtil.showUnique(activity, "✅ 配置已保存");
                                d.dismiss();
                            }
                        },
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface d, int which) {
                                ReaLog.log("system", "取消退出拦截设置");
                                d.dismiss();
                            }
                        },
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface d, int which) {
                                ReaLog.log("system", "打开拦截记录管理");
                                d.dismiss();
                                Spkill.showManagePatternsDialog(activity, HookInit.this);
                            }
                        }
                },
                mainLayout
        );
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

private void updateCheckBoxText(CheckBox cb, String label, boolean isEnabled) {
    String displayText;
    if (isEnabled) {
        // 启用状态：强制黑色
        displayText = "<font color='#000000'>" + label + "</font>";
    } else {
        // 禁用状态：红色加粗 + (已禁用) 小字
        displayText = "<b><font color='#F44336'>" + label + "</font>" +
                "<font color='#FF5722'><small><b> (已禁用)</small></b></font>";
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        cb.setText(com.install.appinstall.xl.ru.RuStrings.fromHtml(displayText, Html.FROM_HTML_MODE_LEGACY));
    } else {
        cb.setText(com.install.appinstall.xl.ru.RuStrings.fromHtml(displayText));
    }
}
 

    public void showStatusSwitchDialog(final Activity activity, final TextView floatingView) {
        ReaLog.log("system", "打开悬浮窗状态切换对话框");
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Boolean currentStatus = installStatusMap.get(currentTargetApp);
                    final boolean status = currentStatus != null ? currentStatus : true;
                    DetectedPackages detected = analyzeDetectedPackages();
                    int totalPackages = detected.installedPackages.size() + detected.notInstalledPackages.size();

                    StringBuilder msg = new StringBuilder();
                    msg.append("当前状态: ")
                            .append(status ? "<font color='#4CAF50'>【已安装】</font>" : "<font color='#F44336'>【未安装】</font>")
                            .append("<br>");
                    if (totalPackages > 0) {
                        msg.append("捕获应用总累计：<font color='#FF5722'><b>").append(totalPackages).append("</b></font><br><br>");
                        if (!detected.installedPackages.isEmpty()) {
                            msg.append("✅ 设置已安装的包(").append(detected.installedPackages.size()).append("项):<br>");
                            for (String pkg : detected.installedPackages)
                                msg.append("+ ").append(pkg).append("<br>");
                            msg.append("<br>");
                        }
                        if (!detected.notInstalledPackages.isEmpty()) {
                            msg.append("❌ 设置未安装的包(").append(detected.notInstalledPackages.size()).append("项):<br>");
                            for (String pkg : detected.notInstalledPackages)
                                msg.append("- ").append(pkg).append("<br>");
                            msg.append("<br>");
                        }
                    } else {
                        msg.append("📊 当前应用 未检测 到任何包<br><br>");
                    }

                    List<String> userPackages = userDefinedPackagesMap.getOrDefault(currentTargetApp, new ArrayList<String>());
                    List<String> excluded = excludedPackagesMap.getOrDefault(currentTargetApp, new ArrayList<String>());
                    List<String> fakeList = new ArrayList<String>();
                    for (String pkg : userPackages) if (!excluded.contains(pkg)) fakeList.add(pkg);
                    if (!fakeList.isEmpty()) {
                        msg.append("<font color='#4CAF50'>❣️ 手动添加的包名(").append(fakeList.size()).append("项):</font><br>");
                        for (String pkg : fakeList) {
                            String desc = "";
                            for (PackageConfig cfg : packageConfigMap.getOrDefault(currentTargetApp, new ArrayList<PackageConfig>())) {
                                if (cfg.packageName.equals(pkg)) {
                                    if ("installed".equals(cfg.statusMode)) desc = "（固定已安装）";
                                    else if ("not_installed".equals(cfg.statusMode))
                                        desc = "（固定未安装）";
                                    break;
                                }
                            }
                            msg.append("☆ ").append(pkg).append("<font color='#9E9E9E' size='2'>").append(desc).append("</font><br>");
                        }
                        msg.append("<br>");
                    }
                    if (!excluded.isEmpty()) {
                        msg.append("<font color='#F44336'>❌ 手动添加的排除包名(").append(excluded.size()).append("项):</font><br>");
                        for (String pkg : excluded) msg.append("☆ ").append(pkg).append("<br>");
                        msg.append("<br>");
                    }
                    msg.append("<font color='#9E9E9E'><small><b>请选择切换/进入设置：</b></small></font>");

                    AlertDialog dialog = createBoundedDialog(activity, "包名列表", msg.toString(),
                            new String[]{"切换为已安装", "切换为未安装", "更多配置设置"},
                            new DialogInterface.OnClickListener[]{
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface d, int which) {
                                            if (!status) {
                                                ReaLog.log("misc", "用户切换为已安装");
                                                handleStatusSwitch(activity, floatingView, true);
                                            } else {
                                                ToastUtil.showUnique(activity, "当前已是已安装状态");
                                                ReaLog.log("misc", "已是已安装状态，无需切换");
                                            }
                                        }
                                    },
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface d, int which) {
                                            if (status) {
                                                ReaLog.log("misc", "用户切换为未安装");
                                                handleStatusSwitch(activity, floatingView, false);
                                            } else {
                                                ToastUtil.showUnique(activity, "当前已是未安装状态");
                                                ReaLog.log("misc", "已是未安装状态，无需切换");
                                            }
                                        }
                                    },
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface d, int which) {
                                            d.dismiss();
                                            ReaLog.log("system", "打开更多配置设置");
                                            showMoreFunctionsDialog(activity, floatingView);
                                        }
                                    }
                            });
                    statusSwitchDialog = dialog;
                    dialog.show();
                } catch (Throwable t) {
                    log("显示状态切换对话框异常: " + t.getMessage());
                    showFallbackDialog(activity, floatingView, true);
                }
            }
        });
    }

    private void showFallbackDialog(final Activity activity, final TextView floatingView, final boolean status) {
        Activity validActivity = activity;
        if (validActivity == null || validActivity.isFinishing() ||
                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && validActivity.isDestroyed())) {
            if (ReaLog.sHookInit != null) {
                Activity current = ReaLog.sHookInit.getCurrentActivity();
                if (current != null && !current.isFinishing() &&
                        (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1 || !current.isDestroyed())) {
                    validActivity = current;
                }
            }
            if (validActivity == null) {
                Context ctx = ReaLog.sHookInit != null ? ReaLog.sHookInit.getApplicationContext() : null;
                if (ctx != null) ToastUtil.showUnique(ctx, "Activity 已失效，无法显示备用对话框");
                return;
            }
        }
        final Activity finalActivity = validActivity;

        if (finalActivity == null || finalActivity.isFinishing()) return;
        new com.install.appinstall.xl.ru.RuDialogBuilder(finalActivity, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                .setTitle("切换状态")
                .setItems(new String[]{"切换为已安装", "切换为未安装", "刷新配置"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean current = installStatusMap.getOrDefault(currentTargetApp, true);
                        switch (which) {
                            case 0:
                                if (!current) {
                                    ReaLog.log("system", "备用对话框: 切换为已安装");
                                    handleStatusSwitch(finalActivity, floatingView, true);
                                } else {
                                    ReaLog.log("system", "备用对话框: 当前已是已安装，无需切换");
                                }
                                break;
                            case 1:
                                if (current) {
                                    ReaLog.log("system", "备用对话框: 切换为未安装");
                                    handleStatusSwitch(finalActivity, floatingView, false);
                                } else {
                                    ReaLog.log("system", "备用对话框: 当前已是未安装，无需切换");
                                }
                                break;
                            case 2:
                                ReaLog.log("system", "备用对话框: 刷新配置");
                                refreshApplication(finalActivity);
                                break;
                        }
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ReaLog.log("system", "备用对话框: 取消");
                        dialog.dismiss();
                    }
                })
                .show();
        ReaLog.log("system", "备用对话框已显示");
    }

    private void showLaunchInterceptConfigDialog(final Activity activity, final TextView floatingView) {
        ReaLog.log("system", "打开启动拦截设置对话框");
        final boolean isLaunchInterceptEnabled = launchInterceptMap.getOrDefault(currentTargetApp, true);
        String currentStatus = isLaunchInterceptEnabled ? "<font color='#4CAF50'>已开启</font>" : "<font color='#F44336'>已关闭</font>";
        String htmlMessage = "当前启动拦截状态：" + currentStatus + "<br><br>" +
                "<font color='#9E9E9E'>• 开启：拦截所有第三方应用启动请求</font><br>" +
                "<font color='#9E9E9E'>• 关闭：允许应用正常启动第三方程序</font><br>" +
                "<font color='#9E9E9E'>• 列表：管理黑白名单/智能处理规则</font><br><br>";
        final AlertDialog dialog = new com.install.appinstall.xl.ru.RuDialogBuilder(activity, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                .setTitle("启动拦截设置")
                .setMessage(com.install.appinstall.xl.ru.RuStrings.fromHtml(htmlMessage))
                .setPositiveButton("开启启动拦截", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!launchInterceptMap.getOrDefault(currentTargetApp, true)) {
                            launchInterceptMap.put(currentTargetApp, true);
                            saveConfigToFile();
                            ReaLog.log("launch_intercept", "启动拦截: 开启");
                            ToastUtil.showUnique(activity, "✅ 启动拦截已开启");
                        } else {
                            ToastUtil.showUnique(activity, "当前已是开启状态");
                            ReaLog.log("system", "启动拦截: 已是开启状态");
                        }
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("关闭启动拦截", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (launchInterceptMap.getOrDefault(currentTargetApp, true)) {
                            launchInterceptMap.put(currentTargetApp, false);
                            saveConfigToFile();
                            ReaLog.log("launch_intercept", "启动拦截: 关闭");
                            ToastUtil.showUnique(activity, "❌ 启动拦截已关闭");
                        } else {
                            ToastUtil.showUnique(activity, "当前已是关闭状态");
                            ReaLog.log("system", "启动拦截: 已是关闭状态");
                        }
                        dialog.dismiss();
                    }
                })
                .setNeutralButton("处理列表", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ReaLog.log("system", "打开启动拦截处理列表");
                        dialog.dismiss();
                        instar.showManageListDialog(activity, HookInit.this, currentTargetApp);
                    }
                })
                .create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface d) {
                try {
                    Window window = dialog.getWindow();
                    if (window != null) {
                        WindowManager.LayoutParams params = window.getAttributes();
                        params.width = WindowManager.LayoutParams.MATCH_PARENT;
                        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
                        params.gravity = Gravity.CENTER;
                        params.horizontalMargin = 0.05f;
                        window.setAttributes(params);
                    }
                } catch (Exception e) {
                    ReaLog.log("system", "启动拦截对话框异常: " + e.getMessage());
                }
            }
        });
        if (activity != null && !activity.isFinishing() && !dialog.isShowing()) {
            dialog.show();
        }
    }

    private String getChangedKeys(Map<String, Boolean> original, Map<String, Boolean> current) {
        List<String> changed = new ArrayList<String>();
        for (Map.Entry<String, Boolean> entry : current.entrySet()) {
            boolean oldVal = original.getOrDefault(entry.getKey(), false);
            if (oldVal != entry.getValue()) {
                changed.add(entry.getKey() + ":" + (entry.getValue() ? "开" : "关"));
            }
        }
        String result = changed.isEmpty() ? "无变更" : String.join(", ", changed);
        ReaLog.log("misc", "子功能配置变更: " + result);
        return result;
    }

    private void showCustomPermissionDialog(final Activity activity, final TextView floatingView) {
        ReaLog.log("system", "打开权限防护设置对话框");
        final boolean currentGlobalFake = permissionFakeMap.getOrDefault(currentTargetApp, true);
        final boolean originalGlobalFake = currentGlobalFake;

        Map<String, Boolean> detailMap = permissionFakeDetailMap.get(currentTargetApp);
        if (detailMap == null) {
            detailMap = new HashMap<String, Boolean>();
            permissionFakeDetailMap.put(currentTargetApp, detailMap);
        }
        final List<String> allPermissions = getAppDeclaredPermissions();
        if (allPermissions.isEmpty()) {
            ToastUtil.showUnique(activity, "无法获取应用权限列表");
            ReaLog.log("permission", "无法获取应用权限列表");
            return;
        }

        final Map<String, Boolean> workingMap = new HashMap<String, Boolean>();
        for (String perm : allPermissions) {
            boolean defaultVal = currentGlobalFake && Arrays.asList(DETECTION_PERMISSIONS).contains(perm);
            if (detailMap.containsKey(perm)) {
                workingMap.put(perm, detailMap.get(perm));
            } else {
                workingMap.put(perm, defaultVal);
            }
        }
        final Map<String, Boolean> originalWorkingMap = new HashMap<String, Boolean>(workingMap);

        LinearLayout mainLayout = new LinearLayout(activity);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(30, 20, 30, 20);

        final TextView statusLine = new com.install.appinstall.xl.ru.RuTextView(activity);
        statusLine.setText("权限防护：防护中 | 假装已授权(0)项");
        statusLine.setTextColor(0xFF333333);
        statusLine.setTextSize(14);
        statusLine.setPadding(0, 0, 0, 15);
        mainLayout.addView(statusLine);

        TextView tipView = new com.install.appinstall.xl.ru.RuTextView(activity);
        tipView.setText("勾选对应权限将返回假装「已授权」\n取消全部勾选即关闭权限防护,放行应用检查\n请按需选择假装,部分权限假装后可能出现异常或无效！");
        tipView.setTextColor(0xFFFF0000);
        tipView.setTextSize(10);
        tipView.setPadding(0, 0, 0, 20);
        tipView.setGravity(Gravity.START);
        mainLayout.addView(tipView);

        LinearLayout topBar = new LinearLayout(activity);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setGravity(Gravity.CENTER_VERTICAL);

        final Button toggleSelectAllBtn = new com.install.appinstall.xl.ru.RuButton(activity);
        toggleSelectAllBtn.setText("全选");
        toggleSelectAllBtn.setPadding(20, 8, 20, 8);
        toggleSelectAllBtn.setTextColor(0xFFFFFFFF);
        toggleSelectAllBtn.setBackground(getRoundButtonDrawable(0xFF2196F3));

        final Button filterBtn = new com.install.appinstall.xl.ru.RuButton(activity);
        filterBtn.setText("默认权限");
        filterBtn.setPadding(20, 8, 20, 8);
        filterBtn.setTextColor(0xFFFFFFFF);
        filterBtn.setBackground(getRoundButtonDrawable(0xFF2196F3));
        final boolean[] filterDefaultOnly = new boolean[]{false};

        Button cleanConfigBtn = new com.install.appinstall.xl.ru.RuButton(activity);
        cleanConfigBtn.setText("清理配置");
        cleanConfigBtn.setPadding(20, 8, 20, 8);
        cleanConfigBtn.setTextColor(0xFFFFFFFF);
        cleanConfigBtn.setBackground(getRoundButtonDrawable(0xFFF44336));

        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        btnParams.setMargins(0, 0, 20, 0);
        topBar.addView(toggleSelectAllBtn, btnParams);
        topBar.addView(filterBtn, btnParams);
        topBar.addView(cleanConfigBtn, btnParams);
        mainLayout.addView(topBar);

        final ScrollView scrollView = new ScrollView(activity);
        scrollView.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
        scrollView.setFocusable(false);
        scrollView.setFocusableInTouchMode(false);
        final LinearLayout listContainer = new LinearLayout(activity);
        listContainer.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(listContainer);
        mainLayout.addView(scrollView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));

        final List<CheckBox> checkBoxList = new ArrayList<CheckBox>();

        final Runnable updateStatusLine = new Runnable() {
            @Override
            public void run() {
                int checkedCount = 0;
                for (Boolean v : workingMap.values()) {
                    if (v) checkedCount++;
                }
                boolean globalEnabled = (checkedCount > 0);
                String statusText = "权限防护：" + (globalEnabled ? "防护中" : "未防护") +
                        " | 假装已授权(" + checkedCount + ")项";
                statusLine.setText(statusText);
                statusLine.setTextColor(globalEnabled ? 0xFF4CAF50 : 0xFFF44336);
            }
        };

        final Runnable updateSelectAllButtonState = new Runnable() {
            @Override
            public void run() {
                boolean allChecked = true;
                for (CheckBox cb : checkBoxList) {
                    if (!cb.isChecked()) {
                        allChecked = false;
                        break;
                    }
                }
                if (allChecked) {
                    toggleSelectAllBtn.setText("取消全选");
                    toggleSelectAllBtn.setBackground(getRoundButtonDrawable(0xAAFF6347));
                } else {
                    toggleSelectAllBtn.setText("全选");
                    toggleSelectAllBtn.setBackground(getRoundButtonDrawable(0xAA2196F3));
                }
            }
        };

        final Runnable refreshList = new Runnable() {
            @Override
            public void run() {
                listContainer.removeAllViews();
                checkBoxList.clear();

                boolean filterOnlyDefault = filterDefaultOnly[0];

                for (final String perm : allPermissions) {
                    if (perm == null || perm.trim().isEmpty()) continue;
                    if (filterOnlyDefault && !Arrays.asList(DETECTION_PERMISSIONS).contains(perm)) {
                        continue;
                    }

                    final CheckBox cb = new com.install.appinstall.xl.ru.RuCheckBox(activity);
                    String friendly = PermissionName.getFriendlyName(perm);
                    if (friendly == null || friendly.trim().isEmpty()) {
                        friendly = perm;
                    }
                    cb.setText(friendly + "\n" + perm);
                    cb.setTextSize(12);
                    cb.setTextColor(0xFF000000);
                    cb.setPadding(20, 12, 20, 12);
                    cb.setSingleLine(false);
                    cb.setTag(perm);

                    cb.setChecked(workingMap.getOrDefault(perm, false));

                    cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            workingMap.put(perm, isChecked);
                            updateSelectAllButtonState.run();
                            updateStatusLine.run();
                            ReaLog.log("permission", "权限勾选变更: " + perm + " -> " + (isChecked ? "授权" : "放行"));
                        }
                    });

                    cb.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
                            android.content.ClipData clip = android.content.ClipData.newPlainText("permission", perm);
                            clipboard.setPrimaryClip(clip);
                            ToastUtil.showUnique(activity, "✅ 已复制\n" + perm);
                            ReaLog.log("permission", "复制权限名: " + perm);
                            return true;
                        }
                    });

                    listContainer.addView(cb);
                    checkBoxList.add(cb);

                    View divider = new View(activity);
                    divider.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
                    divider.setBackgroundColor(0xFFE0E0E0);
                    listContainer.addView(divider);
                }

                if (listContainer.getChildCount() == 0) {
                    TextView empty = new com.install.appinstall.xl.ru.RuTextView(activity);
                    empty.setText("没有匹配的权限");
                    empty.setPadding(20, 50, 20, 50);
                    empty.setTextColor(0xFF999999);
                    empty.setGravity(Gravity.CENTER);
                    listContainer.addView(empty);
                }
                updateSelectAllButtonState.run();
                updateStatusLine.run();
                ReaLog.log("permission", "权限列表刷新完成，总数: " + checkBoxList.size());
            }
        };

        filterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterDefaultOnly[0] = !filterDefaultOnly[0];
                if (filterDefaultOnly[0]) {
                    filterBtn.setText("全局权限");
                    filterBtn.setBackground(getRoundButtonDrawable(0xAAFF6347));
                    ReaLog.log("system", "切换到全局权限视图");
                } else {
                    filterBtn.setText("默认权限");
                    filterBtn.setBackground(getRoundButtonDrawable(0xAA2196F3));
                    ReaLog.log("system", "切换到默认权限视图");
                }
                refreshList.run();
            }
        });

        toggleSelectAllBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean currentlyAllChecked = true;
                for (CheckBox cb : checkBoxList) {
                    if (!cb.isChecked()) {
                        currentlyAllChecked = false;
                        break;
                    }
                }
                boolean targetState = !currentlyAllChecked;
                for (CheckBox cb : checkBoxList) {
                    cb.setChecked(targetState);
                    String perm = (String) cb.getTag();
                    workingMap.put(perm, targetState);
                }
                ReaLog.log("misc", "权限全选/取消全选: " + (targetState ? "全选" : "取消全选"));
                updateSelectAllButtonState.run();
                updateStatusLine.run();
            }
        });

        cleanConfigBtn.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        Map<String, Boolean> currentDetail = permissionFakeDetailMap.get(currentTargetApp);
        if (currentDetail == null || currentDetail.isEmpty()) {
            ToastUtil.showUnique(activity, "暂无自定义配置");
            return;
        }
        AlertDialog confirmDialog = createBoundedDialog(
                activity,
                "清理配置",
                "确定要清空所有<font color='#FF5722'><b>自定义勾选</b></font>的权限配置吗？<br><br>清空后将恢复默认勾选基础权限。",
                new String[]{"确认清理", "取消"},
                new DialogInterface.OnClickListener[]{
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface d, int which) {
                                ReaLog.log("misc", "清理自定义权限配置");
                                permissionFakeDetailMap.put(currentTargetApp, new HashMap<String, Boolean>());
                                permissionFakeMap.put(currentTargetApp, true);
                                for (String perm : allPermissions) {
                                    boolean isDefault = Arrays.asList(DETECTION_PERMISSIONS).contains(perm);
                                    workingMap.put(perm, isDefault);
                                }
                                // 同步更新原始状态，防止保存时提示“未修改”
                                originalWorkingMap.clear();
                                originalWorkingMap.putAll(workingMap);
                                
                                saveConfigToFile();
                                reHookPermissionMethods(activity.getClassLoader());
                                clearPermissionCache(activity);
                                sendPermissionChangeBroadcast(activity);
                                refreshList.run();
                                ToastUtil.showUnique(activity, "✅ 清理成功");
                                d.dismiss();
                            }
                        },
                        null
                }
        );
        confirmDialog.show();
    }
});

        refreshList.run();

        final AlertDialog dialog = createBoundedDialog(
                activity,
                "权限防护设置(Beta)",
                null,
                new String[]{"保存设置", "取消"},
                new DialogInterface.OnClickListener[]{
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface d, int which) {
                                Map<String, Boolean> currentUIState = new HashMap<String, Boolean>();
                                for (CheckBox cb : checkBoxList) {
                                    String perm = (String) cb.getTag();
                                    currentUIState.put(perm, cb.isChecked());
                                }

                                if (!currentUIState.equals(originalWorkingMap)) {
                                    Map<String, Boolean> newDetail = new HashMap<String, Boolean>();
                                    boolean anyFaked = false;
                                    for (Map.Entry<String, Boolean> entry : currentUIState.entrySet()) {
                                        String perm = entry.getKey();
                                        boolean isChecked = entry.getValue();
                                        boolean isDefault = Arrays.asList(DETECTION_PERMISSIONS).contains(perm);
                                        if (isChecked != isDefault) {
                                            newDetail.put(perm, isChecked);
                                        }
                                        if (isChecked) anyFaked = true;
                                    }

                                    boolean newGlobalFake = anyFaked;
                                    permissionFakeMap.put(currentTargetApp, newGlobalFake);
                                    permissionFakeDetailMap.put(currentTargetApp, newDetail);
                                    saveConfigToFile();

                                    ReaLog.log("misc", "保存权限设置: 全局=" + (newGlobalFake ? "开启" : "关闭") +
                                            ", 自定义=" + newDetail.size() + "项");

                                    reHookPermissionMethods(activity.getClassLoader());
                                    clearPermissionCache(activity);
                                    sendPermissionChangeBroadcast(activity);

                                    if (newGlobalFake != originalGlobalFake) {
                                        ToastUtil.showUnique(activity, newGlobalFake ? "✅ 权限防护已开启" : "❌ 权限防护已关闭");
                                        showPermissionFakeRefreshPrompt(activity, newGlobalFake);
                                    } else {
                                        ToastUtil.showUnique(activity, "✅ 配置已保存");
                                    }

                                    if (foatInstance != null) {
                                        foatInstance.updateFloatingView(activity);
                                    }
                                    d.dismiss();
                                } else {
                                    ToastUtil.showUnique(activity, "未修改设置");
                                    ReaLog.log("system", "保存权限设置: 无变更");
                                    d.dismiss();
                                    showCustomPermissionDialog(activity, floatingView);
                                }
                            }
                        },
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface d, int which) {
                                ReaLog.log("system", "取消权限设置");
                                d.dismiss();
                            }
                        }
                },
                mainLayout
        );
        dialog.show();
        ReaLog.log("system", "权限防护设置对话框已显示");
    }

    private void reHookPermissionMethods(ClassLoader classLoader) {
        if (mPrsprn != null) {
            mPrsprn.reHookPermissionMethods(classLoader);
        }
        Context context = getApplicationContext();
        if (context != null) {
            clearPermissionCache(context);
            sendPermissionChangeBroadcast(context);
        }
        ReaLog.log("permission", "权限防护：重载权限方法完成");
    }

private void clearPermissionCache(Context context) {
    try {
        PackageManager pm = context.getPackageManager();
        try {
            Method clearPreferredMethod = PackageManager.class.getDeclaredMethod("clearPackagePreferredActivities", String.class);
            clearPreferredMethod.setAccessible(true);
            clearPreferredMethod.invoke(pm, currentTargetApp);
        } catch (Throwable t) {
            // 忽略
        }
        try {
            Class<?> appClass = Class.forName(context.getPackageName() + ".App");
            Field cacheField = appClass.getDeclaredField("sPermissionCache");
            cacheField.setAccessible(true);
            Object cache = cacheField.get(null);
            if (cache instanceof Map) {
                ((Map<?, ?>) cache).clear();
            }
        } catch (Throwable t) {
            // 忽略
        }
        try {
            Class<?> permissionCheckerClass = Class.forName("androidx.core.content.PermissionChecker");
            Field sPermissionCacheField = permissionCheckerClass.getDeclaredField("sPermissionCache");
            sPermissionCacheField.setAccessible(true);
            Object cache = sPermissionCacheField.get(null);
            if (cache instanceof Map) {
                ((Map<?, ?>) cache).clear();
            }
        } catch (Throwable t) {
            // 忽略
        }
        // 清空 Prsprn 中的权限组缓存（去除 modifiers 反射）
        try {
            Class<?> prsprnClass = Class.forName("com.install.appinstall.xl.util.Prsprn");
            Field cacheField = prsprnClass.getDeclaredField("sPermissionGroupCache");
            cacheField.setAccessible(true);
            Object cache = cacheField.get(null);
            if (cache instanceof Map) {
                ((Map<?, ?>) cache).clear();
                ReaLog.log("permission", "权限防护：已清空权限组缓存");
            }
        } catch (Throwable t) {
            ReaLog.log("permission", "权限防护：清空权限组缓存失败: " + t.getMessage());
        }
    } catch (Throwable t) {
        ReaLog.log("permission", "权限防护：清空权限缓存异常: " + t.getMessage());
    }
}

    private void sendPermissionChangeBroadcast(Context context) {
        try {
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction("android.intent.action.PACKAGE_PERMISSION_CHANGED");
            broadcastIntent.setData(Uri.parse("package:" + currentTargetApp));
            broadcastIntent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
            context.sendBroadcast(broadcastIntent);
            Intent customIntent = new Intent();
            customIntent.setAction("com.install.appinstall.xl.PERMISSION_CHANGED");
            customIntent.putExtra("packageName", currentTargetApp);
            context.sendBroadcast(customIntent);
        } catch (Throwable t) {
            log("发送权限广播异常: " + t.getMessage());
        }
    }

    private void showPermissionFakeRefreshPrompt(final Activity activity, final boolean enabled) {
        ReaLog.log("system", "权限防护: " + (enabled ? "已开启" : "已关闭"));
        if (activity == null || activity.isFinishing()) {
            return;
        }
        try {
            String title = enabled ? "权限防护已开启" : "权限防护已关闭";
            String message = enabled
                    ? "✅ <font color='#4CAF50'>权限防护功能已开启</font><br><br>" +
                    "功能效果：<br>" +
                    "• 勾选的权限时返回虚假已授权<br>" +
                    "• 部分应用可能需要重启或无效<br><br>" +
                    "是否立即刷新应用使设置生效？"
                    : "❌ <font color='#F44336'>权限防护功能已关闭</font><br><br>" +
                    "功能效果：<br>" +
                    "• 取消勾选应用权限时返回真实状态<br>" +
                    "• (比如当前应用没有授权,应用能检测到)<br><br>" +
                    "• 部分应用可能因此拒绝运行或无效<br>" +
                    "是否立即刷新应用使设置生效？";
            AlertDialog dialog = createBoundedDialog(
                    activity,
                    title,
                    message,
                    new String[]{"立即刷新", "稍后"},
                    new DialogInterface.OnClickListener[]{
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ReaLog.log("misc", "用户选择立即刷新(权限)");
                                    refreshApplication(activity);
                                }
                            },
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ReaLog.log("misc", "用户选择稍后刷新(权限)");
                                    dialog.dismiss();
                                }
                            }
                    }
            );
            dialog.show();
        } catch (Throwable t) {
            log("显示权限防护刷新提示异常: " + t.getMessage());
        }
    }

    private Drawable getRoundButtonDrawable(int color) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(25f);
        return drawable;
    }

    private List<String> getAppDeclaredPermissions() {
        List<String> permList = new ArrayList<>();
        try {
            Context context = (Context) XposedHelpers.callStaticMethod(
                    XposedHelpers.findClass("android.app.ActivityThread", null),
                    "currentApplication"
            );
            if (context == null) return permList;
            PackageManager pm = context.getPackageManager();
            PackageInfo pkgInfo = pm.getPackageInfo(currentTargetApp, PackageManager.GET_PERMISSIONS);
            if (pkgInfo.requestedPermissions != null) {
                permList.addAll(Arrays.asList(pkgInfo.requestedPermissions));
            }
        } catch (Throwable t) {
            log("获取声明列表失败: " + t.getMessage());
            ReaLog.log("system", "获取声明列表失败: " + t.getMessage());
        }
        return permList;
    }

    public void showHideDialog(final Activity activity, final View floatingView) {
        ReaLog.log("system", "打开管理配置对话框");
        try {
            LinearLayout titleLayout = new LinearLayout(activity);
            titleLayout.setOrientation(LinearLayout.HORIZONTAL);
            titleLayout.setGravity(Gravity.CENTER_VERTICAL);
            titleLayout.setPadding(80, 20, 20, 20);

            TextView titleText = new com.install.appinstall.xl.ru.RuTextView(activity);
            titleText.setText("管理配置/悬浮窗设置");
            titleText.setTextSize(18);
            titleText.setTextColor(0xFF333333);
            titleText.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
            titleText.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1));

            Button permHideBtn = new com.install.appinstall.xl.ru.RuButton(activity);
            permHideBtn.setText("隐藏悬浮窗(长期)");
            permHideBtn.setTextSize(12);
            permHideBtn.setPadding(20, 20, 20, 20);
            permHideBtn.setTextColor(0xFFFFFFFF);
            try {
                Class<?> gradientDrawableClass = Class.forName("android.graphics.drawable.GradientDrawable");
                Object gd = gradientDrawableClass.newInstance();
                Method setColor = gradientDrawableClass.getMethod("setColor", int.class);
                Method setCornerRadius = gradientDrawableClass.getMethod("setCornerRadius", float.class);
                setColor.invoke(gd, 0xAAF44336);
                setCornerRadius.invoke(gd, 25f);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    permHideBtn.setBackground((android.graphics.drawable.Drawable) gd);
                } else {
                    permHideBtn.setBackgroundDrawable((android.graphics.drawable.Drawable) gd);
                }
            } catch (Throwable e) {
                permHideBtn.setBackgroundColor(0xAAF44336);
            }
            LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            int marginPx = (int) (8 * activity.getResources().getDisplayMetrics().density);
            btnParams.setMargins(marginPx, marginPx, marginPx, marginPx);
            permHideBtn.setLayoutParams(btnParams);
            titleLayout.addView(titleText);
            titleLayout.addView(permHideBtn);

            String message = "【隐藏】隐藏应用内的悬浮窗显示功能<br>" +
                    "<font color='#9E9E9E'><b>· [临时] 隐藏后后通过双击音量键恢复显示</b></font><br>" +
                    "<font color='#9E9E9E'><b>· [长期] 隐藏后通过三击音量键+/-恢复显示</b></font><br>" +
                    "<font color='#F44336'><small>—————————————————</small></font><br>" +
                    "【清理配置】清理自动捕获+自定义添加的数据<br>" +
                    "<font color='#9E9E9E'><b>清理后会自动捕获新数据/自定义包名需重新添加</b></font><br><br>" +
                    "<font color='#F44336'><small><b>请选择操作：</b></small></font>";

            final AlertDialog[] dialogHolder = new AlertDialog[1];

            AlertDialog dialog = createBoundedDialog(
                    activity,
                    null,
                    message,
                    new String[]{"临时隐藏", "清理配置", "取消"},
                    new DialogInterface.OnClickListener[]{new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface d, int which) {
                            ReaLog.log("misc", "用户选择临时隐藏悬浮窗");
                            if (foatInstance != null) {
                                foatInstance.tempHideFloatingView(activity, floatingView);
                            } else {
                                ToastUtil.showUnique(activity, "悬浮窗模块未初始化");
                            }
                            d.dismiss();
                        }
                    },
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface d, int which) {
                                    d.dismiss();
                                    ReaLog.log("misc", "用户选择清理配置");
                                    List<String> userPackages = userDefinedPackagesMap.getOrDefault(currentTargetApp, new ArrayList<String>());
                                    if (userPackages.isEmpty()) {
                                        clearAllPackageLists(activity, floatingView);
                                    } else {
                                        showClearConfirmDialog(activity, floatingView);
                                    }
                                }
                            },
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface d, int which) {
                                    ReaLog.log("system", "关闭管理配置对话框");
                                    d.dismiss();
                                }
                            }
                    }
            );
            dialogHolder[0] = dialog;

            permHideBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ReaLog.log("floating", "用户选择长期隐藏悬浮窗");
                    foatInstance.hideFloatingView(activity, floatingView);
                    if (dialogHolder[0] != null && dialogHolder[0].isShowing()) {
                        dialogHolder[0].dismiss();
                    }
                }
            });

            dialog.setCustomTitle(titleLayout);
            dialog.show();
        } catch (Throwable t) {
            log("❌ 显示隐藏对话框异常: " + t.getMessage());
        }
    }

    private void clearAllPackageLists(final Activity activity, final View floatingView) {
        try {
            ReaLog.log("system", "打开清理所有包名列表");

            synchronized (globalCapturedPackages) {
                globalCapturedPackages.clear();
            }

            appCapturedPackages.clear();

            List<InterceptPattern> patterns = interceptPatternsMap.get(currentTargetApp);
            if (patterns != null) {
                for (InterceptPattern pattern : patterns) {
                    pattern.installedPackages.clear();
                    pattern.notInstalledPackages.clear();
                    pattern.choiceCount = 0;
                    pattern.silentIntercept = false;
                    pattern.recentChoices.clear();
                }
            }

            userDefinedPackagesMap.put(currentTargetApp, new ArrayList<String>());
            packageConfigMap.put(currentTargetApp, new ArrayList<PackageConfig>());
            excludedPackagesMap.put(currentTargetApp, new ArrayList<String>());
            clearSmartFakeCache();
            sessionChoiceMap.clear();
            pathPackageCache.clear();
            if (mPkgMgr != null) {
                mPkgMgr.invalidateAppDirCache();
            }
            packageStatusCache.clear();
            mSystemCoreCache.clear();
            detectedCache.clear();
            mRealSystemCache.clear();

            saveConfigToFile();

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String message = "<b>✅ 清理完成</b><br>" +
                                "<small>• 已清空全局包列表</small>" +
                                "<small>• 已清空当前应用包列表<br></small>" +
                                "<small>• 已清空用户自定义包名</small>" +
                                "<small>• 已清空包名独立配置<br></small>" +
                                "<small>• 已重置包名数据缓存</small>" +
                                "<small>• 拦截模式计数已重置<br></small>" +
                                "<b>• 配置已保存</b>";
                        ToastUtil.showUnique(activity, message, true);
                        if (floatingView != null) {
                            showRefreshConfirmDialog(activity, null);
                        }
                        ReaLog.log("misc", "数据缓存清理完成");
                    } catch (Throwable t) {
                        ToastUtil.showUnique(activity, "✅ 清理完成");
                    }
                }
            });

            log("✅ 清理所有包列表完成");
        } catch (Throwable t) {
            log("清理包列表异常: " + t.getMessage());
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtil.showUnique(activity, "❌清理失败");
                }
            });
        }
    }

    private void clearSmartFakeCache() {
        if (mPkgMgr != null) {
            mPkgMgr.clearSmartFakeCache();
        }
    }

    public static AlertDialog createBoundedDialog(final Activity activity, String title, String message,
            String[] buttonTexts, DialogInterface.OnClickListener[] listeners) {
        return createBoundedDialog(activity, title, message, buttonTexts, listeners, null);
    }

    public static AlertDialog createBoundedDialog(final Activity activity, String title, String message,
            String[] buttonTexts, DialogInterface.OnClickListener[] listeners,
            final View customView) {
        long now = System.currentTimeMillis();
        synchronized (HookInit.class) {
            if (now - sLastDialogCreateTime < DIALOG_DEDUP_WINDOW_MS) {
                if (sLastDialogRef != null) {
                    AlertDialog old = sLastDialogRef.get();
                    if (old != null && old.isShowing()) {
                        try {
                            old.dismiss();
                        } catch (Throwable ignored) {
                        }
                    }
                    sLastDialogRef = null;
                }
            }
            sLastDialogCreateTime = now;
        }

        Activity validActivity = activity;
        if (validActivity == null || validActivity.isFinishing() ||
                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && validActivity.isDestroyed())) {
            if (ReaLog.sHookInit != null) {
                Activity current = ReaLog.sHookInit.getCurrentActivity();
                if (current != null && !current.isFinishing() &&
                        (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1 || !current.isDestroyed())) {
                    validActivity = current;
                }
            }
            if (validActivity == null) {
                Context ctx = ReaLog.sHookInit != null ? ReaLog.sHookInit.getApplicationContext() : null;
                if (ctx != null) {
                    ToastUtil.showUnique(ctx, "Activity 已失效，无法显示对话框");
                }
                return null;
            }
        }
        final Activity finalActivity = validActivity;

        AlertDialog.Builder builder = new com.install.appinstall.xl.ru.RuDialogBuilder(finalActivity, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT).setTitle(title);
        if (customView != null) {
            if (customView.getParent() != null) {
                ((ViewGroup) customView.getParent()).removeView(customView);
            }
            ScrollView scrollView = new ScrollView(finalActivity);
            scrollView.setVerticalScrollBarEnabled(false);
            scrollView.setHorizontalScrollBarEnabled(false);
            scrollView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            scrollView.setFillViewport(true);
            scrollView.addView(customView);
            builder.setView(scrollView);
        } else if (message != null && !message.isEmpty()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                builder.setMessage(com.install.appinstall.xl.ru.RuStrings.fromHtml(message, Html.FROM_HTML_MODE_LEGACY));
            } else {
                builder.setMessage(com.install.appinstall.xl.ru.RuStrings.fromHtml(message));
            }
        }

        if (buttonTexts != null) {
            int len = buttonTexts.length;
            DialogInterface.OnClickListener[] safeListeners = new DialogInterface.OnClickListener[len];
            if (listeners != null) {
                int copyLen = Math.min(listeners.length, len);
                System.arraycopy(listeners, 0, safeListeners, 0, copyLen);
            }
            if (len >= 1) builder.setPositiveButton(buttonTexts[0], safeListeners[0]);
            if (len >= 2) builder.setNegativeButton(buttonTexts[1], safeListeners[1]);
            if (len >= 3) builder.setNeutralButton(buttonTexts[2], safeListeners[2]);
        }

        final AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);

        synchronized (HookInit.class) {
            sLastDialogRef = new WeakReference<AlertDialog>(dialog);
        }

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface d) {
                synchronized (HookInit.class) {
                    if (sLastDialogRef != null && sLastDialogRef.get() == dialog) {
                        sLastDialogRef = null;
                    }
                }
            }
        });

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                try {
                    Window window = dialog.getWindow();
                    if (window != null) {
                        WindowManager.LayoutParams params = window.getAttributes();
                        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                        params.dimAmount = 0.4f;
                        params.width = WindowManager.LayoutParams.MATCH_PARENT;
                        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
                        params.gravity = Gravity.CENTER;
                        params.horizontalMargin = 0.05f;
                        window.setAttributes(params);
                    }
                    if (customView == null) {
                        try {
                            final TextView messageView = dialog.findViewById(android.R.id.message);
                            if (messageView != null) {
                                messageView.setTextIsSelectable(true);
                                messageView.setFocusable(true);
                                messageView.setFocusableInTouchMode(true);
                                messageView.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        messageView.requestFocus();
                                    }
                                }, 50);
                            }
                        } catch (Throwable ignored) {
                        }
                    }
                } catch (Throwable t) {
                    ReaLog.log("system", "设置对话框异常: " + t.getMessage());
                }
            }
        });

        return dialog;
    }

    private void handleStatusSwitch(Activity activity, TextView floatingView, boolean newStatus) {
        try {
            installStatusMap.put(currentTargetApp, newStatus);

            detectedCache.clear();
            packageStatusCache.clear();
            mSystemCoreCache.clear();
            mRealSystemCache.clear();
            if (mPkgMgr != null) {
                mPkgMgr.invalidateAppDirCache();
            }
            saveConfigImmediate();

            if (floatingView != null) {
                String statusText = newStatus ? "已安装" : "未安装";
                boolean isBlockingExit = blockExitMap.getOrDefault(currentTargetApp, false);
                String blockText = isBlockingExit ? "[拦截]" : "";
                floatingView.setText("安装防护(" + statusText + ")" + blockText);
                floatingView.setBackgroundColor(newStatus ? 0xAA4CAF50 : 0xAAF44336);
            }

            ToastUtil.showUnique(activity, "切换为" + (newStatus ? "已安装" : "未安装") + "状态");
            showRefreshPrompt(activity, "切换成功");
            log("状态切换: " + (newStatus ? "已安装" : "未安装"));
            ReaLog.log("system", "状态切换: " + (newStatus ? "已安装" : "未安装"));
        } catch (Throwable t) {
            log("处理状态切换异常: " + t.getMessage());
            ToastUtil.showUnique(activity, "切换失败");
        }
    }

    private void showRefreshPrompt(final Activity activity, final String message) {
        if (activity == null || activity.isFinishing()) {
            return;
        }
        try {
            AlertDialog dialog = createBoundedDialog(activity, "状态切换成功",
                    message + "<br><br>是否立即刷新应用使状态生效？",
                    new String[]{"立即刷新", "稍后"},
                    new DialogInterface.OnClickListener[]{
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ReaLog.log("system", "用户选择立即刷新(状态切换)");
                                    refreshApplication(activity);
                                }
                            },
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ReaLog.log("system", "用户选择稍后刷新(状态切换)");
                                    dialog.dismiss();
                                }
                            },
                    }
            );
            dialog.show();
        } catch (Throwable t) {
            log("显示刷新提示异常: " + t.getMessage());
        }
    }

    private void showRefreshConfirmDialog(final Activity activity, final String customMessage) {
        if (activity == null || activity.isFinishing()) {
            ReaLog.log("system", "刷新对话框: Activity无效");
            return;
        }
        try {
            String message;
            if (customMessage != null && !customMessage.isEmpty()) {
                message = customMessage + "<br><br>是否立即刷新应用使配置生效？";
            } else {
                message = "这将重新加载配置文件并更新显示，<br>" +
                        "使状态切换立即生效。<br><br>" +
                        "确定要刷新吗？";
            }
            AlertDialog dialog = createBoundedDialog(activity, "刷新应用", message,
                    new String[]{"立即刷新", "取消"},
                    new DialogInterface.OnClickListener[]{
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ReaLog.log("system", "用户选择立即刷新");
                                    refreshApplication(activity);
                                }
                            },
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ReaLog.log("system", "用户取消刷新");
                                    dialog.dismiss();
                                }
                            }
                    }
            );
            dialog.show();
        } catch (Throwable t) {
            log("显示刷新对话框异常: " + t.getMessage());
        }
    }

    private void refreshApplication(final Activity activity) {
        try {
            detectedCache.clear();
            packageStatusCache.clear();
            mSystemCoreCache.clear();
            mRealSystemCache.clear();
            if (mPkgMgr != null) {
                mPkgMgr.invalidateAppDirCache();
            }

            installStatusMap.remove(currentTargetApp);
            floatingShownMap.remove(currentTargetApp);
            loadConfigFromFile();

            if (foatInstance != null) {
                foatInstance.updateFloatingView(activity);
            }

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Boolean currentStatus = installStatusMap.get(currentTargetApp);
                        String statusText = "未知";
                        if (currentStatus != null) {
                            statusText = currentStatus ? "已安装" : "未安装";
                        }
                        String toastMessage = "✅ 刷新完成<br>当前状态: " + statusText;
                        ToastUtil.showUnique(activity, toastMessage, true);
                        ReaLog.log("system", "刷新成功,当前状态：" + statusText);
                    } catch (Throwable e) {
                        Boolean currentStatus = installStatusMap.get(currentTargetApp);
                        String statusText = currentStatus != null ? (currentStatus ? "已安装" : "未安装") : "未知";
                        ToastUtil.showUnique(activity, "刷新完成\n当前状态: " + statusText);
                    }
                }
            });
        } catch (Throwable t) {
            log("刷新应用异常: " + t.getMessage());
            ReaLog.log("system", "刷新应用异常: " + t.getMessage());
            activity.runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            ToastUtil.showUnique(activity, "刷新失败");
                        }
                    }
            );
        }
    }

    public void updateFloatingView(final Activity activity) {
        if (foatInstance != null) {
            foatInstance.updateFloatingView(activity);
        }
    }
    

    private void applyCrashProtect(boolean enable, Context context) {
        ReaLog.log("misc", "应用Java异常: " + (enable ? "启用" : "禁用"));
        if (!enable) {
            Thread.setDefaultUncaughtExceptionHandler(null);
            return;
        }

        Context appCtx = context != null ? context.getApplicationContext() : getApplicationContext();
        if (appCtx == null && sCachedAppContext != null) appCtx = sCachedAppContext;
        if (appCtx != null) sCachedAppContext = appCtx;

        Intent launchIntent = null;
        if (appCtx != null) {
            try {
                launchIntent = appCtx.getPackageManager().getLaunchIntentForPackage(appCtx.getPackageName());
                if (launchIntent != null) sCachedLaunchIntent = launchIntent;
            } catch (Throwable ignored) {
            }
        }
        if (launchIntent == null && sCachedLaunchIntent != null) {
            launchIntent = sCachedLaunchIntent;
        }

        final Context finalCtx = appCtx;
        final Intent finalLaunchIntent = launchIntent;
        final boolean canRestart = (finalCtx != null && finalLaunchIntent != null);

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                if (isHandlingCrash) return;
                isHandlingCrash = true;

                try {
                    String msg = "捕获崩溃: " + e.getClass().getName() + " - " + e.getMessage();
                    ReaLog.log("exit_intercept", msg);
                } catch (Throwable ignored) {
                }

                try {
                    android.util.Log.e("CrashProtect", "崩溃: " + e.getClass().getName());
                    if (e.getMessage() != null) {
                        android.util.Log.e("CrashProtect", "原因: " + e.getMessage());
                    }
                } catch (Throwable ignored) {
                }

                long now = System.currentTimeMillis();
                if (now - sLastCrashTime > 60_000) {
                    sCrashCount = 0;
                }
                sCrashCount++;
                sLastCrashTime = now;

                if (sCrashCount >= 3) {
                    android.util.Log.e("CrashProtect", "熔断触发（连续3次），交给系统处理");
                    try {
                        doSaveConfigToFile();
                    } catch (Throwable ignored) {
                    }
                    Thread.UncaughtExceptionHandler defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
                    if (defaultHandler != null && defaultHandler != this) {
                        defaultHandler.uncaughtException(t, e);
                    } else {
                        Process.killProcess(Process.myPid());
                        System.exit(0);
                    }
                    return;
                }

                try {
                    doSaveConfigToFile();
                } catch (Throwable ignored) {
                }

                if (canRestart && finalCtx != null && finalLaunchIntent != null) {
                    try {
                        Thread.sleep(300);
                        finalLaunchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        finalCtx.startActivity(finalLaunchIntent);
                    } catch (Throwable ignored) {
                        android.util.Log.e("CrashProtect", "重启失败");
                    } finally {
                        Process.killProcess(Process.myPid());
                        System.exit(0);
                    }
                } else {
                    android.util.Log.e("CrashProtect", "无法重启，静默退出");
                    Process.killProcess(Process.myPid());
                    System.exit(0);
                }
            }
        });
    }

    private void hookIndirectExitMethods(ClassLoader classLoader) {
        try {
            XposedHelpers.findAndHookMethod(
                    "java.lang.reflect.Method",
                    classLoader,
                    "invoke",
                    Object.class,
                    Object[].class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            Method method = (Method) param.thisObject;
                            if (method.getDeclaringClass().getName().equals("java.lang.System") &&
                                    method.getName().equals("exit")) {
                                param.setResult(null);
                                param.setThrowable(null);
                                Spkill.handleAppExit(HookInit.this, "反射调用 System.exit()", param);
                            }
                        }
                    }
            );

            XposedHelpers.findAndHookMethod(
                    "java.lang.Runtime",
                    classLoader,
                    "exit",
                    int.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            param.setResult(null);
                            param.setThrowable(null);
                            Spkill.handleAppExit(HookInit.this, "Runtime.exit()", param);
                        }
                    }
            );

            XposedHelpers.findAndHookMethod(
                    "java.lang.System",
                    classLoader,
                    "loadLibrary",
                    String.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            String libName = (String) param.args[0];
                            if (libName.contains("native") ||
                                    libName.contains("exit") ||
                                    libName.contains("kill")) {
                            }
                        }
                    }
            );
        } catch (Throwable t) {
        }
    }

    private void hookGlobalExitSources(ClassLoader classLoader) {
        try {
            XposedHelpers.findAndHookMethod(
                    "android.os.Handler",
                    classLoader,
                    "post",
                    Runnable.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            boolean blockExit = blockExitMap.getOrDefault(currentTargetApp, false);
                            boolean superBlock = superBlockExitMap.getOrDefault(currentTargetApp, false);
                            if (!blockExit && !superBlock) {
                                return;
                            }
                            Runnable runnable = (Runnable) param.args[0];
                            String runnableStr = runnable.toString().toLowerCase();
                            if (runnableStr.contains("exit") || runnableStr.contains("kill") || runnableStr.contains("finish")) {
                                DetectedPackages detected = analyzeDetectedPackages();
                                if (!detected.installedPackages.isEmpty() || !detected.notInstalledPackages.isEmpty()) {
                                    param.setResult(false);
                                    ReaLog.log("exit_intercept", "拦截退出任务: " + runnableStr);
                                }
                            }
                        }
                    }
            );

            try {
                XposedHelpers.findAndHookMethod(
                        "android.app.Activity",
                        classLoader,
                        "onResume",
                        new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param)
                                    throws Throwable {
                                Activity activity = (Activity) param.thisObject;
                                ActivityManager am = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
                                if (am != null) {
                                    try {
                                        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
                                        if (!tasks.isEmpty() && tasks.get(0).topActivity.getPackageName().equals(currentTargetApp)) {
                                            ReaLog.log("system", "检测到目标应用: " + tasks.get(0).topActivity.getPackageName()
                                                    + ", 当前页面: " + activity.getClass().getName());
                                        }
                                    } catch (SecurityException e) {
                                        ReaLog.log("system", "记录获取任务列表无权限");
                                    }
                                }
                            }
                        }
                );
            } catch (Throwable t) {
            }

            try {
                XposedHelpers.findAndHookMethod(
                        "java.lang.Thread",
                        classLoader,
                        "run",
                        new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param)
                                    throws Throwable {
                                Thread thread = (Thread) param.thisObject;
                                String threadName = thread.getName().toLowerCase();
                                if (threadName.contains("check") ||
                                        threadName.contains("detect") ||
                                        threadName.contains("exit")) {
                                    ReaLog.log("exit_intercept", "检测到可疑线程: " + threadName);
                                }
                            }
                        }
                );
            } catch (Throwable t) {
            }

        } catch (Throwable t) {
            log("退出拦截初始化失败: " + t.getMessage());
            ReaLog.log("exit_intercept", "退出拦截初始化失败: " + t.getMessage());
        }
    }

    private void hookRunnableSystemExit(ClassLoader classLoader) {
        try {
            XposedHelpers.findAndHookMethod(
                    "java.lang.Runnable",
                    classLoader,
                    "run",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(
                                final XC_MethodHook.MethodHookParam param)
                                throws Throwable {
                            boolean blockExit = blockExitMap.getOrDefault(currentTargetApp, false);
                            boolean superBlock = superBlockExitMap.getOrDefault(currentTargetApp, false);
                            if (!blockExit && !superBlock) {
                                return;
                            }

                            DetectedPackages detected = analyzeDetectedPackages();
                            if (detected.installedPackages.isEmpty() &&
                                    detected.notInstalledPackages.isEmpty()) {
                                return;
                            }

                            ReaLog.log("exit_intercept", "检测到 System.exit 调用，尝试拦截");

                            XC_MethodHook exitHook = new XC_MethodHook() {
                                @Override
                                protected void beforeHookedMethod(
                                        XC_MethodHook.MethodHookParam exitParam)
                                        throws Throwable {
                                    exitParam.setResult(null);
                                    exitParam.setThrowable(null);
                                    ReaLog.log("exit_intercept", "调用 System.exit() 已拦截");
                                    new Handler(Looper.getMainLooper()).post(
                                            new Runnable() {
                                                @Override
                                                public void run() {
                                                    Activity activity = getCurrentActivity();
                                                    if (activity != null) {
                                                        ToastUtil.showUnique(activity, "已拦截异步退出");
                                                    }
                                                }
                                            }
                                    );
                                }
                            };

                            Method exitMethod = Class.forName("java.lang.System").getMethod(
                                    "exit",
                                    int.class
                            );
                            XposedBridge.hookMethod(exitMethod, exitHook);
                            try {
                                Runnable originalRunnable = (Runnable) param.thisObject;
                                originalRunnable.run();
                                ReaLog.log("exit_intercept", "拦截exit 执行完成");
                            } catch (Throwable t) {
                            } finally {
                                XposedBridge.unhookMethod(exitMethod, exitHook);
                            }
                            param.setResult(null);
                        }
                    }
            );
        } catch (Throwable t) {
        }
    }

    private void hookActivityDefaultBack(ClassLoader classLoader) {
        Class<?> fragmentActivityClass = null;
        try {
            fragmentActivityClass = Class.forName("androidx.fragment.app.FragmentActivity");
        } catch (Throwable ignored) {
        }
        final Class<?> cachedFragmentActivityClass = fragmentActivityClass;

        class BackPressHandler {
            private long lastPressTime = 0;
            private static final long DEBOUNCE_MS = 200;

            boolean handleBefore(Activity activity, MethodHookParam param) {
                if (!forceDefaultBackMap.getOrDefault(currentTargetApp, false)) {
                    return false;
                }

                long now = System.currentTimeMillis();
                if (now - lastPressTime < DEBOUNCE_MS) {
                    param.setResult(true);
                    return true;
                }
                lastPressTime = now;

                if (activity == null || activity.isFinishing()
                        || (Build.VERSION.SDK_INT >= 17 && activity.isDestroyed())) {
                    param.setResult(true);
                    return true;
                }

                if (cachedFragmentActivityClass != null && cachedFragmentActivityClass.isInstance(activity)) {
                    try {
                        Object fm = XposedHelpers.callMethod(activity, "getSupportFragmentManager");
                        int count = (int) XposedHelpers.callMethod(fm, "getBackStackEntryCount");
                        if (count > 0) {
                            XposedHelpers.callMethod(fm, "popBackStackImmediate");
                            param.setResult(true);
                            ReaLog.log("back_key", "返回键消费回退栈");
                            return true;
                        }
                    } catch (Throwable ignored) {
                    }
                }

                try {
                    Object nativeFm = XposedHelpers.callMethod(activity, "getFragmentManager");
                    if (nativeFm != null) {
                        int count = (int) XposedHelpers.callMethod(nativeFm, "getBackStackEntryCount");
                        if (count > 0) {
                            XposedHelpers.callMethod(nativeFm, "popBackStackImmediate");
                            param.setResult(true);
                            ReaLog.log("back_key", "返回键消费原生回退栈");
                            return true;
                        }
                    }
                } catch (Throwable ignored) {
                }

                return false;
            }

            void handleAfter(Activity activity) {
                if (!forceDefaultBackMap.getOrDefault(currentTargetApp, false)) {
                    return;
                }

                if (activity == null || activity.isFinishing()
                        || (Build.VERSION.SDK_INT >= 17 && activity.isDestroyed())) {
                    return;
                }
                activity.finish();
                ReaLog.log("back_key", "返回键触发 Activity.finish()");
            }
        }

        final BackPressHandler handler = new BackPressHandler();

        try {
            XposedHelpers.findAndHookMethod(
                    "android.app.Activity",
                    classLoader,
                    "onBackPressed",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            Activity activity = (Activity) param.thisObject;
                            handler.handleBefore(activity, param);
                        }

                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            Activity activity = (Activity) param.thisObject;
                            handler.handleAfter(activity);
                        }
                    }
            );

            XposedHelpers.findAndHookMethod(
                    "android.app.Activity",
                    classLoader,
                    "dispatchKeyEvent",
                    android.view.KeyEvent.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            android.view.KeyEvent event = (android.view.KeyEvent) param.args[0];
                            if (event.getKeyCode() == android.view.KeyEvent.KEYCODE_BACK
                                    && event.getAction() == android.view.KeyEvent.ACTION_DOWN
                                    && !event.isLongPress()) {
                                Activity activity = (Activity) param.thisObject;
                                handler.handleBefore(activity, param);
                            }
                        }

                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            android.view.KeyEvent event = (android.view.KeyEvent) param.args[0];
                            if (event.getKeyCode() == android.view.KeyEvent.KEYCODE_BACK
                                    && event.getAction() == android.view.KeyEvent.ACTION_DOWN) {
                                Activity activity = (Activity) param.thisObject;
                                handler.handleAfter(activity);
                            }
                        }
                    }
            );

            try {
                XposedHelpers.findAndHookMethod(
                        "android.app.Activity",
                        classLoader,
                        "onKeyDown",
                        int.class,
                        android.view.KeyEvent.class,
                        new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param)
                                    throws Throwable {
                                int keyCode = (int) param.args[0];
                                android.view.KeyEvent event = (android.view.KeyEvent) param.args[1];
                                if (keyCode == android.view.KeyEvent.KEYCODE_BACK
                                        && event.getAction() == android.view.KeyEvent.ACTION_DOWN
                                        && !event.isLongPress()) {
                                    Activity activity = (Activity) param.thisObject;
                                    handler.handleBefore(activity, param);
                                }
                            }

                            @Override
                            protected void afterHookedMethod(MethodHookParam param)
                                    throws Throwable {
                                int keyCode = (int) param.args[0];
                                android.view.KeyEvent event = (android.view.KeyEvent) param.args[1];
                                if (keyCode == android.view.KeyEvent.KEYCODE_BACK
                                        && event.getAction() == android.view.KeyEvent.ACTION_DOWN) {
                                    Activity activity = (Activity) param.thisObject;
                                    handler.handleAfter(activity);
                                }
                            }
                        }
                );
            } catch (Throwable ignored) {
            }

            try {
                XposedHelpers.findAndHookMethod(
                        "android.app.Activity",
                        classLoader,
                        "onKeyUp",
                        int.class,
                        android.view.KeyEvent.class,
                        new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param)
                                    throws Throwable {
                                int keyCode = (int) param.args[0];
                                android.view.KeyEvent event = (android.view.KeyEvent) param.args[1];
                                if (keyCode == android.view.KeyEvent.KEYCODE_BACK
                                        && event.getAction() == android.view.KeyEvent.ACTION_UP
                                        && !event.isLongPress()) {
                                    Activity activity = (Activity) param.thisObject;
                                    handler.handleBefore(activity, param);
                                }
                            }

                            @Override
                            protected void afterHookedMethod(MethodHookParam param)
                                    throws Throwable {
                                int keyCode = (int) param.args[0];
                                android.view.KeyEvent event = (android.view.KeyEvent) param.args[1];
                                if (keyCode == android.view.KeyEvent.KEYCODE_BACK
                                        && event.getAction() == android.view.KeyEvent.ACTION_UP) {
                                    Activity activity = (Activity) param.thisObject;
                                    handler.handleAfter(activity);
                                }
                            }
                        }
                );
            } catch (Throwable ignored) {
            }

            ReaLog.log("misc", "返回键控制已安装");
        } catch (Throwable t) {
            log("❌ Hook返回键失败: " + t.getMessage());
            ReaLog.log("misc", "返回键控制异常: " + t.getMessage());
        }
    }

    private void hookWindowFlags(ClassLoader classLoader) {
        try {
            Class<?> window = XposedHelpers.findClass("android.view.Window", classLoader);

            XposedHelpers.findAndHookMethod(window, "setFlags", int.class, int.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            try {
                                int flags = (int) param.args[1];
                                if ((flags & WindowManager.LayoutParams.FLAG_SECURE) != 0) {
                                    flags &= ~WindowManager.LayoutParams.FLAG_SECURE;
                                    param.args[1] = flags;
                                    ReaLog.log("Window", "屏幕限制解除(setFlags)");
                                }
                            } catch (Throwable t) {
                            }
                        }
                    });

            XposedHelpers.findAndHookMethod(window, "addFlags", int.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            try {
                                int flags = (int) param.args[0];
                                if ((flags & WindowManager.LayoutParams.FLAG_SECURE) != 0) {
                                    flags &= ~WindowManager.LayoutParams.FLAG_SECURE;
                                    param.args[0] = flags;
                                    ReaLog.log("Window", "屏幕限制解除(addFlags)");
                                }
                            } catch (Throwable t) {
                            }
                        }
                    });
        } catch (Throwable e) {
        }
    }

    private void hookDialogCancelableMethods(ClassLoader classLoader) {
        try {
            XposedHelpers.findAndHookMethod("android.app.Dialog", classLoader, "setCancelable", boolean.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            param.args[0] = true;
                        }
                    });

            XposedHelpers.findAndHookMethod("android.app.Dialog", classLoader, "setCanceledOnTouchOutside", boolean.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            param.args[0] = true;
                        }
                    });

            XposedHelpers.findAndHookMethod("android.app.Dialog", classLoader, "show", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    try {
                        Object dialog = param.thisObject;
                        XposedHelpers.callMethod(dialog, "setCancelable", true);
                        XposedHelpers.callMethod(dialog, "setCanceledOnTouchOutside", true);
                    } catch (Throwable t) {
                        ReaLog.log("dialog", "强制对话框设置异常: " + t.getMessage());
                    }
                }
            });

            XposedHelpers.findAndHookMethod("android.app.AlertDialog$Builder", classLoader, "create", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    try {
                        Object dialog = param.getResult();
                        if (dialog != null) {
                            XposedHelpers.callMethod(dialog, "setCancelable", true);
                            XposedHelpers.callMethod(dialog, "setCanceledOnTouchOutside", true);
                        }
                    } catch (Throwable t) {
                        ReaLog.log("dialog", "AlertDialog.create异常: " + t.getMessage());
                    }
                }
            });

            try {
                XposedHelpers.findAndHookMethod("android.app.ProgressDialog", classLoader, "setCancelable", boolean.class,
                        new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param)
                                    throws Throwable {
                                param.args[0] = true;
                            }
                        });
            } catch (Throwable ignored) {
            }

            hookDialogFragmentMethods(classLoader);
            hookAndroidXDialogFragmentMethods(classLoader);
            ReaLog.log("dialog", "Hook对话框完成强制可取消");

        } catch (Throwable t) {
            ReaLog.log("system", "hook对话框强制取消异常: " + t.getMessage());
        }
    }

    private void hookDialogFragmentMethods(ClassLoader classLoader) {
        try {
            Class<?> dialogFragmentClass = Class.forName("android.app.DialogFragment", false, classLoader);
            XposedHelpers.findAndHookMethod(dialogFragmentClass, "onCreateDialog", Bundle.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            try {
                                Object dialog = param.getResult();
                                if (dialog != null) {
                                    XposedHelpers.callMethod(dialog, "setCancelable", true);
                                    XposedHelpers.callMethod(dialog, "setCanceledOnTouchOutside", true);
                                    Object dialogFragment = param.thisObject;
                                    XposedHelpers.callMethod(dialogFragment, "setCancelable", true);
                                }
                            } catch (Throwable t) {
                                ReaLog.log("dialog", "Dialog异常: " + t.getMessage());
                            }
                        }
                    });
            XposedHelpers.findAndHookMethod(dialogFragmentClass, "setCancelable", boolean.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            param.args[0] = true;
                        }
                    });
            ReaLog.log("dialog", "原生Dialog完成");
        } catch (ClassNotFoundException e) {
            ReaLog.log("dialog", "原生Dialog类不存在，跳过");
        } catch (Throwable t) {
            ReaLog.log("dialog", "原生Dialog异常: " + t.getMessage());
        }
    }

    private void hookAndroidXDialogFragmentMethods(ClassLoader classLoader) {
        try {
            Class<?> dialogFragmentClass = Class.forName(
                    "androidx.fragment.app.DialogFragment",
                    false,
                    classLoader
            );
            XposedHelpers.findAndHookMethod(
                    dialogFragmentClass,
                    "onCreateDialog",
                    Bundle.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            try {
                                Object dialog = param.getResult();
                                if (dialog != null) {
                                    XposedHelpers.callMethod(dialog, "setCancelable", true);
                                    XposedHelpers.callMethod(dialog, "setCanceledOnTouchOutside", true);
                                    Object dialogFragment = param.thisObject;
                                    XposedHelpers.callMethod(dialogFragment, "setCancelable", true);
                                }
                            } catch (Throwable t) {
                                ReaLog.log("dialog", "AndroidX Dialog异常: " + t.getMessage());
                            }
                        }
                    }
            );

            XposedHelpers.findAndHookMethod(
                    dialogFragmentClass,
                    "setCancelable",
                    boolean.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            param.args[0] = true;
                        }
                    }
            );
        } catch (ClassNotFoundException e) {
        } catch (Throwable t) {
            ReaLog.log("dialog", "AndroidXDialogFrag异常: " + t.getMessage());
        }
    }

    private void hookQueryIntentServices(ClassLoader classLoader) {
        try {
            XposedHelpers.findAndHookMethod(
                    "android.app.ApplicationPackageManager",
                    classLoader,
                    "queryIntentServices",
                    Intent.class,
                    int.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            try {
                                if (param.thisObject == null) return;
                                String pkg = null;
                                try {
                                    pkg = (String) XposedHelpers.callMethod(param.thisObject, "getPackageName");
                                } catch (Throwable ignored) {
                                }
                                if (MODULE_PACKAGE.equals(pkg)) {
                                    return;
                                }
                                Intent intent = (Intent) param.args[0];
                                if (intent == null) return;
                                String targetPackage = extractPackageFromIntent(intent);
                                if (targetPackage != null) {
                                    if (targetPackage.equals(currentTargetApp)) {
                                        return;
                                    }
                                    handleIntentQueryForIntentHook(param, targetPackage, "queryIntentServices");
                                }
                            } catch (Throwable t) {
                            }
                        }
                    }
            );

            try {
                XposedHelpers.findAndHookMethod(
                        "android.app.ApplicationPackageManager",
                        classLoader,
                        "queryIntentServicesAsUser",
                        Intent.class, int.class, int.class,
                        new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) {
                                Intent intent = (Intent) param.args[0];
                                if (intent != null) {
                                    String pkg = extractPackageFromIntent(intent);
                                    if (pkg != null) {
                                        if (mPkgMgr != null) {
                                            mPkgMgr.captureValidPackage(pkg);
                                        }
                                    }
                                }
                            }
                        }
                );
            } catch (Throwable ignored) {
            }

            ReaLog.log("intent_query", "查询意图服务 Hook成功");
        } catch (Throwable t) {
            ReaLog.log("intent_query", "查询意图服务 Hook失败: " + t.getMessage());
        }
    }

    private void handleIntentQueryForIntentHook(MethodHookParam param, String targetPackage, String methodName) {
        try {
            if (!installStatusMap.getOrDefault(currentTargetApp, true)) {
                param.setResult(new ArrayList<Object>());
                ReaLog.log("package_query", methodName + "查询拦截: 未安装，返回空列表");
                return;
            }

            if (isExcludedPackage(targetPackage) || isSystemCorePackage(targetPackage)) {
                ReaLog.log("intent_query", methodName + ": " + targetPackage + " 排除包或系统包，放行");
                return;
            }
            if (!shouldReturnInstalledForPackage(targetPackage)) {
                param.setResult(new ArrayList<Object>());
                ReaLog.log("intent_query", methodName + ": " + targetPackage + " 设置未安装，返回空列表");
                return;
            }
            List<Object> fakeList = new ArrayList<Object>();
            for (String fakePkg : globalCapturedPackages) {
                if (shouldReturnInstalledForPackage(fakePkg)) {
                    Object fakeResolveInfo = createFakeResolveInfo(fakePkg);
                    if (fakeResolveInfo != null) {
                        fakeList.add(fakeResolveInfo);
                    }
                }
            }
            param.setResult(fakeList);
            ReaLog.log("intent_query", methodName + ": " + targetPackage + " 返回 " + fakeList.size() + " 条虚假数据");
        } catch (Throwable t) {
            param.setResult(new ArrayList<Object>());
            ReaLog.log("intent_query", "处理 Intent 查询异常: " + t.getMessage());
        }
    }

    public ResolveInfo createFakeResolveInfo(String packageName) {
        if (mPkgMgr != null) {
            return (ResolveInfo) mPkgMgr.createFakeResolveInfo(packageName);
        }
        return null;
    }

    private Object createEmptyResolveInfo() {
        if (mPkgMgr != null) {
            return mPkgMgr.createEmptyResolveInfo();
        }
        return null;
    }

    private void hookResolveActivity(ClassLoader classLoader) {
        try {
            XposedHelpers.findAndHookMethod(
                    "android.app.ApplicationPackageManager",
                    classLoader,
                    "resolveActivity",
                    Intent.class,
                    int.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            try {
                                if (param.thisObject == null) return;
                                String pkg = null;
                                try {
                                    pkg = (String) XposedHelpers.callMethod(param.thisObject, "getPackageName");
                                } catch (Throwable ignored) {
                                }
                                if (MODULE_PACKAGE.equals(pkg)) {
                                    return;
                                }
                                Intent intent = (Intent) param.args[0];
                                if (intent == null) return;
                                String targetPackage = extractPackageFromIntent(intent);
                                if (targetPackage == null) {
                                    return;
                                }

                                if (targetPackage.equals(currentTargetApp)) {
                                    return;
                                }

                                if (isExcludedPackage(targetPackage) || isSystemCorePackage(targetPackage)) {
                                    ReaLog.log("intent_query", "解析活动: 排除包或系统包 " + targetPackage + "，放行");
                                    return;
                                }
                                if (!shouldReturnInstalledForPackage(targetPackage)) {
                                    param.setResult(createEmptyResolveInfo());
                                    ReaLog.log("intent_query", "解析活动: " + targetPackage + " 设置未安装");
                                    return;
                                }
                                ReaLog.log("intent_query", "解析活动: " + targetPackage + " 放行");
                            } catch (Throwable t) {
                            }
                        }
                    }
            );
            ReaLog.log("intent_query", "解析活动before Hook成功");
        } catch (Throwable t) {
            ReaLog.log("intent_query", "解析活动before Hook失败: " + t.getMessage());
        }
    }

    private void hookQueryBroadcastReceivers(ClassLoader classLoader) {
        try {
            XposedHelpers.findAndHookMethod(
                    "android.app.ApplicationPackageManager",
                    classLoader,
                    "queryBroadcastReceivers",
                    Intent.class,
                    int.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            Intent intent = (Intent) param.args[0];
                            if (intent == null) {
                                return;
                            }
                            String pkg = extractPackageFromIntent(intent);
                            if (pkg == null || pkg.isEmpty()) {
                                return;
                            }

                            if (pkg.equals(currentTargetApp)) {
                                return;
                            }

                            if (pkg.equals(currentTargetApp) || isSystemCorePackage(pkg)) {
                                ReaLog.log("intent_query", "查询广播: " + pkg + " 自身或系统包，放行");
                                return;
                            }

                            if (!shouldReturnInstalledForPackage(pkg)) {
                                param.setResult(new ArrayList<ResolveInfo>());
                                ReaLog.log("intent_query", "查询广播: " + pkg + " 设置未安装，返回空列表");
                                return;
                            }
                            ReaLog.log("intent_query", "查询广播: " + pkg + " 放行");
                        }
                    }
            );
        } catch (Throwable t) {
            log("❌ Hook 查询广播接收 失败: " + t.getMessage());
            ReaLog.log("intent_query", "Hook 查询广播接收异常: " + t.getMessage());
        }
    }

    private void hookStartActivity(ClassLoader classLoader) {
        try {
            Class<?> instrumentationClass = XposedHelpers.findClass("android.app.Instrumentation", classLoader);
            XposedBridge.hookAllMethods(instrumentationClass, "execStartActivity", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    ReaLog.log("launch_intercept", "启动拦截：执行启动活动 拦截入口");
                    handleExecStartActivity(param);
                }
            });
        } catch (Throwable t) {
            ReaLog.log("launch_intercept", "执行启动活动异常: " + t.getMessage());
        }
    }

    private void handleExecStartActivity(XC_MethodHook.MethodHookParam param) {
        try {
            boolean isLaunchIntercept = launchInterceptMap.getOrDefault(currentTargetApp, true);
            if (!isLaunchIntercept) {
                ReaLog.log("launch_intercept", "启动拦截已关闭");
                return;
            }

            Intent intent = null;
            Activity activity = null;
            int requestCode = -1;
            for (Object arg : param.args) {
                if (arg instanceof Intent) intent = (Intent) arg;
                else if (arg instanceof Activity) activity = (Activity) arg;
                else if (arg instanceof Integer && requestCode == -1) requestCode = (int) arg;
            }
            if (intent == null) {
                ReaLog.log("launch_intercept", "启动拦截：Intent为空，无法拦截");
                return;
            }
            if (intent.hasExtra("__hook_skip_flag")) {
                ReaLog.log("launch_intercept", "启动拦截：内部启动，跳过拦截");
                return;
            }

            String currentPkg = currentTargetApp;
            String targetPkg = extractPackageFromIntent(intent);
            if (targetPkg == null && intent.getData() != null) {
                Uri data = intent.getData();
                String scheme = data.getScheme();
                if ("market".equals(scheme) || "appmarket".equals(scheme)) {
                    targetPkg = data.getQueryParameter("id");
                    ReaLog.log("launch_intercept", "从market链接提取包名: " + targetPkg);
                }
            }

            String[] systemPermissionPackages = {
                    "com.android.permissioncontroller",
                    "com.android.packageinstaller",
                    "com.google.android.packageinstaller",
                    "com.android.settings",
                    "com.google.android.permissioncontroller"
            };
            if (targetPkg != null) {
                for (String sysPkg : systemPermissionPackages) {
                    if (sysPkg.equals(targetPkg)) {
                        ReaLog.log("launch_intercept", "系统权限包，放行: " + targetPkg);
                        return;
                    }
                }
            }
            String action = intent.getAction();
            if (action != null && (action.startsWith("android.settings.") ||
                    "android.intent.action.REQUEST_PERMISSIONS".equals(action))) {
                ReaLog.log("launch_intercept", "系统设置/权限请求，放行: " + action);
                return;
            }

            if (targetPkg != null && (targetPkg.equals(currentPkg) || isExcludedPackage(targetPkg))) {
                ReaLog.log("launch_intercept", "自身包或排除包，放行: " + targetPkg);
                return;
            }
            boolean isImplicit = (targetPkg == null && intent.getComponent() == null && intent.getPackage() == null);
            if (!isImplicit && targetPkg == null) {
                ReaLog.log("launch_intercept", "无法确定目标包，放行");
                return;
            }

            final String finalTargetPkg = (targetPkg != null) ? targetPkg : "";
            param.setResult(null);

            final Activity finalActivity = (activity != null) ? activity : getCurrentActivity();
            if (finalActivity == null || finalActivity.isFinishing()) {
                ReaLog.log("launch_intercept", "启动拦截：当前Activity无效，无法显示弹窗");
                return;
            }
            final int finalRequestCode = requestCode;
            final Intent originalIntent = new Intent(intent);
            final boolean reallyInstalled = !TextUtils.isEmpty(targetPkg) && isPackageReallyInstalled(targetPkg);

            ReaLog.log("launch_intercept", "准备显示启动确认弹窗，目标: " + (TextUtils.isEmpty(targetPkg) ? "隐式Intent" : targetPkg));
            debounceAndShowDialog(finalActivity, originalIntent, finalTargetPkg, finalRequestCode, reallyInstalled);
        } catch (Throwable t) {
            log("handleExecStartActivity 异常: " + t.getMessage());
            ReaLog.log("launch_intercept", "处理执行启动活动异常: " + t.getMessage());
        }
    }

    private void showLaunchConfirmDialog(final Activity activity, final Intent originalIntent,
            final String targetPkg, final int requestCode,
            final boolean reallyInstalled) {
        try {
            if (activity.isFinishing() || activity.isDestroyed()) {
                ReaLog.log("launch_intercept", "启动拦截：Activity已销毁，无法显示启动确认弹窗");
                return;
            }

            Uri uri = originalIntent.getData();
            final String finalIdentifier;
            final String finalDisplayName;
            if (!TextUtils.isEmpty(targetPkg)) {
                finalIdentifier = targetPkg;
                finalDisplayName = targetPkg;
            } else if (uri != null) {
                finalIdentifier = uri.toString();
                String uriStr = uri.toString();
                finalDisplayName = uriStr.length() > 120 ? uriStr.substring(0, 117) + "..." : uriStr;
            } else {
                String intentUri = originalIntent.toUri(0);
                if (TextUtils.isEmpty(intentUri)) {
                    String action = originalIntent.getAction();
                    intentUri = (action != null) ? action : "unknown_intent";
                }
                finalIdentifier = intentUri;
                String shortDesc;
                if (originalIntent.getAction() != null) {
                    shortDesc = originalIntent.getAction();
                } else if (originalIntent.getCategories() != null && !originalIntent.getCategories().isEmpty()) {
                    shortDesc = "category:" + originalIntent.getCategories().iterator().next();
                } else {
                    shortDesc = intentUri;
                }
                if (shortDesc.length() > 120) {
                    shortDesc = shortDesc.substring(0, 117) + "...";
                }
                finalDisplayName = "外部链接: " + shortDesc;
            }
            ReaLog.log("launch_intercept", "启动确认弹窗，标识: " + finalIdentifier);
           // 在调用 getAutoAction 和 checkAutoChoice 之前增加
if (finalIdentifier != null) {
    // 自动处理（黑/白名单）
    String action = getAutoAction(currentTargetApp, finalIdentifier);
    if (action != null) {
        if ("black".equals(action)) {
            if (requestCode > 0) {
                callOnActivityResult(activity, requestCode, Activity.RESULT_CANCELED, originalIntent);
            }
            ToastUtil.showUnique(activity, "⛔ 黑名单已自动拦截\n" + finalIdentifier);
            ReaLog.log("auto", "⛔ 黑名单自动拦截: " + finalIdentifier);
            return;
        } else if ("white".equals(action)) {
            performRealLaunch(activity, originalIntent, targetPkg, finalDisplayName, requestCode);
            ReaLog.log("auto", "💚 白名单自动放行: " + finalIdentifier);
            return;
        }
    }
    // 智能判断
    String autoChoice = checkAutoChoice(currentTargetApp, finalIdentifier);
    if (autoChoice != null) {
        if ("real".equals(autoChoice)) {
            performRealLaunch(activity, originalIntent, targetPkg, finalDisplayName, requestCode);
            ReaLog.log("auto", "智能判断-真实启动: " + finalIdentifier);
        } else if ("fake".equals(autoChoice)) {
            performFakeLaunch(activity, finalDisplayName, requestCode, originalIntent);
            ReaLog.log("auto", "智能判断-虚假启动: " + finalIdentifier);
        } else if ("cancel".equals(autoChoice)) {
            if (requestCode > 0) {
                callOnActivityResult(activity, requestCode, Activity.RESULT_CANCELED, originalIntent);
            }
            ToastUtil.showUnique(activity, "🕸️ 已自动判断取消\n" + finalIdentifier);
            ReaLog.log("auto", "智能判断-取消启动: " + finalIdentifier);
        }
        return;
    }
}

            String pkgTip = reallyInstalled ? finalDisplayName : finalDisplayName + (TextUtils.isEmpty(targetPkg) ? "" : "（未安装）");
            boolean isBrowser = isBrowserFromIntent(originalIntent, activity.getPackageManager());
            String browserTip = isBrowser ? "<br><b>(<font color='#FF5722'>检测到跳转浏览器</font>，<font color='#1E90FF'>可选择虚假启动屏蔽</font>)</b>" : "";

            String msg = "当前应用想要打开<br><font color='#FF5722'><b>" + pkgTip + "</b></font>" + browserTip + "<br><br>" +
                    "—————————————————<br>" +
                    "【虚假启动】不需要启动跳转到应用，返回假装已启动<br>" +
                    "【真实启动】真正的打开想要的应用，按宿主要求执行<br>" +
                    "【取消启动】阻止跳转到第三方应用，可能会重复提醒<br>" +
                    "—————————————————<br>";
            if (originalIntent.getData() != null || (originalIntent.getAction() != null &&
                    (originalIntent.getAction().equals(Intent.ACTION_OPEN_DOCUMENT) ||
                            originalIntent.getAction().equals(Intent.ACTION_SEND)))) {
                msg +=
                        "<font color='#1E90FF'>检测到启动的应用中包含参数跳转请求</font><br>" +
                                "<font color='#FF5722'>虚假:</font><font color='#1E90FF'>假装参数返回</font>/<font color='#FF5722'>真实:</font><font color='#1E90FF'>附带参数跳转</font><br>" +
                                "<font color='#D3D3D3'><b>部分应用虚假启动需切换至后台再回来生效</b></font><br>";
            }
            boolean canResolve = (originalIntent.getData() != null) || (originalIntent.getAction() != null);

            LinearLayout contentLayout = new LinearLayout(activity);
            contentLayout.setOrientation(LinearLayout.VERTICAL);
            contentLayout.setPadding(40, 30, 40, 30);
            TextView msgView = new com.install.appinstall.xl.ru.RuTextView(activity);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                msgView.setText(com.install.appinstall.xl.ru.RuStrings.fromHtml(msg, Html.FROM_HTML_MODE_LEGACY));
            } else {
                msgView.setText(com.install.appinstall.xl.ru.RuStrings.fromHtml(msg));
            }
            msgView.setTextIsSelectable(true);
            msgView.setTextSize(14);
            msgView.setTextColor(0xFF333333);
            contentLayout.addView(msgView);

            if (finalIdentifier != null) {
                View divider = new View(activity);
                divider.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
                divider.setBackgroundColor(0xFFE0E0E0);
                contentLayout.addView(divider);
                Button autoBtn = new com.install.appinstall.xl.ru.RuButton(activity);
                autoBtn.setText("⚙ 设置自动处理");
                autoBtn.setTextSize(14);
                autoBtn.setPadding(40, 15, 40, 15);
                autoBtn.setBackgroundColor(0xAAFF6347);
                autoBtn.setTextColor(0xFFFFFFFF);
                autoBtn.setGravity(Gravity.CENTER);
                autoBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ReaLog.log("system", "打开自动处理设置: " + finalIdentifier);
                        instar.showAutoSettingDialog(activity, finalIdentifier, finalDisplayName, null, HookInit.this, currentTargetApp);
                    }
                });
                contentLayout.addView(autoBtn);
            }

            if (canResolve) {
                AlertDialog dialog = createBoundedDialog(activity, "启动确认", null,
                        new String[]{"真实启动", "虚假启动", "取消启动"},
                        new DialogInterface.OnClickListener[]{
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface d, int which) {
                                        ReaLog.log("launch_intercept", "用户选择真实启动: " + finalDisplayName);
                                        performRealLaunch(activity, originalIntent, targetPkg, finalDisplayName, requestCode);
                                        if (finalIdentifier != null) {
                                            addAutoRecord(currentTargetApp, finalIdentifier, "real");
                                            HookInit.this.saveConfigToFile();
                                        }
                                        d.dismiss();
                                    }
                                },
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface d, int which) {
                                        ReaLog.log("launch_intercept", "用户选择虚假启动: " + finalDisplayName);
                                        performFakeLaunch(activity, finalDisplayName, requestCode, originalIntent);
                                        if (finalIdentifier != null) {
                                            addAutoRecord(currentTargetApp, finalIdentifier, "fake");
                                            HookInit.this.saveConfigToFile();
                                        }
                                        d.dismiss();
                                    }
                                },
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface d, int which) {
                                        if (requestCode > 0) {
                                            callOnActivityResult(activity, requestCode, Activity.RESULT_CANCELED, originalIntent);
                                        }
                                        ToastUtil.showUnique(activity, "已取消启动" + finalDisplayName);
                                        ReaLog.log("launch_intercept", "用户选择取消启动: " + finalDisplayName);
                                        if (finalIdentifier != null) {
                                            addAutoRecord(currentTargetApp, finalIdentifier, "cancel");
                                            HookInit.this.saveConfigToFile();
                                        }
                                        d.dismiss();
                                    }
                                }
                        },
                        contentLayout
                );
                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface d) {
                        if (requestCode > 0) {
                            callOnActivityResult(activity, requestCode, Activity.RESULT_CANCELED, originalIntent);
                        }
                        ReaLog.log("system", "启动确认弹窗取消");
                    }
                });
                dialog.show();
            } else {
                AlertDialog dialog = createBoundedDialog(activity, "启动确认", null,
                        new String[]{"尝试强启", "虚假启动", "取消启动"},
                        new DialogInterface.OnClickListener[]{
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface d, int which) {
                                        ReaLog.log("launch_intercept", "用户选择尝试启动2: " + finalDisplayName);
                                        performRealLaunch(activity, originalIntent, targetPkg, finalDisplayName, requestCode);
                                        if (finalIdentifier != null) {
                                            addAutoRecord(currentTargetApp, finalIdentifier, "real");
                                            saveConfigToFile();
                                        }
                                        d.dismiss();
                                    }
                                },
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface d, int which) {
                                        ReaLog.log("launch_intercept", "用户选择虚假启动2: " + finalDisplayName);
                                        performFakeLaunch(activity, finalDisplayName, requestCode, originalIntent);
                                        if (finalIdentifier != null) {
                                            addAutoRecord(currentTargetApp, finalIdentifier, "fake");
                                            saveConfigToFile();
                                        }
                                        d.dismiss();
                                    }
                                },
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface d, int which) {
                                        if (requestCode > 0) {
                                            callOnActivityResult(activity, requestCode, Activity.RESULT_CANCELED, originalIntent);
                                        }
                                        ToastUtil.showUnique(activity, "已取消启动" + finalDisplayName);
                                        ReaLog.log("launch_intercept", "用户选择取消启动2: " + finalDisplayName);
                                        if (finalIdentifier != null) {
                                            addAutoRecord(currentTargetApp, finalIdentifier, "cancel");
                                            saveConfigToFile();
                                        }
                                        d.dismiss();
                                    }
                                }
                        },
                        contentLayout
                );
                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface d) {
                        if (requestCode > 0) {
                            callOnActivityResult(activity, requestCode, Activity.RESULT_CANCELED, originalIntent);
                        }
                        ReaLog.log("launch_intercept", "启动确认弹窗取消2");
                    }
                });
                dialog.show();
            }
        } catch (Throwable t) {
            log("显示启动确认异常: " + t.getMessage());
            ReaLog.log("system", "显示启动确认对话框异常: " + t.getMessage());
        }
    }

    private void performRealLaunch(Activity activity, Intent originalIntent, String targetPkg, String finalDisplayName, int requestCode) {
        try {
            Intent launchIntent;
            if (TextUtils.isEmpty(targetPkg)) {
                launchIntent = new Intent(originalIntent);
                launchIntent.putExtra("__hook_skip_flag", true);
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (originalIntent.getData() != null) {
                    launchIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
                ReaLog.log("launch_intercept", "真实启动: " + finalDisplayName);
            } else {
                launchIntent = buildLaunchIntent(activity, originalIntent, targetPkg, true);
                ReaLog.log("launch_intercept", "真实启动: " + targetPkg);
            }
            activity.startActivity(launchIntent);
            ToastUtil.showUnique(activity, "✅ 已启动：" + finalDisplayName);
            ReaLog.log("launch_intercept", "启动成功: " + finalDisplayName);
            refreshAppToForeground(activity);
        } catch (ActivityNotFoundException e) {
            if (!TextUtils.isEmpty(targetPkg)) {
                try {
                    Intent launchIntent = buildLaunchIntent(activity, originalIntent, targetPkg, false);
                    List<ResolveInfo> resolveList = activity.getPackageManager().queryIntentActivities(launchIntent, 0);
                    if (resolveList != null && !resolveList.isEmpty()) {
                        activity.startActivity(launchIntent);
                        ToastUtil.showUnique(activity, "✅ 启动主页：" + targetPkg);
                        ReaLog.log("launch_intercept", "启动主页: " + targetPkg);
                        refreshAppToForeground(activity);
                        return;
                    }
                } catch (Exception fallbackE) {
                    ReaLog.log("launch_intercept", "启动失败2: " + fallbackE.getMessage());
                }
            }
            ToastUtil.showUnique(activity, "启动失败：无法处理该跳转\n" + finalDisplayName);
            ReaLog.log("launch_intercept", "启动失败: 无法处理该跳转 - " + finalDisplayName);
        } catch (Exception e) {
            ToastUtil.showUnique(activity, "启动异常：" + e.getMessage());
            ReaLog.log("launch_intercept", "启动异常: " + e.getMessage());
        }
    }

    private boolean isWeChatShareIntent(Intent intent) {
        if (intent == null) {
            ReaLog.log("share_detect", "微信分享: Intent为空");
            return false;
        }
        String pkg = intent.getPackage();
        String action = intent.getAction();
        boolean result = "com.tencent.mm".equals(pkg) &&
                (Intent.ACTION_SEND.equals(action) || Intent.ACTION_SEND_MULTIPLE.equals(action));
        if (result) {
            ReaLog.log("share_detect", "检测到微信分享Intent");
        }
        return result;
    }

    private boolean isQQShareIntent(Intent intent) {
        if (intent == null) {
            ReaLog.log("share_detect", "QQ分享Intent为空");
            return false;
        }
        String pkg = intent.getPackage();
        if ("com.tencent.mobileqq".equals(pkg)) {
            String action = intent.getAction();
            if (Intent.ACTION_SEND.equals(action) || Intent.ACTION_SEND_MULTIPLE.equals(action)) {
                ReaLog.log("share_detect", "检测到QQ分享Intent (标准方式)");
                return true;
            }
        }
        Uri data = intent.getData();
        if (data != null) {
            String scheme = data.getScheme();
            if ("mqqapi".equals(scheme)) {
                String host = data.getHost();
                String path = data.getPath();
                if (host != null && host.contains("share")) {
                    if (path != null && (path.contains("to_fri") || path.contains("to_qzone"))) {
                        ReaLog.log("share_detect", "检测到QQ分享Intent (自定义)");
                        return true;
                    }
                }
            }
        }
        ReaLog.log("share_detect", "非QQ分享Intent");
        return false;
    }

    private boolean isDingTalkShareIntent(Intent intent) {
        if (intent == null) {
            ReaLog.log("share_detect", "钉钉分享Intent为空");
            return false;
        }
        String pkg = intent.getPackage();
        if ("com.alibaba.android.rimet".equals(pkg)) {
            String action = intent.getAction();
            if (Intent.ACTION_SEND.equals(action) || Intent.ACTION_SEND_MULTIPLE.equals(action)) {
                ReaLog.log("share_detect", "检测到钉钉分享Intent (标准方式)");
                return true;
            }
        }
        Uri data = intent.getData();
        if (data != null) {
            String scheme = data.getScheme();
            if ("dingtalk".equals(scheme) || "ddshare".equals(scheme)) {
                ReaLog.log("share_detect", "检测到钉钉分享Intent (自定义)");
                return true;
            }
        }
        ReaLog.log("share_detect", "非钉钉分享Intent");
        return false;
    }

    private boolean isWeiboShareIntent(Intent intent) {
        if (intent == null) {
            ReaLog.log("share_detect", "微博Intent为空");
            return false;
        }
        String pkg = intent.getPackage();
        if ("com.sina.weibo".equals(pkg)) {
            String action = intent.getAction();
            if (Intent.ACTION_SEND.equals(action) || Intent.ACTION_SEND_MULTIPLE.equals(action)) {
                ReaLog.log("share_detect", "检测到微博分享Intent (标准方式)");
                return true;
            }
        }
        Uri data = intent.getData();
        if (data != null && "sinaweibo".equals(data.getScheme())) {
            ReaLog.log("share_detect", "检测到微博分享Intent (自定义)");
            return true;
        }
        ReaLog.log("share_detect", "非微博分享Intent");
        return false;
    }
/*原
    private void performFakeLaunch(Activity activity, String finalDisplayName, int requestCode, Intent originalIntent) {
        try {
            Map<String, Boolean> shareDetail = shareFakeDetailMap.getOrDefault(currentTargetApp, getDefaultShareDetailMap());
            boolean wechatFake = shareDetail.getOrDefault("wechat", false);
            boolean qqFake = shareDetail.getOrDefault("qq", false);
            boolean dingtalkFake = shareDetail.getOrDefault("dingtalk", false);
            boolean weiboFake = shareDetail.getOrDefault("weibo", false);

            if (originalIntent != null && (wechatFake || qqFake || dingtalkFake || weiboFake)) {
                if (wechatFake && isWeChatShareIntent(originalIntent)) {
                    ShareHook.fakeWeChatShare(activity, activity.getClassLoader(), currentTargetApp);
                    ReaLog.log("share_fake", "微信分享假装成功(插入虚假启动中)");
                } else if (qqFake && isQQShareIntent(originalIntent)) {
                    ShareHook.fakeQQShare(activity, activity.getClassLoader());
                    ReaLog.log("share_fake", "QQ分享假装成功(插入虚假启动中)");
                } else if (dingtalkFake && isDingTalkShareIntent(originalIntent)) {
                    ShareHook.fakeDingTalkShare(activity, activity.getClassLoader());
                    ReaLog.log("share_fake", "钉钉分享假装成功(插入虚假启动中)");
                } else if (weiboFake && isWeiboShareIntent(originalIntent)) {
                    ShareHook.fakeWeiboShare(activity, activity.getClassLoader());
                    ReaLog.log("share_fake", "微博分享假装成功(插入虚假启动中)");
                }
            }

            long minDuration = 3 * 60 * 1000;
            long maxDuration = 6 * 60 * 1000;
            long fakeDuration = minDuration + (long) (Math.random() * (maxDuration - minDuration));
            sFakeTimeOffset = fakeDuration;
            ReaLog.log("time_fake", "设置虚假启动偏移: " + fakeDuration + "ms (" + (fakeDuration / 1000) + "秒)");
            new Handler(Looper.getMainLooper()).removeCallbacks(mClearFakeTimeRunnable);

            if (requestCode > 0) {
                Intent resultIntent = new Intent();
                if (originalIntent != null) {
                    Uri data = originalIntent.getData();
                    if (data != null) resultIntent.setData(data);
                    resultIntent.setAction(originalIntent.getAction());
                    Set<String> categories = originalIntent.getCategories();
                    if (categories != null) {
                        for (String cat : categories) resultIntent.addCategory(cat);
                    }
                    Bundle extras = originalIntent.getExtras();
                    if (extras != null) resultIntent.putExtras(extras);
                    resultIntent.setFlags(originalIntent.getFlags());
                }
                callOnActivityResult(activity, requestCode, Activity.RESULT_OK, resultIntent);
                ReaLog.log("launch_intercept", "虚假启动成功(有回调): " + finalDisplayName);
            }

            ToastUtil.showUnique(activity, "✅ 已虚假启动" + finalDisplayName);
            ReaLog.log("launch_intercept", "虚假启动成功: " + finalDisplayName);
            refreshAppToForeground(activity);
            new Handler(Looper.getMainLooper()).postDelayed(mClearFakeTimeRunnable, 5000);
        } catch (Throwable e) {
            ToastUtil.showUnique(activity, "虚假启动失败");
            ReaLog.log("launch_intercept", "虚假启动异常: " + e.getMessage());
            sFakeTimeOffset = 0;
            new Handler(Looper.getMainLooper()).removeCallbacks(mClearFakeTimeRunnable);
        }
    }
    */
private void performFakeLaunch(Activity activity, String finalDisplayName, int requestCode, Intent originalIntent) {
    // ===== 文件选择器/分享 CHOOSER 过滤 =====
    if (originalIntent != null) {
        String action = originalIntent.getAction();
        if (isFilePickerAction(action) || isChooserWithFilePicker(originalIntent) || isChooserWithShareIntent(originalIntent)) {
            
            // 文件选择器 → 返回虚假 Uri
            if (isFilePickerAction(action) || isChooserWithFilePicker(originalIntent)) {
                Intent resultIntent = new Intent();
                Uri fakeUri = Uri.parse("content://com.xiaolin.fake.yhzl/fake_file_" + System.currentTimeMillis());
                resultIntent.setData(fakeUri);
                if (requestCode > 0) {
                    callOnActivityResult(activity, requestCode, Activity.RESULT_OK, resultIntent);
                }
                ReaLog.log("launch_intercept", "【虚假启动】文件选择器返回虚假 Uri: " + fakeUri);
                ToastUtil.showUnique(activity, "✅ 已返回虚假 Uri: \n" + fakeUri);
                return;
            }
            
            // 分享 CHOOSER → 返回取消
            if (isChooserWithShareIntent(originalIntent)) {
                Intent targetIntent = originalIntent.getParcelableExtra(Intent.EXTRA_INTENT);
                String targetAction = targetIntent != null ? targetIntent.getAction() : "unknown_share";
                ReaLog.log("launch_intercept", "【虚假启动】系统分享,阻断启动: " + targetAction);
                ToastUtil.showUnique(activity, "✅ 已阻断系统分享: \n" + targetAction);
                if (requestCode > 0) {
                    callOnActivityResult(activity, requestCode, Activity.RESULT_CANCELED, null);
                }
                return;
            }
        }
    }

    try {
        // ===== 假装分享（微信/QQ/钉钉/微博） =====
        Map<String, Boolean> shareDetail = shareFakeDetailMap.getOrDefault(currentTargetApp, getDefaultShareDetailMap());
        boolean wechatFake = shareDetail.getOrDefault("wechat", false);
        boolean qqFake = shareDetail.getOrDefault("qq", false);
        boolean dingtalkFake = shareDetail.getOrDefault("dingtalk", false);
        boolean weiboFake = shareDetail.getOrDefault("weibo", false);

        if (originalIntent != null && (wechatFake || qqFake || dingtalkFake || weiboFake)) {
            if (wechatFake && isWeChatShareIntent(originalIntent)) {
                ShareHook.fakeWeChatShare(activity, activity.getClassLoader(), currentTargetApp);
                ReaLog.log("share_fake", "微信分享假装成功(插入虚假启动中)");
            } else if (qqFake && isQQShareIntent(originalIntent)) {
                ShareHook.fakeQQShare(activity, activity.getClassLoader());
                ReaLog.log("share_fake", "QQ分享假装成功(插入虚假启动中)");
            } else if (dingtalkFake && isDingTalkShareIntent(originalIntent)) {
                ShareHook.fakeDingTalkShare(activity, activity.getClassLoader());
                ReaLog.log("share_fake", "钉钉分享假装成功(插入虚假启动中)");
            } else if (weiboFake && isWeiboShareIntent(originalIntent)) {
                ShareHook.fakeWeiboShare(activity, activity.getClassLoader());
                ReaLog.log("share_fake", "微博分享假装成功(插入虚假启动中)");
            }
        }

        // ===== 设置时间偏移（统一使用 sFakeTimeOffset，清除 sTimeOffset） =====
        long minDuration = 3 * 60 * 1000;
        long maxDuration = 6 * 60 * 1000;
        long fakeDuration = minDuration + (long) (Math.random() * (maxDuration - minDuration));
        sFakeTimeOffset = fakeDuration;
        sTimeOffset = 0; // 清除其他偏移
        ReaLog.log("time_fake", "设置虚假启动偏移: " + fakeDuration + "ms (" + (fakeDuration / 1000) + "秒)");
        new Handler(Looper.getMainLooper()).removeCallbacks(mClearFakeTimeRunnable);

        // ===== 返回结果 =====
        if (requestCode > 0) {
            Intent resultIntent = new Intent();
            if (originalIntent != null) {
                Uri data = originalIntent.getData();
                if (data != null) resultIntent.setData(data);
                resultIntent.setAction(originalIntent.getAction());
                Set<String> categories = originalIntent.getCategories();
                if (categories != null) {
                    for (String cat : categories) resultIntent.addCategory(cat);
                }
                Bundle extras = originalIntent.getExtras();
                if (extras != null) resultIntent.putExtras(extras);
                resultIntent.setFlags(originalIntent.getFlags());
            }
            callOnActivityResult(activity, requestCode, Activity.RESULT_OK, resultIntent);
            ReaLog.log("launch_intercept", "虚假启动成功(有回调): " + finalDisplayName);
        }

        ToastUtil.showUnique(activity, "✅ 已虚假启动: " + finalDisplayName + "\n时间偏移" + (fakeDuration / 1000) + "秒");
        ReaLog.log("launch_intercept", "虚假启动成功: " + finalDisplayName);
        refreshAppToForeground(activity);
        new Handler(Looper.getMainLooper()).postDelayed(mClearFakeTimeRunnable, 5000);

    } catch (Throwable e) {
        ToastUtil.showUnique(activity, "虚假启动失败");
        ReaLog.log("launch_intercept", "虚假启动异常: " + e.getMessage());
        e.printStackTrace();
        // 清除所有时间偏移
        sFakeTimeOffset = 0;
        sTimeOffset = 0;
        new Handler(Looper.getMainLooper()).removeCallbacks(mClearFakeTimeRunnable);
    }
}
/**
 * 检查是否为文件选择器 Action（全版本兼容）
 */
private boolean isFilePickerAction(String action) {
    if (action == null) {
        return false;
    }
    
    return Intent.ACTION_GET_CONTENT.equals(action) ||
           Intent.ACTION_OPEN_DOCUMENT.equals(action) ||
           Intent.ACTION_PICK.equals(action) ||
           "android.intent.action.OPEN_DOCUMENT_TREE".equals(action);
}

/**
 * 检查 CHOOSER 内部是否为文件选择
 */
private boolean isChooserWithFilePicker(Intent originalIntent) {
    if (originalIntent == null || !Intent.ACTION_CHOOSER.equals(originalIntent.getAction())) {
        return false;
    }
    
    Intent targetIntent = originalIntent.getParcelableExtra(Intent.EXTRA_INTENT);
    if (targetIntent != null) {
        return isFilePickerAction(targetIntent.getAction());
    }
    
    return false;
}

/**
 * 检查 CHOOSER 内部是否为分享
 */
private boolean isChooserWithShareIntent(Intent originalIntent) {
    if (originalIntent == null || !Intent.ACTION_CHOOSER.equals(originalIntent.getAction())) {
        return false;
    }
    
    Intent targetIntent = originalIntent.getParcelableExtra(Intent.EXTRA_INTENT);
    if (targetIntent != null) {
        String targetAction = targetIntent.getAction();
        return Intent.ACTION_SEND.equals(targetAction) ||
               Intent.ACTION_SEND_MULTIPLE.equals(targetAction);
    }
    
    return false;
}

    private void hookActivityLifecycleForFakeBackground(ClassLoader classLoader) {
        try {
            XposedHelpers.findAndHookMethod(
                    "android.app.Activity",
                    classLoader,
                    "onPause",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            Activity activity = (Activity) param.thisObject;
                            if (!activity.getPackageName().equals(currentTargetApp)) return;
                            mPauseTimeMap.put(activity, System.currentTimeMillis());
                            ReaLog.log("time_fake", "记录时间: " + System.currentTimeMillis());
                        }
                    }
            );

            XposedHelpers.findAndHookMethod(
                    "android.app.Activity",
                    classLoader,
                    "onResume",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            Activity activity = (Activity) param.thisObject;
                            if (!activity.getPackageName().equals(currentTargetApp)) return;

                            Long pauseTime = mPauseTimeMap.get(activity);
                            if (pauseTime == null) {
                                return;
                            }

                            long realBackgroundDuration = System.currentTimeMillis() - pauseTime;

                            if (realBackgroundDuration < 3000) {
                                long minDuration = 65 * 1000;
                                long maxDuration = 5 * 60 * 1000;
                                long fakeDuration = minDuration + (long) (Math.random() * (maxDuration - minDuration));
                                sTimeOffset = fakeDuration;
                                ReaLog.log("time_fake", "设置前后台切换偏移: " + fakeDuration + "ms (" + (fakeDuration / 1000) + "秒)");
                                String durationStr = String.format("%d分%d秒",
                                        fakeDuration / 60000,
                                        (fakeDuration % 60000) / 1000);
                                ReaLog.log("time_fake", "Activity前后台切换: " + durationStr);
                            } else {
                                ReaLog.log("time_fake", "是真实后台[" + realBackgroundDuration + "ms]，跳过");
                            }

                            mPauseTimeMap.remove(activity);
                        }

                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            if (sTimeOffset != 0) {
                                ReaLog.log("time_fake", "清除前后台切换偏移 (原偏移 " + sTimeOffset + "ms)");
                                sTimeOffset = 0;
                            }
                        }
                    }
            );

            XposedHelpers.findAndHookMethod(
                    "java.lang.System",
                    null,
                    "currentTimeMillis",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            if (sTimeOffset != 0) {
                                long realTime = (long) param.getResult();
                                param.setResult(realTime - sTimeOffset);
                            }
                        }
                    }
            );
        } catch (Throwable t) {
            log("❌ Hook Activity前后台失败: " + t.getMessage());
            ReaLog.log("time_fake", "Activity前后台切换异常: " + t.getMessage());
        }
    }

    private void simulateBackgroundSwitch(final Activity activity) {
        if (activity == null || activity.isFinishing()) {
            ReaLog.log("time_fake", "前后Activity无效");
            return;
        }
        try {
            final ActivityManager am = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
            if (am == null) {
                ReaLog.log("time_fake", "Activity活动为空");
                return;
            }
            final int taskId = activity.getTaskId();

            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        activity.moveTaskToBack(true);
                        ReaLog.log("time_fake", "模拟移到后台");
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        am.moveTaskToFront(taskId, 0);
                                        ReaLog.log("time_fake", "模拟拉回前台(高版本)");
                                    } else {
                                        Intent intent = activity.getPackageManager()
                                                .getLaunchIntentForPackage(activity.getPackageName());
                                        if (intent != null) {
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                                            activity.startActivity(intent);
                                            ReaLog.log("time_fake", "模拟拉回前台(低版本)");
                                        }
                                    }
                                } catch (Throwable e) {
                                    log("拉回前台失败: " + e.getMessage());
                                    ReaLog.log("time_fake", "拉回前台异常: " + e.getMessage());
                                }
                            }
                        }, 30);
                    } catch (Throwable e) {
                        log("移到后台失败: " + e.getMessage());
                        ReaLog.log("time_fake", "移到后台异常: " + e.getMessage());
                    }
                }
            }, 10);
        } catch (Throwable e) {
            log("后台切换失败: " + e.getMessage());
        }
    }

    private Object createFakeUsageStats(String packageName, long fakeOffset) {
        try {
            Class<?> statsClass = Class.forName("android.app.usage.UsageStats");
            Object stats = statsClass.newInstance();
            long now = System.currentTimeMillis() - fakeOffset;
            XposedHelpers.setObjectField(stats, "mPackageName", packageName);
            XposedHelpers.setLongField(stats, "mTotalTimeInForeground", fakeOffset + 1000);
            XposedHelpers.setLongField(stats, "mLastTimeUsed", now);
            XposedHelpers.setLongField(stats, "mLastTimeForeground", now);
            return stats;
        } catch (Throwable t) {
            return null;
        }
    }

    private void installTimeFakeHooks(ClassLoader classLoader) {
        try {
            XposedHelpers.findAndHookMethod(
                    "java.lang.System",
                    classLoader,
                    "currentTimeMillis",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            long totalOffset = sFakeTimeOffset != 0 ? sFakeTimeOffset : sTimeOffset;
                            if (totalOffset != 0) {
                                long realTime = (long) param.getResult();
                                param.setResult(realTime - totalOffset);
                            }
                        }
                    }
            );
        } catch (Throwable t) {
        }

        try {
            XposedHelpers.findAndHookMethod(
                    "android.os.SystemClock",
                    classLoader,
                    "elapsedRealtime",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            long totalOffset = sFakeTimeOffset != 0 ? sFakeTimeOffset : sTimeOffset;
                            if (totalOffset != 0) {
                                long realTime = (long) param.getResult();
                                param.setResult(realTime - totalOffset);
                            }
                        }
                    }
            );
        } catch (Throwable t) {
            ReaLog.log("time_fake", "时间偏移异常1: " + t.getMessage());
        }

        try {
            XposedHelpers.findAndHookMethod(
                    "android.os.SystemClock",
                    classLoader,
                    "uptimeMillis",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            long totalOffset = sFakeTimeOffset != 0 ? sFakeTimeOffset : sTimeOffset;
                            if (totalOffset != 0) {
                                long realTime = (long) param.getResult();
                                param.setResult(realTime - totalOffset);
                            }
                        }
                    }
            );
        } catch (Throwable t) {
            ReaLog.log("time_fake", "时间偏移异常2: " + t.getMessage());
        }

        try {
            XposedHelpers.findAndHookMethod(
                    "android.os.SystemClock",
                    classLoader,
                    "elapsedRealtimeNanos",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            long totalOffset = sFakeTimeOffset != 0 ? sFakeTimeOffset : sTimeOffset;
                            if (totalOffset != 0) {
                                long realTime = (long) param.getResult();
                                param.setResult(realTime - (totalOffset * 1_000_000L));
                            }
                        }
                    }
            );
        } catch (Throwable t) {
            ReaLog.log("time_fake", "时间偏移异常3: " + t.getMessage());
        }

        try {
            XposedHelpers.findAndHookMethod(
                    "android.app.usage.UsageStatsManager",
                    classLoader,
                    "queryUsageStats",
                    int.class,
                    long.class,
                    long.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            if (sFakeTimeOffset != 0) {
                                List<Object> fakeStats = new ArrayList<Object>();
                                synchronized (globalCapturedPackages) {
                                    for (String pkg : globalCapturedPackages) {
                                        Object stats = createFakeUsageStats(pkg, sFakeTimeOffset);
                                        if (stats != null) fakeStats.add(stats);
                                    }
                                }
                                param.setResult(fakeStats);
                            }
                        }
                    }
            );
        } catch (Throwable t) {
            ReaLog.log("time_fake", "时间偏移异常4: " + t.getMessage());
        }
    }

    private Intent buildLaunchIntent(Activity activity, Intent oriIntent, String pkg, boolean needParams) {
        if (needParams && oriIntent != null) {
            Intent launchIntent = new Intent(oriIntent);
            launchIntent.putExtra("__hook_skip_flag", true);
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (oriIntent.getData() != null) {
                String scheme = oriIntent.getData().getScheme();
                if ("content".equals(scheme) || "file".equals(scheme)) {
                    launchIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    launchIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                }
            }
            ReaLog.log("launch_intercept", "启动拦截：带参启动 " + pkg);
            return launchIntent;
        } else {
            Intent launchIntent = new Intent(Intent.ACTION_MAIN);
            launchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            launchIntent.setPackage(pkg);
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            launchIntent.putExtra("__hook_skip_flag", true);
            ReaLog.log("launch_intercept", "启动拦截：无参启动 " + pkg);
            return launchIntent;
        }
    }
/*
    private void callOnActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        try {
            Method method = Activity.class.getDeclaredMethod("onActivityResult", int.class, int.class, Intent.class);
            method.setAccessible(true);
            method.invoke(activity, requestCode, resultCode, data);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
        }
    }
    */
    private void callOnActivityResult(final Activity activity, final int requestCode, final int resultCode, final Intent data) {
    ReaLog.log("launch_intercept", "虚假启动: 请求码=" + requestCode + ",回调码=" + resultCode);
    // 先尝试同步调用
    boolean success = false;
    try {
        XposedHelpers.callMethod(activity, "onActivityResult", requestCode, resultCode, data);
        success = true;
    } catch (Throwable t) {
        
        try {
            Method method = Activity.class.getDeclaredMethod("onActivityResult", int.class, int.class, Intent.class);
            method.setAccessible(true);
            method.invoke(activity, requestCode, resultCode, data);
            success = true;
        } catch (Throwable e2) {
        }
    }
    // 如果都失败了，延迟在主线程再试一次
    if (!success) {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    Method method = Activity.class.getDeclaredMethod("onActivityResult", int.class, int.class, Intent.class);
                    method.setAccessible(true);
                    method.invoke(activity, requestCode, resultCode, data);
                } catch (Throwable e) {
                }
            }
        }, 200);
    }
}



    private boolean isPackageReallyInstalled(String packageName) {
        if (TextUtils.isEmpty(packageName)) return false;
        try {
            Context context = (Context) XposedHelpers.callStaticMethod(
                    XposedHelpers.findClass("android.app.ActivityThread", null),
                    "currentApplication"
            );
            if (context == null) return false;
            PackageManager pm = context.getPackageManager();
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        } catch (Throwable t) {
            return false;
        }
    }

    private String extractPackageFromIntent(Intent intent) {
        if (intent == null) return null;
        if (intent.getComponent() != null) {
            return intent.getComponent().getPackageName();
        }
        if (!TextUtils.isEmpty(intent.getPackage())) {
            return intent.getPackage();
        }
        Uri data = intent.getData();
        if (data != null && "package".equals(data.getScheme())) {
            return data.getSchemeSpecificPart();
        }
        return null;
    }

    private void refreshAppToForeground(final Activity activity) {
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) return;
        try {
            Window window = activity.getWindow();
            if (window != null) {
                window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                window.getDecorView().invalidate();
                window.getDecorView().requestLayout();
            }
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    activity.onWindowFocusChanged(true);
                }
            });
        } catch (Throwable e) {
        }
    }

    private boolean isDetectionUrl(String url) {
        if (mPkgMgr != null) {
            return mPkgMgr.isDetectionUrl(url);
        }
        return false;
    }

    private void fakeOkHttpResponse(Object callback, String url, boolean isInstalled) {
        if (mPkgMgr != null) {
            mPkgMgr.fakeOkHttpResponse(callback, url, isInstalled);
        }
    }

    private Object createOkHttpFakeResponse(boolean isInstalled) {
        if (mPkgMgr != null) {
            return mPkgMgr.createOkHttpFakeResponse(isInstalled);
        }
        return null;
    }

    private boolean isBrowserFromIntent(Intent intent, PackageManager pm) {
        if (intent == null || pm == null) {
            return false;
        }
        String pkg = extractPackageFromIntent(intent);
        if (pkg == null) {
            return false;
        }
        String lowerPkg = pkg.toLowerCase();
        String[] browserKeywords = {
                "browser", "chrome", "quark", "firefox", "edge", "opera", "safari",
                "maxthon", "uc", "via", "yandex", "vivaldi", "brave", "duckduckgo",
                "huawei.browser", "honor.browser", "miui.browser", "xiaomi.browser",
                "oppo.browser", "realme.browser", "vivo.browser", "iqoo.browser",
                "oneplus.browser", "meizu.browser", "nubia.browser", "zte.browser",
                "lenovo.browser", "moto.browser", "heytap.browser", "samsung.browser"
        };
        for (String key : browserKeywords) {
            if (lowerPkg.contains(key)) {
                ReaLog.log("launch_intercept", "跳转浏览器: true -> " + pkg);
                return true;
            }
        }
        return false;
    }

    private boolean isBaseSystemPackage(String packageName) {
        if (mPkgMgr != null) {
            return mPkgMgr.isBaseSystemPackage(packageName);
        }
        return false;
    }

    private boolean isVendorPackage(String packageName) {
        if (mPkgMgr != null) {
            return mPkgMgr.isVendorPackage(packageName);
        }
        return false;
    }

    public boolean isRealSystemPackage(String packageName) {
    if (packageName == null) return false;

    // 使用 mRealSystemCache 缓存（已在 HookInit 中定义）
    Boolean cached = mRealSystemCache.get(packageName);
    if (cached != null && cached) {
        return true;
    }

    try {
        Context ctx = getApplicationContext();
        if (ctx == null) {
            return false;
        }
        PackageManager pm = ctx.getPackageManager();
        ApplicationInfo ai = pm.getApplicationInfo(packageName, 0);
        if (ai == null) {
            return false;
        }

        boolean isSystem = false;

        // 1. 路径匹配（扩展系统分区）
        if (ai.sourceDir != null) {
            isSystem = ai.sourceDir.startsWith("/system/") ||
                       ai.sourceDir.startsWith("/product/") ||
                       ai.sourceDir.startsWith("/vendor/") ||
                       ai.sourceDir.startsWith("/odm/");
        }

        // 2. 标志位兜底
        if (!isSystem) {
            isSystem = (ai.flags & ApplicationInfo.FLAG_SYSTEM) != 0 ||
                       (ai.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0;
        }

        if (isSystem) {
            mRealSystemCache.put(packageName, true);
        }
        return isSystem;
    } catch (PackageManager.NameNotFoundException e) {
        mRealSystemCache.put(packageName, false);
        return false;
    } catch (Throwable t) {
        ReaLog.log("system", "过滤系统应用异常: " + t.getMessage());
        return false;
    }
}

    public boolean isSystemCorePackage(String packageName) {
        if (mPkgMgr != null) {
            return mPkgMgr.isSystemCorePackage(packageName);
        }
        return false;
    }

    public int getPackageStatus(String packageName) {
        if (mPkgMgr != null) {
            return mPkgMgr.getPackageStatus(packageName);
        }
        return -1;
    }

    private boolean shouldReturnInstalledForPackage(String packageName) {
        if (mPkgMgr != null) {
            return mPkgMgr.shouldReturnInstalledForPackage(packageName);
        }
        return false;
    }

    private boolean isExcludedPackage(String targetPkg) {
        if (mPkgMgr != null) {
            return mPkgMgr.isExcludedPackage(targetPkg);
        }
        return false;
    }

    private boolean isValidPackageName(String str) {
        if (mPkgMgr != null) {
            return mPkgMgr.isValidPackageName(str);
        }
        return false;
    }

    private void hookQueryIntentActivities(ClassLoader classLoader) {
        try {
            XposedBridge.hookAllMethods(
                    XposedHelpers.findClass("android.app.ApplicationPackageManager", classLoader),
                    "queryIntentActivities",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            handleQuery(param);
                        }
                    }
            );
            try {
                XposedBridge.hookAllMethods(
                        XposedHelpers.findClass("android.app.ApplicationPackageManager", classLoader),
                        "queryIntentActivitiesAsUser",
                        new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) {
                                handleQuery(param);
                            }
                        }
                );
            } catch (Throwable e) {
            }
        } catch (Throwable e) {
            log("Hook 查询意图活动失败: " + e.getMessage());
        }
    }

    private void handleQuery(MethodHookParam param) {
        try {
            if (!installStatusMap.getOrDefault(currentTargetApp, true)) {
                param.setResult(new ArrayList<ResolveInfo>());
                ReaLog.log("package_query", "查询意图活动: 未安装，返回空列表");
                return;
            }

            Intent intent = null;
            for (Object o : param.args) {
                if (o instanceof Intent) {
                    intent = (Intent) o;
                    break;
                }
            }
            if (intent == null) {
                param.setResult(new ArrayList<ResolveInfo>());
                return;
            }

            if (MODULE_PACKAGE.equals(currentTargetApp)) {
                return;
            }

            String pkg = extractPackageFromIntent(intent);
            if (pkg != null && (pkg.equals(currentTargetApp) || isSystemCorePackage(pkg) || isExcludedPackage(pkg))) {
                return;
            }

            boolean isExplicit = (intent.getComponent() != null || intent.getPackage() != null);
            if (isExplicit) {
                return;
            }

            List<ResolveInfo> fakeList = new ArrayList<>();
            synchronized (globalCapturedPackages) {
                for (String fakePkg : globalCapturedPackages) {
                    if (shouldReturnInstalledForPackage(fakePkg)) {
                        ResolveInfo fake = createFakeResolveInfo(fakePkg);
                        if (fake != null) {
                            fakeList.add(fake);
                        }
                    }
                }
            }
            param.setResult(fakeList);
            ReaLog.log("launch", "查询意图活动 返回 " + fakeList.size() + " 个虚假隐式活动");
        } catch (Exception e) {
            log("handleQuery异常: " + e.getMessage());
            param.setResult(new ArrayList<ResolveInfo>());
        }
    }
/*
private void handleQuery(MethodHookParam param) {
    try {
        Intent intent = null;
        for (Object o : param.args) {
            if (o instanceof Intent) {
                intent = (Intent) o;
                break;
            }
        }
        if (intent == null) {
            param.setResult(new ArrayList<ResolveInfo>());
            return;
        }

        // ===== 新增：启动检测放行逻辑 =====
        // 检测是否为启动检测查询（ACTION_MAIN + CATEGORY_LAUNCHER）
        boolean isLaunchQuery = Intent.ACTION_MAIN.equals(intent.getAction()) &&
                intent.hasCategory(Intent.CATEGORY_LAUNCHER);
        String pkg = extractPackageFromIntent(intent);
        // 如果包在捕获列表中（固定已安装），且是启动查询，则放行
        if (pkg != null && isLaunchQuery && HookInit.globalCapturedPackages.contains(pkg)) {
            ReaLog.log("intent_query", "启动检测放行: " + pkg);
            return; // 不拦截，让系统返回真实信息
        }
        // ===== 新增结束 =====

        if (!installStatusMap.getOrDefault(currentTargetApp, true)) {
            param.setResult(new ArrayList<ResolveInfo>());
            ReaLog.log("package_query", "查询意图活动: 未安装，返回空列表");
            return;
        }

        if (MODULE_PACKAGE.equals(currentTargetApp)) {
            return;
        }

        if (pkg != null && (pkg.equals(currentTargetApp) || isSystemCorePackage(pkg) || isExcludedPackage(pkg))) {
            return;
        }

        boolean isExplicit = (intent.getComponent() != null || intent.getPackage() != null);
        if (isExplicit) {
            return;
        }

        List<ResolveInfo> fakeList = new ArrayList<>();
        synchronized (HookInit.globalCapturedPackages) {
            for (String fakePkg : HookInit.globalCapturedPackages) {
                if (shouldReturnInstalledForPackage(fakePkg)) {
                    ResolveInfo fake = createFakeResolveInfo(fakePkg);
                    if (fake != null) {
                        fakeList.add(fake);
                    }
                }
            }
        }
        param.setResult(fakeList);
        ReaLog.log("launch", "查询意图活动 返回 " + fakeList.size() + " 个虚假隐式活动");
    } catch (Exception e) {
        log("handleQuery异常: " + e.getMessage());
        param.setResult(new ArrayList<ResolveInfo>());
    }
}
*/
    private void hookDesktopAppQuery(ClassLoader classLoader) {
        try {
            XposedHelpers.findAndHookMethod(
                    "android.app.ApplicationPackageManager",
                    classLoader,
                    "queryIntentActivities",
                    Intent.class,
                    int.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            handleDesktopAppQuery(param);
                        }
                    }
            );

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    XposedHelpers.findAndHookMethod(
                            "android.app.ApplicationPackageManager",
                            classLoader,
                            "queryIntentActivitiesAsUser",
                            Intent.class,
                            int.class,
                            int.class,
                            new XC_MethodHook() {
                                @Override
                                protected void beforeHookedMethod(MethodHookParam param) {
                                    handleDesktopAppQuery(param);
                                }
                            }
                    );
                } catch (Throwable ignored) {
                }
            }

            ReaLog.log("intent_query", "桌面应用列表穿透已安装");
        } catch (Throwable t) {
            ReaLog.log("intent_query", "桌面应用列表穿透失败: " + t.getMessage());
        }
    }

    private void handleDesktopAppQuery(MethodHookParam param) {
        try {
          /*  if (!installStatusMap.getOrDefault(currentTargetApp, true)) {
                param.setResult(new ArrayList<ResolveInfo>());
                ReaLog.log("intent_query", "桌面穿透查询: 未安装，返回空列表");
                return;
            }*/
            Intent intent = null;
            for (Object arg : param.args) {
                if (arg instanceof Intent) {
                    intent = (Intent) arg;
                    break;
                }
            }
            if (intent == null) return;

            boolean isLauncherQuery = Intent.ACTION_MAIN.equals(intent.getAction()) &&
                    intent.hasCategory(Intent.CATEGORY_LAUNCHER);
            if (!isLauncherQuery) return;

            Set<String> allPackages = new HashSet<>();

            allPackages.addAll(HookInit.globalCapturedPackages);

            List<PackageConfig> configs = packageConfigMap.getOrDefault(currentTargetApp, new ArrayList<>());
            for (PackageConfig config : configs) {
                if ("installed".equals(config.statusMode)) {
                    allPackages.add(config.packageName);
                }
            }

            allPackages.removeAll(HookInit.excludedPackagesMap.getOrDefault(currentTargetApp, new ArrayList<>()));

            List<ResolveInfo> fakeList = new ArrayList<>();
            for (String pkg : allPackages) {
                if (isSystemCorePackage(pkg)) continue;
                if (isExcludedPackage(pkg)) continue;
                if (!shouldReturnInstalledForPackage(pkg)) continue;
                ResolveInfo fake = createFakeResolveInfo(pkg);
                if (fake != null) {
                    fakeList.add(fake);
                }
            }

            if (fakeList.isEmpty()) {
                ResolveInfo selfFake = createFakeResolveInfo(currentTargetApp);
                if (selfFake != null) {
                    fakeList.add(selfFake);
                } else {
                    ResolveInfo fallback = new ResolveInfo();
                    ActivityInfo ai = new ActivityInfo();
                    ai.packageName = currentTargetApp;
                    ai.name = currentTargetApp + ".MainActivity";
                    ai.enabled = true;
                    ai.exported = true;
                    fallback.activityInfo = ai;
                    fallback.priority = 1;
                    fakeList.add(fallback);
                }
                ReaLog.log("intent_query", "桌面穿透查询，兜底返回自身包");
            }

            param.setResult(fakeList);
            ReaLog.log("intent_query", "桌面穿透查询返回 " + fakeList.size() + " 个虚假应用");

        } catch (Throwable t) {
            ReaLog.log("intent_query", "桌面穿透查询异常: " + t.getMessage());
        }
    }

    private void hookBundleGetString(ClassLoader classLoader) {
        try {
            Class<?> bundleClass = Class.forName("android.os.Bundle", false, classLoader);
            XposedBridge.hookAllMethods(bundleClass, "getString", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    try {
                        if (param.thisObject == null) {
                            if (param.args.length >= 2) {
                                param.setResult(param.args[1]);
                            } else {
                                param.setResult("");
                            }
                            return;
                        }
                        Bundle bundle = (Bundle) param.thisObject;
                        try {
                            bundle.hashCode();
                        } catch (Throwable e) {
                            if (param.args.length >= 2) {
                                param.setResult(param.args[1]);
                            } else {
                                param.setResult("");
                            }
                            return;
                        }
                        if (param.args.length >= 1) {
                            String key = (String) param.args[0];
                            if (!bundle.containsKey(key)) {
                                if (param.args.length >= 2) {
                                    param.setResult(param.args[1]);
                                } else {
                                    param.setResult("");
                                }
                                return;
                            }
                        }
                    } catch (Throwable t) {
                    }
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    try {
                        if (param.getResult() == null) {
                            if (param.args.length >= 2) {
                                param.setResult(param.args[1]);
                            } else {
                                param.setResult("");
                            }
                        }
                    } catch (Throwable t) {
                    }
                }
            });

            XposedBridge.hookAllMethods(bundleClass, "getString", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    try {
                        if (param.args.length == 2 && param.args[0] instanceof String) {
                            if (param.thisObject == null) {
                                param.setResult(param.args[1]);
                                return;
                            }
                            Bundle bundle = (Bundle) param.thisObject;
                            try {
                                bundle.hashCode();
                            } catch (Throwable e) {
                                param.setResult(param.args[1]);
                                return;
                            }
                            String key = (String) param.args[0];
                            if (!bundle.containsKey(key)) {
                                param.setResult(param.args[1]);
                            }
                        }
                    } catch (Throwable t) {
                    }
                }
            });
        } catch (Throwable t) {
            ReaLog.log("system", "Bundle失败: " + t.getMessage());
        }
    }

    private void hookBundleEmptyInstance(ClassLoader classLoader) {
        try {
            Class<?> bundleClass = Class.forName("android.os.Bundle", false, classLoader);
            XposedHelpers.findAndHookConstructor(bundleClass, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    if (param.getResult() == null) return;
                    Bundle bundle = (Bundle) param.getResult();
                    try {
                        bundle.putBoolean("__hook_protected", true);
                    } catch (Throwable ignored) {
                    }
                }
            });

            String[] methods = {"getString", "getInt", "getBoolean", "getLong",
                    "getDouble", "getFloat", "getBundle", "getSerializable",
                    "getParcelable", "getParcelableArrayList", "getStringArrayList",
                    "getIntegerArrayList", "getBooleanArray", "containsKey"
            };
            for (final String methodName : methods) {
                try {
                    XposedHelpers.findAndHookMethod(bundleClass, methodName, String.class,
                            new XC_MethodHook() {
                                @Override
                                protected void beforeHookedMethod(MethodHookParam param)
                                        throws Throwable {
                                    if (param.thisObject == null) {
                                        setDefaultReturnValue(param);
                                        return;
                                    }
                                    Bundle bundle = (Bundle) param.thisObject;
                                    try {
                                        bundle.hashCode();
                                    } catch (Throwable e) {
                                        setDefaultReturnValue(param);
                                    }
                                }

                                private void setDefaultReturnValue(MethodHookParam param) {
                                    if (param.method instanceof Method) {
                                        Class<?> returnType = ((Method) param.method).getReturnType();
                                        if (returnType == String.class) param.setResult("");
                                        else if (returnType == int.class) param.setResult(0);
                                        else if (returnType == long.class) param.setResult(0L);
                                        else if (returnType == boolean.class)
                                            param.setResult(false);
                                        else if (returnType == double.class) param.setResult(0.0);
                                        else if (returnType == float.class) param.setResult(0.0f);
                                        else param.setResult(null);
                                    }
                                }
                            });
                } catch (Throwable ignored) {
                }
                try {
                    XposedHelpers.findAndHookMethod(bundleClass, methodName, String.class, Object.class,
                            new XC_MethodHook() {
                                @Override
                                protected void beforeHookedMethod(MethodHookParam param)
                                        throws Throwable {
                                    if (param.thisObject == null) {
                                        param.setResult(param.args[1]);
                                        return;
                                    }
                                    Bundle bundle = (Bundle) param.thisObject;
                                    try {
                                        bundle.hashCode();
                                    } catch (Throwable e) {
                                        param.setResult(param.args[1]);
                                        return;
                                    }
                                    String key = (String) param.args[0];
                                    if (!bundle.containsKey(key)) {
                                        param.setResult(param.args[1]);
                                    }
                                }
                            });
                } catch (Throwable ignored) {
                }
            }
        } catch (Throwable t) {
            ReaLog.log("system", "HookBundle失败: " + t.getMessage());
        }
    }

    private void hookSoPathInApp(ClassLoader classLoader) {
        try {
            Context ctx = getApplicationContext();
            if (ctx == null) {
                ReaLog.log("system", "so路径修复: 无法获取 Context");
                return;
            }
            ApplicationInfo ai = ctx.getApplicationInfo();
            if (ai == null) {
                ReaLog.log("system", "so路径修复: ApplicationInfo 为空");
                return;
            }
            File sourceFile = new File(ai.sourceDir);
            File parent = sourceFile.getParentFile();
            if (parent == null) {
                ReaLog.log("system", "so路径修复: 无法获取 parent 目录");
                return;
            }
            String correctLibDir = new File(parent, "lib").getAbsolutePath();
            String currentLibDir = ai.nativeLibraryDir;
            if (!correctLibDir.equals(currentLibDir)) {
                XposedHelpers.setObjectField(ai, "nativeLibraryDir", correctLibDir);
                ReaLog.log("system", "so路径已修复: " + currentLibDir + " -> " + correctLibDir);
            }
        } catch (Throwable t) {
            ReaLog.log("system", "so路径修复异常: " + t.getMessage());
        }
    }

    private void hookPackageManagerReflect() {
        try {
            XposedHelpers.findAndHookMethod(
                    "java.lang.Class",
                    null,
                    "getMethod",
                    String.class,
                    Class[].class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            try {
                                if (!(param.thisObject instanceof Class)) return;
                                String methodName = (String) param.args[0];

                                if (methodName == null) return;
                                if (!methodName.contains("getPackageInfo") &&
                                        !methodName.contains("getApplicationInfo") &&
                                        !methodName.contains("getInstalledPackages") &&
                                        !methodName.contains("getInstalledApplications") &&
                                        !methodName.contains("AsUser") &&
                                        !methodName.contains("hidden") &&
                                        !methodName.contains("internal")) {
                                    return;
                                }

                                Class<?> targetClass = (Class<?>) param.thisObject;
                                String className = targetClass.getName();
                                boolean isPackageManagerRelated = className.equals("android.content.pm.PackageManager") ||
                                        className.equals("android.app.ApplicationPackageManager") ||
                                        className.contains("PackageManager");

                                if (isPackageManagerRelated) {
                                    param.setResult(null);
                                }
                            } catch (Throwable t) {
                                log("反射监控异常: " + t.getMessage());
                            }
                        }
                    }
            );
        } catch (Throwable t) {
            log("Hook反射监控初始化失败: " + t.getMessage());
        }
    }


    private void hookSystemFileRead() {
        try {
            XposedHelpers.findAndHookMethod(
                    "java.io.FileInputStream",
                    null,
                    "FileInputStream",
                    String.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            String filePath = (String) param.args[0];
                            if (filePath.contains("/data/system/packages.xml") ||
                                    filePath.contains("/data/system/packages.list") ||
                                    filePath.contains("com.android.settings/databases/apps.db")) {
                                param.setThrowable(new SecurityException("权限不足，无法读取"));
                            }
                        }
                    }
            );
        } catch (Throwable t) {
        }
    }

    private void hookSystemProperties(ClassLoader classLoader) {
        try {
            Class<?> clazz = XposedHelpers.findClassIfExists("android.os.SystemProperties", classLoader);
            if (clazz == null) {
                return;
            }

            final String targetPkg = currentTargetApp;
            if (targetPkg == null || targetPkg.isEmpty()) {
                ReaLog.log("intent_query", "系统属性：当前目标包名为空，跳过");
                return;
            }

            try {
                XposedHelpers.findAndHookMethod(clazz, "get", String.class,
                        new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param)
                                    throws Throwable {
                                String key = (String) param.args[0];
                                String value = (String) param.getResult();
                                if (value != null && !value.isEmpty() && value.contains(targetPkg)) {
                                    String filtered = filterPackageFromProperty(value, targetPkg);
                                    param.setResult(filtered);
                                    ReaLog.log("intent_query", "系统属性: " + key + " 过滤包名");
                                }
                            }
                        }
                );
            } catch (Throwable ignored) {
                ReaLog.log("intent_query", "系统属性(单参数) Hook异常: " + ignored.getMessage());
            }

            try {
                XposedHelpers.findAndHookMethod(clazz, "get", String.class, String.class,
                        new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param)
                                    throws Throwable {
                                String key = (String) param.args[0];
                                String value = (String) param.getResult();
                                if (value != null && !value.isEmpty() && value.contains(targetPkg)) {
                                    String filtered = filterPackageFromProperty(value, targetPkg);
                                    param.setResult(filtered);
                                    ReaLog.log("intent_query", "系统属性(双参数): " + key + " 过滤包名");
                                }
                            }
                        }
                );
            } catch (Throwable ignored) {
                ReaLog.log("intent_query", "系统属性(双参数) Hook异常: " + ignored.getMessage());
            }

            try {
                XposedHelpers.findAndHookMethod(clazz, "getInt", String.class, int.class,
                        new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param)
                                    throws Throwable {
                                String key = (String) param.args[0];
                                if (key != null && key.toLowerCase().contains("package")) {
                                    param.setResult(param.args[1]);
                                    ReaLog.log("intent_query", "系统属性: " + key + " 返回默认值");
                                }
                            }
                        }
                );
            } catch (Throwable ignored) {
                ReaLog.log("intent_query", "系统属性 Hook异常: " + ignored.getMessage());
            }
        } catch (Throwable t) {
            log("❌ Hook 系统属性失败: " + t.getMessage());
            ReaLog.log("intent_query", "系统属性异常: " + t.getMessage());
        }
    }

    private String filterPackageFromProperty(String value, String targetPkg) {
        ReaLog.log("intent_query", "过滤属性中的包名: target=" + targetPkg);
        if (value == null || value.isEmpty() || targetPkg == null || targetPkg.isEmpty()) {
            ReaLog.log("intent_query", "属性过滤: 值或目标包名为空，返回原值");
            return value;
        }
        String[] delimiters = {":", ",", ";", " "};
        for (String delim : delimiters) {
            if (value.contains(delim)) {
                String[] parts = value.split(delim);
                StringBuilder filtered = new StringBuilder();
                for (String part : parts) {
                    String trimmed = part.trim();
                    if (!trimmed.contains(targetPkg)) {
                        if (filtered.length() > 0) filtered.append(delim);
                        filtered.append(trimmed);
                    } else {
                        ReaLog.log("intent_query", "属性过滤: 移除包名 " + trimmed);
                    }
                }
                return filtered.toString();
            }
        }
        if (value.contains(targetPkg)) {
            ReaLog.log("intent_query", "属性过滤: 移除单一包名");
            return "";
        }
        return value;
    }

    public void initForEmbed(ClassLoader classLoader, String targetPackageName, Context context) {
        ReaLog.log("system", "内嵌模式初始化开始: " + targetPackageName);
        currentTargetApp = targetPackageName;

        try {
            loadConfigFromFile();
            ReaLog.log("system", "内嵌模式配置加载完成: " + targetPackageName);
        } catch (Throwable e) {
            createDefaultConfig();
            ReaLog.log("system", "内嵌模式创建默认配置: " + targetPackageName);
        }

        Context appContext = context;
        if (appContext == null) {
            try {
                appContext = (Context) XposedHelpers.callStaticMethod(
                        XposedHelpers.findClass("android.app.ActivityThread", null),
                        "currentApplication");
            } catch (Throwable e) {
            }
        }
        if (appContext == null) {
            try {
                appContext = (Context) XposedHelpers.callStaticMethod(
                        XposedHelpers.findClass("android.app.AppGlobals", null),
                        "getInitialApplication");
            } catch (Throwable e) {
            }
        }
        ClassLoader realClassLoader = classLoader;
        if (realClassLoader == null && appContext != null) {
            realClassLoader = appContext.getClassLoader();
        }
        if (realClassLoader == null) {
            realClassLoader = getClass().getClassLoader();
        }
        String realPackageName = targetPackageName;
        if (realPackageName == null || realPackageName.isEmpty()) {
            if (appContext != null) {
                realPackageName = appContext.getPackageName();
            } else {
                realPackageName = "unknown";
            }
        }

        if (appContext != null) {
            try {
                ApplicationInfo ai = appContext.getApplicationInfo();
                if (ai != null) {
                    XposedHelpers.setObjectField(ai, "nativeLibraryDir", ai.nativeLibraryDir);
                }
            } catch (Throwable ignored) {
            }
        }

        Map<String, Boolean> detail = antiDetectionDetailMap.getOrDefault(realPackageName, new HashMap<String, Boolean>());
        AntiDetection.initForEmbed(classLoader, currentTargetApp, this);
        ReaLog.log("system", "内嵌模式痕迹检测初始化完成");

        VpnStatusFaker.installForEmbed(realClassLoader, this, realPackageName);
        ReaLog.log("system", "内嵌模式网络代理初始化完成");

        boolean forceDefaultBack = forceDefaultBackMap.getOrDefault(realPackageName, false);
        if (forceDefaultBack) {
            hookActivityDefaultBack(realClassLoader);
            ReaLog.log("system", "内嵌模式强制返回键已启用");
        }

        boolean crashProtect = crashProtectEnabledMap.getOrDefault(realPackageName, true);
        applyCrashProtect(crashProtect, appContext);
        ReaLog.log("system", "内嵌模式Java异常吞噬: " + (crashProtect ? "启用" : "禁用"));

        doHook(realPackageName, realClassLoader, appContext);
        ReaLog.log("system", "内嵌模式初始化完成: " + realPackageName);
    }

    public static class HookProvider extends ContentProvider {
        private HookInit hookInstance;

        @Override
        public boolean onCreate() {
            try {
                String targetPackage = getContext() != null
                        ? getContext().getPackageName()
                        : "unknown";
                ClassLoader appClassLoader = getContext() != null
                        ? getContext().getClassLoader()
                        : null;
                hookInstance = new HookInit();
                hookInstance.initForEmbed(appClassLoader, targetPackage, getContext());
                return true;
            } catch (Throwable t) {
                return false;
            }
        }

        @Override
        public Cursor query(
                Uri uri,
                String[] projection,
                String selection,
                String[] selectionArgs,
                String sortOrder) {
            if (hookInstance != null &&
                    uri != null &&
                    uri.toString().contains("module_status")) {
                MatrixCursor cursor = new MatrixCursor(
                        new String[]{"module_active", "target_app", "install_status"}
                );
                cursor.addRow(
                        new Object[]{
                                true,
                                hookInstance.currentTargetApp,
                                hookInstance.installStatusMap.get(hookInstance.currentTargetApp),
                        }
                );
                return cursor;
            }
            return null;
        }

        @Override
        public String getType(Uri uri) {
            return null;
        }

        @Override
        public Uri insert(Uri uri, ContentValues values) {
            if (hookInstance != null &&
                    uri != null &&
                    uri.toString().contains("update_config")) {
                if (values != null) {
                    try {
                        String key = values.getAsString("key");
                        String value = values.getAsString("value");

                        if ("install_status".equals(key)) {
                            boolean newStatus = "true".equals(value);
                            hookInstance.installStatusMap.put(
                                    hookInstance.currentTargetApp,
                                    newStatus
                            );
                            hookInstance.saveConfigToFile();
                        }
                    } catch (Throwable t) {
                    }
                }
            }
            return null;
        }

        @Override
        public int delete(Uri uri, String selection, String[] selectionArgs) {
            return 0;
        }

        @Override
        public int update(
                Uri uri,
                ContentValues values,
                String selection,
                String[] selectionArgs) {
            return 0;
        }
    }
}
