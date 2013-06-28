package client;

import generated.AcceptMessageType;
import generated.AwaitMoveMessageType;
import generated.LoginMessageType;
import generated.MazeCom;
import generated.MazeComType;
import generated.MoveMessageType;
import generated.ObjectFactory;

import java.io.IOException;
import java.net.Socket;

import javax.xml.bind.JAXBException;

import networking.XmlInStream;
import networking.XmlOutStream;
import client.types.GameHasEndedException;
import client.types.IllegalTurnException;
import client.types.RecievedWrongTypeException;

public class ServerContext {
	ObjectFactory obf = new ObjectFactory();
	Socket socket;
	XmlOutStream xmlout;
	XmlInStream xmlin;

	int id;

	public ServerContext(Socket s) throws IOException {
		this.socket = s;
		this.xmlout = new XmlOutStream(s.getOutputStream());
		this.xmlin = new XmlInStream(s.getInputStream());
	}

	public void login() throws IOException {
		this.login("Amazing Seahorse");
	}

	public void send(MazeCom mc) throws IOException, JAXBException {
		this.xmlout.write(mc);
	}

	public MazeCom receive() {
		return this.xmlin.readMazeCom();
	}

	public int login(String name) throws IOException {
		MazeCom request = this.obf.createMazeCom();
		request.setMcType(MazeComType.LOGIN);
		LoginMessageType loginMessage = this.obf.createLoginMessageType();
		loginMessage.setName(name);
		request.setLoginMessage(loginMessage);
		this.xmlout.write(request);
		MazeCom response = this.xmlin.readMazeCom();
		if (response.getMcType().equals(MazeComType.LOGINREPLY)) {
			this.id = response.getLoginReplyMessage().getNewID();
		} else if (response.getMcType().equals(MazeComType.DISCONNECT)) {
			System.out.println("Server disconnected me:");
			System.out.println(response.getDisconnectMessage().getErroCode().name());
		} else if (response.getMcType().equals(MazeComType.ACCEPT)) {
			if (!request.getAcceptMessage().isAccept()) {
				// Server does not accept me - try another name...
				this.login(name + "e");
			} else {
				System.out.println("This is now very weird.");
			}
		}
		return this.id;
	}

	public AwaitMoveMessageType waitForMyTurn() throws GameHasEndedException {
		MazeCom packet = this.xmlin.readMazeCom();
		if (packet.getMcType().equals(MazeComType.AWAITMOVE)) {
			return packet.getAwaitMoveMessage();
		} else if (packet.getMcType().equals(MazeComType.WIN)) {
			throw new GameHasEndedException(packet.getWinMessage());
		} else {
			throw new RecievedWrongTypeException(packet);
		}
	}

	public void doMyTurn(MoveMessageType moveMessage) throws IllegalTurnException {
		MazeCom message = this.obf.createMazeCom();
		message.setId(this.id);
		message.setMcType(MazeComType.MOVE);
		message.setMoveMessage(moveMessage);
		this.xmlout.write(message);

		MazeCom response = this.xmlin.readMazeCom();
		if (response.getMcType().equals(MazeComType.ACCEPT)) {
			AcceptMessageType acceptMessage = response.getAcceptMessage();
			if (acceptMessage.isAccept()) {
				return;
			} else {
				throw new IllegalTurnException(acceptMessage.getErrorCode().name());
			}
		} else {
			throw new RecievedWrongTypeException(response);
		}
	}
}
