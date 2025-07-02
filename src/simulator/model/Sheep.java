package simulator.model;

import java.util.List;
import simulator.misc.Utils;
import simulator.misc.Vector2D;

public class Sheep extends Animal {
	public final static String _SHEEP_GENETIC_CODE = "Sheep";//nombre que se muestra en la lista de especies
	public final static double _INIT_SIGHT_SHEEP = 40.0;
	public final static double _INIT_SPEED_SHEEP = 35.0;
	public final static double _BOOST_FACTOR_SHEEP = 2.0;
	public final static double _MAX_AGE_SHEEP = 8;
	public final static double _FOOD_DROP_RATE = 20.0;
	public final static double _DESIRE_INCREASE_RATE_SHEEP = 40.0;
	public final static double _PREGNANT_PROBABILITY_SHEEP = 0.9;

	private Animal _danger_source;
	private SelectionStrategy _danger_strategy;

//CONSTRUCTORAS
	public Sheep(SelectionStrategy mate_strategy, SelectionStrategy danger_strategy, Vector2D pos) {
		super(_SHEEP_GENETIC_CODE, Diet.HERBIVORE, _INIT_SIGHT_SHEEP, _INIT_SPEED_SHEEP, mate_strategy, pos);
		this._danger_strategy = danger_strategy;
		this._danger_source = null;
	}

	protected Sheep(Sheep p1, Animal p2) {
		super(p1, p2);
		this._danger_strategy = p1._danger_strategy;
		this._danger_source = null;
	}

// METODOS SOBRE SU ESTADO
	public void update(double dt) {
		switch (this._state) {
		case NORMAL:
			normal_action(dt);
			break;
		case DANGER:
			danger_action(dt);
			break;
		case MATE:
			mate_action(dt);
			break;
		case DEAD:
			return;
		}
		super.final_update(_MAX_AGE_SHEEP, dt);
	}

	private void normal_action(double dt) {
		// Parte 1
		super.paso_1_normal(dt, _FOOD_DROP_RATE, _DESIRE_INCREASE_RATE_SHEEP);

		// Parte 2
		if (this._danger_source == null) {
			find_danger_source();
		}
		if (this._danger_source != null) {
			set_state(State.DANGER);
		} else if (this._desire > _DESIRE_THRESHOLD) {
			set_state(State.MATE);
		}
	}

	private void danger_action(double dt) {
		// Parte 1
		if (this._danger_source != null && this._danger_source.get_state() == State.DEAD) {
			this._danger_source = null;
		}
		// Parte 2
		if (this._danger_source == null) {
			super.paso_1_normal(dt, _FOOD_DROP_RATE, _DESIRE_INCREASE_RATE_SHEEP);

		} else {
			this._dest = _pos.plus(_pos.minus(_danger_source.get_position()).direction());
			super.paso_2(dt, _BOOST_FACTOR_SHEEP, _FOOD_DROP_RATE, _DESIRE_INCREASE_RATE_SHEEP);
		}
		// Parte 3
		if (this._danger_source == null
				|| this._sight_range < this._pos.distanceTo(this._danger_source.get_position())) {
			find_danger_source();
			if (this._danger_source == null) {
				if (this._desire >= _DESIRE_THRESHOLD) {
					set_state(State.MATE);
				} else {
					set_state(State.NORMAL);
				}
			}

		}
	}

	private void mate_action(double dt) {
		// Parte 1
		if (_mate_target != null && ((_mate_target.get_state() == State.DEAD)
				|| (_pos.distanceTo(_mate_target.get_position()) > _sight_range))) {
			this._mate_target = null;
		}
		// Parte 2
		if (this._mate_target == null) {
			super.pick_mate(_SHEEP_GENETIC_CODE);
			if (this._mate_target == null) {
				super.paso_1_normal(dt, _FOOD_DROP_RATE, _DESIRE_INCREASE_RATE_SHEEP);
			}
		} else {
			this._dest = this._mate_target.get_position();
			super.paso_2(dt, _BOOST_FACTOR_SHEEP, _FOOD_DROP_RATE, _DESIRE_INCREASE_RATE_SHEEP);
			if (this._pos.distanceTo(this._mate_target.get_position()) < _COLLISION_RANGE) {
				this._desire = VALUE_MIN;
				this._mate_target._desire = VALUE_MIN;
				if (Utils._rand.nextDouble() < _PREGNANT_PROBABILITY_SHEEP && !this.is_pregnant()) {
					this._baby = new Sheep(this, _mate_target);
				}
				this._mate_target = null;
			}
		}

		if (this._danger_source == null) {
			find_danger_source();
		}
		if (this._danger_source != null) {
			set_state(State.DANGER);
		} else if (this._desire < _DESIRE_THRESHOLD) {
			set_state(State.NORMAL);
		}

	}

	private void find_danger_source() {
		// filtra la lista de animales para encontrar los que son una amenazass
		List<Animal> carnivore_animals_in_range = this._region_mngr.get_animals_in_range(this,
				(animal) -> animal._diet == Diet.CARNIVORE);
		this._danger_source = this._danger_strategy.select(this, carnivore_animals_in_range);
	}

	protected void set_state(State state) {
		if (state == State.NORMAL) {
			this._state = State.NORMAL;
			this._danger_source = null;
			this._mate_target = null;
		} else if (state == State.MATE) {
			this._state = State.MATE;
			this._danger_source = null;
		} else if (state == State.DANGER) {
			this._state = State.DANGER;
			this._mate_target = null;
		} else {
			this._state = State.DEAD;
			this._mate_target = null;
			this._danger_source = null;
		}
	}
}
