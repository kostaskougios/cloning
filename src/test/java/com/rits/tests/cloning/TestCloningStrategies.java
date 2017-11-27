package com.rits.tests.cloning;

import com.rits.cloning.Cloner;
import com.rits.cloning.CloningStrategyFactory;
import com.rits.cloning.ICloningStrategy;
import org.junit.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.junit.Assert.*;

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

	@Test
	public void ignoreStrategy() {
		Cloner cloner = Cloner.standard();
		cloner.registerCloningStrategy(new ICloningStrategy() {
			public Strategy strategyFor(Object toBeCloned) {
				return Strategy.IGNORE;
			}
		});
		Object o = new Object();
		assertNotSame(o, cloner.deepClone(o));
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	@interface Ann {
	}

	@Ann
	class AnnotatedExample {
	}

	class NotAnnotatedExample {
	}

	@Test
	public void annotatedClassPositive() {
		Cloner cloner = Cloner.standard();
		cloner.registerCloningStrategy(CloningStrategyFactory.annotatedClass(Ann.class, ICloningStrategy.Strategy.NULL_INSTEAD_OF_CLONE));
		assertNull(cloner.deepClone(new AnnotatedExample()));
	}

	@Test
	public void annotatedClassNegative() {
		Cloner cloner = Cloner.standard();
		cloner.registerCloningStrategy(CloningStrategyFactory.annotatedClass(Ann.class, ICloningStrategy.Strategy.NULL_INSTEAD_OF_CLONE));
		assertNotNull(cloner.deepClone(new NotAnnotatedExample()));
	}
}
