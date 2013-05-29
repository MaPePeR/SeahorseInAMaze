package client;

public class Main {
	public static void main(String args[]){
		String hostname;
		int port;
		if (args.length!=2){
			hostname= "localhost";
			port = 1234;
		} else{
			hostname = args[0];
			port = Integer.parseInt(args[1]);
		}
		Client client = new Client(hostname, port);
		client.run();
	}

}
