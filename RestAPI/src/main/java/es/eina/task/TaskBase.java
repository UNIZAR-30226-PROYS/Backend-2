package es.eina.task;


import java.util.Timer;
import java.util.TimerTask;

public abstract class TaskBase extends TimerTask{
	private static Timer timer = new Timer("TASK_TIMER");

	public TaskBase(long period, boolean launchFirst){
		this(0, period);
		if(launchFirst){
			run();
		}
	}

	public TaskBase(long delay, long period){
		timer.schedule(this, delay, period);
	}

	/**
	 * Stops this Task
	 */
	public static void cleanUp(){
		timer.cancel();
	}


}
