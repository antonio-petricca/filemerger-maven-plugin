package io.github.antoniopetricca.maven.plugins.filemerger;

import java.io.File;

public class SourceFileConfiguration extends AbstractFileConfiguration {

    private boolean encode;
    private String  placeholder;

    public String getPlaceholder() {
        return placeholder;
    }

    public boolean isEncode() {
        return encode;
    }

}
