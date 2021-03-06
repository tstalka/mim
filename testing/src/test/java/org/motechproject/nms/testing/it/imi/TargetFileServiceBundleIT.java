package org.motechproject.nms.testing.it.imi;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.nms.imi.service.SettingsService;
import org.motechproject.nms.imi.service.TargetFileService;
import org.motechproject.nms.imi.service.contract.TargetFileNotification;
import org.motechproject.nms.kilkari.domain.CallRetry;
import org.motechproject.nms.kilkari.domain.CallStage;
import org.motechproject.nms.kilkari.domain.DeactivationReason;
import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionOrigin;
import org.motechproject.nms.kilkari.repository.CallRetryDataService;
import org.motechproject.nms.kilkari.repository.SubscriberDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionPackDataService;
import org.motechproject.nms.kilkari.service.SubscriberService;
import org.motechproject.nms.kilkari.service.SubscriptionService;
import org.motechproject.nms.props.domain.DayOfTheWeek;
import org.motechproject.nms.region.repository.CircleDataService;
import org.motechproject.nms.region.repository.DistrictDataService;
import org.motechproject.nms.region.repository.LanguageDataService;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.nms.region.service.DistrictService;
import org.motechproject.nms.testing.it.utils.ChecksumHelper;
import org.motechproject.nms.testing.it.utils.RegionHelper;
import org.motechproject.nms.testing.it.utils.SubscriptionHelper;
import org.motechproject.nms.testing.service.TestingService;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class TargetFileServiceBundleIT extends BasePaxIT {

    private static final String LOCAL_OBD_DIR = "imi.local_obd_dir";
    private static final String REMOTE_OBD_DIR = "imi.remote_obd_dir";


    private String localObdDirBackup;
    private String remoteObdDirBackup;

    @Inject
    TargetFileService targetFileService;
    @Inject
    SubscriptionService subscriptionService;
    @Inject
    SubscriptionDataService subscriptionDataService;
    @Inject
    SubscriberDataService subscriberDataService;
    @Inject
    SubscriptionPackDataService subscriptionPackDataService;
    @Inject
    CallRetryDataService callRetryDataService;
    @Inject
    LanguageDataService languageDataService;
    @Inject
    CircleDataService circleDataService;
    @Inject
    StateDataService stateDataService;
    @Inject
    DistrictDataService districtDataService;
    @Inject
    DistrictService districtService;
    @Inject
    SubscriberService subscriberService;
    @Inject
    SettingsService settingsService;
    @Inject
    TestingService testingService;


    RegionHelper rh;
    SubscriptionHelper sh;


    @Before
    public void before() {
        testingService.clearDatabase();

        localObdDirBackup = ImiTestHelper.setupTestDir(settingsService, LOCAL_OBD_DIR, "obd-local-dir-it");
        remoteObdDirBackup = ImiTestHelper.setupTestDir(settingsService, REMOTE_OBD_DIR, "obd-remote-dir-it");

        rh = new RegionHelper(languageDataService, circleDataService, stateDataService, districtDataService,
                districtService);

        sh = new SubscriptionHelper(subscriptionService, subscriberDataService, subscriptionPackDataService,
                languageDataService, circleDataService, stateDataService, districtDataService, districtService);
    }


    @After
    public void restoreSettings() {
        settingsService.getSettingsFacade().setProperty(REMOTE_OBD_DIR, remoteObdDirBackup);
        settingsService.getSettingsFacade().setProperty(LOCAL_OBD_DIR, localObdDirBackup);
    }


    @Test
    public void testTargetFileGeneration() throws NoSuchAlgorithmException, IOException {

        Subscriber subscriber1 = new Subscriber(1111111111L, rh.hindiLanguage(), rh.delhiCircle());
        subscriber1.setLastMenstrualPeriod(DateTime.now().minusDays(90)); // startDate will be today
        subscriberDataService.create(subscriber1);
        subscriptionService.createSubscription(1111111111L, rh.hindiLanguage(), sh.pregnancyPack(),
                SubscriptionOrigin.MCTS_IMPORT);


        // Should not be picked up because it's been deactivated
        Subscriber subscriber2 = new Subscriber(2222222222L, rh.kannadaLanguage(), rh.karnatakaCircle());
        subscriber2.setLastMenstrualPeriod(DateTime.now().minusDays(90)); // startDate will be today
        subscriberDataService.create(subscriber2);
        Subscription subscription2 = subscriptionService.createSubscription(2222222222L, rh.kannadaLanguage(),
                sh.pregnancyPack(), SubscriptionOrigin.MCTS_IMPORT);
        subscriptionService.deactivateSubscription(subscription2, DeactivationReason.CHILD_DEATH);

        //Should not be picked up because it's not for today
        Subscriber subscriber3 = new Subscriber(6666666666L, rh.kannadaLanguage(), rh.karnatakaCircle());
        subscriber3.setDateOfBirth(DateTime.now().plusDays(1)); // startDate is DOB + 1 for child packs,
                                                    // so setting the DOB tomorrow this should be picked up
                                                    // the day after tomorrow
        subscriberDataService.create(subscriber3);
        subscriptionService.createSubscription(6666666666L, rh.kannadaLanguage(), sh.childPack(),
                SubscriptionOrigin.IVR);


        // Should not be picked up because it's not for today
        callRetryDataService.create(new CallRetry("11111111-1111-1111-1111-111111111111", 3333333333L,
                DayOfTheWeek.today().nextDay(), CallStage.RETRY_1, "w1_m1.wav", "w1_1",
                rh.hindiLanguage().getCode(), rh.delhiCircle().getName(), SubscriptionOrigin.IVR));


        TargetFileNotification tfn = targetFileService.generateTargetFile();
        assertNotNull(tfn);

        // Should not pickup subscription2 because its status is not ACTIVE
        // Should not pickup subscription3 because it's for tomorrow
        // Should not pickup call retry record because it's for tomorrow also
        assertEquals(1, (int) tfn.getRecordsCount());

        //read the file to get record count
        File targetDir = new File(settingsService.getSettingsFacade().getProperty("imi.local_obd_dir"));
        File targetFile = new File(targetDir, tfn.getFileName());
        int recordCount = 0;
        boolean header = true;
        try (InputStream is = Files.newInputStream(targetFile.toPath());
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            while ((reader.readLine()) != null) {
                if (header) {
                    header = false;
                    continue;
                }
                recordCount++;
            }
        }

        String checksum = ChecksumHelper.checksum(targetFile);

        assertEquals((int)tfn.getRecordsCount(), recordCount);

        assertEquals(tfn.getChecksum(), checksum);
    }


    @Test
    public void testServicePresent() {
        assertTrue(targetFileService != null);
    }


    // un-ignore to create a large sample OBD file
    @Ignore
    public void createLargeFile() {
        SubscriptionHelper sh = new SubscriptionHelper(subscriptionService, subscriberDataService,
                subscriptionPackDataService, languageDataService, circleDataService, stateDataService,
                districtDataService, districtService);

        for (int i=0 ; i<1000 ; i++) {
            sh.mksub(SubscriptionOrigin.MCTS_IMPORT, DateTime.now());
        }

        for (int i=0 ; i<1000 ; i++) {

            int randomWeek = (int) (Math.random() * sh.childPack().getWeeks());
            Subscription sub = sh.mksub(
                    SubscriptionOrigin.MCTS_IMPORT,
                    DateTime.now().minusDays(7 * randomWeek - 1)
            );
            callRetryDataService.create(new CallRetry(
                    sub.getSubscriptionId(),
                    sub.getSubscriber().getCallingNumber(),
                    DayOfTheWeek.today(),
                    CallStage.RETRY_1,
                    sh.getContentMessageFile(sub, randomWeek),
                    sh.getWeekId(sub, randomWeek),
                    sh.getLanguageCode(sub),
                    sh.getCircle(sub),
                    SubscriptionOrigin.MCTS_IMPORT
            ));
        }

        TargetFileNotification tfn = targetFileService.generateTargetFile();
        assertNotNull(tfn);
        getLogger().debug("Generated {}", tfn.getFileName());
    }

    // To check that target file should contain correct weekID according to LMP of the subscriber.
    @Test
    public void verifyFT151() throws NoSuchAlgorithmException, IOException {

        Subscriber subscriber1 = new Subscriber(1111111111L, rh.hindiLanguage(), rh.delhiCircle());
        subscriber1.setLastMenstrualPeriod(DateTime.now().minusDays(125)); // weekId will be W6_1
        subscriberDataService.create(subscriber1);
        Subscription subscription = subscriptionService.createSubscription(1111111111L, rh.hindiLanguage(),
                sh.pregnancyPack(), SubscriptionOrigin.MCTS_IMPORT);
        subscription.setNeedsWelcomeMessage(false);
        subscriptionDataService.update(subscription);

        List<String> contents = new ArrayList<>();
        String line;

        TargetFileNotification tfn = targetFileService.generateTargetFile();

        File targetDir = new File(settingsService.getSettingsFacade().getProperty("imi.local_obd_dir"));
        File targetFile = new File(targetDir, tfn.getFileName());
        int recordCount = 0;
        boolean header = true;
        try (InputStream is = Files.newInputStream(targetFile.toPath());
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            while ((line = reader.readLine()) != null) {
                if (header) {
                    header = false;
                    continue;
                }
                recordCount++;
                contents.add(line.split(",")[7]); //column 8 is for weekId
            }
        }

        String checksum = ChecksumHelper.checksum(targetFile);

        assertEquals((int)tfn.getRecordsCount(), recordCount);
        assertEquals(tfn.getChecksum(), checksum);
        assertTrue("w6_1".equals(contents.get(0)));
        assertEquals(1, recordCount);
    }

    // To check that target file should contain correct weekID according to DOB of the subscriber.
    @Test
    public void verifyFT152() throws NoSuchAlgorithmException, IOException {


        Subscriber subscriber1 = new Subscriber(1111111111L, rh.hindiLanguage(), rh.delhiCircle());
        subscriber1.setDateOfBirth(DateTime.now().minusDays(28)); // weekId will be W5_1
        subscriberDataService.create(subscriber1);
        Subscription subscription = subscriptionService.createSubscription(1111111111L, rh.hindiLanguage(),
                sh.childPack(), SubscriptionOrigin.MCTS_IMPORT);
        subscription.setNeedsWelcomeMessage(false);
        subscriptionDataService.update(subscription);

        List<String> contents = new ArrayList<>();
        String line;

        TargetFileNotification tfn = targetFileService.generateTargetFile();

        File targetDir = new File(settingsService.getSettingsFacade().getProperty("imi.local_obd_dir"));
        File targetFile = new File(targetDir, tfn.getFileName());
        int recordCount = 0;
        boolean header = true;
        try (InputStream is = Files.newInputStream(targetFile.toPath());
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            while ((line = reader.readLine()) != null) {
                if (header) {
                    header = false;
                    continue;
                }
                recordCount++;
                contents.add(line.split(",")[7]); //column 8 is for weekId
            }
        }

        String checksum = ChecksumHelper.checksum(targetFile);
        assertEquals((int)tfn.getRecordsCount(), recordCount);
        assertEquals(tfn.getChecksum(), checksum);
        assertTrue("w5_1".equals(contents.get(0)));

        //update the date of birth of the subscriber
        Subscriber subscriber2 = subscriberDataService.findByCallingNumber(1111111111L);
        subscriber2.setDateOfBirth(DateTime.now().minusDays(21)); // weekId will be W4_1
        subscriberService.update(subscriber2);

        // again generate the target file to check correct weekId is picked after DOB is changed.
        tfn = targetFileService.generateTargetFile();
        contents.clear();
        targetFile = new File(targetDir, tfn.getFileName());
        recordCount = 0;
        header = true;
        try (InputStream is = Files.newInputStream(targetFile.toPath());
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            while ((line = reader.readLine()) != null) {
                if (header) {
                    header = false;
                    continue;
                }
                recordCount++;
                contents.add(line.split(",")[7]); //column 8 is for weekId
            }
        }
        checksum = ChecksumHelper.checksum(targetFile);
        assertEquals((int)tfn.getRecordsCount(), recordCount);
        assertEquals(tfn.getChecksum(), checksum);
        assertTrue("w4_1".equals(contents.get(0)));
        assertEquals(1, recordCount);
    }

    /*
    *To verify welcome message is played along with next week’s content as per the LMP.
    */
    @Test
    public void verifyFT190() throws NoSuchAlgorithmException, IOException {
        Subscriber subscriber1 = new Subscriber(1111111111L, rh.hindiLanguage(), rh.delhiCircle());
        subscriber1.setLastMenstrualPeriod(DateTime.now().minusDays(90)); // weekId will be W1_1
        subscriberDataService.create(subscriber1);
        subscriptionService.createSubscription(1111111111L, rh.hindiLanguage(), sh.pregnancyPack(),
                SubscriptionOrigin.MCTS_IMPORT);

        List<String> contents = new ArrayList<>();
        String line;

        TargetFileNotification tfn = targetFileService.generateTargetFile();

        File targetDir = new File(settingsService.getSettingsFacade().getProperty("imi.local_obd_dir"));
        File targetFile = new File(targetDir, tfn.getFileName());
        int recordCount = 0;
        boolean header = true;
        try (InputStream is = Files.newInputStream(targetFile.toPath());
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            while ((line = reader.readLine()) != null) {
                if (header) {
                    header = false;
                    continue;
                }
                recordCount++;
                contents.add(line.split(",")[6]); //column 7 is for content filename
            }
        }

        String checksum = ChecksumHelper.checksum(targetFile);
        assertEquals((int)tfn.getRecordsCount(), recordCount);
        assertEquals(tfn.getChecksum(), checksum);
        assertTrue("welcome.wav".equals(contents.get(0)));
    }

    /*
    *To verify welcome message is played along with next week’s content, as per the DOB.
    */
    @Test
    public void verifyFT191() throws NoSuchAlgorithmException, IOException {
        Subscriber subscriber1 = new Subscriber(1111111111L, rh.hindiLanguage(), rh.delhiCircle());
        subscriber1.setDateOfBirth(DateTime.now()); // weekId will be W1_1
        subscriberDataService.create(subscriber1);
        subscriptionService.createSubscription(1111111111L, rh.hindiLanguage(), sh.childPack(),
                SubscriptionOrigin.MCTS_IMPORT);

        List<String> contents = new ArrayList<>();
        String line;

        TargetFileNotification tfn = targetFileService.generateTargetFile();

        File targetDir = new File(settingsService.getSettingsFacade().getProperty("imi.local_obd_dir"));
        File targetFile = new File(targetDir, tfn.getFileName());
        int recordCount = 0;
        boolean header = true;
        try (InputStream is = Files.newInputStream(targetFile.toPath());
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            while ((line = reader.readLine()) != null) {
                if (header) {
                    header = false;
                    continue;
                }
                recordCount++;
                contents.add(line.split(",")[6]); //column 7 is for content filename
            }
        }

        String checksum = ChecksumHelper.checksum(targetFile);
        assertEquals((int)tfn.getRecordsCount(), recordCount);
        assertEquals(tfn.getChecksum(), checksum);
        assertTrue("welcome.wav".equals(contents.get(0)));

    }
    //todo: test success notification is sent to the IVR system




    @Test
    public void testChecksumsVaryWithFileContent() throws NoSuchAlgorithmException, IOException,
            InterruptedException {

        Subscriber subscriber1 = new Subscriber(1111111111L, rh.hindiLanguage(), rh.delhiCircle());
        subscriber1.setLastMenstrualPeriod(DateTime.now().minusDays(90)); // startDate will be today
        subscriberDataService.create(subscriber1);
        subscriptionService.createSubscription(subscriber1.getCallingNumber(), rh.hindiLanguage(),
                sh.pregnancyPack(), SubscriptionOrigin.MCTS_IMPORT);

        TargetFileNotification tfn1 = targetFileService.generateTargetFile();
        assertNotNull(tfn1);
        getLogger().debug(tfn1.toString());

        // Sleep two seconds so the file names are different
        Thread.sleep(2000L);

        testingService.clearDatabase();

        Subscriber subscriber2 = new Subscriber(2222222222L, rh.kannadaLanguage(), rh.karnatakaCircle());
        subscriber2.setLastMenstrualPeriod(DateTime.now().minusDays(90)); // startDate will be today
        subscriberDataService.create(subscriber2);
        subscriptionService.createSubscription(subscriber2.getCallingNumber(), rh.kannadaLanguage(),
                sh.pregnancyPack(), SubscriptionOrigin.MCTS_IMPORT);

        TargetFileNotification tfn2 = targetFileService.generateTargetFile();
        assertNotNull(tfn2);
        getLogger().debug(tfn2.toString());

        assertFalse(tfn1.getChecksum().equals(tfn2.getChecksum()));
    }

}
