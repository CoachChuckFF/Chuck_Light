package chuck;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import chuck.defines.Filepaths;
import chuck.lighting.Scene;

public class SceneManager {
	
	private static final String DEFAULT_SCENE = "default";
	
	private ArrayList<Scene> scenes;
	private Scene currentScene;
	
	private Path scene;
	
	int currentIndex;
	
	public SceneManager(int[] dmxVals) throws IOException {
		scene = Paths.get(Filepaths.SCENE_DIR, DEFAULT_SCENE);
		Files.createDirectories(scene.getParent());
		
		scenes = new ArrayList<Scene>();
		currentIndex = 0;
		currentScene = new Scene(dmxVals);
		
		try {
			Files.createFile(scene);
		} catch (FileAlreadyExistsException ex) {
			parseSceneFile();
		}
	}
	
	public void updateSceneFile() throws IOException {
		String line = "";
		Files.deleteIfExists(scene);
		try (BufferedWriter writer = Files.newBufferedWriter(scene)) {
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
			Files.delete(scene);
			throw e;
		}
	}
	
	private void parseSceneFile() throws IOException {
		String line;
		try (BufferedReader br = Files.newBufferedReader(scene)) {
			while ((line = br.readLine()) != null) {
				// use comma as separator
				String[] dmxLine = line.split(",");
				int[] dmxVals = new int[513];
				for(int i = 0; i < 513; i++){
					dmxVals[i] = Integer.parseInt(dmxLine[i]);
				}
				addScene(dmxVals);

			}
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
