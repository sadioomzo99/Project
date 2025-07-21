
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class DVDBounceTest {
    @Before
    public void setUp() throws Exception {
        DVDBounce dvdBounce = new DVDBounce(100, 100);


        dvdBounce.lines.add(new Line(0, 0, 20, 50));
        dvdBounce.lines.add(new Line(10, 8, 30, 40));
        dvdBounce.lines.add(new Line(15, 12, 20, 30));
        dvdBounce.lines.add(new Line(20, 30, 40, 60));
        dvdBounce.lines.add(new Line(0, 0, 20, 50));
    }

    @Test
    public void getShortestDistance() {
        DVDBounce dvdBounce = new DVDBounce(100, 100);
        dvdBounce.lines.add(new Line(0, 0, 20, 50));
        dvdBounce.lines.add(new Line(10, 8, 30, 40));
        dvdBounce.lines.add(new Line(15, 12, 20, 30));
        dvdBounce.lines.add(new Line(20, 30, 40, 60));
        dvdBounce.lines.add(new Line(0, 0, 20, 50));

         assertEquals( 18.681541692269406,dvdBounce.getShortestDistance(),0.05);
    }

    @Test
    public void totalDistance() {
        DVDBounce dvdBounce = new DVDBounce(100, 100);
        dvdBounce.lines.add(new Line(0, 0, 20, 50));
        dvdBounce.lines.add(new Line(10, 8, 30, 40));
        dvdBounce.lines.add(new Line(15, 12, 20, 30));
        dvdBounce.lines.add(new Line(20, 30, 40, 60));
        dvdBounce.lines.add(new Line(0, 0, 20, 50));

        assertEquals(200.17627511782578,dvdBounce.totalDistance(),0.05);
    }


    @Test
    public void checkRepeat(){
        DVDBounce dvdBounce = new DVDBounce(100, 100);
        dvdBounce.lines.add(new Line(0, 0, 20, 50));
        dvdBounce.lines.add(new Line(10, 8, 30, 40));
        dvdBounce.lines.add(new Line(15, 12, 20, 30));
        dvdBounce.lines.add(new Line(20, 30, 40, 60));
        dvdBounce.lines.add(new Line(0, 0, 20, 50));
        assertTrue(dvdBounce.checkRepeat());
    }

    @Test
    public void frameTrick() {
        DVDBounce dvdBounce = new DVDBounce(100, 100);
        dvdBounce.lines.add(new Line(0, 0, 20, 50));
        dvdBounce.lines.add(new Line(10, 8, 30, 40));
        dvdBounce.lines.add(new Line(15, 12, 20, 30));
        dvdBounce.lines.add(new Line(20, 30, 40, 60));
        dvdBounce.lines.add(new Line(0, 0, 20, 50));

        assertTrue(dvdBounce.frameTrick());

    }
}
