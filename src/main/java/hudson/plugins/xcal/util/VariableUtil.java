/*
 * Copyright (C) 2019-2020  XC Software (Shenzhen) Ltd.
 *
 */

package hudson.plugins.xcal.util;

public final class VariableUtil {

    private VariableUtil() {
    }

    public enum IssueAttributeName {
        RULE_CODE,
        SEVERITY,
        VULNERABLE,
        LIKELIHOOD,
        REMEDIATION_COST,
        CERTAINTY,
        PRIORITY,
        CATEGORY,
        ERROR_CODE,
        NO_OF_TRACE_SET,
        COMPLEXITY,
        COMPLEXITY_MAX,
        COMPLEXITY_MIN,
        COMPLEXITY_RATE
    }

    public enum GerritParameters {
        GERRIT_CHANGE_SUBJECT,
        GERRIT_CHANGE_COMMIT_MESSAGE,
        GERRIT_BRANCH,
        GERRIT_TOPIC,
        GERRIT_CHANGE_ID,
        GERRIT_CHANGE_NUMBER,
        GERRIT_CHANGE_URL,
        GERRIT_PATCHSET_NUMBER,
        GERRIT_PATCHSET_REVISION,
        GERRIT_PROJECT,
        GERRIT_REFSPEC,
        GERRIT_CHANGE_ABANDONER,
        GERRIT_CHANGE_ABANDONER_NAME,
        GERRIT_CHANGE_ABANDONER_EMAIL,
        GERRIT_CHANGE_OWNER,
        GERRIT_CHANGE_OWNER_NAME,
        GERRIT_CHANGE_OWNER_EMAIL,
        GERRIT_CHANGE_RESTORER,
        GERRIT_CHANGE_RESTORER_NAME,
        GERRIT_CHANGE_RESTORER_EMAIL,
        GERRIT_PATCHSET_UPLOADER,
        GERRIT_PATCHSET_UPLOADER_NAME,
        GERRIT_PATCHSET_UPLOADER_EMAIL,
        GERRIT_EVENT_ACCOUNT,
        GERRIT_EVENT_ACCOUNT_NAME,
        GERRIT_EVENT_ACCOUNT_EMAIL,
        GERRIT_REFNAME,
        GERRIT_OLDREV,
        GERRIT_NEWREV,
        GERRIT_SUBMITTER,
        GERRIT_SUBMITTER_NAME,
        GERRIT_SUBMITTER_EMAIL,
        GERRIT_NAME,
        GERRIT_HOST,
        GERRIT_PORT,
        GERRIT_SCHEME,
        GERRIT_VERSION,
        GERRIT_EVENT_HASH,
        GERRIT_EVENT_TYPE
    }

    public enum GitlabParameters {
        gitlabBranch,
        gitlabSourceBranch,
        gitlabActionType,
        gitlabUserName,
        gitlabUserUsername,
        gitlabUserEmail,
        gitlabSourceRepoHomepage,
        gitlabSourceRepoName,
        gitlabSourceNamespace,
        gitlabSourceRepoURL,
        gitlabSourceRepoSshUrl,
        gitlabSourceRepoHttpUrl,
        gitlabMergeRequestTitle,
        gitlabMergeRequestDescription,
        gitlabMergeRequestId,
        gitlabMergeRequestIid,
        gitlabMergeRequestState,
        gitlabMergedByUser,
        gitlabMergeRequestAssignee,
        gitlabMergeRequestLastCommit,
        gitlabMergeRequestTargetProjectId,
        gitlabTargetBranch,
        gitlabTargetRepoName,
        gitlabTargetNamespace,
        gitlabTargetRepoSshUrl,
        gitlabTargetRepoHttpUrl,
        gitlabBefore,
        gitlabAfter,
        gitlabTriggerPhrase
    }
}
