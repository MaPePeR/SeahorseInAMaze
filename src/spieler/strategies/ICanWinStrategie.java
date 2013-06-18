package spieler.strategies;

import ourGenerated.Board;
import assessment.Assessment;
import assessment.Assessmentfield;
import assessment.High;
import assessment.IStrategie;
import assessment.Strategie;

public class ICanWinStrategie implements IStrategie {

	@Override
	public High bewerte(Board board) {
		int gegner[]={1,1,1};//mit spieler anzahl vergleichen
		return Strategie.bewerte(board, 100, 10, 0, 5, gegner);
	}

}
