package com.ek.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.ScriptPreProcessor;
import net.sourceforge.htmlunit.corejs.javascript.EvaluatorException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ek.configuration.SystemConfig;
import com.ek.executor.GetPagePhotoExecutor;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

@Service
public class ClamberService {

  private static final Logger logger = LogManager.getLogger(ClamberService.class);
  private static String BASE_URL = "https://ahri-hentai.com/readOnline2.php?ID=";

  @Autowired SystemConfig conf;

  public void exec(String url) {
    try {

      String ID = url.substring(url.indexOf("ID=") + 3, url.length());
      logger.info(ID);
      HtmlPage htmlPage;
      int maxPage=0;
      try (WebClient webClient = getWebClient()) {
        htmlPage = webClient.getPage(BASE_URL + ID + "&host_id=0");
        webClient.waitForBackgroundJavaScript(1000);
        HtmlElement body = htmlPage.getBody();
        		logger.info(body.asXml());
        Document doc = Jsoup.parse(body.asXml());

        String pages = doc.getElementById("next_page_btn_area2").select("a").last().attr("href");
        logger.info(pages);
        pages = pages.split("page=")[1].split("&")[0];
//        pages = pages.substring(pages.lastIndexOf("=") + 1, pages.length());

        logger.info(pages);

        maxPage = Integer.valueOf(pages);
      }

      ArrayList<CompletableFuture> result = new ArrayList<>();

      ExecutorService executor = Executors.newFixedThreadPool(5);
      try {
        for (int i = 1; i <= maxPage; i += 2) {
          try (WebClient pageClient = getWebClient()) {

            htmlPage = pageClient.getPage(BASE_URL + ID + "&host_id=17&page=" + i);
            pageClient.waitForBackgroundJavaScript(1000);
            HtmlElement pageData = htmlPage.getBody();
            GetPagePhotoExecutor page = new GetPagePhotoExecutor();
            page.setTitle(htmlPage.getTitleText());
            page.setPageNumber(i);
            page.setPageData(pageData);
            page.setSavePath(conf.savepath);

            result.add(CompletableFuture.runAsync(page, executor));
          }
        }
        CompletableFuture[] jobs = new CompletableFuture[result.size()];
        result.toArray(jobs);
        CompletableFuture future = CompletableFuture.allOf(jobs);
        future.get();
        System.out.println("ClamberService Completed");
        executor.shutdown();
      } catch (EvaluatorException e) {
        // Do nothing
      } finally {
        executor.shutdown();
      }
    } catch (Exception e) {
      logger.error(e);
      e.printStackTrace();
    }
  }

  private WebClient getWebClient() {
    WebClient webClient = new WebClient();
    webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
    webClient.getOptions().setUseInsecureSSL(false);
    webClient.getOptions().setJavaScriptEnabled(true);
    webClient.getOptions().setCssEnabled(false);
    webClient.getOptions().setDownloadImages(false);

    webClient.getOptions().setPopupBlockerEnabled(false);
    webClient.setScriptPreProcessor(
        new ScriptPreProcessor() {
          @Override
          public String preProcess(
              HtmlPage htmlPage, String s, String s1, int i, HtmlElement htmlElement) {
            return s.replaceAll("<http*ads*>","");
//            return s;
          }
        });
    webClient.getOptions().setRedirectEnabled(false);
    webClient.getOptions().setThrowExceptionOnScriptError(false);
    webClient.getOptions().setTimeout(3000);
    webClient.setJavaScriptTimeout(300);

    return webClient;
  }
}
