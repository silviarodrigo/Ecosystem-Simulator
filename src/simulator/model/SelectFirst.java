package simulator.model;

import java.util.List;

public class SelectFirst implements SelectionStrategy {

	// Estrategia que devuelve el primer animal de la lista si esta no es vacias
	public Animal select(Animal a, List<Animal> as) {
		if (as.isEmpty()) {
			return null;
		} else {
			return as.get(0);
		}
	}
}
