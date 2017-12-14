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

@Path("/pay/")
public class PayResource {

	private EntityManager entityManager;
	private Logger log = Logger.getLogger(HBCI.class);

	public PayResource(EntityManager em) {
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
	public PayView index() {

		return new PayView();

	}

}
