package org.motechproject.nms.imi.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.nms.props.domain.DayOfTheWeek;

@Entity(tableName = "nms_imi_retries")
public class CallRetry {
    @Field
    private String subscriptionId;

    @Field
    private Long msisdn;

    @Field
    private DayOfTheWeek dayOfTheWeek;

    @Field
    private CallStage callStage;

    @Field
    private String languageLocationCode;

    @Field
    private String circle;

    @Field
    private String subscriptionOrigin;

    public CallRetry() { }

    public CallRetry(String subscriptionId, Long msisdn, DayOfTheWeek dayOfTheWeek, CallStage callStage,
                     String languageLocationCode, String circle, String subscriptionOrigin) {
        this.subscriptionId = subscriptionId;
        this.msisdn = msisdn;
        this.dayOfTheWeek = dayOfTheWeek;
        this.callStage = callStage;
        this.languageLocationCode = languageLocationCode;
        this.circle = circle;
        this.subscriptionOrigin = subscriptionOrigin;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public Long getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(Long msisdn) {
        this.msisdn = msisdn;
    }

    public DayOfTheWeek getDayOfTheWeek() {
        return dayOfTheWeek;
    }

    public void setDayOfTheWeek(DayOfTheWeek dayOfTheWeek) {
        this.dayOfTheWeek = dayOfTheWeek;
    }

    public CallStage getCallStage() {
        return callStage;
    }

    public void setCallStage(CallStage callStage) {
        this.callStage = callStage;
    }

    public String getLanguageLocationCode() {
        return languageLocationCode;
    }

    public void setLanguageLocationCode(String languageLocationCode) {
        this.languageLocationCode = languageLocationCode;
    }

    public String getCircle() {
        return circle;
    }

    public void setCircle(String circle) {
        this.circle = circle;
    }

    public String getSubscriptionOrigin() {
        return subscriptionOrigin;
    }

    public void setSubscriptionOrigin(String subscriptionOrigin) {
        this.subscriptionOrigin = subscriptionOrigin;
    }

    @Override
    public String toString() {
        return "CallRetry{" +
                "subscriptionId='" + subscriptionId + '\'' +
                ", msisdn=" + msisdn +
                ", dayOfTheWeek=" + dayOfTheWeek +
                ", callStage=" + callStage +
                ", languageLocationCode='" + languageLocationCode + '\'' +
                ", circle='" + circle + '\'' +
                ", subscriptionOrigin='" + subscriptionOrigin + '\'' +
                '}';
    }
}
