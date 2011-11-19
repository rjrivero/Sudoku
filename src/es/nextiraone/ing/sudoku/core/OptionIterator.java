package es.nextiraone.ing.sudoku.core;

import java.util.Iterator;


public final class OptionIterator implements Iterable<Integer>, Iterator<Integer> {
	
	/** Iterador sobre los posibles valores de una celda */

	private int cell;
	private final Cache cache;
	private final int offset;

	public OptionIterator(final Cache cache, final int cell) {
		this.cell   = cell;
		this.cache  = cache;
		this.offset = 0;
	}

	public OptionIterator(final Cache cache, final int cell, final int offset) {
		this.cell   = cell;
		this.cache  = cache;
		this.offset = offset;
	}

	@Override
	public final boolean hasNext() {
		return (cell != cache.EMPTYMASK);
	}

	@Override
	public final Integer next() {
		int index = Integer.numberOfTrailingZeros(cell);
		cell = cache.getMaskWithout(cell, index);
		return new Integer(index + offset);
	}

	@Override
	public final void remove() {
		throw new UnsupportedOperationException();
	}

	@Override
	public final Iterator<Integer> iterator() {
		return this;
	}
}
