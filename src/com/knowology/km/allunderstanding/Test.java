package com.knowology.km.allunderstanding;

public class Test {

	public static void main(String[] args) {
		
		Producer t = new Producer();
		
		t.start();
		
		while(true){
			if(t.isStoped){
				System.out.println("线程运行完毕");
				return;
			}
		}
	}

	static class Producer extends Thread{
		private boolean isStoped;
		
		@Override
		public void run() {
			System.out.println("我是aaaa");
			isStoped = true;
		}
	}
}
