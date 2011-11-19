package es.nextiraone.ing.sudoku.core;


public class Cell {

    /** Celda del sudoku */

	private Sudoku root;
	private int value;
	private final int offset;

	protected Cell(final Sudoku root, final int offset) {
		/** inicializa la celda */
		this.root   = root;
		this.offset = offset;
		this.value  = root.getAt(offset);
	}

	public Iterable<Integer> getValues() {
        /** Devuelve la lista de valores posibles de la celda */
		return root.getCache().getOption(value, 1);
	}

	public int getValue() {
        /** Devuelve el valor al que esta fijada la celda, o 0 si no lo esta */
		return root.getCache().getValue(value);
	}

	public int getLength() {
        /** Devuelve el numero de valores posibles que le quedan a la celda */
		return root.getCache().getLength(value);
	}

	public boolean contains(final int value) {
        /** Comprueba si el valor esta ente los posibles de la celda */
		Cache cache = root.getCache();
		assert(value > 0 && value <= cache.DIMS);
		return cache.doesCellContain(this.value, cache.getMask(value - 1));
	}
	
	public void refresh() {
		this.value = root.getAt(offset);
	}
	
	public void rebind(Sudoku root) {
		this.root = root;
		refresh();
	}
}
