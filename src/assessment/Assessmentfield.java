package assessment;

import ourGenerated.Position;

public class Assessmentfield {
	public static int[][] and(boolean a[][],boolean b[][])
	{
		int x;
		int c[][]=new int[7][7];
		for(int y=0;y<7;y++)
			for(x=0;x<7;x++)
				if(a[y][x]&&b[y][x])
					c[y][x]=1;
		return c;
	}
	
	public static int[][] mult(boolean a[][],int b[][])
	{ return mult(a,b,1);}
	
	public static int[][] mult(boolean a[][],int b[][],int alpha)
	{
		int x;
		int c[][]=new int[7][7];
		for(int y=0;y<7;y++)
			for(x=0;x<7;x++)
				if(a[y][x])
					c[y][x]=alpha*b[y][x];
		return c;
	}
	
	public static int[][] mult(int a[][],int b[][],int alpha)
	{
		int x;
		int c[][]=new int[7][7];
		for(int y=0;y<7;y++)
			for(x=0;x<7;x++)
				c[y][x]=alpha*(b[y][x]*a[y][x]);
		return c;
	}
	
	
	public static int[][] add(int a[][],int b[][])
	{		
		int x;
		int c[][]=new int[7][7];
		for(int y=0;y<7;y++)
			for(x=0;x<7;x++)
				c[y][x]=b[y][x]+a[y][x];
		return c;
	}
	
	/**
	 * returns an array, so that alpha*(a+b)
	 * @param a
	 * @param b
	 * @param alpha
	 * @return
	 */
	public static int[][] add(int a[][],int b[][],int alpha)
	{
		int x;
		int c[][]=new int[7][7];
		for(int y=0;y<7;y++)
			for(x=0;x<7;x++)
				c[y][x]=alpha*(b[y][x]+a[y][x]);
		return c;
	}
	


	
	
	
	public static int count(boolean a[][])
	{
		int x;
		int c=0;
		for(int y=0;y<7;y++)
			for(x=0;x<7;x++)
				if(a[y][x])
					c++;
		return c;
	}
	
	public static int count(int a[][])
	{
		int x;
		int c=0;
		for(int y=0;y<7;y++)
			for(x=0;x<7;x++)
				if(a[y][x]>0)
					c++;
		return c;
	}
	
	public static int[][] higherField(int a[][],int b[][])
	{
		int x;
		int c[][]=new int[7][7];
		for(int y=0;y<7;y++)
			for(x=0;x<7;x++){
				if(a[y][x]>b[y][x])
					c[y][x]=a[y][x];
				else
					c[y][x]=b[y][x];
			}
		return c;
	}
	
	public static High findHigh(int[][] field)
	{
		High high = new High();
		int x;
		for(int y=0;y<7;y++)
			for(x=0;x<7;x++)
				if(field[y][x]>high.value)
					high=new High(x,y,field[y][x]);
		return high;
	}
	
	public static void increase(int[][] m,int i)
	{
		for(int j=0;j<7;j++)
			for(int k=0;k<7;k++)
				m[j][k]*=i;
	}

}
