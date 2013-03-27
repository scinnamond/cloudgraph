package org.cloudgraph.web.jaas;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.web.sdo.personalization.User;
import org.cloudgraph.web.sdo.personalization.UserRole;
import org.cloudgraph.web.sdo.personalization.query.QUser;
import org.plasma.sdo.access.client.SDODataAccessClient;

import commonj.sdo.DataGraph;

public class TomcatLoginModule implements LoginModule {

	private CallbackHandler handler;
	private Subject subject;
	private UserPrincipal userPrincipal;
	private RolePrincipal rolePrincipal;
	private String login;
	private List<String> userGroups;
	private static Log log =LogFactory.getLog(
			TomcatLoginModule.class);

	@Override
	public void initialize(Subject subject, CallbackHandler callbackHandler,
			Map<String, ?> sharedState, Map<String, ?> options) {

		handler = callbackHandler;
		this.subject = subject;
	}

	@Override
	public boolean login() throws LoginException {

		Callback[] callbacks = new Callback[2];
		callbacks[0] = new NameCallback("login");
		callbacks[1] = new PasswordCallback("password", true);

		try {
			handler.handle(callbacks);
			String name = ((NameCallback) callbacks[0]).getName();
			String password = String.valueOf(((PasswordCallback) callbacks[1])
					.getPassword());

			User user = findUser(name, password);
			if (user != null) {
				userGroups = new ArrayList<String>();
				for (UserRole userRole : user.getUserRole()) {
					userGroups.add(userRole.getRole().getName());
				}
				if (userGroups.size() == 0)
					throw new LoginException("Authentication failed - user '"
							+ name + "' has no roles");
				login = name;
				return true;
			}
			else
			    throw new LoginException("Authentication failed - invalid username and/or password");

		} catch (IOException e) {
			throw new LoginException(e.getMessage());
		} catch (UnsupportedCallbackException e) {
			throw new LoginException(e.getMessage());
		}
	}
	
	private User findUser(String username, String password) {
		QUser root = QUser.newQuery();
		root.select(root.wildcard())
		    .select(root.userRole().wildcard())
		    .select(root.userRole().role().wildcard());
		root.where(root.username().eq(username)
			.and(root.password().eq(password)));
	    SDODataAccessClient service = new SDODataAccessClient();
	    DataGraph[] results = service.find(root);
	    if (results == null || results.length == 0)
	    	return null;
		User user = (User)results[0].getRootObject();
		String dump = user.dump();
		
		return user;
	}

	@Override
	public boolean commit() throws LoginException {

		userPrincipal = new UserPrincipal(login);
		subject.getPrincipals().add(userPrincipal);

		if (userGroups != null && userGroups.size() > 0) {
			for (String groupName : userGroups) {
				rolePrincipal = new RolePrincipal(groupName);
				subject.getPrincipals().add(rolePrincipal);
			}
		}

		return true;
	}

	@Override
	public boolean abort() throws LoginException {
		return false;
	}

	@Override
	public boolean logout() throws LoginException {
		subject.getPrincipals().remove(userPrincipal);
		subject.getPrincipals().remove(rolePrincipal);
		return true;
	}

}
