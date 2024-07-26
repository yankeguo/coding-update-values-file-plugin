package com.yankeguo.jenkins.plugins.updateremotefile.providers;

import com.yankeguo.jenkins.plugins.updateremotefile.Provider;
import com.yankeguo.jenkins.plugins.updateremotefile.ProviderException;

import java.util.Map;

public class CodingRepositoryProvider implements Provider {
    private final String username;
    private final String password;
    private final String target;

    public CodingRepositoryProvider(String username, String password, String target) {
        this.username = username;
        this.password = password;
        this.target = target;
    }

    @Override
    public Map<String, Object> fetch() throws ProviderException {
        //TODO: implement fetch
        return Map.of();
    }

    @Override
    public void update(Map<String, Object> data) throws ProviderException {
        //TODO: implement update
    }

}
