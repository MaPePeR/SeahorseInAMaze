package assessment;

import ourGenerated.Board;
import ourGenerated.Position;

public class Strategie implements IStrategie {

	@Override
	public High bewerte(Board board) {
		Assessment as=new Assessment(board);
		//Boolean Array, True = Position hat schatz.
		boolean t[][]=as.findTreasures();		
		//Wo kann ich hingehen
		boolean[][] iGo=as.whereICanGo();
		//Von wo aus komm ich zum schatz
		boolean[][] tWay=as.whereToGo(board.getTreasurePosition());
		//Abstand zu Treasure von jeder Position
		int [][]tWeights=as.weights(board.getTreasurePosition());//28-100
		
		//Entfernung zu wegen die zum schatz fuehren
		int [][]tWayWeigths=as.nearTheWay(tWay, 1); //1 = umgebung die gewertet werden soll
		//10*(a+b)
		int[][]tadd=Assessmentfield.add(tWayWeigths, tWeights, 10);
		int[][]tposible=Assessmentfield.mult(iGo,tadd);
		High h=Assessmentfield.findHigh(tposible);
		
		
		//Wieviel Felder kann ich erreichen
		int fields=Assessmentfield.count(iGo);
		
		//Wieviele Schaetze kann ich erreichen
		int schaetze=Assessmentfield.count(Assessmentfield.and(iGo,t));
		
		h.value+=fields+10*schaetze;
		
		Position gegner=board.getSpielerPositions().values().iterator().next();
		
		boolean[][] gway=as.whereToGo(gegner);
		int gegnerSchaetze=Assessmentfield.count(Assessmentfield.and(gway,t));
		h.value=h.value*10+(24-gegnerSchaetze);
		return h;
	}
	
	
	public static High bewerte(Board board,int nah, int wegnah,int schaetze,int bewegung,int[] gegner) {
		
		Assessment as=new Assessment(board);
		//Boolean Array, True = Position hat schatz.
		boolean t[][]=as.findTreasures();		
		//Wo kann ich hingehen
		boolean[][] iGo=as.whereICanGo();
		//Von wo aus komm ich zum schatz
		boolean[][] tWay=as.whereToGo(board.getTreasurePosition());
		//Abstand zu Treasure von jeder Position
		int [][]tWeights=as.weights(board.getTreasurePosition());//28-100		
		//Entfernung zu wegen die zum schatz fuehren
		int [][]tWayWeights=as.nearTheWay(tWay, 1); //1 = umgebung die gewertet werden soll
		//10*(a+b)
		
		
		
		Assessmentfield.increase(tWeights, nah);
		Assessmentfield.increase(tWayWeights, wegnah);
		
		//Wieviel Felder kann ich erreichen
		int fields=Assessmentfield.count(iGo);		
		//Wieviele Schaetze kann ich erreichen
		int schaetze2=Assessmentfield.count(Assessmentfield.and(iGo,t));
		
		
		
		int[][]tadd=Assessmentfield.add(tWayWeights, tWeights);
		int[][]tposible=Assessmentfield.mult(iGo,tadd);
		High h=Assessmentfield.findHigh(tposible);
		
		

		
		h.value+=fields*bewegung+schaetze2*schaetze;
		
		
		
		int i=0;
		//bitte ueberabeiten wenn fehler
		for(Position pGegner:board.getSpielerPositions().values())
		{
			if(pGegner!=board.getMyPosition())
			{
				boolean[][] gway=as.whereToGo(pGegner);
				int gegnerSchaetze=Assessmentfield.count(Assessmentfield.and(gway,t));	
			
				h.value+=gegner[i]*(24-gegnerSchaetze);
				++i;
			}
		}
		return h;
	}
	
	
}
