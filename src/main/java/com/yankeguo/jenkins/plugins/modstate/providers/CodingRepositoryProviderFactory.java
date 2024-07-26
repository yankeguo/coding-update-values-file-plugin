package com.yankeguo.jenkins.plugins.modstate.providers;

import com.yankeguo.jenkins.plugins.modstate.Provider;
import com.yankeguo.jenkins.plugins.modstate.ProviderException;
import com.yankeguo.jenkins.plugins.modstate.ProviderFactory;

public class CodingRepositoryProviderFactory implements ProviderFactory {
    @Override
    public Provider create(String username, String password, String target) throws ProviderException {
        return new CodingRepositoryProvider(username, password, target);
    }
}
