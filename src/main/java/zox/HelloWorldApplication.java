package zox;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.function.IntSupplier;

import javax.persistence.EntityManager;

import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

import com.scottescue.dropwizard.entitymanager.EntityManagerBundle;
import com.scottescue.dropwizard.entitymanager.ScanningEntityManagerBundle;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.db.DataSourceFactory;
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
	
	private final EntityManagerBundle<HelloWorldConfiguration> entityManagerBundle = 
	        new ScanningEntityManagerBundle<HelloWorldConfiguration>("zox") { /*zox is the package to be scanned for JPA annotations*/
	    @Override
	    public DataSourceFactory getDataSourceFactory(HelloWorldConfiguration configuration) {
	        return configuration.getDataSourceFactory();
	    }
	};
	
	@Override
	public void initialize(Bootstrap<HelloWorldConfiguration> bootstrap) {
		bootstrap.addBundle(new AssetsBundle("/assets/", "/"));
		bootstrap.addBundle(new ViewBundle<HelloWorldConfiguration>());
		bootstrap.addBundle(entityManagerBundle);
	}

	@Override
	public void run(HelloWorldConfiguration configuration, Environment environment) {
		
		
		final DBIFactory factory = new DBIFactory();
	    final DBI jdbi = factory.build(environment, configuration.getDataSourceFactory(), "derby");
	    
	    boolean tableExists=false;
	    final ISupplierDAO dao = jdbi.onDemand(ISupplierDAO.class);
	    try (Handle h = jdbi.open()) {
	    	  try (Connection c = h.getConnection()) {
	    	    try (ResultSet tables = c.getMetaData().getColumns(null, "%", "%", "%")) {
	    	      // loop over columns/tables here...
	    	    	ResultSetMetaData rsmd = tables.getMetaData();
	    	    	 int columnsNumber = rsmd.getColumnCount();
	    	    	System.err.println("***********************************");
	    	    	while (tables.next()) {
/*	    	    		 for (int i = 1; i <= columnsNumber; i++) {
	    	    	           if (i > 1) System.out.print(",  ");
	    	    	           String columnValue = tables.getString(i);
	    	    	           System.out.print(i+":"+columnValue + " " + rsmd.getColumnName(i));
	    	    	       }*/

	    	 	    	    	       System.out.println(tables.getString(3));
	    	 	    	    	       if (tables.getString(3).equals("SOMETHING")) {
	    	 	    	    	    	   tableExists=true;
	    	 	    	    	       }
	    	    	     }
	    	    	if (!tableExists) {
	    	    		dao.createSomethingTable();
	    	            
	    	    	}
	    	    	System.err.println("***********************************");
	    	    	
	    	    }
	    	  } catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	}
		
		 final EntityManager entityManager = entityManagerBundle.getSharedEntityManager();
			final HelloWorldResource resource = new HelloWorldResource(configuration.getTemplate(),
					configuration.getDefaultName(),  configuration.getBank_code(), configuration.getBank_account(), configuration.getBank_user(),
					configuration.getBank_rdhfile(), configuration.getBank_rdhpassphrase(), configuration.getBank_url(),
					dao, entityManager);
			environment.jersey().register(resource);
			final TestResource testr = new TestResource(entityManager);
			environment.jersey().register(testr);
			final AuthResource authr = new AuthResource(entityManager);
			environment.jersey().register(authr);
			environment.jersey().register(new AuthDynamicFeature(
		            new BasicCredentialAuthFilter.Builder<Person>()
		                .setAuthenticator(new UserAuthenticator())
		                .setAuthorizer(new UserAuthorizer())
		                .setRealm("Corridors Administration")
		                .buildAuthFilter()));
		    environment.jersey().register(RolesAllowedDynamicFeature.class);
		    //If you want to use @Auth to inject a custom Principal type into your resource
		    environment.jersey().register(new AuthValueFactoryProvider.Binder<>(Person.class));
		    
		System.err.println("deb starting sender with domain " + configuration.getDomain() + " username "
				+ configuration.getUsername());
		RecipientComThread ct = new RecipientComThread();
		ct.setDomain(configuration.getDomain()).setUsername(configuration.getUsername())
				.setPassword(configuration.getPassword());

		ct.start();

	}

}
