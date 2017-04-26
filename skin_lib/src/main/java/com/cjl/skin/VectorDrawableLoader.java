package com.cjl.skin;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.util.WeakHashMap;

/**
 * read VectorDrawable for Android which version lower than 23
 *
 * @author CJL
 * @since 2016-07-04
 */
public class VectorDrawableLoader {

    private static TypedValue typedValue = new TypedValue();

    // SparseArray 中 byte 值 0不是VectorDrawable 1 是VectorDrawable，2 AnimatedVectorDrawable
    private static WeakHashMap<Context, SparseArray<Byte>> sTypeCache = new WeakHashMap<>(2, 0.5f);

    /**
     * @return 如果resId是VectorDrawable，则返回VectorDrawable，否则返回null
     */
    public static Drawable loadDrawable(Context ctx, Resources res, int resId) {
        if (Build.VERSION.SDK_INT >= 21) {
            return null;
        }
        Context context = ctx.getApplicationContext();
        SparseArray<Byte> cache = sTypeCache.get(context);
        if (cache == null) {
            cache = new SparseArray<>();
            sTypeCache.put(context, cache);
        }

        byte cacheType = cache.get(resId, (byte) -1);

        if (cacheType == 0) {
            return null;
        } else if (cacheType == 1) {
            return VectorDrawableCompat.create(res, resId, context.getTheme());
        } else if (cacheType == 2) {
            return AnimatedVectorDrawableCompat.create(context, resId);
        } else if (cacheType == -1) { // 还未缓存ID
            final TypedValue tv = typedValue;
            try {
                res.getValue(resId, tv, true);
            } catch (Exception e) { // 貌似小米改动了资源ID什么的，这地方可能报错
                return null;
            }

            if (tv.string != null && tv.string.toString().endsWith(".xml")) {
                // If the resource is an XML file, let's try and parse it
                try {
                    final XmlPullParser parser = res.getXml(resId);
                    final AttributeSet attrs = Xml.asAttributeSet(parser);
                    int type;
                    while ((type = parser.next()) != XmlPullParser.START_TAG && type != XmlPullParser.END_DOCUMENT) {
                        // Empty loop
                    }
                    if (type != XmlPullParser.START_TAG) {
                        throw new XmlPullParserException("No start tag found");
                    }
                    Drawable dr = null;

                    final String tagName = parser.getName();

                    if ("vector".equals(tagName)) {
                        cache.put(resId, (byte) 1);
                        dr = VectorDrawableCompat.createFromXmlInner(res, parser, attrs, context.getTheme());
                    } else if ("animated-vector".equals(tagName)) {
                        cache.put(resId, (byte) 2);
                        if (Build.VERSION.SDK_INT >= 11) {
                            dr = AnimatedVectorDrawableCompat.createFromXmlInner(context, res, parser, attrs,
                                    context.getTheme());
                        }
                    }
                    if (dr != null) {
                        dr.setChangingConfigurations(tv.changingConfigurations);
                    }
                    return dr;
                } catch (Exception e) {
                    Log.e(Constants.TAG, "Exception while inflating drawable" + e.getMessage());
                }
            } else {
                cache.put(resId, (byte) 0);
            }
        }
        return null;
    }
}
