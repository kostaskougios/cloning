package com.rits.cloning;

import java.lang.reflect.Field;

public class CloningStrategyFactory {
	public static ICloningStrategy annotatedField(final Class annotationClass, final ICloningStrategy.Strategy strategy) {
		return new ICloningStrategy() {
			public Strategy strategyFor(Object toBeCloned, Field field) {
				if (toBeCloned == null) return Strategy.IGNORE;
				if (field.getDeclaredAnnotation(annotationClass) != null) return strategy;
				return Strategy.IGNORE;
			}
		};
	}
}
