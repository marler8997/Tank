package jmar.awt;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

public class JPanelX extends JPanel {
	public JPanelX() {
		super();
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
	}
}
