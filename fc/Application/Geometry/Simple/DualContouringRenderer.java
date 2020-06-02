package fc.Application.Geometry.Simple;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL43;

import Services.ClockService;
import fc.GLObjects.GLSLCompileException;
import fc.Math.Matrix;
import fc.Math.Vec3f;
import fc.Rendering.Camera;

public class DualContouringRenderer
{	

	private int m_ViewportWidth;
	private int m_ViewportHeight;
	private FloatBuffer m_Buffer;
	private Camera m_Camera;
	
	static int inf = - 25;
	static int sup = + 25;
	private static float inc = 1.0f;
	
	int computeShaderID; 
	int programID;
	int objects_size = 0;
	int group_x=(int)(-inf + sup + inc);
    int group_y=(int)(-inf + sup + inc);
    int group_z=(int)(-inf + sup + inc);
    int nbGroups = group_x*group_y*group_z;
    ArrayList<Sphere> SphereList;
	ArrayList<Plan> PlanList;
	ArrayList<Cylindre> CylindreList;
	ArrayList<Cone> ConeList;
		  
	private static Point3D[][][] Mat=new Point3D[(int) (-inf + sup + inc )][(int) (-inf + sup + inc)][(int) (-inf + sup + inc)];
	
	void setMat(float x,float y,float z, Point3D p)
	{
		
		int i = (int) (x +(-inf) + inc) -1;
		int j = (int) (y +(-inf) + inc) -1;
		int k = (int) (z +(-inf) + inc) -1;

		Mat[i][j][k] = p;
	}
	
	static Point3D getMat(float x, float y, float z)
	{
		int i = (int) (x +(-inf) + inc) -1;
		int j = (int) (y +(-inf) + inc) -1;
		int k = (int) (z +(-inf) + inc) -1;

		return Mat[i][j][k];
	}
	
	private static int Changement(Segment s) {

		if(s.a.x!=s.b.x) return 0;
		if(s.a.y!=s.b.y) return 1;
						 return 2;
	}
	static int count_primitives = 0;
	static void display(Segment s)
	{
		float min;
		Point3D a = null,b = null,c = null,d = null;
		switch(Changement(s)){
			case 0:
				min=Math.min(s.a.x, s.b.x);
				a=getMat(min,s.a.y-inc,s.a.z-inc);
				b=getMat(min,s.a.y-inc,s.a.z);
				c=getMat(min,s.a.y,s.a.z-inc);
				d=getMat(min,s.a.y,s.a.z);
				break;
			case 1:
				min=Math.min(s.a.y, s.b.y);
				a=getMat(s.a.x-inc,min,s.a.z-inc);
				b=getMat(s.a.x-inc,min,s.a.z);
				c=getMat(s.a.x,min,s.a.z-inc);
				d=getMat(s.a.x,min,s.a.z);
				break;
			
			default:
				min=Math.min(s.a.z,s.b.z);
				a=getMat(s.a.x-inc,s.a.y-inc,min);
				b=getMat(s.a.x-inc,s.a.y,min);
				c=getMat(s.a.x,s.a.y-inc,min);
				d=getMat(s.a.x,s.a.y,min);
		}
	
		if(a!=null && b!= null && c!= null&& d!= null)
		{	
			GL11.glDisable(GL11.GL_CULL_FACE);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			GL11.glLineWidth(1.5f);
		
			for (int q = 0; q < 2; q++)
			{
				if (q == 0) 
				{
					GL11.glColor3d(0.98,0.32,0.87);
					GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
				} else {
					GL11.glColor3d(0.18,0.32,0.37);
					GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
				}
				
				GL11.glBegin(GL11.GL_TRIANGLES);
				GL11.glVertex3d(a.x,a.y,a.z);
				GL11.glVertex3d(b.x,b.y,b.z);
				GL11.glVertex3d(d.x,d.y,d.z);
				
				GL11.glVertex3d(a.x,a.y,a.z);
				GL11.glVertex3d(c.x,c.y,c.z);
				GL11.glVertex3d(d.x,d.y,d.z);
				GL11.glEnd();
				count_primitives+=2;	
			}
		}
		
	}
	
	protected void finalize()
	{
		GL20.glDeleteShader(computeShaderID);
	}
	
	public DualContouringRenderer(GeometryDocument document, Camera camera)
	{

		computeShaderID = GL20.glCreateShader(GL43.GL_COMPUTE_SHADER);
	    programID = GL20.glCreateProgram();
	    GL20.glShaderSource(computeShaderID, new ComputeShaderDualContouring().getCode());
	      GL20.glCompileShader(computeShaderID);
	      if (GL20.glGetShaderi(computeShaderID, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) 
	          throw new GLSLCompileException("infoLog : ", GL20.glGetShaderInfoLog(computeShaderID));
	      GL20.glAttachShader(programID, computeShaderID);
	      GL20.glLinkProgram(programID);
	      
		 m_Camera = camera;

		SphereList =  new ArrayList<Sphere> ();
		PlanList = new ArrayList<Plan>();
		CylindreList = new ArrayList<Cylindre> ();
		ConeList = new ArrayList<Cone> ();

		Sphere sphere0 = new Sphere(new Point3D(0,0,0),5);
		Sphere sphere1 = new Sphere(new Point3D(0,7,0),3.5f);
		Sphere sphere2 = new Sphere(new Point3D(-3.1f,3,4),1.4f);
		Sphere sphere3 = new Sphere(new Point3D(3.1f,3,4),1.4f);
		Sphere sphere4 = new Sphere(new Point3D(2,7,3),1.0f);
		Sphere sphere5 = new Sphere(new Point3D(-2,7,3),1.0f);
		
		Plan plan0 = new Plan(new Point3D(-1,1,0),new Point3D(10,-10,10),new Point3D(10,-10,-10),new Point3D(-10,-10,-10),new Point3D(-10,-10,10));
		Plan plan1 = new Plan(new Point3D(0,0,1),new Point3D(-10,-10,-10),new Point3D(10,-10,-10),new Point3D(10,10,-10),new Point3D(-10,10,-10));
		Plan plan2 = new Plan(new Point3D(0,1,0),new Point3D(-2,5,6),new Point3D(2,5,6),new Point3D(2f,5,3),new Point3D(-2f,5,3));
		Plan plan3 = new Plan(new Point3D(0,1,0),new Point3D(15,-5,20),new Point3D(15,-5,-20),new Point3D(-15,-5,-20),new Point3D(-15,-5,20));
		Plan plan4 = new Plan(new Point3D(0,1,0),new Point3D(15,-5,20),new Point3D(15,-5,-20),new Point3D(-15,-5,-20),new Point3D(-15,-5,20));
		
		Cylindre cylindre0 = new Cylindre(new Point3D(-2,-9,0),1f,6);
		Cylindre cylindre1 = new Cylindre(new Point3D(2,-9,0),1f,6);
		Cylindre cylindre2 = new Cylindre(new Point3D(-10,-10,-10),1,20);
		Cylindre cylindre3 = new Cylindre(new Point3D(10,-10,-10),1,20);
		Cylindre cylindre4 = new Cylindre(new Point3D(5,5,0),2,5);
		
		Cone cone0 = new Cone(new Point3D(0.1f,10.0f,0),5.0f,5.0f);
		Cone cone1 = new Cone(new Point3D(18,12,5),3,1.5f);
		Cone cone2 = new Cone(new Point3D(-18,12,5),3,1.5f);
		Cone cone3 = new Cone(new Point3D(-10,10,-10),3.0f,1.5f);
		Cone cone4 = new Cone(new Point3D(10,10,-10),3.0f,1.5f);

		SphereList.add(sphere0);
		SphereList.add(sphere1);
		SphereList.add(sphere2);
		SphereList.add(sphere3);
		SphereList.add(sphere4);
		SphereList.add(sphere5);
		// PlanList.add(plan0);
		PlanList.add(plan1);
		// PlanList.add(plan2);
		// PlanList.add(plan3);
		// PlanList.add(plan4);
		CylindreList.add(cylindre0);
		CylindreList.add(cylindre1);
		CylindreList.add(cylindre2);
		CylindreList.add(cylindre3);
		// CylindreList.add(cylindre4);
		ConeList.add(cone0);
		ConeList.add(cone1);
		ConeList.add(cone2);
		ConeList.add(cone3);
		ConeList.add(cone4);
		
	  if(SphereList.size() > 0 )
	   objects_size += SphereList.size() * SphereList.get(0).size;
	  if(PlanList.size() > 0 )
		   objects_size += PlanList.size() * PlanList.get(0).size;
	  if(ConeList.size() > 0 )
		  objects_size += ConeList.size() * ConeList.get(0).size; 
	  if(CylindreList.size() > 0 )
		     objects_size += CylindreList.size() * CylindreList.get(0).size ;
	}

	void updateClock(){
		for(Sphere s:SphereList)
			s.updateClock();
		for(Plan p:PlanList)
			p.updateClock();
		for(Cone c:ConeList)
			c.updateClock();
		for(Cylindre cy:CylindreList)
			cy.updateClock();
	}

	boolean grow = true;
	boolean grow_c = true;

	void animateScene(){

		updateClock();

		if( SphereList.get(0).getTime() < 5.0){
			if(grow){
					SphereList.get(0).setRadius((float)SphereList.get(0).getTime());
					ConeList.get(0).setRadius(SphereList.get(0).getTime());
					CylindreList.get(0).RotateY(SphereList.get(0).getTime()/30);
					CylindreList.get(1).RotateY(SphereList.get(0).getTime()/30);
		
			}else{
					SphereList.get(0).setRadius( 5 - (float)SphereList.get(0).getTime());
					ConeList.get(0).setRadius(5 - (float)SphereList.get(0).getTime());
					CylindreList.get(0).RotateY(0 - SphereList.get(0).getTime()/30);
					CylindreList.get(1).RotateY(0 - SphereList.get(0).getTime()/30);
				}
		}else if ( SphereList.get(0).getTime() > 6){
				SphereList.get(0).resetClock();
				if(grow)
				 grow = false;
				else 
				 grow = true;
		}

		if( CylindreList.get(2).getTime() <=20){
			if(grow_c){
				CylindreList.get(2).setHeight(CylindreList.get(2).getTime() );
				CylindreList.get(3).setHeight(CylindreList.get(2).getTime() );
			}else{
					CylindreList.get(2).setHeight(20 - CylindreList.get(2).getTime() );
					CylindreList.get(3).setHeight(20 - CylindreList.get(2).getTime() );
				}
		}else if ( CylindreList.get(2).getTime() > 20){
				CylindreList.get(2).resetClock();
				if(grow_c)
					grow_c = false;
				else 
					grow_c = true;
		}

		ConeList.get(1).RotateY( ConeList.get(1).getTime() / (float) 200);
		ConeList.get(1).RotateX( -ConeList.get(1).getTime() / (float) 160);
		ConeList.get(2).RotateY( -ConeList.get(1).getTime() / (float) 200);
		ConeList.get(2).RotateX( -ConeList.get(1).getTime() / (float) 100);

		if(ConeList.get(1).getTime() > 3)
			ConeList.get(1).resetClock();

		PlanList.get(0).RotateZ(PlanList.get(0).getTime() / (float) 15);
		if(PlanList.get(0).getTime() > 1)
			PlanList.get(0).resetClock();
		
	}

	void fillBuffer(FloatBuffer floatBuffer) {
		for(Sphere i:SphereList) 
			for(int j = 0 ; j < i.size/4; j++) 
				floatBuffer.put(i.array[j]);

		for(Plan i:PlanList) 
			for(int j = 0 ; j < i.size/4; j++) 
				floatBuffer.put(i.array[j]);
			
		for(Cone i:ConeList) 
			for(int j = 0 ; j < i.size/4; j++) 
				floatBuffer.put(i.array[j]);
		  
		for(Cylindre i:CylindreList) 
			for(int j = 0 ; j < i.size/4; j++) 
				floatBuffer.put(i.array[j]);
		  
		floatBuffer.put(-Float.POSITIVE_INFINITY);
	}
	
	public void renderScene()
	{
		
		long start = System.nanoTime();
		Vec3f position = new Vec3f(0,0,0);
		GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glViewport(0, 0, m_ViewportWidth, m_ViewportHeight);
		GL11.glLightfv(GL11.GL_LIGHT1, GL11.GL_POSITION, arrayToBuffer(new float[] {(float)position.x, (float)position.y, (float)position.z, 0}));

		GL11.glLineWidth(1.0f);

		if (m_Buffer == null) m_Buffer = BufferUtils.createFloatBuffer(16);

		Matrix matrix = m_Camera.getModelViewMatrix();
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadMatrixf((FloatBuffer)m_Buffer.put(matrix.toColumnMajorArray()).flip());
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		matrix = m_Camera.getProjectionMatrix();
		GL11.glLoadMatrixf((FloatBuffer)m_Buffer.put(matrix.toColumnMajorArray()).flip());
		
		animateScene(); // Animation 
		
		int buffer1 = 2;
		GL15.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, buffer1);
		GL15.glBufferData (GL43.GL_SHADER_STORAGE_BUFFER, objects_size + 4, GL43.GL_STATIC_DRAW );
		GL43.glBindBufferBase( GL43.GL_SHADER_STORAGE_BUFFER, 1, buffer1);
		
		FloatBuffer floatBuffer2 = GL43.glMapBufferRange(GL43.GL_SHADER_STORAGE_BUFFER,0,objects_size + 4, GL43.GL_MAP_WRITE_BIT | GL43.GL_MAP_INVALIDATE_BUFFER_BIT).asFloatBuffer();
		fillBuffer(floatBuffer2);
		GL43.glUnmapBuffer(GL43.GL_SHADER_STORAGE_BUFFER);
		
		int buffer0 = 1;
		GL15.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, buffer0);
		GL15.glBufferData (GL43.GL_SHADER_STORAGE_BUFFER, nbGroups*75*4, GL43.GL_STATIC_DRAW );
		GL43.glBindBufferBase( GL43.GL_SHADER_STORAGE_BUFFER, 0, buffer0);
		GL20.glUseProgram(programID);
		GL43.glDispatchCompute(group_x,group_y,group_z);
		GL43.glMemoryBarrier(GL43.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT);
		FloatBuffer floatBuffer1 = GL43.glMapBufferRange(GL43.GL_SHADER_STORAGE_BUFFER,0,nbGroups*75*4, GL43.GL_MAP_READ_BIT).asFloatBuffer();
		
		int index,index2;
		for (int i = 0; i < group_x; i++) 
			for(int j=0 ; j < group_y; j++) 
				for(int k=0 ; k < group_z; k++) 
				{   
					index=i*group_y*group_z+ j*group_z+ k;
					if( floatBuffer1.get(index) >  -100  ){
						Point3D result =new Point3D(floatBuffer1.get(index),floatBuffer1.get(index + nbGroups),floatBuffer1.get(index + 2*nbGroups));  
						setMat(i+inf,j+inf,k+inf,result);	  	
						for(int w=0; w < 12; w++)
						{
							index2 = 3 * nbGroups + (index) * 72 + w * 6;
							float ax = floatBuffer1.get(index2 + 0);
							if(ax < -1000) 
								continue;
							float ay = floatBuffer1.get(index2 + 1);
							float az = floatBuffer1.get(index2 + 2);
							float bx = floatBuffer1.get(index2 + 3);
							float by = floatBuffer1.get(index2 + 4);
							float bz = floatBuffer1.get(index2 + 5);
							display(new Segment(new Point3D(ax,ay,az),new Point3D(bx,by,bz)));
						}
						
					}
				}
		
			GL43.glUnmapBuffer(GL43.GL_SHADER_STORAGE_BUFFER);
			GL20.glUseProgram(0);
			System.out.println(count_primitives+" Displayed triangles ");
			System.out.println("It took "+(System.nanoTime()-start)/1000000.0+" ms");
			Mat=new Point3D[(int) (-inf + sup + inc )][(int) (-inf + sup + inc)][(int) (-inf + sup + inc)];
			count_primitives = 0;
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
	
}

class Point3D implements Comparable<Point3D> 
{
	float x;
	float y;
	float z;
	
	public Point3D(float x,float y, float z)
	{
		this.x=x;
		this.y=y;
		this.z=z;
	}
	
	
	public Point3D(Point3D a)
	{
		this.x=a.x;
		this.y=a.y;
		this.z=a.z;
	}
	
	public String toString() 
	{
		return Float.toString(x)+" "+Float.toString(y)+" "+Float.toString(z);
	}
	
	@Override
	public int compareTo(Point3D o)
	{
	    if(Float.compare(x, o.x)!=0) 
	    	return Float.compare(x, o.x);
	    else if(Float.compare(y, o.y)!=0) 
	     	return Float.compare(y, o.y);
	 	return Float.compare(z, o.z);
	    
	}
	
	public float dot(Point3D a)
	{
		return a.x*x+a.y*y+a.z*z;
	}
	
	public Point3D add(Point3D a)
	{
		return new Point3D (a.x + x, a.y + y, a.z + z);
	}
	
	public Point3D sous(Point3D a) {
		
		return new Point3D(x-a.x,y-a.y,z-a.z);
	}
	
	public Point3D mult(float a) 
	{
		return new Point3D(x*a,y*a,z*a) ;
	}
	

	Point3D normalize() {
		float length = length();

		if (length != 0.0)
			return new Point3D(x/length,y/length,z/length);
		else
			return this;
	};

	public float length() {
		
		return (float) Math.sqrt(x*x+y*y+z*z);
	};

}

class Segment
{
	Point3D a;
	Point3D b;
	
	public Segment(Point3D a, Point3D b)
	{
		this.a=new Point3D(a);
		this.b=new Point3D(b);
	}

	public String toString()
	{
		return a.toString()+" | "+b.toString();
	}
}

class Sphere
{
	float[] array;
	float id= 1;
	int size = 4 + 4 * 3 + 4 ;
	ClockService clock;
	Sphere copy;
	//origin, radius
	Sphere(Point3D o, float radius)
	{
		array = new float[] {id,o.x,o.y,o.z,radius};
		clock = new ClockService();
	}

	void updateClock(){
		clock.onIdle();
	}

	void resetClock(){
		clock.restart();
	}
	float getTime(){
		return (float)clock.getSeconds();
	}
	Point3D getOrigin(){
		return new Point3D(array[1],array[2],array[3]);
	}

	float getRadius(){
		return array[4];
	}
	
	void setOrigin(Point3D p){
		array[1]=p.x;
		array[2]=p.y;
		array[3]=p.z;
	}
	
	void setRadius(float r){
		array[4]=r;
	}
	void Translate(Point3D p) {
		array[1]+=p.x;
		array[2]+=p.y;
		array[3]+=p.z;
	}
	void Scale(Point3D p) {
		array[1]*=p.x;
		array[2]*=p.y;
		array[3]*=p.z;
	}
	void RotateX(float theta) {
		float sin = (float) Math.sin(theta);
	    float cos = (float) Math.cos(theta);
	    float y= array[2];
		float z =array[3];
	    array[2]= y*cos- z*sin;
		array[3]= z*cos + y*sin;
	}
	
	void RotateY(float theta) {
		float sin = (float) Math.sin(theta);
	    float cos = (float) Math.cos(theta);
	    float x= array[1];
		float z =array[3];
	    array[1]= x*cos+ z*sin;
		array[3]= z*cos - x*sin;
	}
	
	void RotateZ(float theta) {
		float sin = (float) Math.sin(theta);
	    float cos = (float) Math.cos(theta);
	    float x= array[1];
		float y =array[2];
	    array[1]= x*cos - y*sin;
		array[3]= y*cos + x*sin;
	}
}

class Plan
{
	float[] array;
	float id = 2;
	int size= 5 * 3 * 4 + 4;
	ClockService clock;
	
	//normal and 4 edges CCW
	Plan(Point3D n,Point3D a,Point3D b,Point3D c,Point3D d)
	{
		array = new float[] {id,n.x,n.y,n.z,a.x,a.y,a.z,b.x,b.y,b.z,c.x,c.y,c.z,d.x,d.y,d.z};
		clock = new ClockService();
	}

	void updateClock(){
		clock.onIdle();
	}

	void resetClock(){
		clock.restart();
	}
	
	float getTime(){
		return (float)clock.getSeconds();
	}
	
	void Translate(Point3D p) {
		array[4]+=p.x;
		array[5]+=p.y;
		array[6]+=p.z;
		array[7]+=p.x;
		array[8]+=p.y;
		array[9]+=p.z;
		array[10]+=p.x;
		array[11]+=p.y;
		array[12]+=p.z;
		array[13]+=p.x;
		array[14]+=p.y;
		array[15]+=p.z;
	}

	void Scale(Point3D p) {
		array[4]*=p.x;
		array[5]*=p.y;
		array[6]*=p.z;
		array[7]*=p.x;
		array[8]*=p.y;
		array[9]*=p.z;
		array[10]*=p.x;
		array[11]*=p.y;
		array[12]*=p.z;
		array[13]*=p.x;
		array[14]*=p.y;
		array[15]*=p.z;
	}
	
	void RotateX(float theta) {
		float sin = (float) Math.sin(theta);
	    float cos = (float) Math.cos(theta);
		float y = array[5];
		float z = array[6];
		array[5]=y*cos- z*sin;
		array[6]=z*cos + y*sin;
		y = array[8];
		z = array[9];
		array[8]=y*cos- z*sin;
		array[9]=z*cos + y*sin;
		y = array[11];
		z = array[12];
		array[11]=y*cos- z*sin;
		array[12]=z*cos + y*sin;
		y = array[14];
		z = array[15];
		array[14]=y*cos- z*sin;
		array[15]=z*cos + y*sin;
		
	}
	
	void RotateY(float theta) {
		float sin = (float) Math.sin(theta);
	    float cos = (float) Math.cos(theta);
		float x = array[4];
		float z = array[6];
		array[4]=x*cos- z*sin;
		array[6]=z*cos + x*sin;
		x = array[7];
		z = array[9];
		array[7]=x*cos- z*sin;
		array[9]=z*cos + x*sin;
		x = array[10];
		z = array[12];
		array[10]=x*cos- z*sin;
		array[12]=z*cos + x*sin;
		x = array[13];
		z = array[15];
		array[13]=x*cos- z*sin;
		array[15]=z*cos + x*sin;
	}
	
	void RotateZ(float theta) {
		float sin = (float) Math.sin(theta);
	    float cos = (float) Math.cos(theta);
		float x = array[4];
		float y = array[5];
		array[4]=x*cos- y*sin;
		array[5]=y*cos + x*sin;
		x = array[7];
		y = array[8];
		array[7]=x*cos- y*sin;
		array[8]=y*cos + x*sin;
		x = array[10];
		y = array[11];
		array[10]=x*cos- y*sin;
		array[11]=y*cos + x*sin;
		x = array[13];
		y = array[14];
		array[13]=x*cos- y*sin;
		array[14]=y*cos + x*sin;
	}
	
}

class Cylindre
{
	float[] array;
	float id = 3;
	int size = 4 + 4 + 4*3 + 4;
	ClockService clock;
	
	//center, radius and height
	Cylindre(Point3D c, float radius, float height)
	{
		array = new float[] {id, c.x, c.y, c.z, radius, height};
		clock = new ClockService();
	}

	void updateClock(){
		clock.onIdle();
	}

	float getTime(){
		return (float)clock.getSeconds();
	}

	void resetClock(){
		clock.restart();
	}
	
	void setCenter(Point3D p){
		array[1]=p.x;
		array[2]=p.y;
		array[3]=p.z;
	}
	
	void setRadius(float r){
		array[4]=r;
	}
	
	void setHeight(float h){
		array[5]=h;
	}

	Point3D getCenter() {
		return new Point3D(array[1],array[2],array[3]); 
	}
	
	float getRadius(){
		return array[4];
	}
	
	float getHeight(){
		return array[5];
	}
	
	void Translate(Point3D p) {
		array[1]+=p.x;
		array[2]+=p.y;
		array[3]+=p.z;
	}
	
	void Scale(Point3D p) {
		array[1]*=p.x;
		array[2]*=p.y;
		array[3]*=p.z;
	}
	
	void RotateX(float theta) {
		float sin = (float) Math.sin(theta);
	    float cos = (float) Math.cos(theta);
	    float y= array[2];
		float z =array[3];
	    array[2]= y*cos- z*sin;
		array[3]= z*cos + y*sin;
	}
	
	void RotateY(float theta) {
		float sin = (float) Math.sin(theta);
	    float cos = (float) Math.cos(theta);
	    float x= array[1];
		float z =array[3];
	    array[1]= x*cos+ z*sin;
		array[3]= z*cos - x*sin;
	}
	
	void RotateZ(float theta) {
		float sin = (float) Math.sin(theta);
	    float cos = (float) Math.cos(theta);
	    float x= array[1];
		float y =array[2];
	    array[1]= x*cos - y*sin;
		array[3]= y*cos + x*sin;
	}
	
}

class Cone
{
	float[] array;
	float id = 4;
	int size= 4 + 4 + 4*3 + 4;
	ClockService clock;
	
	//center, radius and height
	Cone(Point3D c,float radius,float height)
	{
		array = new float[] {id,c.x,c.y,c.z,radius,height};
		clock = new ClockService();
	}
	
	void updateClock(){
		clock.onIdle();
	}

	float getTime(){
		return (float)clock.getSeconds();
	}

	void resetClock(){
		clock.restart();
	}

	void setCenter(Point3D p){
		array[1]=p.x;
		array[2]=p.y;
		array[3]=p.z;
	}
	
	void setRadius(float r){
		array[4]=r;
	}
	
	void setHeight(float h){
		array[5]=h;
	}
	
	Point3D getCenter() {
		return new Point3D(array[1],array[2],array[3]); 
	}
	
	float getRadius(){
		return array[4];
	}
	
	float getHeight(){
		return array[5];
	}

	void Translate(Point3D p) {
		array[1]+=p.x;
		array[2]+=p.y;
		array[3]+=p.z;
	}

	void Scale(Point3D p) {
		array[1]*=p.x;
		array[2]*=p.y;
		array[3]*=p.z;
	}

	void RotateX(float theta) {
		float sin = (float) Math.sin(theta);
	    float cos = (float) Math.cos(theta);
	    float y= array[2];
		float z =array[3];
	    array[2]= y*cos- z*sin;
		array[3]= z*cos + y*sin;
	}
	
	void RotateY(float theta) {
		float sin = (float) Math.sin(theta);
	    float cos = (float) Math.cos(theta);
	    float x= array[1];
		float z =array[3];
	    array[1]= x*cos+ z*sin;
		array[3]= z*cos - x*sin;
	}
	
	void RotateZ(float theta) {
		float sin = (float) Math.sin(theta);
	    float cos = (float) Math.cos(theta);
	    float x= array[1];
		float y =array[2];
	    array[1]= x*cos - y*sin;
		array[3]= y*cos + x*sin;
	}
}





