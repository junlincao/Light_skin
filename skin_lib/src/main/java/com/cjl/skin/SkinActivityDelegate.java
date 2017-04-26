package com.cjl.skin;

import android.content.Context;
import android.support.v4.util.ArrayMap;
import android.support.v4.view.LayoutInflaterCompat;
import android.support.v4.view.LayoutInflaterFactory;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Activity实现Skinable委托代理类
 *
 * @author CJL
 * @since 2017-04-25
 */
public class SkinActivityDelegate implements LayoutInflaterFactory {

    private AppCompatActivity baseActivity;

    private static final Class<?>[] sConstructorSignature = new Class[]{Context.class, AttributeSet.class};
    private static final Map<String, Constructor<? extends View>> sConstructorMap = new ArrayMap<>();
    private final Object[] mConstructorArgs = new Object[2];

    private SkinActivityDelegate(AppCompatActivity activity) {
        this.baseActivity = activity;
        LayoutInflater layoutInflater = LayoutInflater.from(activity);
        LayoutInflaterCompat.setFactory(layoutInflater, this);
    }

    public static SkinActivityDelegate delegate(AppCompatActivity activity) {
        return new SkinActivityDelegate(activity);
    }

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        if ("ViewStub".equals(name)) {
            return null;
        }
        View view = baseActivity.getDelegate().createView(parent, name, context, attrs);
        if (view == null) {
            view = createViewFromTag(context, name, attrs);
        }

        if (view != null) {
            List<SkinAttr> skinAttrList = getSupportAttrs(attrs, context);
            if (view instanceof ISkinable) {
                skinAttrList.add(AttrManager.CUSTOM_VIEW_ATTR);
            }
            if (!skinAttrList.isEmpty()) {
                SkinManager manager = SkinManager.getInstance();
                manager.addSkinView(view, skinAttrList);

                if (manager.isUsePlugin()) {
                    manager.applyViewSkin(view, skinAttrList);
                }
            }
        }
        return view;
    }


    private View createViewFromTag(Context context, String name, AttributeSet attrs) {
        if (name.equals("view")) {
            name = attrs.getAttributeValue(null, "class");
        }

        try {
            mConstructorArgs[0] = context;
            mConstructorArgs[1] = attrs;

            if (name.indexOf('.') == -1) {
                // try the android.widget prefix first...
                return createView(context, name, "View".equals(name) ? "android.view." : "android.widget.");
            } else {
                return createView(context, name, null);
            }
        } catch (Exception e) {
            // We do not want to catch these, lets return null and let the actual LayoutInflater
            // try
            return null;
        } finally {
            // Don't retain references on context.
            mConstructorArgs[0] = null;
            mConstructorArgs[1] = null;
        }
    }

    private View createView(Context context, String name, String prefix)
            throws ClassNotFoundException, InflateException {
        Constructor<? extends View> constructor = sConstructorMap.get(name);

        try {
            if (constructor == null) {
                // Class not found in the cache, see if it's real, and try to add it
                Class<? extends View> clazz = context.getClassLoader().loadClass(
                        prefix != null ? (prefix + name) : name).asSubclass(View.class);

                constructor = clazz.getConstructor(sConstructorSignature);
                sConstructorMap.put(name, constructor);
            }
            constructor.setAccessible(true);
            return constructor.newInstance(mConstructorArgs);
        } catch (Exception e) {
            // We do not want to catch these, lets return null and let the actual LayoutInflater
            // try
            return null;
        }
    }


    private List<SkinAttr> getSupportAttrs(AttributeSet attrs, Context context) {
        List<SkinAttr> skinAttrs = new ArrayList<>();
        for (int i = 0; i < attrs.getAttributeCount(); i++) {
            String attrName = attrs.getAttributeName(i);
//            String nameSpace = ((XmlResourceParser) attrs).getAttributeNamespace(i);

            String attrValue = attrs.getAttributeValue(i);

            if (attrValue.startsWith("@")) {
                int id;
                try {
                    id = Integer.parseInt(attrValue.substring(1));
                } catch (NumberFormatException e) {
                    continue;
                }
                if (id == 0) {
                    continue;
                }
                String resName = context.getResources().getResourceName(id);
                int entryNameIdx = resName.lastIndexOf('/');
                int typeIdx = resName.indexOf(':');
                String entryName = resName.substring(entryNameIdx + 1);
                String typeName = resName.substring(typeIdx + 1, entryNameIdx);

                AttrManager.IAttr sa = AttrManager.innerGetSupportAttrs().get(attrName);

                if (sa != null) {
                    skinAttrs.add(new SkinAttr(sa, entryName, typeName));
                }
            }
        }
        return skinAttrs;
    }
}
