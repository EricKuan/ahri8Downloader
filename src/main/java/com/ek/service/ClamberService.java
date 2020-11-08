package com.ek.service;

import com.ek.configuration.SystemConfig;
import com.ek.model.PhotoBean;
import com.ek.tools.GetEHUtil;
import com.gargoylesoftware.htmlunit.ScriptPreProcessor;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.sourceforge.htmlunit.corejs.javascript.EvaluatorException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class ClamberService {

    private static final Logger logger = LogManager.getLogger(ClamberService.class);
    private static String BASE_URL = "https://ahri-hentai.com/readOnline2.php?ID=";
    private static String BASEURL_KEY_WORD = "HTTP_IMAGE";
    private static String PHOTO_KEY_WORD = "Original_Image_List";
    private static String SIZE_WORD = "_w1100.";

    @Autowired
    SystemConfig conf;

    public void exec(String url) {
        try {

            String ID = url.substring(url.indexOf("ID=") + 3, url.length());
            logger.info(ID);
            HtmlPage htmlPage;
            List<PhotoBean> photoBeanList;
            Gson gson = new Gson();
            String baseUrl;
            try (WebClient webClient = getWebClient()) {
                htmlPage = webClient.getPage(BASE_URL + ID + "&host_id=0");
                HtmlElement body = htmlPage.getBody();

                String bodyText = body.asXml();
                //處理基本 URL
                baseUrl = getBaseUrl(bodyText);

                logger.info("baseURL: {}", baseUrl);

                //處理圖片網址
                String photoJsonStr = getPhotoJsonStr(bodyText);
                logger.info("photoJsonStr: {}", photoJsonStr);

                photoBeanList = gson.fromJson(photoJsonStr, new TypeToken<List<PhotoBean>>() {
                }.getType());


            }

            //開始下載
            ArrayList<CompletableFuture> result = new ArrayList<>();

            ExecutorService executor = Executors.newFixedThreadPool(5);
            try {
                StringBuilder savePath = new StringBuilder();
                savePath.append(conf.savepath)
                        .append("\\")
                        .append(htmlPage.getTitleText());
                for (PhotoBean photoBean : photoBeanList) {
                    StringBuilder urlBuilder = new StringBuilder()
                            .append(baseUrl)
                            .append(photoBean.getNew_filename())
                            .append(SIZE_WORD)
                            .append(photoBean.getExtension());
                    StringBuilder saveFileName = new StringBuilder();
                    saveFileName.append(photoBean.getSort())
                            .append(".")
                            .append(photoBean.getExtension());

                    result.add(CompletableFuture.runAsync(() -> {
                        GetEHUtil.savePhoto(urlBuilder.toString()
                                , saveFileName.toString()
                                , savePath.toString());
                    }, executor));

                }
                CompletableFuture[] jobs = new CompletableFuture[result.size()];
                result.toArray(jobs);
                CompletableFuture future = CompletableFuture.allOf(jobs);
                future.get();
                System.out.println("ClamberService Completed");
                executor.shutdown();
            } catch (EvaluatorException e) {
                logger.error(e);
                e.printStackTrace();
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
        webClient.getOptions().setJavaScriptEnabled(false);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setDownloadImages(false);

        webClient.getOptions().setPopupBlockerEnabled(false);
        webClient.getOptions().setRedirectEnabled(false);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setTimeout(3000);
        webClient.setJavaScriptTimeout(1000);

        return webClient;
    }

    private String getBaseUrl(String bodyText) {
        int urlIndex = bodyText.indexOf(BASEURL_KEY_WORD);
        String searchTxt = bodyText.substring(urlIndex);

        searchTxt = searchTxt.substring(0, searchTxt.indexOf(";"));
        searchTxt = searchTxt.substring(searchTxt.indexOf("\"") + 1);
        searchTxt = searchTxt.substring(0, searchTxt.indexOf("\""));
        StringBuilder baseUrl = new StringBuilder("https:").append(searchTxt);
        return baseUrl.toString();
    }

    private String getPhotoJsonStr(String bodyText) {
        int urlIndex = bodyText.indexOf(PHOTO_KEY_WORD);
        String searchTxt = bodyText.substring(urlIndex);
        searchTxt = searchTxt.substring(searchTxt.indexOf("=") + 1);
        searchTxt = searchTxt.substring(searchTxt.indexOf("=") + 1);
        searchTxt = searchTxt.substring(0, searchTxt.indexOf(";"));

        searchTxt = searchTxt.trim();
        logger.info(searchTxt);
        return searchTxt;
    }
}
