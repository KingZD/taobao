package com.tb.Taopassword;

import android.app.Application;
import android.content.Context;

import com.alibaba.fastjson.JSON;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class Tb implements IXposedHookLoadPackage {
    private Class<?> shareRequestDOClass;//com.taobao.sns.share.ShareRequestDO类 请求体
    private Field mShareRequestFiled;//com.taobao.sns.share.taotoken.view.TaoTokenShareView里面字段mShareRequest 用于接受赋值调用淘口令接口 请求数据引用
    private Object taoTokenShareViewObj;//com.taobao.sns.share.taotoken.view.TaoTokenShareView实例化 淘宝分享view 类

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) {
        if ("com.taobao.etao".equals(lpparam.packageName)) {
            startHookETao();
        }
    }

    /**
     * 开始骚操作
     */
    private void startHookETao() {
        XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                Context context = (Context) param.args[0];
                LogUtils.init(context);
                final ClassLoader cl = ((Context) param.args[0]).getClassLoader();
                initParam(cl);
                taoPassword(cl);
                //模拟自动调用获取淘口令 如需更改，请按照下面格式替换
                timer();
            }
        });
    }

    /**
     * 获取参数引用
     * @param classLoader
     */
    private void initParam(final ClassLoader classLoader) {
        XposedHelpers.findAndHookMethod("com.taobao.sns.activity.ISWebViewActivity", classLoader, "createView", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                shareRequestDOClass = XposedHelpers.findClass("com.taobao.sns.share.ShareRequestDO", classLoader);
                Class<?> taoTokenShareViewClass = XposedHelpers.findClass("com.taobao.sns.share.taotoken.view.TaoTokenShareView", classLoader);
                taoTokenShareViewObj = XposedHelpers.newInstance(taoTokenShareViewClass);
                Field mActivityFiled = XposedHelpers.findField(taoTokenShareViewClass, "mActivity");
                mActivityFiled.setAccessible(true);
                mActivityFiled.set(taoTokenShareViewObj, param.thisObject);
                mShareRequestFiled = XposedHelpers.findField(taoTokenShareViewClass, "mShareRequest");
                mShareRequestFiled.setAccessible(true);
            }
        });
    }

    /**
     * 请求淘口令入口
     * @throws Exception
     */

    private void getTaobaoPassword(int mFrom, String mTitle, String mContent, String mUrl, String mPicUrl, int mIconResId, String mTtid) {
        if (shareRequestDOClass == null) {
            LogUtils.eTag("generateTaoPassword", "调用方法getTaobaoPassword()失败，重要参数为shareRequestDOClass=null");
            return;
        }
        if (mShareRequestFiled == null) {
            LogUtils.eTag("generateTaoPassword", "调用方法getTaobaoPassword()失败，重要参数为mShareRequestFiled=null");
            return;
        }
        if (taoTokenShareViewObj == null) {
            LogUtils.eTag("generateTaoPassword", "调用方法getTaobaoPassword()失败，重要参数为taoTokenShareViewObj=null");
            return;
        }
        try {
            Object shareRequestDOObj = XposedHelpers.newInstance(shareRequestDOClass,
                    new Class<?>[]{int.class, String.class, String.class, String.class, String.class, int.class, String.class},
                    mFrom, mTitle, mContent, mUrl, mPicUrl, mIconResId, mTtid);
            mShareRequestFiled.set(taoTokenShareViewObj, shareRequestDOObj);
            Object mShareRequestObj = mShareRequestFiled.get(taoTokenShareViewObj);
            LogUtils.iTag("generateTaoPassword", JSON.toJSONString(mShareRequestObj));
            XposedHelpers.callMethod(taoTokenShareViewObj, "initToken");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 模拟传入自定义需要转换的淘口令商品参数
     * 主动请求获取淘口令
     */
    private void timer() {
        Observable
                .interval(0, 10 * 1000, TimeUnit.MILLISECONDS)
                .flatMap(new Function<Long, ObservableSource<String>>() {
                    @Override
                    public ObservableSource<String> apply(Long aLong) throws Exception {
                        //这里是为了测试 生成不同的淘口令
                        int mFrom = 2;
                        String mTitle = "测试主动调用淘口令方法".concat(String.valueOf(aLong));
                        String mContent = String.valueOf(aLong).concat("测试主动调用淘口令方法https://detail.tmall.com/item.htm?id=576827670841&item_id=532930896580");
                        String mUrl = "https://detail.tmall.com/item.htm?id=576827670841&item_id=532930896580";
                        String mPicUrl = "http://img3.imgtn.bdimg.com/it/u=3347326845,2280670588&fm=11&gp=0.jpg";
                        int mIconResId = 2130838588;
                        String mTtid = "701234@etao_android_8.8.8";
                        getTaobaoPassword(mFrom, mTitle, mContent, mUrl, mPicUrl, mIconResId, mTtid);
                        return Observable.just("");
                    }
                })
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<Object>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Object o) {
                        LogUtils.iTag("generateTaoPassword", "调用成功");
                    }

                    @Override
                    public void onError(Throwable e) {
                        LogUtils.eTag("调用淘口令接口错误", e);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    /**
     * 初始化e淘的获取淘口令的重要参数
     * 这里获取得到的淘口令结果 监听异步回调
     * @param classLoader
     */
    private void taoPassword(final ClassLoader classLoader) {
        Class<?> tpShareListener = XposedHelpers.findClass("com.taobao.taopassword.listener.TPShareListener", classLoader);
        LogUtils.iTag("generateTaoPassword", tpShareListener);

        Class<?> MtopResponse = XposedHelpers.findClass("mtopsdk.mtop.domain.MtopResponse", classLoader);
        Class<?> BaseOutDo = XposedHelpers.findClass("mtopsdk.mtop.domain.BaseOutDo", classLoader);
        XposedHelpers.findAndHookMethod(
                "com.taobao.taopassword.generate.TaoPasswordGenerateRequest",
                classLoader,
                "onSuccess",
                int.class,
                MtopResponse,
                BaseOutDo,
                Object.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                        LogUtils.iTag("generateTaoPassword", JSON.toJSONString(param.args[1]), JSON.toJSONString(param.args[2]), JSON.toJSONString(param.args[3]));
                    }
                });

    }

}
