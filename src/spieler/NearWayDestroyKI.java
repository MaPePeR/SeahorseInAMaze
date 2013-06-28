package spieler;

import generated.MoveMessageType;

import java.util.Map;

import ourGenerated.Board;
import ourGenerated.Card;
import ourGenerated.Position;
import spieler.strategies.NearWayDestroyStrategie;
import assessment.High;
import assessment.IStrategie;
import client.types.IllegalTurnException;

public class NearWayDestroyKI extends Spieler {

	@Override
	public MoveMessageType doTurn(Board bt, Map<Integer, Integer> idHasNTreasuresleft) {
		IStrategie strat;
		this.currentMaxHigh = new High(0, 0, Integer.MIN_VALUE);
		/*
		 * if(idHasNTreasuresleft.get(this.id) == 1) { //Das Spiel endet gleich
		 * strat = new ICanWinStrategie(); } else
		 * if(idHasNTreasuresleft.values().contains(1)) { //Jemand anderes
		 * braucht nur noch einen Schatz strat = new
		 * PreventEnemyFromWinningStrategie(); } else { strat = new
		 * DefaultStrategie(); }
		 */

		strat = new NearWayDestroyStrategie();
		Card c = bt.getShiftCard();
		for (int rotationCount = 0; rotationCount < 4; ++rotationCount) {
			for (int x = 5; x >= 0; x -= 2) {
				System.out.println("x");
				this.versuche(strat, bt, x, 0, c, rotationCount);
				this.versuche(strat, bt, x, 6, c, rotationCount);
			}
			for (int y = 5; y >= 0; y -= 2) {
				System.out.println("y");
				this.versuche(strat, bt, 0, y, c, rotationCount);
				this.versuche(strat, bt, 6, y, c, rotationCount);
			}
			c.turnCounterClockwise(1);
		}

		c.turnCounterClockwise(this.currentMaxRotationCount);
		MoveMessageType move = new MoveMessageType();
		move.setNewPinPos(this.currentMaxHigh.pos.getPositionType());
		move.setShiftCard(c.getCardType());
		move.setShiftPosition(new Position(this.currentMaxX, this.currentMaxY).getPositionType());
		return move;
	}

	High currentMaxHigh = new High(0, 0, Integer.MIN_VALUE);
	int currentMaxX, currentMaxY, currentMaxRotationCount;

	private void versuche(IStrategie strat, Board bt, int x, int y, Card c, int rotationCount) {
		try {
			High h = strat.bewerte(bt.shift(new Position(x, y), c));
			System.out.println(h.value);
			if (h.value > this.currentMaxHigh.value) {
				this.currentMaxHigh = h;
				this.currentMaxX = x;
				this.currentMaxY = y;
				this.currentMaxRotationCount = rotationCount;
			}
		} catch (IllegalTurnException e) {
			System.out.println("IllegalTurn");
		}
	}

	@Override
	public String getName() {
		return "Strategic Seahorse NWD";
	}

}
