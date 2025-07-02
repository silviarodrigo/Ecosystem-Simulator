package simulator.model;

import java.util.List;
import org.json.JSONObject;
import simulator.misc.Utils;
import simulator.misc.Vector2D;

public abstract class Animal implements Entity, AnimalInfo {
	// CONSTANTES
	public final static double _INIT_ENERGY = 100.0;
	public final static double _MUTATION_TOLERANCE = 0.2;
	public final static double _NEARBY_FACTOR = 60.0;
	public final static double _COLLISION_RANGE = 8.0;
	public final static double _HUNGER_DECAY_EXP_FACTOR = 0.007;
	public final static double _FOOD_DROP_BOOST_FACTOR = 1.2;
	public final static double _DESIRE_THRESHOLD = 65.0;

	public final static double RANDOM_PARAMETER = 0.1;
	public final static double VALUE_MAX = 100.0;
	public final static double VALUE_MIN = 0.0;

	// ENUMERADOS
	public enum Diet {//todos los tipos de dietas
		HERBIVORE, CARNIVORE
	};

	public enum State {//todos los tipos de estados
		NORMAL, MATE, HUNGER, DANGER, DEAD
	};

	// ATRIBUTOS
	protected String _genetic_code;
	protected Diet _diet;
	protected State _state;
	protected Vector2D _pos;
	protected Vector2D _dest;
	protected double _energy;
	protected double _speed;
	protected double _age;
	protected double _desire;
	protected double _sight_range;
	protected Animal _mate_target;
	protected Animal _baby;
	protected AnimalMapView _region_mngr;
	protected SelectionStrategy _mate_strategy;

//CONSTRUCTORAS
	protected Animal(String genetic_code, Diet diet, double sight_range, double init_speed,
			SelectionStrategy mate_strategy, Vector2D pos) {
		// comprobamos la validez de los parametros
		if (genetic_code == null || genetic_code.equalsIgnoreCase("")) {
			throw new IllegalArgumentException("genetic_code debe ser una cadena no vacía.");
		} else if (sight_range <= 0) {
			throw new IllegalArgumentException("sight_range no es un número positivo.");
		} else if (init_speed <= 0) {
			throw new IllegalArgumentException("init_speed no es un número positivo.");
		} else if (mate_strategy == null) {
			throw new IllegalArgumentException("mate_strategy es null.");
		} else {
			this._genetic_code = genetic_code;
			this._diet = diet;
			this._sight_range = sight_range;
			this._pos = pos;
			this._mate_strategy = mate_strategy;
			this._mate_target = null;
			this._speed = Utils.get_randomized_parameter(init_speed, RANDOM_PARAMETER);
			this._state = State.NORMAL;
			this._energy = _INIT_ENERGY;
			this._desire = 0.0;
			this._mate_target = null;
			this._baby = null;
			this._region_mngr = null;
			this._dest = null;
			this._age = 0.0;
		}

	}

	protected Animal(Animal p1, Animal p2) {
		assert (p1.get_genetic_code() == p2.get_genetic_code());
		this._dest = null;
		this._baby = null;
		this._region_mngr = null;
		this._state = State.NORMAL;
		this._desire = 0.0;
		this._genetic_code = p1.get_genetic_code();
		this._diet = p1.get_diet();
		this._energy = (p1.get_energy() + p2.get_energy()) / 2;
		_pos = p1.get_position()
				.plus(Vector2D.get_random_vector(-1, 1).scale(_NEARBY_FACTOR * (Utils._rand.nextGaussian() + 1)));
		this._sight_range = Utils.get_randomized_parameter((p1.get_sight_range() + p2.get_sight_range()) / 2,
				_MUTATION_TOLERANCE);
		this._speed = Utils.get_randomized_parameter((p1.get_speed() + p2.get_speed()) / 2, _MUTATION_TOLERANCE);
		this._mate_strategy = p2._mate_strategy;

	}

//GETTERS
	public State get_state() {
		return this._state;
	}

	public Vector2D get_position() {
		return this._pos;
	}

	public String get_genetic_code() {
		return this._genetic_code;
	}

	public Diet get_diet() {
		return this._diet;
	}

	public double get_speed() {
		return this._speed;
	}

	public double get_sight_range() {
		return this._sight_range;
	}

	public double get_energy() {
		return this._energy;
	}

	public double get_age() {
		return this._age;
	}

	public Vector2D get_destination() {
		return this._dest;
	}

	public boolean is_pregnant() {
		return this._baby != null;
	}

//METODOS SOBRE ESTADOS
	protected abstract void set_state(State state);

	// PASOS COMUNES
	protected void move(double speed) {
		this._pos = this._pos.plus(_dest.minus(this._pos).direction().scale(speed));
	}

	protected void paso_1_normal(double dt, double ENERGY3, double DESIRE) {
		if (this._pos.distanceTo(_dest) < _COLLISION_RANGE) {
			this._dest = new Vector2D(Utils._rand.nextDouble(_region_mngr.get_width() - 1),
					Utils._rand.nextDouble(_region_mngr.get_height()));
		}
		move(_speed * dt * Math.exp((_energy - VALUE_MAX) * _HUNGER_DECAY_EXP_FACTOR));
		// actualizacion de parametros basicos
		this._age += dt;
		this._energy -= ENERGY3 * dt;
		this._energy = Utils.constrain_value_in_range(this._energy, VALUE_MIN, VALUE_MAX);
		this._desire += DESIRE * dt;
		this._desire = Utils.constrain_value_in_range(this._desire, VALUE_MIN, VALUE_MAX);
	}

	protected void paso_2(double dt, double SPEED, double ENERGY3, double DESIRE) {
		move(SPEED * this._speed * dt * Math.exp((_energy - _INIT_ENERGY) * _HUNGER_DECAY_EXP_FACTOR));
		// actualizacion de parametros basicos
		this._age += dt;
		this._energy -= ENERGY3 * _FOOD_DROP_BOOST_FACTOR * dt;
		this._energy = Utils.constrain_value_in_range(this._energy, VALUE_MIN, VALUE_MAX);
		this._desire += DESIRE * dt;
		this._desire = Utils.constrain_value_in_range(this._desire, VALUE_MIN, VALUE_MAX);
	}

	protected void final_update(double _MAX_AGE, double dt) {
		// Si esta fuera del mapa, se ajusta
		if (this._pos.getX() >= this._region_mngr.get_width() || this._pos.getX() < 0
				|| this._pos.getY() >= this._region_mngr.get_height() || this._pos.getY() < 0) {
			this._pos = ajustar_posicion();
			set_state(State.NORMAL);
		}

		if (this._energy <= 0.0 || this._age > _MAX_AGE) {
			set_state(State.DEAD);
		}

		if (this._state != State.DEAD) {
			this._energy += this._region_mngr.get_food(this, dt);
			this._energy = Utils.constrain_value_in_range(this._energy, VALUE_MIN, VALUE_MAX);
		}
	}

	// RELACIONES ENTRE ANIMALES
	protected void pick_mate(String GENETIC_CODE) {
		// filtramos la lista de animales para obtener las parejas compatibles
		List<Animal> mates_in_range = this._region_mngr.get_animals_in_range(this,
				(animal) -> animal.get_genetic_code() == GENETIC_CODE);
		this._mate_target = this._mate_strategy.select(this, mates_in_range);
	}

	public Animal deliver_baby() {
		Animal baby_aux = this._baby;
		this._baby = null;
		return baby_aux;
	}

// OTROS METODOS
	public void init(AnimalMapView reg_mngr) {
		this._region_mngr = reg_mngr;
		if (this._pos == null) {
			this._pos = new Vector2D(Utils._rand.nextDouble(this._region_mngr.get_width() - 1),
					Utils._rand.nextDouble(this._region_mngr.get_height() - 1));
		} else {
			this._pos = ajustar_posicion();
		}
		this._dest = new Vector2D(Utils._rand.nextDouble(this._region_mngr.get_width() - 1),
				Utils._rand.nextDouble(this._region_mngr.get_height() - 1));
	}

	protected Vector2D ajustar_posicion() {
		double x = this._pos.getX();
		double y = this._pos.getY();
		double width = this._region_mngr.get_width();
		double height = this._region_mngr.get_height();
		while (x >= width)
			x -= width;
		while (x < 0)
			x += width;
		while (y >= height)
			y -= height;
		while (y < 0)
			y += height;
		return new Vector2D(x, y);
	}

//JSONs
	public JSONObject as_JSON() {
		JSONObject jo = new JSONObject();
		jo.put("pos", this._pos.asJSONArray());
		jo.put("gcode", this._genetic_code);
		jo.put("diet", this._diet);
		jo.put("state", this._state.name());
		return jo;
	}
}
