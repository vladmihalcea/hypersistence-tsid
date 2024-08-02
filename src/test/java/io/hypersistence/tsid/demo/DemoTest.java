package io.hypersistence.tsid.demo;

import io.hypersistence.tsid.TSID;

public class DemoTest {

	private static final String HORIZONTAL_LINE = "----------------------------------------";

	public static void printList() {
		int max = 100;

		System.out.println(HORIZONTAL_LINE);
		System.out.println("### TSID number");
		System.out.println(HORIZONTAL_LINE);

		for (int i = 0; i < max; i++) {
			System.out.println(TSID.Factory.newInstance1024().generate().toLong());
		}

		System.out.println(HORIZONTAL_LINE);
		System.out.println("### TSID string");
		System.out.println(HORIZONTAL_LINE);

		for (int i = 0; i < max; i++) {
			System.out.println(TSID.Factory.newInstance1024().generate());
		}
	}

	public static void main(String[] args) {
		printList();
	}
}
