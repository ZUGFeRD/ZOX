package zox;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Optional;
import java.util.function.IntSupplier;

import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.QueryParam;

import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.hibernate.SessionEventListener;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

import com.codahale.metrics.annotation.Timed;
import com.scottescue.dropwizard.entitymanager.EntityManagerBundle;
import com.scottescue.dropwizard.entitymanager.ScanningEntityManagerBundle;
import com.scottescue.dropwizard.entitymanager.UnitOfWork;
import com.scottescue.dropwizard.entitymanager.UnitOfWorkAwareProxyFactory;

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

public class CorridorsApplication extends Application<CorridorsConfiguration> {
	public static void main(String[] args) throws Exception {
		new CorridorsApplication().run(args);
	}

	@Override
	public String getName() {
		return "hello-world";
	}

	private EntityManagerBundle<CorridorsConfiguration> entityManagerBundle;

	@Override
	public void initialize(Bootstrap<CorridorsConfiguration> bootstrap) {
		bootstrap.addBundle(new AssetsBundle("/assets/", "/"));
		bootstrap.addBundle(new ViewBundle<CorridorsConfiguration>());
		 
		 entityManagerBundle= new ScanningEntityManagerBundle<CorridorsConfiguration>(
					"zox") { /* zox is the package to be scanned for JPA annotations */
				@Override
				public DataSourceFactory getDataSourceFactory(CorridorsConfiguration configuration) {
					return configuration.getDataSourceFactory();
				}
			};
		bootstrap.addBundle(entityManagerBundle);
	}

	@Override
	public void run(CorridorsConfiguration configuration, Environment environment) {

		final DBIFactory factory = new DBIFactory();
		final DBI jdbi = factory.build(environment, configuration.getDataSourceFactory(), "derby");

		final ISupplierDAO dao = jdbi.onDemand(ISupplierDAO.class);
					
		final EntityManager entityManager = entityManagerBundle.getSharedEntityManager();

		final HelloWorldResource resource = new HelloWorldResource(configuration.getTemplate(),
				configuration.getDefaultName(), configuration.getBank_code(), configuration.getBank_account(),
				configuration.getBank_user(), configuration.getBank_rdhfile(), configuration.getBank_rdhpassphrase(),
				configuration.getBank_url(), dao, entityManager);
		environment.jersey().register(resource);
		final PayResource testr = new PayResource(entityManager);
		environment.jersey().register(testr);
		 
			UnitOfWorkAwareProxyFactory proxyFactory = new UnitOfWorkAwareProxyFactory(entityManagerBundle);

			PersonService personService = proxyFactory.create(
			        PersonService.class, 
			        EntityManager.class, 
			        entityManager);
			personService.ensureInitialDBEntries();

			final AuthResource authr = new AuthResource(entityManager);
			environment.jersey().register(authr);
			environment.jersey()
					.register(new AuthDynamicFeature(new BasicCredentialAuthFilter.Builder<Person>()
							.setAuthenticator(new UserAuthenticator(personService)).setAuthorizer(new UserAuthorizer())
							.setRealm("Corridors Administration").buildAuthFilter()));
			environment.jersey().register(RolesAllowedDynamicFeature.class);
			// If you want to use @Auth to inject a custom Principal type into your resource
			environment.jersey().register(new AuthValueFactoryProvider.Binder<>(Person.class));
			
			 environment.jersey().register(MultiPartFeature.class);
			
			// other init tasks
			 
			 if (!new File("./uploads").exists()) {
				 (new File("./uploads")).mkdirs();
			 }

		RecipientComThread ct = new RecipientComThread();
		ct.setDomain(configuration.getDomain()).setUsername(configuration.getUsername())
				.setPassword(configuration.getPassword());

		ct.start();

	}

}
