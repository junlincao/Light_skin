package com.cjl.skin;

/**
 * 自定义View切换主题样式
 *
 * @author CJL
 * @since 2016-06-27
 */
public interface ISkinable {

    /**
     * 从主题资源中切换背景、颜色什么的
     *
     * @param rm
     *            主题资源
     */
    void applySkin(ResourceManager rm);
}
