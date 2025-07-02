package simulator.view;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import simulator.model.Animal;
import javax.swing.table.AbstractTableModel;
import java.util.Map;
import simulator.control.Controller;
import simulator.launcher.Main;
import simulator.model.AnimalInfo;
import simulator.model.EcoSysObserver;
import simulator.model.MapInfo;
import simulator.model.RegionInfo;
import simulator.model.MapInfo.RegionData;
import simulator.view.RegionsTableModel.Info;

class SpeciesTableModel2 extends AbstractTableModel implements EcoSysObserver {
	private static final long serialVersionUID = 1L;
	protected List<Info> _steps;
	protected int _cols;
	protected int _rows;
	protected String[] _cols_name;
	protected Animal.State[] _states = { Animal.State.HUNGER, Animal.State.DANGER };
	protected int cont_steps = 0;

	class Info {
		String steps;
		String hunger;
		String danger;

		Info(int step, List<AnimalInfo> animals) {
			this.steps = String.valueOf(step);
			this.hunger = String.valueOf(animals.stream().filter((a) -> a.get_state() == Animal.State.HUNGER).count());
			this.danger = String.valueOf(animals.stream().filter((a) -> a.get_state() == Animal.State.DANGER).count());
		}

		public String get_steps() {
			return steps;
		}
	}

	SpeciesTableModel2(Controller ctrl) {
		this._steps = new ArrayList<>();
		this._cols = 3;
		this._cols_name = new String[1 + _states.length];
		this._cols_name[0] = "Step";
		this.cont_steps = 0;
		// leemos los tipos de estados desde el enumerado para poder cambiarlos
		for (int i = 0; i < _states.length; i++) {
			this._cols_name[i + 1] = _states[i].toString();
		}
		ctrl.addObserver(this);
	}

//GETTERS
	public int getRowCount() {
		return this._steps == null ? 0 : this._steps.size();
	}

	public int getColumnCount() {
		return this._cols;
	}

	public String getColumnName(int col) {
		return _cols_name[col];
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		if (columnIndex == 0) {
			return this._steps.get(rowIndex).get_steps();
		} else if (columnIndex == 1) {
			return this._steps.get(rowIndex).hunger;
		} else {
			return this._steps.get(rowIndex).danger;
		}
	}

//METODOS ECOSYSOBSERVER
	public void onRegister(double time, MapInfo map, List<AnimalInfo> animals) {
		;
	}

	public void onReset(double time, MapInfo map, List<AnimalInfo> animals) {
		this._steps.clear();
		fireTableStructureChanged();
	}

	public void onAnimalAdded(double time, MapInfo map, List<AnimalInfo> animals, AnimalInfo a) {
		;
	}

	public void onRegionSet(int row, int col, MapInfo map, RegionInfo r) {
		;
	}

	public void onAvanced(double time, MapInfo map, List<AnimalInfo> animals, double dt) {
		update_new_step(animals);
	}

	private void update_new_step(List<AnimalInfo> animals) {
		this.cont_steps++;
		this._steps.add(new Info(this.cont_steps, animals));
		if (this._steps.size() > 10) {
			this._steps.remove(0);
		}
		// avisamos de que la tabla ha cambiado
		fireTableStructureChanged();
	}

}
