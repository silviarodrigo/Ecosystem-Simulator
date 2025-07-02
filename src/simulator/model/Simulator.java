package simulator.model;

import java.util.LinkedList;
import java.util.*;
import java.util.Collections;
import java.util.List;
import org.json.JSONObject;

import simulator.factories.Factory;
import simulator.model.Animal.State;

public class Simulator implements JSONable, Observable<EcoSysObserver> {

	private Factory<Animal> _animals_factory;
	private Factory<Region> _regions_factory;
	private RegionManager _region_mngr;
	private List<Animal> _animals;
	private double time;
	private List<EcoSysObserver> _observers;

	public Simulator(int cols, int rows, int width, int height, Factory<Animal> animals_factory,
			Factory<Region> regions_factory) {
		this._animals_factory = animals_factory;
		this._regions_factory = regions_factory;
		this._region_mngr = new RegionManager(cols, rows, width, height);
		this._animals = new LinkedList<>();
		this.time = 0.0;
		this._observers = new LinkedList<>();
	}

//GETTERS	
	public MapInfo get_map_info() {
		return this._region_mngr;
	}

	public double get_time() {
		return this.time;
	}

	public List<? extends AnimalInfo> get_animals() {
		return Collections.unmodifiableList(this._animals);
	}

//MANEJO DE ANIMALES
	private void add_animal(Animal a) {
		this._animals.add(a);
		this._region_mngr.register_animal(a);
		notify_on_animalAdded(a);
	}

	public void add_animal(JSONObject a_json) {
		Animal a = this._animals_factory.create_instance(a_json);
		add_animal(a);
	}

//MANEJO DE REGIONES
	private void set_region(int row, int col, Region r) {
		this._region_mngr.set_region(row, col, r);
		for (EcoSysObserver o : this._observers) {
			o.onRegionSet(row, col, this._region_mngr, r);
		}
	}

	public void set_region(int row, int col, JSONObject r_json) {
		Region r = this._regions_factory.create_instance(r_json);
		set_region(row, col, r);
	}

//OTROS METODOS
	public void advance(double dt) {
		this.time += dt;

		// eliminamos los animales muertos
		List<Animal> dead_animals = new LinkedList<>();
		for (Animal a : this._animals) {
			if (a.get_state() == State.DEAD) {
				dead_animals.add(a);
			}
		}
		this._animals.removeAll(dead_animals);
		dead_animals.forEach((a) -> this._region_mngr.unregister_animal(a));

		// actualizamos el resto de animales y sus regiones
		for (Animal a : this._animals) {
			a.update(dt);
			this._region_mngr.update_animal_region(a);
		}
		// actualizamos todas las regiones
		this._region_mngr.update_all_regions(dt);

		// comprobamos si hay y añadimos los nuevos animales
		List<Animal> babies = new LinkedList<>();
		for (Animal a : this._animals) {
			if (a.is_pregnant()) {
				babies.add(a.deliver_baby());
			}
		}
		babies.forEach((a) -> add_animal(a));
		notify_on_advanced(dt);
	}

//JSONs
	public JSONObject as_JSON() {
		JSONObject jo = new JSONObject();
		jo.put("time", this.time);
		jo.put("state", this._region_mngr.as_JSON());
		return jo;
	}

//METODOS PARA OBSERVADORES
	public void reset(int cols, int rows, int width, int height) {
		this.time = 0.0;
		this._animals = new LinkedList<>();
		this._region_mngr = new RegionManager(cols, rows, width, height);
		notify_on_reset();
	}

	public void addObserver(EcoSysObserver o) {
		if (!this._observers.contains(o)) {
			this._observers.add(o);
			o.onRegister(this.time, this._region_mngr, new ArrayList<>(_animals));
		}
	}

	public void removeObserver(EcoSysObserver o) {
		if (this._observers.contains(o)) {
			this._observers.remove(o);
		}
	}

	private void notify_on_advanced(double dt) {
		List<AnimalInfo> animals = new ArrayList<>(_animals);
		for (EcoSysObserver o : this._observers) {
			o.onAvanced(this.time, this._region_mngr, animals, dt);
		}
	}

	private void notify_on_reset() {
		List<AnimalInfo> animals = new ArrayList<>(_animals);
		for (EcoSysObserver o : this._observers) {
			o.onReset(this.time, this._region_mngr, animals);
		}
	}

	private void notify_on_animalAdded(Animal a) {
		List<AnimalInfo> animals = new ArrayList<>(_animals);
		for (EcoSysObserver o : this._observers) {
			o.onAnimalAdded(this.time, this._region_mngr, animals, a);
		}
	}

}
