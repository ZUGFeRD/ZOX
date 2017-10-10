package zox;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;

public class HelloWorldApplication extends Application<HelloWorldConfiguration> {
    public static void main(String[] args) throws Exception {
        new HelloWorldApplication().run(args);
    }

    @Override
    public String getName() {
        return "hello-world";
    }

    @Override
    public void initialize(Bootstrap<HelloWorldConfiguration> bootstrap) {
    		bootstrap.addBundle(new AssetsBundle("/assets/", "/"));
    		bootstrap.addBundle(new ViewBundle<HelloWorldConfiguration>());
    }

    @Override
    public void run(HelloWorldConfiguration configuration,
                    Environment environment) {
    	  final HelloWorldResource resource = new HelloWorldResource(
    		        configuration.getTemplate(),
    		        configuration.getDefaultName()
    		    );
    		    environment.jersey().register(resource);
    		    
    	  final PersonResource per=new PersonResource();
		    environment.jersey().register(per);
		    
	    	System.err.println("deb starting sender with domain "+configuration.getDomain()+" username "+configuration.getUsername());
	        RecipientComThread ct = new RecipientComThread();
			ct.setDomain(configuration.getDomain()).setUsername(configuration.getUsername()).setPassword(configuration.getPassword());
			
			ct.run();

    }

}
