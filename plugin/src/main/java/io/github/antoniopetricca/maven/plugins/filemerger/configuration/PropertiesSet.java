package io.github.antoniopetricca.maven.plugins.filemerger.configuration;

import org.apache.maven.plugin.MojoExecutionException;

import java.util.Properties;

public class PropertiesSet extends AbstractSet {

    private Properties properties;
    private String[]   propertyFilePatterns;

    public Properties getProperties() {
        return properties;
    }

    public String[] getPropertyFilePatterns() {
        return propertyFilePatterns;
    }

    public boolean hasPropertyFiles() {
        return ((null != propertyFilePatterns ) && ( propertyFilePatterns.length > 0));
    }

    @Override
    public void validate()
        throws MojoExecutionException
    {
        super.validate();

        if ((null != propertyFilePatterns ) && ( propertyFilePatterns.length > 0)) {
            for (String propertyFilePattern : propertyFilePatterns) {
                validate(
                    ((null != propertyFilePattern) && !propertyFilePattern.isEmpty()),
                    String.format(
                        "Property file pattern \"%s\" null or empty.",
                        propertyFilePattern
                    )
                );
            }
        }
    }

}
