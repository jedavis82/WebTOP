package org.webtop.module.wavesfinal;

import org.webtop.util.*;

public class LinearSource extends WaveSource {

	private float angle;
	
	public LinearSource(Engine e, float A, float L, float E, float x, float y, float T) {
		super(e, A, L, E, x, y);
		angle = T;
	}
	public float getValue(float x, float y, float t) {
		return (float) (amplitude * Math.cos(k * (x * Math.cos(WTMath.toRads(angle))
				+ y * Math.sin(WTMath.toRads(angle)) - WAVE_SPEED * t) + WTMath.toRads(phase)));
	}
	public float getAngle() {
		return angle;
	}

	public void setAngle(float a, boolean setVRML) {
		angle = a;
		//if(setVRML) set_angle.setValue(angle); //how to use this in x3d?
	}
}
