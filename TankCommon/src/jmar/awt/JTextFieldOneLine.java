package jmar.awt;

import java.awt.Dimension;

import javax.swing.JTextField;

public class JTextFieldOneLine extends JTextField {
	public JTextFieldOneLine() {
		super();
		setMaximumSize(new Dimension(Integer.MAX_VALUE, getPreferredSize().height));
	}
}
