package zox;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.pdfbox.io.IOUtils;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.sasl.SASLMechanism;
import org.jivesoftware.smack.sasl.provided.SASLPlainMechanism;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration.Builder;
import org.jivesoftware.smack.util.FileUtils;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferNegotiator;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import org.jivesoftware.smackx.offline.OfflineMessageManager;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.EntityFullJid;
import org.jxmpp.jid.EntityJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;
import org.mustangproject.ZUGFeRD.ZUGFeRDExporter;
import org.mustangproject.ZUGFeRD.ZUGFeRDImporter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class SenderComThread extends Thread {
	private static XMPPTCPConnection xmppConnection;
	private ChatManager chatmanager;
	boolean isCancelled = false;

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
		if (c == null) {
			c = chatmanager.createChat(participant, null);
		}
		try {
			c.sendMessage(m);
		} catch (NotConnectedException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	public void run() {
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
			configBuilder.setXmppDomain(JidCreate.domainBareFrom("jochens-air.fritz.box"));
		} catch (XmppStringprepException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		configBuilder.setUsernameAndPassword("zender", "zender");
		/*
		 * TLSUtils.acceptAllCertificates(conf); conf.setResource("sender");
		 */
		xmppConnection = new XMPPTCPConnection(configBuilder.build());
		String fileTransferRecipient = "zox@jochens-air.fritz.box";
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
				if (entry.getJid().equals(fileTransferRecipient)) {
					fileTransferRecipientWithCurrentService = (EntityFullJid) roster.getPresence(entry.getJid())
							.getFrom();

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
			File toTransfer = new File("totransfer.pdf");
			oft.sendFile(toTransfer, "ZUGFeRD invoice");

			FileInputStream fisTargetFile = new FileInputStream(toTransfer);

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

}
