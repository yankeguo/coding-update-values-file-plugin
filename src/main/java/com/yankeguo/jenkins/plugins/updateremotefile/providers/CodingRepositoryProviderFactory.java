package com.yankeguo.jenkins.plugins.updateremotefile.providers;

import com.yankeguo.jenkins.plugins.updateremotefile.Provider;
import com.yankeguo.jenkins.plugins.updateremotefile.ProviderException;
import com.yankeguo.jenkins.plugins.updateremotefile.ProviderFactory;

public class CodingRepositoryProviderFactory implements ProviderFactory {
    @Override
    public Provider create(String username, String password, String target) throws ProviderException {
        return new CodingRepositoryProvider(username, password, target);
    }
}
