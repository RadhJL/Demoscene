// Copyright (c) 2016,2017 Frédéric Claux, Université de Limoges. Tous droits réservés.

package fc.Application.Geometry.Simple;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import fc.Math.Matrix;
import fc.Math.Vec3f;
import fc.Rendering.Camera;

public class GeometryRenderer
{
	private int m_ViewportWidth;
	private int m_ViewportHeight;
	private FloatBuffer m_Buffer;
	private Camera m_Camera;
	
	public GeometryRenderer(GeometryDocument document, Camera camera)
	{
		m_Camera = camera;
	}
	
	public void setViewport(int width, int height)
	{
		m_ViewportWidth = width;
		m_ViewportHeight = height;
		m_Camera.setAspectRatio((float)width / (float)height);
	}
	    
    private FloatBuffer arrayToBuffer(float[] values)
    {
    	FloatBuffer buf = BufferUtils.createFloatBuffer(values.length);
        for (float v : values)
        	buf.put(v);
        buf.flip();
        return buf;
    }
    
	public void renderScene()
	{
		Vec3f position = new Vec3f(0,0,0);
		
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        
		GL11.glViewport(0, 0, m_ViewportWidth, m_ViewportHeight);
    	GL11.glLightfv(GL11.GL_LIGHT1, GL11.GL_POSITION, arrayToBuffer(new float[] {(float)position.x, (float)position.y, (float)position.z, 0}));
    	
    	GL11.glPointSize(5.0f);
    	
    	if (m_Buffer == null)
    	{
    		m_Buffer = BufferUtils.createFloatBuffer(16);
    	}
    	
    	Matrix matrix = m_Camera.getModelViewMatrix();
    	GL11.glMatrixMode(GL11.GL_MODELVIEW);
    	GL11.glLoadMatrixf((FloatBuffer)m_Buffer.put(matrix.toColumnMajorArray()).flip());
    	GL11.glMatrixMode(GL11.GL_PROJECTION);
    	matrix = m_Camera.getProjectionMatrix();
    	GL11.glLoadMatrixf((FloatBuffer)m_Buffer.put(matrix.toColumnMajorArray()).flip());
    	
    	GL11.glBegin(GL11.GL_POINTS);
    	float inc = 1.0f;
    	for (float x=-10.0f; x <= 10.0f; x += inc)
    	{
        	for (float y=-10.0f; y <= 10.0f; y += inc)
        	{
            	for (float z=-10.0f; z <= 10.0f; z += inc)
            	{
                	GL11.glVertex3f(x, y, z);
            	}
        	}
    	}
    	GL11.glEnd();
 	}
}
