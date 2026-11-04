package org.jahia.community.buggyapp.common;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

/**
 * Stops every background scenario when the module is stopped or redeployed. Without it, a running
 * {@code native-memory-leak} or {@code background-noise} would keep its daemon thread (and what it holds) after a
 * redeploy, out of reach of {@code --stop}, whose new bundle class loader starts with no running scenario.
 */
@Component(immediate = true, service = BackgroundScenarioCleaner.class)
public class BackgroundScenarioCleaner {

    @Deactivate
    void deactivate() {
        BackgroundScenario.stopAll();
    }
}
