package com.install.appinstall.xl.util;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import org.json.JSONObject;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Collection;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class ShareHook {
    public interface BooleanProvider { boolean get(); }

    public static void hookWeChatShareOnly(ClassLoader classLoader, String targetPackage, BooleanProvider wechatEnabledGetter) {
        hookWeChatShare(classLoader, targetPackage, wechatEnabledGetter);
    }
    public static void hookQQShareOnly(ClassLoader classLoader, BooleanProvider qqEnabledGetter) {
        hookQQShare(classLoader, qqEnabledGetter);
        hookQQCallbacks(classLoader, qqEnabledGetter);
    }

    private static void hookWeChatShare(final ClassLoader classLoader, final String targetPackage, final BooleanProvider enabledGetter) {
        try {
            Class<?> wxApiImplClass = XposedHelpers.findClassIfExists("com.tencent.mm.opensdk.openapi.BaseWXApiImplV10", classLoader);
            if (wxApiImplClass == null) wxApiImplClass = XposedHelpers.findClassIfExists("com.tencent.mm.opensdk.openapi.WXApiImplV10", classLoader);
            if (wxApiImplClass != null) {
                XposedHelpers.findAndHookMethod(wxApiImplClass, "sendReq", "com.tencent.mm.opensdk.modelbase.BaseReq", new XC_MethodReplacement() {
                        @Override protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                            if (!enabledGetter.get()) return XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);
                            showToast("[分享]微信成功");
                            ReaLog.log("share_fake", "微信分享假装成功");
                            Object resp = buildWechatSuccessResp(classLoader);
                            if (resp != null) {
                                Object handler = findWXAPIEventHandler(classLoader);
                                if (handler != null) XposedHelpers.callMethod(handler, "onResp", resp);
                                else callOnRespViaTempActivity(classLoader, resp, targetPackage);
                            }
                            return true;
                        }
                    });
            }
            Class<?> baseRespClass = XposedHelpers.findClassIfExists("com.tencent.mm.opensdk.modelbase.BaseResp", classLoader);
            if (baseRespClass != null) {
                XposedHelpers.findAndHookMethod(baseRespClass, "getErrCode", new XC_MethodHook() {
                        @Override protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            if (enabledGetter.get()) { param.setResult(0); ReaLog.log("share_fake", "微信虚假信息创建成功"); }
                        }
                    });
            }
        } catch (Throwable t) {}
    }

    private static Object findWXAPIEventHandler(ClassLoader classLoader) {
        try { Class<?> handlerInterface = XposedHelpers.findClass("com.tencent.mm.opensdk.openapi.IWXAPIEventHandler", classLoader);
            List<Activity> activities = getAllActivities();
            for (Activity act : activities) if (handlerInterface.isInstance(act)) return act;
        } catch (Throwable t) {}
        return null;
    }

    private static void callOnRespViaTempActivity(ClassLoader classLoader, Object resp, String targetPackage) {
        String entryName = targetPackage + ".wxapi.WXEntryActivity";
        Object tempActivity = null;
        try { Class<?> entryClass = XposedHelpers.findClass(entryName, classLoader);
            tempActivity = entryClass.newInstance();
            Method onRespMethod = XposedHelpers.findMethodExact(entryClass, "onResp", XposedHelpers.findClass("com.tencent.mm.opensdk.modelbase.BaseResp", classLoader));
            onRespMethod.invoke(tempActivity, resp);
        } catch (Throwable t) {} finally { if (tempActivity != null) { try { XposedHelpers.callMethod(tempActivity, "finish"); } catch (Throwable ignored) {} } }
    }

    private static List<Activity> getAllActivities() {
        List<Activity> result = new ArrayList<>();
        try { Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Object activityThread = XposedHelpers.callStaticMethod(activityThreadClass, "currentActivityThread");
            Object activitiesMap = XposedHelpers.getObjectField(activityThread, "mActivities");
            if (activitiesMap == null) return result;
            Collection<?> values = ((Map<?, ?>) activitiesMap).values();
            for (Object record : values) {
                if (record == null) continue;
                Activity activity = (Activity) XposedHelpers.getObjectField(record, "activity");
                if (activity != null && !activity.isFinishing()) result.add(activity);
            }
        } catch (Throwable t) {}
        return result;
    }

    private static Object buildWechatSuccessResp(ClassLoader classLoader) {
        try { Class<?> respClass = XposedHelpers.findClass("com.tencent.mm.opensdk.modelmsg.SendMessageToWX$Resp", classLoader);
            Object resp = XposedHelpers.newInstance(respClass);
            XposedHelpers.setIntField(resp, "errCode", 0); return resp;
        } catch (Throwable e) { try { Class<?> baseRespClass = XposedHelpers.findClass("com.tencent.mm.opensdk.modelbase.BaseResp", classLoader);
                Object resp = XposedHelpers.newInstance(baseRespClass);
                XposedHelpers.setIntField(resp, "errCode", 0); return resp;
            } catch (Throwable t) { return null; } }
    }

    private static void hookQQShare(final ClassLoader classLoader, final BooleanProvider enabledGetter) {
        try {
            Class<?> qqShareClass = XposedHelpers.findClassIfExists("com.tencent.connect.share.QQShare", classLoader);
            if (qqShareClass != null) {
                XposedHelpers.findAndHookMethod(qqShareClass, "shareToQQ", Activity.class, Bundle.class,
                    XposedHelpers.findClass("com.tencent.tauth.IUiListener", classLoader), new XC_MethodHook() {
                        @Override protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            if (!enabledGetter.get()) return;
                            showToast("[分享]QQ好友");
                            ReaLog.log("share_fake", "QQ好友分享假装成功");
                            Object listener = param.args[2];
                            if (listener != null) { Object result = createSuccessJson(classLoader);
                                if (result != null) XposedHelpers.callMethod(listener, "onComplete", result); }
                            param.setResult(null);
                        }
                    });
            }
            Class<?> qzoneClass = XposedHelpers.findClassIfExists("com.tencent.connect.share.QzoneShare", classLoader);
            if (qzoneClass != null) {
                XposedHelpers.findAndHookMethod(qzoneClass, "shareToQzone", Activity.class, Bundle.class,
                    XposedHelpers.findClass("com.tencent.tauth.IUiListener", classLoader), new XC_MethodHook() {
                        @Override protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            if (!enabledGetter.get()) return;
                            showToast("[分享]QQ空间");
                            ReaLog.log("share_fake", "QQ空间分享假装成功");
                            Object listener = param.args[2];
                            if (listener != null) { Object result = createQzoneSuccessJson(classLoader);
                                if (result != null) XposedHelpers.callMethod(listener, "onComplete", result); }
                            param.setResult(null);
                        }
                    });
            }
            Class<?> baseApiClass = XposedHelpers.findClassIfExists("com.tencent.connect.common.BaseApi", classLoader);
            if (baseApiClass != null) {
                XposedHelpers.findAndHookMethod(baseApiClass, "request", Activity.class, String.class, Bundle.class, String.class,
                    XposedHelpers.findClass("com.tencent.tauth.IUiListener", classLoader), new XC_MethodHook() {
                        @Override protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            if (!enabledGetter.get()) return;
                            String api = (String) param.args[1];
                            if (api != null && api.contains("share")) {
                                showToast("[分享]QQ成功");
                                ReaLog.log("share_fake", "QQ分享(通用)假装成功");
                                Object listener = param.args[4];
                                if (listener != null) { JSONObject result = new JSONObject(); result.put("ret", 0);
                                    XposedHelpers.callMethod(listener, "onComplete", result); }
                                param.setResult(null);
                            }
                        }
                    });
            }
        } catch (Throwable t) {}
    }

    private static void hookQQCallbacks(final ClassLoader classLoader, final BooleanProvider enabledGetter) {
        try {
            Class<?> iuiListenerClass = XposedHelpers.findClassIfExists("com.tencent.tauth.IUiListener", classLoader);
            if (iuiListenerClass != null) {
                XposedHelpers.findAndHookMethod(iuiListenerClass, "onError", XposedHelpers.findClassIfExists("com.tencent.tauth.UiError", classLoader), new XC_MethodHook() {
                        @Override protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            if (!enabledGetter.get()) return;
                            Object listener = param.thisObject;
                            Object result = createSuccessJson(classLoader);
                            if (result != null) XposedHelpers.callMethod(listener, "onComplete", result);
                            param.setResult(null);
                        }
                    });
                XposedHelpers.findAndHookMethod(iuiListenerClass, "onCancel", new XC_MethodHook() {
                        @Override protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            if (!enabledGetter.get()) return;
                            Object listener = param.thisObject;
                            Object result = createSuccessJson(classLoader);
                            if (result != null) XposedHelpers.callMethod(listener, "onComplete", result);
                            param.setResult(null);
                        }
                    });
            }
        } catch (Throwable t) {}
    }

    private static Object createSuccessJson(ClassLoader classLoader) {
        try { Class<?> jsonClass = XposedHelpers.findClass("org.json.JSONObject", classLoader);
            Object json = XposedHelpers.newInstance(jsonClass);
            XposedHelpers.callMethod(json, "put", "ret", 0);
            XposedHelpers.callMethod(json, "put", "error_message", ""); return json;
        } catch (Throwable t) { return null; }
    }
    private static Object createQzoneSuccessJson(ClassLoader classLoader) {
        try { Class<?> jsonClass = XposedHelpers.findClass("org.json.JSONObject", classLoader);
            Object json = XposedHelpers.newInstance(jsonClass);
            XposedHelpers.callMethod(json, "put", "ret", 0);
            XposedHelpers.callMethod(json, "put", "error_message", "");
            XposedHelpers.callMethod(json, "put", "post_id", "fake_post_123456"); return json;
        } catch (Throwable t) { return null; }
    }

    private static void showToast(final String message) { Context ctx = getCurrentActivityContext(); if (ctx != null) ToastUtil.show(ctx, message); }
    private static Context getCurrentActivityContext() {
        try { Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Object activityThread = XposedHelpers.callStaticMethod(activityThreadClass, "currentActivityThread");
            Object activitiesMap = XposedHelpers.getObjectField(activityThread, "mActivities");
            if (activitiesMap == null) return null;
            Collection<?> values = ((Map<?, ?>) activitiesMap).values();
            for (Object record : values) { if (record == null) continue;
                Activity activity = (Activity) XposedHelpers.getObjectField(record, "activity");
                if (activity != null && !activity.isFinishing()) return activity; }
        } catch (Throwable t) {}
        return null;
    }

    public static void hookDingTalkShareOnly(ClassLoader classLoader, BooleanProvider enabledGetter) {
        hookDingTalkShare(classLoader, enabledGetter);
        hookDingTalkCallbacks(classLoader, enabledGetter);
    }
    private static void hookDingTalkShare(final ClassLoader classLoader, final BooleanProvider enabledGetter) {
        try {
            Class<?> ddShareApiClass = XposedHelpers.findClassIfExists("com.alibaba.android.ddsharesdk.share.IDDShareApi", classLoader);
            if (ddShareApiClass != null) {
                XposedBridge.hookAllMethods(ddShareApiClass, "sendReq", new XC_MethodReplacement() {
                        @Override protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                            if (!enabledGetter.get()) return XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);
                            showToast("[分享]钉钉成功");
                            ReaLog.log("share_fake", "钉钉分享假装成功");
                            Object resp = buildDingTalkSuccessResp(classLoader);
                            if (resp != null) { Object handler = findDingTalkEventHandler(classLoader);
                                if (handler != null) XposedHelpers.callMethod(handler, "onResp", resp);
                                else callDingTalkOnRespViaTempActivity(classLoader, resp); }
                            return true;
                        }
                    });
            }
            Class<?> baseApiClass = XposedHelpers.findClassIfExists("com.alibaba.android.ddsharesdk.share.BaseApi", classLoader);
            if (baseApiClass != null) {
                XposedHelpers.findAndHookMethod(baseApiClass, "sendReq", XposedHelpers.findClass("com.alibaba.android.ddsharesdk.model.base.BaseReq", classLoader), new XC_MethodReplacement() {
                        @Override protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                            if (!enabledGetter.get()) return XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);
                            showToast("[分享]钉钉成功");
                            ReaLog.log("share_fake", "钉钉分享假装成功");
                            Object resp = buildDingTalkSuccessResp(classLoader);
                            if (resp != null && param.args.length > 1) { Object callback = param.args[1];
                                if (callback != null) XposedHelpers.callMethod(callback, "onResp", resp); }
                            return true;
                        }
                    });
            }
            Class<?> baseRespClass = XposedHelpers.findClassIfExists("com.alibaba.android.ddsharesdk.model.base.BaseResp", classLoader);
            if (baseRespClass != null) {
                XposedHelpers.findAndHookMethod(baseRespClass, "getErrCode", new XC_MethodHook() {
                        @Override protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            if (enabledGetter.get()) { param.setResult(0); ReaLog.log("share_fake", "钉钉虚假信息创建成功"); }
                        }
                    });
            }
        } catch (Throwable t) {}
    }
    private static void hookDingTalkCallbacks(final ClassLoader classLoader, final BooleanProvider enabledGetter) {
        try {
            Class<?> handlerClass = XposedHelpers.findClassIfExists("com.alibaba.android.ddsharesdk.model.callback.IDDAPIEventHandler", classLoader);
            if (handlerClass != null) {
                XposedHelpers.findAndHookMethod(handlerClass, "onResp", XposedHelpers.findClass("com.alibaba.android.ddsharesdk.model.base.BaseResp", classLoader), new XC_MethodHook() {
                        @Override protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            if (!enabledGetter.get()) return;
                            Object resp = param.args[0];
                            XposedHelpers.setIntField(resp, "errCode", 0); XposedHelpers.setIntField(resp, "errType", 0);
                        }
                    });
            }
        } catch (Throwable t) {}
    }
    private static Object buildDingTalkSuccessResp(ClassLoader classLoader) {
        try { Class<?> respClass = XposedHelpers.findClass("com.alibaba.android.ddsharesdk.model.share.SendMessageToDD$Resp", classLoader);
            Object resp = XposedHelpers.newInstance(respClass);
            XposedHelpers.setIntField(resp, "errCode", 0); XposedHelpers.setIntField(resp, "errType", 0); return resp;
        } catch (Throwable e) { try { Class<?> baseRespClass = XposedHelpers.findClass("com.alibaba.android.ddsharesdk.model.base.BaseResp", classLoader);
                Object resp = XposedHelpers.newInstance(baseRespClass);
                XposedHelpers.setIntField(resp, "errCode", 0); return resp;
            } catch (Throwable t) { return null; } }
    }
    private static Object findDingTalkEventHandler(ClassLoader classLoader) {
        try { Class<?> handlerInterface = XposedHelpers.findClass("com.alibaba.android.ddsharesdk.model.callback.IDDAPIEventHandler", classLoader);
            List<Activity> activities = getAllActivities();
            for (Activity act : activities) if (handlerInterface.isInstance(act)) return act;
        } catch (Throwable t) {}
        return null;
    }
    private static void callDingTalkOnRespViaTempActivity(ClassLoader classLoader, Object resp) {
        try { Class<?> entryClass = XposedHelpers.findClass("com.alibaba.android.ddsharesdk.share.DDShareActivity", classLoader);
            Object tempActivity = entryClass.newInstance();
            Method onRespMethod = XposedHelpers.findMethodExact(entryClass, "onResp", XposedHelpers.findClass("com.alibaba.android.ddsharesdk.model.base.BaseResp", classLoader));
            onRespMethod.invoke(tempActivity, resp);
            XposedHelpers.callMethod(tempActivity, "finish");
        } catch (Throwable ignored) {}
    }

    public static void hookWeiboShareOnly(ClassLoader classLoader, BooleanProvider enabledGetter) {
        hookWeiboShare(classLoader, enabledGetter);
        hookWeiboCallbacks(classLoader, enabledGetter);
    }
    private static void hookWeiboShare(final ClassLoader classLoader, final BooleanProvider enabledGetter) {
        try {
            Class<?> weiboShareApiClass = XposedHelpers.findClassIfExists("com.sina.weibo.sdk.api.share.IWeiboShareAPI", classLoader);
            if (weiboShareApiClass != null) {
                XposedBridge.hookAllMethods(weiboShareApiClass, "sendRequest", new XC_MethodReplacement() {
                        @Override protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                            if (!enabledGetter.get()) return XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);
                            showToast("[分享]微博成功");
                            ReaLog.log("share_fake", "微博分享假装成功");
                            Object response = buildWeiboSuccessResponse(classLoader);
                            Object callback = findWeiboCallback(param);
                            if (callback != null) XposedHelpers.callMethod(callback, "onSuccess", response);
                            return true;
                        }
                    });
            }
            Class<?> weiboAPIClass = XposedHelpers.findClassIfExists("com.sina.weibo.sdk.api.WeiboAPI", classLoader);
            if (weiboAPIClass != null) {
                XposedHelpers.findAndHookMethod(weiboAPIClass, "share", Activity.class, XposedHelpers.findClass("com.sina.weibo.sdk.api.WeiboMultiMessage", classLoader),
                    XposedHelpers.findClass("com.sina.weibo.sdk.api.share.IWeiboHandler$Response", classLoader), new XC_MethodReplacement() {
                        @Override protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                            if (!enabledGetter.get()) return XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);
                            showToast("[分享]微博成功");
                            ReaLog.log("share_fake", "微博分享(旧API)假装成功");
                            Object callback = param.args[2];
                            if (callback != null) { Object response = buildWeiboSuccessResponse(classLoader);
                                XposedHelpers.callMethod(callback, "onResponse", response); }
                            return true;
                        }
                    });
            }
            Class<?> baseResponseClass = XposedHelpers.findClassIfExists("com.sina.weibo.sdk.api.share.BaseResponse", classLoader);
            if (baseResponseClass != null) {
                XposedHelpers.findAndHookMethod(baseResponseClass, "getErrCode", new XC_MethodHook() {
                        @Override protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            if (enabledGetter.get()) { param.setResult(0); ReaLog.log("share_fake", "微博虚假信息创建成功"); }
                        }
                    });
            }
        } catch (Throwable t) {}
    }
    private static void hookWeiboCallbacks(final ClassLoader classLoader, final BooleanProvider enabledGetter) {
        try {
            Class<?> weiboResponseClass = XposedHelpers.findClassIfExists("com.sina.weibo.sdk.api.WeiboAPI$Response", classLoader);
            if (weiboResponseClass != null) {
                XposedHelpers.findAndHookMethod(weiboResponseClass, "onComplete", new XC_MethodHook() {
                        @Override protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            if (!enabledGetter.get()) return;
                            param.setResult(null);
                            XposedHelpers.callMethod(param.thisObject, "onSuccess");
                        }
                    });
            }
            Class<?> wbShareCallbackClass = XposedHelpers.findClassIfExists("com.sina.weibo.sdk.api.share.WbShareCallback", classLoader);
            if (wbShareCallbackClass != null) {
                XposedHelpers.findAndHookMethod(wbShareCallbackClass, "onWbShareSuccess", new XC_MethodHook() {
                        @Override protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            if (!enabledGetter.get()) return; param.setResult(null);
                        }
                    });
            }
        } catch (Throwable t) {}
    }
    private static Object buildWeiboSuccessResponse(ClassLoader classLoader) {
        try { Class<?> responseClass = XposedHelpers.findClass("com.sina.weibo.sdk.api.share.SendMessageToWeiboResponse", classLoader);
            Object response = XposedHelpers.newInstance(responseClass);
            XposedHelpers.setIntField(response, "errCode", 0);
            XposedHelpers.setObjectField(response, "errMsg", "success"); return response;
        } catch (Throwable e) { try { Class<?> baseRespClass = XposedHelpers.findClass("com.sina.weibo.sdk.api.share.BaseResponse", classLoader);
                Object response = XposedHelpers.newInstance(baseRespClass);
                XposedHelpers.setIntField(response, "errCode", 0); return response;
            } catch (Throwable t) { return null; } }
    }
    private static Object findWeiboCallback(XC_MethodHook.MethodHookParam param) {
        for (Object arg : param.args) if (arg != null && (arg.getClass().getName().contains("IWeiboHandler$Response") || arg.getClass().getName().contains("WbShareCallback"))) return arg;
        return null;
    }

    public static void fakeDingTalkShare(Activity activity, ClassLoader classLoader) {
        try { Object resp = buildDingTalkSuccessResp(classLoader);
            if (resp == null) return;
            Object handler = findDingTalkEventHandler(classLoader);
            if (handler != null) XposedHelpers.callMethod(handler, "onResp", resp);
            else callDingTalkOnRespViaTempActivity(classLoader, resp);
            showToast("[分享]钉钉成功");
            ReaLog.log("share_fake", "钉钉分享(主动调用)");
        } catch (Throwable ignored) {}
    }

    public static void fakeWeiboShare(Activity activity, ClassLoader classLoader) {
        try { Object response = buildWeiboSuccessResponse(classLoader);
            if (response == null) return;
            Object callback = findWeiboShareListener(activity, classLoader);
            if (callback != null) XposedHelpers.callMethod(callback, "onSuccess", response);
            showToast("[分享]微博成功");
            ReaLog.log("share_fake", "微博分享(主动调用)");
        } catch (Throwable ignored) {}
    }

    private static Object findWeiboShareListener(Activity activity, ClassLoader classLoader) {
        try { Class<?> responseClass = XposedHelpers.findClass("com.sina.weibo.sdk.api.share.IWeiboHandler$Response", classLoader);
            if (responseClass.isInstance(activity)) return activity;
        } catch (Throwable ignore) {}
        return null;
    }

    public static void fakeWeChatShare(Activity activity, ClassLoader classLoader, String targetPackage) {
        try { Object resp = buildWechatSuccessResp(classLoader);
            if (resp == null) return;
            Object handler = findWXAPIEventHandler(classLoader);
            if (handler != null) XposedHelpers.callMethod(handler, "onResp", resp);
            else callOnRespViaTempActivity(classLoader, resp, targetPackage);
            showToast("[分享]微信成功");
            ReaLog.log("share_fake", "微信分享(主动调用)");
        } catch (Throwable ignored) {}
    }

    public static void fakeQQShare(Activity activity, ClassLoader classLoader) {
        try { Object json = createSuccessJson(classLoader);
            if (json == null) return;
            Object listener = findQQShareListener(activity, classLoader);
            if (listener != null) XposedHelpers.callMethod(listener, "onComplete", json);
            showToast("[分享]QQ成功");
            ReaLog.log("share_fake", "QQ分享(主动调用)");
        } catch (Throwable ignored) {}
    }
    private static Object findQQShareListener(Activity activity, ClassLoader classLoader) {
        try { Class<?> listenerClass = XposedHelpers.findClass("com.tencent.tauth.IUiListener", classLoader);
            if (listenerClass.isInstance(activity)) return activity;
        } catch (Throwable ignore) {}
        return null;
    }
}
