package spieler;

import generated.AwaitMoveMessageType;
import generated.MoveMessageType;
import generated.TreasureType;
import generated.TreasuresToGoType;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ourGenerated.Board;
import ourGenerated.Card;
import ourGenerated.Position;

public abstract class Spieler implements ISpieler {
	public abstract MoveMessageType doTurn(Board bt, Map<Integer, Integer> idHasNTreasuresleft);

	@Override
	public abstract String getName();

	protected int id;

	@Override
	public int getId() {
		return this.id;
	}

	@Override
	public void setId(int id) {
		this.lastIdHasNTreasuresleft = null;
		this.id = id;
	}

	private Map<Integer, Integer> lastIdHasNTreasuresleft = null;
	protected List<TreasureType> alreadyFoundTreasures = new LinkedList<TreasureType>();

	@Override
	public MoveMessageType doTurn(AwaitMoveMessageType awaitMoveMessageType) {
		Board board = new Board(awaitMoveMessageType.getBoard(), awaitMoveMessageType.getTreasure(), this.id);
		board.outputPretty();
		TreeMap<Integer, Integer> idHasNTreasuresleft = new TreeMap<Integer, Integer>();
		for (TreasuresToGoType ttgt : awaitMoveMessageType.getTreasuresToGo()) {
			System.out.print("Spieler " + ttgt.getPlayer() + " braucht noch " + ttgt.getTreasures());
			System.out.println(ttgt.getPlayer() == this.id ? "<" : "");
			idHasNTreasuresleft.put(ttgt.getPlayer(), ttgt.getTreasures());
			if (this.lastIdHasNTreasuresleft != null) {
				if (ttgt.getTreasures() < this.lastIdHasNTreasuresleft.get(ttgt.getPlayer())) {
					// Spieler hat einen Schatz gefunden.
					Position spielerPos = board.getSpielerPositions().get(ttgt.getPlayer());
					// Nur eine Bewegung Pro Runde => Er steht jetzt gerade auf
					// dem Schatz
					this.alreadyFoundTreasures.add(board.getCards()[spielerPos.y][spielerPos.x].getTreasure());
				}
			}
		}

		this.lastIdHasNTreasuresleft = idHasNTreasuresleft;
		MoveMessageType moveMessage = this.doTurn(board, idHasNTreasuresleft);
		if (moveMessage.getNewPinPos() == null) {
			throw new RuntimeException("KI did not set new pin pos!");
		}
		if (moveMessage.getNewPinPos().equals(
				board.shiftCardPosition(new Position(moveMessage.getShiftPosition()), board.getTreasurePosition()))) {
			// Wir stehen (nach dem shiften) auf unserer SchatzKarte => Diese
			// wird im nächsten Zug weg sein.
			if (!this.alreadyFoundTreasures.contains(board.getTreasure())) {
				this.alreadyFoundTreasures.add(board.getTreasure());
			}
		}
		System.out.println("Start Board: ");
		board.outputPretty();
		System.out.format("Board after %c has been shiftet: \n", new Card(moveMessage.getShiftCard()).getChar());
		System.out.println("Moving to " + new Position(moveMessage.getNewPinPos()));
		try {
			Board shiftedBoard = board.shift(new Position(moveMessage.getShiftPosition()),
					new Card(moveMessage.getShiftCard()));
			board.setMyPosition(new Position(moveMessage.getNewPinPos()));
			shiftedBoard.outputPretty();
		} catch (Exception e) {
			System.out.println("illegal Move!");
		}
		return moveMessage;
	}

	public List<Position> filterPositionsForTreasures(Board b, List<Position> positions) {
		List<Position> positionsWithTreasures = new LinkedList<Position>();
		Card cards[][] = b.getCards();
		for (Position p : positions) {
			if (cards[p.y][p.x].getTreasure() != null) {
				positionsWithTreasures.add(p);
			}
		}
		return positionsWithTreasures;
	}

	public List<Position> filterPositionsForNotYetTakenTreasures(Board b, List<Position> positions) {
		List<Position> positionsWithNotYetTakenTreasures = new LinkedList<Position>();
		Card cards[][] = b.getCards();
		for (Position p : positions) {
			if (cards[p.y][p.x].getTreasure() != null
					&& !this.alreadyFoundTreasures.contains(cards[p.y][p.x].getTreasure())) {
				positionsWithNotYetTakenTreasures.add(p);
			}
		}
		return positionsWithNotYetTakenTreasures;
	}

}
