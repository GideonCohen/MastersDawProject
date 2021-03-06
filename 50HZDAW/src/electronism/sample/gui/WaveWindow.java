package electronism.sample.gui;

import electronism.sample.Generator;
import electronism.sample.Sample;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;


public class WaveWindow extends JFrame {
    private static final long serialVersionUID = 1L;
    WaveEditor editor;
	
	public WaveWindow()
	{
		JPanel panel = new JPanel(new BorderLayout());
		
		JButton button = new JButton("Open Wav");
		button.addActionListener(new ActionListener() {
		
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser("./");
				int returnVal = fc.showOpenDialog(null);

				if (returnVal == JFileChooser.APPROVE_OPTION)
				{
					File file = fc.getSelectedFile();
					try {
						Generator generator = new Generator();
						Sample wav = generator.loadStandardizedSample(file);

//						Sample wav = new Sample(file);
						editor.setWave(wav);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					
				}
				
			}
		
		});
		
		
		editor = new WaveEditor();

		
		panel.add(button, BorderLayout.NORTH);
		panel.add(editor, BorderLayout.CENTER);
		
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		this.getContentPane().add(panel);
		
		
	}
}
