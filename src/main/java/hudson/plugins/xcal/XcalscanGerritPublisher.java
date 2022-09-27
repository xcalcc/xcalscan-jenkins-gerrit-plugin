package hudson.plugins.xcal;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.google.common.base.MoreObjects;
import com.google.gerrit.extensions.api.changes.ReviewInput;
import com.google.gerrit.extensions.api.changes.ReviewResult;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.sonyericsson.hudson.plugins.gerrit.trigger.GerritManagement;
import com.sonyericsson.hudson.plugins.gerrit.trigger.GerritServer;
import com.sonyericsson.hudson.plugins.gerrit.trigger.hudsontrigger.GerritTrigger;
import com.sonyericsson.hudson.plugins.gerrit.trigger.hudsontrigger.GerritTriggerParameters;
import hudson.AbortException;
import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.*;
import hudson.plugins.xcal.action.XcalscanAnalysisAction;
import hudson.plugins.xcal.payload.IssueDiff;
import hudson.plugins.xcal.service.GerritService;
import hudson.plugins.xcal.util.CommonUtil;
import hudson.plugins.xcal.util.JenkinsRouter;
import hudson.plugins.xcal.util.VariableUtil;
import hudson.security.ACL;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import jenkins.model.ParameterizedJobMixIn;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static hudson.plugins.xcal.util.Localization.getLocalized;

@Slf4j
public class XcalscanGerritPublisher extends Notifier {

    private final String credential;
    private AbstractBuild<?, ?> build;
    private TaskListener listener;

    @DataBoundConstructor
    public XcalscanGerritPublisher(String credential) {
        this.credential = credential;
    }

    public String getCredential() {
        return credential;
    }

    private StandardUsernamePasswordCredentials getCredentials(String credential) {
        log.info("[getCredentials] credential id: {}", credential);
        StandardUsernamePasswordCredentials passwordCredentials = null;
        if (StringUtils.isNotBlank(credential)) {
            passwordCredentials = CredentialsProvider.findCredentialById(credential, StandardUsernamePasswordCredentials.class, this.build);
        }
        return passwordCredentials;
    }

    private Map<String, String> getEnvs() throws IOException, InterruptedException {
        Map<String, String> envParamMap = new LinkedHashMap<>();
        EnvVars envVars = this.build.getEnvironment(this.listener);
        for (Map.Entry<String, String> entry : envVars.entrySet()) {
            envParamMap.put(entry.getKey(), entry.getValue());
        }
        return envParamMap;
    }

    private String retrieveServerName(Map<String, String> envVars, GerritTrigger trigger) {
        String serverName = envVars.get(GerritTriggerParameters.GERRIT_NAME.name());
        String triggerServerName = trigger != null ? trigger.getServerName() : null;
        serverName = MoreObjects.firstNonNull(serverName, triggerServerName);

        if (org.apache.commons.lang.StringUtils.isEmpty(serverName)) {
            throw new IllegalArgumentException(getLocalized("jenkins.plugin.error.gerrit.server.empty"));
        }
        return serverName;
    }

    private String retrieveChangeNumber(Map<String, String> envVars) {
        String changeNum = envVars.get(GerritTriggerParameters.GERRIT_CHANGE_NUMBER.name());
        if (org.apache.commons.lang.StringUtils.isEmpty(changeNum)) {
            throw new IllegalArgumentException(getLocalized("jenkins.plugin.error.gerrit.change.number.empty"));
        }
        Integer changeNumber = NumberUtils.createInteger(changeNum);
        if (changeNumber == null) {
            throw new IllegalArgumentException(getLocalized("jenkins.plugin.error.gerrit.change.number.format"));
        }
        return changeNum;
    }

    private String retrievePatchSetNumber(Map<String, String> envVars) {
        String patchSetNum = envVars.get(GerritTriggerParameters.GERRIT_PATCHSET_NUMBER.name());
        if (org.apache.commons.lang.StringUtils.isEmpty(patchSetNum)) {
            throw new IllegalArgumentException(getLocalized("jenkins.plugin.error.gerrit.patchset.number.empty"));
        }
        Integer patchSetNumber = NumberUtils.createInteger(patchSetNum);
        if (patchSetNumber == null) {
            throw new IllegalArgumentException(getLocalized("jenkins.plugin.error.gerrit.patchset.number.format"));
        }
        return patchSetNum;
    }

    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
        log.info("[perform] in XcalscanGerritPublisher");
        listener.getLogger().println("------------------------------------------------------------------------");
        listener.getLogger().println("[XcalscanGerritPublisher] processing Xcalscan gerrit review");
        listener.getLogger().println("------------------------------------------------------------------------");
        this.build = build;
        this.listener = listener;
        XcalscanAnalysisAction xcalscanAnalysisAction = null;

        try {
            Object xcalAction = build.getAllActions().stream().filter(action -> StringUtils.equalsIgnoreCase("hudson.plugins.xcal.action.XcalscanAnalysisAction", action.getClass().getName())).findFirst().orElse(null);
            if (xcalAction != null) {
                xcalscanAnalysisAction = CommonUtil.objectMapper.readValue(CommonUtil.writeObjectToJsonStringSilently(xcalAction), XcalscanAnalysisAction.class);
                //listener.getLogger().println(CommonUtil.formatString("xcalscanAnalysisAction: {}", xcalscanAnalysisAction));
            }
        } catch (Exception e) {
            listener.getLogger().println(CommonUtil.formatString("Exception, {}: {}", e.getClass(), e.getMessage()));
        }
        //get Credential
        StandardUsernamePasswordCredentials usernamePasswordCredentials = getCredentials(credential);

        //get Gerrit change refs
        Map<String, String> envParamMap = this.getEnvs();
        //load revision info

        GerritTrigger gerritTrigger = ParameterizedJobMixIn.getTrigger(build.getProject(), GerritTrigger.class);
        if (gerritTrigger != null && xcalscanAnalysisAction != null) {
            String gerritServerName = this.retrieveServerName(envParamMap, gerritTrigger);
            String changeNumber = this.retrieveChangeNumber(envParamMap);
            String patchSetNumber = this.retrievePatchSetNumber(envParamMap);

            GerritService gerritService = new GerritService(gerritServerName, usernamePasswordCredentials);
            this.postGerritReview(gerritService, changeNumber, patchSetNumber, xcalscanAnalysisAction, Locale.ENGLISH);
            this.postGerritReview(gerritService, changeNumber, patchSetNumber, xcalscanAnalysisAction, Locale.SIMPLIFIED_CHINESE);
        }
        return true;
    }

    private void postGerritReview(GerritService gerritService, String changeNumber, String patchSetNumber, XcalscanAnalysisAction xcalscanAnalysisAction, Locale locale) throws AbortException {
        String reviewMessage = Messages._XcalscanGerritPublisher_review_message_result(
                xcalscanAnalysisAction.getProjectName(),
                xcalscanAnalysisAction.getCommitId(),
                xcalscanAnalysisAction.getRisk(),
                xcalscanAnalysisAction.getIssuesCount(),
                xcalscanAnalysisAction.getDefiniteCount(),
                xcalscanAnalysisAction.getHighPriorityCount(),
                xcalscanAnalysisAction.getMediumPriorityCount(),
                xcalscanAnalysisAction.getLowPriorityCount(),
                xcalscanAnalysisAction.getUrl()).toString(locale);
        if (StringUtils.isNotBlank(xcalscanAnalysisAction.getBaselineCommitId())) {
            reviewMessage = reviewMessage + "\n" + Messages._XcalscanGerritPublisher_review_message_dsr(
                    xcalscanAnalysisAction.getNewIssueCount(),
                    xcalscanAnalysisAction.getFixedIssueCount(),
                    xcalscanAnalysisAction.getBaselineCommitId(),
                    xcalscanAnalysisAction.getDsrUrl()).toString(locale);
        }

        if (xcalscanAnalysisAction.getIssueDiffs().size() > 0) {
            List<IssueDiff> issueDiffs = xcalscanAnalysisAction.getIssueDiffs();
            JSONObject ruleInfo = xcalscanAnalysisAction.getRuleInfo();
            for (IssueDiff id : issueDiffs) {
                // Get the necessary values
                id.setPath(id.getIssue().
                        getIssueAttributes().
                        stream().
                        filter(c -> c.getName().equals(VariableUtil.IssueAttributeName.NO_OF_TRACE_SET)).
                        findFirst().get().getValue());
            }

            List<IssueDiff> newIssues = issueDiffs.stream().filter(issueDiff -> org.apache.commons.lang.StringUtils.equalsIgnoreCase("NEW", issueDiff.getType())).collect(Collectors.toList());
            List<IssueDiff> fixedIssues = issueDiffs.stream().filter(issueDiff -> org.apache.commons.lang.StringUtils.equalsIgnoreCase("FIXED", issueDiff.getType())).collect(Collectors.toList());

            //String[] header = {"Risk Level", "ID", "Type", "Rule & Standard", "File", "Line", "Function", "Variable", "Path", "Description"};

            if (newIssues.size() > 0) {
                //String[][] newIssueArr = generateIssueTable(newIssues, ruleInfo);
                //TextTable newIssueTable = new TextTable(header, newIssueArr);
                //reviewMessage = reviewMessage + "\n" + newIssueTable.toString();

                reviewMessage = reviewMessage + "\n" + Messages._XcalscanGerritPublisher_review_message_dsr_new(newIssues.size()).toString(locale);
                reviewMessage = reviewMessage + "\n" + getDSRDetailString(locale, ruleInfo, newIssues);
            }
            if (fixedIssues.size() > 0) {
                //String[][] fixedIssueArr = generateIssueTable(fixedIssues, ruleInfo);
                //TextTable fixedIssueTable = new TextTable(header, fixedIssueArr);
                //reviewMessage = reviewMessage + "\n" + fixedIssueTable.toString();

                reviewMessage = reviewMessage + "\n" + Messages._XcalscanGerritPublisher_review_message_dsr_fixed(newIssues.size()).toString(locale);
                reviewMessage = reviewMessage + "\n" + getDSRDetailString(locale, ruleInfo, fixedIssues);
            }
        }
        listener.getLogger().println(CommonUtil.formatString("[perform] reviewMessage: {}", reviewMessage));

        ReviewResult reviewResult;
        try {
            reviewResult = gerritService.getRevision(changeNumber, patchSetNumber).review(new ReviewInput().message(reviewMessage));
        } catch (RestApiException | NullPointerException | IllegalArgumentException | IllegalStateException e) {
            listener.getLogger().println(CommonUtil.formatString("Unable to post review, {}: {}", e.getClass(), e.getMessage()));
            throw new AbortException("Unable to post review: " + e.getMessage());
        }
        if (StringUtils.isEmpty(reviewResult.error)) {
            listener.getLogger().println("Review has been sent");
        } else {
            listener.getLogger().println(CommonUtil.formatString("Unable to post review: {}", reviewResult.error));
            throw new AbortException("Unable to post review: " + reviewResult.error);
        }
    }

    private String getDSRDetailString(Locale locale, JSONObject ruleInfo, List<IssueDiff> issueDiffs) {
        String reviewMessage = "";
        for (int i = 0; i < issueDiffs.size(); i++) {
            Map<String, String> severityMap = new HashMap<>();
            severityMap.put("HIGH", "高");
            severityMap.put("MEDIUM", "中");
            severityMap.put("LOW", "低");
            String ruleName;
            String severity;
            if (locale.equals(Locale.SIMPLIFIED_CHINESE)) {
                ruleName = JSONObject.fromObject(ruleInfo.get(issueDiffs.get(i).getIssueCode())).get("rule_name_chi").toString();
                severity = severityMap.get(issueDiffs.get(i).getSeverity());
            } else {
                ruleName = JSONObject.fromObject(ruleInfo.get(issueDiffs.get(i).getIssueCode())).get("rule_name_eng").toString();
                severity = issueDiffs.get(i).getSeverity();
            }
            reviewMessage = reviewMessage + Messages._XcalscanGerritPublisher_review_message_dsr_detail(
                    severity,
                    issueDiffs.get(i).getIssue().getSeq(),
                    issueDiffs.get(i).getIssueCode(),
                    ruleName,
                    issueDiffs.get(i).getIssue().getRuleInformation().getRuleSetDisplayName().toUpperCase(),
                    issueDiffs.get(i).getRelativePath(),
                    String.valueOf(issueDiffs.get(i).getLineNo()),
                    issueDiffs.get(i).getFunctionName(),
                    issueDiffs.get(i).getVariableName(),
                    issueDiffs.get(i).getPath()
            ).toString(locale) + "\n";
        }
        return reviewMessage;
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Symbol("XcalscanGerrit")
    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {


        public String getGlobalToolConfigUrl() {
            return JenkinsRouter.getGlobalToolConfigUrl();
        }

        public static final String CATEGORY = "Code-Review";

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types
            return true;
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.XcalscanGerritPublisher_descriptor_displayName();
        }

        public ListBoxModel doFillCredentialItems(@AncestorInPath Item item, @QueryParameter String credential) {
            log.info("[doFillCredentialItems] credential: {}", credential);
            StandardListBoxModel result = new StandardListBoxModel();
            if (Jenkins.get().hasPermission(Item.CONFIGURE)) {
                result.includeEmptyValue().includeAs(ACL.SYSTEM, item, StandardUsernamePasswordCredentials.class);
            }
            return result;
        }

        public ListBoxModel doFillGerritServerNameItems(@QueryParameter String gerritServerName) {
            log.info("[doFillGerritServerNameItems] gerritServerName: {}", gerritServerName);
            List<GerritServer> gerritServers = GerritManagement.get().getServers();
            ListBoxModel listBoxModel = new ListBoxModel();
            for (GerritServer gerritServer : gerritServers) {
                listBoxModel.add(new ListBoxModel.Option(gerritServer.getDisplayName(), gerritServer.getName()));
            }
            return listBoxModel;
        }

        public FormValidation doTestConnection(@QueryParameter String gerritServerName, @QueryParameter String credential) {
            FormValidation result;
            log.debug("[doTestConnection] gerritServerName: {}, credential: {}", gerritServerName, credential);

            if (StringUtils.isBlank(gerritServerName)) {
                return FormValidation.error(Messages.XcalscanGerritPublisher_form_validation_errors_empty_gerritServerName());
            }

            StandardUsernamePasswordCredentials usernamePasswordCredentials = this.getCredentials(credential);
            GerritService gerritService = new GerritService(gerritServerName, usernamePasswordCredentials);
            try {
                log.debug("[doTestConnection] gerrit version");
                String version = gerritService.getGerritApi().config().server().getVersion();
                log.debug("[doTestConnection] gerrit version: {}", version);
                if (StringUtils.isNotBlank(version)) {
                    result = FormValidation.okWithMarkup(Messages.XcalscanGerritPublisher_form_validation_success(version));
                } else {
                    result = FormValidation.error(Messages.XcalscanGerritPublisher_form_validation_errors_empty_serverVersion());
                }
            } catch (RestApiException e) {
                log.debug("[doTestConnection] {}: {}", e.getClass(), e.getMessage());
                result = FormValidation.error(CommonUtil.formatString("{}: {}", e.getClass(), e.getMessage()));
            }
            return result;
        }

        private StandardUsernamePasswordCredentials getCredentials(String credential) {
            StandardUsernamePasswordCredentials result = null;
            if (StringUtils.isNotBlank(credential)) {
                result = CredentialsMatchers.firstOrNull(
                        CredentialsProvider.lookupCredentials(
                                StandardUsernamePasswordCredentials.class,
                                Jenkins.getInstanceOrNull(),
                                ACL.SYSTEM,
                                Collections.emptyList()
                        ),
                        CredentialsMatchers.allOf(
                                CredentialsMatchers.always(),
                                CredentialsMatchers.withId(credential)
                        )

                );
            }
            return result;
        }
    }
}

