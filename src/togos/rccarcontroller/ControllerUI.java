package togos.rccarcontroller;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JPanel;

public class ControllerUI
{
	interface StateChangeListener<T> {
		public void stateChanged(T t);
	}
	
	static class InputWatcher extends KeyAdapter {
		Set<Integer> keysDown = new HashSet<Integer>();
		Set<StateChangeListener<InputWatcher>> changeListeners = new HashSet<StateChangeListener<InputWatcher>>();
		
		@Override public void keyPressed(KeyEvent e) {
			keysDown.add(e.getKeyCode());
			for( StateChangeListener<InputWatcher> l : changeListeners ) l.stateChanged(this);
		}
		
		@Override public void keyReleased(KeyEvent e) {
			keysDown.remove(e.getKeyCode());
			for( StateChangeListener<InputWatcher> l : changeListeners ) l.stateChanged(this);
		}
		
		public void addChangeListener(StateChangeListener<InputWatcher> l) {
			changeListeners.add(l);
		}
		public void removeChangeListener(StateChangeListener<InputWatcher> l) {
			changeListeners.remove(l);
		}
		
		public boolean keyIsDown(int kc) {
			return keysDown.contains(kc);
		}
	}
	
	static class ArrowPanel extends JPanel {
		private static final long serialVersionUID = -3944262117176027072L;
		
		// -1 = stopped, 0 = forward, 1 = forward-right, 2 = right, ... 7 = forward-left 
		public int direction = -1;
		
		static final Polygon arrowShape = new Polygon(
			new int[] { 0, 2, 1, 1, -1, -1, -2, 0 },
			new int[] { -2, 0, 0, 2, 2, 0, 0, -2 },
			8
		);
		
		@Override public void paint(Graphics _g) {
			Graphics2D g = (Graphics2D)_g;
			
			g.setColor(getBackground());
			Rectangle clip = g.getClipBounds();
			g.fillRect(clip.x, clip.y, clip.width, clip.height);
			
			int w = getWidth();
			int h = getHeight();
			int s = Math.min(w,h);
			int cx = w/2, cy = h/2;
			
			AffineTransform savedXf = g.getTransform();
			{
				g.translate(cx, cy);
				g.scale(s/6, s/6);
				if( direction == -1 ) {
					g.setColor(Color.GRAY);
					g.fillRect(-1, -1, 2, 2);
				} else {
					g.rotate( direction * Math.PI/4 );
					g.setColor(Color.WHITE);
					g.fillPolygon(arrowShape);
				}
				g.fillRect(30, 30, 10, 10);
			}
			g.setTransform(savedXf);
		}
		
		public void setDirection( int dir ) {
			if( dir != direction ) {
				direction = dir;
				repaint();
			}
		}
	}
	
	public static void main( String[] args ) {
		final ArrowPanel arrowPanel = new ArrowPanel();
		arrowPanel.setBackground(Color.BLACK);
		arrowPanel.setPreferredSize(new Dimension(256, 256));

		final InputWatcher inputWatcher = new InputWatcher();
		inputWatcher.addChangeListener(new StateChangeListener<InputWatcher>() {
			protected int pick( boolean a, boolean b, int av, int nv, int bv ) {
				return
					a && !b ? av :
					b && !a ? bv :
					nv;
			}
			
			protected int calcDirection( boolean up, boolean down, boolean left, boolean right ) {
				if( up && !down ) {
					return pick( left, right, 7, 0, 1 );
				} else if( down && !up ) {
					return pick( left, right, 5, 4, 3 );
				} else {
					return pick( left, right, 6, -1, 2 );
				}
			}
			
			@Override public void stateChanged(InputWatcher t) {
				boolean up    = t.keyIsDown(KeyEvent.VK_UP   ) || t.keyIsDown(KeyEvent.VK_W);
				boolean down  = t.keyIsDown(KeyEvent.VK_DOWN ) || t.keyIsDown(KeyEvent.VK_S);
				boolean left  = t.keyIsDown(KeyEvent.VK_LEFT ) || t.keyIsDown(KeyEvent.VK_A);
				boolean right = t.keyIsDown(KeyEvent.VK_RIGHT) || t.keyIsDown(KeyEvent.VK_D);
				
				arrowPanel.setDirection(calcDirection( up, down, left, right ));
			}
		});
		arrowPanel.addKeyListener(inputWatcher);
		
		final Frame f = new Frame("RC Car Controller");
		f.add(arrowPanel);
		f.pack();
		f.addWindowListener(new WindowAdapter() {
			@Override public void windowClosing(WindowEvent e) {
				f.dispose();
			}
		});
		f.setVisible(true);
		arrowPanel.requestFocus();
	}
}
