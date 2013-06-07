package ourGenerated;

import generated.CardType;
import generated.TreasureType;

import java.util.ArrayList;

public class Card {

	protected boolean openings[] = new boolean[4];
	// 0=oben, 1=rechts, 2=unten, 3=links
	protected TreasureType treasure;
	protected ArrayList<Integer> players;

	public ArrayList<Integer> getPlayers(){
		return players;
	}
	public Card(CardType c) {
		players =(ArrayList<Integer>)c.getPin().getPlayerID();
		treasure = c.getTreasure();
		openings[0] = c.getOpenings().isTop();
		openings[1] = c.getOpenings().isRight();
		openings[2] = c.getOpenings().isBottom();
		openings[3] = c.getOpenings().isLeft();

	}

	public CardType getCardType() {
		CardType card = new CardType();
		
		CardType.Openings open = new CardType.Openings();
		open.setTop(openings[0]);
		open.setRight(openings[1]);
		open.setBottom(openings[2]);
		open.setLeft(openings[3]);
		card.setOpenings(open);
		CardType.Pin pin = new CardType.Pin();
		for (int i = 0; i < players.size(); i++)
			pin.getPlayerID().add(players.get(i));

		return card;
	}
	public TreasureType getTreasure() {
		return treasure;
	}
	public void setTreasure(TreasureType treasure) {
		this.treasure = treasure;
	}
	public void turnCounterClockwise(){
		boolean top = openings[0];
		for(int i=1;i<openings.length;i++)
			openings[i-1]=openings[i];
		openings[3]=top;
	}
}
