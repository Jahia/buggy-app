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
 *
 * <p>The run's {@code ClassPool} deliberately keeps every generated {@code CtClass} while the leak runs: in a heap
 * dump this is the trail (javassist objects retained by this thread) that the MAT step of the training exercise
 * follows. The pool is local to the run, so that heap is released when the run ends.</p>
 */
public class MetaspaceLeakThread extends StoppableThread {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetaspaceLeakThread.class);
    private static final AtomicLong RUN_SEQUENCE = new AtomicLong();
    private static final int LOG_EVERY = 50_000;
    private final String classNamePrefix;
    private volatile long classesCreated;
    private volatile Exception failure;

    public MetaspaceLeakThread() {
        this.classNamePrefix = MetaspaceLeakThread.class.getPackageName() + ".MetaspaceObject"
                + RUN_SEQUENCE.incrementAndGet() + "_";
    }

    @Override
    public void run() {
        // Private pool per run: retains the CtClass objects during the run only (see class Javadoc)
        final ClassPool classPool = new ClassPool(true);
        try {
            while (keepRunning()) {
                final CtClass ctClass = classPool.makeClass(getClassName(classesCreated));
                ctClass.toClass(MetaspaceLeakThread.class);
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
