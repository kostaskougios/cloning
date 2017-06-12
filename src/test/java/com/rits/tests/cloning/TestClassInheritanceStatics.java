package com.rits.tests.cloning;

import com.rits.cloning.Cloner;
import org.junit.Test;

/**
 * @author kostas.kougios
 * Date: 12/06/17
 */
public class TestClassInheritanceStatics {

	static class Parent {
		private String instanceFieldParent;
		private static String staticFieldParent;
	}

	static class Child extends Parent {
		private String instanceFieldChild;
		private static String staticFieldChild;
	}

	@Test
	public void testStaticFields_Parent() {
		Cloner cloner = new Cloner();
		cloner.registerStaticFields(Parent.class);
		// Works fine
	}

	@Test
	public void testStaticFields_Child() {
		Cloner cloner = new Cloner();
		cloner.registerStaticFields(Child.class);
		// Fails
	}
}
