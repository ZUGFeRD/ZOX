package zox;

public class Recipient {

	public static void main(String[] args) {
System.out.println("Starting recipient");	
RecipientComThread ct=new RecipientComThread();
ct.run();

	}

}
