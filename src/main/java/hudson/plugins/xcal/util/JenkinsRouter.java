package hudson.plugins.xcal.util;

import hudson.util.VersionNumber;
import jenkins.model.Jenkins;

/**
 * Utility class to encapsulate the details of routing information in the Jenkins web application.
 * Use this class to get Jenkins URLs and relative paths.
 */
public final class JenkinsRouter {
  private static boolean isBeforeV2(){
    boolean isBeforeV2;
    VersionNumber version = Jenkins.getVersion();
    if(version==null){
      isBeforeV2 = false;
    }else {
      isBeforeV2 = version.isOlderThan(new VersionNumber("2"));
    }
    return isBeforeV2;
  }

  public static final boolean BEFORE_V2 = isBeforeV2();

  private JenkinsRouter() {
    throw new AssertionError("utility class, forbidden constructor");
  }

  public static String getGlobalToolConfigUrl() {
    return getRootUrl() + getGlobalToolConfigRelPath();
  }

  private static String getRootUrl() {
    return Jenkins.get().getRootUrl();
  }

  private static String getGlobalToolConfigRelPath() {
    return BEFORE_V2 ? "configure" : "configureTools";
  }

}
