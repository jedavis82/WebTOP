package org.webtop.module.twoslitphoton;



import java.awt.*;
import java.awt.event.*; 

import javax.swing.*; 

import org.web3d.x3d.sai.*;
import org.web3d.x3d.*; 
import org.sdl.gui.numberbox.*; 
import org.sdl.gui.*;
import org.sdl.math.FPRound;
import org.webtop.wsl.client.*; 
import org.webtop.wsl.event.*;
import org.webtop.util.*;
import org.webtop.util.script.*;
import org.webtop.x3d.output.*; 
import org.webtop.x3d.widget.*;

import org.webtop.component.*;
import org.webtop.wsl.script.*;

public class TwoSlitPhoton extends WApplication implements 
	ActionListener, NumberBox.Listener, AdjustmentListener, X3DFieldEventListener{
	
	//Various variables used in the animation part of the module
	private Animation animation; 
	private Engine engine;
	private Data data; 
	
	//Create the data class here
	public static final class Data implements Animation.Data, Cloneable{
		public float wavelength = 0f; 
		public float width = 0f; 
		public float distance = 0f; 
		
		public Animation.Data copy(){
			try{
				return((Data)clone()); //all data is primitive, so clone is fine
				
			}
			catch(CloneNotSupportedException e){
				return null; //can't happen
			}
		}
	}
	
	//Global Vars
	//Constants
	private static final float	WAVELENGTH_DEF=550;
	private static final float	WAVELENGTH_MIN=400;
	private static final float	WAVELENGTH_MAX=800;
	private static final int	RATE_DEF=40;
	private static final int	RATE_MIN=1;
	private static final int	RATE_MAX=5000;
	private static final float	WIDTH_DEF=0.04f;
	private static final float	WIDTH_MIN=0f;
	private static final float	WIDTH_MAX=1f;
	private static final float	DISTANCE_DEF=0.25f;
	private static final float	DISTANCE_MIN=0f;
	private static final float	DISTANCE_MAX=1f;
	private static final float	EXPOSURE_DEF=20;
	private static final float	EXPOSURE_MIN=1;
	private static final float	EXPOSURE_MAX=60;//was 30
	//private static final int ANIMATION_PERIOD=50;
	private static final int ANIMATION_PERIOD=100;
	private static final int PLOT_RES = 1500;
	private static final float PLOT_SCALE = 250;
	private static final int DEF_BRIGHTNESS = 5;
	private static final int MAX_BRIGHTNESS = 10;
	private static final int STOPPED = 0;
	private static final int PLAYING = 1;
	private static final int PAUSED = 2;
	private static final int UPDATE=0;
	private static final int NO_UPDATE=1;
	
	private int animationState = STOPPED;
	
	//Slit Stuffs
	private BoxMaker slitEngine;					//slit geometry creator
	//private NSlitDragger nslitDraggerEngine;//JD	//slit widget coordinator
	private SFFloat	nsd_set_width;
	private SFFloat	nsd_set_distance;
	
	//	UI elements
	private FloatBox wavelengthField;
	private IntBox rateField;
	private FloatBox widthField;
	private FloatBox distanceField;
	private FloatBox exposureTimeField;
	private JButton resetButton;
	private JButton clearButton;
	private JButton animationButton;
	private StateButton audioButton;
	private JLabel photonCount;
	private JLabel elapsedTime;
	private JScrollBar brightnessScrollbar;
	private ToggleButton widgetsButton;
	Switch WidgetsSwitch;
	private static float brightnessValue;
	
	//GUI Scripting Elements
	private NumberBoxScripter wavelengthScripter;
	private NumberBoxScripter rateScripter;
	private NumberBoxScripter widthScripter;
	private NumberBoxScripter distanceScripter;
	private NumberBoxScripter exposureTimeScripter;
	private ButtonScripter resetScripter;
	private ButtonScripter clearScripter;
	private ButtonScripter animationScripter;
	private StateButtonScripter audioScripter;
	private StateButtonScripter widgetsScripter;
	
	private ScalarScripter wavelengthWidgetScripter;
	private ScrollbarScripter brightnessScripter;
	//X3D Stuffs
	private LinePlot plot;
	private SFFloat transparency;
	private WheelWidget wavelengthWidget;
	private SFVec3f slit1Scale, slit2Scale, slit3Scale;
	private SFVec3f slit1Translation, slit2Translation, slit3Translation;
	private ScalarCoupler wavelengthCoupler;
	
	public TwoSlitPhoton(String title, String world)
	{
		super(title, world, true, false);
	}
	
	protected String getAuthor() {
		return "Jeremy Davis";
	}

	protected String getDate() {
		return null;
	}

	protected Component getFirstFocus() {
		return wavelengthField;
	}

	protected int getMajorVersion() {
		return 6;
	}

	protected int getMinorVersion() {
		return 1;
	}

	protected String getModuleName() {
		return "TwoSlitPhoton";
	}

	protected int getRevision() {
		return 1;
	}

	protected void setupGUI() {
		JPanel panel;
		panel = new JPanel();
		JPanel panel2 = new JPanel(); 
		JPanel panel3 = new JPanel();
		controlPanel.setLayout(new GridLayout(3,1));
		
		photonCount = new JLabel("   0");
		elapsedTime =new JLabel("   0.0");
		resetButton = new JButton("Reset"); 
		clearButton= new JButton("Clear Screen"); 
		animationButton = new JButton(" Play ");
		audioButton =new StateButton("Audio ", new String[] {"",""},new String[]{"On","Off"});
		widgetsButton = new ToggleButton("Hide Widgets", "Show Widgets", false);
		widgetsButton.addListener(new StateButton.Listener(){
			public void stateChanged(StateButton sb, int state){
				WidgetsSwitch.setVisible(widgetsButton.getStateBool());
			}
		});
		panel.add(new JLabel("        Wavelength:",JLabel.RIGHT));
		panel.add(wavelengthField=new FloatBox(WAVELENGTH_MIN,WAVELENGTH_MAX,45,5));
		panel.add(new JLabel("nm"));
		
		panel.add(new JLabel("Width:",JLabel.RIGHT));
		panel.add(widthField=new FloatBox(WIDTH_MIN,WIDTH_MAX,45,5));
		panel.add(new JLabel("mm"));
		
		panel.add(new JLabel("Distance:",JLabel.RIGHT));
		panel.add(distanceField=new FloatBox(DISTANCE_MIN,DISTANCE_MAX,45,5));
		panel.add(new JLabel("mm   "));
		
		panel.add(new JLabel("Photons/sec:",JLabel.RIGHT));
		panel.add(rateField=new IntBox(RATE_MIN,RATE_MAX,45,5));
		
		panel2.add(new JLabel("Exposure time:",JLabel.RIGHT));
		panel2.add(exposureTimeField=new FloatBox(EXPOSURE_MIN,EXPOSURE_MAX,20,5));
		panel2.add(new JLabel("sec"));
	
		panel2.add(new JLabel("Photons:",JLabel.RIGHT) );
		photonCount.setHorizontalAlignment(JLabel.RIGHT);
		panel2.add(photonCount);
		
		panel2.add(new JLabel("Elapsed time:",JLabel.RIGHT) );
		elapsedTime.setHorizontalAlignment(JLabel.RIGHT);
		panel2.add(elapsedTime);
		panel2.add(new JLabel("sec"));
		
		panel3.add(animationButton);
		panel3.add(clearButton);
		panel3.add(resetButton);
		panel3.add(audioButton);
		panel3.add(widgetsButton);
		
		panel2.add(new JLabel("Brightness:",JLabel.RIGHT) );
		brightnessScrollbar=new JScrollBar(JScrollBar.HORIZONTAL,DEF_BRIGHTNESS,1,1,MAX_BRIGHTNESS);
		brightnessScrollbar.addAdjustmentListener(this);
		panel2.add(brightnessScrollbar);
		
		//add(panel, BorderLayout.CENTER);
		controlPanel.add(panel, BorderLayout.NORTH);
		controlPanel.add(panel2, BorderLayout.CENTER);
		controlPanel.add(panel3, BorderLayout.SOUTH);
		setDefaults();
		
		
		//Add Listeners
		wavelengthField.addNumberListener(this);
		rateField.addNumberListener(this);
		widthField.addNumberListener(this);
		distanceField.addNumberListener(this);
		exposureTimeField.addNumberListener(this);
		animationButton.addActionListener(this);
		resetButton.addActionListener(this);
		clearButton.addActionListener(this);
		
		audioButton.addListener(new StateButton.Listener(){
			public void stateChanged(StateButton sb, int state){
				Engine.muted = !Engine.muted;
			}
		});
		
		//Couplers and Scripters
		wavelengthCoupler = new ScalarCoupler(wavelengthWidget,wavelengthField,1);
		wavelengthScripter = new NumberBoxScripter(wavelengthField,getWSLPlayer(),null, "wavelength",new Float(WAVELENGTH_DEF));
		rateScripter = new NumberBoxScripter(rateField, getWSLPlayer(), null, "photonRate", new Float(RATE_DEF));
		widthScripter = new NumberBoxScripter(widthField, getWSLPlayer(), null, "slitWidth", new Float(WIDTH_DEF));
		distanceScripter = new NumberBoxScripter(distanceField, getWSLPlayer(), null, "slitDistance",new Float(DISTANCE_DEF));
		exposureTimeScripter = new NumberBoxScripter(exposureTimeField, getWSLPlayer(), null, "exposureTime", new Float(EXPOSURE_DEF));
		animationScripter = new ButtonScripter(animationButton, getWSLPlayer(), null, "animation");
		resetScripter = new ButtonScripter(resetButton, getWSLPlayer(), null, "reset");
		clearScripter = new ButtonScripter(clearButton, getWSLPlayer(), null, "clearScreen");
		wavelengthWidgetScripter = new ScalarScripter(wavelengthWidget, getWSLPlayer(),null, "wavelengthdrag", WAVELENGTH_DEF);
		audioScripter=new StateButtonScripter(audioButton, getWSLPlayer(), null, "audio", new String[] {"On","Off"},1);
		brightnessScripter = new ScrollbarScripter(brightnessScrollbar,getWSLPlayer(),null,
				"brightnessScrollbar",DEF_BRIGHTNESS,null);
		widgetsScripter = new StateButtonScripter(widgetsButton, getWSLPlayer(),null, 
				"hideWidgets", new String[]{"Hide Widgets", "Show Widgets"},0);
		
		
		
		//Set up the toolbar
		ToolBar toolbar = getToolBar();
        toolBar.addBrowserButton("Directions", "/org/webtop/html/twoslit/directions.html");
        toolBar.addBrowserButton("Theory", "/org/webtop/html/twoslit/theory.html");
        toolBar.addBrowserButton("Examples", "/org/webtop/html/twoslit/examples.html");
        toolBar.addBrowserButton("Exercises", "/org/webtop/html/twoslit/exercises.html");
        toolBar.addBrowserButton("Images", "/org/webtop/html/twoslit/images.html");
        toolBar.addBrowserButton("About", "/org/webtop/html/license.html");
		setDefaults();
	}

	protected void setupMenubar() {

	}

	protected void setupX3D() {
		
		plot=new LinePlot(new IndexedSet(getSAI(),getSAI().getNode("ilsNode")),1000,PLOT_RES);
		transparency = (SFFloat) getSAI().getOutputField(getSAI().getNode("TRANS_WORKER"), 
				"transparency_in" ,this, "transparency");
		setBrightness(DEF_BRIGHTNESS);
		
		
		wavelengthWidget=new WheelWidget(getSAI(),getSAI().getNode("WavelengthWidget"),(short)2,"Turn to change the wavelength.");
		wavelengthWidget.addListener(this);
		
		WidgetsSwitch = new Switch(getSAI(), getSAI().getNode("wavelengthWidgetSwitch"), 1);
		//getManager().addHelper(new ScalarCoupler(wavelengthWidget,wavelengthField,1,new ScalarCoupler.Converter(Lambda.linear(0.04f,0f),Lambda.linear(1,0f))));
		
		//All of these calls were interfering with the loading of the GUI. Put them inside of 
		//setDefaults() and called the method when the GUI finished loading. JD
		/*engine = new Engine(this,getSAI());
		slitEngine = new BoxMaker(getSAI()); //testing (seems to be okay) JD
		//nslitDraggerEngine = new NSlitDragger(getSAI()); //JD
		animation = new Animation(engine,data,ANIMATION_PERIOD); //testing JD */
		//engine.setRate(RATE_DEF);//testing JD
		//engine.updateScreen();
		
		//get the slit values for nasty hacking
		slit1Translation = (SFVec3f)getSAI().getField("slit1", "translation");
		slit2Translation = (SFVec3f)getSAI().getField("slit2", "translation");
		slit3Translation = (SFVec3f)getSAI().getField("slit3", "translation");
		slit1Scale = (SFVec3f)getSAI().getField("slit1", "scale");
		slit2Scale = (SFVec3f)getSAI().getField("slit2", "scale");
		slit3Scale = (SFVec3f)getSAI().getField("slit3", "scale");
	}
	
	//MODULE SPECIFIC METHODS
	protected void setDefaults() {
		data = new Data();
		data.wavelength=WAVELENGTH_DEF;
		data.width=WIDTH_DEF;
		data.distance=DISTANCE_DEF;
		
		//Moved from setupX3D() because they were causing the GUI to not load properly
		engine = new Engine(this,getSAI());
		slitEngine = new BoxMaker(getSAI()); //testing (seems to be okay) JD
		//nslitDraggerEngine = new NSlitDragger(getSAI()); //JD
		animation = new Animation(engine,data,ANIMATION_PERIOD); //testing JD
		engine.updateScreen();
		
		animation.setData(data);
		animation.setPaused(true);
		animation.setPlaying(true);
		
		
	}
	
	protected void setWidgetsEnabled(boolean yes){}
	protected void setGUIEnabled(boolean yes){}
	
	public void setDefaultValues() {
		wavelengthField.setValue(WAVELENGTH_DEF);
		widthField.setValue(WIDTH_DEF);
		distanceField.setValue(DISTANCE_DEF);
		rateField.setValue(RATE_DEF);
		exposureTimeField.setValue(EXPOSURE_DEF);
		brightnessValue = 5;
	}
	
	
protected void startup() {
		
		// update the nslitDragger
		/*nslitDraggerEngine.n = 2;
		nslitDraggerEngine.evaluate(true, true);
		nslitDraggerEngine.setMode(NSlitDragger.NSLIT);
		nslitDraggerEngine.updateWidthDraggerConstraints();
		nslitDraggerEngine.updateDistanceDraggerConstraints();	
		
		// width
		nsd_set_width = (EventInSFFloat) getEAI().getEI("nslitDragger","width");
		getEAI().getEO("nslitDragger","width",this,new WidgetEvent(WidgetEvent.WIDTH,WidgetEvent.MOUSE_DRAG));

		getEAI().getEO("nslitDragger","wd_isOver_changed",this,new WidgetEvent(WidgetEvent.WIDTH,WidgetEvent.MOUSE_OVER));
		getEAI().getEO("nslitDragger","wd_isActive_changed",this,new WidgetEvent(WidgetEvent.WIDTH,WidgetEvent.MOUSE_CLICK));

		// distance
		nsd_set_distance = (EventInSFFloat) getEAI().getEI("nslitDragger","distance");
		getEAI().getEO("nslitDragger","distance",this,new WidgetEvent(WidgetEvent.DISTANCE,WidgetEvent.MOUSE_DRAG));

		getEAI().getEO("nslitDragger","dd_isOver_changed",this,new WidgetEvent(WidgetEvent.DISTANCE,WidgetEvent.MOUSE_OVER));
		getEAI().getEO("nslitDragger","dd_isActive_changed",this,new WidgetEvent(WidgetEvent.DISTANCE,WidgetEvent.MOUSE_CLICK));
		
		// is this it??
		slitEngine.evaluate();
		
		//evaluatePlot();
		 */
	}

	//Change the width of the slits
	boolean setWidth(float width) {
		//////////////////////////////////////////////////////////////////////////
		// update the slit aperture
		/*float oldWidth=slitEngine.slitWidth;
		slitEngine.slitWidth = width * 1000.0f;
		if(!slitEngine.evaluate()) {
			slitEngine.slitWidth=oldWidth;
			//setFromTextField=false;
			System.out.println("Returning false");
			return false;
		}*/
		//////////////////////////////////////////////////////////////////////////
		// update the draggers
		//nsd_set_width.setValue(width * 1000.0f);
		
//		SFVec3f slit1Translation = (SFVec3f)getSAI().getField("slit1", "translation");
//		float[] values = new float[3];
//		slit1Translation.getValue(values);
//		System.out.println("Values: " + values[0] + "," + values[1] + "," + values[2]);
//		values[0] += (width*1000f)/2f; //may be 1000 or 100...keep working from here
//		System.out.println("Values after: " + values[0] + "," + values[1] + "," + values[2]);
//		slit1Translation.setValue(values);
		
		//Think going to have to change the scale each time setWidth and setDistance is called
		//Need to figure out which slit to change when. [JD]
		float wNew = widthField.getValue();
		float dNew = distanceField.getValue();
		float a = Math.abs(dNew - wNew);
		float[] values = new float[3];
		slit3Scale.getValue(values);
		//going to have to subtract this from the max value or something...look what happens when you set width to 0
		//also look what happens when you set width to 1
		//values[0] = 350f + (width*1000f)/4f; 
		/*if(wNew == .04f)
			values[0] =350f;
		else*/
			values[0] = 0f + 350f*25.3f*a/4.4f/*0.21f*/;
		slit3Scale.setValue(values);
		
		return true;
	}

	////////////////////////////////////////////////////////////////////////////
	// Change the distance between the slits
	boolean setDistance(float distance) {
		//////////////////////////////////////////////////////////////////////////
		// update the slit aperture
//		float oldDistance=slitEngine.distance;
//		slitEngine.distance = distance * 1000.0f;
//		if(!slitEngine.evaluate()) {
//			slitEngine.distance=oldDistance;
//			//setFromTextField=false;
//			return false;
//		}
		//////////////////////////////////////////////////////////////////////////
		// update the draggers
		//nsd_set_distance.setValue(distance * 1000.0f);
		/*SFVec3f slit1Scale = (SFVec3f)getSAI().getField("slit1","scale");
		slit1Scale.setValue(new float[]{distance*1000f,0f,0f});*/
		
		float bDefault = .5f*(1 - .25f - .04f);
		float b = .5f*(1 - distanceField.getValue() - widthField.getValue());
		float scaleVal = 350f * b/0.355f;
		float transVal = 325f + (325f / 0.355f) * (.5f*(bDefault - b));
		float scaleValues[] = new float[3];
		float transValues[] = new float[3];
		slit2Scale.getValue(scaleValues);
		slit2Translation.getValue(transValues); 
		scaleValues[0] = scaleVal; 
		transValues[0] = transVal; 
		slit2Scale.setValue(scaleValues);
		slit2Translation.setValue(transValues);
		
		slit1Scale.setValue(scaleValues);
		transValues[0]=-1*transValues[0];
		slit1Translation.setValue(transValues);
		setWidth(widthField.getValue());
		return true;
	}

	//EVALUATE AND UPDATE THE LINE PLOT
	public void evaluatePlot() {
		float plotValues[] = new float[PLOT_RES];
		float xstep = 20000/(float)PLOT_RES;
		float x = -10000;
		for (int q=0; q<PLOT_RES; q++) {
			plotValues[q] = PLOT_SCALE*Engine.compute2SlitIntensity(wavelengthField.getValue()*1000,widthField.getValue(),x*1000,1000,distanceField.getValue());
			//System.out.println("Plot Value: " + plotValues[q]);
			x+=xstep;
		}
		plot.setValues(plotValues);
	}
	//ANIMATION METHODS
	public void setPlaying() {
		exposureTimeField.setEnabled(false);
		setState(PLAYING);
		animationButton.setText("Pause");
		animation.setPaused(false);	
	}
	public void setPaused() {
		setState(PAUSED);
		animationButton.setText("Resume");
		animation.setPaused(true);
	}
	public void setStopped() {
		exposureTimeField.setEnabled(true);
		setState(STOPPED);
		animationButton.setText("Play");
		animation.setPaused(true);
		engine.reset();
		engine.updateScreen();
		if(audioButton.getState() == 0)
			Engine.muted = true;
		else if(audioButton.getState() == 1)
			Engine.muted = false;
		enableNumberBoxes();
	}
	
	public int getState() {
		return animationState;
	}

	public void setState(int st) {
		animationState = st;
	}
	
	public void setBrightness(float value) {
		transparency.setValue(((float)MAX_BRIGHTNESS-value)/(float)MAX_BRIGHTNESS);
	}
	
	public void setElapsedTime(float time) {
		elapsedTime.setText(String.valueOf(FPRound.toFixVal(time,1)));
	}
	public void setPhotonCount(int count) {
		photonCount.setText(String.valueOf(count));
	}
	public float getExposureTime() {
		return exposureTimeField.getValue();
	}	
	//END MODULE SPECIFIC METHODS
	
	/*
	 * EVENT HANDLING METHODS
	 */
	public void invalidEvent(String node, String event) {

	}
	//implement actionListener
	public void actionPerformed(ActionEvent e){
		if(e.getSource().equals(animationButton)){
			if (getState()==STOPPED) {
				setPlaying();
				disableNumberBoxes();
			}
			else if (getState()==PLAYING) {
				setPaused();
				
				Engine.stopPlayer();
				enableNumberBoxes();
			}
			else if (getState()==PAUSED) {
				setPlaying();
				disableNumberBoxes();
			}
		}
		else if(e.getSource().equals(clearButton)){
			engine.clearScreen(UPDATE);
		}
		else if(e.getSource().equals(resetButton)){
			setStopped();
			engine.reset();
			engine.clearScreen(UPDATE);
			setDefaultValues();
			getStatusBar().clearWarning();
			enableNumberBoxes();
			audioButton.setState(0);
			Engine.muted = true;
			Engine.stopPlayer();
			brightnessScrollbar.setValue(DEF_BRIGHTNESS);
		}
	}
	
	//Don't allow the users to change the parameters if the module is playing
	public void disableNumberBoxes(){
		wavelengthField.setEnabled(false);
		distanceField.setEnabled(false);
		widthField.setEnabled(false);
		exposureTimeField.setEnabled(false);
		rateField.setEnabled(false);
		wavelengthWidget.setEnabled(false);
	}
	//Allow the user to use the number boxes if the module is not playing
	public void enableNumberBoxes(){
		wavelengthField.setEnabled(true);
		distanceField.setEnabled(true);
		widthField.setEnabled(true);
		exposureTimeField.setEnabled(true);
		rateField.setEnabled(true);
		wavelengthWidget.setEnabled(true);
	}
	public void hitReset()
	{
		System.out.println("Hitting Reset");
		resetButton.doClick();
	}
	public void hitPlay()
	{
		System.out.println("Hitting Play");
		animationButton.doClick();
	}
	//implement NumberBox.Listener
	public void invalidEntry(NumberBox source, Number badVal){
		getStatusBar().setWarningText("Invalid Entry");
	}
	public void numChanged(NumberBox source, Number num){
		if (source == wavelengthField) {
			getStatusBar().clearWarning();
			data.wavelength=num.floatValue();
			if (!wavelengthWidget.isActive())
				wavelengthWidget.setValue(data.wavelength);
		}
		else if (source == widthField) {
			if((Float)num <= distanceField.getValue()){
				getStatusBar().clearWarning();
				data.width=num.floatValue();
				//slitEngine.slitWidth=newVal.floatValue();
				//slitEngine.evaluate();
				setWidth(num.floatValue());
			}
			else{
				invalidEntry(source, num);
			}
		}
		else if (source == distanceField) {
			getStatusBar().clearWarning();
			data.distance=num.floatValue();
			//slitEngine.distance=nslitDraggerEngine.distance;
			//slitEngine.evaluate();
			setDistance(num.floatValue());
		}
		else if (source == rateField) {
			getStatusBar().clearWarning();
			engine.setRate(num.intValue());
		}
		animation.setData(data);
		if (!animation.isPaused())
			animation.update();
		evaluatePlot();

	}
	public void boundsForcedChange(NumberBox source, Number num){
		
	}
	
	//implement adjustmentListener
	//@Override
	public void adjustmentValueChanged(AdjustmentEvent evt) {
		System.out.println("brightness:" + evt.getValue());
		setBrightness(evt.getValue());
		brightnessValue = evt.getValue();
	}
	
	public static float getBrightnessValue()
	{
		return brightnessValue;
	}
	
	//implement X3DFieldEventListener
	//@Override
	public void readableFieldChanged(X3DFieldEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	/*END EVENT HANDLING
	 */
	
	
	//NATIVE WAPPLICATION METHODS
	public String getWSLModuleName() {
		return "twoslit";
	}
	
	protected void toWSLNode(WSLNode node) {
		super.toWSLNode(node);
		wavelengthScripter.addTo(node);
		rateScripter.addTo(node);
		widthScripter.addTo(node);
		distanceScripter.addTo(node);
		exposureTimeScripter.addTo(node);
		//resetScripter.addTo(node);
		//clearScripter.addTo(node);
		//animationScripter.addTo(node);
		//wavelengthWidgetScripter.addTo(node);
		audioScripter.addTo(node);
	}
	//END NATIVE WAPP METHODS
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TwoSlitPhoton twoslitphoton = new TwoSlitPhoton("TwoSlitPhoton", 
				"/org/webtop/x3dscene/twoslit_test.x3dv");
		//Dr. Foley and Jeremy's Final Nasty Hack pt 2.
		twoslitphoton.hitPlay();
		twoslitphoton.hitReset();
		System.out.println(" Directory: " + System.getProperty("user.dir"));
		//JOptionPane.showMessageDialog(null, System.getProperty("user.dir"));
		//End Nastiness
	}
}
