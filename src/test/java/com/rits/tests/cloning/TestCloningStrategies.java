package com.rits.tests.cloning;

import com.rits.cloning.Cloner;
import com.rits.cloning.CloningStrategyFactory;
import com.rits.cloning.ICloningStrategy;
import org.junit.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class TestCloningStrategies {

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	@interface Ann {
	}

	class AnnotatedExample {
		@Ann
		public Object o = new Object();
	}

	class NotAnnotatedExample {
		public Object o = new Object();
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
}
