package org.jahia.community.buggyapp.metaspaceleak;

import java.util.concurrent.atomic.AtomicLong;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import org.jahia.community.buggyapp.common.StoppableThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fills the metaspace by generating new classes until stopped.
 *
 * <p>Each run uses its own class name prefix: the previous implementation restarted at
 * {@code MetaspaceObject0} in the shared {@code ClassPool.getDefault()}, so every run after the first died with
 * "frozen class (cannot edit)". Classes are defined in this module's class loader (neighbor class), so the
 * metaspace is reclaimed when the bundle is refreshed or uninstalled, instead of requiring a JVM restart.</p>
 */
public class MetaspaceLeakThread extends StoppableThread {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetaspaceLeakThread.class);
    private static final AtomicLong RUN_SEQUENCE = new AtomicLong();
    private static final int LOG_EVERY = 50_000;
    private final String classNamePrefix;
    private volatile long classesCreated;
    private volatile Exception failure;

    public MetaspaceLeakThread() {
        super("buggy-app-metaspace-leak");
        this.classNamePrefix = MetaspaceLeakThread.class.getPackageName() + ".MetaspaceObject"
                + RUN_SEQUENCE.incrementAndGet() + "_";
    }

    @Override
    public void run() {
        // A private pool per run, and each CtClass is detached once loaded: otherwise the pool keeps every
        // CtClass on the heap and the scenario leaks heap as well as metaspace.
        final ClassPool classPool = new ClassPool(true);
        try {
            while (keepRunning()) {
                final CtClass ctClass = classPool.makeClass(getClassName(classesCreated));
                ctClass.toClass(MetaspaceLeakThread.class);
                ctClass.detach();
                classesCreated++;
                if (classesCreated % LOG_EVERY == 0) {
                    LOGGER.info("{} new classes created", classesCreated);
                }
            }
        } catch (CannotCompileException | RuntimeException ex) {
            failure = ex;
            LOGGER.error("Metaspace leak stopped after {} classes", classesCreated, ex);
        }
    }

    String getClassName(long index) {
        return classNamePrefix + index;
    }

    public long getClassesCreated() {
        return classesCreated;
    }

    Exception getFailure() {
        return failure;
    }
}
