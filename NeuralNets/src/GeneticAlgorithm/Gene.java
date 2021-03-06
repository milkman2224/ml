package GeneticAlgorithm;

import java.io.Serializable;

public class Gene implements Serializable{
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2252841865976965039L;
	public final static int BIAS = 0;
	public final static int WEIGHT = 1;
	
	private int ID;
	private int type;
	private int neuronID;
	private int inputNumber;
	private double weight;
	
	public Gene (int ID, int neuronID , int inputIndex, double weight, int type){
		this.ID = ID;
		this.neuronID = neuronID;
		this.inputNumber = inputIndex;
		this.weight = weight;
		this.type = type;
	}
	public int getNeuronID() {
		return neuronID;
	}
	public void setNeuronID(int neuronID) {
		this.neuronID = neuronID;
	}
	public int getInputNumber() {
		return inputNumber;
	}
	public void setInputNumber(int inputNumber) {
		this.inputNumber = inputNumber;
	}
	public double getWeight() {
		return weight;
	}
	public void setWeight(double weight) {
		this.weight = weight;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public int getID() {
		return ID;
	}
	public void setID(int iD) {
		ID = iD;
	}
}
