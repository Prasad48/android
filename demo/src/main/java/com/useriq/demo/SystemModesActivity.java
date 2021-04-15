package com.useriq.demo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

public class SystemModesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int flag = getIntent().getIntExtra("flags", -1);
        getWindow().getDecorView().setSystemUiVisibility(flag);
        setContentView(R.layout.activity_system_modes);

        int uiVisibility = getWindow().getDecorView().getSystemUiVisibility();
        boolean isImmersiveModeEnabled = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            isImmersiveModeEnabled = ((uiVisibility | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) == uiVisibility);
        }
        boolean isNavHidden = ((uiVisibility | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == uiVisibility);
        boolean isStatusBarHidden = ((uiVisibility | View.SYSTEM_UI_FLAG_FULLSCREEN) == uiVisibility);
        boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
        boolean hasHomeKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_HOME);

        boolean isNavigationBarAvailable = !(hasBackKey && hasHomeKey);

        Toast.makeText(this, "isImmersiveModeEnabled: " + isImmersiveModeEnabled, Toast.LENGTH_SHORT).show();
        Toast.makeText(this, "isNavHidden: " + isNavHidden, Toast.LENGTH_SHORT).show();
        Toast.makeText(this, "isStatusBarHidden: " + isStatusBarHidden, Toast.LENGTH_SHORT).show();
        Toast.makeText(this, "isNavigationBarAvailable: " + isNavigationBarAvailable, Toast.LENGTH_SHORT).show();
    }
}
