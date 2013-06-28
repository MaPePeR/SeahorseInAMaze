package client;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Random;

import spieler.ISpieler;

public class Main {
	public static final String defaultHostname = "localhost";
	public static final int defaultPort = 5123;

	public static void main(String args[]) throws UnknownHostException, IOException {
		String hostname;
		int port;
		ISpieler ki;
		int howOften = 1;

		if (args.length >= 1) {
			ki = tryToLoadSpieler(args[0]);
			if (ki == null) {
				return;
			}
		} else {
			showUsage();
			return;
		}

		if (args.length >= 2) {
			hostname = args[1];
		} else {
			System.out.println("Using Default-Hostname: localhost");
			hostname = "localhost";
		}
		if (args.length >= 3) {
			try {
				port = Integer.parseInt(args[2]);
			} catch (NumberFormatException e) {
				System.out.println(args[2] + " is not a number");
				showUsage();
				return;
			}
		} else {
			System.out.println("Using Default-Port: 5123");
			port = 5123;
		}

		if (args.length == 4) {
			try {
				howOften = Integer.parseInt(args[3]);
				if (howOften <= 0) {
					System.out.println("Please use a natural number for the number of games");
					showUsage();
					return;
				}
			} catch (NumberFormatException e) {
				System.out.println(args[3] + " is not a number");
				showUsage();
				return;
			}
		} else if (args.length > 4) {
			System.out.println("Too many arguments!");
			showUsage();
			return;
		}

		// int won = 0;
		// int lost = 0;
		// int error = 0;
		Random rand = new Random();
		for (int i = 0; i < howOften; i++) {
			try {
				ki = tryToLoadSpieler(args[0]); // this should not fail, because
												// it worked the first time
				Client client = new Client(hostname, port);
				System.out.println("Starting KI ...");
				if (client.run(ki)) {
					// won++;
				} else {
					// lost++;
				}
			} catch (Throwable t) {
				t.printStackTrace();
				// error++;
			}
			WinStatistics.printStatistic();
			// System.out.format("Statistic after %d/%d games: \n",i + 1,
			// howOften);
			// System.out.format("%5s %5s %5s\n", "won", "lost", "error");
			// System.out.format("%5d %5d %5d\n", won, lost , error);
			if (i + 1 < howOften) {
				try {
					Thread.sleep(500 + rand.nextInt(2000));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		}

	}

	public static void showUsage() {
		System.out.format("USAGE: KI-Name [HOSTNAME=%s] [PORT=%d] [How often=1]\n", defaultHostname, defaultPort);
	}

	public static ISpieler tryToLoadSpieler(String spielername) {
		ClassLoader cl = Main.class.getClassLoader();
		Class<?> spielerInterface;
		try {
			spielerInterface = cl.loadClass("spieler.ISpieler");
		} catch (ClassNotFoundException e1) {
			throw new RuntimeException(e1);
		}

		try {
			Class<?> clazz = cl.loadClass("spieler." + spielername);
			if (clazz != null) {
				if (spielerInterface.isAssignableFrom(clazz)) {
					try {
						return (ISpieler) clazz.newInstance();
					} catch (InstantiationException e) {
						System.out.println("Coudn't instantiate spieler." + spielername);
						return null;
					} catch (IllegalAccessException e) {
						System.out.println("Coudn't access spieler." + spielername);
						return null;
					}
				} else {
					System.out.println("KI '" + spielername + "' does not implement ISpieler");
					return null;
				}
			}
		} catch (ClassNotFoundException e) {
			System.out.println("Coudn't find Class: spieler." + spielername);
			return null;
		}
		return null;
	}

}
