package org.webtop.module.wavesfinal;


import org.web3d.x3d.sai.*;
import org.webtop.x3d.SAI;
import org.webtop.x3d.X3DObject;
import org.webtop.x3d.output.Switch;

public class Pool extends X3DObject {
	public static final int FULL=1,SPARSE=2;

	private final float size;			// length of side of square

	private int resolution;
	private float spacing;

	private int sparseResolution;
	private float sparseSpacing;

	private int renderingMode=FULL;

	private boolean gridVisible=true;
	private boolean normalPerVertex=true;

	private MFFloat SetHeightEI,SetSparseHeightEI;
	private SFBool SetSparseEI;
	private final Switch gridSwitch;
	
	public Pool(SAI sai,float s) {
		super(sai,sai.getNode("World"));
		gridSwitch=new Switch(sai,sai.getNode("Grid-SWITCH"),2);
		size=s;
		reset();
	}
	
	public void reset() {
		normalPerVertex = true;
		setResolutions(200, 50);
		gridSwitch.setChoice(0);
	}
	
	public void setOptions(int res, boolean isPerVertex) {
		normalPerVertex = isPerVertex;
		setResolution(res);
	}

	public void setNormalPerVertex(boolean isPerVertex) {
		destroy();
		normalPerVertex = isPerVertex;
		createNode();
		
	}

	public boolean getNormalPerVertex() {return normalPerVertex;}

	public void setResolutions(int res1, int res2) {
		System.out.println("Attempting to destroy pool");
		destroy();
		resolution = res1;
		spacing = size/(res1-1);
		sparseResolution = res2;
		sparseSpacing = size/(res2-1);
		createNode();
	}
	
	public void setResolution(int res) {setResolutions(res, sparseResolution);}

	public int getResolution() {return resolution;}

	public float getSpacing() {return spacing;}

	public void setSparseResolution(int res) {setResolutions(resolution, res);}

	public int getSparseResolution() {return sparseResolution;}

	public float getSparseSpacing() {return sparseSpacing;}
	
	public void setHeight(float height[]) {
		try {
			if(renderingMode == FULL) SetHeightEI.setValue(height.length,height);
			else SetSparseHeightEI.setValue(height.length,height);
		} catch(OutOfMemoryError fake) {}
		catch(ClassCastException fake) {}
	}

	//Note -- applyRenderingMode() does the actual work!
	public void setRenderingMode(int mode) {renderingMode=mode;}

	public void applyRenderingMode()
	{SetSparseEI.setValue(renderingMode==SPARSE);}

	public int getRenderingMode() {return renderingMode;}

	public void setGridVisible(boolean visible) {
		gridSwitch.setVisible(gridVisible=visible);
	}

	public boolean getGridVisible() {return gridVisible;}

	/*public void callback(EventOut e, double timestamp, Object data) {
		if(data.equals("mouse_clicked")) {
			float xyz[] = ((EventOutSFVec3f)e).getValue();
			webtop.util.DebugPrinter.println("(x, y, z)=(" + xyz[0] + ", " + xyz[1] + ", " + xyz[2] + ")");
		}
	}*/

	private void createNode(){
		System.out.println("Create Node Pool Called");
		createProto("Pool");
		
		//access the initialize only fields
		SFInt32 res = (SFInt32)getNode().node.getField("resolution");
		SFFloat spac = (SFFloat)getNode().node.getField("spacing");
		MFFloat hght = (MFFloat)getNode().node.getField("height");
		SFInt32 sparseRes = (SFInt32)getNode().node.getField("sparse_resolution");
		SFFloat sparseSpace = (SFFloat)getNode().node.getField("sparse_spacing");
		MFFloat sparseHeight = (MFFloat)getNode().node.getField("sparse_height");
		SFBool norm = (SFBool)getNode().node.getField("normalPerVertex");
		SFVec3f trans = (SFVec3f)getNode().node.getField("translation");
		
		
		//Initialize large grid
		res.setValue(resolution);
		spac.setValue(spacing);
		
		//Initialize sparse grid
		sparseRes.setValue(sparseResolution);
		sparseSpace.setValue(sparseSpacing);
		
		//Move pool
		float values[] = {-size/2,0f,-size/2};		
		for(int i = 0; i < values.length; i++)
			System.out.println("Translation: ["+i+"]: " + values[i]);
		trans.setValue(values);
		
		//Set normal per vertex calculations
		norm.setValue(normalPerVertex);
		
		
		//Attach pool to scene
		place();
		//Bind input fields
		SetHeightEI = (MFFloat) sai.getInputField(getNode(),"set_height");
		SetSparseHeightEI = (MFFloat) sai.getInputField(getNode(),"set_sparse_height");
		SetSparseEI = (SFBool) sai.getInputField(getNode(),"set_sparse");
	}
}
