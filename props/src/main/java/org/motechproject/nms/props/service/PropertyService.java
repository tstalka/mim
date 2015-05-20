package org.motechproject.nms.props.service;

import org.motechproject.nms.props.domain.Service;
import org.motechproject.nms.region.domain.State;

/**
 * Service class that provides access to various service-level configuration parameters.
 */
public interface PropertyService {
    boolean isServiceDeployedInState(Service service, State state);

    /**
     * Gets the maximum number of allowed Kilkari subscriptions
     * @return maximum number of allowed Kilkari subscriptions
     */
    int kilkariSubscriptionCap();
}
