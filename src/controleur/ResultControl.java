package controleur;

import java.io.IOException;
import java.util.Arrays;
import commons.ReadWrite;
import Utils.Defaults;
import moteur.Envelop;
import moteur.Spaghetti;

public class ResultControl {

	private static ResultControl instance;

	public static synchronized ResultControl getInstance(){
		if (instance == null){
			instance = new ResultControl();
		}
		return instance;
	}

	public ResultControl() {
	}
	
	public Envelop readEnvelop(String file) throws IOException, Exception{
		Double[][] w=ReadWrite.read(file,Defaults.resultSep,1);
		Envelop env = new Envelop();
		env.setX(w[0]);
		env.setMaxpost(w[1]);
		env.setMedian(w[2]);
		env.setQlow(w[3]);
		env.setQhigh(w[4]);
		env.setNx(env.getX().length);
		return env;
	}

	public Spaghetti readSpaghetti(String file) throws IOException, Exception{
		Double[][] w=ReadWrite.read(file,Defaults.resultSep,1);
		Spaghetti spag = new Spaghetti();
		spag.setX(w[0]);
		spag.setY(Arrays.copyOfRange(w,1,w.length));
		spag.setNx(spag.getX().length);
		spag.setNspag(w.length-1);
		return spag;
	}

}
