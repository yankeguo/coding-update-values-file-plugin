package com.yankeguo.jenkins.plugins.updateremotefile;

import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.matchers.ConstantMatcher;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.*;
import hudson.model.queue.Tasks;
import hudson.security.ACL;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

public class UpdateRemoteFileTask extends Builder {

    private static final String PROVIDER_CODING_REPO = "coding-repo";

    private final String provider;

    private final String credentialsId;

    private final String targets;

    private final String entries;

    @DataBoundConstructor
    public UpdateRemoteFileTask(String provider, String credentialsId, String targets, String entries) {
        super();
        this.provider = provider;
        this.credentialsId = credentialsId;
        this.targets = targets;
        this.entries = entries;
    }

    public String getProvider() {
        return provider;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    public String getTargets() {
        return targets;
    }

    public String getEntries() {
        return entries;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        System.out.println("Provider: " + getProvider());
        System.out.println("Credentials ID: " + getCredentialsId());
        System.out.println("Targets: " + getTargets());
        System.out.println("Entries: " + getEntries());
        return true;
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public DescriptorImpl() {
            super(UpdateRemoteFileTask.class);
            load();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        public ListBoxModel doFillCredentialsIdItems(@AncestorInPath Item project,
                                                     @QueryParameter String url,
                                                     @QueryParameter String credentialsId) {
            if (project == null && !Jenkins.get().hasPermission(Jenkins.ADMINISTER) ||
                    project != null && !project.hasPermission(Item.EXTENDED_READ)) {
                return new StandardListBoxModel().includeCurrentValue(credentialsId);
            }
            if (project == null) {
                /* Construct a fake project, suppress the deprecation warning because the
                 * replacement for the deprecated API isn't accessible in this context. */
                @SuppressWarnings("deprecation")
                Item fakeProject = new FreeStyleProject(Jenkins.get(), "fake-" + UUID.randomUUID());
                project = fakeProject;
            }
            return new StandardListBoxModel()
                    .includeEmptyValue()
                    .includeMatchingAs(
                            project instanceof Queue.Task
                                    ? Tasks.getAuthenticationOf((Queue.Task) project)
                                    : ACL.SYSTEM,
                            project,
                            StandardUsernamePasswordCredentials.class,
                            new ArrayList<>(),
                            new ConstantMatcher(true)
                    )
                    .includeCurrentValue(credentialsId);
        }

        public ListBoxModel doFillProviderItems() {
            ListBoxModel model = new ListBoxModel();
            if (Locale.getDefault().getLanguage().equals("zh")) {
                model.add("CODING 仓库", PROVIDER_CODING_REPO);
            } else {
                model.add("CODING Repository", PROVIDER_CODING_REPO);
            }
            return model;
        }

        @NonNull
        @Override
        public String getDisplayName() {
            if (Locale.getDefault().getLanguage().equals("zh")) {
                return "更新远程文件（CODING 仓库等）";
            }
            return "Update remote file (CODING Repo, etc.)";
        }
    }
}