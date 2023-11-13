package io.github.antoniopetricca.maven.plugins.filemerger.configuration;

import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;

public class SourceFile extends AbstractFile {

    private boolean binary;
    private File    file;
    private String  placeholder;

    public File getFile() {
        return file;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public boolean isBinary() {
        return binary;
    }

    @Override
    public void validate()
        throws MojoExecutionException
    {
        validate(
            ((null != placeholder) && !placeholder.isEmpty()),
            "Null or empty placeholder."
        );

        validate(
            ((null != file) && file.exists()),
            String.format(
                "Invalid source file (%s).",
                ((null != file) ? file.toString() : "null")
        ));
    }

}