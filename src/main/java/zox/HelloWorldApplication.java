package zox;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.IntSupplier;

import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.jdbi.DBIFactory;
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
	public void run(HelloWorldConfiguration configuration, Environment environment) {
		
		
		final DBIFactory factory = new DBIFactory();
	    final DBI jdbi = factory.build(environment, configuration.getDataSourceFactory(), "derby");
	   /* try (Handle h = jdbi.open()) {
	    	  try (Connection c = h.getConnection()) {
	    	    try (ResultSet tables = c.getMetaData().getColumns(null, "%", "%", "%")) {
	    	      // loop over columns/tables here...
	    	    	do {

		    	    	System.err.println(tables.getString(0));
	    	    	}
	    	    	while (tables.next());
	    	    }
	    	  } catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	}*/
	    final ISupplierDAO dao = jdbi.onDemand(ISupplierDAO.class);
		
		final HelloWorldResource resource = new HelloWorldResource(configuration.getTemplate(),
				configuration.getDefaultName(), dao);
		environment.jersey().register(resource);


		System.err.println("deb starting sender with domain " + configuration.getDomain() + " username "
				+ configuration.getUsername());
		RecipientComThread ct = new RecipientComThread();
		ct.setDomain(configuration.getDomain()).setUsername(configuration.getUsername())
				.setPassword(configuration.getPassword());

		ct.run();

	}

}
