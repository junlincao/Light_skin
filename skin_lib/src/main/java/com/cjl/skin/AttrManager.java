package com.cjl.skin;

import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 属性资源管理类
 *
 * @author CJL
 * @since 2017-04-25
 */
public final class AttrManager {

    /**
     * 所有的属性资源
     */
    private static final Map<String, IAttr> sSkinAttrs = new HashMap<>();
    /**
     * 自定义View属性资源
     */
    public static final SkinAttr CUSTOM_VIEW_ATTR = new CustomViewSkinAttr();

    /**
     * 支持换肤的属性
     *
     * @author CJL
     * @since 2016-09-14
     */
    public interface IAttr<T> {

        /**
         * attr name
         */
        String getAttrName();

        /**
         * 设置资源值到View中
         *
         * @param view view
         * @param obj  皮肤资源值，比如Drawable，ColorStateList
         */
        void apply(View view, @NonNull T obj);

        /**
         * 从皮肤资源中读取资源值
         *
         * @param rm      ResourceManager
         * @param resName 资源名
         * @param resType 资源类型（比如T为drawable，但是xml中可以设置为drawable Id, mipmap Id,color Id, 查找时候需要分别对待）
         * @return 资源值 为null表示皮肤包中无此属性值，则换肤时候不会改变替换此属性值
         */
        T getTypeValue(ResourceManager rm, String resName, @ResourceManager.RES_TYPE String resType);
    }

    /**
     * Drawable资源属性
     */
    public static abstract class DrawableAttr implements IAttr<Drawable> {
        @Override
        public Drawable getTypeValue(ResourceManager rm, String resName, @ResourceManager.RES_TYPE String resType) {
            if (ResourceManager.TYPE_DRAWABLE.equals(resType)) {
                return rm.getDrawableByName(resName);
            } else if (ResourceManager.TYPE_MIPMAP.equals(resType)) {
                return rm.getMipmapByName(resName);
            } else if (ResourceManager.TYPE_COLOR.equals(resType)) {
                Integer color = rm.getColor(resName);
                if (color != null) {
                    return new ColorDrawable(color);
                }
            }
            return null;
        }
    }

    /**
     * ColorStateList资源属性
     */
    public static abstract class ColorStateListAttr implements IAttr<ColorStateList> {
        @Override
        public ColorStateList getTypeValue(ResourceManager rm, String resName, String resType) {
            return rm.getColorStateList(resName);
        }
    }

    /**
     * Color资源属性
     */
    public static abstract class ColorAttr implements IAttr<Integer> {
        @Override
        public Integer getTypeValue(ResourceManager rm, String resName, String resType) {
            return rm.getColor(resName);
        }
    }

    /**
     * 自定义View资源，此处getTypeValue返回空值，apply时候自行从ResourceManager读取想要的值
     */
    public static class CustomViewAttr implements IAttr<Void> {
        @Override
        public String getAttrName() {
            return "◠‿◠";
        }

        @Override
        public void apply(View view, @NonNull Void resName) {
        }

        @Override
        public Void getTypeValue(ResourceManager rm, String resName, String resType) {
            return null;
        }
    }

    private AttrManager() {

    }


    /**
     * 添加换肤属性
     *
     * @param attr Attr
     * @return 旧Attr
     */
    public static IAttr addSkinAttr(IAttr attr) {
        IAttr oldAttr = sSkinAttrs.put(attr.getAttrName(), attr);
        if (oldAttr != null) {
            Log.w(Constants.TAG, "overide attr->" + attr.getAttrName());
        }
        return oldAttr;
    }

    /**
     * 移除换肤属性
     *
     * @param attrName 属性名
     */
    public static IAttr removeSkinAttr(String attrName) {
        return sSkinAttrs.remove(attrName);
    }

    /**
     * 获取支持的属性列表(列表无法修改！)
     */
    public static Map<String, IAttr> getSupportedAttrs() {
        return Collections.unmodifiableMap(sSkinAttrs);
    }

    static Map<String, IAttr> innerGetSupportAttrs() {
        return sSkinAttrs;
    }

    private static class CustomViewSkinAttr extends SkinAttr {
        CustomViewSkinAttr() {
            super(new CustomViewAttr(), null, null);
        }

        @Override
        public void apply(ResourceManager resourceManager, View view) {
            if (view instanceof ISkinable) {
                ((ISkinable) view).applySkin(resourceManager);
            }
        }
    }


    /**
     * 添加默认支持的属性
     */
    static {
        sSkinAttrs.put("background", new DrawableAttr() {
            @Override
            public String getAttrName() {
                return "background";
            }

            @Override
            public void apply(View view, @NonNull Drawable drawable) {
                if (Build.VERSION.SDK_INT >= 16) {
                    view.setBackground(drawable);
                } else {
                    view.setBackgroundDrawable(drawable);
                }
            }
        });

        sSkinAttrs.put("textColor", new ColorStateListAttr() {
            @Override
            public String getAttrName() {
                return "textColor";
            }

            @Override
            public void apply(View view, @NonNull ColorStateList color) {
                if (view instanceof TextView) {
                    ((TextView) view).setTextColor(color);
                }
            }
        });

        sSkinAttrs.put("src", new DrawableAttr() {
            @Override
            public String getAttrName() {
                return "src";
            }

            @Override
            public void apply(View view, @NonNull Drawable drawable) {
                if (view instanceof ImageView) {
                    ((ImageView) view).setImageDrawable(drawable);
                }
            }
        });

        sSkinAttrs.put("textColorHint", new ColorStateListAttr() {
            @Override
            public String getAttrName() {
                return "textColorHint";
            }

            @Override
            public void apply(View view, @NonNull ColorStateList color) {
                if (view instanceof TextView) {
                    ((TextView) view).setHintTextColor(color);
                }
            }
        });

        sSkinAttrs.put("listSelector", new DrawableAttr() {
            @Override
            public String getAttrName() {
                return "listSelector";
            }

            @Override
            public void apply(View view, @NonNull Drawable drawable) {
                if (view instanceof AbsListView) {
                    ((AbsListView) view).setSelector(drawable);
                }
            }
        });

        sSkinAttrs.put("divider", new DrawableAttr() {
            @Override
            public String getAttrName() {
                return "divider";
            }

            @Override
            public void apply(View view, @NonNull Drawable drawable) {
                if (view instanceof ListView) {
                    ((ListView) view).setDivider(drawable);
                }
            }
        });

        sSkinAttrs.put("foreground", new DrawableAttr() {
            @Override
            public String getAttrName() {
                return "foreground";
            }

            @Override
            public void apply(View view, @NonNull Drawable obj) {
                if (view instanceof FrameLayout) {
                    ((FrameLayout) view).setForeground(obj);
                } else if (Build.VERSION.SDK_INT >= 23) {
                    view.setForeground(obj);
                }
            }
        });
    }

}
