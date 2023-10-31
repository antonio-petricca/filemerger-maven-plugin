package io.github.antoniopetricca.maven.plugins.filemerger;

import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.util.Properties;

public class TargetFileConfiguration extends AbstractFileConfiguration {

    private Integer                   indentation = 0;
    private Properties                properties;
    private SourceFileConfiguration[] sourceFiles;
    private File                      targetFile;
    private File                      templateFile;

    public Integer getIndentation() {
        return indentation;
    }

    public Properties getProperties() {
        return properties;
    }

    public SourceFileConfiguration[] getSourceFiles() {
        return sourceFiles;
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
            ((null != sourceFiles) && (sourceFiles.length > 0)),
            "Null or empty source files list."
        );

        for (SourceFileConfiguration sourceFileConfiguration : sourceFiles) {
            sourceFileConfiguration.validate();
        }

        validate(
            ((null != templateFile) && templateFile.exists()),
            "Template file null or not found."
        );
    }

}
