package io.github.antoniopetricca.maven.plugins.filemerger;

// https://www.baeldung.com/maven-plugin

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.filtering.DefaultMavenReaderFilter;
import org.apache.maven.shared.filtering.MavenFilteringException;

import java.io.*;
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

    @Component(role = DefaultMavenReaderFilter.class)
    protected DefaultMavenReaderFilter defaultMavenReaderFilter;

    @Component(role = MavenProject.class)
    MavenProject mavenProject;

    @Component(role = MavenSession.class)
    MavenSession mavenSession;

    @Parameter(required = true)
    private TargetFileConfiguration[] targetFiles;

    String convertReaderToString(Reader reader) throws IOException {
        StringWriter writer = new StringWriter();
        char[]       buffer = new char[8192];

        int charsRead;

        while (
            (charsRead = reader.read(buffer, 0, buffer.length)) != -1
        ) {
            writer.write(buffer, 0, charsRead);
        }

        return writer.toString();
    }

    private String filterFileContent(String content)
        throws IOException, MavenFilteringException
    {
        log.info("Filtering...");

        StringReader sourceReader  = new StringReader(content);

        Reader targetReader = defaultMavenReaderFilter.filter(
            sourceReader,
            true,
            mavenProject,
            null,
            false,
            mavenSession
        );

        return convertReaderToString(targetReader);
    }

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

    private String mergeSourceFile(
        SourceFileConfiguration sourceFileConfiguration,
        String                  targetFileContent,
        String                  indentationString
    )
        throws IOException, MavenFilteringException
    {
        Charset sourceCharset = Charset.forName(
            sourceFileConfiguration.getCharset()
        );

        File sourceFile = sourceFileConfiguration.getFile();

        log.info(String.format(
            "Merging source file \"%s\"...",
            sourceFile.toString()
        ));

        String sourceFileContent;

        if (sourceFileConfiguration.isEncode())  {
            sourceFileContent = getFileContent(sourceFile, sourceCharset);

            if (sourceFileConfiguration.isFiltering()) {
                sourceFileContent = filterFileContent(sourceFileContent);
            }

            byte[] sourceFileBytes = sourceFileContent.getBytes(sourceCharset);

            sourceFileContent = Base64
                .getEncoder()
                .encodeToString(sourceFileBytes);

            if (null != indentationString) {
                sourceFileContent = (indentationString + sourceFileContent);
            }
        } else {
            if (null != indentationString) {
                List<String> sourceFileLines   = getFileLines(sourceFile, sourceCharset);
                final String streamIndentation = indentationString;

                sourceFileContent = sourceFileLines
                    .stream()
                    .map(line -> (streamIndentation + line))
                    .collect(Collectors.joining("\n"));
            } else {
                sourceFileContent = getFileContent(sourceFile, sourceCharset);
            }

            if (sourceFileConfiguration.isFiltering()) {
                sourceFileContent = filterFileContent(sourceFileContent);
            }
        }

        return targetFileContent.replace(
            sourceFileConfiguration.getPlaceholder(),
            sourceFileContent
        );
    }

    private void mergeTargetFile(TargetFileConfiguration targetFileConfiguration)
        throws IOException, MavenFilteringException
    {
        File targetFile = targetFileConfiguration.getFile();

        log.info(String.format(
            "Merging target file \"%s\"...",
            targetFile.toString()
        ));

        Charset targetCharset = Charset.forName(
            targetFileConfiguration.getCharset()
        );

        String  targetFileContent = getFileContent(targetFile, targetCharset);

        Integer indentationAmount = targetFileConfiguration.getIndentation();
        String  indentationString = null;

        if (indentationAmount > 0) {
            indentationString = StringUtils.leftPad("", indentationAmount, " ");
        }

        for (SourceFileConfiguration sourceFileConfiguration : targetFileConfiguration.getSourceFiles()) {
            targetFileContent = mergeSourceFile(
                sourceFileConfiguration,
                targetFileContent,
                indentationString
            );
        }

        log.info("Writing merged target file...");

        if (targetFileConfiguration.isFiltering()) {
            targetFileContent = filterFileContent(targetFileContent);
        }

        Files.write(
            targetFile.toPath(),
            targetFileContent.getBytes(targetCharset)
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
            } catch(IOException | MavenFilteringException exception) {
                throw new RuntimeException(exception);
            }
        }

        log.info("Done.");
    }

}
