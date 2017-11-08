package zox;

import io.dropwizard.auth.Authorizer;

public class UserAuthorizer implements Authorizer<Person> {
	    @Override
	    public boolean authorize(Person user, String role) {
	        return user.getName().equals("good-guy") && role.equals("ADMIN");
	    }
	
}
