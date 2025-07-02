package simulator.factories;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.LinkedList;

public class BuilderBasedFactory<T> implements Factory<T> {

	private Map<String, Builder<T>> _builders;
	private List<JSONObject> _builders_info;

	public BuilderBasedFactory() {
		this._builders = new HashMap<String, Builder<T>>();
		this._builders_info = new LinkedList<>();
	}

	public List<JSONObject> get_info() {
		return Collections.unmodifiableList(_builders_info);
	}

//BUILDERS
	public BuilderBasedFactory(List<Builder<T>> builders) {
		this();
		for (int i = 0; i < builders.size(); i++) {
			add_builder(builders.get(i));
		}
	}

	public void add_builder(Builder<T> b) {
		this._builders.put(b.get_type_tag(), b);
		this._builders_info.add(b.get_info());
	}

//JSONs
	public T create_instance(JSONObject info) {
		if (info == null) {
			throw new IllegalArgumentException("’info’ cannot be null");
		}
		if (this._builders.containsKey(info.getString("type"))) {
			T t = this._builders.get(info.getString("type"))
					.create_instance(info.has("data") ? info.getJSONObject("data") : new JSONObject());
			if (t != null) {
				return t;
			}
		}
		throw new IllegalArgumentException("Unrecognized ‘info’:" + info.toString());
	}

}
