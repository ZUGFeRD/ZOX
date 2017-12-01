package zox;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.log4j.Logger;

import com.codahale.metrics.annotation.Timed;
import com.scottescue.dropwizard.entitymanager.EntityManagerBundle;
import com.scottescue.dropwizard.entitymanager.UnitOfWork;

public class PersonService {

	private Logger log = Logger.getLogger(PersonService.class);
	protected EntityManager em;

	public PersonService(EntityManager entityManager) {
		em = entityManager;
	}

	public PersonService(EntityManagerBundle eb) {
		em = eb.getSharedEntityManager();
	}

	/***
	 * seed the DB
	 */
	@Timed
	@UnitOfWork(transactional = true)
	public void ensureInitialDBEntries() {
		String queryString = "SELECT p FROM Person p ";
		Query query = em.createQuery(queryString);

		if (query.getResultList().isEmpty()) {

			log.info("Creating user");
			Person user = new Person();
			user.setName("jstaerk");
			user.setPassword("test");
			em.persist(user);
			em.flush();
			queryString = "SELECT p FROM Person p ";
			query = em.createQuery(queryString);
			if (query.getResultList().isEmpty()) {
			  System.err.println("Should not happen");	
			}

		} else {
			log.info("User exists");

		}
	}

	@Timed
	@UnitOfWork(transactional = true)
	public Person getPersonByName(String name) {
		String queryString = "SELECT p FROM Person p " + "WHERE LOWER(p.name) = LOWER(:username)";
		Query query = em.createQuery(queryString);

		query.setParameter("username", name);
		if (query.getResultList().size() > 0) {
			Person res = (Person) query.getResultList().get(0);
			return res;
		} else {
			return null;
		}
	}

}
