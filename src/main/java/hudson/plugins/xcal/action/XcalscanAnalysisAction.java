package hudson.plugins.xcal.action;

import hudson.model.InvisibleAction;
import hudson.plugins.xcal.payload.IssueDiff;
import lombok.*;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import javax.annotation.CheckForNull;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Persists in a build Xcalscan related information.
 */
@EqualsAndHashCode(callSuper = true)
@ExportedBean(defaultVisibility = 2)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class XcalscanAnalysisAction extends InvisibleAction {
    // Dashboard URL
    private String url;
    private String serverUrl;
    private UUID projectUUID;
    private String projectId;
    private String projectName;
    private UUID scanTaskId;
    private String status;
    private String risk;
    private String issuesCount;
    private String fileCount;
    private String lineCount;
    private String newIssueCount;
    private String newIssuePathCount;
    private String fixedIssueCount;
    private String fixedIssuePathCount;
    private String commitId;
    private String baselineCommitId;
    private String baselineScanTaskId;
    private String dsrUrl;
    private String dsrUrlFixed;
    private String dsrUrlNew;
    private String scanTimeTotal;
    private Date scanTime;
    private String highPriorityCount;
    private String mediumPriorityCount;
    private String lowPriorityCount;
    private String definiteCount;
    private boolean isNew;
    private boolean isSkipped;

    private List<IssueDiff> issueDiffs;

    JSONObject ruleInfo;


    public Double getDensity() {
        return Double.parseDouble(issuesCount) / Double.parseDouble(lineCount);
    }

    @CheckForNull
    @Exported
    public UUID getScanTaskId() {
        return scanTaskId;
    }

    @CheckForNull
    @Exported
    public UUID getProjectUUID() {
        return projectUUID;
    }

    @CheckForNull
    @Exported
    public String getServerUrl() {
        return serverUrl;
    }

    @Exported
    public boolean isNew() {
        return isNew;
    }

    @Exported
    public boolean isSkipped() {
        return isSkipped;
    }

    @CheckForNull
    @Exported(name = "xcalscanDashboardUrl")
    public String getUrl() {
        return url;
    }
}
