package app;
//package com.journaldev.threads;
import java.io.*;
import java.util.*;
//import java.util.concurrent.*;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

class Disk{
	int num;
	String diskNum;
	static final int NUM_SECTORS = 1024;
	StringBuffer sectors[] = new StringBuffer[NUM_SECTORS];
	DiskCircle diskCircle;
	StackPane diskStack;
	GridPane diskGrid;
	Text diskTitle;
	VBox leftVbox;
	VBox rightVbox;
	VBox diskVbox;
	Disk(int i){
		this.num = i;
		this.diskNum = "Disk"+Integer.toString(i);	
		this.diskCircle = new DiskCircle();
		this.diskStack = new StackPane();
		this.diskGrid = new GridPane();
		this.leftVbox = new VBox();
		leftVbox.setAlignment(Pos.TOP_LEFT);
		this.rightVbox = new VBox();
		rightVbox.setAlignment(Pos.TOP_LEFT);
		this.diskVbox = new VBox();
		diskVbox.setAlignment(Pos.TOP_CENTER);
		this.diskTitle = new Text(diskNum);
		diskTitle.setTextAlignment(TextAlignment.CENTER);
		ResetTitleColor();
		diskGrid.add(leftVbox,0,0);
		diskGrid.add(rightVbox, 1, 0);
		diskGrid.setAlignment(Pos.TOP_CENTER);
		GridPane.setMargin(leftVbox, new Insets(15,2.5,15,5));
		GridPane.setMargin(rightVbox, new Insets(15,5,15,2.5));
		diskVbox.getChildren().setAll(diskTitle,diskGrid);
		StackPane.setMargin(diskCircle, new Insets(20,0,5,0));
		diskCircle.heightProperty().bind(diskGrid.heightProperty());
		diskStack.setAlignment(Pos.TOP_CENTER);
		diskStack.getChildren().setAll(diskCircle,diskVbox);

	}
	public void ResetTitleColor() {
		diskTitle.setStyle("-fx-font: 30px Tahoma;\r\n" + 
				"    -fx-fill: linear-gradient(from 0% 0% to 150% 200%, repeat, lightcyan 0%, aqua 100%);\r\n" + 
				"    -fx-stroke: black;\r\n" + 
				"    -fx-stroke-width: .5;");
	}
	public void ResetDisk() {
		Platform.runLater( () -> {
				//System.out.println("\t\t\t\tRESET");
				ResetTitleColor();
				driverClass2.diskGP.getChildren().remove(this.diskStack);
				driverClass2.userGP.getChildren().remove(this.diskStack);
				//TODO: delete?
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				driverClass2.diskGP.add(this.diskStack, 0,num-1);

		});

	}
	public void MoveToUser(int UserNum) {
		Platform.runLater( () -> {
			//System.out.println("\t\t\t\tMOVE");
			driverClass2.userGP.getChildren().remove(this.diskStack);
			driverClass2.diskGP.getChildren().remove(diskStack);
			//TODO: delete?
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			driverClass2.userGP.add(this.diskStack, UserNum-1, 1);

		});

	}
	public class DiskCircle extends Cylinder{
		DiskCircle(){
			setRadius(80.0f);
			setHeight(100);
			setRotationAxis(Rotate.X_AXIS);
			setRotate(15);
			final PhongMaterial orangeMaterial = new PhongMaterial();
		    orangeMaterial.setDiffuseColor(Color.rgb(255,204,102,.6));
		    orangeMaterial.setSpecularColor(Color.WHITE);
		    setMaterial(orangeMaterial);

		}
	}

	void write(int sector, StringBuffer data, int UserNum){
		try{
		StringBuffer tmp = new StringBuffer(data.toString());
		Text txt = new Text(data.toString());
		txt.setTextAlignment(TextAlignment.CENTER);
		txt.setStyle("-fx-font: 9px Tahoma;\r\n" + 
				"    -fx-fill: "+driverClass2.UserColor[UserNum-1] +";\r\n");
		
		
		//System.out.println(diskNum+ ": writing " + data.toString() + " to sector "+Integer.toString(sector));
		Thread.sleep((long) (200*driverClass2.speedLevels[driverClass2.Speed]));
		sectors[sector] = tmp;
		Platform.runLater( () -> {
			diskTitle.setStyle("-fx-font: 30px Tahoma;\r\n" + 
					"    -fx-fill: "+driverClass2.UserColor[UserNum-1] +";\r\n" + 
					"    -fx-stroke: black;\r\n" + 
					"    -fx-stroke-width: .5;");
			if(sector%2 ==0) {
				leftVbox.getChildren().add(txt);
			}else {
				rightVbox.getChildren().add(txt);

			}
    		});
		}
		catch(InterruptedException IE){
			System.out.println("IE Error");
		}
	}
	void read(int sector, StringBuffer data){
		
		try{
		Thread.sleep((long) (200*driverClass2.speedLevels[driverClass2.Speed]));
		data.append(sectors[sector]);
		//System.out.println(diskNum+" read: data = "+ data.toString()+ " from sector " + Integer.toString(sector));
		}
		catch(InterruptedException IE){
			System.out.println("IE Error");
		}
	}
	

}

class UserThread extends Thread{
	int num;
	String userNum;
	String filePath;
	StringBuffer currentBuffer = new StringBuffer();
	FileReader fileReader;
	BufferedReader bufferedReader;
	int currentDiskIdx;
	Disk currentDisk;
	int currentDiskSector;
	boolean fileEnd;
	ResourceManager DiskResMan;
	ResourceManager PrinterResMan;
	Disk[] disks;
	Printer[] printers;
	DiskManager DM;
	FileInfo fileInfo;
	String key;
	DirectoryManager DirMan;
	UserBox userBox;
	StackPane userStack;
	Text userTitle;
	Text userCurBuf;
	VBox userVbox;
	UserThread(int i,ResourceManager DRM, Disk[] dks, DiskManager dm, 
		DirectoryManager dirM, ResourceManager PRM, Printer[] prs){
		try{
			this.num = i;
			this.userNum = "USER"+Integer.toString(i);
			this.filePath = "inputs/"+userNum;
			this.fileReader = new FileReader(filePath);
			this.bufferedReader = new BufferedReader(fileReader);
			this.currentDiskIdx = -1;
			this.currentDiskSector = -1;
			this.DiskResMan = DRM;
			this.PrinterResMan = PRM;
			this.disks = driverClass2.Disks;
			this.DM = dm;
			this.fileEnd = false;
			this.DirMan = dirM;
			this.printers = prs;
			
			this.userVbox = new VBox();
			this.userBox = new UserBox();
			this.userStack = new StackPane();
			this.userTitle = new Text(userNum);
			userVbox.setAlignment(Pos.TOP_CENTER);
			this.userTitle.setStyle("-fx-font: 30px Tahoma;\r\n" + 
					"    -fx-fill: "+driverClass2.UserColor[num-1]+";\r\n" + 
					"    -fx-stroke: black;\r\n" + 
					"    -fx-stroke-width: .5;");
			this.userVbox.getChildren().setAll(userTitle);
	    	this.userStack.getChildren().setAll(userBox,userVbox);

		}
		catch(FileNotFoundException ex) {
            System.out.println(
                "Unable to open file '" + 
                filePath + "'");                
        }
	}
	public class UserBox extends Rectangle{
		UserBox(){
			setWidth(160);
	        setHeight(150);
	        setArcWidth(20);
	        setArcHeight(20);                
	        setFill(Color.rgb(192,228,230,.25) );
	        setStroke(Color.rgb(192,228,230,.5));
		}
	}
	public String UserText() { 
		String sec = "n/a";
		if(currentDiskSector >= 0)
			sec = ""+currentDiskSector;
		String idx = "n/a";
		if(currentDiskIdx >=0)
			idx = ""+currentDiskIdx;
		return "StringBuffer:\n" + currentBuffer
		+ "\nCurrentDisk: "+ idx
		+ "\nDiskSector: " + sec;
	}
	
	void getLine(){

        String line = null;
		try{
            if((line = bufferedReader.readLine()) != null) {
            	currentBuffer.append(line);
            	
        		Platform.runLater( () -> {
	    			this.userCurBuf = new Text("Current Buffer:\n" + currentBuffer.toString());
	    			this.userCurBuf.setStyle("-fx-font: 16px Tahoma;\r\n" + 
	    				"    -fx-fill: "+driverClass2.UserColor[num-1]+";\r\n" + 
	    				"    -fx-stroke: black;\r\n" + 
	    				"    -fx-stroke-width: .15;");
	    			this.userVbox.getChildren().setAll(userTitle,userCurBuf);
        		});
            }
			else{
			fileEnd = true;
			//System.out.println("EOF Found");
			}
		}
		catch(FileNotFoundException ex) {
            System.out.println(
                "Unable to open file '" + 
                filePath + "'");                
        }
		catch(IOException ex) {
            System.out.println(
                "Error reading file '" 
                + filePath + "'");                  
		}
	}


	void evalBuffer()throws InterruptedException,IOException{
		String[] command = currentBuffer.toString().split(" ");
		//System.out.println("\neval curBuf: " + currentBuffer.toString());
		switch(command[0]){
			case ".save": 
				key = command[1];
				currentDiskIdx = DiskResMan.request();
				currentDisk = disks[currentDiskIdx];
				currentDiskSector = DM.DiskSectors[currentDiskIdx];
				fileInfo = new FileInfo(currentDiskIdx,currentDiskSector,0);
				//System.out.println("eval in save curDiskIdx: " + Integer.toString(currentDiskIdx));
				
				currentDisk.MoveToUser(num);

				break;
			case ".end":
			//	System.out.println("eval got end... key = " + key);
				currentDisk.ResetDisk();
				DirMan.enter(key,fileInfo);
				DM.setCurrentSector(currentDiskIdx, currentDiskSector);
				DiskResMan.release(currentDiskIdx);
				currentDiskIdx = -1;
				currentDiskSector = -1;
				
    			

            	
				break;
			case ".print":

				
				PrintJobThread newThread = new PrintJobThread(command[1], PrinterResMan, printers, disks, DirMan,num);
				newThread.setDaemon(true);
				newThread.start();
				break;
			default:
				currentDisk.write(currentDiskSector, currentBuffer,num);
				currentDiskSector++;
				fileInfo.fileLength++;
		}
		currentBuffer.delete(0,currentBuffer.length());
		Platform.runLater( () -> {
			userVbox.getChildren().setAll(userTitle,userCurBuf);
		});

		
	}
	public void run(){
		while(true){
			try{
			Thread.sleep(1);
			getLine();
			if(fileEnd) break;
			evalBuffer();
			}
			
			catch(InterruptedException IE){
				System.out.println("InterruptedException");
			}
			catch(IOException IO){
				System.out.println("IO Ecxp");
			}

		}
		//System.out.println("Finishing Run for "+ userNum);
	}
	
}

class FileInfo{
	int diskNumber;
	int startingSector;
	int fileLength;
	FileInfo(int DN, int Sec,int len){
		this.diskNumber = DN;
		this.startingSector = Sec;
		this.fileLength = len;
	}
	void printInfo(){
		System.out.println("diskNum: " + diskNumber + " startSec: " + startingSector + " len:"+ fileLength);
	}

}
class DiskManager{
	int[] DiskSectors;

	DiskManager(){
		this.DiskSectors = new int[2];
		this.DiskSectors[0] = 0;
		this.DiskSectors[1] = 0;
	}
	void setCurrentSector(int disk, int sector){
		DiskSectors[disk] = sector;
	}
}

class DirectoryManager{
	Hashtable<String, FileInfo> T = new Hashtable<String, FileInfo>();
	void enter(String key, FileInfo file){
		T.put(key,file);
	}
	FileInfo lookup(String key){
		return T.get(key);
	}
}
class ResourceManager{
	boolean isFree[];
	ResourceManager(int numOfItems){
		isFree = new boolean[numOfItems];
		for(int i = 0; i < isFree.length; ++i)
			isFree[i] = true;
	}
	synchronized int request()throws InterruptedException{
		while(true){
			for(int i = 0; i < isFree.length; ++i){
				if(isFree[i]){
					isFree[i] = false;
					return i;
				}
			}
			this.wait(); //block until someone releases a Resource
		}
	}
	synchronized void release(int index){
		isFree[index] = true;
		this.notify(); //let a waiting thread run
	}

}
class PrintJobThread extends Thread{
	int userNum;
	String File;
	ResourceManager PrinterResMan;
	Printer p;
	int printerIdx;
	Printer[] printers;
	Disk[] disks;
	FileInfo fileInfo;
	DirectoryManager DirMan;
	FileWriter fileWriter;
	BufferedWriter bufferedWriter;
	
	StringBuffer currentBuf;

	PJTBox PJTbox;
	StackPane PJTStack;
	Label PJTLabel;
	Text PJTTitle;
	Text PJTFile;
	VBox PJTVbox;
	int PJTnum;
	PrintJobThread(String file, ResourceManager PRM, Printer[] Ps,
			Disk[] Ds, DirectoryManager DM, int n)throws IOException{


		this.userNum = n;
		this.File = file;
		this.disks = Ds;
		this.DirMan = DM;
		this.fileInfo = DM.lookup(file);

		for( int x = 0; x< driverClass2.openPJT[userNum-1].length; ++x) {
			if(driverClass2.openPJT[userNum-1][x] == 0) {
				this.PJTnum = x;
				break;
			}
		}
		driverClass2.openPJT[userNum-1][PJTnum] = 1;
		
		Platform.runLater( () -> {

		this.PJTbox = new PJTBox();
		this.PJTStack = new StackPane();
		this.PJTVbox = new VBox();
		this.PJTTitle = new Text("PrintJobThread");
		PJTVbox.setAlignment(Pos.CENTER);
		PJTStack.setAlignment(Pos.TOP_CENTER);
		this.PJTTitle.setStyle("-fx-font: 16px Tahoma;\r\n" + 
				"    -fx-fill: "+driverClass2.UserColor[userNum-1]+";" + 
				"    -fx-stroke: black;\r\n" + 
				"    -fx-stroke-width: .15;");
		this.PJTFile = new Text(File);
		this.PJTFile.setStyle("-fx-font: 14px Tahoma;\r\n" + 
				"    -fx-fill: "+driverClass2.UserColor[userNum-1]+";" + 
				"    -fx-stroke: black;\r\n" + 
				"    -fx-stroke-width: .15;");
		PJTbox.heightProperty().bind(PJTVbox.heightProperty());
		this.PJTVbox.getChildren().setAll(PJTTitle,PJTFile);
    	this.PJTStack.getChildren().setAll(PJTbox,PJTVbox);

		driverClass2.userGP.add(PJTStack, userNum-1, PJTnum+2);
		});
		this.PrinterResMan = PRM;
	}
	public void requestPrinter() {
		this.printerIdx = -1;
		try {
			this.printerIdx = PrinterResMan.request();
			this.p = driverClass2.Printers[printerIdx];
			this.fileWriter = new FileWriter(p.filePath,true);
			this.bufferedWriter = new BufferedWriter(fileWriter);
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public class PJTBox extends Rectangle{
		PJTBox(){
			setWidth(160);
	        setHeight(80);
	        setArcWidth(20);
	        setArcHeight(20);                
	        setFill(Color.rgb(192,228,230,.25) );
	        setStroke(Color.rgb(192,228,230,.5));
		}
	}

	public void run(){
		try{
			requestPrinter();

		for(int i = 0; i <fileInfo.fileLength; ++i){
			Thread.sleep(1);
			StringBuffer tmp = new StringBuffer();
			disks[fileInfo.diskNumber].read(fileInfo.startingSector+i,tmp);
			currentBuf = new StringBuffer(tmp.toString());
			Text PJTBuffer= new Text("Current Buffer:\n" + currentBuf);
			PJTBuffer.setStyle("-fx-font: 14px Tahoma;\r\n" + 
					"    -fx-fill: "+driverClass2.UserColor[userNum-1]+";" + 
					"    -fx-stroke: black;\r\n" + 
					"    -fx-stroke-width: .15;");
						
			Platform.runLater( () -> {
				PJTVbox.getChildren().setAll(PJTTitle,PJTFile,PJTBuffer);
		    	PJTStack.getChildren().setAll(PJTbox,PJTVbox);

				driverClass2.printerGP.getChildren().remove(PJTStack);
				driverClass2.userGP.getChildren().remove(PJTStack);
				driverClass2.printerGP.add(PJTStack,printerIdx,1);
			});			
			Thread.sleep((long) (2750*driverClass2.speedLevels[driverClass2.Speed]));
			p.printToFile(tmp, bufferedWriter,userNum);
		}
		PrinterResMan.release(printerIdx);
		bufferedWriter.close();
		Platform.runLater( () -> {
			driverClass2.userGP.getChildren().remove(PJTStack);
			driverClass2.printerGP.getChildren().remove(PJTStack);
		});
		driverClass2.openPJT[userNum-1][PJTnum] = 0;

		}
		catch(IOException IO){
			System.out.println("ioexc");
		}

		catch(InterruptedException IE){
			System.out.println("intEx");
		}

	}
}

class Printer{
	String printerNum;
	String filePath;
	PrinterBox printerBox;
	StackPane printerStack;
	Label printerLabel;
	String printerText;
	Text printerTitle;
	VBox InprinterVbox;
	
	Printer(int i) throws IOException{
		this.printerNum = "PRINTER" + Integer.toString(i);
		this.filePath = "outputs/"+printerNum;
		this.printerBox = new PrinterBox();
		this.InprinterVbox = new VBox();
		InprinterVbox.setAlignment(Pos.TOP_CENTER);
	//	this.printerText = printerNum + "\npath:\n" + filePath;
		this.printerTitle = new Text(printerNum);
		this.printerTitle.setStyle("-fx-font: 30px Tahoma;\r\n" + 
				"    -fx-fill: #E042E5;" + 
				"    -fx-stroke: black;\r\n" + 
				"    -fx-stroke-width: .25;");
	//	this.printerLabel = new Label(printerText);
	//	printerLabel.setFont(new Font("Arial", 10));
		InprinterVbox.getChildren().add(printerTitle);
		this.printerStack = new StackPane();
		//this.printerStack.getChildren().setAll(printerBox,printerLabel);
		printerStack.getChildren().setAll(printerBox,InprinterVbox);
		printerBox.heightProperty().bind(printerStack.heightProperty());
	//	StackPane.setAlignment(printerLabel,Pos.TOP_LEFT);
	//	StackPane.setMargin(printerLabel, new Insets(5));		
	}
	//make sure printThread sleeps for 2750

	void printToFile(StringBuffer data, BufferedWriter BW,int userNum) throws IOException{
		//System.out.println("calling printToFile");
		BW.write(data.toString()+"\n");
		//BW.close();
		//addToText(data.toString());/
		Platform.runLater( () -> {
		Text txt = new Text(data.toString());
		txt.setTextAlignment(TextAlignment.CENTER);
		txt.setStyle("-fx-font: 10px Tahoma;\r\n" + 
				"    -fx-fill: "+ driverClass2.UserColor[userNum-1]+";");
		this.printerTitle.setStyle("-fx-font: 30px Tahoma;\r\n" + 
				"    -fx-fill: "+ driverClass2.UserColor[userNum-1]+";" + 
				"    -fx-stroke: black;\r\n" + 
				"    -fx-stroke-width: .25;");
		
		InprinterVbox.getChildren().add(txt);
			
		/*
		printerStack.getChildren().remove(printerLabel);
		printerLabel = new Label(printerText);
		printerLabel.setFont(new Font("Arial", 10));
		printerStack.getChildren().setAll(printerBox,printerLabel);
		StackPane.setAlignment(printerLabel,Pos.TOP_LEFT);
		StackPane.setMargin(printerLabel, new Insets(5));*/
		});
	}
	

	public class PrinterBox extends Rectangle{
		PrinterBox(){
			setWidth(160);
	        setHeight(150);
	        setArcWidth(20);
	        setArcHeight(20);                
	        setFill(Color.rgb(216,189,222,.15) );
	        setStroke(Color.rgb(216,189,222,.75));
		}
	}
	/*
	public String PrinterText() { 
		return printerNum
		+ "\npath:\n" + filePath;
	}*/
	public void addToText(String newtext) {
		printerText += "\n" +newtext;
	}

}


public class driverClass2 extends Application{
	public static int NUMBER_OF_USERS=4;
	public static int NUMBER_OF_DISKS=2;
	public static int NUMBER_OF_PRINTERS=3;
	
	public static int[][] openPJT = {{0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0}};

	public static String[] UserColor = {"#F363D1","#44C9F9","#23F946","#F94D23"};
	
	static UserThread[] Users = new UserThread[NUMBER_OF_USERS];
	static Disk[] Disks = new Disk[NUMBER_OF_DISKS];
	static Printer[] Printers = new Printer[NUMBER_OF_PRINTERS];
	
	Button speedButton;
	public static int Speed = 1;
	public static double[] speedLevels = {.5,1,2,4};
	public static String[] speedStrings = {"Double\nSpeed","Normal\nSpeed","Half\nSpeed","Quarter\nSpeed"};
	//javaFX
	public static GridPane gridpane = new GridPane();	
	Stage primaryStage;
	//public static GridPane LabelsGP = new GridPane();
	//VBox userVbox = new VBox();
	//VBox diskVbox = new VBox();
	//VBox printerVbox = new VBox();
	
	
	public static GridPane diskGP = new GridPane();
	public static GridPane userGP = new GridPane();
	public static GridPane printerGP = new GridPane();
	
	

	
	private void exitProgram() {
		primaryStage.close();
	}

	private void changeSpeed() {
		if(Speed >=3) {
			Speed = 0;
		} else Speed++;
		speedButton.setText(speedStrings[Speed]);
		//System.out.println("Speed is now " + speedLevels[Speed]);
		
	}
	public class CategoryBox extends Rectangle{
		CategoryBox(){
			setWidth(140);
	        setHeight(50);
	        setArcWidth(20);
	        setArcHeight(20);                
	        setFill(Color.rgb(216,189,222,.15) );
	        setStroke(Color.rgb(216,189,222,.75));
		}
	}

	
	
	public void startSetup() {
		primaryStage.setTitle("Ethan Chen's 141OS");
		gridpane.setAlignment(Pos.CENTER);
		Button startButton = new Button("Start OS");
		startButton.setStyle("-fx-background-color: #82DA56; -fx-border-color: #3C9211; -fx-border-width: 2px");
		gridpane.add(startButton, 0, 1);
		Text WelcomeText = new Text("Welcome to\nEthan Chen's\nOperating System!");
		WelcomeText.setTextAlignment(TextAlignment.CENTER);
		WelcomeText.setStyle("-fx-font: 36px Tahoma;\r\n" + 
				"    -fx-fill: #F363D1;\r\n" + 
				"    -fx-stroke: black;\r\n" + 
				"    -fx-stroke-width: .5;");
        GridPane.setConstraints(WelcomeText, 0, 0, 1, 1,HPos.CENTER, VPos.CENTER);
        GridPane.setConstraints(startButton, 0, 1, 1, 1,HPos.CENTER, VPos.CENTER);
		gridpane.add(WelcomeText, 0, 0);
		for(Node i:gridpane.getChildren()) {
			GridPane.setMargin(i, new Insets(10));
		}
		Scene scene= new Scene(gridpane, 400, 400);
		primaryStage.setScene(scene);
		primaryStage.setMinWidth(400);
		primaryStage.setMinHeight(400);
		gridpane.setStyle("-fx-background-color: #B3C1C1;");
		primaryStage.show();
		startButton.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event){
				try {
				OSsetup(startButton,WelcomeText);
				}
				catch(Exception E){
					System.out.println("uh oh");
					
				}
			}
		});
	}
	public void OSsetup(Button startButton,Text WelcomeText)	throws Exception {
		//System.out.println("Start was pressed");
		
		ResourceManager DiskRM = new ResourceManager(2);
		ResourceManager PrinterRM = new ResourceManager(3);
		DiskManager DiskMan = new DiskManager();
		DirectoryManager DirMan = new DirectoryManager();


		//create 2 disks and add to list 'Disks'
		Disk disk1 = new Disk(1);
		Disk disk2 = new Disk(2);
		Disks[0] = disk1;
		Disks[1] = disk2;	

		//create 3 printers and add to list 'Printers'	
		Printer Printer1 = new Printer(1);
		Printer Printer2 = new Printer(2);
		Printer Printer3 = new Printer(3);
		Printers[0] = Printer1;
		Printers[1] = Printer2;
		Printers[2] = Printer3;
		for(int i = 0; i < NUMBER_OF_PRINTERS;++i) {
			FileWriter fileWriter = new FileWriter(Printers[i].filePath);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			bufferedWriter.close();
		}
		//create 4 userThreads and add to list 'Users'
		UserThread user1 = new UserThread(1, DiskRM,Disks,DiskMan,DirMan,PrinterRM,Printers);
		UserThread user2 = new UserThread(2, DiskRM,Disks,DiskMan,DirMan,PrinterRM,Printers);
		UserThread user3 = new UserThread(3, DiskRM,Disks,DiskMan,DirMan,PrinterRM,Printers);
		UserThread user4 = new UserThread(4, DiskRM,Disks,DiskMan,DirMan,PrinterRM,Printers);
		Users[0] = user1;
		Users[1] = user2;
		Users[2] = user3;
		Users[3] = user4;

		Users[0].setDaemon(true);
		Users[1].setDaemon(true);
		Users[2].setDaemon(true);
		Users[3].setDaemon(true);
		
		
		gridpane.getChildren().remove(startButton);
		gridpane.getChildren().remove(WelcomeText);

		gridpane.setAlignment(Pos.TOP_LEFT);
		primaryStage.setMinWidth(1600);
		primaryStage.setMinHeight(1000);
		primaryStage.setX(0);
		primaryStage.setY(0);
		//categoryLabels
		VBox userVbox = new VBox();
		CategoryBox UserCategoryBox = new CategoryBox();
		Text UserCategoryText = new Text("Users");
		UserCategoryText.setStyle("-fx-font: 30px Tahoma;\r\n" + 
				"    -fx-fill: #004C99;" + 
				"    -fx-stroke: black;\r\n" + 
				"    -fx-stroke-width: .25;");
		StackPane UserCategoryStack = new StackPane();
		UserCategoryBox.setWidth((160+20)*4);
		UserCategoryStack.setAlignment(Pos.CENTER);
		UserCategoryStack.getChildren().setAll(UserCategoryText,UserCategoryBox);
		userVbox.getChildren().setAll(UserCategoryStack,userGP);
		
		VBox diskVbox = new VBox();
		CategoryBox DiskCategoryBox = new CategoryBox();
		Text DiskCategoryText = new Text("Disks");
		DiskCategoryText.setStyle("-fx-font: 30px Tahoma;\r\n" + 
				"    -fx-fill: #004C99;" + 
				"    -fx-stroke: black;\r\n" + 
				"    -fx-stroke-width: .25;");
		StackPane DiskCategoryStack = new StackPane();
		//DiskCategoryBox.widthProperty().bind(diskGP.widthProperty());
		DiskCategoryBox.setWidth(160);
		DiskCategoryStack.setAlignment(Pos.TOP_CENTER);
		DiskCategoryStack.getChildren().setAll(DiskCategoryText,DiskCategoryBox);
		GridPane.setMargin(disk1.diskStack, new Insets(20,0,15,0));
		GridPane.setMargin(disk2.diskStack, new Insets(15,0,20,0));
		diskVbox.getChildren().setAll(DiskCategoryStack,diskGP);

		VBox printerVbox = new VBox();
		CategoryBox PrinterCategoryBox = new CategoryBox();
		Text PrinterCategoryText = new Text("Printers");
		PrinterCategoryText.setStyle("-fx-font: 30px Tahoma;\r\n" + 
				"    -fx-fill: #004C99;" + 
				"    -fx-stroke: black;\r\n" + 
				"    -fx-stroke-width: .25;");
		StackPane PrinterCategoryStack = new StackPane();
		PrinterCategoryBox.setWidth((160+20)*3);
		PrinterCategoryStack.setAlignment(Pos.CENTER);
		PrinterCategoryStack.getChildren().setAll(PrinterCategoryText,PrinterCategoryBox);
		printerVbox.getChildren().setAll(PrinterCategoryStack,printerGP);
		
		
		userGP.add(user1.userStack, 0, 0);
		userGP.add(user2.userStack, 1, 0);
		userGP.add(user3.userStack, 2, 0);
		userGP.add(user4.userStack, 3, 0);
		diskGP.add(disk1.diskStack, 0, 0);
		diskGP.add(disk2.diskStack, 0, 1);
		printerGP.add(Printer1.printerStack, 0, 0);
		printerGP.add(Printer2.printerStack, 1, 0);
		printerGP.add(Printer3.printerStack, 2, 0);
		Button exitButton = new Button("Exit");
		exitButton.setStyle("-fx-background-color: #FD5B5B; -fx-border-color: #FF0000; -fx-border-width: 2px");
		speedButton = new Button(speedStrings[Speed]);
		
		VBox vbox = new VBox(8); // spacing = 8
	    vbox.getChildren().addAll(exitButton,speedButton);
		VBox.setMargin(exitButton, new Insets(10));
		gridpane.add(vbox, 3, 0);
		exitButton.setOnAction(e -> exitProgram());
		speedButton.setOnAction(s -> changeSpeed());
		
		gridpane.add(userVbox, 1, 0);
		gridpane.add(printerVbox, 2, 0);
		gridpane.add(diskVbox, 0, 0);
		//change to Vbox
		/*
		gridpane.add(userGP, 1, 1);
		gridpane.add(printerGP, 2, 1);
		gridpane.add(diskGP, 0, 1);
		*/
		for(Node i:userGP.getChildren()) {
			GridPane.setMargin(i, new Insets(10));
		}
		for(Node i:printerGP.getChildren()) {
			GridPane.setMargin(i, new Insets(10));
		}
		/*
		for(Node i:gridpane.getChildren()) {
			GridPane.setMargin(i, new Insets(10));
		}
		*/
		//Users[0].start();
		
		
		for(int i = 0; i < NUMBER_OF_USERS; ++i){
			Users[i].start();
		}
		
		
		
	}
	
	public static void main(String[] args)throws IOException,InterruptedException{
		launch(args);
		
	}
	public void start(Stage primaryStage1) throws Exception{
		primaryStage = primaryStage1;
		Platform.runLater( () -> {
			startSetup();
		});
		
		

/*
		for(int i = 0; i < NUMBER_OF_USERS; ++i){
			Users[i].start();
			
		}
*/
	}
}
