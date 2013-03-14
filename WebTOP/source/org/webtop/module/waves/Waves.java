package org.webtop.module.waves;

import java.awt.Component;

import org.sdl.gui.numberbox.*;
import org.webtop.component.*;
import org.webtop.util.WTMath;
import org.webtop.util.script.*;
import org.webtop.wsl.script.WSLNode;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.*;
import java.awt.event.*;

public class Waves extends WApplication implements ActionListener, ItemListener, 
NumberBox.Listener, ChangeListener{

	//GUI Elements
	//1st panel in both linear and point source panels:
		//drop down for linear or point source, back arrow button, play button, forward arrow 
		//button, drop down for smooth,etc.., reset and hide grid buttons (if they will fit)
	//second and third row for point source panel: 
		//point source # active checkbox, numberboxes [amplitude, wavelength, phase, x, and y]
	//second and third row for linear source panel: 
		//linear source # active checkbox, nboxes: [amplitude, wavelength, phase, angle]
	
	public JComboBox sourceDrop, resolutionDrop;
	public JButton back, forward,reset, hide;
	public StateButton play;
	public JCheckBox linearOneActive, linearTwoActive, pointOneActive, pointTwoActive;
	public FloatBox linearOneAmplitude, linearOneWavelength, linearOnePhase, pointOneX,
		pointOneY, linearTwoAmplitude, linearTwoWavelength, linearTwoPhase, pointTwoX, 
		pointTwoY, pointOneAmplitude, pointOneWavelength, pointOnePhase, linearOneAngle,
		pointTwoAmplitude, pointTwoWavelength, pointTwoPhase, linearTwoAngle;
	public JPanel entirePanel, linearOnePanel, linearTwoPanel, pointOnePanel, pointTwoPanel,
		linearOneInner, linearTwoInner, pointOneInner, pointTwoInner;
	public JTabbedPane tabbedPane;
	private Engine engine;
	private boolean gridVisible;
	private ResolutionDialog preference;
	
	//Scripting
	private NumberBoxScripter l1AmpScripter, l1WaveScripter, l1PhaseScripter, p1XScripter, p1YScripter,
		l2AmpScripter, l2WaveScripter, l2PhaseScripter, p2XScripter, p2YScripter, p1AmpScripter,
		p1WaveScripter, p1PhaseScripter, l1AngleScripter, p2AmpScripter, p2WaveScripter, 
		p2PhaseScripter, l2AngleScripter;
	private StateButtonScripter playScripter;
	private ButtonScripter resetScripter, hideScripter, backScripter, forwardScripter;
	private ChoiceScripter resolutionScripter;
	private FolderPanelScripter panelScripter; 
	private CheckboxScripter l1Scripter, l2Scripter, p1Scripter, p2Scripter;
	
	private boolean set = false; //used to allow an item listener to be attached to the checkboxes for scripting
	public Waves(String title, String world){
		super(title,world,true,false);
		engine = new Engine(this);
		engine.start();
		engine.setPoolOptions(200, true);
		preference = new ResolutionDialog(this, engine);
	}
	
	protected String getAuthor() {
		// TODO Auto-generated method stub
		return "Jeremy Davis";
	}

	protected String getDate() {
		// TODO Auto-generated method stub
		return null;
	}

	protected Component getFirstFocus() {
		// TODO Auto-generated method stub
		return null;
	}

	protected int getMajorVersion() {
		// TODO Auto-generated method stub
		return 0;
	}

	protected int getMinorVersion() {
		// TODO Auto-generated method stub
		return 0;
	}

	protected String getModuleName() {
		// TODO Auto-generated method stub
		return "waves";
	}

	protected int getRevision() {
		// TODO Auto-generated method stub
		return 0;
	}

	protected void setDefaults() {
		// TODO Auto-generated method stub

	}

	protected void setupGUI() {
		//controlPanel.setLayout(new GridLayout(2,1));
		
		entirePanel = new JPanel();
		linearOnePanel = new JPanel(new VerticalLayout());
		linearOneInner = new JPanel();
		linearTwoPanel = new JPanel(new VerticalLayout());
		linearTwoInner = new JPanel();
		pointOnePanel = new JPanel(new VerticalLayout());
		pointOneInner = new JPanel();
		pointTwoPanel = new JPanel(new VerticalLayout());
		pointTwoInner = new JPanel();
		//only show linear panel one on startup
		/*linearOnePanel.setVisible(true);
		linearTwoPanel.setVisible(false);
		pointOnePanel.setVisible(false);
		pointTwoPanel.setVisible(false);*/
		
		//stuff in entire panel
		JLabel source = new JLabel("Select Source:");
		sourceDrop = new JComboBox();
		sourceDrop.addItemListener(this);
		sourceDrop.addItem("Linear Source 1");
		sourceDrop.addItem("Linear Source 2");
		sourceDrop.addItem("Point Source 1");
		sourceDrop.addItem("Point Source 2");
		back = new JButton("<");
		back.addActionListener(this);
		forward = new JButton(">");
		forward.addActionListener(this);
		play = new StateButton("Play", "Stop", false);
		play.addListener(new StateButton.Listener()
		  {
			public void stateChanged(StateButton sb, int state){
				state = sb.getState();
				if(state==0){
					engine.play();
					back.setEnabled(false);
					forward.setEnabled(false);
				}
				else if(state ==1){
					engine.pause();
					back.setEnabled(true);
					forward.setEnabled(true);
				}
			}
		  }
		);
		reset = new JButton("Reset");
		reset.addActionListener(this);
		hide = new JButton("Hide Grid");
		hide.addActionListener(this);
		JLabel resolution = new JLabel("Resolution: ");
		resolutionDrop = new JComboBox();
		resolutionDrop.addItem("Very Smooth");
		resolutionDrop.addItem("Smooth");
		resolutionDrop.addItem("Medium");
		resolutionDrop.addItem("Fast");
		resolutionDrop.addItem("Fastest");
		resolutionDrop.addItem("Custom...");
		resolutionDrop.setSelectedIndex(1);
		resolutionDrop.addItemListener(this);
		
		//entirePanel.add(source);
		//entirePanel.add(sourceDrop);
		entirePanel.add(back);
		entirePanel.add(play);
		entirePanel.add(forward);
		entirePanel.add(reset);
		entirePanel.add(hide);
		entirePanel.add(resolution);
		entirePanel.add(resolutionDrop);
		
		//stuff for linear panel 1
		JLabel active = new JLabel("Active:");
		linearOneActive = new JCheckBox();
		linearOneActive.addActionListener(this);
		linearOneActive.addItemListener(this);
		JLabel amplitude = new JLabel("Amplitude:");
		JLabel wavelength = new JLabel("Wavelength:");
		JLabel phase = new JLabel("Phase:");
		JLabel angle = new JLabel("Angle:");
		JLabel x = new JLabel("X:");
		JLabel y = new JLabel("Y:");
		linearOneAmplitude = new FloatBox(0,10,4,4);
		linearOneAmplitude.addNumberListener(this);
		linearOneWavelength = new FloatBox(3,50,8,4);
		linearOneWavelength.addNumberListener(this);
		linearOnePhase = new FloatBox(0,WTMath.toDegs(3.1415927f*2),0,4);
		linearOnePhase.addNumberListener(this);
		linearOneAngle = new FloatBox(0,360,0,4);
		linearOneAngle.addNumberListener(this);
		//linearOnePanel.add(entirePanel);
		linearOneInner.add(new JLabel("Active:"));//may not need
		linearOneInner.add(active);
		linearOneInner.add(linearOneActive);
		linearOneInner.add(new JLabel("Amplitude:"));
		linearOneInner.add(linearOneAmplitude);
		linearOneInner.add(new JLabel("Wavelength:"));
		linearOneInner.add(linearOneWavelength);
		linearOneInner.add(new JLabel("Phase:"));
		linearOneInner.add(linearOnePhase);
		linearOneInner.add(new JLabel("Angle:"));
		linearOneInner.add(linearOneAngle);
		
		//stuff for linear panel 2
		linearTwoActive = new JCheckBox();
		linearTwoActive.addActionListener(this);
		linearTwoActive.addItemListener(this);
		linearTwoAmplitude = new FloatBox(0,10,4,4);
		linearTwoAmplitude.addNumberListener(this);
		linearTwoWavelength = new FloatBox(3,50,8,4);
		linearTwoWavelength.addNumberListener(this);
		linearTwoPhase = new FloatBox(0,WTMath.toDegs(3.1415927f*2),0,4);
		linearTwoPhase.addNumberListener(this);
		linearTwoAngle = new FloatBox(0,360,0,4);
		linearTwoAngle.addNumberListener(this);
		//linearTwoPanel.add(entirePanel);
		linearTwoInner.add(new JLabel("Active:"));
		linearTwoInner.add(linearTwoActive);
		linearTwoInner.add(new JLabel("Amplitude:"));
		linearTwoInner.add(linearTwoAmplitude);
		linearTwoInner.add(new JLabel("Wavelength:"));
		linearTwoInner.add(linearTwoWavelength);
		linearTwoInner.add(new JLabel("Phase:"));
		linearTwoInner.add(linearTwoPhase);
		linearTwoInner.add(new JLabel("Angle:"));
		linearTwoInner.add(linearTwoAngle);
		
		//stuff for point panel 1
		//X -50 to 50, ps1: def X=0, def Y= 16
		//ps2 def: X=0, Y= -16
		pointOneActive = new JCheckBox();
		pointOneActive.addActionListener(this);
		pointOneActive.addItemListener(this);
		pointOneAmplitude = new FloatBox(0,10,6,4);
		pointOneAmplitude.addNumberListener(this);
		pointOneWavelength = new FloatBox(3,50,8,4);
		pointOneWavelength.addNumberListener(this);
		pointOnePhase = new FloatBox(0,WTMath.toDegs(3.1415927f*2),0,4);
		pointOnePhase.addNumberListener(this);
		pointOneX = new FloatBox(-50,50,0,4);
		pointOneX.addNumberListener(this);
		pointOneY = new FloatBox(-50,50,16,4);
		pointOneY.addNumberListener(this);
		pointOneInner.add(new JLabel("Active:"));
		pointOneInner.add(pointOneActive);
		pointOneInner.add(new JLabel("Amplitude:"));
		pointOneInner.add(pointOneAmplitude);
		pointOneInner.add(new JLabel("Wavelength:"));
		pointOneInner.add(pointOneWavelength);
		pointOneInner.add(new JLabel("Phase:"));
		pointOneInner.add(pointOnePhase);
		pointOneInner.add(new JLabel("X:"));
		pointOneInner.add(pointOneX);
		pointOneInner.add(new JLabel("Y:"));
		pointOneInner.add(pointOneY);
		
		//stuff for point panel 2
		pointTwoActive = new JCheckBox();
		pointTwoActive.addActionListener(this);
		pointTwoActive.addItemListener(this);
		pointTwoAmplitude = new FloatBox(0,10,6,4);
		pointTwoAmplitude.addNumberListener(this);
		pointTwoWavelength = new FloatBox(3,50,8,4);
		pointTwoWavelength.addNumberListener(this);
		pointTwoPhase = new FloatBox(0,WTMath.toDegs(3.1415927f *2),0,4);
		pointTwoPhase.addNumberListener(this);
		pointTwoX = new FloatBox(-50,50,0,4);
		pointTwoX.addNumberListener(this);
		pointTwoY = new FloatBox(-50,50,-16,4);
		pointTwoY.addNumberListener(this);
		pointTwoInner.add(active);
		pointTwoInner.add(pointTwoActive);
		pointTwoInner.add(amplitude);
		pointTwoInner.add(pointTwoAmplitude);
		pointTwoInner.add(wavelength);
		pointTwoInner.add(pointTwoWavelength);
		pointTwoInner.add(phase);
		pointTwoInner.add(pointTwoPhase);
		pointTwoInner.add(x);
		pointTwoInner.add(pointTwoX);
		pointTwoInner.add(y);
		pointTwoInner.add(pointTwoY);
		
		linearOnePanel.add(linearOneInner);
		linearTwoPanel.add(linearTwoInner);
		pointOnePanel.add(pointOneInner);
		pointTwoPanel.add(pointTwoInner);
		
		tabbedPane = new JTabbedPane();
		tabbedPane.add("Controls", entirePanel);
		tabbedPane.add("Line Source 1", linearOnePanel);
		tabbedPane.add("Line Source 2", linearTwoPanel);
		tabbedPane.add("Point Source 1", pointOnePanel);
		tabbedPane.add("Point Source 2", pointTwoPanel);
		tabbedPane.addChangeListener(this);
		addToConsole(tabbedPane);
		
		/*l1AmpScripter, l1WaveScripter, l1PhaseScripter, p1XScripter, p1YScripter,
		l2AmpScripter, l2WaveScripter, l2PhaseScripter, p2XScripter, p2YScripter, p1AmpScripter,
		p1WaveScripter, p1PhaseScripter, l1AngleScripter, p2AmpScripter, p2WaveScripter, 
		p2PhaseScripter, l2AngleScripter,playScripter*/
		l1AmpScripter = new NumberBoxScripter(linearOneAmplitude,getWSLPlayer(),null,
				"linearOneAmp", 4);
		l1WaveScripter = new NumberBoxScripter(linearOneWavelength, getWSLPlayer(), null, 
				"linearOneWave", 8);
		l1PhaseScripter = new NumberBoxScripter(linearOnePhase, getWSLPlayer(), null, 
				"linearOnePhase", 0);
		p1XScripter = new NumberBoxScripter(pointOneX, getWSLPlayer(), null, 
				"pointOneX", 0);
		p1YScripter = new NumberBoxScripter(pointOneY, getWSLPlayer(), null, 
				"pointOneY", 16);
		l2AmpScripter = new NumberBoxScripter(linearTwoAmplitude, getWSLPlayer(), null,
				"linearTwoAmp", 4);
		l2WaveScripter = new NumberBoxScripter(linearTwoWavelength, getWSLPlayer(), null,
				"linearTwoWave", 8);
		l2PhaseScripter = new NumberBoxScripter(linearTwoPhase, getWSLPlayer(), null, 
				"linearTwoPhase", 0);
		p2XScripter = new NumberBoxScripter(pointTwoX, getWSLPlayer(), null, 
				"pointTwoX", 0);
		p2YScripter = new NumberBoxScripter(pointTwoY, getWSLPlayer(), null, 
				"pointTwoY", -16);
		p1AmpScripter = new NumberBoxScripter(pointOneAmplitude, getWSLPlayer(), null, 
				"pointOneAmp", 6);
		p1WaveScripter = new NumberBoxScripter(pointOneWavelength, getWSLPlayer(), null, 
				"pointOneWave", 8);
		p1PhaseScripter = new NumberBoxScripter(pointOnePhase, getWSLPlayer(), null, 
				"pointOnePhase", 0);
		l1AngleScripter = new NumberBoxScripter(linearOneAngle, getWSLPlayer(), null, 
				"linearOneAngle", 0);
		p2AmpScripter = new NumberBoxScripter(pointTwoAmplitude, getWSLPlayer(), null, 
				"pointTwoAmp", 6);
		p2WaveScripter = new NumberBoxScripter(pointTwoWavelength, getWSLPlayer(), null, 
				"pointTwoWave", 8);
		p2PhaseScripter = new NumberBoxScripter(pointTwoPhase, getWSLPlayer(), null, 
				"pointTwoPhase", 0);
		l2AngleScripter = new NumberBoxScripter(linearTwoAngle, getWSLPlayer(), null, 
				"linearTwoAngle", 0);
		playScripter = new StateButtonScripter(play, getWSLPlayer(), null, "play", 
				new String[]{"Play", "Stop"}, 0);
		resetScripter = new ButtonScripter(reset, getWSLPlayer(),null, "reset");
		hideScripter = new ButtonScripter(hide, getWSLPlayer(), null, "hide");
		backScripter = new ButtonScripter(back, getWSLPlayer(), null, "back");
		forwardScripter = new ButtonScripter(forward, getWSLPlayer(), null, "forward");
		resolutionScripter = new ChoiceScripter(resolutionDrop, getWSLPlayer(), null, "quality", 
				new String[]{"Very Smooth", "Smooth", "Medium", "Fast", "Fastest", "Custom"}, 
				1, this);
		panelScripter = new FolderPanelScripter(tabbedPane, getWSLPlayer(), null, "panel", null);
		l1Scripter = new CheckboxScripter(linearOneActive, getWSLPlayer(), null, "LinearOneActive", false, 
				this);
		l2Scripter = new CheckboxScripter(linearTwoActive, getWSLPlayer(), null, "LinearTwoActive", false, 
				this);
		p1Scripter = new CheckboxScripter(pointOneActive, getWSLPlayer(), null, "PointOneActive", true, this);
		p2Scripter = new CheckboxScripter(pointTwoActive, getWSLPlayer(), null, "PointTwoActive", true, this);
		
		ToolBar toolBar = getToolBar();
		toolBar.addBrowserButton("Directions", "/org/webtop/html/wave/directions.html");
		toolBar.addBrowserButton("Theory","/org/webtop/html/wave/theory.html");
		toolBar.addBrowserButton("Examples","/org/webtop/html/wave/examples.html");
		toolBar.addBrowserButton("Exercises", "/org/webtop/html/wave/exercises.html");
		toolBar.addBrowserButton("Images","/org/webtop/html/wave/images.html");
		toolBar.addBrowserButton("About", "/org/webtop/html/license.html");
		
		setupActiveSources();
	}

	public void setupActiveSources()
	{
		//both point sources should be active initially.  Will have to check, when updating pool,
		//whether or not each active checkbox is clicked. 
		pointOneActive.setSelected(true);
		pointTwoActive.setSelected(true);
		linearOneActive.setSelected(false);
		linearTwoActive.setSelected(false);
		checkNumberBoxes();
		set = true;
	}
	
	public void checkNumberBoxes()
	{
		if(pointOneActive.isSelected()){
			//enable number boxes for point source one
			pointOneAmplitude.setEnabled(true);
			pointOneWavelength.setEnabled(true);
			pointOnePhase.setEnabled(true);
			pointOneX.setEnabled(true);
			pointOneY.setEnabled(true);
		}
		else{
			//disable number boxes for point source one
			pointOneAmplitude.setEnabled(false);
			pointOneWavelength.setEnabled(false);
			pointOnePhase.setEnabled(false);
			pointOneX.setEnabled(false);
			pointOneY.setEnabled(false);
		}
		if(pointTwoActive.isSelected()){
			//enable
			pointTwoAmplitude.setEnabled(true);
			pointTwoWavelength.setEnabled(true);
			pointTwoPhase.setEnabled(true);
			pointTwoX.setEnabled(true);
			pointTwoY.setEnabled(true);
		}
		else{
			//disable
			pointTwoAmplitude.setEnabled(false);
			pointTwoWavelength.setEnabled(false);
			pointTwoPhase.setEnabled(false);
			pointTwoX.setEnabled(false);
			pointTwoY.setEnabled(false);
		}
		if(linearOneActive.isSelected()){
			//enable
			linearOneAmplitude.setEnabled(true);
			linearOneWavelength.setEnabled(true);
			linearOnePhase.setEnabled(true);
			linearOneAngle.setEnabled(true);
		}
		else{
			//disable
			linearOneAmplitude.setEnabled(false);
			linearOneWavelength.setEnabled(false);
			linearOnePhase.setEnabled(false);
			linearOneAngle.setEnabled(false);
		}
		if(linearTwoActive.isSelected()){
			//enable
			linearTwoAmplitude.setEnabled(true);
			linearTwoWavelength.setEnabled(true);
			linearTwoPhase.setEnabled(true);
			linearTwoAngle.setEnabled(true);
		}
		else{
			//disable
			linearTwoAmplitude.setEnabled(false);
			linearTwoWavelength.setEnabled(false);
			linearTwoPhase.setEnabled(false);
			linearTwoAngle.setEnabled(false);
		}
	}
	public boolean linearOneEnabled(){
		return linearOneActive.isSelected();
	}
	public boolean linearTwoEnabled(){
		return linearTwoActive.isSelected();
	}
	public boolean pointOneEnabled(){
		return pointOneActive.isSelected();
	}
	public boolean pointTwoEnabled(){
		return pointTwoActive.isSelected();
	}
	
	public void setDefaultValues(){
		linearOneAmplitude.setValue(4);
		linearOneWavelength.setValue(8);
		linearOnePhase.setValue(0);
		linearOneAngle.setValue(0);
		linearTwoAmplitude.setValue(4);
		linearTwoWavelength.setValue(8);
		linearTwoPhase.setValue(0);
		linearTwoAngle.setValue(0);
		pointOneAmplitude.setValue(6);
		pointOneWavelength.setValue(8);
		pointOnePhase.setValue(0);
		pointOneX.setValue(0);
		pointOneY.setValue(16);
		pointTwoAmplitude.setValue(6);
		pointTwoWavelength.setValue(8);
		pointTwoPhase.setValue(0);
		pointTwoX.setValue(0);
		pointTwoY.setValue(-16);
	}
	
	protected void setupMenubar() {
		// TODO Auto-generated method stub

	}

	protected void setupX3D() {
		// TODO Auto-generated method stub

	}

	public void invalidEvent(String node, String event) {
		// TODO Auto-generated method stub

	}

	public String getWSLModuleName() {
		// TODO Auto-generated method stub
		return "waves";
	}
	
	//public StatusBar getStatusBar(){ return this.getStatusBar();}
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Waves waves = new 
		Waves("Waves", "/org/webtop/x3dscene/wavestest.x3dv");
	}

	//Item and Action and Numberbox listener
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		/*if(e.getSource().equals(linearOneActive)||e.getSource().equals(linearTwoActive)||
				e.getSource().equals(pointOneActive)||e.getSource().equals(pointTwoActive)){
			System.out.println("Checkbox changed");
			checkNumberBoxes();
			engine.update();
		}
		else*/ if(e.getSource().equals(reset)){
			play.setState(0);
			hide.setText("Hide Grid");
			engine.setGridVisible(true);
			setupActiveSources();
			setDefaultValues();
			engine.setPoolOptions(200, true);
			resolutionDrop.setSelectedIndex(1);
			engine.update();
		}
		else if(e.getSource().equals(this.forward)){
			engine.nextFrame();
		}
		else if(e.getSource().equals(this.back)){
			engine.prevFrame();
		}
		else if(e.getSource()==hide) {
			gridVisible = !gridVisible;
			if(gridVisible) 
				hide.setText("Hide Grid");
			else 
				hide.setText("Show Grid");
			engine.setGridVisible(gridVisible);
		}
	}

	public void itemStateChanged(ItemEvent e) {
		Object source = e.getSource();
		if(e.getSource().equals(this.resolutionDrop)){
			int selected = resolutionDrop.getSelectedIndex();
			switch(selected){
			case 0: 
				engine.setPoolOptions(300, true);
				break;
			case 1: 
				engine.setPoolOptions(200, true);
				break;
			case 2: 
				engine.setPoolOptions(150, true);
				break;
			case 3: 
				engine.setPoolOptions(100, true);
				break;
			case 4: 
				engine.setPoolOptions(50, true);
				break;
			case 5: 
				preference.setVisible(true);
			}
		}
		else if(source == linearOneActive || source == linearTwoActive || source == pointOneActive ||
				source == pointTwoActive){
			System.out.println("Source is checkbox");
			if(set == true){
				System.out.println("Set is true");
				checkNumberBoxes();
				engine.update();
			}
		}
	}

	public void boundsForcedChange(NumberBox source, Number oldVal) {
		// TODO Auto-generated method stub
		
	}

	public void invalidEntry(NumberBox source, Number badVal) {
		this.getStatusBar().setWarningText("Invalid entry: " + badVal);
		
	}

	public void numChanged(NumberBox source, Number newVal) {
		this.getStatusBar().clearWarning();
		if(source == linearOneAmplitude || source == linearOneWavelength || source == linearOnePhase
			|| source == pointOneX || source == pointOneY || source == linearTwoAmplitude || 
			source == linearTwoWavelength || source == pointTwoX || source == pointTwoY || 
			source == pointTwoAmplitude || source == pointTwoWavelength || source == pointTwoPhase ||
			source == linearTwoAngle || source == linearTwoPhase || source == pointOneWavelength 
			|| source == pointOnePhase || source == linearOneAngle || source == pointOneAmplitude){
				this.engine.update();
		}
		
	}

	public void stateChanged(ChangeEvent e) {
		System.out.println("State changed");
		set = true;
	}
	
	public void setGridVisible(boolean visible) {
		gridVisible = visible;
		if(visible) 
			hide.setText("Hide Grid");
		else 
			hide.setText("Show Grid");
	}
	
	protected void toWSLNode(WSLNode node){
		super.toWSLNode(node);
		/*l1AmpScripter, l1WaveScripter, l1PhaseScripter, p1XScripter, p1YScripter,
		l2AmpScripter, l2WaveScripter, l2PhaseScripter, p2XScripter, p2YScripter, p1AmpScripter,
		p1WaveScripter, p1PhaseScripter, l1AngleScripter, p2AmpScripter, p2WaveScripter, 
		p2PhaseScripter, l2AngleScripter,playScripter*/
		l1AmpScripter.addTo(node); l1WaveScripter.addTo(node); l1PhaseScripter.addTo(node);
		p1XScripter.addTo(node); p1YScripter.addTo(node); l2AmpScripter.addTo(node);
		l2WaveScripter.addTo(node); l2PhaseScripter.addTo(node); p2XScripter.addTo(node); 
		p2YScripter.addTo(node); p1AmpScripter.addTo(node); p1WaveScripter.addTo(node); 
		p1PhaseScripter.addTo(node); l1AngleScripter.addTo(node); p2AmpScripter.addTo(node);
		p2WaveScripter.addTo(node); p2PhaseScripter.addTo(node); l2AngleScripter.addTo(node);
		playScripter.addTo(node); resolutionScripter.addTo(node); panelScripter.addTo(node);
		l1Scripter.addTo(node); l2Scripter.addTo(node); p1Scripter.addTo(node); p2Scripter.addTo(node);
	}
	
	//Viewpoints
}
