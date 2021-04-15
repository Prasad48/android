package com.useriq;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author sudhakar
 * @created 12-May-2018
 */

/**
 * Logger to unify logging in rdp & in SDK <br/><br/>
 *
 * <b>Usage:</b><br/>
 *
 * <pre>
 * private static Logger log = Logger.init(MyClass.class.getSimpleName());
 * ...
 * log.e("some err", extraObj, ...)
 * </pre>
 * <br/>
 * <p>
 * Logger global config <br/>
 * <pre>
 * Logger.addPrinter(..)
 * Logger.setPrinter(..)
 * Logger.setLevel(..)
 * </pre>
 */
public class Logger {
    private static Level level = Level.DEBUG;
    private static Set<Printer> printers = new HashSet<>();

    public static final Printer SYSTEM = new Printer() {
        @Override
        public void print(Level level, String tag, String msg, Throwable throwable) {
            String str = String.format("%s: %s: %s\n", level, tag, msg);

            if (throwable != null) {
                String trace = getStackTrace(throwable);
                System.err.println(str);
                System.err.println(trace);
            } else {
                System.out.println(str);
            }
        }
    };

    static {
        // We're adding default system printer
        // Use setPrinter to replace defaults (in SDK)
        Logger.addPrinters(SYSTEM);
    }

    private final String tag;

    private Logger(String tag) {
        this.tag = tag;
    }

    public static Logger init(String tag) {
        return new Logger(tag);
    }

    public boolean isAtleast(Logger.Level other) {
        return Logger.level.compareTo(other) <= 0;
    }

    public static void clear() {
        printers.clear();
    }

    public static void setLevel(Level newLevel) {
        Logger.level = newLevel;
    }

    public static void addPrinters(Printer... printers) {
        Logger.printers.addAll(Arrays.asList(printers));
    }

    public void d(String msg) {
        log(Level.DEBUG, tag, msg, null);
    }

    public void i(String msg) {
        log(Level.INFO, tag, msg, null);
    }

    public void w(String msg) {
        log(Level.WARNING, tag, msg, null);
    }

    public void e(String msg, Throwable throwable) {
        log(Level.ERROR, tag, msg, throwable);
    }

    private void log(Level level, String tag, String msg, Throwable throwable) {
        if (!isAtleast(level)) return;

        for (Printer printer : printers) {
            printer.print(level, tag, msg, throwable);
        }
    }

    /**
     * <p>Gets the stack trace from a Throwable as a String.</p>
     *
     * <p>The result of this method vary by JDK version as this method
     * uses {@link Throwable#printStackTrace(java.io.PrintWriter)}.
     * On JDK1.3 and earlier, the cause exception will not be shown
     * unless the specified throwable alters printStackTrace.</p>
     *
     * @param throwable the <code>Throwable</code> to be examined
     * @return the stack trace as generated by the exception's
     * <code>printStackTrace(PrintWriter)</code> method
     */
    private static String getStackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        throwable.printStackTrace(pw);
        return sw.getBuffer().toString();
    }

    public enum Level {
        DEBUG,
        INFO,
        WARNING,
        ERROR
    }

    public interface Printer {
        void print(Level level, String tag, String msg, Throwable throwable);
    }
}
