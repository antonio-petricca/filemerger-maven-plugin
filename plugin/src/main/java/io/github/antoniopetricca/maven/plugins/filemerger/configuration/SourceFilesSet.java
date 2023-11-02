package io.github.antoniopetricca.maven.plugins.filemerger.configuration;

import org.apache.maven.plugin.MojoExecutionException;

public class SourceFilesSet extends AbstractSet {

    private SourceFile[] sourceFiles;

    public SourceFile[] getSourceFiles() {
        return sourceFiles;
    }

    @Override
    public void validate()
        throws MojoExecutionException
    {
        super.validate();

        validate(
            (null != sourceFiles),
            "Null source files list."
        );

        for (SourceFile sourceFile : sourceFiles) {
            sourceFile.validate();
        }
    }

}
