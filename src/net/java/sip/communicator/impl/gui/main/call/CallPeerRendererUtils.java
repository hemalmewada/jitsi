/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.gui.main.call;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import net.java.sip.communicator.impl.gui.*;
import net.java.sip.communicator.impl.gui.utils.*;
import net.java.sip.communicator.util.swing.*;

/**
 * An utility class that reassembles common methods used by different
 * <tt>CallPeerRenderer</tt>s.
 *
 * @author Lubomir Marinov
 * @author Yana Stamcheva
 * @author Adam Netocny
 */
public class CallPeerRendererUtils
{
    /**
     * Sets the given <tt>background</tt> color to the given <tt>component</tt>.
     *
     * @param component the component to which we set the background
     * @param background the background color to set
     */
    public static void setBackground(Component component, Color background)
    {
        component.setBackground(background);
        if (component instanceof Container)
        {
            Component[] components = ((Container) component).getComponents();
            for (Component c : components)
                setBackground(c, background);
        }
    }

    /**
     * Adds the given <tt>KeyListener l</tt> to the given <tt>component</tt>.
     *
     * @param component the component to which we add <tt>l</tt>
     * @param l the <tt>KeyListener</tt> to add
     */
    public static void addKeyListener(Component component, KeyListener l)
    {
        component.addKeyListener(l);
        if (component instanceof Container)
        {
            Component[] components = ((Container) component).getComponents();
            for (Component c : components)
                addKeyListener(c, l);
        }
    }

    /**
     * Creates a buttons bar from the given list of button components.
     *
     * @param fullScreen indicates if the created button bar would be shown in
     * full screen mode
     * @param buttons the list of buttons to add in the created button bar
     * @return the created button bar
     */
    public static JComponent createButtonBar(boolean fullScreen,
                                            Component[] buttons)
    {
        JComponent buttonBar = fullScreen
                                ? new CallToolBarPanel(true, false)
                                : new CallToolBarPanel(false, false);

        if (buttons != null)
        {
            for (Component button : buttons)
            {
                if (button != null)
                    ((Container) buttonBar).add(button);
            }
        }

        return buttonBar;
    }

    /**
     * Creates a buttons bar from the given list of button components.
     *
     * @param fullScreen indicates if the created button bar would be shown in
     * full screen mode
     * @param buttons the list of buttons to add in the created button bar
     * @return the created button bar
     */
    public static JComponent createIncomingCallButtonBar()
    {
        return new CallToolBarPanel(false, true);
    }

    /**
     * Gets the first <tt>Frame</tt> in the ancestor <tt>Component</tt>
     * hierarchy of a specific <tt>Component</tt>.
     * <p>
     * The located <tt>Frame</tt> (if any) is often used as the owner of
     * <tt>Dialog</tt>s opened by the specified <tt>Component</tt> in
     * order to provide natural <tt>Frame</tt> ownership.
     *
     * @param component the <tt>Component</tt> which is to have its
     * <tt>Component</tt> hierarchy examined for <tt>Frame</tt>
     * @return the first <tt>Frame</tt> in the ancestor
     * <tt>Component</tt> hierarchy of the specified <tt>Component</tt>;
     * <tt>null</tt>, if no such <tt>Frame</tt> was located
     */
    public static Frame getFrame(Component component)
    {
        while (component != null)
        {
            Container container = component.getParent();

            if (container instanceof Frame)
                return (Frame) container;

            component = container;
        }
        return null;
    }

    /**
     * The tool bar container shown in the call window.
     */
    private static class CallToolBarPanel
        extends OrderedTransparentPanel
    {
        final Color toolbarColor
            = new Color(GuiActivator.getResources().getColor(
                "service.gui.CALL_TOOL_BAR"));

        final Color toolbarFullScreenColor
            = new Color(GuiActivator.getResources().getColor(
                "service.gui.CALL_TOOL_BAR_FULL_SCREEN"));

        final Color toolbarInCallBorderColor
            = new Color(GuiActivator.getResources().getColor(
                "service.gui.IN_CALL_TOOL_BAR_BORDER"));

        final Color toolbarInCallShadowColor
            = new Color(GuiActivator.getResources().getColor(
                "service.gui.IN_CALL_TOOL_BAR_BORDER_SHADOW"));

        final Image buttonSeparatorImage
            = ImageLoader.getImage(ImageLoader.CALL_TOOLBAR_SEPARATOR);

        final Image buttonDarkSeparatorImage
            = ImageLoader.getImage(ImageLoader.CALL_TOOLBAR_DARK_SEPARATOR);

        private final boolean isFullScreen;

        private final boolean isIncomingCall;

        private final int TOOL_BAR_BORDER = 2;

        private final int TOOL_BAR_X_GAP = 3;

        private Image separatorImage;

        public CallToolBarPanel(boolean isFullScreen,
                                boolean isIncomingCall)
        {
            this.isFullScreen = isFullScreen;
            this.isIncomingCall = isIncomingCall;

            setLayout(new FlowLayout(FlowLayout.CENTER, TOOL_BAR_X_GAP, 0));
            setBorder(BorderFactory.createEmptyBorder(
                TOOL_BAR_BORDER,
                TOOL_BAR_BORDER, 
                TOOL_BAR_BORDER,
                TOOL_BAR_BORDER));
        }

        public void paintComponent(Graphics g)
        {
            super.paintComponent(g);

            g = g.create();

            AntialiasingManager.activateAntialiasing(g);

            try
            {
                if (isIncomingCall)
                {
                    g.setColor(toolbarInCallShadowColor);
                    g.drawRoundRect(
                        0, 0, getWidth() - 1, getHeight() - 2, 10, 10);

                    g.setColor(toolbarInCallBorderColor);
                    g.drawRoundRect(
                        0, 0, getWidth() - 1, getHeight() - 3, 10, 10);
                }
                else
                {
                    if (isFullScreen)
                        g.setColor(toolbarFullScreenColor);
                    else
                        g.setColor(toolbarColor);

                    g.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                }

                if (!isFullScreen)
                {
                    // We add the border.
                    int x = CallToolBarButton.DEFAULT_WIDTH
                            + TOOL_BAR_BORDER + TOOL_BAR_X_GAP;

                    while (x < getWidth() - TOOL_BAR_BORDER - TOOL_BAR_X_GAP)
                    {
                        separatorImage = (isIncomingCall)
                                ? buttonDarkSeparatorImage
                                : buttonSeparatorImage;

                        g.drawImage(separatorImage,
                            x + 1,
                            (getHeight()
                                - separatorImage.getHeight(this))/2,
                            this);

                        x += CallToolBarButton.DEFAULT_WIDTH + TOOL_BAR_X_GAP;
                    }
                }
            }
            finally
            {
                g.dispose();
            }
        }
    }
}
