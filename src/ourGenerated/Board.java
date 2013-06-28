package ourGenerated;

import generated.BoardType;
import generated.CardType;
import generated.TreasureType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.fusesource.jansi.AnsiConsole;
import org.fusesource.jansi.AnsiRenderer;

import client.types.IllegalTurnException;

public class Board {

	private Card cards[][];
	protected Card shiftCard;// die einzufuegende Karte
	protected TreasureType treasure;// der als naechstes zu erreichende Schatz
	protected int id;
	// (enthaelt auch die PlayerId)
	protected Position myPosition;// aktuelle Position des Spielers
	protected Position forbidden;// die Position, an die nicht gelegt werden
	// darf
	protected Position treasurePosition;// position des zu findenden

	// schatzes

	public Position myPosition() {
		return this.myPosition;
	}

	public void setCard(int i, int j, Card c) {
		if (i > 0 && j > 0 && i < this.cards.length && j < this.cards[i].length) {
			this.cards[i][j] = c;
		}
	}

	public Card[][] getCards() {
		return this.cards;
	}

	public Card getShiftCard() {
		return this.shiftCard;
	}

	protected Map<Integer, Position> spielerPositions = new TreeMap<Integer, Position>();

	public Map<Integer, Position> getSpielerPositions() {
		return this.spielerPositions;
	}

	public Board(BoardType b, TreasureType t, int id) {
		this.forbidden = b.getForbidden() != null ? new Position(b.getForbidden()) : null;
		this.cards = new Card[7][7];
		this.treasure = t;
		this.id = id;
		int y = 0, x = 0;
		boolean foundMe = false, foundTreasure = false;
		for (BoardType.Row row : b.getRow()) {
			x = 0;
			for (CardType c : row.getCol()) {
				this.cards[y][x] = new Card(c);
				Position p = new Position(x, y);
				for (int playerID : c.getPin().getPlayerID()) {
					this.spielerPositions.put(playerID, p);
					if (playerID == id) {
						this.myPosition = p;
						foundMe = true;
					}
				}
				if (t.equals(c.getTreasure())) {
					this.treasurePosition = p;
					foundTreasure = true;
				}
				x++;
			}
			y++;
		}

		this.shiftCard = new Card(b.getShiftCard());
		if (!foundTreasure && this.treasure.equals(this.shiftCard.treasure)) {
			foundTreasure = true;
			this.treasurePosition = null;
		}

		if (!foundMe || !foundTreasure) {
			System.out.println("Ungueltiges Brett");
			throw new IllegalArgumentException("Something is wrong with that board...");
		}

	}

	public BoardType getBoardType() {
		BoardType b = new BoardType();
		b.setShiftCard(this.shiftCard.getCardType());
		List<BoardType.Row> rows = b.getRow();// erzeugt das Row-Objekt...
		for (int i = 0; i < this.cards.length; i++) {
			for (int j = 0; j < this.cards[i].length; j++) {
				rows.get(i).getCol().add(this.cards[i][j].getCardType());
			}
		}

		return b;
	}

	public TreasureType getTreasure() {
		return this.treasure;
	}

	public Position getMyPosition() {
		return this.myPosition;
	}

	public Position getForbidden() {
		return this.forbidden;
	}

	/**
	 * @returns <b>null</b> if the current Treasure is on the shift card.
	 */
	public Position getTreasurePosition() {
		return this.treasurePosition;
	}

	public List<Position> getPossiblePositionsFromPosition(Position position) {
		// canVisit is actually used as a Queue.
		int canVisit[] = new int[7 * 7];
		int canVisitSize = 0;
		int haveRevisited = 0;
		int currentIndex;
		canVisit[canVisitSize++] = position.y * 7 + position.x;
		int x, y;
		boolean[] currentCardOpenings;
		boolean[] visited = new boolean[7 * 7];
		visited[position.y * 7 + position.x] = true;

		while (haveRevisited < canVisitSize) {
			currentIndex = canVisit[haveRevisited++];
			x = currentIndex % 7;
			y = currentIndex / 7;
			currentCardOpenings = this.cards[y][x].openings;

			if (y > 0 && !visited[currentIndex - 7] && currentCardOpenings[0] && this.cards[y - 1][x].openings[2]) { // Oben
				canVisit[canVisitSize++] = (currentIndex - 7);
				visited[currentIndex - 7] = true;
			}

			if (x < 6 && !visited[currentIndex + 1] && currentCardOpenings[1] && this.cards[y][x + 1].openings[3]) { // Rechts
				canVisit[canVisitSize++] = (currentIndex + 1);
				visited[currentIndex + 1] = true;
			}

			if (y < 6 && !visited[currentIndex + 7] && currentCardOpenings[2] && this.cards[y + 1][x].openings[0]) { // Unten
				canVisit[canVisitSize++] = (currentIndex + 7);
				visited[currentIndex + 7] = true;
			}

			if (x > 0 && !visited[currentIndex - 1] && currentCardOpenings[3] && this.cards[y][x - 1].openings[1]) { // Links
				canVisit[canVisitSize++] = currentIndex - 1;
				visited[currentIndex - 1] = true;
			}
		}

		ArrayList<Position> list = new ArrayList<Position>(canVisitSize);
		for (int i = canVisitSize - 1; i >= 0; --i) {
			list.add(new Position(canVisit[i] % 7, canVisit[i] / 7));
		}
		return list;
	}

	public int[][] howManyWallsBlockMyWayTo(Position position) {
		// canVisit is actually used as a Queue.
		int walls[][] = new int[7][7];
		for (int y = 7 - 1; y >= 0; --y) {
			for (int x = 7 - 1; x >= 0; --x) {
				walls[y][x] = Integer.MAX_VALUE;
			}
		}
		int currentIndex = position.y * 7 + position.x;
		walls[position.y][position.x] = 0;
		int x, y;
		boolean[] currentCardOpenings;
		boolean[] visited = new boolean[7 * 7];
		int didVisitAllFromEndTo = visited.length - 1;
		int currentWalls;
		int currentWallsAdd;
		while (currentIndex >= 0) {
			x = currentIndex % 7;
			y = currentIndex / 7;
			currentWalls = walls[y][x];
			currentCardOpenings = this.cards[y][x].openings;

			if (y > 0) { // Oben
				currentWallsAdd = (currentCardOpenings[0] ? 0 : 1) + (this.cards[y - 1][x].openings[2] ? 0 : 1);
				if (walls[y - 1][x] > currentWalls + currentWallsAdd) {
					walls[y - 1][x] = currentWalls + currentWallsAdd;
				}
			}

			if (x < 7 - 1) { // Rechts
				currentWallsAdd = (currentCardOpenings[1] ? 0 : 1) + (this.cards[y][x + 1].openings[3] ? 0 : 1);
				if (walls[y][x + 1] > currentWalls + currentWallsAdd) {
					walls[y][x + 1] = currentWalls + currentWallsAdd;
				}
			}

			if (y < 7 - 1) { // Unten
				currentWallsAdd = (currentCardOpenings[2] ? 0 : 1) + (this.cards[y + 1][x].openings[0] ? 0 : 1);
				if (walls[y + 1][x] > currentWalls + currentWallsAdd) {
					walls[y + 1][x] = currentWalls + currentWallsAdd;
				}
			}

			if (x > 0) { // Links
				currentWallsAdd = (currentCardOpenings[3] ? 0 : 1) + (this.cards[y][x - 1].openings[1] ? 0 : 1);
				if (walls[y][x - 1] > currentWalls + currentWallsAdd) {
					walls[y][x - 1] = currentWalls + currentWallsAdd;
				}
			}
			visited[currentIndex] = true;
			currentIndex = -1;
			currentWalls = Integer.MAX_VALUE;

			while (didVisitAllFromEndTo >= 0 && visited[didVisitAllFromEndTo]) {
				--didVisitAllFromEndTo;
			}
			for (int i = didVisitAllFromEndTo; i >= 0; --i) {
				if (i >= 0 && !visited[i]) {
					if (walls[i / 7][i % 7] < currentWalls) {
						currentWalls = walls[i / 7][i % 7];
						currentIndex = i;
					}
				}
			}
		}
		return walls;
	}

	private void howManyWallsStraightLineRekursive(int[][] walls, int x, int y, int dx, int dy) {
		int newx = x + dx, newy = y + dy;
		if (0 > newx || newx > 6 || 0 > newy || newy > 6) {
			return;
		}
		boolean currentCardOpenings[] = this.cards[y][x].openings;
		if (dx != 0 && dy != 0) {
			walls[newy][newx] = walls[y][x]
					+ Math.min((currentCardOpenings[1 + dy] ? 0 : 1) + (this.cards[newy][x].openings[1 - dy] ? 0 : 1)
							+ (this.cards[newy][x].openings[2 - dx] ? 0 : 1)
							+ (this.cards[newy][newx].openings[2 + dx] ? 0 : 1), (currentCardOpenings[2 - dx] ? 0 : 1)
							+ (this.cards[y][newx].openings[2 + dx] ? 0 : 1)
							+ (this.cards[y][newx].openings[1 - dy] ? 0 : 1)
							+ (this.cards[newy][newx].openings[1 + dy] ? 0 : 1));
			this.howManyWallsStraightLineRekursive(walls, newx, newy, dx, 0); // Vertikal
			this.howManyWallsStraightLineRekursive(walls, newx, newy, 0, dy); // Horizontal
			this.howManyWallsStraightLineRekursive(walls, newx, newy, dx, dy);
		} else if (dy != 0) { // Vertikal
			walls[newy][newx] = walls[y][x] + (currentCardOpenings[1 + dy] ? 0 : 1)
					+ (this.cards[newy][x].openings[1 - dy] ? 0 : 1);
			this.howManyWallsStraightLineRekursive(walls, newx, newy, 0, dy);
		} else { // dx != 0 //Horizontal
			walls[newy][newx] = walls[y][x] + (currentCardOpenings[2 - dx] ? 0 : 1)
					+ (this.cards[y][newx].openings[2 + dx] ? 0 : 1);
			this.howManyWallsStraightLineRekursive(walls, newx, newy, dx, 0);
		}
	}

	public int[][] howManyWallsStraightLine(Position pos) {
		int[][] walls = new int[7][7];
		this.howManyWallsStraightLineRekursive(walls, pos.x, pos.y, 0, -1);
		this.howManyWallsStraightLineRekursive(walls, pos.x, pos.y, 1, 0);
		this.howManyWallsStraightLineRekursive(walls, pos.x, pos.y, 0, 1);
		this.howManyWallsStraightLineRekursive(walls, pos.x, pos.y, -1, 0);

		this.howManyWallsStraightLineRekursive(walls, pos.x, pos.y, 1, -1);
		this.howManyWallsStraightLineRekursive(walls, pos.x, pos.y, 1, 1);
		this.howManyWallsStraightLineRekursive(walls, pos.x, pos.y, -1, 1);
		this.howManyWallsStraightLineRekursive(walls, pos.x, pos.y, -1, -1);
		return walls;
	}

	public Board(Board b) {
		// cards kopieren
		this.cards = new Card[b.cards.length][b.cards[0].length];
		for (int i = 0; i < b.cards.length; i++) {
			for (int j = 0; j < b.cards[i].length; j++) {
				this.cards[i][j] = new Card(b.cards[i][j]);
			}
		}
		// shiftCard kopieren
		this.shiftCard = new Card(b.shiftCard);

		// forbidden-Position kopieren
		this.forbidden = b.forbidden;

		// myPosition kopieren
		this.myPosition = b.myPosition;

		// treasure-Position kopieren
		this.treasurePosition = b.treasurePosition;

		// treasure kopieren
		this.treasure = b.getTreasure();

		// ID
		this.id = b.id;
	}

	/**
	 * 
	 * @param p
	 *            Die Position, an der eingefuegt wird
	 * @param c
	 *            die Karte, die eingefuegt wird
	 * @return
	 */
	public Board shift(Position p, Card c) throws IllegalTurnException {
		if (!this.isValidMove(p, c, true)) {
			throw new IllegalTurnException("Es wurde kein gueltiger Zug gefunden");
		}
		Board newBoard = new Board(this);
		Card tmp = null;
		int start = 0, direction = 0;
		boolean vertikal = false;
		if (p.x == 0) {// Karte wird links eingefuegt
			tmp = newBoard.cards[p.y][6];// die rechteste Karte der Zeile
			start = 6;
			direction = -1;
			vertikal = false;
		} else if (p.x == 6) {// Karte wird rechts eingefuegt
			tmp = newBoard.cards[p.y][0];// die linkeste Karte der Zeile
			start = 0;
			direction = 1;
			vertikal = false;
		} else if (p.y == 0) {// karte wird oben eingefuegt
			tmp = newBoard.cards[6][p.x];// die letzte Karte der Spalte
			start = 6;
			direction = -1;
			vertikal = true;
		} else if (p.y == 6) { // karte wird unten eingefuegt
			tmp = newBoard.cards[0][p.x]; // oberste karte der Spalte
			start = 0;
			direction = 1;
			vertikal = true;
		}

		for (int i = start; i <= 6 && i >= 0 && i + direction <= 6 && i + direction >= 0; i += direction) {
			if (vertikal) {
				newBoard.cards[i][p.x] = newBoard.cards[i + direction][p.x];
			} else {
				newBoard.cards[p.y][i] = newBoard.cards[p.y][i + direction];
			}
		}
		newBoard.cards[p.y][p.x] = new Card(this.shiftCard);
		newBoard.shiftCard = tmp;

		// Die Rausgeschobenen kommen wieder aufs board.
		newBoard.cards[p.y][p.x].getPlayers().clear();
		newBoard.cards[p.y][p.x].getPlayers().addAll(newBoard.shiftCard.getPlayers());
		newBoard.shiftCard.getPlayers().clear();

		for (int spieler : this.spielerPositions.keySet()) {
			newBoard.spielerPositions.put(spieler, Board.shiftPlayerPosition(this.spielerPositions.get(spieler),
					vertikal ? p.x : p.y, direction, vertikal));
		}
		newBoard.myPosition = Board.shiftPlayerPosition(this.myPosition, vertikal ? p.x : p.y, direction, vertikal);
		newBoard.treasurePosition = Board.shiftCardPosition(this.treasurePosition, vertikal ? p.x : p.y, direction,
				vertikal);
		// TODO: shift my & treasure position
		if (vertikal) {
			newBoard.forbidden = new Position(p.x, (p.y + 6) % 12);
		} else {
			newBoard.forbidden = new Position((p.x + 6) % 12, p.y);
		}

		return newBoard;
	}

	public static Position shiftCardPosition(Position p, int pos, int direction, boolean vertikal) {
		if (p == null) { // Karte ist aktuelle shift-karte => sie wird an der
			// neuen stelle reingeschoben.
			if (vertikal) {
				if (direction > 0) {
					return new Position(0, pos);
				} else {
					return new Position(6, pos);
				}
			} else {
				if (direction > 0) {
					return new Position(pos, 0);
				} else {
					return new Position(pos, 6);
				}
			}
		} else {
			if (vertikal) {
				if (p.x == pos && (p.y + direction >= 7 || p.y + direction < 0)) { // Karte
																					// wird
																					// rausgeschoben
					return null;
				} else {
					return new Position(p.x, p.x == pos ? (7 + p.y - direction) % 7 : p.y);
				}
			} else {
				if (p.y == pos && (p.x + direction >= 7 || p.x + direction < 0)) { // Karte
																					// wird
																					// rausgeschoben
					return null;
				} else {
					return new Position(p.y == pos ? (7 + p.x - direction) % 7 : p.x, p.y);
				}
			}
		}
	}

	public boolean isValidMove(Position p, Card c) {
		return this.isValidMove(p, c, false);
	}

	public boolean isValidMove(Position p, Card c, boolean printError) {
		if (!c.isSame(this.shiftCard)) {
			if (printError) {
				System.err.println("Shiftcard is not the same.");
			}
			return false;
		}
		if (p.equals(this.forbidden)) {
			if (printError) {
				System.err.println("Forbidden Position used.");
			}
			return false;
		}

		return ((p.x % 6 == 0 && p.y % 2 == 1) || (p.y % 6 == 0 && p.x % 2 == 1));
	}

	public static Position shiftPlayerPosition(Position p, int pos, int direction, boolean vertical) {
		if (p == null) {
			throw new IllegalArgumentException("Player position can not be null");
		} else {
			if (vertical) {
				return new Position(p.x, p.x == pos ? (7 + p.y - direction) % 7 : p.y);
			} else {
				return new Position(p.y == pos ? (7 + p.x - direction) % 7 : p.x, p.y);
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Illegal Position: " + this.forbidden);
		sb.append('\n');
		for (int y = 0; y < 7; y++) {
			for (int x = 0; x < 7; x++) {
				sb.append(this.cards[y][x].getChar());
			}
			sb.append('\n');
		}
		return sb.toString();
	}

	public void outputPretty() {
		if (this.treasurePosition == null) {
			AnsiConsole.out
					.println(AnsiRenderer.render("Shiftcard: @|bg_blue,white " + this.shiftCard.getChar() + "|@"));
		} else {
			AnsiConsole.out.println("Shiftcard: " + this.shiftCard);
		}

		String style;
		for (int y = 0; y < 7; y++) {
			for (int x = 0; x < 7; x++) {
				style = "@|black";
				if (x == this.myPosition.x && y == this.myPosition.y) {
					style += ",yellow";
				}
				if (!(y % 2 == 1 || x % 2 == 1)) {
					style += ",negative_on";
				}
				if (this.treasurePosition != null && x == this.treasurePosition.x && y == this.treasurePosition.y) {
					style += ",blue";
				}
				if (this.forbidden != null && x == this.forbidden.x && y == this.forbidden.y) {
					style += ",bg_red";
				}
				AnsiConsole.out.print(AnsiRenderer.render(style + " " + this.cards[y][x].getChar() + "|@"));

			}
			AnsiConsole.out.println();
		}
	}

	public void clearForbidden() {
		this.forbidden = null;
	}

	public static Position shiftCardPosition(Position shiftPosition, Position treasurePosition) {
		boolean vertikal = false;
		int direction;
		if (shiftPosition.x == 0) {// Karte wird links eingefuegt
			direction = -1;
			vertikal = false;
		} else if (shiftPosition.x == 6) {// Karte wird rechts eingefuegt
			direction = 1;
			vertikal = false;
		} else if (shiftPosition.y == 0) {// karte wird oben eingefuegt
			direction = -1;
			vertikal = true;
		} else if (shiftPosition.y == 6) { // karte wird unten eingefuegt
			direction = 1;
			vertikal = true;
		} else {
			throw new IllegalArgumentException();
		}
		return Board.shiftCardPosition(treasurePosition, vertikal ? shiftPosition.x : shiftPosition.y, direction,
				vertikal);
	}
	public static Position shiftPlayerPosition(Position shiftPosition, Position playerPosition) {
		boolean vertikal = false;
		int direction;
		if (shiftPosition.x == 0) {// Karte wird links eingefuegt
			direction = -1;
			vertikal = false;
		} else if (shiftPosition.x == 6) {// Karte wird rechts eingefuegt
			direction = 1;
			vertikal = false;
		} else if (shiftPosition.y == 0) {// karte wird oben eingefuegt
			direction = -1;
			vertikal = true;
		} else if (shiftPosition.y == 6) { // karte wird unten eingefuegt
			direction = 1;
			vertikal = true;
		} else {
			throw new IllegalArgumentException();
		}
		return Board.shiftPlayerPosition(playerPosition, vertikal ? shiftPosition.x : shiftPosition.y, direction,
				vertikal);
	}

	public void setMyPosition(Position position) {
		this.myPosition = position;

	}
}
