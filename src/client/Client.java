package client;

import generated.AwaitMoveMessageType;
import generated.MazeComType;
import generated.MoveMessageType;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import spieler.ISpieler;
import client.types.GameHasEndedException;
import client.types.IllegalTurnException;
import client.types.RecievedWrongTypeException;

public class Client {
	private ServerContext context;
	public boolean cont;

	public Client(String hostname, int port) throws UnknownHostException,
			IOException {
		Socket s = new Socket(hostname, port);
		this.context = new ServerContext(s);
	}

	public void run(ISpieler spieler) {
		try {
			int id;
			id = this.context.login(spieler.getName());
			spieler.setId(id);

		} catch (IOException e) {
			System.out.println("Login fehlgeschlagen");
			e.printStackTrace();
		}

		try {

			while (true) {// Der Ablauf des Programms
				AwaitMoveMessageType request = this.context.waitForMyTurn();
				MoveMessageType myturn = spieler.doTurn(request);
				try {
					this.context.doMyTurn(myturn);
				} catch (IllegalTurnException e) {
					System.err.println("KI wanted to do a invalid Turn! "
							+ e.getMessage());
				}
			}
		} catch (GameHasEndedException e) {
			System.out.format("The Game has ended Winner: %d %s\n",
					e.getWinMessage().getWinner().getId(),
					e.getWinMessage().getWinner().getValue());
			if(spieler.getId() == e.getWinMessage().getWinner().getId()) {
				System.out.println("THATS ME!!!");
			}
		} catch (RecievedWrongTypeException e) {
			if (e.getFailPacket().getMcType().equals(MazeComType.DISCONNECT)) {
				System.out.println("The Server does not like us. DISCONNECT: "
						+ e.getFailPacket().getDisconnectMessage().getName());
			} else {
				System.out.println("Invalid Packet: "
						+ e.getFailPacket().getMcType());
				throw e;
			}
		}

	}
}
