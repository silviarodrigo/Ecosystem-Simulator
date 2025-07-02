package simulator.model;

import java.util.List;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ArrayList;
import simulator.control.Controller;
import simulator.model.MapInfo.RegionData;

public class CarnivoreAnimals implements EcoSysObserver {
	private Map<RegionData, Integer> animal_cont;

	public CarnivoreAnimals(Controller ctrl) {
		this.animal_cont = new HashMap<>();
		ctrl.addObserver(this);
	}

	public void onRegister(double time, MapInfo map, List<AnimalInfo> animals) {
		Iterator<RegionData> iterator = map.iterator();
		while (iterator.hasNext()) {
			RegionData r = iterator.next();
			this.animal_cont.put(r, 0);
		}
		update(map);
	}

	public void onReset(double time, MapInfo map, List<AnimalInfo> animals) {
		this.animal_cont.clear();
		//update(map);

	}

	public void onAnimalAdded(double time, MapInfo map, List<AnimalInfo> animals, AnimalInfo a) {
		//update(map);

	}

	public void onRegionSet(int row, int col, MapInfo map, RegionInfo r) {
		update(map);

	}

	public void onAvanced(double time, MapInfo map, List<AnimalInfo> animals, double dt) {
		update(map);

	}

	// actualiza la tabla con los valores del mapa
	private void update(MapInfo map) {
		Iterator<RegionData> iterator = map.iterator();
		while (iterator.hasNext()) {
			RegionData r = iterator.next();
			List<AnimalInfo> animals = r.r().getAnimalsInfo();
			int num_animals = (int) animals.stream().filter((a) -> a.get_diet() == Animal.Diet.CARNIVORE).count();
			if (num_animals > 3) {
				int aux = this.animal_cont.get(r);
				aux++;
				this.animal_cont.put(r, aux);
			}
		}
	}

	public void imprimir_animales() {
		for (RegionData r : this.animal_cont.keySet()) {
			System.out.println("(" + r.col() + "," + r.row() + "):" + this.animal_cont.get(r));
		}
	}

}
