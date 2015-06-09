package org.motechproject.nms.region.domain;

import org.motechproject.mds.annotations.Cascade;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.domain.MdsEntity;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Element;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Unique;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

/**
 * This class Models data for State location records
 */
@Entity(tableName = "nms_states")
public class State extends MdsEntity {

    @Field
    @Column(allowsNull = "false")
    @NotNull
    private String name;

    @Field
    @Unique
    @Column(allowsNull = "false")
    @NotNull
    private Long code;

    @Field
    @Cascade(delete = true)
    @Persistent(mappedBy = "state", defaultFetchGroup = "true")
    private Set<District> districts;

    @Field
    @Cascade(delete = true)
    @Persistent(table = "nms_states_join_circles", mappedBy = "states", defaultFetchGroup = "true")
    @Join(column = "state_id")
    @Element(column = "circle_id")
    private Set<Circle> circles;

    public State() {
        this.districts = new HashSet<>();
        this.circles = new HashSet<>();
    }

    public State(String name, Long code) {
        this.name = name;
        this.code = code;
        this.districts = new HashSet<>();
        this.circles = new HashSet<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getCode() {
        return code;
    }

    public void setCode(Long code) {
        this.code = code;
    }

    public Set<District> getDistricts() {
        return districts;
    }

    public void setDistricts(Set<District> districts) {
        this.districts = districts;
    }

    public Set<Circle> getCircles() {
        return circles;
    }

    public void setCircles(Set<Circle> circles) {
        this.circles = circles;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        State state = (State) o;

        if (name != null ? !name.equals(state.name) : state.name != null) {
            return false;
        }
        return !(code != null ? !code.equals(state.code) : state.code != null);

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (code != null ? code.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "State{" +
                "name='" + name + '\'' +
                ", code=" + code +
                '}';
    }
}
