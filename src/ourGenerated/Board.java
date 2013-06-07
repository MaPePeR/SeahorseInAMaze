package ourGenerated;

import generated.BoardType;
import generated.CardType;
import generated.PositionType;
import generated.TreasuresToGoType;

import java.util.List;

public class Board {

	private Card cards[][];
	protected Card shiftCard;// die einzufuegende Karte
	protected TreasuresToGoType ttg;// der als naechstes zu erreichende Schatz
									// (enthaelt auch die PlayerId)
	protected PositionType myPosition;//aktuelle Position des Spielers
	protected PositionType forbidden;//die Position, an die nicht gelegt werden darf
	protected PositionType treasure;//position des zu findenden schatzes

	public PositionType myPosition() {
		return myPosition;
	}

	public void setCard(int i, int j, Card c) {
		if(i>0 && j>0 && i<cards.length && j<cards[i].length)
			cards[i][j] = c;
	}

	public Card[][] getCards() {
		return cards;
	}

	public void setShiftCard(Card c) {
		shiftCard = c;
	}

	public Card getShiftCard() {
		return shiftCard;
	}

	public Board(BoardType b, TreasuresToGoType ttg) {
		myPosition = new PositionType();
		this.ttg = ttg;
		int i = 0, j = 0;
		boolean foundMe=false, foundTreasure=false;
		for (BoardType.Row row : b.getRow()) {
			for (CardType c : row.getCol()) {
				cards[i][j] = new Card(c);
				if (c.getPin().getPlayerID().contains(ttg.getPlayer())) {
					myPosition.setRow(i);
					myPosition.setCol(j);
					foundMe=true;
				}
				if (c.getTreasure().equals(ttg.getTreasures())) {
					treasure.setRow(i);
					treasure.setCol(j);
					foundTreasure=true;
				}
				j++;
			}
			i++;
		}
		
		shiftCard = new Card(b.getShiftCard());
		if(!(foundMe||foundTreasure)){
			System.out.println("Ungueltiges Brett");
		}

	}

	public BoardType getBoardType() {
		BoardType b = new BoardType();
		b.setShiftCard(shiftCard.getCardType());
		List<BoardType.Row> rows = b.getRow();// erzeugt das Row-Objekt...
		for (int i = 0; i < cards.length; i++) {
			for (int j = 0; j < cards[i].length; j++) {
				rows.get(i).getCol().add(cards[i][j].getCardType());
			}
		}

		return b;
	}

	public TreasuresToGoType getTtg() {
		return this.ttg;
	}

	public void setTtg(TreasuresToGoType ttg) {
		this.ttg = ttg;
	}

	public PositionType getMyPosition() {
		return this.myPosition;
	}

	public void setMyPosition(PositionType myPosition) {
		this.myPosition = myPosition;
	}

	public PositionType getForbidden() {
		return this.forbidden;
	}

	public void setForbidden(PositionType forbidden) {
		this.forbidden = forbidden;
	}

	public PositionType getTreasure() {
		return this.treasure;
	}

	public void setTreasure(PositionType treasure) {
		this.treasure = treasure;
	}

	public void setCards(Card[][] cards) {
		this.cards = cards;
	}

}
