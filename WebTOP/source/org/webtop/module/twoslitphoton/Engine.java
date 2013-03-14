package org.webtop.module.twoslitphoton;



import org.webtop.util.*; 
import org.webtop.util.Animation.*;
import org.webtop.x3d.*;
import org.web3d.x3d.sai.*;
import org.web3d.*;
import java.applet.AudioClip;
import org.webtop.x3d.output.*;
import sun.audio.*;

import java.io.*;
import java.net.URL;
import java.applet.AudioClip;
import java.awt.Component;

import javax.swing.JApplet;
import javax.swing.JOptionPane;


public class Engine extends JApplet implements AnimationEngine {
	
	private TwoSlitPhoton wapp;
	private Animation animation;
	
	private static final int  MAX_COUNT=50000;
	private static final float  Z=1000;
	private static final float  X_SCALE=20;
	private static final int UPDATE=0;
	private static final int NO_UPDATE=1;
	
	private final SAI sai;

	private float t=0;
	private float periods=0;
	private int photonsPerPeriod;
	private float difference;
	private float remainder;
	
	private float wavelength;
	private float width;
	private float distance;
	private int count = 0;

	private TwoSlitPhoton.Data curData;
	
	private AudioStream click;
	private AudioClip click1;
	private static AudioDataStream cas;
	public static boolean muted = true;
	

	public static final float MAX_WIDTH=100000,MAX_HEIGHT=100000,DEF_WIDTH=1000,DEF_HEIGHT=300;

	private MFVec3f pointCoords;
	private SFFloat set_X;
	private SFFloat set_Y;
	private SFFloat set_Z;
	private MFColor pointColors;
	
	private static final float RES = 0.25f;
	private float X_size=DEF_WIDTH;
	private float Y_size=DEF_HEIGHT;
	private int X_res=(int)(X_size*RES);
	private int Y_res=(int)(Y_size*RES);
	private static final boolean COLOR_PER_VERTEX=true;
	private IFSScreen ifs;
	private IndexedSet is;
	private float XOrigin;
	private float YOrigin;
	private final float xGridUnit = X_size/X_res;
	private final float yGridUnit = Y_size/Y_res;
	
	private boolean played = false;

	// for coloring
	float[] target;
	
	float[][] colors=new float[X_res*Y_res][3];
	float[] intensities=new float[X_res*Y_res];
	
	int rCount = 0;
	//constructor
	public Engine(TwoSlitPhoton _wapp, SAI _sai){
		sai = _sai;
		wapp = _wapp; 
		try {
			File filename = new File("sounds/click5.AU");//should load if not running from cmd prompt
			if(!filename.exists()){
				System.out.println("LOCAL MACHINE RUN");
				//if the module is being ran on the lab machine, then this will allow the file 
				//to properly load.  I.e...not from the installer but from the command prompt
				File filename2 = new File("C:\\home\\davis\\source\\org\\" +
						"webtop\\module\\twoslitphoton\\click5.AU");
				InputStream in = new FileInputStream(filename2);
				click = new AudioStream(in);
			}
			else{
				//JOptionPane.showMessageDialog(null, "INSTALLER RUN");
				InputStream in = new FileInputStream(filename);
				click = new AudioStream(in);
			}
			AudioData data = click.getData();
			cas = new ContinuousAudioDataStream (data);
		} catch( Exception e ) {
				DebugPrinter.println( "Audio goes kaboom, probably file not found" );
				System.out.println("Audio goes kaboom, probably file not found");
		}
		
		//pretty sure getEI maps to getInputField(), if problems arise, they will be here [JD]
		set_X = (SFFloat) sai.getInputField("IFSMover","translation_in_x");
		set_Y = (SFFloat) sai.getInputField("IFSMover","translation_in_y");
		set_Z = (SFFloat)sai.getInputField("IFSMover","translation_in_z");
		
		ifs=new IFSScreen(new IndexedSet(sai,sai.getNode("IFS")),new int[][] {},X_size,Y_size);
		ifs.setResolution(X_res,Y_res);
		ifs.setup();
		
		set_X.setValue(-X_size/2);
		set_Y.setValue(Y_size/2);
		set_Z.setValue(-Z);

		colors=new float[X_res*Y_res][3];
		intensities=new float[X_res*Y_res];
		
		float[][] imgpts = new float[count][];

		XOrigin = X_size/2;
		YOrigin = -Y_size/2;
		
	}
	public void init(Animation a) {
		animation = a; 
		clearScreen(UPDATE); //put in when finished debugging
		
	}

	public boolean timeElapsed(float periods) {
		played = true;
		t+=animation.getPeriod()/1000f;
		if((t<wapp.getExposureTime()-.1f))
		{
			System.out.println("Exposure Time: " + wapp.getExposureTime() + " Time: " + t);
			wapp.setElapsedTime(t);
		}
		else if(t>=(wapp.getExposureTime()-.1f))
		{
			System.out.println("Time should stop: " + wapp.getExposureTime() + "Time: " + t);
			wapp.setElapsedTime(wapp.getExposureTime());
			System.out.println("Time stopped: " + wapp.getExposureTime());
		}
		periods+=1;
		return true;
	}
	
	public void execute(Data d) {
		if(curData!=d) {	
			curData=(TwoSlitPhoton.Data)d;
			wavelength=curData.wavelength;
			width=curData.width;
			distance=curData.distance;
			
		}
		
		//Dr. Foley's and Jeremy's Final NASTY Hack
		if(rCount < 1){
			wapp.hitReset();
		}
		rCount++;
		//END NASTY Hack
		
		int photons = photonsPerPeriod;
		
		if (remainder>=1) {
			photons+=1;
			remainder-=1;
		}
		
		for (int j=0; j<photons; j++) {
			while(!tryPhoton((float)((Math.random()*20000)-10000),(float)((Math.random()*300)-150),(float)(Math.random()))) {};
			if(!muted) 
			{
				AudioPlayer.player.start(cas);
			}
			else if(muted)
			{
				AudioPlayer.player.stop(cas);
			}
			
			if (t>=wapp.getExposureTime()) {
				wapp.setStopped();
				
			}
			
			if (count==MAX_COUNT) {
				wapp.setStopped();
				count = 0;
				clearScreen(NO_UPDATE);
			}
		}
		
		remainder+=difference;
		if (!animation.isPaused())
			updateScreen();	
		else 
			AudioPlayer.player.stop(cas);

	}
	public static void stopPlayer()
	{
		AudioPlayer.player.stop(cas);
	}
	public void setT(float time) {
		t = time;
	}
	
//////////////////////////////////////////////////////////////////////////
	// Description:
	//		Computes the intensity for the 2 slit aperture
	//////////////////////////////////////////////////////////////////////////
	//
	// alpha(x, z) = (Pi / lambda) * w * sin(theta)
	//						 ~ (Pi / lambda) * w * (x / z)
	//
	// beta(x, z) = (Pi / lambda) * d * sin(theta)
	//						~ (Pi / lambda) * d * (x / z)
	//
	//					{ [sin(alpha(x, z)) / alpha(x, z)]^2					 if x != 0
	// f(x, z) =		{
	//					{ 1														 if x = 0
	//					
	// g(x, z) =		{ [cos(beta(x, z))]^2	 if beta != m * Pi
	//
	// and m is in Z.
	//
	// I(x, z) = f(x, z) * g(x, z)
	//
	//////////////////////////////////////////////////////////////////////////
	public static float
	compute2SlitIntensity(float lz, // lambda * z
												float w,	// width
												float x,	// x
												float z,	// z
												float d)	// distance
	{
		final float
			xp = x * (float) Math.PI,		// x * Pi
			alpha = xp * w / lz,				// alpha(x, z)
			beta = xp * d / lz;					// beta(x, z)
		float f, g;										// f(x, z), g(x, z)

		if(x == 0.0f)
			f = 1;
		else
			f = (float) Math.pow(Math.sin(alpha) / alpha, 2);

		g = (float) Math.pow(Math.cos(beta), 2);

		return f * g;
	}
	
	public boolean tryPhoton(float x, float y, float ran) {
		float intensity = compute2SlitIntensity(wavelength*Z,width,x*1000,Z,distance);
		if (intensity>ran || intensity==1) {
			
			int xCoord = (int)(((XOrigin+x/X_SCALE)/xGridUnit));
			int yCoord = (int)((YOrigin+y)/yGridUnit);
			
			
			int index = Y_res*xCoord-yCoord;
			
			target = colors[index];
			float brightnessVal = TwoSlitPhoton.getBrightnessValue()/10f;
			WTMath.hls2rgb(target,WTMath.hue(wavelength),/*0.5f*/brightnessVal,1f);
			count++;
			
			wapp.setPhotonCount(count);
			return true;
		}
		else
			return false;
	}
	
	public void reset() {
		System.out.println("Engine Reset Called");
		t = 0;
		if(played)
			played = false;
		else
			wapp.setElapsedTime(0);
		muted = true;
	}
	
	public void setRate(int rate){
		photonsPerPeriod = (int)(rate/(1000/animation.getPeriod()));
		difference = (float)(rate/(float)(1000/animation.getPeriod()))-photonsPerPeriod;
		remainder = 0;
	}
	
	public void updateScreen() {
		/*for(int i = 0; i<X_res*Y_res; i++){
			for(int j=0; j<3; j++){
				colors[i][j] = 1f; 
				//System.out.println("Colors[" + i + "] [" + j + "]" + colors[i][j]);
			}
		}*/
		ifs.setColors(colors);	
	}
	public void clearScreen(int update){
		count = 0;
		for (int i=0; i<X_res*Y_res; i++) {
			colors[i]=new float[] {0f,0f,0f};
		}
		if (update==UPDATE) {
			wapp.setPhotonCount(0);
			ifs.setColors(colors);
		}
	}

}
