package com.cjl.skin;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * BaseActivity
 *
 * @author CJL
 * @since 2017-02-22
 */
public class BaseSkinActivity extends AppCompatActivity implements SkinManager.ISkinChangedListener {

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        SkinActivityDelegate.delegate(this);
        super.onCreate(savedInstanceState);
        SkinManager.getInstance().addChangedListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SkinManager.getInstance().removeChangedListener(this);
    }

    @Override
    public void onSkinChanged(ResourceManager rm) {

    }
}
