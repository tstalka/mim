package org.motechproject.nms.region.domain;

import org.motechproject.mds.annotations.Cascade;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.domain.MdsEntity;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Unique;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

/**
 * This class Models data for HealthBlock location records
 */
@Entity(tableName = "nms_health_blocks")
public class HealthBlock extends MdsEntity {

    @Field
    @Column(allowsNull = "false", length = 35)
    @NotNull
    @Size(min = 1, max = 35)
    private String name;

    @Field
    @Column(allowsNull = "false", length = 50)
    @NotNull
    @Size(min = 1, max = 50)
    private String regionalName;

    @Field
    @Column(allowsNull = "false", length = 50)
    @NotNull
    @Size(min = 1, max = 50)
    private String hq;

    @Field
    @Unique
    @Column(allowsNull = "false")
    @NotNull
    private Long code;

    @Field
    @Column(allowsNull = "false")
    @NotNull
    private Taluka taluka;

    @Field
    @Cascade(delete = true)
    @Persistent(mappedBy = "healthBlock", defaultFetchGroup = "true")
    private Set<HealthFacility> healthFacilities;

    public HealthBlock() {
        this.healthFacilities = new HashSet<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRegionalName() {
        return regionalName;
    }

    public void setRegionalName(String regionalName) {
        this.regionalName = regionalName;
    }

    public String getHq() {
        return hq;
    }

    public void setHq(String hq) {
        this.hq = hq;
    }

    public Long getCode() {
        return code;
    }

    public void setCode(Long code) {
        this.code = code;
    }

    public Taluka getTaluka() {
        return taluka;
    }

    public void setTaluka(Taluka taluka) {
        this.taluka = taluka;
    }

    public Set<HealthFacility> getHealthFacilities() {
        return healthFacilities;
    }

    public void setHealthFacilities(Set<HealthFacility> healthFacilities) {
        this.healthFacilities = healthFacilities;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        HealthBlock other = (HealthBlock) o;

        if (name != null ? !name.equals(other.name) : other.name != null) {
            return false;
        }
        return !(code != null ? !code.equals(other.code) : other.code != null);

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (code != null ? code.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "HealthBlock{" +
                "name='" + name + '\'' +
                ", code=" + code +
                ", taluka=" + taluka +
                '}';
    }
}
