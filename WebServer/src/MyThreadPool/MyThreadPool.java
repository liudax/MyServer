package MyThreadPool;

public interface MyThreadPool<Job extends Runnable> {
	
	public void execute(Job job);
	
	public void shutdown();
	
	public void addWorkers(int num);
	
	public void removeWorker(int num);
	
	public int getJobSize();
}
