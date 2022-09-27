package hudson.plugins.xcal.service;

import com.cloudbees.plugins.credentials.common.UsernamePasswordCredentials;
import com.google.common.base.MoreObjects;
import com.google.gerrit.extensions.api.GerritApi;
import com.google.gerrit.extensions.api.changes.RevisionApi;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.sonyericsson.hudson.plugins.gerrit.trigger.GerritManagement;
import com.sonyericsson.hudson.plugins.gerrit.trigger.config.IGerritHudsonTriggerConfig;
import com.urswolfer.gerrit.client.rest.GerritAuthData;
import com.urswolfer.gerrit.client.rest.GerritRestApiFactory;
import hudson.util.Secret;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import static hudson.plugins.xcal.util.Localization.getLocalized;

@Data
@Slf4j
public class GerritService {

    private GerritApi gerritApi;
    private String serverName;
    private String username = null;
    private String password = null;

    public GerritService(String serverName, UsernamePasswordCredentials credentials) {
        this.serverName = serverName;
        if (credentials != null) {
            this.username = credentials.getUsername();
            this.password = Secret.toString(credentials.getPassword());
        }
        initialize();
    }

    private void initialize() {
        log.debug("[init] serverName: {}, username:{}, blank password: {}", serverName, username, StringUtils.isBlank(password));
        IGerritHudsonTriggerConfig gerritConfig = GerritManagement.getConfig(this.serverName);
        if (gerritConfig == null) {
            throw new IllegalArgumentException(getLocalized("jenkins.plugin.error.gerrit.config.empty"));
        }

        String gerritFrontEndUrl = gerritConfig.getGerritFrontEndUrl();

        boolean useRestApi = gerritConfig.isUseRestApi();
        checkRestApiAllowed(useRestApi);

        String username = getUsername(this.username, gerritConfig);
        String password = getPassword(this.password, gerritConfig);
        if (StringUtils.isEmpty(username)) {
            throw new IllegalArgumentException(getLocalized("jenkins.plugin.error.gerrit.user.empty"));
        }
        GerritAuthData.Basic authData = new GerritAuthData.Basic(gerritFrontEndUrl, username, password, useRestApi);
        gerritApi = new GerritRestApiFactory().create(authData);
        try {
            Integer size = gerritApi.changes().query().get().size();
            log.debug("[init] size: {}", size);

        } catch (RestApiException e) {
            log.debug("[init] {}: {}", e.getClass(), e.getMessage());
        }
    }

    public RevisionApi getRevision(String changeNumber, String patchSetNumber) throws RestApiException {
        return gerritApi.changes().id(changeNumber).revision(patchSetNumber);
    }

    private void checkRestApiAllowed(boolean useRestApi) {
        if (!useRestApi) {
            throw new IllegalStateException(getLocalized("jenkins.plugin.error.gerrit.restapi.off"));
        }
    }

    private String getUsername(String username, IGerritHudsonTriggerConfig gerritConfig) {
        return MoreObjects.firstNonNull(username, gerritConfig.getGerritHttpUserName());
    }

    private String getPassword(String password, IGerritHudsonTriggerConfig gerritConfig) {
        return MoreObjects.firstNonNull(password, gerritConfig.getGerritHttpPassword());
    }
}
