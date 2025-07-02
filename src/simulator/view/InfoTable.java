package simulator.view;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableModel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

public class InfoTable extends JPanel {
	private static final long serialVersionUID = 1L;
	String _title;
	TableModel _tableModel;

	InfoTable(String title, TableModel tableModel) {
		_title = title;
		_tableModel = tableModel;
		initGUI();
	}

	private void initGUI() {
		this.setLayout(new BorderLayout());
		TitledBorder borde = BorderFactory.createTitledBorder(this._title);
		this.setBorder(borde);
		JTable tbl = new JTable(this._tableModel);
		JScrollPane scb = new JScrollPane(tbl, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		this.add(scb);
	}
}
