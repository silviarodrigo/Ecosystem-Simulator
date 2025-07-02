package simulator.view;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import simulator.control.Controller;
import simulator.model.AnimalInfo;
import simulator.model.EcoSysObserver;
import simulator.model.MapInfo;
import simulator.model.RegionInfo;

class StatusBar extends JPanel implements EcoSysObserver {
	private static final long serialVersionUID = 1L;
	private JLabel _title_timeLabel;
	private JLabel _timeLabel;
	private JLabel _title_animalsLabel;
	private JLabel _animalsLabel;
	private JLabel _title_dimensionsLabel;
	private JLabel _dimensionsLabel;

	StatusBar(Controller ctrl) {
		initGUI();
		ctrl.addObserver(this);
	}

	private void initGUI() {
		this.setLayout(new FlowLayout(FlowLayout.LEFT));
		this.setBorder(BorderFactory.createBevelBorder(1));

		JSeparator s = new JSeparator(JSeparator.VERTICAL);
		s.setPreferredSize(new Dimension(10, 20));

		//Tiempo
		this._title_timeLabel = new JLabel("Time: ");
		this._timeLabel = new JLabel();
		this.add(s);
		this.add(this._title_timeLabel);
		this.add(this._timeLabel);

		//Animales
		this._title_animalsLabel = new JLabel("Total Animals: ");
		this._animalsLabel = new JLabel();
		this.add(s);
		this.add(this._title_animalsLabel);
		this.add(this._animalsLabel);

		//Dimensiones
		this._title_dimensionsLabel = new JLabel("Dimension: ");
		this._dimensionsLabel = new JLabel();
		this.add(s);
		this.add(this._title_dimensionsLabel);
		this.add(this._dimensionsLabel);

	}

//METODOS ECOSYSOBERVER
	public void onRegister(double time, MapInfo map, List<AnimalInfo> animals) {
		this._timeLabel.setText(String.format("%.3f", time));
		this._animalsLabel.setText(String.valueOf(animals.size()));
		this._dimensionsLabel.setText(String.valueOf(map.get_width()) + "x" + String.valueOf(map.get_height()) + "x"
				+ String.valueOf(map.get_cols()) + "x" + String.valueOf(map.get_rows()));
	}

	public void onReset(double time, MapInfo map, List<AnimalInfo> animals) {
		this._timeLabel.setText(String.format("%.3f", time));
		this._animalsLabel.setText(String.valueOf(animals.size()));
		this._dimensionsLabel.setText(String.valueOf(map.get_width()) + "x" + String.valueOf(map.get_height()) + "x"
				+ String.valueOf(map.get_cols()) + "x" + String.valueOf(map.get_rows()));
	}

	public void onAnimalAdded(double time, MapInfo map, List<AnimalInfo> animals, AnimalInfo a) {
		this._animalsLabel.setText(String.valueOf(animals.size()));

	}

	public void onRegionSet(int row, int col, MapInfo map, RegionInfo r) {
		this._dimensionsLabel.setText(String.valueOf(map.get_width()) + "x" + String.valueOf(map.get_height()) + "x"
				+ String.valueOf(map.get_cols()) + "x" + String.valueOf(map.get_rows()));
	}

	public void onAvanced(double time, MapInfo map, List<AnimalInfo> animals, double dt) {
		this._timeLabel.setText(String.format("%.3f", time));
		this._animalsLabel.setText(String.valueOf(animals.size()));
	}
}
