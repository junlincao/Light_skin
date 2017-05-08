package com.cjl.skin;

import android.view.View;

/**
 * 换肤属性
 *
 * @author CJL
 * @since 2017-02-22
 */
public class SkinAttr {

    /**
     * 资源名
     */
    public final String resName;

    /**
     * 资源类型
     */
    @ResourceManager.RES_TYPE
    public final String resType;
    /**
     * 资源属性
     */
    public final AttrManager.IAttr attr;

    public SkinAttr(AttrManager.IAttr attr, String resName, @ResourceManager.RES_TYPE String resType) {
        this.resName = resName;
        this.attr = attr;
        this.resType = resType;
    }

    @SuppressWarnings("unchecked")
    public void apply(ResourceManager resourceManager, View view) {
        attr.apply(view, attr.getTypeValue(resourceManager, resName, resType));
    }
}
