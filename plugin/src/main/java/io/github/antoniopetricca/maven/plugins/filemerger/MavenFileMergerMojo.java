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
import java.util.Properties;
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

    private void ensureDestinationFile(File file)
        throws IOException
    {
        File path = file.getParentFile();

        if (!path.exists()) {
            log.info(String.format(
                "Creating destination folder \"%s\"...",
                path
            ));

            Files.createDirectories(
                path.toPath()
            );
        }

        if (!file.exists()) {
            log.info(String.format(
                "Creating destination file \"%s\"...",
                file
            ));

            file.createNewFile();
        }
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

    private Charset getCharset(AbstractFileConfiguration fileConfiguration) {
        return Charset.forName(
            fileConfiguration.getCharset()
        );
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

    private String getIndentation(TargetFileConfiguration targetFileConfiguration) {
        Integer indentationAmount = targetFileConfiguration.getIndentation();
        String  indentationString = null;

        if (indentationAmount > 0) {
            indentationString = StringUtils.leftPad("", indentationAmount, " ");
        }

        return indentationString;
    }

    private String mergeSourceFile(
        SourceFileConfiguration sourceFileConfiguration,
        String                  targetFileContent,
        String                  indentationString
    )
        throws IOException, MavenFilteringException
    {
        Charset sourceCharset = getCharset(sourceFileConfiguration);
        File    sourceFile    = sourceFileConfiguration.getFile();

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
        Properties propertiesBackup = setProperties(targetFileConfiguration);
        File       destinationFile  = targetFileConfiguration.getDestination();
        File       templateFile     = targetFileConfiguration.getTemplate();

        if (null == destinationFile) {
            destinationFile = templateFile;
        }

        log.info(String.format(
            "Merging template file \"%s\" into \"%s\"...",
            templateFile,
            destinationFile
        ));

        ensureDestinationFile(destinationFile);

        Charset destinationCharset     = getCharset(targetFileConfiguration);
        String  destinationFileContent = getFileContent(templateFile, destinationCharset);
        String  indentation            = getIndentation(targetFileConfiguration);

        for (SourceFileConfiguration sourceFileConfiguration : targetFileConfiguration.getSourceFiles()) {
            destinationFileContent = mergeSourceFile(
                sourceFileConfiguration,
                destinationFileContent,
                indentation
            );
        }

        log.info("Writing merged destination file...");

        if (targetFileConfiguration.isFiltering()) {
            destinationFileContent = filterFileContent(destinationFileContent);
        }

        Files.write(
            destinationFile.toPath(),
            destinationFileContent.getBytes(destinationCharset)
        );

        setProperties(propertiesBackup);
    }

    private Properties setProperties(TargetFileConfiguration targetFileConfiguration) {
        return setProperties(
            targetFileConfiguration.getProperties()
        );
    }

    private Properties setProperties(Properties newProperties) {
        Properties currentProperties = mavenProject.getProperties();
        Properties backupProperties  = (Properties)currentProperties.clone();

        newProperties
            .entrySet()
            .forEach(
                property ->
                    currentProperties.setProperty(
                        (String)property.getKey(),
                        (String)property.getValue()
                    )
            );

        return backupProperties;
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
