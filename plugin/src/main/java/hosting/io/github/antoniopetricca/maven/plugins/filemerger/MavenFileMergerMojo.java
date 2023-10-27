package hosting.io.github.antoniopetricca.maven.plugins.filemerger;

// https://www.baeldung.com/maven-plugin

import org.apache.commons.lang3.StringUtils;
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
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Mojo(
    defaultPhase = LifecyclePhase.PROCESS_RESOURCES,
    name         = "file-merge"
)
public class MavenFileMergerMojo extends AbstractMojo {

    private final Log log = this.getLog();

    @Parameter(required = true)
    private TargetFileConfiguration[] targetFiles;

    private byte[] getFileBytes(File file)
        throws IOException
    {
        return Files.readAllBytes(
            file.toPath()
        );
    }

    private String getFileContent(File file, Charset charset)
        throws IOException
    {
        return new String(
            getFileBytes(file),
            charset
        );
    }

    private List<String> getFileLines(File file, Charset charset)
        throws IOException
    {
        return Files.readAllLines(
            file.toPath(),
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

        String  targetFileContent = getFileContent(targetFile, charset);

        Integer indentationAmount = targetFileConfiguration.getIndentation();
        String  indentationString = null;

        if (indentationAmount > 0) {
            indentationString = StringUtils.leftPad("", indentationAmount, " ");
        }

        for (SourceFileConfiguration sourceFileConfiguration : targetFileConfiguration.getSourceFiles()) {
            File sourceFile = sourceFileConfiguration.getFile();

            log.info(String.format(
                "Merging source file \"%s\"...",
                sourceFile.toString()
            ));

            String sourceFileContent;

            if (sourceFileConfiguration.isBinary())  {
                byte[] sourceFileBytes = getFileBytes(sourceFile);

                sourceFileContent = Base64
                    .getEncoder()
                    .encodeToString(sourceFileBytes);

                if (null != indentationString) {
                    sourceFileContent = (indentationString + sourceFileContent);
                }
            } else {
                if (null != indentationString) {
                    List<String> sourceFileLines   = getFileLines(sourceFile, charset);
                    final String streamIndentation = indentationString;

                    sourceFileContent = sourceFileLines
                        .stream()
                        .map(line -> (streamIndentation + line))
                        .collect(Collectors.joining("\n"));
                } else {
                    sourceFileContent = getFileContent(sourceFile, charset);
                }
            }

            targetFileContent = targetFileContent.replaceAll(
                sourceFileConfiguration.getPlaceholder(),
                sourceFileContent
            );
        }

        log.info("Writing merged target file...");

        Files.write(
            targetFile.toPath(),
            targetFileContent.getBytes(charset)
        );
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

        log.info("Done.");
    }

}
