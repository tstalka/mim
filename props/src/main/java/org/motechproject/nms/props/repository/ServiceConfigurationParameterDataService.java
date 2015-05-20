package org.motechproject.nms.props.repository;

import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.nms.props.domain.Service;
import org.motechproject.nms.props.domain.ServiceConfigurationParameter;

public interface ServiceConfigurationParameterDataService extends MotechDataService<ServiceConfigurationParameter> {
    @Lookup
    ServiceConfigurationParameter findByServiceAndName(@LookupField(name="service") Service service,
                                                       @LookupField(name="parameterName") String parameterName);
}
