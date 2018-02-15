import java.util.*;
import java.awt.*;
import javax.swing.*;
public class Splot extends JPanel{
	//tool used for me to debug machine learning algorithms
	private String hTitle, vTitle, title, type;
	private double[] x, y;
	Splot(String type, String title, String hTitle, String vTitle, double[] x, double[] y){
		this.type = type;										//hist line or scatter plot
		this.x = x;												//x and y coordinates
		this.y = y;
		this.hTitle = hTitle;
		this.vTitle = vTitle;									//titles
		this.title = title;
		if(type.equals("line") || type.equals("hist"))			//these graph types have uniform intervals of x in order
			Arrays.sort(x);
		if(type.equals("hist")){
			double[] tmpX = new double[x.length+1];
			double[] tmpY = new double[y.length+1];
			for(int i = 0; i < x.length; i++){
				tmpX[i] = x[i];
				tmpY[i] = y[i];
			}
			tmpX[tmpX.length-1] = x[tmpX.length-2]*2 - x[tmpX.length-3];	//adding extra entry hack for te histogram representation
			tmpY[tmpY.length-1] = -1e17;
			this.x = tmpX;
			this.y = tmpY;
		}
	}
	private void plot(double minX, double maxX, double interval, Graphics g){
		double maxY = y[0], minY = y[0];
		final int MARGIN = 30;												//margin creates whitespace for the relative diagram
		int height = this.getHeight();
		int width = this.getWidth();
		g.setColor(new Color(255,255,255));
		g.fillRect(0,0,width,height);
		g.setColor(new Color(0,0,0));	
		g.drawLine(MARGIN,MARGIN,MARGIN,height-MARGIN);
		g.drawLine(MARGIN,height-MARGIN,width-MARGIN,height-MARGIN);
		int counter = 0;
		height -= (MARGIN*2);
		width -= (MARGIN*2);
		for(int i = 0; i < y.length; i++){									//find ymin and ymax for scaling
			if(y[i] < -1e16)			//invalid, used for the histogram
				continue;
			if(y[i] > maxY)
				maxY = y[i];
			else if(y[i] < minY)
				minY = y[i];
		}
		if(type.toLowerCase().equals("scat")){
			for(int i = 0; i < x.length; i++){
				g.setColor(new Color(130,130,130));
				int x1, y1;
				x1 = (int) Math.round(((x[i]-minX)/(maxX-minX))*(double)width);
				y1 = (int) Math.round(((y[i]-minY)/(maxY-minY))*(double)height);
				g.fillOval(x1-3+MARGIN,height-y1-3+MARGIN,6,6);
			}
		}
		else{
			for(double i = minX; i < maxX; i += interval){
				if(type.toLowerCase().equals("line")){
					g.setColor(new Color(130,130,130));
					int x1,y1,x2,y2;
					x1 = (int) Math.round(((i-minX)/(maxX-minX))*(double)width);
					x2 = (int) Math.round(((i+interval-minX)/(maxX-minX))*(double)width);
					
					y1 = (int) Math.round(((y[counter]-minY)/(maxY-minY))*(double)height);
					y2 = (int) Math.round(((y[counter+1]-minY)/(maxY-minY))*(double)height);

					g.drawLine(x1+MARGIN,height-y1+MARGIN,x2+MARGIN,height-y2+MARGIN);
				}
				else if(type.toLowerCase().equals("hist")){
					g.setColor(new Color(130,130,130));
					int x1,y1,x2,y2;
					x1 = (int) Math.round(((i-minX)/(maxX-minX))*(double)width);
					x2 = (int) Math.round(((i+interval-minX)/(maxX-minX))*(double)width);
					
					y1 = (int) Math.round(((y[counter]-minY)/(maxY-minY))*(double)height);

					g.fillRect(x1+MARGIN,height-y1+MARGIN,x2-x1,y1);
				}
				counter++;
			}
		}
	}
	public void paintComponent(Graphics g){
		plot(x[0],x[x.length-1],x[1]-x[0],g);							//intervals, x scale is found here
	}
	private Splot(){}

	//================ DEBUG ===========================
	
	public static void main(String[] args){
		double[] quarter = {1,2,3,4,5,6,7,8,9,10,11};
		double[] revenue = {100,22,300,290,250,200,190,250,400,155,55};
		Splot sales = new Splot("scat","overview","quarter","revenue",quarter,revenue);
		Splot sales1 = new Splot("line","overview","quarter","revenue",quarter,revenue);
		Splot sales2 = new Splot("hist","overview","quarter","revenue",quarter,revenue);
		JFrame window = new JFrame("plot test");
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setSize(800,800);
		window.add(sales);
		window.setVisible(true);
		try{Thread.sleep(2000);}catch(Exception e){}
		window.remove(sales);
		window.add(sales1);
		window.setVisible(true);
		try{Thread.sleep(2000);}catch(Exception e){}
		window.remove(sales1);
		window.add(sales2);
		window.setVisible(true);
	}
}