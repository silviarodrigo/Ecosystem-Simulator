package simulator.view;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import org.json.JSONObject;
import simulator.control.Controller;
import simulator.launcher.Main;
import simulator.model.AnimalInfo;
import simulator.model.EcoSysObserver;
import simulator.model.MapInfo;
import simulator.model.RegionInfo;

class ChangeRegionsDialog extends JDialog implements EcoSysObserver {
	private static final long serialVersionUID = 1L;
	private DefaultComboBoxModel<String> _regionsModel;
	private DefaultComboBoxModel<String> _fromRowModel;
	private DefaultComboBoxModel<String> _toRowModel;
	private DefaultComboBoxModel<String> _fromColModel;
	private DefaultComboBoxModel<String> _toColModel;
	private DefaultTableModel _dataTableModel;
	private Controller _ctrl;
	private List<JSONObject> _regionsInfo;
	//nombres de las columnas
	private String[] _headers = { "Key", "Value", "Description" };

	ChangeRegionsDialog(Controller ctrl) {
		super((Frame) null, true);
		this._ctrl = ctrl;
		initGUI();
		ctrl.addObserver(this);
	}

	private void initGUI() {
		setTitle("Change Regions");
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		setContentPane(mainPanel);

	// HELP TEXT
		JLabel helpMsg = new JLabel(
				"<html> <p> Select a region type, the rows/cols interval, and provide values for the parameters in the <b>Value column</b> (default values are used for parameters with no value).<p> <html>");
		helpMsg.setAlignmentX(CENTER_ALIGNMENT);
		mainPanel.add(helpMsg);

		mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

	// ELEMENTOS VENTANA
		// Creamos el panel de la tabla
		JPanel table_panel = new JPanel();
		mainPanel.add(table_panel);

		// Creamos el comboBox
		JPanel combobox_panel = new JPanel();

		// Creamos el panel de botones
		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setAlignmentX(CENTER_ALIGNMENT);

		_regionsInfo = Main._regions_factory.get_info();

	// TABLA
		_dataTableModel = new DefaultTableModel() {
			private static final long serialVersionUID = 1L;

			public boolean isCellEditable(int row, int column) {
				return column == 1;
			}
		};
		_dataTableModel.setColumnIdentifiers(_headers);

		JTable tbl = new JTable(this._dataTableModel) {
			private static final long serialVersionUID = 1L;

			// we override prepareRenderer to resize columns to fit to content
			@Override
			public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
				Component component = super.prepareRenderer(renderer, row, column);
				int rendererWidth = component.getPreferredSize().width;
				TableColumn tableColumn = getColumnModel().getColumn(column);
				tableColumn.setPreferredWidth(
						Math.max(rendererWidth + getIntercellSpacing().width, tableColumn.getPreferredWidth()));
				return component;
			}
		};

		JScrollPane dataTableScrolls = new JScrollPane(tbl, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		// Añadimos los elementos al Panel principal (help se añadio antes para que saliese antes)
		mainPanel.add(dataTableScrolls);
		mainPanel.add(combobox_panel);
		mainPanel.add(buttonsPanel);

	// COMBOBOX
		// Regions type
		_regionsModel = new DefaultComboBoxModel<>();

		for (JSONObject o : this._regionsInfo) {
			this._regionsModel.addElement(o.get("type").toString());
		}

		JComboBox<String> regionsSelector = new JComboBox<String>(_regionsModel);
		regionsSelector.addActionListener((e) -> {
			updateTableModel(regionsSelector.getSelectedIndex());
		});

		combobox_panel.add(new JLabel("Region type:"));
		combobox_panel.add(regionsSelector);

		// From y to Cols y Rows
		this._fromRowModel = new DefaultComboBoxModel<>();
		this._toRowModel = new DefaultComboBoxModel<>();
		this._fromColModel = new DefaultComboBoxModel<>();
		this._toColModel = new DefaultComboBoxModel<>();
		JComboBox<String> fromRowSelector = new JComboBox<String>(_fromRowModel);
		JComboBox<String> toRowSelector = new JComboBox<String>(_toRowModel);
		JComboBox<String> fromColSelector = new JComboBox<String>(_fromColModel);
		JComboBox<String> toColSelector = new JComboBox<String>(_toColModel);

		combobox_panel.add(new JLabel("Row from/to:"));
		combobox_panel.add(fromRowSelector);
		combobox_panel.add(toRowSelector);
		combobox_panel.add(new JLabel("Col from/to:"));
		combobox_panel.add(fromColSelector);
		combobox_panel.add(toColSelector);

	// OK y CANCEL
		// Cancel
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener((e) -> {
			setVisible(false);
		});
		buttonsPanel.add(cancelButton);

		// OK
		JButton okButton = new JButton("OK");
		okButton.addActionListener((e) -> {
			createJSON(this._ctrl);
			setVisible(false);
		});
		buttonsPanel.add(okButton);

		// Terminamos de ajustar la ventana
		setPreferredSize(new Dimension(700, 400));
		pack();
		setResizable(false);
		setVisible(false);
	}

	public void open(Frame parent) {
		setLocation(
				parent.getLocation().x + parent.getWidth() / 2 - getWidth() / 2, 
				parent.getLocation().y + parent.getHeight() / 2 - getHeight() / 2);
		pack();
		setVisible(true);
	}

//JSONS
	private void createJSON(Controller ctrl) {
		int fromRow = Integer.parseInt(this._fromRowModel.getSelectedItem().toString());
		int toRow = Integer.parseInt(this._toRowModel.getSelectedItem().toString());
		int fromCol = Integer.parseInt(this._fromColModel.getSelectedItem().toString());
		int toCol = Integer.parseInt(this._toColModel.getSelectedItem().toString());
		String type = (String) this._regionsModel.getSelectedItem();
		StringBuilder data = new StringBuilder();
		data.append('{');
		for (int i = 0; i < _dataTableModel.getRowCount(); i++) {
			String k = _dataTableModel.getValueAt(i, 0).toString();
			String v = _dataTableModel.getValueAt(i, 1).toString();
			if (!v.isEmpty()) {
				data.append('"');
				data.append(k);
				data.append('"');
				data.append(':');
				data.append(v);
				data.append(',');
			}
		}
		if (data.length() > 1)
			data.deleteCharAt(data.length() - 1);
		data.append('}');

		String json = "{ \"regions\": [ {" + "\"row\": [" + fromRow + "," + toRow + "], " +

				"\"col\": [" + fromCol + "," + toCol + "], " +

				"\"spec\": {" +

				"\"type\": \"" + type + "\", " +

				"\"data\": " + data +

				"}" +

				"} ] }";

		JSONObject rs = new JSONObject(json);
		ctrl.set_regions(rs);
	}
	
	//Metemos los datos particulares de los tipos de regiones
	private void updateTableModel(int _dataIdx) {
		JSONObject info = this._regionsInfo.get(_dataIdx);
		JSONObject data = info.getJSONObject("data");
		Object[] aux = data.keySet().toArray();
		_dataTableModel.setNumRows(data.keySet().size());
		for (int i = 0; i < data.keySet().size(); i++) {
			_dataTableModel.setValueAt(aux[i], i, 0);
			_dataTableModel.setValueAt("", i, 1);
			_dataTableModel.setValueAt(data.get(aux[i].toString()), i, 2);
		}
	}
	

//METODOS ECOSYSOBSERVER
	public void onRegister(double time, MapInfo map, List<AnimalInfo> animals) {
		rellenar(map);
	}

	public void onReset(double time, MapInfo map, List<AnimalInfo> animals) {
		rellenar(map);
	}

	public void onAnimalAdded(double time, MapInfo map, List<AnimalInfo> animals, AnimalInfo a) {
		// no requiere implementacion
	}

	public void onRegionSet(int row, int col, MapInfo map, RegionInfo r) {
		// no requiere implementacion
	}

	public void onAvanced(double time, MapInfo map, List<AnimalInfo> animals, double dt) {
		// no requiere implementacion
	}
	
	//Rellenamos los rows y cols selectors del comboBox
	private void rellenar(MapInfo map) {
		this._fromColModel.removeAllElements();
		this._fromRowModel.removeAllElements();
		this._toColModel.removeAllElements();
		this._toRowModel.removeAllElements();

		for (int i = 0; i < map.get_cols(); i++) {
			this._fromColModel.addElement(String.valueOf(i));
			this._toColModel.addElement(String.valueOf(i));
		}
		for (int i = 0; i < map.get_rows(); i++) {
			this._fromRowModel.addElement(String.valueOf(i));
			this._toRowModel.addElement(String.valueOf(i));
		}
	}
}
