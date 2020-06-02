// Copyright (c) 2016,2017 Fr�d�ric Claux, Universit� de Limoges. Tous droits r�serv�s.

package fc.Application.Geometry.Simple;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.opengl.GLCanvas;
import org.eclipse.swt.opengl.GLData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.lwjgl.opengl.GL;

import fc.Math.Vec3f;
import fc.Rendering.Camera;

public class GeometryViewerCanvas extends GLCanvas
{
	private GeometryDocument m_Document;
	public Camera m_Camera;
	private DualContouringRenderer m_RendererDualConturing;
	private MarchingCubesRenderer m_RendererMarchingCubes;
	private SphereTracerRenderer m_RendererSphereTracer;

	private static GLData createGLData()
	{
		GLData data = new GLData();
		data.doubleBuffer = true;
		return data;
	}
	
	public GeometryViewerCanvas(Composite parent, int style, String m_renderer)
	{
		super(parent, style, createGLData());
		final String renderer = (m_renderer != null) ? m_renderer : "sphere";
		if (m_Camera != null) m_Camera = null;
		m_Camera = new Camera(new Vec3f(0,0,40), new Vec3f(0,0,0));

		addListener(SWT.Resize,  new Listener()
		{
			@Override
		    public void handleEvent(Event e)
			{
				switch (renderer){
					case "marching": if(m_RendererMarchingCubes != null) m_RendererMarchingCubes.setViewport(getSize().x, getSize().y);break;
					case "dual": if(m_RendererDualConturing != null) m_RendererDualConturing.setViewport(getSize().x, getSize().y);break;
					case "sphere": if(m_RendererSphereTracer != null) m_RendererSphereTracer.setViewport(getSize().x, getSize().y);break;
				}
			}
		});

		addPaintListener(new PaintListener()
		{
			@Override
			public void paintControl(PaintEvent e)
			{
		    	setCurrent();

				switch (renderer){
					case "marching": if(m_RendererMarchingCubes != null) m_RendererMarchingCubes.renderScene();break;
					case "dual": if(m_RendererDualConturing != null) m_RendererDualConturing.renderScene();break;
					case "sphere": if(m_RendererSphereTracer != null) m_RendererSphereTracer.renderScene();break;
				}
				
		        swapBuffers();
			}
		});
		
		setCurrent();
		GL.createCapabilities();
		
		// OpenGL functions may only be called once GL.createCapabilities has been invoked
		//m_Renderer = new GeometryRenderer(m_Document, m_Camera);
		//m_Renderer = new MarchingCubesRenderer(m_Document, m_Camera);
		//m_Renderer = new SphereTracerRenderer(m_Document, m_Camera);
		switch (renderer){
			case "marching": this.m_RendererMarchingCubes = new MarchingCubesRenderer(m_Document, m_Camera);
				System.out.println("1");break;
			case "dual": this.m_RendererDualConturing = new DualContouringRenderer(m_Document,m_Camera);
				System.out.println("2");break;
			case "sphere": this.m_RendererSphereTracer = new SphereTracerRenderer(m_Document,m_Camera);
				System.out.println("3");break;
		}
	}

	public void setDocument(GeometryDocument doc)
	{
		m_Document = doc;
	}
	
	@Override
	public void dispose()
	{
		super.dispose();
	}
}
