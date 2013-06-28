package ourGenerated;

import generated.CardType;
import generated.TreasureType;

import java.util.ArrayList;

public class Card {

	public boolean openings[] = new boolean[4];
	// 0=oben, 1=rechts, 2=unten, 3=links
	protected TreasureType treasure;
	protected ArrayList<Integer> players;

	public ArrayList<Integer> getPlayers() {
		return this.players;
	}

	public Card(CardType c) {
		this.players = (ArrayList<Integer>) c.getPin().getPlayerID();
		this.treasure = c.getTreasure();
		this.openings[0] = c.getOpenings().isTop();
		this.openings[1] = c.getOpenings().isRight();
		this.openings[2] = c.getOpenings().isBottom();
		this.openings[3] = c.getOpenings().isLeft();

	}

	public CardType getCardType() {
		CardType card = new CardType();

		CardType.Openings open = new CardType.Openings();
		open.setTop(this.openings[0]);
		open.setRight(this.openings[1]);
		open.setBottom(this.openings[2]);
		open.setLeft(this.openings[3]);
		card.setOpenings(open);
		// CardType.Pin pin = new CardType.Pin();
		// for (int i = 0; i < players.size(); i++)
		// pin.getPlayerID().add(players.get(i));
		card.setPin(new CardType.Pin());
		// card.getPin().getPlayerID().addAll(players);
		card.setTreasure(this.treasure);
		return card;
	}

	public TreasureType getTreasure() {
		return this.treasure;
	}

	public void setTreasure(TreasureType treasure) {
		this.treasure = treasure;
	}

	public void turnCounterClockwise(int times) {
		for (int j = 0; j < times; j++) {
			boolean top = this.openings[0];
			for (int i = 1; i < this.openings.length; i++) {
				this.openings[i - 1] = this.openings[i];
			}
			this.openings[3] = top;
		}
	}

	public Card(Card c) {
		// openings
		for (int i = 0; i < this.openings.length; i++) {
			this.openings[i] = c.openings[i];
		}
		// players
		this.players = new ArrayList<Integer>();
		for (Integer i : c.players) {
			this.players.add(i);
		}
		// treasure
		this.treasure = c.getTreasure();
		// das ist zwar kein Copy, treasure wird aber sowieso nicht
		// veraendert... FIXME
	}

	@Override
	public boolean equals(Object o) {
		boolean equal = false;
		if ((o instanceof Card)) {
			equal = true;
			Card other = (Card) o;
			for (int i = 0; i < this.openings.length; i++) {
				if (this.openings[i] != other.openings[i]) {
					return false;
				}
			}

			for (Integer player : this.players) {
				if (!other.players.contains(player)) {
					return false;
				}
			}

		}

		return equal;
	}

	/**
	 * Gibt an, ob es sich um die gleiche (gedrehte) Kate handelt
	 * 
	 * @param other
	 * @return
	 */
	public boolean isSame(Card other) {
		Card c = new Card(other);
		for (int i = 0; i < this.openings.length; i++) {
			c.turnCounterClockwise(1);
			if (this.equals(c)) {
				return true;
			}
		}

		return false;
	}

	// 0 1 2 3 4 5 6 7 8 9 A B C D E F
	// U+255x ═ ║ ╒ ╓ ╔ ╕ ╖ ╗ ╘ ╙ ╚ ╛ ╜ ╝ ╞ ╟
	// U+256x ╠ ╡ ╢ ╣ ╤ ╥ ╦ ╧ ╨ ╩ ╪ ╫ ╬
	public char getChar() {
		if (this.openings[0] && this.openings[2]) {// Oben & unten offen
			if (this.openings[1] && this.openings[3]) {// Alle offen{
				return '╬';
			} else if (this.openings[1]) { // Oben, rechts, unten offen
				return '╠';
			} else if (this.openings[3]) {
				return '╣';
			} else {
				return '║';
			}
		} else if (this.openings[0]) {// Oben aber nicht unten offen{
			if (this.openings[1]) {// Oben und rechts, nicht unten
				if (this.openings[3]) { // oben rechts links
					return '╩';
				} else { // oben rechts
					return '╚';
				}
			} else { // Oben, nicht rechts, nicht unten
				if (this.openings[3]) {// oben, links nicht rechts nicht unten
					return '╝';
				} else {
					return '╨';
				}
			}
		} else if (this.openings[2]) { // Unten aber nicht oben offen
			if (this.openings[1]) {// unten und rechts, nicht unten
				if (this.openings[3]) { // unten rechts links
					return '╦';
				} else { // unten rechts
					return '╔';
				}
			} else { // unten, nicht rechts, nicht unten
				if (this.openings[3]) {// unten, links nicht rechts nicht unten
					return '╗';
				} else {
					return '╥';
				}
			}
		} else { // Weder oben noch unten offen
			if (this.openings[1] && this.openings[3]) { // Rechts und links
				return '═';
			} else if (this.openings[1]) {
				return '╞';
			} else if (this.openings[3]) {
				return '╡';
			} else { // Garkeine öffnung
				return 'X';
			}
		}
	}

	@Override
	public String toString() {
		return "" + this.getChar() + " " + this.treasure + " " + this.players.toString();
	}

	public boolean[] getOpenings() {
		return this.openings;
	}
}
