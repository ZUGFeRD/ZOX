package zox;


import java.util.Optional;

import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentials;

public class UserAuthenticator implements Authenticator<BasicCredentials, Person> {
	protected PersonService ps;
	public UserAuthenticator (PersonService ps) {
		this.ps=ps;
	}
	
	 @Override
	    public Optional<Person> authenticate(BasicCredentials credentials) throws AuthenticationException {
		 Person candidate=ps.getPersonByName(credentials.getUsername());
		 	if (candidate.getPassword().equals(credentials.getPassword())) {
		 		return Optional.of(candidate);
		 	}
		    return Optional.empty();
	    }
}
