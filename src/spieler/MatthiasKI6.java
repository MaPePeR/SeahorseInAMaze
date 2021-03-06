package spieler;

import generated.MoveMessageType;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import ourGenerated.Board;
import ourGenerated.Card;
import ourGenerated.Position;
import client.types.IllegalTurnException;

public class MatthiasKI6 extends Spieler {

	private class Bewertung implements Comparable<Bewertung> {
		public final boolean canFindTreasure;

		public final int howManyWallsBlockMyWayToTreasure;

		public final int myNetworkSize;

		public final int averageEnemyMovability;
		
		public final int wallsNearTreasure;
		
		public final int absDistance;

		public Bewertung(boolean canFindTreasure, int howManyWallsBlockMyWay, int averageEnemyMovability,
				int myNetworkSize, int wallsNearTreasure, int absDistance) {
			this.canFindTreasure = canFindTreasure;
			this.howManyWallsBlockMyWayToTreasure = howManyWallsBlockMyWay;
			this.averageEnemyMovability = averageEnemyMovability;
			this.myNetworkSize = myNetworkSize;
			this.wallsNearTreasure = wallsNearTreasure;
			this.absDistance = absDistance;
		}

		@Override
		public int compareTo(Bewertung o) {
			if (o == null) {
				return 1;
			}
			if (this.canFindTreasure == o.canFindTreasure) {
				if (this.myNetworkSize == o.myNetworkSize) {
					if (this.howManyWallsBlockMyWayToTreasure == o.howManyWallsBlockMyWayToTreasure) {
						if(this.wallsNearTreasure == o.wallsNearTreasure) {
							if(this.absDistance == o.absDistance) {
								if (this.averageEnemyMovability == o.averageEnemyMovability) {
									return 0;
								} else {
									return this.averageEnemyMovability < o.averageEnemyMovability ? 1 : -1;
								}
							} else {
								return this.absDistance < o.absDistance ? 1 : -1;
							}
						} else {
							return this.wallsNearTreasure < o.wallsNearTreasure ? 1 : -1;
						}
					} else {
						return this.howManyWallsBlockMyWayToTreasure < o.howManyWallsBlockMyWayToTreasure ? 1 : -1;
					}
				} else {
					return this.myNetworkSize > o.myNetworkSize ? 1 : -1;
				}
			} else {
				return this.canFindTreasure ? 1 : -1;
			}
		}

		@Override
		public String toString() {
			return String.format("Treasure: %d\nNetworkSize: %d\nWalls: %d\nMovability: %d\n", this.canFindTreasure ? 1
					: 0, this.myNetworkSize, this.howManyWallsBlockMyWayToTreasure, this.averageEnemyMovability);
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
		
		int absDistance = Integer.MAX_VALUE;
		if(treasurePos != null) {
			List<Position> oldMovePos = movePositions;
			movePositions = new LinkedList<Position>();
			
			for(Position p: oldMovePos) {
				int sqd = Math.abs(p.x-treasurePos.x) + Math.abs(p.y-treasurePos.y);
				if (sqd < absDistance) {
					movePositions.clear();
					absDistance = sqd;
				}
				if (sqd <= absDistance) {
					movePositions.add(p);
				}
			}
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

		int enemysCanMoveTiles = 0;
		if(idHasNTreasuresleft.size() == 2) { //Nur ein Gegner
			// Summiere auf, wieviele Felder mein Gegner nach einem Shift erreichen können.
			enemysCanMoveTiles  = EnemyCanMoveRekursion.maxMovementSum(shiftetBoard, idHasNTreasuresleft.keySet(), this.id);
		} else {
			// Summiere auf, wieviele Felder meine Gegner erreichen können.
			for (Entry<Integer, Position> entry : shiftetBoard.getSpielerPositions().entrySet()) {
				if (!idHasNTreasuresleft.keySet().contains(entry.getKey())) { 
					// Ist kein Aktiver Spieler
					continue;
				}
				if (entry.getKey() != this.id) {
					enemysCanMoveTiles += shiftetBoard.getPossiblePositionsFromPosition(entry.getValue()).size();
				}
			}
		}
		
		int wallsNearTreasure = 0;
		if(treasurePos != null) {
			wallsNearTreasure = countNearWalls(shiftetBoard, treasurePos);
		} else {
			wallsNearTreasure = Integer.MAX_VALUE;
		}
		
		

		Bewertung b = new Bewertung(canFindTreasure, howManyWallsBlockMyWayToTreasure, enemysCanMoveTiles,
				myNetworkSize, wallsNearTreasure, absDistance);

		// Aktuelle Bewertung ist besser. => Alte gute zuege verwerfen
		if (b.compareTo(this.currentMaxBewertung) > 0) {
			this.currentMaxZuege.clear();
		}
		if (b.compareTo(this.currentMaxBewertung) >= 0) {
			this.currentMaxBewertung = b;
			this.currentMaxZuege.add(new Zug(shiftPosition, rotationCount, movePositions));
		}
	}

	private int countNearWalls(Board b, Position p) {
		Card cards[][] = b.getCards();
		boolean cOpenings[] = b.getCards()[p.y][p.x].getOpenings();
		int walls[] = {Integer.MAX_VALUE,Integer.MAX_VALUE,Integer.MAX_VALUE,Integer.MAX_VALUE};
		if(cOpenings[0] && p.y > 0) { //Oben offen
			int left=0, right=0;
			for(int i=p.x; i > 0; i-- ) {
				if(cards[p.y - 1][i].getOpenings()[2]) {
					break;
				} else {
					left++;
				}
			}
			for(int i=p.x; i < 7; i++ ) {
				if(cards[p.y - 1][i].getOpenings()[2]) {
					break;
				} else {
					right++;
				}
			}
			walls[0] = Math.min(left, right);
		}
		
		if(cOpenings[2] && p.y < 6) { //Unten offen
			int left=0, right=0;
			for(int i=p.x; i > 0; i-- ) {
				if(cards[p.y + 1][i].getOpenings()[0]) {
					break;
				} else {
					left++;
				}
			}
			for(int i=p.x; i < 7; i++ ) {
				if(cards[p.y + 1][i].getOpenings()[0]) {
					break;
				} else {
					right++;
				}
			}
			walls[1] = Math.min(left, right);

		}
		
		if(cOpenings[1] && p.x < 6) { //Rechts offen
			int oben=0, unten=0;
			for(int i=p.y; i > 0; i-- ) {
				if(cards[i][p.x + 1].getOpenings()[3]) {
					break;
				} else {
					oben++;
				}
			}
			for(int i=p.x; i < 7; i++ ) {
				if(cards[i][p.x + 1].getOpenings()[3]) {
					break;
				} else {
					unten++;
				}
			}
			walls[2] = Math.min(oben, unten);

		}
		
		if(cOpenings[3] && p.x > 0) { //Rechts offen
			int oben=0, unten=0;
			for(int i=p.y; i > 0; i-- ) {
				if(cards[i][p.x - 1].getOpenings()[1]) {
					break;
				} else {
					oben++;
				}
			}
			for(int i=p.x; i < 7; i++ ) {
				if(cards[i][p.x - 1].getOpenings()[1]) {
					break;
				} else {
					unten++;
				}
			}
			walls[3] = Math.min(oben, unten);
		}
		return Math.min(Math.min(walls[0], walls[1]), Math.min(walls[2], walls[3]));
	}
	
	@Override
	public String getName() {
		return "Hot Seahorse";
	}
	
	private static class EnemyCanMoveRekursion {

		private static int maxMovement = 0;
		private static int maxMovementSum(Board b, Set<Integer> activePlayers, int ignoreID) {
			maxMovement = 0;
			
			Card c = b.getShiftCard();
			for (int rotationCount = 0; rotationCount < 4; ++rotationCount) {
				for (int x = 5; x >= 0; x -= 2) {
					EnemyCanMoveRekursion.versuche(b,activePlayers, ignoreID, new Position(x, 0), c);
					EnemyCanMoveRekursion.versuche(b,activePlayers, ignoreID, new Position(x, 6), c);
				}
				for (int y = 5; y >= 0; y -= 2) {
					EnemyCanMoveRekursion.versuche(b,activePlayers, ignoreID, new Position(0, y), c);
					EnemyCanMoveRekursion.versuche(b,activePlayers, ignoreID, new Position(6, y), c);
				}
				c.turnCounterClockwise(1);
			}
			
			return maxMovement;
		}
		private static void versuche(Board b, Set<Integer> activePlayers, int ignoreID, Position shiftPosition, Card c) {
			if(!b.isValidMove(shiftPosition, c)) return;
			Board shiftetBoard;
			try {
				shiftetBoard = b.shift(shiftPosition, c);
			} catch (IllegalTurnException e) {
				return;
			}
			
			int enemysCanMoveTiles = 0;
			for (Entry<Integer, Position> entry : shiftetBoard.getSpielerPositions().entrySet()) {
				if (!activePlayers.contains(entry.getKey())) { 
					// Ist kein Aktiver Spieler
					continue;
				}
				if (entry.getKey() != ignoreID) {
					enemysCanMoveTiles += b.getPossiblePositionsFromPosition(entry.getValue()).size();
				}
			}
			maxMovement = Math.max(maxMovement, enemysCanMoveTiles);
		}
	}

}
