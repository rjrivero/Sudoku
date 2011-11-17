package es.nextiraone.ing.sudoku.core;

import java.util.Arrays;


public class Cell {

	private int mask;
	private int length;
	private int value;

	public Cell(int mask) {
		assert(mask >= 0 && mask < Cache.VALS);
		this.mask   = mask;
		this.length = Cache.LENGTH[mask];
		this.value  = Cache.VALUE[mask];
	}

	public int[] getValues() {
		int[] options = Cache.OPT[mask];
		int[] values  = Arrays.copyOf(options, options.length);
		for(int index = 0; index < options.length; index++) {
			values[index] += 1;
		}
		return values;
	}

	public int getValue() {
		return value;
	}

	public int getLength() {
		return length;
	}

	public boolean contains(int value) {
		assert(value > 0 && value <= Cache.DIMS);
		return ((mask & Cache.MASK[value-1]) != 0);
	}
}
