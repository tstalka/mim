package org.motechproject.nms.kilkari.service;

import org.motechproject.nms.kilkari.domain.InboxCallDetails;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionPackMessage;
import org.motechproject.nms.kilkari.exception.NoInboxForSubscriptionException;


/**
 *
 */
public interface InboxService {

    long addInboxCallDetails(InboxCallDetails inboxCallDetails);

    SubscriptionPackMessage getInboxMessage(Subscription subscription) throws NoInboxForSubscriptionException;
}
