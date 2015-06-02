package org.motechproject.nms.imi.validator;

import org.motechproject.nms.imi.web.contract.FileInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

/**
 * Validation helper class to handle cdr validations
 */
public final class CdrValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(CdrValidator.class);

    public static final String NOT_PRESENT = "<%s: Not Present>";
    public static final String INVALID = "<%s: Invalid>";
    public static final Pattern TARGET_FILENAME_PATTERN = Pattern.compile("OBD_[0-9]{14}\\.csv");

    private CdrValidator() {

    }

    public static boolean validateFieldPresent(StringBuilder errors, String fieldName, Object value) {
        if (value != null) {
            return true;
        }
        errors.append(String.format(NOT_PRESENT, fieldName));
        return false;
    }


    /**
     * Validate that the file name matches expected pattern
     * @param errors error builder to fill
     * @param targetFileName name of the file
     * @return true it the pattern match is successful
     */
    public static boolean validateTargetFileName(StringBuilder errors, String targetFileName) {
        if (validateFieldPresent(errors, "fileName", targetFileName)) {
            if (TARGET_FILENAME_PATTERN.matcher(targetFileName).matches()) {
                return true;
            } else {
                errors.append(String.format(INVALID, "fileName"));
                LOGGER.debug(errors.toString());
                return false;
            }
        }
        return false;
    }


    public static boolean validateCdrFileInfo(StringBuilder errors, FileInfo fileInfo,
                                               String fieldName, String targetFileName) {

        boolean valid = true;

        if (fileInfo == null) {
            errors.append(String.format(NOT_PRESENT, fieldName));
            return false;
        }

        if (validateFieldPresent(errors, "cdrFile", fileInfo.getCdrFile())) {
            if (!fileInfo.getCdrFile().equals(fieldName + "_" + targetFileName)) {
                errors.append(String.format(INVALID, fieldName));
                valid = false;
            }
        }

        if (!validateFieldPresent(errors, "checksum", fileInfo.getChecksum())) {
            valid = false;
        }

        if (fileInfo.getRecordsCount() < 0) {
            errors.append(String.format(INVALID, "recordsCount"));
            valid = false;
        }

        return valid;
    }

}
