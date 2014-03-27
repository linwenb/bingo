package main;

import java.io.Serializable;

@SuppressWarnings({ "serial", "rawtypes" })
public class Score implements Serializable,Comparable
{
	public String Id;
	public double score;

	public Score(String _Id, double _score)
	{
		Id = _Id;
		score = _score;
	}

	@Override
	public int compareTo(Object arg)
	{
		// sort the words according to the score
		double sco = ((Score)arg).score;
		if(sco > score)
			return 1;
		else if (sco < score)
			return -1;
		else
			return 0;
	}
}