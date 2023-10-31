package io.github.antoniopetricca.maven.plugins.filemerger.configuration;

import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.util.Properties;

public class TargetFileConfiguration extends AbstractFileConfiguration {

    private Integer    indentation = 0;
    private Properties properties;
    private String     sourceFileSet;
    private File       targetFile;
    private File       templateFile;

    public Integer getIndentation() {
        return indentation;
    }

    public Properties getProperties() {
        return properties;
    }

    public String getSourceFileSet() {
        return sourceFileSet;
    }

    public File getTargetFile() {
        return targetFile;
    }

    public File getTemplateFile() {
        return templateFile;
    }

    @Override
    public void validate()
        throws MojoExecutionException
    {
        validate(
            (indentation >= 0),
            "Indentation must be greater than zero."
        );

        validate(
            ((null != sourceFileSet) && !sourceFileSet.isEmpty()),
            "Null or empty source file set identifier."
        );

        validate(
            ((null != templateFile) && templateFile.exists()),
            "Template file null or not found."
        );
    }

}
