/*
 * Copyright (C) 2019-2020  XC Software (Shenzhen) Ltd.
 *
 */

package hudson.plugins.xcal.payload;

import hudson.plugins.xcal.util.VariableUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IssueDto {
    UUID id;
    String issueKey;
    String seq;
    RuleInformation ruleInformation;
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
    String complexity;
    Double complexityRate;
    String checksum;
    String message;
    String ignored;
    String status;
    String action;
    AssignTo assignTo;
    String createdBy;
    Date createdOn;
    String modifiedBy;
    Date modifiedOn;
    @Builder.Default
    List<IssueTrace> issueTraces = new ArrayList<>();
    @Builder.Default
    List<IssueTraceInfo> issueTraceInfos = new ArrayList<>();
    @Builder.Default
    List<IssueAttribute> issueAttributes = new ArrayList<>();

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class IssueTrace {
        UUID id;
        Integer seq;
        UUID scanFileId;
        String relativePath;
        String scanFilePath;
        @Builder.Default
        Integer lineNo = 0;
        @Builder.Default
        Integer columnNo = 0;
        String functionName;
        String variableName;
        String checksum;
        String message;
        @Builder.Default
        Long scanFileSize = 0L;
        @Builder.Default
        Integer scanFileNoOfLines = 0;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class IssueTraceInfo {
        String id; // checksum
        Integer noOfTrace;
        String message;
        Double complexity;
        Double complexityRate;
        @Builder.Default
        Map<String, String> attributes = new HashMap<>();
        @Builder.Default
        List<IssueTrace> issueTraces = new ArrayList<>();
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AssignTo {
        UUID id;
        String displayName;
        String email;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RuleInformation {
        UUID id;
        String ruleSet;
        String ruleSetDisplayName;
        String ruleSetVersion;
        String scanEngineName;
        String scanEngineVersion;
        String ruleCode;
        String category;
        String vulnerable;
        String name;
        String certainty;
        String priority;
        String severity;
        String likelihood;
        String remediationCost;
        String language;
        String url;
        String detail;
        String description;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class IssueAttribute {
        VariableUtil.IssueAttributeName name;
        String value;
    }

    public Optional<IssueAttribute> getFirstAttribute(VariableUtil.IssueAttributeName attribute) {
        return this.getIssueAttributes().stream().filter(issueAttribute -> attribute == issueAttribute.name).findFirst();
    }
}
