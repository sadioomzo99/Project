package de.umr.ds.task3;

public class Evaluation {

	/**
	 * Applies the model to each data vector in the dataset and computes the
	 * accuracy.
	 * 
	 * @return accuracy
	 */
	// TODO Task 3d)
	  public static double accuracy(Perceptron model, Dataset dataset) {
		double count=0;
		for(DataPoint point: dataset) {

			int fire = model.predict(point);
			if (fire == point.getLabel()) {
				count++;
			}

		}
		return  count/dataset.size();
	}

}
