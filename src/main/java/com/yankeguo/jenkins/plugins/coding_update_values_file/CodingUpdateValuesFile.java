package com.yankeguo.jenkins.plugins.coding_update_values_file;

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

public class CodingUpdateValuesFile extends Builder {

    @DataBoundConstructor
    public CodingUpdateValuesFile() {
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        System.out.println("Hello, world!");
        return true;
    }

    @Extension
    public static final class BuildStepDescriptorImpl extends BuildStepDescriptor<Builder> {

        public BuildStepDescriptorImpl() {
            super(CodingUpdateValuesFile.class);
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
                return "更新 CODING 仓库中的键值文件";
            }
            return "Update values file in a CODING repository";
        }
    }
}
