package org.motechproject.nms.imi.web;

import org.motechproject.nms.imi.domain.FileAuditRecord;
import org.motechproject.nms.imi.domain.FileType;
import org.motechproject.nms.imi.exception.ExecException;
import org.motechproject.nms.imi.exception.InvalidCdrFileException;
import org.motechproject.nms.imi.exception.NotFoundException;
import org.motechproject.nms.imi.repository.FileAuditRecordDataService;
import org.motechproject.nms.imi.service.CdrFileService;
import org.motechproject.nms.imi.service.TargetFileService;
import org.motechproject.nms.imi.service.impl.ScpHelper;
import org.motechproject.nms.imi.validator.CdrValidator;
import org.motechproject.nms.imi.web.contract.AggregateBadRequest;
import org.motechproject.nms.imi.web.contract.BadRequest;
import org.motechproject.nms.imi.web.contract.CdrFileNotificationRequest;
import org.motechproject.nms.imi.web.contract.FileProcessedStatusRequest;
import org.motechproject.server.config.SettingsFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * IMI Controller - handles all API interaction with the IMI IVR vendor
 */
@Controller
public class ImiController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImiController.class);

    private SettingsFacade settingsFacade;
    private CdrFileService cdrFileService;
    private TargetFileService targetFileService;
    private FileAuditRecordDataService fileAuditRecordDataService;

    @Autowired
    public ImiController(SettingsFacade settingsFacade, CdrFileService cdrFileService,
                         TargetFileService targetFileService, FileAuditRecordDataService fileAuditRecordDataService) {
        this.settingsFacade = settingsFacade;
        this.cdrFileService = cdrFileService;
        this.targetFileService = targetFileService;
        this.fileAuditRecordDataService = fileAuditRecordDataService;
    }

    private void verifyFileExistsInAuditRecord(String fileName) {
        if (fileAuditRecordDataService.countFindByFileName(fileName) < 1) {
            throw new NotFoundException(String.format("<%s: Not Found>", fileName));
        }
    }


    /**
     * 4.2.6
     * CDR File Notification
     *
     * IVR shall invoke this API to notify when a CDR file is ready.
     */
    @RequestMapping(value = "/cdrFileNotification",
            method = RequestMethod.POST,
            headers = { "Content-type=application/json" })
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void notifyNewCdrFile(@RequestBody CdrFileNotificationRequest request) {
        StringBuilder failureReasons = new StringBuilder();
        CdrValidator.validateTargetFileName(failureReasons, request.getFileName());
        CdrValidator.validateCdrFileInfo(failureReasons, request.getCdrSummary(), "cdrSummary",
                request.getFileName());
        CdrValidator.validateCdrFileInfo(failureReasons, request.getCdrDetail(), "cdrDetail",
                request.getFileName());

        if (failureReasons.length() > 0) {
            throw new IllegalArgumentException(failureReasons.toString());
        }


        // Check the provided OBD file (aka: targetFile) exists in the FileAuditRecord table
        verifyFileExistsInAuditRecord(request.getFileName());


        // Copy the file from the IMI network share (imi.remote_cdr_dir) into local cdr dir (imi.local_cdr_dir)
        ScpHelper scpHelper = new ScpHelper(settingsFacade);
        String fileName = request.getCdrDetail().getCdrFile();
        try {
            scpHelper.scpCdrFromRemote(fileName);
        } catch (ExecException e) {
            String error = String.format("Error copying CDR file %s: %s", fileName, e.getMessage());
            LOGGER.error(error);
            fileAuditRecordDataService.create(new FileAuditRecord(
                    FileType.CDR_DETAIL_FILE,
                    fileName,
                    false,
                    error,
                    null,
                    null
            ));
            //todo: send alert
            throw new IllegalArgumentException(e.getMessage(), e);
        }

        // This checks the file, checksum, record count & csv lines, then sends an event to proceed to phase 2 of the
        // CDR processing task also handled by the IMI module: processDetailFile
        cdrFileService.verifyDetailFileChecksumAndCount(request.getCdrDetail());
    }


    /**
     * 4.2.7
     * Notify File Processed Status
     *
     * IVR shall invoke this API to update about the status of file copy after initial checks on the file
     * are completed.
     */
    @RequestMapping(value = "obdFileProcessedStatusNotification",
            method = RequestMethod.POST,
            headers = { "Content-type=application/json" })
    @ResponseStatus(HttpStatus.OK)
    public void notifyFileProcessedStatus(@RequestBody FileProcessedStatusRequest request) {
        StringBuilder failureReasons = new StringBuilder();

        CdrValidator.validateFieldPresent(failureReasons, "fileProcessedStatus",
                request.getFileProcessedStatus());
        CdrValidator.validateFieldPresent(failureReasons, "fileName", request.getFileName());

        if (failureReasons.length() > 0) {
            throw new IllegalArgumentException(failureReasons.toString());
        }

        // Check the provided OBD file (aka: targetFile) exists in the FileAuditRecord table
        verifyFileExistsInAuditRecord(request.getFileName());

        //
        targetFileService.handleFileProcessedStatusNotification(request);
    }


    @ExceptionHandler({ NotFoundException.class })
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public BadRequest handleException(NotFoundException e) {
        return new BadRequest(e.getMessage());
    }


    @ExceptionHandler({ RuntimeException.class })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public BadRequest handleException(RuntimeException e) {
        return new BadRequest(e.getMessage());
    }


    /**
     * Handles malformed JSON, returns a slightly more informative message than a generic HTTP-400 Bad Request
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BadRequest handleException(HttpMessageNotReadableException e) {
        return new BadRequest(e.getMessage());
    }


    /**
     * Handles InvalidCdrFileException - potentially a large amount of errors all in one list of string
     */
    //todo: IT or UT
    @ExceptionHandler(InvalidCdrFileException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public AggregateBadRequest handleException(InvalidCdrFileException e) {
        return new AggregateBadRequest(e.getMessages());
    }


    /**
     * Handles any other exception
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public BadRequest handleException(Exception e) {
        return new BadRequest(e.getMessage());
    }
}
