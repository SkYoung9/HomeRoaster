import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JLabel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;

import javax.swing.JList;
import javax.swing.AbstractListModel;
import javax.swing.DefaultListModel;
import javax.swing.JFormattedTextField;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.ListSelectionModel;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

public class Recipe extends JPanel {
	private JFormattedTextField _recipeLocation;
	private JTable _tableSteps;
	
	/**
	 * Create the panel.
	 */
	public Recipe() {
		setLayout(new BorderLayout(0, 0));
		JLabel lblStatusDesc = new JLabel("Roast Status");
		
		JPanel designPanel = new JPanel();
		GridBagLayout gbl_designPanel = new GridBagLayout();
		gbl_designPanel.columnWidths = new int[]{258, 332, 0};
		gbl_designPanel.rowHeights = new int[]{56, 26, 350, 0};
		gbl_designPanel.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		gbl_designPanel.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		designPanel.setLayout(gbl_designPanel);
		
		_tableSteps = new JTable();
		_tableSteps.setModel(new RecipeModel());
		for (int i = 0; i < _tableSteps.getColumnCount(); i++) {
			_tableSteps.getColumnModel().getColumn(i).setCellRenderer(new RecipeCellRenderer());
		}
		//_tableSteps.setDefaultRenderer(Object.class, new RecipeCellRenderer());
		_tableSteps.getColumnModel().getColumn(3).setPreferredWidth(100);
		JLabel lblListDesc = new JLabel("Recipes");
		GridBagConstraints gbc_lblListDesc = new GridBagConstraints();
		gbc_lblListDesc.fill = GridBagConstraints.BOTH;
		gbc_lblListDesc.insets = new Insets(0, 0, 5, 5);
		gbc_lblListDesc.gridx = 0;
		gbc_lblListDesc.gridy = 0;
		designPanel.add(lblListDesc, gbc_lblListDesc);
		
		JLabel lblRoastingSteps = new JLabel("Roasting Steps");
		GridBagConstraints gbc_lblRoastingSteps = new GridBagConstraints();
		gbc_lblRoastingSteps.fill = GridBagConstraints.BOTH;
		gbc_lblRoastingSteps.insets = new Insets(0, 0, 5, 0);
		gbc_lblRoastingSteps.gridx = 1;
		gbc_lblRoastingSteps.gridy = 0;
		designPanel.add(lblRoastingSteps, gbc_lblRoastingSteps);
		
		_recipeLocation = new JFormattedTextField();
		_recipeLocation.setText("./");
		_recipeLocation.setFocusLostBehavior(JFormattedTextField.COMMIT);
		GridBagConstraints gbc__recipeLocation = new GridBagConstraints();
		gbc__recipeLocation.anchor = GridBagConstraints.SOUTH;
		gbc__recipeLocation.fill = GridBagConstraints.HORIZONTAL;
		gbc__recipeLocation.insets = new Insets(0, 0, 5, 5);
		gbc__recipeLocation.gridx = 0;
		gbc__recipeLocation.gridy = 1;
		designPanel.add(_recipeLocation, gbc__recipeLocation);
		GridBagConstraints gbc_tableSteps = new GridBagConstraints();
		gbc_tableSteps.fill = GridBagConstraints.BOTH;
		gbc_tableSteps.gridheight = 2;
		gbc_tableSteps.gridx = 1;
		gbc_tableSteps.gridy = 1;
		designPanel.add(new JScrollPane(_tableSteps), gbc_tableSteps);
		
		JList<RecipeFile> list = new JList<RecipeFile>();
		list.setModel(getRecipes());
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					loadRecipe(list.getSelectedValue());
				}
			}
		});
		GridBagConstraints gbc_list = new GridBagConstraints();
		gbc_list.fill = GridBagConstraints.BOTH;
		gbc_list.insets = new Insets(0, 0, 0, 5);
		gbc_list.gridx = 0;
		gbc_list.gridy = 2;
		designPanel.add(list, gbc_list);
		JPanel roastPanel = new JPanel();
		roastPanel.setLayout(new BorderLayout(0, 0));
		roastPanel.add(lblStatusDesc, BorderLayout.NORTH);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.addTab("Design", designPanel);
		tabbedPane.addTab("Roast", roastPanel);
		
		JPanel graphPanel = new JPanel();
		roastPanel.add(graphPanel, BorderLayout.CENTER);
		add(tabbedPane);
	}
	
	private void loadRecipe(RecipeFile recipe) {
		DefaultTableModel model = (DefaultTableModel) _tableSteps.getModel();
		for (int i = model.getRowCount() - 1; i >= 0; i--)
		{
			model.removeRow(i);
		}
		
		//System.out.println(model.getRowCount());
		
		for (RoastStep step : recipe.getSteps()) {
			model.addRow(new Object[] {step.order, step.time, step.temperature, step.startAfterTemp});
		}
	}
	
	private DefaultListModel<RecipeFile> getRecipes() {
		DefaultListModel<RecipeFile> list = new DefaultListModel<RecipeFile>();

		String path = _recipeLocation.getText();
		File folder = new File(path);
		for (File f : folder.listFiles()) {
			RecipeFile xmlFile = new RecipeFile(f.getPath());
			if (xmlFile.isRecipe())
			{
				list.addElement(xmlFile);
			}
		}
		
		return list;
	}
	
	private class RecipeFile extends File {
		/**
		 * 
		 */
		private static final long serialVersionUID = 7865661129907661854L;
		final String SCHEMA_FILE = "./recipe.xsd";
		boolean _isRecipe = false;
		Document _xmlDoc;
		
		public RecipeFile(String path) {
			super(path);
			if (this.isFile()) {
				_isRecipe = validateXML();
			}
			
			if (_isRecipe) {
				try {
					_xmlDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(this);
				} catch (SAXException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ParserConfigurationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
			}
		}
		
		public boolean isRecipe() {
			return _isRecipe;
		}
		
		public List<RoastStep> getSteps() {
			ArrayList<RoastStep> list = new ArrayList<RoastStep>();
			RoastStep step;
			String order, time, temperature, startAfterTemp; 
			if (_isRecipe) {
				NodeList nl = _xmlDoc.getDocumentElement().getElementsByTagName("step");
				
				for (int i = 0; i < nl.getLength(); i++) {
					Element el = (Element) nl.item(i);
					order = el.getElementsByTagName("order").item(0).getTextContent();
					time = el.getElementsByTagName("time").item(0).getTextContent();
					temperature = el.getElementsByTagName("temp").item(0).getTextContent();
					startAfterTemp = el.getElementsByTagName("startAfterTemp").item(0).getTextContent();

					step = new RoastStep(Integer.parseInt(order),Integer.parseInt(time), Integer.parseInt(temperature), Boolean.parseBoolean(startAfterTemp)); 
					list.add(step);
				}
			}
			return list;
		}
		
		private boolean validateXML() {
			Source xmlFile = new StreamSource(this);
			SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			
			try {
				Schema schema = schemaFactory.newSchema(new File(SCHEMA_FILE));
				Validator validator = schema.newValidator();
				validator.validate(xmlFile);
				//System.out.println(xmlFile.getSystemId() + " is  valid");
				return true;
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				//System.out.println(xmlFile.getSystemId() + " is NOT valid reason:" + e);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return false;
		}
		
		public String toString() {
			if (_isRecipe) {
				return _xmlDoc.getDocumentElement().getElementsByTagName("name").item(0).getTextContent();
			}
			return "Not Recipe";
		}
	}
	
	public boolean nextStep() {
		RecipeModel model = (RecipeModel) _tableSteps.getModel();
		int nextRow = model.getSelectedRow() + 1;
		
		if (nextRow < model.getRowCount()) {
			model.setSelectedRow(nextRow);
			return true;
		}
		return false;
	}
	
	public int getTime() {
		RecipeModel model = (RecipeModel) _tableSteps.getModel();
		return (int) model.getValueAt(model.getSelectedRow(), RecipeModel.TIME_INDEX);
	}

	public int getTemp() {
		RecipeModel model = (RecipeModel) _tableSteps.getModel();
		return (int) model.getValueAt(model.getSelectedRow(), RecipeModel.TEMP_INDEX);
	}
	
	public boolean getStartAfterTemp() {
		RecipeModel model = (RecipeModel) _tableSteps.getModel();
		return (boolean) model.getValueAt(model.getSelectedRow(), RecipeModel.STARTAFTERTEMP_INDEX);
	}	
	
	private class RoastStep {
		public int order, time, temperature;
		public boolean startAfterTemp;
		
		public RoastStep(int order, int time, int temperature, boolean startAfterTemp) {
			this.order = order;
			this.time = time;
			this.temperature = temperature;
			this.startAfterTemp = startAfterTemp;
		}
	}
	
	static class RecipeModel extends DefaultTableModel {
		final Color DEFAULT_COLOR = Color.WHITE;
		final Color SELECT_COLOR = Color.CYAN;
		private int _selectedRow = 0;
		final Class[] COLUMN_TYPES = {Integer.class, Integer.class, Integer.class, Boolean.class};
		final static String[] COLUMN_HEADERS = { "Order", "Time", "Temperature", "Start After Temp." };
		final static int TIME_INDEX = 1, TEMP_INDEX = 2, STARTAFTERTEMP_INDEX = 3;


		public RecipeModel() {
			super(new Object[][] { {null, null, null, null} }, COLUMN_HEADERS);
	}
		
		@Override
		public Class getColumnClass(int columnIndex) {
			return COLUMN_TYPES[columnIndex];
		}
		
		public void setSelectedRow(int row) {
	    	fireTableRowsUpdated(_selectedRow, _selectedRow);
	    	_selectedRow = row;
	    	fireTableRowsUpdated(_selectedRow, _selectedRow);
	    }
		
		public int getSelectedRow() {
			return _selectedRow;
		}

	    public Color getRowColour(int row) {
	        if (_selectedRow == row)
	        	return SELECT_COLOR;
	        return DEFAULT_COLOR;
	    }
	}
	
	static class RecipeCellRenderer extends DefaultTableCellRenderer {

	    @Override
	    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
	    	RecipeModel model = (RecipeModel) table.getModel();
	        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
	        c.setBackground(model.getRowColour(row));
	        return c;
	    }
	}
}
