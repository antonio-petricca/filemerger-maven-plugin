package io.github.antoniopetricca.maven.plugins.filemerger;

import java.io.File;
import java.nio.charset.StandardCharsets;

public abstract class AbstractFileConfiguration {

    private String  charset = StandardCharsets.UTF_8.toString();
    private boolean filtering;

    public String getCharset() {
        return charset;
    }

    public boolean isFiltering() {
        return filtering;
    }

}
