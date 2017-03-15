package hu.oe.nik.szfmv17t.physics;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class AccelerationTest {

	public static final double[] GEAR_MAX_ACCELERATION = new double[] { 6, 0, 10, 6, 4.5, 2.65, 1.6 };

	
	@Test
	public void acceleration_0_0() {
		assertEquals(0,Acceleration.calculateAcceleration(0, 0),0);
	}
	
	@Test
	public void acceleration_0_10() {
		assertEquals(0,Acceleration.calculateAcceleration(0, 10),0);
	}
	
	@Test
	public void acceleration_0_N() {
		for (double percentage = 0; percentage < 1; percentage+=0.01) {
			assertEquals(0,Acceleration.calculateAcceleration(0, percentage),0);
		}		
	}
	
	@Test
	public void acceleration_1_0() {
		assertEquals(0,Acceleration.calculateAcceleration(1, 0),0);
	}
	
	@Test
	public void acceleration_1_100() {
		
		double expected=10;
		double actual=Acceleration.calculateAcceleration(1, 1);		
		assertEquals(expected,actual,0);
			
	}
	
	@Test
	public void acceleration_1_N() {
		for (double percentage = 0; percentage < 1; percentage+=0.01) {			
			double expected=10*percentage;
			double actual=Acceleration.calculateAcceleration(1, percentage);			
			assertEquals(expected,actual,0);	
		}		
	}
	
	@Test
	public void acceleration_R_N() {
		for (double percentage = 0; percentage < 1; percentage+=0.01) {			
			double expected=6*percentage;
			double actual=Acceleration.calculateAcceleration(-1, percentage);			
			assertEquals(expected,actual,0);	
		}		
	}
	
	
	@Test
	public void acceleration_allHundred() {
		double[] gear_max_acceleration = new double[] { 6, 0, 10, 6, 4.5, 2.65, 1.6 };
		for (int j = -1; j < 6; j++) {
			double actGearMaxAcc = gear_max_acceleration[j+1];
			for (double percentage = 0; percentage < 1; percentage+=0.01) {				
				double expected = actGearMaxAcc * percentage;
				double actual = Acceleration.calculateAcceleration(j, percentage);
				assertEquals(expected, actual, 0);
			}
		}
	}
}
