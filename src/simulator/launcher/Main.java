package simulator.launcher;

import java.io.File;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.json.JSONObject;
import org.json.JSONTokener;
import simulator.model.CarnivoreAnimals;
import simulator.control.Controller;
import simulator.factories.Builder;
import simulator.factories.BuilderBasedFactory;
import simulator.factories.DefaultRegionBuilder;
import simulator.factories.DynamicSupplyRegionBuilder;
import simulator.factories.Factory;
import simulator.factories.SelectClosestBuilder;
import simulator.factories.SelectFirstBuilder;
import simulator.factories.SelectYoungestBuilder;
import simulator.factories.SheepBuilder;
import simulator.factories.WolfBuilder;
import simulator.misc.Utils;
import simulator.model.Animal;
import simulator.model.Region;
import simulator.model.SelectionStrategy;
import simulator.model.Simulator;
import simulator.view.MainWindow;

public class Main {

	/*
	 * //La linea de comandos para ejecutar el modo batch es: -m batch -i
	 * resources/examples/ex1.json -o resources/tmp/myout.json -t 60.0 -dt 0.03 -sv
	 * 
	 * 
	 * //Para añadir observador en modo bacth: - creas clase que vaya a implementar
	 * al observador (en model!) - si debe imprimir, acordarte de metodo extra que
	 * imprima - añades en el main atributo observador - añades comando en el parse
	 * y en el build_options - inicializas el nuevo observador en modo batch - lo
	 * imprimes
	 * 
	 * 
	 * //Para añadir nueva tabla: - creas clase tablaXTableModel que extiende a
	 * abstractTableModel e implementa el observador - la añades en el mainWindow (o
	 * donde quieres que se vea)
	 * 
	 * 
	 * //Para añadir nuevo animal/region: - fill_in_data del builder cambia la info
	 * del changeRegionsDialog - añadimos clase builder de la nueva region/animal -
	 * la añadimos en el main cuando inicializamos las factorias
	 * 
	 */

	private enum ExecMode {
		BATCH("batch", "Batch mode"), GUI("gui", "Graphical User Interface mode");

		private String _tag;
		private String _desc;

		private ExecMode(String modeTag, String modeDesc) {
			_tag = modeTag;
			_desc = modeDesc;
		}

		public String get_tag() {
			return _tag;
		}

		public String get_desc() {
			return _desc;
		}
	}

	// CONSTANTES
	private final static Double _default_time = 10.0;
	private final static Double _default_delta_time = 0.03;
	private final static int _default_height = 600;
	private final static int _default_width = 800;
	private final static int _default_rows = 15;
	private final static int _default_cols = 20;

	// ATRIBUTOS
	private static Double _time = null;
	public static Double _delta_time = null;
	private static String _in_file = null;
	private static String _out_file = null;
	private static Boolean _sv = false;
	private static ExecMode _mode = ExecMode.GUI;
	public static Factory<Region> _regions_factory;
	public static Factory<Animal> _animals_factory;
	private static Boolean carnivore_animal = false;

	private static void parse_args(String[] args) {

		// define the valid command line options
		//
		Options cmdLineOptions = build_options();

		// parse the command line as provided in args
		//
		CommandLineParser parser = new DefaultParser();
		try {
			CommandLine line = parser.parse(cmdLineOptions, args);
			parse_help_option(line, cmdLineOptions);
			parse_in_file_option(line);
			parse_out_file_option(line);
			parse_time_option(line);
			parse_delta_time_option(line);
			parse_sv_option(line);
			parse_mode_option(line);
			parse_car_option(line);

			// if there are some remaining arguments, then something wrong is
			// provided in the command line!
			//
			String[] remaining = line.getArgs();
			if (remaining.length > 0) {
				String error = "Illegal arguments:";
				for (String o : remaining)
					error += (" " + o);
				throw new ParseException(error);
			}

		} catch (ParseException e) {
			System.err.println(e.getLocalizedMessage());
			System.exit(1);
		}

	}

	private static Options build_options() {
		Options cmdLineOptions = new Options();
		// delta time
		cmdLineOptions.addOption(Option.builder("dt").longOpt("delta-time").hasArg()
				.desc("A double representing actual time, in seconds, per simulation step. Default value: "
						+ _default_delta_time + ".")
				.build());

		// help
		cmdLineOptions.addOption(Option.builder("h").longOpt("help").desc("Print this message.").build());

		// input file
		cmdLineOptions
				.addOption(Option.builder("i").longOpt("input").hasArg().desc("Initial configuration file.").build());

		// output file
		cmdLineOptions.addOption(
				Option.builder("o").longOpt("output").hasArg().desc("Output file, where output is written.").build());

		// simulator viewer
		cmdLineOptions.addOption(
				Option.builder("sv").longOpt("simple-viewer").desc("Show the viewer window in console mode.").build());

		// time
		cmdLineOptions.addOption(Option.builder("t").longOpt("time").hasArg()
				.desc("An real number representing the total simulation time in seconds. Default value: "
						+ _default_time + ".")
				.build());
		// mode
		cmdLineOptions.addOption(Option.builder("m").longOpt("mode").hasArg().desc(
				"Execution Mode. Possible values: 'batch (Batch mode), 'gui' (Graphical User Interface mode). Default value: 'gui'.")
				.build());
		// car
		cmdLineOptions.addOption(Option.builder("car").longOpt("carnivore")
				.desc("Carnivores: counts the numbers of steps in which a region has more than three carnivore animals")
				.build());

		return cmdLineOptions;
	}

//METODOS PARSE
	private static void parse_help_option(CommandLine line, Options cmdLineOptions) {
		if (line.hasOption("h")) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(Main.class.getCanonicalName(), cmdLineOptions, true);
			System.exit(0);
		}
	}

	private static void parse_in_file_option(CommandLine line) throws ParseException {
		_in_file = line.getOptionValue("i");
		if (_mode == ExecMode.BATCH && _in_file == null) {
			throw new ParseException("In batch mode an input configuration file is required");
		}
	}

	private static void parse_out_file_option(CommandLine line) throws ParseException {
		_out_file = line.getOptionValue("o");
		if (_mode == ExecMode.BATCH && _out_file == null) {
			throw new ParseException("In batch mode an output configuration file is required");
		}
	}

	private static void parse_time_option(CommandLine line) throws ParseException {
		String t = line.getOptionValue("t", _default_time.toString());
		try {
			_time = Double.parseDouble(t);
			assert (_time >= 0);
		} catch (Exception e) {
			throw new ParseException("Invalid value for time: " + t);
		}
	}

	private static void parse_delta_time_option(CommandLine line) throws ParseException {
		String dt = line.getOptionValue("dt", _default_delta_time.toString());
		try {
			_delta_time = Double.parseDouble(dt);
			assert (_delta_time >= 0);
		} catch (Exception e) {
			throw new ParseException("Invalid value for delta time: " + dt);
		}
	}

	private static void parse_sv_option(CommandLine line) {
		if (line.hasOption("sv")) {
			_sv = true;
		}
	}

	private static void parse_mode_option(CommandLine line) throws ParseException {
		if (line.hasOption("m")) {
			_mode = null;
			String m = line.getOptionValue("m");
			for (ExecMode e : ExecMode.values()) {
				if (e.get_tag().equals(m.toLowerCase())) {
					_mode = e;
				}
			}
			if (_mode == null) {
				throw new ParseException("An existing mode must be selected");
			}
		}
	}

	private static void parse_car_option(CommandLine line) {
		if (line.hasOption("car")) {
			carnivore_animal = true;
		}
	}

//METODOS FACTORIAS
	private static void init_factories() {
		// añadimos todos los tipos de selecciones
		List<Builder<SelectionStrategy>> selection_strategy_builders = new ArrayList<>();
		selection_strategy_builders.add(new SelectFirstBuilder());
		selection_strategy_builders.add(new SelectClosestBuilder());
		selection_strategy_builders.add(new SelectYoungestBuilder());
		Factory<SelectionStrategy> selection_strategy_factory = new BuilderBasedFactory<SelectionStrategy>(
				selection_strategy_builders);

		// añadimos todos los tipos de regiones
		List<Builder<Region>> regions_builders = new ArrayList<>();
		regions_builders.add(new DefaultRegionBuilder());
		regions_builders.add(new DynamicSupplyRegionBuilder());
		_regions_factory = new BuilderBasedFactory<Region>(regions_builders);

		// añadimos todos los tipos de animales
		List<Builder<Animal>> animals_builders = new ArrayList<>();
		animals_builders.add(new SheepBuilder(selection_strategy_factory));
		animals_builders.add(new WolfBuilder(selection_strategy_factory));
		_animals_factory = new BuilderBasedFactory<Animal>(animals_builders);

	}

//JSONs
	private static JSONObject load_JSON_file(InputStream in) {
		return new JSONObject(new JSONTokener(in));
	}

//METODOS DE EJECUCION
	private static void start_batch_mode() throws Exception {
		InputStream is = new FileInputStream(new File(_in_file));
		JSONObject jo = load_JSON_file(is);
		OutputStream out = new FileOutputStream(new File(_out_file));
		Simulator sim;
		sim = new Simulator(jo.getInt("cols"), jo.getInt("rows"), jo.getInt("width"), jo.getInt("height"),
				_animals_factory, _regions_factory);
		Controller controller;
		controller = new Controller(sim);
		controller.load_data(jo);
		CarnivoreAnimals ca = new CarnivoreAnimals(controller);
		controller.run(_time, _delta_time, _sv, out);
		if (carnivore_animal) {
			ca.imprimir_animales();
		}
		out.close();
	}

	private static void start_GUI_mode() throws Exception {
		Simulator sim;
		Controller controller;
		if (_in_file != null) {
			InputStream is = new FileInputStream(new File(_in_file));
			JSONObject jo = load_JSON_file(is);
			sim = new Simulator(jo.getInt("cols"), jo.getInt("rows"), jo.getInt("width"), jo.getInt("height"),
					_animals_factory, _regions_factory);
			controller = new Controller(sim);
			controller.load_data(jo);
		} else {
			sim = new Simulator(_default_cols, _default_rows, _default_width, _default_height, _animals_factory,
					_regions_factory);
			controller = new Controller(sim);
		}
		SwingUtilities.invokeAndWait(() -> new MainWindow(controller));
	}

	private static void start(String[] args) throws Exception {
		init_factories();
		parse_args(args);
		switch (_mode) {
		case BATCH:
			start_batch_mode();
			break;
		case GUI:
			start_GUI_mode();
			break;
		}
	}

//MAIN
	public static void main(String[] args) {
		Utils._rand.setSeed(2147483647l);
		try {
			start(args);
		} catch (Exception e) {
			System.err.println("Something went wrong ...");
			System.err.println();
			e.printStackTrace();
		}
	}
}
