package simulator.view;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import simulator.control.Controller;
import simulator.model.AnimalInfo;
import simulator.model.EcoSysObserver;
import simulator.model.MapInfo;
import simulator.model.RegionInfo;

class MapWindow extends JFrame implements EcoSysObserver {
	private static final long serialVersionUID = 1L;
	private Controller _ctrl;
	private AbstractMapViewer _viewer;
	private Frame _parent;

	MapWindow(Frame parent, Controller ctrl) {
		super("[MAP VIEWER]");
		_ctrl = ctrl;
		_parent = parent;
		intiGUI();
		ctrl.addObserver(this);
	}

	private void intiGUI() {
		JPanel mainPanel = new JPanel(new BorderLayout());
		setContentPane(mainPanel);
		
		//creamos y añadimos el visor
		this._viewer = new MapViewer();
		mainPanel.add(this._viewer, BorderLayout.CENTER);
		
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				_ctrl.removeObserver(MapWindow.this);
			}
		});
		pack();
		if (_parent != null)
			setLocation(_parent.getLocation().x + _parent.getWidth() / 2 - getWidth() / 2,
					_parent.getLocation().y + _parent.getHeight() / 2 - getHeight() / 2);
		setResizable(false);
		setVisible(true);
	}

//METODOS ECOSYSOBSERVER
	public void onRegister(double time, MapInfo map, List<AnimalInfo> animals) {
		SwingUtilities.invokeLater(() -> {
			this._viewer.reset(time, map, animals);
			pack();
		});
	}

	public void onReset(double time, MapInfo map, List<AnimalInfo> animals) {
		SwingUtilities.invokeLater(() -> {
			this._viewer.reset(time, map, animals);
			pack();
		});
	}

	public void onAnimalAdded(double time, MapInfo map, List<AnimalInfo> animals, AnimalInfo a) {
		SwingUtilities.invokeLater(() -> {
			this._viewer.update(animals, time);
		});
	}

	public void onRegionSet(int row, int col, MapInfo map, RegionInfo r) {
		//no requiere implementacion
	}

	public void onAvanced(double time, MapInfo map, List<AnimalInfo> animals, double dt) {
		SwingUtilities.invokeLater(() -> {
			this._viewer.update(animals, time);
		});
	}

}
