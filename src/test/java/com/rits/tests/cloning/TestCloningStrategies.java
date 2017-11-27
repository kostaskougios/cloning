package com.rits.tests.cloning;

import com.rits.cloning.Cloner;
import com.rits.cloning.ICloningStrategy;
import org.junit.Test;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class TestCloningStrategies {

	@Test
	public void nullInsteadOfClone() {
		Cloner cloner = Cloner.standard();
		cloner.registerCloningStrategy(new ICloningStrategy() {
			public Strategy strategyFor(Object toBeCloned) {
				return Strategy.NULL_INSTEAD_OF_CLONE;
			}
		});
		Object o = new Object();
		assertNull(cloner.deepClone(o));
	}

	@Test
	public void sameInstanceInsteadOfClone() {
		Cloner cloner = Cloner.standard();
		cloner.registerCloningStrategy(new ICloningStrategy() {
			public Strategy strategyFor(Object toBeCloned) {
				return Strategy.SAME_INSTANCE_INSTEAD_OF_CLONE;
			}
		});
		Object o = new Object();
		assertSame(o, cloner.deepClone(o));
	}
}
