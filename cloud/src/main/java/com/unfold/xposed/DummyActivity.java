package com.unfold.xposed;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Toast;

/**
 * Created by manpreet on 25/10/17.
 */

public class DummyActivity extends Activity {

    private void createData() {
        try {
            new CreateBuilProp(DummyActivity.this, new FakeBuilProp(DummyActivity.this)).newBuild();
            //  FakeHardwareInfo.CreatDataCpu(DummyActivity.this);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Create Data ERROR!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //createData();
    }
}
