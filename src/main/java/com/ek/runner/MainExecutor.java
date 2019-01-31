package com.ek.runner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.ek.controller.ClambController;


import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.logging.log4j.LogManager;


@Component
public class MainExecutor implements CommandLineRunner{

	private static final Logger LOG = LogManager.getLogger(MainExecutor.class.getName());
	
	@Autowired
	ClambController contorller;
	
	@Override
	public void run(String... args) throws Exception {

		contorller.exec();
		System.out.println("Mission Completed");
//		delCompleteFile();
		System.exit(0);
	}

	private static void delCompleteFile() throws IOException {
		String filePath = "./conf/downliadList.txt";
		File myFile = new File(filePath);
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
		String backPath = filePath.substring(0, filePath.length() - 4) + "." + sdf.format(cal.getTime()) + ".txt";
		System.out.println(backPath);
		myFile.renameTo(new File(backPath));
		myFile = new File(filePath);
		if ((myFile.createNewFile())) {
			System.out.println("downloadList.txt reset Completed!");
		}
	}

}
