package orthoscape;

import java.awt.Color;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.values.NodeShape;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualPropertyDependency;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;

public class CreateBlueRedPAIVisualStyleAction extends AbstractCyAction {

	private static final long serialVersionUID = 1L;
	private CyApplicationManager cyApplicationManagerServiceRef;
	private VisualStyleFactory visualStyleFactoryServiceRef;
	private VisualMappingManager vmmServiceRef;
	
	private VisualMappingFunctionFactory vmfFactoryD;
	private VisualMappingFunctionFactory vmfFactoryP;
	
	int red;
	int green;
	int blue;
	
	public CreateBlueRedPAIVisualStyleAction(CyApplicationManager cyApplicationManagerServiceRef, VisualMappingManager vmmServiceRef, VisualStyleFactory visualStyleFactoryServiceRef, 
			VisualMappingFunctionFactory vmfFactoryC,VisualMappingFunctionFactory vmfFactoryD,VisualMappingFunctionFactory vmfFactoryP){
		super("PAI Based");
		setPreferredMenu("Apps.Orthoscape.3) Coloring.Blue-Red Style");

		this.cyApplicationManagerServiceRef = cyApplicationManagerServiceRef;
		this.visualStyleFactoryServiceRef = visualStyleFactoryServiceRef;
		this.vmmServiceRef =  vmmServiceRef;
		this.vmfFactoryD	= vmfFactoryD;
		this.vmfFactoryP	= vmfFactoryP;
	}
	
	/**
	 *  DOCUMENT ME!
	 *
	 * @param e DOCUMENT ME!
	 */
	public void actionPerformed(ActionEvent e) {
		
		CyTable nodeTable = cyApplicationManagerServiceRef.getCurrentNetwork().getDefaultNodeTable();
		if(nodeTable.getColumn("PAI") == null){
			JPanel errorpanel = new JPanel();
    		errorpanel.setLayout(new BoxLayout(errorpanel, BoxLayout.Y_AXIS));
    		errorpanel.add(new JLabel("You have to count something in \"Working\" mode."));	
    		JOptionPane.showMessageDialog(null, errorpanel);
    		return;
		}

		// If the style already existed, remove it first
		Iterator<?> it = vmmServiceRef.getAllVisualStyles().iterator();
		while (it.hasNext()){
			VisualStyle curVS = (VisualStyle)it.next();
			if (curVS.getTitle().equalsIgnoreCase("KEGG Blue-Red PAI Style"))
			{
				vmmServiceRef.removeVisualStyle(curVS);
				break;
			}
		}
		
		// Create a new Visual style
		VisualStyle vs= this.visualStyleFactoryServiceRef.createVisualStyle("KEGG Blue-Red PAI Style");
		this.vmmServiceRef.addVisualStyle(vs);

		// Standard visual styles parameter's values
		PassthroughMapping<String, Paint> nodeLabelColorMapping =
				(PassthroughMapping<String, Paint>) this.vmfFactoryP.createVisualMappingFunction     ("Label",  String.class, BasicVisualLexicon.NODE_LABEL_COLOR);		
		PassthroughMapping<String, String> nodeLabelMapping     =
				(PassthroughMapping<String, String>) this.vmfFactoryP.createVisualMappingFunction    ("Label",  String.class, BasicVisualLexicon.NODE_LABEL);
		PassthroughMapping<String, NodeShape> nodeShapeMapping  =
				(PassthroughMapping<String, NodeShape>) this.vmfFactoryP.createVisualMappingFunction ("Shape",  String.class, BasicVisualLexicon.NODE_SHAPE);
		PassthroughMapping<String, Double> nodeHeightMapping    =
				(PassthroughMapping<String, Double>) this.vmfFactoryP.createVisualMappingFunction    ("Height", String.class, BasicVisualLexicon.NODE_HEIGHT);
		PassthroughMapping<String, Double> nodeWidthMapping     =
				(PassthroughMapping<String, Double>) this.vmfFactoryP.createVisualMappingFunction    ("Width",  String.class, BasicVisualLexicon.NODE_WIDTH);
        
		
 		DiscreteMapping<String, Paint> dMapping = (DiscreteMapping<String, Paint>) this.vmfFactoryD.createVisualMappingFunction("PAI", String.class, BasicVisualLexicon.NODE_FILL_COLOR);			

 		CyTable networksTable = cyApplicationManagerServiceRef.getCurrentNetwork().getDefaultNetworkTable();
    	CyColumn netcolumn = networksTable.getColumn("org");
 		List<String> netstorage = netcolumn.getValues(String.class);
 					
 		// Organism's loading to get taxonomy row    	
	    String sURL = "http://www.kegg.jp/dbget-bin/www_bget?" + netstorage.get(0);	
	    String curURLagain;
	    
	    Map<String, String> badOrganisms = new HashMap<String, String>();		// Initialization of names not available to create on Windows
		badOrganisms.put("con", "Bacteria; Proteobacteria; Alphaproteobacteria; Rhodobacterales; Rhodobacteraceae; Confluentimicrobium; Confluentimicrobium sp. EMB200-NS6");
		badOrganisms.put("prn", "Bacteria; Bacteroidetes; Flavobacteriia; Flavobacteriales; Flavobacteriaceae; Polaribacter; Polaribacter reichenbachii 6Alg 8T");
		
	    
		if (badOrganisms.containsKey(netstorage.get(0))){
	    	curURLagain = badOrganisms.get(netstorage.get(0));
	    }
	    else{
		    curURLagain = OrthoscapeHelpFunctions.loadUrl(sURL);
		    
		    String[] curlines = OrthoscapeHelpFunctions.stringFounder(curURLagain, "Definition");	    	
	    	String[] defStr = curlines[1].split("\n");
	    	String defString = defStr[0];
	    	String[] curlinesmore;
	    	curlinesmore = defString.split(">", 4);
	    	defString = curlinesmore[2];
	    	curlinesmore = defString.split("<", 2);
	    	defString = curlinesmore[0].trim();
	    	curURLagain = curlines[1];
	
	    	curlines = OrthoscapeHelpFunctions.stringFounder(curURLagain, "Lineage");
	    	curURLagain = curlines[1];
		 	
			curlinesmore = curURLagain.split(">", 4);
			curURLagain = curlinesmore[2];
		       	
			String[] curlinesless;
			curlinesless = curURLagain.split("<", 2);
			curURLagain = curlinesless[0];	
			curURLagain = curURLagain.trim() + "; " + defString;
	    }
		       	
		String[] alltaxes = curURLagain.split(";");
		List<String> taxstorage = new ArrayList<String>();
		
		for (int t=alltaxes.length-1; t>=0; t--){
			if (t<9){
				taxstorage.add( "0" + String.valueOf(t+1) + "_" + alltaxes[t].trim());
			}
			else{
				taxstorage.add( String.valueOf(t+1) + "_" + alltaxes[t].trim());
			}
		}
		taxstorage.add("00_Cellular Organisms");
					
 		dMapping.putMapValue("It's a path", Color.WHITE);
 		dMapping.putMapValue("It's a compound", Color.WHITE);
 		dMapping.putMapValue("It's Kegg own orthologous group", Color.WHITE);
 		dMapping.putMapValue("No data", Color.WHITE);
 		
		float value; 
		for (int i=0; i<taxstorage.size(); i++){
			value = (float)i/taxstorage.size();
			getHeatMapColor(value);
			dMapping.putMapValue(taxstorage.get(i), new Color(red, green, blue));
		}
 				
		VisualPropertyDependency<?> dependency = null;
		for (VisualPropertyDependency<?> dep : vs.getAllVisualPropertyDependencies()){
            if (dep.getDisplayName().equalsIgnoreCase("Lock node width and height"))
                dependency = dep;
        }
        if (dependency != null && dependency.isDependencyEnabled()){
            dependency.setDependency(false);		
        }
        
        vs.setDefaultValue(BasicVisualLexicon.NODE_LABEL_FONT_SIZE, 7);
        vs.setDefaultValue(BasicVisualLexicon.NODE_FILL_COLOR, Color.WHITE);
        vs.setDefaultValue(BasicVisualLexicon.NODE_HEIGHT, 20d);
        vs.setDefaultValue(BasicVisualLexicon.NODE_WIDTH, 60d);
        
        vs.setDefaultValue(BasicVisualLexicon.NODE_LABEL, "Default node");
        
		vs.addVisualMappingFunction(nodeLabelColorMapping);
		vs.addVisualMappingFunction(nodeLabelMapping);	
		vs.addVisualMappingFunction(nodeShapeMapping);	
		vs.addVisualMappingFunction(nodeHeightMapping);	
		vs.addVisualMappingFunction(nodeWidthMapping);	
		
		vs.addVisualMappingFunction(dMapping);		
		vmmServiceRef.setCurrentVisualStyle(vs);
	}

	public void getValueBetweenTwoFixedColors(float value){
		int aR = 0;   int aG = 0; int aB=255;  // RGB for our 1st color (blue in this case).
		int bR = 255; int bG = 0; int bB=0;    // RGB for our 2nd color (red in this case).
	 
		red   = (int) ((float)(bR - aR) * value + aR);      // Evaluated as -255*value + 255.
		green = (int) ((float)(bG - aG) * value + aG);      // Evaluates as 0.
		blue  = (int) ((float)(bB - aB) * value + aB);      // Evaluates as 255*value + 0.
	}
	
	void getHeatMapColor(float value){
	  int NUM_COLORS = 5;
	  float[][] color = { {0,0,1}, {0.25f,0.35f,0.8f}, {0.8f,0.8f,0.8f}, {0.8f,0.35f,0.25f}, {1,0,0}};
	  // A static array of 5 colors:  (from blue to red} for each.
	  
		// Rainbow
//	  int NUM_COLORS = 7;
//	  float[][] color = { {1,0,0}, {1,0.5f,0}, {1,1,0}, {0,1,0}, {0,1,1}, {0,0,1}, {0.5f,0,1} } ;
	 
	  int idx1;        // |-- Our desired color will be between these two indexes in "color".
	  int idx2;        // |
	  float fractBetween = 0;  // Fraction between "idx1" and "idx2" where our value is.
	 
	  if(value <= 0){ idx1 = idx2 = 0;}    // accounts for an input <=0
	  else if(value >= 1)  {idx1 = idx2 = NUM_COLORS-1;}    // accounts for an input >=0
	  else{
	    value = value * (NUM_COLORS-1);         // Will multiply value by 3.
	    idx1  = (int)value;                     // Our desired color will be after this index.
	    idx2  = idx1+1;                         // ... and before this index (inclusive).
	    fractBetween = value - idx1;			// Distance between the two indexes (0-1).
	  }
	 
	  red   = (int) (255*((color[idx2][0] - color[idx1][0])*fractBetween + color[idx1][0]));
	  green = (int) (255*((color[idx2][1] - color[idx1][1])*fractBetween + color[idx1][1]));
	  blue  = (int) (255*((color[idx2][2] - color[idx1][2])*fractBetween + color[idx1][2]));
	}
}