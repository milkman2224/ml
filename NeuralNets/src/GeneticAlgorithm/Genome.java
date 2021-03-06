package GeneticAlgorithm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Genome implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1321226803013352370L;
	private int ID = 0;
	private Map<String,Double> fitnessProperties = new HashMap <String, Double> ();
	private List <Gene> genes = new ArrayList <Gene>();
	private double overallFitness = 0.0;
	private double  boltzmannFitness = 0.0;
	private Genome mother = null;
	private Genome father = null;
	
	public Genome (){}
	public Genome (int ID){
		this.ID = ID;
	}
	public Genome (int ID, String...finesses){
		this.ID = ID;
		for (String property : finesses ){
			this.fitnessProperties.put(property, 0.0);
		}
	}

	
	public double getOverallFitness() {
		return overallFitness;
	}
	public void setOverallFitness(double fitness) {
		 overallFitness = fitness;
	}
	public Map<String,Double> getFitnessProperties() {
		return fitnessProperties;
	}
	public void setFitnessProperties(Map<String, Double> fitnessProperties) {
		this.fitnessProperties = fitnessProperties;
	}
	public List<Gene> getGenes() {
		return genes;
	}
	public void setGenes(List<Gene> genes) {
		this.genes = genes;
	}
	public int getID() {
		return ID;
	}
	public void setID(int iD) {
		ID = iD;
	}
	public double getBoltzmannFitness() {
		return boltzmannFitness;
	}
	public void setBoltzmannFitness(double boltzmannFitness) {
		this.boltzmannFitness = boltzmannFitness;
	}

	public Genome getMother() {
		return mother;
	}
	public void setMother(Genome mother) {
		this.mother = mother;
	}
	public Genome getFather() {
		return father;
	}
	public void setFather(Genome father) {
		this.father = father;
	}
}

