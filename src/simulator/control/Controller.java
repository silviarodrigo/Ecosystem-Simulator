package simulator.control;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.*;
import org.json.JSONArray;
import org.json.JSONObject;
import simulator.model.AnimalInfo;
import simulator.model.EcoSysObserver;
import simulator.model.MapInfo;
import simulator.model.Simulator;
import simulator.view.SimpleObjectViewer;
import simulator.view.SimpleObjectViewer.ObjInfo;

public class Controller {
	Simulator _sim;

	public Controller(Simulator sim) {
		this._sim = sim;
	}

	public void run(double t, double dt, boolean sv, OutputStream out) {
		SimpleObjectViewer view = null;
		if (sv) {
			MapInfo m = _sim.get_map_info();
			view = new SimpleObjectViewer("[ECOSYSTEM]", m.get_width(), m.get_height(), m.get_cols(), m.get_rows());
			view.update(to_animals_info(_sim.get_animals()), _sim.get_time(), dt);
		}
		// guarda el json de entrada
		JSONObject jo = new JSONObject();
		jo.put("in", this._sim.as_JSON());
		// actualizamos la simulacion
		while (this._sim.get_time() <= t) {
			this._sim.advance(dt);
			if (sv) {
				view.update(to_animals_info(_sim.get_animals()), _sim.get_time(), dt);
			}
		}
		// guarda el json de salida
		jo.put("out", this._sim.as_JSON());
		PrintStream p = new PrintStream(out);
		p.println(jo);
		// cerramos el visor
		if (sv) {
			view.close();
		}
	}

// METODOS DE CARGA
	public void load_data(JSONObject data) {
		// carga regiones
		if (data.has("regions")) {
			set_regions(data);
		}

		// carga animales
		JSONArray ja_animals = data.getJSONArray("animals");
		for (int i = 0; i < ja_animals.length(); i++) {
			load_animals(ja_animals.getJSONObject(i));
		}
	}

	public void load_regions(JSONObject data) {
		for (int R = data.getJSONArray("row").getInt(0); R <= data.getJSONArray("row").getInt(1); R++) {
			for (int C = data.getJSONArray("col").getInt(0); C <= data.getJSONArray("col").getInt(1); C++) {
				_sim.set_region(R, C, data.getJSONObject("spec"));
			}
		}
	}

	public void load_animals(JSONObject data) {
		for (int i = 0; i < data.getInt("amount"); i++) {
			_sim.add_animal(data.getJSONObject("spec"));
		}
	}

//METODOS PARA OBSERVADORES
	public void reset(int cols, int rows, int width, int height) {
		this._sim.reset(cols, rows, width, height);
	}

	public void set_regions(JSONObject rs) {
		JSONArray ja_regions = rs.getJSONArray("regions");
		for (int i = 0; i < ja_regions.length(); i++) {
			load_regions(ja_regions.getJSONObject(i));
		}
	}

	public void advance(double dt) {
		this._sim.advance(dt);
	}

	public void addObserver(EcoSysObserver o) {
		this._sim.addObserver(o);
	}

	public void removeObserver(EcoSysObserver o) {
		this._sim.removeObserver(o);
	}

// OTROS METODOS
	private List<ObjInfo> to_animals_info(List<? extends AnimalInfo> animals) {
		List<ObjInfo> ol = new ArrayList<>(animals.size());
		for (AnimalInfo a : animals)
			ol.add(new ObjInfo(a.get_genetic_code(), (int) a.get_position().getX(), (int) a.get_position().getY(),
					(int) Math.round(a.get_age()) + 2));
		return ol;
	}

}
