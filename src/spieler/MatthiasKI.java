package spieler;

import generated.MoveMessageType;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import ourGenerated.Board;
import ourGenerated.Card;
import ourGenerated.Position;
import client.types.IllegalTurnException;

public class MatthiasKI extends Spieler {

	private class Bewertung implements Comparable<Bewertung> {
		public final boolean canFindTreasure;

		public final int howManyWallsBlockMyWayToTreasure;

		public final int myNetworkSize;

		public final double averageEnemyMovability;

		public Bewertung(boolean canFindTreasure, int howManyWallsBlockMyWay, double averageEnemyMovability,
				int myNetworkSize) {
			this.canFindTreasure = canFindTreasure;
			this.howManyWallsBlockMyWayToTreasure = howManyWallsBlockMyWay;
			this.averageEnemyMovability = averageEnemyMovability;
			this.myNetworkSize = myNetworkSize;
		}

		@Override
		public int compareTo(Bewertung o) {
			if (o == null) {
				return 1;
			}
			if (this.canFindTreasure == o.canFindTreasure) {
				if (this.myNetworkSize == o.myNetworkSize) {
					if (this.howManyWallsBlockMyWayToTreasure == o.howManyWallsBlockMyWayToTreasure) {
						if (Math.abs(this.averageEnemyMovability - o.averageEnemyMovability) <= 0.05) {
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
				return this.canFindTreasure ? 1 : -1;
			}
		}

		@Override
		public String toString() {
			return String.format("Treasure: %d\nNetworkSize: %d\nWalls: %d\nMovability: %f\n", this.canFindTreasure ? 1
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
				this.versuche(bt, x, 0, c, rotationCount);
				this.versuche(bt, x, 6, c, rotationCount);
			}
			for (int y = 5; y >= 0; y -= 2) {
				this.versuche(bt, 0, y, c, rotationCount);
				this.versuche(bt, 6, y, c, rotationCount);
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

	private void versuche(Board bt, int x, int y, Card c, int rotationCount) {
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

		boolean canFindTreasure = treasurePos != null && whereCanIGo.contains(treasurePos);

		if (!canFindTreasure && treasurePos != null) {
			walls = shiftetBoard.howManyWallsBlockMyWayTo(treasurePos);
			for (int i = 5; i >= 0; i -= 2) {
				walls[0][i] = walls[6][i] = Math.min(walls[0][i], walls[6][i]);
				walls[i][0] = walls[i][6] = Math.min(walls[i][0], walls[i][6]);
			}
		} else {
			walls = new int[7][7];
		}

		List<Position> movePositions = new LinkedList<Position>();
		int howManyWallsBlockMyWayToTreasure = 0;

		if (!canFindTreasure) {
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
		} else {
			movePositions.add(treasurePos);
		}

		int playersInMyNetwork = 0;
		for (Position p : whereCanIGo) {
			playersInMyNetwork += shiftetBoard.getCards()[p.y][p.x].getPlayers().size();
		}
		int myNetworkSize = whereCanIGo.size() / playersInMyNetwork;

		int enemysCanMoveTiles = 0;
		for (Position pos : shiftetBoard.getSpielerPositions().values()) {
			enemysCanMoveTiles += shiftetBoard.getPossiblePositionsFromPosition(pos).size();
		}
		double averageEnemyMovability = ((double) enemysCanMoveTiles) / shiftetBoard.getSpielerPositions().size();

		Bewertung b = new Bewertung(canFindTreasure, howManyWallsBlockMyWayToTreasure, averageEnemyMovability,
				myNetworkSize);
		if (b.compareTo(this.currentMaxBewertung) > 0) {
			// System.out.println(b);
			this.currentMaxZuege.clear();
		}
		if (b.compareTo(this.currentMaxBewertung) >= 0) {
			this.currentMaxBewertung = b;
			this.currentMaxZuege.add(new Zug(shiftPosition, rotationCount, movePositions));
		}
	}

	@Override
	public String getName() {
		return "Nasses Seepferdchen";
	}

}
