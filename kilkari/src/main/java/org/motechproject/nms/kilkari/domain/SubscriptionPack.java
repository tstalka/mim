package org.motechproject.nms.kilkari.domain;

import org.joda.time.DateTime;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.annotations.Ignore;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Unique;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * Models the kinds of message campaign (i.e. pack) a subscriber can subscribe to, for example antenatal or
 * postnatal
 */
@Entity(tableName = "nms_subscription_packs")
public class SubscriptionPack {

    private static final int NUM_RETRY_FOR_1_MSG_PER_WEEK = 3;
    private static final int NUM_RETRY_FOR_2_MSG_PER_WEEK = 1;
    private static final int THREE_MONTHS = 90;
    private static final int DAYS_IN_WEEK = 7;

    @Field
    @Unique
    @Column(allowsNull = "false", length = 100)
    @NotNull
    @Size(min = 1, max = 100)
    private String name;

    @Field
    @Column(allowsNull = "false")
    @NotNull
    private SubscriptionPackType type;

    @Field
    @Column(allowsNull = "false")
    @NotNull
    private int weeks;

    @Field
    @Column(allowsNull = "false")
    @NotNull
    private int messagesPerWeek;

    @Field
    private List<SubscriptionPackMessage> messages;

    public SubscriptionPack(String name, SubscriptionPackType type, int weeks, int messagesPerWeek,
                            List<SubscriptionPackMessage> messages) {
        this.name = name;
        this.type = type;
        this.weeks = weeks;
        this.messagesPerWeek = messagesPerWeek;
        this.messages = messages;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SubscriptionPackType getType() {
        return type;
    }

    public void setType(SubscriptionPackType type) {
        this.type = type;
    }

    public int getWeeks() {
        return weeks;
    }

    public void setWeeks(int weeks) {
        this.weeks = weeks;
    }

    public int getMessagesPerWeek() {
        return messagesPerWeek;
    }

    public void setMessagesPerWeek(int messagesPerWeek) {
        if (messagesPerWeek < 1 || messagesPerWeek > 2) {
            throw new IllegalArgumentException(
                    "Subscription packs may not have fewer than one or more than two messages per week.");
        }
        this.messagesPerWeek = messagesPerWeek;
    }

    public List<SubscriptionPackMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<SubscriptionPackMessage> messages) {
        this.messages = messages;
    }

    public boolean isReferenceDateValidForPack(DateTime date) {
        if (date.isAfterNow()) {
            return false;
        }

        int packLengthInDays = weeks * DAYS_IN_WEEK;
        DateTime startDate = date;

        if (type == SubscriptionPackType.PREGNANCY) {
            startDate = date.plusDays(THREE_MONTHS);
        }

        return startDate.plusDays(packLengthInDays).isAfterNow();
    }

    @Ignore
    public int retryCount() {
        //See SRS 6.2.2 - Figure 7
        if (messagesPerWeek == 1) {
            return NUM_RETRY_FOR_1_MSG_PER_WEEK;
        } else {
            return NUM_RETRY_FOR_2_MSG_PER_WEEK;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SubscriptionPack that = (SubscriptionPack) o;

        return name.equals(that.name);

    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
