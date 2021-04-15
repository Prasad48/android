package com.useriq.sdk;

import android.content.Context;
import android.content.res.AssetManager;

import com.useriq.Logger;
import com.useriq.MPack;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.useriq.sdk.util.Utils.readAll;

/**
 * @author sudhakar
 * @created 03-Oct-2018
 */
class SyncDataCache {
    static final String UIQ_DIR = "uiq";
    private static final Logger logger = Logger.init(SyncDataCache.class.getSimpleName());
    private static final String SYNC_DATA_PACK_FILE = "sync-data.pack";
    private final String uiqDir;

    SyncDataCache(String uiqDir) {
        this.uiqDir = uiqDir;
    }

    /**
     * Bootstrap tries to load sync data from bundled assets.
     * Expected files:
     * <ol>
     *  <li>/uiq/sync-data.pack or /uiq/sync-data.json</li>
     *  <li>/uiq/assets/*</li>
     * </ol>
     *
     * @param ctx application context
     * @return SyncData
     * @throws IOException when unable to ready/copy
     */
    Map<String, Object> bootstrap(Context ctx) throws IOException {
        Map<String, Object> bundledData = null;

        String[] list = ctx.getAssets().list(UIQ_DIR);
        List<String> assetList = list != null ? Arrays.asList(list) : new ArrayList<String>();

        try {
            // read bundled data from app if present
            String packFile = UIQ_DIR + "/" + SYNC_DATA_PACK_FILE;
            if(assetList.contains(SYNC_DATA_PACK_FILE)) {
                InputStream in = ctx.getAssets().open(packFile);;
                bundledData = (Map<String, Object>) MPack.decode(readAll(in));
            }
        } catch (Exception e) {
            logger.e("bootstrap() load from sync-data failed", e);
        }

        File file = new File(uiqDir, SYNC_DATA_PACK_FILE);
        Map<String, Object> cachedData = new HashMap<>();;

        if(file.exists()) {
            byte[] bytes = readFileToBytes(file);
            if(bytes.length != 0) {
                cachedData = (Map<String, Object>) MPack.decode(bytes);
            }
        } else {
            createSyncFile(file);
            cachedData = new HashMap<>();
        }

        if(bundledData == null) return cachedData;

        long cachedVersion = -1L;
        long bundledVersion = -1L;

        if(cachedData.containsKey("version")) {
            cachedVersion = ((Number) cachedData.get("version")).longValue();
        }

        if(bundledData.containsKey("version")) {
            bundledVersion = ((Number) bundledData.get("version")).longValue();
        }

        if(bundledVersion > cachedVersion) {
            AssetManager am = ctx.getAssets();
            String appDir = ctx.getFilesDir().getAbsolutePath() + "/" + UIQ_DIR;
            copyAssetFolder(am, UIQ_DIR, appDir);

            return bundledData;
        }

        return cachedData;
    }

    void save(Map<String, Object> data) throws IOException {
        Map<String, Object> combinedData = new HashMap<>();

        File file = new File(uiqDir, SYNC_DATA_PACK_FILE);
        if(file.exists()) {
            byte[] bytes = readFileToBytes(file);
            if(bytes.length != 0) {
                combinedData = (Map<String, Object>) MPack.decode(bytes);
            }
        } else {
            createSyncFile(file);
        }
        combinedData.putAll(data);
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(MPack.encode(combinedData));
        fos.close();
    }

    void addMockData(Map<String, Object> data) {
        Map<String, Object> qGroup = new HashMap<>();
        qGroup.put("id", "Group1");
        qGroup.put("name", "Group 1");

        List<Map<String, Object>> qList = new ArrayList<>();
        Map<String, Object> qn = new HashMap<>();
        qn.put("id", "qn1");
        qn.put("name", "This is the first question under group 1 for testing purpose");

        List<Map<String, Object>> qDescList = new ArrayList<>();
        Map<String, Object> desc1 = new HashMap<>();
        desc1.put("type", "rtf");
        desc1.put("value", "<b><i>Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean massa. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus.</i></b>");
        Map<String, Object> desc2 = new HashMap<>();
        desc2.put("type", "rtf");
        desc2.put("value", "<b><i>Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean massa. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus.</i></b>");

        qDescList.add(desc1);
        qDescList.add(desc2);
        qn.put("desc", qDescList);
        qList.add(qn);
        qGroup.put("questions", qList);

        List<Map<String, Object>> qGroupList = new ArrayList<>();
        qGroupList.add(qGroup);


        Map<String, Object> ctxHelp = new HashMap<>();
        ctxHelp.put("id", "Group1");
        ctxHelp.put("screenId", null);

        Map<String, Object> ctxHelpItm1 = new HashMap<>();
        ctxHelpItm1.put("type", "question");
        ctxHelpItm1.put("id", "qn1");

        List<Map<String, Object>> ctxHelpItems = new ArrayList<>();
        ctxHelpItems.add(ctxHelpItm1);
        ctxHelp.put("entries", ctxHelpItems);

        List<Map<String, Object>> list = new ArrayList<>();
        list.add(ctxHelp);

        data.put("ctxHelp", list);
        data.put("qGroup", qGroupList);
    }

    private void createSyncFile(File file) {
        try {
            if(file.createNewFile()) {
                logger.i("createSyncFile(): created new file: " + file.getAbsolutePath());
            }
        } catch (IOException e) {
            logger.e("createSyncFile() failed: " + file, e);
        }
    }

    private static byte[] readFileToBytes(File file) throws IOException {
        byte[] bytes = new byte[(int) file.length()];
        DataInputStream dis = new DataInputStream(new FileInputStream(file));
        dis.readFully(bytes);
        dis.close();
        return bytes;
    }

    private static void copyAssetFolder(AssetManager am,
                                        String fromAssetPath, String toPath) throws IOException {
        new File(toPath).mkdirs();

        String[] files = am.list(fromAssetPath);
        if(files == null) {
            throw new IOException("copyAssetFolder: list files is null '" + fromAssetPath + "'");
        }

        for (String file : files) {
            String from = fromAssetPath + "/" + file;
            String to = toPath + "/" + file;

            String[] children = am.list(from);
            boolean isDir = children != null && children.length > 0;

            if(isDir) copyAssetFolder(am, from, to);
            else copyAsset(am, from, to);
        }
    }

    private static void copyAsset(AssetManager am,
                                  String fromAssetPath, String toPath) throws IOException {
        InputStream in;
        OutputStream out;
        in = am.open(fromAssetPath);
        new File(toPath).createNewFile();
        out = new FileOutputStream(toPath);
        copyFile(in, out);
        in.close();
        out.flush();
        out.close();
    }

    private static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }
}
