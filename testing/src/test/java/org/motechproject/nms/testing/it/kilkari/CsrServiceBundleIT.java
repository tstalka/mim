package org.motechproject.nms.testing.it.kilkari;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.EventRelay;
import org.motechproject.mds.config.SettingsService;
import org.motechproject.nms.kilkari.domain.CallRetry;
import org.motechproject.nms.kilkari.domain.CallStage;
import org.motechproject.nms.kilkari.domain.CallSummaryRecord;
import org.motechproject.nms.kilkari.domain.DeactivationReason;
import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionOrigin;
import org.motechproject.nms.kilkari.domain.SubscriptionPack;
import org.motechproject.nms.kilkari.domain.SubscriptionPackType;
import org.motechproject.nms.kilkari.domain.SubscriptionStatus;
import org.motechproject.nms.kilkari.dto.CallSummaryRecordDto;
import org.motechproject.nms.kilkari.repository.CallRetryDataService;
import org.motechproject.nms.kilkari.repository.CallSummaryRecordDataService;
import org.motechproject.nms.kilkari.repository.SubscriberDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionPackDataService;
import org.motechproject.nms.kilkari.service.CsrService;
import org.motechproject.nms.kilkari.service.SubscriptionService;
import org.motechproject.nms.props.domain.DayOfTheWeek;
import org.motechproject.nms.props.domain.FinalCallStatus;
import org.motechproject.nms.props.domain.RequestId;
import org.motechproject.nms.props.domain.StatusCode;
import org.motechproject.nms.region.domain.Circle;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.Language;
import org.motechproject.nms.region.domain.LanguageLocation;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.repository.CircleDataService;
import org.motechproject.nms.region.repository.DistrictDataService;
import org.motechproject.nms.region.repository.LanguageDataService;
import org.motechproject.nms.region.repository.LanguageLocationDataService;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.nms.testing.it.api.utils.SubscriptionPackBuilder;
import org.motechproject.nms.testing.it.utils.CsrHelper;
import org.motechproject.nms.testing.it.utils.SubscriptionHelper;
import org.motechproject.nms.testing.service.TestingService;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class CsrServiceBundleIT extends BasePaxIT {

    private static final String PROCESS_SUMMARY_RECORD_SUBJECT = "nms.imi.kk.process_summary_record";
    private static final String CSR_PARAM_KEY = "csr";
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormat.forPattern("yyyyMMddHHmmss");

    @Inject
    EventRelay eventRelay;

    @Inject
    CsrService csrService;

    @Inject
    private SettingsService settingsService;

    @Inject
    private SubscriptionService subscriptionService;

    @Inject
    private SubscriptionPackDataService subscriptionPackDataService;

    @Inject
    private SubscriptionDataService subscriptionDataService;

    @Inject
    private SubscriberDataService subscriberDataService;

    @Inject
    private LanguageDataService languageDataService;

    @Inject
    private CallRetryDataService callRetryDataService;

    @Inject
    private CallSummaryRecordDataService csrDataService;

    @Inject
    private LanguageLocationDataService languageLocationDataService;

    @Inject
    private CircleDataService circleDataService;

    @Inject
    private StateDataService stateDataService;

    @Inject
    private DistrictDataService districtDataService;

    @Inject
    private TestingService testingService;


    @Before
    public void cleanupDatabase() {

        testingService.clearDatabase();

        subscriptionPackDataService.create(
                SubscriptionPackBuilder.createSubscriptionPack(
                        "childPack",
                        SubscriptionPackType.CHILD,
                        SubscriptionPackBuilder.CHILD_PACK_WEEKS,
                        1));
        subscriptionPackDataService.create(
                SubscriptionPackBuilder.createSubscriptionPack(
                        "pregnancyPack",
                        SubscriptionPackType.PREGNANCY,
                        SubscriptionPackBuilder.PREGNANCY_PACK_WEEKS,
                        2));

        csrService.buildMessageDurationCache();
    }


    @Test
    public void testServicePresent() {
        assertTrue(csrService != null);
    }


    private Language makeLanguage() {
        Language language = languageDataService.findByName("Hindi");
        if (language != null) {
            return language;
        }
        return languageDataService.create(new Language("Hindi"));
    }

    private LanguageLocation makeLanguageLocation() {
        LanguageLocation languageLocation = languageLocationDataService.findByCode("99");
        if (languageLocation != null) {
            return languageLocation;
        }

        Language language = makeLanguage();
        Circle circle = makeCircle();

        languageLocation = new LanguageLocation("99", circle, language, false);
        languageLocation.getDistrictSet().add(makeDistrict());
        return languageLocationDataService.create(languageLocation);
    }

    private Circle makeCircle() {
        Circle circle = circleDataService.findByName("XX");
        if (circle != null) {
            return circle;
        }

        return circleDataService.create(new Circle("XX"));
    }

    private State makeState() {
        State state = stateDataService.findByCode(1l);
        if (state != null) {
            return state;
        }

        state = new State();
        state.setName("State 1");
        state.setCode(1L);

        return stateDataService.create(state);
    }

    private District makeDistrict() {
        District district = districtDataService.findById(1L);
        if (district != null) {
            return district;
        }

        district = new District();
        district.setName("District 1");
        district.setRegionalName("District 1");
        district.setCode(1L);
        district.setState(makeState());

        return districtDataService.create(district);
    }

    private Long makeNumber() {
        return (long) (Math.random() * 9000000000L) + 1000000000L;
    }


    private Subscription makeSubscription(SubscriptionOrigin origin, DateTime startDate) {
        Subscriber subscriber = subscriberDataService.create(new Subscriber(
                makeNumber(),
                makeLanguageLocation(),
                makeCircle()
        ));
        SubscriptionPack subscriptionPack = subscriptionService.getSubscriptionPack("childPack");
        Subscription subscription = new Subscription(subscriber, subscriptionPack, origin);
        subscription.setStartDate(startDate);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription = subscriptionService.create(subscription);
        getLogger().debug("Created subscription {}", subscription.toString());
        return subscription;
    }


    private Map<Integer, Integer> makeStatsMap(StatusCode statusCode, int count) {
        Map<Integer, Integer> map = new HashMap<>();
        map.put(statusCode.getValue(), count);
        return map;
    }


    @Test
    public void verifyServiceFunctional() {
        Subscription subscription = makeSubscription(SubscriptionOrigin.IVR, DateTime.now().minusDays(14));

        CallSummaryRecordDto csr = new CallSummaryRecordDto(
                new RequestId(subscription.getSubscriptionId(), "11112233445566"),
                subscription.getSubscriber().getCallingNumber(),
                "w1_1.wav",
                "w1_1",
                makeLanguageLocation().getCode(),
                makeCircle().getName(),
                FinalCallStatus.FAILED,
                makeStatsMap(StatusCode.OBD_FAILED_INVALIDNUMBER, 3),
                0,
                3
        );

        Map<String, Object> eventParams = new HashMap<>();
        eventParams.put(CSR_PARAM_KEY, csr);
        MotechEvent motechEvent = new MotechEvent(PROCESS_SUMMARY_RECORD_SUBJECT, eventParams);
        csrService.processCallSummaryRecord(motechEvent);
    }


    // Deactivate if user phone number does not exist
    // https://github.com/motech-implementations/mim/issues/169
    @Test
    public void verifyIssue169() {
        Subscription subscription = makeSubscription(SubscriptionOrigin.IVR, DateTime.now().minusDays(14));
        Subscriber subscriber = subscription.getSubscriber();

        LanguageLocation languageLocation;
        languageLocation = (LanguageLocation) subscriberDataService.getDetachedField(subscriber,
                "languageLocation");

        Circle circle;
        circle = (Circle) subscriberDataService.getDetachedField(subscriber, "circle");


        csrDataService.create(new CallSummaryRecord(
                new RequestId(subscription.getSubscriptionId(), "11112233445566").toString(),
                subscription.getSubscriber().getCallingNumber(),
                "w1_1.wav",
                "w1_1",
                makeLanguageLocation().getCode(),
                makeCircle().getName(),
                FinalCallStatus.FAILED,
                makeStatsMap(StatusCode.OBD_FAILED_INVALIDNUMBER, 10),
                0,
                10,
                3
        ));

        callRetryDataService.create(new CallRetry(
                subscription.getSubscriptionId(),
                subscription.getSubscriber().getCallingNumber(),
                DayOfTheWeek.today(),
                CallStage.RETRY_LAST,
                "w1_1.wav",
                "w1_1",
                makeLanguageLocation().getCode(),
                makeCircle().getName(),
                SubscriptionOrigin.MCTS_IMPORT
        ));

        CallSummaryRecordDto csr = new CallSummaryRecordDto(
                new RequestId(subscription.getSubscriptionId(), "11112233445566"),
                subscription.getSubscriber().getCallingNumber(),
                "w1_1.wav",
                "w1_1",
                makeLanguageLocation().getCode(),
                makeCircle().getName(),
                FinalCallStatus.FAILED,
                makeStatsMap(StatusCode.OBD_FAILED_INVALIDNUMBER, 3),
                0,
                3
        );

        Map<String, Object> eventParams = new HashMap<>();
        eventParams.put(CSR_PARAM_KEY, csr);
        MotechEvent motechEvent = new MotechEvent(PROCESS_SUMMARY_RECORD_SUBJECT, eventParams);
        csrService.processCallSummaryRecord(motechEvent);

        subscription = subscriptionDataService.findBySubscriptionId(subscription.getSubscriptionId());
        assertEquals(SubscriptionStatus.DEACTIVATED, subscription.getStatus());
        assertEquals(DeactivationReason.INVALID_NUMBER, subscription.getDeactivationReason());
    }


    @Test
    public void verifySubscriptionCompletion() {

        String timestamp = DateTime.now().toString(TIME_FORMATTER);

        CsrHelper helper = new CsrHelper(timestamp, subscriptionService, subscriptionPackDataService,
                subscriberDataService, languageDataService, languageLocationDataService, circleDataService,
                stateDataService, districtDataService);

        helper.makeRecords(1,3,0,0);

        for (CallSummaryRecordDto record : helper.getRecords()) {
            Map<String, Object> eventParams = new HashMap<>();
            eventParams.put(CSR_PARAM_KEY, record);
            MotechEvent motechEvent = new MotechEvent(PROCESS_SUMMARY_RECORD_SUBJECT, eventParams);
            csrService.processCallSummaryRecord(motechEvent);
        }

        List<Subscription> subscriptions = subscriptionDataService.findByStatus(SubscriptionStatus.COMPLETED);
        assertEquals(3, subscriptions.size());
    }


    @Test
    public void verifyFT140() {
        /**
         * To check that NMS shall not retry OBD message for which all OBD attempts(1 actual+3 retry) fails with
         * single message per week configuration.
         */

            String timestamp = DateTime.now().toString(TIME_FORMATTER);

        // Create a record in the CallRetry table marked as "last try" and verify it is erased from the
        // CallRetry table

        SubscriptionHelper sh = new SubscriptionHelper(subscriptionService, subscriberDataService,
                subscriptionPackDataService, languageDataService, languageLocationDataService, circleDataService,
                stateDataService, districtDataService);

        Subscription subscription = sh.mksub(SubscriptionOrigin.MCTS_IMPORT, DateTime.now().minusDays(3));
        String contentFileName = sh.getContentMessageFile(subscription, 0);
        CallRetry retry = callRetryDataService.create(new CallRetry(
                subscription.getSubscriptionId(),
                subscription.getSubscriber().getCallingNumber(),
                DayOfTheWeek.today(),
                CallStage.RETRY_LAST,
                contentFileName,
                "XXX",
                "XXX",
                "XX",
                SubscriptionOrigin.MCTS_IMPORT
        ));


        Map<Integer, Integer> callStats = new HashMap<>();
        CallSummaryRecordDto record = new CallSummaryRecordDto(
                new RequestId(subscription.getSubscriptionId(), timestamp),
                subscription.getSubscriber().getCallingNumber(),
                contentFileName,
                "XXX",
                "XXX",
                "XX",
                FinalCallStatus.FAILED,
                callStats,
                0,
                5
        );


        Map<String, Object> eventParams = new HashMap<>();
        eventParams.put(CSR_PARAM_KEY, record);
        MotechEvent motechEvent = new MotechEvent(PROCESS_SUMMARY_RECORD_SUBJECT, eventParams);
        csrService.processCallSummaryRecord(motechEvent);

        // There should be no calls to retry since the one above was the last try
        assertEquals(0, callRetryDataService.count());
    }


    @Test
    public void verifyFT144() {

        String timestamp = DateTime.now().toString(TIME_FORMATTER);

        // To check that NMS shall retry the OBD messages which failed as IVR did not attempt OBD for those messages.

        SubscriptionHelper sh = new SubscriptionHelper(subscriptionService, subscriberDataService,
                subscriptionPackDataService, languageDataService, languageLocationDataService, circleDataService,
                stateDataService, districtDataService);

        Subscription subscription = sh.mksub(SubscriptionOrigin.MCTS_IMPORT, DateTime.now());
        String contentFileName = sh.getContentMessageFile(subscription, 0);
        CallRetry retry = callRetryDataService.create(new CallRetry(
                subscription.getSubscriptionId(),
                subscription.getSubscriber().getCallingNumber(),
                DayOfTheWeek.today(),
                CallStage.RETRY_1,
                contentFileName,
                "XXX",
                "XXX",
                "XX",
                SubscriptionOrigin.MCTS_IMPORT
        ));


        Map<Integer, Integer> callStats = new HashMap<>();
        callStats.put(StatusCode.OBD_FAILED_NOATTEMPT.getValue(),1);
        CallSummaryRecordDto record = new CallSummaryRecordDto(
                new RequestId(subscription.getSubscriptionId(), timestamp),
                subscription.getSubscriber().getCallingNumber(),
                contentFileName,
                "XXX",
                "XXX",
                "XX",
                FinalCallStatus.FAILED,
                callStats,
                0,
                1
        );


        Map<String, Object> eventParams = new HashMap<>();
        eventParams.put(CSR_PARAM_KEY, record);
        MotechEvent motechEvent = new MotechEvent(PROCESS_SUMMARY_RECORD_SUBJECT, eventParams);
        csrService.processCallSummaryRecord(motechEvent);

        // There should be one calls to retry since the one above was the last failed with No attempt
        assertEquals(1, callRetryDataService.count());

        List<CallRetry> retries = callRetryDataService.retrieveAll();

        assertEquals(subscription.getSubscriptionId(), retries.get(0).getSubscriptionId());
        assertEquals(CallStage.RETRY_2, retries.get(0).getCallStage());
        assertEquals(DayOfTheWeek.today().nextDay(),retries.get(0).getDayOfTheWeek());

    }



    @Test
    public void verifyFT149() {

        String timestamp = DateTime.now().toString(TIME_FORMATTER);

        // To check that NMS shall not retry the OBD messages which failed due to user number in dnd.

        SubscriptionHelper sh = new SubscriptionHelper(subscriptionService, subscriberDataService,
                subscriptionPackDataService, languageDataService, languageLocationDataService, circleDataService,
                stateDataService, districtDataService);

        Subscription subscription = sh.mksub(SubscriptionOrigin.MCTS_IMPORT, DateTime.now());
        String contentFileName = sh.getContentMessageFile(subscription, 0);

        Map<Integer, Integer> callStats = new HashMap<>();
        callStats.put(StatusCode.OBD_DNIS_IN_DND.getValue(),1);
        CallSummaryRecordDto record = new CallSummaryRecordDto(
                new RequestId(subscription.getSubscriptionId(), timestamp),
                subscription.getSubscriber().getCallingNumber(),
                contentFileName,
                "XXX",
                "XXX",
                "XX",
                FinalCallStatus.REJECTED,
                callStats,
                0,
                1
        );


        Map<String, Object> eventParams = new HashMap<>();
        eventParams.put(CSR_PARAM_KEY, record);
        MotechEvent motechEvent = new MotechEvent(PROCESS_SUMMARY_RECORD_SUBJECT, eventParams);
        csrService.processCallSummaryRecord(motechEvent);

        // There should be no calls to retry since the one above was the last rejected with DND reason
        assertEquals(0, callRetryDataService.count());
    }


    //todo: verify multiple days' worth of summary record aggregation
    //todo: verify more stuff I can't think of now
}
