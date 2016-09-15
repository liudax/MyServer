package MyThreadPool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class DefaultThreadPool<Job extends Runnable> implements MyThreadPool<Job> {
	private static final int MAX_WORKER_NUMBERS = 10;
	private static final int DEFAULT_WORKER_NUMBERS = 5;
	private static final int MIN_WORKER_NUMBERS = 1;
	
	private final LinkedList<Job> jobs = new LinkedList<Job>();
	
	private final List<Worker> workers =Collections.synchronizedList(new ArrayList<Worker>());
	
	private int workerNum = DEFAULT_WORKER_NUMBERS;
	
	private AtomicLong threadNum = new AtomicLong();
	
	public DefaultThreadPool( ){
		this(DEFAULT_WORKER_NUMBERS);
	}
	public DefaultThreadPool(int num){
		workerNum = num >MAX_WORKER_NUMBERS ? MAX_WORKER_NUMBERS : 
			num<MIN_WORKER_NUMBERS?MIN_WORKER_NUMBERS:DEFAULT_WORKER_NUMBERS;
		initializeWorkers(workerNum);
	}
	
	private void initializeWorkers(int num){
		for (int i = 0; i < num; i++) {
			Worker worker = new Worker();
			workers.add(worker);
			Thread t = new Thread(worker,"worker-"+threadNum.incrementAndGet());
			t.start();
		}
	}
	@Override
	public void execute(Job job) {
		if(job!=null){
			synchronized (jobs) {
				if(job!=null){
					jobs.addLast(job);
					jobs.notify();
				}
			}
		}
	}
	
	public void executeHead(Job job) {
		if(job!=null){
			synchronized (jobs) {
				if(job!=null){
					jobs.addFirst(job);
					jobs.notify();
				}
			}
		}
	}

	@Override
	public void shutdown() {
		for (Worker worker : workers) {
			worker.shutdown();
		}
	}

	@Override
	public void addWorkers(int num) {
		if(num+this.workerNum>MAX_WORKER_NUMBERS)
			num = MAX_WORKER_NUMBERS - this.workerNum;
		initializeWorkers(num);
		this.workerNum +=num;
	}

	@Override
	public void removeWorker(int num) {
		synchronized (jobs) {
			if(num>this.workerNum)
				throw new IllegalArgumentException("num is bigger than this size");
			int count = 0;
			while(count<num){
				Worker worker = workers.get(count);
				if(workers.remove(worker)){
					worker.shutdown();
					count++;
				}
			}
			this.workerNum -= count;
		}
	}

	@Override
	public int getJobSize() {
		return jobs.size();
	}
	
	class Worker implements Runnable{
		private volatile boolean running =true;
		@Override
		public void run() {
			while(running){
				Job job =null;
				synchronized (jobs) {
						if(getJobSize()==0){
							try{
								jobs.wait();
							}catch(Exception e ){
								Thread.currentThread().interrupt();
								return;
							}
						}
						System.out.println("jobsµÄ³¤¶È"+jobs.size());
						job=jobs.removeFirst();									
				}
				if(job!=null){
					try{
						job.run();
					}catch(Exception e){
					}
				}
			}
		}
		public void shutdown(){
			running = false;
		}
		
	}

}
