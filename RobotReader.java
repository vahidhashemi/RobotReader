/**
 * This class reads the Robot.txt file associated with every website.
 * Usage :
 * 
 * and you can call robotreader using isUrlAllowed()
 */


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.StringTokenizer;

//import dcs.shef.ac.uk.com6504.net.spider.core.Spider;


public class RobotReader 
{
	//Instance variables
	private static final String DISALLOW = "Disallow";
	private static final String ALLOW = "Allow";
	private static final String USER_AGENT = "User-agent";
	private static final String CRAWLER_DELAY = "Crawl-delay";
	private String USER_AGENT_NAME;
	private ArrayList<String> disallowed;
	private ArrayList<String> allowed;
	private boolean wholeSiteDisallowed;
	private String robotTXT="";
	private int crawlerDelayTime;
	
	
	public int getCrawlerDelayTime() {
		return crawlerDelayTime;
	}

	private void setCrawlerDelayTime(int crawlerDelayTime) {
		this.crawlerDelayTime = crawlerDelayTime;
	}

	/**
	 * This method removes the last slash form a URL
	 * @param u
	 * @return
	 */
	private static String removeLastSlash(String u)
	{
		int lastSlashIndex=0;
		if ((lastSlashIndex=u.lastIndexOf('/')) != -1)
		{
			if (lastSlashIndex == u.length()-1)
				return u.substring(0,u.lastIndexOf('/'));
			else
				return u;
		}
		else
			return u;
	}
	
	/**
	 * @return the wholeSiteDisallowed
	 */
	public boolean isWholeSiteDisallowed() {
		return wholeSiteDisallowed;
	}

	/**
	 * @param wholeSiteDisallowed the wholeSiteDisallowed to set
	 */
	public void setWholeSiteDisallowed(boolean wholeSiteDisallowed) {
		this.wholeSiteDisallowed = wholeSiteDisallowed;
	}

	/**
	 * @return the uSER_AGENT_NAME
	 */
	public String getUSER_AGENT_NAME() {
		return USER_AGENT_NAME;
	}

	/**
	 * @param uSER_AGENT_NAME the uSER_AGENT_NAME to set
	 */
	private void setUSER_AGENT_NAME(String uSER_AGENT_NAME) {
		USER_AGENT_NAME = uSER_AGENT_NAME;
	}
/**
 * Returns the Robot.txt file as a String
 * @return
 */
	public String getRobotTXT() {
		return robotTXT;
	}
/**
 * Sets the Robot.txt
 * @param robotTXT
 */
	public void setRobotTXT(String robotTXT) {
		this.robotTXT = robotTXT;
	}


	/**
	 * Parameterised Constructor
	 */
	public RobotReader(String crawlerSignature )
	{
		disallowed = new ArrayList<String>();
		allowed = new ArrayList<String>();
		setUSER_AGENT_NAME(crawlerSignature);
		
	}
	
	/**
	 * If requested webAddress is in the list of disallowed URLs then return false else true
	 * sample : isUralAllowed("/catalogs/");
	 * 
	 * @param webAddress
	 * @return true/false
	 */
	public boolean isUrlAllowed(String webAddress)
	{
		//TODO : think about sorting later on
		Collections.sort(disallowed);
		Collections.sort(allowed);
		
		//there are some circumstances that a particular web address is allowed but not any other address within that address
		//for this matter we need to look at allowed list and check if that particular webaddress is allowed
		for(String address : allowed)
		{
			if (webAddress.startsWith(address))
				return true;
		}
		//then we will look for "/" if there was a "/" means whole website is disallowed
		if(isWholeSiteDisallowed())
			return false;
		//looking for particular disallows 
		for(String address : disallowed)
		{
			if (webAddress.startsWith(removeLastSlash(address)))
				return false;
		}
		return true;
		
	}
	
	
	/**
	 * To read the Robot.txt file
	 * @param url
	 */
	public void readRobot(URL url)
	{
		String strHost = url.getHost();
        // form URL of the robots.txt file
        String strRobot = "http://" + strHost + "/robots.txt";
        String strCommands;
        URL urlRobot = null;
        
        try 
        {
              urlRobot = new URL(strRobot);
        }
        catch (MalformedURLException e) 
        {
             // something weird is happening, so don't trust it
        	//Utils.log( e);
        	strCommands = "";
        }
        
        try 
        {
            InputStream urlRobotStream = urlRobot.openStream();
            //my code start bb
            
            BufferedReader br = new BufferedReader(new InputStreamReader(urlRobot.openStream()));
            String s=br.readLine();
            while( s!=null)
            {
            	setRobotTXT(getRobotTXT()+s+"\r\n");
            	s=br.readLine();
            }
            parse();
        }
        catch (Exception e) {
        	//Utils.log( e);
			
		}
	}
	
	/**
	 * Parse the Robot.txt file
	 */
	private void parse()
	{
		String[] lines = getRobotTXT().split("\r\n");
			for(int i=0;i<lines.length;i++)
			{
				//searching for user-agent
				//if suitable results found, then go further
				//else skip the lines to other user-agent line
	
				//searching
				if (lines[i].toLowerCase().startsWith(USER_AGENT.toLowerCase()))
				{
					//if find something then check for * or spider name
					if ( lines[i].toLowerCase().contains("*") || lines[i].toLowerCase().contains(getUSER_AGENT_NAME()))
					{
						
						for(int j=i+1;j<lines.length;j++)
						{
							//if line starts with disallowed or allow we need
							//to take of either of them.Either store them into disallowed list
							//or allowed list
	
							//comment line then skip
							if (lines[j].contains("#")) continue;
							
							//since we already know a command has a format : allow : path
							//we can use following lines
							StringTokenizer st ;
							String command  ;
							String value;
							try
							{
								 st = new StringTokenizer(lines[j], ":");
								 command = st.nextToken().toLowerCase();
								 value = st.nextToken().trim();
							}
							catch (Exception e) 
							{
								continue;
							}
							
							try
							{
								if (command.equals(CRAWLER_DELAY.toLowerCase()))
								{
									int delay = (int)(Float.parseFloat(value) * 1000);
									//We can accept spider object as an argument to the constructor of this class and call
									//respective sleep method from spider like following 
									//spider.setCrawlerSleepTime( (int)(Float.parseFloat(value) * 1000));
									//OR we can have a getter/setter to get/set sleep time 
									setCrawlerDelayTime(delay);
								}
							}
							catch (Exception e) 
							{
								//Utils.log(e);
							}
							
							//there are some circumstances that a webmaster wants one of his website's virtual dir totally be allowed example : [Allow : /dir/*]
							if (value.contains("/*"))
								value = value.substring(0,value.length()-2);
							
							if (command.equalsIgnoreCase(ALLOW.toLowerCase()))
							{
	
								allowed.add(value);
							}
							if(command.startsWith(DISALLOW.toLowerCase()))
							{
								//there is a special case which webmaster doesn't want a robot to access any kind of data in their website
								if (value.trim().equals("/"))
									setWholeSiteDisallowed(true);
								disallowed.add(value);
							}
							
							
							//if reaching to other user-agent exit the current one
							//starts to check for new user-agent outside nested loop!
							if(lines[j].toLowerCase().startsWith(USER_AGENT.toLowerCase()))
								break;
						}//end for j
					}//end if
				}//end user-agent
			}//end for i
		
		

	}//end parse

	
	/**
	 * Main- for testing
	 * @param arg
	 */
	public static void main(String[] arg)
	{
		RobotReader r = new RobotReader("test");
		try
		{
			r.readRobot(new URL("http://localhost/oc2"));
			//r.parse();
			//System.out.println(r.disallowed);
			//System.out.println(r.isUrlAllowed("/catalogs"));
			
			
		}
		catch (Exception e) {
			//Utils.log( e);
		}
		
	}
}