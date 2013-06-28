package ourGenerated;

import generated.CardType;
import generated.CardType.Openings;

public class BlockedCard extends Card {
	public BlockedCard() {
		super(getBlockedCardType());
	}
	
	public static CardType getBlockedCardType() {
		CardType c = new CardType();
		Openings o = new Openings();
		o.setBottom(false);
		o.setTop(false);
		o.setRight(false);
		o.setLeft(false);
		c.setOpenings(o);
		return c;
	}
}
