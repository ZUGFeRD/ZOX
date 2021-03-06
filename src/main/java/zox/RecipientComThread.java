package zox;

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
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
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
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.EntityJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;
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
	private Object lock = new Object();

	String username = null;
	String password = null;
	String domain = null;

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

		if (domain == null) {
			throw new RuntimeException("Domain must be set before starting");
		}
		if (username == null) {
			throw new RuntimeException("Username must be set before starting");
		}
		if (password == null) {
			throw new RuntimeException("Password must be set before starting");
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

			// Create the file transfer manager
			final FileTransferManager manager = FileTransferManager.getInstanceFor(xmppConnection);
			// Create the listener
			manager.addFileTransferListener(new FileTransferListener() {
				public void fileTransferRequest(FileTransferRequest request) {
					// Check to see if the request should be accepted
					// Accept it
					System.out.println("File transfer request from " + request.getRequestor().asUnescapedString());
					IncomingFileTransfer transfer = request.accept();
					try {
						String filename = "received_file.pdf";
						transfer.recieveFile(new File(filename));
						ZUGFeRDImporter zi = new ZUGFeRDImporter();
						zi.extract(filename);
						if (zi.canParse()) {
							zi.parse();
							System.out.println("Amounts to " + zi.getAmount());
							Chat chat = chatmanager.chatWith(request.getRequestor().asEntityBareJidIfPossible());
							try {
								chat.send("Thank you for your invoice over " + zi.getAmount());
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
					// request.reject(); would also be possible ;-)

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
					List<Message> offMessages = offlineManager.getMessages();
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
			chatmanager.addIncomingListener(new IncomingChatMessageListener() {
				
				@Override
				public void newIncomingMessage(EntityBareJid from, Message theMessage, Chat theChat) {
					// TODO Auto-generated method stub
					// TODO Auto-generated method stub
					final String body = theMessage.getBody();
					if (body != null) {
						if (body.trim().startsWith("<ping")) {
							
							sendPong(theChat.getXmppAddressOfChatPartner(), theChat, body);

						}
						if (body.trim().startsWith("<exit")) {
							isCancelled = true;
							synchronized (lock) {
								try {
									// Do something with the message
									// here like update some status
									lock.notify();
								} catch (Exception e) {
									e.printStackTrace();
								}

							}
						}
					}

				}
			});
			
			Presence presence = new Presence(Presence.Type.available);
			xmppConnection.sendStanza(presence);

			do {
				Thread.yield();

				synchronized (lock) {
					try {
						lock.wait();
						// use the updated status
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
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

	public String getUsername() {
		return username;
	}

	public RecipientComThread setUsername(String username) {
		this.username = username;
		return this;
	}

	public String getPassword() {
		return password;
	}

	public RecipientComThread setPassword(String password) {
		this.password = password;
		return this;
	}

	public String getDomain() {
		return domain;
	}

	public RecipientComThread setDomain(String domain) {
		this.domain = domain;
		return this;
	}

}
