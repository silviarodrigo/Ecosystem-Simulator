package simulator.factories;

import org.json.JSONObject;

import simulator.misc.Utils;
import simulator.misc.Vector2D;
import simulator.model.Animal;
import simulator.model.SelectFirst;
import simulator.model.SelectionStrategy;
import simulator.model.Wolf;

public class WolfBuilder extends Builder<Animal> {
	Factory<SelectionStrategy> _f;

	public WolfBuilder(Factory<SelectionStrategy> f) {
		super("wolf", "Wolf");
		this._f = f;
	}

	protected Animal create_instance(JSONObject data) {
		// Seleccionamos el mate_strategy
		SelectionStrategy ms;
		if (data.has("mate_strategy")) {
			JSONObject jo = data.getJSONObject("mate_strategy");
			ms = _f.create_instance(jo);
		} else {
			ms = new SelectFirst();
		}

		// Seleccionamos el hunt_strategy
		SelectionStrategy hs;
		if (data.has("hunt_strategy")) {
			JSONObject jo = data.getJSONObject("hunt_strategy");
			hs = _f.create_instance(jo);
		} else {
			hs = new SelectFirst();
		}

		// Seleccionamos la pos
		Vector2D pos;
		if (data.has("pos")) {
			JSONObject jo = data.getJSONObject("pos");
			pos = new Vector2D(
					Utils._rand.nextDouble(jo.getJSONArray("x_range").getDouble(0),
							jo.getJSONArray("x_range").getDouble(1)),
					Utils._rand.nextDouble(jo.getJSONArray("y_range").getDouble(0),
							jo.getJSONArray("y_range").getDouble(1)));
		} else {
			pos = null;
		}
		return new Wolf(ms, hs, pos);
	}

//JSONs
	protected void fill_in_data(JSONObject o) {
		o.put("mate_strategy", "a json that contains information about its mate strategy");
		o.put("hunt_strategy", "a json that contains information about its hunting strategy");
		JSONObject jo = new JSONObject();
		jo.put("x_range", "the range in which the x component of the animal position is in");
		jo.put("y_range", "the range in which the y component of the animal position is in");
		o.put("pos", jo);

	}
}
