package com.yankeguo.jenkins.plugins.modstate;

public interface Provider {
    Object fetch() throws ProviderException;

    void update(Object data) throws ProviderException;
}
