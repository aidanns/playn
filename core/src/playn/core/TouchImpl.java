/**
 * Copyright 2012 The PlayN Authors
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
package playn.core;

import java.util.HashMap;
import java.util.Map;

import pythagoras.f.Point;

/**
 * Handles the common logic for all platform {@link Touch} implementations.
 */
public class TouchImpl implements Touch {

  private static final int MAX_ACTIVE_LAYERS = 32;

  private boolean enabled = true;
  private Listener listener;
  private Map<Integer,AbstractLayer> activeLayers = new HashMap<Integer,AbstractLayer>();

  @Override
  public boolean hasTouch() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  @Override
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  @Override
  public void setListener(Listener listener) {
    this.listener = listener;
  }

  public void onTouchStart(Event.Impl[] touches) {
    if (!enabled)
      return;

    if (listener != null)
      listener.onTouchStart(touches);

    GroupLayer root = PlayN.graphics().rootLayer();
    if (root.interactive()) {
      for (Event.Impl event : touches) {
        Point p = new Point(event.x(), event.y());
        root.transform().inverseTransform(p, p);
        p.x += root.originX();
        p.y += root.originY();
        AbstractLayer hitLayer = (AbstractLayer)root.hitTest(p);
        if (hitLayer != null) {
          activeLayers.put(event.id(), hitLayer);
          final Event.Impl localEvent = event.localize(hitLayer);
          localEvent.setPreventDefault(event.getPreventDefault());
          hitLayer.interact(LayerListener.class, new AbstractLayer.Interaction<LayerListener>() {
            public void interact(LayerListener l) {
              l.onTouchStart(localEvent);
            }
          });
          event.setPreventDefault(localEvent.getPreventDefault());
        }
      }
    }
  }

  public void onTouchMove(Event.Impl[] touches) {
    if (!enabled)
      return;

    if (listener != null)
      listener.onTouchMove(touches);

    for (Event.Impl event : touches) {
      AbstractLayer activeLayer = activeLayers.get(event.id());
      if (activeLayer != null) {
        final Event.Impl localEvent = event.localize(activeLayer);
        localEvent.setPreventDefault(event.getPreventDefault());
        activeLayer.interact(LayerListener.class, new AbstractLayer.Interaction<LayerListener>() {
          public void interact(LayerListener l) {
            l.onTouchMove(localEvent);
          }
        });
        event.setPreventDefault(localEvent.getPreventDefault());
      }
    }
  }

  public void onTouchEnd(Event.Impl[] touches) {
    if (!enabled)
      return;

    if (listener != null)
      listener.onTouchEnd(touches);

    for (Event.Impl event : touches) {
      AbstractLayer activeLayer = activeLayers.get(event.id());
      if (activeLayer != null) {
        final Event.Impl localEvent = event.localize(activeLayer);
        localEvent.setPreventDefault(event.getPreventDefault());
        activeLayer.interact(LayerListener.class, new AbstractLayer.Interaction<LayerListener>() {
          public void interact(LayerListener l) {
            l.onTouchEnd(localEvent);
          }
        });
        event.setPreventDefault(localEvent.getPreventDefault());
        activeLayers.put(event.id(), null);
      }
    }
  }
}
