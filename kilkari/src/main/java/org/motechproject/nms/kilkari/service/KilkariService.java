package org.motechproject.nms.kilkari.service;

import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionPack;

/**
 *
 */
public interface KilkariService {

    Subscriber getSubscriber(String callingNumber);

    void createSubscription(String callingNumber, int languageLocationCode, String subscriptionPack);

    Subscription getSubscription(String subscriptionId);

    void deactivateSubscription(Subscription subscription);

    SubscriptionPack getSubscriptionPack(String name);

    long getCountSubscriptionPack(String name);
}