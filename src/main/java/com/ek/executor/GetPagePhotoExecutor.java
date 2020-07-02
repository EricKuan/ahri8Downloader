package com.ek.executor;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.ek.tools.GetEHUtil;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class GetPagePhotoExecutor implements Runnable{

	private static final Logger logger = LogManager.getLogger(GetPagePhotoExecutor.class);
	
	private String title;
	private HtmlElement pageData;
	private String savePath;
	private int pageNumber;
	
	
	public String getTitle() {
		return title;
	}


	public void setTitle(String title) {
		this.title = title;
	}


	public HtmlElement getPageData() {
		return pageData;
	}


	public void setPageData(HtmlElement pageData) {
		this.pageData = pageData;
	}


	public String getSavePath() {
		return savePath;
	}


	public void setSavePath(String savePath) {
		this.savePath = savePath;
	}


	public int getPageNumber() {
		return pageNumber;
	}


	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}




	@Override
	public void run() {
		if(pageNumber%2==0) {
			logger.info("雙數頁跳過!");
			return;
		}
		

		try {
			Document doc = Jsoup.parse(pageData.asXml());
			Elements url = doc.getElementById("show_image_area").select("div").select("img");
			int fileNameCount = (pageNumber-1)*10;
			for(int i=0;i<url.size();i++) {
				logger.info(url.get(i).attr("src"));
				GetEHUtil.savePhoto(url.get(i).attr("src"), (fileNameCount+i)+".jpg" , savePath + "\\" + title);
			}
		} catch (FailingHttpStatusCodeException e) {
			logger.error(e);
		}
		
	
		
	}
}
