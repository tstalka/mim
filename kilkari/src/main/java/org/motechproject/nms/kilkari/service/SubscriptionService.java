package org.motechproject.nms.kilkari.service;

import org.motechproject.nms.kilkari.domain.InboxCallDetails;
import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionPack;
import org.motechproject.nms.kilkari.domain.SubscriptionMode;
import org.motechproject.nms.language.domain.Language;

/**
 *
 */
public interface SubscriptionService {

    Subscriber getSubscriber(long callingNumber);

    void createSubscription(long callingNumber, Language language, SubscriptionPack subscriptionPack,
                            SubscriptionMode mode);

    Subscription getSubscription(String subscriptionId);

    void deactivateSubscription(Subscription subscription);

    SubscriptionPack getSubscriptionPack(String name);

    //TODO: we'll probably want to move this to a new InboxService once we do more Inbox work in Sprint 2
    long addInboxCallDetails(InboxCallDetails inboxCallDetails);
}
