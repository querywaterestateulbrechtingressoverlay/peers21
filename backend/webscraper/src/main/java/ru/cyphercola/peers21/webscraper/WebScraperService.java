package ru.cyphercola.peers21.webscraper;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.cyphercola.peers21.webscraper.exception.ExternalServerErrorException;

import java.time.Duration;
import java.util.Scanner;

@Service
public class WebScraperService {
  private final Logger logger = LoggerFactory.getLogger(WebScraperService.class);
  private final WebDriver driver;

  private String websiteUrl;
  private String authPageUrl;
  private String apiUsername;
  private String apiPassword;

  @Autowired
  public WebScraperService(ExternalApiRequestServiceProperties properties) {
    driver = new FirefoxDriver(new FirefoxOptions().addArguments("--headless"));
    driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
  }

  private void authorize() {
    WebElement form = driver.findElement(By.cssSelector("form"));
    WebElement username = driver.findElement(By.name("username"));
    WebElement password = driver.findElement(By.name("password"));
    WebElement submitButton = driver.findElement(By.cssSelector("button[type=submit]"));
    username.sendKeys(apiUsername);
    password.sendKeys(apiPassword);
    submitButton.click();
  }

  public int parseTribePoints(String login) throws ExternalServerErrorException {
    driver.navigate().to(websiteUrl + "/profile/" + login);
    if (driver.getCurrentUrl().contains(authPageUrl)) {
      logger.info("authorization required, logging in...");
      authorize();
    }
    if (driver.getCurrentUrl().equals(websiteUrl + "/profile/" + login)) {
      try {
        WebElement coalitionPower = driver.findElement(By.cssSelector("p[data-testid='coalition.power']"));
        return new Scanner(coalitionPower.getText()).nextInt();
      } catch (NoSuchElementException e) {
        throw new ExternalServerErrorException("couldn't find an element with the specified id");
      }
    }
    throw new ExternalServerErrorException("error occured while parsing peer " + login + ", got redirected to " + driver.getCurrentUrl());
  }
}
