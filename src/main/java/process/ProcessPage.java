package process;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.io.FileInputStream;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

public class ProcessPage 
{
	static PrintWriter fw = null; /* */
	public static int waitPeriod; 
	
	private static Properties loadInstructions(String name, int pgNr)
	{
		Properties p = new Properties();
		InputStream input = null;

		try 
		{
			input = new FileInputStream(name+"."+pgNr+".txt");
			p.load(new InputStreamReader(input, Charset.forName("UTF-8")));
		}
		catch (IOException ex) { ex.printStackTrace(); return null;}
		return p;
	}
	
	public static void processForm(String SPName, int pgNr, WebDriver driver, JavascriptExecutor executor) throws Exception 
    {
		/*
		if (fw == null) 
		{
			String dt = (""+java.time.LocalDateTime.now()).replaceAll(":", ".");
			fw = new PrintWriter(SPName+"-Report-Log-"+dt+".csv");
			fw.println("Scenario Identifier,"+SPName);
			fw.println("Scenario Description, ");
			LocalDateTime dt = java.time.LocalDateTime.now();
			fw.println("Testing Date and Time,"+dt);
		}
		*/
		Properties p = loadInstructions(SPName, pgNr);
    	if (p == null) throw new Exception("Error reading Instructions file "+pgNr);
    	
		do {
		driver.manage().timeouts().implicitlyWait(waitPeriod, TimeUnit.MILLISECONDS);}
		while (!driver.getCurrentUrl().equals(p.getProperty("page.url")));
		//Thread.sleep(waitPeriod);
    	LogFile.writeMsg("Processing "+driver.getTitle()+" at "+driver.getCurrentUrl());
    	//LogFile.writeMsg(driver.getPageSource());
    	
    	/*
    	if (pgNr==1) fw.println("SP Test Starting Page,URL: "+driver.getCurrentUrl());
		if (pgNr==2) fw.println("Redirection to LEPS eIDAS API,URL: "+driver.getCurrentUrl());
		if (pgNr==3) 
			if (driver.getCurrentUrl().startsWith("https://se-eidas.redsara.es/IdP")) fw.println("Redirection to IdP,URL: "+driver.getCurrentUrl());
			else 
			{
				fw.println("Redirection to IdP,URL: ");
				fw.print("Return to SP,"+driver.getCurrentUrl());
			}
		if (pgNr==4) fw.print("Return to SP,"+driver.getCurrentUrl());
		if (pgNr>4)  fw.print(" "+driver.getCurrentUrl());
		fw.flush();
		*/
    	
    	
    	int inr = Integer.parseInt(p.getProperty("events.nr"));
    	for (int i = 1; i <= inr; i++)
    	{
    		String type = p.getProperty("event"+i+".type");
    		String id = p.getProperty("event"+i+".id");
    		String val = p.getProperty("event"+i+".value");
    		System.out.println(type+" "+id+" "+val);
    		System.out.flush();
    		switch (type)
    		{
    			case "select":
    				selectDropDownQS(id, val, true, driver, executor); break;
    			case "selectByVal":
    				selectDropDownQS(id, val, false, driver, executor); break;
    			case "radio":
    				clickRadio(id, driver, executor); break;
    			case "jsbutton":
    				clickJSButton(id, driver, executor); break;
    			case "button":
    				clickButton(id, driver, executor); break;
    			case "checkbox":
    				clickButton(id, driver, executor); break;
    			case "text":
    				inputText(id, val, driver, executor); break;
    			case "link":
    				clickLink(id, true, driver, executor); break;
    			case "linkByText":
    				clickLink(id, false, driver, executor); break;
    			case "goTo":
    				driver.get(id); return;
    			default:
    	             throw new IllegalArgumentException("Illegal event "+i+" type in page: "+pgNr);
    		}
    	}    	
    }
	
	private static void clickLink(String text, boolean byID, WebDriver driver, JavascriptExecutor executor) throws Exception
	{
		if (byID) driver.findElement(By.id(text)).click();
		else driver.findElement(By.linkText(text)).click();
		Thread.sleep(waitPeriod/10);
	}
	/*
	private static void selectDropDown(String selectID, String selection, boolean byText, WebDriver driver, JavascriptExecutor executor) throws Exception
	{
		boolean classicFailed = true;
		try {selectDropDownW3C(selectID, selection, false, driver, executor);}
		catch (Exception e) {classicFailed = true;}
		if (classicFailed) selectDropDownQS(selectID, selection, false, driver, executor);
	}
	*/
	private static void selectDropDownQS(String selectID, String selection, boolean byText, WebDriver driver, JavascriptExecutor executor) throws Exception
	{
		if (byText) executor.executeScript("var select = document.querySelector(\"#"+selectID+"\"); for(var i = 0; i < select.options.length; i++){ if(select.options[i].text == \""+selection+"\"){ select.options[i].selected = true; } }");
		else executor.executeScript("var select = document.querySelector(\"#"+selectID+"\"); for(var i = 0; i < select.options.length; i++){ if(select.options[i].value == \""+selection+"\"){ select.options[i].selected = true; } }");
		Thread.sleep(waitPeriod/10);
	}
	/*
	private static void selectDropDownW3C(String selectID, String selection, boolean byText, WebDriver driver, JavascriptExecutor executor) throws Exception
	{
		WebElement CC = driver.findElement(By.id(selectID));
		List<WebElement> options = CC.findElements(By.tagName("option"));
        for (WebElement option : options) 
        {
        	String val = option.getText();
        	System.out.println("["+selection+"] ["+val+"] ["+option.getAttribute("value")+"]");
        	System.out.flush();
        	if (!byText) val = option.getAttribute("value");
            if (selection.equals(val))
            {
            	System.out.println("Selected "+val);
            	System.out.flush();
            	option.click();
            	executor.executeScript("arguments[0].click();", option);
//            	executor.executeScript("var evt = document.createEvent('MouseEvents');" + "evt.initMouseEvent('click',true, true, window, 0, 0, 0, 0, 0, false, false, false, false, 0,null);" + "arguments[0].dispatchEvent(evt);",  option);
            	option.sendKeys(Keys.RETURN);
            	option.sendKeys(Keys.SPACE);
            	Thread.sleep(waitPeriod);
            	break;
            }   
        }        
	}
	*/
	private static void clickJSButton(String buttonID, WebDriver driver, JavascriptExecutor executor) throws Exception
	{
		WebElement ele = driver.findElement(By.id(buttonID));
		executor.executeScript("arguments[0].click();", ele);
		Thread.sleep(waitPeriod/10);
	}
	
	private static void clickButton(String buttonID, WebDriver driver, JavascriptExecutor executor) throws Exception
	{
		WebElement ele = driver.findElement(By.id(buttonID));
		ele.click();
		Thread.sleep(waitPeriod/10);
	}

	private static void clickRadio(String radioID, WebDriver driver, JavascriptExecutor executor) throws Exception
	{
		List<WebElement> a = driver.findElements(By.id(radioID));
		a.get(0).click();
		Thread.sleep(waitPeriod/10);
	}
	
	private static void inputText(String textID, String value, WebDriver driver, JavascriptExecutor executor) throws Exception
	{
		WebElement ele = driver.findElement(By.id(textID));
		ele.clear();
		ele.sendKeys(value);
		Thread.sleep(waitPeriod/10);
	}
	
	private static void scrollIntoView(WebElement ele, WebDriver driver, JavascriptExecutor executor) throws Exception
	{
		//driver.switchTo().frame(ele);
		executor.executeScript("arguments[0].scrollIntoView(true);", ele);
		//WebDriverWait wait = new WebDriverWait(driver, 20); //here, wait time is 20 seconds
		//wait.until(ExpectedConditions.visibilityOf(ele));
		Thread.sleep(waitPeriod/10);
	}
}
