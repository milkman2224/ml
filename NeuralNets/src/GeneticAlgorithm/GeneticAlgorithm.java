package GeneticAlgorithm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import Neurons.Connection;
import Neurons.InputConnection;
import Neurons.NeuralLayer;
import Neurons.NeuralNetwork;
import Neurons.Neuron;
import Utils.Round;

public abstract class GeneticAlgorithm {
	
	
	private static final double WHEEL = 360.0;
	private List <Genome> population = new ArrayList <Genome> ();
	private List <Genome> candidateParents = new ArrayList <Genome> ();
	private List <Genome> eliteParents = new ArrayList <Genome> ();
	private Map <String,Double> fitnessAverages = new HashMap <String,Double>();
	private double overallFitnessAverage = 0.0;
	private int generation = -1;
	private double maxGeneVal = 1.0;
	private double minGeneVal = 0.0;
	private double mutationRate = 0.15;
	private double maxBias = 1.0;
	private double minBias = 0.0;
	private String logFile = null;
	private Genome fittestGenome = null;
	private double sumFitness = 0.0;
	private double crossPointValue = 0.5;
	
	public abstract void evaluateGenome (Genome genome ,Object...parameters);
	public abstract void evaluateGeneration ();
	public abstract void preEvolutionActions ();
	public abstract void postEvolutionActions ();
	
	public void newRandomPopulation (NeuralNetwork network, int populationSize, String...fitnessProperties){
		for (int i = 0; i < populationSize ; i++){
			Genome genome = new Genome (i,fitnessProperties);
			genome.setBoltzmannFitness(0.0);
			for (Neuron neuron : network.getInputLayer().getNeurons()){generateRandomGenes(neuron, genome);}
			for (NeuralLayer layer : network.getHiddenLayers()){
				for (Neuron neuron : layer.getNeurons()){
					generateRandomGenes(neuron, genome);
				}
			}
			for (Neuron neuron : network.getOutputLayer().getNeurons()){generateRandomGenes(neuron, genome);}
			population.add(genome);
		}
		for (String property : fitnessProperties){fitnessAverages.put(property, 0.0);}
		generation++;
	}
	
	private void generateRandomGenes (Neuron neuron, Genome genome){
		double weight =  0.0;
		Random rand = new Random();
		int count = 0;
		Gene gene = null;
		for (InputConnection connection : neuron.getInputConnections()){
			weight = rand.nextDouble();
			weight = Round.remapValues(weight, 0, 1, minGeneVal, maxGeneVal);
			gene = new  Gene(count, neuron.getID(), count, weight, Gene.WEIGHT);
			genome.getGenes().add(gene);
			count++;
		}
	}
	
	public void newGeneration (int eliteSampleSize, int boltzSampleSize,double boltzTemperature, int populationSize ){
		evaluateGeneration();
		preEvolutionActions();
		
		eliteParents.clear();
		candidateParents.clear();
		Random rand = new Random ();
		Genome child = null;
		generation ++;
		int childID = population.size() * generation;
		

		for (String property : fitnessAverages.keySet()){elitismSelection(property, eliteSampleSize);}
		boltzmannSelection(boltzTemperature, boltzSampleSize);
		population.clear();
		
		ArrayList <Genome> parents = new ArrayList<Genome>();
		for (Genome genome : eliteParents){parents.add(genome);}
		for (Genome genome : candidateParents){parents.add(genome);}
		
		while (population.size() < populationSize){
			Genome father =parents.get(rand.nextInt(parents.size()));
			Genome mother = parents.get(rand.nextInt(parents.size()));
			if ( rand.nextDouble() >= 0.1 && (population.size()+2) < populationSize){
				int crossPoint = (father.getGenes().size()/2);//rand.nextInt(father.getGenes().size());
				mate(mother, father, childID, crossPoint);
				mate(father, mother, childID+1, crossPoint);
				childID = childID+2;
			}else{
				mate(mother, father, childID, null);
				childID++;
			}
		}
		for (String property: fitnessAverages.keySet()){fitnessAverages.put(property, 0.0);}
		overallFitnessAverage = 0.0;
		postEvolutionActions();
		if (population.size() == 41 || population.size() == 201){System.out.println("nnnnnnnnnn");}
	}
	
	public void elitismSelection (String property, int sampleSize){
		List <Genome> tempList = new ArrayList <Genome> ();
		for (Genome genome : population){tempList.add(genome);}
		
		while (sampleSize > 0){
			Genome highest = tempList.get(0);
			for (Genome genome : tempList){
				double candidate = genome.getFitnessProperties().get(property);
				double current = highest.getFitnessProperties().get(property);
				if (current < candidate){highest = genome;}
			}
			eliteParents.add(highest);
			tempList.remove(highest);
			sampleSize--;
		}
	}
	
	private double  boltzmannFitness (double fitness , double temperature){
		if (fitness == 0){return 0.0;}
		double exponent = fitness/temperature;
		return  Round.round( (Math.pow(Math.E, exponent)), 3 );
	}
	
	public void boltzmannSelection (double temperature, int numberOfSelections){
		List <RouletteEntity> selectionPool = new ArrayList<RouletteEntity>();
		
		double sumFitness = 0.0;
		int notZeros = 0;
		for (Genome genome : population){
			if ( genome.getOverallFitness() != 0){
				double boltzFitness = boltzmannFitness(genome.getOverallFitness(), temperature);
				genome.setBoltzmannFitness(boltzFitness);
				sumFitness = sumFitness + boltzFitness;
				notZeros++;
			}
		}
		
		double startPoint = 0.0;
		double endPoint = 0.0;
		if (sumFitness != 0.0 && notZeros >= 2){
			for (Genome genome : population){
				if (genome.getOverallFitness() > 0){
					endPoint = startPoint + ((genome.getBoltzmannFitness()/sumFitness)*WHEEL);}
					RouletteEntity rouletteEntity = new RouletteEntity(genome, startPoint, endPoint);
					selectionPool.add(rouletteEntity);
					startPoint = endPoint;
				}
		}else{
			for (Genome genome : population){
				endPoint = startPoint + (WHEEL/population.size());
				RouletteEntity rouletteEntity = new RouletteEntity(genome, startPoint, endPoint);
				selectionPool.add(rouletteEntity);
				startPoint = endPoint;
			}
		}
	stochasticUniversallSampling (selectionPool,numberOfSelections);
	}
	
	private Genome findFromRoulette (double point,List <RouletteEntity> selectionPool ){

		Genome foundGenome = null;
		for (int i = 0; selectionPool.size() > i ; i++){
			RouletteEntity entity = selectionPool.get(i);
			if (point >= entity.startPoint && point < entity.endPoint){
				foundGenome = entity.genome;
			}
		}
		return foundGenome;
	}
	private  void stochasticUniversallSampling (List <RouletteEntity> selectionPool,int numberOfSelections){
		
		double stepSize =  WHEEL / numberOfSelections;
		double nextPoint = new Random ().nextDouble() * WHEEL;
		int selected = 0;
		while (selected < numberOfSelections ){
			if (nextPoint > WHEEL){nextPoint = nextPoint - WHEEL;}
			Genome selectedGenome = findFromRoulette (nextPoint, selectionPool);
			if (selectedGenome.getOverallFitness() > 10){
				System.out.println("shizz");
			}
			candidateParents.add(selectedGenome);
			nextPoint = nextPoint + stepSize;
			selected ++;
		}
	}
	
	public Genome singlePointCrossOver (Genome father, Genome mother,int childID, int crossPoint,String...fitnesses){
		Genome child = new Genome (childID,fitnesses);
		Gene gene = null;
		for (int i = 0 ; i < crossPoint ; i++){
			Gene dadGene = father.getGenes().get(i);
			gene = new Gene(i, dadGene.getNeuronID(), dadGene.getInputNumber(), dadGene.getWeight(), dadGene.getType());
			child.getGenes().add(gene);
		}
		for (int i = crossPoint; i < mother.getGenes().size(); i++){
			Gene momGene = mother.getGenes().get(i);
			 gene = new Gene(i, momGene.getNeuronID(), momGene.getInputNumber(), momGene.getWeight(), momGene.getType());
			child.getGenes().add(gene);
		}
		return child;
	}
	
	public Genome uniformCrossOver(Genome father, Genome mother, int childID, String...fitnesses){
		Genome child = new Genome (childID, fitnesses);
		Random rand = new Random ();
		Gene gene;
		int randNum = 0;
		for (int i = 0; i < father.getGenes().size(); i++){
			randNum = rand.nextInt(10);
			if (randNum <= 4){
				Gene dadGene = father.getGenes().get(i);
				gene = new Gene(i, dadGene.getNeuronID(), dadGene.getInputNumber(), dadGene.getWeight(), dadGene.getType());
				child.getGenes().add(gene);
			}else{
				Gene momGene = mother.getGenes().get(i);
				gene = new Gene(i, momGene.getNeuronID(), momGene.getInputNumber(), momGene.getWeight(), momGene.getType());
				child.getGenes().add(gene);
			}
		}
		return child;
	}
	
	public void mutate (Genome child ){
		Random rand = new Random ();
		double willMutate = 0.0;
		double newWeight = 0.0;
		
		for (Gene gene : child.getGenes()){
			willMutate = rand.nextDouble();
			if (willMutate <= mutationRate){
				newWeight = ( rand.nextDouble());
				newWeight = Round.remapValues(newWeight, 0, 1, minGeneVal, maxGeneVal);
				gene.setWeight(newWeight);
			}
		}
	}
	
	public Genome replicateGenome (Genome genome, int childID, boolean doMutate){
		Genome replica = new Genome (childID);
		Gene newGene = null;
		for (Gene gene : genome.getGenes() ){
			newGene = new Gene(gene.getID(),gene.getNeuronID(),gene.getInputNumber(),gene.getWeight(),gene.getType());
			replica.getGenes().add(newGene);
		}
		for (String propery : genome.getFitnessProperties().keySet() ){
			replica.getFitnessProperties().put(propery, 0.0);
		}
		if (doMutate == true){mutate (replica);}
		replica.setFather(genome);
		replica.setMother(genome);
		return replica;
	}
	
	public Genome blend (Genome father, Genome mother, int childID, double ratio){
		Genome child = new Genome (childID);
		Gene gene;
		for (int i = 0; i < father.getGenes().size(); i++){
			Gene dadGene = father.getGenes().get(i);
			Gene momGene = mother.getGenes().get(i);
			double weight = (ratio*dadGene.getWeight())+ ((1.0-ratio)*momGene.getWeight());
			gene = new Gene(i, dadGene.getNeuronID(), dadGene.getInputNumber(),weight, dadGene.getType());
			child.getGenes().add(gene);
		}
		mutate(child);
		for (String property : fitnessAverages.keySet()) {child.getFitnessProperties().put(property, 0.0);}
		return child;
	}
	
	public void mate (Genome father, Genome mother,int childID,Integer crossPoint){
		Genome child;
		if (crossPoint == null){
			child = uniformCrossOver (father,mother,childID);
		}else{
			child = singlePointCrossOver (father,mother,childID,crossPoint);
		}
		for (String property : fitnessAverages.keySet()) {child.getFitnessProperties().put(property, 0.0);}
		child.setMother(mother);
		child.setFather(father);
		mutate (child);
		population.add(child);
	}
	
	//-----------------------GETTERS & SETTERS --------------

	public List<Genome> getPopulation() {
		return population;
	}
	public void setPopulation(List<Genome> population) {
		this.population = population;
	}

	public List<Genome> getCandidateParents() {
		return candidateParents;
	}
	public void setCandidateParents(List< Genome> candidateParents) {
		this.candidateParents = candidateParents;
	}
	public List<Genome> getEliteParents() {
		return eliteParents;
	}
	public void setEliteParents(List<Genome> eliteParents) {
		this.eliteParents = eliteParents;
	}
	public Map<String, Double> getFitnessAverages() {
		return fitnessAverages;
	}
	public void setFitnessAverages(Map<String, Double> fitnessAverages) {
		this.fitnessAverages = fitnessAverages;
	}
	public double getOverallFitnessAverage() {
		return overallFitnessAverage;
	}
	public void setOverallFitnessAverage(double overallFitnessAverage) {
		this.overallFitnessAverage = overallFitnessAverage;
	}
	public double getMutationRate() {
		return mutationRate;
	}
	public void setMutationRate(double mutationRate) {
		this.mutationRate = mutationRate;
	}

	public int getGeneration() {
		return generation;
	}
	public void setGeneration(int generation) {
		this.generation = generation;
	}
	public static double getWheel() {
		return WHEEL;
	}
	public double getMaxGeneVal() {
		return maxGeneVal;
	}
	public void setMaxGeneVal(double maxGeneVal) {
		this.maxGeneVal = maxGeneVal;
	}
	public double getMinGeneVal() {
		return minGeneVal;
	}
	public void setMinGeneVal(double minGeneVal) {
		this.minGeneVal = minGeneVal;
	}
	public Genome getFittestGenome() {
		return fittestGenome;
	}
	public void setFittestGenome(Genome fittestGenome) {
		this.fittestGenome = fittestGenome;
	}
	public String getLogFile() {
		return logFile;
	}
	public void setLogFile(String logFile) {
		this.logFile = logFile;
	}
	public double getMaxBias() {
		return maxBias;
	}
	public void setMaxBias(double maxBias) {
		this.maxBias = maxBias;
	}
	public double getMinBias() {
		return minBias;
	}
	public void setMinBias(double minBias) {
		this.minBias = minBias;
	}
	public double getSumFitness() {
		return sumFitness;
	}
	public void setSumFitness(double sumFitness) {
		this.sumFitness = sumFitness;
	}
	
}

class RouletteEntity{
	
	public Genome genome;
	public double startPoint;
	public double endPoint;
	
	public RouletteEntity (Genome genome, double startPoint, double endPoint){
		this.genome = genome;
		this.startPoint = startPoint;
		this.endPoint = endPoint;
	}
}


