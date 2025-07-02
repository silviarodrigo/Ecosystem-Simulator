package simulator.model;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SelectYoungest implements SelectionStrategy {

	// Estrategia que selecciona al animal mas joven de la lista
	public Animal select(Animal a, List<Animal> as) {
		if (as.isEmpty()) {
			return null;
		}
		double age = a.get_age();
		// ordenamos los animales por orden creciente de edad
		Comparator<Animal> cmp = (a1, a2) -> {
			if (Math.abs(age - a1.get_age()) < Math.abs(age - a2.get_age()))
				return -1;
			else if (Math.abs(age - a1.get_age()) > Math.abs(age - a2.get_age()))
				return 1;
			else
				return 0;
		};
		return Collections.min(as, cmp);

	}
}
