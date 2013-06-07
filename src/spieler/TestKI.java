package spieler;

import generated.BoardType;
import generated.MoveMessageType;
import generated.ObjectFactory;
import generated.TreasuresToGoType;

public class TestKI extends Spieler {

	
	ObjectFactory obf = new ObjectFactory();

	@Override
	public MoveMessageType doTurn(BoardType bt, TreasuresToGoType ttg) {
		MoveMessageType mmt = this.obf.createMoveMessageType();
		return mmt;
	}

	@Override
	public String getName() {
		return "Testing Seahorse";
	}

}
