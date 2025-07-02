package simulator.view;

import java.util.List;
import java.util.HashMap;
import simulator.model.Animal;
import javax.swing.table.AbstractTableModel;
import java.util.Map;
import simulator.control.Controller;
import simulator.model.AnimalInfo;
import simulator.model.EcoSysObserver;
import simulator.model.MapInfo;
import simulator.model.RegionInfo;

class SpeciesTableModel extends AbstractTableModel implements EcoSysObserver {
	private static final long serialVersionUID = 1L;
	protected Map<String, Map<Animal.State, Integer>> _map;
	protected int _cols;
	protected int _rows;
	protected String[] _cols_name;
	protected Animal.State[] _states;

	SpeciesTableModel(Controller ctrl) {
		this._map = new HashMap<>();
		this._cols = Animal.State.values().length + 1;
		this._states = Animal.State.values();
		this._cols_name = new String[1 + _states.length];
		this._cols_name[0] = "Species";
		//leemos los tipos de estados desde el enumerado para poder cambiarlos
		for (int i = 0; i < _states.length; i++) {
			this._cols_name[i + 1] = _states[i].toString();
		}
		ctrl.addObserver(this);
	}
	
//GETTERS
	public int getRowCount() {
		return this._map.keySet().size();
	}

	public int getColumnCount() {
		return this._states.length + 1;
	}

	public String getColumnName(int col) {
		return _cols_name[col];
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		Object s = null;
		Object[] species = this._map.keySet().toArray();
		String specie = species[rowIndex].toString();
		switch (columnIndex) {
		case 0:
			s = specie;
			break;
		default:
			s = String.valueOf(this._map.get(specie).get(this._states[columnIndex - 1]));
			break;
		}
		return s;

	}

//METODOS ECOSYSOBSERVER
	public void onRegister(double time, MapInfo map, List<AnimalInfo> animals) {
		updateList(animals);
	}

	public void onReset(double time, MapInfo map, List<AnimalInfo> animals) {
		updateList(animals);
	}

	public void onAnimalAdded(double time, MapInfo map, List<AnimalInfo> animals, AnimalInfo a) {
		updateList(animals);
	}

	public void onRegionSet(int row, int col, MapInfo map, RegionInfo r) {
		//no requiere implementacion
	}

	public void onAvanced(double time, MapInfo map, List<AnimalInfo> animals, double dt) {
		updateList(animals);
	}

	//Actualiza los valores de las especies
	private void updateList(List<AnimalInfo> animals) {
		this._map.clear();
		for (AnimalInfo a : animals) {
			updateAnimal(a);
		}
		//avisamos de que la tabla ha cambiado
		fireTableStructureChanged();
	}

	private void updateAnimal(AnimalInfo a) {
		String gc = a.get_genetic_code();
		Map<Animal.State, Integer> info = this._map.get(gc);
		if (info == null) {
			info = new HashMap<Animal.State, Integer>();
			this._map.put(gc, info);
			for (Animal.State as : Animal.State.values()) {
				info.put(as, 0);
			}
		}
		info.put(a.get_state(), info.get(a.get_state()) + 1);
	}

}
