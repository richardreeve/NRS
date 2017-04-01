/* $Id: Matrix.java,v 1.2 2005/05/09 22:07:01 hlrossano Exp $ */

package nrs.tracker.sticktrack;

import java.lang.ArithmeticException;
import java.util.LinkedList;
import java.io.BufferedReader;
import java.io.FileReader;

/**
*	@author Hugo Rosano
*/

public final class Matrix
{
double [][] matrix;
	
	public Matrix(){
		matrix = new double[0][0];
	}

	public Matrix(int r, int c){
		matrix = new double[r][c];
	}

	public Matrix(double[][] matrix){
		this.matrix = matrix;
	}

	public Matrix(java.util.LinkedList list){
		matrix = new double[list.size()][((LinkedList)list.getFirst()).size()];
		LinkedList cols;
		int r = 0, c = 0;
		java.util.Iterator it = list.iterator(), itc;
		while(it.hasNext()){
			cols = (LinkedList) it.next();
			itc = cols.iterator();
			c = 0;
			while(itc.hasNext())
				matrix[r][c++] = ((Double) itc.next()).doubleValue();
			r++;
		}
	}

	public void clear(){
		matrix = new double[0][0];
	}

	public boolean isEmpty(){
		return  matrix.length == 0;
	}

	public Matrix(double [] matrix, int r, int c){
		r = (r>0)?r:1;
		c = (c>0)?c:1;
		this.matrix = new double[r][c];
		int coun=0;
		for(int i=0;i<r;i++){
			for(int j=0;j<c;j++)
				this.matrix[i][j] = matrix[coun++];
		}
	}

	public int rows(){
		return matrix.length;}

	public int cols(){
		return matrix[0].length;}

	public double[][] getValues(){
		return matrix;}


	public void set(int r, int c, double val){
		matrix[r][c] = val;}

	public Matrix getCol(int col){
		double [][] res = new double[rows()][1];
		for(int i=0;i<rows();i++)
			res[i][0] = matrix[i][col];
		return new Matrix(res);
	}

	public Matrix getRow(int row){
		double [][] res = new double[1][cols()];
		res[0] = matrix[row];
		return new Matrix(res);
	}

	public Matrix sumCols(){
		double [][] res = new double[rows()][1];
		double temp;
		for(int i=0;i<rows();i++){
			temp = 0;
			for(int j=0;j<cols();j++)
				temp += matrix[i][j];
			res[i][0] = temp;
		}
		return new Matrix(res);
	}

	public Matrix sumRows(){
		double [][] res = new double[1][cols()];
		double temp;
		for(int j=0;j<cols();j++){
			temp = 0;
			for(int i=0;i<rows();i++)
				temp += matrix[i][j];
			res[0][j] = temp;
		}
		return new Matrix(res);
	}

	public Matrix inverse(){
		return adjoint().div(det());
	}

	public Matrix reshape(){
		double [][] res = new double[1][rows()*cols()];
		int coun=0;
		for(int i=0;i<rows();i++){
			for(int j=0;j<cols();j++)
				res[0][coun++] = this.matrix[i][j];
		}
		return new Matrix(res);
	}

	public double det(){
		double ans = 0;
		int size = matrix.length;
		if(matrix.length == 1)
			return matrix[0][0];
		if(matrix.length == 2)
			return matrix[0][0]*matrix[1][1] - matrix[0][1]*matrix[1][0];
		for(int j=0; j < size; j++)
			ans += matrix[0][j] * sign(0,j) * innerMat(0,j).det();
		return ans;
	}
	
	public Matrix div(double val){
		double [][] res = new double[rows()][cols()];
		for(int i=0;i<rows();i++){
			for(int j=0;j<cols();j++)
				res[i][j] = matrix[i][j]/val;
		}
		return new Matrix(res);
	}

	public Matrix adjoint(){
		double[][] res = new double[cols()][rows()];
		for(int i=0;i<rows();i++){
			for(int j=0;j<cols();j++)
				res[j][i] = cofactor(i,j);
		}
		return new Matrix(res);
	}

	public double cofactor(int r, int c){
		return  innerMat(r,c).det() * sign(r,c) ;
	}

	public Matrix transpose(){
		double [][] res = new double[cols()][rows()];
		for(int i=0;i<rows();i++){
			for(int j=0;j<cols();j++)
				res[j][i] = matrix[i][j];
		}
		return new Matrix(res);
	}

	public Matrix glueRight(Matrix mat2){
		double [][] res = new double[rows()][cols() + mat2.cols()];
		double [][] mat = mat2.getValues();
		for(int i=0;i<rows();i++){
			for(int j=0;j<cols();j++)
				res[i][j] = matrix[i][j];
		}
		for(int i=0;i<rows();i++){
			for(int j=0;j<mat2.cols();j++)
				res[i][j+cols()] = mat[i][j];
		}
		return new Matrix(res);
	}

	public Matrix mul(Matrix matrix2){
		double [][] mat = matrix2.getValues();
		double [][] res = new double[rows()][matrix2.cols()];
		if( cols() == matrix2.rows() ){
			double sum;
			for(int i=0;i<rows();i++){
				for(int j=0;j<matrix2.cols();j++){
					sum =0;
					for(int k=0;k<cols();k++)
						sum += matrix[i][k]*mat[k][j];
					res[i][j]=sum;
				}
			}
		}
		else
			throw new ArithmeticException("Matrix sizes not compatible." + this +" != "+matrix2);
		return new Matrix(res);
	}

	public Matrix arrayMul(Matrix matrix2){
		double [][] mat = matrix2.getValues();
		double [][] res = this.getValues();
		if(rows() == matrix2.rows() && cols() == matrix2.cols()){
			for(int i=0;i<rows();i++){
				for(int j=0;j<cols();j++)
					res[i][j] *= mat[i][j];
			}
		}
		else
			throw new ArithmeticException("Matrix sizes not compatible." + this +" != "+matrix2);
		return new Matrix(res);
	}

	public Matrix repeatRight(int num){
		double [][] mat = this.getValues();
		double [][] res = new double[rows()][cols()*num];
		int coun=0;
		for(int i=0;i<rows();i++){
			for(int k=0;k<num;k++){
				for(int j=0;j<cols();j++){
					res[i][coun++] = mat[i][j];
				}
			}
			coun=0;
		}
		return new Matrix(res);
	}

	public Matrix repeatDown(int num){
		double [][] mat = this.getValues();
		double [][] res = new double[rows()*num][cols()];
		int coun=0;
		for(int j=0;j<cols();j++){
			for(int k=0;k<num;k++){
				for(int i=0;i<rows();i++){
					res[coun++][j] = mat[i][j];
				}
			}
			coun=0;
		}
		return new Matrix(res);
	}

	public Matrix innerMat(int r, int c){
		double [][] res = new double[rows()-1][cols()-1];
		int ti,tj;
		for(int i=0;i<rows();i++){
			for(int j=0;j<cols();j++){
				if(i!= r && j != c){
					ti = (i>r)? i-1:i;
					tj = (j>c)? j-1:j;
					res[ti][tj] = matrix[i][j];
				}
			}
		}
		return new Matrix(res);
	}

	private int sign(int r, int c){
		int sign = -1;
		if( (r%2 == 0 && c%2 ==0) || (r%2 == 1 && c%2 ==1) )
			sign = 1;
		return sign;
	}

	public String toFile(String name){
		String temp = name + "\t= [\n";
		for(int i=0;i<rows();i++){
			temp += "";
			for (int j=0;j<cols();j++ ){
				temp += matrix[i][j] +"\t";
			}
			temp += ";\n";
		}
		temp += "];\n";
		return temp;
	}

	public String toString(){
		return "Matrix "+rows()+"x"+cols()+"\n";
	}

	public static Matrix getMatrixFromFile(String _fileName){
		double [][] res = new double[0][0];
		LinkedList _list = new LinkedList();
		LinkedList cols;
		try{
			BufferedReader readin = new BufferedReader(new FileReader(_fileName));
			readin.readLine();
			String temp = "", coords[];
			while( (temp = readin.readLine()) != null  && !temp.equals("];") ){
				coords = temp.split("\t");
				cols = new LinkedList();
				for(int i=0; i<coords.length-1; i++)
					cols.add(new Double(coords[i]));
				_list.add(cols);
			}
			res = new Matrix(_list).getValues();
			readin.close();
		}catch(Exception ex){
			System.out.println("Error while reading matrices\n"+ex);}
		return new Matrix(res);
	}

};