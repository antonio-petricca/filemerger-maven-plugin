package io.github.antoniopetricca.maven.plugins.filemerger.configuration;

import org.apache.maven.plugin.MojoExecutionException;

public class TargetFile extends AbstractFile {

    private boolean copyPermissions;
    private Integer  indentation = 0;
    private String   propertiesSet;
    private String   sourceFilesSet;
    private String   targetFolder;
    private String[] templateFilePatterns;

    public Integer getIndentation() {
        return indentation;
    }

    public boolean isCopyPermissions() {
        return copyPermissions;
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

    public String[] getTemplateFilePatterns() {
        return templateFilePatterns;
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
            ((null != targetFolder) && !targetFolder.isEmpty()),
            "Target folder null or empty."
        );

        validate(
            ((null != templateFilePatterns ) && (templateFilePatterns.length > 0)),
            "Template file patterns null or empty."
        );

        for (String templateFilePattern : templateFilePatterns) {
            validate(
                ((null != templateFilePattern) && !templateFilePattern.isEmpty()),
                String.format(
                    "Template file pattern \"%s\" null or empty.",
                   templateFilePattern
                )
            );
        }
    }

}
