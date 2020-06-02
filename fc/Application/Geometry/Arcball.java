// Copyright (c) 2016,2017 Frédéric Claux, Université de Limoges. Tous droits réservés.

package fc.Application.Geometry;

import fc.Math.Quaternion;
import fc.Math.Vec3f;

public class Arcball
{
	/*!
	 *	CArcball default constructor
	 */
	public Arcball()
	{
		
	}

	/*!
	 *	CArcball constructor
	 *  \param width  width of the window
	 *  \param height height of the window
	 *  \param ox     original x position
	 *  \param oy     original y position
	 */
	public Arcball(int win_width, int win_height, int ox, int oy)
	{
		m_radius = (win_width < win_height ) ? win_width/2 : win_height/2;
		m_center = new Vec3f( win_width/2, win_height/2, 0 );
		Vec3f p = new Vec3f(ox, oy, 0);
		m_position = plane2sphere(p);
	};

	/*!
	 *	Update the arcball
	 *	\param nx current x-position of mouse
	 *    \param ny current y-position of mouse
	 *    \return quoternion of the roation from the old position to the new position
	 */
	public Quaternion update(int nx, int ny)
	{
		Vec3f position = plane2sphere(new Vec3f(nx, ny, 0));
		Vec3f cp = m_position.cross(position);
		Quaternion r = new Quaternion(cp.x, cp.y, cp.z, m_position.dot(position));
		m_position = position;
		return r;
	};


	/*!
	 *	mapping a planar point v to the unit sphere point r
	 *  \param v input planar point
	 *  \param output: point on the unit sphere
	 */
	public Vec3f plane2sphere(Vec3f v)
	{
		Vec3f f = new Vec3f(v.x, v.y, v.z);
		f = f.div(m_radius);
		float l = (float)Math.sqrt(f.dot(f));
		if (l > 1.0f)
		    return new Vec3f( f.x/l, f.y/l, 0);
		float fz = (float)Math.sqrt(1.0 - l*l);
		
		return new Vec3f(f.x, f.y, fz);
	};

	/*!
	 *	current position on the unit sphere
	 */
	public Vec3f m_position;
	/*!
	 *	radius of the sphere
	 */
	public float m_radius;
	/*!
	 *	center of sphere on the plane
	 */
	public Vec3f m_center;
}
