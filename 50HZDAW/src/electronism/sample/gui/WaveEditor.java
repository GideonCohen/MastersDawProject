package electronism.sample.gui;

import electronism.sample.Sample;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


public class WaveEditor extends JPanel 
{
    private static final long serialVersionUID = 1L;

    /**
	 * The Wave view
	 */
	private WaveView view;
	
	WaveformEditor form;
	Scrollbar scrollbar;
	public WaveEditor()
	{
		
		this.addComponentListener(new ComponentListener() {
		
			public void componentShown(ComponentEvent e) {
				updateScrollbar();
			}
		
			public void componentResized(ComponentEvent e) {
				updateScrollbar();
			}
		
			public void componentMoved(ComponentEvent e) {
				updateScrollbar();
			}
		
			public void componentHidden(ComponentEvent e) {
			}
		
		});
		
		setLayout(new BorderLayout());
		
		scrollbar = new Scrollbar(Scrollbar.HORIZONTAL);
		scrollbar.setMinimum(0);
		scrollbar.setMaximum(0);
		scrollbar.setBlockIncrement(1);
		scrollbar.setUnitIncrement(1);
		scrollbar.setVisible(true);
		scrollbar.addAdjustmentListener(new AdjustmentListener() {
		
			public void adjustmentValueChanged(AdjustmentEvent e) {
				view.setXOffset(e.getValue()+1);
			}
		
		});
		
		
		form = new WaveformEditor(this);

//		add(form, BorderLayout.CENTER);
		add(scrollbar, BorderLayout.SOUTH);
		

		view = new WaveView();
		view.addMouseWheelListener(new MouseWheelListener() {
			
			public void mouseWheelMoved(MouseWheelEvent e) {
				int amt = e.getUnitsToScroll();
				if (amt > 0)
					view.zoom(10);
				else
					view.zoom(-10);
			}
		
		});

		view.addResolutionChangeListener(new ResolutionChangeListener() {
		
			public void change(ResolutionChangeEvent e) {
				updateScrollbar();
			}
		
		});
		
		add(view, BorderLayout.CENTER);
		
		Font font = new Font("Arial", 0, 10);
		WaveVerticalRule vRule = new WaveVerticalRule();
		vRule.setFont(font);
		add(vRule, BorderLayout.WEST);
		
		
		
	}
	
	
	public void setWave(Sample wave)
	{
		this.view.setWave(wave);
		scrollbar.setVisible(true);
		updateScrollbar();
		
	}
	
	public void updateScrollbar()
	{
		if (view != null && view.getWave() != null)
		{
			double newMax = (view.getWave().getNbOfPoints()-view.getInnerWidth()*view.getResolution())/view.getResolution();
			if (newMax < 1) newMax=0;
			scrollbar.setMaximum((int)Math.ceil(newMax));
			repaint();
		}
	}
	
}
