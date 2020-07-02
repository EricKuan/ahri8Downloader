package com.ek.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
	private static String BASE_URL = "https://ahri8.com/readOnline2.php?ID=";
	
	@Autowired
	SystemConfig conf;
	
	public void exec(String url) throws FailingHttpStatusCodeException, MalformedURLException, IOException, InterruptedException, ExecutionException {
		WebClient webClient = new WebClient();

		String ID = url.substring(url.indexOf("ID=") + 3, url.length());
		logger.info(ID);

		webClient.getOptions().setUseInsecureSSL(true);
		webClient.getOptions().setJavaScriptEnabled(false);
		webClient.getOptions().setCssEnabled(false);
		webClient.getOptions().setRedirectEnabled(false);
		webClient.getOptions().setThrowExceptionOnScriptError(false);
		webClient.getOptions().setTimeout(5000);
		HtmlPage htmlPage = webClient.getPage(BASE_URL + ID + "&host_id=0");
		webClient.waitForBackgroundJavaScript(3000);
		HtmlElement body = htmlPage.getBody();

		Document doc = Jsoup.parse(body.asXml());
		String pages = doc.getElementById("next_page_btn_area2").select("a").last().attr("href");
		pages = pages.substring(pages.lastIndexOf("=") + 1, pages.length());
		logger.info(pages);

		int maxPage = Integer.valueOf(pages);

		ArrayList<CompletableFuture> result = new ArrayList<>();
		ExecutorService executor = Executors.newFixedThreadPool(5);
		for (int i = 1; i <= maxPage; i += 2) {
			htmlPage = webClient.getPage(BASE_URL + ID + "&host_id=17&page=" + i);
			webClient.waitForBackgroundJavaScript(3000);
			HtmlElement pageData = htmlPage.getBody();
			GetPagePhotoExecutor page = new GetPagePhotoExecutor();
			page.setTitle(htmlPage.getTitleText());
			page.setPageNumber(i);
			page.setPageData(pageData);
			page.setSavePath(conf.savepath);

			result.add(CompletableFuture.runAsync(page, executor));

		}
		webClient.close();
		CompletableFuture[] jobs = new CompletableFuture[result.size()];
		result.toArray(jobs);
		CompletableFuture future = CompletableFuture.allOf(jobs);
		future.get();
		System.out.println("Mission Completed");
		executor.shutdown();
	}
}
