package com.thegraid.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties specific to Launcher.
 * <p>
 * Properties are configured in the {@code application.yml} file.
 * See {@link tech.jhipster.config.JHipsterProperties} for a good example.
 */
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class ApplicationProperties {
    // jhipster-needle-application-properties-property
    // jhipster-needle-application-properties-property-getter
    // jhipster-needle-application-properties-property-class

    // When using env.getProperty() or @Value("${prop.path}") we don't need these:
    // (which enable props.thegraid.assetBase)
    // Thegraid thegraid = new Thegraid();
    // public Thegraid getThegraid() { return thegraid; }

    // public class Thegraid {
    //     public String assetBase;
    //     public String getAssetBase() { return assetBase; };
    //     public void setAssetBase(String base) { assetBase = base; }
    // }
}
