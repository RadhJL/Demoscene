// Copyright (c) 2016,2017 Fr�d�ric Claux, Universit� de Limoges. Tous droits r�serv�s.

package fc.Application.Geometry.Simple;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.opengl.GLData;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import Services.ClockService;
import fc.Application.Application;

public class SimpleGeometryApplication extends Application
{
	public static void main(String[] args)
	{
		SimpleGeometryApplication app = new SimpleGeometryApplication();
		singleton = app;
		app.init(false);
		app.run();
		app.dispose();
	}

	static public SimpleGeometryApplication singleton = null;
	
	private GeometryViewerComposite m_MainComposite;
	private CustomComposite m_LeftComposite;
	private GeometryDocument m_Document;
	private String m_renderer = "marching";
//	private String m_renderer = "dual";
//	private String m_renderer = "sphere";

	private ClockService m_ClockService;
	
	@Override
	public void init(boolean minimized)
	{
		super.init(minimized);
		
		m_Document = new GeometryDocument();
		m_MainComposite.setDocument(m_Document);
	}
	
	@Override
	public void createMenus()
	{
		super.createMenus();
		new MenuItem(m_FileMenu, SWT.SEPARATOR);
//		createFileOpenMenu();
		createDisplayMethodsItem();
	}
	
	private void createFileOpenMenu()
	{
		MenuItem openMenu = new MenuItem(m_FileMenu, SWT.CASCADE);
		openMenu.setText("&Open...");
		openMenu.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				// TODO
			}
		});
	}

	private void createDisplayMethodsItem()
	{
		MenuItem methodItem = new MenuItem(m_FileMenu, SWT.CASCADE);
		methodItem.setText("&Methode d'affichage");
		Menu menuMethod = new Menu(m_FileMenu);
		methodItem.setMenu(menuMethod);
		MenuItem marchingCubeChoice = new MenuItem(menuMethod, SWT.CASCADE);
		MenuItem dualConturingChoice = new MenuItem(menuMethod, SWT.CASCADE);
		MenuItem sphereTracingChoice = new MenuItem(menuMethod, SWT.CASCADE);
		marchingCubeChoice.setText("&Marching Cube");
		dualConturingChoice.setText("&Dual Conturing");
		sphereTracingChoice.setText("&Sphere Tracing");

		marchingCubeChoice.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				m_renderer = "marching";
				createRightPane();
			}
		});
		dualConturingChoice.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				m_renderer = "dual";
				createRightPane();
			}
		});
		sphereTracingChoice.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				m_renderer = "sphere";
				createRightPane();
			}
		});
	}

	@Override
	public void createLeftPane()
	{
		m_LeftComposite = new CustomComposite(m_Shell, SWT.NONE);
		m_LeftComposite.setLayout(new GridLayout());
		
		GridData gridData = new GridData();
		gridData.grabExcessVerticalSpace = true;
		gridData.verticalAlignment = GridData.FILL;
		gridData.widthHint = 300;
		m_LeftComposite.setLayoutData(gridData);
	}
	
	@Override
	public void createRightPane()
	{
		GLData data = new GLData();
		data.doubleBuffer = true;
		m_MainComposite = new GeometryViewerComposite(m_Shell, SWT.NO_BACKGROUND, m_renderer);
		
		GridData rightGridData = new GridData();
		rightGridData.grabExcessHorizontalSpace = true;
		rightGridData.horizontalAlignment = GridData.FILL;
		rightGridData.grabExcessVerticalSpace = true;
		rightGridData.verticalAlignment = GridData.FILL;
		m_MainComposite.setLayoutData(rightGridData);
		
		m_LeftComposite.m_Canvas = m_MainComposite.m_Canvas;
	}
	
	@Override
	protected void registerServices() {
		super.registerServices();
		m_ClockService = new ClockService();
		m_Site.addService(m_ClockService);
	}
	
	@Override
	public void run() {
		while (!m_Shell.isDisposed())
		{
			if (!m_Display.readAndDispatch())
				//m_Display.sleep();
				onIdle();
		}
	}
	
	private void onIdle() {
		m_ClockService.onIdle();
		m_MainComposite.onIdle();		
	}
}
