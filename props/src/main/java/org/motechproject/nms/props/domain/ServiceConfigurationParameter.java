package org.motechproject.nms.props.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Unique;
import javax.validation.constraints.NotNull;

@Entity(tableName = "nms_service_configuration_properties")
@Unique(name = "UNIQUE_SERVICE_PARAMETER_COMPOSITE_IDX", members = { "service", "parameterName" })
public class ServiceConfigurationParameter {

    @Field
    @NotNull
    @Column(allowsNull = "false")
    private Service service;

    @Field
    @NotNull
    @Column(allowsNull = "false")
    private String parameterName;

    @Field
    @NotNull
    @Column(allowsNull = "false")
    private String value;

    public ServiceConfigurationParameter(Service service, String parameterName, String value) {
        this.service = service;
        this.parameterName = parameterName;
        this.value = value;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public String getParameterName() {
        return parameterName;
    }

    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
