package jmar.awt;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

public class JPanelY extends JPanel {
	public JPanelY() {
		super();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	}
}
