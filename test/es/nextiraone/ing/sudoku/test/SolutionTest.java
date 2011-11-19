package es.nextiraone.ing.sudoku.test;

import org.junit.Assert;
import org.junit.Test;

import es.nextiraone.ing.sudoku.core.DeadEndException;
import es.nextiraone.ing.sudoku.core.Solutions;


public class SolutionTest extends  SudokuBase {

	Solutions solution;
	
	public void mySetUp() {
		solution = new Solutions(sudoku);
	}
	
	@Test
	public void testHasSolutions() throws DeadEndException {
		Assert.assertEquals(solution.hasNext(), true);
	}

}
