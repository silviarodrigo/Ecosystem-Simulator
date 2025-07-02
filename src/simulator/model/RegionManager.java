package simulator.model;

import java.util.ArrayList;
import java.util.Iterator;
import org.json.JSONArray;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.function.Predicate;

import org.json.JSONObject;

public class RegionManager implements AnimalMapView {
	private int _width_map;
	private int _height_map;
	private int _width_region;
	private int _height_region;
	private int _cols_map;
	private int _rows_map;

	protected Region _regions[][];
	protected Map<Animal, Region> _animal_region;

	public RegionManager(int cols, int rows, int width, int height) {
		this._cols_map = cols;
		this._rows_map = rows;
		this._width_map = width;
		this._height_map = height;

		this._width_region = this._width_map / cols + (width % cols != 0 ? 1 : 0);
		this._height_region = this._height_map / rows + (height % rows != 0 ? 1 : 0);

		this._regions = new Region[this._rows_map][this._cols_map];
		//columna superior izquierda = (0,0)
		for (int i = 0; i < this._rows_map; i++) { //y = height = rows
			for (int j = 0; j < this._cols_map; j++) {//x = width = cols
				this._regions[i][j] = new DefaultRegion();
			}
		}
		this._animal_region = new HashMap<Animal, Region>();
	}

//GETTERS
	public int get_cols() {
		return this._cols_map;
	}

	public int get_rows() {
		return this._rows_map;
	}

	public int get_width() {
		return this._width_map;
	}

	public int get_height() {
		return this._height_map;
	}

	public int get_region_width() {
		return this._width_region;
	}

	public int get_region_height() {
		return this._height_region;
	}

//MANEJO ANIMALES	
	void register_animal(Animal a) {
		if (!this._animal_region.containsKey(a)) {
			a.init(this);
			Region r = animal_in_region(a);
			r.add_animal(a);
			this._animal_region.put(a, r);
		}
	}

	void unregister_animal(Animal a) {
		Region r = this._animal_region.get(a);
		r.remove_animal(a);
		this._animal_region.remove(a);
	}

	void update_animal_region(Animal a) {
		Region r1 = animal_in_region(a);
		Region r2 = this._animal_region.get(a);
		if (r1 != r2) {
			r1.add_animal(a);
			r2.remove_animal(a);
			this._animal_region.put(a, r1);
		}
	}

	public List<Animal> get_animals_in_range(Animal e, Predicate<Animal> filter) {
		double r = e.get_sight_range();
		double rightBound = e.get_position().getX() + r;
		rightBound = rightBound >= this._width_map ? this._width_map - 1 : rightBound;
		double leftBound = e.get_position().getX() - r;
		leftBound = leftBound < 0 ? 0 : leftBound;
		double lowerBound = e.get_position().getY() + r;
		lowerBound = lowerBound >= this._height_map ? this._height_map - 1 : lowerBound;
		double upperBound = e.get_position().getY() - r;
		upperBound = upperBound < 0 ? 0 : upperBound;

		int x0 = (int) (leftBound / this._width_region);
		int x1 = (int) (rightBound / this._width_region);
		int y0 = (int) (upperBound / this._height_region);
		int y1 = (int) (lowerBound / this._height_region);

		ArrayList<Animal> animals = new ArrayList<>();
		for (int x = x0; x <= x1; x++) {
			for (int y = y0; y <= y1; y++) {
				Region region = this._regions[y][x];
				for (Animal a : region.getAnimals()) {
					if (a != e && a.get_position().distanceTo(e.get_position()) <= r && filter.test(a)) {
						animals.add(a);
					}
				}
			}
		}
		return animals;

	}

//MANEJO REGIONES
	void set_region(int row, int col, Region r) {
		Region old_region = _regions[row][col];
		if (old_region != null) {
			for (Animal a : old_region.getAnimals()) {
				r.add_animal(a);
				_animal_region.put(a, r);
			}
		}
		_regions[row][col] = r;
	}

	public double get_food(Animal a, double dt) {
		return this._animal_region.get(a).get_food(a, dt);
	}

	void update_all_regions(double dt) {
		for (int i = 0; i < this._rows_map; i++) {
			for (int j = 0; j < this._cols_map; j++) {
				this._regions[i][j].update(dt);
			}
		}
	}

	// devuelve la region en la que esta un animal (dada su posicion)
	private Region animal_in_region(Animal a) {
		int y = (int) a.get_position().getY() / _height_region;
		int x = (int) a.get_position().getX() / _width_region;
		return this._regions[y][x];
	}
	
	//Recorre las regiones
	public Iterator<RegionData> iterator() {
		return new Iterator<MapInfo.RegionData>() {
			int r = 0;
			int c = 0;

			public RegionData next() {
				assert (r < _rows_map);
				//enviamos la region a la tabla de regiones
				RegionData rd = new RegionData(r, c, _regions[r][c]);
				c = (c + 1) % _cols_map;
				if (c == 0)
					r = r + 1;
				return rd;
			}

			public boolean hasNext() {
				return r < _rows_map;
			}
		};
	}

//JSONs
	public JSONObject as_JSON() {
		JSONObject jo = new JSONObject();
		JSONArray json_regiones = new JSONArray();
		for (int i = 0; i < this._rows_map; i++) {
			for (int j = 0; j < this._cols_map; j++) {
				JSONObject jo1 = new JSONObject();
				jo1.put("row", i);
				jo1.put("col", j);
				jo1.put("data", this._regions[i][j].as_JSON());
				json_regiones.put(jo1);
			}
		}
		jo.put("regiones", json_regiones);
		return jo;
	}


}
