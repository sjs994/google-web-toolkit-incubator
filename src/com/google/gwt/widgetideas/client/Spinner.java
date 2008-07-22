/*
 * Copyright 2008 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.gwt.widgetideas.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ImageBundle;
import com.google.gwt.user.client.ui.MouseListener;
import com.google.gwt.user.client.ui.Widget;

import java.util.ArrayList;
import java.util.List;

/**
 * The {@link Spinner} provide two arrows for in- and decreasing values. A
 * linked {@link SpinnerListener}
 */
public class Spinner {
  /**
   * Default resources for spinning arrows
   */
  public interface SpinnerResources extends ImageBundle {
    @Resource("arrowDown.png")
    AbstractImagePrototype arrowDown();

    @Resource("arrowDownHover.png")
    AbstractImagePrototype arrowDownHover();

    @Resource("arrowDownPressed.png")
    AbstractImagePrototype arrowDownPressed();

    @Resource("arrowUp.png")
    AbstractImagePrototype arrowUp();

    @Resource("arrowUpHover.png")
    AbstractImagePrototype arrowUpHover();

    @Resource("arrowUpPressed.png")
    AbstractImagePrototype arrowUpPressed();
  }

  private static final int INITIAL_SPEED = 7;

  private SpinnerResources images;
  private final Image decrementArrow = new Image();
  private final Image incrementArrow = new Image();

  private List<SpinnerListener> spinnerListeners = new ArrayList<SpinnerListener>();
  private int step, minStep, maxStep, initialSpeed = 7;
  private long value, min, max;
  private boolean increment;
  private boolean constrained;

  private final Timer timer = new Timer() {
    private int counter = 0;
    private int speed = 7;

    public void cancel() {
      super.cancel();
      speed = initialSpeed;
      counter = 0;
    }

    public void run() {
      counter++;
      if (speed <= 0 || counter % speed == 0) {
        speed--;
        counter = 0;
        if (increment) {
          increase();
        } else {
          decrease();
        }
      }
      if (speed < 0 && step < maxStep) {
        step += 1;
      }
    }
  };

  private MouseListener mouseListener = new MouseListener() {
    public void onMouseDown(Widget sender, int x, int y) {
      if (sender == incrementArrow) {
        images.arrowUpPressed().applyTo((Image) sender);
        increment = true;
        increase();
      } else {
        images.arrowDownPressed().applyTo((Image) sender);
        increment = false;
        decrease();
      }
      timer.scheduleRepeating(30);
    }

    public void onMouseEnter(Widget sender) {
      if (sender == incrementArrow) {
        images.arrowUpHover().applyTo((Image) sender);
      } else {
        images.arrowDownHover().applyTo((Image) sender);
      }
    }

    public void onMouseLeave(Widget sender) {
      cancelTimer(sender);
    }

    public void onMouseMove(Widget sender, int x, int y) {
    }

    public void onMouseUp(Widget sender, int x, int y) {
      cancelTimer(sender);
    }
  };

  /**
   * @param spinner the widget listening to the arrows
   * @param value initial value
   */
  public Spinner(SpinnerListener spinner, long value) {
    this(spinner, value, 0, 0, 1, 99, false);
  }

  /**
   * @param spinner the widget listening to the arrows
   * @param value initial value
   * @param min min value
   * @param max max value
   */
  public Spinner(SpinnerListener spinner, long value, long min, long max) {
    this(spinner, value, min, max, 1, 99, true);
  }

  /**
   * @param spinner the widget listening to the arrows
   * @param value initial value
   * @param min min value
   * @param max max value
   * @param minStep min value for stepping
   * @param maxStep max value for stepping
   */
  public Spinner(SpinnerListener spinner, long value, long min, long max,
      int minStep, int maxStep) {
    this(spinner, value, min, max, minStep, maxStep, true);
  }

  /**
   * @param spinner the widget listening to the arrows
   * @param value initial value
   * @param min min value
   * @param max max value
   * @param minStep min value for stepping
   * @param maxStep max value for stepping
   * @param constrained determines if min and max value will take effect
   */
  public Spinner(SpinnerListener spinner, long value, long min, long max,
      int minStep, int maxStep, boolean constrained) {
    this(spinner, value, min, max, minStep, maxStep, constrained,
        (SpinnerResources) GWT.create(SpinnerResources.class));
  }

  /**
   * @param spinner the widget listening to the arrows
   * @param value initial value
   * @param min min value
   * @param max max value
   * @param minStep min value for stepping
   * @param maxStep max value for stepping
   * @param constrained determines if min and max value will take effect
   * @param images the arrows images
   */
  public Spinner(SpinnerListener spinner, long value, long min, long max,
      int minStep, int maxStep, boolean constrained, SpinnerResources images) {
    super();
    spinnerListeners.add(spinner);
    this.images = images;
    this.value = value;
    this.constrained = constrained;
    this.step = minStep;
    this.minStep = minStep;
    this.maxStep = maxStep;
    this.min = min;
    this.max = max;
    this.initialSpeed = INITIAL_SPEED;
    incrementArrow.addMouseListener(mouseListener);
    images.arrowUp().applyTo(incrementArrow);
    decrementArrow.addMouseListener(mouseListener);
    images.arrowDown().applyTo(decrementArrow);
    fireOnValueChanged();
  }

  /**
   * @param listener the listener to add
   */
  public void addSpinnerListener(SpinnerListener listener) {
    spinnerListeners.add(listener);
  }

  /**
   * @return the image representing the decreating arrow
   */
  public Image getDecrementArrow() {
    return decrementArrow;
  }

  /**
   * @return the image representing the increasing arrow
   */
  public Image getIncrementArrow() {
    return incrementArrow;
  }

  /**
   * @return the maximum value
   */
  public long getMax() {
    return max;
  }

  /**
   * @return the maximum spinner step
   */
  public int getMaxStep() {
    return maxStep;
  }

  /**
   * @return the minimum value
   */
  public long getMin() {
    return min;
  }

  /**
   * @return the minimum spinner step
   */
  public int getMinStep() {
    return minStep;
  }

  /**
   * @return the current value
   */
  public long getValue() {
    return value;
  }

  /**
   * @return true is min and max values are active, false if not
   */
  public boolean isConstrained() {
    return constrained;
  }

  /**
   * @param listener the listener to remove
   */
  public void removeSpinnerListener(SpinnerListener listener) {
    spinnerListeners.remove(listener);
  }

  /**
   * @param initialSpeed the initial speed of the spinner. Higher values mean
   *          lower speed, default value is 7
   */
  public void setInitialSpeed(int initialSpeed) {
    this.initialSpeed = initialSpeed;
  }

  /**
   * @param max the maximum value. Will not have any effect if constrained is
   *          set to false
   */
  public void setMax(long max) {
    this.max = max;
  }

  /**
   * @param maxStep the maximum step for this spinner
   */
  public void setMaxStep(int maxStep) {
    this.maxStep = maxStep;
  }

  /**
   * @param min the minimum value. Will not have any effect if constrained is
   *          set to false
   */
  public void setMin(long min) {
    this.min = min;
  }

  /**
   * @param minStep the minimum step for this spinner
   */
  public void setMinStep(int minStep) {
    this.minStep = minStep;
  }

  /**
   * @param value sets the current value of this spinner
   * @param fireEvent fires value changed event if set to true
   */
  public void setValue(long value, boolean fireEvent) {
    this.value = value;
    if (fireEvent) {
      fireOnValueChanged();
    }
  }

  /**
   * Decreases the current value of the spinner by subtracting current step
   */
  protected void decrease() {
    value -= step;
    if (constrained && value < min) {
      value = min;
      timer.cancel();
    }
    fireOnValueChanged();
  }

  /**
   * Increases the current value of the spinner by adding current step
   */
  protected void increase() {
    value += step;
    if (constrained && value > max) {
      value = max;
      timer.cancel();
    }
    fireOnValueChanged();
  }

  private void cancelTimer(Widget sender) {
    step = minStep;
    if (sender == incrementArrow) {
      images.arrowUp().applyTo((Image) sender);
    } else {
      images.arrowDown().applyTo((Image) sender);
    }
    timer.cancel();
  }

  private void fireOnValueChanged() {
    for (SpinnerListener listener : spinnerListeners) {
      listener.onSpinning(value);
    }
  }
}