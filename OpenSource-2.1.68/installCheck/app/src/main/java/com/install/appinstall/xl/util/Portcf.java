package com.install.appinstall.xl.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.install.appinstall.xl.HookInit;
import com.install.appinstall.xl.HookInit.PackageConfig;
import com.install.appinstall.xl.HookInit.InterceptPattern;
import java.util.concurrent.ConcurrentHashMap;

public class Portcf {

    private static final LinkedHashMap<String, String> CONFIG_ITEMS = new LinkedHashMap<>();

    static {
        CONFIG_ITEMS.put("install_status", "安装状态");
        CONFIG_ITEMS.put("floating_shown", "悬浮窗显示");
        CONFIG_ITEMS.put("permanent_hidden", "永久隐藏");
        CONFIG_ITEMS.put("floating_x", "悬浮窗X坐标");
        CONFIG_ITEMS.put("floating_y", "悬浮窗Y坐标");
        CONFIG_ITEMS.put("user_disabled_auto_block", "自动拦截");
        CONFIG_ITEMS.put("block_exit", "普通拦截");
        CONFIG_ITEMS.put("super_block_exit", "超强拦截");
        CONFIG_ITEMS.put("permission_fake", "权限防护");
        CONFIG_ITEMS.put("launch_intercept", "启动拦截");
        CONFIG_ITEMS.put("selinux_fake", "SELinux伪装");
        CONFIG_ITEMS.put("vendor_enabled", "强制包启用");
        CONFIG_ITEMS.put("force_default_back", "返回键控制");
        CONFIG_ITEMS.put("crash_protect_enabled", "闪退防护");
        CONFIG_ITEMS.put("anti_detection_detail", "痕迹检测");
        CONFIG_ITEMS.put("vpn_fake_detail", "网络代理");
        CONFIG_ITEMS.put("permission_fake_detail", "权限防护");
        CONFIG_ITEMS.put("share_fake_detail", "假装分享");
        CONFIG_ITEMS.put("user_defined_packages", "自定义包名");
        CONFIG_ITEMS.put("excluded_packages", "已排除包名");
        CONFIG_ITEMS.put("package_configs", "独立包名配置");
        CONFIG_ITEMS.put("auto_actions", "黑白名单");
        CONFIG_ITEMS.put("auto_records", "智能记录");
        CONFIG_ITEMS.put("intercept_patterns", "拦截模式");
        CONFIG_ITEMS.put("rea_log_pause", "日志运行状态");
    }

    // ========== 🔧 新增：空监听器工具方法 ==========
    private static DialogInterface.OnClickListener emptyListener() {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface d, int which) {
                d.dismiss();
            }
        };
    }

    public static String exportConfig(Activity activity, String targetApp, List<String> keysToExport) {
        ReaLog.log("config", "开始导出配置: " + targetApp + ", 选定项数: " + (keysToExport == null ? "全部" : keysToExport.size()));
        try {
            File dir = activity.getExternalFilesDir(null);
            if (dir == null) {
                ReaLog.log("config", "导出失败: 无法获取外部存储目录");
                return "storage_error";
            }
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "installcf_" + targetApp + "_" + timeStamp + ".json";
            File configFile = new File(dir, fileName);
            JSONObject appConfig = buildConfigJson(targetApp, keysToExport);
            if (appConfig == null || appConfig.length() == 0) {
                ReaLog.log("config", "导出失败: 没有有效配置项可导出");
                return "no_items";
            }
            JSONObject root = new JSONObject();
            root.put(targetApp, appConfig);
            writeJsonToFile(configFile, root);
            int total = (keysToExport == null || keysToExport.isEmpty()) ? CONFIG_ITEMS.size() : keysToExport.size();
            ReaLog.log("config", "导出成功: " + total + " 项, 路径: " + configFile.getAbsolutePath());
            return "success:" + total + ":" + configFile.getAbsolutePath();
        } catch (Throwable t) {
            ReaLog.log("config", "导出异常: " + t.getMessage());
            return "error:" + t.getMessage();
        }
    }

    private static JSONObject buildConfigJson(String targetApp, List<String> keysToExport) throws Exception {
        JSONObject appConfig = new JSONObject();
        boolean exportAll = (keysToExport == null || keysToExport.isEmpty());
        if (exportAll) keysToExport = new ArrayList<>(CONFIG_ITEMS.keySet());
        for (String key : keysToExport) {
            try {
                switch (key) {
                    case "install_status":
                        appConfig.put("install_status", HookInit.installStatusMap.getOrDefault(targetApp, true)); break;
                    case "floating_shown":
                        appConfig.put("floating_shown", HookInit.floatingShownMap.getOrDefault(targetApp, true)); break;
                    case "floating_x": {
                            Float x = HookInit.floatingXMap.get(targetApp);
                            appConfig.put("floating_x", x != null ? String.valueOf(x) : "null"); break;
                        }
                    case "floating_y": {
                            Float y = HookInit.floatingYMap.get(targetApp);
                            appConfig.put("floating_y", y != null ? String.valueOf(y) : "null"); break;
                        }
                    case "user_disabled_auto_block":
                        appConfig.put("user_disabled_auto_block", HookInit.userDisabledAutoBlockMap.getOrDefault(targetApp, false)); break;
                    case "block_exit":
                        appConfig.put("block_exit", HookInit.blockExitMap.getOrDefault(targetApp, false)); break;
                    case "super_block_exit":
                        appConfig.put("super_block_exit", HookInit.superBlockExitMap.getOrDefault(targetApp, false)); break;
                    case "permission_fake":
                        appConfig.put("permission_fake", HookInit.permissionFakeMap.getOrDefault(targetApp, true)); break;
                    case "launch_intercept":
                        appConfig.put("launch_intercept", HookInit.launchInterceptMap.getOrDefault(targetApp, true)); break;
                    case "selinux_fake":
                        appConfig.put("selinux_fake", HookInit.selinuxFakeMap.getOrDefault(targetApp, true)); break;
                    case "vendor_enabled": {
                            Boolean v = HookInit.vendorChoiceMap.get(targetApp);
                            appConfig.put("vendor_enabled", v != null ? v : false); break;
                        }
                    case "force_default_back":
                        appConfig.put("force_default_back", HookInit.forceDefaultBackMap.getOrDefault(targetApp, false)); break;
                    case "crash_protect_enabled":
                        appConfig.put("crash_protect_enabled", HookInit.crashProtectEnabledMap.getOrDefault(targetApp, true)); break;
                    case "anti_detection_detail": {
                            Map<String, Boolean> map = HookInit.antiDetectionDetailMap.getOrDefault(targetApp, new HashMap<String, Boolean>());
                            appConfig.put("anti_detection_detail", new JSONObject(map)); break;
                        }
                    case "vpn_fake_detail": {
                            Map<String, Boolean> map = HookInit.vpnFakeDetailMap.getOrDefault(targetApp, new HashMap<String, Boolean>());
                            appConfig.put("vpn_fake_detail", new JSONObject(map)); break;
                        }
                    case "permission_fake_detail": {
                            Map<String, Boolean> map = HookInit.permissionFakeDetailMap.getOrDefault(targetApp, new HashMap<String, Boolean>());
                            appConfig.put("permission_fake_detail", new JSONObject(map)); break;
                        }
                    case "share_fake_detail": {
                            Map<String, Boolean> map = HookInit.shareFakeDetailMap.getOrDefault(targetApp, HookInit.getDefaultShareDetailMap());
                            appConfig.put("share_fake_detail", new JSONObject(map)); break;
                        }
                    case "user_defined_packages": {
                            List<String> list = HookInit.userDefinedPackagesMap.getOrDefault(targetApp, new ArrayList<String>());
                            appConfig.put("user_defined_packages", stringListToJsonArray(list)); break;
                        }
                    case "excluded_packages": {
                            List<String> list = HookInit.excludedPackagesMap.getOrDefault(targetApp, new ArrayList<String>());
                            appConfig.put("excluded_packages", stringListToJsonArray(list)); break;
                        }
                    case "package_configs": {
                            List<PackageConfig> configs = HookInit.packageConfigMap.getOrDefault(targetApp, new ArrayList<PackageConfig>());
                            JSONArray arr = new JSONArray();
                            for (PackageConfig cfg : configs) {
                                JSONObject obj = new JSONObject();
                                obj.put("packageName", cfg.packageName);
                                obj.put("statusMode", cfg.statusMode);
                                arr.put(obj);
                            }
                            appConfig.put("package_configs", arr); break;
                        }
                    case "auto_actions": {
                            Map<String, String> map = HookInit.autoActionMap.get(targetApp);
                            if (map != null && !map.isEmpty()) appConfig.put("auto_actions", new JSONObject(map)); break;
                        }
                    case "auto_records": {
                            Map<String, List<String>> records = HookInit.autoChoiceRecordsMap.get(targetApp);
                            if (records != null && !records.isEmpty()) {
                                JSONObject recJson = new JSONObject();
                                for (Map.Entry<String, List<String>> entry : records.entrySet()) {
                                    JSONArray arr = new JSONArray();
                                    for (String s : entry.getValue()) arr.put(s);
                                    recJson.put(entry.getKey(), arr);
                                }
                                appConfig.put("auto_records", recJson);
                            } break;
                        }
                    case "intercept_patterns": {
                            List<InterceptPattern> patterns = HookInit.interceptPatternsMap.get(targetApp);
                            if (patterns != null && !patterns.isEmpty()) {
                                JSONArray arr = new JSONArray();
                                for (InterceptPattern p : patterns) {
                                    JSONObject obj = new JSONObject();
                                    obj.put("pattern_hash", p.patternHash);
                                    obj.put("installed_packages", stringListToJsonArray(p.installedPackages));
                                    obj.put("not_installed_packages", stringListToJsonArray(p.notInstalledPackages));
                                    obj.put("user_choice", p.userChoice != null ? p.userChoice : "");
                                    obj.put("choice_count", p.choiceCount);
                                    obj.put("last_detected_time", p.lastDetectedTime);
                                    obj.put("silent_intercept", p.silentIntercept);
                                    arr.put(obj);
                                }
                                appConfig.put("intercept_patterns", arr);
                            } break;
                        }
                    case "rea_log_pause": {
                            Map<String, Boolean> pauseMap = HookInit.reaLogPauseMap.get(targetApp);
                            if (pauseMap != null && !pauseMap.isEmpty()) {
                                appConfig.put("rea_log_pause", new JSONObject(pauseMap));
                            }
                            break;
                        }
                    default: break;
                }
            } catch (Throwable t) {
                ReaLog.log("config", "构建配置项 " + key + " 异常: " + t.getMessage());
            }
        }
        // ===== 强制加入永久隐藏配置（无论是否勾选） =====
        Boolean permHidden = HookInit.permanentHiddenMap.get(targetApp);
        if (permHidden != null) {
            appConfig.put("permanent_hidden", permHidden);
        }
        return appConfig.length() > 0 ? appConfig : null;
    }

    public static String importConfig(Activity activity, String targetApp, List<String> keysToImport,
                                      TextView floatingView, HookInit hookInit, File importFile) {
        ReaLog.log("config", "开始导入配置: " + targetApp + ", 选定项数: " + (keysToImport == null ? "全部" : keysToImport.size()));
        File dir = activity.getExternalFilesDir(null);
        if (dir == null) {
            ReaLog.log("config", "导入失败: 无法访问存储");
            return "storage_error";
        }
        File fileToUse = importFile;
        if (fileToUse == null) {
            File[] files = dir.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File d, String name) {
                        return (name.startsWith("installcf_") && name.endsWith(".json")) ||
                            "install_fake_config.json".equals(name);
                    }
                });
            if (files == null || files.length == 0) {
                ReaLog.log("config", "未找到配置文件");
                return "no_files";
            }
            Arrays.sort(files, new Comparator<File>() {
                    @Override
                    public int compare(File f1, File f2) {
                        return Long.compare(f2.lastModified(), f1.lastModified());
                    }
                });
            fileToUse = files[0];
            ReaLog.log("config", "自动选择最新配置文件: " + fileToUse.getName());
        }
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileToUse), StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            reader.close();

            JSONObject root = new JSONObject(sb.toString());
            List<String> packages = getTopLevelPackages(root);
            if (packages.isEmpty()) {
                ReaLog.log("config", "配置文件中没有顶层包名");
                return "no_package";
            }
            String sourcePackage = packages.get(0);
            ReaLog.log("config", "读取到来源包名: " + sourcePackage);

            JSONObject appConfig = root.getJSONObject(sourcePackage);
            boolean importAll = (keysToImport == null || keysToImport.isEmpty());
            if (importAll) keysToImport = new ArrayList<>(CONFIG_ITEMS.keySet());

            int totalKeys = keysToImport.size();
            int successCount = 0;
            int skippedCount = 0;
            List<String> importedKeys = new ArrayList<>();

            for (String key : keysToImport) {
                if (!appConfig.has(key)) {
                    skippedCount++;
                    ReaLog.log("config", "配置项 " + key + " 在文件中不存在，跳过");
                    continue;
                }
                importedKeys.add(key);
                applyConfigItem(targetApp, key, appConfig);
                successCount++;
            }

            hookInit.saveConfigImmediate();  // 立即保存
            ReaLog.log("config", "导入完成，成功 " + successCount + " 项，跳过 " + skippedCount + " 项");

            // 刷新 ReaLog 暂停状态缓存
            ReaLog.reloadPauseState();

            if (floatingView != null) {
                String statusText = HookInit.installStatusMap.getOrDefault(targetApp, true) ? "已安装" : "未安装";
                String blockText = (HookInit.blockExitMap.getOrDefault(targetApp, false) ||
                    HookInit.superBlockExitMap.getOrDefault(targetApp, false)) ? "[拦截]" : "";
                floatingView.setText("安装防护(" + statusText + ")" + blockText);
                floatingView.invalidate();
                floatingView.requestLayout();
            }
            hookInit.updateFloatingView(activity);

            ReaLog.log("config", "导入成功: " + successCount + " 项, 来源: " + sourcePackage + ", 跳过: " + skippedCount);
            // 强制刷新悬浮窗位置
            if (activity != null && !activity.isFinishing()) {
                try {
                    ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
                    View oldView = decorView.findViewWithTag("install_fake_floating");
                    if (oldView != null) {
                        decorView.removeView(oldView);
                    }
                    hookInit.updateFloatingView(activity);
                } catch (Throwable ignored) {}
            }

            return "success:" + totalKeys + ":" + successCount + ":" + skippedCount;
        } catch (Throwable t) {
            ReaLog.log("config", "导入异常: " + t.getMessage());
            return "error:" + t.getMessage();
        }
    }

    private static void applyConfigItem(String targetApp, String key, JSONObject appConfig) throws Exception {
        switch (key) {
                // ===== 直接覆盖的配置（单一值） =====
            case "install_status":
                HookInit.installStatusMap.put(targetApp, appConfig.optBoolean("install_status", true));
                break;
            case "floating_shown":
                HookInit.floatingShownMap.put(targetApp, appConfig.optBoolean("floating_shown", true));
                break;
            case "permanent_hidden":
                HookInit.permanentHiddenMap.put(targetApp, appConfig.optBoolean("permanent_hidden", false));
                break;
            case "floating_x": {
                    String val = appConfig.optString("floating_x", "null");
                    if (!"null".equals(val) && !val.isEmpty()) {
                        try { HookInit.floatingXMap.put(targetApp, Float.parseFloat(val)); } catch (NumberFormatException ignored) {}
                    } else HookInit.floatingXMap.remove(targetApp);
                    break;
                }
            case "floating_y": {
                    String val = appConfig.optString("floating_y", "null");
                    if (!"null".equals(val) && !val.isEmpty()) {
                        try { HookInit.floatingYMap.put(targetApp, Float.parseFloat(val)); } catch (NumberFormatException ignored) {}
                    } else HookInit.floatingYMap.remove(targetApp);
                    break;
                }
            case "user_disabled_auto_block":
                HookInit.userDisabledAutoBlockMap.put(targetApp, appConfig.optBoolean("user_disabled_auto_block", false));
                break;
            case "block_exit":
                HookInit.blockExitMap.put(targetApp, appConfig.optBoolean("block_exit", false));
                break;
            case "super_block_exit":
                HookInit.superBlockExitMap.put(targetApp, appConfig.optBoolean("super_block_exit", false));
                break;
            case "permission_fake":
                HookInit.permissionFakeMap.put(targetApp, appConfig.optBoolean("permission_fake", true));
                break;
            case "launch_intercept":
                HookInit.launchInterceptMap.put(targetApp, appConfig.optBoolean("launch_intercept", true));
                break;
            case "selinux_fake":
                HookInit.selinuxFakeMap.put(targetApp, appConfig.optBoolean("selinux_fake", true));
                break;
            case "vendor_enabled":
                HookInit.vendorChoiceMap.put(targetApp, appConfig.optBoolean("vendor_enabled", false));
                break;
            case "force_default_back":
                HookInit.forceDefaultBackMap.put(targetApp, appConfig.optBoolean("force_default_back", false));
                break;
            case "crash_protect_enabled":
                HookInit.crashProtectEnabledMap.put(targetApp, appConfig.optBoolean("crash_protect_enabled", true));
                break;
            case "anti_detection_detail": {
                    JSONObject detail = appConfig.optJSONObject("anti_detection_detail");
                    Map<String, Boolean> map = new HashMap<String, Boolean>();
                    if (detail != null) {
                        Iterator<String> it = detail.keys();
                        while (it.hasNext()) {
                            String k = it.next();
                            map.put(k, detail.getBoolean(k));
                        }
                    }
                    HookInit.antiDetectionDetailMap.put(targetApp, map);
                    break;
                }
            case "vpn_fake_detail": {
                    JSONObject detail = appConfig.optJSONObject("vpn_fake_detail");
                    Map<String, Boolean> map = new HashMap<String, Boolean>();
                    if (detail != null) {
                        Iterator<String> it = detail.keys();
                        while (it.hasNext()) {
                            String k = it.next();
                            map.put(k, detail.getBoolean(k));
                        }
                    }
                    HookInit.vpnFakeDetailMap.put(targetApp, map);
                    break;
                }
            case "permission_fake_detail": {
                    JSONObject detail = appConfig.optJSONObject("permission_fake_detail");
                    Map<String, Boolean> map = new HashMap<String, Boolean>();
                    if (detail != null) {
                        Iterator<String> it = detail.keys();
                        while (it.hasNext()) {
                            String k = it.next();
                            map.put(k, detail.getBoolean(k));
                        }
                    }
                    HookInit.permissionFakeDetailMap.put(targetApp, map);
                    break;
                }
            case "share_fake_detail": {
                    JSONObject detail = appConfig.optJSONObject("share_fake_detail");
                    Map<String, Boolean> map = new HashMap<String, Boolean>();
                    if (detail != null) {
                        Iterator<String> it = detail.keys();
                        while (it.hasNext()) {
                            String k = it.next();
                            map.put(k, detail.getBoolean(k));
                        }
                    }
                    HookInit.shareFakeDetailMap.put(targetApp, map);
                    break;
                }

                // ===== 合并型配置（列表/Map） =====
            case "user_defined_packages": {
                    JSONArray arr = appConfig.optJSONArray("user_defined_packages");
                    List<String> importList = arr != null ? jsonArrayToStringList(arr) : new ArrayList<String>();
                    List<String> currentList = HookInit.userDefinedPackagesMap.getOrDefault(targetApp, new ArrayList<String>());
                    for (String pkg : importList) {
                        if (!currentList.contains(pkg)) {
                            currentList.add(pkg);
                        }
                    }
                    HookInit.userDefinedPackagesMap.put(targetApp, currentList);
                    break;
                }
            case "excluded_packages": {
                    JSONArray arr = appConfig.optJSONArray("excluded_packages");
                    List<String> importList = arr != null ? jsonArrayToStringList(arr) : new ArrayList<String>();
                    List<String> currentList = HookInit.excludedPackagesMap.getOrDefault(targetApp, new ArrayList<String>());
                    for (String pkg : importList) {
                        if (!currentList.contains(pkg)) {
                            currentList.add(pkg);
                        }
                    }
                    HookInit.excludedPackagesMap.put(targetApp, currentList);
                    break;
                }
            case "package_configs": {
                    JSONArray arr = appConfig.optJSONArray("package_configs");
                    List<PackageConfig> importConfigs = new ArrayList<PackageConfig>();
                    if (arr != null) {
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject obj = arr.getJSONObject(i);
                            PackageConfig cfg = new PackageConfig(obj.getString("packageName"));
                            cfg.statusMode = obj.optString("statusMode", "follow");
                            importConfigs.add(cfg);
                        }
                    }
                    List<PackageConfig> currentConfigs = HookInit.packageConfigMap.getOrDefault(targetApp, new ArrayList<PackageConfig>());
                    Map<String, PackageConfig> configMap = new HashMap<String, PackageConfig>();
                    for (PackageConfig cfg : currentConfigs) {
                        configMap.put(cfg.packageName, cfg);
                    }
                    for (PackageConfig cfg : importConfigs) {
                        configMap.put(cfg.packageName, cfg);
                    }
                    HookInit.packageConfigMap.put(targetApp, new ArrayList<PackageConfig>(configMap.values()));
                    break;
                }
            case "auto_actions": {
                    JSONObject obj = appConfig.optJSONObject("auto_actions");
                    Map<String, String> importMap = new HashMap<String, String>();
                    if (obj != null) {
                        Iterator<String> it = obj.keys();
                        while (it.hasNext()) {
                            String k = it.next();
                            importMap.put(k, obj.getString(k));
                        }
                    }
                    Map<String, String> currentMap = HookInit.autoActionMap.getOrDefault(targetApp, new HashMap<String, String>());
                    currentMap.putAll(importMap);
                    HookInit.autoActionMap.put(targetApp, currentMap);
                    break;
                }
            case "auto_records": {
                    JSONObject obj = appConfig.optJSONObject("auto_records");
                    Map<String, List<String>> importRecords = new HashMap<String, List<String>>();
                    if (obj != null) {
                        Iterator<String> it = obj.keys();
                        while (it.hasNext()) {
                            String k = it.next();
                            JSONArray arr = obj.getJSONArray(k);
                            List<String> list = new ArrayList<String>();
                            for (int i = 0; i < arr.length(); i++) list.add(arr.getString(i));
                            importRecords.put(k, list);
                        }
                    }
                    Map<String, List<String>> currentRecords = HookInit.autoChoiceRecordsMap.getOrDefault(targetApp, new HashMap<String, List<String>>());
                    currentRecords.putAll(importRecords);
                    HookInit.autoChoiceRecordsMap.put(targetApp, currentRecords);
                    break;
                }
            case "intercept_patterns": {
                    JSONArray arr = appConfig.optJSONArray("intercept_patterns");
                    List<InterceptPattern> importPatterns = new ArrayList<InterceptPattern>();
                    if (arr != null) {
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject obj = arr.getJSONObject(i);
                            InterceptPattern p = new InterceptPattern(
                                obj.getString("pattern_hash"),
                                jsonArrayToStringList(obj.getJSONArray("installed_packages")),
                                jsonArrayToStringList(obj.getJSONArray("not_installed_packages"))
                            );
                            p.userChoice = obj.optString("user_choice", "");
                            p.choiceCount = obj.optInt("choice_count", 0);
                            p.lastDetectedTime = obj.optLong("last_detected_time", System.currentTimeMillis());
                            p.silentIntercept = obj.optBoolean("silent_intercept", false);
                            importPatterns.add(p);
                        }
                    }
                    List<InterceptPattern> currentPatterns = HookInit.interceptPatternsMap.getOrDefault(targetApp, new ArrayList<InterceptPattern>());
                    Map<String, InterceptPattern> patternMap = new HashMap<String, InterceptPattern>();
                    for (InterceptPattern p : currentPatterns) {
                        patternMap.put(p.patternHash, p);
                    }
                    for (InterceptPattern p : importPatterns) {
                        patternMap.put(p.patternHash, p);
                    }
                    HookInit.interceptPatternsMap.put(targetApp, new ArrayList<InterceptPattern>(patternMap.values()));
                    break;
                }
            case "rea_log_pause": {
                    JSONObject obj = appConfig.optJSONObject("rea_log_pause");
                    Map<String, Boolean> importMap = new ConcurrentHashMap<String, Boolean>();
                    if (obj != null) {
                        Iterator<String> it = obj.keys();
                        while (it.hasNext()) {
                            String k = it.next();
                            importMap.put(k, obj.getBoolean(k));
                        }
                    }
                    Map<String, Boolean> currentMap = HookInit.reaLogPauseMap.getOrDefault(targetApp, new ConcurrentHashMap<String, Boolean>());
                    currentMap.putAll(importMap);
                    HookInit.reaLogPauseMap.put(targetApp, currentMap);
                    break;
                }
            default:
                break;
        }
    }

    // ========== 所有对话框统一复用 HookInit.createBoundedDialog ==========

    private static void showConfirmDialog(Activity activity, String title, String message,
                                          DialogInterface.OnClickListener onConfirm,
                                          DialogInterface.OnClickListener onCancel, HookInit hookInit) {
        AlertDialog dialog = HookInit.createBoundedDialog(activity, title, message,
                                                          new String[]{"确定执行", "返回上一步"},
                                                          new DialogInterface.OnClickListener[]{onConfirm, onCancel});
        try {
            TextView msgView = dialog.findViewById(android.R.id.message);
            if (msgView != null) msgView.setTextIsSelectable(true);
        } catch (Throwable ignored) {}
        dialog.show();
    }

    public static void showExportSelectionDialog(final Activity activity, final String targetApp, final HookInit hookInit) {
        ReaLog.log("config", "显示导出选择对话框");
        // ===== 修改1：过滤掉 permanent_hidden =====
        final Map<String, Boolean> selectedMap = new LinkedHashMap<String, Boolean>();
        LinkedHashMap<String, String> displayMap = new LinkedHashMap<>();
        for (String key : CONFIG_ITEMS.keySet()) {
            if (!"permanent_hidden".equals(key)) {
                selectedMap.put(key, true);
                displayMap.put(key, CONFIG_ITEMS.get(key));
            }
        }

        Button cleanBtn = new Button(activity);
        cleanBtn.setText("清理配置");
        cleanBtn.setPadding(20, 10, 20, 10);
        cleanBtn.setTextColor(0xFFFFFFFF);
        cleanBtn.setBackground(getRoundButtonDrawable(0xAAFF6347));
        cleanBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDeleteConfigDialog(activity, targetApp, hookInit);
                }
            });

        LinearLayout selectionLayout = createSelectionLayout(activity, selectedMap, displayMap, targetApp, true, cleanBtn, null);
        LinearLayout titleLayout = createTitleWithTip(activity, " 选择要导出的配置项", "<b>注：仅导出已勾选的配置！</b>");

        AlertDialog dialog = HookInit.createBoundedDialog(activity, null, null,
                                                          new String[]{"下一步", "取消导出"},
                                                          new DialogInterface.OnClickListener[]{
                                                              new DialogInterface.OnClickListener() {
                                                                  @Override
                                                                  public void onClick(DialogInterface d, int which) {
                                                                      List<String> selectedKeys = getSelectedKeys(selectedMap);
                                                                      if (selectedKeys.isEmpty()) {
                                                                          ToastUtil.show(activity, "请至少选择一项");
                                                                          ReaLog.log("config", "导出取消: 未选择任何项");
                                                                          d.dismiss();
                                                                          showExportSelectionDialog(activity, targetApp, hookInit);
                                                                          return;
                                                                      }
                                                                      String filePath = buildExportFilePath(activity, targetApp);
                                                                      ReaLog.log("config", "用户确定导出，选定 " + selectedKeys.size() + " 项");
                                                                      showExportConfirmDialog(activity, targetApp, selectedKeys, filePath, hookInit);
                                                                      d.dismiss();
                                                                  }
                                                              },
                                                              emptyListener()
                                                          },
                                                          selectionLayout
                                                          );
        dialog.setCustomTitle(titleLayout);
        dialog.show();
    }

    private static void showExportConfirmDialog(final Activity activity, final String targetApp,
                                                final List<String> selectedKeys, final String filePath,
                                                final HookInit hookInit) {
        ReaLog.log("config", "显示导出确认对话框，路径: " + filePath);
        String msg = buildConfirmMessage(selectedKeys, targetApp);
        msg += "<br><br><b>导出路径：</b><font color='#2196F3'>" + filePath + "</font>";

        int total = CONFIG_ITEMS.size();
        int selected = selectedKeys.size();
        int skipped = total - selected;
        msg += "<br><br>即将导出 <b><font color='#FF5722'>" + selected + "</font></b> 项，未勾选/忽略 <b><font color='#FF5722'>" + skipped + "</font></b> 项";
        msg += "<br>确定要 <b><font color='#FF5722'>继续并执行</font></b> 吗？";

        showConfirmDialog(activity, "确认导出以下配置项:", msg,
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface d, int which) {
                    ReaLog.log("config", "用户确认导出");
                    String result = exportConfig(activity, targetApp, selectedKeys);
                    if (result != null && result.startsWith("success:")) {
                        String[] parts = result.split(":");
                        int total = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
                        String path = parts.length > 2 ? parts[2] : filePath;
                        try {
                            ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
                            android.content.ClipData clip = android.content.ClipData.newPlainText("config_path", path);
                            clipboard.setPrimaryClip(clip);
                        } catch (Throwable ignored) {}
                        ToastUtil.show(activity, "✅ 已导出 " + total + " 项配置\n\n成功复制路径:\n" + path);
                        ReaLog.log("config", "导出成功，共 " + total + " 项");
                    } else {
                        ToastUtil.show(activity, "导出失败：" + (result != null ? result : "未知错误"));
                        ReaLog.log("config", "导出失败: " + result);
                    }
                    d.dismiss();
                }
            },
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface d, int which) {
                    ReaLog.log("config", "用户取消导出");
                    d.dismiss();
                    showExportSelectionDialog(activity, targetApp, hookInit);
                }
            },
            hookInit
        );
    }

    public static void showImportSelectionDialog(final Activity activity, final String targetApp,
                                                 final TextView floatingView, final HookInit hookInit) {
        ReaLog.log("config", "显示导入选择对话框");
        File dir = activity.getExternalFilesDir(null);
        if (dir == null) { ToastUtil.show(activity, "无法访问存储"); return; }
        File[] files = dir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File d, String name) {
                    return (name.startsWith("installcf_") && name.endsWith(".json")) ||
                        "install_fake_config.json".equals(name);
                }
            });
        if (files == null || files.length == 0) {
            ToastUtil.show(activity, "未找到配置文件\n请先导出文件");
            ReaLog.log("config", "未找到配置文件");
            return;
        }
        Arrays.sort(files, new Comparator<File>() {
                @Override
                public int compare(File f1, File f2) {
                    return Long.compare(f2.lastModified(), f1.lastModified());
                }
            });
        final List<File> fileList = new ArrayList<File>(Arrays.asList(files));
        ReaLog.log("config", "找到 " + fileList.size() + " 个配置文件");

        LinearLayout mainLayout = new LinearLayout(activity);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(40, 20, 40, 20);

        TextView tipText = new TextView(activity);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            tipText.setText(Html.fromHtml("<b>请选择要<font color='#FF5722'><b>导入</b></font>的配置文件</b>", Html.FROM_HTML_MODE_LEGACY));
        } else {
            tipText.setText(Html.fromHtml("<b>请选择要<font color='#FF5722'><b>导入</b></font>的配置文件</b>"));
        }
        tipText.setTextColor(0xFFFF5722);
        tipText.setTextSize(12);
        tipText.setPadding(0, 0, 0, 15);
        mainLayout.addView(tipText);

        final LinearLayout listContainer = new LinearLayout(activity);
        listContainer.setOrientation(LinearLayout.VERTICAL);
        ScrollView scrollView = new ScrollView(activity);
        scrollView.setVerticalScrollBarEnabled(false);
        scrollView.addView(listContainer);
        mainLayout.addView(scrollView);

        final int[] selectedIndex = new int[]{0};

        for (int i = 0; i < fileList.size(); i++) {
            final int index = i;
            File f = fileList.get(i);
            String fileName = f.getName();
            boolean isOldFormat = "install_fake_config.json".equals(fileName);
            String displayName = fileName + (isOldFormat ? " [旧格式]" : "");

            String timeStr = "";
            if (!isOldFormat) {
                try {
                    String[] parts = fileName.split("_");
                    if (parts.length >= 3) {
                        String ts = parts[parts.length - 1].replace(".json", "");
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
                        Date date = sdf.parse(ts);
                        timeStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(date);
                    }
                } catch (Exception e) {
                    timeStr = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(f.lastModified());
                }
            } else {
                timeStr = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(f.lastModified());
            }

            final LinearLayout itemLayout = new LinearLayout(activity);
            itemLayout.setOrientation(LinearLayout.VERTICAL);
            itemLayout.setPadding(30, 10, 30, 10);
            itemLayout.setBackgroundColor(0x00000000);

            final TextView nameView = new TextView(activity);
            nameView.setText(displayName);
            nameView.setTextColor(0xFF333333);
            nameView.setTextSize(14);
            itemLayout.addView(nameView);

            final TextView timeView = new TextView(activity);
            timeView.setText(timeStr);
            timeView.setTextColor(0xFF888888);
            timeView.setTextSize(11);
            timeView.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
            itemLayout.addView(timeView);

            itemLayout.setClickable(true);
            itemLayout.setFocusable(true);
            itemLayout.setTag(i);
            if (i == 0) {
                itemLayout.setBackgroundColor(0xAA2196F3);
                nameView.setTextColor(0xFFFFFFFF);
                timeView.setTextColor(0xFFDDDDDD);
            }
            itemLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        for (int j = 0; j < listContainer.getChildCount(); j++) {
                            View child = listContainer.getChildAt(j);
                            if (child instanceof LinearLayout) {
                                LinearLayout layout = (LinearLayout) child;
                                layout.setBackgroundColor(0x00000000);
                                TextView nv = (TextView) layout.getChildAt(0);
                                TextView tv = (TextView) layout.getChildAt(1);
                                nv.setTextColor(0xFF333333);
                                tv.setTextColor(0xFF888888);
                            }
                        }
                        itemLayout.setBackgroundColor(0xAA2196F3);
                        nameView.setTextColor(0xFFFFFFFF);
                        timeView.setTextColor(0xFFDDDDDD);
                        selectedIndex[0] = index;
                    }
                });
            listContainer.addView(itemLayout);
            View divider = new View(activity);
            divider.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
            divider.setBackgroundColor(0xFFE0E0E0);
            listContainer.addView(divider);
        }

        AlertDialog dialog = HookInit.createBoundedDialog(activity, "选择配置文件", null,
                                                          new String[]{"确认选择", "取消导入", "清理配置"},
                                                          new DialogInterface.OnClickListener[]{
                                                              new DialogInterface.OnClickListener() {
                                                                  @Override
                                                                  public void onClick(DialogInterface d, int which) {
                                                                      File selectedFile = fileList.get(selectedIndex[0]);
                                                                      ReaLog.log("config", "用户选择导入文件: " + selectedFile.getName());
                                                                      d.dismiss();
                                                                      doImportFile(activity, targetApp, selectedFile, floatingView, hookInit);
                                                                  }
                                                              },
                                                              emptyListener(),
                                                              new DialogInterface.OnClickListener() {
                                                                  @Override
                                                                  public void onClick(DialogInterface d, int which) {
                                                                      showDeleteConfigDialog(activity, targetApp, hookInit);
                                                                  }
                                                              }
                                                          },
                                                          mainLayout
                                                          );
        dialog.show();
    }

    private static void doImportFile(final Activity activity, final String targetApp, final File file,
                                     final TextView floatingView, final HookInit hookInit) {
        ReaLog.log("config", "开始导入文件: " + file.getName());
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            reader.close();

            final JSONObject root = new JSONObject(sb.toString());
            final List<String> packages = getTopLevelPackages(root);
            if (packages.isEmpty()) {
                ToastUtil.show(activity, "配置文件中没有可用包名");
                ReaLog.log("config", "文件中无顶层包名");
                return;
            }

            final String sourcePackage;
            if (packages.size() == 1) {
                sourcePackage = packages.get(0);
                ReaLog.log("config", "自动选择唯一包名: " + sourcePackage);
            } else {
                LinearLayout pkgLayout = new LinearLayout(activity);
                pkgLayout.setOrientation(LinearLayout.VERTICAL);
                pkgLayout.setPadding(40, 20, 40, 20);
                TextView pkgMsg = new TextView(activity);
                pkgMsg.setText("请选择要导入的来源包名：");
                pkgMsg.setTextSize(14);
                pkgMsg.setTextColor(0xFF333333);
                pkgLayout.addView(pkgMsg);

                final String[] pkgArray = packages.toArray(new String[0]);
                final LinearLayout pkgListLayout = new LinearLayout(activity);
                pkgListLayout.setOrientation(LinearLayout.VERTICAL);
                pkgListLayout.setPadding(0, 10, 0, 10);

                final int[] selectedPkgIndex = new int[]{0};
                for (int i = 0; i < pkgArray.length; i++) {
                    final int idx = i;
                    final String pkg = pkgArray[i];
                    final LinearLayout item = new LinearLayout(activity);
                    item.setOrientation(LinearLayout.HORIZONTAL);
                    item.setPadding(20, 10, 20, 10);
                    final TextView pkgTv = new TextView(activity);
                    pkgTv.setText(pkg);
                    pkgTv.setTextSize(14);
                    pkgTv.setTextColor(0xFF333333);
                    item.addView(pkgTv);
                    if (i == 0) item.setBackgroundColor(0xAA2196F3);
                    item.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                for (int j = 0; j < pkgListLayout.getChildCount(); j++) {
                                    View child = pkgListLayout.getChildAt(j);
                                    child.setBackgroundColor(0x00000000);
                                    TextView tv = (TextView) ((LinearLayout) child).getChildAt(0);
                                    tv.setTextColor(0xFF333333);
                                }
                                item.setBackgroundColor(0xAA2196F3);
                                pkgTv.setTextColor(0xFFFFFFFF);
                                selectedPkgIndex[0] = idx;
                            }
                        });
                    pkgListLayout.addView(item);
                    View divider = new View(activity);
                    divider.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
                    divider.setBackgroundColor(0xFFE0E0E0);
                    pkgListLayout.addView(divider);
                }
                pkgLayout.addView(pkgListLayout);

                AlertDialog pkgDialog = HookInit.createBoundedDialog(activity, "选择来源包名", null,
                                                                     new String[]{"确认", "取消"},
                                                                     new DialogInterface.OnClickListener[]{
                                                                         new DialogInterface.OnClickListener() {
                                                                             @Override
                                                                             public void onClick(DialogInterface d, int which) {
                                                                                 String srcPkg = pkgArray[selectedPkgIndex[0]];
                                                                                 ReaLog.log("config", "用户选择来源包名: " + srcPkg);
                                                                                 try {
                                                                                     JSONObject appConfig = root.getJSONObject(srcPkg);
                                                                                     showImportItemsDialog(activity, targetApp, srcPkg, appConfig, floatingView, hookInit, file);
                                                                                 } catch (Throwable t) {
                                                                                     ToastUtil.show(activity, "读取包配置失败");
                                                                                     ReaLog.log("config", "读取包配置异常: " + t.getMessage());
                                                                                 }
                                                                                 d.dismiss();
                                                                             }
                                                                         },
                                                                         emptyListener()
                                                                     },
                                                                     pkgLayout
                                                                     );
                pkgDialog.show();
                return;
            }

            JSONObject appConfig = root.getJSONObject(sourcePackage);
            showImportItemsDialog(activity, targetApp, sourcePackage, appConfig, floatingView, hookInit, file);
        } catch (Throwable t) {
            ReaLog.log("config", "读取文件异常: " + t.getMessage());
            ToastUtil.show(activity, "读取文件失败：" + t.getMessage());
        }
    }

    private static void showImportItemsDialog(final Activity activity, final String targetApp,
                                              final String sourcePackage, final JSONObject appConfig,
                                              final TextView floatingView, final HookInit hookInit,
                                              final File file) {
        ReaLog.log("config", "显示导入项选择对话框，来源包: " + sourcePackage);
        try {
            final List<String> availableKeys = new ArrayList<String>();
            for (String key : CONFIG_ITEMS.keySet()) {
                if (appConfig.has(key)) availableKeys.add(key);
            }
            if (availableKeys.isEmpty()) {
                ToastUtil.show(activity, "该包名下没有可导入的配置项");
                ReaLog.log("config", "来源包无可用配置项");
                return;
            }

            // ===== 修改2：过滤掉 permanent_hidden =====
            final Map<String, Boolean> selectedMap = new LinkedHashMap<String, Boolean>();
            LinkedHashMap<String, String> displayMap = new LinkedHashMap<String, String>();
            for (String key : availableKeys) {
                if (!"permanent_hidden".equals(key)) {
                    selectedMap.put(key, true);
                    displayMap.put(key, CONFIG_ITEMS.get(key));
                }
            }

            LinearLayout selectionLayout = createSelectionLayout(activity, selectedMap, displayMap,
                                                                 null, true, null, appConfig);

            LinearLayout titleLayout = createTitleWithTip(activity, " 选择要导入的配置项",
                                                          "<b>来源: " + sourcePackage + " | 仅导入存在且已勾选的配置！</b>");

            AlertDialog dialog = HookInit.createBoundedDialog(activity, null, null,
                                                              new String[]{"下一步", "返回上一步"},
                                                              new DialogInterface.OnClickListener[]{
                                                                  new DialogInterface.OnClickListener() {
                                                                      @Override
                                                                      public void onClick(DialogInterface d, int which) {
                                                                          List<String> selectedKeys = getSelectedKeys(selectedMap);
                                                                          if (selectedKeys.isEmpty()) {
                                                                              ToastUtil.show(activity, "请至少选择一项");
                                                                              ReaLog.log("config", "导入取消: 未选择任何项");
                                                                              d.dismiss();
                                                                              showImportItemsDialog(activity, targetApp, sourcePackage, appConfig, floatingView, hookInit, file);
                                                                              return;
                                                                          }
                                                                          ReaLog.log("config", "用户选择导入 " + selectedKeys.size() + " 项");
                                                                          showImportConfirmDialog(activity, targetApp, selectedKeys, sourcePackage, floatingView, hookInit, file, appConfig);
                                                                          d.dismiss();
                                                                      }
                                                                  },
                                                                  new DialogInterface.OnClickListener() {
                                                                      @Override
                                                                      public void onClick(DialogInterface d, int which) {
                                                                          ReaLog.log("config", "用户取消导入选择");
                                                                          d.dismiss();
                                                                          showImportSelectionDialog(activity, targetApp, floatingView, hookInit);
                                                                      }
                                                                  }
                                                              },
                                                              selectionLayout
                                                              );
            dialog.setCustomTitle(titleLayout);
            dialog.show();
        } catch (Throwable t) {
            ReaLog.log("config", "配置导入异常: " + t.getMessage());
            ToastUtil.show(activity, "配置导入失败");
        }
    }

    private static void showImportConfirmDialog(final Activity activity, final String targetApp,
                                                final List<String> selectedKeys, final String sourcePackage,
                                                final TextView floatingView, final HookInit hookInit,
                                                final File file, final JSONObject fileAppConfig) {
        String filePath = file.getAbsolutePath();
        ReaLog.log("config", "显示导入确认对话框，来源: " + sourcePackage + ", 导入项: " + selectedKeys.size());

        String configList = buildConfirmMessage(selectedKeys, targetApp, fileAppConfig);
        StringBuilder msg = new StringBuilder();
        msg.append("即将从 <font color='#FF5722'><b>").append(sourcePackage).append("</b></font> 导入以下配置项：<br><br>");
        msg.append(configList);

        int existsCount = 0;
        for (String key : selectedKeys) {
            if (fileAppConfig.has(key)) existsCount++;
        }
        int totalAll = CONFIG_ITEMS.size();
        int notSelected = totalAll - selectedKeys.size();

        msg.append("<br><b>导入路径：</b><font color='#2196F3'>").append(filePath).append("</font>");
        msg.append("<br><br>即将导入 <b><font color='#FF5722'>").append(existsCount).append("</b></font> 项");
        msg.append("，未勾选/不存在 <b><font color='#FF5722'>").append(notSelected).append("</b></font> 项");
        msg.append("<br>确定要 <b><font color='#FF5722'>继续并执行</font></b> 吗？");

        showConfirmDialog(activity, "确认导入", msg.toString(),
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface d, int which) {
                    ReaLog.log("config", "用户确认导入");
                    String result = importConfig(activity, targetApp, selectedKeys, floatingView, hookInit, file);
                    if (result != null && result.startsWith("success:")) {
                        String[] parts = result.split(":");
                        int total = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
                        int success = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
                        int skipped = parts.length > 3 ? Integer.parseInt(parts[3]) : 0;

                        String toastMsg = "总选择配置 " + total + " 项\n已成功覆盖 " + success + " 项";
                        if (skipped > 0) toastMsg += "\n已跳过损坏 " + skipped + " 项";
                        else toastMsg += "\n全部导入成功！";
                        ToastUtil.show(activity, toastMsg);
                        ReaLog.log("config", "导入结果: 成功 " + success + ", 跳过 " + skipped);

                        if (floatingView != null) {
                            String statusText = HookInit.installStatusMap.getOrDefault(targetApp, true) ? "已安装" : "未安装";
                            String blockText = (HookInit.blockExitMap.getOrDefault(targetApp, false) ||
                                HookInit.superBlockExitMap.getOrDefault(targetApp, false)) ? "[拦截]" : "";
                            floatingView.setText("安装防护(" + statusText + ")" + blockText);
                            floatingView.invalidate();
                            floatingView.requestLayout();
                        }
                        hookInit.updateFloatingView(activity);

                        if (result.startsWith("success:") && file.getName().equals("install_fake_config.json")) {
                            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (file.exists() && file.delete()) {
                                            ReaLog.log("config", "已删除旧格式配置文件: " + file.getAbsolutePath());
                                        } else {
                                            ReaLog.log("config", "旧格式配置文件删除失败或已不存在: " + file.getAbsolutePath());
                                        }
                                    }
                                }, 3000);
                        }
                    } else {
                        ToastUtil.show(activity, "导入失败：" + result);
                        ReaLog.log("config", "导入失败: " + result);
                    }
                    d.dismiss();
                }
            },
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface d, int which) {
                    ReaLog.log("config", "用户取消导入");
                    d.dismiss();
                    showImportSelectionDialog(activity, targetApp, floatingView, hookInit);
                }
            },
            hookInit
        );
    }


// 原有 buildConfirmMessage（从内存读取）
    private static String buildConfirmMessage(List<String> keys, String targetApp) {
        return buildConfirmMessage(keys, targetApp, null);
    }

// 新增重载：支持从 JSONObject 读取（用于导入预览）
    private static String buildConfirmMessage(List<String> keys, String targetApp, JSONObject fileAppConfig) {
        StringBuilder msg = new StringBuilder();
        for (String key : keys) {
            String display = CONFIG_ITEMS.get(key);
            if (display == null) display = key;
            // 根据是否有 fileAppConfig 决定数据来源
            String value = (fileAppConfig != null) 
                ? getConfigValueDisplay(targetApp, key, fileAppConfig) 
                : getConfigValueDisplay(targetApp, key);
            if (value == null) value = "未知";
            String color = value.contains("关闭") || value.contains("未安装") || value.contains("禁用") ? "#F44336" : "#4CAF50";
            String coloredValue = colorizeNumericValues(value);
            msg.append("• ").append(display).append("：<b><font color='").append(color).append("'>").append(coloredValue).append("</font></b><br>");
        }
        return msg.toString();
    }

    private static String colorizeNumericValues(String value) {
        if (TextUtils.isEmpty(value)) return value;
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+(?:\\.\\d+)?(?:/\\d+(?:\\.\\d+)?)?)");
        java.util.regex.Matcher matcher = pattern.matcher(value);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "<font color='#FF5722'><b>" + matcher.group(1) + "</b></font>");
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static LinearLayout createTitleWithTip(Activity activity, String title, String tip) {
        LinearLayout layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 10);
        TextView titleView = new TextView(activity);
        titleView.setText(title);
        titleView.setTextSize(18);
        titleView.setTextColor(0xFF333333);
        titleView.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        layout.addView(titleView);
        if (tip != null && !tip.isEmpty()) {
            TextView tipView = new TextView(activity);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                tipView.setText(Html.fromHtml(tip, Html.FROM_HTML_MODE_LEGACY));
            } else {
                tipView.setText(Html.fromHtml(tip));
            }
            tipView.setTextColor(0xFFFF5722);
            tipView.setTextSize(12);
            tipView.setPadding(0, 5, 0, 0);
            layout.addView(tipView);
        }
        return layout;
    }

    public static void showDeleteConfigDialog(final Activity activity, final String targetApp, final HookInit hookInit) {
        ReaLog.log("config", "显示删除配置对话框");
        File dir = activity.getExternalFilesDir(null);
        if (dir == null) { ToastUtil.show(activity, "无法访问存储"); return; }
        File[] files = dir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File d, String name) {
                    return (name.startsWith("installcf_") && name.endsWith(".json")) ||
                        "install_fake_config.json".equals(name);
                }
            });
        if (files == null || files.length == 0) {
            ToastUtil.show(activity, "没有可删除的配置文件");
            ReaLog.log("config", "没有可删除的配置文件");
            return;
        }
        Arrays.sort(files, new Comparator<File>() {
                @Override
                public int compare(File f1, File f2) {
                    return Long.compare(f2.lastModified(), f1.lastModified());
                }
            });

        LinearLayout mainLayout = new LinearLayout(activity);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(40, 20, 40, 20);

        TextView tip = new TextView(activity);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            tip.setText(Html.fromHtml("<b>请选择要<font color='#FF5722'><b>删除</b></font>的配置文件：</b>", Html.FROM_HTML_MODE_LEGACY));
        } else {
            tip.setText(Html.fromHtml("<b>请选择要<font color='#FF5722'><b>删除</b></font>的配置文件：</b>"));
        }
        tip.setTextColor(0xFF333333);
        tip.setTextSize(14);
        tip.setPadding(0, 0, 0, 15);
        mainLayout.addView(tip);

        final LinearLayout listContainer = new LinearLayout(activity);
        listContainer.setOrientation(LinearLayout.VERTICAL);
        ScrollView scrollView = new ScrollView(activity);
        scrollView.setVerticalScrollBarEnabled(false);
        scrollView.addView(listContainer);
        mainLayout.addView(scrollView);

        final List<File> fileList = Arrays.asList(files);
        final Map<Integer, Boolean> selectedMap = new HashMap<Integer, Boolean>();
        for (int i = 0; i < fileList.size(); i++) selectedMap.put(i, false);

        for (int i = 0; i < fileList.size(); i++) {
            final int index = i;
            File f = fileList.get(i);
            String fileName = f.getName();
            boolean isOldFormat = "install_fake_config.json".equals(fileName);
            String displayName = fileName + (isOldFormat ? " [旧格式]" : "");

            String timeStr = "";
            if (!isOldFormat) {
                try {
                    String[] parts = fileName.split("_");
                    if (parts.length >= 3) {
                        String ts = parts[parts.length - 1].replace(".json", "");
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
                        Date date = sdf.parse(ts);
                        timeStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(date);
                    }
                } catch (Exception e) {
                    timeStr = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(f.lastModified());
                }
            } else {
                timeStr = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(f.lastModified());
            }

            LinearLayout itemLayout = new LinearLayout(activity);
            itemLayout.setOrientation(LinearLayout.VERTICAL);
            itemLayout.setPadding(10, 5, 10, 5);
            itemLayout.setBackgroundColor(0x00000000);

            final CheckBox cb = new CheckBox(activity);
            cb.setText(displayName);
            cb.setTextSize(14);
            cb.setTextColor(0xFF333333);
            itemLayout.addView(cb);

            TextView timeView = new TextView(activity);
            timeView.setText(timeStr);
            timeView.setTextColor(0xFF888888);
            timeView.setTextSize(11);
            timeView.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
            timeView.setPadding(40, 0, 0, 0);
            itemLayout.addView(timeView);

            cb.setTag(index);
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        selectedMap.put(index, isChecked);
                    }
                });
            itemLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cb.setChecked(!cb.isChecked());
                    }
                });
            listContainer.addView(itemLayout);
            View divider = new View(activity);
            divider.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
            divider.setBackgroundColor(0xFFE0E0E0);
            listContainer.addView(divider);
        }

        AlertDialog dialog = HookInit.createBoundedDialog(activity, "删除已导出配置", null,
                                                          new String[]{"确认删除", "取消删除"},
                                                          new DialogInterface.OnClickListener[]{
                                                              new DialogInterface.OnClickListener() {
                                                                  @Override
                                                                  public void onClick(DialogInterface d, int which) {
                                                                      List<File> toDelete = new ArrayList<File>();
                                                                      for (Map.Entry<Integer, Boolean> entry : selectedMap.entrySet()) {
                                                                          if (entry.getValue()) toDelete.add(fileList.get(entry.getKey()));
                                                                      }
                                                                      if (toDelete.isEmpty()) {
                                                                          ToastUtil.show(activity, "请至少选择一个文件");
                                                                          d.dismiss();
                                                                          showDeleteConfigDialog(activity, targetApp, hookInit);
                                                                          return;
                                                                      }
                                                                      int success = 0;
                                                                      for (File f : toDelete) {
                                                                          if (f.delete()) success++;
                                                                          ReaLog.log("config", "删除文件: " + f.getName());
                                                                      }
                                                                      ToastUtil.show(activity, "成功删除 " + success + "/" + toDelete.size() + " 个配置文件");
                                                                      ReaLog.log("config", "删除完成，成功 " + success + " 个");
                                                                      d.dismiss();
                                                                  }
                                                              },
                                                              emptyListener()
                                                          },
                                                          mainLayout
                                                          );
        dialog.show();
    }

    private static String buildExportFilePath(Activity activity, String targetApp) {
        File dir = activity.getExternalFilesDir(null);
        if (dir == null) return null;
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        return new File(dir, "installcf_" + targetApp + "_" + timeStamp + ".json").getAbsolutePath();
    }

    public static boolean deleteConfig(Activity activity) {
        try {
            File dir = activity.getExternalFilesDir(null);
            if (dir == null) return false;
            File[] files = dir.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File d, String name) {
                        return name.startsWith("installcf_") && name.endsWith(".json");
                    }
                });
            if (files == null || files.length == 0) return false;
            boolean success = true;
            for (File f : files) {
                if (!f.delete()) success = false;
                else ReaLog.log("config", "删除配置文件: " + f.getName());
            }
            return success;
        } catch (Throwable t) {
            return false;
        }
    }

    private static List<String> getTopLevelPackages(JSONObject root) throws Exception {
        List<String> packages = new ArrayList<String>();
        Iterator<String> keys = root.keys();
        while (keys.hasNext()) packages.add(keys.next());
        return packages;
    }

    private static void writeJsonToFile(File file, JSONObject root) throws Exception {
        OutputStream os = new FileOutputStream(file);
        OutputStreamWriter writer = new OutputStreamWriter(os, StandardCharsets.UTF_8);
        writer.write(root.toString(2));
        writer.flush();
        writer.close();
        os.close();
    }

    private static List<String> jsonArrayToStringList(JSONArray array) throws Exception {
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < array.length(); i++) {
            String item = array.getString(i);
            if (item != null && !item.isEmpty()) list.add(item);
        }
        return list;
    }

    private static JSONArray stringListToJsonArray(List<String> list) {
        JSONArray array = new JSONArray();
        for (String item : list) {
            if (item != null && !item.isEmpty()) array.put(item);
        }
        return array;
    }

    private static LinearLayout createSelectionLayout(Activity activity, final Map<String, Boolean> selectedMap,
                                                      LinkedHashMap<String, String> displayMap,
                                                      final String targetApp, final boolean showStatus, View extraButton,
                                                      final JSONObject fileAppConfig) {
        LinearLayout mainLayout = new LinearLayout(activity);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(40, 30, 40, 30);

        LinearLayout topBar = new LinearLayout(activity);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setGravity(Gravity.CENTER_VERTICAL);

        final Button toggleAllBtn = new Button(activity);
        toggleAllBtn.setText("全选");
        toggleAllBtn.setPadding(20, 10, 20, 10);
        toggleAllBtn.setTextColor(0xFFFFFFFF);
        toggleAllBtn.setBackground(getRoundButtonDrawable(0xFF2196F3));
        LinearLayout.LayoutParams toggleParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        toggleParams.rightMargin = 20;
        toggleAllBtn.setLayoutParams(toggleParams);
        topBar.addView(toggleAllBtn);

        if (extraButton != null) topBar.addView(extraButton);
        mainLayout.addView(topBar);

        final ScrollView scrollView = new ScrollView(activity);
        scrollView.setVerticalScrollBarEnabled(false);
        scrollView.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
        final LinearLayout listContainer = new LinearLayout(activity);
        listContainer.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(listContainer);
        mainLayout.addView(scrollView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));

        final List<CheckBox> checkBoxList = new ArrayList<CheckBox>();
        final Map<CheckBox, String> baseTextMap = new HashMap<CheckBox, String>();

        final Runnable updateSelectAllButtonState = new Runnable() {
            @Override
            public void run() {
                boolean allChecked = true;
                for (CheckBox cb : checkBoxList) {
                    if (!cb.isChecked()) { allChecked = false; break; }
                }
                if (allChecked) {
                    toggleAllBtn.setText("取消全选");
                    toggleAllBtn.setBackground(getRoundButtonDrawable(0xAAFF6347));
                } else {
                    toggleAllBtn.setText("全选");
                    toggleAllBtn.setBackground(getRoundButtonDrawable(0xFF2196F3));
                }
            }
        };

        for (final Map.Entry<String, String> entry : displayMap.entrySet()) {
            final String key = entry.getKey();
            final String displayName = entry.getValue();
            String baseText = displayName;
            String state = "";
            if (showStatus) {
                state = getConfigValueDisplay(targetApp, key, fileAppConfig);
                if (state != null && !state.isEmpty()) baseText = displayName + "：";
            }
            final String finalBaseText = baseText;
            final String finalState = state;

            final CheckBox cb = new CheckBox(activity);
            boolean checked = selectedMap.getOrDefault(key, true);
            updateCheckBoxText(cb, finalBaseText, finalState, checked, showStatus);
            cb.setChecked(checked);
            cb.setPadding(20, 10, 20, 10);
            cb.setTextSize(14);
            cb.setTag(key);
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        selectedMap.put(key, isChecked);
                        String newState = getConfigValueDisplay(targetApp, key, fileAppConfig);
                        updateCheckBoxText(cb, finalBaseText, newState, isChecked, showStatus);
                        updateSelectAllButtonState.run();
                    }
                });
            listContainer.addView(cb);
            checkBoxList.add(cb);
            baseTextMap.put(cb, finalBaseText);
            View divider = new View(activity);
            divider.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
            divider.setBackgroundColor(0xFFE0E0E0);
            listContainer.addView(divider);
        }

        toggleAllBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean allChecked = true;
                    for (CheckBox cb : checkBoxList) {
                        if (!cb.isChecked()) { allChecked = false; break; }
                    }
                    boolean target = !allChecked;
                    for (CheckBox cb : checkBoxList) {
                        cb.setChecked(target);
                        String key = (String) cb.getTag();
                        selectedMap.put(key, target);
                        String state = getConfigValueDisplay(targetApp, key, fileAppConfig);
                        updateCheckBoxText(cb, baseTextMap.get(cb), state, target, showStatus);
                    }
                    updateSelectAllButtonState.run();
                }
            });
        updateSelectAllButtonState.run();
        return mainLayout;
    }

    private static void updateCheckBoxText(CheckBox cb, String baseText, String state, boolean checked, boolean showStatus) {
        String text;
        if (checked) {
            // 选中状态：baseText 强制黑色，state 保持原有颜色（绿/红）
            if (showStatus && state != null && !state.isEmpty()) {
                String color = state.contains("关闭") || state.contains("未安装") || state.contains("禁用") ? "#F44336" : "#4CAF50";
                text = "<font color='#000000'>" + baseText + "</font><b><font color='" + color + "'>" + state + "</font></b>";
            } else {
                text = "<font color='#000000'>" + baseText + "</font>";
            }
        } else {
            // 未选中状态：整体红色加粗 + (已禁用) 小字
            String content = baseText + (showStatus && state != null ? state : "");
            text = "<b><font color='#F44336'>" + content + " </font><font color='#FF5722'><small>(已禁用)</small></font></b>";
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            cb.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY));
        } else {
            cb.setText(Html.fromHtml(text));
        }
    }

    private static List<String> getSelectedKeys(Map<String, Boolean> selectedMap) {
        List<String> list = new ArrayList<String>();
        for (Map.Entry<String, Boolean> entry : selectedMap.entrySet()) {
            if (entry.getValue()) list.add(entry.getKey());
        }
        return list;
    }

    private static String getConfigValueDisplay(String targetApp, String key, JSONObject fileAppConfig) {
        if (fileAppConfig != null) {
            try {
                switch (key) {
                    case "install_status": return fileAppConfig.optBoolean("install_status", true) ? "已安装" : "未安装";
                    case "floating_shown": return fileAppConfig.optBoolean("floating_shown", true) ? "显示" : "隐藏";
                    case "floating_x": {
                            String val = fileAppConfig.optString("floating_x", "null");
                            return "null".equals(val) ? "未设置" : val;
                        }
                    case "floating_y": {
                            String val = fileAppConfig.optString("floating_y", "null");
                            return "null".equals(val) ? "未设置" : val;
                        }
                    case "user_disabled_auto_block": return fileAppConfig.optBoolean("user_disabled_auto_block", false) ? "已禁用" : "启用";
                    case "block_exit": return fileAppConfig.optBoolean("block_exit", false) ? "开启" : "关闭";
                    case "super_block_exit": return fileAppConfig.optBoolean("super_block_exit", false) ? "开启" : "关闭";
                    case "permission_fake": return fileAppConfig.optBoolean("permission_fake", true) ? "开启" : "关闭";
                    case "launch_intercept": return fileAppConfig.optBoolean("launch_intercept", true) ? "开启" : "关闭";
                    case "selinux_fake": return fileAppConfig.optBoolean("selinux_fake", true) ? "开启" : "关闭";
                    case "vendor_enabled": return fileAppConfig.optBoolean("vendor_enabled", false) ? "开启" : "关闭";
                    case "force_default_back": return fileAppConfig.optBoolean("force_default_back", false) ? "模块级" : "自定义";
                    case "crash_protect_enabled": return fileAppConfig.optBoolean("crash_protect_enabled", true) ? "开启" : "关闭";
                    case "anti_detection_detail": {
                            JSONObject detail = fileAppConfig.optJSONObject("anti_detection_detail");
                            int total = detail != null ? detail.length() : 0;
                            int count = 0;
                            if (detail != null) {
                                Iterator<String> it = detail.keys();
                                while (it.hasNext()) {
                                    if (detail.optBoolean(it.next(), false)) count++;
                                }
                            }
                            return count + "/" + total + " 项";
                        }
                    case "vpn_fake_detail": {
                            JSONObject detail = fileAppConfig.optJSONObject("vpn_fake_detail");
                            int total = detail != null ? detail.length() : 0;
                            int count = 0;
                            if (detail != null) {
                                Iterator<String> it = detail.keys();
                                while (it.hasNext()) {
                                    if (detail.optBoolean(it.next(), false)) count++;
                                }
                            }
                            return count + "/" + total + " 项";
                        }
                    case "permission_fake_detail": {
                            JSONObject detail = fileAppConfig.optJSONObject("permission_fake_detail");
                            return (detail != null ? detail.length() : 0) + " 项";
                        }
                    case "share_fake_detail": {
                            JSONObject detail = fileAppConfig.optJSONObject("share_fake_detail");
                            int total = detail != null ? detail.length() : 4;
                            int count = 0;
                            if (detail != null) {
                                Iterator<String> it = detail.keys();
                                while (it.hasNext()) {
                                    if (detail.optBoolean(it.next(), false)) count++;
                                }
                            }
                            return count + "/" + total + " 项";
                        }
                    case "user_defined_packages": {
                            JSONArray arr = fileAppConfig.optJSONArray("user_defined_packages");
                            return (arr != null ? arr.length() : 0) + " 个包";
                        }
                    case "excluded_packages": {
                            JSONArray arr = fileAppConfig.optJSONArray("excluded_packages");
                            return (arr != null ? arr.length() : 0) + " 个包";
                        }
                    case "package_configs": {
                            JSONArray arr = fileAppConfig.optJSONArray("package_configs");
                            return (arr != null ? arr.length() : 0) + " 个配置";
                        }
                    case "auto_actions": {
                            JSONObject obj = fileAppConfig.optJSONObject("auto_actions");
                            return (obj != null ? obj.length() : 0) + " 条记录";
                        }
                    case "auto_records": {
                            JSONObject obj = fileAppConfig.optJSONObject("auto_records");
                            return (obj != null ? obj.length() : 0) + " 条记录";
                        }
                    case "intercept_patterns": {
                            JSONArray arr = fileAppConfig.optJSONArray("intercept_patterns");
                            return (arr != null ? arr.length() : 0) + " 条记录";
                        }
                    case "rea_log_pause": {
                            if (fileAppConfig != null) {
                                JSONObject pauseObj = fileAppConfig.optJSONObject("rea_log_pause");
                                if (pauseObj != null) {
                                    int total = pauseObj.length();
                                    int paused = 0;
                                    Iterator<String> keys = pauseObj.keys();
                                    while (keys.hasNext()) {
                                        String k = keys.next();
                                        if (pauseObj.optBoolean(k, false)) paused++;
                                    }
                                    int recording = total - paused;
                                    return "记录中:" + recording + "项, 已暂停:" + paused + "项";
                                } else {
                                    return "未设置";
                                }
                            } else {
                                if (targetApp == null) return "未设置";
                                Map<String, Boolean> map = HookInit.reaLogPauseMap.get(targetApp);
                                if (map == null || map.isEmpty()) return "未设置";
                                int total = map.size();
                                int paused = 0;
                                for (Boolean v : map.values()) if (v) paused++;
                                int recording = total - paused;
                                return "记录中:" + recording + "项, 已暂停:" + paused + "项";
                            }
                        }
                    default: return "未知";
                }
            } catch (Throwable t) { return "读取失败"; }
        } else {
            try {
                switch (key) {
                    case "install_status": return HookInit.installStatusMap.getOrDefault(targetApp, true) ? "已安装" : "未安装";
                    case "floating_shown": return HookInit.floatingShownMap.getOrDefault(targetApp, true) ? "显示" : "隐藏";
                    case "floating_x": {
                            Float f = HookInit.floatingXMap.get(targetApp);
                            return f != null ? String.valueOf(f) : "未设置";
                        }
                    case "floating_y": {
                            Float f = HookInit.floatingYMap.get(targetApp);
                            return f != null ? String.valueOf(f) : "未设置";
                        }
                    case "user_disabled_auto_block": return HookInit.userDisabledAutoBlockMap.getOrDefault(targetApp, false) ? "已禁用" : "启用";
                    case "block_exit": return HookInit.blockExitMap.getOrDefault(targetApp, false) ? "开启" : "关闭";
                    case "super_block_exit": return HookInit.superBlockExitMap.getOrDefault(targetApp, false) ? "开启" : "关闭";
                    case "permission_fake": return HookInit.permissionFakeMap.getOrDefault(targetApp, true) ? "开启" : "关闭";
                    case "launch_intercept": return HookInit.launchInterceptMap.getOrDefault(targetApp, true) ? "开启" : "关闭";
                    case "selinux_fake": return HookInit.selinuxFakeMap.getOrDefault(targetApp, true) ? "开启" : "关闭";
                    case "vendor_enabled": {
                            Boolean v = HookInit.vendorChoiceMap.get(targetApp);
                            return v != null && v ? "开启" : "关闭";
                        }
                    case "force_default_back": return HookInit.forceDefaultBackMap.getOrDefault(targetApp, false) ? "模块级" : "自定义";
                    case "crash_protect_enabled": return HookInit.crashProtectEnabledMap.getOrDefault(targetApp, true) ? "开启" : "关闭";
                    case "anti_detection_detail": {
                            Map<String, Boolean> map = HookInit.antiDetectionDetailMap.getOrDefault(targetApp, new HashMap<String, Boolean>());
                            int count = 0;
                            for (Boolean b : map.values()) if (b) count++;
                            return count + "/" + map.size() + " 项";
                        }
                    case "vpn_fake_detail": {
                            Map<String, Boolean> map = HookInit.vpnFakeDetailMap.getOrDefault(targetApp, new HashMap<String, Boolean>());
                            int count = 0;
                            for (Boolean b : map.values()) if (b) count++;
                            return count + "/" + map.size() + " 项";
                        }
                    case "permission_fake_detail": {
                            Map<String, Boolean> map = HookInit.permissionFakeDetailMap.getOrDefault(targetApp, new HashMap<String, Boolean>());
                            int count = 0;
                            for (Boolean b : map.values()) if (b) count++;
                            return count + " 项";
                        }
                    case "share_fake_detail": {
                            Map<String, Boolean> map = HookInit.shareFakeDetailMap.getOrDefault(targetApp, HookInit.getDefaultShareDetailMap());
                            int count = 0;
                            for (Boolean b : map.values()) if (b) count++;
                            return count + "/" + map.size() + " 项";
                        }
                    case "user_defined_packages": {
                            List<String> list = HookInit.userDefinedPackagesMap.getOrDefault(targetApp, new ArrayList<String>());
                            return list.size() + " 个包";
                        }
                    case "excluded_packages": {
                            List<String> list = HookInit.excludedPackagesMap.getOrDefault(targetApp, new ArrayList<String>());
                            return list.size() + " 个包";
                        }
                    case "package_configs": {
                            List<PackageConfig> list = HookInit.packageConfigMap.getOrDefault(targetApp, new ArrayList<PackageConfig>());
                            return list.size() + " 个配置";
                        }
                    case "auto_actions": {
                            Map<String, String> map = HookInit.autoActionMap.getOrDefault(targetApp, new HashMap<String, String>());
                            return map.size() + " 条记录";
                        }
                    case "auto_records": {
                            Map<String, List<String>> map = HookInit.autoChoiceRecordsMap.getOrDefault(targetApp, new HashMap<String, List<String>>());
                            return map.size() + " 条记录";
                        }
                    case "intercept_patterns": {
                            List<InterceptPattern> list = HookInit.interceptPatternsMap.getOrDefault(targetApp, new ArrayList<InterceptPattern>());
                            return list.size() + " 条记录";
                        }
                    case "rea_log_pause": {
                            if (targetApp == null) return "未设置";
                            Map<String, Boolean> map = HookInit.reaLogPauseMap.get(targetApp);
                            if (map == null || map.isEmpty()) {
                                return "未设置";
                            }
                            int total = map.size();
                            int paused = 0;
                            for (Boolean v : map.values()) if (v) paused++;
                            int recording = total - paused;
                            return "记录中:" + recording + "项, 已暂停:" + paused + "项";
                        }
                    default: return "未知";
                }
            } catch (Throwable t) { return "读取失败"; }
        }
    }

    private static String getConfigValueDisplay(String targetApp, String key) {
        return getConfigValueDisplay(targetApp, key, null);
    }

    private static android.graphics.drawable.Drawable getRoundButtonDrawable(int color) {
        android.graphics.drawable.GradientDrawable drawable = new android.graphics.drawable.GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(25f);
        return drawable;
    }
}
