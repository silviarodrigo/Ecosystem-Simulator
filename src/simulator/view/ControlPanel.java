package simulator.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import org.json.JSONObject;
import org.json.JSONTokener;

import simulator.control.Controller;
import simulator.launcher.Main;

class ControlPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private Controller _ctrl;
	private ChangeRegionsDialog _changeRegionsDialog;
	private JToolBar _toolaBar;
	private JFileChooser _fc;
	private boolean _stopped = false;
	private JButton _quitButton;
	private JButton _openButton;
	private JButton _viewerButton;
	private JButton _regionsButton;
	private JButton _runButton;
	private JButton _stopButton;
	private JLabel label_steps;
	private JLabel label_delta_time;
	private JTextField _delta_time_TextField;
	private JSpinner _stepsSpinner;

	ControlPanel(Controller ctrl) {
		_ctrl = ctrl;
		initGUI();
	}

	private void initGUI() {
		setLayout(new BorderLayout());
		_toolaBar = new JToolBar();
		add(_toolaBar, BorderLayout.PAGE_START);

		// OpenButton
		_toolaBar.addSeparator();
		_openButton = new JButton();
		_openButton.setToolTipText("Load an input file into the simulator");
		_openButton.setIcon(loadImage("resources/icons/open.png"));
		_openButton.addActionListener((e) -> openFileButton());
		_toolaBar.add(_openButton);

		// ViewerButton
		_toolaBar.addSeparator();
		_viewerButton = new JButton();
		_viewerButton.setToolTipText("Map Viewer");
		_viewerButton.setIcon(loadImage("resources/icons/viewer.png"));
		_viewerButton.addActionListener((e) -> {
			SwingUtilities.invokeLater(() -> viewerButton());
		});
		_toolaBar.add(_viewerButton);

		// RegionsButton
		_toolaBar.addSeparator();
		_regionsButton = new JButton();
		_regionsButton.setToolTipText("Change Regions");
		_regionsButton.setIcon(loadImage("resources/icons/regions.png"));
		_regionsButton.addActionListener((e) -> this._changeRegionsDialog.open(ViewUtils.getWindow(this)));
		_toolaBar.add(_regionsButton);

		// RunButton
		_toolaBar.addSeparator();
		_runButton = new JButton();
		_runButton.setToolTipText("Run the simulator");
		_runButton.setIcon(loadImage("resources/icons/run.png"));
		_runButton.addActionListener((e) -> run());
		_toolaBar.add(_runButton);

		// StopButton
		_toolaBar.addSeparator();
		_stopButton = new JButton();
		_stopButton.setToolTipText("Stop the simulator");
		_stopButton.setIcon(loadImage("resources/icons/stop.png"));
		_stopButton.addActionListener((e) -> this._stopped = true);
		_toolaBar.add(_stopButton);

		// StepsSpinner
		_toolaBar.addSeparator();
		label_steps = new JLabel("Steps: ");
		_toolaBar.add(label_steps);
		_stepsSpinner = new JSpinner(new SpinnerNumberModel(10000, 1, 10000, 1));
		_stepsSpinner.setMaximumSize(new Dimension(80, 40));
		_stepsSpinner.setMinimumSize(new Dimension(80, 40));
		_stepsSpinner.setPreferredSize(new Dimension(80, 40));
		_stepsSpinner.setToolTipText("Simulator steps to run: 1-10000");
		_toolaBar.add(_stepsSpinner);

		// DeltaTimeTextField
		_toolaBar.addSeparator();
		label_delta_time = new JLabel("Delta-Time: ");
		_toolaBar.add(label_delta_time);
		_delta_time_TextField = new JTextField(Main._delta_time.toString());
		_delta_time_TextField.setToolTipText("Real time (seconds) corresponding to a step");
		_toolaBar.add(_delta_time_TextField);

		// Quit Button
		_toolaBar.add(Box.createGlue()); // this aligns the button to the right
		_toolaBar.addSeparator();
		_quitButton = new JButton();
		_quitButton.setToolTipText("Exit");
		_quitButton.setIcon(loadImage("resources/icons/exit.png"));
		_quitButton.addActionListener((e) -> ViewUtils.quit(this));
		_toolaBar.add(_quitButton);

		// Inicializamos por defecto fc a nuestro directorio
		this._fc = new JFileChooser();
		this._fc.setCurrentDirectory(new File(System.getProperty("user.dir") + "/resources/examples"));

		// Inicializamos _changeRegionsDialog
		this._changeRegionsDialog = new ChangeRegionsDialog(this._ctrl);
	}

//METODOS DE BOTONES
	// Ejecutan la simulacion
	private void run() {
		this._stopped = false;
		setEnableButtons(false);
		try {
			int steps = Integer.parseInt(this._stepsSpinner.getValue().toString());
			double dt = Double.parseDouble(_delta_time_TextField.getText());
			if (dt < 0)
				throw new Exception();
			run_sim(steps, dt);
		} catch (Exception e) {
			ViewUtils.showErrorMsg("Invalid delta time parameter");
			this._stopped = true;
			setEnableButtons(true);
		}
	}

	private void run_sim(int n, double dt) {
		if (n > 0 && !_stopped) {
			try {
				_ctrl.advance(dt);
				SwingUtilities.invokeLater(() -> run_sim(n - 1, dt));
			} catch (Exception e) {
				ViewUtils.showErrorMsg(e.getMessage());
				setEnableButtons(true);
				_stopped = true;
			}
		} else {
			setEnableButtons(true);
			_stopped = true;
		}
	}

	// Bloquea y habilita los botones
	private void setEnableButtons(boolean b) {
		this._quitButton.setEnabled(b);
		this._openButton.setEnabled(b);
		this._viewerButton.setEnabled(b);
		this._regionsButton.setEnabled(b);
		this._runButton.setEnabled(b);
	}

	// Boton fileChooser
	private void openFileButton() {
		int i = this._fc.showOpenDialog(ViewUtils.getWindow(this));
		if (i == JFileChooser.APPROVE_OPTION) {
			try {
				InputStream is = new FileInputStream(this._fc.getSelectedFile());
				JSONObject jo = new JSONObject(new JSONTokener(is));
				this._ctrl.reset(jo.getInt("cols"), jo.getInt("rows"), jo.getInt("width"), jo.getInt("height"));
				this._ctrl.load_data(jo);
			} catch (Exception e) {
				ViewUtils.showErrorMsg(e.getMessage());
			}
		}
	}

//OTROS METODOS
	private ImageIcon loadImage(String path) {
		return new ImageIcon(Toolkit.getDefaultToolkit().createImage(path));
		
	}

	private void viewerButton() {
		//Usamos ViewUtils.getWindow(this) para coger el Frame Parent
		new MapWindow(ViewUtils.getWindow(this), _ctrl);
	}
}
