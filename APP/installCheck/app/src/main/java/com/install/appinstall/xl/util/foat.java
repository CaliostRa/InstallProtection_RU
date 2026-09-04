package com.install.appinstall.xl.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import com.install.appinstall.xl.HookInit;
import com.install.appinstall.xl.util.DebugModeManager;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.GradientDrawable;

import java.lang.ref.WeakReference;

public class foat {
    private final HookInit hookInit;
    private TextView currentFloatingView;
    private Handler floatingTopHandler;
    private static long lastVolumeDownTime = 0;
    private static long lastVolumeUpTime = 0;
    private static final int DOUBLE_CLICK_TIMEOUT = 600;
    private static final int DEBOUNCE_TIMEOUT = 150;

    private long lastVolumeClickTime = 0;
    private int volumeClickCount = 0;
    private static final int TRIPLE_CLICK_TIMEOUT = 600;

    private static long lastKeyDownTime = 0;

    private ViewGroup mCurrentDecorView;
    private View mCurrentFloatingView;
    private boolean mAddViewHookInstalled = false;

    private long lastPermanentHiddenToastTime = 0;
    private static final int PERMANENT_HIDDEN_TOAST_INTERVAL = 2000;

    private boolean hasVibratePermissionWarned = false;

    // ========== 自动收边相关 ==========
    private Handler autoHideHandler = new Handler(Looper.getMainLooper());
    private Runnable autoHideRunnable;
    private boolean isCollapsed = false;
    private boolean isAnimating = false; 
    private float fullWidth, fullHeight;
    private float fullX, fullY;
    private float collapsedWidthPx;
    private float collapsedX, collapsedY;
    private boolean snapToRight = false;
    private boolean isDragging = false;
    private static final long AUTO_HIDE_DELAY = 8000; // 8秒无操作收边

    public foat(HookInit hookInit) {
        this.hookInit = hookInit;
    }

    // ========== 重置收边状态（供外部调用） ==========
    public void resetCollapsedState() {
        isCollapsed = false;
        isAnimating = false;
    }

    // ========== 音量键重置 ==========
    public void resetVolumeKeyForHide() {
        long now = System.currentTimeMillis();
        lastVolumeDownTime = now - 1000;
        lastVolumeUpTime = now - 1000;
        lastKeyDownTime = 0;
        volumeClickCount = 0;
        lastVolumeClickTime = 0;
        ReaLog.log("floating", "音量键时间戳/计数已重置, now=" + now);
    }

    // ========== 临时/永久隐藏 ==========
    public void tempHideFloatingView(final Activity activity, final View floatingView) {
        if (DebugModeManager.isDebugModeActive()) {
            AlertDialog dialog = HookInit.createBoundedDialog(
                activity,
                "调试模式冲突",
                "当前处于调试模式，无法隐藏悬浮窗。<br><br>" +
                "您可以选择关闭调试模式后再隐藏，或取消操作。",
                new String[]{"关闭调试模式并隐藏", "取消"},
                new DialogInterface.OnClickListener[]{
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface d, int which) {
                            DebugModeManager.disableDebugMode();
                            d.dismiss();
                            performTempHide(activity, floatingView);
                            hookInit.showRestartConfirmDialog(activity);
                        }
                    },
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface d, int which) {
                            d.dismiss();
                        }
                    }
                }
            );
            dialog.show();
            return;
        }
        performTempHide(activity, floatingView);
    }

    public void hideFloatingView(final Activity activity, final View floatingView) {
        if (DebugModeManager.isDebugModeActive()) {
            AlertDialog dialog = HookInit.createBoundedDialog(
                activity,
                "调试模式冲突",
                "当前处于调试模式，无法隐藏悬浮窗。<br><br>" +
                "您可以选择关闭调试模式后再隐藏，或取消操作。",
                new String[]{"关闭调试模式并隐藏", "取消"},
                new DialogInterface.OnClickListener[]{
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface d, int which) {
                            DebugModeManager.disableDebugMode();
                            d.dismiss();
                            performPermanentHide(activity, floatingView);
                            hookInit.showRestartConfirmDialog(activity);
                        }
                    },
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface d, int which) {
                            d.dismiss();
                        }
                    }
                }
            );
            dialog.show();
            return;
        }
        performPermanentHide(activity, floatingView);
    }

    private void performTempHide(Activity activity, View floatingView) {
        try {
            if (floatingView != null) {
                ViewGroup parent = (ViewGroup) floatingView.getParent();
                if (parent != null) parent.removeView(floatingView);
            }
            String targetApp = hookInit.getCurrentTargetApp();
            HookInit.floatingShownMap.put(targetApp, false);
            HookInit.permanentHiddenMap.put(targetApp, false);
            currentFloatingView = null;
            mCurrentFloatingView = null;
            mCurrentDecorView = null;
            stop定时置顶();
            isCollapsed = false;
            isAnimating = false;
            resetVolumeKeyForHide();
            ReaLog.log("floating", "用户隐藏悬浮窗(临时)，双击音量键恢复");
            ToastUtil.showUnique(activity, "✅ 悬浮窗临时隐藏\n可双击音量键恢复");
        } catch (Throwable t) {
            ReaLog.log("floating", "临时隐藏悬浮窗异常: " + t.getMessage());
            ToastUtil.showUnique(activity, "临时隐藏失败");
        }
    }

    private void performPermanentHide(Activity activity, View floatingView) {
        try {
            if (floatingView != null) {
                ViewGroup parent = (ViewGroup) floatingView.getParent();
                if (parent != null) parent.removeView(floatingView);
            }
            String targetApp = hookInit.getCurrentTargetApp();
            HookInit.floatingShownMap.put(targetApp, false);
            HookInit.permanentHiddenMap.put(targetApp, true);
            currentFloatingView = null;
            mCurrentFloatingView = null;
            mCurrentDecorView = null;
            stop定时置顶();
            isCollapsed = false;
            isAnimating = false;
            hookInit.saveConfigToFile();
            ReaLog.log("floating", "用户隐藏悬浮窗(永久)，三击音量键恢复");
            ToastUtil.showUnique(activity, "✅ 悬浮窗永久隐藏\n三击音量键恢复");
            resetVolumeKeyForHide();
        } catch (Throwable t) {
            ReaLog.log("floating", "永久隐藏悬浮窗异常: " + t.getMessage());
            ToastUtil.showUnique(activity, "永久隐藏失败");
        }
    }

    // ========== 恢复永久隐藏 ==========
    private void restorePermanentHidden(Activity activity) {
        Activity targetActivity = activity;
        if (targetActivity == null || targetActivity.isFinishing()) {
            Activity current = hookInit.getCurrentResumedActivity();
            if (current != null && !current.isFinishing()) {
                targetActivity = current;
            } else {
                ReaLog.log("floating", "恢复永久隐藏失败：没有有效的 Activity");
                return;
            }
        }
        final Activity finalActivity = targetActivity;

        String targetApp = hookInit.getCurrentTargetApp();
        Boolean permanent = HookInit.permanentHiddenMap.get(targetApp);
        if (permanent != null && permanent) {
            HookInit.permanentHiddenMap.put(targetApp, false);
            HookInit.floatingShownMap.put(targetApp, true);
            hookInit.saveConfigToFile();

            finalActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showFloatingView(finalActivity);
                    }
                });

            ToastUtil.showUnique(finalActivity, "永久隐藏已恢复\n悬浮窗显示");
            ReaLog.log("floating", "三击音量键恢复永久隐藏");
            vibrate(finalActivity);
            resetVolumeKeyForHide();
        } else {
            ToastUtil.showUnique(finalActivity, "当前未永久隐藏");
        }
    }

    private void showPermanentHiddenToast(Activity activity) {
        long now = System.currentTimeMillis();
        if (now - lastPermanentHiddenToastTime > PERMANENT_HIDDEN_TOAST_INTERVAL) {
            ToastUtil.showUnique(activity, "悬浮窗永久隐藏中\n三击音量键恢复");
            lastPermanentHiddenToastTime = now;
        }
    }

    // ========== 音量键双击/三击 ==========
    public void initVolumeKeyDoubleClick(ClassLoader classLoader) {
        XposedHelpers.findAndHookMethod(Activity.class, "dispatchKeyEvent", KeyEvent.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Activity activity = (Activity) param.thisObject;
                    KeyEvent event = (KeyEvent) param.args[0];
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        int keyCode = event.getKeyCode();
                        long currentTime = System.currentTimeMillis();

                        if (currentTime - lastKeyDownTime < DEBOUNCE_TIMEOUT) {
                            ReaLog.log("floating", "防抖忽略重复按键，diff=" + (currentTime - lastKeyDownTime));
                            return;
                        }
                        lastKeyDownTime = currentTime;

                        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                            if (currentTime - lastVolumeClickTime < TRIPLE_CLICK_TIMEOUT) {
                                volumeClickCount++;
                            } else {
                                volumeClickCount = 1;
                            }
                            lastVolumeClickTime = currentTime;

                            if (volumeClickCount >= 3) {
                                volumeClickCount = 0;
                                Boolean permanent = HookInit.permanentHiddenMap.get(hookInit.getCurrentTargetApp());
                                if (permanent != null && permanent) {
                                    restorePermanentHidden(activity);
                                } else {
                                    ReaLog.log("floating", "三击音量键，但当前未永久隐藏，忽略");
                                }
                            }
                        }

                        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                            long diff = currentTime - lastVolumeDownTime;
                            if (diff < DOUBLE_CLICK_TIMEOUT) {
                                handleDoubleClick(activity);
                            }
                            lastVolumeDownTime = currentTime;
                        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                            long diff = currentTime - lastVolumeUpTime;
                            if (diff < DOUBLE_CLICK_TIMEOUT) {
                                handleDoubleClick(activity);
                            }
                            lastVolumeUpTime = currentTime;
                        }

                        // 调试模式触发（仅当悬浮窗存在时）
                        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                            if (currentFloatingView != null) {
                                DebugModeManager.onVolumeKeyPressed(keyCode, activity);
                            }
                        }
                    }
                }
            });
    }

    private void handleDoubleClick(Activity activity) {
        String targetApp = hookInit.getCurrentTargetApp();
        Boolean permanent = HookInit.permanentHiddenMap.get(targetApp);
        if (permanent != null && permanent) {
            showPermanentHiddenToast(activity);
            ReaLog.log("floating", "双击音量键，但当前为永久隐藏，忽略");
            return;
        }

        Boolean currentlyShown = HookInit.floatingShownMap.get(targetApp);
        if (currentlyShown == null) currentlyShown = true;

        if (!currentlyShown) {
            HookInit.floatingShownMap.put(targetApp, true);
            HookInit.permanentHiddenMap.put(targetApp, false);
            showFloatingView(activity);
            ToastUtil.showUnique(activity, "悬浮窗 已恢复显示(临时)");
            ReaLog.log("floating", "用户通过双击恢复临时隐藏");
            vibrate(activity);
        } else {
            ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
            View floatingView = decorView.findViewWithTag("install_fake_floating");
            if (floatingView != null) {
                bringFloatingToFront(activity, (TextView) floatingView, decorView);
                start定时置顶(activity, decorView);
                ReaLog.log("floating", "置顶悬浮窗");
                vibrate(activity);
            } else {
                showFloatingView(activity);
            }
        }
    }

    private void bringFloatingToFront(Activity activity, TextView floatingView, ViewGroup decorView) {
        if (activity.isFinishing() || floatingView == null || decorView == null) return;
        floatingView.bringToFront();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            floatingView.setElevation(999999999f);
            floatingView.setTranslationZ(999999999f);
        }
    }

    // ========== Activity生命周期Hook ==========
    public void hookActivityLifecycle(ClassLoader classLoader) {
        try {
            XposedHelpers.findAndHookMethod("android.app.Activity", classLoader, "onCreate", Bundle.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        try {
                            final Activity activity = (Activity) param.thisObject;
                            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            String targetApp = hookInit.getCurrentTargetApp();
                                            Boolean shouldShow = HookInit.floatingShownMap.get(targetApp);
                                            Boolean permanent = HookInit.permanentHiddenMap.get(targetApp);
                                            if (permanent != null && permanent) shouldShow = false;
                                            if (shouldShow == null || shouldShow) {
                                                showFloatingView(activity);
                                            }
                                        } catch (Throwable t) {}
                                    }
                                }, 850);
                        } catch (Throwable t) {}
                    }
                }
            );

            XposedHelpers.findAndHookMethod("android.app.Activity", classLoader, "onResume",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        try {
                            Activity activity = (Activity) param.thisObject;
                            hookInit.setCurrentResumedActivity(activity);
                            String targetApp = hookInit.getCurrentTargetApp();
                            Boolean permanent = HookInit.permanentHiddenMap.get(targetApp);
                            Boolean shouldShow = HookInit.floatingShownMap.get(targetApp);
                            if (permanent != null && permanent) shouldShow = false;
                            if (shouldShow == null) shouldShow = true;
                            ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
                            View floatingView = decorView.findViewWithTag("install_fake_floating");
                            if (floatingView != null) {
                                decorView.removeView(floatingView);
                            }
                            if (shouldShow) {
                                showFloatingView(activity);
                            }
                        } catch (Throwable t) {}
                    }
                }
            );

            XposedHelpers.findAndHookMethod("android.app.Activity", classLoader, "onPause",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        try {
                            Activity activity = (Activity) param.thisObject;
                            Activity current = hookInit.getCurrentResumedActivity();
                            if (current == activity) {
                                hookInit.setCurrentResumedActivity(null);
                            }
                        } catch (Throwable t) {}
                    }
                }
            );

            XposedHelpers.findAndHookMethod("android.app.Activity", classLoader, "onDestroy",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        try {
                            Activity activity = (Activity) param.thisObject;
                            Activity current = hookInit.getCurrentResumedActivity();
                            if (current == activity) {
                                hookInit.setCurrentResumedActivity(null);
                            }
                            ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
                            View floatingView = decorView.findViewWithTag("install_fake_floating");
                            if (floatingView != null) {
                                decorView.removeView(floatingView);
                                ReaLog.log("floating", "Activity销毁时移除悬浮窗");
                            }
                            if (currentFloatingView != null && currentFloatingView.getParent() == null) {
                                currentFloatingView = null;
                            }
                            if (mCurrentDecorView == decorView) {
                                mCurrentDecorView = null;
                                mCurrentFloatingView = null;
                            }
                            if (currentFloatingView != null) {
                                currentFloatingView.animate().cancel();
                            }
                        } catch (Throwable t) {}
                    }
                }
            );
        } catch (Throwable t) {}
    }

    // ========== 置顶Hook ==========
    private void hookDecorViewAddView() {
        if (mAddViewHookInstalled) return;
        try {
            XposedBridge.hookAllMethods(ViewGroup.class, "addView", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if (mCurrentDecorView == null || mCurrentFloatingView == null) return;
                        ViewGroup target = (ViewGroup) param.thisObject;
                        if (target.getRootView() == mCurrentDecorView) {
                            try {
                                mCurrentFloatingView.bringToFront();
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                                    mCurrentFloatingView.setElevation(999999999f);
                                    mCurrentFloatingView.setTranslationZ(999999999f);
                                }
                            } catch (Throwable ignored) {}
                        }
                    }
                });
            mAddViewHookInstalled = true;
            ReaLog.log("floating", "悬浮窗置顶已启用");
        } catch (Throwable t) {
            ReaLog.log("floating", "悬浮窗置顶失败: " + t.getMessage());
        }
    }

    // ========== 显示悬浮窗 ==========
    public void showFloatingView(final Activity activity) {
        try {
            String targetApp = hookInit.getCurrentTargetApp();
            Boolean permanent = HookInit.permanentHiddenMap.get(targetApp);
            Boolean shouldShow = HookInit.floatingShownMap.get(targetApp);
            if (permanent != null && permanent) shouldShow = false;
            if (shouldShow == null) shouldShow = true;
            if (!shouldShow) {
                ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
                View existingView = decorView.findViewWithTag("install_fake_floating");
                if (existingView != null) decorView.removeView(existingView);
                return;
            }

            final ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
            View oldView = decorView.findViewWithTag("install_fake_floating");
            if (oldView != null) {
                decorView.removeView(oldView);
                if (currentFloatingView == oldView) currentFloatingView = null;
                if (mCurrentFloatingView == oldView) mCurrentFloatingView = null;
                if (mCurrentDecorView == decorView) mCurrentDecorView = null;
            }

            activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (activity.isFinishing() || activity.isDestroyed()) return;

                            final TextView floatingView = createFloatingView(activity);
                            if (floatingView == null) return;

                            decorView.addView(floatingView, new ViewGroup.LayoutParams(
                                                  ViewGroup.LayoutParams.WRAP_CONTENT,
                                                  ViewGroup.LayoutParams.WRAP_CONTENT));
                            ReaLog.log("floating", "悬浮窗已创建并显示");

                            mCurrentDecorView = decorView;
                            mCurrentFloatingView = floatingView;
                            currentFloatingView = floatingView;
                            hookDecorViewAddView();

                            floatingView.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            String targetApp = hookInit.getCurrentTargetApp();
                                            Float savedX = HookInit.floatingXMap.get(targetApp);
                                            Float savedY = HookInit.floatingYMap.get(targetApp);
                                            int screenWidth = decorView.getWidth();
                                            int screenHeight = decorView.getHeight();
                                            int viewWidth = floatingView.getWidth();
                                            int viewHeight = floatingView.getHeight();
                                            if (viewWidth == 0) viewWidth = 200;
                                            if (viewHeight == 0) viewHeight = 80;
                                            float x, y;
                                            if (savedX != null && savedY != null && savedX >= 0 && savedY >= 0) {
                                                x = savedX;
                                                y = savedY;
                                            } else {
                                                x = screenWidth - viewWidth - 50;
                                                y = 200;
                                            }
                                            if (screenWidth > 0 && screenHeight > 0) {
                                                x = Math.max(10, Math.min(x, screenWidth - viewWidth - 10));
                                                y = Math.max(150, Math.min(y, screenHeight - viewHeight - 250));
                                            }
                                            floatingView.setX(x);
                                            floatingView.setY(y);
                                            HookInit.floatingXMap.put(targetApp, x);
                                            HookInit.floatingYMap.put(targetApp, y);
                                            // 如果是收边状态，立即应用收边
                                            if (isCollapsed) {
                                                floatingView.post(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            if (currentFloatingView != null && isCollapsed) {
                                                                applyCollapsedStyle(activity);
                                                            }
                                                        }
                                                    });
                                            }
                                        } catch (Throwable t) {}
                                    }
                                });

                            updateFloatingToTop(activity, floatingView, decorView);
                            start定时置顶(activity, decorView);
                        } catch (Throwable t) {
                            ReaLog.log("floating", "悬浮窗UI异常: " + t.getMessage());
                        }
                    }
                });
        } catch (Throwable t) {
            ReaLog.log("floating", "悬浮窗View异常: " + t.getMessage());
        }
    }

    // ========== 定时置顶 ==========
    public void start定时置顶(final Activity activity, final ViewGroup decorView) {
        stop定时置顶();
        final WeakReference<Activity> activityRef = new WeakReference<>(activity);

        floatingTopHandler = new Handler(Looper.getMainLooper());
        floatingTopHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Activity act = activityRef.get();
                    if (act == null || act.isFinishing() || currentFloatingView == null) {
                        stop定时置顶();
                        return;
                    }
                    updateFloatingToTop(act, currentFloatingView, decorView);
                    if (isCollapsed && currentFloatingView != null) {
                        animateCollapsedToEdge(act);
                    }
                    floatingTopHandler.postDelayed(this, 3000);
                }
            }, 3000);

        // 自动收边计时（仅当未收边且悬浮窗可见时）
        if (!isCollapsed && currentFloatingView != null && !isAnimating) {
            autoHideHandler.removeCallbacks(autoHideRunnable);
            autoHideRunnable = new Runnable() {
                @Override
                public void run() {
                    Activity act = activityRef.get();
                    if (act == null || act.isFinishing() || currentFloatingView == null || isCollapsed || isAnimating) return;
                    collapseFloatingView(act);
                }
            };
            autoHideHandler.postDelayed(autoHideRunnable, AUTO_HIDE_DELAY);
        }
    }

    public void stop定时置顶() {
        if (floatingTopHandler != null) {
            floatingTopHandler.removeCallbacksAndMessages(null);
            floatingTopHandler = null;
        }
        autoHideHandler.removeCallbacks(autoHideRunnable);
        if (currentFloatingView != null) {
            currentFloatingView.animate().cancel();
        }
    }

    // ========== 置顶刷新 ==========
    public void updateFloatingToTop(Activity activity, TextView floatingView, ViewGroup decorView) {
        if (activity.isFinishing() || floatingView == null || decorView == null) return;
        floatingView.bringToFront();
        floatingView.requestLayout();
        floatingView.invalidate();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            floatingView.setElevation(999999999f);
            floatingView.setTranslationZ(999999999f);
        }
    }

    // ========== 更新悬浮窗文本 ==========
    public void updateFloatingTextOnly() {
        if (currentFloatingView == null) return;
        currentFloatingView.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        String targetApp = hookInit.getCurrentTargetApp();
                        Boolean currentStatus = HookInit.installStatusMap.get(targetApp);
                        boolean status = currentStatus != null ? currentStatus : true;
                        if (isCollapsed) {
                            int bgColor = status ? 0xAA4CAF50 : 0xAAF44336;
                            GradientDrawable gd = new GradientDrawable();
                            gd.setColor(bgColor);
                            gd.setCornerRadius(25f);
                            currentFloatingView.setBackground(gd);
                            return;
                        }
                        String statusText = status ? "已安装" : "未安装";
                        boolean blockExit = HookInit.blockExitMap.getOrDefault(targetApp, false);
                        boolean superBlock = HookInit.superBlockExitMap.getOrDefault(targetApp, false);
                        String blockText = (blockExit || superBlock) ? "[拦截]" : "";
                        currentFloatingView.setText("安装防护(" + statusText + ")" + blockText);
                        int bgColor = status ? 0xAA4CAF50 : 0xAAF44336;
                        GradientDrawable gd = new GradientDrawable();
                        gd.setColor(bgColor);
                        gd.setCornerRadius(25f);
                        currentFloatingView.setBackground(gd);
                    } catch (Throwable t) {}
                }
            });
    }

    public void updateFloatingView(final Activity activity) {
        if (activity == null) {
            updateFloatingTextOnly();
            return;
        }
        activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String targetApp = hookInit.getCurrentTargetApp();
                        Boolean permanent = HookInit.permanentHiddenMap.get(targetApp);
                        Boolean shouldShow = HookInit.floatingShownMap.get(targetApp);
                        if (permanent != null && permanent) shouldShow = false;
                        if (shouldShow == null) shouldShow = true;
                        View existingView = activity.getWindow().getDecorView().findViewWithTag("install_fake_floating");
                        if (!shouldShow) {
                            if (existingView != null) {
                                ((ViewGroup) existingView.getParent()).removeView(existingView);
                                if (currentFloatingView == existingView) currentFloatingView = null;
                                stop定时置顶();
                                isCollapsed = false;
                                isAnimating = false;
                            }
                            return;
                        }
                        if (existingView == null) {
                            showFloatingView(activity);
                            return;
                        }
                        if (existingView instanceof TextView) {
                            TextView tv = (TextView) existingView;
                            Boolean currentStatus = HookInit.installStatusMap.get(targetApp);
                            boolean status = currentStatus != null ? currentStatus : true;
                            if (isCollapsed) {
                                int bgColor = status ? 0xAA4CAF50 : 0xAAF44336;
                                GradientDrawable gd = new GradientDrawable();
                                gd.setColor(bgColor);
                                gd.setCornerRadius(25f);
                                tv.setBackground(gd);
                                return;
                            }
                            String statusText = status ? "已安装" : "未安装";
                            boolean blockExit = HookInit.blockExitMap.getOrDefault(targetApp, false);
                            boolean superBlock = HookInit.superBlockExitMap.getOrDefault(targetApp, false);
                            String blockText = (blockExit || superBlock) ? "[拦截]" : "";
                            tv.setText("安装防护(" + statusText + ")" + blockText);
                            int bgColor = status ? 0xAA4CAF50 : 0xAAF44336;
                            GradientDrawable gd = new GradientDrawable();
                            gd.setColor(bgColor);
                            gd.setCornerRadius(25f);
                            tv.setBackground(gd);
                        }
                    } catch (Throwable t) {
                        ReaLog.log("floating", "悬浮窗更新异常: " + t.getMessage());
                    }
                }
            });
    }

    // ========== 创建悬浮窗View ==========
    private TextView createFloatingView(final Activity activity) {
        try {
            final TextView floatingView = new com.install.appinstall.xl.ru.RuTextView(activity);
            floatingView.setTag("install_fake_floating");
            String targetApp = hookInit.getCurrentTargetApp();
            Boolean currentStatus = HookInit.installStatusMap.get(targetApp);
            final boolean status = currentStatus != null ? currentStatus : true;
            String statusText = status ? "已安装" : "未安装";
            boolean blockExit = HookInit.blockExitMap.getOrDefault(targetApp, false);
            boolean superBlock = HookInit.superBlockExitMap.getOrDefault(targetApp, false);
            String blockText = (blockExit || superBlock) ? "[拦截]" : "";
            floatingView.setText("安装防护(" + statusText + ")" + blockText);
            floatingView.setTextSize(14);
            floatingView.setTextColor(0xFFFFFFFF);
            floatingView.setPadding(25, 15, 25, 15);
            floatingView.setGravity(Gravity.CENTER);
            int bgColor = status ? 0xAA4CAF50 : 0xAAF44336;
            GradientDrawable gd = new GradientDrawable();
            gd.setColor(bgColor);
            gd.setCornerRadius(25f);
            floatingView.setBackground(gd);

            // ========== 触摸事件 ==========
            floatingView.setOnTouchListener(new View.OnTouchListener() {
                    private float startX, startY, initialX, initialY;
                    private boolean longPressTriggered = false;
                    private boolean isClickHandled = false;
                    private Handler longPressHandler;
                    private long firstClickTime = 0;
                    private int clickCount = 0;
                    private Handler clickHandler = new Handler();
                    private static final int DOUBLE_CLICK_DELAY = 300;
                    private boolean isDragging = false;

                    @Override
                    public boolean onTouch(final View v, MotionEvent event) {
                        try {
                            // ========== 收边状态 ==========
                            if (isCollapsed) {
                                switch (event.getAction()) {
                                    case MotionEvent.ACTION_DOWN:
                                        stop定时置顶();
                                        startX = event.getRawX();
                                        startY = event.getRawY();
                                        initialX = v.getX();
                                        initialY = v.getY();
                                        isDragging = false;
                                        // 清理旧任务
                                        clickHandler.removeCallbacksAndMessages(null);
                                        if (longPressHandler != null) longPressHandler.removeCallbacksAndMessages(null);
                                        return true;

                                    case MotionEvent.ACTION_MOVE:
                                        float dx = event.getRawX() - startX;
                                        float dy = event.getRawY() - startY;
                                        if (Math.abs(dx) > 20 || Math.abs(dy) > 20) {
                                            isDragging = true;
                                        }
                                        if (isDragging) {
                                            float newX = initialX + dx;
                                            float newY = initialY + dy;
                                            ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
                                            int screenWidth = decorView.getWidth();
                                            int screenHeight = decorView.getHeight();
                                            float viewWidth = (collapsedWidthPx > 0) ? collapsedWidthPx : 60 * activity.getResources().getDisplayMetrics().density;
                                            float viewHeight = v.getHeight();
                                            newX = Math.max(10, Math.min(newX, screenWidth - viewWidth - 10));
                                            newY = Math.max(150, Math.min(newY, screenHeight - viewHeight - 250));
                                            v.setX(newX);
                                            v.setY(newY);
                                            snapToRight = (newX + viewWidth / 2) > screenWidth / 2;
                                            collapsedX = newX;
                                            collapsedY = newY;
                                            String targetApp = hookInit.getCurrentTargetApp();
                                            HookInit.floatingXMap.put(targetApp, newX);
                                            HookInit.floatingYMap.put(targetApp, newY);
                                        }
                                        return true;

                                    case MotionEvent.ACTION_UP:
                                        if (!isDragging) {
                                            // 单击展开
                                            expandFloatingView(activity);
                                        } else {
                                            // 拖动结束，保存位置并重新计时
                                            ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
                                            start定时置顶(activity, decorView);
                                            hookInit.saveConfigToFile();
                                            isDragging = false;
                                        }
                                        return true;

                                    case MotionEvent.ACTION_CANCEL:
                                        isDragging = false;
                                        return true;
                                }
                                return true;
                            }

                            // ========== 展开状态 ==========
                            switch (event.getAction()) {
                                case MotionEvent.ACTION_DOWN:
                                    // 清理旧任务和状态
                                    clickHandler.removeCallbacksAndMessages(null);
                                    if (longPressHandler != null) longPressHandler.removeCallbacksAndMessages(null);
                                    longPressTriggered = false;
                                    isClickHandled = false;

                                    stop定时置顶();
                                    long currentTime = System.currentTimeMillis();
                                    if (currentTime - firstClickTime < DOUBLE_CLICK_DELAY) {
                                        clickCount++;
                                        clickHandler.removeCallbacksAndMessages(null);
                                        isClickHandled = false;
                                        if (clickCount == 2) {
                                            clickCount = 0;
                                            firstClickTime = 0;
                                            ReaLog.log("system", "双击悬浮窗打开添加包名");
                                            hookInit.showAddPackageDialog(activity);
                                            return true;
                                        }
                                    } else {
                                        clickCount = 1;
                                        firstClickTime = currentTime;
                                    }
                                    clickHandler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (clickCount == 1 && !longPressTriggered && !isDragging && !isClickHandled) {
                                                    isClickHandled = true;
                                                    ReaLog.log("system", "单击悬浮窗打开状态切换");
                                                    hookInit.showStatusSwitchDialog(activity, floatingView);
                                                    clickCount = 0;
                                                    firstClickTime = 0;
                                                }
                                            }
                                        }, DOUBLE_CLICK_DELAY);

                                    longPressHandler = new Handler();
                                    longPressHandler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                clickHandler.removeCallbacksAndMessages(null);
                                                longPressTriggered = true;
                                                isClickHandled = true;
                                                clickCount = 0;
                                                firstClickTime = 0;
                                                if (hookInit.statusSwitchDialog != null && hookInit.statusSwitchDialog.isShowing()) {
                                                    hookInit.statusSwitchDialog.dismiss();
                                                    hookInit.statusSwitchDialog = null;
                                                }
                                                ReaLog.log("system", "长按悬浮窗打开管理设置");
                                                hookInit.showHideDialog(activity, v);
                                            }
                                        }, 300);

                                    startX = event.getRawX();
                                    startY = event.getRawY();
                                    initialX = v.getX();
                                    initialY = v.getY();
                                    isDragging = false;
                                    return true;

                                case MotionEvent.ACTION_MOVE:
                                    float deltaX = Math.abs(event.getRawX() - startX);
                                    float deltaY = Math.abs(event.getRawY() - startY);
                                    if (!isDragging && (deltaX > 80 || deltaY > 80)) { // 提高阈值，减少误触
                                        isDragging = true;
                                        clickHandler.removeCallbacksAndMessages(null);
                                        if (longPressHandler != null) longPressHandler.removeCallbacksAndMessages(null);
                                        isClickHandled = true;
                                        clickCount = 0;
                                        firstClickTime = 0;
                                    }
                                    if (isDragging) {
                                        float newX = initialX + (event.getRawX() - startX);
                                        float newY = initialY + (event.getRawY() - startY);
                                        ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
                                        int screenWidth = decorView.getWidth();
                                        int screenHeight = decorView.getHeight();
                                        int viewWidth = v.getWidth();
                                        int viewHeight = v.getHeight();
                                        if (viewWidth == 0) viewWidth = 200;
                                        if (viewHeight == 0) viewHeight = 80;
                                        newX = Math.max(10, Math.min(newX, screenWidth - viewWidth - 10));
                                        newY = Math.max(150, Math.min(newY, screenHeight - viewHeight - 250));
                                        v.setX(newX);
                                        v.setY(newY);
                                        String targetApp = hookInit.getCurrentTargetApp();
                                        HookInit.floatingXMap.put(targetApp, newX);
                                        HookInit.floatingYMap.put(targetApp, newY);
                                    }
                                    return true;

                                case MotionEvent.ACTION_UP:
                                    if (longPressHandler != null) longPressHandler.removeCallbacksAndMessages(null);
                                    if (longPressTriggered) {
                                        clickHandler.removeCallbacksAndMessages(null);
                                        isClickHandled = true;
                                    }
                                    if (isDragging) {
                                        String targetApp = hookInit.getCurrentTargetApp();
                                        HookInit.floatingXMap.put(targetApp, v.getX());
                                        HookInit.floatingYMap.put(targetApp, v.getY());
                                        hookInit.saveConfigToFile();
                                        ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
                                        start定时置顶(activity, decorView);
                                    } else {
                                        ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
                                        start定时置顶(activity, decorView);
                                    }
                                    isDragging = false;
                                    return true;

                                case MotionEvent.ACTION_CANCEL:
                                    clickHandler.removeCallbacksAndMessages(null);
                                    if (longPressHandler != null) longPressHandler.removeCallbacksAndMessages(null);
                                    isDragging = false;
                                    longPressTriggered = false;
                                    isClickHandled = true;
                                    clickCount = 0;
                                    firstClickTime = 0;
                                    ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
                                    start定时置顶(activity, decorView);
                                    return true;
                            }
                        } catch (Throwable t) {
                            ReaLog.log("floating", "触摸事件异常: " + t.getMessage());
                        }
                        return true;
                    }
                });

            currentFloatingView = floatingView;
            return floatingView;
        } catch (Throwable t) {
            ReaLog.log("floating", "悬浮窗创建异常: " + t.getMessage());
            return null;
        }
    }

    // ========== 收边动画 ==========
    private void animateCollapse(final View view, float fromWidth, float toWidth,
                                 float fromX, float toX, float toY, final Runnable onEnd) {
        ValueAnimator anim = ValueAnimator.ofFloat(0, 1);
        anim.setDuration(300);
        anim.setInterpolator(new DecelerateInterpolator());

        final float startX = fromX;
        final float startWidth = fromWidth;
        final float endX = toX;
        final float endWidth = toWidth;
        final float targetY = toY;

        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float fraction = animation.getAnimatedFraction();
                    float currentWidth = startWidth + (endWidth - startWidth) * fraction;
                    float currentX = startX + (endX - startX) * fraction;

                    ViewGroup.LayoutParams lp = view.getLayoutParams();
                    lp.width = (int) currentWidth;
                    view.setLayoutParams(lp);
                    view.setX(currentX);
                    view.setY(targetY);
                }
            });
        if (onEnd != null) {
            anim.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        onEnd.run();
                    }
                });
        }
        anim.start();
    }

    private void animateExpand(final View view, float fromWidth, float toWidth,
                               float fromX, float toX, float toY, final Runnable onEnd) {
        ValueAnimator anim = ValueAnimator.ofFloat(0, 1);
        anim.setDuration(300);
        anim.setInterpolator(new DecelerateInterpolator());

        final float startX = fromX;
        final float startWidth = fromWidth;
        final float endX = toX;
        final float endWidth = toWidth;
        final float targetY = toY;

        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float fraction = animation.getAnimatedFraction();
                    float currentWidth = startWidth + (endWidth - startWidth) * fraction;
                    float currentX = startX + (endX - startX) * fraction;

                    ViewGroup.LayoutParams lp = view.getLayoutParams();
                    lp.width = (int) currentWidth;
                    view.setLayoutParams(lp);
                    view.setX(currentX);
                    view.setY(targetY);
                }
            });
        if (onEnd != null) {
            anim.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        onEnd.run();
                    }
                });
        }
        anim.start();
    }

    /**
     * 收边状态下平滑移动到边缘（用于定时器循环）
     */
    private void animateCollapsedToEdge(Activity activity) {
        if (currentFloatingView == null || activity.isFinishing() || !isCollapsed || isAnimating) return;

        ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
        int screenWidth = decorView.getWidth();
        int screenHeight = decorView.getHeight();
        float density = activity.getResources().getDisplayMetrics().density;
        float margin = 8 * density;
        int targetWidth = (int)(60 * density);

        // 确保宽度正确
        ViewGroup.LayoutParams lp = currentFloatingView.getLayoutParams();
        if (lp.width != targetWidth) {
            lp.width = targetWidth;
            currentFloatingView.setLayoutParams(lp);
        }

        float currentX = currentFloatingView.getX();
        float currentY = currentFloatingView.getY();
        // 根据当前中心点判断吸附方向
        float centerX = currentX + targetWidth / 2;
        snapToRight = centerX > screenWidth / 2;

        float targetX = snapToRight ? screenWidth - targetWidth - margin : margin;
        float targetY = Math.max(150, Math.min(currentY, screenHeight - currentFloatingView.getHeight() - 250));

        // 如果已经非常接近边缘（误差 < 5px），则直接设置并跳过动画
        if (Math.abs(currentX - targetX) < 5 && Math.abs(currentY - targetY) < 5) {
            return;
        }

        // 执行滑动动画
        ValueAnimator anim = ValueAnimator.ofFloat(0, 1);
        anim.setDuration(300);
        anim.setInterpolator(new DecelerateInterpolator());
        final float startX = currentX;
        final float startY = currentY;
        final float endX = targetX;
        final float endY = targetY;

        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float fraction = animation.getAnimatedFraction();
                    float curX = startX + (endX - startX) * fraction;
                    float curY = startY + (endY - startY) * fraction;
                    currentFloatingView.setX(curX);
                    currentFloatingView.setY(curY);
                }
            });
        anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    // 更新存储位置
                    String targetApp = hookInit.getCurrentTargetApp();
                    HookInit.floatingXMap.put(targetApp, endX);
                    HookInit.floatingYMap.put(targetApp, endY);
                    collapsedX = endX;
                    collapsedY = endY;
                }
            });
        anim.start();
    }

    // ========== 收边与展开核心 ==========
    private void collapseFloatingView(final Activity activity) {
        if (currentFloatingView == null || activity.isFinishing() || isCollapsed || isAnimating) return;
        isAnimating = true;

        fullWidth = currentFloatingView.getWidth();
        fullHeight = currentFloatingView.getHeight();
        fullX = currentFloatingView.getX();
        fullY = currentFloatingView.getY();

        float density = activity.getResources().getDisplayMetrics().density;
        collapsedWidthPx = 60 * density;

        ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
        int screenWidth = decorView.getWidth();
        int screenHeight = decorView.getHeight();

        float centerX = fullX + fullWidth / 2;
        snapToRight = centerX > screenWidth / 2;

        float margin = 8 * density;
        final float targetX = snapToRight ? screenWidth - collapsedWidthPx - margin : margin;
        final float targetY = Math.max(150, Math.min(fullY, screenHeight - fullHeight - 250));

        collapsedX = targetX;
        collapsedY = targetY;

        animateCollapse(
            currentFloatingView,
            fullWidth,
            collapsedWidthPx,
            fullX,
            targetX,
            targetY,
            new Runnable() {
                @Override
                public void run() {
                    currentFloatingView.setText("安防");
                    currentFloatingView.setTextSize(12);
                    isCollapsed = true;
                    isAnimating = false;
                    String targetApp = hookInit.getCurrentTargetApp();
                    HookInit.floatingXMap.put(targetApp, targetX);
                    HookInit.floatingYMap.put(targetApp, targetY);
                    hookInit.saveConfigToFile();
                    start定时置顶(activity, (ViewGroup) activity.getWindow().getDecorView());
                    ReaLog.log("floating", "悬浮窗：自动吸附");
                }
            }
        );
    }

    private void expandFloatingView(final Activity activity) {
        if (currentFloatingView == null || activity.isFinishing() || !isCollapsed || isAnimating) return;
        isAnimating = true;

        final ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
        int screenWidth = decorView.getWidth();
        int screenHeight = decorView.getHeight();
        float density = activity.getResources().getDisplayMetrics().density;
        float margin = 8 * density;

        // 使用当前视图位置作为基准
        final float currentX = currentFloatingView.getX();
        final float currentY = currentFloatingView.getY();
        final float currentWidth = currentFloatingView.getWidth();

        // 根据当前中心点判断吸附方向
        snapToRight = (currentX + currentWidth / 2) > screenWidth / 2;

        // 计算展开目标 X
        final float targetX;
        if (snapToRight) {
            targetX = screenWidth - fullWidth - margin;
        } else {
            targetX = margin;
        }
        final float targetY = Math.max(150, Math.min(currentY, screenHeight - fullHeight - 250));

        // 执行展开动画
        animateExpand(
            currentFloatingView,
            currentWidth,
            fullWidth,
            currentX,
            targetX,
            targetY,
            new Runnable() {
                @Override
                public void run() {
                    // 恢复文本和样式
                    String targetApp = hookInit.getCurrentTargetApp();
                    Boolean currentStatus = HookInit.installStatusMap.get(targetApp);
                    boolean status = currentStatus != null ? currentStatus : true;
                    String statusText = status ? "已安装" : "未安装";
                    boolean blockExit = HookInit.blockExitMap.getOrDefault(targetApp, false);
                    boolean superBlock = HookInit.superBlockExitMap.getOrDefault(targetApp, false);
                    String blockText = (blockExit || superBlock) ? "[拦截]" : "";
                    currentFloatingView.setText("安装防护(" + statusText + ")" + blockText);
                    currentFloatingView.setTextSize(14);
                    currentFloatingView.setPadding(25, 15, 25, 15);
                    currentFloatingView.setGravity(Gravity.CENTER);
                    int bgColor = status ? 0xAA4CAF50 : 0xAAF44336;
                    GradientDrawable gd = new GradientDrawable();
                    gd.setColor(bgColor);
                    gd.setCornerRadius(25f);
                    currentFloatingView.setBackground(gd);

                    // 更新保存的位置（展开位置）
                    HookInit.floatingXMap.put(targetApp, targetX);
                    HookInit.floatingYMap.put(targetApp, targetY);
                    hookInit.saveConfigToFile();

                    isCollapsed = false;
                    isAnimating = false;
                    start定时置顶(activity, decorView);
                    ReaLog.log("floating", "悬浮窗：恢复展开");
                }
            }
        );
    }

    // ========== 调整收边位置（横竖屏适配） ==========
    private void adjustCollapsedPosition(Activity activity) {
        if (currentFloatingView == null || activity.isFinishing() || !isCollapsed) return;
        ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
        int screenWidth = decorView.getWidth();
        int screenHeight = decorView.getHeight();
        float density = activity.getResources().getDisplayMetrics().density;

        // 确保宽度正确
        int targetWidth = (int)(60 * density);
        ViewGroup.LayoutParams lp = currentFloatingView.getLayoutParams();
        if (lp.width != targetWidth) {
            lp.width = targetWidth;
            currentFloatingView.setLayoutParams(lp);
        }

        float margin = 8 * density;
        float targetX = snapToRight ? screenWidth - targetWidth - margin : margin;
        float targetY = Math.max(150, Math.min(collapsedY, screenHeight - currentFloatingView.getHeight() - 250));

        currentFloatingView.setX(targetX);
        currentFloatingView.setY(targetY);
        collapsedX = targetX;
        collapsedY = targetY;
        String targetApp = hookInit.getCurrentTargetApp();
        HookInit.floatingXMap.put(targetApp, targetX);
        HookInit.floatingYMap.put(targetApp, targetY);
    }

    // ========== 强制应用收边样式 ==========
    private void applyCollapsedStyle(Activity activity) {
        if (currentFloatingView == null || activity.isFinishing()) return;
        if (isCollapsed) {
            float density = activity.getResources().getDisplayMetrics().density;
            collapsedWidthPx = 60 * density;
            ViewGroup.LayoutParams lp = currentFloatingView.getLayoutParams();
            lp.width = (int) collapsedWidthPx;
            currentFloatingView.setLayoutParams(lp);
            currentFloatingView.setText("安防");
            currentFloatingView.setTextSize(12);
            adjustCollapsedPosition(activity);
        } else {
            collapseFloatingView(activity);
        }
    }

    // ========== 获取当前悬浮窗 ==========
    public TextView getCurrentFloatingView() {
        return currentFloatingView;
    }

    // ========== 震动 ==========
    private void vibrate(Activity activity) {
        try {
            if (activity == null) return;
            Vibrator vibrator = (Vibrator) activity.getSystemService(android.content.Context.VIBRATOR_SERVICE);
            if (vibrator == null) return;
            if (!vibrator.hasVibrator()) return;
            vibrator.vibrate(50);
        } catch (SecurityException e) {
            if (!hasVibratePermissionWarned) {
                hasVibratePermissionWarned = true;
            }
        } catch (Throwable t) {}
    }
}
