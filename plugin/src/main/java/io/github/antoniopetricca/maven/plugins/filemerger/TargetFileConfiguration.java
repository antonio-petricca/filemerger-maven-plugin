package io.github.antoniopetricca.maven.plugins.filemerger;

public class TargetFileConfiguration extends AbstractFileConfiguration {

    private Integer                   indentation = 0;
    private SourceFileConfiguration[] sourceFiles;

    public Integer getIndentation() {
        return indentation;
    }

    public SourceFileConfiguration[] getSourceFiles() {
        return sourceFiles;
    }

}
