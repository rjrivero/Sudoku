package es.nextiraone.ing.sudoku.test;

import org.junit.Before;

import es.nextiraone.ing.sudoku.core.Cell;
import es.nextiraone.ing.sudoku.core.Sudoku;

public abstract class SudokuBase {

	protected Sudoku sudoku;
	protected int size;
	protected Cell cell;

	@Before
	public void setUp() {
		sudoku = new Sudoku();
		cell   = null;
		size   = sudoku.getSize();
		mySetUp();
	}
	
	protected void mySetUp() { }
}
