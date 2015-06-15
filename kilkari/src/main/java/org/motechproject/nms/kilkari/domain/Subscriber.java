package org.motechproject.nms.kilkari.domain;

import org.joda.time.DateTime;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.annotations.Ignore;
import org.motechproject.mds.domain.MdsEntity;
import org.motechproject.nms.region.domain.Circle;
import org.motechproject.nms.region.domain.Language;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Unique;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * A Kilkari subscriber (recipient of the service, i.e. a pregnant woman) essentially identified by her
 * phone number. May represent a beneficiary who was subscribed via IVR or MCTS. In the case of beneficiaries
 * sourced from MCTS, the mother and child fields will be populated with demographic data.
 */
// TODO: Remove maxFetchDepth once https://applab.atlassian.net/browse/MOTECH-1678 is resolved
@Entity(maxFetchDepth = -1, tableName = "nms_subscribers")
public class Subscriber extends MdsEntity {
    @Field
    @Unique
    @Min(value = 1000000000L, message = "callingNumber must be 10 digits")
    @Max(value = 9999999999L, message = "callingNumber must be 10 digits")
    @Column(length = 10, allowsNull = "false")
    private Long callingNumber;

    @Field
    private DateTime dateOfBirth;

    @Field
    private DateTime lastMenstrualPeriod;

    @Field
    private Language language;

    @Field
    private Circle circle;

    //TODO: making this a bi-directional relationship until MOTECH-1638 is fixed. See #31.
    @Field
    @Persistent(mappedBy = "subscriber", defaultFetchGroup = "true")
    private Set<Subscription> subscriptions;

    @Field
    private MctsMother mother;

    @Field
    private MctsChild child;

    public Subscriber() {
        this.subscriptions = new HashSet<>();
    }

    public Subscriber(Long callingNumber) {
        this.callingNumber = callingNumber;
        this.subscriptions = new HashSet<>();
    }

    public Subscriber(Long callingNumber, Language language) {
        this(callingNumber);
        this.language = language;
        this.subscriptions = new HashSet<>();
    }

    public Subscriber(Long callingNumber, Language language, Circle circle) {
        this(callingNumber, language);
        this.circle = circle;
        this.subscriptions = new HashSet<>();
    }

    public Long getCallingNumber() {
        return callingNumber;
    }

    public void setCallingNumber(Long callingNumber) {
        this.callingNumber = callingNumber;
    }

    public DateTime getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(DateTime dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public DateTime getLastMenstrualPeriod() {
        return lastMenstrualPeriod;
    }

    public void setLastMenstrualPeriod(DateTime lastMenstrualPeriod) {
        this.lastMenstrualPeriod = lastMenstrualPeriod;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public Set<Subscription> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(Set<Subscription> subscriptions) {
        this.subscriptions = subscriptions;
    }

    public Circle getCircle() {
        return circle;
    }

    public void setCircle(Circle circle) {
        this.circle = circle;
    }

    @Ignore
    public Set<Subscription> getActiveSubscriptions() {
        Set<Subscription> activeSubscriptions = new HashSet<>();

        Iterator<Subscription> subscriptionIterator = subscriptions.iterator();
        Subscription currentSubscription;
        while (subscriptionIterator.hasNext()) {
            currentSubscription = subscriptionIterator.next();

            if (currentSubscription.getStatus() == SubscriptionStatus.ACTIVE) {
                activeSubscriptions.add(currentSubscription);
            }
        }
        return activeSubscriptions;
    }

    @Ignore
    public Set<Subscription> getAllSubscriptions() {
        // TODO: I have no idea why I need to do this, but returning just this.subscriptions always results in
        // an empty set. Bi-directional relationship bug?
        Set<Subscription> allSubscriptions = new HashSet<>();

        Iterator<Subscription> subscriptionIterator = subscriptions.iterator();
        Subscription currentSubscription;
        while (subscriptionIterator.hasNext()) {
            currentSubscription = subscriptionIterator.next();
            allSubscriptions.add(currentSubscription);
        }
        return allSubscriptions;
    }

    public MctsMother getMother() {
        return mother;
    }

    public void setMother(MctsMother mother) {
        this.mother = mother;
    }

    public MctsChild getChild() {
        return child;
    }

    public void setChild(MctsChild child) {
        this.child = child;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Subscriber that = (Subscriber) o;

        return !(callingNumber != null ? !callingNumber.equals(that.callingNumber) : that.callingNumber != null);

    }

    @Override
    public int hashCode() {
        return callingNumber != null ? callingNumber.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Subscriber{" +
                "id=" + getId() +
                ", circle='" + circle +
                ", subscriptions=" + subscriptions +
                '}';
    }
}
