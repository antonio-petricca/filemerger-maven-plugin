package io.github.antoniopetricca.maven.plugins.filemerger.configuration;

import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.util.Properties;

public class PropertiesSet extends AbstractSet {

    private Properties properties;
    private File[]     propertyFiles;

    public Properties getProperties() {
        return properties;
    }

    public File[] getPropertyFiles() {
        return propertyFiles;
    }

    public boolean hasPropertyFiles() {
        return ((null != propertyFiles) && (propertyFiles.length > 0));
    }

    @Override
    public void validate()
        throws MojoExecutionException
    {
        super.validate();

        if ((null != propertyFiles) && (propertyFiles.length > 0)) {
            for (File propertyFile : propertyFiles) {
                validate(
                    (propertyFile.exists() && propertyFile.isFile()),
                    String.format(
                        "File \"%s\" not found",
                        propertyFile.getAbsolutePath()
                    )
                );
            }
        }
    }

}