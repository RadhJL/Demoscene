// Copyright (c) 2016,2017 Fr�d�ric Claux, Universit� de Limoges. Tous droits r�serv�s.

package fc.Rendering;

import fc.Math.Matrix;
import fc.Math.Quaternion;
import fc.Math.Vec3f;
import fc.Math.Vec4f;

public class Camera
{
	public float fovYindegrees = 45.0f;
	public float focalDistance = 0.1f;
	public float zfar = 100.0f;

	private Matrix m_ModelView;
	private Matrix m_Projection;
    
    public Matrix getProjectionMatrix()
    {
    	return m_Projection;
    }
    
    public void setAspectRatio(float aspectRatio)
    {
    	m_Projection = Matrix.glhPerspectivef2(fovYindegrees, aspectRatio, focalDistance, zfar);
    }

    public Camera()
    {
    	m_ModelView = new Matrix();
    	m_Projection = Matrix.glhPerspectivef2(fovYindegrees, 1.0f, focalDistance, zfar);
    }
    
    public Camera(Vec3f pos, Vec3f lookAt)
    {
    	m_ModelView = Matrix.gluLookAt(pos, lookAt, new Vec3f(0,1,0));
    }
    
    public Camera(Matrix modelView, Matrix proj)
    {
    	m_ModelView = modelView;
    	m_Projection = proj;
    }
    
    public Matrix getModelViewMatrix()
    {
    	return new Matrix(m_ModelView);
    }
    
    public Vec3f getRotationSpacePosition() // Vec3(0,0,'distance from eye to (0,0,0)')
    {
    	Vec3f f = getForward();
    	Vec3f eye = getWorldSpacePosition();
        Quaternion q = Quaternion.createRotationFromTo(f, new Vec3f(0,0,-1));
        return Matrix.fromQuaternion(q).mul(new Vec4f(eye, 1)).toVec3f();
    }
    
    public Vec3f getWorldSpacePosition() // camera eye position in world coordinates
    {
    	return m_ModelView.invert().mul(new Vec4f(0,0,0,1)).toVec3f();
    }
    
    public void translateRotationSpacePosition(Vec3f pos)
    {
        m_ModelView.set(0, 3, m_ModelView.at(0, 3) - pos.x);
    	m_ModelView.set(1, 3, m_ModelView.at(1, 3) - pos.y);
    	m_ModelView.set(2, 3, m_ModelView.at(2, 3) - pos.z);
    }
    
    private static float sign(float v)
    {
    	return v < 0 ? -1 : 1;
    }
    
    // See https://github.com/openscenegraph/OpenSceneGraph/blob/master/src/osg/Matrix_implementation.cpp
    // There are 3 implementations.. which one to choose?
    public Quaternion getRotation()
    {
    	Matrix m = m_ModelView;
    	
    	// sign : 
    	
    	float w = 0.5f * (float)Math.sqrt(Math.max(0.0f, 1.0f + m.at(0,0) + m.at(1,1) + m.at(2,2)));
    	float x = 0.5f * (float)Math.sqrt(Math.max(0.0f, 1.0f + m.at(0,0) - m.at(1,1) - m.at(2,2)));
    	float y = 0.5f * (float)Math.sqrt(Math.max(0.0f, 1.0f - m.at(0,0) + m.at(1,1) - m.at(2,2)));
    	float z = 0.5f * (float)Math.sqrt(Math.max(0.0f, 1.0f - m.at(0,0) - m.at(1,1) + m.at(2,2)));
    	
    	x = x * sign(m.at(2,1) - m.at(1,2));
    	y = y * sign(m.at(0,2) - m.at(2,0));
    	z = z * sign(m.at(1,0) - m.at(0,1));
    	
    	return new Quaternion(x, y, z, w);
	}
    
    // TODO: what about scaling ?
    public void setRotation(Quaternion quat)
    {
    	Matrix r = Matrix.fromQuaternion(quat);
    	
    	// Copy translation from m_ModelView
    	r.set(0, 3, m_ModelView.at(0, 3));
    	r.set(1, 3, m_ModelView.at(1, 3));
    	r.set(2, 3, m_ModelView.at(2, 3));
    	
    	m_ModelView = r;
    }
    
    // See Matrix_implementation::getLookAt
    public Vec3f getForward()
    {
        Matrix m3x3 = new Matrix();
		for (int c=0; c < 3; c++) // Note: 3 here, not 4
		{
			for (int r=0; r < 3; r++)
			{
				m3x3.set(r, c, m_ModelView.at(r, c));
			}
		}

        Vec3f e = m_ModelView.invert().mul(new Vec4f(0,0,0,1)).toVec3f();
        Vec3f f = m_ModelView.invert().mul(new Vec4f(0,0,-1,1)).toVec3f();

        return f.sub(e).norm();
    }
    
    public Vec3f getRight()
    {
        return getForward().cross(getUp());
    }
    
    public Vec3f getUp()
    {
        Matrix m3x3 = new Matrix();
		for (int c=0; c < 3; c++) // Note: 3 here, not 4
		{
			for (int r=0; r < 3; r++)
			{
				m3x3.set(r, c, m_ModelView.at(r, c));
			}
		}

        Vec3f e = m_ModelView.invert().mul(new Vec4f(0,0,0,1)).toVec3f();
        Vec3f u = m_ModelView.invert().mul(new Vec4f(0,1,0,1)).toVec3f();

        return u.sub(e).norm();
    }
}
