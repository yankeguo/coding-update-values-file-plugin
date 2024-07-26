package com.yankeguo.jenkins.plugins.updateremotefile;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.util.Locale;

public class UpdateRemoteFile extends Builder {

    @DataBoundConstructor
    public UpdateRemoteFile() {
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        System.out.println("Hello, world!");
        return true;
    }

    @Extension
    public static final class BuildStepDescriptorImpl extends BuildStepDescriptor<Builder> {

        public BuildStepDescriptorImpl() {
            super(UpdateRemoteFile.class);
            load();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        @NonNull
        @Override
        public String getDisplayName() {
            Locale currentLocale = Locale.getDefault();
            if (currentLocale.getLanguage().equals("zh")) {
                return "更新远程文件（CODING 仓库等）";
            }
            return "Update remote file (CODING Repo, etc.)";
        }
    }
}
