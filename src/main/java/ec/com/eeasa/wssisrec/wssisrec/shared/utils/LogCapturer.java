package ec.com.eeasa.wssisrec.wssisrec.shared.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Utilidad para capturar la salida de consola (System.out y System.err)
 * de forma segura y en el hilo actual (ThreadLocal).
 */
public class LogCapturer {
    private static final ThreadLocal<ByteArrayOutputStream> THREAD_OUT = new ThreadLocal<>();
    private static PrintStream originalOut;
    private static PrintStream originalErr;
    private static boolean initialized = false;

    public static synchronized void start() {
        if (!initialized) {
            originalOut = System.out;
            originalErr = System.err;
            
            System.setOut(new PrintStream(new DoubleOutputStream(originalOut)));
            System.setErr(new PrintStream(new DoubleOutputStream(originalErr)));
            initialized = true;
        }
        THREAD_OUT.set(new ByteArrayOutputStream());
    }

    public static String stopAndGet() {
        ByteArrayOutputStream baos = THREAD_OUT.get();
        THREAD_OUT.remove();
        try {
            return baos != null ? baos.toString("UTF-8") : "";
        } catch (java.io.UnsupportedEncodingException e) {
            return baos != null ? baos.toString() : "";
        }
    }

    private static class DoubleOutputStream extends OutputStream {
        private final PrintStream original;

        public DoubleOutputStream(PrintStream original) {
            this.original = original;
        }

        @Override
        public void write(int b) throws IOException {
            original.write(b);
            ByteArrayOutputStream baos = THREAD_OUT.get();
            if (baos != null) {
                baos.write(b);
            }
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            original.write(b, off, len);
            ByteArrayOutputStream baos = THREAD_OUT.get();
            if (baos != null) {
                baos.write(b, off, len);
            }
        }
    }
}
