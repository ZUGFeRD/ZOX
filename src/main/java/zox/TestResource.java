package zox;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;

import javax.annotation.security.PermitAll;
import javax.persistence.EntityManager;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import com.codahale.metrics.annotation.Timed;
import com.scottescue.dropwizard.entitymanager.UnitOfWork;

@PermitAll

@Path("/test/")
public class TestResource {

	private EntityManager entityManager;
	private Logger log = Logger.getLogger(HBCI.class);

	public TestResource(EntityManager em) {
		this.entityManager = em;
	}

	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Path("UploadFile")
	public Response uploadFile(@FormDataParam("file") final InputStream fileInputStream,
			@FormDataParam("file") final FormDataContentDisposition contentDispositionHeader) {

		String filePath = "./uploads/"+contentDispositionHeader.getFileName();
		saveFile(fileInputStream, filePath);
		
		String output = "File can be downloaded from the following location : " + filePath;

		return Response.status(200).entity(output).build();

	}



	private void saveFile(InputStream uploadedInputStream, String serverLocation) {
		java.nio.file.Path outputPath = FileSystems.getDefault().getPath(serverLocation);
		try {
			Files.copy(uploadedInputStream, outputPath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@GET
	@Timed
	@PermitAll

	@Path("/")
	@Produces(MediaType.TEXT_HTML)
	@UnitOfWork(transactional = true)
	public AuthView sayHello(@PathParam("id") String id) {

		PersonService ps = new PersonService(entityManager);
		/*
		 * FileRepositoryBuilder builder = new FileRepositoryBuilder(); try { Repository
		 * repository = builder.setGitDir(new File("/Users/jstaerk/workspace/ZUV/.git"))
		 * .readEnvironment() // scan environment GIT_* variables .findGitDir() // scan
		 * up the file system tree .build(); Ref HEAD =
		 * repository.findRef("refs/heads/master"); ObjectId head =
		 * repository.resolve("HEAD"); Git git = new Git(repository); PullCommand
		 * pc=git.pull(); pc.call(); AddCommand ac=git.add();
		 * ac.addFilepattern("test.txt"); ac.call();
		 * 
		 * CommitCommand cc=git.commit(); cc.setMessage("Test"); cc.call(); PushCommand
		 * puc=git.push(); puc.call();
		 * 
		 * return new AuthView("repo OK:"+repository.getFullBranch()); } catch
		 * (IOException e) { // TODO Auto-generated catch block e.printStackTrace(); }
		 * catch (WrongRepositoryStateException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); } catch (InvalidConfigurationException e) { // TODO
		 * Auto-generated catch block e.printStackTrace(); } catch
		 * (InvalidRemoteException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); } catch (CanceledException e) { // TODO Auto-generated
		 * catch block e.printStackTrace(); } catch (RefNotFoundException e) { // TODO
		 * Auto-generated catch block e.printStackTrace(); } catch
		 * (RefNotAdvertisedException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); } catch (NoHeadException e) { // TODO Auto-generated
		 * catch block e.printStackTrace(); } catch (TransportException e) { // TODO
		 * Auto-generated catch block e.printStackTrace(); } catch (GitAPIException e) {
		 * // TODO Auto-generated catch block e.printStackTrace(); }
		 */
		return new AuthView("repo fail");

	}

}
