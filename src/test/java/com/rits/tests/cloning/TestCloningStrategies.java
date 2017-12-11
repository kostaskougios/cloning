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

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	@interface Ann {
	}

	class Data {
	}
	class AnnotatedExample {
		@Ann
		public Data o = new Data();
	}

	class NotAnnotatedExample {
		public Data o = new Data();
	}

	@Test
	public void annotatedClassPositive() {
		Cloner cloner = Cloner.standard();
		cloner.registerCloningStrategy(CloningStrategyFactory.annotatedField(Ann.class, ICloningStrategy.Strategy.NULL_INSTEAD_OF_CLONE));
		assertNull(cloner.deepClone(new AnnotatedExample()).o);
	}

	@Test
	public void annotatedClassNegative() {
		Cloner cloner = Cloner.standard();
		cloner.registerCloningStrategy(CloningStrategyFactory.annotatedField(Ann.class, ICloningStrategy.Strategy.NULL_INSTEAD_OF_CLONE));
		assertNotNull(cloner.deepClone(new NotAnnotatedExample()).o);
	}

	@Test
	public void annotatedClassSameInstancePositive() {
		Cloner cloner = Cloner.standard();
		cloner.registerCloningStrategy(CloningStrategyFactory.annotatedField(Ann.class, ICloningStrategy.Strategy.SAME_INSTANCE_INSTEAD_OF_CLONE));
		AnnotatedExample ae = new AnnotatedExample();
		assertSame(ae.o, cloner.deepClone(ae).o);
	}

	@Test
	public void annotatedClassSameInstanceNegative() {
		Cloner cloner = Cloner.standard();
		cloner.registerCloningStrategy(CloningStrategyFactory.annotatedField(Ann.class, ICloningStrategy.Strategy.SAME_INSTANCE_INSTEAD_OF_CLONE));
		NotAnnotatedExample ae = new NotAnnotatedExample();
		assertNotSame(ae.o, cloner.deepClone(ae).o);
	}
}
