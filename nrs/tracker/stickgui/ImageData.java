/* $Id: ImageData.java,v 1.2 2005/05/09 22:05:12 hlrossano Exp $ */
package nrs.tracker.stickgui;

import java.awt.image.*;
import nrs.tracker.palette.*;

/**
*	@author Hugo L. Rosano
*	@version 1.0
*	This class is for handling images on RGB format
*/
public class ImageData extends BufferedImage{
//this.getRaster().getMin()

	/** 
	*	Initialize class with a BufferedImage
	*	@param bimage is a {@link BufferedImage} with the image data
	*/
	public ImageData(BufferedImage bimage){
		super(bimage.getWidth(),bimage.getHeight(),bimage.getType());
		this.setData(bimage.getRaster());
	}
	/** Set a new BufferedImage*/
	public void setBufferedImage(BufferedImage bimage){
		this.setData(bimage.getRaster());
	}

	/** return the mean colour value of the pixel specified by r and c
	* @param r and c specified the position on the image
	* @return	the average RGB value for that pixel
	*/
	public int getGray(int r, int c){
		int[] colour = new int[3];
		this.getRaster().getPixel(r,c,colour);
		return (colour[0]+colour[1]+colour[2])/3;
	}
	/** Get the colour at the given position */
	public Colour getColour(int r, int c){
		int[] colour = new int[3];
		this.getRaster().getPixel(r,c,colour);
		return new Colour(colour);
	}
	/** Set the same value for the three colours at the given position */
	public void setGray(int row, int col, int gray){
		this.getRaster().getSampleModel().setPixel(row,col,new int[]{gray,gray,gray},this.getRaster().getDataBuffer());
	}
	/** Set this colour in the image at the given position */
	public void setColour(int row, int col, Colour colour){
		if(insideBounds(row, col))
			this.getRaster().getSampleModel().setPixel(row,col,colour.toArray(),this.getRaster().getDataBuffer());		
	}
	public void adjustColours(){
		Colour colour = new Colour();
		double ctemp[];
		double mean=0, cov=0, counter=0, min, max, scale;
		for(int i=this.getRaster().getMinX();i<this.getWidth();i++){
			for(int j=this.getRaster().getMinY();j<this.getHeight();j++){
				ctemp = getColour(i,j).toArrayDouble();
				mean += ctemp[0]+ctemp[1]+ctemp[2];
				counter += 3;
			}
		}
		mean /= counter;
		for(int i=this.getRaster().getMinX();i<this.getWidth();i++){
			for(int j=this.getRaster().getMinY();j<this.getHeight();j++){
				ctemp = getColour(i,j).toArrayDouble();
				cov += (ctemp[0]-mean)*(ctemp[0]-mean) + (ctemp[1]-mean)*(ctemp[1]-mean) + (ctemp[2]-mean)*(ctemp[2]-mean);
			}
		}
		cov /= (counter-1);
		cov = Math.sqrt(cov);
		min = mean-cov*3;
		if(min<0) min = 0;
		max = mean+cov*2;
		if(max>255) max = 255;
		scale = 255/(max-min);
		for(int i=this.getRaster().getMinX();i<this.getWidth();i++){
			for(int j=this.getRaster().getMinY();j<this.getHeight();j++){
				ctemp = getColour(i,j).toArrayDouble();
				colour.setRed((int)((ctemp[0]-min)*scale));
				colour.setGreen((int)((ctemp[1]-min)*scale));
				colour.setBlue((int)((ctemp[2]-min)*scale));
				setColour(i,j,colour);
			}
		}
	}
	public ImageData goDouble(){
		BufferedImage bufimg = new BufferedImage(2*this.getWidth(), 2*this.getHeight(), this.getType());
		int row, col;
		Colour t_colour = new Colour();
		for(int i=this.getRaster().getMinX();i<this.getWidth();i++){
			for(int j=this.getRaster().getMinY();j<this.getHeight();j++){
				t_colour = this.getColour(i,j);
				row = i-this.getRaster().getMinX()+bufimg.getRaster().getMinX();
				col = j-this.getRaster().getMinY()+bufimg.getRaster().getMinY();
				bufimg.getRaster().getSampleModel().setPixel(row*2,col*2,t_colour.toArray(),bufimg.getRaster().getDataBuffer());
				bufimg.getRaster().getSampleModel().setPixel(row*2,col*2+1,t_colour.toArray(),bufimg.getRaster().getDataBuffer());
				bufimg.getRaster().getSampleModel().setPixel(row*2+1,col*2,t_colour.toArray(),bufimg.getRaster().getDataBuffer());
				bufimg.getRaster().getSampleModel().setPixel(row*2+1,col*2+1,t_colour.toArray(),bufimg.getRaster().getDataBuffer());
			}
		}
		//this.setBufferedImage(bufimg);
		return new ImageData(bufimg);
	}
	/** Basic edition filters applied to the image.
	* @param typeEdit 0	Binary image, 1	Negative, 2	Gray scale
	*/
	public void editImg(int typeEdit){
		for(int i=this.getRaster().getMinX();i<this.getWidth();i++){
			for(int j=this.getRaster().getMinY();j<this.getHeight();j++){
				switch(typeEdit){
				case 0:
					setGray(i,j,(getGray(i,j)<128)?0:255);
					break;
				case 1:
					setColour(i,j,getColour(i,j).getNegative());
					break;
				case 2:
					setGray(i,j,getGray(i,j));
					break;
				}
			}
		}
	}

	/** This function tests if the vector is within the image boundaries*/
	public boolean insideBounds(int r, int c){
		return (r>=this.getRaster().getMinX())&&(r<(this.getRaster().getMinX()+this.getWidth()))&&(c>=this.getRaster().getMinY())&&(c<(this.getRaster().getMinY()+this.getHeight()));
	}

	/**Refreshes the image*/
	public void refresh(){
		this.setData(getRaster());
	}

}