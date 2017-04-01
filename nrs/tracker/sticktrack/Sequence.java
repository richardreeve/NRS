/* $Id: Sequence.java,v 1.5 2005/05/09 22:07:01 hlrossano Exp $ */
package nrs.tracker.sticktrack;

import java.util.LinkedList;
import java.util.Iterator;
//import java.util.Collection;
import java.io.BufferedWriter;
import java.io.FileWriter;

/**
 * This class contains TimeLine classes.
 * TimeLine contains groups, has a start and an end time
 * 
 * @author Hugo Rosano
 * @deprecated This class is now deprecated, use package nrs.tracker.jointmanager
 */
public class Sequence extends LinkedList
{
	private int time = 0;

	/**
	 * Constructor empty
	 *
	 */
	public Sequence(){}

	/**
	 * Constructor from a Group of joints, it just add it
	 * @param joints
	 */
	public Sequence(Group joints){
		add(joints);
	}

	/**
	 * Iterates CompoundGroup and add it to this
	 * @param cgroup
	 */
	public void add(CompoundGroup cgroup){
		Iterator it = cgroup.iterator();
		while(it.hasNext())
			this.add( (Group)it.next() );
	}

	/**
	 * Each vector of the group is located to a group of the last sequence depending on how close is the last vector
	 * @param joints
	 */
	public void add(Group joints){
		LinkedList availables = availables();		// these are the 
		int m = availables.size();
		int n = joints.size();
		double [][] dist = new double[m][n];
		if(m==0)
			dist = new double[1][n];
		int minN =0;
		for(int i=0;i<m;i++){
			for(int j=0;j<n;j++)
				dist[i][j] = ((TimeLine)availables.get(i)).distance( (Vector2D) joints.get(j) );
		}
		double min = Double.MAX_VALUE;
		for(int i=0;i<m;i++){
			for(int j=0;j<n;j++){
				if(dist[i][j] < min){
					min = dist[i][j];
					minN = j;
				}
			}
			if(min == Double.MAX_VALUE)
				break;
			((TimeLine)availables.get(i)).add( joints.get(minN) );
			for(int k=0;k<m;k++)
				dist[k][minN] = Double.MAX_VALUE;
			min = Double.MAX_VALUE;
		}
		if(m==n){
			time++;
			return;
		}
		if(m<n){
			for(int j=0;j<n;j++){
				if(dist[0][j] < Double.MAX_VALUE)
					this.add( new TimeLine((Vector2D) joints.get(j), time) );
			}
			time++;
			return;
		}
		for(int i=0;i<m;i++){
			if (((TimeLine)availables.get(i)).finishTime() < time)
				((TimeLine)availables.get(i)).close();
		}
		time++;
	}

	/**
	 * Check open TimeLines
	 * @return a LinkedList with the available TimeLine classes
	 */
	LinkedList availables(){
		LinkedList avas = new LinkedList();
		Iterator it = this.iterator();
		while(it.hasNext()){
			TimeLine temp = (TimeLine)it.next();
			if(temp.isAvailable())
				avas.add(temp);
		}
		return avas;
	}

	/**
	 * Send information to the specified file
	 * @param fileName
	 */
	public void toFile(String fileName){
		int joint = 0, tm;
		Iterator it = this.iterator();
		try{
			BufferedWriter pal = new BufferedWriter(new FileWriter(fileName));
			while(it.hasNext()){
				pal.write("coords"+ (joint++) +"=[\n");
				TimeLine tline = (TimeLine)it.next();
				tm = tline.startTime();
				Iterator vec = tline.iterator();
				while(vec.hasNext()){
					pal.write(  ((Vector2D)vec.next()).toString()+'\t'+ (tm++) + "\t;\n");
				}
				pal.write("];\n");
			}
			pal.close();
		}catch(java.io.IOException e){System.out.println("Problems while saving Sequence!!!");}			
	}

	/**
	 * TimeLine contains groups, has a start and an end time
	 * @author Hugo Rosano
	 *
	 */
	class TimeLine extends LinkedList
	{
		/**
		 * start time of TimeLine
		 */
		private int start;
		/**
		 * end time of TimeLine
		 */
		private boolean state = true;
		TimeLine(int startTime){
			this.start = startTime;
		}
		TimeLine(Vector2D vec, int startTime){
			this.start = startTime;
			this.add(vec);
		}

		boolean isAvailable(){
			return state;
		}
		void close(){
			this.state = false;
		}
		double distance(Vector2D vec){
			if(isEmpty())
				return 0;
			return ((Vector2D)getLast()).sub(vec).getMagnitud();
		}
		int startTime(){
			return start;
		}
		int finishTime(){
			return start+this.size()-1;
		}
	}
}