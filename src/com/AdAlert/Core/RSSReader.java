package com.AdAlert.Core;

import com.AdAlert.Core.Models.FeedItem;
import com.AdAlert.GUI.GUI;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class RSSReader extends Thread
{
    // Feeds You Want To Monitor
    String RSSPath[] =
    {
        "http://portland.craigslist.org/search/bpo?sort=date&format=rss",   // Boat Parts
        "http://portland.craigslist.org/search/boa?sort=date&format=rss"    // Boats
    };
    
    static final Image image = Toolkit.getDefaultToolkit().getImage("C:\\Users\\Admin\\Desktop\\Code\\Java\\Bullseye\\Bullseye\\src\\main\\webapp\\Resources\\Images\\favicon.png");

    static TrayIcon trayIcon = new TrayIcon(image, "Craigslist Alert");
    
    Set<FeedItem> FeedList = new HashSet<>();
    
    private static RSSReader instance = null;

    private URL rssURL;

    public static RSSReader getInstance() {
        if (instance == null)
            instance = new RSSReader();
        return instance;
    }

    public void setURL(URL url) {
        rssURL = url;
    }

    public void writeFeed()
    {
        try
        {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(rssURL.openStream());

            NodeList items = doc.getElementsByTagName("item");

            // Loop All Our Live Feed Items
            for (int i = 0; i < items.getLength(); i++)
            {
                Element item = (Element) items.item(i);
                
                // Construct A New Feed Item
                FeedItem hItem = new FeedItem(getValue(item, "title"), getValue(item, "description"), getValue(item, "link"));
                
                // Check If It Is Already In Our List
                if(FeedList.contains(hItem) == false)
                {
                    FeedList.add(hItem);
                    System.out.println("Added New Item To List: " + getValue(item, "title") + " : " + getValue(item, "link"));
                    trayIcon.displayMessage(getValue(item, "title"), getValue(item, "link"), TrayIcon.MessageType.INFO);
                    GUI.setjTextArea1(getValue(item, "title") + " : " + getValue(item, "link"));

                    GUI.setjLabel1("Last Checked: " + System.currentTimeMillis());
                }
            }
        }
        catch (Exception e) {
            System.out.println("An Exception Occured! Details: " + e.getMessage());
        }
    }

    public String getValue(Element parent, String nodeName)
    {
        return parent.getElementsByTagName(nodeName).item(0).getFirstChild().getNodeValue().replaceAll("(&#x0024;)","\\|\\$");
    }
    
    @Override
    public void run()
    {
        // Now Enter Our Infinite Loop
        while(true)
        {
            for (String RSSFeed : RSSPath)
            {
                try
                {
                    RSSReader reader = RSSReader.getInstance();
                    reader.setURL(new URL(RSSFeed));
                    reader.writeFeed();
                    Thread.sleep(10000); // 10 Seconds!
                }
                catch (Exception e)
                {
                    System.out.println("An Exception Occured In Main: " + e.getMessage());
                }
            }
        }
    }

    public RSSReader()
    {        
        // Setup System Tray Icon Stuff
        if (SystemTray.isSupported())
        {
            System.out.println("System Tray Is Supported!");
            SystemTray tray = SystemTray.getSystemTray();

            trayIcon.setImageAutoSize(true);
            trayIcon.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.out.println("In here");
                    trayIcon.displayMessage("Tester!", "Some action performed", TrayIcon.MessageType.INFO);
                }
            });
            
            // Now Lets Try And Append The Tray Icon
            try
            {
                tray.add(trayIcon);
            }
            catch (Exception e)
            {
                System.err.println("TrayIcon could not be added.");
            }
        }
    }
}