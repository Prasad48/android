package com.useriq;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;

/**
 * @author sudhakar
 * @created 18-Mar-2018
 */
public class SimpleRPCTest {
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    final SimpleRPC server = new SimpleRPC(Piped.serverTransport, new SimpleRPC.IService() {
        @SimpleRPC.Expose
        public long add(long a, long b) {
            return a + b;
        }

        @SimpleRPC.Expose
        public long mul(long a, long b) {
            return a * b;
        }

        @SimpleRPC.Expose
        public void noti(String arg) {
            System.out.println("noti: " + arg);
        }

        @SimpleRPC.Expose
        public void willThrow() throws Exception {
            Map<String, String> err = new HashMap<>();
            err.put("name", "UNABLE_TO_COMPUTE");
            err.put("desc", "Compute error occurred");
            throw new SimpleRPC.ResponseError(err);
        }
    });


    @Test
    public void request() throws Exception {
        final SimpleRPC client = new SimpleRPC(Piped.clientTransport, new SimpleRPC.IService() {
        });

        long sum = (long) client.request("add", 3, 2);
        client.notify("noti", "hello");
        assertEquals(sum, 5);

        exception.expect(SimpleRPC.ResponseError.class);
        try {
            Object willThrow = client.request("willThrow");
        } catch (SimpleRPC.ResponseError e) {
            Map<String, String> details = (Map<String, String>) e.getDetails();
            System.out.println(details);
        }
    }

    @Test
    public void notifyFn() throws Exception {
    }
}