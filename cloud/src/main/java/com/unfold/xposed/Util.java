package com.unfold.xposed;

import android.content.Context;
import android.os.Build;
import android.support.v4.view.MotionEventCompat;
import android.widget.Toast;

import java.net.InetAddress;
import java.util.Random;

public class Util {
    public static int randInt(int min, int max) {
        return new Random().nextInt((max - min) + 1) + min;
    }

    public static boolean isOreoOrGreater() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

    public static int inetAddressToInt(InetAddress inetaddress) throws IllegalArgumentException {
        byte[] abyte0 = inetaddress.getAddress();
        if (abyte0.length == 4) {
            return ((((abyte0[3] & MotionEventCompat.ACTION_MASK) << 24) | ((abyte0[2] & MotionEventCompat.ACTION_MASK) << 16)) | ((abyte0[1] & MotionEventCompat.ACTION_MASK) << 8)) | (abyte0[0] & MotionEventCompat.ACTION_MASK);
        }
        throw new IllegalArgumentException("Not an IPv4 address");
    }

    private void createData(Context context) {

        try {
            new CreateBuilProp(context, new FakeBuilProp(context)).newBuild();
            FakeHardwareInfo.CreatDataCpu(context);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Create Data ERROR!", Toast.LENGTH_LONG).show();
        }

    }

}

