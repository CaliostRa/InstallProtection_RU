package com.install.appinstall.xl.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import com.install.appinstall.xl.HookInit;
import com.install.appinstall.xl.util.DebugModeManager;

public class AntiDetection {

    private static final String MODULE_PACKAGE = "com.install.appinstall.xl";

    // ========== 9 个独立开关 ==========
    private static volatile boolean enableClass = true;
    private static volatile boolean enableFile = true;
    private static volatile boolean enablePm = true;
    private static volatile boolean enableProc = true;
    private static volatile boolean enableCmd = true;
    private static volatile boolean enableStacktrace = true;
    private static volatile boolean enableRoot = true;
    private static volatile boolean enableAdb = true;
    private static volatile boolean enableDev = true;

    private static volatile boolean isEnabled = false;
    private static HookInit sHookInstance;
    // private static final List<String> sDisabledFeatures = Collections.synchronizedList(new
    // ArrayList<>());
    private static final List<
    String> sDisabledFeatures = Collections.synchronizedList(new ArrayList<String>());
    // ========== 错误计数（内存，每次启动重置） ==========
    private static final Map<String, Integer> sErrorCountMap = new ConcurrentHashMap<
    String, Integer>();
    private static final int ERROR_THRESHOLD = 2;
    /*
     private static final Set<String> IGNORABLE_EXCEPTIONS = new HashSet<String>(Arrays.asList(
     "java.lang.NoSuchMethodError",
     "java.lang.NoClassDefFoundError",
     "java.lang.ClassNotFoundException"
     ));

     // ========== 包名黑名单 ==========
     private static final Set<String> ROOT_PACKAGES_EXACT = new HashSet<String>(Arrays.asList(
     "com.topjohnwu.magisk",
     "io.github.vvb2060.magisk",
     "com.thirdparty.superuser",
     "com.koushikdutta.superuser",
     "eu.chainfire.supersu",
     "me.phh.superuser",
     "com.kingroot.kinguser",
     "com.kingouser.com",
     "com.kingo.root",
     "com.devadvance.rootcloak",
     "com.amphoras.hidemyroot",
     "com.dimonvideo.luckypatcher",
     "com.chelpus.lackypatch",
     "cc.madkite.freedom",
     "org.blackmart.market",
     "com.saurik.substrate",
     "me.weishu.kernelsu",
     "com.rifsxd.ksunext",
     "me.bmax.apatch",
     "com.nenya.mag",
     "com.magisk.modules.help"
     ));

     private static final Set<String> XPACKAGES_EXACT = new HashSet<String>(Arrays.asList(
     "de.robv.android.xposed.installer",
     "de.robv.android.xposed",
     "org.meowcat.edxposed.manager",
     "io.github.lsposed.manager",
     "org.lsposed.manager",
     "com.github.lsposed.manager",
     "com.lsposed.lspatch",
     "com.topjohnwu.magisk",
     "io.github.huskydg.magisk",
     "io.va.exposed.",
     "com.saurik.substrate"
     ));
     */

// ========== 敏感关键词统一数组（忽略大小写，用于模糊匹配） ==========
    private static final Set<String> SENSITIVE_KEYWORDS = new HashSet<String>(Arrays.asList(
                                                                                  // Xposed / LSPosed 相关
                                                                                  "xposed", "edxposed", "lsposed", "lspd", "zygisk_lsposed",
                                                                                  // Magisk 及其分支
                                                                                  "magisk", "kitsune", "alpha", "shamiko", "zygisk", "riru", "sui",
                                                                                  // KernelSU / APatch
                                                                                  "kernelsu", "ksu", "ksud", "apatch",
                                                                                  // 太极
                                                                                  "taichi",
                                                                                  // 检测工具
                                                                                  "momo", "ruru", "applist detector", "native detector",
                                                                                  // 虚拟环境
                                                                                  "virtualxposed", "virtual app",
                                                                                  // 其他
                                                                                  "superuser", "supersu", "kingroot", "kinguser", "kingo", "rootcloak",
                                                                                  "hidemyroot", "luckypatcher", "lackypatch", "freedom", "blackmart",
                                                                                  "substrate", "exposed", "modules.help"
                                                                              ));

// ========== 精确包名列表（优先匹配） ==========
    private static final Set<String> XPACKAGES_EXACT = new HashSet<String>(Arrays.asList(
                                                                               // Xposed / LSPosed
                                                                               "de.robv.android.xposed.installer",
                                                                               "de.robv.android.xposed",
                                                                               "org.meowcat.edxposed.manager",
                                                                               "io.github.lsposed.manager",
                                                                               "org.lsposed.manager",
                                                                               "com.github.lsposed.manager",
                                                                               "com.lsposed.lspatch",
                                                                               // Magisk
                                                                               "com.topjohnwu.magisk",
                                                                               "io.github.huskydg.magisk",
                                                                               // KernelSU / APatch
                                                                               "me.weishu.kernelsu",
                                                                               "com.rifsxd.ksunext",
                                                                               "me.bmax.apatch",
                                                                               // 太极
                                                                               "me.weishu.exp",
                                                                               // 老版 SuperSU
                                                                               "com.noshufou.android.su",
                                                                               "eu.chainfire.supersu",
                                                                               "com.koushikdutta.superuser",
                                                                               // 检测工具
                                                                               "com.byxiaorun.detector",
                                                                               "com.godevelopers.XposedChecker",
                                                                               "com.scottyab.rootbeer.sample",
                                                                               // 其他
                                                                               "io.va.exposed",
                                                                               "com.saurik.substrate",
                                                                               "com.kingroot.kinguser",
                                                                               "com.kingouser.com",
                                                                               "com.kingo.root",
                                                                               "com.devadvance.rootcloak",
                                                                               "com.amphoras.hidemyroot",
                                                                               "com.dimonvideo.luckypatcher",
                                                                               "com.chelpus.lackypatch",
                                                                               "cc.madkite.freedom",
                                                                               "org.blackmart.market",
                                                                               "com.nenya.mag",
                                                                               "com.magisk.modules.help"
                                                                           ));

// ========== 可忽略异常列表 ==========
    private static final Set<String> IGNORABLE_EXCEPTIONS = new HashSet<String>(Arrays.asList(
                                                                                    "java.lang.NoSuchMethodError",
                                                                                    "java.lang.NoClassDefFoundError",
                                                                                    "java.lang.ClassNotFoundException"
                                                                                ));

    // ========== 日志 ==========
    private static void log(String msg) {
        if (sHookInstance != null) {
            sHookInstance.log(msg);
        } else {
            XposedBridge.log(msg);
        }
    }

    // ========== 开关设置 ==========
    public static void setClassEnabled(boolean enabled) {
        enableClass = enabled;
    }

    public static void setFileEnabled(boolean enabled) {
        enableFile = enabled;
    }

    public static void setPmEnabled(boolean enabled) {
        enablePm = enabled;
    }

    public static void setProcEnabled(boolean enabled) {
        enableProc = enabled;
    }

    public static void setCmdEnabled(boolean enabled) {
        enableCmd = enabled;
    }

    public static void setStacktraceEnabled(boolean enabled) {
        enableStacktrace = enabled;
    }

    public static void setRootEnabled(boolean enabled) {
        enableRoot = enabled;
    }

    public static void setAdbEnabled(boolean enabled) {
        enableAdb = enabled;
    }

    public static void setDevEnabled(boolean enabled) {
        enableDev = enabled;
    }

    public static void reset() {
        isEnabled = false;
    }

    private static void setFeatureEnabled(String key, boolean enabled) {
        if ("class".equals(key)) enableClass = enabled;
        else if ("file".equals(key)) enableFile = enabled;
        else if ("pm".equals(key)) enablePm = enabled;
        else if ("proc".equals(key)) enableProc = enabled;
        else if ("cmd".equals(key)) enableCmd = enabled;
        else if ("stacktrace".equals(key)) enableStacktrace = enabled;
        else if ("root".equals(key)) enableRoot = enabled;
        else if ("adb".equals(key)) enableAdb = enabled;
        else if ("dev".equals(key)) enableDev = enabled;
    }

    private static void disableSubFeature(String key) {
        if (sHookInstance == null) return;
        try {
            String app = sHookInstance.getCurrentTargetApp();
            if (app == null || app.isEmpty()) return;
            Map<String, Boolean> detailMap = HookInit.antiDetectionDetailMap.get(app);
            if (detailMap == null) {
                detailMap = new HashMap<String, Boolean>();
                HookInit.antiDetectionDetailMap.put(app, detailMap);
            }
            detailMap.put(key, false);
            sHookInstance.saveConfigToFile();
            if (!sDisabledFeatures.contains(key)) {
                sDisabledFeatures.add(key);
            }
            ReaLog.antiDetection("error", "子功能自动禁用", key);
        } catch (Throwable t) {
            log("禁用子功能失败: " + t.getMessage());
        }
    }

    // ========== 错误计数与自动禁用 ==========
    private static boolean checkAndDisableOnStartup(String featureKey, boolean enabled) {
        if (!enabled) return true;
        Integer count = sErrorCountMap.get(featureKey);
        if (count != null && count >= ERROR_THRESHOLD) {
            setFeatureEnabled(featureKey, false);
            disableSubFeature(featureKey);
            ReaLog.antiDetection("auto_disable", "启动时自动禁用 " + featureKey, "错误计数=" + count);
            return true;
        }
        return false;
    }

    private static boolean recordErrorAndMaybeDisable(String featureKey, Throwable t) {
        String exName = t.getClass().getName();
        if (IGNORABLE_EXCEPTIONS.contains(exName)) {
            ReaLog.antiDetection("warn", featureKey + " 可忽略异常", exName);
            return false;
        }
        int newCount = sErrorCountMap.getOrDefault(featureKey, 0) + 1;
        sErrorCountMap.put(featureKey, newCount);
        if (newCount >= ERROR_THRESHOLD) {
            setFeatureEnabled(featureKey, false);
            disableSubFeature(featureKey);
            ReaLog.antiDetection("error", featureKey + " 达到阈值，自动禁用", "计数=" + newCount);
            return true;
        }
        ReaLog.antiDetection("warn", featureKey + " 发生异常，计数=" + newCount, t.getMessage());
        return false;
    }

    private static void installFeature(String featureKey, boolean enabled, Runnable installAction) {
        if (!enabled) return;
        if (checkAndDisableOnStartup(featureKey, enabled)) {
            return;
        }
        try {
            installAction.run();
            // ReaLog.antiDetection("hook", featureKey + " 安装成功", "");
        } catch (Throwable t) {
            boolean disabled = recordErrorAndMaybeDisable(featureKey, t);
            if (!disabled) {
                ReaLog.antiDetection("warn", featureKey + " 部分失败", t.getMessage());
            }
        }
    }

    // ========== LSPosed初始化入口 ==========
    public static void init(final XC_LoadPackage.LoadPackageParam lpparam, HookInit hookInstance) {
        sHookInstance = hookInstance;
        sErrorCountMap.clear();

        Map<String, Boolean> detail = HookInit.antiDetectionDetailMap.getOrDefault(
            sHookInstance.getCurrentTargetApp(), new HashMap<String, Boolean>());
        enableClass = detail.getOrDefault("class", true);
        enableFile = detail.getOrDefault("file", true);
        enablePm = detail.getOrDefault("pm", true);
        enableProc = detail.getOrDefault("proc", true);
        enableCmd = detail.getOrDefault("cmd", true);
        enableStacktrace = detail.getOrDefault("stacktrace", true);
        enableRoot = detail.getOrDefault("root", true);
        enableAdb = detail.getOrDefault("adb", true);
        enableDev = detail.getOrDefault("dev", true);

        ReaLog.antiDetection("status", "痕迹检测开关: class=" + enableClass + ", file=" + enableFile
                             + ", pm=" + enablePm + ", proc=" + enableProc + ", cmd=" + enableCmd
                             + ", stacktrace=" + enableStacktrace + ", root=" + enableRoot
                             + ", adb=" + enableAdb + ", dev=" + enableDev, "");

        if (!enableClass && !enableFile && !enablePm && !enableProc &&
            !enableCmd && !enableStacktrace && !enableRoot && !enableAdb && !enableDev) {
            log("全部痕迹检测功能已关闭");
            return;
        }
        if (isEnabled) return;
        isEnabled = true;

        installFeature("class", enableClass, new Runnable() {
                public void run() {
                    try {
                        hookClassHide(lpparam.classLoader);
                    } catch (Throwable t) {
                        throw new RuntimeException(t);
                    }
                }
            });
        installFeature("file", enableFile, new Runnable() {
                public void run() {
                    try {
                        hookFileHide(lpparam.classLoader);
                    } catch (Throwable t) {
                        throw new RuntimeException(t);
                    }
                }
            });
        installFeature("pm", enablePm, new Runnable() {
                public void run() {
                    try {
                        hookPmHide(lpparam.classLoader);
                    } catch (Throwable t) {
                        throw new RuntimeException(t);
                    }
                }
            });
        installFeature("proc", enableProc, new Runnable() {
                public void run() {
                    try {
                        hookProcHide(lpparam.classLoader);
                    } catch (Throwable t) {
                        throw new RuntimeException(t);
                    }
                }
            });
        installFeature("cmd", enableCmd, new Runnable() {
                public void run() {
                    try {
                        hookCmdHide(lpparam.classLoader);
                    } catch (Throwable t) {
                        throw new RuntimeException(t);
                    }
                }
            });
        installFeature("stacktrace", enableStacktrace, new Runnable() {
                public void run() {
                    try {
                        hookStacktraceHide(lpparam.classLoader);
                    } catch (Throwable t) {
                        throw new RuntimeException(t);
                    }
                }
            });
        installFeature("root", enableRoot, new Runnable() {
                public void run() {
                    try {
                        hookRootHide(lpparam.classLoader);
                    } catch (Throwable t) {
                        throw new RuntimeException(t);
                    }
                }
            });
        installFeature("adb", enableAdb, new Runnable() {
                public void run() {
                    try {
                        hookAdbHide(lpparam.classLoader);
                    } catch (Throwable t) {
                        throw new RuntimeException(t);
                    }
                }
            });
        installFeature("dev", enableDev, new Runnable() {
                public void run() {
                    try {
                        hookDevHide(lpparam.classLoader);
                    } catch (Throwable t) {
                        throw new RuntimeException(t);
                    }
                }
            });

        try {
            hookSystemProperties(lpparam.classLoader);
        } catch (Throwable ignored) {
        }
        if (!sDisabledFeatures.isEmpty()) showDisabledToast();
    }

// 内嵌模式初始化入口
    public static void initForEmbed(final ClassLoader classLoader, String packageName, HookInit hookInstance) {
        sHookInstance = hookInstance;
        sErrorCountMap.clear();

        // ===== 1. 先从原有配置加载（保持兼容） =====
        Map<String, Boolean> detail = HookInit.antiDetectionDetailMap.getOrDefault(
            sHookInstance.getCurrentTargetApp(), new HashMap<String, Boolean>());
        enableClass = detail.getOrDefault("class", true);
        enableFile = detail.getOrDefault("file", true);
        enablePm = detail.getOrDefault("pm", true);
        enableProc = detail.getOrDefault("proc", true);
        enableCmd = detail.getOrDefault("cmd", true);
        enableStacktrace = detail.getOrDefault("stacktrace", true);
        enableRoot = detail.getOrDefault("root", true);
        enableAdb = detail.getOrDefault("adb", true);
        enableDev = detail.getOrDefault("dev", true);

        // ===== 2. 【新增】调试模式强制覆盖（优先级最高） =====
        if (DebugModeManager.isDebugModeActive()) {
            enableClass = DebugModeManager.isFeatureEnabled("class_hide");
            enableFile = DebugModeManager.isFeatureEnabled("file_hide");
            enablePm = DebugModeManager.isFeatureEnabled("pm_hide");
            enableProc = DebugModeManager.isFeatureEnabled("proc_hide");
            enableCmd = DebugModeManager.isFeatureEnabled("cmd_hide"); // 确保 DebugModeManager 中有此
            // key
            enableStacktrace = DebugModeManager.isFeatureEnabled("stacktrace_hide");
            enableRoot = DebugModeManager.isFeatureEnabled("root_hide");
            enableAdb = DebugModeManager.isFeatureEnabled("adb_hide");
            enableDev = DebugModeManager.isFeatureEnabled("dev_hide");
        }

        ReaLog.antiDetection("status", "痕迹检测开关: class=" + enableClass + ", file=" + enableFile
                             + ", pm=" + enablePm + ", proc=" + enableProc + ", cmd=" + enableCmd
                             + ", stacktrace=" + enableStacktrace + ", root=" + enableRoot
                             + ", adb=" + enableAdb + ", dev=" + enableDev, "");

        if (!enableClass && !enableFile && !enablePm && !enableProc &&
            !enableCmd && !enableStacktrace && !enableRoot && !enableAdb && !enableDev) {
            log("全部痕迹检测功能已关闭");
            return;
        }
        if (isEnabled) return;
        isEnabled = true;

        installFeature("class", enableClass, new Runnable() {
                public void run() {
                    try {
                        hookClassHide(classLoader);
                    } catch (Throwable t) {
                        throw new RuntimeException(t);
                    }
                }
            });
        installFeature("file", enableFile, new Runnable() {
                public void run() {
                    try {
                        hookFileHide(classLoader);
                    } catch (Throwable t) {
                        throw new RuntimeException(t);
                    }
                }
            });
        installFeature("pm", enablePm, new Runnable() {
                public void run() {
                    try {
                        hookPmHide(classLoader);
                    } catch (Throwable t) {
                        throw new RuntimeException(t);
                    }
                }
            });
        installFeature("proc", enableProc, new Runnable() {
                public void run() {
                    try {
                        hookProcHide(classLoader);
                    } catch (Throwable t) {
                        throw new RuntimeException(t);
                    }
                }
            });
        installFeature("cmd", enableCmd, new Runnable() {
                public void run() {
                    try {
                        hookCmdHide(classLoader);
                    } catch (Throwable t) {
                        throw new RuntimeException(t);
                    }
                }
            });
        installFeature("stacktrace", enableStacktrace, new Runnable() {
                public void run() {
                    try {
                        hookStacktraceHide(classLoader);
                    } catch (Throwable t) {
                        throw new RuntimeException(t);
                    }
                }
            });
        installFeature("root", enableRoot, new Runnable() {
                public void run() {
                    try {
                        hookRootHide(classLoader);
                    } catch (Throwable t) {
                        throw new RuntimeException(t);
                    }
                }
            });
        installFeature("adb", enableAdb, new Runnable() {
                public void run() {
                    try {
                        hookAdbHide(classLoader);
                    } catch (Throwable t) {
                        throw new RuntimeException(t);
                    }
                }
            });
        installFeature("dev", enableDev, new Runnable() {
                public void run() {
                    try {
                        hookDevHide(classLoader);
                    } catch (Throwable t) {
                        throw new RuntimeException(t);
                    }
                }
            });

        try {
            hookSystemProperties(classLoader);
        } catch (Throwable ignored) {
        }
        if (!sDisabledFeatures.isEmpty()) showDisabledToast();
    }

    private static void showDisabledToast() {
        try {
            Context ctx = sHookInstance.getApplicationContext();
            if (ctx == null) {
                Activity act = sHookInstance.getCurrentActivity();
                if (act != null && !act.isFinishing()) ctx = act;
            }
            final Context finalCtx = ctx;
            if (finalCtx != null) {
                StringBuilder sb = new StringBuilder("部分检测异常\n已自动禁用：\n");
                for (int i = 0; i < sDisabledFeatures.size(); i++) {
                    String key = sDisabledFeatures.get(i);
                    String friendly;
                    if ("class".equals(key)) friendly = "框架隐藏";
                    else if ("file".equals(key)) friendly = "文件隐藏";
                    else if ("pm".equals(key)) friendly = "服务隐藏";
                    else if ("proc".equals(key)) friendly = "信息隐藏";
                    else if ("cmd".equals(key)) friendly = "命令隐藏";
                    else if ("stacktrace".equals(key)) friendly = "堆栈隐藏";
                    else if ("root".equals(key)) friendly = "Root隐藏";
                    else if ("adb".equals(key)) friendly = "ADB调试隐藏";
                    else if ("dev".equals(key)) friendly = "开发者选项隐藏";
                    else friendly = key;
                    if (i > 0) sb.append("、");
                    sb.append(friendly);
                }
                final String msg = sb.toString();
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        public void run() {
                            ToastUtil.show(finalCtx, msg);
                        }
                    }, 1500);
            }
            sDisabledFeatures.clear();
        } catch (Throwable ignored) {
        }
    }

    // ============================================================
    // 以下为所有 Hook 方法的完整实现（全部展开，无省略）
    // 每个子功能内部回调均已增加错误计数
    // ============================================================

    private static void hookClassHide(ClassLoader classLoader) throws Throwable {
        ReaLog.antiDetection("hook", "安装 框架隐藏", "");
        XposedHelpers.findAndHookMethod("java.lang.Class", classLoader,
            "forName", String.class, boolean.class, ClassLoader.class,
            new XC_MethodHook() {
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (!enableClass) return;
                    try {
                        String className = (String) param.args[0];
                        if (isXposedClass(className)) {
                            ReaLog.antiDetection("class", "Class.forName 拦截", className);
                            param.setThrowable(new ClassNotFoundException("Class not found: " + className));
                        }
                    } catch (Throwable t) {
                        recordErrorAndMaybeDisable("class", t);
                    }
                }
            });

        XposedHelpers.findAndHookMethod("java.lang.ClassLoader", classLoader,
            "loadClass", String.class, boolean.class,
            new XC_MethodHook() {
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (!enableClass) return;
                    try {
                        String className = (String) param.args[0];
                        if (isXposedClass(className)) {
                            ReaLog.antiDetection("class", "loadClass 拦截", className);
                            param.setThrowable(new ClassNotFoundException("Class not found: " + className));
                        }
                    } catch (Throwable t) {
                        recordErrorAndMaybeDisable("class", t);
                    }
                }
            });

        XposedHelpers.findAndHookMethod("java.lang.Class", classLoader,
            "getDeclaredClasses", new XC_MethodHook() {
                protected void afterHookedMethod(MethodHookParam param) {
                    if (!enableClass) return;
                    try {
                        Class<?>[] classes = (Class<?>[]) param.getResult();
                        if (classes == null) return;
                        List<Class<?>> safe = new ArrayList<Class<?>>();
                        for (Class<?> c : classes) {
                            if (c != null && !isXposedClass(c.getName())) {
                                safe.add(c);
                            } else if (c != null) {
                                ReaLog.antiDetection("class", "getDeclaredClasses 过滤", c.getName());
                            }
                        }
                        param.setResult(safe.toArray(new Class<?>[0]));
                    } catch (Throwable t) {
                        recordErrorAndMaybeDisable("class", t);
                    }
                }
            });

        XposedHelpers.findAndHookMethod("java.lang.Class", classLoader,
            "getDeclaredMethods", new XC_MethodHook() {
                protected void afterHookedMethod(MethodHookParam param) {
                    if (!enableClass) return;
                    try {
                        Method[] methods = (Method[]) param.getResult();
                        if (methods == null) return;
                        List<Method> safe = new ArrayList<Method>();
                        for (Method m : methods) {
                            if (m != null && !isXposedClass(m.getDeclaringClass().getName())) {
                                safe.add(m);
                            } else if (m != null) {
                                ReaLog.antiDetection("class", "getDeclaredMethods 过滤", m.getDeclaringClass().getName());
                            }
                        }
                        param.setResult(safe.toArray(new Method[0]));
                    } catch (Throwable t) {
                        recordErrorAndMaybeDisable("class", t);
                    }
                }
            });

        XposedHelpers.findAndHookMethod("java.lang.Class", classLoader,
            "getDeclaredFields", new XC_MethodHook() {
                protected void afterHookedMethod(MethodHookParam param) {
                    if (!enableClass) return;
                    try {
                        Field[] fields = (Field[]) param.getResult();
                        if (fields == null) return;
                        List<Field> safe = new ArrayList<Field>();
                        for (Field f : fields) {
                            if (f != null && !isXposedClass(f.getDeclaringClass().getName())) {
                                safe.add(f);
                            } else if (f != null) {
                                ReaLog.antiDetection("class", "getDeclaredFields 过滤", f.getDeclaringClass().getName());
                            }
                        }
                        param.setResult(safe.toArray(new Field[0]));
                    } catch (Throwable t) {
                        recordErrorAndMaybeDisable("class", t);
                    }
                }
            });

        XposedHelpers.findAndHookMethod("java.lang.Class", classLoader,
            "getClasses", new XC_MethodHook() {
                protected void afterHookedMethod(MethodHookParam param) {
                    if (!enableClass) return;
                    try {
                        Class<?>[] classes = (Class<?>[]) param.getResult();
                        if (classes == null) return;
                        List<Class<?>> safe = new ArrayList<Class<?>>();
                        for (Class<?> c : classes) {
                            if (c != null && !isXposedClass(c.getName())) {
                                safe.add(c);
                            } else if (c != null) {
                                ReaLog.antiDetection("class", "getClasses 过滤", c.getName());
                            }
                        }
                        param.setResult(safe.toArray(new Class<?>[0]));
                    } catch (Throwable t) {
                        recordErrorAndMaybeDisable("class", t);
                    }
                }
            });

        // ========== 新增：System.getProperty / System.getenv 拦截 ==========
        try {
            XposedHelpers.findAndHookMethod(System.class, "getProperty", String.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        if (!enableClass) return;
                        try {
                            String key = (String) param.args[0];
                            if (key != null && key.toLowerCase().contains("xposed")) {
                                param.setResult(null);
                                ReaLog.antiDetection("class", "System.getProperty 拦截", key);
                            }
                        } catch (Throwable t) {
                            recordErrorAndMaybeDisable("class", t);
                        }
                    }
                });

            XposedHelpers.findAndHookMethod(System.class, "getenv", String.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        if (!enableClass) return;
                        try {
                            String key = (String) param.args[0];
                            if (key != null && key.toLowerCase().contains("xposed")) {
                                param.setResult(null);
                                ReaLog.antiDetection("class", "System.getenv 拦截", key);
                            }
                        } catch (Throwable t) {
                            recordErrorAndMaybeDisable("class", t);
                        }
                    }
                });
        } catch (Throwable t) {
            ReaLog.antiDetection("warn", "System属性拦截安装失败", t.getMessage());
        }
    }

    /*
     private static boolean isXposedClass(String className) {
     if (className == null) return false;
     if (className.startsWith("de.robv.android.xposed.") ||
     className.startsWith("org.lsposed.lspd.") ||
     className.startsWith("io.github.lsposed.") ||
     className.equals("de.robv.android.xposed.XposedBridge") ||
     className.equals("de.robv.android.xposed.XposedHelpers") ||
     className.equals("de.robv.android.xposed.XC_MethodHook") ||
     className.equals("de.robv.android.xposed.XposedInit") ||
     className.equals("de.robv.android.xposed.XposedMods") ||
     className.equals("de.robv.android.xposed.callbacks.XC_LoadPackage") ||
     className.contains(".lsposed.") ||
     className.contains(".edxposed.")) {
     ReaLog.antiDetection("class", "匹配敏感类", className);
     return true;
     }
     return false;
     }
     */
    private static boolean isXposedClass(String className) {
        if (className == null) return false;
        // 精确匹配核心危险类，放行 XposedHelpers
        if (className.equals("de.robv.android.xposed.XposedBridge") ||
            className.equals("de.robv.android.xposed.XC_MethodHook") ||
            className.equals("de.robv.android.xposed.XposedInit") ||
            className.equals("de.robv.android.xposed.XposedMods") ||
            className.equals("de.robv.android.xposed.callbacks.XC_LoadPackage") ||
            className.startsWith("org.lsposed.lspd.") ||
            className.startsWith("io.github.lsposed.") ||
            className.contains(".lsposed.") ||
            className.contains(".edxposed.")) {
            ReaLog.antiDetection("class", "匹配敏感类", className);
            return true;
        }
        return false;
    }

    private static void hookFileHide(ClassLoader classLoader) throws Throwable {
        ReaLog.antiDetection("hook", "安装 文件隐藏", "");
        Class<?> fileClass = XposedHelpers.findClass("java.io.File", classLoader);
        XposedBridge.hookAllConstructors(fileClass, new XC_MethodHook() {
                protected void afterHookedMethod(MethodHookParam param) {
                    try {
                        File file = (File) param.thisObject;
                        if (file != null && isXposedPath(file.getAbsolutePath())) {
                            XposedHelpers.setAdditionalInstanceField(file, "anti_detect_xposed_file", true);
                        }
                    } catch (Throwable t) {
                        recordErrorAndMaybeDisable("file", t);
                    }
                }
            });

        String[] methods = {"exists", "listFiles", "list", "canRead", "canWrite",
            "isDirectory", "isFile", "length", "lastModified"
        };
        for (final String methodName : methods) {
            XposedBridge.hookAllMethods(fileClass, methodName, new XC_MethodHook() {
                    protected void afterHookedMethod(MethodHookParam param) {
                        if (!enableFile) return;
                        try {
                            File file = (File) param.thisObject;
                            if (file == null) return;
                            Boolean marked = (Boolean) XposedHelpers.getAdditionalInstanceField(file, "anti_detect_xposed_file");
                            if (marked != null && marked) {
                                setFileResult(param, methodName, file.getAbsolutePath());
                                return;
                            }
                            String path = file.getAbsolutePath();
                            if (isXposedPath(path)) {
                                setFileResult(param, methodName, path);
                            }
                        } catch (Throwable t) {
                            recordErrorAndMaybeDisable("file", t);
                        }
                    }

                    private void setFileResult(MethodHookParam param, String methodName, String path) {
                        ReaLog.antiDetection("file", "File." + methodName + " 隐藏", path);
                        if (methodName.equals("exists")) param.setResult(false);
                        else if (methodName.equals("listFiles")) param.setResult(new File[0]);
                        else if (methodName.equals("list")) param.setResult(new String[0]);
                        else if (methodName.equals("canRead") || methodName.equals("canWrite") ||
                                 methodName.equals("isDirectory") || methodName.equals("isFile")) {
                            param.setResult(false);
                        } else if (methodName.equals("length") || methodName.equals("lastModified")) {
                            param.setResult(0L);
                        }
                    }
                });
        }

        // ========== 新增：AssetManager.open 拦截（assets/xposed*） ==========
        try {
            XposedHelpers.findAndHookMethod(android.content.res.AssetManager.class, "open", String.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        if (!enableFile) return;
                        try {
                            String fileName = (String) param.args[0];
                            if (fileName != null && fileName.toLowerCase().contains("xposed")) {
                                param.setThrowable(new java.io.FileNotFoundException(fileName));
                                ReaLog.antiDetection("file", "AssetManager.open 拦截", fileName);
                            }
                        } catch (Throwable t) {
                            recordErrorAndMaybeDisable("file", t);
                        }
                    }
                });
        } catch (Throwable t) {
            ReaLog.antiDetection("warn", "AssetManager.open 拦截安装失败", t.getMessage());
        }
    }

    private static boolean isXposedPath(String path) {
        if (path == null) return false;
        String lower = path.toLowerCase();
        if (lower.startsWith("/data/data/de.robv.android.xposed") ||
            lower.startsWith("/data/xposed") ||
            lower.startsWith("/system/xposed") ||
            lower.startsWith("/system/bin/xposed") ||
            lower.startsWith("/system/lib64/xposed") ||
            lower.startsWith("/system/lib/xposed") ||
            lower.startsWith("/data/data/xposed") ||
            lower.contains("libxposed") ||
            lower.contains("xposed.prop") ||
            lower.contains("xposed.log") ||
            lower.contains("/xposed_")) {
            ReaLog.antiDetection("file", "匹配 Xposed 路径", path);
            return true;
        }
        if (lower.contains("/data/adb/modules/zygisk_lsposed") ||
            lower.contains("/data/adb/lspd") ||
            lower.contains("/data/adb/lsposed") ||
            lower.contains("/data/data/org.lsposed.manager") ||
            lower.contains("/data/data/io.github.lsposed.manager") ||
            lower.contains("liblsposed.so") ||
            lower.contains("/data/adb/lspatch") ||
            lower.contains("/data/adb/modules/lspatch") ||
            lower.contains("liblspatch.so") ||
            lower.contains("lspatch.prop") ||
            lower.contains("/data/adb/riru") ||
            lower.contains("/data/adb/modules/riru")) {
            ReaLog.antiDetection("file", "匹配 LSPosed/Riru 路径", path);
            return true;
        }
        return false;
    }

    private static void hookPmHide(ClassLoader classLoader) throws Throwable {
        ReaLog.antiDetection("hook", "安装 服务隐藏", "");
        XposedHelpers.findAndHookMethod("android.app.ApplicationPackageManager", classLoader,
            "getPackageInfo", String.class, int.class,
            new XC_MethodHook() {
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (!enablePm) return;
                    try {
                        String pkg = (String) param.args[0];
                        if (isXposedPackage(pkg)) {
                            ReaLog.antiDetection("pm", "getPackageInfo 过滤", pkg);
                            param.setResult(null);
                        }
                    } catch (Throwable t) {
                        recordErrorAndMaybeDisable("pm", t);
                    }
                }
            });

        XposedHelpers.findAndHookMethod("android.app.ApplicationPackageManager", classLoader,
            "getInstalledPackages", int.class,
            new XC_MethodHook() {
                protected void afterHookedMethod(MethodHookParam param) {
                    if (!enablePm) return;
                    try {
                        List<?> list = (List<?>) param.getResult();
                        if (list == null) return;
                        Iterator<?> it = list.iterator();
                        while (it.hasNext()) {
                            Object info = it.next();
                            String pkg = getPackageNameFromInfo(info);
                            if (isXposedPackage(pkg)) {
                                ReaLog.antiDetection("pm", "getInstalledPackages 过滤", pkg);
                                it.remove();
                            }
                        }
                    } catch (Throwable t) {
                        recordErrorAndMaybeDisable("pm", t);
                    }
                }
            });

        XposedHelpers.findAndHookMethod("android.app.ApplicationPackageManager", classLoader,
            "getInstalledApplications", int.class,
            new XC_MethodHook() {
                protected void afterHookedMethod(MethodHookParam param) {
                    if (!enablePm) return;
                    try {
                        List<?> list = (List<?>) param.getResult();
                        if (list == null) return;
                        Iterator<?> it = list.iterator();
                        while (it.hasNext()) {
                            Object info = it.next();
                            String pkg = getPackageNameFromInfo(info);
                            if (isXposedPackage(pkg)) {
                                ReaLog.antiDetection("pm", "getInstalledApplications 过滤", pkg);
                                it.remove();
                            }
                        }
                    } catch (Throwable t) {
                        recordErrorAndMaybeDisable("pm", t);
                    }
                }
            });

        XposedHelpers.findAndHookMethod("android.app.ActivityManager", classLoader,
            "getRunningAppProcesses", new XC_MethodHook() {
                protected void afterHookedMethod(MethodHookParam param) {
                    if (!enablePm) return;
                    try {
                        List<ActivityManager.RunningAppProcessInfo> procs = (List<
                            ActivityManager.RunningAppProcessInfo>) param.getResult();
                        if (procs == null) return;
                        Iterator<ActivityManager.RunningAppProcessInfo> it = procs.iterator();
                        while (it.hasNext()) {
                            ActivityManager.RunningAppProcessInfo info = it.next();
                            if (info != null && info.processName != null) {
                                String name = info.processName;
                                if (isXposedProcess(name)) {
                                    ReaLog.antiDetection("pm", "getRunningAppProcesses 过滤", name);
                                    it.remove();
                                }
                            }
                        }
                    } catch (Throwable t) {
                        recordErrorAndMaybeDisable("pm", t);
                    }
                }
            });

        XposedHelpers.findAndHookMethod("android.app.ActivityManager", classLoader,
            "getRunningServices", int.class,
            new XC_MethodHook() {
                protected void afterHookedMethod(MethodHookParam param) {
                    if (!enablePm) return;
                    try {
                        List<ActivityManager.RunningServiceInfo> services = (List<
                            ActivityManager.RunningServiceInfo>) param.getResult();
                        if (services == null) return;
                        Iterator<ActivityManager.RunningServiceInfo> it = services.iterator();
                        while (it.hasNext()) {
                            ActivityManager.RunningServiceInfo info = it.next();
                            if (info != null && info.service != null) {
                                String pkg = info.service.getPackageName();
                                if (isXposedPackage(pkg)) {
                                    ReaLog.antiDetection("pm", "getRunningServices 过滤", pkg);
                                    it.remove();
                                }
                            }
                        }
                    } catch (Throwable t) {
                        recordErrorAndMaybeDisable("pm", t);
                    }
                }
            });
    }

    /*
     private static boolean isXposedPackage(String pkg) {
     if (pkg == null) return false;
     if (XPACKAGES_EXACT.contains(pkg)) {
     ReaLog.antiDetection("pm", "匹配 Xposed 包名(精确)", pkg);
     return true;
     }
     String lower = pkg.toLowerCase();
     if (lower.contains("xposed") || lower.contains("edxposed") || lower.contains("lsposed")) {
     ReaLog.antiDetection("pm", "匹配 Xposed 包名(关键词)", pkg);
     return true;
     }
     return false;
     }

     private static boolean isXposedProcess(String name) {
     if (name == null) return false;
     String lower = name.toLowerCase();
     if (lower.contains("xposed") || lower.contains("edxposed") ||
     lower.contains("lsposed") || lower.contains("lspd") ||
     lower.contains("zygisk_lsposed") || lower.contains(MODULE_PACKAGE)) {
     ReaLog.antiDetection("pm", "匹配 Xposed 进程", name);
     return true;
     }
     return false;
     }*/

// ========== 判断是否为敏感包名 ==========
    private static boolean isXposedPackage(String pkg) {
        if (pkg == null) return false;
        // 1. 精确匹配
        if (XPACKAGES_EXACT.contains(pkg)) {
            ReaLog.antiDetection("pm", "匹配敏感包名(精确)", pkg);
            return true;
        }
        // 2. 模糊匹配（遍历统一关键词数组）
        String lower = pkg.toLowerCase();
        for (String keyword : SENSITIVE_KEYWORDS) {
            if (lower.contains(keyword)) {
                ReaLog.antiDetection("pm", "匹配敏感包名(关键词: " + keyword + ")", pkg);
                return true;
            }
        }
        return false;
    }

// ========== 判断是否为敏感进程 ==========
    private static boolean isXposedProcess(String name) {
        if (name == null) return false;
        String lower = name.toLowerCase();
        // 遍历统一关键词数组
        for (String keyword : SENSITIVE_KEYWORDS) {
            if (lower.contains(keyword)) {
                ReaLog.antiDetection("pm", "匹配敏感进程(关键词: " + keyword + ")", name);
                return true;
            }
        }
        // 特殊进程名（自身模块）
        if (lower.contains(MODULE_PACKAGE)) {
            ReaLog.antiDetection("pm", "匹配自身模块进程", name);
            return true;
        }
        return false;
    }

    private static String getPackageNameFromInfo(Object info) {
        try {
            return (String) XposedHelpers.getObjectField(info, "packageName");
        } catch (Throwable e) {
            return null;
        }
    }

    private static void hookProcHide(ClassLoader classLoader) throws Throwable {
        ReaLog.antiDetection("hook", "安装 信息隐藏", "");
        Class<?> fisClass = XposedHelpers.findClass("java.io.FileInputStream", classLoader);
        XposedBridge.hookAllConstructors(fisClass, new XC_MethodHook() {
                protected void afterHookedMethod(MethodHookParam param) {
                    try {
                        if (param.args != null && param.args.length == 1 && param.args[0]
                            instanceof String) {
                            String path = (String) param.args[0];
                            if (isSensitiveProcPath(path)) {
                                XposedHelpers.setAdditionalInstanceField(param.thisObject, "anti_detect_proc", true);
                            }
                        }
                    } catch (Throwable t) {
                        recordErrorAndMaybeDisable("proc", t);
                    }
                }
            });

        XposedHelpers.findAndHookMethod(fisClass, "read", new XC_MethodHook() {
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (!enableProc) return;
                    try {
                        if (isProcStream(param.thisObject)) {
                            ReaLog.antiDetection("proc", "FileInputStream.read 拦截", "敏感路径");
                            param.setResult(-1);
                        }
                    } catch (Throwable t) {
                        recordErrorAndMaybeDisable("proc", t);
                    }
                }
            });
        XposedHelpers.findAndHookMethod(fisClass, "read", byte[].class, new XC_MethodHook() {
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (!enableProc) return;
                    try {
                        if (isProcStream(param.thisObject)) {
                            ReaLog.antiDetection("proc", "FileInputStream.read(byte[]) 拦截", "敏感路径");
                            param.setResult(-1);
                        }
                    } catch (Throwable t) {
                        recordErrorAndMaybeDisable("proc", t);
                    }
                }
            });
        XposedHelpers.findAndHookMethod(fisClass, "read", byte[].class, int.class, int.class, new XC_MethodHook() {
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (!enableProc) return;
                    try {
                        if (isProcStream(param.thisObject)) {
                            ReaLog.antiDetection("proc", "FileInputStream.read(byte[],int,int) 拦截", "敏感路径");
                            param.setResult(-1);
                        }
                    } catch (Throwable t) {
                        recordErrorAndMaybeDisable("proc", t);
                    }
                }
            });

        XposedHelpers.findAndHookMethod("java.io.BufferedReader", classLoader,
            "readLine", new XC_MethodHook() {
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (!enableProc) return;
                    try {
                        Object in = XposedHelpers.getObjectField(param.thisObject, "in");
                        if (in != null && isProcStream(in)) {
                            ReaLog.antiDetection("proc", "BufferedReader.readLine 拦截", "敏感路径");
                            param.setResult("");
                        }
                    } catch (Throwable t) {
                        recordErrorAndMaybeDisable("proc", t);
                    }
                }
            });
    }

    private static boolean isSensitiveProcPath(String path) {
        if (path == null) return false;
        String lower = path.toLowerCase();
        if (lower.contains("/proc/self/maps") ||
            lower.contains("/proc/self/mountinfo") ||
            lower.contains("/proc/self/status") ||
            lower.contains("/proc/self/cmdline") ||
            lower.contains("/proc/self/environ") ||
            lower.contains("/proc/self/limits") ||
            lower.contains("/proc/net/unix") ||
            lower.contains("/proc/net") ||
            lower.contains("/sys/class/net")) {
            ReaLog.antiDetection("proc", "敏感 proc 路径", path);
            return true;
        }
        return false;
    }

    private static boolean isProcStream(Object stream) {
        if (stream == null) return false;
        Boolean flag = (Boolean) XposedHelpers.getAdditionalInstanceField(stream, "anti_detect_proc");
        return flag != null && flag;
    }

    private static void hookCmdHide(ClassLoader classLoader) throws Throwable {
        ReaLog.antiDetection("hook", "安装 命令隐藏", "");
        XposedHelpers.findAndHookMethod("java.lang.Runtime", classLoader,
            "exec", String.class, new XC_MethodHook() {
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (!enableCmd) return;
                    try {
                        String cmd = (String) param.args[0];
                        if (isXposedCommand(cmd)) {
                            ReaLog.antiDetection("cmd", "Runtime.exec 拦截命令", cmd);
                            param.setResult(createFakeProcess());
                        }
                    } catch (Throwable t) {
                        recordErrorAndMaybeDisable("cmd", t);
                    }
                }
            });
        XposedHelpers.findAndHookMethod("java.lang.Runtime", classLoader,
            "exec", String[].class, new XC_MethodHook() {
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (!enableCmd) return;
                    try {
                        String[] cmds = (String[]) param.args[0];
                        if (cmds != null && cmds.length > 0) {
                            String full = String.join(" ", cmds);
                            if (isXposedCommand(full)) {
                                ReaLog.antiDetection("cmd", "Runtime.exec(String[]) 拦截命令", full);
                                param.setResult(createFakeProcess());
                            }
                        }
                    } catch (Throwable t) {
                        recordErrorAndMaybeDisable("cmd", t);
                    }
                }
            });
        XposedHelpers.findAndHookMethod("java.lang.ProcessBuilder", classLoader,
            "start", new XC_MethodHook() {
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (!enableCmd) return;
                    try {
                        ProcessBuilder pb = (ProcessBuilder) param.thisObject;
                        List<String> command = pb.command();
                        if (command != null && !command.isEmpty()) {
                            String full = String.join(" ", command);
                            if (isXposedCommand(full)) {
                                ReaLog.antiDetection("cmd", "ProcessBuilder.start 拦截命令", full);
                                param.setResult(createFakeProcess());
                            }
                        }
                    } catch (Throwable t) {
                        recordErrorAndMaybeDisable("cmd", t);
                    }
                }
            });
    }

    private static boolean isXposedCommand(String cmd) {
        if (cmd == null) return false;
        String lower = cmd.toLowerCase();
        if (lower.contains("xposed") || lower.contains("xposed.prop") ||
            lower.contains("xposed.log") || lower.contains("getprop xposed") ||
            lower.contains("ls /data/xposed") || lower.contains("cat /data/xposed") ||
            lower.contains("ps | grep xposed") || lower.contains("pm list packages | grep xposed") ||
            lower.contains("lsposed") || lower.contains("lspd") ||
            (lower.contains("busybox") && lower.contains("xposed"))) {
            ReaLog.antiDetection("cmd", "匹配 Xposed 命令", cmd);
            return true;
        }
        return false;
    }

    private static Process createFakeProcess() {
        return new Process() {
            public OutputStream getOutputStream() {
                return new OutputStream() {
                    public void write(int b) {}
                };
            }

            public InputStream getInputStream() {
                return new java.io.ByteArrayInputStream(new byte[0]);
            }

            public InputStream getErrorStream() {
                return new java.io.ByteArrayInputStream(new byte[0]);
            }

            public int waitFor() throws InterruptedException {
                return 0;
            }

            public int exitValue() {
                return 0;
            }

            public void destroy() {}
        };
    }

    private static void hookStacktraceHide(ClassLoader classLoader) throws Throwable {
        ReaLog.antiDetection("hook", "安装 堆栈隐藏", "");
        XposedHelpers.findAndHookMethod("java.lang.Throwable", classLoader,
            "getStackTrace", new XC_MethodHook() {
                protected void afterHookedMethod(MethodHookParam param) {
                    if (!enableStacktrace) return;
                    try {
                        StackTraceElement[] stack = (StackTraceElement[]) param.getResult();
                        if (stack == null) return;
                        List<StackTraceElement> filtered = new ArrayList<StackTraceElement>();
                        for (StackTraceElement e : stack) {
                            if (!isSensitiveClassInStack(e.getClassName())) {
                                filtered.add(e);
                            } else {
                                ReaLog.antiDetection("stacktrace", "Throwable.getStackTrace 移除", e.getClassName());
                            }
                        }
                        param.setResult(filtered.toArray(new StackTraceElement[0]));
                    } catch (Throwable t) {
                        recordErrorAndMaybeDisable("stacktrace", t);
                    }
                }
            });

        try {
            XposedHelpers.findAndHookMethod("java.lang.Throwable", classLoader,
                "nativeGetStackTrace", Object.class, new XC_MethodHook() {
                    protected void afterHookedMethod(MethodHookParam param) {
                        if (!enableStacktrace) return;
                        try {
                            StackTraceElement[] stack = (StackTraceElement[]) param.getResult();
                            if (stack == null) return;
                            List<StackTraceElement> filtered = new ArrayList<
                                StackTraceElement>();
                            for (StackTraceElement e : stack) {
                                if (!isSensitiveClassInStack(e.getClassName())) {
                                    filtered.add(e);
                                } else {
                                    ReaLog.antiDetection("stacktrace", "nativeGetStackTrace 移除", e.getClassName());
                                }
                            }
                            param.setResult(filtered.toArray(new StackTraceElement[0]));
                        } catch (Throwable t) {
                            recordErrorAndMaybeDisable("stacktrace", t);
                        }
                    }
                });
        } catch (Throwable ignored) {
        }

        XposedHelpers.findAndHookMethod("java.lang.Thread", classLoader,
            "getAllStackTraces", new XC_MethodHook() {
                protected void afterHookedMethod(MethodHookParam param) {
                    if (!enableStacktrace) return;
                    try {
                        Map<Thread, StackTraceElement[]> traces = (Map<
                            Thread, StackTraceElement[]>) param.getResult();
                        if (traces == null) return;
                        for (Map.Entry<Thread, StackTraceElement[]> entry : traces.entrySet()) {
                            List<StackTraceElement> filtered = new ArrayList<
                                StackTraceElement>();
                            for (StackTraceElement e : entry.getValue()) {
                                if (!isSensitiveClassInStack(e.getClassName())) {
                                    filtered.add(e);
                                } else {
                                    ReaLog.antiDetection("stacktrace", "Thread.getAllStackTraces 移除", e.getClassName());
                                }
                            }
                            entry.setValue(filtered.toArray(new StackTraceElement[0]));
                        }
                    } catch (Throwable t) {
                        recordErrorAndMaybeDisable("stacktrace", t);
                    }
                }
            });

        XposedHelpers.findAndHookMethod("java.lang.StackTraceElement", classLoader,
            "getClassName", new XC_MethodHook() {
                protected void afterHookedMethod(MethodHookParam param) {
                    if (!enableStacktrace) return;
                    try {
                        String cls = (String) param.getResult();
                        if (isSensitiveClassInStack(cls)) {
                            ReaLog.antiDetection("stacktrace", "StackTraceElement.getClassName 替换", cls + " -> android.os.Handler");
                            param.setResult("android.os.Handler");
                        }
                    } catch (Throwable t) {
                        recordErrorAndMaybeDisable("stacktrace", t);
                    }
                }
            });

        try {
            Class<
                ?> vmStack = XposedHelpers.findClassIfExists("dalvik.system.VMStack", classLoader);
            if (vmStack != null) {
                XposedHelpers.findAndHookMethod(vmStack, "getThreadStackTrace", Thread.class,
                    new XC_MethodHook() {
                        protected void afterHookedMethod(MethodHookParam param) {
                            if (!enableStacktrace) return;
                            try {
                                StackTraceElement[] stack = (StackTraceElement[]) param.getResult();
                                if (stack == null) return;
                                List<StackTraceElement> filtered = new ArrayList<
                                    StackTraceElement>();
                                for (StackTraceElement e : stack) {
                                    if (!isSensitiveClassInStack(e.getClassName())) {
                                        filtered.add(e);
                                    } else {
                                        ReaLog.antiDetection("stacktrace", "VMStack.getThreadStackTrace 移除", e.getClassName());
                                    }
                                }
                                param.setResult(filtered.toArray(new StackTraceElement[0]));
                            } catch (Throwable t) {
                                recordErrorAndMaybeDisable("stacktrace", t);
                            }
                        }
                    });
            }
        } catch (Throwable ignored) {
        }
    }

    private static boolean isSensitiveClassInStack(String className) {
        if (className == null) return false;
        String lower = className.toLowerCase();
        if (lower.contains("xposed") || lower.contains("lsposed") ||
            lower.contains("edxp") || lower.contains("zygisk") ||
            lower.contains("riru") || lower.contains("magisk") ||
            lower.contains("kernelsu") || lower.contains("ksu") ||
            lower.contains("de.robv.android.xposed") ||
            lower.contains("io.github.libxposed") ||
            lower.contains("supersu") || lower.contains("superuser") ||
            lower.contains("kingroot") || lower.contains("substrate") ||
            lower.contains("saurik")) {
            ReaLog.antiDetection("stacktrace", "敏感类匹配", className);
            return true;
        }
        return false;
    }

    private static void hookRootHide(ClassLoader classLoader) throws Throwable {
        ReaLog.antiDetection("hook", "安装 Root 隐藏", "");
        Class<?> fileClass = XposedHelpers.findClass("java.io.File", classLoader);
        XposedBridge.hookAllConstructors(fileClass, new XC_MethodHook() {
                protected void afterHookedMethod(MethodHookParam param) {
                    try {
                        File file = (File) param.thisObject;
                        if (file != null && isRootPath(file.getAbsolutePath())) {
                            XposedHelpers.setAdditionalInstanceField(file, "anti_detect_root_file", true);
                        }
                    } catch (Throwable t) {
                        recordErrorAndMaybeDisable("root", t);
                    }
                }
            });

        String[] methods = {"exists", "listFiles", "list", "canRead", "canWrite",
            "isDirectory", "isFile", "length", "lastModified"
        };
        for (final String methodName : methods) {
            XposedBridge.hookAllMethods(fileClass, methodName, new XC_MethodHook() {
                    protected void afterHookedMethod(MethodHookParam param) {
                        if (!enableRoot) return;
                        try {
                            File file = (File) param.thisObject;
                            if (file == null) return;
                            Boolean marked = (Boolean) XposedHelpers.getAdditionalInstanceField(file, "anti_detect_root_file");
                            if (marked != null && marked) {
                                setRootFileResult(param, methodName, file.getAbsolutePath());
                                return;
                            }
                            String path = file.getAbsolutePath();
                            if (isRootPath(path)) {
                                setRootFileResult(param, methodName, path);
                            }
                        } catch (Throwable t) {
                            recordErrorAndMaybeDisable("root", t);
                        }
                    }

                    private void setRootFileResult(MethodHookParam param, String methodName, String path) {
                        ReaLog.antiDetection("root", "File." + methodName + " 隐藏", path);
                        if (methodName.equals("exists")) param.setResult(false);
                        else if (methodName.equals("listFiles")) param.setResult(new File[0]);
                        else if (methodName.equals("list")) param.setResult(new String[0]);
                        else if (methodName.equals("canRead") || methodName.equals("canWrite") ||
                                 methodName.equals("isDirectory") || methodName.equals("isFile")) {
                            param.setResult(false);
                        } else if (methodName.equals("length") || methodName.equals("lastModified")) {
                            param.setResult(0L);
                        }
                    }
                });
        }

        // 包名隐藏
        XposedHelpers.findAndHookMethod("android.app.ApplicationPackageManager", classLoader,
            "getPackageInfo", String.class, int.class,
            new XC_MethodHook() {
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (!enableRoot) return;
                    try {
                        String pkg = (String) param.args[0];
                        if (isRootPackage(pkg)) {
                            ReaLog.antiDetection("root", "getPackageInfo 过滤", pkg);
                            param.setResult(null);
                        }
                    } catch (Throwable t) {
                        recordErrorAndMaybeDisable("root", t);
                    }
                }
            });
        XposedHelpers.findAndHookMethod("android.app.ApplicationPackageManager", classLoader,
            "getInstalledPackages", int.class,
            new XC_MethodHook() {
                protected void afterHookedMethod(MethodHookParam param) {
                    if (!enableRoot) return;
                    try {
                        List<?> list = (List<?>) param.getResult();
                        if (list == null) return;
                        Iterator<?> it = list.iterator();
                        while (it.hasNext()) {
                            Object info = it.next();
                            String pkg = getPackageNameFromInfo(info);
                            if (isRootPackage(pkg)) {
                                ReaLog.antiDetection("root", "getInstalledPackages 过滤", pkg);
                                it.remove();
                            }
                        }
                    } catch (Throwable t) {
                        recordErrorAndMaybeDisable("root", t);
                    }
                }
            });
        XposedHelpers.findAndHookMethod("android.app.ApplicationPackageManager", classLoader,
            "getInstalledApplications", int.class,
            new XC_MethodHook() {
                protected void afterHookedMethod(MethodHookParam param) {
                    if (!enableRoot) return;
                    try {
                        List<?> list = (List<?>) param.getResult();
                        if (list == null) return;
                        Iterator<?> it = list.iterator();
                        while (it.hasNext()) {
                            Object info = it.next();
                            String pkg = getPackageNameFromInfo(info);
                            if (isRootPackage(pkg)) {
                                ReaLog.antiDetection("root", "getInstalledApplications 过滤", pkg);
                                it.remove();
                            }
                        }
                    } catch (Throwable t) {
                        recordErrorAndMaybeDisable("root", t);
                    }
                }
            });
        XposedHelpers.findAndHookMethod("android.app.ApplicationPackageManager", classLoader,
            "getLaunchIntentForPackage", String.class,
            new XC_MethodHook() {
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (!enableRoot) return;
                    try {
                        String pkg = (String) param.args[0];
                        if (isRootPackage(pkg)) {
                            ReaLog.antiDetection("root", "getLaunchIntentForPackage 过滤", pkg);
                            param.setResult(null);
                        }
                    } catch (Throwable t) {
                        recordErrorAndMaybeDisable("root", t);
                    }
                }
            });

        // 进程隐藏
        XposedHelpers.findAndHookMethod("android.app.ActivityManager", classLoader,
            "getRunningAppProcesses", new XC_MethodHook() {
                protected void afterHookedMethod(MethodHookParam param) {
                    if (!enableRoot) return;
                    try {
                        List<ActivityManager.RunningAppProcessInfo> procs = (List<
                            ActivityManager.RunningAppProcessInfo>) param.getResult();
                        if (procs == null) return;
                        Iterator<ActivityManager.RunningAppProcessInfo> it = procs.iterator();
                        while (it.hasNext()) {
                            ActivityManager.RunningAppProcessInfo info = it.next();
                            if (info != null && info.processName != null) {
                                String name = info.processName;
                                if (isRootProcess(name)) {
                                    ReaLog.antiDetection("root", "进程Processes 过滤", name);
                                    it.remove();
                                }
                            }
                        }
                    } catch (Throwable t) {
                        recordErrorAndMaybeDisable("root", t);
                    }
                }
            });
        XposedHelpers.findAndHookMethod("android.app.ActivityManager", classLoader,
            "getRunningServices", int.class,
            new XC_MethodHook() {
                protected void afterHookedMethod(MethodHookParam param) {
                    if (!enableRoot) return;
                    try {
                        List<ActivityManager.RunningServiceInfo> services = (List<
                            ActivityManager.RunningServiceInfo>) param.getResult();
                        if (services == null) return;
                        Iterator<ActivityManager.RunningServiceInfo> it = services.iterator();
                        while (it.hasNext()) {
                            ActivityManager.RunningServiceInfo info = it.next();
                            if (info != null && info.service != null) {
                                String pkg = info.service.getPackageName();
                                if (isRootPackage(pkg)) {
                                    ReaLog.antiDetection("root", "进程Services 过滤", pkg);
                                    it.remove();
                                }
                            }
                        }
                    } catch (Throwable t) {
                        recordErrorAndMaybeDisable("root", t);
                    }
                }
            });

        // 命令隐藏
        XposedHelpers.findAndHookMethod("java.lang.Runtime", classLoader,
            "exec", String.class, new XC_MethodHook() {
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (!enableRoot) return;
                    try {
                        String cmd = (String) param.args[0];
                        if (isRootCommand(cmd)) {
                            ReaLog.antiDetection("root", "Runtime.exec 拦截命令", cmd);
                            param.setResult(createFakeProcess());
                        }
                    } catch (Throwable t) {
                        recordErrorAndMaybeDisable("root", t);
                    }
                }
            });
        XposedHelpers.findAndHookMethod("java.lang.Runtime", classLoader,
            "exec", String[].class, new XC_MethodHook() {
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (!enableRoot) return;
                    try {
                        String[] cmds = (String[]) param.args[0];
                        if (cmds != null && cmds.length > 0) {
                            String full = String.join(" ", cmds);
                            if (isRootCommand(full)) {
                                ReaLog.antiDetection("root", "Runtime.exec(String[]) 拦截命令", full);
                                param.setResult(createFakeProcess());
                            }
                        }
                    } catch (Throwable t) {
                        recordErrorAndMaybeDisable("root", t);
                    }
                }
            });
        XposedHelpers.findAndHookMethod("java.lang.ProcessBuilder", classLoader,
            "start", new XC_MethodHook() {
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (!enableRoot) return;
                    try {
                        ProcessBuilder pb = (ProcessBuilder) param.thisObject;
                        List<String> command = pb.command();
                        if (command != null && !command.isEmpty()) {
                            String full = String.join(" ", command);
                            if (isRootCommand(full)) {
                                ReaLog.antiDetection("root", "ProcessBuilder.start 拦截命令", full);
                                param.setResult(createFakeProcess());
                            }
                        }
                    } catch (Throwable t) {
                        recordErrorAndMaybeDisable("root", t);
                    }
                }
            });
    }

    private static boolean isRootPath(String path) {
        if (path == null) return false;
        String lower = path.toLowerCase();
        if (lower.contains("/data/adb/magisk") ||
            lower.contains("/sbin/magisk") ||
            lower.contains("/system/bin/magisk") ||
            lower.contains("/data/adb/modules") ||
            lower.contains("/system/app/magisk.apk")) {
            ReaLog.antiDetection("root", "匹配 Magisk 路径", path);
            return true;
        }
        if (lower.contains("/data/adb/ksu") ||
            lower.contains("/data/adb/ksud") ||
            lower.contains("/data/adb/modules/ksu") ||
            lower.contains("kernelsu")) {
            ReaLog.antiDetection("root", "匹配 KernelSU 路径", path);
            return true;
        }
        if (lower.contains("/data/adb/apatch") ||
            lower.contains("/data/adb/modules/apatch") ||
            lower.contains("apatch")) {
            ReaLog.antiDetection("root", "匹配 APatch 路径", path);
            return true;
        }
        if (lower.contains("/system/bin/su") ||
            lower.contains("/sbin/su") ||
            lower.contains("/system/xbin/su") ||
            lower.contains("supersu") ||
            lower.contains("superuser")) {
            ReaLog.antiDetection("root", "匹配 su 路径", path);
            return true;
        }
        return false;
    }

    private static boolean isRootPackage(String pkg) {
        if (pkg == null) return false;
        if (XPACKAGES_EXACT.contains(pkg)) {
            ReaLog.antiDetection("root", "匹配 Root 包名(精确)", pkg);
            return true;
        }
        String lower = pkg.toLowerCase();
        if (lower.contains("magisk") || lower.contains("supersu") ||
            lower.contains("superuser") || lower.contains("kingroot") ||
            lower.contains("kinguser") || lower.contains("rootcloak") ||
            lower.contains("luckypatcher") || lower.contains("substrate") ||
            lower.contains("kernelsu") || lower.contains("ksu") ||
            lower.contains("apatch")) {
            ReaLog.antiDetection("root", "匹配 Root 包名(关键词)", pkg);
            return true;
        }
        return false;
    }

    private static boolean isRootProcess(String name) {
        if (name == null) return false;
        String lower = name.toLowerCase();
        if (lower.contains("su") || lower.contains("magisk") ||
            lower.contains("magiskd") || lower.contains("busybox") ||
            lower.contains("supersu") || lower.contains("superuser") ||
            lower.contains("kingroot") || lower.contains("substrate") ||
            lower.contains("kernelsu") || lower.contains("ksud") ||
            lower.contains("apatch")) {
            ReaLog.antiDetection("root", "匹配 Root 进程", name);
            return true;
        }
        return false;
    }

    private static boolean isRootCommand(String cmd) {
        if (cmd == null) return false;
        String lower = cmd.toLowerCase();
        if (lower.contains("su") || lower.contains("magisk") ||
            lower.contains("busybox") || lower.contains("which su") ||
            lower.contains("ksud") || lower.contains("kernelsu") ||
            lower.contains("apatch") ||
            lower.contains("ls /system/bin/su") || lower.contains("cat /system/build.prop") ||
            lower.contains("mount -o rw") || lower.contains("chmod 6755")) {
            ReaLog.antiDetection("root", "匹配 Root 命令", cmd);
            return true;
        }
        return false;
    }

    private static void hookAdbHide(ClassLoader classLoader) throws Throwable {
        ReaLog.antiDetection("hook", "安装 ADB 隐藏", "");
        XposedHelpers.findAndHookMethod("android.provider.Settings$Global", classLoader,
            "getInt", ContentResolver.class, String.class, int.class,
            new XC_MethodHook() {
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (!enableAdb) return;
                    try {
                        String key = (String) param.args[1];
                        if ("adb_enabled".equals(key)) {
                            ReaLog.antiDetection("adb", "ADB 隐藏1", "adb_enabled -> 0");
                            param.setResult(0);
                        }
                    } catch (Throwable t) {
                        recordErrorAndMaybeDisable("adb", t);
                    }
                }
            });
        XposedHelpers.findAndHookMethod("android.provider.Settings$Secure", classLoader,
            "getInt", ContentResolver.class, String.class, int.class,
            new XC_MethodHook() {
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (!enableAdb) return;
                    try {
                        String key = (String) param.args[1];
                        if ("adb_enabled".equals(key)) {
                            ReaLog.antiDetection("adb", "ADB 隐藏2", "adb_enabled -> 0");
                            param.setResult(0);
                        }
                    } catch (Throwable t) {
                        recordErrorAndMaybeDisable("adb", t);
                    }
                }
            });
    }

    private static void hookDevHide(ClassLoader classLoader) throws Throwable {
        ReaLog.antiDetection("hook", "安装 开发者选项隐藏", "");
        XposedHelpers.findAndHookMethod("android.provider.Settings$Global", classLoader,
            "getInt", ContentResolver.class, String.class, int.class,
            new XC_MethodHook() {
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (!enableDev) return;
                    try {
                        String key = (String) param.args[1];
                        if ("development_settings_enabled".equals(key)) {
                            ReaLog.antiDetection("dev", "开发者隐藏1：", "development_settings_enabled -> 0");
                            param.setResult(0);
                        }
                    } catch (Throwable t) {
                        recordErrorAndMaybeDisable("dev", t);
                    }
                }
            });
        XposedHelpers.findAndHookMethod("android.provider.Settings$Secure", classLoader,
            "getInt", ContentResolver.class, String.class, int.class,
            new XC_MethodHook() {
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (!enableDev) return;
                    try {
                        String key = (String) param.args[1];
                        if ("development_settings_enabled".equals(key)) {
                            ReaLog.antiDetection("dev", "开发者隐藏2：", "development_settings_enabled -> 0");
                            param.setResult(0);
                        }
                    } catch (Throwable t) {
                        recordErrorAndMaybeDisable("dev", t);
                    }
                }
            });
    }

    private static void hookSystemProperties(ClassLoader classLoader) {
        ReaLog.antiDetection("hook", "安装 系统属性隐藏", "");
        try {
            Class<
                ?> spClass = XposedHelpers.findClassIfExists("android.os.SystemProperties", classLoader);
            if (spClass == null) return;

            XposedHelpers.findAndHookMethod(spClass, "get", String.class, String.class,
                new XC_MethodHook() {
                    protected void afterHookedMethod(MethodHookParam param) {
                        try {
                            String key = (String) param.args[0];
                            if (key != null && key.toLowerCase().contains("xposed")) {
                                ReaLog.antiDetection("system", "系统属性隐藏 拦截", key);
                                param.setResult(param.args[1]);
                            }
                        } catch (Throwable t) {
                            // 此方法不属于任何子功能，仅记录日志，不进行计数禁用
                            // log("SystemProperties.get 回调异常: " + t.getMessage());

                        }
                    }
                });
        } catch (Throwable ignored) {
        }
    }
}
