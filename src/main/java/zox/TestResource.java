package zox;

import java.io.File;
import java.io.IOException;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.errors.CanceledException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidConfigurationException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.RefNotAdvertisedException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import com.codahale.metrics.annotation.Timed;
import com.google.errorprone.annotations.NoAllocation;
import com.scottescue.dropwizard.entitymanager.UnitOfWork;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;

@Path("/test/")
@Produces(MediaType.TEXT_HTML)
@PermitAll
public class TestResource {

	private EntityManager entityManager;
	private Logger log = Logger.getLogger(HBCI.class);

	public TestResource(EntityManager em) {
		this.entityManager = em;
	}

	@GET
	@Timed
	@PermitAll
	@UnitOfWork(transactional = true)
	public AuthView sayHello(@PathParam("id") String id) {
		
		PersonService ps=new PersonService(entityManager);
		ps.dbInitialEntries();
		System.err.println("test");
		/*
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		try {
			Repository repository = builder.setGitDir(new File("/Users/jstaerk/workspace/ZUV/.git"))
			  .readEnvironment() // scan environment GIT_* variables
			  .findGitDir() // scan up the file system tree
			  .build();
			Ref HEAD = repository.findRef("refs/heads/master");
			ObjectId head = repository.resolve("HEAD");
			Git git = new Git(repository);
			PullCommand pc=git.pull();
			pc.call();
			AddCommand ac=git.add();
			ac.addFilepattern("test.txt");
			ac.call();

			CommitCommand cc=git.commit();
			cc.setMessage("Test");
			cc.call();
			PushCommand puc=git.push();
			puc.call();

			return new AuthView("repo OK:"+repository.getFullBranch());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WrongRepositoryStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidRemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CanceledException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RefNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RefNotAdvertisedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoHeadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransportException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		return new AuthView("repo fail");

	}

}
