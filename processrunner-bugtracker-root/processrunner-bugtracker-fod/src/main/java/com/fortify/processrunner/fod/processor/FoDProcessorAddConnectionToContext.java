package com.fortify.processrunner.fod.processor;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fortify.fod.connection.FoDConnectionFactoryClientCredentials;
import com.fortify.fod.connection.FoDConnectionFactoryUserCredentials;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextProperty;
import com.fortify.processrunner.fod.context.IContextFoD;
import com.fortify.processrunner.processor.AbstractProcessor;
import com.fortify.util.rest.IRestConnectionFactory;

// TODO Add constants for constant strings (FoDTenant, FoDUserName, ...)
// TODO Make FoD URL configurable via context
// TODO Clean up the code for setting credentials from context/console
public class FoDProcessorAddConnectionToContext extends AbstractProcessor {
	private static final Log LOG = LogFactory.getLog(FoDProcessorAddConnectionToContext.class);
	private IRestConnectionFactory connectionFactory;
	
	public FoDProcessorAddConnectionToContext() {}
	
	public FoDProcessorAddConnectionToContext(IRestConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}
	
	@Override
	public List<ContextProperty> getContextProperties(Context context) {
		if ( connectionFactory != null ) {
			if ( connectionFactory instanceof FoDConnectionFactoryUserCredentials ) {
				return getContextPropertiesForUserCredentials(context);
			} else if ( connectionFactory instanceof FoDConnectionFactoryClientCredentials ) {
				return getContextPropertiesForClientCredentials(context);
			}
		}
		return super.getContextProperties(context);
	}
	
	private List<ContextProperty> getContextPropertiesForUserCredentials(Context context) {
		List<ContextProperty> result = new ArrayList<ContextProperty>();
		result.add(new ContextProperty("FoDTenant", "FoD tenant", false));
		result.add(new ContextProperty("FoDUserName", "FoD user name", false));
		result.add(new ContextProperty("FoDPassword", "FoD password", false));
		return result;
	}

	private List<ContextProperty> getContextPropertiesForClientCredentials(Context context) {
		//FoDConnectionFactoryClientCredentials cf = (FoDConnectionFactoryClientCredentials)getConnectionFactory();
		List<ContextProperty> result = new ArrayList<ContextProperty>();
		result.add(new ContextProperty("FoDApiKey", "FoD API Key", false));
		result.add(new ContextProperty("FoDClientSecret", "FoD client secret", false));
		return result;
	}
	
	private static final void addCredentialsFromContext(Context context, IRestConnectionFactory cf) {
		
		if ( cf != null ) {
			if ( cf instanceof FoDConnectionFactoryUserCredentials ) {
				addUserCredentials(context, (FoDConnectionFactoryUserCredentials)cf);
			} else if ( cf instanceof FoDConnectionFactoryClientCredentials ) {
				addClientCredentials(context, (FoDConnectionFactoryClientCredentials)cf);
			}
		}
	}

	// TODO Add IContextFoDUserCredentials interface instead of accessing map keys
	// TODO Simplify this method?
	private static final void addUserCredentials(Context context, FoDConnectionFactoryUserCredentials cf) {
		String tenant = (String)context.get("FoDTenant");
		String userName = (String)context.get("FoDUserName");
		String password = (String)context.get("FoDPassword");
		
		if ( !StringUtils.isBlank(tenant) ) {
			cf.setTenant(tenant);
		}
		if ( !StringUtils.isBlank(userName) ) {
			cf.setUserName(userName);
		}
		if ( !StringUtils.isBlank(password) ) {
			cf.setPassword(password);
		}
		
		if ( cf.getTenant()==null ) {
			cf.setTenant(System.console().readLine("FoD Tenant: "));
		}
		if ( cf.getUserName()==null ) {
			cf.setUserName(System.console().readLine("FoD User Name: "));
		}
		if ( cf.getPassword()==null ) {
			cf.setPassword(new String(System.console().readPassword("FoD Password: ")));
		}
		
	}
	
	// TODO Add IContextFoDClientCredentials interface instead of accessing map keys
	// TODO Simplify this method?
	private static final void addClientCredentials(Context context, FoDConnectionFactoryClientCredentials cf) {
		String apiKey = (String)context.get("FoDApiKey");
		String clientSecret = (String)context.get("FoDClientSecret");
		
		if ( !StringUtils.isBlank(apiKey) ) {
			cf.setClientId(apiKey);
		}
		if ( !StringUtils.isBlank(clientSecret) ) {
			cf.setClientSecret(clientSecret);
		}
		if ( cf.getClientId()==null ) {
			cf.setClientId(System.console().readLine("FoD API Key"));
		}
		if ( cf.getClientSecret()==null ) {
			cf.setClientSecret(new String(System.console().readPassword("FoD Client Secret")));
		}
	}

	@Override
	protected boolean preProcess(Context context) {
		IRestConnectionFactory cf = getConnectionFactory();
		LOG.info("Adding connection to context");
		addCredentialsFromContext(context, cf);
		context.as(IContextFoD.class).setFoDConnection(
				cf.getConnection());
		return true;
	}

	public IRestConnectionFactory getConnectionFactory() {
		return connectionFactory;
	}

	public void setConnectionFactory(IRestConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}
}
