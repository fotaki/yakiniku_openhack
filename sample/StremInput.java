package sample;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class StremInput {
	static public byte[] req_packets = {
		0x55,0x00,0x0b,0x00,
		0x50,  //  acc def 50 max 100 back 0
		0x50,  // 　左右
		0x00,0x00,0x00,0x00,   // not use
		0x00
	};

	static private String HostName = "10.10.100.254";
	static private int PortNumber = 8899;

	public static void main(String[] args) {
		go_go();
		System.out.println("first");
		try{Thread.sleep(3000);}catch(Exception e){}
		// TODO 自動生成されたメソッド・スタブ
		int tFlag ;
		arenano :
		while(true){
			tFlag = -10;
			tFlag = get_int();
			System.out.println(tFlag);
			if(tFlag == -10){
				break;
			}
			switch (tFlag){
				case 1:
					System.out.println("go");
					go_go();
					break;
				case 2:
					System.out.println("left");
					go_left();
					break;
				case 3:
					System.out.println("right");
					go_right();
					break;
				case 4:
					System.out.println("back");
					go_back();
					break;
				default:
					break arenano;
			}
		}

		go_go();
//		System.out.println(req_packets[4]);
		try{
			Thread.sleep(5000); //3000ミリ秒Sleepする
		}catch(InterruptedException e){}
		stop_stop();
	}

	public static void go_go(){
		req_packets[4] = 0x60;
		req_packets[5] = 0x50;
		req_packets[6] = 0x50;
		req_packets[7] = 0x50;
		req_packets[10] = sum_packets();
		throw_packet();
	}
	public static void go_back(){
		req_packets[4] = 0x40;
		req_packets[5] = 0x50;
		req_packets[6] = 0x50;
		req_packets[7] = 0x50;
		req_packets[10] = sum_packets();
		throw_packet();
	}

	public static void go_right(){
		req_packets[4] = 0x50;
		req_packets[5] = 0x60;
		req_packets[6] = 0x50;
		req_packets[7] = 0x50;
		req_packets[10] = sum_packets();
		throw_packet();
	}

	public static void go_left(){
		req_packets[4] = 0x50;
		req_packets[5] = 0x40;
		req_packets[6] = 0x50;
		req_packets[7] = 0x50;
		req_packets[10] = sum_packets();
		throw_packet();
	}

	public static void stop_stop(){
		req_packets[4] = 0x50;
		req_packets[5] = 0x50;
		req_packets[6] = 0x50;
		req_packets[7] = 0x50;
		req_packets[10] = sum_packets();
		throw_packet();

	}

	private static byte sum_packets(){
		byte sum = 0;
		for(int i = 0; i < req_packets.length; i++){
			sum += req_packets[i];
		}
		return sum;
	}

	public static void throw_packet(){
		for(int i = 0; i < req_packets.length; i++){
			System.out.println(Byte.toString(req_packets[i]));
		}
		System.out.println("throw_packet");
		Socket MyClient;
		try {
			MyClient = new Socket(HostName, PortNumber);
			OutputStream os = MyClient.getOutputStream();
			DataOutputStream dos = new DataOutputStream(os);
			dos.write(req_packets);
			dos.close();
			os.close();
		} catch (UnknownHostException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

		try{
			Thread.sleep(3000);
		}catch(Exception e){}

	}

	public static int get_int(){
		try{
			int a;
			BufferedReader stdReader =
		        new BufferedReader(new InputStreamReader(System.in));
			String b = stdReader.readLine();
			a = Integer.parseInt(b);
			stdReader.close();

			return a;
		}catch(Exception e) {
			return -10;
		}
	}

	public static String get_string(){
		try{
			String a;
			BufferedReader stdReader =
		        new BufferedReader(new InputStreamReader(System.in));

			a = stdReader.readLine();
			stdReader.close();

			return a;
		}catch(Exception e) {
			return "hoge";
		}
	}
}
