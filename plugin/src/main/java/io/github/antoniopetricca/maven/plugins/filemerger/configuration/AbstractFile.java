package io.github.antoniopetricca.maven.plugins.filemerger.configuration;

import java.nio.charset.StandardCharsets;

public abstract class AbstractFile extends AbstractValidObject {

    private String  charset   = StandardCharsets.UTF_8.name();
    private boolean filtering = false;

    public String getCharset() {
        return charset;
    }

    public boolean isFiltering() {
        return filtering;
    }

}
