package io.github.antoniopetricca.maven.plugins.filemerger;

import java.io.File;

public class SourceFileConfiguration {

    private File    file;
    private boolean isBinary;
    private String  placeholder;

    public File getFile() {
        return file;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public boolean isBinary() {
        return isBinary;
    }

}
