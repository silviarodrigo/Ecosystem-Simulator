package simulator.factories;


import org.json.JSONObject;

import simulator.model.DynamicSupplyRegion;
import simulator.model.Region;

public class DynamicSupplyRegionBuilder extends Builder<Region> {
	private final static double DEFAULT_INIT_FACTOR = 2.0;
	private final static double DEFAULT_INIT_FOOD = 1000.0;

	public DynamicSupplyRegionBuilder() {
		super("dynamic", "Dynamic food supply");
	}

	protected Region create_instance(JSONObject data) {
		double factor;
		if (data.has("factor")) {
			factor = data.getDouble("factor");
		} else {
			factor = DEFAULT_INIT_FACTOR;
		}

		double food;
		if (data.has("food")) {
			food = data.getDouble("food");
		} else {
			food = DEFAULT_INIT_FOOD;
		}
		return new DynamicSupplyRegion(food, factor);
	}

//JSONs
	protected void fill_in_data(JSONObject o) {// guardamos los componentes extras de la region
		o.put("factor", "food increase factor (optional, default 2.0)");
		o.put("food", "initial amount of food (optional, default 100.0)");
	}

}
