package com.ek.controller;

import com.ek.executor.GetPagePhotoExecutor;
import com.ek.service.ClamberService;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class ClambController {

  @Autowired ClamberService clambService;


  private static final Logger logger = LogManager.getLogger(GetPagePhotoExecutor.class);

  public void exec() throws Exception {

    FileInputStream fileInStreamObj = new FileInputStream("./conf/downliadList.txt");
    InputStream inStreamObject = fileInStreamObj;
    Scanner sc = new Scanner(inStreamObject);
    ArrayList<CompletableFuture> result = new ArrayList<>();

    ExecutorService executor = Executors.newFixedThreadPool(5);
    try {
      while (sc.hasNext()) {
        String url = sc.nextLine();

        result.add(
            CompletableFuture.runAsync(
                () -> {
                  try {
                    clambService.exec(url);
                  } catch (FailingHttpStatusCodeException e) {
                    logger.error(e);
                    e.printStackTrace();
                  }
                },
                executor));
      }

      CompletableFuture[] jobs = new CompletableFuture[result.size()];
      result.toArray(jobs);
      CompletableFuture future = CompletableFuture.allOf(jobs);
      future.join();
      System.out.println("ClambController Completed");
    } catch (Exception e) {
      logger.error(e);
      e.printStackTrace();
    } finally {
      executor.shutdown();
    }
  }
}
