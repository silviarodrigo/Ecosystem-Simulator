package simulator.model;

import java.util.stream.Stream;

import simulator.model.Animal.Diet;

public class DefaultRegion extends Region {
	
	//lo que se muestra en la tabla de regiones
	public String toString() {
		return "Default region";
	}

	public double get_food(Animal a, double dt) {
		if (a.get_diet() == Diet.CARNIVORE) {
			return 0.0;
		} else {
			// filtra la lista de animales para coger solo los herbivoros
			Stream<Animal> stream = animals.stream();
			int num_herbivores = (int) stream.filter((e) -> e.get_diet() == Diet.HERBIVORE).count();
			return _FOOD_EXPONENT
					* Math.exp(-Math.max(0, num_herbivores - _FOOD_SHORTAGE_TH_HERBS) * _FOOD_SHORTAGE_EXP_HERBS) * dt;
		}
	}

	public void update(double dt) {
		;
	}
}
