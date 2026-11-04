package org.jahia.community.buggyapp.dbconnectionleak;

import java.lang.reflect.Method;
import java.util.OptionalInt;

/**
 * Reads connection pool figures by reflection, so the module does not import pool implementation classes.
 *
 * <p>Jahia's datasource is a Spring {@code DelegatingDataSource} ({@code ReadOnlyModeAwareDataSource}) around the
 * Tomcat JDBC pool ({@code getMaxActive()}, {@code getActive()}); wrappers are followed through
 * {@code getTargetDataSource()}. Other common pools (DBCP2, HikariCP) getter names are tried as well.</p>
 */
final class PoolIntrospection {

    private static final int MAX_WRAPPER_DEPTH = 5;

    private PoolIntrospection() {
    }

    /** Maximum number of connections the pool can lend, if it can be found. */
    static OptionalInt maxActive(Object dataSource) {
        return firstIntGetter(dataSource, "getMaxActive", "getMaxTotal", "getMaximumPoolSize");
    }

    /** Number of connections currently lent, if it can be found. */
    static OptionalInt active(Object dataSource) {
        return firstIntGetter(dataSource, "getActive", "getNumActive");
    }

    private static OptionalInt firstIntGetter(Object dataSource, String... getterNames) {
        Object current = dataSource;
        for (int depth = 0; current != null && depth < MAX_WRAPPER_DEPTH; depth++) {
            for (String getterName : getterNames) {
                final Object value = invokeQuietly(current, getterName);
                if (value instanceof Number) {
                    return OptionalInt.of(((Number) value).intValue());
                }
            }
            current = invokeQuietly(current, "getTargetDataSource");
        }
        return OptionalInt.empty();
    }

    private static Object invokeQuietly(Object target, String methodName) {
        try {
            final Method method = target.getClass().getMethod(methodName);
            return method.invoke(target);
        } catch (ReflectiveOperationException | RuntimeException ex) {
            // Not available on this implementation: try the next getter / wrapper
            return null;
        }
    }
}
