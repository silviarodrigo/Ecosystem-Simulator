package simulator.view;

import java.util.List;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.table.AbstractTableModel;

import simulator.control.Controller;
import simulator.model.Animal;
import simulator.model.AnimalInfo;
import simulator.model.EcoSysObserver;
import simulator.model.MapInfo;
import simulator.model.MapInfo.RegionData;
import simulator.model.RegionInfo;

public class RegionsTableModel extends AbstractTableModel implements EcoSysObserver {

	private static final long serialVersionUID = 1L;
	private List<Info> _regions;
	private String[] _cols_name;

	class Info {
		String _row;
		String _col;
		String _desc;
		String _diet_info[];

		Info(RegionData r) {
			this._row = String.valueOf(r.row());
			this._col = String.valueOf(r.col());
			this._desc = r.r().toString();
			Animal.Diet[] diets = Animal.Diet.values();
			this._diet_info = new String[diets.length];
			List<AnimalInfo> animals = r.r().getAnimalsInfo();
			for (int i = 0; i < diets.length; i++) {
				Animal.Diet d = diets[i];
				this._diet_info[i] = String.valueOf(animals.stream().filter((a) -> a.get_diet() == d).count());
			}
		}
	}

	RegionsTableModel(Controller ctrl) {
		//Creamos la tabla y le damos valores a sus columnas
		this._regions = new LinkedList<Info>();
		this._cols_name = new String[3 + Animal.Diet.values().length];
		this._cols_name[0] = "Row";
		this._cols_name[1]="Col";
		this._cols_name[2]="Desc.";
		//leemos los tipos de dietas desde el enumerado para poder cambiarlos
		for (int i = 0; i < Animal.Diet.values().length; i++) {
			this._cols_name[i + 3] = Animal.Diet.values()[i].toString();
		}
		ctrl.addObserver(this);
	}
//GETTERS
	public int getRowCount() {
		return this._regions == null ? 0 : this._regions.size();
	}

	public int getColumnCount() {
		return Animal.Diet.values().length + 3;
	}

	public String getColumnName(int col) {
		return _cols_name[col];
	}
	
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (columnIndex == 0) {
			return this._regions.get(rowIndex)._row;
		} else if (columnIndex == 1) {
			return this._regions.get(rowIndex)._col;
		} else if (columnIndex == 2) {
			return this._regions.get(rowIndex)._desc;
		} else {
			return this._regions.get(rowIndex)._diet_info[columnIndex - 3];
		}
	}


//METODOS ECOSYSOBSERVER
	public void onRegister(double time, MapInfo map, List<AnimalInfo> animals) {
		update(map);
	}

	public void onReset(double time, MapInfo map, List<AnimalInfo> animals) {
		update(map);
	}

	public void onAnimalAdded(double time, MapInfo map, List<AnimalInfo> animals, AnimalInfo a) {
		update(map);
	}

	public void onRegionSet(int row, int col, MapInfo map, RegionInfo r) {
		update(map);
	}

	public void onAvanced(double time, MapInfo map, List<AnimalInfo> animals, double dt) {
		update(map);
	}
	
	//actualiza la tabla con los valores del mapa
	private void update(MapInfo map) {
		this._regions.clear();
		Iterator<RegionData> iterator = map.iterator();
		while (iterator.hasNext()) {
			RegionData r = iterator.next();
			this._regions.add(new Info(r));
		}
		//avisamos de que la tabla ha cambiado
		fireTableStructureChanged();
	}
	
	

}
