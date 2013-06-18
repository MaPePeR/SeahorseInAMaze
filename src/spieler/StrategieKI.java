package spieler;

import generated.MoveMessageType;

import java.util.Map;

import client.types.IllegalTurnException;

import assessment.High;
import assessment.IStrategie;

import ourGenerated.Board;
import ourGenerated.Card;
import ourGenerated.Position;
import spieler.strategies.DefaultStrategie;
import spieler.strategies.ICanWinStrategie;
import spieler.strategies.PreventEnemyFromWinningStrategie;

public class StrategieKI extends Spieler {

	
	@Override
	public MoveMessageType doTurn(Board bt, Map<Integer, Integer> idHasNTreasuresleft) {
		IStrategie strat;
		currentMaxHigh = new High(0,0, Integer.MIN_VALUE);
		/*if(idHasNTreasuresleft.get(this.id) == 1) {
			//Das Spiel endet gleich
			strat = new ICanWinStrategie();
		} else if(idHasNTreasuresleft.values().contains(1)) {
			//Jemand anderes braucht nur noch einen Schatz
			strat = new PreventEnemyFromWinningStrategie();
		} else {
			strat = new DefaultStrategie();
		}*/
		
		strat=new DefaultStrategie();
		Card c = bt.getShiftCard();
		for(int rotationCount = 0; rotationCount<4; ++rotationCount) {
			for(int x = 5; x >= 0; x-=2) {
				versuche(strat, bt, x, 0, c, rotationCount);
				versuche(strat, bt, x, 6, c, rotationCount);
			}
			for(int y = 5; y >= 0; y-=2) {
				versuche(strat, bt, 0, y, c, rotationCount);
				versuche(strat, bt, 6, y, c, rotationCount);
			}
			c.turnCounterClockwise(1);
		}
		
		c.turnCounterClockwise(currentMaxRotationCount);
		MoveMessageType move = new MoveMessageType();
		move.setNewPinPos(currentMaxHigh.pos.getPositionType());
		move.setShiftCard(c.getCardType());
		move.setShiftPosition(new Position(currentMaxX, currentMaxY).getPositionType());
		return move;
	}
	
	High currentMaxHigh = new High(0,0, Integer.MIN_VALUE);
	int currentMaxX, currentMaxY, currentMaxRotationCount;
	private void versuche(IStrategie strat, Board bt, int x, int y, Card c, int rotationCount) {
		try {
			High h = strat.bewerte(bt.shift(new Position(x,y), c));
			if(h.value > currentMaxHigh.value) {
				currentMaxHigh = h;
				currentMaxX=x;
				currentMaxY=y;
				currentMaxRotationCount = rotationCount;
			}
		} catch (IllegalTurnException e) {
			System.out.println("IllegalTurn");
		}
		
	}

	@Override
	public String getName() {
		return "Strategic Seahorse";
	}

}
