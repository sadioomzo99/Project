package de.umr.ds.task3;

import java.util.Random;

/**
 * A Perceptron holds weights and bias and can be applied to a data vector to
 * predict its class. Weights and bias are initialized randomly.
 */
public class Perceptron {

    public Vector getW() {
        return w;
    }

    public int getB() {
        return b;
    }

    // TODO Task 3b)
    Vector w;
    int b;
    Random random = new Random();

    public Perceptron() {

        this.b = random.nextInt(2);
        this.w = new Vector(random.nextInt(2),random.nextInt(2));
    }


    /**
     * Apply the perceptron to classify a data vector.
     *
     * @param x An input vector
     * @return 0 or 1
     */
    // TODO Task 3b)
    public int predict(Vector x) {
        if(x.dot(w)+b>0){
            return 1;
        }
        return 0;
    }

    public  void  predictIncrement(int is , int should, Vector vector,double alpha){
        Vector vector1 =vector.mult(alpha *(should -is));

        this.b= (int) (alpha*(should-is)*1);
        this.w= w.add(vector1);
    }


}
