package io.github.antoniopetricca.maven.plugins.filemerger.configuration;

import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;

public class TargetFile extends AbstractFile {

    private Integer indentation = 0;
    private String  propertiesSet;
    private String  sourceFilesSet;
    private String  targetFolder;
    private String  templateFiles;

    public Integer getIndentation() {
        return indentation;
    }

    public String getPropertiesSet() {
        return propertiesSet;
    }

    public String getSourceFilesSet() {
        return sourceFilesSet;
    }

    public String getTargetFolder() {
        return targetFolder;
    }

    public String getTemplateFiles() {
        return templateFiles;
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
            ((null != targetFolder ) && !targetFolder.isEmpty()),
            "Target folder null or not found."
        );

        validate(
            ((null != templateFiles) && !templateFiles.isEmpty()),
            "Template file null or not found."
        );
    }

}
