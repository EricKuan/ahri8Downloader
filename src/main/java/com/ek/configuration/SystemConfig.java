package com.ek.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class SystemConfig {
	@Value("${getManga.rootpath}")
	public String rootpath;
	
	@Value("${getManga.savepath}")
	public String savepath;
	
	@Value("${getManga.layer}")
	public int layer;
	
	@Value("${getManga.phantompath}")
	public String phantompath;
}
