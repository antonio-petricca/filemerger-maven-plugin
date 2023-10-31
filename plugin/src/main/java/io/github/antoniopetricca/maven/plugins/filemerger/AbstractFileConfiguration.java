package io.github.antoniopetricca.maven.plugins.filemerger;

import org.apache.maven.plugin.MojoExecutionException;

import java.nio.charset.StandardCharsets;

public abstract class AbstractFileConfiguration {

    private String  charset   = StandardCharsets.UTF_8.name();
    private boolean filtering = false;

    protected void validate(boolean condition, String errorMessageFormat, Object... errorMessageArguments)
        throws MojoExecutionException
    {
        if (!condition) {
            throw new MojoExecutionException(
                String.format(
                    errorMessageFormat,
                    errorMessageArguments
                )
            );
        }
    }

    public String getCharset() {
        return charset;
    }

    public boolean isFiltering() {
        return filtering;
    }

    public abstract void validate()
        throws MojoExecutionException;

}
