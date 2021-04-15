package com.useriq.sdk.capture;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import com.useriq.sdk.UIRootView;

import java.nio.IntBuffer;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.opengles.GL10;

import static android.graphics.Bitmap.Config.ARGB_8888;
import static android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND;

/**
 *
 * Derived from https://github.com/tarek360/Instacapture/blob/master/instacapture/src/main/java/com/tarek360/instacapture/screenshot/ScreenshotTaker.kt
 */
public class ScreenImg {
    private static final String TAG = ScreenImg.class.getSimpleName();

    // Draw all the views including the background (eg: dialog)
    static Bitmap capture(final List<ViewRoot> viewRoots, int w, int h) throws InterruptedException {
        final Bitmap bitmap = Bitmap.createBitmap(w, h, ARGB_8888);

        // We need to do it in main thread
        Looper mainLooper = Looper.getMainLooper();

        if (Looper.myLooper() == mainLooper) {
            drawRootsToBitmap(viewRoots, bitmap);
        } else {
            final CountDownLatch latch = new CountDownLatch(1);
            new Handler(mainLooper).post(new Runnable() {
                @Override
                public void run() {
                    try {
                        drawRootsToBitmap(viewRoots, bitmap);
                    } finally {
                        latch.countDown();
                    }
                }
            });

            latch.await();
        }

        return bitmap;
    }

    private static void drawRootsToBitmap(List<ViewRoot> viewRoots, Bitmap bitmap) {
        for (ViewRoot rootData : viewRoots) {
            drawRootToBitmap(rootData, bitmap);
        }
    }

    private static void drawRootToBitmap(ViewRoot config, Bitmap bitmap) {
        // now only dim supported
        if ((config.lParams.flags & FLAG_DIM_BEHIND) == FLAG_DIM_BEHIND) {
            Canvas dimCanvas = new Canvas(bitmap);

            int alpha = (int) (255 * config.lParams.dimAmount);
            dimCanvas.drawARGB(alpha, 0, 0, 0);
        }

        Canvas canvas = new Canvas(bitmap);
        canvas.translate(config.winFrame.left, config.winFrame.top);
        config.view.draw(canvas);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            drawUnDrawableViews(config.view, canvas);
    }

    private static void drawUnDrawableViews(View v, Canvas canvas) {
        if (!(v instanceof ViewGroup))
            return;

        ViewGroup viewGroup = (ViewGroup) v;
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);

            drawUnDrawableViews(child, canvas);

            if (child instanceof TextureView) {
                drawTextureView((TextureView) child, canvas);
            }

            if (child instanceof GLSurfaceView) {
                drawGLSurfaceView((GLSurfaceView) child, canvas);
            }
        }
    }

    private static void drawGLSurfaceView(GLSurfaceView surfaceView, Canvas canvas) {

        if (surfaceView.getWindowToken() != null) {
            int[] location = new int[2];

            surfaceView.getLocationOnScreen(location);
            final int width = surfaceView.getWidth();
            final int height = surfaceView.getHeight();

            final int x = 0;
            final int y = 0;
            int[] b = new int[width * (y + height)];

            final IntBuffer ib = IntBuffer.wrap(b);
            ib.position(0);

            //To wait for the async call to finish before going forward
            final CountDownLatch countDownLatch = new CountDownLatch(1);
            surfaceView.queueEvent(new Runnable() {
                @Override
                public void run() {
                    EGL10 egl = (EGL10) EGLContext.getEGL();
                    egl.eglWaitGL();
                    GL10 gl = (GL10) egl.eglGetCurrentContext().getGL();

                    gl.glFinish();

                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    gl.glReadPixels(x, 0, width, y + height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, ib);
                    countDownLatch.countDown();
                }
            });

            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            int[] bt = new int[width * height];
            int i = 0;
            for (int k = 0; i < height; k++) {
                for (int j = 0; j < width; j++) {
                    int pix = b[(i * width + j)];
                    int pb = pix >> 16 & 0xFF;
                    int pr = pix << 16 & 0xFF0000;
                    int pix1 = pix & 0xFF00FF00 | pr | pb;
                    bt[((height - k - 1) * width + j)] = pix1;
                }
                i++;
            }

            Bitmap sb = Bitmap.createBitmap(bt, width, height, Bitmap.Config.ARGB_8888);
            Paint paint = new Paint();
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_ATOP));
            canvas.drawBitmap(sb, location[0], location[1], paint);
            sb.recycle();
        }
    }

    private static void drawTextureView(TextureView textureView, Canvas canvas) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH)
            return;

        int[] textureViewLocation = new int[2];
        textureView.getLocationOnScreen(textureViewLocation);
        Bitmap textureViewBitmap = textureView.getBitmap();
        if (textureViewBitmap != null) {
            Paint paint = new Paint();
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_ATOP));
            canvas.drawBitmap(textureViewBitmap, textureViewLocation[0], textureViewLocation[1], paint);
            textureViewBitmap.recycle();
        }
    }

}
