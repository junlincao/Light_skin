package com.cjl.skin;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatDelegate;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import dalvik.system.DexClassLoader;

/**
 * 皮肤管理类
 *
 * @author CJL
 * @since 2017-02-22
 */
public class SkinManager {
    private ResourceManager mResourceManager;
    private String mCurSkinPath;

    private List<ISkinChangedListener> mSkinChangedListeners = new LinkedList<>();
    private List<SkinView> mSkinViews = new LinkedList<>();

    private SkinManager() {
    }

    private static class SingletonHolder {
        final static SkinManager sInstance = new SkinManager();
    }

    public static SkinManager getInstance() {
        return SingletonHolder.sInstance;
    }

    /**
     * 初始化皮肤
     *
     * @param context Context
     */
    public void init(Context context) {
        Context appContext = context.getApplicationContext();
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        String skinPath = getSkinPath(appContext);
        try {
            if (TextUtils.isEmpty(skinPath)) {
                return;
            }
            File file = new File(skinPath);
            if (!file.exists()) {
                return;
            }
            try {
                mResourceManager = loadPlugin(appContext, skinPath);
                mCurSkinPath = skinPath;
            } catch (Exception e) {
                Log.e(Constants.TAG, "apply skin on init failed!", e);
            }
        } finally {
            if (mResourceManager == null) {
                useDefaultSkin(context);
            }
        }
    }

    /**
     * 判断是否正在使用皮肤
     */
    public boolean isUsePlugin() {
        return !TextUtils.isEmpty(mCurSkinPath);
    }

    /**
     * 使用默认皮肤样式
     *
     * @param context Context
     */
    public void useDefaultSkin(Context context) {
        clearPluginInfo(context);
        mResourceManager = new ResourceManager(context, context.getResources(), context.getPackageName());
        notifyChangedListeners();
    }

    /**
     * @return 皮肤资源管理器
     */
    public ResourceManager getResourceManager() {
        if (mResourceManager == null) {
            throw new RuntimeException("init() or changeSkin(...) method must be call before use!");
        }
        return mResourceManager;
    }

    private void clearPluginInfo(Context context) {
        mCurSkinPath = null;
        mResourceManager = null;
        saveSkinPath(context, null);
    }

    /**
     * 换肤
     *
     * @param context  Context
     * @param skinPath 皮肤路径
     * @param cb       换肤回调
     */
    public void changeSkin(Context context, final String skinPath, final ISkinChangingCallback cb) {
        final ISkinChangingCallback callback = cb == null ? new EmptySkinChangingCallback() : cb;

        callback.onStart();

        if (TextUtils.isEmpty(skinPath) || !new File(skinPath).exists()) {
            callback.onError(new NullPointerException("skin file not exists!"));
            return;
        }

        PackageManager mPm = context.getPackageManager();
        PackageInfo mInfo = mPm.getPackageArchiveInfo(skinPath, PackageManager.GET_ACTIVITIES);
        if (mInfo == null) {
            callback.onError(new NullPointerException("skin file is not a apk file!->" + skinPath));
            return;
        }
        if (skinPath.equals(mCurSkinPath)) {
            return;
        }

        final Context appContext = context.getApplicationContext();

        new AsyncTask<Void, Void, ResourceManager>() {
            @Override
            protected ResourceManager doInBackground(Void... params) {
                try {
                    return loadPlugin(appContext, skinPath);
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onError(e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(ResourceManager resourceManager) {
                if (resourceManager == null) {
                    return;
                }
                try {
                    saveSkinPath(appContext, skinPath);
                    mResourceManager = resourceManager;
                    mCurSkinPath = skinPath;
                    notifyChangedListeners();
                    callback.onComplete();
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onError(e);
                }
            }
        }.execute();
    }


    /**
     * 加载皮肤资源。预读取Drawable和Color资源名以提高效率
     *
     * @param mContext Context
     * @param skinPath 皮肤路径
     * @return 皮肤资源管理器
     */
    private ResourceManager loadPlugin(Context mContext, String skinPath) throws Exception {
        if (TextUtils.isEmpty(skinPath)) {
            throw new IllegalArgumentException("SkinPath is null!");
        }
        // android低版本DexClassLoader貌似对文件后缀有要求（我4.1模拟器用.skin文件无法读取里面的类），此处校验一下后缀
        if (!skinPath.endsWith(".apk") && !skinPath.endsWith(".zip")) {
            throw new IllegalArgumentException("Please use '.apk' or '.zip' for file suffix!");
        }

        PackageInfo mInfo = mContext.getPackageManager().getPackageArchiveInfo(skinPath, PackageManager.GET_META_DATA);
        final String skinPkgName = mInfo.packageName;
        String rPkgName = skinPkgName;
        Bundle bundle = mInfo.applicationInfo.metaData;
        if (bundle != null && !TextUtils.isEmpty(bundle.getString(Constants.MANIFEST_PACKAGE))) {
            rPkgName = bundle.getString(Constants.MANIFEST_PACKAGE);
        }
        DexClassLoader classLoader = new DexClassLoader(skinPath, mContext.getCacheDir().getAbsolutePath(),
                null, ClassLoader.getSystemClassLoader());

        // read all drawable name and it's id to map
        Class drawableClass = Class.forName(rPkgName + ".R$drawable", true, classLoader);
        HashMap<String, Integer> drawables = new HashMap<>();
        for (Field f : drawableClass.getDeclaredFields()) {
            drawables.put(f.getName(), f.getInt(null));
        }
        // read all color name and it's id to map
        Class colorClass = Class.forName(rPkgName + ".R$color", true, classLoader);
        HashMap<String, Integer> colors = new HashMap<>();
        for (Field f : colorClass.getDeclaredFields()) {
            colors.put(f.getName(), f.getInt(null));
        }

        AssetManager assetManager = AssetManager.class.newInstance();
        Method addAssetPath = assetManager.getClass().getMethod("addAssetPath", String.class);
        addAssetPath.invoke(assetManager, skinPath);

        Resources superRes = mContext.getResources();
        Resources mResources = new Resources(assetManager, superRes.getDisplayMetrics(), superRes.getConfiguration());
        return new ResourceManager(mContext, mResources, skinPkgName, drawables, colors);
    }


    /**
     * 添加一个View到列表，皮肤切换的时候会对这个View做相应换肤处理
     *
     * @param view      View
     * @param skinViews 换肤属性
     */
    public void addSkinView(View view, List<SkinAttr> skinViews) {
        mSkinViews.add(new SkinView(view, skinViews));
    }

    /**
     * 添加皮肤变更事件回调
     * <p>
     * 记得在相应生命周期内remove
     *
     * @param listener ISkinChangedListener
     */
    public void addChangedListener(ISkinChangedListener listener) {
        if (listener != null) {
            mSkinChangedListeners.add(listener);
        }
    }

    /**
     * 移除皮肤变更事件回调
     *
     * @param listener ISkinChangedListener
     */
    public void removeChangedListener(ISkinChangedListener listener) {
        if (listener != null) {
            mSkinChangedListeners.remove(listener);
        }
    }

    @SuppressWarnings("unchecked")
    private void notifyChangedListeners() {
        ResourceManager rm = getResourceManager();
        for (Iterator<SkinView> it = mSkinViews.iterator(); it.hasNext(); ) {
            SkinView skinView = it.next();
            View view = skinView.viewRef.get();
            if (view == null) {
                it.remove();
            } else {
                applyViewSkin(view, skinView.attrs);
            }
        }

        for (ISkinChangedListener listener : mSkinChangedListeners) {
            listener.onSkinChanged(rm);
        }
    }


    void applyViewSkin(@NonNull View view, @NonNull List<SkinAttr> attrs) {
        ResourceManager resourceManager = mResourceManager;
        for (SkinAttr sa : attrs) {
            sa.apply(resourceManager, view);
        }
    }

    @SuppressLint("ApplySharedPref")
    private void saveSkinPath(Context context, String path) {
        SharedPreferences sp = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        sp.edit().putString(Constants.SP_PLUGIN_PATH, path).commit();
    }

    /**
     * 获取当前使用的皮肤保存路径
     *
     * @param context Context
     * @return 皮肤路径，如果没使用皮肤则为null
     */
    public String getSkinPath(Context context) {
        SharedPreferences sp = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        return sp.getString(Constants.SP_PLUGIN_PATH, null);
    }


    /**
     * 皮肤切换事件回调
     */
    public interface ISkinChangedListener {
        void onSkinChanged(ResourceManager rm);
    }

    /**
     * 皮肤变更过程回调
     */
    public interface ISkinChangingCallback {
        void onStart();

        void onError(Exception e);

        void onComplete();
    }

    public class EmptySkinChangingCallback implements ISkinChangingCallback {
        @Override
        public void onStart() {

        }

        @Override
        public void onError(Exception e) {
            Log.e(Constants.TAG, "换肤失败！", e);
        }

        @Override
        public void onComplete() {

        }
    }

    private static class SkinView {
        WeakReference<View> viewRef;
        List<SkinAttr> attrs;

        SkinView(View view, List<SkinAttr> attrs) {
            this.viewRef = new WeakReference<>(view);
            this.attrs = attrs;
        }
    }
}
