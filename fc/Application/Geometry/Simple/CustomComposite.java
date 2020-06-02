package fc.Application.Geometry.Simple;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL20.glBindAttribLocation;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import javax.imageio.ImageIO;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Scale;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GL20;

import Services.ClockService;
import fc.GLObjects.GLProgram;
import fc.GLObjects.GLRenderTarget;
import fc.GLObjects.GLSLCompileException;
import fc.GLObjects.GLShaderMatrixParameter;
import fc.GLObjects.GLShaderStorageBuffer;
import fc.Math.Matrix;
import fc.Math.Vec2i;

public class CustomComposite extends Composite
{
	public GeometryViewerCanvas m_Canvas;
	
	public CustomComposite(Composite c, int style)
	{
		
		super(c, style);
		
		
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		this.setLayout(gridLayout);

		Label label = new Label(this, SWT.NONE);
	    label.setText("Test label");
	    
		Button btn = new Button(this, SWT.PUSH);
		btn.setText("Test button");
		btn.addListener(SWT.Selection, new Listener()
		{
			@Override
			public void handleEvent(Event e)
			{
				m_Canvas.setCurrent(); // Make sure the OpenGL context for the GLCanvas is active. This is mandatory!
				
				MarchingCubeComposite.runMarchingCubeShader();
				//DualContouringRenderer.computeShader();
//				System.out.println("PNG file generated in the project directory.");
			}
		});

		
		
		Group gClock = new Group(this, SWT.NONE);
		gClock.setLayout (new FillLayout ());
		gClock.setText("Clock");
		
		Button bRestart = new Button(gClock, SWT.PUSH);
		bRestart.setText("Restart");
		bRestart.addListener(SWT.Selection, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				ClockService clock = (ClockService) SimpleGeometryApplication.singleton.queryService(ClockService.class);
				clock.restart();;
			}
		});
		
		Button bBackward = new Button(gClock, SWT.PUSH);
		bBackward.setText("<");
		bBackward.addListener(SWT.Selection, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				ClockService clock = (ClockService) SimpleGeometryApplication.singleton.queryService(ClockService.class);
				clock.setTimeMultiplier(-1.0);
			}
		});
		
		Button bForward = new Button(gClock, SWT.PUSH);
		bForward.setText("||");
		bForward.addListener(SWT.Selection, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				ClockService clock = (ClockService) SimpleGeometryApplication.singleton.queryService(ClockService.class);
				clock.setTimeMultiplier(0.0);
			}
		});
		
		Button bPlay = new Button(gClock, SWT.PUSH);
		bPlay.setText(">");
		bPlay.addListener(SWT.Selection, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				ClockService clock = (ClockService) SimpleGeometryApplication.singleton.queryService(ClockService.class);
				clock.setTimeMultiplier(1.0);
			}
		});
		
		
		/*Scale scale = new Scale(gClock, SWT.HORIZONTAL); 
		scale.setSize(100,80);
		scale.setMinimum(0);
		scale.setMaximum(20);
		scale.setSelection(10);
		scale.setIncrement(1);*/
	}
	
	//
	// Il est possible de faire du rendu offline (un rendu qui n'est pas dirig� vers l'�cran)
	// de la mani�re suivante.
	// Cet exemple montre aussi comment utiliser des shaders.
	//
	/*private void performOfflineRendering()
	{
		int width = 600;
		int height = 400;
		
		GLRenderTarget rt = new GLRenderTarget(width, height, GL30.GL_RGBA32I, GL30.GL_RGBA_INTEGER, GL11.GL_INT);
		
		GLProgram shader = new GLProgram()
		{
			@Override
			protected void preLinkStep()
			{
				glBindAttribLocation(m_ProgramId, 0, "in_Position");
			}
		};
		shader.init(new MyVertexShader(), new MyFragmentShader());
		
		GLShaderMatrixParameter matParam = new GLShaderMatrixParameter("u_mvpMatrix");
		matParam.init(shader);
		
		GLVec2iTriangle tri = new GLVec2iTriangle(new Vec2i[]{new Vec2i(0,0), new Vec2i(width-1,0), new Vec2i(width/2,width/2)});
		
		// This is typically done once per frame
		GL11.glViewport(0, 0, width, height);
		rt.bind();
        glClearColor(0.0f, 1.0f, 0.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		shader.begin();
		matParam.set(Matrix.createOrtho(0, width, 0, height, -1, 1));
		tri.render();
		shader.end();
		rt.unbind();
		// =====================================
		
		saveImage(rt, "enveloppe_rouge_et_verte.png");
		
		// Freeing resources
		tri.dispose();		
		rt.dispose();
	}*/
	
	private void runComputeShader()
	{
		String str = "#version 430\r\n" + 
				"\r\n" + 
				"layout (local_size_x = 8, local_size_y = 8) in;\r\n" + 
				"layout(std430, binding=0) coherent buffer PixelBuffer { float data[]; } " + " b " + ";\n" +
				"void main() {\r\n" +
				"ivec2 coords = ivec2(gl_GlobalInvocationID);\n" +
				"int i = coords.y * 8 * 10 + coords.x;\n" +
				"b.data[i] = i;" +
				"}";
		
		int computeShaderID = GL20.glCreateShader(GL43.GL_COMPUTE_SHADER);
		GL20.glShaderSource(computeShaderID, str);
		GL20.glCompileShader(computeShaderID);
		int isCompiled = GL20.glGetShaderi(computeShaderID, GL20.GL_COMPILE_STATUS);
		if (isCompiled == GL11.GL_FALSE)
		{
			String infoLog = GL20.glGetShaderInfoLog(computeShaderID);
			throw new GLSLCompileException(str, infoLog);
		}
		
		int programID = GL20.glCreateProgram();
		GL20.glAttachShader(programID, computeShaderID);
		GL20.glLinkProgram(programID);
		GL20.glDeleteShader(computeShaderID);
		
		final int nb = 8 * 8 * 10 * 10; // 8 * 8 local size // 10 * 10 workgroups
		GLShaderStorageBuffer buffer = new GLShaderStorageBuffer(nb * 4); // float = 4 bytes
		
		GL20.glUseProgram(programID);
		buffer.bind(null);
        GL43.glDispatchCompute(10,10,1);
        GL43.glMemoryBarrier(GL43.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT);
        FloatBuffer floatBuffer = GL20.glMapBuffer(GL43.GL_SHADER_STORAGE_BUFFER, GL20.GL_READ_WRITE).asFloatBuffer();
        for (int i = 0; i < nb; ++i) {
        	System.out.println(floatBuffer.get(i));
        }
        buffer.unbind(null);
        GL20.glUseProgram(0);
        
        
        		
        //----
        GL20.glDeleteProgram(programID);
        buffer.dispose();
	}
	
	private void saveImage(GLRenderTarget rt, String imageFileName)
	{
		float[][][] pixels = rt.readBackAsFloat();
		
		BufferedImage img = new BufferedImage(pixels[0].length, pixels.length, BufferedImage.TYPE_INT_RGB);
		
		for (int y=0; y < pixels.length; y++)
		{
			for (int x=0; x < pixels[0].length; x++)
			{
				int r = (int)(pixels[y][x][0] * 255.0f);
				int g = (int)(pixels[y][x][1] * 255.0f);
				int b = (int)(pixels[y][x][2] * 255.0f);

				img.setRGB(x, y, (r<<16) | (g<<8) | b);
			}
		}
		rt.dispose();
		
	    File outputFile = new File(System.getProperty("user.dir"), imageFileName);
	    try
	    {
	    	ImageIO.write(img, "png", outputFile);
	    }
	    catch (IOException e)
	    {
	    	System.out.println("Error, IOException caught: " + e.toString());
	    }
	}
}

