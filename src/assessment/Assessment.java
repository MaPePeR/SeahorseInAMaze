package assessment;


import ourGenerated.*;
import generated.PositionType;
import generated.TreasureType;

public class Assessment {
	
	Board board;
	
	
	
	public Assessment(Board karte) {		
		board=karte;	
	}	
	
	public int[][] weights(Position pos)//28-100
	{
		int x=pos.x;
		int y=pos.y;
		int[][] weights=new int[7][7];
		int iw;
		int x_;
		for(int y_=0;y_<7;y_++){
			iw=100-(y_-x)*(y_-x);
			for(x_=0;x_<7;x_++)
				weights[y_][x_]=iw-(x_-y)*(x_-y);
		}
		return weights;
	}
	
	public boolean[][] findTreasures()
	{
		Card[][] Cards=board.getCards();
		boolean karten[][]=new boolean[7][7];
		for(int y=0;y<7;y++)
			for(int x=0;x<7;x++)
				if(Cards[y][x].getTreasure() != null)
					karten[y][x]=true;				
		return karten;
	}
	
	
	public boolean[][] whereToGo(Position pos)
	{
		boolean[][] marked=new boolean[7][7];
		whereToGo(board.getCards(),pos,marked);
		return marked;
	}
	
	
	public boolean[][] whereICanGo()
	{
		boolean[][] marked=new boolean[7][7];
		whereToGo(board.getCards(),board.getMyPosition(),marked);
		return marked;
	}
	
	private void whereToGo(Card[][] Cards,Position pos,boolean marked[][])
	{
		int x=pos.x;
		int y=pos.y;
		marked[x][y]=true;
		if(x<8&&Cards[x+1][y].getOpenings()[0]&&!marked[x+1][y]){
			x++;		
			whereToGo(Cards,pos,marked);
			x--;
		}
		if(x>=0&&Cards[x-1][y].getOpenings()[2]&&!marked[x][y]){
			x--;		
			whereToGo(Cards,pos,marked);
			x++;
		}
			
		if(y<8&&Cards[x][y+1].getOpenings()[1]&&!marked[x][y+1]){
			y++;		
			whereToGo(Cards,pos,marked);
			y--;
		}
				
		if(y>=0&&Cards[x][y-1].getOpenings()[3]&&!marked[x][y-1])
		{
			y--;		
			whereToGo(Cards,pos,marked);
			x++;
		}
	}
	
	
	//vorsicht sehr aufwendig
    public int[][] nearTheWay(boolean[][] marked,int umfeld){
    	Position pos=board.getMyPosition();
    	int stx,endx,sty,endy;
    	if(pos.x<umfeld)
    		stx=0;
    	else
    		stx=pos.x-umfeld;
    	if(pos.y<umfeld)
    		sty=0;
    	else
    		sty=pos.y-umfeld;
    	if(pos.x+umfeld<6)
    		endx=pos.x+umfeld;
    	else
    		endx=6;
    	if(pos.y+umfeld<6)
    		endy=pos.y+umfeld;
    	else
    		endy=6;
    	int[][] result=new int[7][7];
    	for(;stx<endx;stx++)
    		for(;sty<endy;sty++)
    			if(marked[stx][sty])
    				result=Assessmentfield.higherField(weights(new Position(stx,sty)),result);
    	
    	
    	return result;
    }
	
	
	
}

