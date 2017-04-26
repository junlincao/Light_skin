# Light_skin
一个轻量级但功能强大的换肤库，一个重新造的轮子...


###集成步骤：
1. 在Application中初始化皮肤：
```
SkinManager.getInstance().init(this);
```

2. Activity继承BaseSkinActivity:
```
public class BaseActivity extends BaseSkinActivity{}
```
   如果你已经继承其它Activity，也可以考虑不继承BaseSkinActivity，只需要在onCreate方法第一行添加
```
SkinActivityDelegate.delegate(this);
```


###皮肤库文件制作
皮肤即为apk文件。皮肤包不用包含代码，只包含资源文件。
将要换肤的资源文件保证与主app中相同类型的资源文件名相同即可。

注意：由于会预读取皮肤R.class文件，需要皮肤包AndroidManifest.xml中的package值需要和applicationId相同。
如果不同，可以在AndroidManifest.xml添加meta-data，value使用AndroidManifest.xml中的package值。如：
```
<meta-data android:name="MANIFEST_PACKAGE" android:value="com.cjl.skinsample1"/>
```


### 自定义控件换肤

自定义控件换肤有2中方案，具体用法见sample

* 方案1：适合所有没有实现ISkinable接口的第三方控件，不用重写控件，只要控件有更换资源的方法即可！
    调用AttrManager addSkinAttr()方法，添加换肤方法
    注意：getAttrName()方法返回的值必须和xml中定义该属性一致.比如 View background在xml中定义为android:background="@drawable/selector_bg"，则getAttrName()方法返回"background"!

```
// 为CustomView1添加换肤属性
AttrManager.addSkinAttr(new AttrManager.ColorAttr() {
    @Override
    public String getAttrName() {
        return "paintTextColor";
    }

    @Override
    public void apply(View view, Integer obj) {
        if (view instanceof CustomView1) {
            ((CustomView1) view).setTextColor(obj);
        }
    }
});
```
* 方案2：自定义控件实现ISkinable接口，在applySkin()方法中处理换肤
```
public class CustomView2 extends View implements ISkinable {
    @Override
    public void applySkin(ResourceManager rm) {
        Integer tColor = rm.getColor("text_color");
        if (tColor != null) {
            setTextColor(tColor);
        }
    }
}
```

### 扩展可换肤的属性

目前添加了一些常用的自动替换资源的属性：
 View background foreground, TextView textColor textColorHint, ImageView src, ListView listSelector divider
可以根据自己项目需要，添加更多属性。
比如：View的backgroundTint是android L 开始添加的，如果需要，可以添加其换肤：
```
// android:backgroundTint="@color/..."
AttrManager.addSkinAttr(new AttrManager.ColorStateListAttr() {
    @Override
    public String getAttrName() {
        return "backgroundTint";
    }

    @Override
    public void apply(View view, ColorStateList obj) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view.setBackgroundTintList(obj);
        }
    }
});
```

甚至，我们可以换肤的同时改变View其它属性：
比如更改padding值
```
xml中：
<TextView
    android:layout_width="match_parent"
    android:layout_height="100dp"
    android:background="@color/color_bg"
    android:padding="@dimen/default_padding"/>
    
// 添加padding值替换   
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
```

