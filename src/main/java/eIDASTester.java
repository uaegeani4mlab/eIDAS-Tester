import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
//import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.GeckoDriverService;
import org.openqa.selenium.firefox.internal.ProfilesIni;
//import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import process.LogFile;

public class eIDASTester 
{
	private static WebDriver driver;
	private static JavascriptExecutor executor;

	private static Properties loadInstructions(String name)
	{
		Properties p = new Properties();
		InputStream input = null;

		try 
		{
			input = new FileInputStream(name+".txt");
			p.load(new InputStreamReader(input, Charset.forName("UTF-8")));
		}
		catch (IOException ex) { ex.printStackTrace(); return null;}
		return p;
	}
	
	static void setupFirefox(String geckoPath, String ffBinPath, String firefoxProfileName, boolean headless) 
    {
		//"C:\Program Files\Mozilla Firefox\firefox.exe" -P to manually create profiles

		File pathBinary = new File(ffBinPath);
		if (!pathBinary.exists()) System.exit(0);
		FirefoxBinary firefoxBinary = new FirefoxBinary(pathBinary);
		//firefoxBinary.addCommandLineOptions("--headless");
		//firefoxBinary.addCommandLineOptions("--attach-console");
		//firefoxBinary.addCommandLineOptions("--marionette");

		//File gecko = new File("C:/Users/adanar/marsworkspace/eIDAS-1.4/eIDAS-Tester/target/geckodriver.exe");
		File gecko = new File(geckoPath);
		if (!gecko.exists()) System.exit(0);
		System.setProperty("webdriver.gecko.driver", gecko.getAbsolutePath());
		System.setProperty("java.net.preferIPv4Stack", "true");
		//System.setProperty("webdriver.firefox.driver", "C:/Users/adanar/marsworkspace/eIDAS-1.4/eIDAS-Tester/target/geckodriver.zip");
		//System.setProperty("webdriver.firefox.marionette", gecko.getAbsolutePath());
		//System.setProperty(FirefoxDriver.SystemProperty.DRIVER_USE_MARIONETTE, "true");
		//System.setProperty(FirefoxDriver.SystemProperty.BROWSER_BINARY, pathBinary.getAbsolutePath());

		File logFile = new File("marionette.txt");
		try {logFile.delete(); logFile.createNewFile();} catch (Exception e){e.printStackTrace();}
		System.setProperty("webdriver.firefox.logfile", logFile.getAbsolutePath());

        FirefoxOptions firefoxOptions = new FirefoxOptions();
        if (headless) firefoxOptions.setHeadless(true);
        else firefoxOptions.setHeadless(false);
        firefoxOptions.setBinary(firefoxBinary);
        //firefoxOptions.setCapability("marionette", true);
        firefoxOptions.setCapability("acceptInsecureCerts", true);
        //firefoxOptions.setLegacy(true);

        //System.setProperty(FirefoxDriver.SystemProperty.BROWSER_PROFILE, "spanish");
        //System.setProperty("security.default_personal_cert", "Select Automatically");
        ProfilesIni profile = new ProfilesIni();
        FirefoxProfile ffProfile = profile.getProfile(firefoxProfileName);
        ffProfile.setPreference("javascript.enabled", true);
		ffProfile.setPreference("security.default_personal_cert", "Select Automatically");
        firefoxOptions.setProfile(ffProfile);
 
        driver = new FirefoxDriver(firefoxOptions);
    	executor = (JavascriptExecutor)driver;

    	driver.manage().timeouts().implicitlyWait(90,TimeUnit.SECONDS); 
    	driver.manage().timeouts().pageLoadTimeout(50,TimeUnit.SECONDS);
    	driver.manage().window().maximize(); 
    	driver.manage().deleteAllCookies(); 
    }
	/*
	static void setupChrome() 
    {
		File gecko = new File("C:/Users/adanar/marsworkspace/eIDAS-1.4/eIDAS-Tester/target/chromedriver.exe");    
		if (!gecko.exists()) System.exit(0);
		System.setProperty("webdriver.chrome.driver", gecko.getAbsolutePath());
		
		//"C:\Program Files (x86)\Google\Chrome\Application\chrome.exe"
		
		DesiredCapabilities capabilities = DesiredCapabilities.htmlUnit();
    	capabilities.setJavascriptEnabled(true);
    	capabilities.setPlatform(org.openqa.selenium.Platform.WIN10);
    	
    	driver = new ChromeDriver(capabilities);
    	executor = (JavascriptExecutor)driver;
    }
    */
    static void setupHtmlUnit() 
    {
    	DesiredCapabilities capabilities = DesiredCapabilities.htmlUnit();
    	capabilities.setJavascriptEnabled(true);
    	//driver = new HtmlUnitDriver(capabilities);
    	executor = (JavascriptExecutor)driver;
    	driver.manage().deleteAllCookies();
    }
    
    static void tearDown() 
    {
    	driver.quit();
    }

    static void writeError(String msg, Exception e)
    {
    	driver.quit();
    	process.LogFile.writeError(msg, e);
    	process.LogFile.closeLog();
    	tearDown();
		System.exit(0);
    }
    
	public static void main(String[] args) 
	{
		if (args.length != 1)
		{
			System.err.println("Usage: TestConfFileName");
			System.exit(0);
		}
		Properties p = loadInstructions("tester-config");
    	if (p == null) LogFile.writeError("Error reading Tester Configuration file", null);
    	String geckoPath = p.getProperty("gecko.path");
    	String ffPath = p.getProperty("firefox.path");
    	
		String SPName = args[0];
		p = loadInstructions(SPName);
    	if (p == null) LogFile.writeError("Error reading Test Configuration file", null);
    	String startURL = p.getProperty("start.url");
    	String firefoxProfileName = p.getProperty("firefox.profile");
    	if (firefoxProfileName == null || firefoxProfileName.trim().isEmpty()) firefoxProfileName = "default";
    	String headless = p.getProperty("firefox.headless");
    	if (headless == null || headless.trim().isEmpty()) headless = "true";
    	String wp = p.getProperty("waitPeriod.ms");
    	if (wp == null || wp.trim().isEmpty()) wp = "20000";
    	process.ProcessPage.waitPeriod = Integer.parseInt(wp);
    	
		try{
			LogFile.createLog(SPName);
		}catch (Exception e){e.printStackTrace();return;}
		
		//setupHtmlUnit();
		setupFirefox(geckoPath, ffPath, firefoxProfileName, Boolean.parseBoolean(headless));
		//setupChrome();
		
		driver.get(startURL);
		
		int pgCount = 1;
		while (true)
		{
			try{
				process.ProcessPage.processForm(SPName, pgCount, driver, executor);
			}catch (Exception e){writeError("Error at page "+pgCount, e);}
			pgCount++;
			if (!(new File(SPName+"."+pgCount+".txt").exists())) break;
		}		
		//driver.manage().timeouts().implicitlyWait(process.ProcessPage.waitPeriod, TimeUnit.MILLISECONDS);
		//try {Thread.sleep(process.ProcessPage.waitPeriod);} catch (Exception e){}
		LogFile.writeMsg(driver.getPageSource());
		
		File scrFile = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
		try {
			String dt = (""+java.time.LocalDateTime.now()).replaceAll(":", ".");
			FileUtils.copyFile(scrFile, new File(SPName+"-screenshot.png"));
		} catch (IOException e) {
			writeError("Error saving last page screenshot "+pgCount, e);
		}
		tearDown();	
	}

}
