package com.useriq.sdk;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.useriq.Logger;
import com.useriq.sdk.util.NetworkUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Created by smylsamy on 03/09/16.
 * <p>
 * Handles the download of required assets
 */
public class AssetManager {
    private static final Logger logger = Logger.init(AssetManager.class.getSimpleName());
    private static final int MAX_ASSET_SIZE = 150 * 1024; //250kb
    private final String assetFolder;
    private final Object lock = new Object();
    private final Set<String> failedAssets = new HashSet<>();
    private final Set<String> inProgressAssets = new HashSet<>();
    private int totalCount;
    private int finishCount;
    private boolean isStarted = false;
    private ProgressListener listener = null;


    public AssetManager(String assetFolder) {
        this.totalCount = 0;
        this.finishCount = 0;
        this.assetFolder = assetFolder;
    }

    /**
     * Triggers download on each missing asset spanning a new
     * Http.Request per asset. Concurrency is controlled by available
     * threads in Http.
     * <p>
     * Lock ensures only single thread can schedule jobs. Until all
     * missing assets or tried (failed/succeeded), other threads simply wait.
     * So **ensure** not to call `download` from UIThread
     *
     * @param newAssetIds
     */
    public void download(List<String> newAssetIds, ThreadExecutor jobExecutor) {
        if (newAssetIds == null || newAssetIds.size() == 0) {
            if (listener != null) {
                reportProgress();
            }
            return;
        }

        synchronized (lock) {
            isStarted = true;

            File fileDirectory = new File(assetFolder);

            // delete unused files.
            if (fileDirectory.exists()) {
                if (newAssetIds == null || newAssetIds.size() == 0) return;
                // lists all the files into an array
                String[] dirFiles = fileDirectory.list();
                for (String existingAsset : dirFiles) {
                    if (!newAssetIds.contains(existingAsset)) {
                        File asset = new File(assetFolder, existingAsset);
                        if (asset.exists()) {
                            asset.delete();
                        }
                    }
                }

            }

            for (String assetId : newAssetIds) {
                File file = new File(assetFolder, assetId);
                if (file.exists()) {
                    continue;
                }

                if (inProgressAssets.contains(assetId))
                    continue;

                inProgressAssets.add(assetId);
                download(assetId, jobExecutor);
                if (failedAssets.contains(assetId))
                    failedAssets.remove(assetId);
                else
                    this.totalCount++;
            }

            if (totalCount == 0) {
                if (listener != null) {
                    reportProgress();
                }
            }
        }
    }

    private void reportProgress() {
        int percent = totalCount == 0 ? 100
                : (int) Math.floor(finishCount * 100f / totalCount);

        if (listener != null)
            listener.onProgress(percent);

        int failedCount = failedAssets.size();

        if ((failedCount + finishCount) == totalCount) {
            if (listener != null)
                listener.onFinish(failedCount);

            reset();
        }
    }

    private void download(String assetId, ThreadExecutor jobExecutor) {

        DownloadCb downloadCb;
        try {
            downloadCb = new DownloadCb(assetId);
            logger.d("download(): " + assetId);
        } catch (IOException e) {
            logger.e("download(): failed for asset " + assetId, e);
            return;
        }
        new Http.Request("GET", getAssetUrl(assetId)).stream(jobExecutor, downloadCb);
    }

    private void reset() {
        synchronized (lock) {
            totalCount = 0;
            finishCount = 0;
            failedAssets.clear();
            inProgressAssets.clear();
        }
    }

    public static String getAssetUrl(String assetId) {
        return BuildConfig.STATIC_SERVER_URL + "/" + assetId;
    }

    public File getAsset(String assetId) {
        File file = new File(assetFolder, assetId);
        return file.exists() ? file : null;
    }

    private byte[] compressBitmap(byte[] bytes, double scale, int quality) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

        if(bitmap.getByteCount() <= MAX_ASSET_SIZE) return bytes;

        int scaledWidth = (int) (bitmap.getWidth() * scale);
        int scaledHeight = (int) (bitmap.getHeight() * scale);
        bitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true);

        ByteArrayOutputStream imgOut = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, imgOut);

        byte[] outBytes = imgOut.toByteArray();
        logger.d("compressBitmap(): " + bytes.length + " to " + outBytes.length);

        return outBytes;
    }

    public boolean isFinished() {
        return isStarted && totalCount == 0;
    }

    public void setListener(ProgressListener listener) {
        this.listener = listener;
    }

    public interface ProgressListener {
        void onProgress(int percent);

        void onFinish(int failed);
    }

    class DownloadCb implements Http.StreamCb {
        private final String assetId;
        private final File tempFile;
        private final FileOutputStream tempOs;

        public DownloadCb(String assetId) throws IOException {
            this.assetId = assetId;

            String tempFileName = "temp-" + UUID.randomUUID().toString();
            tempFile = new File(assetFolder, tempFileName);

            tempFile.createNewFile();
            tempOs = new FileOutputStream(tempFile);
        }

        @Override
        public void onBytes(byte[] bytes) {
            try {
                tempOs.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void response(Http.Request req, Http.Response res, Exception e) {
            NetworkUtil.closeQuietly(tempOs);

            synchronized (lock) {
                tempFile.delete();

                if (e != null) {
                    failedAssets.add(assetId);
                    logger.e("DownloadCb.response(): failed for asset: " + assetId, e);
                } else {
                    File file = new File(assetFolder, assetId);
                    byte[] data = res.data;

                    if (file.exists()) {
                        logger.d("DownloadCb.response(): file exits for asset: " + assetId);
                        return;
                    }

                    try {
                        file.createNewFile();
                        FileOutputStream fos = new FileOutputStream(file);
                        data = compressBitmap(data, 0.5, 50);
                        fos.write(data);
                        fos.close();

                    } catch (FileNotFoundException e1) {
                        e1.printStackTrace();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }

                    finishCount++;
                }

                inProgressAssets.remove(assetId);
            }
            reportProgress();
        }
    }
}
