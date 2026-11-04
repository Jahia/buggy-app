package org.jahia.community.buggyapp.nativememoryleak;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

/**
 * Releases a direct or memory-mapped buffer immediately, without waiting for the garbage collector.
 *
 * <p>Uses {@code sun.misc.Unsafe#invokeCleaner(ByteBuffer)}, the supported replacement (Java 9+) for the
 * {@code jdk.internal.ref.Cleaner} / {@code sun.nio.ch.DirectBuffer} internals used previously. Those required
 * adding {@code jdk.internal.ref} to {@code org.osgi.framework.bootdelegation}; {@code sun.misc.Unsafe} lives in
 * the exported {@code jdk.unsupported} module and is loaded through the platform class loader, so no OSGi import
 * or framework configuration is needed.</p>
 */
final class DirectBufferCleaner {

    private static final Object UNSAFE;
    private static final Method INVOKE_CLEANER;

    static {
        try {
            final Class<?> unsafeClass = ClassLoader.getPlatformClassLoader().loadClass("sun.misc.Unsafe");
            final Field theUnsafe = unsafeClass.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            UNSAFE = theUnsafe.get(null);
            INVOKE_CLEANER = unsafeClass.getMethod("invokeCleaner", ByteBuffer.class);
        } catch (ReflectiveOperationException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    private DirectBufferCleaner() {
    }

    /**
     * Unmaps / frees {@code buffer}. The buffer must not be used afterwards.
     *
     * @throws IllegalArgumentException if the buffer is not direct, or is a slice or duplicate
     */
    static void clean(ByteBuffer buffer) {
        try {
            INVOKE_CLEANER.invoke(UNSAFE, buffer);
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException("Cannot call Unsafe.invokeCleaner", ex);
        } catch (InvocationTargetException ex) {
            final Throwable cause = ex.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new IllegalStateException("Unsafe.invokeCleaner failed", cause);
        }
    }
}
