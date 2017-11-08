package zox;

import javax.annotation.security.DenyAll;
import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import com.codahale.metrics.annotation.Timed;
import com.scottescue.dropwizard.entitymanager.UnitOfWork;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;

@Path("/auth/{id:([0-9a-zA-Z]*)}")
@Produces(MediaType.TEXT_HTML)
@DenyAll
public class AuthResource {

	private EntityManager entityManager;
	private Logger log = Logger.getLogger(HBCI.class);
	GoogleAuthenticator gAuth = null;
	GoogleAuthenticatorKey key = null;

	public AuthResource(EntityManager em) {
		this.entityManager = em;
	}

	@GET
	@Timed
	@RolesAllowed("ADMIN")
	@UnitOfWork(transactional = true)
	public AuthView sayHello(@PathParam("id") String id) {

		if (id.equals("")) {
			gAuth = new GoogleAuthenticator();
			key = gAuth.createCredentials();

			return new AuthView("<img src='"+key.getQRBarcodeURL("Jochen", "usegroup", key.getKey())+"'>");
		} else {
			int idi=Integer.parseInt(id);
			String  res="not valid";
			if (gAuth.authorize(key.getKey(), idi)) {
				res="<strong>valid</strong>";	
			}
			return new AuthView("test is "+res);
		}

	}

}
