package com.install.appinstall.xl.util;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Selinuxhook {
    private static final Set<String> SELINUX_SYSFS_PATHS = new HashSet<String>(Arrays.asList(
                                                                                   "/sys/fs/selinux/enforce", "/sys/fs/selinux/status", "/sys/fs/selinux/mls", "/sys/fs/selinux/policy"));
    private static final Map<String, byte[]> SELINUX_FAKE_CONTENTS = new HashMap<String, byte[]>();
    static {
        SELINUX_FAKE_CONTENTS.put("/sys/fs/selinux/enforce", "1\n".getBytes());
        SELINUX_FAKE_CONTENTS.put("/sys/fs/selinux/status", "enabled\n".getBytes());
        SELINUX_FAKE_CONTENTS.put("/sys/fs/selinux/mls", "1\n".getBytes());
        SELINUX_FAKE_CONTENTS.put("/sys/fs/selinux/policy", new byte[0]);
    }
    private static final Map<String, Boolean> enabledMap = new ConcurrentHashMap<String, Boolean>();
    private static final AtomicBoolean isFaking = new AtomicBoolean(false);
    private static final ThreadLocal<Boolean> skipFake = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return false;
        }
    };

    public static void setEnabled(String packageName, boolean enabled) {
        if (packageName != null) {
            enabledMap.put(packageName, enabled);
            ReaLog.log("selinux", "SELinux伪装: " + (enabled ? "开启" : "关闭") + " (包: " + packageName + ")");
        }
    }

    private static boolean isEnabledForCurrentApp() {
        String currentPkg = getCurrentPackageName();
        if (currentPkg == null) return false;
        Boolean enabled = enabledMap.get(currentPkg);
        return enabled != null && enabled;
    }

    private static String getCurrentPackageName() {
        try {
            Object currentApp = XposedHelpers.callStaticMethod(
                XposedHelpers.findClass("android.app.ActivityThread", null),
                "currentApplication");
            if (currentApp != null) {
                return (String) XposedHelpers.callMethod(currentApp, "getPackageName");
            }
        } catch (Throwable ignored) {}
        return null;
    }

    public static String getRealSelinuxMode() {
        skipFake.set(true);
        try {
            try {
                FileInputStream fis = new FileInputStream("/sys/fs/selinux/enforce");
                int value = fis.read();
                fis.close();
                if (value == '1') return "Enforcing";
                else if (value == '0') return "Permissive";
            } catch (Throwable ignored) {}
            try {
                Process process = Runtime.getRuntime().exec(new String[]{"/system/bin/getenforce"});
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line = reader.readLine();
                process.waitFor();
                if (line != null) {
                    String trimmed = line.trim();
                    if ("Enforcing".equalsIgnoreCase(trimmed)) return "Enforcing";
                    if ("Permissive".equalsIgnoreCase(trimmed)) return "Permissive";
                }
            } catch (Throwable ignored) {}
            try {
                String prop = (String) XposedHelpers.callStaticMethod(
                    XposedHelpers.findClass("android.os.SystemProperties", null),
                    "get", "ro.boot.selinux", "");
                if ("enforcing".equalsIgnoreCase(prop)) return "Enforcing";
                if ("permissive".equalsIgnoreCase(prop)) return "Permissive";
            } catch (Throwable ignored) {}
            return "Unknown";
        } finally {
            skipFake.set(false);
        }
    }

    public static void installHooks(ClassLoader classLoader) {
        hookFileRead();
        hookSystemProperties();
        hookGetEnforceCommand();
    }

    private static void hookFileRead() {
        try {
            XposedHelpers.findAndHookConstructor(FileInputStream.class, String.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if (!isEnabledForCurrentApp()) return;
                        String path = (String) param.args[0];
                        if (SELINUX_SYSFS_PATHS.contains(path)) {
                            XposedHelpers.setAdditionalInstanceField(param.thisObject, "selinux_path", path);
                            ReaLog.log("selinux", "检测到SELinux文件读取: " + path);
                        }
                    }
                });

            XposedHelpers.findAndHookMethod("java.io.FileInputStream", null, "read",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        if (skipFake.get() || !isEnabledForCurrentApp()) return;
                        String path = (String) XposedHelpers.getAdditionalInstanceField(param.thisObject, "selinux_path");
                        if (path != null && SELINUX_FAKE_CONTENTS.containsKey(path)) {
                            if (isFaking.compareAndSet(false, true)) {
                                try {
                                    byte[] fakeData = SELINUX_FAKE_CONTENTS.get(path);
                                    param.setResult((fakeData.length > 0) ? (fakeData[0] & 0xFF) : -1);
                                    ReaLog.log("selinux", "拦截SELinux文件读取: " + path);
                                } finally {
                                    isFaking.set(false);
                                }
                            }
                        }
                    }
                });

            XposedHelpers.findAndHookMethod("java.io.FileInputStream", null, "read",
                byte[].class, int.class, int.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        if (skipFake.get() || !isEnabledForCurrentApp()) return;
                        String path = (String) XposedHelpers.getAdditionalInstanceField(param.thisObject, "selinux_path");
                        if (path != null && SELINUX_FAKE_CONTENTS.containsKey(path)) {
                            if (isFaking.compareAndSet(false, true)) {
                                try {
                                    byte[] fakeData = SELINUX_FAKE_CONTENTS.get(path);
                                    byte[] buf = (byte[]) param.args[0];
                                    int off = (int) param.args[1];
                                    int len = (int) param.args[2];
                                    int copyLen = Math.min(fakeData.length, len);
                                    if (copyLen > 0) {
                                        System.arraycopy(fakeData, 0, buf, off, copyLen);
                                        param.setResult(copyLen);
                                    } else {
                                        param.setResult(-1);
                                    }
                                    ReaLog.log("selinux", "拦截SELinux文件读取(批量): " + path);
                                } finally {
                                    isFaking.set(false);
                                }
                            }
                        }
                    }
                });

            XposedHelpers.findAndHookMethod("java.io.BufferedReader", null, "readLine",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        if (skipFake.get() || !isEnabledForCurrentApp()) return;
                        Object reader = XposedHelpers.getObjectField(param.thisObject, "in");
                        if (reader != null) {
                            String path = (String) XposedHelpers.getAdditionalInstanceField(reader, "selinux_path");
                            if (path != null && SELINUX_FAKE_CONTENTS.containsKey(path)) {
                                if (isFaking.compareAndSet(false, true)) {
                                    try {
                                        byte[] fakeData = SELINUX_FAKE_CONTENTS.get(path);
                                        param.setResult(new String(fakeData).trim());
                                        ReaLog.log("selinux", "拦截SELinux readLine: " + path);
                                    } finally {
                                        isFaking.set(false);
                                    }
                                }
                            }
                        }
                    }
                });
        } catch (Throwable t) {
            //XposedBridge.log("[SELinuxHook] hookFileRead 失败: " + t.getMessage());
        }
    }

    private static void hookSystemProperties() {
        try {
            Class<?> spClass = XposedHelpers.findClass("android.os.SystemProperties", null);
            XposedBridge.hookAllMethods(spClass, "get", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        if (!isEnabledForCurrentApp()) return;
                        String key = (String) param.args[0];
                        if (key != null && key.toLowerCase().contains("selinux")) {
                            param.setResult("enforcing");
                            ReaLog.log("selinux", "拦截SELinux系统属性: " + key);
                        }
                    }
                });
        } catch (Throwable t) {
            //   XposedBridge.log("[SELinuxHook] hookSystemProperties 失败: " + t.getMessage());
        }
    }

    private static void hookGetEnforceCommand() {
        try {
            final Class<?> runtimeClass = Class.forName("java.lang.Runtime");
            Method execMethod = runtimeClass.getMethod("exec", String.class);
            XposedBridge.hookMethod(execMethod, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        if (skipFake.get() || !isEnabledForCurrentApp()) return;
                        String cmd = (String) param.args[0];
                        if (cmd != null && cmd.trim().equals("getenforce")) {
                            param.setResult(createFakeProcess("Enforcing"));
                            ReaLog.log("selinux", "拦截getenforce命令");
                        }
                    }
                });
            Method execArrayMethod = runtimeClass.getMethod("exec", String[].class);
            XposedBridge.hookMethod(execArrayMethod, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        if (skipFake.get() || !isEnabledForCurrentApp()) return;
                        String[] cmdArray = (String[]) param.args[0];
                        if (cmdArray != null && cmdArray.length == 1 && "getenforce".equals(cmdArray[0])) {
                            param.setResult(createFakeProcess("Enforcing"));
                            ReaLog.log("selinux", "拦截getenforce命令(数组)");
                        }
                    }
                });
        } catch (Throwable t) {
            // XposedBridge.log("[SELinuxHook] hookGetEnforceCommand 失败: " + t.getMessage());
        }
    }

    private static Process createFakeProcess(final String output) {
        final InputStream fakeIn = new ByteArrayInputStream(output.getBytes());
        return new Process() {
            @Override public OutputStream getOutputStream() {
                return new OutputStream() { @Override public void write(int b) {} };
            }
            @Override public InputStream getInputStream() { return fakeIn; }
            @Override public InputStream getErrorStream() { return new ByteArrayInputStream(new byte[0]); }
            @Override public int waitFor() { return 0; }
            @Override public int exitValue() { return 0; }
            @Override public void destroy() {}
        };
    }
}
