package com.useriq;


import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.useriq.RPCErrors.internalErr;
import static com.useriq.RPCErrors.noMethod;
import static com.useriq.RPCErrors.timeoutErr;

/**
 * SimpleRPC allows to implement methods via SimpleRPC.IService,
 * which can be called remotely from other end. <br/><br/>
 * <p>
 * In addition to it remote methods implemented on the other side,
 * can be called on the rpc instance <br/><br/>
 *
 * <b>Error Handling</b><br/>
 *
 * <ol>
 * <li>Network & remote REQUEST errors are thrown</li>
 * <li>Other errors are logged & sent back</li>
 * </ol>
 *
 * @author sudhakar
 */
public class SimpleRPC implements RPCTransport.MessageHandler {
    private static final byte REQUEST = 0;
    private static final byte RESPONSE = 1;
    private static final byte NOTIFY = 2;

    private static final long MAX_ID = Double.valueOf(Math.pow(2, 31) - 1).longValue();

    private final Map<Long, Pending> map = new ConcurrentHashMap<>();

    private final IService service;
    private final Map<String, Method> rpcMethods = new HashMap<>();
    private final RPCTransport transport;
    private final Logger log = Logger.init(SimpleRPC.class.getSimpleName());

    private long msgId = 0;

    public SimpleRPC(RPCTransport transport, IService service) {
        this.transport = transport;
        this.service = service;
        populateMethods();
        transport.setMsgHandler(this);
    }

    @Override
    public void handleMessage(List packet) throws IOException {
        byte type = ((Long) packet.get(0)).byteValue();

        switch (type) {
            case REQUEST: {
                long msgId = (long) packet.get(1);
                String funcName = (String) packet.get(2);
                System.out.println("funcName: " + funcName);
                Object[] args = ((List) packet.get(3)).toArray();
                System.out.println("args: " + args.toString());

                Method method = rpcMethods.get(funcName);

                if (method == null) {
                    Map<String, Object> err = noMethod(funcName);
                    transport.send(Arrays.asList(RESPONSE, msgId, err, null));
                    log.e(err.toString(), new NoSuchMethodError(funcName));
                    return;
                }

                Object error;
                try {
                    Object result = method.invoke(service, args);
                    transport.send(Arrays.asList(RESPONSE, msgId, null, result));
                    return;
                } catch (InvocationTargetException e) {
                    if (e.getTargetException() instanceof ResponseError) {
                        // If called method throws ResponseError, its expected condition
                        // So we dont print trace
                        ResponseError err = (ResponseError) e.getTargetException();
                        error = err.details;
                        log.e("RESPONSE_ERROR", e);
                    } else {
                        // If called method throws any other error, we print trace
                        // But we mask it when we send across
                        error = internalErr();
                        log.e(error.toString(), e);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    error = e;
                }

                transport.send(Arrays.asList(RESPONSE, msgId, error, null));
                return;
            }
            case RESPONSE: {
                long msgId = (long) packet.get(1);
                Object error = packet.get(2);
                Object result = packet.get(3);
                Pending pending = map.remove(msgId);

                if (pending == null) return; // timed out earlier

                if (error != null) pending.error = new ResponseError("REMOTE_ERROR", error);
                else pending.result = result;

                pending.latch.countDown();
                return;
            }
            case NOTIFY: {
                String funcName = (String) packet.get(1);
                Object[] args = ((List) packet.get(2)).toArray();

                Method method = rpcMethods.get(funcName);

                if (method == null) {
                    Map<String, Object> err = noMethod(funcName);
                    transport.send(Arrays.asList(RESPONSE, msgId, err, null));
                    log.e(err.toString(), new NoSuchMethodError(funcName));
                    return;
                }

                try {
                    method.invoke(service, args);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public Object request(String method, Object... args) throws Exception {
        long id = this.nextId();
        List data = Arrays.asList(REQUEST, id, method, Arrays.asList(args));
        Pending pending = new Pending();
        map.put(id, pending);

        transport.send(data);

        boolean resolved = pending.latch.await(10, TimeUnit.SECONDS);
        map.remove(id);

        if (!resolved) {
            throw new ResponseError("REQUEST_TIMED_OUT", timeoutErr(method));
        }

        if (pending.error != null) throw pending.error;

        return pending.result;
    }

    /**
     * notify Notifies remote method. Not to be confused with
     * java.lang.Object.notify
     *
     * @param method method name
     * @param args   args for method innovation
     */
    public void notify(String method, Object... args) throws Exception {
        List data = Arrays.asList(NOTIFY, method, Arrays.asList(args));
        transport.send(data);
    }

    private Long nextId() {
        return msgId < MAX_ID ? msgId++ : (msgId = 0);
    }

    private void populateMethods() {
        Class type = service.getClass();

        for (Class c = type; c != null; c = c.getSuperclass()) {
            for (Method m : c.getDeclaredMethods()) {

                boolean isValid = (
                        Modifier.isPublic(m.getModifiers()) &&
                                m.isAnnotationPresent(SimpleRPC.Expose.class)
                );

                if (!isValid) continue;

                assertParamTypes(m);

                // Even if public, disable security manager for
                // performance reasons
                m.setAccessible(false);

                rpcMethods.put(m.getName(), m);
            }
        }

        System.out.println(rpcMethods.toString());

    }

    private void assertParamTypes(Method m) {
        Class<?> returnType = m.getReturnType();
        Class<?>[] parameterTypes = m.getParameterTypes();
        Class<?>[] pTypes = new Class[parameterTypes.length + 1];

        System.arraycopy(parameterTypes, 0, pTypes, 0, parameterTypes.length);
        pTypes[parameterTypes.length] = returnType;

        for (Class<?> cls : pTypes) {
            if (cls.isAssignableFrom(Object.class))
                throw new RuntimeException("Object not supported (Use Precise types): " + m.toString());
            if (cls.isAssignableFrom(Integer.class) || cls.isAssignableFrom(int.class))
                throw new RuntimeException("Integer not supported (Use Long): " + m.toString());
            if (cls.isAssignableFrom(Short.class) || cls.isAssignableFrom(short.class))
                throw new RuntimeException("Short not supported (Use Long): " + m.toString());
            if (cls.isAssignableFrom(Character.class) || cls.isAssignableFrom(char.class))
                throw new RuntimeException("Character not supported (Use Long): " + m.toString());
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface Expose {
    }

    public interface IService {
    }

    public static class ResponseError extends Exception {
        private Object details;

        ResponseError(Object details) {
            this.details = details;
        }

        ResponseError(String name, Object details) {
            super(name);
            this.details = details;
        }

        public Object getDetails() {
            return details;
        }
    }

    private static class Pending {
        final private CountDownLatch latch = new CountDownLatch(1);
        private ResponseError error;
        private Object result;
    }
}
