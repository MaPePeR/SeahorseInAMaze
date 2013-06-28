package spieler.strategies;

import ourGenerated.Board;
import assessment.High;
import assessment.IStrategie;
import assessment.Strategie;

public class NearWayDestroyStrategie implements IStrategie {

	@Override
	public High bewerte(Board b) {
		int gegner[] = { 7, 7, 7 };
		return Strategie.bewerte(b, 3, 12, 4, 2, gegner);
	}
}
