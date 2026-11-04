package org.jahia.community.buggyapp.nativememoryleak;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Off-heap (native) memory allocation through {@code sun.misc.Unsafe}.
 *
 * <p>{@code sun.misc.Unsafe} lives in the {@code jdk.unsupported} module, which is exported and opened on
 * Java 11 and 17, unlike {@code jdk.internal.*}. It is loaded through the platform class loader so the bundle
 * does not need an OSGi {@code Import-Package} on {@code sun.misc} (which would depend on the framework's
 * boot delegation settings).</p>
 */
final class UnsafeMemory {

    private static final byte FILL_BYTE = 1;
    private static final Object UNSAFE;
    private static final Method ALLOCATE_MEMORY;
    private static final Method SET_MEMORY;
    private static final Method FREE_MEMORY;

    static {
        try {
            final Class<?> unsafeClass = ClassLoader.getPlatformClassLoader().loadClass("sun.misc.Unsafe");
            final Field theUnsafe = unsafeClass.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            UNSAFE = theUnsafe.get(null);
            ALLOCATE_MEMORY = unsafeClass.getMethod("allocateMemory", long.class);
            SET_MEMORY = unsafeClass.getMethod("setMemory", long.class, long.class, byte.class);
            FREE_MEMORY = unsafeClass.getMethod("freeMemory", long.class);
        } catch (ReflectiveOperationException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    private UnsafeMemory() {
    }

    /**
     * Allocates {@code bytes} of native memory and writes to every byte so the pages are really committed
     * (visible in the process RSS and in Native Memory Tracking), not just reserved.
     *
     * @return the address of the allocated block, to be released with {@link #free(long)}
     */
    static long allocateAndTouch(long bytes) {
        final long address = (Long) invoke(ALLOCATE_MEMORY, bytes);
        invoke(SET_MEMORY, address, bytes, FILL_BYTE);
        return address;
    }

    static void free(long address) {
        invoke(FREE_MEMORY, address);
    }

    private static Object invoke(Method method, Object... args) {
        try {
            return method.invoke(UNSAFE, args);
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException("Cannot call Unsafe." + method.getName(), ex);
        } catch (InvocationTargetException ex) {
            final Throwable cause = ex.getCause();
            if (cause instanceof Error) {
                // OutOfMemoryError when malloc fails: let it surface as is
                throw (Error) cause;
            }
            throw new IllegalStateException("Unsafe." + method.getName() + " failed", cause);
        }
    }
}
