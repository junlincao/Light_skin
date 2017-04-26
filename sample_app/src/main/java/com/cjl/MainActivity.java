package com.cjl;

import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.Toast;

import com.cjl.skin.BaseSkinActivity;
import com.cjl.skin.SkinManager;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends BaseSkinActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        RadioButton btnDefault = (RadioButton) findViewById(R.id.btn_skin_default);
        RadioButton btnCustom1 = (RadioButton) findViewById(R.id.btn_skin_custom1);
        RadioButton btnCustom2 = (RadioButton) findViewById(R.id.btn_skin_custom2);

        btnDefault.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    SkinManager.getInstance().useDefaultSkin(getApplicationContext());
                }
            }
        });
        btnCustom1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    try {
                        changeToSkin(R.raw.sample_skin1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        btnCustom2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    try {
                        changeToSkin(R.raw.sample_skin2);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });


    }

    private void changeToSkin(int skinRawId) throws Exception {
        InputStream is = getResources().openRawResource(skinRawId);

        File skinFile = new File(getCacheDir(), "skin_" + skinRawId + ".apk");
        FileOutputStream fos = new FileOutputStream(skinFile);
        byte[] buf = new byte[1024];
        int len;
        while ((len = is.read(buf)) != -1) {
            fos.write(buf, 0, len);
        }
        fos.flush();

        closeSilent(is);
        closeSilent(fos);

        SkinManager.getInstance().changeSkin(getApplicationContext(), skinFile.getAbsolutePath(), new SkinManager.ISkinChangingCallback() {
            @Override
            public void onStart() {

            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(getApplicationContext(), "change skin failed！", Toast.LENGTH_SHORT).show();
                Log.e("---", "change skin failed！", e);
            }

            @Override
            public void onComplete() {

            }
        });
    }

    private void closeSilent(Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException e) {
            // null
        }
    }
}
