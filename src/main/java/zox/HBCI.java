package zox;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.kapott.hbci.GV.HBCIJob;
import org.kapott.hbci.callback.HBCICallback;
import org.kapott.hbci.callback.HBCICallbackConsole;
import org.kapott.hbci.callback.HBCICallbackSwing;
import org.kapott.hbci.manager.HBCIHandler;
import org.kapott.hbci.manager.HBCIUtils;
import org.kapott.hbci.passport.AbstractHBCIPassport;
import org.kapott.hbci.passport.HBCIPassport;
import org.kapott.hbci.status.HBCIExecStatus;


public class HBCI {
	private static HBCI _instance;

	private HBCIPassport passport;
	private HBCIHandler hbciHandle;
	private Logger log=Logger.getLogger(HBCI.class);

	private String bank_rdhfile;

	 private final static Map<Integer, String> settings = new HashMap<Integer,String>()
	  {{
	    // Demo-Konto bei der ApoBank
	    put(HBCICallback.NEED_COUNTRY,         "DE");
	    put(HBCICallback.NEED_FILTER,          "Base64");
	    put(HBCICallback.NEED_PORT,            "443");
	    put(HBCICallback.NEED_PT_PIN,          "11111");
	    put(HBCICallback.NEED_PT_TAN,          "123456"); // hier geht jede 6-stellige Zahl
	    put(HBCICallback.NEED_PT_SECMECH,      "900"); // wird IMHO nicht benoetigt, weil es beim Demo-Account eh nur dieses eine Verfahren gibt
	    put(HBCICallback.NEED_CONNECTION,      ""); // ignorieren
	    put(HBCICallback.CLOSE_CONNECTION,     ""); // ignorieren
	  }};

	public static synchronized HBCI getInstance(String bank_code, String bank_account, String bank_user,
			String bank_rdhfile, String bank_rdhpassphrase, String bank_url) {
		if (_instance == null) {
			_instance = new HBCI(bank_code, bank_account, bank_user,
				 bank_rdhfile, bank_rdhpassphrase, bank_url);
		}
		return _instance;
	}

	private HBCI(String bank_code, String bank_account, String bank_user,
			String bank_rdhfile, String bank_rdhpassphrase, String bank_url) {
		try {
		    HBCICallback callback = new HBCICallbackConsole()
		    {
		      public void callback(HBCIPassport passport, int reason, String msg, int datatype, StringBuffer retData)
		      {
		        // haben wir einen vordefinierten Wert?
		        String value = settings.get(reason);
		        if (value != null)
		        {
		          retData.replace(0,retData.length(),value);
		          return;
		        }

		        // Ne, dann an Super-Klasse delegieren
		        super.callback(passport, reason, msg, datatype, retData);
		      }
		    };

		    Properties props = new Properties();
		    props.put("client.passport.default", "RDHNew"); //$NON-NLS-1$ //$NON-NLS-2$
		    props.put("client.passport.RDHNew.init", "1");
		    props.put("client.passport.RDHNew.filename", bank_rdhfile);
		    settings.put(HBCICallback.NEED_BLZ,             bank_code);
		    settings.put(HBCICallback.NEED_CUSTOMERID,      bank_account);
		    settings.put(HBCICallback.NEED_PASSPHRASE_LOAD, bank_rdhpassphrase);
		    settings.put(HBCICallback.NEED_PASSPHRASE_SAVE, bank_rdhpassphrase);
		    settings.put(HBCICallback.NEED_USERID,          bank_user);
		    settings.put(HBCICallback.NEED_HOST,            bank_url);
			 
		    this.bank_rdhfile=bank_rdhfile;

		   
			HBCIUtils.init(props, callback);
			HBCIUtils.setParam("log.loglevel.default", "3"); //$NON-NLS-1$ //$NON-NLS-2$
			HBCIUtils.setParam("log.filter", "2"); //$NON-NLS-1$ //$NON-NLS-2$
			// HBCIUtils.setParam("log.loglevel.default",Config.getInstance().get("hbci.loglevel","3"));
			// HBCIUtils.setParam("log.filter",Config.getInstance().get("hbci.filterlevel","2"));
		} catch (Exception e) {
			log.error(e);
		}
	}

	public synchronized void setParam(String key, String value) {
		HBCIUtils.setParam(key, value);
	}

	private synchronized void initPassport() {
		try {

			/*
			 * setParam("client.passport.default","PinTan");
			 * System.err.println(client.getDataPath()+File.separator+"pintan");
			 * setParam
			 * ("client.passport.PinTan.filename",client.getDataPath()+File
			 * .separator+"pintan");
			 * setParam("client.passport.PinTan.checkcert","1");
			 * setParam("client.passport.PinTan.init","1");
			 */


			setParam("client.passport.default", "RDHNew"); //$NON-NLS-1$ //$NON-NLS-2$
			setParam("client.passport.RDHNew.init", "1");
			setParam("client.passport.RDHNew.filename", bank_rdhfile);
			// the "passport" file will be stored in this directory -- just enter one where you have write privileges //$NON-NLS-1$
	
			passport = AbstractHBCIPassport.getInstance();
		} catch (Exception ex) {
			passport = null;
			ex.printStackTrace();
			throw new RuntimeException("", ex); //$NON-NLS-1$
		}

		try {
			String pversion = "210";//passport.getHBCIVersion(); will return the last used version, and empty string if not yet used previously. 2.10=210 is a safe assumption for my account //$NON-NLS-1$
			hbciHandle = new HBCIHandler(pversion, passport);
		} catch (Exception ex) {
			try {
				passport.close();
			} catch (Exception ex1) {
			}
			passport = null;
			log.error(ex);
		}
	}

	public synchronized HBCIPassport getPassport() {
		if (passport == null) {
			initPassport();
		}

		return passport;
	}

	public HBCIPassport getCurrentPassport() {
		return passport;
	}

	public synchronized void closePassport() {
		if (hbciHandle != null) {
			try {
				hbciHandle.close();
			} catch (Exception e) {
				try {
					passport.close();
				} catch (Exception ex) {
				}
			} finally {
				passport = null;
				hbciHandle = null;
			}
		}
	}

	public synchronized HBCIJob newJob(String jobname) {
		getPassport();

		try {
			return hbciHandle.newJob(jobname);
		} catch (Exception e) {
			log.error(e);
			throw new RuntimeException("", e); //$NON-NLS-1$
		}
	}

	public synchronized void addJob(String customerid, HBCIJob job) {
		getPassport();

		try {
			job.addToQueue(customerid);
		} catch (Exception e) {
			
			throw new RuntimeException("", e); //$NON-NLS-1$
		}
	}

	public synchronized HBCIExecStatus execute(String headline) {
		getPassport();

		try {
			showAbortWindow();
			HBCIExecStatus status = hbciHandle.execute();
			hideAbortWindow();

			return status;
		} catch (Exception e) {
			hideAbortWindow();
			log.error(e);
			throw new RuntimeException("", e); //$NON-NLS-1$
		}
	}

	private void showAbortWindow() {
	}

	private synchronized void hideAbortWindow() {
	}
}
