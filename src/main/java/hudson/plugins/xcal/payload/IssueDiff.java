/*
 * Copyright (C) 2019-2020  XC Software (Shenzhen) Ltd.
 *
 */

package hudson.plugins.xcal.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IssueDiff {
    UUID id;
    UUID scanTaskId;
    UUID baselineScanTaskId;
    UUID issueId;
    String issueKey;
    String checksum;
    String type;
    String issueCategory;
    String ruleSet;
    String vulnerable;
    String certainty;
    String issueCode;
    String issueName;
    String critical;
    String severity;
    String likelihood;
    String remediationCost;
    UUID scanFileId;
    String relativePath;
    String scanFilePath;
    @Builder.Default
    Integer lineNo = 0;
    @Builder.Default
    Integer columnNo = 0;
    String functionName;
    String variableName;
    String createdBy;
    Date createdOn;
    String modifiedBy;
    Date modifiedOn;

    String path;

    IssueDto issue;
}
