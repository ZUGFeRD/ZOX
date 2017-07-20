package zox;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException;

import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.sasl.SASLMechanism;
import org.jivesoftware.smack.sasl.provided.SASLPlainMechanism;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration.Builder;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferNegotiator;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import org.jxmpp.jid.EntityFullJid;
import org.jxmpp.jid.EntityJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class SenderComThread extends Thread {
	private static XMPPTCPConnection xmppConnection;
	private ChatManager chatmanager;
	boolean isCancelled = false;

	String username = null;
	String password = null;
	String domain = null;
	String recipient = null;
	String filename = null;

	public void sendPong(EntityJid participant, Chat c, String requestMessage) {
		Message m = new Message();
		m.setTo(participant);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		String idResponseAttr = "";

		Node response;
		try {
			builder = factory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(requestMessage));
			Document document = builder.parse(is);
			response = document.getFirstChild();
			Element e = (Element) response;
			String id = e.getAttribute("id");
			String logStr = "ping received from " + participant;
			if ((id != null) && (id.length() != 0)) {
				idResponseAttr = " id='" + id + "'";
				logStr += " id=" + id;
			}

		} catch (ParserConfigurationException e1) {
			e1.printStackTrace();
		} catch (SAXException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		m.setBody("<pong" + idResponseAttr + "/>");
		
		try {
			if (c == null) {
				c = chatmanager.chatWith(JidCreate.entityBareFrom(participant));
			}
			c.send(m);
		} catch (NotConnectedException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (XmppStringprepException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void run() {
		
		if (domain==null) {
			throw new RuntimeException("Domain must be set before starting");
		}
		if (username==null) {
			throw new RuntimeException("Username must be set before starting");
		}
		if (password==null) {
			throw new RuntimeException("Password must be set before starting");
		}
		if (recipient==null) {
			throw new RuntimeException("Recipient must be set before starting");
		}
		if (filename==null) {
			throw new RuntimeException("Filename must be set before starting");
		}
		
		
		SASLAuthentication.unBlacklistSASLMechanism("PLAIN");
		SASLPlainMechanism newsasl = new SASLPlainMechanism();
		SASLAuthentication.registerSASLMechanism(newsasl);
		// SASLDigestMD5Mechanism dig5 = new SASLDigestMD5Mechanism();
		// SASLAuthentication.registerSASLMechanism(dig5);
		SASLAuthentication.blacklistSASLMechanism(SASLMechanism.DIGESTMD5);
		SASLAuthentication.blacklistSASLMechanism(SASLMechanism.CRAMMD5);

		// Create a connection to the jabber.org server._
		Builder configBuilder = XMPPTCPConnectionConfiguration.builder();
		configBuilder.setSendPresence(false);

		configBuilder.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
		configBuilder.setDebuggerEnabled(true);
		try {
			configBuilder.setXmppDomain(JidCreate.domainBareFrom(domain));
		} catch (XmppStringprepException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		configBuilder.setUsernameAndPassword(username, password);
		/*
		 * TLSUtils.acceptAllCertificates(conf); conf.setResource("sender");
		 */
		xmppConnection = new XMPPTCPConnection(configBuilder.build());
		try {
			// SASLAuthentication.supportSASLMechanism("PLAIN", 0);

			xmppConnection.connect();

			xmppConnection.login();

			Presence presence = new Presence(Presence.Type.available);
			xmppConnection.sendStanza(presence);
			// Create the file transfer manager
			final FileTransferManager manager = FileTransferManager.getInstanceFor(xmppConnection);

			EntityFullJid fileTransferRecipientWithCurrentService = null;

			Roster roster = Roster.getInstanceFor(xmppConnection);

			if (!roster.isLoaded()) {
				roster.reloadAndWait();

			}

			Collection<RosterEntry> entries = roster.getEntries();
			System.out.println("my roster");
			for (RosterEntry entry : entries) {
				if (entry.getJid().equals(recipient)) {
					fileTransferRecipientWithCurrentService = (EntityFullJid) roster.getPresence(entry.getJid())
							.getFrom().asEntityFullJidIfPossible();

				}

			}
			System.out.println("roster end");
			if (fileTransferRecipientWithCurrentService == null) {
				throw new Exception("Recipient not found in roster");
			}
			// manager.createOutgoingFileTransfer(JidCreate.entityFullFrom("psi","jochens-air.fritz.box","smack"))
			// XmppStringUtils.completeJidFrom(USER, SERV, "mobile")
			// final OutgoingFileTransfer
			// oft=manager.createOutgoingFileTransfer(JidCreate.entityFullFrom("psi","jochens-air.fritz.box","Jochens-Air"));
			final OutgoingFileTransfer oft = manager
					.createOutgoingFileTransfer(fileTransferRecipientWithCurrentService);
			File toTransfer = new File(filename);
			
			
			oft.sendFile(toTransfer, "ZUGFeRD invoice");

			//FileInputStream fisTargetFile = new FileInputStream(toTransfer);

			// FileTransferNegotiator negotiator = new FileTransferNegotiator();
			System.err.println("file transfer negotiable:  " + FileTransferNegotiator.isServiceEnabled(xmppConnection));

			while (!oft.isDone()) {
				System.out.println(oft.getProgress() + " is done!");
				// System.out.println(transfer.getStreamID() + " is done!");

				Thread.yield();
			}
			System.out.println("on end ");
			presence = new Presence(Presence.Type.unavailable);
			xmppConnection.sendStanza(presence);

			xmppConnection.disconnect();

		} catch (SmackException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (XMPPException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// We've done our job!
		System.exit(0);
	}
	

	public String getUsername() {
		return username;
	}

	public SenderComThread setUsername(String username) {
		this.username = username;
		return this;
	}

	public String getPassword() {
		return password;
	}

	public SenderComThread setPassword(String password) {
		this.password = password;
		return this;
	}

	public String getDomain() {
		return domain;
	}

	public SenderComThread setDomain(String domain) {
		this.domain = domain;
		return this;
	}	

	public String getRecipient() {
		return domain;
	}

	public SenderComThread setRecipient(String recipient) {
		this.recipient = recipient;
		return this;
	}
	
	public String getFilename() {
		return filename;
	}

	public SenderComThread setFilename(String filename) {
		this.filename = filename;
		return this;
	}

}
