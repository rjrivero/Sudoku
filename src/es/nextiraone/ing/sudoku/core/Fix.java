package es.nextiraone.ing.sudoku.core;

public final class Fix {

	/** Bean que sirve para agregar valores al Sudoku */

	private int offset;
	private int value;

	public Fix(final Cache cache, int offset, int value) {
		/** Fija la celda en el offset dado al valor especificado */
		assert (offset >= 0 && offset < cache.CELLS);
		assert (value >= 1 && value <= cache.DIMS);
		this.offset = offset;
		this.value  = value;
	}

	public Fix(final Cache cache, int row, int col, int value) {
		/** Fija la celda en las coordenadas dadas al valor especificado */
		assert (row >= 0 && row < cache.DIMS);
		assert (col >= 0 && col < cache.DIMS);
		assert (value >= 1 && value <= cache.DIMS);
		this.offset = cache.getOffset(row, col);
		this.value  = value;
	}

	public final int getOffset() {
		return offset;
	}

	public final int getValue() {
		return value;
	}
}
