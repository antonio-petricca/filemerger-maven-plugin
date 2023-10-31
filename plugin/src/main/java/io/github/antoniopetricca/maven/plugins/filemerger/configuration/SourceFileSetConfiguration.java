package io.github.antoniopetricca.maven.plugins.filemerger.configuration;

import org.apache.maven.plugin.MojoExecutionException;

public class SourceFileSetConfiguration extends AbstractConfiguration {

    private String                    id;
    private SourceFileConfiguration[] sourceFiles;

    public String getId() {
        return id;
    }

    public SourceFileConfiguration[] getSourceFiles() {
        return sourceFiles;
    }

    @Override
    public void validate()
        throws MojoExecutionException
    {
        validate(
            ((null != id) && !id.isEmpty()),
            "Null or empty source file set identifier."
        );

        validate(
            ((null != sourceFiles) && (sourceFiles.length > 0)),
            "Null or empty source files list."
        );

        for (SourceFileConfiguration sourceFileConfiguration : sourceFiles) {
            sourceFileConfiguration.validate();
        }
    }

}
