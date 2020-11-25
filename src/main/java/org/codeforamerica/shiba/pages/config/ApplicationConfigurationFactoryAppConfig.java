package org.codeforamerica.shiba.pages.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfigurationFactoryAppConfig {

    @Bean
    public ApplicationConfigurationFactory applicationConfigurationFactory() {
        ApplicationConfigurationFactory factory = new ApplicationConfigurationFactory();
        return factory;
    }

    @Bean
    public ApplicationConfiguration applicationConfiguration() throws Exception {
        return applicationConfigurationFactory().getObject();
    }

}
