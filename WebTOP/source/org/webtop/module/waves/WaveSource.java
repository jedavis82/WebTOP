package org.webtop.module.waves;

import org.web3d.x3d.*;
import org.web3d.x3d.sai.*;
import org.webtop.x3d.*;


public abstract class WaveSource {

	protected static final float WAVE_SPEED = 1;
	protected float amplitude;
	protected float wavelength;
	protected float phase;
	protected float k;
	
	protected SFFloat set_amplitude;
	protected SFFloat set_wavelength;
	protected SFFloat set_phase;

	public static final float MAX_AMPLITUDE = 10.0f;
	public static final float MAX_WAVELENGTH = 50.0f;
	//For reasons of simplicity and widget sensitivity, we only use half of the
	//possible range of phases:
	public static final float MAX_PHASE = (float) Math.PI;
	
	private SAI sai;

	public static final int TYPE_NONE = 0;
	public static final int TYPE_LINEAR = 1;
	public static final int TYPE_RADIAL = 2;
	
	public WaveSource(Engine e, float A, float L, float E, float x, float y) {
		amplitude = A;
		wavelength = L;
		phase = E;
		k = (float) (2 * Math.PI / wavelength);
		//sai = w.getSAI();  //may not need this either
		//may not need these to update the pool heights with
		/*set_amplitude = (SFFloat) sai.getField(getNode(), "set_amplitude");getEI(getNode(),"set_amplitude");
		set_wavelength = (SFFloat) eai.getEI(getNode(),"set_wavelength");
		set_phase = (SFFloat) eai.getEI(getNode(),"set_phase");*/

	}
	
	public float getAmplitude() {return amplitude;}
	public float getWavelength() {return wavelength;}
	public float getPhase() {return phase;}
	
	
}
