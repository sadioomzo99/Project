package de.umr.ds.task3;

import java.util.*;

public class Training {

	private static final double alpha = 0.05; // learning rate
	private static final int epochs = 100; // number of epochs

	/**
	 * A perceptron is trained on a dataset. After each epoch the perceptron's
	 * parameters are updated, the dataset is shuffled and the accuracy is computed.
	 * 
	 * @param perceptron the perceptron to train
	 * @param dataset the training dataset
	 */
	private static void train(Perceptron perceptron, Dataset dataset) {
		Visualization visualization = new Visualization(dataset,perceptron.getW(),perceptron.getB());
		int epoch=0;
		int bestEpoch=0;
		double bestAcc=0;
		Perceptron bestperceptron=perceptron;
		Map<Double,Perceptron> accuracy = new HashMap<>();

		while (epoch<epochs){
		for(DataPoint point: dataset){

			int fire=perceptron.predict(point);
			if( fire ==point.getLabel()){
				break;
			}
			if(fire ==0 && point.getLabel()==1){
				perceptron.predictIncrement(fire,point.getLabel(),point,alpha);
			}
			if(fire==1 && point.getLabel()==0){
				perceptron.predictIncrement(fire,point.getLabel(),point,alpha);
			}
		}

			if(Evaluation.accuracy(perceptron,dataset)>bestAcc){
				bestAcc=Evaluation.accuracy(perceptron,dataset);
				bestperceptron = perceptron;
				bestEpoch=epoch;
			};
			Collections.shuffle(dataset);
			visualization.update(perceptron.w,perceptron.b,epoch);
			epoch++;
		}
		// TODO Task 3c)

		visualization.update(bestperceptron.w,bestperceptron.b,bestEpoch);
	}



	public static void main(String[] args) {

		Dataset dataset = new Dataset(1000);
		Perceptron perceptron = new Perceptron();
		train(perceptron, dataset);

	}

}
