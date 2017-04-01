// $Id: Palette.java,v 1.3 2005/05/09 22:03:55 hlrossano Exp $
package nrs.tracker.palette;

//import java.lang.Math.*;
import java.util.Vector;
import java.util.Iterator;
import java.util.logging.Logger;
 
/**
*	@author Hugo L. Rosano
*	@version 1.0
*	This class is for representing many compound colours contained in it
*/
public class Palette extends Vector{

private double invCovariances[][], means[][];
private int ns[];
private String onEdition = "";

// Class logger
private static Logger m_log = Logger.getLogger("nrs.tracker.palette");

/**	Files saved by the palette can be accessed in this PATH */
static final public String PATH = "nrs\\tracker\\palette\\";

	/** return the matrix of double containing the inverse covariance of all compound colours
	* @return invCovariances */
	public double[][] getInvCovariances(){
		return invCovariances;
	}	
	/** return the matrix of doubles containing the means of all compound colours
	*@return means */
	public double[][] getMeans(){
		return means;
	}
	/** return the number of compound colours contained in this Palette
	* @return number of elements */
	public int[] getNs(){
		return ns;
	}
	/** Set statistics to this palette for groups found previously ignoring current samples
	*	@param invCovariances new inverse covariances
	*	@param	means new mean values
	*	@param	ns new number of elements
	*/
	public void setStats(double [][] invCovariances, double [][] means, int [] ns){
		this.invCovariances = invCovariances;
		this.means = means;
		this.ns =  ns;
	}

	/** Calculated statistics about the colours found so far and return a LinkedList containing this CompoundColour
	* Groups must be size()>0
	*/
	public void updateColourStatistics(){
		int size = this.size();
		invCovariances = new double[size][9];
		means = new double[size][3];
		ns = new int[size];

		CompoundColour compoundC;
		for(int i=0;i<size;i++){
			compoundC = (CompoundColour)this.get(i);
			if(compoundC.size()>0){
				ns[i] = compoundC.size();
				for(int j=0;j<9;j++)
					invCovariances[i] = compoundC.getInvCovariance();
				for(int j=0;j<3;j++)
					means[i] = compoundC.getMean();
			}
		}
	}

	/** add a new CompoundColour with the specified name and makes it the Compound Colour currently on edition
	*	@param	colourName name of the new Compound Colour
	*	@return true if the CompoundColour was added
	*/
	public boolean add(String colourName){
		return setOnEdition(colourName);
	}

	/** Sets the name of the CompoundColour currently on edition, if not found creates a new CompoundColour for that name
	*	@param	onEdition name of the compound colour on edition 
	*/
	public boolean setOnEdition(String onEdition){
		onEdition = onEdition.trim().toLowerCase();
		if(!onEdition.equals("")){
			this.onEdition = onEdition;
			if(!this.contains(new CompoundColour(onEdition))){
                          System.out.println("setOnEdition adding colour: "
                                             + onEdition);
				this.add(new CompoundColour(onEdition));
				return true;
			}
		}
		return false;
	}

	/** Gets the name of the CompoundColour id currently on edition
	*	@return		the name of the compound colour currently on edition
	*/
	public String onEdit(){
		return onEdition;
	}

	public boolean hasOnEdition(){
		return !onEdition.equals("");
	}

	/** return the compound colour currently on edition
	*@return current compound colour currently on edition*/
	public CompoundColour getOnEdition(){
		int temp = this.indexOf(new CompoundColour(onEdition));
		if(temp!=-1)
			return (CompoundColour)this.get(temp);
		return null;
	}

	/** Deletes the CompoundColour with the specified id. If the element on edition is erased, then the first in
	* the colour group is used.
	*	@return true if could erase
	*	@param	id name of the compound colour to be erased
	*/
	public boolean DeleteCompoundColour(String id){
		if( !this.remove(new CompoundColour(id)) )
			return false;
		if (this.size()>0){
			if (id.equals(onEdition))
				onEdition = this.firstElement().toString();
		}else
			onEdition = "";
		return true;
	}

	public void undoOnEdition(){
		if(hasOnEdition())
			getOnEdition().undo();
	}

	/** Return all the probabilities for all compound colours
	*	@return vector containing the normalized probability of belonging to each of the compound colours in this palette
	*	@param colour to be tested
	*/
	public double [] probabilisticsForAll(Colour colour){
		int size = ns.length;
		double tot = 0.0, res[] = new double[size];
		for(int i=0;i<size;i++){
			res[i] = probObjectGivenColour(i,colour)*ns[i];
		}
		for(int i=0;i<size;i++)
			tot += res[i];
		for(int i=0;i<size;i++)
			res[i] /= tot;
		return res;
	}

	/** Probabilistic to belon to the CompondColour on edition
	*@param colour to be tested
	*@return double with probabilistic, -1 if no CompoundColour con edition
	*/
	public double probabilisticOnEdition(Colour colour){
		if (hasOnEdition()){
			double [] temp = probabilisticsForAll(colour);
			return temp[this.indexOf(new CompoundColour(onEdition))];
		}
		return -1;	
	}

	public CompoundColour moreProbable(Colour colour){
		if (hasOnEdition()){
			double [] temp = probabilisticsForAll(colour);
			double max = -1;
			int indx = -1;
			for (int i=0; i<temp.length; i++){
				if(temp[i] > max){
					max = temp[i];
					indx = i;
				}
			}
			return (CompoundColour)this.get(indx);
		}
		return new CompoundColour();
	}

	private double probObjectGivenColour(int compoundInd, Colour colour){
		double [] r = new double[]{colour.getRed()-means[compoundInd][0], colour.getGreen()-means[compoundInd][1], colour.getBlue()-means[compoundInd][2]};
		double res = 0;
		int indx = 0;
		for(int i=0;i<3;i++){
			for(int j=0;j<3;j++)
				res += invCovariances[compoundInd][indx++]*r[i]*r[j];
		}
		res = Math.exp(-0.5*res);
		return res;
	}

	/** Load palette_name
	*	@param	fileName is the name of the file
	*/
	void load(String fileName){
		String line="", editTemp, file = fileName.trim().toLowerCase();
		try{
			if(!file.equals("")){
				this.clear();
				String []temp;
				int []rgb;

				java.io.BufferedReader readin = new java.io.BufferedReader(new java.io.FileReader(PATH + file+".plt"));

                                
				editTemp = readin.readLine();
				while ((line = readin.readLine()) != null){
					if(line.endsWith("ioc"))
						setOnEdition(  (line.substring(0, line.indexOf("=")-1)).trim() );
					else if(!line.endsWith("eoc")){
						temp = line.split("\t");
						rgb = new int[]{Integer.parseInt(temp[0]), Integer.parseInt(temp[1]), Integer.parseInt(temp[2])};
						getOnEdition().add( new Colour(rgb) );
					}
				}
				onEdition = editTemp;
				readin.close();
			}else
				throw new java.io.IOException();
		}catch(Exception e)
                {
                  System.out.println("Problems while loading palette!!!");
                }
	}

	/** 
         * Save palette colours to the specified absolute filename. Ie
         * the filename should contain path, name and extension
         * information.
         */
	void saveAbsolute(String fileName){
		try{
			CompoundColour compoundC;
			java.io.BufferedWriter pal = new java.io.BufferedWriter(new java.io.FileWriter(fileName.trim().toLowerCase()));
			Iterator it = this.iterator();
			pal.write(onEdition + '\n');
			while(it.hasNext()){
				compoundC = (CompoundColour)it.next();
				pal.write(  compoundC.id() + "\t=ioc\n" );
				Iterator itcc = compoundC.iterator();
				while(itcc.hasNext())
					pal.write(  ((Colour)itcc.next()).toFile() +'\n' );
				pal.write("eoc\n");
			}
			pal.close();
                        m_log.fine("Saved to file \""
                                   + fileName.trim().toLowerCase()
                                   + "\"");
                        
		}
                catch(java.io.IOException e)
                {
                  System.out.println("Problems while saving palette!!!");
                  m_log.warning("Failed to save to file \""
                                + fileName.trim().toLowerCase()
                                + "\"");
                  m_log.warning("Exception=" + e);
                }
	}

	/** Load a saved palette from the specifed absolute filename
         *
         *	@param fileName is the absolute (path + name +
         *	extension) filename of the palette file to load
         */
	public void loadAbsolute(String fileName){
		String line="", editTemp, file = fileName.trim().toLowerCase();
		try{
			if(!file.equals("")){
				this.clear();
				String []temp;
				int []rgb;

				java.io.BufferedReader readin = new java.io.BufferedReader(new java.io.FileReader(file));

                                
				editTemp = readin.readLine();
				while ((line = readin.readLine()) != null){
					if(line.endsWith("ioc"))
						setOnEdition(  (line.substring(0, line.indexOf("=")-1)).trim() );
					else if(!line.endsWith("eoc")){
						temp = line.split("\t");
						rgb = new int[]{Integer.parseInt(temp[0]), Integer.parseInt(temp[1]), Integer.parseInt(temp[2])};
						getOnEdition().add( new Colour(rgb) );
					}
				}
				onEdition = editTemp;
				readin.close();
                                m_log.fine("Loaded palette \""+file+"\"");
			}else
				throw new java.io.IOException();
		}catch(Exception e)
                {
                  System.out.println("Problems while loading palette!!!");
                  m_log.warning("Failed to load palette \"" + file + "\"");
                  m_log.warning("Exception=" + e);
                  System.out.println(e);
                }
	}

	/** Send to a file called palette_(id).m all the compound colours contained in this class with its internal colours also.
	*	The format of the file is meant to be read on matlab
	*/
	public void save(String fileName){
		try{
			CompoundColour compoundC;
			java.io.BufferedWriter pal = new java.io.BufferedWriter(new java.io.FileWriter(PATH + fileName.trim().toLowerCase() +".plt"));
			Iterator it = this.iterator();
			pal.write(onEdition + '\n');
			while(it.hasNext()){
				compoundC = (CompoundColour)it.next();
				pal.write(  compoundC.id() + "\t=ioc\n" );
				Iterator itcc = compoundC.iterator();
				while(itcc.hasNext())
					pal.write(  ((Colour)itcc.next()).toFile() +'\n' );
				pal.write("eoc\n");
			}
			pal.close();
		}catch(java.io.IOException e){System.out.println("Problems while saving palette!!!");}
	}

	/** Send to a file called fileName.m the statistic variables of the many colour classes found in this Palette
	*	@param	fileName is the name of the file which will be created in a matlab format
	*/
	public void outputStats(String fileName){
		try{
			java.io.BufferedWriter stats = new java.io.BufferedWriter(new java.io.FileWriter(PATH + fileName.trim().toLowerCase() +".m"));
			stats.write(String.valueOf(invCovariances.length)+'\n');
			stats.write("invCovariances=[\n");
			for(int i=0;i<invCovariances.length;i++){
				for(int j=0;j<invCovariances[0].length;j++)
					stats.write( String.valueOf(invCovariances[i][j])+'\t');
				stats.write(";\n");
			}
			stats.write("];\nmeans=[\n");
			for (int i=0;i<means.length;i++ ){
				for(int j=0;j<means[0].length;j++)
					stats.write( String.valueOf(means[i][j])+'\t');
				stats.write(";\n");
			}
			stats.write("];\nnumbers=[\n");
			for (int i=0;i<ns.length;i++)
				stats.write( String.valueOf(ns[i]) + "\t;\n" );
				stats.write("];\n %labels\n");
			for (int i=0;i<this.size() ;i++ )
				stats.write(((CompoundColour)this.get(i)).id()+"\t= "+ (i+1) + "\t;\n");
			stats.write("%end labels");
			stats.close();
		}catch(java.io.IOException e){System.out.println("Problems while saving stats!!!");}
		save(fileName);
	}	

        /** 
         * Write to a file with the specifed filename the current values
         * associated with the compound colour statistics.
         *
         *  @param fileName is the name of the file which will be
         *  created in a format compatible with Matlab.
         */

	public void outputStatsAbsolute(String fileName){
		try{
			java.io.BufferedWriter stats = new java.io.BufferedWriter(new java.io.FileWriter(fileName.trim().toLowerCase()));
			stats.write(String.valueOf(invCovariances.length)+'\n');
			stats.write("invCovariances=[\n");
			for(int i=0;i<invCovariances.length;i++){
				for(int j=0;j<invCovariances[0].length;j++)
					stats.write( String.valueOf(invCovariances[i][j])+'\t');
				stats.write(";\n");
			}
			stats.write("];\nmeans=[\n");
			for (int i=0;i<means.length;i++ ){
				for(int j=0;j<means[0].length;j++)
					stats.write( String.valueOf(means[i][j])+'\t');
				stats.write(";\n");
			}
			stats.write("];\nnumbers=[\n");
			for (int i=0;i<ns.length;i++)
				stats.write( String.valueOf(ns[i]) + "\t;\n" );
				stats.write("];\n %labels\n");
			for (int i=0;i<this.size() ;i++ )
				stats.write(((CompoundColour)this.get(i)).id()+"\t= "+ (i+1) + "\t;\n");
			stats.write("%end labels");
			stats.close();
		}
                catch(java.io.IOException e)
                {
                  m_log.warning("Failed to save colour statistics to file \"" 
                                + fileName + "\"");
                  m_log.warning("Exception=" + e);
                  System.out.println(e);
                }
	}
	
	/** Read from the specified file the statistics of colour groups
	*	@return a String array with the IDs of the colour groups found
	*	@param fileName is the name of the file meant to be read
	*/
	public String[] readStatsFile(String fileName){
		String labels[] = new String[]{""}, temp[];
		int n=0;
		try{
			java.io.BufferedReader readin = new java.io.BufferedReader(new java.io.FileReader(PATH + fileName.trim().toLowerCase() +".m"));
			n = Integer.parseInt(readin.readLine());
			readin.readLine();
			this.invCovariances = new double[n][9];
			this.means = new double[n][3];
			this.ns = new int[n];
			labels = new String[n];
			for(int i=0;i<n;i++){
				temp = readin.readLine().split("\t");
				for(int j=0;j<9;j++)
					this.invCovariances[i][j] = Double.parseDouble(temp[j]);
			}
			readin.readLine();
			readin.readLine();
			for(int i=0;i<n;i++){
				temp = readin.readLine().split("\t");
				for(int j=0;j<3;j++)
					this.means[i][j] = Double.parseDouble(temp[j]);
			}
			readin.readLine();
			readin.readLine();
			for(int i=0;i<n;i++){
				temp = readin.readLine().split("\t");
				this.ns[i] = Integer.parseInt(temp[0]);
			}
			readin.readLine();
			readin.readLine();
			for(int i=0;i<n;i++){
				temp = readin.readLine().split("\t");
				labels[i] = temp[0].trim();
				this.setOnEdition(labels[i]);
			}
			readin.close();
			this.onEdition = labels[0];
		}catch (java.io.IOException e){System.out.println("Error while reading");}
		load(fileName);
		return labels;
	}

}
