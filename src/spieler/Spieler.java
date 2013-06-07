package spieler;

import generated.BoardType;
import generated.MoveMessageType;
import generated.TreasuresToGoType;


public abstract class Spieler {
	public abstract MoveMessageType doTurn(BoardType bt, TreasuresToGoType ttg);
	public abstract String getName();
	protected int id;
	
	
	public void setId(int id){
		this.id=id;
	}
	
	
}
