// Copyright (c) 2016,2017 Fr�d�ric Claux, Universit� de Limoges. Tous droits r�serv�s.

package fc.Application.Geometry.Simple;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import fc.Application.Geometry.Arcball;
import fc.Math.Quaternion;
import fc.Math.Vec3f;
import fc.Rendering.Camera;

public class GeometryViewerComposite extends Composite
{
	public GeometryViewerCanvas m_Canvas;
	
	private Arcball m_Arcball;
	
	private boolean m_LeftButtonPressed = false;
	private boolean m_MiddleButtonPressed = false;
	private boolean m_RightButtonPressed = false;
	
	private int m_CursorPosX = -1;
	private int m_CursorPosY = -1;
	private int m_StartX = -1;
	private int m_StartY = -1;
	
	public GeometryViewerComposite(Composite c, int style, String m_renderer)
	{
		super(c, style);
		
		final GeometryViewerComposite that = this;
		
		getShell().getDisplay().addFilter(SWT.MouseDown, new Listener()
		{
			@Override
			public void handleEvent(Event e)
			{
				that.mouseDown(e.button);
				
			}
		});
		
		getShell().getDisplay().addFilter(SWT.MouseUp, new Listener()
		{
			@Override
			public void handleEvent(Event e)
			{
				that.mouseUp(e.button);
				
			}
		});
		
		getShell().getDisplay().addFilter(SWT.MouseMove, new Listener()
		{
			@Override
			public void handleEvent(Event e)
			{
				that.mouseMove(e.x, e.y);
			}
		});
		
		
		FillLayout fillLayout = new FillLayout();
		this.setLayout(fillLayout);

		m_Canvas = new GeometryViewerCanvas(this, SWT.NO_BACKGROUND | SWT.NO_REDRAW_RESIZE, m_renderer);
	}
	
	protected void mouseMove(int x, int y)
	{
		m_CursorPosX = x;
		m_CursorPosY = y;
		
		Camera camera = getCamera();
		if (camera == null)
			return;
		
		if (m_LeftButtonPressed || m_MiddleButtonPressed || m_RightButtonPressed)
			onCameraChanging();
		
		if (m_LeftButtonPressed)
		{
			Quaternion rot = m_Arcball.update(m_CursorPosX - getSize().x/2, getSize().y - m_CursorPosY - getSize().y/2);
			camera.setRotation(rot.mul(camera.getRotation()));
		}
		if (m_MiddleButtonPressed)
		{
			float scale = 5.0f/getSize().y;
			Vec3f trans = new Vec3f(-scale*(m_CursorPosX-m_StartX), -scale*(m_StartY-m_CursorPosY), 0  );
			m_StartX = m_CursorPosX;
			m_StartY = m_CursorPosY;
			//m_Scene.camera.setRotationSpacePosition(m_Scene.camera.getRotationSpacePosition().add(trans));
			camera.translateRotationSpacePosition(trans);
		}
		if (m_RightButtonPressed)
		{
			float scale = 10.0f/getSize().y;
			Vec3f trans = new Vec3f(0, 0, scale*(m_StartY-m_CursorPosY));
			m_StartX = m_CursorPosX;
			m_StartY = m_CursorPosY;
			//m_Scene.camera.setRotationSpacePosition(m_Scene.camera.getRotationSpacePosition().add(trans));
			camera.translateRotationSpacePosition(trans);
			//System.out.println("translated by " + trans.x + "," + trans.y + "," + trans.z);
		}
		
		if (m_LeftButtonPressed || m_MiddleButtonPressed || m_RightButtonPressed)
			onCameraChanged();
		
		repaint();
	}
	
	protected void mouseDown(int button)
	{
		m_StartX = m_CursorPosX;
		m_StartY = m_CursorPosY;

		if (button == 1)
		{
			m_Arcball = new Arcball(getSize().x, getSize().y, m_CursorPosX-getSize().x/2,  getSize().y-m_CursorPosY-getSize().y/2);
			m_LeftButtonPressed = true;
		}
		if (button == 2)
		{
			m_MiddleButtonPressed = true;
		}
		if (button == 3)
		{
			m_RightButtonPressed = true;
		}
		
		repaint();
	}
	
	protected void mouseUp(int button)
	{
		m_StartX = -1;
		m_StartY = -1;

		if (button == 1)
			m_LeftButtonPressed = false;
		if (button == 2)
			m_MiddleButtonPressed = false;
		if (button == 3)
			m_RightButtonPressed = false;
	}
	
	private void repaint()
	{
		for (Control c : getChildren())
		{
			c.redraw(); // invalidate whole client area rectangle
			c.update();
		}
	}
	
	protected Camera getCamera()
	{
		return m_Canvas.m_Camera;
	}
	
	protected void onCameraChanging()
	{
		// Override in derived class
	}
	
	protected void onCameraChanged()
	{
		// Override in derived class
	}
	
	public void setDocument(GeometryDocument doc)
	{
		m_Canvas.setDocument(doc);
	}
	
	public void onIdle() {
		m_Canvas.redraw();
	}
}
