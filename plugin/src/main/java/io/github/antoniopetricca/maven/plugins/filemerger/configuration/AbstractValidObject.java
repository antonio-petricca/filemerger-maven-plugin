package io.github.antoniopetricca.maven.plugins.filemerger.configuration;

import org.apache.maven.plugin.MojoExecutionException;

public abstract class AbstractValidObject {

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

    public abstract void validate()
        throws MojoExecutionException;

}
