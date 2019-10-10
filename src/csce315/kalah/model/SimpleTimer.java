package csce315.kalah.model;

import javafx.animation.*;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.*;
import javafx.util.Duration;

/**
* <h1>SimpleTimer class</h1>
* This class controls a simple countdown timer
* 
*/
public class SimpleTimer {
	
	private ReadOnlyIntegerWrapper timeLeft;
	private ReadOnlyDoubleWrapper timeLeftDouble;
	private Timeline timeline = new Timeline();
	private boolean _isRunning;
	
	
	/**
	 * Default constructor sets some default values
	 */
	public SimpleTimer() {
		
		_isRunning = false;
		
	    timeLeft = new ReadOnlyIntegerWrapper(1);
	    timeLeftDouble = new ReadOnlyDoubleWrapper(1);
	}
	
	
	/**
	 * Configures the timer for a given number of seconds, this must be
	 * called at least once before the timer is started
	 * @param seconds The number of seconds the timer will start at
	 */
	public void setTimer(int seconds) {
		  if (timeline != null) {
		       timeline.stop();
		   }
		  
		timeLeft = new ReadOnlyIntegerWrapper(seconds);
	    timeLeftDouble = new ReadOnlyDoubleWrapper(seconds);
	    
	    //Setup a new timeline with the correct number of seconds
		timeline = new Timeline(
			      new KeyFrame(
			        Duration.ZERO,          
			        new KeyValue(timeLeftDouble, seconds)
			      ),
			      new KeyFrame(
			        Duration.seconds(seconds), 
			        new KeyValue(timeLeftDouble, 0)
			      )
			    );
		
	    timeline.setOnFinished(e -> {
	    	_isRunning = false;
	    });
		
	    //Add a listener to update the timeLeft value
	    timeLeftDouble.addListener(new InvalidationListener() {
		      @Override public void invalidated(Observable o) {
		        timeLeft.set((int) Math.ceil(timeLeftDouble.get()));
		      }
		    });
	}

	
	/**
	 * Begins the countdown of the timer.
	 */
	public void start() {
		_isRunning = true;
		timeline.playFromStart();
	}
	
	
	
	/**
	 * Stops the timer.
	 */
	public void pause() {
		_isRunning = false;
		timeline.pause();
	}
	
	
	/**
	 * Continues the time from where it was last stopped.
	 */
	public void resume() {
		_isRunning = true;
		timeline.play();
	}
	
	
	/**
	 * Returns if the time is running or not.
	 * @return boolean Returns true if the time is actively counting down, false otherwise
	 */
	public boolean isRunning() {
		return _isRunning;
	}
	
	
	/**
	 * Returns the number of seconds left in the countdown. Can be used to bind to a GUI element
	 * @return ReadOnlyIntegerProperty Returns the amount of time left in seconds
	 */
	public ReadOnlyIntegerProperty timeLeftProperty() {
		return timeLeft.getReadOnlyProperty();
	}

}