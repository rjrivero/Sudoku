package es.nextiraone.ing.sudoku.core;


public class Cell {

    /** Celda del sudoku */

	private final int cell;
	private final int length;
	private final int value;

	protected Cell(final int cell) {
		/** inicializa la celda */
		assert(cell >= 0 && cell < Cache.VALS);
		this.cell   = cell;
		this.length = Cache.getLength(cell);
		this.value  = (length == 1) ? Cache.getValue(cell) : 0;
	}

	public Iterable<Integer> getValues() {
        /** Devuelve la lista de valores posibles de la celda */
		return Cache.getOption(cell, 1);
	}

	public int getValue() {
        /** Devuelve el valor al que esta fijada la celda, o 0 si no lo esta */
		return value;
	}

	public int getLength() {
        /** Devuelve el numero de valores posibles que le quedan a la celda */
		return length;
	}

	public boolean contains(final int value) {
        /** Comprueba si el valor esta ente los posibles de la celda */
		assert(value > 0 && value <= Cache.DIMS);
		return Cache.doesCellContain(cell, Cache.getMask(value - 1));
	}
}
