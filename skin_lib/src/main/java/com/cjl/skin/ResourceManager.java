package com.cjl.skin;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;

import java.util.HashMap;

/**
 * 皮肤资源管理器
 *
 * @author CJL
 * @since 2017-02-22
 */
public class ResourceManager {
    public static final String TYPE_DRAWABLE = "drawable";
    public static final String TYPE_MIPMAP = "mipmap";
    public static final String TYPE_COLOR = "color";
    public static final String TYPE_INTEGER = "integer";
    public static final String TYPE_ARRAY = "array";
    public static final String TYPE_DIMENSION = "dimen";
    public static final String TYPE_BOOL = "bool";
    public static final String TYPE_STRING = "string";

    @StringDef({TYPE_DRAWABLE, TYPE_MIPMAP, TYPE_COLOR, TYPE_INTEGER, TYPE_ARRAY, TYPE_DIMENSION, TYPE_BOOL, TYPE_STRING})
    public @interface RES_TYPE {

    }

    private Resources mResources;
    private String mPluginPackageName;

    private ResourceManager.VectorDrawableSupport mVectorSupport;

    private boolean mSearchIdFromMap = true;
    private HashMap<String, Integer> mDrawableMap;
    private HashMap<String, Integer> mColorMap;


    ResourceManager(Context context, Resources res, String pluginPackageName) {
        mResources = res;
        mPluginPackageName = pluginPackageName;
        mSearchIdFromMap = false;

        mVectorSupport = new ResourceManager.VectorDrawableSupport(context, res, pluginPackageName);
    }

    ResourceManager(Context context, Resources res, String pluginPackageName,
                    HashMap<String, Integer> drawableMap, HashMap<String, Integer> colorMap) {
        this(context, res, pluginPackageName);

        mColorMap = colorMap;
        mDrawableMap = drawableMap;
        mSearchIdFromMap = true;
    }

    @Nullable
    public Drawable getDrawableByName(String name) {
        if (mSearchIdFromMap && mDrawableMap != null) {
            Integer tmpId = mDrawableMap.get(name);
            return tmpId == null ? null : getDrawableById(tmpId);
        } else {
            int id = mResources.getIdentifier(name, TYPE_DRAWABLE, mPluginPackageName);
            return getDrawableById(id);
        }
    }

    @Nullable
    Drawable getDrawableById(@DrawableRes int id) {
        if (id == 0) {
            return null;
        }
        Drawable d = mVectorSupport.loadVectorDrawable(id);
        return d == null ? mResources.getDrawable(id) : d;
    }

    @Nullable
    public Drawable getMipmapByName(String name) {
        int id = mResources.getIdentifier(name, TYPE_MIPMAP, mPluginPackageName);
        if (id == 0) {
            return null;
        }
        Drawable d = mVectorSupport.loadVectorDrawable(id);
        return d == null ? mResources.getDrawable(id) : d;
    }

    @Nullable
    public Integer getColor(String name) {
        if (mSearchIdFromMap && mColorMap != null) {
            Integer id = mColorMap.get(name);
            return id == null ? null : mResources.getColor(id);
        } else {
            int id = mResources.getIdentifier(name, TYPE_COLOR, mPluginPackageName);
            return id == 0 ? null : mResources.getColor(id);
        }
    }

    @Nullable
    Integer getColor(@ColorRes int id) {
        return id == 0 ? null : mResources.getColor(id);
    }

    @Nullable
    public ColorStateList getColorStateList(String name) {
        if (mSearchIdFromMap && mColorMap != null) {
            Integer id = mColorMap.get(name);
            return id == null ? null : mResources.getColorStateList(id);
        } else {
            int id = mResources.getIdentifier(name, TYPE_COLOR, mPluginPackageName);
            return id == 0 ? null : mResources.getColorStateList(id);
        }
    }

    @Nullable
    ColorStateList getColorStateList(int id) {
        return id == 0 ? null : mResources.getColorStateList(id);
    }

    @Nullable
    public Integer getInteger(String name) {
        int id = mResources.getIdentifier(name, TYPE_INTEGER, mPluginPackageName);
        return id == 0 ? null : mResources.getInteger(id);
    }

    @Nullable
    public int[] getIntArray(String name) {
        int id = mResources.getIdentifier(name, TYPE_ARRAY, mPluginPackageName);
        return id == 0 ? null : mResources.getIntArray(id);
    }

    @Nullable
    public Integer getDimensionPixelSize(String name) {
        int id = mResources.getIdentifier(name, TYPE_DIMENSION, mPluginPackageName);
        return id == 0 ? null : mResources.getDimensionPixelSize(id);
    }

    @Nullable
    public Boolean getBoolean(String name) {
        int id = mResources.getIdentifier(name, TYPE_BOOL, mPluginPackageName);
        return id == 0 ? null : mResources.getBoolean(id);
    }

    @Nullable
    public String getString(String name) {
        int id = mResources.getIdentifier(name, TYPE_STRING, mPluginPackageName);
        return id == 0 ? null : mResources.getString(id);
    }

    @Nullable
    public String[] getStringArray(String name) {
        int id = mResources.getIdentifier(name, TYPE_ARRAY, mPluginPackageName);
        return id == 0 ? null : mResources.getStringArray(id);
    }

    private static class VectorDrawableSupport {
        private Context mReplaceResContext;
        private Resources mRes;

        VectorDrawableSupport(Context context, final Resources resources, final String mPluginPackageName) {
            mRes = resources;

            if (context.getPackageName().equals(mPluginPackageName)) {
                mReplaceResContext = context;
            } else {
                mReplaceResContext = new ContextWrapper(context) {
                    @Override
                    public String getPackageName() {
                        return mPluginPackageName;
                    }

                    @Override
                    public AssetManager getAssets() {
                        return resources.getAssets();
                    }

                    @Override
                    public Resources getResources() {
                        return resources;
                    }

                    @Override
                    public Context getApplicationContext() {
                        return this;
                    }
                };
            }
        }

        @Nullable
        Drawable loadVectorDrawable(int resId) {
            return VectorDrawableLoader.loadDrawable(mReplaceResContext, mRes, resId);
        }
    }


}
