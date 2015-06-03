package org.motechproject.nms.imi.service;

import org.motechproject.event.MotechEvent;
import org.motechproject.nms.imi.web.contract.FileInfo;

import java.io.File;
import java.util.List;

/**
 *  Process and the cdr detail and summary files from IMI
 */
public interface CdrFileService {

    enum Action {
        PASS1, // record count, valid csv, checksum
        PASS2, // record count, valid csv + sort order, entities (subscription, circle, etc...) exist
        PASS3  // record count, valid csv + aggregate CDRS into CSR and send for distributed processing
    }


    /**
     * Copies the given file from remote share to localhost
     * @param fileName name of the file to be copied
     */
    void scpFileToLocal(String fileName);

    /**
     * Verifies the checksum & record count provided in fileInfo match the checksum & record count of file
     * also verifies all csv rows are valid.
     *
     * @param file          file to process
     * @param fileInfo      file information provided about the file (ie: expected checksum & recordCount)
     *
     */
    List<String> verifyChecksumAndCountAndCsv(File file, FileInfo fileInfo);


    /**
     * Verifies all entities referenced in the detail exist in the database and verify the file is sorted
     *
     * @param file      file to process

     * @return          a list of errors (failure) or an empty list (success)
     */
    List<String> verifyDetailFileEntitiesAndSortOrder(File file);


    /**
     * Send aggregated detail records for processing as CallSummaryRecordDto in MOTECH events
     *
     * NOTE: only exposed here for ITs
     *
     * @param file          file to process
     * @return          a list of errors (failure) or an empty list (success)
     */
    List<String> sendAggregatedRecords(File file);


    /**
     * Verify file exists, verify checksum & record count match. Then sends event to proceed to CDR processing
     * phase 2
     */
    void verifyDetailFileChecksumAndCount(FileInfo fileInfo);


    /**
     * Aggregates multiple detail records provided my IMI into one summary record for each call in a given day.
     * Then sends a MOTECH PROCESS_SUMMARY_RECORD event for each summary record such that the summary record
     * process is distributed among all MOTECH nodes.
     *
     * NOTE: only exposed here for ITs. Normally called by the MOTECH event system (it's a @MotechListener)
     */
    List<String> processDetailFile(MotechEvent event);
}
