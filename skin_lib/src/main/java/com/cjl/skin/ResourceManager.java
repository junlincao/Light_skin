package com.cjl.skin;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.support.v4.content.ContextCompat;

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

    private boolean isBaseResource;
    private Context mBaseContext;
    private Resources mSkinResource;
    private String mSkinPackageName;

    private ResourceManager.VectorDrawableSupport mVectorSupport;

    private HashMap<String, Integer> mDrawableMap;
    private HashMap<String, Integer> mColorMap;


    ResourceManager(Context context, Resources res, String skinPackageName) {
        mBaseContext = context;
        mSkinResource = res;
        mSkinPackageName = skinPackageName;
        isBaseResource = context.getPackageName().equals(skinPackageName);
        if (!isBaseResource) {
            mVectorSupport = new ResourceManager.VectorDrawableSupport(context, res, skinPackageName);
        }
    }

    ResourceManager(Context context, Resources res, String pluginPackageName,
                    HashMap<String, Integer> drawableMap, HashMap<String, Integer> colorMap) {
        this(context, res, pluginPackageName);

        mColorMap = colorMap;
        mDrawableMap = drawableMap;
    }

    @Nullable
    public Drawable getDrawableByName(String name) {
        int id = 0;
        boolean isSkinRes = true;

        if (!isBaseResource && mDrawableMap != null) {
            Integer tmpId = mDrawableMap.get(name);
            if (tmpId != null) {
                id = tmpId;
            }
        } else if (!isBaseResource) {
            id = mSkinResource.getIdentifier(name, TYPE_DRAWABLE, mSkinPackageName);
        }
        if (id <= 0) {
            isSkinRes = false;
            id = mBaseContext.getResources().getIdentifier(name, TYPE_DRAWABLE, mBaseContext.getPackageName());
        }
        if (isSkinRes) {
            Drawable d = mVectorSupport.loadVectorDrawable(id); // checkVectorDrawable
            return d == null ? mSkinResource.getDrawable(id) : d;
        } else {
            // checkVectorDrawable first
            Drawable d = VectorDrawableLoader.loadDrawable(mBaseContext, mBaseContext.getResources(), id);
            return d == null ? ContextCompat.getDrawable(mBaseContext, id) : d;
        }
    }

    @Nullable
    public Drawable getMipmapByName(String name) {
        int id = 0;
        boolean isSkinRes = true;
        if (!isBaseResource) {
            id = mSkinResource.getIdentifier(name, TYPE_MIPMAP, mSkinPackageName);
        }
        if (id == 0) {
            isSkinRes = false;
            id = mBaseContext.getResources().getIdentifier(name, TYPE_MIPMAP, mBaseContext.getPackageName());
        }
        if (isSkinRes) {
            Drawable d = mVectorSupport.loadVectorDrawable(id); // checkVectorDrawable
            return d == null ? mSkinResource.getDrawable(id) : d;
        } else {
            // checkVectorDrawable first
            Drawable d = VectorDrawableLoader.loadDrawable(mBaseContext, mBaseContext.getResources(), id);
            return d == null ? ContextCompat.getDrawable(mBaseContext, id) : d;
        }
    }

    @Nullable
    public Integer getColor(String name) {
        int id = 0;
        if (!isBaseResource && mColorMap != null) {
            id = mColorMap.get(name);
        } else if (!isBaseResource) {
            id = mSkinResource.getIdentifier(name, TYPE_COLOR, mSkinPackageName);
        }
        if (id == 0) {
            id = mBaseContext.getResources().getIdentifier(name, TYPE_COLOR, mBaseContext.getPackageName());
            return id == 0 ? null : ContextCompat.getColor(mBaseContext, id);
        } else {
            return mSkinResource.getColor(id);
        }
    }

    @Nullable
    public ColorStateList getColorStateList(String name) {
        int id = 0;
        if (!isBaseResource && mColorMap != null) {
            id = mColorMap.get(name);
        } else if (!isBaseResource) {
            id = mSkinResource.getIdentifier(name, TYPE_COLOR, mSkinPackageName);
        }
        if (id == 0) {
            id = mBaseContext.getResources().getIdentifier(name, TYPE_COLOR, mBaseContext.getPackageName());
            return id == 0 ? null : ContextCompat.getColorStateList(mBaseContext, id);
        } else {
            return mSkinResource.getColorStateList(id);
        }
    }

    @Nullable
    public Integer getInteger(String name) {
        int id = 0;
        if (!isBaseResource) {
            id = mSkinResource.getIdentifier(name, TYPE_INTEGER, mSkinPackageName);
            if (id != 0) {
                return mSkinResource.getInteger(id);
            }
        }
        id = mBaseContext.getResources().getIdentifier(name, TYPE_INTEGER, mBaseContext.getPackageName());
        return id == 0 ? null : mBaseContext.getResources().getInteger(id);
    }

    @Nullable
    public int[] getIntArray(String name) {
        int id = 0;
        if (!isBaseResource) {
            id = mSkinResource.getIdentifier(name, TYPE_ARRAY, mSkinPackageName);
            if (id != 0) {
                return mSkinResource.getIntArray(id);
            }
        }
        id = mBaseContext.getResources().getIdentifier(name, TYPE_ARRAY, mBaseContext.getPackageName());
        return id == 0 ? null : mBaseContext.getResources().getIntArray(id);
    }

    @Nullable
    public Integer getDimensionPixelSize(String name) {
        int id = 0;
        if (!isBaseResource) {
            id = mSkinResource.getIdentifier(name, TYPE_DIMENSION, mSkinPackageName);
            if (id != 0) {
                return mSkinResource.getDimensionPixelSize(id);
            }
        }
        id = mBaseContext.getResources().getIdentifier(name, TYPE_DIMENSION, mBaseContext.getPackageName());
        return id == 0 ? null : mBaseContext.getResources().getDimensionPixelSize(id);
    }

    @Nullable
    public Boolean getBoolean(String name) {
        int id = 0;
        if (!isBaseResource) {
            id = mSkinResource.getIdentifier(name, TYPE_BOOL, mSkinPackageName);
            if (id != 0) {
                return mSkinResource.getBoolean(id);
            }
        }
        id = mBaseContext.getResources().getIdentifier(name, TYPE_BOOL, mBaseContext.getPackageName());
        return id == 0 ? null : mBaseContext.getResources().getBoolean(id);
    }

    @Nullable
    public String getString(String name) {
        int id = 0;
        if (!isBaseResource) {
            id = mSkinResource.getIdentifier(name, TYPE_STRING, mSkinPackageName);
            if (id != 0) {
                return mSkinResource.getString(id);
            }
        }
        id = mBaseContext.getResources().getIdentifier(name, TYPE_STRING, mBaseContext.getPackageName());
        return id == 0 ? null : mBaseContext.getResources().getString(id);
    }

    @Nullable
    public String[] getStringArray(String name) {
        int id = 0;
        if (!isBaseResource) {
            id = mSkinResource.getIdentifier(name, TYPE_ARRAY, mSkinPackageName);
            if (id != 0) {
                return mSkinResource.getStringArray(id);
            }
        }
        id = mBaseContext.getResources().getIdentifier(name, TYPE_ARRAY, mBaseContext.getPackageName());
        return id == 0 ? null : mBaseContext.getResources().getStringArray(id);
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
