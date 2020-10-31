package com.ek.controller;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ek.configuration.SystemConfig;
import com.ek.executor.GetPagePhotoExecutor;
import com.ek.service.ClamberService;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


@Service
public class ClambController {

	
	@Autowired
	ClamberService clambService;
	
	@Autowired
	SystemConfig conf;

	private static final Logger logger = LogManager.getLogger(GetPagePhotoExecutor.class);

	public void exec() throws Exception {
		
		ExecutorService executor = Executors.newFixedThreadPool(5);
		FileInputStream fileInStreamObj = new FileInputStream("./conf/downliadList.txt");
		InputStream inStreamObject = fileInStreamObj;
		Scanner sc = new Scanner(inStreamObject);
		ArrayList<CompletableFuture> result = new ArrayList<>();
		while (sc.hasNext()) {
			String url = sc.nextLine();
			
			result.add(CompletableFuture.runAsync(()->{
				try {
					clambService.exec(url);
				} catch (FailingHttpStatusCodeException | IOException | InterruptedException | ExecutionException e) {
					logger.error(e);
				}
			}, executor));
			
		}
		
		CompletableFuture[] jobs = new CompletableFuture[result.size()];
		result.toArray(jobs);
		CompletableFuture future = CompletableFuture.allOf(jobs);
		future.get();
		System.out.println("Mission Completed");
		executor.shutdown();
	}
}
