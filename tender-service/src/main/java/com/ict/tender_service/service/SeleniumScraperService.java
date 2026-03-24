package com.ict.tender_service.service;

import com.ict.tender_service.model.Tender;
import com.ict.tender_service.repository.TenderRepository;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;

@Service
public class SeleniumScraperService {

    private static final Logger log =
            Logger.getLogger(SeleniumScraperService.class.getName());

    @Autowired
    private TenderRepository tenderRepository;

    // Runs every day at 7 AM
    @Scheduled(cron = "0 0 7 * * ?")
    public void scrapeAllPortals() {
        log.info("Starting Selenium scraping...");
        scrapeEprocure();
        scrapeEtenderBihar();
        log.info("Selenium scraping completed!");
    }

    private ChromeDriver createDriver() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
        return new ChromeDriver(options);
    }

    // Scrape eprocure.gov.in
    private void scrapeEprocure() {
        ChromeDriver driver = null;
        try {
            log.info("Scraping eprocure.gov.in...");
            driver = createDriver();
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

            driver.get("https://eproc2.bihar.gov.in/EPSV2Web/openarea/tenderListingPage.action");
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.tagName("table")));

            List<WebElement> rows = driver.findElements(
                    By.cssSelector("table tbody tr"));

            for (WebElement row : rows) {
                try {
                    List<WebElement> cols = row.findElements(By.tagName("td"));
                    if (cols.size() >= 4) {
                        String title = cols.get(1).getText();
                        String org = cols.get(2).getText();
                        String deadline = cols.get(3).getText();

                        if (isBiharRelated(title, org)) {
                            saveTender(title, org, deadline,
                                    "eprocure.gov.in",
                                    "https://eprocure.gov.in",
                                    "Government Tender");
                        }
                    }
                } catch (Exception e) {
                    log.warning("Error processing row: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            log.warning("Error scraping eprocure: " + e.getMessage());
        } finally {
            if (driver != null) driver.quit();
        }
    }

    // Scrape etender.bihar.gov.in
    private void scrapeEtenderBihar() {
        ChromeDriver driver = null;
        try {
            log.info("Scraping eproc2.bihar.gov.in...");
            driver = createDriver();
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

            driver.get("https://eproc2.bihar.gov.in/EPSV2Web/openarea/tenderListingPage.action");

            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.tagName("table")));
            Thread.sleep(3000);

            // Print all table headers first to understand structure
            List<WebElement> headers = driver.findElements(
                    By.cssSelector("table thead th"));
            log.info("Headers count: " + headers.size());
            for (int i = 0; i < headers.size(); i++) {
                log.info("Header " + i + ": " + headers.get(i).getText());
            }

            List<WebElement> rows = driver.findElements(
                    By.cssSelector("table tbody tr"));
            log.info("Found " + rows.size() + " rows");

            for (WebElement row : rows) {
                try {
                    List<WebElement> cols = row.findElements(By.tagName("td"));
                    if (cols.size() >= 6) {
                        String tenderId = cols.get(1).getText().trim();
                        String title = cols.get(2).getText().trim();
                        String refNo = cols.get(3).getText().trim();
                        String dept = cols.get(4).getText().trim();
                        String endDate = cols.get(5).getText().trim();

                        if (!title.isEmpty() && !tenderId.isEmpty()) {
                            saveTender(title, dept, endDate,
                                    "eproc2.bihar.gov.in",
                                    "https://eproc2.bihar.gov.in/EPSV2Web/openarea/tenderListingPage.action",
                                    "Bihar Government Tender");
                        }
                    }
                } catch (Exception e) {
                    log.warning("Error processing row: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            log.warning("Error scraping Bihar tender: " + e.getMessage());
        } finally {
            if (driver != null) driver.quit();
        }
    }
    private boolean isBiharRelated(String title, String org) {
        String combined = (title + " " + org).toLowerCase();
        return combined.contains("bihar") ||
                combined.contains("darbhanga") ||
                combined.contains("benipur") ||
                combined.contains("buidco") ||
                combined.contains("road") ||
                combined.contains("nala") ||
                combined.contains("drain") ||
                combined.contains("civil");
    }

    private void saveTender(String title, String org,
                            String deadline, String source,
                            String sourceUrl, String category) {
        try {
            if (title == null || title.isEmpty()) return;

            // Check duplicate by title AND source
            boolean exists = tenderRepository
                    .findAll()
                    .stream()
                    .anyMatch(t -> t.getTitle().equals(title)
                            && t.getSource().equals(source));

            if (!exists) {
                Tender tender = new Tender();
                tender.setTitle(title);
                tender.setOrganization(org);
                tender.setDistrict("Bihar");
                tender.setCategory(category);
                tender.setSource(source);
                tender.setSourceUrl(sourceUrl);
                tender.setPublishedDate(LocalDate.now());
                tender.setDeadline(LocalDate.now().plusDays(30));
                tender.setStatus(Tender.TenderStatus.ACTIVE);
                tenderRepository.save(tender);
                log.info("Saved tender: " + title);
            } else {
                log.info("Duplicate skipped: " + title);
            }
        } catch (Exception e) {
            log.warning("Error saving tender: " + e.getMessage());
        }
    }
    // Manual trigger
    public void scrapeNow() {
        scrapeAllPortals();
    }
}