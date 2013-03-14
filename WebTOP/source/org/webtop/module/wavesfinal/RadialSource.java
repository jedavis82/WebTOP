package org.webtop.module.wavesfinal;

import org.webtop.util.*;

public class RadialSource extends WaveSource {

	private float beta;
	private WavesFinal applet;
	private int which;
	public RadialSource(Engine e,float A, float L, float E, float x, float y, WavesFinal ap) {
		super(e, A, L, E, x, y);
		beta = k * 0.01f * wavelength;
		applet = ap;
		//createVRMLNode();
	}
	
	public float getValue(float x, float y, float t, int _which) {
		which = _which;
		if(k*R(x, y, which) <= 6.0f) {
			return 4.0f * (float) (U1(x, y) * Math.cos(k*WAVE_SPEED*t - phase) + U2(x,y) * Math.sin(k*WAVE_SPEED*t - phase));
		} else {
			return 4.0f * (float) ( (amplitude/(2*beta)) * (WTMath.j1(beta) * Math.sqrt(2/(Math.PI*k*R(x, y, which)))
				* Math.cos(k * (R(x, y, which) - WAVE_SPEED*t) + phase + (Math.PI/4))));
		}
	}
	
	public void setWavelength(float L, boolean setVRML) {
		//super.setWavelength(L, setVRML);//how to call this in x3d?
		beta = k * 0.01f * wavelength;
	}
	
	private float R(float x, float y, int which) {
		//the X and Y are from the number boxes in the applet
		//if this is for the first radial source
		float X,Y;
		if(which ==1){
			X = applet.pointOneX.getValue();
			Y = applet.pointOneY.getValue();
		}
		else{
			X = applet.pointTwoX.getValue();
			Y = applet.pointTwoY.getValue();
		}
		return (float) Math.sqrt( (X-x)*(X-x) + (Y-y)*(Y-y) );
		//return 0f;
	}

	private float U1(float x, float y) {
		if(R(x, y,which)<=0.01) {
			return (float) (amplitude * (-1.0/(beta*beta)) * (1.0/Math.PI + 0.5*beta * WTMath.j0(k*R(x, y,which))
				* WTMath.y1(beta)));
		} else {
			return (float) (amplitude*(-1/(2*beta))*WTMath.j1(beta)*WTMath.y0(k*R(x, y,which)));
		}
	}

	private float U2(float x, float y) {
		return (float) (amplitude / (2*beta) * WTMath.j1(beta)*WTMath.j0(k*R(x, y,which)));
	}
}
