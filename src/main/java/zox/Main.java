package zox;

import com.sanityinc.jargs.CmdLineParser;
import com.sanityinc.jargs.CmdLineParser.Option;

public class Main {
	private static void printUsage() {
		System.err.println(getUsage());
	}

	private static String getUsage() {
		return "Usage: zox [-a,--action=receive|send]  [-p,--password]  [-d,--domain] [-u,--username] [-p,--password] [-r,--recipient] [-f,--filename] | [-h,--help]\r\n";
	}

	private static void printHelp() {
		System.out.println("Mustangproject.org's XMPP Recipient\r\n");
	}

	public static void main(String[] args) {
		CmdLineParser parser = new CmdLineParser();
		Option<String> actionOption = parser.addStringOption('a', "action");
		Option<String> usernameOption = parser.addStringOption('u', "username");
		Option<String> passwordOption = parser.addStringOption('p', "password");
		Option<String> domainOption = parser.addStringOption('d', "domain");
		Option<String> recipientOption = parser.addStringOption('r', "recipientJID");
		Option<String> filenameOption = parser.addStringOption('f', "filename");
		Option<Boolean> helpOption = parser.addBooleanOption('h', "help");

		if (args.length == 0) {
			printUsage();
			System.exit(2);

		}
		try {
			parser.parse(args);
		} catch (CmdLineParser.OptionException e) {
			System.err.println(e.getMessage());
			printUsage();
			System.exit(2);
		}

		String action = parser.getOptionValue(actionOption);
		String username = parser.getOptionValue(usernameOption);
		String password = parser.getOptionValue(passwordOption);
		String domain = parser.getOptionValue(domainOption);
		String recipient = parser.getOptionValue(recipientOption);
		String filename = parser.getOptionValue(filenameOption);
		
		Boolean helpRequested = parser.getOptionValue(helpOption, Boolean.FALSE);

		if (helpRequested) {
			printHelp();
		} else if ((username==null)||(password==null)||(domain==null)) {
			printUsage();
		} if (action.equalsIgnoreCase("send")) {
			System.out.println("Starting sender");
			SenderComThread ct = new SenderComThread();
			ct.setDomain(domain).setUsername(username).setPassword(password).setRecipient(recipient).setFilename(filename);
			
			ct.run();
		
		} else {
			System.out.println("Starting receiver");
			RecipientComThread ct = new RecipientComThread();
			ct.setDomain(domain).setUsername(username).setPassword(password);
			
			ct.run();
		
		}

	}

}
