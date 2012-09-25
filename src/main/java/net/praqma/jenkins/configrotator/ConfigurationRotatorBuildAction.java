package net.praqma.jenkins.configrotator;

import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.scm.SCM;
import java.io.IOException;
import javax.servlet.ServletException;
import net.praqma.jenkins.configrotator.ConfigurationRotator.ResultType;
import net.praqma.jenkins.configrotator.scm.ConfigRotatorChangeLogSet;
import net.praqma.jenkins.configrotator.scm.clearcaseucm.ClearCaseUCM;
import net.praqma.jenkins.configrotator.scm.clearcaseucm.ClearCaseUCMConfigRotatorChangeLogSet;
import net.praqma.jenkins.configrotator.scm.clearcaseucm.ClearCaseUCMConfiguration;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

public class ConfigurationRotatorBuildAction implements Action {
	
    private String description;
	private AbstractBuild<?, ?> build;
	private Class<? extends AbstractConfigurationRotatorSCM> clazz;
	private ResultType result = ResultType.UNDETERMINED;
	private AbstractConfiguration configuration;
	
	public ConfigurationRotatorBuildAction( AbstractBuild<?, ?> build, Class<? extends AbstractConfigurationRotatorSCM> clazz, AbstractConfiguration configuration ) {
		this.build = build;
		this.clazz = clazz;
		this.configuration = configuration;
	}
	
	public Class<?> getClazz() {
		return clazz;
	}

	
	
	public void doReset( StaplerRequest req, StaplerResponse rsp ) throws IOException, ServletException {
		SCM scm = build.getProject().getScm();
		if( scm instanceof ConfigurationRotator ) {
			//((ConfigurationRotator)scm).setFresh( build.getProject(), true );
			((ConfigurationRotator)scm).setConfigurationByAction( build.getProject(), this );
			//rsp.forwardToPreviousPage( req. );
			rsp.sendRedirect( "../../" );
		} else {
			rsp.sendError( StaplerResponse.SC_BAD_REQUEST, "Not a Configuration Rotator job" );
		}
	}
	
	public void setResult( ResultType result ) {
		this.result = result;
	}
	
	public boolean isDetermined() {
		return result.equals( ResultType.COMPATIBLE ) || result.equals( ResultType.INCOMPATIBLE );
	}
	
	public boolean isCompatible() {
		return result.equals( ResultType.COMPATIBLE );
	}
	
	public ResultType getResult() {
		return result;
	}

	@Override
	public String getIconFileName() {
		return null;
	}

	@Override
	public String getDisplayName() {
		return null;
	}

	@Override
	public String getUrlName() {
		return "config-rotator";
	}

	public AbstractBuild<?, ?> getBuild() {
		return build;
	}
	
	public AbstractConfiguration<AbstractConfigurationComponent> getConfiguration() {
		return configuration;
	}
    
    public <T extends AbstractConfiguration> T getConfiguration(Class<T> clazz) {
        return (T)configuration;
    }

    public AbstractConfiguration getConfigurationWithOutCast() {
        return configuration;
    }
    
	@Override
	public String toString() {
		return "Build action: " + configuration;
	}

    /**
     * @return the description
     */
    public String getDescription() {
        /**
         * Ensure backwards compatability
         */
        if(description == null) {
            ClearCaseUCMConfiguration current = this.getConfiguration(ClearCaseUCMConfiguration.class);
        
            if(current != null) {
                ConfigurationRotator rotator = (ConfigurationRotator)this.getBuild().getProject().getScm();
                if(current.getChangedComponent() == null) {
                    return "New Configuration - no changes yet";
                } else {
                    int currentComponentIndex = current.getChangedComponentIndex();
                    String currentBaseline = current.getChangedComponent().getBaseline().getNormalizedName();
                    ConfigurationRotatorBuildAction previous = rotator.getAcrs().getLastResult(this.getBuild().getProject(), ClearCaseUCM.class);
                    String previousBaseline = previous.getConfiguration(ClearCaseUCMConfiguration.class).getList().get(currentComponentIndex).getBaseline().getNormalizedName();

                    return String.format("Baseline changed from %s to %s", previousBaseline, currentBaseline);
                }
            }    
        }  
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }
}
