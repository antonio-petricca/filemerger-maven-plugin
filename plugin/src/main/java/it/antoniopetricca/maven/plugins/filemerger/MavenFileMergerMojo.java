package it.antoniopetricca.maven.plugins.filemerger;

// https://www.baeldung.com/maven-plugin

import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.util.List;

@Mojo(
    defaultPhase = LifecyclePhase.PROCESS_RESOURCES,
    name         = "file-merger"

)
public class MavenFileMergerMojo extends AbstractMojo {

    private final  Log log = this.getLog();

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    @Override
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if (log.isDebugEnabled()) {
            log.debug("Executing...");
        }

        List<Dependency> dependencies    = project.getDependencies();
        long             numDependencies = dependencies.size();

        getLog().info("Number of dependencies: " + numDependencies);
    }

}
