package com.cjl;

import android.app.Application;
import android.support.annotation.NonNull;
import android.view.View;

import com.cjl.skin.AttrManager;
import com.cjl.skin.ResourceManager;
import com.cjl.skin.SkinManager;

/**
 * com.cjl
 *
 * @author CJL
 * @since 2017-04-26
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        SkinManager.getInstance().init(this);

        // 为CustomView1添加换肤属性
        AttrManager.addSkinAttr(new AttrManager.ColorAttr() {
            @Override
            public String getAttrName() {
                return "paintTextColor";
            }

            @Override
            public void apply(View view, @NonNull Integer obj) {
                if (view instanceof CustomView1) {
                    ((CustomView1) view).setTextColor(obj);
                }
            }
        });

        AttrManager.addSkinAttr(new AttrManager.IAttr<Integer>() {
            @Override
            public String getAttrName() {
                return "padding";
            }

            @Override
            public void apply(View view, @NonNull Integer pd) {
                view.setPadding(pd, pd, pd, pd);
            }

            @Override
            public Integer getTypeValue(ResourceManager rm, String resName, @ResourceManager.RES_TYPE String resType) {
                if (ResourceManager.TYPE_DIMENSION.equals(resType)) {
                    return rm.getDimensionPixelSize(resName);
                }
                return null;
            }
        });
    }
}
