import java.io.*;
import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.imageio.*;
import java.awt.image.*;

public class ROI{
	//region of interest
	//take the raw images and grow the palette tree
	//identify key colors in the palette and find region of interest, using export function we can export cropped images to destination folder

	static ColorNode redRoot = null;					//these contain the palettes
	static ColorNode blueRoot = null;
	static ColorNode greenRoot = null;

	public static void insert(String color, ColorNode focus, ColorNode entry){
		if(color.equals("red")){
			if(redRoot == null)
				redRoot = entry;
			else{
				if(entry.red == focus.red)
					return;
				if(entry.red > focus.red){
					if(focus.right == null)
						focus.right = entry;
					else
						insert(color, focus.right, entry);
				}
				else{
					if(focus.left == null)
						focus.left = entry;
					else
						insert(color, focus.left, entry);
				}
			}
		}
		else if(color.equals("green")){
			if(greenRoot == null)
				greenRoot = entry;
			else{
				if(entry.green == focus.green)
					return;
				if(entry.green > focus.green){
					if(focus.right == null)
						focus.right = entry;
					else
						insert(color, focus.right, entry);
				}
				else{
					if(focus.left == null)
						focus.left = entry;
					else
						insert(color, focus.left, entry);
				}
			}
		}
		else if(color.equals("blue")){
			if(blueRoot == null)
				blueRoot = entry;
			else{
				if(entry.blue == focus.blue)
					return;
				if(entry.blue > focus.blue){
					if(focus.right == null)
						focus.right = entry;
					else
						insert(color, focus.right, entry);
				}
				else{
					if(focus.left == null)
						focus.left = entry;
					else
						insert(color, focus.left, entry);
				}
			}
		}
	}

	public static boolean queryUtil(ColorNode focus, int red, int green, int blue){
		if(red != -1){
			if(focus != null){
				if(focus.red == red)
					return true;
				else if(focus.red > red)
					return queryUtil(focus.left,red,green,blue);
				else
					return queryUtil(focus.right,red,green,blue);
			}
		}
		else if(green != -1){
			if(focus != null){
				if(focus.green == green)
					return true;
				else if(focus.red > green)
					return queryUtil(focus.left,red,green,blue);
				else
					return queryUtil(focus.right,red,green,blue);
			}
		}
		else{
			if(focus != null){
				if(focus.blue == blue)
					return true;
				else if(focus.blue > blue)
					return queryUtil(focus.left,red,green,blue);
				else
					return queryUtil(focus.right,red,green,blue);
			}
		}
		return false;
	}

	public static boolean query(int red, int green, int blue){
		return queryUtil(redRoot, red,-1,-1) && queryUtil(greenRoot,-1,green,-1) && queryUtil(blueRoot,-1,-1,blue);
	}

	public static class ColorNode{
		int red, green, blue;
		ColorNode left, right;
		ColorNode(int red, int green, int blue){
			left = null;
			right = null;
			this.red = red;
			this.green = green;
			this.blue = blue;
			insert("red",redRoot,this);
			insert("green",greenRoot,this);						//add to al	l the trees for later
			insert("blue",blueRoot,this);
		}
		private ColorNode(){}
	}

	//=========== above code is used for color searching

	static Node root = null;
	static final int DEPTH = 40;
	static final double ACCURACY = 0.95;		//stop branching once accuracy reaches 85%
	static final double PRECISION = 0.85;
	static final int TOTAL_COLORS = 32;
	static final int DIVISOR = 256 / TOTAL_COLORS;

	public static class Picture{
		boolean target;
		boolean[][][] colors;
		int height, width;
		Picture(boolean[][][] colors, int height, int width, boolean target){
			this.colors = colors;
			this.height = height;
			this.width = width;
			this.target = target;
		}	
	}

	public static class Node{
		ArrayList<Picture> gallery;
		int rKey, gKey, bKey, level;		//these are the keys that split the node
		Node left, right;
		Node(){
			rKey = -1;
			gKey = -1;
			bKey = -1;
			left = null;
			right = null;
			level = 0;
			gallery = new ArrayList<Picture>();
		}
	}

	public static void growTree(Node focus){
		int targetCount = 0;
		double percentageBias = (double)targetCount/(double)focus.gallery.size();
		if(focus == null || focus.level >= DEPTH || focus.gallery.size() == 0)
			return;
		for(Picture p: focus.gallery)
			if(p.target)
				targetCount++;
		if(percentageBias > ACCURACY)
			return;
		if(targetCount == 0 || targetCount == focus.gallery.size())
			return;
		int rKey = -1, gKey = -1, bKey = -1;
		double maxQ = -1; 	
		ArrayList<Picture> left = null;
		ArrayList<Picture> right = null;
		for(int r = 0; r < TOTAL_COLORS; r++){
			for(int g = 0; g < TOTAL_COLORS; g++){
				for(int b = 0; b < TOTAL_COLORS; b++){
					ArrayList<Picture> hasColor = new ArrayList<Picture>();
					ArrayList<Picture> noColor = new ArrayList<Picture>();		//contains color and does not contain color
					int hasColorTarget = 0;
					int hasColorNoTarget = 0;
					int noColorTarget = 0;
					int noColorNoTarget = 0;
					for(Picture p: focus.gallery){
						if(p.colors[r][g][b]){
							if(p.target)
								hasColorTarget++;
							else
								hasColorNoTarget++;
							hasColor.add(p);
						}
						else{
							if(p.target)
								noColorTarget++;
							else
								noColorNoTarget++;
							noColor.add(p);
						}
					}
					if(hasColor.size() == 0 || noColor.size() == 0)
						continue;
					double hasColorGini = Math.pow((double)hasColorTarget/(double)hasColor.size(),2) + 
										  Math.pow((double)hasColorNoTarget/(double)hasColor.size(),2);
					double noColorGini  = Math.pow((double)noColorTarget/(double)noColor.size(),2) + 
										  Math.pow((double)noColorNoTarget/(double)noColor.size(),2);
					double weightGini   = hasColorGini * ((double)hasColor.size()/(double)focus.gallery.size()) + 
									 	  noColorGini * ((double)noColor.size()/(double)focus.gallery.size());
					if(weightGini > maxQ){
						maxQ = weightGini;
						rKey = r;
						gKey = g;
						bKey = b;
						left = new ArrayList<Picture>(noColor);
						right = new ArrayList<Picture>(hasColor);
					}
					percentageBias = 0;
					targetCount = 0;				//reinit
					for(Picture p: right)
						if(p.target)
							targetCount++;
					percentageBias = (double)targetCount / (double)(left.size() + right.size());
					if(!query(rKey,gKey,bKey) && percentageBias >= PRECISION)
						new ColorNode(rKey,gKey,bKey);
				}
			}
		}
		if(rKey != -1){
			focus.left = new Node();
			focus.right = new Node();
			focus.left.gallery = new ArrayList<Picture>(left);
			focus.right.gallery = new ArrayList<Picture>(right);
			focus.left.level = focus.level + 1;
			focus.right.level = focus.level + 1;
			focus.rKey = rKey;
			focus.gKey = gKey;
			focus.bKey = bKey;	
			growTree(focus.left);
			growTree(focus.right);
			growTree(focus.left);			
			growTree(focus.right);
		}
	}

	public static void processRaw(String source, boolean target){		
	//predict and take region of interest
		try{
			File folder = new File(source);
			File[] images = folder.listFiles();
			for(int k = 0; k < images.length; k++){
				String name = images[k].getName();
				if(root == null)
					root = new Node();
				BufferedImage picture = ImageIO.read(new File(source+"/"+name));
				boolean[][][] colors = new boolean[TOTAL_COLORS][TOTAL_COLORS][TOTAL_COLORS];
				for(int i = 0; i < picture.getWidth(); i++){
					for(int j = 0; j < picture.getHeight(); j++){
						Color c = new Color(picture.getRGB(i,j));
						colors[c.getRed()/DIVISOR][c.getGreen()/DIVISOR][c.getBlue()/DIVISOR] = true;
					}
				}
				root.gallery.add(new Picture(colors, picture.getHeight(), picture.getWidth(), target));
			}
			growTree(root);
		} catch (IOException e){
			e.printStackTrace();
		}
	}

	public static void exportResults(String source, String destination){
		File srcDir = new File(source);
		File[] files = srcDir.listFiles();
		for(int k = 0; k < files.length; k++){
			BufferedImage img = null;
			try{
				//attempt to get the image
				img = ImageIO.read(files[k]);
			} catch (IOException e){
				System.out.println("problem reading from file");
				return;	//finish
			}
			int maxR = -1;
			int maxL = 2000000;
			int maxU = 2000000;
			int maxD = -1;
			for(int i = 0; i < img.getWidth(); i++){
				for(int j = 0; j < img.getHeight(); j++){
					if(query(new Color(img.getRGB(i,j)).getRed()/DIVISOR, new Color(img.getRGB(i,j)).getGreen()/DIVISOR, new Color(img.getRGB(i,j)).getBlue()/DIVISOR)){
						if(i > maxR)
							maxR = i;
						if(i < maxL)
							maxL = i;
						if(j > maxD)
							maxD = j;
						if(j < maxU)
							maxU = j;
					}
				}
			}
			//crop the image and add to destination
			BufferedImage croppedImage = new BufferedImage(maxR - maxL, maxD - maxU, BufferedImage.TYPE_INT_RGB);
			Graphics g = croppedImage.getGraphics();
			int x = 0,y = 0;			//for output
			for(int i = maxL; i <= maxR; i++){		//for input
				y = 0;
				for(int j = maxU; j <= maxD; j++){
					g.setColor(new Color(img.getRGB(i,j)));
					g.fillRect(x,y,1,1);
					y++;
				}
				x++;
			}
			String writeName = files[k].getName();
			String extension = writeName.substring(writeName.indexOf(".")+1);
			try{
				ImageIO.write( croppedImage, extension, new File(destination+"/"+writeName));
			} catch (Exception e){
				System.out.println("issue writing image to file using ImageIO");
			}
		}
	}
	
	public static void main(String[] args){
		String currDir = new File("").getAbsolutePath();

		processRaw(currDir+"/unprocessed_target",true);
		processRaw(currDir+"/unprocessed_notarget",false);
		//exportResults(currDir+"/test",currDir+"/output");
	}

	public static class Result extends JPanel{
		BufferedImage test;
		private Result(){}
		Result(BufferedImage test){
			this.test = test;
		}
		public void paintComponent(Graphics g){
			int maxR = -1;
			int maxL = 2000000;
			int maxU = 2000000;
			int maxD = -1;
			for(int i = 0; i < test.getWidth(); i+=2){
				for(int j = 0; j < test.getHeight(); j+=2){
					g.setColor(new Color(test.getRGB(i,j)));
					if(query(new Color(test.getRGB(i,j)).getRed()/8, new Color(test.getRGB(i,j)).getGreen()/8, new Color(test.getRGB(i,j)).getBlue()/8)){
						if(i > maxR)
							maxR = i;
						if(i < maxL)
							maxL = i;
						if(j > maxD)
							maxD = j;
						if(j < maxU)
							maxU = j;
						//ENABLE THIS TO SHOW PALETTE SELECTION g.setColor(new Color(0,10,210));
						g.fillRect(i/2,j/2,2,2);   
					}
					else
						g.fillRect(i/2,j/2,2,2);
				}
			}
			g.setColor(new Color(230,0,0));
			g.drawLine(maxR/2,maxU/2,maxR/2,maxD/2);
			g.drawLine(maxL/2,maxU/2,maxL/2,maxD/2);
			g.drawLine(maxR/2,maxU/2,maxL/2,maxU/2);
			g.drawLine(maxR/2,maxD/2,maxL/2,maxD/2);
		}
	}

	public static void debug(String dir){
		try{
			BufferedImage test = ImageIO.read(new File(dir));			//name of the file please
			JFrame window = new JFrame("Region of Interest Test");
			window.setSize(test.getWidth(),test.getHeight());
			window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

			JPanel imagePane = new Result(test);
			imagePane.setPreferredSize(new Dimension(test.getWidth(),test.getHeight()));

			JScrollPane scrollPane = new JScrollPane(imagePane);
			scrollPane.getViewport().setPreferredSize(new Dimension(test.getWidth(),test.getHeight()));
			scrollPane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);

			window.add(scrollPane);
			window.pack();
			window.setVisible(true);
		} catch (IOException e){
			e.printStackTrace();
		}
	}
}