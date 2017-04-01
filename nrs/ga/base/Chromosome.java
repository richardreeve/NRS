package nrs.ga.base;

import java.util.ArrayList;

public class Chromosome
{
    private ArrayList m_Genes;
    private double m_evaluation;

    /** Constructor. */
    public Chromosome(){
	m_Genes = new ArrayList();
	m_evaluation = 0.0; //starts at 0.0
    }

    public void addGenes(int i){
	//create i new Gene objects
	for(int x = 0; x < i; x++){
	    m_Genes.add(new Gene());
	}
    }

    /** For clone() purposes. 
     * @param genes List of Genes.
    */
    public void addGenes(ArrayList genes){
	m_Genes.clear();
	Gene g;
	for(int x = 0; x < genes.size(); x++){
	    g = (Gene) genes.get(x);
	    m_Genes.add(g.clone());
	}
    }

    public void clearGenes(){
	m_Genes.clear();
    }

    public void setEvaluation(double f){
	m_evaluation = f;
    }

    public ArrayList getGenes(){
	return m_Genes;
    }
    
    public int getNumGenes(){
	return m_Genes.size();
    }
    
    public double getEvaluation(){
	return m_evaluation;
    }

    //need deep cloning
    public Object clone(){
	Chromosome c = new Chromosome();
	ArrayList a = new ArrayList();
	Gene g;

	for(int i = 0; i < m_Genes.size(); i++){
	    g = (Gene) m_Genes.get(i);
	    a.add(g.clone());
	}
	c.addGenes(a);
	c.setEvaluation(this.m_evaluation);
	return c;
    }
}

