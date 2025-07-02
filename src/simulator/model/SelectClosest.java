package simulator.model;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import simulator.misc.Vector2D;

public class SelectClosest implements SelectionStrategy {

	// Estrategia que selecciona al animal mas cercano de la lista
	public Animal select(Animal a, List<Animal> as) {
		if (as.isEmpty()) {
			return null;
		}
		Vector2D a_pos = a.get_position();
		// ordenamos los animales por distancia a nuestro animal
		Comparator<Animal> cmp = (a1, a2) -> {
			if (a_pos.distanceTo(a1.get_position()) < a_pos.distanceTo(a2.get_position()))
				return -1;
			else if (a_pos.distanceTo(a1.get_position()) > a_pos.distanceTo(a2.get_position()))
				return 1;
			else
				return 0;
		};
		return Collections.min(as, cmp);

	}

}
