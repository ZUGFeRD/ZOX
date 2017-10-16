package zox;

import com.codahale.metrics.annotation.Timed;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.concurrent.atomic.AtomicLong;
import java.util.List;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.kapott.hbci.GV.HBCIJob;
import org.kapott.hbci.GV_Result.GVRKUms;
import org.kapott.hbci.GV_Result.GVRKUms.UmsLine;

import org.kapott.hbci.structures.Konto;




@Path("/hello-world")
@Produces(MediaType.APPLICATION_JSON)
public class HelloWorldResource {
    private final String template;
    private final String defaultName;
    private final AtomicLong counter;
    private ISupplierDAO dao;
	private Logger log=Logger.getLogger(HBCI.class);
    HBCI hbci =null;
    Konto bankAccount;

    public HelloWorldResource(String template, String defaultName,
    		String bank_code, String bank_account, String bank_user,
			String bank_rdhfile, String bank_rdhpassphrase, String bank_url,
    		ISupplierDAO dao) {
        this.template = template;
        this.defaultName = defaultName;
        this.counter = new AtomicLong();
        this.dao=dao;
        hbci = HBCI.getInstance( bank_code,  bank_account,  bank_user,
    			 bank_rdhfile,  bank_rdhpassphrase, bank_url);
		bankAccount = new Konto(bank_code, bank_account);

    }

    @GET
    @Timed
    public Saying sayHello(@QueryParam("name") Optional<String> name) {
        final String value = String.format(template, name.orElse(defaultName));
        log.info("dao insert");
        
        dao.insert("Schalalala");
        log.info("hbci4java");

		HBCIJob job = hbci.newJob("KUmsAll"); // nächster Auftrag ist Saldenabfrage //$NON-NLS-1$
		job.setParam("my", bankAccount); // Kontonummer für Saldenabfrage //$NON-NLS-1$
		job.setParam("startdate", "2017-10-01"); //$NON-NLS-1$
		job.setParam("enddate", "2017-10-15"); //$NON-NLS-1$

		job.addToQueue();

		hbci.execute("Abholen von Kontoinformationen"); //$NON-NLS-1$
		GVRKUms result = (GVRKUms) job.getJobResult();
		if (result.isOK()) {
			List<UmsLine> turnover = result.getFlatData();
			int i = 0;
			for (UmsLine umsLine : turnover) {
				String purpose = ""; //$NON-NLS-1$
				for (Object purposeLine : umsLine.usage) {
					purpose += "@" + purposeLine.toString(); //$NON-NLS-1$
				}

				if (umsLine.other.name2 == null) {
					// this can be null when the transaction comes from the bank
					// institute, e.g. annual interest rates
					umsLine.other.name2 = ""; //$NON-NLS-1$
				}

				log.info("purpose="+purpose+";name="+ 
						umsLine.other.name+";name2="+
						umsLine.other.name2+";othernumber="+
						umsLine.other.number+";blz="+
						umsLine.other.blz+";valuta="+
						umsLine.valuta+";value="+
						umsLine.value.getLongValue());
				i++;
			}

		}


        
        return new Saying(counter.incrementAndGet(), value);
        
    }
}