package com.useriq.rdp;


import android.util.Log;

import com.useriq.Logger;
import com.useriq.SimpleRPC;
import com.useriq.WSTransport;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;

/**
 * @author sudhakar
 * @created 04-Apr-2018
 */
public class RDPMain {
    private static final String SERVER_PATH = "/data/local/tmp/unfold-rdp.jar";
    private static final Logger logger = Logger.init(RDPMain.class.getSimpleName());
    private static String version = "1.3";

    private RDPMain() { }

    public static void start(final Options options) throws IOException {
        System.out.println("version: " + version);
        Log.d("RDPMain", options.toString());
        Device device = new Device(options);

        HashMap<String, String> headers = new HashMap<>();
        headers.put("emu-id", options.emuId);

        WSTransport wsTransport = new WSTransport() {
            @Override
            public URI getHost() throws Exception {
                return new URI(options.host);
            }
        };

        wsTransport.setHeaders(headers);
        final SimpleRPC rpc = new SimpleRPC(wsTransport, new RDPService(device));

        device.setRotationListener(new Device.RotationListener() {
            @Override
            public void onRotationChanged(int rotation) {
                try {
                    System.out.println("onRotationChanged: " + rotation);
                    rpc.notify("onRotationChanged", rotation);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        try {
            wsTransport.start();
        } finally {
            wsTransport.cancel();
        }
    }


    private static Options createOptions(String... args) {
        if (args.length != 4) {
            throw new IllegalArgumentException("Expecting 5 parameters");
        }

        String host = args[0];
        String emuId = args[1];
        int maxSize = Integer.parseInt(args[2]) & ~7; // multiple of 8
        int bitRate = Integer.parseInt(args[3]);

        return new Options(host, emuId, maxSize, bitRate);
    }

    private static void unlinkSelf() {
        try {
            new File(SERVER_PATH).delete();
        } catch (Exception e) {
            logger.e("Cannot unlink server", e);
        }
    }

    public static void main(String... args) throws Exception {
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                logger.e("Exception on thread " + t, e);
            }
        });

        unlinkSelf();
        Options options = createOptions(args);
        logger.i(options.toString());
        start(options);
    }
}
