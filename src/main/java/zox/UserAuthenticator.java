package zox;


import java.util.Optional;

import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentials;

public class UserAuthenticator implements Authenticator<BasicCredentials, Person> {
	 @Override
	    public Optional<Person> authenticate(BasicCredentials credentials) throws AuthenticationException {
	        if ("secret".equals(credentials.getPassword())) {
	            return Optional.of(new Person(credentials.getUsername()));
	        }
	        return Optional.empty();
	    }
}
