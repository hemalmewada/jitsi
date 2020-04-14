/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Copyright @ 2015 Atlassian Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.java.sip.communicator.launcher;

import java.util.*;
import org.osgi.framework.*;

import java.awt.*;

/**
 * Activates if splash screen is available to draw progress and
 * currently loading bundle name.
 *
 * @author Damian Minkov
 */
public class SplashScreenUpdater
    implements ServiceListener, BundleListener
{
    /**
     * A reference to the bundle context that is currently in use.
     */
    private BundleContext bundleContext = null;

    /**
     * The splash screen if any.
     */
    private volatile SplashScreen splash;

    /**
     * The splash screen graphics.
     */
    private volatile Graphics2D g;

    /**
     * The colors.
     */
    private Color TEXT_BACKGROUND = new Color(203, 202, 202);
    private Color TEXT_FOREGROUND = new Color(82, 82, 82);
    private Color PROGRESS_FOREGROUND = new Color(177, 174, 173);

    public SplashScreenUpdater(BundleContext bundleContext)
    {
        this.bundleContext = bundleContext;
        splash = SplashScreen.getSplashScreen();

        if(splash == null ||
            !splash.isVisible() ||
            (g = splash.createGraphics()) == null)
        {
            return;
        }

        bundleContext.addBundleListener(this);
        bundleContext.addServiceListener(this);
    }

    /**
     * Unsets the listener that we set when we start this bundle.
     */
    public void stop()
    {
        bundleContext.removeServiceListener(this);
        bundleContext.removeBundleListener(this);
        SplashScreen copy = splash;
        g = null;
        splash = null;
        if (copy != null && copy.isVisible())
        {
            copy.close();
        }

        TEXT_BACKGROUND = null; TEXT_FOREGROUND = null;
        PROGRESS_FOREGROUND = null;
    }

    @Override
    public void serviceChanged(ServiceEvent serviceEvent)
    {
        try
        {
            // If the main frame was set visible, the splash screen was/will
            // be closed by Java automatically. Otherwise we need to do that
            // manually.
            Object service =
                bundleContext
                    .getService(serviceEvent.getServiceReference());
            if (service.getClass().getSimpleName().equals("UIServiceImpl"))
            {
                stop();
                return;
            }

            bundleContext.ungetService(serviceEvent.getServiceReference());
        }
        catch(Throwable e)
        {
            stop();
        }
    }

    @Override
    public void bundleChanged(BundleEvent event)
    {
        if (g == null || splash == null)
        {
            return;
        }

        if (event.getType() != BundleEvent.INSTALLED &&
            event.getType() != BundleEvent.STARTED &&
            event.getType() != BundleEvent.RESOLVED)
        {
            return;
        }

        double progress1 = Arrays.stream(bundleContext.getBundles())
            .filter(b -> b.getState() >= Bundle.INSTALLED)
            .count();
        double progress2 = Arrays.stream(bundleContext.getBundles())
            .filter(b -> b.getState() >= Bundle.RESOLVED)
            .count();
        double progress3 = Arrays.stream(bundleContext.getBundles())
            .filter(b -> b.getState() == Bundle.ACTIVE)
            .count();
        double progressMax = bundleContext.getBundles().length * 3;

        int progressWidth = 233;
        int progressHeight = 14;
        int progressX = 168;
        int progressY = 97;

        int textHeight = 20;
        int textBaseX = 150;
        int textBaseY = 145 + (50 - textHeight)/2 + textHeight;

        int currentProgressWidth = 
            (int) (((progress1 + progress2 + progress3) / progressMax)
                * progressWidth);

        g.setComposite(AlphaComposite.Clear);
        g.setPaintMode();
        // first clear the space for text
        g.setColor(TEXT_BACKGROUND);
        g.clearRect(
            textBaseX - 1,// -1 to clear a pix left from some txt
            textBaseY - textHeight,
            (int) this.splash.getBounds().getWidth() - textBaseX,
            textHeight + 5);
        g.fillRect(
            textBaseX - 1,// -1 to clear a pix left from some txt
            textBaseY - textHeight,
            (int) this.splash.getBounds().getWidth() - textBaseX,
            textHeight + 5);

        // then fill the progress
        g.setColor(PROGRESS_FOREGROUND);
        g.fillRect(progressX, progressY,
            currentProgressWidth, progressHeight);
        g.drawRect(progressX, progressY,
            currentProgressWidth, progressHeight);

        g.setRenderingHint(
            RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g.setColor(TEXT_FOREGROUND);

        Object bundleName =
            event.getBundle().getHeaders().get(Constants.BUNDLE_NAME);

        if(bundleName == null)
        {
            bundleName = event.getBundle().getSymbolicName();
        }

        if (bundleName == null)
        {
            bundleName = event.getBundle().getBundleId();
        }

        g.drawString(bundleName.toString(), textBaseX, textBaseY);
        splash.update();
    }
}
