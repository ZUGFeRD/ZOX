package zox;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.codahale.metrics.annotation.Timed;
import com.scottescue.dropwizard.entitymanager.EntityManagerBundle;
import com.scottescue.dropwizard.entitymanager.UnitOfWork;

public class PersonService {

	protected EntityManager em;
	public PersonService(EntityManager entityManager) {
		em=entityManager;
	}
	public PersonService(EntityManagerBundle eb) {
		em=eb.getSharedEntityManager();
	}

	@Timed
    @UnitOfWork(transactional = true)
	public void dbInitialEntries() {
		Person user=new Person();
		user.setName("jstaerk");
		user.setPassword("test");
		em.persist(user);
	}
	
	
	@Timed
    @UnitOfWork(transactional = true)
	public Person getPersonByName(String name) {
	    String queryString = "SELECT p FROM Person p " +
	                         "WHERE LOWER(p.name) = LOWER(:username)";
	    Query query = em.createQuery(queryString);
	    
	    query.setParameter("username", name);
	    Person res =  (Person)query.getResultList().get(0);
	    return res;
	}
	
}
