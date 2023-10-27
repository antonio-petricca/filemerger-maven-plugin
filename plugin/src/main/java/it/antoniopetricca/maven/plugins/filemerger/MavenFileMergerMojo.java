package it.antoniopetricca.maven.plugins.filemerger;

// https://www.baeldung.com/maven-plugin

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;

@Mojo(
    defaultPhase = LifecyclePhase.PROCESS_RESOURCES,
    name         = "file-merge"
)
public class MavenFileMergerMojo extends AbstractMojo {

    private final Log log = this.getLog();

    @Parameter(required = true)
    private TargetFileConfiguration[] targetFiles;

    private String getFileContent(File file, Charset charset)
        throws IOException
    {
        return new String(
            Files.readAllBytes(
                file.toPath()
            ),
            charset
        );
    }

    private void mergeTargetFile(TargetFileConfiguration targetFileConfiguration)
        throws IOException
    {
        File targetFile = targetFileConfiguration.getFile();

        log.info(String.format(
            "Merging target file \"%s\"...",
            targetFile.toString()
        ));

        Charset charset = Charset.forName(
            targetFileConfiguration.getCharset()
        );

        String targetFileContent = getFileContent(targetFile, charset);

        for (SourceFileConfiguration sourceFileConfiguration : targetFileConfiguration.getSourceFiles()) {
            File sourceFile = sourceFileConfiguration.getFile();

            log.info(String.format(
                "Merging source file \"%s\"...",
                sourceFile.toString()
            ));

            String sourceFileContent = getFileContent(sourceFile, charset);

            targetFileContent = targetFileContent.replaceAll(
                sourceFileConfiguration.getPlaceholder(),
                sourceFileContent
            );
        }

        log.info("Done.");
    }

    @Override
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        log.info(String.format(
            "Merging %d file(s)...",
            targetFiles.length
        ));

        for (TargetFileConfiguration targetFileConfiguration : targetFiles) {
            try {
                mergeTargetFile(targetFileConfiguration);
            } catch(IOException exception) {
                throw new RuntimeException(exception);
            }
        }
    }

}
