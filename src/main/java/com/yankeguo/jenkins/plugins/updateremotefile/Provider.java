package com.yankeguo.jenkins.plugins.updateremotefile;

import java.util.Map;

public interface Provider {
    Map<String, Object> fetch() throws ProviderException;

    void update(Map<String, Object> data) throws ProviderException;
}
