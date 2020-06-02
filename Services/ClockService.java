package Services;

public class ClockService {
	private long m_StartTime;
	private long m_LastRealTime;
	private long m_CurrentRealTime;
	private long m_CurrentTime;
	private double m_TimeMultiplier;
	
	public ClockService() {
		restart();
		m_TimeMultiplier = 1.0;
		updateCurrentTime();
	}
	
	public void restart() {
		m_StartTime = System.currentTimeMillis();
		m_CurrentRealTime = getSystemTime();
		m_LastRealTime = m_CurrentRealTime;
		m_CurrentTime = 0;
	}
	
	private void updateCurrentTime() {
		long elapsedTime = (long) ((m_CurrentRealTime - m_LastRealTime) * m_TimeMultiplier);
		m_CurrentTime += elapsedTime;
	}

	private long getSystemTime() {
		return System.currentTimeMillis() - m_StartTime;
	}
	
	public void onIdle() {
		m_LastRealTime = m_CurrentRealTime;
		m_CurrentRealTime = getSystemTime();
		updateCurrentTime();
	}
	
	public long getMillis() {
		return m_CurrentTime;
	}
	
	public double getSeconds() {
		return (double)(m_CurrentTime) / 1000.0;
	}
	
	public void setTimeMultiplier(double multiplier) {
		m_TimeMultiplier = multiplier;
	}
	
	public double getTimeMultiplier() {
		return m_TimeMultiplier;
	}
}
