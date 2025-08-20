package com.geektrust.backend;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import com.geektrust.backend.appConfig.ApplicationConfig;
import com.geektrust.backend.commands.CommandInvoker;
import com.geektrust.backend.exceptions.NoSuchCommandException;

// ./gradlew run --args="sample_input/input1.txt"

public class App {
	private static final int MINIMUM_ARGS_REQUIRED = 1;
	private static final int INPUT_FILE_ARG_INDEX = 0;
	private static final int COMMAND_NAME_INDEX = 0;
	private static final String TOKEN_DELIMITER = " ";

	public static void main(String[] args){
		if(args.length >= MINIMUM_ARGS_REQUIRED){
			String inputFile = args[INPUT_FILE_ARG_INDEX];
			run(inputFile);
		}
	}

	public static void run(String commandLineArgs){
		//Logic to perform the task :
		ApplicationConfig applicationConfig = new ApplicationConfig();
		CommandInvoker commandInvoker = applicationConfig.getCommandInvoker();
		BufferedReader reader;
		String inputFile = commandLineArgs;

		try{
			reader = new BufferedReader(new FileReader(inputFile));
			String line = reader.readLine();
			while(line != null){
				List<String> tokens = Arrays.asList(line.split(TOKEN_DELIMITER));

				commandInvoker.executeCommand(tokens.get(COMMAND_NAME_INDEX), tokens);

				//read the next Line :
				line = reader.readLine();
			}
			reader.close();
		}catch(IOException | NoSuchCommandException e){
			System.out.println(e);
		}
	}
}