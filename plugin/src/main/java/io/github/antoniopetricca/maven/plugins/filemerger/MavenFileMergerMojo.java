package io.github.antoniopetricca.maven.plugins.filemerger;

// https://www.baeldung.com/maven-plugin

import io.github.antoniopetricca.maven.plugins.filemerger.configuration.*;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.filtering.DefaultMavenReaderFilter;
import org.apache.maven.shared.filtering.MavenFilteringException;
import org.codehaus.plexus.util.DirectoryScanner;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.*;
import java.util.stream.Collectors;

@Mojo(
    defaultPhase = LifecyclePhase.PROCESS_RESOURCES,
    name         = "file-merge"
)
public class MavenFileMergerMojo extends AbstractMojo {

    private final Log log = this.getLog();

    @Component(role = DefaultMavenReaderFilter.class)
    protected DefaultMavenReaderFilter defaultMavenReaderFilter;

    @Parameter(defaultValue = "${project}", readonly = true)
    MavenProject mavenProject;

    @Parameter(defaultValue = "${session}", readonly = true)
    MavenSession mavenSession;

    @Parameter(required = false)
    private PropertiesSet[] propertiesSets;

    @Parameter(defaultValue = "false", required = false)
    private boolean skip;

    @Parameter(required = false)
    private SourceFilesSet[] sourceFilesSets;

    @Parameter(required = true)
    private TargetFile[] targetFiles;

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

    private void copyFilePermissions(Path sourcePath, Path targetPath)
        throws IOException
    {
        Set<PosixFilePermission> sourcePermissions = Files.getPosixFilePermissions(sourcePath);
        Files.setPosixFilePermissions(targetPath, sourcePermissions);
    }

    private File ensureTargetFile(File targetFolder, File templateFile, boolean copyPermissions)
        throws IOException
    {
        Path   targetPath       = targetFolder.toPath();
        String templateFileName = templateFile.getName();

        if (!targetFolder.exists()) {
            log.info(String.format(
                "Creating target folder \"%s\"...",
                targetFolder
            ));

            Files.createDirectories(targetPath);
        }

        File targetFile = targetPath
            .resolve(templateFileName)
            .toFile();

        if (!targetFile.exists()) {
            log.info(String.format(
                "Creating target file \"%s\"...",
                targetFile
            ));

            targetFile.createNewFile();
        }

        if (copyPermissions) {
            log.info("Applying file permissions...");

            if (SystemUtils.IS_OS_LINUX || SystemUtils.IS_OS_LINUX) {
                copyFilePermissions(
                    templateFile.toPath(),
                    targetFile.toPath()
                );
            } else {
                log.warn("File permission not applied because OS is not a unix OS.");
            }
        }

        return targetFile;
    }

    private String filterContent(String content)
        throws IOException, MavenFilteringException
    {
        if (log.isDebugEnabled()) {
            log.debug("Filtering...");
        }

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

    private Charset getCharset(AbstractFile fileConfiguration) {
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

    private String getIndentation(TargetFile targetFileConfiguration) {
        Integer indentationAmount = targetFileConfiguration.getIndentation();
        String  indentationString = null;

        if (indentationAmount > 0) {
            indentationString = StringUtils.leftPad("", indentationAmount, " ");
        }

        return indentationString;
    }

    private Properties getPropertiesConfiguration(String setId)
        throws IOException, MojoExecutionException, MavenFilteringException
     {
        Optional<PropertiesSet> propertiesSetConfiguration =
            Arrays.stream(this.propertiesSets)
                  .filter(
                      propertiesSet -> propertiesSet.getId().equals(setId)
                  )
                  .findFirst();

        if (!propertiesSetConfiguration.isPresent()) {
            throw new MojoExecutionException(
                String.format(
                    "Properties set \"%s\" not found.",
                    setId
                )
            );
        }

        PropertiesSet propertiesSet = propertiesSetConfiguration.get();
        propertiesSet.validate();

        Properties properties = propertiesSetConfiguration
            .get().getProperties();

        if ((null == properties) || (0 == properties.size())) {
            properties = new Properties();
        }

        if (propertiesSet.hasPropertyFiles()) {
            String[] propertyFilePatterns = scanForFiles(
                propertiesSet.getPropertyFilePatterns()
            );

            for (String propertyFilePattern : propertyFilePatterns) {
                Properties fileProperties = loadProperties(propertyFilePattern);

                if ((null != fileProperties) && (fileProperties.size() > 0)) {
                    properties.putAll(fileProperties);
                }
            }
        }

        return properties;
    }

    private SourceFile[] getSourceFilesConfiguration(String setId)
        throws MojoExecutionException
    {
        Optional<SourceFilesSet> sourceFileConfigurations =
            Arrays.stream(this.sourceFilesSets)
                .filter(
                    sourceFilesSet -> sourceFilesSet.getId().equals(setId)
                )
                .findFirst();

        if (!sourceFileConfigurations.isPresent()) {
           throw new MojoExecutionException(
                String.format(
                    "Source file set \"%s\" not found.",
                    setId
                )
           );
        }

        SourceFilesSet sourceFilesSet =
            sourceFileConfigurations.get();

        sourceFilesSet.validate();

        return sourceFileConfigurations
            .get()
            .getSourceFiles();
    }

    private Properties loadProperties(String propertyFile)
        throws IOException, MavenFilteringException
    {
        log.info(String.format(
            "Loading property file \"%s\"...",
            propertyFile
        ));

        File            file        = new File(propertyFile);
        String          fileExt     = FilenameUtils.getExtension(file.getName());
        FileInputStream inputStream = new FileInputStream(file);
        Properties      properties  = new Properties();

        if (fileExt.equalsIgnoreCase("xml"))
        {
            properties.loadFromXML(inputStream);
        } else {
            properties.load(inputStream);
        }

        for (String name : properties.stringPropertyNames()) {
            String value = properties.getProperty(name);
            value        = filterContent(value);

            properties.setProperty(name, value);
        }

        return properties;
    }

    private String mergeSourceFile(
        SourceFile sourceFileConfiguration,
        String     targetFileContent,
        String     indentationString
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
                sourceFileContent = filterContent(sourceFileContent);
            }
        }

        return targetFileContent.replace(
            sourceFileConfiguration.getPlaceholder(),
            sourceFileContent
        );
    }

    private void mergeTargetFile(
        File         targetFolder,
        String       templateFileName,
        SourceFile[] sourceFilesConfiguration,
        Charset      targetCharset,
        String       indentation,
        boolean      filtering,
        boolean      copyPermissions
    )
        throws IOException, MavenFilteringException, MojoExecutionException
    {
        log.info(String.format(
            "Merging template file \"%s\" into \"%s\"...",
            templateFileName,
            targetFolder
        ));

        File templateFile = new File(templateFileName);

        if (!templateFile.exists()) {
            throw new MojoExecutionException(String.format(
                "Template file \"%s\" does not exists.",
                templateFileName
            ));
        }

        File   targetFile        = ensureTargetFile(targetFolder, templateFile, copyPermissions);
        String targetFileContent = getFileContent(templateFile, targetCharset);

        if (null != sourceFilesConfiguration) {
            for (SourceFile sourceFile : sourceFilesConfiguration) {
                targetFileContent = mergeSourceFile(sourceFile,
                    targetFileContent,
                    indentation
                );
            }
        }

        log.info("Writing target file...");

        if (filtering) {
            targetFileContent = filterContent(targetFileContent);
        }

        Files.write(
            targetFile.toPath(),
            targetFileContent.getBytes(targetCharset)
        );
    }

    private void mergeTargetFile(
        TargetFile   targetFileConfiguration,
        Properties   properties,
        SourceFile[] sourceFilesConfiguration
    )
        throws IOException, MavenFilteringException, MojoExecutionException
    {
        String[] templateFilePatterns = targetFileConfiguration.getTemplateFilePatterns();

        log.info("Merging template file(s)...");

        targetFileConfiguration.validate();

        boolean    copyPermissions       = targetFileConfiguration.isCopyPermissions();
        String     indentation           = getIndentation(targetFileConfiguration);
        Properties propertiesBackup      = setProperties(properties, false);
        Charset    targetCharset         = getCharset(targetFileConfiguration);
        String     targetFolderName      = targetFileConfiguration.getTargetFolder();
        File       targetFolder          = new File(targetFolderName);
        String[]   resolvedTemplateFiles = scanForFiles(templateFilePatterns);

        if ((null == templateFilePatterns) || (0 == templateFilePatterns.length)) {
            log.warn("No template files found.");
        } else {
            boolean filtering = targetFileConfiguration.isFiltering();

            for (int index = 0; index < resolvedTemplateFiles.length; index++) {
                mergeTargetFile(
                    targetFolder,
                    resolvedTemplateFiles[index],
                    sourceFilesConfiguration,
                    targetCharset,
                    indentation,
                    filtering,
                    copyPermissions
                );
            }
        }

        setProperties(propertiesBackup, true);
    }

    private String[] scanForFiles(String[] filePatterns) {
        DirectoryScanner directoryScanner = new DirectoryScanner();

        directoryScanner.setBasedir(".");
        directoryScanner.setCaseSensitive(true);
        directoryScanner.setFollowSymlinks(false);
        directoryScanner.setIncludes(filePatterns);

        directoryScanner.scan();

        return directoryScanner.getIncludedFiles();
    }

    private Properties setProperties(Properties newProperties, boolean replace) {
        Properties currentProperties = mavenProject.getProperties();
        Properties backupProperties  = (Properties)currentProperties.clone();

        if (null != newProperties) {
            if (replace) {
                currentProperties.clear();
            }

            currentProperties.putAll(newProperties);
        }

        return backupProperties;
    }

    @Override
    public void execute()
        throws MojoExecutionException
    {
        if (skip) {
            log.warn("Execution skipped.");
            return;
        }

        log.info("Merging...");

        for (TargetFile targetFile : targetFiles) {
            try {
                Properties properties    = null;
                String     propertiesSet = targetFile.getPropertiesSet();

                if ((null != propertiesSet) && !propertiesSet.isEmpty()) {
                    properties = getPropertiesConfiguration(propertiesSet);
                }

                SourceFile[] sourceFiles    = null;
                String       sourceFilesSet = targetFile.getSourceFilesSet();

                if ((null != sourceFilesSet) && !sourceFilesSet.isEmpty()) {
                    sourceFiles = getSourceFilesConfiguration(sourceFilesSet);
                }

                mergeTargetFile(targetFile, properties, sourceFiles);
            } catch(IOException | MavenFilteringException exception) {
                throw new MojoExecutionException(exception);
            }
        }

        log.info("Done.");
    }

}
