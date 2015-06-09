package org.motechproject.nms.api.web;

import org.motechproject.nms.api.web.contract.kilkari.CallDataRequest;
import org.motechproject.nms.api.web.contract.kilkari.InboxCallDetailsRequest;
import org.motechproject.nms.api.web.contract.kilkari.InboxResponse;
import org.motechproject.nms.api.web.contract.kilkari.InboxSubscriptionDetailResponse;
import org.motechproject.nms.api.web.contract.kilkari.SubscriptionRequest;
import org.motechproject.nms.api.web.exception.NotDeployedException;
import org.motechproject.nms.api.web.exception.NotFoundException;
import org.motechproject.nms.kilkari.domain.DeactivationReason;
import org.motechproject.nms.kilkari.domain.InboxCallData;
import org.motechproject.nms.kilkari.domain.InboxCallDetailRecord;
import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionOrigin;
import org.motechproject.nms.kilkari.domain.SubscriptionPack;
import org.motechproject.nms.kilkari.domain.SubscriptionPackMessage;
import org.motechproject.nms.kilkari.exception.NoInboxForSubscriptionException;
import org.motechproject.nms.kilkari.service.InboxService;
import org.motechproject.nms.kilkari.service.SubscriberService;
import org.motechproject.nms.kilkari.service.SubscriptionService;
import org.motechproject.nms.props.domain.Service;
import org.motechproject.nms.props.service.PropertyService;
import org.motechproject.nms.region.domain.Circle;
import org.motechproject.nms.region.domain.Language;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.service.CircleService;
import org.motechproject.nms.region.service.LanguageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * KilkariController
 */
@RequestMapping("/kilkari")
@Controller
public class KilkariController extends BaseController {

    /*
     4.2.5.1.5 Body Elements
     */
    public static final int SUBSCRIPTION_ID_LENGTH = 36;
    public static final Set<String> SUBSCRIPTION_PACK_SET = new HashSet<>(Arrays.asList("48WeeksPack", "72WeeksPack"));

    @Autowired
    private SubscriberService subscriberService;

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private LanguageService languageService;

    @Autowired
    private CircleService circleService;

    @Autowired
    private InboxService inboxService;

    @Autowired
    private PropertyService propertyService;

    /**
     * 4.2.2 Get Inbox Details API
     * IVR shall invoke this API to get the Inbox details of the beneficiary identified by ‘callingNumber’.
     * /api/kilkari/inbox?callingNumber=1111111111&callId=123456789123456&languageLocationCode=10
     *
     */
    @RequestMapping("/inbox")
    @ResponseBody
    public InboxResponse getInboxDetails(@RequestParam(required = false) Long callingNumber,
                                         @RequestParam(required = false) Long callId) {

        StringBuilder failureReasons = validate(callingNumber, callId);
        if (failureReasons.length() > 0) {
            throw new IllegalArgumentException(failureReasons.toString());
        }

        Subscriber subscriber = subscriberService.getSubscriber(callingNumber);
        if (subscriber == null) {
            throw new NotFoundException(String.format(NOT_FOUND, "callingNumber"));
        }

        Set<Subscription> subscriptions = subscriber.getAllSubscriptions();
        Set<InboxSubscriptionDetailResponse> subscriptionDetails = new HashSet<>();
        SubscriptionPackMessage inboxMessage;
        String weekId;
        String fileName;

        for (Subscription subscription : subscriptions) {

            try {
                inboxMessage = inboxService.getInboxMessage(subscription);
                weekId = (inboxMessage == null) ? null : inboxMessage.getWeekId();
                fileName = (inboxMessage == null) ? null : inboxMessage.getMessageFileName();

                subscriptionDetails.add(new InboxSubscriptionDetailResponse(subscription.getSubscriptionId(),
                        subscription.getSubscriptionPack().getName(),
                        weekId,
                        fileName));

            } catch (NoInboxForSubscriptionException e) {
                // there's no inbox, don't add anything to the list
            }
        }

        return new InboxResponse(subscriptionDetails);
    }

    private void validateSaveInboxCallDetailsContent(StringBuilder failureReasons,
                                                     Set<CallDataRequest> content) {
        if (content == null || content.size() == 0) {
            // Empty content is acceptable (when the IVR vendor plays promotional content)
            return;
        }
        if (content.size() != 2) {
            // Valid content must contain two elements
            failureReasons.append(String.format(INVALID, "content"));
            return;
        }
        int failureReasonsLength = failureReasons.length();
        Set<String> subscriptionPacks = new HashSet<>();
        for (CallDataRequest data : content) {
            validateFieldExactLength(failureReasons, "subscriptionId", data.getSubscriptionId(), SUBSCRIPTION_ID_LENGTH);
            subscriptionPacks.add(data.getSubscriptionPack());
            validateFieldString(failureReasons, "inboxWeekId", data.getInboxWeekId());
            validateFieldString(failureReasons, "contentFileName", data.getContentFileName());
            validateFieldPositiveLong(failureReasons, "startTime", data.getStartTime());
            validateFieldPositiveLong(failureReasons, "endTime", data.getEndTime());
        }

        //check we have all required subscription packs
        if (!SUBSCRIPTION_PACK_SET.equals(subscriptionPacks)) {
            failureReasons.append(String.format(INVALID, "subscriptionPack"));
        }

        //some field error occurred, add an error on the "content" parent field
        if (failureReasonsLength != failureReasons.length()) {
            failureReasons.append(String.format(INVALID, "content"));
        }

    }

    private StringBuilder validateSaveInboxCallDetails(InboxCallDetailsRequest request) {
        StringBuilder failureReasons = validate(request.getCallingNumber(), request.getCallId(),
                request.getOperator(), request.getCircle());

        validateFieldPresent(failureReasons, "callStartTime", request.getCallStartTime());
        validateFieldPresent(failureReasons, "callEndTime", request.getCallEndTime());
        validateFieldPresent(failureReasons, "callDurationInPulses", request.getCallDurationInPulses());
        validateFieldCallStatus(failureReasons, "callStatus", request.getCallStatus());
        validateFieldCallDisconnectReason(failureReasons, "callDisconnectReason", request.getCallDisconnectReason());
        validateSaveInboxCallDetailsContent(failureReasons, request.getContent());

        return failureReasons;
    }


    /**
     * 4.2.5 Save Inbox Call Details
     * IVR shall invoke this API to send the call detail information corresponding to the Inbox access inbound call for
     *    which inbox message(s) is played.
     * /api/kilkari/inboxCallDetails
     *
     */
    @RequestMapping(value = "/inboxCallDetails",
            method = RequestMethod.POST,
            headers = { "Content-type=application/json" })
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public void saveInboxCallDetails(@RequestBody InboxCallDetailsRequest request) {
        StringBuilder failureReasons = validateSaveInboxCallDetails(request);
        if (failureReasons.length() > 0) {
            throw new IllegalArgumentException(failureReasons.toString());
        }

        Set<InboxCallData> content = new HashSet<>();
        if (request.getContent() != null && request.getContent().size() > 0) {
            for (CallDataRequest inboxCallDetailsRequestCallData : request.getContent()) {
                content.add(new InboxCallData(
                        inboxCallDetailsRequestCallData.getSubscriptionId(),
                        inboxCallDetailsRequestCallData.getSubscriptionPack(),
                        inboxCallDetailsRequestCallData.getInboxWeekId(),
                        inboxCallDetailsRequestCallData.getContentFileName(),
                        epochToDateTime(inboxCallDetailsRequestCallData.getStartTime()),
                        epochToDateTime(inboxCallDetailsRequestCallData.getEndTime())
                ));
            }
        }

        InboxCallDetailRecord inboxCallDetailRecord = new InboxCallDetailRecord(
                request.getCallingNumber(),
                request.getOperator(),
                request.getCircle(),
                request.getCallId(),
                epochToDateTime(request.getCallStartTime()),
                epochToDateTime(request.getCallEndTime()),
                request.getCallDurationInPulses(),
                request.getCallStatus(),
                request.getCallDisconnectReason(),
                content);

        inboxService.addInboxCallDetails(inboxCallDetailRecord);
    }

    /**
     * 4.2.3 Create Subscription Request API
     * IVR shall invoke this API to request the creation of the subscription of the beneficiary.
     * /api/kilkari/subscription
     */
    @RequestMapping(value = "/subscription",
            method = RequestMethod.POST,
            headers = { "Content-type=application/json" })
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public void createSubscription(@RequestBody SubscriptionRequest subscriptionRequest) {
        StringBuilder failureReasons = validate(subscriptionRequest.getCallingNumber(),
                                                subscriptionRequest.getCallId(),
                                                subscriptionRequest.getOperator(),
                                                subscriptionRequest.getCircle());
        validateFieldPresent(failureReasons, "subscriptionPack", subscriptionRequest.getSubscriptionPack());
        validateFieldPresent(failureReasons, "languageLocationCode",
                             subscriptionRequest.getLanguageLocationCode());

        if (failureReasons.length() > 0) {
            throw new IllegalArgumentException(failureReasons.toString());
        }

        Language language;
        language = languageService.getForCode(subscriptionRequest.getLanguageLocationCode());
        if (language == null) {
            throw new NotFoundException(String.format(NOT_FOUND, "languageLocationCode"));
        }

        Circle circle = circleService.getByName(subscriptionRequest.getCircle());
        State state = getSingleStateFromCircleAndLanguage(circle, language);
        if (!propertyService.isServiceDeployedInState(Service.KILKARI, state)) {
            throw new NotDeployedException(String.format(NOT_DEPLOYED, Service.KILKARI));
        }

        SubscriptionPack subscriptionPack;
        subscriptionPack = subscriptionService.getSubscriptionPack(subscriptionRequest.getSubscriptionPack());
        if (subscriptionPack == null) {
            throw new NotFoundException(String.format(NOT_FOUND, "subscriptionPack"));
        }

        subscriptionService.createSubscription(subscriptionRequest.getCallingNumber(), language,
                                               subscriptionPack, SubscriptionOrigin.IVR);
    }

    private State getSingleStateFromCircleAndLanguage(Circle circle, Language language) {
        Set<State> stateSet = languageService.getAllStatesForLanguage(language);

        if (stateSet.size() == 1) {
            return stateSet.iterator().next();
        }

        Set<State> stateList = circle.getStates();
        if (stateList.size() == 1) {
            return stateList.iterator().next();
        }

        return null;
    }
    /**
     * 4.2.4
     * Deactivate Subscription
     * <p/>
     * IVR shall invoke this API to deactivate an existing Kilkari subscription
     */
    @RequestMapping(value = "/subscription",
            method = RequestMethod.DELETE,
            headers = { "Content-type=application/json" })
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public void deactivateSubscription(@RequestBody SubscriptionRequest subscriptionRequest) {
        StringBuilder failureReasons = validate(subscriptionRequest.getCallingNumber(),
                subscriptionRequest.getCallId(), subscriptionRequest.getOperator(),
                subscriptionRequest.getCircle());

        validateFieldPresent(failureReasons, "subscriptionId", subscriptionRequest.getSubscriptionId());

        if (failureReasons.length() > 0) {
            throw new IllegalArgumentException(failureReasons.toString());
        }

        Subscription subscription = subscriptionService.getSubscription(subscriptionRequest.getSubscriptionId());

        if (subscription == null) {
            throw new NotFoundException(String.format(NOT_FOUND, "subscriptionId"));
        }

        subscriptionService.deactivateSubscription(subscription, DeactivationReason.DEACTIVATED_BY_USER);
    }
}
