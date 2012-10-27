package jmar.games;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Label;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.GroupLayout.Alignment;
import javax.swing.JFrame;
import javax.swing.JLabel;

import jmar.ExceptionUtil;

public class PopupWindow {
	public static void PopupSmallMessage(String title, String smallMessage) {
		PopupSmallMessage(title, smallMessage, null);
	}
	public static void PopupSmallMessage(String title, String smallMessage, final ActionListener closedListener) {
		Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
				
		final JFrame frame = new JFrame();
		frame.setTitle(title);
		frame.setLayout(new BorderLayout());
		frame.setAlwaysOnTop(true);
		frame.setSize(500, 200);
		frame.setLocation(screenDim.width / 2 - frame.getWidth() / 2, screenDim.height / 2 - frame.getHeight() / 2);
		
		JLabel label = new JLabel(smallMessage);
		label.setAlignmentX(Component.CENTER_ALIGNMENT);
		frame.add(label);
		frame.addWindowListener(new WindowListener() {
			public void windowOpened(WindowEvent arg0) {}
			public void windowIconified(WindowEvent arg0) {}
			public void windowDeiconified(WindowEvent arg0) {}
			public void windowDeactivated(WindowEvent arg0) {}
			public void windowClosing(WindowEvent arg0) {
				if(closedListener != null) {
					closedListener.actionPerformed(new ActionEvent(frame, arg0.getID(),"windowClosed"));
				}
				frame.dispose();
			}
			public void windowClosed(WindowEvent arg0) {}
			public void windowActivated(WindowEvent arg0) {}
		});
		frame.setVisible(true);
	}

	public static void PopupException(Exception e) {
		PopupException(e, null);
	}
	public static void PopupException(Exception e, final ActionListener closedListener) {
		Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
		
		String className = e.getClass().getSimpleName();
		
		final JFrame frame = new JFrame();
		frame.setLayout(null);
		frame.setTitle(className);
		frame.setAlwaysOnTop(true);
		frame.setSize(500, 250);
		frame.setLocation(screenDim.width / 2 - frame.getWidth() / 2, screenDim.height / 2 - frame.getHeight() / 2);		

		Container contentPane = frame.getContentPane();
		contentPane.setLayout(new FlowLayout());		
		
		Label messageLabel = new Label(e.getMessage());
		TextArea stackTraceTextField = new TextArea(ExceptionUtil.getStackTraceString(e));
	
		contentPane.add(messageLabel);
		contentPane.add(stackTraceTextField);
		
		frame.addWindowListener(new WindowListener() {
			public void windowOpened(WindowEvent arg0) {}
			public void windowIconified(WindowEvent arg0) {}
			public void windowDeiconified(WindowEvent arg0) {}
			public void windowDeactivated(WindowEvent arg0) {}
			public void windowClosing(WindowEvent arg0) {
				if(closedListener != null) {
					closedListener.actionPerformed(new ActionEvent(frame, arg0.getID(),"windowClosed"));
				}
				frame.dispose();
			}
			public void windowClosed(WindowEvent arg0) {}
			public void windowActivated(WindowEvent arg0) {}
		});
		
		frame.setVisible(true);
	}
}
