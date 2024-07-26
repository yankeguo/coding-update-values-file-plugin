package com.yankeguo.jenkins.plugins.modstate;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.matchers.ConstantMatcher;
import com.yankeguo.jenkins.plugins.modstate.providers.CodingRepositoryProviderFactory;
import edu.umd.cs.findbugs.annotations.NonNull;
import groovy.lang.Binding;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.GroovyShell;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.Queue;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

public class ModstateTask extends Builder {

    private static final String PROVIDER_CODING_REPO = "coding-repo";

    private static final Map<String, ProviderFactory> FACTORIES = new HashMap<>();

    static {
        FACTORIES.put(PROVIDER_CODING_REPO, new CodingRepositoryProviderFactory());
    }

    private static final Logger log = LoggerFactory.getLogger(ModstateTask.class);

    private final String provider;

    private final String credentialsId;

    private final String targets;

    private final String script;

    @DataBoundConstructor
    public ModstateTask(String provider, String credentialsId, String targets, String script) {
        super();
        this.provider = provider;
        this.credentialsId = credentialsId;
        this.targets = targets;
        this.script = script;
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

    public String getScript() {
        return script;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        PrintStream logger = listener.getLogger();

        if (getProvider() == null || getProvider().isBlank()) {
            logger.println("Provider not set");
            return false;
        }

        ProviderFactory factory = FACTORIES.get(getProvider());

        if (factory == null) {
            logger.println("Provider not found");
            return false;
        }

        if (getCredentialsId() == null || getCredentialsId().isBlank()) {
            logger.println("Credentials not set");
            return false;
        }

        StandardUsernamePasswordCredentials credentials = CredentialsProvider.findCredentialById(
                getCredentialsId(),
                StandardUsernamePasswordCredentials.class,
                build,
                Collections.emptyList()
        );

        if (credentials == null) {
            logger.println("Credentials not found");
            return false;
        }

        if (getTargets() == null || getTargets().isBlank()) {
            logger.println("Targets not set");
            return false;
        }

        if (getScript() == null || getScript().isBlank()) {
            logger.println("Script not set");
            return false;
        }

        for (String target : getTargets().split("\n")) {
            target = target.trim();
            if (target.isEmpty()) {
                continue;
            }
            try {
                Provider provider = factory.create(credentials.getUsername(), credentials.getPassword().getPlainText(), target);
                logger.println("Modstate: fetching target: " + target);
                Object val = provider.fetch();
                Map<String, String> env = build.getEnvironment(listener);

                Binding binding = new Binding();
                binding.setVariable("env", env);
                binding.setVariable("val", val);

                GroovyShell shell = new GroovyShell(binding);
                shell.evaluate(getScript());
                logger.println("Modstate: updating target: " + target);
                provider.update(val);
            } catch (ProviderException | GroovyRuntimeException e) {
                e.printStackTrace(logger);
                logger.println("Modstate: Failed to run script for target: " + target);
                return false;
            }
        }

        logger.println("Modstate: all done");

        return true;
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public DescriptorImpl() {
            super(ModstateTask.class);
            load();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        public ListBoxModel doFillCredentialsIdItems(@AncestorInPath Item project, @QueryParameter String url, @QueryParameter String credentialsId) {
            if (project == null && !Jenkins.get().hasPermission(Jenkins.ADMINISTER) || project != null && !project.hasPermission(Item.EXTENDED_READ)) {
                return new StandardListBoxModel().includeCurrentValue(credentialsId);
            }
            if (project == null) {
                /* Construct a fake project, suppress the deprecation warning because the
                 * replacement for the deprecated API isn't accessible in this context. */
                @SuppressWarnings("deprecation") Item fakeProject = new FreeStyleProject(Jenkins.get(), "fake-" + UUID.randomUUID());
                project = fakeProject;
            }
            return new StandardListBoxModel().includeEmptyValue().includeMatchingAs(
                    project instanceof Queue.Task ? Tasks.getAuthenticationOf((Queue.Task) project) : ACL.SYSTEM,
                    project,
                    StandardUsernamePasswordCredentials.class,
                    new ArrayList<>(),
                    new ConstantMatcher(true)
            ).includeCurrentValue(credentialsId);
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
                return "更新远程状态";
            }
            return "Modify Remote State";
        }
    }
}
