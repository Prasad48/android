package com.unfold.xposed;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

@SuppressLint("ShowToast")
public class CreateBuilProp {
    FakeBuilProp device;
    Context myContext;

    public CreateBuilProp(Context ctx, FakeBuilProp d) {
        this.myContext = ctx;
        this.device = d;
    }

    public void newBuild() {
        try {
            Writer out = new OutputStreamWriter(new FileOutputStream("/data/data/" + myContext.getPackageName() + "/build.prop.new"));
            out.write(new BuilInfo(this.device).CreateBuild());
            out.close();

            Runtime.getRuntime().exec("su -c cp data/data/" + myContext.getPackageName() + "/build.prop.new  data/.bluestacks.prop");
        } catch (Exception e) {
            Toast.makeText(this.myContext, e.toString(), Toast.LENGTH_LONG).show();
        }
    }
}
