package io.github.antoniopetricca.maven.plugins.filemerger.configuration;

import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.util.Properties;

public class TargetFile extends AbstractFile {

    private Integer indentation = 0;
    private String  propertiesSet;
    private String  sourceFilesSet;
    private File    targetFile;
    private File    templateFile;

    public Integer getIndentation() {
        return indentation;
    }

    public String getPropertiesSet() {
        return propertiesSet;
    }

    public String getSourceFilesSet() {
        return sourceFilesSet;
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
            ((null != sourceFilesSet ) && !sourceFilesSet.isEmpty()),
            "Null or empty source file set identifier."
        );

        validate(
            ((null != templateFile) && templateFile.exists()),
            "Template file null or not found."
        );
    }

}
