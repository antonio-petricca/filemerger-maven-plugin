package io.github.antoniopetricca.maven.plugins.filemerger.configuration;

import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;

public class SourceFileConfiguration extends AbstractFileConfiguration {

    private boolean encode;
    private File    file;
    private String  placeholder;

    public File getFile() {
        return file;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public boolean isEncode() {
        return encode;
    }

    @Override
    public void validate()
        throws MojoExecutionException
    {
        validate(
            ((null != placeholder) && !placeholder.isEmpty()),
            "Null or empty placeholder."
        );

        validate(
            ((null != file) && file.exists()),
            "Source file null or not found."
        );
    }

}
