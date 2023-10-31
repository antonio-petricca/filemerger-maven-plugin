package io.github.antoniopetricca.maven.plugins.filemerger;

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

}
