/* $Id: Grid.java,v 1.2 2005/04/29 11:21:03 hlrossano Exp $ */
package nrs.tracker.sticktrack;

//import java.io.BufferedReader;
//import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import javax.swing.JOptionPane;
import java.awt.image.BufferedImage;
import nrs.tracker.stickgui.ImageData;
import java.util.Iterator;

public class Grid 
{

	private Group gridMarks = new Group();
	private Matrix weightX = new Matrix();
	private Matrix weightY = new Matrix();
	private final int FUNC_SIZE = 9;

	public Grid(){}

	public Grid(Matrix weights){
		weightX = weights.getCol(0);
		weightY = weights.getCol(1);
	}

	public void export(String fileName){
		if(!gridMarks.isEmpty())
			gridMarks.toFile(fileName);
	}

	public Matrix getWx(){
		return weightX;
	}

	public Matrix getWy(){
		return weightY;
	}

	public void setGridMarks(Group grid){
		gridMarks = grid;
	}

	public boolean hasMarks(){
		return !gridMarks.isEmpty();
	}

	public Group getMarks(){
		return gridMarks;
	}

	public void saveWeights(String fileName){
		if(!weightX.isEmpty() && !weightY.isEmpty()){
			try{
				BufferedWriter pal = new BufferedWriter(new FileWriter(fileName));
				pal.write(weightX.glueRight(weightY).toFile("wweights"));
				pal.write("%% phi = [1 x y x*y x^2 y^2 x^2*y x*y^2 x^2*y^2]'\n");
				pal.close();
			}catch(java.io.IOException e){System.out.println("Problems while saving Group!!!");}
			return;
		}
		JOptionPane.showMessageDialog(null, "There is no calibration data to save", "Save Problem", JOptionPane.OK_OPTION);
	}

	public Matrix calibrate(Group refs){
		if(!gridMarks.isEmpty() && (refs.size() == gridMarks.size()) ){
			Matrix _refs = refs.toMatrix();
			Matrix _image = gridMarks.toMatrix();
			weightX = getWeight(_refs.getRow(0), _image);
			weightY = getWeight(_refs.getRow(1), _image);
			gridMarks.clear();
			return weightX.glueRight(weightY);
		}
		return new Matrix();
	}
	
	private Matrix getWeight(Matrix realCoord, Matrix image){
		Matrix phi = phi_function(image);
		try{
			Matrix res = phi.mul(phi.transpose()).inverse().mul(realCoord.repeatDown(phi.rows()).arrayMul(phi).sumCols());
			return res;
		}catch(ArithmeticException e){System.out.println(e);}
		return new Matrix(1,1);
	}

	private Matrix phi_function(Matrix image){
		double [] xs = image.getRow(0).getValues()[0];
		double [] ys = image.getRow(1).getValues()[0];
		double [][] res = new double[FUNC_SIZE][xs.length];
		for(int j=0;j<xs.length;j++){
			res[0][j] = 1;
			res[1][j] = xs[j];
			res[2][j] = ys[j];
			res[3][j] = xs[j]*ys[j];
			res[4][j] = xs[j]*xs[j];
			res[5][j] = ys[j]*ys[j];
			res[6][j] = xs[j]*xs[j]*ys[j];
			res[7][j] = xs[j]*ys[j]*ys[j];
			res[8][j] = xs[j]*xs[j]*ys[j]*ys[j];
		}
		return new Matrix(res);
	}

	public Vector2D transform(Vector2D vec){
		if(!weightX.isEmpty() && !weightY.isEmpty()){
			double x = vec.getX(), y = vec.getY();
			double [] wx = weightX.transpose().getValues()[0];
			double [] wy = weightY.transpose().getValues()[0];
			return  new Vector2D(wx[0] + wx[1]*x + wx[2]*y + wx[3]*x*y + wx[4]*x*x + wx[5]*y*y + wx[6]*x*x*y + wx[7]*x*y*y + wx[8]*x*x*y*y,
					  			 wy[0] + wy[1]*x + wy[2]*y + wy[3]*x*y + wy[4]*x*x + wy[5]*y*y + wy[6]*x*x*y + wy[7]*x*y*y + wy[8]*x*x*y*y);
		}
		return new Vector2D();
	}

	public Group transform(Group group){
		Group res = new Group();
		Iterator it = group.iterator();
		while(it.hasNext())
			res.add( transform( (Vector2D) it.next() ) );
		return res;
	}

	public CompoundGroup transform(CompoundGroup cg){
		CompoundGroup res = new CompoundGroup();
		Iterator it = cg.iterator();
		while(it.hasNext())
			res.add( transform( (Group) it.next() ) );
		return res;
	}

	public ImageData transformImage(ImageData image, int scale, int offset){
		ImageData img = new ImageData(new BufferedImage(1200,1200,BufferedImage.TYPE_INT_RGB));
		Vector2D temp;
		for (int i=0;i<image.getWidth();i++){
			for (int j=0;j<image.getHeight();j++){
				temp = transform(new Vector2D(i,j));
				img.setColour((int)(temp.getX()*scale + offset),(int)(temp.getY()*scale + offset), image.getColour(i,j));
			}
		}
		return img;
	}

};