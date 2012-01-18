package com.rits.tests.cloning;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import com.rits.cloning.Cloner;

/**
 * @author kostantinos.kougios
 *
 * 7 Nov 2008
 */
public class BenchmarkCloner
{

	public static void main(final String[] args) throws MalformedURLException
	{
		final long start = System.currentTimeMillis();
		final Cloner cloner = new Cloner();
		int j = 300000;
		final Calendar gc = Calendar.getInstance();
		while (j-- > 0)
		{
			final URL url = new URL("http://localhost");
			final ArrayList<URL> l = new ArrayList<URL>();
			for (int i = 0; i < 5; i++)
			{
				l.add(url);
			}
			final URL clone = cloner.deepClone(url);
			l.add(clone);
			final ArrayList<URL> clonedList = cloner.deepClone(l);
			clonedList.add(url);
			cloner.deepClone(clonedList);
			cloner.deepClone(gc);

			final HashMap<String, Integer> m = new HashMap<String, Integer>();
			m.put("kostas", 100);
			m.put("tina", 120);
			m.put("george", 150);
			cloner.deepClone(m);
		}
		System.out.println("dt=" + (System.currentTimeMillis() - start));
	}
}
