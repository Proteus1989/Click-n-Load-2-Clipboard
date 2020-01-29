package com.suapp.cnl2c.gui;

import com.suapp.cnl2c.cnl.ClickAndLoadAPI;
import java.awt.AWTException;
import java.awt.CheckboxMenuItem;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.GridLayout;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * A suggested simple graphic main implementation
 *
 * @author Antonio
 */
public class ClickAndLoadWindowsGUIMain
{

    public static final String APP_NAME = "Click'n Load 2 Clipboard";

    /**
     * Creates the GUI and initilizes Click'n Load 2 Clipboard service icon
     * view.
     */
    public ClickAndLoadWindowsGUIMain()
    {
        ClickAndLoadAPI clickAndLoadAPI = ClickAndLoadAPI.getInstance();

        try
        {
            // Getting SystemTray
            SystemTray tray = SystemTray.getSystemTray();

            // Creating TrayIcon
            TrayIcon trayIcon = new TrayIcon(Toolkit.getDefaultToolkit().createImage(this.getClass().getClassLoader().getResource("sysicon.png")), "CnL2C");
            trayIcon.setImageAutoSize(true);
            trayIcon.setToolTip(APP_NAME);

            // Creating PopupMenu enable/disable service item
            CheckboxMenuItem enableServiceCheckboxItem = new CheckboxMenuItem("Enable CnL");
            enableServiceCheckboxItem.addItemListener((ItemEvent itemEvent) ->
            {
                clickAndLoadAPI.isRunning();
                if (enableServiceCheckboxItem.getState())
                    if (!clickAndLoadAPI.startService())
                        enableServiceCheckboxItem.setState(false);
                    else
                        trayIcon.displayMessage(APP_NAME, "Service is running", MessageType.INFO);
                else
                {
                    clickAndLoadAPI.stopService();
                    trayIcon.displayMessage(APP_NAME, "Service stopped properly", MessageType.INFO);
                }
            });

            // Creating PopupMenu about program item
            MenuItem aboutItem = new MenuItem("About");
            aboutItem.addActionListener((ActionEvent actionEvent) ->
            {
                String message = "<html>This program has been developed by Antonio Suárez.<br>To get more information about it, please, click below.</html>";
                String url = "<html><a href='#'>Github website</a></html>";
                String version = "v";
                
                Properties properties = new Properties();
                try
                {
                    properties.load(this.getClass().getClassLoader().getResourceAsStream("project.properties"));
                    version += properties.getProperty("version") + "." + properties.getProperty("revision");
                } catch (IOException ex)
                {
                    Logger.getLogger(ClickAndLoadWindowsGUIMain.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                JPanel panel = new JPanel(new GridLayout(2, 1));
                
                panel.add(new JLabel(message));
                panel.add(new JLabel(url)
                {
                    {
                        setCursor(new Cursor(Cursor.HAND_CURSOR));
                        addMouseListener(new MouseAdapter()
                        {
                            @Override
                            public void mouseClicked(MouseEvent e)
                            {
                                try
                                {
                                    Desktop.getDesktop().browse(new URI("https://github.com/Proteus1989/Click-n-Load-2-Clipboard"));
                                } catch (IOException | URISyntaxException ex)
                                {
                                    Logger.getLogger(ClickAndLoadWindowsGUIMain.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        });
                    }
                });

                JOptionPane.showMessageDialog(null, panel, "About " + APP_NAME + " " + version, JOptionPane.INFORMATION_MESSAGE);
            });

            // Creating PopupMenu exit program item
            MenuItem exitItem = new MenuItem("Exit");
            exitItem.addActionListener((ActionEvent actionEvent) ->
            {
                tray.remove(trayIcon);
                System.exit(0);
            });

            // Creating and filling PopupMenu
            PopupMenu popup = new PopupMenu();
            popup.add(enableServiceCheckboxItem);
            popup.add(aboutItem);
            popup.add(exitItem);

            // Adding PopupMenu to TrayIcon
            trayIcon.setPopupMenu(popup);
            // Displaying TrayIcon in Windows Tray Bar
            tray.add(trayIcon);

            // Adding Click'n Load listener. This listener will copy the links into clipboard
            clickAndLoadAPI.addListener((link) ->
            {
                Logger.getLogger(ClickAndLoadAPI.class.getName()).info(link);
                StringSelection stringSelection = new StringSelection(link);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(stringSelection, null);
                trayIcon.displayMessage(APP_NAME, "Link(s) copied to clipboard", MessageType.INFO);
            });

            // Setting custom error notifier
            clickAndLoadAPI.setOnError((e) ->
            {
                trayIcon.displayMessage(APP_NAME, e.getMessage(), MessageType.ERROR);
            });

            // Starting Click'n Load service
            if (clickAndLoadAPI.startService())
            {
                enableServiceCheckboxItem.setState(true);
                trayIcon.displayMessage(APP_NAME, "Service is running", MessageType.INFO);
            }

        } catch (AWTException | UnsupportedOperationException ex)
        {
            JOptionPane.showMessageDialog(null, "CnL2C cannot create a tray icon.\nClosing app.", APP_NAME, JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(ClickAndLoadWindowsGUIMain.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(-1);
        }

    }

    /**
     * Main method. Initializes the app
     *
     * @param args is not used at all.
     * @throws InterruptedException Thrown when a thread is waiting, sleeping,
     * or otherwise occupied, and the thread is interrupted, either before or
     * during the activity.
     */
    public static void main(String[] args) throws InterruptedException
    {
        ClickAndLoadWindowsGUIMain clickAndLoadWindowsGUIMain = new ClickAndLoadWindowsGUIMain();
        synchronized (clickAndLoadWindowsGUIMain)
        {
            clickAndLoadWindowsGUIMain.wait();
        }

    }
}
