package simulator.view;

import javax.swing.JFrame;

import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.*;
import simulator.control.Controller;

public class MainWindow extends JFrame {
	private static final long serialVersionUID = 1L;
	private Controller _ctrl;

	public MainWindow(Controller ctrl) {
		super("[ECOSYSTEM SIMULATOR]");
		_ctrl = ctrl;
		initGUI();
	}

	private void initGUI() {
		JPanel mainPanel = new JPanel(new BorderLayout());
		setContentPane(mainPanel);

		// ControlPanel
		ControlPanel controlPanel = new ControlPanel(this._ctrl);
		mainPanel.add(controlPanel, BorderLayout.PAGE_START);

		// StatusBar
		StatusBar statusBar = new StatusBar(this._ctrl);
		mainPanel.add(statusBar, BorderLayout.PAGE_END);

		// Tablas
		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		mainPanel.add(contentPanel, BorderLayout.CENTER);

		// Tabla de especies
		InfoTable speciesTable = new InfoTable("Species", new SpeciesTableModel(this._ctrl));
		speciesTable.setPreferredSize(new Dimension(500, 200));
		contentPanel.add(speciesTable);

		// Tabla de regiones
		InfoTable regionsTable = new InfoTable("Regions", new RegionsTableModel(this._ctrl));
		regionsTable.setPreferredSize(new Dimension(500, 200));
		contentPanel.add(regionsTable);

		// Tabla de animales extra
		InfoTable speciesTable2 = new InfoTable("Species2", new SpeciesTableModel2(this._ctrl));
		speciesTable2.setPreferredSize(new Dimension(500, 200));
		contentPanel.add(speciesTable2);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				ViewUtils.quit(MainWindow.this);
			}
		});

		// Terminamos de ajustar la ventana
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setSize(500, 500);
		pack();
		setVisible(true);
	}
}