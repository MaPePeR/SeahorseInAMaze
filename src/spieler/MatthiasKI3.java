package spieler;

import generated.MoveMessageType;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import ourGenerated.BlockedCard;
import ourGenerated.Board;
import ourGenerated.Card;
import ourGenerated.Position;
import client.types.IllegalTurnException;

public class MatthiasKI3 extends Spieler {

	private class Bewertung implements Comparable<Bewertung> {
		public final int recursionsToFindTreasure;

		public final int howManyWallsBlockMyWayToTreasure;

		public final int myNetworkSize;

		public final int averageEnemyMovability;

		public Bewertung(int recursionsToFindTreasure, int howManyWallsBlockMyWay, int averageEnemyMovability,
				int myNetworkSize) {
			this.recursionsToFindTreasure = recursionsToFindTreasure;
			this.howManyWallsBlockMyWayToTreasure = howManyWallsBlockMyWay;
			this.averageEnemyMovability = averageEnemyMovability;
			this.myNetworkSize = myNetworkSize;
		}

		@Override
		public int compareTo(Bewertung o) {
			if (o == null) {
				return 1;
			}
			//a.compareTo(b) < 0 <=> a < b => return -1 
			//a.compareTo(b) > 0 <=> a > b => return 1
			//1 => I'm better.
			if (this.recursionsToFindTreasure == o.recursionsToFindTreasure) {
				if (this.myNetworkSize == o.myNetworkSize) {
					if (this.howManyWallsBlockMyWayToTreasure == o.howManyWallsBlockMyWayToTreasure) {
						if (this.averageEnemyMovability == o.averageEnemyMovability) {
							return 0;
						} else {
							return this.averageEnemyMovability < o.averageEnemyMovability ? 1 : -1;
						}
					} else {
						return this.howManyWallsBlockMyWayToTreasure < o.howManyWallsBlockMyWayToTreasure ? 1 : -1;
					}
				} else {
					return this.myNetworkSize > o.myNetworkSize ? 1 : -1;

				}
			} else {
				return this.recursionsToFindTreasure < o.recursionsToFindTreasure ? 1 : -1;
			}
		}

		@Override
		public String toString() {
			return String.format("Treasure: %d\nNetworkSize: %d\nWalls: %d\nMovability: %d\n",
					this.recursionsToFindTreasure, this.myNetworkSize, this.howManyWallsBlockMyWayToTreasure,
					this.averageEnemyMovability);
		}
	}

	private class Zug {
		public final Position shitPosition;
		public final int cardRotation;
		public final List<Position> movePositions;

		public Zug(Position shiftPosition, int cardRotation, List<Position> movePositions) {
			this.shitPosition = shiftPosition;
			this.cardRotation = cardRotation;
			this.movePositions = movePositions;
		}
	}

	// List<Position> currentMaxMovePositions = new ArrayList<Position>();
	List<Zug> currentMaxZuege = new LinkedList<Zug>();
	// int currentMaxRotationCount, currentMaxX, currentMaxY;
	// Position currentMaxShiftPosition;
	Bewertung currentMaxBewertung;

	Random rand = new Random();

	@Override
	public MoveMessageType doTurn(Board bt, Map<Integer, Integer> idHasNTreasuresleft) {
		this.currentMaxBewertung = null;
		Card c = bt.getShiftCard();
		for (int rotationCount = 0; rotationCount < 4; ++rotationCount) {
			for (int x = 5; x >= 0; x -= 2) {
				this.versuche(bt, x, 0, c, rotationCount, idHasNTreasuresleft);
				this.versuche(bt, x, 6, c, rotationCount, idHasNTreasuresleft);
			}
			for (int y = 5; y >= 0; y -= 2) {
				this.versuche(bt, 0, y, c, rotationCount, idHasNTreasuresleft);
				this.versuche(bt, 6, y, c, rotationCount, idHasNTreasuresleft);
			}
			c.turnCounterClockwise(1);
		}
		System.out.println(this.currentMaxBewertung);
		System.out.format("Selecting Shift Position & Card Rotation from %d possibilities\n",
				this.currentMaxZuege.size());
		Zug z = this.currentMaxZuege.get(this.currentMaxZuege.size() == 1 ? 0 : this.rand.nextInt(this.currentMaxZuege
				.size()));
		c.turnCounterClockwise(z.cardRotation);
		MoveMessageType move = new MoveMessageType();

		move.setShiftPosition(z.shitPosition.getPositionType());
		move.setShiftCard(c.getCardType());

		System.out.format("Selecting New Pin Pos from %d possibilities\n", z.movePositions.size());
		move.setNewPinPos(z.movePositions.get(
				z.movePositions.size() == 1 ? 0 : this.rand.nextInt(z.movePositions.size())).getPositionType());
		System.out.println(this.currentMaxBewertung);
		return move;
	}

	private void versuche(Board bt, int x, int y, Card c, int rotationCount, Map<Integer, Integer> idHasNTreasuresleft) {
		Position shiftPosition = new Position(x, y);
		Board shiftetBoard;

		if (!bt.isValidMove(shiftPosition, c)) {
			return;
		}
		try {
			shiftetBoard = bt.shift(shiftPosition, c);
		} catch (IllegalTurnException e) {
			throw new RuntimeException("This should not have happened ever.");
		}
		Position myPos = shiftetBoard.myPosition();
		Position treasurePos = shiftetBoard.getTreasurePosition();
		List<Position> whereCanIGo = shiftetBoard.getPossiblePositionsFromPosition(myPos);
		int[][] walls;

		// Schatz ist nicht rausgeschoben && Ich kann ihn erreichen
		boolean canFindTreasure = treasurePos != null && whereCanIGo.contains(treasurePos);

		List<Position> movePositions = new LinkedList<Position>();
		int howManyWallsBlockMyWayToTreasure = 0;

		// Kann schatz nicht finden, dieser ist aber auf dem board.
		if (!canFindTreasure && treasurePos != null) {
			walls = shiftetBoard.howManyWallsStraightLine(treasurePos);
			for (int i = 5; i >= 0; i -= 2) {
				walls[0][i] = walls[6][i] = Math.min(walls[0][i], walls[6][i]);
				walls[i][0] = walls[i][6] = Math.min(walls[i][0], walls[i][6]);
			}

			int minWalls = Integer.MAX_VALUE;
			for (Position pos : whereCanIGo) {
				if (walls[pos.y][pos.x] < minWalls) {
					minWalls = walls[pos.y][pos.x];
					movePositions.clear();
					movePositions.add(pos);
				} else if (walls[pos.y][pos.x] == minWalls) {
					movePositions.add(pos);
				}
			}
			howManyWallsBlockMyWayToTreasure = minWalls;
		} else if (canFindTreasure) { // Kann schatz finden => dieser ist auf
										// dem board
			movePositions.add(treasurePos);
		} else { // Schatz ist rausgeschoben
			return; // Das wollen wir nicht.
		}

		// Zähle, wieviele Spieler in dem gleichen Netzwerk sind, wie ich.
		int playersInMyNetwork = 0;
		for (Position p : whereCanIGo) {
			for (Integer playerID : shiftetBoard.getCards()[p.y][p.x].getPlayers()) {
				if (idHasNTreasuresleft.keySet().contains(playerID)) { 
					// Ist ein Aktiver Spieler
					playersInMyNetwork += 1;
				}
			}
		}
		int myNetworkSize = whereCanIGo.size() / playersInMyNetwork;

		// Summiere auf, wieviele Felder meine Gegner erreichen können.
		int enemysCanMoveTiles = 0;
		for (Entry<Integer, Position> entry : shiftetBoard.getSpielerPositions().entrySet()) {
			if (!idHasNTreasuresleft.keySet().contains(entry.getKey())) {
				// Ist KEIN Aktiver Spieler => Skip
				continue;
			}
			if (entry.getKey() != this.id) {
				enemysCanMoveTiles += shiftetBoard.getPossiblePositionsFromPosition(entry.getValue()).size();
			}
		}

		int rekursionsToTreasure = canFindTreasure ? 0 : 4;
		shiftetBoard.clearForbidden();
		if(!canFindTreasure && this.currentMaxBewertung != null && this.currentMaxBewertung.recursionsToFindTreasure > 0) {
			//Kann in diesem Zug nicht sofort den Schatz finden und wir haben auch noch keinen anderen zug in dem das geht.
			rekursionsToTreasure = 0;
			rekursionPositions = null;
			//TODO: Rekursion
			
			for (int x1 = 5; x1 >= 0; x1 -= 2) {
				this.doRekursion(shiftetBoard, new Position(x1, 0), whereCanIGo);
				this.doRekursion(shiftetBoard, new Position(x1, 6), whereCanIGo);			
			}
			for (int y1 = 5; y1 >= 0; y1 -= 2) {
				this.doRekursion(shiftetBoard, new Position(0, y1), whereCanIGo);
				this.doRekursion(shiftetBoard, new Position(6, y1), whereCanIGo);
			}
			if(rekursionPositions != null) {
				movePositions = rekursionPositions;
			}
		}
		Bewertung b = new Bewertung(rekursionsToTreasure, howManyWallsBlockMyWayToTreasure,
				enemysCanMoveTiles, myNetworkSize);

		// Aktuelle Bewertung ist besser. => Alte gute zuege verwerfen
		if (b.compareTo(this.currentMaxBewertung) > 0) {
			this.currentMaxZuege.clear();
		}
		if (b.compareTo(this.currentMaxBewertung) >= 0) {
			this.currentMaxBewertung = b;
			this.currentMaxZuege.add(new Zug(shiftPosition, rotationCount, movePositions));
		}
	}

	private List<Position> shiftPlayerPositions(Position shiftPos, List<Position> l) {
		List<Position> out = new LinkedList<Position>();
		for(Position p: l) {
			out.add(Board.shiftPlayerPosition(shiftPos, p));
		}
		return out;
	}
	
	private List<Position> rekursionPositions = null;
	private void doRekursion(Board b, Position shiftPosition, List<Position> whereCanIGo) {
		List<Position> l = versucheRekursion(b, shiftPosition, whereCanIGo);
		if(l != null) {
			if(rekursionPositions == null || rekursionPositions.size() < l.size()) {
					rekursionPositions = l;
			}
		}
	}
	
	private List<Position> versucheRekursion(Board b, Position shiftPosition, List<Position> whereCanIGo) {
		Board shiftetBoard;
		List<Position> whereIcouldBeAfterShift = shiftPlayerPositions(shiftPosition, whereCanIGo);
		try {
			shiftetBoard = b.shift(shiftPosition, new BlockedCard());
		} catch (IllegalTurnException e) {
			//SHould never happen
			return null;
		}
		List<Position> fromWhereToTreasure = shiftetBoard.getPossiblePositionsFromPosition(shiftetBoard.getTreasurePosition());
		whereIcouldBeAfterShift.retainAll(fromWhereToTreasure);
		
		if(whereIcouldBeAfterShift.size() > 0) {
			return whereIcouldBeAfterShift;
		} else {
			return null;
		}
	}
	
	@Override
	public String getName() {
		return "Burning Seahorse";
	}

}
