import java.util.*;
import javax.imageio.*;
import java.io.*;
import java.awt.*;
import java.awt.image.*;
//binary decisiong tree model with random forest implementation
public class BinaryClassification{

	public static void main(String[] args){
		String currPath = new File().getAbsolutePath();
		processRaw(currPath, currPath+"/unprocessed_target", currPath+"/processed_target");
		processRaw(currPath, currPath+"/unprocessed_notarget", currPath+"/processed_notarget");
	}
}