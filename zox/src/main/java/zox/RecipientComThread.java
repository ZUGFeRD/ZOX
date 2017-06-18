package zox;

import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

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
import org.jivesoftware.smack.sasl.SASLMechanism;
import org.jivesoftware.smack.sasl.provided.SASLPlainMechanism;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration.Builder;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.jivesoftware.smackx.offline.OfflineMessageManager;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.EntityJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;
import org.mustangproject.ZUGFeRD.ZUGFeRDExporter;
import org.mustangproject.ZUGFeRD.ZUGFeRDImporter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class RecipientComThread extends Thread {
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

		configBuilder.setUsernameAndPassword("zox", "zox");
		/*
		 * TLSUtils.acceptAllCertificates(conf); conf.setResource("sender");
		 */
		xmppConnection = new XMPPTCPConnection(configBuilder.build());

		try {
			// SASLAuthentication.supportSASLMechanism("PLAIN", 0);

			xmppConnection.connect();

			xmppConnection.login();
			
			// Create the file transfer manager
			final FileTransferManager manager = FileTransferManager.getInstanceFor(xmppConnection);
			// Create the listener
			manager.addFileTransferListener(new FileTransferListener() {
				public void fileTransferRequest(FileTransferRequest request) {
				// Check to see if the request should be accepted
					// Accept it
					System.out.println("File transfer request from "+request.getRequestor().asUnescapedString());
					IncomingFileTransfer transfer = request.accept();
					try {
						String filename="received_file.pdf";
						transfer.recieveFile(new File(filename));
						ZUGFeRDImporter zi=new ZUGFeRDImporter();
						zi.extract(filename);
						if (zi.canParse()) {
							zi.parse();
							System.out.println("Amounts to "+zi.getAmount());
							Chat chat=chatmanager.createChat(request.getRequestor().asEntityJidIfPossible(), new ChatMessageListener() {

								public void processMessage(Chat arg0, Message arg1) {
									// TODO Auto-generated method stub
									
								}
								
							});
							try {
								chat.sendMessage("Thank you for your invoice over "+zi.getAmount());
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
							
						}
						
						
					} catch (SmackException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					//request.reject(); would also be possible ;-)
				
			}
			});
			Roster.setDefaultSubscriptionMode(Roster.SubscriptionMode.accept_all);

			final OfflineMessageManager offlineManager = new OfflineMessageManager(xmppConnection);
			try {

				if (offlineManager.supportsFlexibleRetrieval()) {

					System.out.println("supports offline message retrieval");

				} else {
					System.out.println("does not support offline message retrieval");
				}

				System.out.println("Number of offline messages:: " + offlineManager.getMessageCount());

				if (offlineManager.getMessageCount() > 0) {
					List offMessages = offlineManager.getMessages();
					Iterator<org.jivesoftware.smack.packet.Message> it = offMessages.iterator();

					while (it.hasNext()) {
						org.jivesoftware.smack.packet.Message message = it.next();
						System.out.println("received offline messages, from [" + message.getFrom() + "] the message:"
								+ message.getBody());
						System.out.println("offline message: " + message.getBody());
					}
					offlineManager.deleteMessages();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			chatmanager = ChatManager.getInstanceFor(xmppConnection);

			MessageListener ml = new MessageListener() {

				public void processMessage(Message arg0) {
					System.out.println("Rec message");
				}
			};
			chatmanager.addChatListener(new ChatManagerListener() {

				public void chatCreated(Chat arg0, boolean arg1) {

					arg0.addMessageListener(new ChatMessageListener() {

						public void processMessage(Chat theChat, Message theMessage) {
							// TODO Auto-generated method stub
							final String body = theMessage.getBody();
							if (body != null) {
								if (body.trim().startsWith("<ping")) {
									sendPong(theChat.getParticipant(), theChat, body);

								}
								if (body.trim().startsWith("<exit")) {
									isCancelled = true;
								}
							}

						}
					});

				}
			});

			Presence presence = new Presence(Presence.Type.available);
			xmppConnection.sendStanza(presence);

			do {

				Thread.yield();
			} while (!isCancelled);
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
		}

	}

}
