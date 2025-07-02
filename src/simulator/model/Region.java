package simulator.model;

import org.json.JSONObject;
import org.json.JSONArray;
import java.util.*;

public abstract class Region implements Entity, FoodSupplier, RegionInfo {
	protected final static double _FOOD_EXPONENT = 60.0;
	protected final static double _FOOD_SHORTAGE_TH_HERBS = 5.0;
	protected final static double _FOOD_SHORTAGE_EXP_HERBS = 2.0;

	protected List<Animal> animals;

	public Region() {
		this.animals = new ArrayList<>();
	}

//GETTERS	
	public List<AnimalInfo> getAnimalsInfo() {
		return new ArrayList<>(this.animals);
	}

//MANEJO ANIMALES EN LA LISTA
	final List<Animal> getAnimals() {
		return Collections.unmodifiableList(animals);
	}

	final void add_animal(Animal a) {
		if (!animals.contains(a)) {
			animals.add(a);
		}
	}

	final void remove_animal(Animal a) {
		if (animals.contains(a)) {
			animals.remove(a);
		}
	}

//JSONs
	public JSONObject as_JSON() {
		JSONObject jo = new JSONObject();
		JSONArray json_animal = new JSONArray();
		for (int i = 0; i < animals.size(); i++) {
			json_animal.put(animals.get(i).as_JSON());
		}
		jo.put("animals", json_animal);
		return jo;
	}

}
