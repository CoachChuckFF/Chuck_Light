package chuck;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

import chuck.defines.Filepaths;
import chuck.lighting.Scene;

public class SceneManager {
	
	private ArrayList<Scene> scenes;
	private File sceneFile;
	private Scene currentScene;
	
	int currentIndex;
	
	public static final String filepath = Filepaths.INFO_FULL_FP + Filepaths.SCENE_REL_FP;
	
	public SceneManager(int[] dmxVals){
		sceneFile = new File(filepath);
		scenes = new ArrayList<Scene>();
		currentIndex = 0;
		currentScene = new Scene(dmxVals);
		
		if(!sceneFile.exists())
		{
			//create scene file
			createSceneFile();
		}
		else
		{
			//parse and populate scenes
			parseSceneFile();
		}
	}
	
	public void createSceneFile(){
		try {
			sceneFile.getParentFile().mkdirs();
			sceneFile.createNewFile();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	public void updateSceneFile(){
		String line = "";
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(filepath))) {
			for (Scene scene : scenes) {
				for(int i = 0; i < 513; i++) {
					if(i != 512){
						System.out.print(scene.getDmxVals()[i] + ",");
						line += scene.getDmxVals()[i] + ",";
					} else { 
						System.out.print(scene.getDmxVals()[i]);
						line += scene.getDmxVals()[i] + "\n";
					}
				}
				System.out.println("");
				writer.write(line);
				line = "";
			}
		} catch (IOException e) {
			// failed to write, try to cleanup
			try {
				Files.delete(Paths.get(filepath));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	
	public void parseSceneFile(){
		String line;
		try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
			while ((line = br.readLine()) != null) {
				// use comma as separator
				String[] dmxLine = line.split(",");
				int[] dmxVals = new int[513];
				for(int i = 0; i < 513; i++){
					dmxVals[i] = Integer.parseInt(dmxLine[i]);
				}
				addScene(dmxVals);

			}
		} catch (IOException e) {

		}
	}
	
	public void addScene(int[] dmxVals){
		scenes.add(new Scene(dmxVals));
	}
	
	public void addScene(Scene scene){
		scenes.add(scene);
	}
	
	public void deleteScene() {
		if(getCurrentIndex() != -1){
			scenes.remove(currentIndex);
		}
	}
	
	public void deleteScene(int index){
		if(index >= scenes.size() || index < 0){
			return;
		}
		
		scenes.remove(index);
	}
	
	public Scene getNextScene(){
		if(++currentIndex >= scenes.size() || scenes.size() == 0){
			
			currentIndex = -1;
			return currentScene;
		}
		
		return scenes.get(currentIndex);
		
	}
	
	public Scene getLastScene(){
		if(--currentIndex <= 0 || scenes.size() == 0){

			currentIndex = scenes.size();
			return currentScene;
		}
		
		return scenes.get(currentIndex);
		
	}
	
	public void setCurrentScene(int[] dmxVals){
		currentIndex = -1;
		currentScene.setDmxVals(dmxVals);
	}
	
	public Scene getCurrentScene(){
		return currentScene;
	}
	
	public int getCurrentIndex(){
		if(currentIndex == -1 || currentIndex == scenes.size()){
			return -1; //this means currentScene is on
		}
		
		return currentIndex;
		
	}
	
	public ArrayList<Scene> getSceneArray(){
		return this.scenes;
	}
	
	public int getSceneCount(){
		return this.scenes.size();
	}
	
}
