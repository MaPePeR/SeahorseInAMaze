package spieler;

import generated.MoveMessageType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import ourGenerated.Board;
import ourGenerated.Card;
import ourGenerated.Position;

public class SimpleKI extends Spieler {

	public String name = "Simple";

	// (0,1)(0,3)(0,5)
	// (1,0)(1,6)
	// (3,0)(3,6)
	// (5,0)(5,6)
	// (6,1)(6,3)(6,5)

	@Override
	public String getName() {
		return this.name;
	}

	public static class Turn implements Comparable<Turn> {
		Position target;// hier laufe ich hin
		Position shiftPosition;// hierhin schiebe ich
		Card shiftCard;
		int bewertung;

		@Override
		public int compareTo(Turn o) {
			return this.bewertung - o.bewertung;
		}

	}

	public int bewerten(Board b, Card shiftCard) {
		return 4;
	}

	@Override
	public MoveMessageType doTurn(Board bt, Map<Integer, Integer> idHasNTreasuresleft) {
		Card shiftCard = bt.getShiftCard();
		ArrayList<Turn> turns = new ArrayList<Turn>();
		for (int i = 0; i < bt.getCards().length; i += 6) {
			for (int j = 1; j < bt.getCards()[i].length; j += 2) {
				Position p1 = new Position(i, j);
				Position p2 = new Position(j, i);
				for (int k = 0; k < 4; k++) {
					Turn t = new Turn();

					t.shiftPosition = p1;
					t.bewertung = this.bewerten(bt, new Card(shiftCard));
					t.shiftCard = shiftCard;
					turns.add(t);

					t.shiftPosition = p2;
					t.bewertung = this.bewerten(bt, new Card(shiftCard));
					turns.add(t);
					t.shiftCard = shiftCard;

					shiftCard.turnCounterClockwise(1);

				}
			}
		}
		Collections.sort(turns);
		MoveMessageType mmt = new MoveMessageType();
		mmt.setShiftCard(turns.get(0).shiftCard.getCardType());
		mmt.setShiftPosition(turns.get(0).shiftPosition.getPositionType());
		// TODO auch noch pintarget setzen

		return mmt;
	}

}
