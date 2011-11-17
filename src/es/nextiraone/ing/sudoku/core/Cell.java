package es.nextiraone.ing.sudoku.core;

import java.util.Arrays;


public class Cell {

    /** Celda del sudoku */

	private int mask;
	private int length;
	private int value;

	protected Cell(int mask) {
        /** Construye la celda a partir de una mascara de bits.
         * 
         * La máscara contiene bits a "1" en las posiciones que
         * corresponden a valores posibles en la celda. Por ejemplo,
         * si la celda puede tomar los valores 2, 3 y 5, la máscara es
         * 0x0016 = 0000 0000 0001 0110 (bits 1, 2 y 4)
         */
		assert(mask >= 0 && mask < Cache.VALS);
		this.mask   = mask;
		this.length = Cache.LENGTH[mask];
		this.value  = Cache.VALUE[mask];
	}

	public int[] getValues() {
        /** Devuelve la lista de valores posibles de la celda */
		int[] options = Cache.OPT[mask];
		int[] values  = Arrays.copyOf(options, options.length);
		for(int index = 0; index < options.length; index++) {
			values[index] += 1;
		}
		return values;
	}

	public int getValue() {
        /** Devuelve el valor al que esta fijada la celda, o 0 si no lo esta */
		return value;
	}

	public int getLength() {
        /** Devuelve la cantidad de valores posibles que le quedan a la celda */
		return length;
	}

	public boolean contains(int value) {
        /** Comprueba si el valor esta ente los posibles de la celda */
		assert(value > 0 && value <= Cache.DIMS);
		return ((mask & Cache.MASK[value-1]) != 0);
	}
}
