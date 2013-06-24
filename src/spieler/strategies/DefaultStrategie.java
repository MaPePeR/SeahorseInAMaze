package spieler.strategies;

import ourGenerated.Board;
import assessment.High;
import assessment.IStrategie;
import assessment.Strategie;

public class DefaultStrategie implements IStrategie {

	@Override
	public High bewerte(Board b) {
		int gegner[]={1,1,1};
		return Strategie.bewerte(b, 15, 8, 4, 2, gegner);
	}

}
