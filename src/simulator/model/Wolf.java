package simulator.model;

import java.util.List;

import simulator.misc.Utils;
import simulator.misc.Vector2D;

public class Wolf extends Animal {
	public final static String _WOLF_GENETIC_CODE = "Wolf";//nombre que se muestra en la lista de especies
	public final static double _INIT_SIGHT_WOLF = 50.0;
	public final static double _INIT_SPEED_WOLF = 60.0;
	public final static double _BOOST_FACTOR_WOLF = 3.0;
	public final static double _MAX_AGE_WOLF = 14;
	public final static double _FOOD_THRSHOLD_WOLF = 50.0;
	public final static double _FOOD_DROP_RATE_WOLF = 18.0;
	public final static double _FOOD_DROP_DESIRE_WOLF = 10.0;
	public final static double _DESIRE_INCREASE_RATE_WOLF = 30.0;
	public final static double _PREGNANT_PROBABILITY_WOLF = 0.75;

	private Animal _hunt_target;
	private SelectionStrategy _hunting_strategy;

//CONSTRUCTORAS
	public Wolf(SelectionStrategy mate_strategy, SelectionStrategy hunting_strategy, Vector2D pos) {
		super(_WOLF_GENETIC_CODE, Diet.CARNIVORE, _INIT_SIGHT_WOLF, _INIT_SPEED_WOLF, mate_strategy, pos);
		this._hunting_strategy = hunting_strategy;
		this._hunt_target = null;
	}

	protected Wolf(Wolf p1, Animal p2) {
		super(p1, p2);
		this._hunting_strategy = p1._hunting_strategy;
		this._hunt_target = null;
	}

//METODOS SOBRE SU ESTADO
	public void update(double dt) {
			switch (this._state) {
			case NORMAL:
				normal_action(dt);
				break;
			case HUNGER:
				hunger_action(dt);
				break;
			case MATE:
				mate_action(dt);
				break;
			case DEAD:
				return;
			}
			super.final_update(_MAX_AGE_WOLF, dt);
	}

	private void normal_action(double dt) {
		// Parte 1
		super.paso_1_normal(dt, _FOOD_DROP_RATE_WOLF, _DESIRE_INCREASE_RATE_WOLF);
		// Parte 2
		if (this._energy < _FOOD_THRSHOLD_WOLF) {
			set_state(State.HUNGER);
		} else if (this._desire > _DESIRE_THRESHOLD) {
			set_state(State.MATE);
		}

	}

	private void hunger_action(double dt) {
		// Parte 1
		if (this._hunt_target == null || (this._hunt_target != null && (this._hunt_target._state == State.DEAD
				|| this._pos.distanceTo(this._hunt_target.get_position()) > this._sight_range))) {
			find_hunt_target();
		}
		// Parte 2
		if (this._hunt_target == null) {
			super.paso_1_normal(dt, _FOOD_DROP_RATE_WOLF, _DESIRE_INCREASE_RATE_WOLF);
		} else {
			this._dest = this._hunt_target.get_position();
			super.paso_2(dt, _BOOST_FACTOR_WOLF, _FOOD_DROP_RATE_WOLF, _DESIRE_INCREASE_RATE_WOLF);
			if (this._pos.distanceTo(this._hunt_target.get_position()) <= _COLLISION_RANGE) {
				this._hunt_target.set_state(State.DEAD);
				this._hunt_target = null;
				this._energy += _FOOD_THRSHOLD_WOLF;
				this._energy = Utils.constrain_value_in_range(this._energy, VALUE_MIN, VALUE_MAX);
			}
		}
		// Parte 3
		if (this._energy > _FOOD_THRSHOLD_WOLF) {
			if (this._desire >= _DESIRE_THRESHOLD) {
				set_state(State.MATE);
			} else {
				set_state(State.NORMAL);
			}
		}
	}

	private void mate_action(double dt) {
		// Parte 1
		if (_mate_target != null && ((_mate_target.get_state() == State.DEAD)
				|| (_pos.distanceTo(_mate_target.get_position()) > _sight_range)))
			this._mate_target = null;
		// Parte 2
		if (this._mate_target == null) {
			super.pick_mate(_WOLF_GENETIC_CODE);
			if (this._mate_target == null) {
				super.paso_1_normal(dt, _FOOD_DROP_RATE_WOLF, _DESIRE_INCREASE_RATE_WOLF);
			}
		} else {
			this._dest = this._mate_target.get_position();
			super.paso_2(dt, _BOOST_FACTOR_WOLF, _FOOD_DROP_RATE_WOLF, _DESIRE_INCREASE_RATE_WOLF);
			if (this._pos.distanceTo(this._mate_target.get_position()) < _COLLISION_RANGE) {
				this._desire = VALUE_MIN;
				this._mate_target._desire = VALUE_MIN;
				if (Utils._rand.nextDouble() < _PREGNANT_PROBABILITY_WOLF && !this.is_pregnant()) {
					this._baby = new Wolf(this, _mate_target);
				}
				this._energy -= _FOOD_DROP_DESIRE_WOLF;
				this._energy = Utils.constrain_value_in_range(this._energy, VALUE_MIN, VALUE_MAX);
				this._mate_target = null;
			}
		}
		if (this._energy < _FOOD_THRSHOLD_WOLF) 
			set_state(State.HUNGER);
	    else if (this._desire < _DESIRE_THRESHOLD) {
			set_state(State.NORMAL);
		}

	}

	private void find_hunt_target() {
		// filtra la lista para encontrar las posibles presas
		List<Animal> herbivore_animals_in_range = this._region_mngr.get_animals_in_range(this,
				(animal) -> animal._diet == Diet.HERBIVORE);
		this._hunt_target = this._hunting_strategy.select(this, herbivore_animals_in_range);
	}

	protected void set_state(State state) {
		if (state == State.NORMAL) {
			this._state = State.NORMAL;
			this._mate_target = null;
			this._hunt_target = null;
		} else if (state == State.MATE) {
			this._state = State.MATE;
			this._hunt_target = null;
		} else if (state == State.HUNGER) {
			this._state = State.HUNGER;
			this._mate_target = null;
		} else {
			this._state = State.DEAD;
			this._hunt_target = null;
			this._mate_target = null;
		}
	}
}
