package simulator.model;

import java.util.stream.Stream;

import simulator.misc.Utils;
import simulator.model.Animal.Diet;

public class DynamicSupplyRegion extends Region {
	private final static double _INCREMENT_FACTOR_FOOD_PROBABILITY = 0.5;

	protected double _food;
	protected double _factor;

	public DynamicSupplyRegion(double num_food, double factor_crec) throws IllegalArgumentException {
		if (num_food < 0.0 || factor_crec <= 0.0) {
			throw new IllegalArgumentException("Tiene que ser un parametro positivo");
		}
		this._food = num_food;
		this._factor = factor_crec;
	}
	
	//lo que se muestra en la tabla de regiones
	public String toString() {
		return "Dynamic region";
	}

	public double get_food(Animal a, double dt) {
		if (a._diet == Diet.CARNIVORE) {
			return 0.0;
		} else {
			// filtra la lista de animales para coger solo los herbivoros
			Stream<Animal> stream = animals.stream();
			int num_herbivores = (int) stream.filter((e) -> e.get_diet() == Diet.HERBIVORE).count();
			// calcula la comida disponible en la region
			double aux = Math.min(_food, _FOOD_EXPONENT
					* Math.exp(-Math.max(0, num_herbivores - _FOOD_SHORTAGE_TH_HERBS) * _FOOD_SHORTAGE_EXP_HERBS) * dt);
			this._food -= aux;
			return aux;
		}

	}

	public void update(double dt) {
		if (Utils._rand.nextDouble() < _INCREMENT_FACTOR_FOOD_PROBABILITY) {
			this._food += dt * this._factor;
		}
	}

}
