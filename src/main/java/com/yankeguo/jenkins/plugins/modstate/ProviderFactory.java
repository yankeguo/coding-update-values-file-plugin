package com.yankeguo.jenkins.plugins.modstate;

public interface ProviderFactory {
    Provider create(String username, String password, String target) throws ProviderException;
}
