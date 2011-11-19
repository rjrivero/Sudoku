package es.nextiraone.ing.sudoku.test;

import org.junit.Before;

import es.nextiraone.ing.sudoku.core.Cache;
import es.nextiraone.ing.sudoku.core.Cell;
import es.nextiraone.ing.sudoku.core.Sudoku;

public abstract class SudokuBase {

	protected Cache cache;
	protected Sudoku sudoku;
	protected int size;
	protected Cell cell;

	@Before
	public void setUp() {
		cache  = new Cache(3);
		sudoku = new Sudoku(cache);
		cell   = null;
		size   = cache.DIMS;
		mySetUp();
	}
	
	protected void mySetUp() { }
}
