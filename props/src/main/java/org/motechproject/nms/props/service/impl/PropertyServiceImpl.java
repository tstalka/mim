package org.motechproject.nms.props.service.impl;

import org.motechproject.mds.query.QueryExecution;
import org.motechproject.mds.util.InstanceSecurityRestriction;
import org.motechproject.nms.props.domain.ServiceConfigurationParameter;
import org.motechproject.nms.props.repository.DeployedServiceDataService;
import org.motechproject.nms.props.service.PropertyService;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.props.repository.ServiceConfigurationParameterDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.jdo.Query;

@Service("propertyService")
public class PropertyServiceImpl implements PropertyService {
    private DeployedServiceDataService deployedServiceDataService;
    private ServiceConfigurationParameterDataService serviceConfigurationParameterDataService;
    private static final String KILKARI_CAP_PARAMETER = "SUBSCRIPTION_CAP";

    @Autowired
    public PropertyServiceImpl(DeployedServiceDataService deployedServiceDataService,
                               ServiceConfigurationParameterDataService serviceConfigurationParameterDataService) {
        this.deployedServiceDataService = deployedServiceDataService;
        this.serviceConfigurationParameterDataService = serviceConfigurationParameterDataService;
    }

    @Override
    public boolean isServiceDeployedInState(final org.motechproject.nms.props.domain.Service service, final State state) {
        // Find a state cap by providing a state
        QueryExecution<Long> stateQueryExecution = new QueryExecution<Long>() {
            @Override
            public Long execute(Query query, InstanceSecurityRestriction restriction) {

                query.setFilter("state == _state && service == _service");
                query.declareParameters("org.motechproject.nms.region.domain.State _state, org.motechproject.nms.props.domain.Service _service");
                query.setResult("count(state)");
                query.setUnique(true);

                return (Long) query.execute(state, service);
            }
        };

        Long isWhitelisted = deployedServiceDataService.executeQuery(stateQueryExecution);

        if (isWhitelisted != null && isWhitelisted > 0) {
            return true;
        }

        return false;
    }

    @Override
    public int kilkariSubscriptionCap() {
        ServiceConfigurationParameter kilkariCap =
                serviceConfigurationParameterDataService.findByServiceAndName(
                        org.motechproject.nms.props.domain.Service.KILKARI,
                        KILKARI_CAP_PARAMETER);

        if (kilkariCap == null) {
            return 0;
        }

        try {
            return Integer.valueOf(kilkariCap.getValue());
        } catch(NumberFormatException e) {
            // log/alert
            return 0;
        }
    }
}
