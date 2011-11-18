package es.nextiraone.ing.sudoku.core;

import java.util.Arrays;


public class Cell {

    /** Celda del sudoku */

	private int cell;
	private int length;
	private int value;

	protected Cell(int cell) {
        /** Construye la celda a partir de una mascara de bits.
         * 
         * La mascara contiene bits a "1" en las posiciones que
         * corresponden a valores posibles en la celda. Por ejemplo,
         * si la celda puede tomar los valores 2, 3 y 5, la mascara es
         * 0x0016 = 0000 0000 0001 0110 (bits 1, 2 y 4)
         */
		assert(cell >= 0 && cell < Cache.VALS);
		this.cell = cell;
		length    = Cache.getLength(cell);
		value     = 0;
		if(length == 1) {
			value = Cache.getValue(cell);
		}
	}

	public int[] getValues() {
        /** Devuelve la lista de valores posibles de la celda */
		int[] options = Cache.getOption(cell);
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
		return ((cell & Cache.getMask(value-1)) != 0);
	}
}
