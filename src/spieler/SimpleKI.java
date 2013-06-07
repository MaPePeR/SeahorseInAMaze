package spieler;

import generated.BoardType;
import generated.MoveMessageType;
import generated.TreasuresToGoType;
import ourGenerated.Board;
import ourGenerated.Card;

public class SimpleKI extends Spieler {

	

	public String name = "Simple";
	
	public Board shift(Board b, Card c, int i, int j){
		
		
		
		return b;
	}
	
	//(0,1)(0,3)(0,5)
	//(1,0)(1,6)
	//(3,0)(3,6)
	//(5,0)(5,6)
	//(6,1)(6,3)(6,5)

	@Override
	public MoveMessageType doTurn(BoardType bt, TreasuresToGoType ttg) {
		Board original = new Board(bt,ttg);
		Board modified = new Board(bt,ttg);
		Card card = original.getShiftCard();
		for(int i=0;i<original.getCards().length;i+=6){
			for(int j=1;j<original.getCards()[i].length;j+=2){
				//einschieben an den Positionen(0,1)(0,3)(0,5) und (6,1)(6,3)(6,5)
				modified=shift(original,card,i,j);
				
				//bearbeiten TODO
				
				//einschieben an den Positionen (1,0)(1,6), (3,0)(3,6) und (5,0)(5,6)
				modified=shift(original,card,j,i);
			}
			
		}
		
		
		
		
		return null;
	}

	@Override
	public String getName() {
		return this.name;
	}

}
