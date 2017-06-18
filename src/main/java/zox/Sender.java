package zox;


public class Sender {

	public static void main(String[] args) {
		System.out.println("Starting sender");	
		SenderComThread ct=new SenderComThread();
		ct.run();
	}

}
