package com.rits.cloning;

public class CloningStrategyFactory {
	public static ICloningStrategy annotatedClass(final Class annotationClass, final ICloningStrategy.Strategy strategy) {
		return new ICloningStrategy() {
			public Strategy strategyFor(Object toBeCloned) {
				if (toBeCloned == null) return Strategy.IGNORE;
				if (toBeCloned.getClass().getDeclaredAnnotation(annotationClass) != null) return strategy;
				return Strategy.IGNORE;
			}
		};
	}
}
