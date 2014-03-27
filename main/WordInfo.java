package main;
import java.io.Serializable;

@SuppressWarnings({ "serial", "rawtypes" })
public class WordInfo implements Serializable,Comparable
{
	public String word;
	public int freq;

	public WordInfo(String _word, int _freq)
	{
		word = _word;
		freq = _freq;
	}

	@Override
	public int compareTo(Object arg)
	{
		// sort the words according to the frequency
		return ((WordInfo)arg).freq - freq;
	}
}