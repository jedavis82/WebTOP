package org.webtop.module.wavesfinal;

import java.util.Enumeration;
import java.util.Vector;

import org.web3d.x3d.sai.*;
import org.webtop.x3d.*;

public class Engine extends Thread implements Runnable, 
org.webtop.module.wavesfinal.PoolController {

	public static final int LOW_RESOLUTION=50,
	MEDIUM_RESOLUTION=100,
	HIGH_RESOLUTION=200;
	public static final boolean FASTEST_NPV=false,
			FAST_NPV=true,
			MEDIUM_NPV=true,
			SMOOTH_NPV=false,
			VERYSMOOTH_NPV=true;
	public static final float POOL_SIZE=100;	//on a side

	private final WavesFinal applet;
	private final SAI sai;
	private Pool pool;
	
	private static int threadCount=0;
	private static synchronized int nextThreadID() {return threadCount++;}

	private float t;
	private float tStep = 1;
	private boolean done;
	private volatile boolean playing = false;
	private volatile boolean wasPlaying = false;
	private volatile boolean dragging = false;
	private float[] height,sparseHeight;
	
	private static float[] square(int size) {return new float[size*size];}
	
	private int resolution;
	private boolean normalPerVertex;
	private float spacing;
	private int sparseResolution;
	private float sparseSpacing;

	private int staticResolution = 200;
	private int animResolution = 100;

	private boolean staticNormalPerVertex = true;
	private boolean animNormalPerVertex = true;

	private boolean autoUpdate = false;

	private final static int updateInterval = 50;
	private final static int minUpdateInterval = 10;
	
	//may not need these
	private Vector sources,widgets,singleSources;
	
	//for source calculations
	private LinearSource s1, s2;
	private RadialSource r1, r2;
	//linear source values
	private float amp1, amp2, wl1, wl2, ph1, ph2, anB1, anB2;
	//radial source values
	private float rAmp1, rAmp2, rWl1, rWl2, rPh1, rPh2, rX1, rX2, rY1, rY2;
	//check to see if the widget has been "created"
	private boolean lin1Active = false, lin2Active = false, r1Active = false,
		r2Active = false;
	
	public Engine(WavesFinal w){
		applet = w;
		sai = w.getSAI();
		
		pool = new Pool(sai, 100);
		
		resolution = pool.getResolution();
		spacing = pool.getSpacing();
		sparseResolution = pool.getSparseResolution();
		sparseSpacing = pool.getSparseSpacing();
		
		System.out.println("Resolution: " + resolution + " Spacing: " + spacing + 
				" sparseRes: " + sparseResolution + " sparseSpacing: " + sparseSpacing);

		height = square(resolution);
		sparseHeight = square(sparseResolution);
		
		autoUpdate = true;
		update();
		this.setPoolOptions(200, true);
	}
	
	private boolean isSparse() {return pool.getRenderingMode()==Pool.SPARSE;}
	public float getHeight(float x,float y) {return getHeight(x,y,t);}
	
	private float getHeight(float x,float y,float t) {
		float h = 0;
		getFBValues();
		// For each source
		//May need to put these back in later [JD]
		/*for(Enumeration waveSources=sources.elements();waveSources.hasMoreElements();)
			h += ((WaveSource) waveSources.nextElement()).getValue(x, y, t);

		for (Enumeration singleWaveSources=singleSources.elements();singleWaveSources.hasMoreElements();)
			h += ((SingleWaveSource) singleWaveSources.nextElement()).getValue(x, y, t);
		 */
		//need to check what's active here, like they did in old waves
		if(applet.linearOneEnabled()){
			s1 = new LinearSource(this, amp1, wl1,
					ph1,-50f,50f,anB1);
			h += s1.getValue(x, y, t);//can do this i think
		}
		if(applet.linearTwoEnabled()){
			s2 = new LinearSource(this, amp2, wl2,
					ph2, -50f, 50f, anB2);
			h+=s2.getValue(x, y, t);
		}
		if(applet.pointOneEnabled()){
			r1 = new RadialSource(this, rAmp1, rWl1, rPh1, rX1, rY1,applet);
			h+=r1.getValue(x, y, t,1);
		}
		if(applet.pointTwoEnabled()){
			r2 = new RadialSource(this, rAmp2, rWl2, rPh2, rX2, rY2,applet);
			h+=r2.getValue(x, y, t,2);
		}
		return h;
	}
	
	public void update() {
		int i, p = 0;
		int u, v, uvmax;
		float x, y, space, newHeight[];		//newHeight is pointer
		boolean lores=isSparse();
		//May need to put these back in later [JD]
		/*if(sampleCount>0) {
			WIterator w=new WIterator();
			i=0;
			do {
				PoolWidget pw=w.next();
				if(pw instanceof SamplingStick) {
					++i;
					((SamplingStick)pw).query();
				}
			} while(i<sampleCount);
		}*/

		if(lores) {
			uvmax=sparseResolution;
			space=sparseSpacing;
			newHeight=sparseHeight/*2*/;
		} else {
			//if(!playing) applet.getStatusBar().setText("Calculating...");
			uvmax=resolution;
			space=spacing;
			newHeight=height/*2*/;
		}
		//check the two line sources
		if(applet.linearOneEnabled() && !lin1Active){
			s1 = new LinearSource(this, amp1, wl1,
					ph1,-50f,50f,anB1);
			lin1Active = true;
		}
		if(applet.linearTwoEnabled() && !lin2Active){
			s2 = new LinearSource(this, amp2, wl2,
					ph2, -50f, 50f, anB2);
			lin2Active = true;
		}
		//check the two point sources
		if(applet.pointOneEnabled() && !r1Active){
			System.out.println("New R1");
			r1 = new RadialSource(this, rAmp1, rWl1, rPh1, rX1, rY1, applet);
			r1Active = true;
		}
		if(applet.pointTwoEnabled() && !r2Active){
			System.out.println("New R2");
			r2 = new RadialSource(this, rAmp2, rWl2, rPh2, rX2, rY2, applet);
			r2Active = true;
		}
		for(y=50.0f, p=0, v=0; v<uvmax; v++, y-=space)
			for(x=-50.0f, u=0; u<uvmax; u++, x+=space, p++){
				getFBValues();
				//this won't work until threading is implemented...testing now
				newHeight[p]=getHeight(x,y,t);
				
				//height[p] = s1.getValue(x, y, t);
				//height[p] = this.getHeight(x, y,t);
				
				//test the sources.  will need to implement threading 
				//in order for newHeight to work since it depends on t and 
				//t is updated in the thread code
				//if(applet.linearOneEnabled())
				//{
					/*s1 = new LinearSource(this, amp1, wl1,
							ph1,-50f,50f,anB1);
					height[p] = s1.getValue(x, y, t);
				}
				if(applet.linearTwoEnabled()){
					s2 = new LinearSource(this, amp2, wl2,
							ph2, -50f, 50f, anB2);
					height[p] = s2.getValue(x, y, t);
				}
				if(applet.pointOneEnabled()){
					r1 = new RadialSource(this, rAmp1, rWl1, rPh1, rX1, rY1,applet);
					height[p]=r1.getValue(x, y, t,1);
				}
				if(applet.pointTwoEnabled()){
					r2 = new RadialSource(this, rAmp2, rWl2, rPh2, rX2, rY2,applet);
					height[p]=r2.getValue(x, y, t,2);
				}*/
			}

		//height2 and sparseHeight2 DISABLED for now

		//There is unsynchronized access to height/2 and to sparseHeight/2,
		//but this is not a problem as we are only swapping them, not modifying.
		try {
			if(lores) {
				//sparseHeight2=sparseHeight;
				pool.setHeight(sparseHeight/*=newHeight*/);
			} else {
				//height2=height;
				pool.setHeight(height/*=newHeight*/);
				//if(!playing) applet.getStatusBar().setText(null);
			}
			pool.applyRenderingMode();
		} catch(OutOfMemoryError fake) {}
		catch(ClassCastException fake) {}
	}
	public void getFBValues(){
		//pull the values anyway for each update?
		amp1 = applet.linearOneAmplitude.getValue();
		wl1 = applet.linearOneWavelength.getValue();
		ph1 = applet.linearOnePhase.getValue();
		anB1 = applet.linearOneAngle.getValue();
		amp2 = applet.linearTwoAmplitude.getValue();
		wl2 = applet.linearTwoWavelength.getValue();
		ph2 = applet.linearTwoPhase.getValue();
		anB2 = applet.linearTwoAngle.getValue();
		rAmp1 = applet.pointOneAmplitude.getValue();
		rWl1 = applet.pointOneWavelength.getValue();
		rPh1 = applet.pointOnePhase.getValue();
		rX1 = applet.pointOneX.getValue();
		rY1 = applet.pointOneY.getValue();
		rAmp2 = applet.pointTwoAmplitude.getValue();
		rWl2 = applet.pointTwoWavelength.getValue();
		rPh2 = applet.pointTwoPhase.getValue();
		rX2 = applet.pointTwoX.getValue();
		rY2 = applet.pointTwoY.getValue();
	}
	
	private boolean betweenFrames;
	public synchronized boolean isBetweenFrames() {return betweenFrames;}

	public void run() {
		long time;
		playing = false;
		done = false;

		while(!done) {
			time = System.currentTimeMillis();
			if(!done) update();
			else return;
			try {
				time = updateInterval - System.currentTimeMillis() + time;
				if(time<minUpdateInterval) time = minUpdateInterval;
				sleep(time);
				synchronized (this) {
					betweenFrames=true;
					while(!playing && !done) wait();
					betweenFrames=false;
				}
			}
			catch(InterruptedException e) {return;}
			t+=tStep;
		}
	}
	
	public void play() {
		if(playing) return;
		wasPlaying = playing;
		playing = true;
		synchronized(this) {notify();}
	}

	public void pause() {
		wasPlaying = playing;
		playing = false;
	}
	
	public void prevFrame() {
		if(!playing) {
			t -= tStep;
			update();
		}
	}

	public void nextFrame() {
		if(!playing) {
			t += tStep;
			update();
		}
	}

	public void exit() {
		destroy();
		done = true;
	}

	public boolean isPlaying() {return playing;}

	public int getResolution() {return resolution;}

	public void setNormalPerVertex(boolean normal) {
		if(playing) pause();
		//statusBar.setText("Working...");
		pool.setNormalPerVertex(normal);
		normalPerVertex = pool.getNormalPerVertex();
		//statusBar.reset();
		if(!wasPlaying && autoUpdate) update();
		else play();
	}

	public boolean getNormalPerVertex() {
		return pool.getNormalPerVertex();
	}
	
	public void setPoolOptions(int res, boolean normalPerVertex) {
		if(res<50 || res>400) return;
		pause();
		pool.setOptions(res, normalPerVertex);
		resolution = pool.getResolution();
		normalPerVertex = pool.getNormalPerVertex();
		spacing = pool.getSpacing();

		height = new float[resolution*resolution];

		//statusBar.reset();
		if(wasPlaying) play();
		else if(autoUpdate) update();
		//statusBar.setText("Working...");

		/*pool.setOptions(res, normalPerVertex);
		resolution = pool.getResolution();
		normalPerVertex = pool.getNormalPerVertex();
		spacing = pool.getSpacing();

		//height = null;
		height = new float[resolution*resolution];

		//statusBar.setText(null);
		//if(wasPlaying) play();
		else if(autoUpdate) update();*/
	}
	
	private void clear() {
		if(playing) pause();
		wasPlaying = false;

		autoUpdate = false;
		pool.reset();

		resolution = pool.getResolution();
		spacing = pool.getSpacing();
		sparseResolution = pool.getSparseResolution();
		sparseSpacing = pool.getSparseSpacing();

		height = new float[resolution*resolution];
		sparseHeight = new float[sparseResolution*sparseResolution];
	}

	public void reset() {
		clear();

		autoUpdate = true;

		update();
	}
	
	public void setGridVisible(boolean gridV){
		pool.setGridVisible(gridV);
	}
}
