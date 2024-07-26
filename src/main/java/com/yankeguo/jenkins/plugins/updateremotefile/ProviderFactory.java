package com.yankeguo.jenkins.plugins.updateremotefile;

public interface ProviderFactory {
    Provider create(String username, String password, String target) throws ProviderException;
}
