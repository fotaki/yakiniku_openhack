package com.fuetrek.fsr.SampleTypeD;
import java.util.ArrayList;
import java.util.List;

public class DecideCommand {

	private final int COMMAND_NUM = 2;
	private final String[] COMMAND_LIST = {"go", "stop"};

	private List<String>[] commandDictionary;

	public DecideCommand() {
		commandDictionary = new List[COMMAND_NUM];
		for(int i = 0; i < COMMAND_NUM; i++){
			commandDictionary[i] = new ArrayList<String>();
		}
		commandDictionary[0].add("いけ");
		commandDictionary[0].add("い");
	}

	/* 音声APIの返り値を辞書に追加する関数 */
	public void addStrToDictionary(int type, String str){
		List<String> target;
		if(0 <= type && type <= COMMAND_NUM){
			target = commandDictionary[type];
		}else{
			return;
		}
		target.add(str);
	}

	/* APIの返り値から、呼び出す命令を判定して該当の値を返す
	 * とりあえず文字列にしておいてあとから調整 */
	public String getCommandType(String str){
		for(int type = 0; type < COMMAND_NUM; type++){
			if(isTargetCommand(type, str)){
				return COMMAND_LIST[type];
			}
		}
		return "none";
	}

	/* 指定タイプのコマンドかどうか判定 */
	private boolean isTargetCommand(int type, String str){
		List<String> target;
		if(0 <= type && type <= COMMAND_NUM){
			target = commandDictionary[type];
		}else{
			return false;
		}
		for (String command : target) {
			if(str.matches(".*" + command + ".*")){
				return true;
			}
		}
		return false;
	}

}
