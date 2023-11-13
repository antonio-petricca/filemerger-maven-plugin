package io.github.antoniopetricca.maven.plugins.filemerger.configuration;

import org.apache.maven.plugin.MojoExecutionException;

public abstract class AbstractSet extends AbstractValidObject {

    private String id;

    public String getId() {
        return id;
    }@Override

    public void validate()
        throws MojoExecutionException
    {
        validate(
            ((null != id) && !id.isEmpty()),
            "Null or empty source file set identifier."
        );
    }

}