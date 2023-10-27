package io.github.antoniopetricca.maven.plugins.filemerger;

import java.io.File;
import java.nio.charset.StandardCharsets;

public class TargetFileConfiguration {

    private String                    charset     = StandardCharsets.UTF_8.toString();
    private File                      file;
    private Integer                   indentation = 0;
    private SourceFileConfiguration[] sourceFiles;

    public String getCharset() {
        return charset;
    }

    public File getFile() {
        return file;
    }

    public Integer getIndentation() {
        return indentation;
    }

    public SourceFileConfiguration[] getSourceFiles() {
        return sourceFiles;
    }

}
