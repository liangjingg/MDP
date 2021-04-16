package timertask;

import java.util.TimerTask;
import imagecomponent.ImageComponent;

public class MoveImageTask extends TimerTask {
	private ImageComponent item;
	private String command;
	private int pixel;
	private Object lock;

	public MoveImageTask(ImageComponent i, String s, int p, Object lock) {
		item = i;
		command = s;
		pixel = p;
		this.lock = lock;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		synchronized (lock) {
			lock.notifyAll();
		}
		item.moveTo(pixel, command);
	}

}
